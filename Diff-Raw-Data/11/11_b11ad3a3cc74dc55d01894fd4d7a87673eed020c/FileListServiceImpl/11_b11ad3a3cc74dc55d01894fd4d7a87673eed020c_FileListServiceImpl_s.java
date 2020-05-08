 package se.ixanon.filmhandler.server;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import se.ixanon.filmhandler.client.services.FileListService;
 import se.ixanon.filmhandler.shared.MovieItem;
 
 @SuppressWarnings("serial")
 public class FileListServiceImpl extends RemoteServiceServlet implements FileListService {
 
	private File dir = new File("/media");
 	
 	public ArrayList<MovieItem> getFiles()
 	{
 		ArrayList<MovieItem> l = new ArrayList<MovieItem>();
 		
 		for(File f : dir.listFiles(new FilenameFilter() {
 			
 			@Override
 			public boolean accept(File dir, String name) {
 				
 				if(name.endsWith(".ts") || name.endsWith(".mp4"))
 				{
 					return true;
 				}
 				else
 					return false;
 			}
 		}))
 		{
 			String name = f.getName().substring(0,f.getName().lastIndexOf("."));
 			String type = f.getName().substring(f.getName().lastIndexOf("."));
 			
 			l.add(new MovieItem(name, type, false));
 		}
 		return l;
 	}
 
 	@Override
 	public void deleteItems(ArrayList<MovieItem> deleteList) {
 		
 		for(MovieItem item : deleteList)
 		{
 			for(File f : dir.listFiles())
 			{	
 				String name = f.getName();
 				String type = "";
 				int p = name.lastIndexOf(".");
 				if (p != -1) {
 					type = name.substring(p);
 					name = name.substring(0, p);
 				}
 				if (name.equals(item.getName()) && type.equals(item.getType())) {
 					f.delete();
 				}
 			}
 		}
 	}
 	@Override
 	public boolean fileExists(String fileName) {
 		for(File f : dir.listFiles())
 		{
 			if(f.getName().equals(fileName))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 }
