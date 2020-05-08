 import processing.core.PApplet;
 import processing.core.PConstants;
 
 import controlP5.*;
 
 public class LoginController implements ControlListener {
   private PApplet parent;
   private ControlP5 controlP5;
   private Textlabel title_lbl1, title_lbl2;
   private ListBox plist;
   private Textfield host, port, login, pass;
   private Button submit;
   private Textlabel status;
   private POP3Profiles prof;
   
 
   LoginController(PApplet parent) {
     this.controlP5 = new ControlP5(parent);
     this.controlP5.addListener(this).setAutoDraw(false);
     this.parent = parent;
     setup();
   }
   public void setup() {
     //TODO refactor / cleanup this method
     ControlFont cf = new ControlFont(parent.createFont("Arial", 16));
     controlP5.setFont(cf);
 		
     cf.setSize(72);
     title_lbl1 = controlP5.addTextlabel("tree", "TREE");
     cf.setSize(16);
     title_lbl2 = controlP5.addTextlabel("tree2", "TREE Representation of Everlasting Emails");
 		
     int w1 = ControlFont.getWidthFor(title_lbl1.getValueLabel().getText(), title_lbl1.getValueLabel(), parent);
     int w2 = ControlFont.getWidthFor(title_lbl2.getValueLabel().getText(), title_lbl2.getValueLabel(), parent);
 		
     title_lbl1.setPosition((parent.width - w1) / 2.0f, 20.0f);
     title_lbl2.setPosition((parent.width - w2) / 2.0f, 100.0f);
 		
     controlP5.addTextlabel("plistlabel", "Profile list:", 20, 150);
     controlP5.addTextlabel("hostlabel", "Hostname:", 20, 250);
     controlP5.addTextlabel("portlabel", "Port:", 20, 300);
     controlP5.addTextlabel("loginlabel", "Username:", 20, 350);
     controlP5.addTextlabel("passwordlabel", "Password: ", 20, 400);
     
     plist = controlP5.addListBox("profilelist", 110, 150, 500, 80);
     host = controlP5.addTextfield("hostbox", 110, 240, 500, 40);
     port = controlP5.addTextfield("portbox", 110, 290, 500, 40);
     login = controlP5.addTextfield("loginbox", 110, 340, 500, 40);
     pass = controlP5.addTextfield("passwordbox", 110, 390, 500, 40);
     cf.setSize(36);
     submit = controlP5.addButton("submitbtn");
     cf.setSize(16);
     pass.setPasswordMode(true);
 		
     submit.setCaptionLabel("Login");
     submit.getCaptionLabel().align(PConstants.CENTER, PConstants.CENTER);
     submit.setSize(200, 70);
     submit.setPosition((parent.width - submit.getWidth()) / 2.0f, 440);
     submit.setColorForeground(0xff606060);
     submit.setColorBackground(0xff404040);
 		
     plist.getCaptionLabel().hide();
     host.getCaptionLabel().hide();
     port.getCaptionLabel().hide();
     login.getCaptionLabel().hide();
     pass.getCaptionLabel().hide();
 		
     plist.setColorForeground(0xff606060);
     plist.setColorBackground(0xff404040);
     host.setColorBackground(0xff404040);
     port.setColorBackground(0xff404040);
     login.setColorBackground(0xff404040);
     pass.setColorBackground(0xff404040);
 		
     prof = new POP3Profiles();
     plist.hideBar();
     plist.addItem("-- Select an item --", 0);
     for (int i = 0; i < prof.getLength(); i++)
     {
       plist.addItem(prof.nameAt(i), i + 1);
     }
 		
    controlP5.mapKeyFor(new ControlKey() { @Override
	public void keyEvent() {
       tab_focus(true);
    }}, PConstants.TAB);
 
    controlP5.mapKeyFor(new ControlKey() { @Override
	public void keyEvent() {
       tab_focus(false);
    }}, PConstants.SHIFT, PConstants.TAB);
 
     status = controlP5.addTextlabel("statusbar", "Ready", 10, 530);
   }
 
   // forward = true means tab forward
   // forward = false means tab backward
   private void tab_focus(boolean forward) {
     Textfield[] text_fields = {host, port, login, pass};
     // to tell if we should set the first item in focus
     boolean text_field_not_found = true;
 
     for (int i = 0; i < text_fields.length; i++) {
       Textfield text_field = text_fields[i];
       if (text_field.isFocus()) {
         text_field.setFocus(false);
         // TODO refactor idx
         int idx = (text_fields.length + i + (forward ? 1 : -1)) % text_fields.length;
         text_fields[idx].setFocus(true);
         text_field_not_found = false;
         break;
       }
     }
 
     if (text_field_not_found) {
       text_fields[0].setFocus(true);
     }
   }
 
   public void display() {
     controlP5.draw();
   }
 
  @Override
public void controlEvent(ControlEvent theEvent)
   {
     if (theEvent.isGroup())
     {
       if (theEvent.getGroup().equals(plist))
       {
         int idx = (int) plist.getValue();
         if (idx != 0)
         {
           idx--;
           host.setText(prof.hostnameAt(idx));
           port.setText(String.valueOf(prof.portAt(idx)));
           
           if (login.getText().isEmpty() || pass.getText().isEmpty())
           {
             host.setFocus(false);
             port.setFocus(false);
             login.setFocus(login.getText().isEmpty());
             pass.setFocus(!login.getText().isEmpty());
           }
         }
       }
     }
     else if (theEvent.isController())
     {
       if (theEvent.getController().equals(submit))
       {
         String shost = host.getText();
         int iport = Integer.parseInt(port.getText());
         String suser = login.getText();
         String spass = pass.getText();
         (new EmailDownloadThread(shost, iport, suser, spass, status)).start();
       }
     }
   }
 }
