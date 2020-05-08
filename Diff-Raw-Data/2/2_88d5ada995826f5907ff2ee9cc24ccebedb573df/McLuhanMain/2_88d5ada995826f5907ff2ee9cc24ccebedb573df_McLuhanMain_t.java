 package com.navmenu;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Robot;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Date;
 import java.util.TimerTask;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.media.opengl.awt.GLJPanel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 
 import org.pirelenito.movieGL.Main;
 
 import com.helper.GlassPane;
 import com.helper.MP3;
 import com.helper.MenuBuilder;
 import com.helper.TextureMap;
 import com.helper.TopSlider;
 import com.jogamp.opengl.util.FPSAnimator;
 import com.physics.HandObject;
 import com.physics.PhysicsEngine;
 import com.physics.PhysicsGrabber;
 
 /**
  * 
  * The main interface and none physics portion of the MIME system.
  * Uses a card layout to progress between Grabber, Loading, Theme Selection and Interaction 
  * system states. 
  * 
  */
 @SuppressWarnings("unused")
 public class McLuhanMain extends JFrame{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1739205605051932874L;
 
 	//McLuhan themes list
 	private String[] DIRS = {"City as Classroom","Extensions of Man","Global Village","The Medium is the Message"};
 
 	private JFrame frame;
 	//private GLCanvas canvas,vidc,grabc;
 	private GLJPanel canvas,vidc,grabc;
 	//TODO convert glnav to class to handle menu grabbing/interaction in gl scene. Might need to be added as an extension to the main gl scene
 	private JPanel menu,glnav;
 
 	private BackgroundPanel p1,p2,p3,p4,load;
 
 	private JButton pmen, movietest,aud;
 
 	private JButton[] btns,btns1,btns2,btns3,btns4;
 
	private Dimension size = new Dimension(1024,768);
 
 	private FPSAnimator animator, animator2;
 	private PhysicsEngine app;
 	private PhysicsGrabber grabber;
 
 	private Main video;
 
 	private Timer quepush, ploop, steadyTimer, depthTimer;//flash, starter;
 	
 	private int isSteady, isDepth;
 
 	private boolean roll, audio;
 
 	private ActionListener gogo, repeat, one, two, three, four, loop, steadyAL, depthAL;
 
 	private TextureMap themes;
 
 	private MP3 soundbite;
 
 	private TopSlider tslide;
 
 	private static Robot mouseRobot;
 
 	public int mode;
 	
 	public HandObject[] handArray;
 
 	private int bgsel;
 
 	private MenuBuilder backsnbtns;
 
 	private JLayeredPane layeredPane;
 	
 	private GlassPane hands;
 	
 	private int pushDepth = 2000;
 
 	public McLuhanMain() {
 		frame = this;
 		frame.setName("McLuhan Server");
 		layeredPane = frame.getLayeredPane();
 		layeredPane.setLayout(new BorderLayout());
 		frame.setSize(size);
 		this.setUndecorated(true);
 		bgsel =1;
 		menu = new JPanel();
 		hands = new GlassPane();
 		hands.setOpaque(false);
 		
 		CardLayout system = new CardLayout();
 		menu.setLayout(system);
 
 		backsnbtns = new MenuBuilder();
 		
 		initGrabber();
 		initTSlide();
 		initTData();
 		initSelections();
 		initSelections2();
 		initSelections3();
 		initSelections4();
 		initLoading();
 		initGLNav();
 		
 		//hands.setHandOne(new Point(525,525));
 		
 		btns = btns1;
 		menu.add("Grabber",grabc);
 		menu.add("1",p1);
 		menu.add("2",p2);
 		menu.add("3",p3);
 		menu.add("4",p4);
 		menu.add("Loader",load);
 		layeredPane.add(tslide,BorderLayout.NORTH,-30000);
 		layeredPane.add(menu,BorderLayout.CENTER,-30000);
 		layeredPane.add(glnav,BorderLayout.WEST,-30000);
 		//frame.getContentPane().add(BorderLayout.NORTH,tslide);
 		//frame.getContentPane().add(BorderLayout.CENTER,menu);
 		//frame.getContentPane().add(BorderLayout.WEST,glnav);
 		frame.setGlassPane(hands);
 		frame.getGlassPane().setVisible(true);
 		//frame.getContentPane().add(BorderLayout.CENTER,hands);
 
 		glnav.setVisible(false);
 		tslide.setVisible(false);
 
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 
 		//creates the flash sequence and theme menu loops
 		//roll = true;
 		//initFlshSeq();
 
 		/*gogo = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				roll = true;
 				flash = new Timer(3000,one);
 				flash.setRepeats(false);
 				flash.start();
 			}
 		};*/
 
 		loop = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				setPage();
 				setBtns(bgsel);
 					//flash.stop();
 					//starter.stop();
 				//starter.start();
 				((CardLayout)menu.getLayout()).show(menu, bgsel+"");
 			}
 		};
 
 	//	starter = new Timer(1000,gogo);
 		//starter.setRepeats(false);
 		//starter.start();
 
 		ploop = new Timer(45000,loop);
 		//ploop.start();
 
 
 		// Create a new mouse robot
 		try {
 			mouseRobot = new Robot();
 		} catch (AWTException awte) {
 			awte.printStackTrace();
 		}
 
 		// Init mode
 		mode = 0;
 		
 		HandObject handOne = new HandObject(0,0,1,0, false);
 		HandObject handTwo = new HandObject(0,0,2,0, false);
 		
 		handArray = new HandObject[2];
 		
 		handArray[0] = handOne;
 		handArray[1] = handTwo;
 		
 		steadyAL = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				isSteady = 0;
 			}
 		};
 		
 		isSteady = 0;
 		
 		steadyTimer = new Timer(1000 * 2, steadyAL);
 		
 		depthAL = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				isDepth = 0;
 			}
 		};
 		
 		isDepth = 0;
 		
 		depthTimer = new Timer(1000 * 2, depthAL);
 	}
 	
 	/*
 	 * Setup the grabber effect scene
 	 */
 	private void initGrabber() {
 		GLProfile glp = GLProfile.getDefault();
 		GLCapabilities caps = new GLCapabilities(glp);
 		//grabc = new GLCanvas(caps);
 		grabc = new GLJPanel(caps);
 		
 		grabber = new PhysicsGrabber();
 		grabc.addGLEventListener(grabber);
 		grabc.addKeyListener(grabber);
 		grabc.addMouseListener(grabber);
 		grabc.addMouseMotionListener(grabber);
 
 		grabc.requestFocus();
 		
 		activateGrabber();
 
 	}
 
 	private void initTSlide() {
 		tslide = new TopSlider();
 	}
 
 	/*
 	 * Creates the systems file and texture data
 	 */
 	private void initTData() {
 		themes = new TextureMap();
 	}
 
 	/*  
 	 * 	Timers used to loop through the theme menu button flash effect and
 	 * theme menu change loop. Sequence progresses from One - Four and then Repeats
 	 * 
 	 * Flashing is achieved by forcing the button modals rollover effect to occur.
 	 */
 	/*
 	private void initFlshSeq() {
 		one = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[0].getModel().setRollover(false);
 				if(roll){
 					btns[0].getModel().setRollover(true);
 					flash = new Timer(3000,two);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 
 		two = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[0].getModel().setRollover(false);
 				if(roll){
 					btns[1].getModel().setRollover(true);
 					flash = new Timer(3000,three);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 
 		three = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[1].getModel().setRollover(false);
 				if(roll){
 					btns[2].getModel().setRollover(true);
 					flash = new Timer(3000,four);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 
 		four = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[2].getModel().setRollover(false);
 				if(roll){
 					btns[3].getModel().setRollover(true);
 					flash = new Timer(3000,repeat);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 
 		repeat = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[3].getModel().setRollover(false);
 				if(roll){
 					flash = new Timer(15000,one);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 
 
 	}
 	*/
 
 	/*
 	 * Generates and starts the base theme selection loop, for each theme option 
 	 * sets up its action call for the specified data objects.
 	 * 
 	 */
 	private void initSelections() {
 		btns1 = backsnbtns.getGroup(1);
 		p1 = backsnbtns.getBack(1);
 		for(int i=0;i<4;i++){
 			btns1[i].setName(i+"");
 			//launches the required opengl scene for the intedned theme selections
 			btns1[i].addActionListener(new ActionListener(){
 				public void actionPerformed(final ActionEvent ae) {
 					int opt = Integer.parseInt(((JButton)ae.getSource()).getName());
 					roll = false;
 					ploop.stop();
 				//	if(flash!=null)
 				//		flash.stop();
 				//	starter.stop();
 					menu.validate();
 					paintSlide(opt);
 					initCanvas(opt);
 					menu.add("ogl",canvas);
 					soundbite = new MP3("McLuhan/"+DIRS[Integer.parseInt(((JButton)ae.getSource()).getName())]+"/mp3.mp3");
 					pmen.setEnabled(true);
 					pmen.setVisible(true);
 					aud.setEnabled(true);
 					aud.setVisible(true);
 					audio = true;
 					aud.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							if(audio){
 								soundbite.pause();
 								audio = false;
 								aud.setIcon(new ImageIcon("mute.jpg"));
 							}
 							else{
 								audio=true;
 								soundbite.resume();
 								aud.setIcon(new ImageIcon("play.jpg"));
 							}
 							aud.validate();	
 						}
 
 					});
 
 					menu.validate();
 					activateAnimation();
 					((CardLayout)menu.getLayout()).show(menu, "Loader");
 					ActionListener time = new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							glnav.setVisible(true);
 							tslide.setVisible(true);
 							((CardLayout)menu.getLayout()).show(menu, "ogl");
 							soundbite.play();
 							app.setSound(soundbite);
 						}
 					};
 					Timer t = new Timer(1000,time);
 					t.setRepeats(false);
 					t.start();
 
 					// Change mode
 					mode = 2;
 				}});
 
 		}
 		for(int i=0;i<4;i++){
 			p1.add(btns1[i]);
 		}
 
 	}
 
 	private void setBtns(int i){
 		if(i==1)
 			btns = btns1;
 		else if(i== 2)
 			btns = btns2;
 		else if(i== 3)
 			btns = btns3;
 		else if(i==4)
 			btns = btns4;
 	}
 
 	private void setPage(){
 		if(bgsel == 4)
 			bgsel =1;
 		else
 			bgsel++;
 	}
 
 	/*
 	 * Updates the top slider to have the required texture data
 	 */
 	private void paintSlide(int opt) {
 		tslide.letsFight(themes.getFiles(opt*3), themes.getFiles((opt*3)+1));
 
 	}
 
 
 	public void activateOGL(){
 		activatecanvas();
 	}
 
 	private void activatecanvas(){
 		pmen.setEnabled(true);
 		pmen.setVisible(true);
 		menu.validate();
 		activateAnimation();
 		((CardLayout)menu.getLayout()).show(menu, "ogl");
 		canvas.validate();
 		hands.repaint();
 	}
 
 	private void activateGrabber(){
 		animator2 = new FPSAnimator(60);
 		animator2.add(grabc);
 		animator2.start();
 	}
 
 
 	/*
 	 * The left side navigation window for muting/playing the
 	 * background audio and returning to theme selection.
 	 * 
 	 */
 	private void initGLNav(){
 		JPanel dem = new JPanel();
 		dem.setLayout(new GridLayout(0,1));
 		dem.setBackground(Color.black);
 
 		aud = new JButton(new ImageIcon("play.jpg"));
 		aud.setPreferredSize(new Dimension(75,75));
 		aud.setOpaque(false);
 		aud.setContentAreaFilled(false);
 
 		pmen = new JButton(new ImageIcon("home.jpg"));
 		pmen.setPreferredSize(new Dimension(75,75));
 		pmen.setOpaque(false);
 		pmen.setContentAreaFilled(false);
 		//destroys the open gl context and reverts to the main theme selection
 		pmen.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				pmen.setEnabled(false);
 				pmen.setVisible(false);
 				aud.setIcon(new ImageIcon("play.jpg"));
 				aud.setEnabled(false);
 				aud.setVisible(false);
 				aud.removeActionListener(aud.getActionListeners()[0]);
 				setPage();
 				setBtns(bgsel);
 				ploop.stop();
 			//	if(flash !=null)
 			//		flash.stop();
 			//	starter.stop();
 				ploop.start();
 				app.stopVid();
 				((CardLayout)menu.getLayout()).show(menu, bgsel+"");
 					menu.remove(canvas);
 					canvas = null;
 					animator.stop();
 					menu.validate();
 					quepush.stop();
 					soundbite.close();
 					tslide.removeAll();
 					glnav.setVisible(false);
 					tslide.setVisible(false);
 				roll = true;
 				Timer test = new Timer(1000,gogo);
 				test.setRepeats(false);
 				test.start();
 
 				// Change mode
 				if(hands != null){
 					hands.releaseHandOne();
 					hands.releaseHandTwo();
 				}
 				mode = 1;
 			}
 		});
 
 		aud.setEnabled(false);
 		aud.setVisible(false);
 
 		pmen.setEnabled(false);
 		pmen.setVisible(false);
 
 		glnav = new JPanel();
 		glnav.setBackground(Color.black);
 		glnav.setLayout(new FlowLayout());
 		((FlowLayout)glnav.getLayout()).setVgap(20);
 		dem.add(pmen);
 		dem.add(aud);
 		//glnav.add(pmen);
 		glnav.add(dem);
 	}
 
 
 	private void activateAnimation() {
 		animator = new FPSAnimator(60);
 		animator.add(canvas);
 		animator.start();
 		// glnav.setVisible(true);
 		//tslide.setVisible(true);
 
 	}
 
 	private void initLoading(){
 		load = new BackgroundPanel("Loading2.gif",245,95,new Dimension(500,500));
 		load.setLayout(null);
 		load.setSize(size);	
 	}
 
 	/*
 	 * Creates the OpenGL context and assigns the physics engine
 	 * for the specified texture data and begins the queuing loop
 	 * for media changes
 	 */
 	private void initCanvas(int opt) {
 		GLProfile glp = GLProfile.getDefault();
 		GLCapabilities caps = new GLCapabilities(glp);
 		//canvas = new GLCanvas(caps);
 		canvas = new GLJPanel(caps);
 
 		app = new PhysicsEngine(themes.getMap(opt*3), themes.getMap((opt*3)+1), themes.getMap((opt*3)+2),themes.getVids(opt));
 		canvas.addGLEventListener(app);
 		canvas.addKeyListener(app);
 		//canvas.addMouseListener(app);
 	//	canvas.addMouseMotionListener(app);
 		tslide.setEngine(app);
 
 		canvas.requestFocus();
 
 		ActionListener timeout = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				app.callTimer();
 				canvas.display();
 			}
 		};
 		quepush = new Timer(70000,timeout);
 		quepush.setRepeats(true);
 		quepush.start();
 		
 		app.setTimer(quepush);
 		app.setAudio(aud);
 	}
 	
 	/*
 	 * 
 	*/
 	private void startGrabber() {
 		if(canvas != null){
 			pmen.setEnabled(false);
 			pmen.setVisible(false);
 			aud.setIcon(new ImageIcon("play.jpg"));
 			aud.setEnabled(false);
 			aud.setVisible(false);
 			aud.removeActionListener(aud.getActionListeners()[0]);
 			setPage();
 			setBtns(bgsel);
 			ploop.stop();
 		//	if(flash !=null)
 		//		flash.stop();
 		//	starter.stop();
 			app.stopVid();
 			menu.remove(canvas);
 			canvas = null;
 			animator.stop();
 			menu.validate();
 			quepush.stop();
 			soundbite.close();
 			tslide.removeAll();
 			glnav.setVisible(false);
 			tslide.setVisible(false);
 			roll = true;
 		}
 	//	if(starter != null)
 	//		starter.stop();
 	//	if(flash != null)
 	//		flash.stop();
 		if(ploop != null)
 			ploop.stop();
 		initGrabber();
 		if(hands != null){
 			hands.releaseHandOne();
 			hands.disableHandOne();
 			hands.releaseHandTwo();
 			hands.disableHandTwo();
 		}
 		for(int i=0;i<2;i++){
 			handArray[i].setPressed(false);
 		}
 		mode = 0;
 		menu.add("Grabber",grabc);
 		((CardLayout)menu.getLayout()).show(menu, "Grabber");
 	}
 	
 	/*
 	 * 
 	*/
 	private void stopGrabber() {
 		menu.remove(grabc);
 		((CardLayout)menu.getLayout()).show(menu, bgsel+"");
 	//	starter.start();
 		ploop.start();
 		
 		return;
 	}
 	
 	/*
 	  Receive function that handles Kinect data for the attention "grabber" display
 	*/
 	public void recvGrabberData(final float x, final float y, float depth, final int select, final String action) {
 		SwingUtilities.invokeLater(new Runnable( ) {
 			public void run( ) {
 				if (action.compareTo("sessionstart") == 0) {
 					mode = 1;
 					stopGrabber();
 			
 					return;
 				}
 		
 				if (action.compareTo("primarypointcreate") == 0) {
 					handArray[select - 1].setState(1);
 			
 					hands.enableHandOne();
 				}
 				if (action.compareTo("primarypointdestroy") == 0) {
 					handArray[select - 1].setState(0);
 			
 					hands.disableHandOne();
 				}
 				if (action.compareTo("pointcreate") == 0) {
 					handArray[select - 1].setState(1);
 			
 					hands.enableHandTwo();
 				}
 				if (action.compareTo("pointdestroy") == 0) {
 					handArray[select - 1].setState(0);
 			
 					hands.disableHandTwo();
 				}
 		
 				double ratioX = (size.getWidth() + 120) / 640;
 				double ratioY = (size.getHeight() + 160) / 480;
 				int newX = (int) ((x - 40) * ratioX);
 				int newY = (int) ((y - 30) * ratioY);
 		
 				if (newX < 0) {
 					newX = 50;
 				}
 				if (newX > 1024) {
 					newX = 1074;
 				}
 				if (newY < 0) {
 					newY = 50;
 				}
 				if (newY > 768) {
 					newY = 718;
 				}
 		
 				mouseRobot.mouseMove(newX, newY);
 		
 				mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 				mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 			}
 		});
 
 		return;
 	}
 
 	/*
 	  Receive function that handles Kinect data for the theme and menu display
 	*/
 	public void recvThemeData(final float x, final float y, float depth, final int select, final String action) {
 		SwingUtilities.invokeLater(new Runnable( ) {
 			public void run( ) {
 				if (select == 0) {
 					return;
 				}
 				System.out.println(action);
 				if (action.compareTo("sessionend") == 0) {
 					mode = 0;
 					startGrabber();
 			
 					return;
 				}
 		
 				if (action.compareTo("primarypointcreate") == 0) {
 					handArray[select - 1].setState(1);
 			
 					hands.enableHandOne();
 				}
 				if (action.compareTo("primarypointdestroy") == 0) {
 					handArray[select - 1].setState(0);
 			
 					hands.disableHandOne();
 				}
 				if (action.compareTo("pointcreate") == 0) {
 					handArray[select - 1].setState(1);
 			
 					hands.enableHandTwo();
 				}
 				if (action.compareTo("pointdestroy") == 0) {
 					handArray[select - 1].setState(0);
 			
 					hands.disableHandTwo();
 				}
 		
 				if (handArray[select - 1].getState() == 0) {
 					return;
 				}
 		
 				double ratioX = (size.getWidth() + 120) / 640;
 				double ratioY = (size.getHeight() + 160) / 480;
 				int newX = (int) ((x - 40) * ratioX);
 				int newY = (int) ((y - 30) * ratioY);
 		
 				if (newX < 0) {
 					newX = 0;
 				}
 				if (newX > 1024) {
 					newX = 1024;
 				}
 				if (newY < 0) {
 					newY = 0;
 				}
 				if (newY > 768) {
 					newY = 768;
 				}
 		
 				handArray[select - 1].setX(newX);
 				handArray[select - 1].setY(newY);
 
 				if (select == 1) {
 					updateHandOne(new Point(newX, newY));
 				}
 				if (select == 2) {
 					updateHandTwo(new Point(newX, newY));
 				}
 				if (action.compareTo("steady") == 0 ) {
 						switch (select) {
 							case 1:
 								mouseRobot.mouseMove(handArray[select - 1].getX(), handArray[select - 1].getY());
 								mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 								mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 								break;
 							default:
 								break;
 						}
 				}
 
 				if (action.compareTo("push") == 0 && handArray[select - 1].getPressed() == false) {
 					switch (select) {
 						case 1:
 							mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 							mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 							break;
 						default:
 							break;
 					}
 				}
 			}
 		});
 		
 		return;
 	}
 	
 	/*
 	  Receive function that handles Kinect data for the physics engine display
 	*/
 	public void recvPhysicsData(final float x, final float y, final float depth, final int select, final String action) {
 		SwingUtilities.invokeLater(new Runnable( ) {
 			public void run( ) {
 				if (select == 0) {
 					return;
 				}
 				
 				if (action.compareTo("sessionend") == 0) {
 					mode = 0;
 					startGrabber();
 					
 					return;
 				}
 
 				if (action.compareTo("primarypointcreate") == 0) {
 					handArray[select - 1].setState(1);
 
 					hands.enableHandOne();
 				}
 				if (action.compareTo("primarypointdestroy") == 0) {
 					handArray[select - 1].setState(0);
 
 					hands.disableHandOne();
 				}
 				if (action.compareTo("pointcreate") == 0) {
 					handArray[select - 1].setState(1);
 
 					hands.enableHandTwo();
 				}
 				if (action.compareTo("pointdestroy") == 0) {
 					handArray[select - 1].setState(0);
 
 					hands.disableHandTwo();
 				}
 
 				if (handArray[select - 1].getState() == 0) {
 					return;
 				}
 
 				double ratioX = (size.getWidth() + 120) / 640;
 				double ratioY = (size.getHeight() + 160) / 480;
 				int newX = (int) ((x - 40) * ratioX);
 				int newY = (int) ((y - 30) * ratioY);
 
 				if (newX < 0) {
 					newX = 0;
 				}
 				if (newX > 1024) {
 					newX = 1024;
 				}
 				if (newY < 0) {
 					newY = 0;
 				}
 				if (newY > 768) {
 					newY = 768;
 				}
 
 				handArray[select - 1].setX(newX);
 				handArray[select - 1].setY(newY);
 
 				if (select == 1) {
 					updateHandOne(new Point(newX, newY));
 				}
 				if (select == 2) {
 					updateHandTwo(new Point(newX, newY));
 				}
 
 				if (action.compareTo("steady") == 0 && handArray[select - 1].getPressed() == false) {
 					if (isSteady == 0 && (newY < 100 || newX < 60)) {
 						isSteady = 1;
 						steadyTimer.start();
 
 						switch (select) {
 							case 1:
 								mouseRobot.mouseMove(handArray[select - 1].getX(), handArray[select - 1].getY());
 								mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 								mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 								break;
 							default:
 								break;
 						}
 					}
 				}
 
 				if (depth < pushDepth && handArray[select - 1].getPressed() == false) {
 					if (isDepth == 0 && (newY < 100 || newX < 60)) {
 						isDepth = 1;
 						depthTimer.start();
 						
 						mouseRobot.mouseMove(handArray[select - 1].getX(), handArray[select - 1].getY());
 						mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 						mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 					}
 					if (newY > 100 || newX > 60) {
 						if(app.getP() != null){
 							switch (select) {
 								case 1:
 									app.handPressed(new Point(handArray[select - 1].getX(), handArray[select - 1].getY()),true);
 									hands.activeHandOne();
 									break;
 								case 2:
 									app.handPressed(new Point(handArray[select - 1].getX(), handArray[select - 1].getY()),false);
 									hands.activeHandTwo();
 									break;
 							}
 							handArray[select - 1].setPressed(true);
 						}
 					}
 				}
 
 				if (depth < pushDepth && handArray[select - 1].getPressed() == true) {
 					if (isDepth == 0 && (newY < 100 || newX < 60)) {
 						isDepth = 1;
 						depthTimer.start();
 						
 						mouseRobot.mouseMove(handArray[select - 1].getX(), handArray[select - 1].getY());
 						mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 						mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 					}
 					if (newY > 100 || newX > 60) {
 						switch (select) {
 							case 1:
 								app.handDragged(new Point(handArray[select - 1].getX(), handArray[select - 1].getY()),true);
 								break;
 							case 2:
 								app.handDragged(new Point(handArray[select - 1].getX(), handArray[select - 1].getY()),false);
 								break;
 						}
 					}
 				}
 
 				if (depth > pushDepth && handArray[select - 1].getPressed() == true) {
 					if (isDepth == 0 && (newY < 100 || newX < 60)) {
 						isDepth = 1;
 						depthTimer.start();
 						
 						mouseRobot.mouseMove(handArray[select - 1].getX(), handArray[select - 1].getY());
 						mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 						mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 					}
 					if (newY > 100 || newX > 60) {
 						switch (select) {
 							case 1:
 								app.handReleased(new Point(handArray[select - 1].getX(), handArray[select - 1].getY()),true);
 								hands.releaseHandOne();
 								break;
 							case 2:
 								app.handReleased(new Point(handArray[select - 1].getX(), handArray[select - 1].getY()),false);
 								hands.releaseHandTwo();
 								break;
 						}
 						handArray[select - 1].setPressed(false);
 					}
 				}
 			}});
 
 		return;
 		
 	}
 
 	/*
 	 * Generates and starts the base theme selection loop, for each theme option 
 	 * sets up its action call for the specified data objects.
 	 * 
 	 */
 	private void initSelections2() {
 		btns2 = backsnbtns.getGroup(2);
 		p2 = backsnbtns.getBack(2);
 		for(int i=0;i<4;i++){
 			btns2[i].setName(i+"");
 			//launches the required opengl scene for the intedned theme selections
 			btns2[i].addActionListener(new ActionListener(){
 				public void actionPerformed(final ActionEvent ae) {
 					int opt = Integer.parseInt(((JButton)ae.getSource()).getName());
 					roll = false;
 					ploop.stop();
 				//		flash.stop();
 				//	starter.stop();
 					menu.validate();
 					paintSlide(opt);
 					initCanvas(opt);
 					menu.add("ogl",canvas);
 					soundbite = new MP3("McLuhan/"+DIRS[Integer.parseInt(((JButton)ae.getSource()).getName())]+"/mp3.mp3");
 					pmen.setEnabled(true);
 					pmen.setVisible(true);
 					aud.setEnabled(true);
 					aud.setVisible(true);
 					audio = true;
 					aud.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							if(audio){
 								soundbite.close();
 								audio = false;
 								aud.setIcon(new ImageIcon("mute.jpg"));
 							}
 							else{
 								audio=true;
 								soundbite.play();
 								aud.setIcon(new ImageIcon("play.jpg"));
 							}
 							aud.validate();	
 						}
 
 					});
 
 					menu.validate();
 					activateAnimation();
 					((CardLayout)menu.getLayout()).show(menu, "Loader");
 					ActionListener time = new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							glnav.setVisible(true);
 							tslide.setVisible(true);
 							((CardLayout)menu.getLayout()).show(menu, "ogl");
 							soundbite.play();
 							app.setSound(soundbite);
 						}
 					};
 					Timer t = new Timer(1000,time);
 					t.setRepeats(false);
 					t.start();
 
 					// Change mode
 					mode = 2;
 				}});
 
 		}
 		for(int i=0;i<4;i++){
 			p2.add(btns2[i]);
 		}
 
 	}
 	/*
 	 * Generates and starts the base theme selection loop, for each theme option 
 	 * sets up its action call for the specified data objects.
 	 * 
 	 */
 	private void initSelections3() {
 		btns3 = backsnbtns.getGroup(3);
 		p3 = backsnbtns.getBack(3);
 		for(int i=0;i<4;i++){
 			btns3[i].setName(i+"");
 			//launches the required opengl scene for the intedned theme selections
 			btns3[i].addActionListener(new ActionListener(){
 				public void actionPerformed(final ActionEvent ae) {
 					int opt = Integer.parseInt(((JButton)ae.getSource()).getName());
 					roll = false;
 					ploop.stop();
 				//		flash.stop();
 				//	starter.stop();
 					menu.validate();
 					paintSlide(opt);
 					initCanvas(opt);
 					menu.add("ogl",canvas);
 					soundbite = new MP3("McLuhan/"+DIRS[Integer.parseInt(((JButton)ae.getSource()).getName())]+"/mp3.mp3");
 					pmen.setEnabled(true);
 					pmen.setVisible(true);
 					aud.setEnabled(true);
 					aud.setVisible(true);
 					audio = true;
 					aud.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							if(audio){
 								soundbite.close();
 								audio = false;
 								aud.setIcon(new ImageIcon("mute.jpg"));
 							}
 							else{
 								audio=true;
 								soundbite.play();
 								aud.setIcon(new ImageIcon("play.jpg"));
 							}
 							aud.validate();	
 						}
 
 					});
 
 					menu.validate();
 					activateAnimation();
 					((CardLayout)menu.getLayout()).show(menu, "Loader");
 					ActionListener time = new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							glnav.setVisible(true);
 							tslide.setVisible(true);
 							((CardLayout)menu.getLayout()).show(menu, "ogl");
 							soundbite.play();
 							app.setSound(soundbite);
 						}
 					};
 					Timer t = new Timer(1000,time);
 					t.setRepeats(false);
 					t.start();
 
 					// Change mode
 					mode = 2;
 				}});
 
 		}
 		for(int i=0;i<4;i++){
 			p3.add(btns3[i]);
 		}
 
 	}
 	/*
 	 * Generates and starts the base theme selection loop, for each theme option 
 	 * sets up its action call for the specified data objects.
 	 * 
 	 */
 	private void initSelections4() {
 		btns4 = backsnbtns.getGroup(4);
 		p4 = backsnbtns.getBack(4);
 		for(int i=0;i<4;i++){
 			btns4[i].setName(i+"");
 			//launches the required opengl scene for the intedned theme selections
 			btns4[i].addActionListener(new ActionListener(){
 				public void actionPerformed(final ActionEvent ae) {
 					ploop.stop();
 			//			flash.stop();
 			//		starter.stop();
 					int opt = Integer.parseInt(((JButton)ae.getSource()).getName());
 					roll = false;
 					menu.validate();
 					paintSlide(opt);
 					initCanvas(opt);
 					menu.add("ogl",canvas);
 					soundbite = new MP3("McLuhan/"+DIRS[Integer.parseInt(((JButton)ae.getSource()).getName())]+"/mp3.mp3");
 					pmen.setEnabled(true);
 					pmen.setVisible(true);
 					aud.setEnabled(true);
 					aud.setVisible(true);
 					audio = true;
 					aud.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							if(audio){
 								soundbite.close();
 								audio = false;
 								aud.setIcon(new ImageIcon("mute.jpg"));
 							}
 							else{
 								audio=true;
 								soundbite.play();
 								aud.setIcon(new ImageIcon("play.jpg"));
 							}
 							aud.validate();	
 						}
 
 					});
 
 					menu.validate();
 					activateAnimation();
 					((CardLayout)menu.getLayout()).show(menu, "Loader"); 
 					ActionListener time = new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							glnav.setVisible(true);
 							tslide.setVisible(true);
 							((CardLayout)menu.getLayout()).show(menu, "ogl");
 							soundbite.play();
 							app.setSound(soundbite);
 						}
 					};
 					Timer t = new Timer(1000,time);
 					t.setRepeats(false);
 					t.start();
 
 					// Change mode
 					mode = 2;
 				}});
 
 		}
 		for(int i=0;i<4;i++){
 			p4.add(btns4[i]);
 		}
 
 	}
 	
 	private void updateHandOne(Point p){
 		hands.setHandOne(p);
 		if(canvas == null){
 			mouseRobot.mouseMove(p.x, p.y);
 		}
 	}
 	
 	private void updateHandTwo(Point p){
 		hands.setHandTwo(p);
 	}
 
 	
 
 }
