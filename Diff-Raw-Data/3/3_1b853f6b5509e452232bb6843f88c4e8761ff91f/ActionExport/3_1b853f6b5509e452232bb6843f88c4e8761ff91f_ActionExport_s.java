 package nl.nikhef.jgridstart.gui;
 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.border.EmptyBorder;
 import javax.swing.plaf.basic.BasicFileChooserUI;
 
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateSelection;
 import nl.nikhef.jgridstart.gui.util.ErrorMessage;
 import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
 import nl.nikhef.jgridstart.util.PasswordCache;
 import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
 
 /** Export selected certificate to PKCS#12/PEM file */
 public class ActionExport extends CertificateAction {
     
     public ActionExport(JFrame parent, CertificateSelection s) {
 	super(parent, s);
 	putValue(NAME, "Export...");
 	putValue(MNEMONIC_KEY, new Integer('E'));
 	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
 	URLLauncherCertificate.addAction("export", this);
     }
     
     @Override
     public void actionPerformed(ActionEvent e) {
 	logger.finer("Action: "+getValue(NAME));
 	final JFileChooser chooser = new CertificateFileChooser(false);
 	
 	// embed in frame with password selection fields
 	final JDialog dlg = new JDialog(parent,
 		"Export the currently selected certificate");
 
 	final JPanel hpane = new JPanel();
 	hpane.setLayout(new BoxLayout(hpane, BoxLayout.X_AXIS));
 	final JCheckBox check = new JCheckBox("Use private key password for the exported file");
 	check.setMnemonic('p');
 	check.setSelected(true);
 	hpane.add(check);
 	hpane.add(Box.createHorizontalGlue());
 	
 	JPanel pane = customFileChooser(dlg, chooser,
 		new AbstractAction("Export") {
         	    public void actionPerformed(ActionEvent e) {
         		try {
         		    File f = chooser.getSelectedFile();
         		    char[] pw = null;
         		    // request password if wanted
         		    if (!check.isSelected()) {
         			pw = PasswordCache.getInstance().getForEncrypt(
         				"PKCS#12 key password for "+f.getName(),
         				f.getCanonicalPath());
         		    }
         		    doExport(e, f, pw);
         		} catch (PasswordCancelledException e1) {
         		    /* do nothing */
         		} catch (Exception e1) {
         		    ErrorMessage.error(parent, "Export error", e1);
         		}
         		dlg.dispose();
         	    }
 		}
 	);
 	pane.add(hpane);
 	
 	dlg.setName("jgridstart-export-file-dialog");
 	dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 	dlg.pack();
 	dlg.setVisible(true);
     }
     
     /** Export the current certificate to a file
      * 
      * @param e originating event
      * @param f File to export to
      * @param pw password to use, or {@code null} to use private key password
      */
     public void doExport(ActionEvent e, File f, char[] pw) {
 	CertificatePair cert = getCertificatePair();
 	logger.info("Exporting certificate "+cert+" to: "+f);
 	try {
 	    cert.exportTo(f, pw);
 	} catch (PasswordCancelledException e1) {
 	    // do nothing
 	} catch (Exception e1) {
 	    logger.severe("Error exporting certificate "+f+": "+e1);
 	    ErrorMessage.error(findWindow(e.getSource()), "Export failed", e1);
 	}
     }
     
     protected static JPanel customFileChooser(final JDialog dlg, final JFileChooser chooser, final Action action) {
 	Insets insets = null;
 	if (chooser.getBorder() instanceof EmptyBorder)
 	    insets = ((EmptyBorder)chooser.getBorder()).getBorderInsets();
 	// disable buttons because we'll roll our own
 	chooser.setControlButtonsAreShown(false);
 	chooser.setApproveButtonText((String)action.getValue(Action.NAME));
 	// dialog panel with chooser on top
 	JPanel panel = new JPanel();
 	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 	panel.add(chooser);
 	// then container panel for our extra elements
 	JPanel contentpane = new JPanel();
 	contentpane.setLayout(new BoxLayout(contentpane, BoxLayout.Y_AXIS));
 	if (insets!=null) contentpane.setBorder(BorderFactory.createEmptyBorder(0, insets.left, 0, insets.right));
 	panel.add(contentpane);
 	// and the bottom buttons
 	JPanel btns = new JPanel();
 	if (insets!=null) btns.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
 	btns.add(Box.createHorizontalGlue());
 	btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
 	final JButton activate = new JButton(new AbstractAction((String)action.getValue(Action.NAME)) {
 	    public void actionPerformed(ActionEvent e) {
 		chooser.approveSelection();
 	    }
 	});
 	btns.add(activate);
 	final JButton cancel = new JButton(new AbstractAction("Cancel") {
 	    public void actionPerformed(ActionEvent e) {
 		chooser.cancelSelection();
 	    }
 	});
 	btns.add(cancel);
 	panel.add(btns);
 
 	dlg.getContentPane().add(panel);
 	dlg.getRootPane().setDefaultButton(activate);
 	dlg.setModal(true);
 	
 	// hook filechooser actions to our own actions
 	chooser.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 		if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
 		    // workaround for JFileChooser bug, see
 		    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4528663
 		    if (chooser.getUI() instanceof BasicFileChooserUI) {
 		        BasicFileChooserUI ui = (BasicFileChooserUI)chooser.getUI();
		        chooser.setSelectedFile(new File(ui.getFileName()));
 		    }
 		    action.actionPerformed(e);
 		}
 		dlg.removeAll();
 		dlg.dispose();
 	    }
 	});
 	
 	return contentpane;
     }
 }
