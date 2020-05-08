 // Attribution-Noncommercial-Share Alike 3.0 Unported
 // (see more at http://creativecommons.org/licenses/by-nc-sa/3.0/)
// (c) 2009 Maxim Kirillov <max630@gmail.com>
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 import java.util.concurrent.BlockingQueue;
 
 public class Main {
 
 	public static void main(String[] args)
 		throws IOException
 	{
 		final Client client = new Client(args[0], Integer.parseInt(args[1]));
 
 		Init.doInit(client);
 
 		GameIO game_io = new GameIO(client);
 		game_io.execute();
 	}
 }
