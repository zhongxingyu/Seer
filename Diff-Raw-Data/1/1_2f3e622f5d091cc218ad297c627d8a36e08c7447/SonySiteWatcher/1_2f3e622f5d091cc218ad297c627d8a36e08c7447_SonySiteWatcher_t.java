 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rbs
  * Date: 20.08.12
  * Time: 14:44
  * To change this template use File | Settings | File Templates.
  */
 public class SonySiteWatcher extends TimerTask{
 
     ArrayList<String> codes = new ArrayList<String>();
     private final URL url;
     private final Pattern pattern;
 
     public SonySiteWatcher(String site) throws MalformedURLException {
         url = new URL(site);
         pattern = Pattern.compile("003e(\\w{4}-\\w{4}-\\w{4})");
     }
 
     @Override
     public void run() {
 
         System.out.println("Searching for updates...");
         try {
             URLConnection urlConnection = url.openConnection();
             InputStream inputStream = urlConnection.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
 
             while(bufferedReader.ready()){
                 String s = bufferedReader.readLine();
                 Matcher matcher = pattern.matcher(s);
                 while (matcher.find()){
                     String code = matcher.group(1);
                     if(!codes.contains(code)){
                         codes.add(code);
                         playSound("alarm.wav");
                         System.out.println("New code found: " + code);
                     }
 
                 }
             }
 
             inputStream.close();
 
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         System.out.println("...Finished");
     }
 
     public static synchronized void playSound(final String url) {
         new Thread(new Runnable() { // the wrapper thread is unnecessary, unless it blocks on the Clip finishing, see comments
             public void run() {
                 try {
                     Clip clip = AudioSystem.getClip();
                     AudioInputStream inputStream = AudioSystem.getAudioInputStream(Main.class.getResource("/sounds/" + url));
                     clip.open(inputStream);
                     clip.start();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
 }
