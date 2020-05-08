 package playlistmanager;
 
 import java.io.File;
 
 public class PlaylistManager
 {
 	public static void main(String[] args)
 	{
 		if(!(new File(PlaylistVariables.getFullDBName()).exists())) {
			// DB does not exist. Create DB.
 			DatabaseCreator.createDatabase();
 		}
 	}
 }
