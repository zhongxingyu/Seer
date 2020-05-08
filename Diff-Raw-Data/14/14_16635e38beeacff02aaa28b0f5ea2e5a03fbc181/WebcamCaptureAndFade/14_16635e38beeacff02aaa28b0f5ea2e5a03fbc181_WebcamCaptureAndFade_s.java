 
 package webcamCaptureAndFade;
 
 import javax.swing.*;
 
 import java.io.*;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.media.*;
 import javax.media.format.*;
 import javax.media.util.*;
 import javax.media.control.*;
 
 import java.text.SimpleDateFormat;
 import java.util.Timer;
 import java.util.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.*;
 import java.awt.event.*;
 
 import com.sun.image.codec.jpeg.*;
 
 /**
  * WEBCAM CAPTURE AND FADE
  * 
  * This program is made for the science center Vitenfabrikken in Sandnes, Norway.
  * It captures images from a normal webcamera in Windows and fades between
  * the captured images.
  * 
  * The program is made during the spring of 2010 for the exhibition "Kem e Sandnes?"
  * ("Who is Sandnes?"). The exhibition will be/is opened on 14th of April by the
  * queen of Norway.
  *
  *
  * Some features:
  * - A picture is only shown once in the fading images (layout1280())
  * - Pictures captures is saved with the current time and date
  * - Last pictures captured as shown more often than older once 
  *   (number of pictures considered last pictures is controlled by lastadded_max,
  *   percentage of new images vs old images is given by percentage_of_new_images)
  * - Newly captured images are, with layout1024(), force to be the next image to 
  *   fade to if the image is not fading at the moment.
  * - 5 seconds between each captured image
  * - Pictures that are in use (fading out or fading in) is cached by the program.
  *   Others are loaded from disk once its needed.
  * - Timeout for capture window
  * - Configurable amount of frames to hold a picture before changing to the next
  * - Optional blinking red border around new images. Color can be configured.
  * - With no camera connected, the program won't start (gives a error message)
  * - Debug by pressing t, y or u (more can be added in keyPressed())
  * - Border around the pictures (layout1024())
  * - Date of the picture in lower right corner (layout1024())
  *
  * 
  * Program is tested with 256M heap size on Windows XP and Windows 7.
  * 
  * Remember to install Java Media Framework.
  * 
  * Program is written by Hallvard Nygrd <hn@jaermuseet.no> for Jrmuseet / Vitenfabrikken.
  * 
  * License:
  * CC-BY-SA: Creative Commons Attribution-Share Alike 3.0 Norway License
  * http://creativecommons.org/licenses/by-sa/3.0/no/
  * 
  * @author Jrmuseet / Vitenfabrikken, Hallvard Nygrd <hn@jaermuseet.no>
  *
  */
 
 public class WebcamCaptureAndFade {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static void main(String s[]) {
 		
 		// Getting save directory
 		String saveDir;
 		if(s.length > 0)
 		{
 			saveDir = s[0];
 		}
 		else
 		{
 			saveDir =
 				JOptionPane.showInputDialog(null, "Please enter directory where " +
 						"the images is/will be saved\n\n" +
 						"Also possible to specifiy as argument 1 when " +
 						"running this program.",
 						 "l:\\webcamtest"); 
 		}
 		
 		// Move mouse to the point 5000,5000 px (out of the screen)
 		Robot rob;
 		try {
 			rob = new Robot();
 			rob.setAutoDelay(500); // 0,5 s
 			rob.mouseMove(5000, 5000);
 		} catch (AWTException e) {
 			e.printStackTrace();
 		}
 		
 		// Make the main window
 		JFrame frame = new JFrame();
 		frame.setAlwaysOnTop(true);
 		frame.setTitle("Webcam capture and imagefading - " +
 				"Vitenfabrikken Jrmuseet - " +
 				"made by Hallvard Nygrd - " +
 				"Vitenfabrikken.no / Jaermuseet.no");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.setUndecorated(true);
 		
 		WebcamCaptureAndFadePanel panel = new WebcamCaptureAndFadePanel(saveDir);
 		frame.getContentPane().add(panel);
 		frame.addKeyListener(panel);
 		frame.pack();
 		
 		frame.setVisible(true);
 		
 
 	}
 }
 
 class WebcamCaptureAndFadePanel extends JPanel implements KeyListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	
 	// ## SETTINGS ##
 	
 	public int fps = 30; // Frames per second
 	
 	public String saveDirectory;
 	
 	// X * fps = x seconds
 	public int number_of_frames_betweencaptures = 5*fps; // Number of frame to wait between captures 
 	public int number_of_frames_showimage = 4*fps; // Number of frames to hold the image before fading to next
 	public int number_of_frames_redborder = (int)0.5*fps; // Number of frames the red border should last, -1 to disable
 	public Color color_redborder = Color.red; // Change the color of the "red" border
 	public int number_of_second_capturewindow = 60; // Number of seconds to have the capturewindow open
 	
 	public boolean captureWindow; // Open captureWindow when pressing the capture key
 	
 	public int percentage_of_new_images = 50; // 50%
 	
 	// ## OTHER STUFF ##
 	// (no need to change any here ;-)
 	public static Player player;
 	public CaptureDeviceInfo di;
 	public MediaLocator ml;
 	public JButton capture;
 	public JButton capture2;
 	public Buffer buf;
 	public Image img;
 	public VideoFormat vf;
 	public BufferToImage btoi;
 	public JPanel buttonpanel;
 	protected Component comp;
 	JFrame cw;
 	public int lastcapture_framenr;
 	public rotatedText cwText;
 	
 	public FormatControl formatControl;
 	public Boolean gotImages = false;
 	
 	public WebcamCaptureAndFadeImagePanel[] imagepanels;
 	
 	public int size_x, size_y;
 	public int sizeCaptureWindow_x, sizeCaptureWindow_y;
 	public int cwLocation_x, cwLocation_y;
 	
 	public boolean enable_datetext;
 	public rotatedText2 datetext;
 	
 	public boolean enable_forceNewImage;
 	
 	public WebcamCaptureAndFadePanel(String saveDir) {
 		
 		System.out.println("Using " + saveDir + " as directory for the images.");
 		saveDirectory = saveDir;
 		
 		getImages();
 		images_used = new ArrayList<Integer>();
 		images_lastadded = new ArrayList<Integer>();
 		images_nevershown = new ArrayList<Integer>();
 		
 		//String str1 = "vfw:Logitech USB Video Camera:0";
 		String str2 = "vfw:Microsoft WDM Image Capture (Win32):0";
 		di = CaptureDeviceManager.getDevice(str2);
 		ml = di.getLocator();
 		
 		try {
 			player = Manager.createRealizedPlayer(ml);
 			formatControl = (FormatControl)player.getControl(
             "javax.media.control.FormatControl");
 			
 			/*
 			Format[] formats = formatControl.getSupportedFormats();
 			for (int i=0; i<formats.length; i++)
 				System.out.println(formats[i].toString());
 			*/
 			
 			player.start();
 		}
 		catch(javax.media.NoPlayerException e) 
 		{
 			 JOptionPane.showMessageDialog(null, "Klarer ikke  starte"+
 					 " programmet pga. feil med kamera. Sjekk at det er koblet til.", 
 					 "IOException", 
 					 JOptionPane.ERROR_MESSAGE); 
 			 System.exit(0);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		/*
 		 * Layout
 		 * 
 		 * Add
 		 * - comp
 		 * - imagepanels
 		 */
 
 		//layout1280();
 		layout1024();
 		
 		// Capture Window
 		if(captureWindow)
 		{
 			cw = new JFrame("Capture from webcam");
 			cw.setAlwaysOnTop(true);
 			cw.setSize(sizeCaptureWindow_x, sizeCaptureWindow_y);
 			cw.addKeyListener(new captureWindowKeyListner());
 			cw.setUndecorated(true);
 			
 			// Add webcam
 			if ((comp = player.getVisualComponent()) != null) {
 				cw.add(comp);
 			}
 			
 			// Add panel to window and set location of window
 			cw.setLocation(cwLocation_x, cwLocation_y);
 		}
 		
 		// Text window
 		cwText = new rotatedText("");
 		
 		/*
 		 * Timer for update
 		 */
 		Timer thread = new Timer();
 		thread.schedule(new frameUpdateTask(), 0, (1000/fps));
 	}
 
 	protected void layout1280 () {
 		// Resolution of the camera pictures divided by 2
 		
 		// 320x240, Creative camera for layout1280
 		size_x = 320/2;
 		size_y = 240/2;
 		sizeCaptureWindow_x = size_x*2;
 		sizeCaptureWindow_y = size_y*2;
 		
 		
 		imagepanels = new WebcamCaptureAndFadeImagePanel[4];
 		imagepanels[0] = new WebcamCaptureAndFadeImagePanel(3,8, size_x, size_y);
 		imagepanels[1] = new WebcamCaptureAndFadeImagePanel(2,3, size_x, size_y);
 		imagepanels[2] = new WebcamCaptureAndFadeImagePanel(2,3, size_x, size_y);
 		imagepanels[3] = new WebcamCaptureAndFadeImagePanel(3,8, size_x, size_y);
 		
 		setLayout(new BorderLayout());
 		setSize((4+2+4)*size_x, (6)*size_y);
 		
 		add(imagepanels[0],	BorderLayout.WEST);
 		
 		JPanel middle = new JPanel(new BorderLayout());
 		middle.add(imagepanels[1], BorderLayout.NORTH);
 		if ((comp = player.getVisualComponent()) != null) {
 			middle.add(comp, BorderLayout.CENTER);
 		}
 		middle.add(imagepanels[2], BorderLayout.SOUTH);
 		middle.setSize(new Dimension(4*size_x, 2*size_y));
 		add(middle, BorderLayout.CENTER);
 		
 		add(imagepanels[3], BorderLayout.EAST);
 		
 		enable_datetext = false;
 		enable_forceNewImage = false;
 		captureWindow = false;
 	}
 
 	protected void layout1024 () {
 
 		// 640x480, Creative camera for layout1024
 		size_x = 1024;
 		size_y = 768;
 		sizeCaptureWindow_x = 680;
 		sizeCaptureWindow_y = 480;
 		cwLocation_x = cwLocation_y = 0;
 		
 		// Borders in relation to a normal screen (not the rotated)
 		int border_top, border_left, border_right, border_bottom;
		border_top = border_bottom = border_left = 50; // Top, left, right when rotated
		border_right = 80;
 		
 		
 		imagepanels = new WebcamCaptureAndFadeImagePanel[1];
		imagepanels[0] = new WebcamCaptureAndFadeImagePanel(1,1, size_x, size_y);
 		
 		//setSize(size_x, size_y);
 		
 		setLayout(null);
 		
 		JComponent jcomp = new JComponent(){
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void paintComponent(Graphics g) {
 				
 				Graphics2D g2 = (Graphics2D)g;
 				
 				g2.setColor(Color.white);
 				g2.fillRect(0, 0, size_x, size_y);
 				
 				super.paintComponent(g);
 			}
 		};
 		datetext = new rotatedText2 ("");
 		
 		add(datetext);
		datetext.setBounds(size_x-25, 10, 40, 100);
 		
 		add(jcomp);
 		jcomp.setBounds(0, 0, size_x, size_y);
 		
 		
 		add(imagepanels[0]);
 		imagepanels[0].setBounds(
 				border_top, border_left, 
 				size_x-border_right-border_left,
 				size_y-border_top-border_bottom);
 		
 		enable_datetext = true;
 		enable_forceNewImage = true;
 		captureWindow = true;
 		number_of_frames_redborder = -1;
 		
 		// Set capture window at center of the screen
 		cwLocation_x = (size_x / 2)-(sizeCaptureWindow_x/2);
 		cwLocation_y = (size_y / 2)-(sizeCaptureWindow_y/2);
 		
 		setSize(size_x, size_y);
 		setBounds(0, 0, 200, 200);
 		setPreferredSize(new Dimension(size_x,size_y));
 	}
 	
 	protected void getImages() {
 		File directory = new File(this.saveDirectory);
 
 		//BufferedImage img = null;
 		
 		images = new ArrayList<String>();
 		if( directory.exists() && directory.isDirectory())
 		{
 			//File[] files = directory.listFiles();
 			String[] files = directory.list();
 		
 			for(int i=0; i < files.length; i++){
 				//System.out.println(files[i]);
 				//try {
 					//if(files[i].getName().startsWith("cam"))
 					if(files[i].startsWith("cam"))
 					{
 						// Windows spesific
 						images.add(this.saveDirectory + "\\" + files[i]);
 						/*
 					    img = ImageIO.read(files[i]);
 					    images.add(img);*/
 					}
 				//} catch (IOException e) {
 				//	System.err.println("Failed on reading image. IOException.");
 				//}
 			}
 		}
 		
 		System.out.println("Total image count = " + images.size());
 		gotImages = true;
 	}
 	
 	public Image getImage(int imagenum)
 	{
 		if(imagenum == -1)
 		{
 			return null;
 		}
 		
 		String path = images.get(imagenum);
 		try {
 			return ImageIO.read(new File(path));
 		} catch (IOException e) {
 			System.out.println("Path til ikke funnet: " + path);
 			return null;
 		}
 	}
 	
 	public static void playerclose() {
 		player.close();
 		player.deallocate();
 	}
 	
 	public void captureImage()
 	{
 		String savepath = this.saveDirectory + "\\cam"
 		+ this.getDateFormatNow("yyyyMMdd_HHmmss-S") + ".jpg";
 		System.out.println("Capturing current image to " +savepath);
 		
 		// Grab a frame
 		FrameGrabbingControl fgc = (FrameGrabbingControl) player
 				.getControl("javax.media.control.FrameGrabbingControl");
 		buf = fgc.grabFrame();
 
 		// Convert it to an image
 		btoi = new BufferToImage((VideoFormat) buf.getFormat());
 		img = btoi.createImage(buf);
 		
 		// save image
 		saveJPG(img, savepath);
 		
 		// show the image
 		//imgpanel.setImage(img);
 		
 		//images.add(img);
 		images.add(savepath);
 		
 		if(images_lastadded.size() >= lastadded_max)
 		{
 			// Remove last
 			images_lastadded.remove(images_lastadded.size()-1);
 		}
 
 		images_lastadded.add(0, images.size()-1);
 		images_nevershown .add(0, images.size()-1);
 		
 		forceNewImage();
 	}
 	
 	public void nextFrame() {
 		for (int i = 0; i < imagepanels.length; i++) {
 			imagepanels[i].nextFrame();
 		}
 	}
 	
 	public void forceNewImage() {
 		if(enable_forceNewImage)
 		{
 			for (int i = 0; i < imagepanels.length; i++) {
 				imagepanels[i].forceNewImage();
 			}
 		}
 	}
 
 	public static void saveJPG(Image img, String s) {
 		BufferedImage bi = new BufferedImage(
 				img.getWidth(null), 
 				img.getHeight(null), 
 				BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = bi.createGraphics();
 		g2.drawImage(img, null, null);
 
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(s);
 		} catch (java.io.FileNotFoundException io) {
 			System.out.println("File Not Found");
 		}
 
 		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
 		param.setQuality(0.5f, false);
 		encoder.setJPEGEncodeParam(param);
 
 		try {
 			encoder.encode(bi);
 			out.close();
 		} catch (java.io.IOException io) {
 			System.out.println("IOException");
 		}
 	}
 
 	public ArrayList<String> images;
 	public ArrayList<Integer> images_used;
 	
 	public ArrayList<Integer> images_lastadded; // Last added, intergers refering to images
 	public int lastadded_max = 20; // How many images is considered "lastadded"
 	public int randomImageNum_maxTries = 100;
 	
 	public int lastImg = 0;
 	
 	public ArrayList<Integer> images_nevershown; // New images that are never shown before
 	
 	public int getRandomImageNum () {
 		if(images.size() <= images_used.size())
 			return -1;
 		else
 		{
 			// Calculate, based on random, if we try to get from the new images
 			boolean getFromNewImages;
 			if(Math.random()*100 < percentage_of_new_images)
 			{
 				getFromNewImages = true;
 			}
 			else
 			{
 				getFromNewImages = false;
 			}
 			
 			int i;
 			int j = 0;
 			int tries = 0;
 			while(true && tries < randomImageNum_maxTries)
 			{
 				// Always try the last added pictures
 				if (
 						images_nevershown.size() > 0 &&
 						tries < (int)(randomImageNum_maxTries/4) // Only use 1/4 of the tries here
 					)
 				{
 					i = images_nevershown.get(0);
 					tries++;
 				}
 				else if (
 						getFromNewImages &&
 						images_lastadded.size() > 0 &&
 						tries < (int)(randomImageNum_maxTries/2) // Only use 1/2 of the tries here
 					)
 				{
 					j = (int)(images_lastadded.size() * Math.random());
 					i = images_lastadded.get(j);
 					tries++;
 				}
 				else
 				{
 					// Random from the rest of the pictures
 					i = (int)(Math.random() * images.size());
 					tries++;
 				}
 				if(!images_used.contains((Integer)i))
 					return i;
 			}
 			
 			System.out.println("Max tries in randomImageNum");
 			return -1;
 		}
 		
 		// Original:
 		//return (int)(Math.random() * images.size());
 	}
 
 	class WebcamCaptureAndFadeImagePanel extends JPanel
 	{
 	
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		
 		// Grid of images, x and y
 		public int grid_x;
 		public int grid_y;
 		
 		// Size for each image, x and y
 		public int size_x;
 		public int size_y;
 
 		public float[][] fade; // How much fading
 		public int[][] wait; // Counter for wait time
 		public int[][] redborder; // Counter for red border on new images
 
 		public int[][]imagenum_now; // Images for the panel, showing now
 		public int[][]imagenum_next; // Images for the panel, fading in
 		//int refers to images in parent object
 		
 		public Image[][]imagenum_now2;
 		public Image[][]imagenum_next2;
 		
 		public WebcamCaptureAndFadeImagePanel (int grid_x, int grid_y, int size_x, int size_y) {
 			
 			// Set to object
 			this.grid_x = grid_x;
 			this.grid_y = grid_y;
 			
 			this.size_x = size_x;
 			this.size_y = size_y;
 			
 			// init tables
 			imagenum_now    = new int[this.grid_x][this.grid_y];
 			imagenum_next   = new int[this.grid_x][this.grid_y];
 			imagenum_now2   = new Image[this.grid_x][this.grid_y];
 			imagenum_next2  = new Image[this.grid_x][this.grid_y];
 			fade = new float[this.grid_x][this.grid_y];
 			wait = new int[this.grid_x][this.grid_y];
 			redborder = new int[this.grid_x][this.grid_y];
 			
 			
 			for (int i = 0; i < imagenum_now.length; i++) {
 				for (int j = 0; j < imagenum_now[i].length; j++) {
 					imagenum_now[i][j]    = getRandomImageNum();
 					images_used.add((Integer)imagenum_now[i][j]);
 					imagenum_next[i][j]   = getRandomImageNum();
 					images_used.add((Integer)imagenum_next[i][j]);
 
 					fade[i][j]            = 0.1f*i*j;
 					redborder[i][j]       = 0;
 					
 					wait[i][j]            = (int)(number_of_frames_showimage*Math.random());
 					
 					imagenum_now2[i][j]   = getImage(imagenum_now[i][j]);
 					imagenum_next2[i][j]  = getImage(imagenum_next[i][j]);
 				}
 			}
 			
 			setPreferredSize(
 					new Dimension(
 						this.grid_x*size_x, 
 						this.grid_y*size_y
 					)
 				);
 		}
 		
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			
 			Graphics2D g2 = (Graphics2D) g;
 
 			AlphaComposite ac, ac2 = null;
 			ac2 = AlphaComposite.getInstance(
 					AlphaComposite.SRC_OVER,
 					1.0f
 				);
 			for (int i = 0; i < imagenum_now.length; i++) {
 				for (int j = 0; j < imagenum_now[i].length; j++) {
 					//System.out.println("x: " + i + ", y: " + j);
 					
 					g2.setComposite(ac2);
 					
 					if(imagenum_now[i][j] != -1)
 					{
 						g2.drawImage(
 								//images.get(imagenum_now[i][j])
 								//getImage(images.get(imagenum_now[i][j])),
 								imagenum_now2[i][j],
 								i*size_x,
 								j*size_y,
 								size_x,
 								size_y,
 								this);
 					}
 					else
 					{
 						// No picture found, ignoring
 						//System.out.println("imagenum_now["+i+"]["+j+"] = -1");
 					}
 					
 					try {
 						ac = AlphaComposite.getInstance(
 							AlphaComposite.SRC_OVER,
 							fade[i][j]
 							);
 					}
 					catch (IllegalArgumentException e) {
 						ac = AlphaComposite.getInstance(
 								AlphaComposite.SRC_OVER,
 								1.0f
 								);
 					}
 					g2.setComposite(ac);
 					if(imagenum_next[i][j] != -1)
 					{
 						g2.drawImage(
 								//images.get(imagenum_next[i][j]),
 								//getImage(images.get(imagenum_next[i][j])), 
 								imagenum_next2[i][j],
 								i*size_x,
 								j*size_y,
 								size_x,
 								size_y,
 								this);
 					}
 					else
 					{
 						// No picture found, ignoring
 						//System.out.println("imagenum_now["+i+"]["+j+"] = -1");
 					}
 					
 					/*
 					if(i == 0 && j == 0)
 						System.out.println("" + imagenum_now[i][j] + 
 								" => " + 
 								imagenum_next[i][j] + ", fade: "+fade[i][j]);;
 					*/
 					
 					// Red border if the image is new
 					if(
 							number_of_frames_redborder != -1 &&
 							(
 								images_nevershown.contains((Integer)imagenum_next[i][j]) ||
 								images_nevershown.contains((Integer)imagenum_now[i][j])
 							)
 						)
 					{
 						g2.setComposite(ac2);
 						g2.setColor(color_redborder);
 						int bordertime = redborder[i][j];
 						if(bordertime > 0)
 						{
 							// Draw border
 							g2.drawRect(i*size_x, j*size_y, size_x-1, size_y-1);
 							
 							if(bordertime > number_of_frames_redborder)
 							{
 								// No more border
 								redborder[i][j] = -number_of_frames_redborder;
 							}
 						}
 						redborder[i][j]++;
 					}
 				}
 			}
 		}
 		
 		public void nextFrame() {
 			int tmp;
 			for (int i = 0; i < imagenum_next.length; i++) {
 				
 				for (int j = 0; j < imagenum_next[i].length; j++) {
 					
 					if(fade[i][j] >= 1.0f)
 					{
 						// Finished fading => Start on the next image
 						
 						// Getting next image
 						tmp = getRandomImageNum();
 						// (doing this before removing to not get the same
 						// image twice)
 						
 						// Removing from images used
 						images_used.remove((Integer)imagenum_now[i][j]);
 						
 						// Removing from never shown since it now has been shown
 						images_nevershown.remove((Integer)imagenum_now[i][j]);
 						
 						// Setting the image that faded in to the now showing now
 						imagenum_now[i][j]  = imagenum_next[i][j];
 						imagenum_now2[i][j] = imagenum_next2[i][j];
 						fade[i][j] = 0.0f; // Will show imagenum_now2[i][j]
 						
 						// Setting the next image to the one that are fading in
 						imagenum_next[i][j] = tmp; // Integer
 						imagenum_next2[i][j] = getImage(tmp); // Image
 						
 						// Adding the new image
 						images_used.add((Integer)imagenum_next[i][j]);
 						
 						// Setting wait time
 						wait[i][j] = number_of_frames_showimage; // Number of frames to wait
 					}
 					else if (wait[i][j] <= 0)
 					{
 						fade[i][j] += 0.05f;
 						if(
 								enable_datetext && 
 								fade[i][j] > 0.50f && 
 								imagenum_next[i][j] != -1)
 						{
 							String path = images.get(imagenum_next[i][j]);
 							if(path.lastIndexOf("cam") != -1)
 							{
 								path = path.substring(path.lastIndexOf("cam")+3);
 								//20100309
 								datetext.setText(
 										path.substring(6, 8) + "." +
 										path.substring(4, 6) + "." +
 										path.substring(0, 4));
 							}
 						}
 					}
 					else
 					{
 						wait[i][j]--; // Wait a little longer
 					}
 					
 				}
 			}
 			repaint();
 		}
 		
 		/**
 		 * Forces a new image if one of the images are not doing a fade
 		 */
 		public void forceNewImage()
 		{
 			int tmp;
 			boolean foundNew = false;
 			for (int i = 0; i < imagenum_next.length; i++) {
 				
 				for (int j = 0; j < imagenum_next[i].length; j++) {
 					
 					if(!foundNew && wait[i][j] > 0)
 					{
 						// Not fading => switch the next image with a new one
 						
 						// Getting next image
 						tmp = getRandomImageNum();
 						// (doing this before removing to not get the same
 						// image twice)
 						
 						// Removing from images used
 						images_used.remove((Integer)imagenum_next[i][j]);
 						
 						// Setting the next image to the one that are fading in
 						imagenum_next[i][j] = tmp; // Integer
 						imagenum_next2[i][j] = getImage(tmp); // Image
 						
 						// Adding the new image
 						images_used.add((Integer)imagenum_next[i][j]);
 						
 						foundNew = true;
 					}
 					
 				}
 			}
 			repaint();
 		}
 	}
 	
 	public int framenr = 0;
 	public class frameUpdateTask extends TimerTask
 	{
 		@Override
 		public void run() {
 			framenr++;
 			nextFrame();
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyCode() == 67) // C 
 		{
 			if(captureWindow)
 			{
 				this.openCaptureWindow();
 			}
 			else
 			{
 				this.captureImage();
 			}
 		}
 		
 		else if(e.getKeyCode() == 27) // Escape
 		{
 			System.out.println("Escape pressed, exiting");
 			System.exit(0);
 		}
 		
 		else if(e.getKeyCode() == 84) // t
 		{
 			// Testing purpose
 			for (int i = 0; i < images.size(); i++) {
 				System.out.println("image " + i + ", used? " + 
 						images_used.contains((Integer)i));
 			}
 			
 		}
 		
 		else if(e.getKeyCode() == 89) // y
 		{
 			// Testing purpose
 			System.out.println("getRandomImageNum() = " + getRandomImageNum());
 		}
 		
 		else if(e.getKeyCode() == 73) // i
 		{
 			// Testing purpose
 			System.out.println("LAST ADDED");
 			for (int i = 0; i < images_lastadded.size(); i++) {
 				System.out.println(i + " - image " + images_lastadded.get(i) + ", used? " + 
 						images_used.contains(
 								(Integer)images_lastadded.get(i)
 							));
 			}
 		}
 		
 		else if(e.getKeyCode() == 85) // u
 		{
 			// Testing purpose
 			for (int i = 0; i < imagepanels.length; i++) {
 				for (int j = 0; j < imagepanels[i].imagenum_now.length; j++) {
 					for (int j2 = 0; j2 < imagepanels[i].imagenum_now[j].length; j2++) {
 						String print1;
 						if(imagepanels[i].imagenum_now[j][j2] < 10)
 							print1 = "  " + imagepanels[i].imagenum_now[j][j2];
 						else if(imagepanels[i].imagenum_now[j][j2] < 100)
 							print1 = " " + imagepanels[i].imagenum_now[j][j2];
 						else
 							print1 = "" + imagepanels[i].imagenum_now[j][j2];
 						String print2;
 						if(imagepanels[i].imagenum_next[j][j2] < 10)
 							print2 = "  " + imagepanels[i].imagenum_next[j][j2];
 						else if(imagepanels[i].imagenum_next[j][j2] < 100)
 							print2 = " " + imagepanels[i].imagenum_next[j][j2];
 						else
 							print2 = "" + imagepanels[i].imagenum_next[j][j2];
 						
 						System.out.println("imagepanels["+i+"]." +
 								"imagenum_now["+j+"]["+j2+"] = " +
 								print1 + 
 								", next = " + print2);
 					}
 				}
 			}
 		}
 		
 		else {
 			displayInfo(e, "KEY TYPED: ");
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 	}
 	
 	private void displayInfo(KeyEvent e, String keyStatus) {
 		// Method copied from http://java.sun.com/docs/books/tutorial/uiswing/events/keylistener.html
 
 		//You should only rely on the key char if the event
 		//is a key typed event.
 		int id = e.getID();
 		String keyString;
 		if (id == KeyEvent.KEY_TYPED) {
 			char c = e.getKeyChar();
 			keyString = "key character = '" + c + "'";
 		} else {
 			int keyCode = e.getKeyCode();
 			keyString = "key code = " + keyCode + " ("
 					+ KeyEvent.getKeyText(keyCode) + ")";
 		}
 
 		int modifiersEx = e.getModifiersEx();
 		String modString = "extended modifiers = " + modifiersEx;
 		String tmpString = KeyEvent.getModifiersExText(modifiersEx);
 		if (tmpString.length() > 0) {
 			modString += " (" + tmpString + ")";
 		} else {
 			modString += " (no extended modifiers)";
 		}
 
 		String actionString = "action key? ";
 		if (e.isActionKey()) {
 			actionString += "YES";
 		} else {
 			actionString += "NO";
 		}
 
 		String locationString = "key location: ";
 		int location = e.getKeyLocation();
 		if (location == KeyEvent.KEY_LOCATION_STANDARD) {
 			locationString += "standard";
 		} else if (location == KeyEvent.KEY_LOCATION_LEFT) {
 			locationString += "left";
 		} else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
 			locationString += "right";
 		} else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
 			locationString += "numpad";
 		} else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
 			locationString += "unknown";
 		}
 
 		// Added:
 		System.out.println("Keypress: " + keyString);
 	}
 	
 	public String getDateFormatNow(String dateFormat)
 	{
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		return sdf.format(Calendar.getInstance().getTime());
 	}
 	
 	Timer cwTimer;
 	public void openCaptureWindow ()
 	{
 		// Capture Window
 		cw.setVisible(true);
 		cw.toFront();
 		
 		// Timer for closing the capturewindow
 		TimerTask task = new TimerTask () {
 			
 			@Override
 			public void run() {
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						System.out.println("Closing cw...");
 						cw.setVisible(false);
 						cwText.setText(""); // Empty text in case there is a text
 					}
 				});
 			}
 		};
 		cwTimer = new Timer();
 		cwTimer.schedule(task, number_of_second_capturewindow*1000);
 	}
 	
 	public class captureWindowKeyListner implements KeyListener
 	{
 		Timer timer;
 		
 		@Override
 		public void keyPressed(KeyEvent arg0) {
 			if(arg0.getKeyCode() == 67) // C 
 			{
 				// Are we allowed to capture a new image?
 				if(framenr - lastcapture_framenr > number_of_frames_betweencaptures)
 				{
 					captureImage();
 					cw.setVisible(false);
 					if(timer != null)
 						timer.cancel();
 					cwText.setText(""); // Empty text
 					lastcapture_framenr = framenr;
 					cwTimer.cancel();
 				}
 				else
 				{
 					//Console debug:
 					//System.out.println("At framenr " + framenr + ", " + (framenr-lastcapture_framenr) +
 					//		"frames has passed, should be " +number_of_frames_betweencaptures);
 					//System.out.println("You must wait an other "+
 					//		(int)Math.ceil(((double)number_of_frames_betweencaptures-(double)(framenr-lastcapture_framenr))/fps) +
 					//		" seconds");
 					
 					TimerTask task = new TimerTask () {
 						
 						boolean finished = false;
 						
 						@Override
 						public void run() {
 							EventQueue.invokeLater(new Runnable() {
 								public void run() {
 									if(!finished)
 									{
 										if((int)Math.ceil(((double)number_of_frames_betweencaptures-(double)(framenr-lastcapture_framenr))/fps) > 0)
 										{
 											cwText.setText("Du m vente "+
 													(int)Math.ceil(((double)number_of_frames_betweencaptures-(double)(framenr-lastcapture_framenr))/fps) +
 													" sekunder fr nytt bilde");
 										}
 										else
 										{
 											finished = true;
 											cwText.setText("Du kan n ta nytt bilde");
 										}
 									}
 								}
 							});
 						}
 					};
 					timer = new Timer();
 					timer.schedule(task, 0, (1000/fps)); // Update for every frame
 				}
 			}
 			
 			else if(arg0.getKeyCode() == 27) // Escape
 			{
 				System.out.println("Escape pressed, exiting");
 				System.exit(0);
 			}
 			
 		}
 
 		@Override
 		public void keyReleased(KeyEvent arg0) {
 		}
 
 		@Override
 		public void keyTyped(KeyEvent arg0) {
 		}
 		
 	}
 	
 	public class rotatedText extends JFrame
 	{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private boolean rotate = false;
 		
 		public String text;
 		protected JComponent textlabel;
 		
 		public int sizeText_x = 18;
 		public int sizeText_y = size_y;
 		
 		public rotatedText (String text) {
 			
 			textlabel = new rotatedTextLabel();
 			add(textlabel);
 			
 			setSize(sizeText_x, sizeText_y);
 			setUndecorated(true);
 			setAlwaysOnTop(true);
 			
 			this.setText(text);
 		}
 		
 		public void setText (String txt)
 		{
 			text = txt;
 			textlabel.repaint();
 			if(!text.equals(""))
 			{
 				if(!isVisible())
 				{
 					// Only setVisible if its needed
 					// or the focus window will change
 					toFront();
 					setVisible(true);
 				}
 				
 				// Give focus to capture window if its there
 				if(cw.isVisible())
 					cw.requestFocus();
 			}
 			else
 			{
 				// Hide window
 				setVisible(false);
 			}
 		}
 		
 		public class rotatedTextLabel extends JComponent
 		{
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 			
 			public void paintComponent (Graphics g)
 			{
 				super.paintComponent(g);
 				
 				Graphics2D g2 = (Graphics2D)g;
 				
 				if(!text.equals(""))
 				{
 				g2.translate(0, getSize().getHeight());
 					g2.rotate(-Math.PI/2);
 					g2.setColor(Color.red);
 					g2.fillRect(0, 0, sizeText_y, sizeText_x);
 					g2.setColor(Color.white);
 					g2.drawString(text, 20, 14);
 		
 					g2.translate(0, -getSize().getHeight());
 					g2.transform(AffineTransform.getQuadrantRotateInstance(1));
 				}
 			}
 			
 			public Dimension getSize() {
 				if(rotate)
 					return new Dimension(super.getSize().height, super.getSize().width);
 				else
 					return super.getSize();
 			}
 			
 			public Dimension getPreferredSize()
 			{
 				return this.getSize();
 			}
 
 			public int getHeight () {
 				return this.getSize().height;
 			}
 			public int getWidth () {
 				return this.getSize().width;
 			}
 		}
 	}
 	
 	public class rotatedText2 extends JComponent
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private boolean rotate = false;
 		
 		public String text;
 		protected JComponent textlabel;
 		
 		public rotatedText2 (String text) {
 			this.setText(text);
 		}
 		
 		public void setText (String txt)
 		{
 			text = txt;
 			repaint();
 		}
 		
 		public void paintComponent (Graphics g)
 		{
 			super.paintComponent(g);
 			
 			Graphics2D g2 = (Graphics2D)g;
 			
 			if(!text.equals(""))
 			{
 			g2.translate(0, getSize().getHeight());
 				g2.rotate(-Math.PI/2);
 				g2.setColor(Color.black);
 				g2.drawString(text, 20, 14);
 	
 				g2.translate(0, -getSize().getHeight());
 				g2.transform(AffineTransform.getQuadrantRotateInstance(1));
 			}
 		}
 		
 		public Dimension getSize() {
 			if(rotate)
 				return new Dimension(super.getSize().height, super.getSize().width);
 			else
 				return super.getSize();
 		}
 		
 		public Dimension getPreferredSize()
 		{
 			return this.getSize();
 		}
 
 		public int getHeight () {
 			return this.getSize().height;
 		}
 		public int getWidth () {
 			return this.getSize().width;
 		}
 	}
 }
