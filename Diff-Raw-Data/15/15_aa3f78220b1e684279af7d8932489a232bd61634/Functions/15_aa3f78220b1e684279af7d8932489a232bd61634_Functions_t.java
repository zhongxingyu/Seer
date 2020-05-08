 package game;
 
 import javax.swing.JOptionPane;
 
 public class Functions {
 	
 	public static Functions functions;
 	
 	public static Functions getInstance(){
 		if (functions == null){
 			functions = new Functions();
 		}
 		return functions;
 	}
 	
 	boolean writing;
 	String out = "";
	int sleep = 50;
 
 	public void write(String text, boolean keep){
 		
 		if(keep == false){
 			out = "";
 		}
 		
		for (int x = 0; x < text.length() - 1; x++){
			out += text.substring(x, x + 1);
			try{
				Thread.sleep(sleep);
			} catch (Exception e){}
		}
 		
 	}
 	
 	public boolean isWriting() {
 		return writing;
 	}
 
 	public void setWriting(boolean writing) {
 		this.writing = writing;
 	}
 
 	public String getOut() {
 		return out;
 	}
 
 	public void setOut(String out) {
 		this.out = out;
 	}
 
 	
 }
