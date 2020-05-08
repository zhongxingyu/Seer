 package edu.usc.chla.vpicu.explorer.newui.connection;
 
 import java.awt.GridBagConstraints;
 
 import edu.usc.chla.vpicu.explorer.BaseProvider;
 import edu.usc.chla.vpicu.explorer.H2Provider;
 
 public class H2Tab extends ConnectionTab {
 
   private static final long serialVersionUID = 1L;
 
   public static final String DBFILE = "Database File";
   public static final String USERNAME = "Username";
   public static final String PASSWORD = "Password";
 
   private final FileChooserButton chooser;
 
   public H2Tab() {
     addLabel(DBFILE);
     chooser = new FileChooserButton(DBFILE);
     chooser.setSuffix(".h2.db");
     add(chooser, gbc(1,row++,1,1,1,0,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL));
 
     addInputRow(USERNAME);
     addLabel(PASSWORD);
     addPasswordField(PASSWORD);
 
     addVerticalGlue();
   }
 
   @Override
   public BaseProvider getProvider() {
     return new H2Provider(chooser.getSelectedFile(),
         fields.get(USERNAME).getText(),
         fields.get(PASSWORD).getText());
   }
 
 }
