 
 /**
  * @author yuchaozh
  *ûѭܣֹͣʱ=˳ٴμӣ
  */
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class Execute 
 {
 	public static void main(String args[]) throws IOException, InterruptedException
 	{
 		Timer timer = new Timer(true);
 		Execute execute = new Execute();
		timer.schedule(new task(), 60*1000);
 		//timer.schedule(finish(), 60*1000);
 		execute.traverse();
 		execute.watch();
 		
 	}
 	
 	public void traverse() throws IOException
 	{
 		//Path startPath = Paths.get("c:/"); 
 		Path startPath = Paths.get("c:/test_file2");
 		TraverseDir traversedir = new TraverseDir();
 		try 
 		{  
 	        Files.walkFileTree(startPath, traversedir);  
 	    } 
 		catch (IOException e) 
 	    {  
 	        System.err.println(e);  
 	    }
 		//System.out.println("dircount: "+TraverseDir.dircount);
 		//System.out.println("~~~~~~~~~~~~~~~~~ ");
 		System.out.println("DirPath Count: "+TraverseDir.dirpath.size());
 		//txtĵļе·
 		FileWriter fw = new FileWriter("C:/DetectedDir.txt");
 		for(int i = 0; i < TraverseDir.dirpath.size(); i++)
 		{
 			fw.write(TraverseDir.dirpath.get(i)+ "\r\n");
 		}
 		//رtxt
 		fw.close();
 	}
 	
 	public void watch() throws IOException, InterruptedException
 	{
 		Watch watch = new Watch();
 		System.out.println("ӵļУ ");
 		for(int i = 0; i < TraverseDir.dirpath.size(); i++)
 		{
 			Object url = "";
 			//System.out.println(TraverseDir.dirpath.size());
 			 url = TraverseDir.dirpath.get(i);
 			 //System.out.println(url.toString());
 			 watch.path.add(Paths.get(url.toString()));
 		}
 		
 		for (int i =0; i < watch.path.size(); i++)
 		{
 			System.out.print(watch.path.get(i) + "; ");
 		}
 		System.out.println();
 		watch.SetPath();
 	}
 }
 
 /****************˳е*******************************/
 class task extends TimerTask
 {
 	public void run()
 	{
 		Watch.quite = -1;
 		//Watch.key.cancel();
 		Watch.valid = false;
 		//System.out.println("quite is setted! "+ Watch.quite);
 		System.out.println("Detection END!");
 		try {
 			Watch.fw.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.exit(-1);
 	}
 }
