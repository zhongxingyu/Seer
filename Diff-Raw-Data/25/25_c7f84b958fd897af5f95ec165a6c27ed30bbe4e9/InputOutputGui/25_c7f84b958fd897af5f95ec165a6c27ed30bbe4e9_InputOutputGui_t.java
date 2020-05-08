 package lab4;
 
 import javax.swing.JOptionPane;
 
 /**
  * This class is responsible for all input and output in the program.
 *
 * @author Jim Lombardo, jlombardo@wctc.edu
  * @version 1.00
  */
 public class InputOutputGui {

    String lastName;
     private NameService nameService;
 
     public InputOutputGui() {
         nameService = new NameService();
     }
 
     public void startConversation() throws InvalidNameException {

         String fullName = JOptionPane.showInputDialog("Enter full name:");
        try {
            lastName = nameService.extractLastName(fullName);
        } catch (InvalidNameException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            System.out.println("Output to console.");
        }
         String msg = "Your last name is: " + lastName;
         JOptionPane.showMessageDialog(null, msg);

     }
 }
