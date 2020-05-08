 package bisbat;
 
 public class Bisbat extends Thread {
 
 	public Connection c;
 	public String name = "Bisbat";
 	public String password = "alpha";
 	private String prompt;
 	public Bisbat() {
		
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Bisbat alpha = new Bisbat();
 		alpha.start();
 		 
 	}
 	public void run() {
 		c = new Connection(this, "www.mortalpowers.com", 4000);
 	 	login();
 	 	explore();
 	}
 	public void login() {
 		c.send(name + "\n" + password + "\n");
 		setUpPrompt();
 	}
 	public void explore() {
 		c.send("look");
 	}
 	public void setUpPrompt() {
		prompt = "!!!BISBAT!!!";
 		c.send("prompt " + prompt);
 		
 	}
 	public String getPrompt() {
 		return prompt;
 	}
 
 }
