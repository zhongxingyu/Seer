 package net.geekgrandad.plugin;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.Port;
 import javax.speech.AudioException;
 import javax.speech.Central;
 import javax.speech.EngineException;
 import javax.speech.EngineStateError;
 import javax.speech.synthesis.Synthesizer;
 import javax.speech.synthesis.SynthesizerModeDesc;
 
 import net.geekgrandad.config.Config;
 import net.geekgrandad.interfaces.MediaControl;
 import net.geekgrandad.interfaces.Provider;
 import net.geekgrandad.interfaces.Reporter;
 import net.geekgrandad.music.ActivateWindow;
 
 public class SpotifyControl implements MediaControl {
 	private Synthesizer synth;
     private Reporter reporter;
 	private Config config;
 	private Robot robot;
 
 	@Override
 	public void setProvider(Provider provider) {
 		this.reporter = provider.getReporter();
 		this.config = provider.getConfig();
 		
 	    // Create a speak synthesizer and start it
 	    try {  
 	      System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
 	      // Create a synthesizer for English
 	      synth = Central.createSynthesizer(new SynthesizerModeDesc(Locale.ENGLISH));
 	      synth.allocate();
 	      synth.resume();
 	    } catch (EngineException e1) {
 	      e1.printStackTrace();
 	    } catch (EngineStateError e1) {
 	      e1.printStackTrace();
 	    } catch (AudioException e) {
 	      e.printStackTrace();
 	    }
 	    
 	    try {
 			robot = new Robot();
 		} catch (AWTException e) {
 			reporter.error("Spotify: error creating Java robot");
 		}		
 	}
 
 	@Override
 	public void start(int id, String playlist, boolean repeat)
 			throws IOException {
		String list = config.getPlaylists().get(playlist);
 		java.awt.Desktop.getDesktop().browse(java.net.URI.create(list));
 		
 	}
 
 	@Override
 	public void open(int id, String file, boolean repeat) throws IOException {
 		reporter.error("Spotify: open not supported");	
 	}
 
 	@Override
 	public void type(int id, String s) throws IOException {
 		reporter.error("Spotify: type not supported");
 	}
 
 	@Override
 	public void select(int id, String service) throws IOException {
 		reporter.error("Spotify: select not supported");	
 	}
 
 	@Override
 	public void pause(int id) throws IOException {
 		stop(0);	
 	}
 
 	@Override
 	public void stop(int id) throws IOException {
 	  if (robot != null) {
 	    System.out.println(ActivateWindow.activate("Spotify"));
 	    System.out.println("Sending space keypress");
 	    robot.keyPress(KeyEvent.VK_SPACE);
 	    robot.keyRelease(KeyEvent.VK_SPACE);
 	  }		
 	}
 
 	@Override
 	public void play(int id) throws IOException {
 		stop(0);		
 	}
 
 	@Override
 	public void record(int id) throws IOException {
 		reporter.error("Spotify: record not supported");		
 	}
 
 	@Override
 	public void ff(int id) throws IOException {
 		reporter.error("Spotify: ff not supported");		
 	}
 
 	@Override
 	public void fb(int id) throws IOException {
 		reporter.error("Spotify: fb not supported");		
 	}
 
 	@Override
 	public void skip(int id) throws IOException {
 		if (robot != null) {
 		  ActivateWindow.activate("Spotify");
 		  System.out.println("Sending next keypress");
 	    robot.keyPress(KeyEvent.VK_CONTROL);
 	    try {
 	      Thread.sleep(1000);
 	    } catch (InterruptedException e) {}
 	      robot.keyPress(KeyEvent.VK_RIGHT);
 	      robot.keyRelease(KeyEvent.VK_RIGHT);
 	      robot.keyRelease(KeyEvent.VK_CONTROL);
 	    }	
 	}
 
 	@Override
 	public void skipb(int id) throws IOException {
 		reporter.error("Spotify: skipb not supported");
 	}
 
 	@Override
 	public void slow(int id) throws IOException {
 		reporter.error("Spotify: slow not supported");
 	}
 
 	@Override
 	public void delete(int id) throws IOException {
 		reporter.error("Spotify: delete not supported");	
 	}
 
 	@Override
 	public void up(int id) throws IOException {
 		reporter.error("Spotify: up not supported");
 	}
 
 	@Override
 	public void down(int id) throws IOException {
 		reporter.error("Spotify: down not supported");	
 	}
 
 	@Override
 	public void left(int id) throws IOException {
 		reporter.error("Spotify: left not supported");		
 	}
 
 	@Override
 	public void right(int id) throws IOException {
 		reporter.error("Spotify: right not supported");	
 	}
 
 	@Override
 	public void ok(int id) throws IOException {
 		reporter.error("Spotify: ok not supported");		
 	}
 
 	@Override
 	public void back(int id) throws IOException {
 		reporter.error("Spotify: back not supported");
 	}
 
 	@Override
 	public void lastChannel(int id) throws IOException {
 		reporter.error("Spotify: lastChannel not supported");
 	}
 
 	@Override
 	public void option(int id, String option) throws IOException {
 		reporter.error("Spotify: option not supported");		
 	}
 
 	@Override
 	public void volumeUp(int id) throws IOException {
 		reporter.error("Spotify: volumeUp not supported");		
 	}
 
 	@Override
 	public void volumeDown(int id) throws IOException {
 		reporter.error("Spotify: volumeDown not supported");		
 	}
 
 	@Override
 	public void mute(int id) throws IOException {
 		reporter.error("Spotify: mute not supported");		
 	}
 
 	@Override
 	public int getVolume(int id) throws IOException {
 		return getVolume();
 	}
 
 	@Override
 	public void setVolume(int id, int volume) throws IOException {
 		setVolume(volume);	
 	}
 
 	@Override
 	public void setChannel(int id, int channel) throws IOException {
 		reporter.error("Spotify: setChannel not supported");	
 	}
 
 	@Override
 	public String getTrack(int id) throws IOException {
 		reporter.error("Spotify: getTrack not supported");
 		return "";
 	}
 
 	@Override
 	public int getChannel(int id) throws IOException {
 		reporter.error("Spotify: getChannel not supported");
 		return 0;
 	}
 
 	@Override
 	public void channelUp(int id) throws IOException {
 		reporter.error("Spotify: channels not supported");
 	}
 
 	@Override
 	public void channelDown(int id) throws IOException {
 		reporter.error("Spotify: channels not supported");	
 	}
 
 	@Override
 	public void thumbsUp(int id) throws IOException {
 		reporter.error("Spotify: rating not supported");	
 	}
 
 	@Override
 	public void thumbsDown(int id) throws IOException {
 		reporter.error("Spotify: rating not supported");	
 	}
 
 	@Override
 	public void digit(int id, int n) throws IOException {
 		reporter.error("Spotify: digit not supported");
 	}
 
 	@Override
 	public void color(int id, int n) throws IOException {
 		reporter.error("Spotify: color not supported");	
 	}
 
 	@Override
 	public void turnOn(int id) throws IOException {
 		reporter.error("Spotify: turnOn not supported");	
 	}
 
 	@Override
 	public void turnOff(int id) throws IOException {
 		reporter.error("Spotify: turnOff not supported");
 	}
 
 	@Override
 	public void pin(int id) throws IOException {
 		reporter.error("Spotify: pin not supported");		
 	}
 
 	@Override
 	public void setSource(int id, int source) throws IOException {
 		reporter.error("Spotify: setSource not supported");
 	}
 
 	@Override
 	public String getArtist(int id) throws IOException {
 		reporter.error("Spotify: getArtist not supported");
 		return "";
 	}
 
 	@Override
 	public String getAlbum(int id) throws IOException {
 		reporter.error("Spotify: getAlbum not supported");
 		return "";
 	}
 
 	@Override
 	public String getPlaylist(int id) throws IOException {
 		reporter.error("Spotify: getPlaylist not supported");
 		return "";
 	}
 
 	@Override
 	public void say(int id, String text) throws IOException {
 		say(text);		
 	}
 
 	@Override
 	public void pageUp(int id) throws IOException {
 		reporter.error("Spotify: pageUp not supported");		
 	}
 
 	@Override
 	public void pageDown(int id) throws IOException {
 		reporter.error("Spotify: pageDown not supported");	
 	}
 
 	@Override
 	public boolean isPlaying(int id) throws IOException {
 		reporter.error("Spotify: isPlaying not supported");
 		return false;
 	}
 
 	@Override
 	public void reboot(int id) throws IOException {
 		reporter.error("Spotify: reboot not supported");
 	}
 
 	@Override
 	public void setPlayer(int id, int playerId) throws IOException {
 		reporter.error("Spotify: left not supported");	
 	}
 
 	@Override
 	public void setRepeat(int id, boolean repeat) throws IOException {
 		reporter.error("Spotify: setRepeat not supported");
 	}
 
 	@Override
 	public void setShuffle(int id, boolean shuffle) throws IOException {
 		reporter.error("Spotify: setShuffle not supported");	
 	}
 	
   private void say(String msg) {
 	try {  
 	      // Speak the message
       synth.speakPlainText(msg, null);
   
       // Wait till speaking is done
       synth.waitEngineState(Synthesizer.QUEUE_EMPTY);
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
   
   private void shutdown() {
     String shutdownCommand = null;
     String osName = System.getProperty("os.name");        
     if (osName.startsWith("Win")) {
       shutdownCommand = "shutdown.exe -s -t 0";
     } else if (osName.startsWith("Linux") || osName.startsWith("Mac")) {
       shutdownCommand = "shutdown -h now";
     } else {
       System.err.println("Shutdown unsupported operating system ...");
     }
     if (shutdownCommand != null)
       try {
         Runtime.getRuntime().exec(shutdownCommand);
       } catch (IOException e) {}
     System.exit(0);
   }
   
 	private int getVolume() {	
 		Mixer.Info[] infos = AudioSystem.getMixerInfo();  
 	    for (Mixer.Info info: infos)  
 	    {  
 	        Mixer mixer = AudioSystem.getMixer(info);  
 	        if (mixer.isLineSupported(Port.Info.SPEAKER))  
 	        {  
 	            Port port;
 				try {
 					port = (Port)mixer.getLine(Port.Info.SPEAKER);
 	                port.open();
 	                if (port.isControlSupported(FloatControl.Type.VOLUME))  
 	                {  
 	                    FloatControl volume = (FloatControl)port.getControl(FloatControl.Type.VOLUME);  
 	                    return (int) (volume.getValue() * 100f) ;
 	                }  
 	                port.close();
 				} catch (LineUnavailableException e) {
 					e.printStackTrace();
 				}     
 	        }  
 	    }
 	    return 0;
 	}
   
 	private void setVolume(int vol) {	
 		Mixer.Info[] infos = AudioSystem.getMixerInfo();  
 	    for (Mixer.Info info: infos)  
 	    {  
 	        Mixer mixer = AudioSystem.getMixer(info);  
 	        if (mixer.isLineSupported(Port.Info.SPEAKER))  
 	        {  
 	            Port port;
 				try {
 					port = (Port)mixer.getLine(Port.Info.SPEAKER);
 	                port.open();
 	                if (port.isControlSupported(FloatControl.Type.VOLUME))  
 	                {  
 	                    FloatControl volume = (FloatControl)port.getControl(FloatControl.Type.VOLUME);  
 	                    volume.setValue(vol/ 100f);
 	                }  
 	                port.close();
 				} catch (LineUnavailableException e) {
 					e.printStackTrace();
 				}     
 	        }  
 	    }
 	}
 
 }
