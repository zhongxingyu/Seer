 package userinterface;
 
 import com.xuggle.mediatool.IMediaReader;
 import com.xuggle.mediatool.MediaListenerAdapter;
 import com.xuggle.mediatool.ToolFactory;
 import com.xuggle.mediatool.event.IVideoPictureEvent;
 import com.xuggle.xuggler.Global;
 
import java.io.File;
 public class LecteurVideo
 {
 	private File fichierVideo;/*Le fichier video*/
 	private String nom;/*Le nom du fichier video*/
 
 
 	IMediaReader mediaReader;/*Le lecteur de flux*/
 
 	public LecteurVideo(File fichierVideo)
 	{
 		this.fichierVideo = fichierVideo;
 		this.nom = fichierVideo.getName();
 		this.initialiserComposant();
 	}
 
 	public void initialiserComposant()
 	{
 
 
 	}
 
 	public void play()
 	{
 	}
 
 	public void pause()
 	{
 
 	}
 
 	public void stop()
 	{
 	}
 
 	public void setVolume(long volume)
 	{
 	}
 
 }
