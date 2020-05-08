 package org.bh.gui.swing;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Locale;
 
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.WindowConstants;
 
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformController;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.BHTranslator;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 public class BHOptionDialog extends JDialog implements ActionListener,
 		IPlatformListener {
 
 	private CellConstraints cons;
 
 	private BHDescriptionLabel language;
 	private JComboBox combo;
 	private BHButton apply;
 
 	public BHOptionDialog() {
 		this.setProperties();
 
 		// setLayout to the status bar
 		String rowDef = "p";
 		String colDef = "0:grow(0.05),0:grow(0.3),0:grow(0.3),0:grow(0.3),0:grow(0.05)";
 		setLayout(new FormLayout(colDef, rowDef));
 		cons = new CellConstraints();
 
 		// create select language components
 		language = new BHDescriptionLabel("MoptionsLanguage");
 
 		combo = new JComboBox(Services.getTranslator().getAvailableLocales());
 		combo.setRenderer(new BHLanguageRenderer());
 		combo.setSelectedItem(Services.getTranslator().getLocale());
 
 		apply = new BHButton("Bapply");
 		apply.setText(BHTranslator.getInstance().translate("Bapply"));
 		apply.addActionListener(this);
 
 		// add components
 		add(language, cons.xywh(2, 1, 1, 1, "right,center"));
 		add(combo, cons.xywh(3, 1, 1, 1));
 		add(apply, cons.xywh(4, 1, 1, 1));
 
 	}
 
 	private void setProperties() {
 		this.setTitle(BHTranslator.getInstance().translate("MoptionsDialog"));
 		this.setSize(400, 200);
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		this.setResizable(false);
 		this.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		PlatformController.preferences.put("language", ((Locale) combo
 				.getSelectedItem()).getLanguage());
 		Services.getTranslator().setLocale((Locale) combo.getSelectedItem());
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			Services.getTranslator().translate("MoptionsDialog");
 			combo.revalidate();
 			combo.repaint();
 		}
 	}
 
 	/**
 	 * Renderer for Locales in Options combobox.
 	 * 
 	 * <p>
 	 * This class implementing the <code>ListCellRenderer</code> Interface
 	 * provides a stamp like method for the Languages.
 	 * 
 	 * @author klaus
 	 * @version 1.0, Jan 7, 2010
 	 * 
 	 */
 	public class BHLanguageRenderer implements ListCellRenderer {
 		
 		/**
 		 * <code>JLabel</code> to be used for every item.
 		 */
 		private JLabel l = null;
 
 		/**
 		 * Default constructor.
 		 */
 		public BHLanguageRenderer() {
 			l = new JLabel();
			l.setPreferredSize(new Dimension(l.getPreferredSize().width, 20));
 		}
 		/**
 		 * Method to return a Component for each item of the List.
 		 */
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 
 			l.setText(((Locale) value).getDisplayLanguage(Services
 					.getTranslator().getLocale()));
 			return l;
 
 		}
 
 	}
 }
