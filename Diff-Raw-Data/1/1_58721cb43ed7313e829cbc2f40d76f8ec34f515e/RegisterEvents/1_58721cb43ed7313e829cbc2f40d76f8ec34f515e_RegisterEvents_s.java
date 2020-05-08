 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package MusicStore;
 
 import BackEnd.*;
 import Gui.*;
 import java.awt.Color;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Jonathan Maderic
  */
 public class RegisterEvents implements Gui.EventImplementation {
 
     public GuiObject MainFrame;
     private User newUser;
     private TextBox username;
     private TextBox password;
     private TextBox name;
     private TextBox address;
     private TextBox credit;
 
     public final void MakeElements() {
         Color ColorScheme = Driver.ColorScheme;
         MainFrame = new Frame("Register", new DPair(0, 0, 0, 0), new DPair(1, 0, 1, 0), ColorExtension.Lighten(ColorScheme, 1), null);
         Frame leftPanel = new Frame("LeftPanel", new DPair(0, 0, 0, 0), new DPair(0, 150, 1, 0), Color.WHITE, MainFrame);
         Frame LoginPanel = new Frame("RegisterPanel", new DPair(0.5, -300, 0.5, -225), new DPair(0, 600, 0, 450), Driver.ColorScheme, MainFrame);
         new TextLabel("UserRegistion", new DPair(0, 215, 0, 40), new DPair(.33, 0, .09, 0), ColorScheme, LoginPanel, "User Registration", 24);
         new TextLabel("Username", new DPair(0, 150, 0, 100), new DPair(.15, 0, .09, 0), ColorScheme, LoginPanel, "User Name:", 14);
         username = new TextBox("Username", new DPair(0, 250, 0, 100), new DPair(.4, 0, 0.09, 0), Color.WHITE, LoginPanel, "Username", 14, new Color(0, 0, 0));
         new TextLabel("Password", new DPair(0, 150, 0, 150), new DPair(.15, 0, .09, 0), ColorScheme, LoginPanel, "Password:", 14);
         password = new TextBox("Password", new DPair(0, 250, 0, 150), new DPair(.4, 0, 0.09, 0), Color.WHITE, LoginPanel, "Password", 14, new Color(0, 0, 0));
         new TextLabel("Name", new DPair(0, 150, 0, 200), new DPair(.15, 0, .09, 0), ColorScheme, LoginPanel, "Name:", 14);
         name = new TextBox("Name", new DPair(0, 250, 0, 200), new DPair(.4, 0, 0.09, 0), Color.WHITE, LoginPanel, "Name", 14, new Color(0, 0, 0));
         new TextLabel("Address", new DPair(0, 150, 0, 250), new DPair(.15, 0, .09, 0), ColorScheme, LoginPanel, "Address:", 14);
         address = new TextBox("Address", new DPair(0, 250, 0, 250), new DPair(.4, 0, 0.09, 0), Color.WHITE, LoginPanel, "Address", 14, new Color(0, 0, 0));
         new TextLabel("Credit", new DPair(0, 150, 0, 300), new DPair(.15, 0, .09, 0), ColorScheme, LoginPanel, "Credit ($):", 14);
         credit = new TextBox("Credit", new DPair(0, 250, 0, 300), new DPair(.4, 0, 0.09, 0), Color.WHITE, LoginPanel, "Credit", 14, new Color(0, 0, 0));
         new TextButton("Back", new DPair(0, 5, 0, 400), new DPair(.07, 0, 0.05, 0), Driver.ColorScheme, LoginPanel, "Back", 16);
         new TextButton("Enter", new DPair(0, 550, 0, 400), new DPair(.07, 0, .05, 0), Driver.ColorScheme, LoginPanel, "Enter", 16);
     }
 
     public RegisterEvents() {
         MakeElements();
     }
 
     @Override
     public void ButtonClicked(GuiObject button, int x, int y) {
         switch (button.GetName()) {
             case "Username":
                 // If the text in the Username text box is "Username" (default), change it when the user clicks.
                 TextBox box = (TextBox) button;
                 if (box.GetText().equals("Username")) {
                     box.SetText("");
                 }
                 break;
             case "Password":
                 // If the text in the Username text box is "Username" (default), change it when the user clicks.
                 TextBox passwordBox = (TextBox) button;
                 if (passwordBox.GetText().equals("Password")) {
                     passwordBox.SetText("");
                 }
                 break;
             case "Name":
                 // If the text in the Username text box is "Username" (default), change it when the user clicks.
                 TextBox nameBox = (TextBox) button;
                 if (nameBox.GetText().equals("Name")) {
                     nameBox.SetText("");
                 }
                 break;
             case "Address":
                 // If the text in the Username text box is "Username" (default), change it when the user clicks.
                 TextBox addressBox = (TextBox) button;
                 if (addressBox.GetText().equals("Address")) {
                     addressBox.SetText("");
                 }
                 break;
             case "Credit":
                 // If the text in the Username text box is "Username" (default), change it when the user clicks.
                 TextBox creditBox = (TextBox) button;
                 if (creditBox.GetText().equals("Credit")) {
                     creditBox.SetText("");
                 }
                 break;
             case "Back":
                 Driver.SetFrame("Login");
                 break;
             case "Enter":
                 System.out.println("1" + username.GetText() + "1");
                 if (username.GetText().equals("Username") || password.GetText().equals("Password") || name.GetText().equals("Name") ||
                         address.GetText().equals("Address") || credit.GetText().equals("Credit") || username.GetText().equals("") || 
                         password.GetText().equals("") || name.GetText().equals("") || address.GetText().equals("") || credit.GetText().equals("")) {
                     JOptionPane.showMessageDialog(null, "Please enter valid values", "invaild value entered", JOptionPane.WARNING_MESSAGE);
                 } else {
                     newUser = new User(username.GetText(), password.GetText(), name.GetText(), address.GetText(), Double.parseDouble(credit.GetText()), false);
                     if (DataLoader.addUserToList(newUser)) {
                        DataLoader.saveToFile();
                         Driver.SetFrame("Login");
                     } else {
                         JOptionPane.showMessageDialog(null, "This user already exists - Please enter a new user", "User Exists", JOptionPane.WARNING_MESSAGE);
                         username.SetText("");
                         password.SetText("");
                         name.SetText("");
                         address.SetText("");
                         credit.SetText("");
                     }
                 }
                 break;
             default:
                 System.out.println("Unidentified item (" + button.getClass().getName() + ") clicked: " + button.GetName());
         }
     }
 
     @Override
     public void MouseDown(GuiObject button, int x, int y) {
     }
 
     @Override
     public void MouseUp(GuiObject button, int x, int y) {
     }
 
     @Override
     public void MouseMove(GuiObject button, int x, int y) {
     }
 }
