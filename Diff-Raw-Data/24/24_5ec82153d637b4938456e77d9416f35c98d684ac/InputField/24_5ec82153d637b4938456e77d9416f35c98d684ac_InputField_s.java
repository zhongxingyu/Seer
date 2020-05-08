 
 package SeljeIRC;
 
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.text.BadLocationException;
 
 /**
  *
  * @author wbserver
  */
 public class InputField extends JPanel {
     private JLabel label = new JLabel();
     private JTextField inputField;
     private String channel;
     private int tabType;
     
     ConnectionHandler connection;
     
     private Pattern inputCommandFinderPattern = Pattern.compile("^/\\w+");
 
     public InputField(ConnectionHandler con, String cha, int TabType){
         super();
         channel = cha;
         connection = con;
         tabType = TabType;
         //FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
         BorderLayout layout = new BorderLayout();
         setLayout(layout);
 
         
         
         label = new JLabel(cha);
             
         add(label,BorderLayout.WEST);
         
         inputField = new JTextField();
         
         
         add(inputField,BorderLayout.CENTER);
         JButton button = new JButton(I18N.get("inputfield.send"));
         add(button,BorderLayout.EAST);
         
         
         button.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent ae){
                 
                 /*
                  * sending input to approporiate screen
                  */
             	postTextToIRC(inputField);
                 
                 
             }
         });
         
         inputField.addActionListener(new ActionListener(){
 
             public void actionPerformed(ActionEvent ae){
                 
                 /*
                  * sending input to approporiate screen
                  */
             	postTextToIRC(inputField);
             	
                 
                 
             }
         });
 
 
     }
     /**
      * Types the text to the server, and resets the text in the textboks
      * @param txtInputField InputField with text to send
      */
     
     private void postTextToIRC(JTextField txtInputField){
     	int typeOfMessage = tabType;
     	String textToPost = txtInputField.getText();
     	
     	Matcher inputCommandFinder = inputCommandFinderPattern.matcher(textToPost);
     	
         if(inputCommandFinder.find())
         	typeOfMessage = SingleTab.STATUS;
     	
         try{
         	switch(typeOfMessage){
     			case SingleTab.PRIVATE: connection.sayToPrivate(textToPost, channel); break;
     			case SingleTab.CHANNEL: connection.sayToChannel(textToPost, channel); break; 
    			default: connection.sayToServer(textToPost); break;
 
         	}
         }catch(BadLocationException e){
         	System.err.println("System error" + e.getMessage());
         }
         
     	txtInputField.setText("");
     
     	
     }
     public void setFocusOnField(){
         inputField.requestFocus();
     }
 }
