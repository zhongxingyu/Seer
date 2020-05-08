 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui;
 
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.util.DefaultUnitConverter;
 
 import eu.lp0.cursus.app.Main;
 import eu.lp0.cursus.ui.component.Displayable;
 import eu.lp0.cursus.ui.preferences.WindowAutoPrefs;
 import eu.lp0.cursus.util.Constants;
 import eu.lp0.cursus.util.Messages;
 
 public class AboutDialog extends JDialog implements Displayable, ActionListener {
 	private static final String TEXT_SPLIT = "\n------------------------------------------------------------------------\n\n"; //$NON-NLS-1$
 
 	protected final Logger log = LoggerFactory.getLogger(getClass());
 
 	private JButton btnClose;
 
 	private WindowAutoPrefs prefs = new WindowAutoPrefs(this);
 
 	public AboutDialog(Frame owner) {
 		super(owner, true);
 
 		initialise();
 	}
 
 	public void display() {
 		assert (SwingUtilities.isEventDispatchThread());
 
 		prefs.display(getOwner());
 	}
 
 	private void initialise() {
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		setTitle(String.format(Messages.getString("about.title"), Constants.APP_DESC)); //$NON-NLS-1$
 		DefaultUnitConverter duc = DefaultUnitConverter.getInstance();
 
 		FormLayout layout = new FormLayout("2dlu, pref, fill:pref:grow, max(30dlu;pref), 2dlu", //$NON-NLS-1$
 				"2dlu, max(15dlu;pref), 2dlu, max(15dlu;pref), 2dlu, fill:max(100dlu;pref):grow, 2dlu, max(16dlu;pref), 2dlu"); //$NON-NLS-1$
 		getContentPane().setLayout(layout);
 
 		JLabel lblName = new JLabel(Constants.APP_NAME + ": " + Messages.getString("about.description")); //$NON-NLS-1$ //$NON-NLS-2$
 		getContentPane().add(lblName, "2, 2, 3, 1"); //$NON-NLS-1$
 
 		JButton btnLink = new JButton();
 		btnLink.setText("<html><body><a href=\"" + Constants.APP_URL + "\">" + Constants.APP_URL + "</a></body></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		btnLink.setBorderPainted(false);
 		btnLink.setRolloverEnabled(false);
 		btnLink.setFocusPainted(true);
 		btnLink.setOpaque(true);
 		btnLink.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (Desktop.isDesktopSupported()) {
 					try {
 						Desktop.getDesktop().browse(new URI(Constants.APP_URL));
 					} catch (IOException e) {
 						log.error("Unable to open browser", e); //$NON-NLS-1$
 					} catch (URISyntaxException e) {
 						log.error("Unable to open browser", e); //$NON-NLS-1$
 					}
 				}
 			}
 		});
 		getContentPane().add(btnLink, "2, 4"); //$NON-NLS-1$
 
 		JScrollPane scrCopying = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		getContentPane().add(scrCopying, "2, 6, 3, 1"); //$NON-NLS-1$
 
 		JTextArea txtCopying = new JTextArea(loadResources("COPYRIGHT", "LICENCE")); //$NON-NLS-1$ //$NON-NLS-2$
 		txtCopying.setFont(Font.decode(Font.MONOSPACED));
 		txtCopying.setEditable(false);
 		scrCopying.setViewportView(txtCopying);
 		scrCopying.setPreferredSize(new Dimension(scrCopying.getPreferredSize().width, duc.dialogUnitYAsPixel(100, scrCopying)));
 
 		btnClose = new JButton(Messages.getString("dialog.ok")); //$NON-NLS-1$
 		btnClose.addActionListener(this);
 		getContentPane().add(btnClose, "4, 8"); //$NON-NLS-1$
 
 		getRootPane().setDefaultButton(btnClose);
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), btnClose.getText());
 		getRootPane().getActionMap().put(btnClose.getText(), new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				btnClose.doClick();
 			}
 		});
 
 		pack();
 		setMinimumSize(getSize());
 		setSize(getSize().width, getSize().height * 3 / 2);
 		btnClose.requestFocusInWindow();
 	}
 
 	private String loadResources(String... names) {
 		StringBuilder sb = new StringBuilder();
 		Iterator<String> it = Arrays.asList(names).iterator();
 		while (it.hasNext()) {
 			String name = it.next();
 
			String tmp;
 			try {
				tmp = IOUtils.toString(Main.class.getResource(name), "UTF-8"); //$NON-NLS-1$
 			} catch (IOException e) {
 				log.error("Unable to load " + name + " resource", e); //$NON-NLS-1$ //$NON-NLS-2$
				tmp = "Error loading " + name + " resource: " + e.getLocalizedMessage(); //$NON-NLS-1$ //$NON-NLS-2$
 			}
			sb.append(tmp);
 
 			if (it.hasNext()) {
 				sb.append(TEXT_SPLIT);
 			}
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == btnClose) {
 			doClose();
 		}
 	}
 
 	private void doClose() {
 		dispose();
 	}
 }
