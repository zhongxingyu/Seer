 /**
  * 
  */
 package mgtsys;
 
 /**
  * @author Yun Wang
  *
  */
 public class main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                new start(false).setVisible(true);
             	//new login_incorrect().setVisible(true);
                 //new student_view("ssbudha").setVisible(true);
             }
         });
 	}
 
 }
