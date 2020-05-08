 /**
  * Created with IntelliJ IDEA.
  * Date  : 28/03/13
  * Time  : 9:28 AM
  */
 import java.applet.Applet;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.net.*;
 import javax.sound.sampled.*;
 import java.util.concurrent.*;
 
 public class Jukebox extends Applet{
 //***************************************//
     Image albumCover;
     MediaTracker MediaTrack;
     AudioInputStream stream;
     Clip music;
     List list;
     URL url;
     URL file;
     URL albumArt = this.getClass().getResource("/Data/AlbumArt/default.jpg");
     String title = "Select a song, just double-click!", artist="", length="";
     Button stop;
     Panel panel;
 //***************************************//
 
     public void init() {
         // Sizes the applet on init to the preferred/intended dimensions
         resize(700, 400);
         setLayout(new BorderLayout(0,3));
 
        // If it gets its own .class from a path containing http://, end most of the program and display an error message.
        // This is to compensate for the fact that if the applet is run without this code on a http server the applet will just crash without explanation.
        // It also provides a URL to download a copy of the files in order to rectify the issue.
         if (this.getClass().getResource("Jukebox.class").toString().contains("http://")) {
             setLayout(new FlowLayout());
            TextArea textArea = (new TextArea(
                    "This Java applet must be run from a folder on your hard drive. Download a copy at...\n" +
                    "http://vlk.me/35\n\n" +
                    "For those wondering, this is due to the fact that this applet scans a \nrelative folder for files rather than having them hardcoded. Sorry!"
            ));
            textArea.setEditable(false);
            textArea.setColumns(20);
            add(textArea);
             setBackground(Color.white);
             albumArt = null;
             title = "";
             artist="";
             length="";
             return;
         }
         // Creates the list component that will house our songs.
         list = new List(7);
 
         /* *
         * Complicated block of code does a bunch of stuff.
         *
         * Firstly it gets a URL to the /data folder that's correct because it's worked out relative to the java file.
         * This means it'll work when uploaded to a web server/anywhere.
         *
         * It then adds each .wav file to the list component in the window, but this is surrounded in a try/catch expression.
         *
         * Simply meaning that if it fails (kind of likely, what if no songs are available, etc.) it'll know what to do still
         * */
 
         url = this.getClass().getResource("/data");
 
         File dir = null;                 // Declares the 'File' "dir".
         try {
             dir = new File(url.toURI()); // Tries to turn the path to /data into an actual file object
         } catch (URISyntaxException e) {
             e.printStackTrace();         // But if it fails, print a stack trace!
         }
         for (File child : dir.listFiles()) {  // For loop that goes through each file in the folder 'dir'
             if(child.getName().endsWith(".wav")) {
                 list.add(child.getName()); // Adds files ending in .wav to the list component.
             } else {
                 // Do nothing
             }
         }
         // Add the list to the applet.
         add(list, BorderLayout.SOUTH);
 
         // Makes a new panel and sets it to 'East' in the original applet. The panel uses FlowLayout.
         panel = new Panel();
         panel.setLayout(new FlowLayout(FlowLayout.CENTER));
         add(panel,BorderLayout.EAST);
 
         // Adds a 'stop' button to the panel.
         Font btn = new Font("Sans-serif", Font.BOLD, 30);
         stop = new Button("■");
         stop.setFont(btn);
         panel.setVisible(true);
         stop.setVisible(true);
         panel.add(stop);
         /*
          * Listen up!
          *  Provides a listener that responds to when items are *double-clicked* on
          *  in the list component.
          */
         list.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 /****************************************
                  * Playing Audio
                  ****************************************/
                 file = this.getClass().getResource("/data/" + list.getSelectedItem());  // Gets the proper, relative URL to the selected file.
 
                 // What if something is already playing? :S
                 if(stream != null) {  // If the AudioStream *isn't* empty...
                     try {
                         stream.close();  // try to close the stream
                     } catch (IOException e) {
                         e.printStackTrace();   // but if it can't, print a stack trace.
                     }
                 }
 
                 if(music != null) {  // If the clip *isn't* empty...
                     music.stop();    // Stop the music.
                     music.flush();   // Flush the cache.
                 }
 
                 stream = null; // Set the stream to null.
                 try {
                     stream = AudioSystem.getAudioInputStream(file);  // Try to put the file into the AudioStream
                 } catch (UnsupportedAudioFileException e) {          // If the file is unsupported..
                     e.printStackTrace();                                // Print a Stack Trace
                 } catch (IOException e) {                            // If there's an I/O exception..
                     e.printStackTrace();                                // Print a Stack Trace
                 }
                 music = null; // Set the clip to null.
                 try {
                     music = AudioSystem.getClip();      // Creates a clip that can be used for playing back an audio file or an audio stream.
                 } catch (LineUnavailableException e) {  // But if there's no line to put it on...
                     e.printStackTrace();                    // Print a Stack Trace
                 }
                 try {
                     music.open(stream);                 // Try to use the clip to open the previously created stream.
                 } catch (LineUnavailableException e) {  // But if there's no line to put it on...
                     e.printStackTrace();                    // Print a Stack Trace.
                 } catch (IOException e) {               // Or if there's an I/O Error.
                     e.printStackTrace();                    // Print a Stack Trace.
                 }
                 music.start();  // Finally, play the music.
 
                 /****************************************
                  * Metadata
                  ****************************************/
 
                 // Track name + Artist
                 String filename = list.getSelectedItem();
 
                 int i = filename.lastIndexOf('.');
                 if(i >= 0) { // If there's an extension
                     filename = filename.substring(0,i); // Cuts off the file extension.
                 }
 
                 if(filename.contains(" - ")) { // If the file has a hyphen separator, assume it separates the title from the artist. RTM!!
                     String[] metadata = filename.split(" - "); // Split the song name into the two parts and store them in an array.
 
                     title = metadata[0];  // The title is the first part of the array (Starts from 0).
                     artist = metadata[1]; // The artist is the second part of the array.
                 } else {
                     title = filename; // If it's not named correctly, just make the title the filename.
                     artist = "";
                 }
 
                 // Track length
                 long lM = (long) (1000 * stream.getFrameLength() / stream.getFormat().getFrameRate());  // Get the audio stream's frame length and divide that by its FrameRate, times 1000 is the length of the file in milliseconds.
 
                 // Converts milliseconds into a readable minutes & seconds format via the TimeUnit class.
                 length = String.format("%d min, %d sec",
                         TimeUnit.MILLISECONDS.toMinutes(lM),
                         TimeUnit.MILLISECONDS.toSeconds(lM) -
                         TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lM))
                 );
 
                 // Album Art
                 // Assume there's a file in the /Data/AlbumArt directory named "TrackName - Artist.jpg"
                 albumArt = this.getClass().getResource("/Data/AlbumArt/" + filename + ".jpg");
 
                 /* Comments are on the side -> */
                 try {
                     albumArt.getContent();                                                 // Try to get the .jpg album art
                 } catch (NullPointerException e) {                                         // If it's not there
                     albumArt = this.getClass().getResource("/Data/AlbumArt/default.jpg");  // Set the album art to the default.
                 } catch (IOException e) {                                                  // But if there's another I/O error.
                     e.printStackTrace();                                                   // Print a stack trace.
                 }
                 /****************************************
                  * Repaint with new variables.
                  ****************************************/
                 repaint();
             }
         }); // List listener end.
 
         // Creates an event listener for the 'stop' button.
         stop.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
 
                 /*
                 * If the AudioStream isn't empty, try to close the stream, but if that's not possible; print a stack trace.
                 */
                 if(stream != null) {
                     try {
                         stream.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
 
                 /*
                 * If the clip isn't empty, stop the music and then flush the cache.
                 */
                 if(music != null) {
                     music.stop();
                     music.flush();
                 }
 
                 // Sets all the metadata back to default values.
                 title = "Select a song, just double-click!";
                 artist = "";
                 length = "";
                 albumArt = this.getClass().getResource("/Data/AlbumArt/default.jpg");
                 repaint();
             }
         });
     }
 
 
 
     public void paint(Graphics g) {
         // No more fuzzy text! Anti-aliasing on!
         Graphics2D g2 = (Graphics2D)g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
 
         //Define some fonts!
         Font fntT = new Font("Trebuchet MS", Font.PLAIN, 22);
         Font fntA = new Font("Trebuchet MS", Font.PLAIN, 18);
         Font fntL = new Font("Trebuchet MS", Font.PLAIN, 16);
 
         //Let's have a MediaTracker!
         MediaTrack = new MediaTracker(this);
 
         // Gets album art and scales it to 250x250 pixels
         albumCover = getImage(albumArt);
         MediaTrack.addImage(albumCover,0);
         g2.drawImage(albumCover, 10, 10, 250, 250, this);
 
         // Draw the song information
         g2.setFont(fntT);
         g2.drawString(title, 270, 50);
         g2.setFont(fntA);
         g2.drawString(artist, 280, 80);
         g2.setFont(fntL);
         g2.drawString(length,280,100);
     }
 
 }
