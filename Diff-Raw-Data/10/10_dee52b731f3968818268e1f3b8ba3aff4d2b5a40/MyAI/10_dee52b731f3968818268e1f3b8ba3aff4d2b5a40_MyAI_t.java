 package compo.spacebumper.java;
 
 import java.io.IOException;
 
 public class MyAI extends Client{
	public void RunAi()throws IOException, InterruptedException 
 	{
 		setName("StupidAI");
		System.out.println("Started! Woo!");
 		char[][] map = WaitForMap();
 		while(Connected())
 		{
 			GameState state = this.getState();
 		}
 	}
 	public MyAI() throws IOException, InterruptedException {
         super();
     }
 	public static void main(String[] args)
 	{
 		System.out.println("Started");
 		try {
 			MyAI ai = new MyAI();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		while(true)
 		{
 			
 		}
 	}
 }
