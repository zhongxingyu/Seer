 package alpha;
 
 import com.googlecode.javacv.FrameGrabber;
 import com.googlecode.javacv.cpp.opencv_core;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 import static edsdk.a.EdSdkLibrary.kEdsPropID_BatteryLevel;
 import edsdk.utils.CanonCamera;
 import edsdk.utils.commands.ShootTask;
 import java.awt.Choice;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.TextArea;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 
 /**
  *
  * @author Christopher Williams
  */
 public class Controls {
 
     //Variables
     private TextArea scriptfield = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
     private JSlider exp, red, green, blue;
     long canonbatterylg = camera.getProperty(kEdsPropID_BatteryLevel);
     private static Frame frame = new Frame();
     protected static JPanel window = new JPanel();
     private JPanel script = new JPanel();
     private JPanel audioStudio = new JPanel();
     protected static JLabel renderCanon, renderWebcam, renderNikon, lexp, lred, lgreen, lblue, timeline;
     private JTabbedPane tabs = new JTabbedPane(JTabbedPane.RIGHT);
     private Font font = new Font("Courier New", Font.PLAIN, 12);
     private JButton cap, record, stopBtn, playaudio, playAnim;
     public static Choice audioformat = new Choice();
     public static Choice sampleRate = new Choice();
     public static Choice channels = new Choice();
     private Rectangle rcap;
     protected static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
     private int height = Toolkit.getDefaultToolkit().getScreenSize().height;
     public static final JFrame f = new JFrame();
     private Toolbar toolbar = new Toolbar();
     protected static int framename = 0;
     public static CanonCamera camera = new CanonCamera();
     private audioRecorder audio = new audioRecorder();
     ImageIcon images = new ImageIcon();
     static boolean canon = true;
     static boolean nikon = true;
     static boolean webcam = true;
 
     public static void main(String args[]) throws InterruptedException {
         camera.openSession();
         camera.beginLiveView();
         //Canon.Constants();
         new Controls();
         new Save_as();
 
         if (camera.getEdsCamera() != null) {
             System.out.println("Canon DSLR Attached");
             canonSLR(camera);
         } else {
             System.out.println("Webcam Attached");
             webcamRender();
         }
     }
 
     //Renders image from nikon DSLR
     private static void nikonSLR() {
         //NIKON CODE !!NEED NIKON CAMERA TO TEST AND THE API WRAPPERS IN JAVA!!
     }
 
     //Renders image from canon DSLR
     private static void canonSLR(final CanonCamera camera) {
         renderCanon = new JLabel();
         if (canon = true) {
             while (canon = true) {
                 try {
                     Thread.sleep(50);
                     BufferedImage canonimage = camera.downloadLiveView();
                     if (canonimage != null) {
                         renderCanon.setIcon(new ImageIcon(canonimage));
                         renderCanon.setBounds((width / 2) - 528, 10, 1056, 704);
                         renderCanon.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
                         renderCanon.setToolTipText("Live Canon DSLR feed");
                         renderCanon.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                         window.add(renderCanon);
                         webcam = false;
                         nikon = false;
                         System.out.println("Battery: " + camera.getProperty(kEdsPropID_BatteryLevel));
                         canonimage.flush();
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Controls.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     //renders buffered image from webcam
     private static void webcamRender() {
         renderWebcam = new JLabel();
         if (webcam = true) {
             while (webcam = true) {
                 try {
                     Thread.sleep(20);
                     BufferedImage webcamImage = (frame.frame().getBufferedImage());
                     if (webcamImage != null) {
                         renderWebcam.setIcon(new ImageIcon(webcamImage));
                         renderWebcam.setBounds(Frame.grabber.getImageWidth(), 10, Frame.grabber.getImageWidth(), Frame.grabber.getImageHeight());
                         renderWebcam.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
                         renderWebcam.setToolTipText("Live webcam feed");
                         renderWebcam.setMaximumSize(new Dimension(1056, 704));
                         renderWebcam.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                         canon = false;
                         nikon = false;
                         window.add(renderWebcam);
                         webcamImage.flush();
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Controls.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         } else {
             webcam = false;
             try {
                 frame.grabber.stop();
             } catch (FrameGrabber.Exception ex) {
                 Logger.getLogger(Controls.class.getName()).log(Level.SEVERE, null, ex);
             }
 
 
         }
     }
 
     //Have to keep program from crashing to make this work
     private void error_Check() {
         if (canon == false && webcam == false && nikon == false) {
             new NoCam_Error();
             System.out.println("Started");
         }
     }
 
     //handles the JFrame and Main Content
     public Controls() {
 
         tabs.add("Frame Grabber", window);
         tabs.add("Script Editor", script);
         tabs.add("Audio Recording", audioStudio);
         f.add(tabs);
 
         System.setProperty("sun.java2d.opengl", "True");
         f.setJMenuBar(toolbar.toolBar);
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             e.printStackTrace();
         }
         f.setTitle("Pre-Alpha-003-A");
         f.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
         f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         f.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 camera.endLiveView();
                 camera.closeSession();
                 CanonCamera.close();
                 try {
                     Frame.grabber.stop();
                 } catch (FrameGrabber.Exception ex) {
                     Logger.getLogger(Controls.class
                             .getName()).log(Level.SEVERE, null, ex);
                 }
                 System.exit(0);
             }
         });
         f.setLocationRelativeTo(null);
         f.setResizable(true);
         f.setVisible(true);
         window.setLayout(null);
 
         //Method init
         error_Check();
         audioEditor();
         timeLine();
         drawButtons();
         scriptEditor();
         f.repaint();
     }
 
     private void sound() {
         //Shutter release
         try {
             AudioInputStream shuttersound = AudioSystem.getAudioInputStream(new File("resources/sounds/350d-shutter.wav"));
             Clip shutter = AudioSystem.getClip();
             shutter.open(shuttersound);
             shutter.start();
         } catch (UnsupportedAudioFileException | IOException | LineUnavailableException uae) {
             System.out.println(uae);
         }
     }
 
     //Audio recording\mixing
     private void audioEditor() {
         audioStudio.setLayout(null);
 
         record = new JButton("Record");
         record.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         record.setToolTipText("Record Audio");
         record.setBounds((width / 2) - 160, 0, 80, 25);
         audioStudio.add(record);
 
         audioStudio.add(audioformat);
         audioformat.setBounds(0, 0, 80, 25);
         audioformat.add("Wav");
         audioformat.add("AIFC");
         audioformat.add("AIFF");
         audioformat.add("AU");
         audioformat.add("SND");
 
         audioStudio.add(sampleRate);
         sampleRate.setBounds(80, 0, 80, 25);
         sampleRate.add("8000");
         sampleRate.add("11025");
         sampleRate.add("16000");
         sampleRate.add("22050");
         sampleRate.add("44100");
 
         audioStudio.add(channels);
         channels.setBounds(160, 0, 80, 25);
         channels.add("Mono");
         channels.add("Stereo");
 
         playaudio = new JButton("Play");
         playaudio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         playaudio.setToolTipText("Play recording");
         playaudio.setBounds((width / 2), 0, 80, 25);
         audioStudio.add(playaudio);
 
         stopBtn = new JButton("Stop");
         stopBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         stopBtn.setToolTipText("Stop Recording");
         stopBtn.setBounds((width / 2) - 80, 0, 80, 25);
         audioStudio.add(stopBtn);
 
         record.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(
                             ActionEvent e) {
                         audio.captureAudio();
                         System.out.println(audio.getAudioFormat().getSampleRate());
                         System.out.println(audio.getAudioFormat().getChannels());
                         audio.audnum++;
                     }
                 });
 
         stopBtn.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(
                             ActionEvent e) {
                         audio.targetDataLine.stop();
                         audio.targetDataLine.close();
                     }
                 });
     }
 
     //Script Editor
     private void scriptEditor() {
         scriptfield.setEditable(true);
         scriptfield.setFont(font);
         scriptfield.setPreferredSize(new Dimension(800, 900));
         scriptfield.setCursor(null);
         script.add(scriptfield);
     }
 
     //TimeLine
     private void timeLine() {
         timeline = new JLabel();
         timeline.setBounds(10, 300, 255, 500);
         timeline.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
         timeline.setToolTipText("Movie timeline");
         timeline.setLayout(new GridLayout(3, 3));
         window.add(timeline);
     }
 
     public File filename() {
         return new File(Save_Algorithm.imgdir + "\\image_" + framename + ".tiff");
     }
 
     //Draws the buttons and adds functions to them
     private void drawButtons() {
 
         playAnim = new JButton("Play");
         playAnim.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         playAnim.setBounds((width / 2) + 30, 750, 80, 25);
         playAnim.setToolTipText("Play Animation");
         window.add(playAnim);
 
         cap = new JButton("Capture");
         cap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         rcap = new Rectangle((width / 2) - 50, 750, 80, 25);
         cap.setBounds(rcap);
         cap.setToolTipText("Capture Frame");
         window.add(cap);
 
         //Not working right
         playAnim.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent action) {
                 if (canon == true) {
                     renderCanon.setIcon(null);
                 } else if (webcam == true) {
                     for (int i = 0; i < framename; i++) {
                         images = new ImageIcon(Save_Algorithm.imgdir + "\\image_" + i + ".tiff");
 //                        renderWebcam.setIcon(null);
 //                        renderWebcam.setIcon(images);
 //                        renderWebcam.revalidate();
                         System.out.println(images);
                     }
                 }
             }
         });
 
         cap.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     if (canon == true) {
                         sound();
                         camera.execute(new ShootTask(filename()));
                         System.out.println("Frame Captured from Canon SLR at... " + Save_as.pathname);
                         framename++;
                     } else if (canon == false) {
                         opencv_core.IplImage img = frame.frame();
                         sound();
                         cvSaveImage(Save_Algorithm.imgdir + "\\image_" + framename + ".tiff", img);
                         System.out.println("Frame Captured from Webcam at... " + Save_as.pathname);
                         framename++;
                     }
                 } catch (RuntimeException e) {
                     throw e;
                 } catch (Exception e) {
                     throw e;
                 }
             }
         });
     }
 }
