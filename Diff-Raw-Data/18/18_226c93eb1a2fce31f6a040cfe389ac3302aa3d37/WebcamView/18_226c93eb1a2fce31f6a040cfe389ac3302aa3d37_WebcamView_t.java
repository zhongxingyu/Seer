 package mkpc.webcam;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.io.IOException;
 
 import javax.media.CannotRealizeException;
 import javax.media.CaptureDeviceManager;
 import javax.media.Manager;
 import javax.media.MediaLocator;
 import javax.media.NoPlayerException;
 import javax.media.Player;
 import javax.media.bean.playerbean.MediaPlayer;
 import javax.swing.JPanel;
 
 import mkpc.log.LogSystem;
 
 public class WebcamView extends JPanel 
 {
 	/**
 	 * This class includes the webcam which u choose!<br />
 	 * Standard URL for one webcam/ videostream is "vfw://"
 	 */
 	private static final long serialVersionUID = 1L;
 	private Player mediaPlayer;
 	
 	public WebcamView(String camURL)
 	{
 		super();
 		this.setLayout(new BorderLayout());	
 		
 		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
 		
 		try
 		{
 			LogSystem.CLog("Devices: " + CaptureDeviceManager.getDeviceList(null));
 		    MediaLocator ml = new MediaLocator(camURL); // "vfw://" standard URL
 		    mediaPlayer = Manager.createRealizedPlayer(ml);
 		    
 		    Component video = mediaPlayer.getVisualComponent();
 			
 			if(video != null)
 			{
 				this.add( video, BorderLayout.CENTER);			
 			}
 			
 			mediaPlayer.setRate(25.0f);
 			mediaPlayer.start();
 		}
 		catch(NoPlayerException noPlayerException)
 		{
 			LogSystem.addLog(LogSystem.getMessage("webcamViewError001"));
 		}
 		catch( CannotRealizeException cre)
 		{
 			LogSystem.addLog(LogSystem.getMessage("webcamViewError002"));
 			LogSystem.addLog( "Could not realize media player." );
 		}
 		catch(IOException ie)
 		{
 			LogSystem.addLog(LogSystem.getMessage("webcamViewError003"));
 			LogSystem.addLog( "Error reading from the source." );
 		}
 	}
 	
 	public void close()
 	{
 		if(this.mediaPlayer != null && this.mediaPlayer.getState() == MediaPlayer.Started)
 		{
 			this.mediaPlayer.close();
 		}
 	}
 }
