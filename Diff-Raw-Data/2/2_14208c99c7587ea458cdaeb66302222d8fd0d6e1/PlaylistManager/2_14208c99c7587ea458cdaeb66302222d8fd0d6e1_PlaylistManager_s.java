 package playlistmanager;
 
 import java.io.File;
 
 public class PlaylistManager
 {
 	public static void main(String[] args)
 	{
 		if(!(new File(PlaylistVariables.getFullDBName()).exists())) {
			// Create DB. DB does not exist.
 			DatabaseCreator.createDatabase();
 		}
 	}
 }
