 
 /**
  * @author yuchaozh
  *
  */
 
 import java.net.URI;
 import java.nio.*;
 import java.io.*;
 import java.nio.file.StandardWatchEventKinds.*;
 import java.nio.file.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 class Watch 
 {
 	public WatchService service;
 	public ArrayList<Path> path;
 	public static int quite = 1;
 	public static boolean valid;
 	public static WatchKey key;
 	public static FileWriter fw;
 
 	//캯
 	public Watch() throws IOException
 	{
 		service = FileSystems.getDefault().newWatchService();
 		//һ
 		path = new ArrayList<Path>();
 	}
 	
 	static <T> WatchEvent<Path> cast(WatchEvent<?> event) {   
 		return (WatchEvent<Path>)event;   
 	}
 	
 	public void  SetPath() throws IOException, InterruptedException
 	{
 		System.out.println("ĴСΪ "+path.size());
 		for (int i=0; i < this.path.size(); i++)
 		{
 			this.path.get(i).register(service,
 		            StandardWatchEventKinds.ENTRY_CREATE,
 		            StandardWatchEventKinds.ENTRY_MODIFY,
 		            StandardWatchEventKinds.ENTRY_DELETE);  // Register the directory
 		}
 		begin();
 	}
 	
 	public void begin() throws IOException, InterruptedException
 	{
 		Path child = null;
 		System.out.println("");
 		fw = new FileWriter("C:/log.txt");
 		SimpleDateFormat dataformate = new SimpleDateFormat("yyyy-MM-dd|hh:mm:ss");
		String time = dataformate.format(new Date());
 		while(true)
 	    {
 	          //WatchKey key = service.take();    // retrieve the watchkey
 			key = service.take();
 	          for (WatchEvent event : key.pollEvents())
 	          {
 	             //System.out.println(event.kind() + ": "+ event.context());  // Display event and file name
 	        	  WatchEvent<Path> evt = cast(event); 
 	        	  //޸ļ
 	        	  Path name = evt.context();
 	        	  //(Path) key.watchable()ر޸ĵĸĿ¼resolveǽļĿ¼͸Ŀ¼
 	        	  child = ((Path) key.watchable()).resolve(name);
 	        	  //fw.write(time+"\r\n");
 	        	  System.out.format(new SimpleDateFormat("yyyy-MM-dd|hh:mm:ss").format(new Date()) + "|%s|%s\n", event.kind(), child);
 	        	  //System.out.println("....................");
 	        	  //System.out.println(time+"|"+event.kind()+"|"+child+"\n");
 	        	  fw.append(time+"|"+event.kind()+"|"+child+"\r\n");
 	          }
 
 	          //boolean valid = key.reset();
 	          valid = key.reset();
 	          if (!valid)
 	          {
 	        	 fw.close();
 	        	 System.out.println("Detection END!");
 	             break; // Exit if directory is deleted
 	          }

 	    }	
 	}
 }
