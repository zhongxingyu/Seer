 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.control.OkEncryptionScreenCommand;
 import br.org.indt.ndg.mobile.Resources;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.TextArea;
 import br.org.indt.ndg.lwuit.extended.ChoiceGroup;
 import br.org.indt.ndg.lwuit.extended.ChoiceGroupListener;
 import br.org.indt.ndg.lwuit.extended.DescriptiveField;
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import com.sun.lwuit.events.FocusListener;
 import org.bouncycastle.crypto.Digest;
 import org.bouncycastle.crypto.digests.MD5Digest;
 import br.org.indt.ndg.mobile.settings.KeyHandler;
 
 /**
  *
  * @author roda
  */
 public class EncryptionConfigScreen extends Screen implements ActionListener, ChoiceGroupListener {
 
     private static final int FALSE = 0;
     private static final int TRUE = 1;
     private static final int ON = 0;
     private static final int OFF = 1;
 
     private TextArea passwordLabel = null;
     private ChoiceGroup cg = null;
     private DescriptiveField tfDesc = null;
     private String password = null;
     private boolean useRecordStore = false;
     private boolean encryptionOn = false;
 
     protected void loadData() {
         encryptionOn = AppMIDlet.getInstance().getSettings().getStructure().getEncryption();
         AppMIDlet.getInstance().getSettings().getStructure().setEncryptionConfigured(TRUE);
         AppMIDlet.getInstance().getSettings().writeSettings();
     }
 
     protected void customize() {
         setTitle(Resources.NEWUI_NOKIA_DATA_GATHERING, Resources.ENCRYPTION);
         form.removeAll();
         form.removeAllCommands();
         form.addCommand(OkEncryptionScreenCommand.getInstance().getCommand());
 
         try{
             form.removeCommandListener(this);
         } catch (NullPointerException npe ) {
             //during first initialisation remove throws exception.
             //this ensure that we have registered listener once
         }
         form.addCommandListener(this);
 

         TextArea questionName = UIUtils.createTextArea( Resources.ENCRYPTION_ENABLE,
                                                         NDGStyleToolbox.fontMedium );
         questionName.getStyle().setFgColor( NDGStyleToolbox.getInstance().listStyle.unselectedFontColor );
         form.addComponent(questionName);
 
         String[] choices = new String[2];
         choices[0] = Resources.ON;
         choices[1] = Resources.OFF;
         int initItem;
         if (encryptionOn) {
             initItem = ON;
         } else {
             initItem = OFF;
         }
         cg = new ChoiceGroup(choices, initItem);
         cg.setCgListener(this);
         // for a better scroll
         questionName.setFocusable(true);
         questionName.addFocusListener(new FocusListener() {
 
             public void focusGained(Component c) {
                 cg.requestFocus();
             }
 
             public void focusLost(Component c) {
             }
         });
 
         form.addComponent(cg);
         cg.requestFocus();
 
         passwordLabel = UIUtils.createTextArea( Resources.ENCRYPTION_WITH_PASSWORD,
                                                 NDGStyleToolbox.fontMedium );
         passwordLabel.getStyle().setFgColor( NDGStyleToolbox.getInstance().listStyle.unselectedFontColor );
         passwordLabel.setEditable(false);
         passwordLabel.setFocusable(false);
 
         form.addComponent(passwordLabel);
         passwordLabel.setVisible(false);
 
         tfDesc = new DescriptiveField(16);
         tfDesc.setVisible(false);
         form.addComponent(tfDesc);
     }
 
     public void actionPerformed(ActionEvent evt) {
         Object cmd = evt.getSource();
         if (cmd == OkEncryptionScreenCommand.getInstance().getCommand()) {
            form.removeCommandListener(this);
             password = tfDesc.getText();
             if(encryptionOn) {
                 Digest digest = new MD5Digest();
 
                 if(password != null && !password.equals("")) {
                     byte[] key = password.getBytes();
                     digest.update(key, 0, key.length);
                     byte[] md5 = new byte[digest.getDigestSize()];
                     digest.doFinal(md5, 0);
 
                     StringBuffer sb = new StringBuffer();
                     for (int i = 0; i < md5.length; i++ ) {
                         byte b = md5[i];
                         if(b >= 0 && b <= 15) {
                             sb.append(Integer.toHexString( (int) 0));
                             sb.append(Integer.toHexString( (int) b));
                         }
                         else
                             sb.append(Integer.toHexString((int) (b & 0xff)));
                     }
 
                     if(useRecordStore) {
                         KeyHandler keyHandler = new KeyHandler();
                         keyHandler.storeKey(sb.toString().getBytes());
                         keyHandler.closeStoreKey();
                     }
                     else {
                         AppMIDlet.getInstance().setKey(sb.toString().getBytes());
                         OkEncryptionScreenCommand.getInstance().execute(null);
                     }
                 }
                 else {
                     GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                     GeneralAlert.getInstance().showCodedAlert( Resources.FAILED_REASON, Resources.EMPTY_KEY, GeneralAlert.ALARM );
                 }
             }
             else {
                 OkEncryptionScreenCommand.getInstance().execute(null);
             }
         }
     }
 
     // Listener from ChoiceGroup
     public void itemChoosed(int i) {
         if (i == ON) {
             AppMIDlet.getInstance().getSettings().getStructure().setEncryption(true);
             cg.setSelectedIndex(ON);
 
             passwordLabel.setVisible(true);
             tfDesc.setVisible(true);
             tfDesc.requestFocus();
         } else {
             AppMIDlet.getInstance().getSettings().getStructure().setEncryption(false);
             cg.setSelectedIndex(OFF);
 
             passwordLabel.setVisible(false);
             tfDesc.setVisible(false);
             tfDesc.setText("");
         }
         form.repaint();
 
         AppMIDlet.getInstance().getSettings().writeSettings();
         encryptionOn = AppMIDlet.getInstance().getSettings().getStructure().getEncryption();
     }
 }
