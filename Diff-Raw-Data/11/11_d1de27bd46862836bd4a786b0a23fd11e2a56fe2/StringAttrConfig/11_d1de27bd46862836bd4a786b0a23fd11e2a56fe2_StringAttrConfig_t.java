 
 package org.paxle.desktop.impl.dialogues.settings;
 
 import javax.swing.JComponent;
 import javax.swing.JPasswordField;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import org.osgi.service.metatype.AttributeDefinition;
 import org.paxle.desktop.impl.Messages;
 import org.paxle.desktop.impl.event.MultipleChangesListener;
 
 class StringAttrConfig extends AbstractAttrConfig<String> {
 	
 	private JTextField comp;
 	private boolean isPassword;
 	
 	public StringAttrConfig(final AttributeDefinition ad) {
 		super(ad);
 	}
 	
 	@Override
 	protected JTextArea createDescription(String desc) {
 		isPassword = ad.getID().toLowerCase().contains("password"); //$NON-NLS-1$
 		final JTextArea a = super.createDescription(desc);
 		if (isPassword) try {
 			final Document doc = a.getDocument();
			doc.insertString(doc.getLength(), Messages.getString("settingsPanel.attrString.pwdSavedUnencrypted"), null); //$NON-NLS-1$
 		} catch (BadLocationException e) { e.printStackTrace(); }
 		return a;
 	}
 	
 	@Override
 	protected JComponent createOptionComp(Object value, final MultipleChangesListener mcl) {
 		comp = (isPassword) ? new JPasswordField() : new JTextField();
 		mcl.addComp2Monitor(comp);
 		return comp;
 	}
 	
 	@Override
 	public String getValue() {
 		return comp.getText();
 	}
 	
 	@Override
 	protected String fromObject(Object value) {
 		return value.toString();
 	}
 	
 	@Override
 	public void setValue(String value) {
 		comp.setText(value);
 	}
 }
