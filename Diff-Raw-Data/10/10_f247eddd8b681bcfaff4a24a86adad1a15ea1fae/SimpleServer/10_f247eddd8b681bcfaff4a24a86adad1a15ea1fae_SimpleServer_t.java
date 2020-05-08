 import javax.swing.JFrame;
 
 
 public class SimpleServer {
 	
 	public void main(String[] args){
 
 		//Create new Server Application
 		Server application = new Server();
 		
 		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.runServer();
 	}
 
 }
