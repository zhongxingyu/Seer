 /*
  * Created on Oct 21, 2003
  *
  * This program is free software; you can redistribute it and/or modify it under 
  * the terms of the GNU General Public License as published by the Free Software 
  * Foundation; either version 2, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with 
  * this software; see the file COPYING. If not, write to the Free Software 
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  * As a special exception, Derone Bryson and the StopMojo Project gives 
  * permission for additional uses of the text contained in its release of 
  * StopMojo.
  *
  * The exception is that, Derone Bryson and the the StopMojo Project hereby 
  * grants permission for non-GPL compatible modules (jar files, libraries, 
  * codecs, etc.) to be used and distributed together with StopMojo. This 
  * permission is above and beyond the permissions granted by the GPL license 
  * StopMojo is covered by.
  *
  * This exception does not however invalidate any other reasons why the 
  * executable file might be covered by the GNU General Public License.
  *
  * This exception applies only to the code released by Derone Bryson and/or the
  * StopMojo Project under the name StopMojo. If you copy code from other Free 
  * Software Foundation releases into a copy of StopMojo, as the General Public 
  * License permits, the exception does not apply to the code that you add in 
  * this way. To avoid misleading anyone as to the status of such modified files, 
  * you must delete this exception notice from them.
  *
  * If you write modifications of your own for StopMojo, it is your choice 
  * whether to permit this exception to apply to your modifications. If you do 
  * not wish that, delete this exception notice.  
  */
 package com.mondobeyondo.stopmojo.capture;
 import java.util.Date;
 import java.util.Random;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment; 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.prefs.Preferences;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.imageio.IIOException;
 import javax.media.CaptureDeviceInfo;
 import javax.media.CaptureDeviceManager;
 import javax.media.Format;
 import javax.media.Manager;
 import javax.media.MediaLocator;
 import javax.media.NoDataSourceException;
 import javax.media.bean.playerbean.MediaPlayer;
 import javax.media.control.FormatControl;
 import javax.media.format.VideoFormat;
 import javax.media.protocol.CaptureDevice;
 import javax.media.protocol.DataSource;
 import javax.media.protocol.FileTypeDescriptor;
 import javax.media.protocol.PushBufferDataSource;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JSplitPane;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.Timer;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.plaf.basic.BasicSplitPaneDivider;
 import javax.swing.plaf.basic.BasicSplitPaneUI;
 
 import com.mondobeyondo.stopmojo.plugin.Plugin;
 import com.mondobeyondo.stopmojo.plugin.PluginManager;
 import com.mondobeyondo.stopmojo.plugin.capture.CapturePlugin;
 import com.mondobeyondo.stopmojo.plugin.capture.CapturePluginException;
 import com.mondobeyondo.stopmojo.util.CDSWrapper;
 import com.mondobeyondo.stopmojo.util.FieldPanel;
 import com.mondobeyondo.stopmojo.util.FramePosSizeHandler;
 import com.mondobeyondo.stopmojo.util.ImageDataSource;
 import com.mondobeyondo.stopmojo.util.Project;
 import com.mondobeyondo.stopmojo.util.ProjectFrameSource;
 import com.mondobeyondo.stopmojo.util.SwingWorker;
 
 import javax.mail.*;
 import javax.mail.internet.*;
 
 import sun.font.FontFamily;
 
 import java.util.*;
 /**
  * @author Derry Bryson
  * @author Peter Reintjes (Museum of Life + Science)
  *
  */
 @SuppressWarnings("serial")
 
 public class CaptureFrame extends JFrame implements ChangeListener, KeyListener {
 
 //	private static final int SCREENWIDTH = 1280;
 //	private static final int SCREENHEIGHT = 1024;
     private static int ccam = 0;
 
 	public static int SCREENWIDTH = 1600;
 	public static int SCREENHEIGHT = 1200;
 	public static int m_seq = 1;
 	public static int m_random = (new Random()).nextInt(100);
 	public static Color colorA = Color.decode("0xDBECF5");
 	public static Color colorB = Color.decode("0xEAEFF0");
 	private static final String
 	  	PREF_VDIVLOC = "VDivLoc",
 		PREF_HDIVLOC = "HDivLoc",
 		PREF_CAPDEVNAME = "CapDevName",
 		PREF_CAPFORMAT = "CapFormat",
 		PREF_CAPPLUGINID = "CapPluginID",
 		PREF_GRIDON = "GridOn",
 		PREF_GRIDNUMH = "GridNumH",
 		PREF_GRIDNUMV = "GridNumV";
 	private static final int
 		FASTEST_FPS = 24,
 		SLOWEST_FPS  = 3;
 	private static final String m_projectFileName = "C:/TEMP/defaultProject/dp/dp.smp";
 	private static final String FFMPEG   = "ffmpeg.exe";
 	private static final String fdir = "C:/TEMP/defaultProject/dp/Frames/";	
 	private static final String INFILES = " -i \""+fdir+"Frame%06d.jpg\" ";
 	
 	// There must be a space in the (empty) title, else option ignored and filename appears!
 	private static final String OPTIONS = "-metadata title=\" \" -y -b 1500k -vcodec wmv2 -acodec wmav2 ";
 		
 	private static int FRAME_TIMEOUT = 40;
 	private static int fps           = 12;
 	private static int PLAY_FRAME_TIMEOUT = 80;
 	private static final int[] cameraIds = new int[] { 3, 8 };
 	private static int currentCameraIndex = 0;
 	
 	private JFrame movieDialog = null;
 	private JLabel movieMessage = null;
 	private Image testPattern = null;
 	private Image testPattern2 = null;
 
  	private JFrame startDialog = null;
  	private JLabel startMessage = null;
  	private static final String m_StartMessage =
  		"<html><body><center><pre>\n\n</pre><h1>" +
  		"<h1>&nbsp;&nbsp;&nbsp;"      +
  		"Press RESET Again to Start a New Movie" +
  		"&nbsp;&nbsp;&nbsp;</h1>"     +
  		"<h1>&nbsp;&nbsp;&nbsp;"      +
  		"This will erase all previous pictures!" +
  		"&nbsp;&nbsp;&nbsp;</h1>"     +
  		"<pre>\n\n</pre>"			  +
  		"<h2>&nbsp;&nbsp;&nbsp;"	  +
  		"(Press any other key to Cancel)" +
  		"&nbsp;&nbsp;&nbsp;</h2>"	  +
  		"<pre>\n\n</pre></center></body></html>";
 
 	private static final String m_MakeMovieMessage =
 		"<html><body><center><pre>\n\n</pre><h1>" +
 		"<h1>&nbsp;&nbsp;&nbsp;"      +
 		"Press SAVE Again to Save this Movie" +
 		"&nbsp;&nbsp;&nbsp;</h1>"     +
 		"<pre>\n\n</pre>"			  +
 		"<h2>&nbsp;&nbsp;&nbsp;"	  +
 		"(Press any other key to Cancel)" +
 		"&nbsp;&nbsp;&nbsp;</h2>"	  +
 		"<pre>\n\n</pre></center></body></html>";
 
 	private static final String m_MakingMovieMessage =
 		"<html><body><center><pre>\n\n</pre><h1>" +
 		"<h1>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"      +
 		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"          +
 		"Saving Movie . . ." +
 		"</h1>"     +
 		"<pre>\n\n</pre>"			  +
 		"<h2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"	  +
 		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"        +
 		"(Please wait)" +
 		"&nbsp;&nbsp;&nbsp;</h2>"	  +
 		"<pre>\n\n</pre></center></body></html>";
 	
 	private static final String m_SwitchingCameraMessage =
 		"<html><body><center><pre>\n\n</pre><h1>" +
 		"<h1>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"      +
 		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"          +
 		"Switching Cameras . . ." +
 		"</h1>"     +
 		"<pre>\n\n</pre>"			  +
 		"<h2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"	  +
 		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"        +
 		"(Please wait)" +
 		"&nbsp;&nbsp;&nbsp;</h2>"	  +
 		"<pre>\n\n</pre></center></body></html>";
 	
 	private static final String m_lastFrameLabel = "Last  Picture";
 	private static final String m_currentFrameLabel = "Current  View";
 	private static final int FRAME_LABELX = 260;
 	private static final int FRAME_LABELY = 360;  
 
 	private static String m__currentPluginID;
 	static int movieButton = 0;
 	static int startButton = 0;
 	
 	public static boolean
 		m_oneCamera = true; 
 	public static boolean
 		mlsVersion = false;           // true will hide all unnecessary controls
 	public static boolean
 		mlsButtonBox = false;         // true hides the Keyboard/Mouse interface
 	private int m_playFrame, m_lastFrame;
 	
   public Timer
     m_timer, m_playtimer;
   
   static public Project
 	  m_prj = null;
   
   static public CaptureFrame me;
   
   public filmStrip m_filmPanel;
   
   private JPanel
 	  m_compPanel,m_compInner,
 	  m_leftPanel, m_leftInner;
   
     private ImagePanel
   	m_compImagePanel,
   	m_leftImagePanel;
     
   private JLabel
 	  m_statusBarLabel;
 
   
   private JSlider
 	  m_prevFrameAlphaSlider,
 		m_mainAlphaSlider;
   
   private JButton
 	  m_previewBut,
 	  m_capture1But,
 	  m_capture11But,
 	  m_povBut;
 
   
 	private JSplitPane
     m_vSplitPane,
     m_hSplitPane;
 	  
 	
 	private JSpinner
 	    m_prevFrameOffsetSpinner,
 		m_curFrameSpinner,
 		m_gridHSpinner,
 		m_gridVSpinner;
 	
 	private Action
 	    m_fileNewAction,
 	    m_fileOpenAction,
 		m_fileCloseAction,
 		m_capture1Action,
 		m_projectPropAction,
 		m_previewAction,
 		m_exportAction,
 		m_exitAction,
 		m_povAction,
 		m_gridOnOffAction;
 	
 	private JCheckBox
 	  m_gridCheckBox;
 	
 	private Preferences
 	  m_pref;
 	
 	private boolean
 	  m_capturing = false;
 	
 	private PluginManager
 	  m_pluginManager;
 
 	private Vector
 	  m_capturePlugins;
 	
 	private CapturePlugin
 	  m_curCapturePlugin = null;
 
     private int    m_nextCapDev = 0;
     private String m_currentPluginID;
     private String m_currentDevName;
 	
     // If there is no external button hardware (LabJack), menus will be displayed
 
   public CaptureFrame(String prjFileName, boolean labjack)
   {
 	  // OPEN PROJECT (This is where we get our config constants
 	  try {	m_prj = new Project(m_projectFileName);}
 	  catch (Exception e1)
 	  {
 		  System.out.println("CaptureFrame: Could not open Project file " + prjFileName);
 		  System.exit(0);
 	  }
 	  if (labjack) 
 	  {
 		  mlsVersion = true;
 		  mlsButtonBox = true;
 	  }
 	  openupPlugins();
 	  /*
   	try
 		{
   	  m_pluginManager = new PluginManager();
   	  m_pluginManager.LoadPlugins("plugins");
 		}
   	catch(Exception e)
 		{
   			System.out.println("PlugIns failed to load");
   			e.printStackTrace();
   			System.exit(1);
   		}
     Vector<?> plugins = m_pluginManager.getPlugins();
 
     for(int i = 0; i < plugins.size(); i++)
     	System.out.println("Plugin " + i + ": " + ((Plugin)plugins.elementAt(i)).getDesc());
     try
 		{
       m_capturePlugins = m_pluginManager.getPlugins(Class.forName("com.mondobeyondo.stopmojo.plugin.capture.CapturePlugin"));
 		}
   	catch(Exception e)
 		{
   		e.printStackTrace();
   		System.exit(1);
 		}
 		*/
   	/*
   	 * Preferences are in the Registry at:
   	 * HKEY_CURRENT_USER
   	 *    Software
   	 *      JavaSoft
   	 *         Prefs
   	 *            com
   	 *              mondobeyondo
   	 *                  stopmojo
   	 *                      capture
   	 * 
   	 */
   	m_pref = Preferences.userNodeForPackage(this.getClass());
 
   	setTitle("North Carolina Museum of Live + Science  Animation Station");
 		setIconImage(Capture.s_stopmojoImage);
 		addWindowListener(new java.awt.event.WindowAdapter()
 		{
 			public void windowClosing(java.awt.event.WindowEvent evt)
 			{
 				onClose(evt);
 			}
 		});
 		
 		initActions();
 		
 		if (mlsButtonBox == false)
 		{
 			setJMenuBar(makeMenuBar());
 			this.getContentPane().add(makeToolBar(), BorderLayout.NORTH);
 		}
 
     m_hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
             m_leftPanel = makePreviewPanel(),
 	    m_compPanel = makeCompPanel());
     /*
   	{
   	    @Override
   	    protected void paintComponent(Graphics g) {
   	    	super.paintComponent(g);
   	    	int w = getWidth( );
   	    	int h = getHeight( );
     	  	  GradientPaint gp = new GradientPaint(
       	    	      0, 0, colorA,
       	    	      0, h, colorB );
   	    	  Graphics2D g2d = (Graphics2D)g;
   	    	  g2d.setPaint( gp );
   	    	  g2d.fillRect( 0, 0, w, h );
   	    }
   	}; 
     */
     m_hSplitPane.setDividerLocation(m_prj.getHDivLoc());
     flattenSplitPane(m_hSplitPane);
     m_hSplitPane.setOpaque(false);
     setPrevFrame(m_prj.getCurFrameNum());
     //	setPrevFrame(0);
     
     m_filmPanel = new filmStrip("C:/TEMP/defaultProject/dp/Frames/", fps, m_lastFrame);
     m_filmPanel.setVisible(true);
     m_vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
 				              m_hSplitPane, 
 				  m_filmPanel);
     /*
  	{
   	    @Override
   	    protected void paintComponent(Graphics g) {
   	    	super.paintComponent(g);
   	    	int w = getWidth( );
   	    	int h = getHeight( );
     	  	  GradientPaint gp = new GradientPaint(
       	    	      0, 0, colorA,
       	    	      0, h, colorB );
   	    	  Graphics2D g2d = (Graphics2D)g;
   	    	  g2d.setPaint( gp );
   	    	  g2d.fillRect( 0, 0, w, h ); 
   	    }
   	};
     */
     m_vSplitPane.setOpaque(false);
 				              
   	if (m_prj != null)
   	{
   		setProject(m_prj);
 //  	System.out.println("captureFrame (initial setting): Setting vertical divider to " + m_prj.getVDivLoc());
   		m_vSplitPane.setDividerLocation(m_prj.getVDivLoc());
   	}
   	else
   	{
   		System.out.println("CaptureFrame(line 380): Project is NULL!");
   	}
   	
     flattenSplitPane(m_vSplitPane);
     getContentPane().add(m_vSplitPane, BorderLayout.CENTER);
     // getContentPane().add(new Pwb("images/12fps.png"),BorderLayout.SOUTH);
     try {
     	testPattern = javax.imageio.ImageIO.read(
     			new File("images/Test000001.png"));
     }	catch(Exception e) { e.printStackTrace(); }
     try {
     	testPattern2 = javax.imageio.ImageIO.read(
     			new File("images/Test000004.png"));
     }	catch(Exception e) { e.printStackTrace(); }
 //    m_compImagePanel.setImage(1, null);
 //    m_leftImagePanel.setImage(0, null);
       
 	if (labjack) setUndecorated(true); // Must be done before displayable/packed
 	pack();
 	setSize(500, 800);
 	FramePosSizeHandler.restoreSizeAndPosition(this);
 //    doLayout();
 	setDefaultCloseOperation(EXIT_ON_CLOSE);
 	updateUI();
 	//System.out.println("JAVA.LIBRARY.PATH =["+System.getProperty("java.library.path"));
 //	System.out.println("JAVA.EXT.DIRS =["+System.getProperty("java.ext.dirs"));
 //	System.out.println("JAVA.CLASS.PATH =["+System.getProperty("java.class.path"));
 	if (m_prj != null)
 	{
 		System.out.println("Using [" + m_prj.getCapPlugin() + "] AND [" +
 				m_prj.getCapDev1() + "] from Project file");
 						
 		setCapDev(m_prj.getCapPlugin(), m_prj.getCapDev1());
 		//System.out.println("after setCapDev in CaptureFrame constructor");
 	}
 	else
 	{
 		System.out.println("Can't set capture device: no Project");
 	}
 	// openCamera();
  	this.getContentPane().addKeyListener(this);
  	this.getContentPane().requestFocus();
  	this.getContentPane().requestFocusInWindow();
      me = this;
 	}
 
   public static void flattenSplitPane(JSplitPane jSplitPane) {
      
         jSplitPane.setUI(new BasicSplitPaneUI() {
           public BasicSplitPaneDivider createDefaultDivider() {
               return new BasicSplitPaneDivider(this) {
                   public void setBorder(Border b) {
                   }
               };
           }
       });
       
       jSplitPane.setBorder(null);
   }
   
 
 
 	public void setTitle(String s)
 	{
 		if(!s.trim().equals(""))
 			s = " - " + s;
 		super.setTitle(Capture.s_appName + ", Version " + Capture.s_appVersion + s);
 
 	}
 	
 	public void setTitle(File file)
 	{
 		setTitle("Museum of Life + Science");
 	}
 	
 	private void setProject(Project prj)
 	{
 		m_prj = prj;
 		if(m_prj != null)
 		{
 		  setTitle(new File(m_prj.getFileName()));
 		  if (m_compImagePanel != null)
 		  {
 			  m_compImagePanel.setAlpha(0, m_prj.getMainAlpha());
 		  }
 		  m_prevFrameOffsetSpinner.setValue(new Integer(m_prj.getPrevFrameOffset()));
 		  setPrevFrameOffset(m_prj.getPrevFrameOffset());
 		  if (m_prevFrameAlphaSlider!= null)
 			  m_prevFrameAlphaSlider.setValue((int)(m_prj.getPrevFrameAlpha() * 100.0));
 		  if (m_mainAlphaSlider!= null)
 			  m_mainAlphaSlider.setValue((int)(m_prj.getMainAlpha() * 100.0));
 		  if (m_curFrameSpinner!= null)
 			  m_curFrameSpinner.setValue(new Integer(m_prj.getCurFrameNum()));
 		  SCREENHEIGHT = m_prj.getScreenHeight();
 		  SCREENWIDTH = m_prj.getScreenWidth();
 
 		  colorA = m_prj.getColorA();
 		  colorB = m_prj.getColorB();
 		  System.out.println("Project Preferences loaded and set");
 		}
 		updateUI();
 	}
 	
   public Project getProject()
   {
   	return m_prj;
   }
 	
 	
   private JPanel makeCompPanel()
   {
   	GridBagConstraints  gbc;
   	JPanel           	  p = new JPanel();
   	Hashtable <Integer, JLabel> labelTable;  	
   	p.setLayout(new BorderLayout());
     
   	m_compInner = new JPanel()
    	{
   	   @Override
   	    protected void paintComponent(Graphics g) {
   		   super.paintComponent(g);
   		   int w = getWidth( );
   		   int h = getHeight( );
   		   GradientPaint gp = new GradientPaint(
   				   0, 0, colorA,
   				   0, h, colorB );
   		   Graphics2D g2d = (Graphics2D)g;
   		   g2d.setPaint( gp );
   		   g2d.fillRect( 0, 0, w, h ); 
   	   }
   	};
 
   	
   	m_compInner.setBorder(new EmptyBorder(150,40,50,40));
 //  	ip.setBorder(new BevelBorder(BevelBorder.LOWERED));
   	m_compInner.setLayout(new BorderLayout());
 	   
  	JPanel m_compLabel = new JPanel()
    	{
   	   @Override
   	    protected void paintComponent(Graphics g) {
   		   super.paintComponent(g);	
   		   Graphics2D g2d = (Graphics2D)g;
 		   Font font = new Font("Trebuchet MS",Font.BOLD,32);
 		   g2d.setFont(font);
 		   g2d.setColor(Color.black);
 		   g2d.drawString(m_currentFrameLabel, 260, 30);
   	   }
    	};
 		   
     m_compImagePanel = new ImagePanel(2, "Current Frame");
     m_compImagePanel.setAlpha(0, (float)1.0);
     m_compImagePanel.setAlpha(1, (float)0.5);
     m_compImagePanel.setPreferredSize(new Dimension(640,480));
 //  	m_compImagePanel.setGridNumX(m_pref.getInt(PREF_GRIDNUMH, 10));
 //  	m_compImagePanel.setGridNumY(m_pref.getInt(PREF_GRIDNUMV, 10));
   	m_compImagePanel.showGrid(m_pref.getBoolean(PREF_GRIDON, false));
 
   	m_compInner.add(m_compImagePanel, BorderLayout.CENTER);
   	m_compLabel.setPreferredSize(new Dimension(600,200));
   	m_compLabel.setOpaque(false);
   	m_compLabel.setVisible(true);
   	m_compInner.setOpaque(false);
   	m_compInner.setVisible(true);
   	m_compImagePanel.setOpaque(false);
   	m_compImagePanel.setVisible(true);
   	
   	p.add(m_compInner, BorderLayout.NORTH);
   	p.add(m_compLabel, BorderLayout.SOUTH);
 
   	JPanel cp = new JPanel();
   	cp.setOpaque(false);
  
 	gbc = new GridBagConstraints();
 	gbc.gridx = 0;
 	gbc.gridy = 0;
 	gbc.fill = GridBagConstraints.BOTH;
 	gbc.anchor = GridBagConstraints.CENTER;
 	gbc.insets = new Insets(5, 5, 5, 5);
 	gbc.weightx = 1.0;
 	gbc.weighty = 1.0;
 	
 	m_prevFrameOffsetSpinner =
 		new JSpinner(new SpinnerNumberModel(
 				new Integer(-1),
 				new Integer(-9999),
 			    new Integer(-1),
 			    new Integer(1)));
 	m_prevFrameOffsetSpinner.addChangeListener(this);
 	int curFrame = 0;
 	if(m_prj != null)
 		curFrame = m_prj.getCurFrameNum();
 	m_curFrameSpinner = new JSpinner(new SpinnerNumberModel(new Integer(curFrame),
 			                                                new Integer(0),
 			                                                new Integer(999999),
 			                                                new Integer(1)));
 	m_curFrameSpinner.addChangeListener(this);
 	
 	if (mlsButtonBox)
 	{
 		gbc.gridx = 20;
 		gbc.gridy = 20;
 		gbc.anchor = GridBagConstraints.SOUTH;
 }
 else
 {
   	cp.setLayout(new GridBagLayout());
   	cp.setOpaque(true);
   	FieldPanel pfp = new FieldPanel();
 
 	  pfp.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Previous Frame Overlay"));
   	pfp.setOpaque(false);
   	m_prevFrameAlphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
   	m_prevFrameAlphaSlider.addChangeListener(this);
   	m_prevFrameAlphaSlider.setMajorTickSpacing(10);
 //  	m_prevFrameAlphaSlider.setMinorTickSpacing(1);
   	labelTable = new Hashtable<Integer, JLabel>();
   	labelTable.put( new Integer(0), new JLabel("0") );
   	labelTable.put( new Integer(50), new JLabel("50") );
   	labelTable.put( new Integer(100), new JLabel("100") );
   	m_prevFrameAlphaSlider.setLabelTable( labelTable );  	
   	m_prevFrameAlphaSlider.setPaintTicks(true);
   	m_prevFrameAlphaSlider.setPaintLabels(true);
 		pfp.addField("Alpha (%):", m_prevFrameAlphaSlider, 100);
   	
 
 		pfp.addField("Frame Offset:", m_prevFrameOffsetSpinner, 10);
 		
   	FieldPanel mp = new FieldPanel();
   	mp.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Video Capture"));
 
   	m_mainAlphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
   	m_mainAlphaSlider.addChangeListener(this);
   	m_mainAlphaSlider.setMajorTickSpacing(10);
 //  	m_mainAlphaSlider.setMinorTickSpacing(0);
   	m_mainAlphaSlider.setPaintTicks(true);
   	labelTable = new Hashtable<Integer, JLabel>();
   	labelTable.put( new Integer(0), new JLabel("0") );
   	labelTable.put( new Integer(50), new JLabel("50") );
   	labelTable.put( new Integer(100), new JLabel("100") );
   	m_mainAlphaSlider.setLabelTable( labelTable );  	
   	m_mainAlphaSlider.setPaintLabels(true);
     mp.addField("Alpha (%):", m_mainAlphaSlider, 100);
     
 
 		mp.addField("Current Frame:", m_curFrameSpinner, 10);
 		cp.add(pfp, gbc);
 		
 		gbc = new GridBagConstraints();
 		gbc.gridx = 1;
 		gbc.gridy = 0;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = new Insets(5, 5, 5, 5);
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
    	cp.add(mp, gbc);
 
    	JPanel butPanel = new JPanel();
    	butPanel.setLayout(new GridBagLayout());
    	
 	ImageIcon m_camI = new ImageIcon("images/cam.png");
 	ImageIcon m_campressed = new ImageIcon("images/campressed.png");
 	
  	m_capture1But = new JButton(m_camI);
    	m_capture1But.addActionListener(m_capture1Action);
     m_capture1But.setPressedIcon(m_campressed);
   //	m_capture1But.setMargin(new Insets(2, 2, 2, 2));
 		gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.CENTER;
 	//	gbc.insets = new Insets(2, 2, 2, 2);
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
 		
   	butPanel.add(m_capture1But, gbc);
   	
  	m_capture11But = new JButton(m_exitAction);
   	m_capture11But.setText("Exit");
 
   	m_capture11But.setMargin(new Insets(2, 2, 2, 2));
 		gbc = new GridBagConstraints();
 		gbc.gridx = 1;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.CENTER;
 		//gbc.insets = new Insets(2, 2, 2, 2);
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
   	// butPanel.add(m_capture11But, gbc);
 
  	m_povBut = new JButton(m_povAction);
   	m_povBut.setText("POV");
   	m_povBut.setMargin(new Insets(2, 2, 2, 2));
 		gbc = new GridBagConstraints();
 		gbc.gridx = 2;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = new Insets(2, 2, 2, 2);
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
   	butPanel.add(m_povBut, gbc);
 	m_povBut.setVisible(false);
 	
 	ImageIcon m_playI = new ImageIcon("images/play.png");
 	ImageIcon m_pressed = new ImageIcon("images/playpressed.png");
   	m_previewBut = new JButton(m_playI);
     m_previewBut.setPressedIcon(m_pressed);
     m_previewBut.addActionListener(m_previewAction);
   	// m_previewBut.setMargin(new Insets(5, 5, 5, 5));
 		gbc = new GridBagConstraints();
 		gbc.gridx = 3;
 		gbc.gridy = 0;
 		gbc.gridwidth = 2;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.CENTER;
 //		gbc.insets = new Insets(2, 2, 2, 2);
 		gbc.weightx = 2.0;
 		gbc.weighty = 1.0;
   	butPanel.add(m_previewBut, gbc);
 
 		gbc = new GridBagConstraints();
 		gbc.gridx = 2;
 		gbc.gridy = 0;
 		gbc.gridheight = 2;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = new Insets(5, 5, 5, 5);
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
       	cp.add(butPanel, gbc);
   }
 	if (!mlsButtonBox)
 		p.add(cp, BorderLayout.SOUTH);
 	
 	p.setOpaque(false);
 	doLayout();
     
   	return p;
   }
   
   private JPanel makePreviewPanel()
   {
   	JPanel 	  p = new JPanel();
 
   	p.setLayout(new BorderLayout());
   	m_leftInner = new JPanel()
   	{
   	    @Override
   	    protected void paintComponent(Graphics g) {
   	    	super.paintComponent(g);
   	    	int w = getWidth( );
   	    	int h = getHeight( );
     	  	  GradientPaint gp = new GradientPaint(
       	    	      0, 0, colorA,
       	    	      0, h, colorB );
   	    	  Graphics2D g2d = (Graphics2D)g;
   	    	  g2d.setPaint( gp );
   	    	  g2d.fillRect( 0, 0, w, h );
   	    }
   	};
   	m_leftInner.setBorder(new EmptyBorder(150, 40, 50, 40));
   	m_leftInner.setLayout(new BorderLayout());
 
   	JPanel m_leftLabel = new JPanel()
   	{
   	    @Override
   	    protected void paintComponent(Graphics g) {
   	    	super.paintComponent(g);
   	    	  Graphics2D g2d = (Graphics2D)g;
   	    	  Font font = new Font("Trebuchet MS",Font.BOLD,32);
   	    	  g2d.setFont(font);
   	    	  g2d.setColor(Color.black);
   	    	  g2d.drawString(m_lastFrameLabel, 280, 30);  
   	    }
   	};
   	m_leftLabel.setPreferredSize(new Dimension(600,200));
     m_leftImagePanel = new ImagePanel()
     {
     @Override
     protected void paintComponent(Graphics g) 
     {
     	// super.paintComponent(g); // Unnecessary, unless something gets changed!
     		Image img = this.m_images[0];
     		if(img != null)
     		{
     			g.drawImage(img, 0, 0, getSize().width, getSize().height, Color.black, null);
     		}
     }
     };
      /* BORDER STUFF ADDED */
      boolean border = true;
      Border raisedbevel=null, loweredbevel=null, compound=null, paneEdge=null;
      if (border)
      {
      paneEdge = BorderFactory.createEmptyBorder(10,10,10,10);
      raisedbevel = BorderFactory.createRaisedBevelBorder();
      loweredbevel = BorderFactory.createLoweredBevelBorder();
      compound = BorderFactory.createCompoundBorder(raisedbevel,loweredbevel);
      }
     
     m_leftImagePanel.setAlpha(0, (float)1.0);
     m_leftImagePanel.setPreferredSize(new Dimension(640,480));
     m_leftInner.add(m_leftImagePanel, BorderLayout.CENTER);
   	m_leftLabel.setOpaque(false);
   	m_leftLabel.setVisible(true);
   	m_leftImagePanel.setOpaque(false);
   	m_leftInner.setOpaque(false);
   	p.add(m_leftInner, BorderLayout.NORTH);
   	p.add(m_leftLabel, BorderLayout.SOUTH);
 
     //	m_speedLabel = new JLabel(SPEED_NORMAL);  	
   //	p.add(m_speedLabel,BorderLayout.SOUTH);
 
 	p.setOpaque(false);
 	if (border)
 	    {
    		m_leftImagePanel.setBorder(compound);
    		//p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));		
 	    }
   	doLayout();
 	return p;
   }
 
 	private void setStatusText(String text)
 	{
 		if(text.equals(""))
 		  text = " ";
 		m_statusBarLabel.setText(text);
 	}
 	
 	private JToolBar makeToolBar()
 	{		
 		JToolBar
 		  toolBar = new JToolBar();
 		
 		JButton
 		  button;
 		
 		toolBar.setFloatable(false);
 		toolBar.setRollover(true);
 		
 		button = new JButton();
 		button.setAction(m_fileNewAction);
 		button.setText("");
 		button.setVerticalTextPosition(JButton.BOTTOM);
 		button.setHorizontalTextPosition(JButton.CENTER);
 		button.setFont(button.getFont().deriveFont(Font.PLAIN, 10));
 		button.setToolTipText("New Project");
 		button.setIcon(Capture.s_bigNewIcon);
 		button.setFocusable(false);
 		toolBar.add(button);
 		
 		button = new JButton();
 		button.setAction(m_fileOpenAction);
 		button.setText("");
 		button.setVerticalTextPosition(JButton.BOTTOM);
 		button.setHorizontalTextPosition(JButton.CENTER);
 		button.setFont(button.getFont().deriveFont(Font.PLAIN, 10));
 		button.setToolTipText("Open Project");
 		button.setIcon(Capture.s_bigOpenIcon);
 		button.setFocusable(false);
 		toolBar.add(button);
 		
 		button = new JButton();
 		button.setAction(m_fileCloseAction);
 		button.setText("");
 		button.setVerticalTextPosition(JButton.BOTTOM);
 		button.setHorizontalTextPosition(JButton.CENTER);
 		button.setFont(button.getFont().deriveFont(Font.PLAIN, 10));
 		button.setToolTipText("Close Project");
 		button.setIcon(Capture.s_bigCloseIcon);
 		button.setFocusable(false);
 		toolBar.add(button);
 
 		toolBar.addSeparator();
 
 		m_gridCheckBox = new JCheckBox();
 		m_gridCheckBox.setAction(m_gridOnOffAction);
 //		m_gridCheckBox.setText("Grid:");
 		m_gridCheckBox.setHorizontalAlignment(JCheckBox.LEFT);
 		m_gridCheckBox.setFont(button.getFont().deriveFont(Font.PLAIN, 10));
 		m_gridCheckBox.setToolTipText("Grid On/Off");
 		m_gridCheckBox.setFocusable(false);
 		m_gridCheckBox.setSelected(m_pref.getBoolean(PREF_GRIDON, false));
 		toolBar.add(new JLabel("Grid:"));
 	  toolBar.add(m_gridCheckBox);
 		
 		m_gridHSpinner = new JSpinner(new SpinnerNumberModel(m_pref.getInt(PREF_GRIDNUMH, 10), 1, 100, 1));
 		m_gridHSpinner.addChangeListener(this);
 		m_gridHSpinner.setMaximumSize(new Dimension(50, 20));
 		m_gridHSpinner.setFont(button.getFont().deriveFont(Font.PLAIN, 10));
 		m_gridHSpinner.setToolTipText("Number of horizontal grid sections");
 		m_gridHSpinner.setFocusable(false);
 		m_gridHSpinner.setValue(new Integer(m_pref.getInt(PREF_GRIDNUMH, 10)));
 		toolBar.add(new JLabel(" H:"));
 	  toolBar.add(m_gridHSpinner);
 		
 		m_gridVSpinner = new JSpinner(new SpinnerNumberModel(m_pref.getInt(PREF_GRIDNUMV, 10), 1, 100, 1));
 		m_gridVSpinner.addChangeListener(this);
 		m_gridVSpinner.setMaximumSize(new Dimension(50, 20));
 		m_gridVSpinner.setFont(button.getFont().deriveFont(Font.PLAIN, 10));
 		m_gridVSpinner.setToolTipText("Number of vertical grid sections");
 		m_gridVSpinner.setFocusable(false);
 		m_gridVSpinner.setValue(new Integer(m_pref.getInt(PREF_GRIDNUMV, 10)));
 		toolBar.add(new JLabel(" V:"));
 	  toolBar.add(m_gridVSpinner);
 		
 		if (mlsVersion) {
 			toolBar.setVisible(false);
 		}
 		return toolBar;
 	}
 	
 	private void initActions()
 	{
 		m_fileNewAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				onFileNew();
 			}
 		};
 		
 		m_fileOpenAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				onFileOpen();
 			}
 		};
 		
 		m_fileCloseAction = new AbstractAction()
 		{
       public void actionPerformed(ActionEvent e) 
       {
       	onFileClose();
       }
 		};
 		
 		m_projectPropAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				onProjectProp();
 			}
 		};
 		m_capture1Action = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				onCapture(1);
 			}
 		};
 		
 		m_povAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				nextCapDev();
 			}
 		};
 		
 		m_previewAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				m_playFrame = 1;
 				setPlayTimer(PLAY_FRAME_TIMEOUT);
 				// onPreview();
 			}
 		};
 		
 		m_exportAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				onExport();
 			}
 		};
 		m_gridOnOffAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				onGridOnOff();
 			}
 		};
 		m_exitAction = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				System.out.println("exitAction");
 				  me.dispose();
 				  me = null;
 				  System.exit(1);
 			}
 		};
 	}
 	
 	public void updateUI()
 	{
 		m_prevFrameOffsetSpinner.setEnabled(m_prj != null && m_capturing);
 		m_curFrameSpinner.setEnabled(m_prj != null && m_capturing);
 		// if (mlsButtonBox) return;
 		if (m_fileCloseAction != null)
 		m_fileCloseAction.setEnabled(m_prj != null);
 		if (m_capture1Action != null)
 		m_capture1Action.setEnabled(m_prj != null && !m_prj.getImageFormat().equals("") && m_capturing);
 		if (m_projectPropAction != null)
 		m_projectPropAction.setEnabled(m_prj != null);
 		if (m_previewAction != null)
 			m_previewAction.setEnabled(m_prj != null && m_prj.getNumFrames() > 0);
 		if (m_exportAction != null)
 		m_exportAction.setEnabled(m_prj != null && m_prj.getNumFrames() > 0);
 		if (m_prevFrameAlphaSlider != null)
 		m_prevFrameAlphaSlider.setEnabled(m_prj != null && m_capturing);
 		if (m_mainAlphaSlider != null)
 		m_mainAlphaSlider.setEnabled(m_capturing);
   	}
 
 	private JMenuBar makeMenuBar()
 	{
 		if (mlsVersion) return null; 
 
 		JMenuBar menuBar = new JMenuBar();
 		  
 		menuBar.add(makeFileMenu());
 		menuBar.add(makeProjectMenu());
 		menuBar.add(makeHelpMenu());
 		if (mlsVersion) 
 		{
 			menuBar.setVisible(false);
 		}
 
 		return menuBar;
 	}
 	
 	private JMenu makeFileMenu()
 	{
 		JMenu
 		  menu,
 		  menu1;
 		  
 		JMenuItem
 		  menuItem;
 		  
 		menu = new JMenu("File");
 		menu.setMnemonic(KeyEvent.VK_F);
 		
 		menuItem = new JMenuItem(m_fileNewAction);
 		menuItem.setText("New Project");
 		menuItem.setMnemonic('n');
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 		menuItem.setIcon(Capture.s_newIcon);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(m_fileOpenAction);
 		menuItem.setText("Open Project");
 		menuItem.setMnemonic('o');
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 		menuItem.setIcon(Capture.s_openIcon);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(m_fileCloseAction);
 		menuItem.setText("Close Project");
 		menuItem.setMnemonic('c');
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		menuItem.setIcon(Capture.s_closeIcon);
 		menu.add(menuItem);
 
 	  menu.addSeparator();
 
     class setCapAction implements java.awt.event.ActionListener
     {
     	int
 			  m_index;
     	  
     	public setCapAction(int index)
     	{
     		m_index = index;
     	}
     	
 			public void actionPerformed(java.awt.event.ActionEvent evt)
 			{
 				onFileSelCapDev(m_index);
 			}
     };
     
 		menu1 = new JMenu("Select/Configure Capture Device");
 		menu1.setMnemonic(KeyEvent.VK_F);
 		for(int i = 0; i < m_capturePlugins.size(); i++)
 		{
 			CapturePlugin
 			  p = (CapturePlugin)m_capturePlugins.elementAt(i);
 			
 		  menuItem = new JMenuItem(p.getDesc() + " Devices");
 			menuItem.addActionListener(new setCapAction(i));
 			menuItem.setEnabled(p.isOk());
 			menu1.add(menuItem);
 		}
 		menu.add(menu1);
 		menu.addSeparator();
 		
 		menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
 		menuItem.addActionListener(new java.awt.event.ActionListener()
 		{
 			public void actionPerformed(java.awt.event.ActionEvent evt)
 			{
 				onFileExit(evt);
 			}
 		});
 		menu.add(menuItem);
 		
 		return menu;
 	}
 	
 	private JMenu makeProjectMenu()
 	{
 		JMenu
 		  menu;
 		  
 		JMenuItem
 		  menuItem;
 		  
 		menu = new JMenu("Project");
 		menu.setMnemonic(KeyEvent.VK_F);
 		
 		menuItem = new JMenuItem(m_projectPropAction);
 		menuItem.setText("Project Properties...");
 		menuItem.setMnemonic('p');
 //		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 //		menuItem.setIcon(EmailApp.s_checkEmailIcon);
 		menu.add(menuItem);
 		
 		menu.addSeparator();
 		
 		menuItem = new JMenuItem(m_capture1Action);
 		menuItem.setText("Capture 1 Frame");
 		menuItem.setMnemonic('c');
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
 //		menuItem.setIcon(EmailApp.s_checkEmailIcon);
 		menu.add(menuItem);
 
 		menu.addSeparator();
 		
 		menuItem = new JMenuItem(m_previewAction);
 		menuItem.setText("Preview");
 		menuItem.setMnemonic('p');
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
 //		menuItem.setIcon(EmailApp.s_checkEmailIcon);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(m_exportAction);
 		menuItem.setText("Export");
 		menuItem.setMnemonic('e');
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 //		menuItem.setIcon(EmailApp.s_checkEmailIcon);
 		menu.add(menuItem);
 
 		return menu;
 	}
 	
 	private JMenu makeHelpMenu()
   {
 	  JMenu
 			menu;
 		  
 	  JMenuItem
 			menuItem;
 		  
 	  menu = new JMenu("Help");
 	  menu.setMnemonic(KeyEvent.VK_H);    
 	  menuItem = new JMenuItem("Help", KeyEvent.VK_H);
 	  menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
 	  menuItem.setIcon(Capture.s_helpIcon);
 	  menu.add(menuItem);
 	  menu.addSeparator();
 	  menuItem = new JMenuItem("About", KeyEvent.VK_A);
 	  menuItem.setIcon(Capture.s_aboutIcon);
 	  menuItem.addActionListener(new java.awt.event.ActionListener()
 	  {
 		  public void actionPerformed(java.awt.event.ActionEvent evt)
 		  {
 			  onAbout();
 		  }
 	  });
 	  menu.add(menuItem);
 	
 	  return menu;
   }
   
 	public void doFrameClose()
 	{
 		doSaveProject();
 		if(m_timer != null)	{ m_timer.stop(); m_timer = null;}
 		if(m_playtimer != null)	{ m_playtimer.stop(); m_playtimer = null;}
 		m_pluginManager.disposeAll();
 //		FramePosSizeHandler.saveSizeAndPosition(this);
 		dispose();
 		System.exit(0);
 	}
 
 	private void doSaveProject()
 	{
 		if(m_prj == null)
 			return;
 		
 		try
 		{
 		  m_prj.write();
 		}
 		catch(Exception e)
 		{
 			Capture.handleError(this, "Unable to write project!", "Error", e, false);
 		}
 		m_prj = null;
 	}
 	
   private void onFileExit(java.awt.event.ActionEvent evt)
   {
   	doFrameClose();
   }
   
   private void onFileNew()
   {
   	pauseCapture();
   	
   	NewProjectDialog
 		  d = new NewProjectDialog(this);
   	
   	if(d.showModal())
   	{
     	if(m_prj != null)
     	{
     		doSaveProject();
     		m_prj = null;
     	}
     	
     	Project
 			  p = null;
     	
     	try
     	{
     		Capture.getPref().put(Capture.PREF_LASTPRJFOLDER, d.getFolder());
     		p = new Project(d.getFolder(), d.getName());
     		setProject(p);
     		m_projectPropAction.actionPerformed(null);
     	}
     	catch(Exception e)
 			{
   			Capture.handleError(this, "Unable to create project!", "Error", e, false);
 			}
   	}
   	unpauseCapture();
   }
   
   public void doFileOpen(String filename)
   {
     try
  	  {
     	Project
 		  p = new Project(filename);
     	
     	if(p != null)
     		setProject(p);
 		}
      catch(Exception e)
 		{
        e.printStackTrace();
 		}
   }
   
   public void onFileOpen()
   {
   	pauseCapture();
   	
   	doSaveProject();
   	
  		JFileChooser
 		  fc = new JFileChooser(Capture.getPref().get(Capture.PREF_LASTPRJFOLDER, ""));
   		
  		fc.addChoosableFileFilter(Project.getFileFilter());
  		if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
  		{
  			Capture.getPref().put(Capture.PREF_LASTPRJFOLDER, fc.getSelectedFile().getParentFile().getAbsolutePath());
       try
   	  {
     	  doFileOpen(fc.getSelectedFile().getAbsolutePath());
 			}
       catch(Exception e)
 			{
         e.printStackTrace();
 			}
   	}
  		
  		unpauseCapture();
   }
   
   public void onFileClose()
   {
     if(m_prj == null)
     	return;
    
     doSaveProject();
     setProject(null);
     updateUI();
   }
   
   public void onProjectProp()
   {
   	if(m_prj == null)
   		return;
   	System.out.println("PROJECT FILE: "+ m_prj.getFileName());
   	ProjectPropDialog
 		  d = new ProjectPropDialog(this, m_prj);
   	
   	d.setVisible(true);
   	updateUI();
   }
   
   private int getCurFrame()
   {
   	return ((Integer)m_curFrameSpinner.getValue()).intValue();
   }
   
   private void setCurFrame(int frame)
   {
   	m_curFrameSpinner.setValue(new Integer(frame));
   	if(m_prj != null)
   	  m_prj.setCurFrameNum(frame);
   }
   
   public void onCapture(int frames)
   {
   	if(m_prj == null)
   		return;
   	
     if (m_timer != null) m_timer.stop();
     Image image = doCapture();
     if(image != null)
     {
       try
			{
      	for(int i = 0; i < frames; i++)
      		m_prj.putFrame(getCurFrame() + i, (BufferedImage) image);
      	  	setCurFrame(getCurFrame() + frames);
     	  	setPrevFrame(getCurFrame() + ((Integer)m_prevFrameOffsetSpinner.getValue()).intValue());
			}
       catch(Exception e)
			{
       	e.printStackTrace();
			}
     }
    m_filmPanel.position(getCurFrame());
     m_timer.start();
     updateUI();
   }
   
   void mlsProject()
   {
 	try
 	{
 	Capture.getPref().put(Capture.PREF_LASTPRJFOLDER, "C:/TEMP/defaultProject/dp");
     Project p = new Project(m_projectFileName);
 	setProject(p);
 	m_projectPropAction.actionPerformed(null);
 	}
 catch(Exception e)
 	{
 		Capture.handleError(this, "Unable to create MLS project!", "Error", e, false);
 	}
 setStatusText("Project dp was created");
   }
 
   private void setCapDev(String pluginID, String devName)
   {
 	  System.out.println("setCapDev "+ pluginID +"  "+devName);
   	if(m_capturePlugins != null)
   	{
   		m_currentPluginID = pluginID;
     	Cursor oldCursor = getCursor();
     	setCursor(new Cursor(Cursor.WAIT_CURSOR));
     	
   		for(int i = 0; i < m_capturePlugins.size(); i++)
   		{
   			CapturePlugin
 				  cp = (CapturePlugin)m_capturePlugins.elementAt(i);	
   			if(cp.getID().equals(pluginID))
   			{
   				try
 					{
     				  if(cp.selectCaptureDevice(this, devName, false))
     					  setCapturePlugin(cp);
     				  else
     					  System.out.println("We didn't select a Capture Device");
 					}
   				catch(Exception e)	{ e.printStackTrace();}
   				m_currentDevName = devName;
   			}
   		}
       
       setCursor(oldCursor);
   	}
   }
   private void closeCapDev()
   {
   	if(m_capturePlugins != null)
   	{
     	Cursor oldCursor = getCursor();
     	setCursor(new Cursor(Cursor.WAIT_CURSOR));
     	
   		for(int i = 0; i < m_capturePlugins.size(); i++)
   		{
   			CapturePlugin
 				  cp = (CapturePlugin)m_capturePlugins.elementAt(i);
   			
   			if(cp.getID().equals(m_currentPluginID))
   			{
   				try {
 					cp.stopCapture();
 				} catch (CapturePluginException e) {
 					System.out.println("Fault stopping capture device");
 					e.printStackTrace();
 				}
   				cp.dispose();
   				cp = null;
   			}
   		}
       setCursor(oldCursor);
   	}
   }
     //  Switch to the next camera (Uses all installed Capture Devices)
 
     public void nextCapDev()
     {
     	System.out.println("nextCapDev");
     	m_nextCapDev = (m_nextCapDev + 1)% m_capturePlugins.size();
     	CapturePlugin
     		cp = (CapturePlugin)m_capturePlugins.elementAt(m_nextCapDev);
     	String devName = cp.getCaptureDeviceName();
     	System.out.println("Using: [" + m_nextCapDev
     			+ "] CapDev("+cp.getID()+",  ["+devName+"] )");
 
     	try                {
 			  if(cp.selectCaptureDevice(this, devName, false))
 				  	setCapturePlugin(cp);
     		}
     	catch(Exception e) { e.printStackTrace();  }
     }
   
   private void setCapturePlugin(CapturePlugin p)
   {
   	Cursor oldCursor = getCursor();
   	setCursor(new Cursor(Cursor.WAIT_CURSOR));
   	
   //   m_compImagePanel.setImage(1, null);
     m_capturing = false;
   	if(m_timer != null)
   		m_timer.stop();
   	if(m_curCapturePlugin != null)
   	{
 		System.out.println("Getting rid of old CapturePlugin");
   		try
 			{
     		m_curCapturePlugin.stopCapture();
     		m_curCapturePlugin.dispose();
     		m_curCapturePlugin = null;
 			}
   		catch(Exception e){ e.printStackTrace(); }
   	}
 			
   	
   	m_curCapturePlugin = null;
   	
   	if(p != null)
   	{
       try                 { p.startCapture(); }
       catch(Exception e)  {	e.printStackTrace(); }
   		m_curCapturePlugin = p;
   		setTimer(FRAME_TIMEOUT);
   		m_capturing = true;
   		updateUI();
   		//System.out.println("selected device: " + p.getCaptureDeviceName());
   		m_pref.put(PREF_CAPDEVNAME, p.getCaptureDeviceName());
   		m_pref.put(PREF_CAPPLUGINID, p.getID());
   	}
   	else
   	{
   		m_compImagePanel.setImage(1, null);
   		System.out.println("m_curCapturePlugin not initialized");
   		System.exit(0);
   	}
     setCursor(oldCursor);
   }
   
   private void onFileSelCapDev(int index)
   {
 	  System.out.println("onFileSelCapDev");
   	CapturePlugin
 		  cp = (CapturePlugin)m_capturePlugins.elementAt(index);
   	
   	if(m_timer != null)
   	{
   		m_timer.stop();
   		m_timer = null;
   	}
   	// setCapturePlugin(null);
   	
   	try
 		{
   	  if(cp.selectCaptureDevice(this, m_pref.get(PREF_CAPDEVNAME, ""), false))
   	  {
   	  	setCapturePlugin(cp);
   	  }
   	  else
   	  	setCapturePlugin(null);
 		}
   	catch(Exception e)
 		{
   		e.printStackTrace();
     	JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 		}
   /*	
   	CaptureDeviceDialog
 		  d = new CaptureDeviceDialog(this, m_pref.get(PREF_CAPDEVNAME, ""), 
 		  		                        m_pref.getInt(PREF_CAPFORMAT, -1));
   	
   	if(d.showModal())
   	{
   		m_pref.put(PREF_CAPDEVNAME, d.getDevName());
   		m_pref.putInt(PREF_CAPFORMAT, d.getFormatIndex());
   	  if(!d.getDevName().trim().equals(""))
   	  {
   			if(m_capDS != null)
   	  		m_capDS.disconnect();
   			
   			if(m_capMediaPlayer != null)
   			{
   				m_capMediaPlayer.close();
   			}
 
 //WRONG order of arguments!
   	  	setCapDev(d.getDevName(), d.getFormatIndex());
 
   	  }
   	}
 */
   }
   
 	private void onClose(java.awt.event.WindowEvent evt)
 	{
 		doFrameClose();
 	}
 	
   private void onAbout()
   {
   	Capture.onAbout(this);
   }
     
   public MediaPlayer createMediaPlayer(DataSource dataSource, JFrame parent) 
   {
     MediaPlayer     
 		  mediaPlayer = null;
 
     if(dataSource == null) 
     {
     	JOptionPane.showMessageDialog(parent, "Datasource is null: " + dataSource, "Error", JOptionPane.ERROR_MESSAGE);
       return(null);
     }
 
     mediaPlayer = new MediaPlayer();
     mediaPlayer.setDataSource(dataSource);
     if(mediaPlayer.getPlayer() == null) 
     {
     	JOptionPane.showMessageDialog(parent, "Unable to create MediaPlayer for: " + dataSource, "Error", JOptionPane.ERROR_MESSAGE);
       return(null);
     }
 
     return(mediaPlayer);
   }
 /*
   private void onPreview2()
   {
   	File
 		  tf = null;
   	
   	if(m_prj != null && m_prj.getNumFrames() > 0)
   	{
   		try
 			{
   			DataSource
 				  ds = null;
   			
   			if(false)
   			{
 		      tf = File.createTempFile("preview", ".mov");
   		
   		    ProjectMovieMaker
 				    pmm = new ProjectMovieMaker(m_prj, FileTypeDescriptor.QUICKTIME, VideoFormat.CINEPAK, tf.getAbsolutePath(), 0);
   		
   		    Cursor oldCursor = getCursor();
   		    setCursor(new Cursor(Cursor.WAIT_CURSOR));
   		  
   		    pmm.doit();
   		    setCursor(oldCursor);
   		  
  		      ds = Manager.createDataSource(new MediaLocator("file:" + tf.getAbsolutePath()));
   			}
   			else
   			{
     		  ds = new ImageDataSource(m_prj.getFps(), new ProjectFrameSource(m_prj));
   			}
   			
   		  MediaPlayer mp = createMediaPlayer(ds, this);
   		  if(mp != null)
   		  {
   			  PlayerDialog
 				    f = new PlayerDialog(this, mp, "Preview");
   			
   			  f.show();
   		  }
 			}
   		catch(IOException e)
 			{
   			e.printStackTrace();
   			JOptionPane.showMessageDialog(this, "Unable to create temp file", "Error", JOptionPane.ERROR_MESSAGE);
 			}
   		catch(ProjectMovieMakerException e)
 			{
   			e.printStackTrace();
   			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			}
   		catch(Exception e)
 			{
   			e.printStackTrace();
   			JOptionPane.showMessageDialog(this, "Unable to create movie preview!", "Error", JOptionPane.ERROR_MESSAGE);
 			}
   	}
   }
   */
   private void onExport()
   {
   	pauseCapture();
   	if(m_prj != null && m_prj.getNumFrames() > 0)
   	{
   		try
 			{
   			ExportDialog
 				  d = new ExportDialog(this, m_prj);
   			
   			d.setVisible(true);
 			}
   		catch (Exception e)
 			{
   			e.printStackTrace();
 			}
   	}
   	unpauseCapture();
   }
   
   private void onGridOnOff()
   {
   	m_compImagePanel.showGrid(m_gridCheckBox.isSelected());
   }
   
 	public void setTimer(int delay)
 	{
 		if(m_timer != null)
 		{
 		  m_timer.stop();
 		  m_timer = null;
 		}
 
     if(delay > 0)
     {
 //System.out.println("setting timout to " + delay + " minutes");    	
       m_timer = new Timer(delay, new ActionListener() 
       	{
 		  	public void actionPerformed(ActionEvent evt) 
 			  {
 		  		// System.out.println(".");
 		  		doSnapShot();
 		  		m_timer.restart();
 			  }
       	});
       m_timer.setRepeats(false);
       m_timer.start();
     }
 	}
 	
 	public void setPlayTimer(int delay)
 	{
 		if(m_playtimer != null)
 		{
 		  m_playtimer.stop();
 		  m_playtimer = null;
 		}
 		if (m_timer != null)
 		{
 			m_timer.stop(); // TURN OFF THE LIVE VIDEO
 		}
 		m_capturing = false;
 
 		if(delay > 0)
 		{
 
 //  System.out.println("setting timeout to " + delay + " minutes");    	
       m_playtimer = new Timer(delay, new ActionListener() 
       {
 		  	public void actionPerformed(ActionEvent evt) 
 			  {
 		  		// System.out.println("-");
 		  		doFrameShot();
 		  		if (m_playFrame <= m_lastFrame)
 		  		{
 		  			m_playtimer.setDelay(PLAY_FRAME_TIMEOUT);  
 		  			m_playtimer.restart();
 		  		}
 		  		else
 		  		{
 		  			m_capturing = true;
 		  	  		setTimer(FRAME_TIMEOUT);
 		  		}
 			  }
 		  });
       	m_playtimer.setRepeats(false);
       	m_playtimer.start();
     }
 	}
 
   
   /*
    *  Poorly named, this is actually called for every frame of
    *  the live video preview, it shouldn't be called snapShot!
    *  Is this done because it could take a while?
    *  This is a callback, so it could hold things up.
    */
   private void doSnapShot() 
   {
   	if(m_curCapturePlugin != null && m_curCapturePlugin.isOk())
   	{
   		SwingWorker worker = new SwingWorker() 
 			{
   				public Object construct() 
   				{
   					try
   					{
   						return m_curCapturePlugin.grabPreviewImage();
 					}
   					catch(Exception e)
 					{
   						e.printStackTrace();
 					}
 		  		return null;
         }
   				
         public void finished() 
         {
         	BufferedImage b = (BufferedImage) get();
         	// This sets Image 0 which is the video previewer
 	        m_compImagePanel.setImage(0, b);
         }
 
       };
 
       worker.start();
   	}
   }
 
 
 public DataSource createCaptureDataSource(String devName, int formatIndex)
 {
 MediaLocator  deviceURL;
 CaptureDeviceInfo  cdi;
 DataSource  ds = null;
 Format	  format,  formats[];
 FormatControl  formatControls[];
 
 System.out.println("createCaptureDataSource " + devName + " format " + formatIndex);
 cdi = CaptureDeviceManager.getDevice(devName);
 format = cdi.getFormats()[formatIndex];
 
 if(cdi != null)
 {
   deviceURL = cdi.getLocator();
 //System.out.println("deviceURL = " + deviceURL);
   try 
 		{
     ds = javax.media.Manager.createDataSource(deviceURL);
   } 
   catch (NoDataSourceException ndse) 
 		{
   	ndse.printStackTrace();
     return null;
   } 
   catch (java.io.IOException ioe) 
 		{
   	ioe.printStackTrace();
     return null;
   }
 }
 if(!(ds instanceof CaptureDevice))
 {
 	JOptionPane.showMessageDialog(this, devName + " is not a capture device!", "Error", JOptionPane.ERROR_MESSAGE);
 	ds = null;
 }
 if(ds != null && format != null)
 {
   formatControls = ((CaptureDevice)ds).getFormatControls();
   for(int i = 0; i < formatControls.length; i++)
   {
   	formats = formatControls[i].getSupportedFormats();
   	for(int j = 0; j < formats.length; j++)
   		if(formats[j].matches(format))
       {
       	formatControls[i].setFormat(format);
       	break;
       }
   }
 }
 return new CDSWrapper((PushBufferDataSource)ds);
 }
 
 private Image doCapture()
 {
 
 	if(m_curCapturePlugin != null && m_curCapturePlugin.isOk())
 	{
 		try
 		{
 		return m_curCapturePlugin.grabImage();
 		}
 		catch(Exception e)
 		{
   	e.printStackTrace();
 		}
 	}
 	
 	return null;
 }
 
 private void doFrameShot() 
 {
 	if (m_playFrame > m_lastFrame)
 	{
 //		System.out.println("doFrameShot("+ m_playFrame + " > " + m_lastFrame+")");
 		m_playtimer.stop();
 		m_playtimer = null;
 	}
 	else
 	{
 		Image img = null;
 		if (m_prj == null)
 		{
 			System.out.println("doFrameShot("+m_playFrame+"): Project is NULL!");
 		}
 		try
 		{
 			
 		  img = m_prj.getFrame(m_playFrame);
 		  m_leftImagePanel.setImage(0, img);
 		}
 		catch(IIOException e)
 		{
 			System.out.println("Frame000" + m_playFrame + ".jpg  was not there");
 			img = null;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		if (img == null)
 		{
 			m_playFrame = 0;
 			m_playtimer.stop();
 			m_playtimer = null;
 		}
 		else
 		{
 			m_playFrame++;
 		}
 	}
 }
 
   private void pauseCapture()
   {
   	if(m_capturing)
   		m_timer.stop();
   }
   
   private void unpauseCapture()
   {
   	if(m_capturing)
   		m_timer.restart();
   }
 
   private void setPrevFrame(int frame)
   {
   	if(m_prj != null)
   	{
   		m_lastFrame = frame;
   		try
 			{
 				Image img2 = m_prj.getFrame(frame);
   				m_leftImagePanel.setImage(0, img2);
   				Image img1 = m_prj.getFrame(frame);
   				m_compImagePanel.setImage(1, img1);
 			}
   		catch(Exception e)
 			{
   			System.out.println("Could not setPrevFrame images");
   			e.printStackTrace();
 			}
   	}
   }
   
   private void setPrevFrameOffset(int offset)
   {
 	  if(m_prj != null)
 	  {
 		  m_prj.setPrevFrameOffset(offset);
 		  setPrevFrame(m_prj.getCurFrameNum() + offset);
 	  }
   }
 
 /* (non-Javadoc)
  * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
  */
   public void stateChanged(ChangeEvent e) 
   {
   	if(e.getSource() == m_prevFrameAlphaSlider)
   	{
   		if(m_prj != null)
   		{
   			m_prj.setPrevFrameAlpha(((float)m_prevFrameAlphaSlider.getValue()) / (float)100.0);
   		}
   		m_compImagePanel.setAlpha(1, ((float)m_prevFrameAlphaSlider.getValue()) / (float)100.0);
   	}
   	else if(e.getSource() == m_mainAlphaSlider)
   	{
   		if(m_prj != null)
   			m_prj.setMainAlpha(((float)m_mainAlphaSlider.getValue()) / (float)100.0);
   		m_compImagePanel.setAlpha(0, ((float)m_mainAlphaSlider.getValue()) / (float)100.0);
   	}
   	else if(e.getSource() == m_prevFrameOffsetSpinner)
   	{
   		setPrevFrameOffset(((Integer)m_prevFrameOffsetSpinner.getValue()).intValue());
   	}
   	else if(e.getSource() == m_gridHSpinner)
   	{
   		m_compImagePanel.setGridNumX(((Integer)m_gridHSpinner.getValue()).intValue());
   	}
   	else if(e.getSource() == m_gridVSpinner)
   	{
   		m_compImagePanel.setGridNumY(((Integer)m_gridVSpinner.getValue()).intValue());
   	}
   }
   /* 320ms  160ms   80ms   40ms   20ms  */
   public void slower()
   {	  
 	  if (movieButton == 1 || startButton == 1)
 	  	{
 	  	  resetMessage();
 	  	  return;
 	  	}
 	  	if (fps > SLOWEST_FPS)
 	  	{
 	  		fps = fps/2;
 	  		newFilmStripSize(fps);
 	  		if (PLAY_FRAME_TIMEOUT < 319)	PLAY_FRAME_TIMEOUT *= 2; 
 //		  	setPlayTimer(PLAY_FRAME_TIMEOUT);  
 	  	}
   }
   
   public void faster()
   {
 	  	if (movieButton == 1 || startButton ==1)
 	  	{
 	  	  resetMessage();
 	  	  return;
 	  	}
 	  	if (fps < FASTEST_FPS)
 	  	{
 	  		fps = fps*2;
 	  		newFilmStripSize(fps); 
 		  	if (PLAY_FRAME_TIMEOUT > 21) PLAY_FRAME_TIMEOUT /= 2; 
 //		  	setPlayTimer(PLAY_FRAME_TIMEOUT);  
 	  	}
   }
   
   public void movie()
   {
 	  if (movieButton == 0)
 	  {
 		  System.out.println("First time Movie button was pressed");
 		  movieButton = 1;
 		  movieMessage = new JLabel(m_MakeMovieMessage);
 		  movieMessage.setVisible(false);
 		  movieMessage.setBackground(colorA);
 		  movieDialog = new JFrame();
 		  movieDialog.setVisible(false);
 		  movieDialog.setContentPane(movieMessage);
 		  movieDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 		  movieDialog.setLocation(SCREENWIDTH/2-200,SCREENHEIGHT/2-200);
 		  movieDialog.setBackground(colorA);
 		  movieDialog.pack();
 		  movieMessage.setVisible(true);
 		  movieDialog.setVisible(true);
 //		  repaint();
 	  }
 	  else
 	  {
 		  System.out.println("Second time Movie button was pressed");
 		  movieMessage = null;
 		  movieMessage = new JLabel(m_MakingMovieMessage);
 		  movieMessage.setBackground(colorA);
 		  movieDialog.setContentPane(movieMessage);
 		  movieDialog.setVisible(true);
 		  movieDialog.repaint();
 		  Date date = new Date();
 		  DateFormat dateFormat = new SimpleDateFormat("MMddyyHH_mm_ss");
 		  String movieFile = "C:/Apache2/htdocs/Movies/mov" +
 		    					dateFormat.format(date)+".wmv";
 		  System.out.println("Create Movie in File: ["+movieFile+"]");
 		  createMovie(movieFile);
 		  mailMovie(movieFile);
 		  resetMessage();
 	  }
   }
   public void resetMessage()
   {
 	  movieButton = 0;
 	  startButton = 0;
 	  if (movieDialog != null)
 		  {
 		  	movieDialog.dispose();
 		  	movieMessage = null;
 		  	movieDialog = null;
 		  	repaint();
 		  }
 
  	  if (startDialog != null)
  	  {
  	  	startDialog.dispose();
  	  	startMessage = null;
  	  	startDialog = null;
  	  	repaint();
  	  }
 
   }
   
 public void nextMovie()
   {
 	  if (startButton == 0)
 	  {
 		  System.out.println("First time Start button was pressed");
 		  startButton = 1;
 		  startMessage = new JLabel(m_StartMessage);
 		  startMessage.setVisible(false);
 		  startMessage.setBackground(colorA);
 		  startDialog = new JFrame();
 		  startDialog.setVisible(false);
 		  startDialog.setContentPane(startMessage);
 		  startDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 		  startDialog.setLocation(SCREENWIDTH/2-200,SCREENHEIGHT/2-200);
 		  startDialog.setBackground(colorA);
 		  startDialog.pack();
 		  startMessage.setVisible(true);
 		  startDialog.setVisible(true);
 //		  repaint();
 	  }
 	  else
 	  {
 		if(m_timer != null)	{ m_timer.stop(); m_timer = null;}
 		if(m_playtimer != null)	{ m_playtimer.stop(); m_playtimer = null;}
 
 		setCurFrame(0);
 		setPrevFrame(0);
 		resetMessage();
 		fps = 12;
   		newFilmStripSize(fps);  
     	Process p = null;
     	try {
 			p = Runtime.getRuntime().exec(
 "rm.exe c:/TEMP/defaultProject/dp/Frames/Frame*");
 		  } catch (IOException e) {
 			System.out.println("Failed to delete Frames");
 		  }
       //System.out.println("Ready to call 'waitFor' on RM process");
 	      try {	p.waitFor(); } catch (InterruptedException e) {}
       try {
 		p = Runtime.getRuntime().exec(
 "cp.exe c:/TEMP/defaultProject/dp/dp.new c:/TEMP/defaultProject/dp/dp.smp");
 	} catch (IOException e) {
 		System.out.println("Failed to copy initialized project file");
 	}
 
 //      System.out.println("Ready to call 'waitFor' on CP process");
       try {	p.waitFor(); } catch (InterruptedException e) {
 		System.out.println("Error waiting for file copy exec to complete");
       }
       System.out.println("Empty Project File should be in place "+ p.exitValue());
 	  setPlayTimer(PLAY_FRAME_TIMEOUT);  
 	  try
 	  	{
 		  setProject(new Project(m_projectFileName));
 	  	}
 	  	catch (Exception e)
 	  	{
 	  		System.out.println("Failed to reload Empty Project");
 	  	}
 	  }
 	  	
   }
   
 
   public void takeAPicture()
   {
  	  if (movieButton == 1 || startButton==1) { resetMessage(); return; }
 	  onCapture(1);
   }
   public void deletePicture()
   {
  	  if (movieButton == 1 || startButton==1) { resetMessage(); return; }
 	  int newFrame = getCurFrame();
 	  if (newFrame > 0 && m_lastFrame > 0)
 	  {
 		  setCurFrame(getCurFrame()-1);
 		  m_lastFrame--;
 		  System.out.println("DELETE: new last Frame is "+newFrame+" m_last = "+m_lastFrame);
 	  }
 	  m_filmPanel.position(getCurFrame());
 	  try
 	  {
 		  	m_leftImagePanel.setImage(0, m_prj.getFrame(m_lastFrame));
 	  }
 	  catch (Exception e) { e.printStackTrace(); }
 	  unlink(newFrame);
   }
   
   public void playPreview()
   {
  	  if (movieButton == 1 || startButton==1) { resetMessage(); return; }
 
 	  m_capturing = false;
 	  m_playFrame = 1;
 	  setPlayTimer(PLAY_FRAME_TIMEOUT);  
   }
 
   public void summarizeCaptureDevices()
   {
 	  int i,j;
 	  System.out.println("Summarize " + m_capturePlugins.size() + " Plugins");  
 	  for (j = 0; j < m_capturePlugins.size(); j++ )
 	  {
 		  CapturePlugin cp =(CapturePlugin)m_capturePlugins.elementAt(j);
 		  String devName = cp.getCaptureDeviceName();
 		  System.out.println("summarizeCaptureDevices " + devName);
 		  
 
 		  Vector<?> devs = CaptureDeviceManager.getDeviceList(null);
 		  for(i = 0; i < devs.size(); i++)
 		  {
 			  System.out.println("Device " + i + " of " + devs.size());
 			  CaptureDeviceInfo
 			  c = (CaptureDeviceInfo)devs.elementAt(i);
 			  System.out.println("Name [" + c.getName() + "]");
 			  Format[] frmts = c.getFormats();
 			  for (j=0; j < frmts.length; j++)
 			  {
 				  System.out.println("F"+ j + "[" + frmts[j].toString() + "]");
 			  }
 		  }
 	  }
   }
   private void reportSize(String lab, JPanel j)
   {
 	  int x = j.getX();
 	  int y = j.getY();
 	  int w = j.getWidth();
 	  int h = j.getHeight();	  
 	  System.out.println(lab+": "+j.getClass()+"  pos("+x+","+y+") dim("+w+","+h+")");
   }
   public void reportSizes()
   {
   
 	System.out.println("LEFT PANEL");
     reportSize("MainPreview",m_leftPanel);
     reportSize("PreviewInner",m_leftInner);
     reportSize("PreviewImage",m_leftImagePanel);
     
   	System.out.println("RIGHT PANEL");
     reportSize("MainPreview",m_compPanel);
     reportSize("PreviewInner",m_compInner);
     reportSize("PreviewImage",m_compImagePanel);
    
   }
   
   private void newFilmStripSize(int fps)
   {
 //	  filmStrip tmp = m_filmPanel;
 	  m_filmPanel = new filmStrip("C:/TEMP/defaultProject/dp/Frames/", fps, m_lastFrame);
 	  m_vSplitPane.setBottomComponent(m_filmPanel);
 //	  m_vSplitPane.getTopComponent().setVisible(true);
 //	  System.out.println("newFilmStripSize: Setting vertical divider to " + m_prj.getVDivLoc());
 	  m_vSplitPane.setDividerLocation(m_prj.getVDivLoc());
 //	  m_vSplitPane.setVisible(true);
 //	  m_filmPanel.setVisible(true);
 //	  tmp = null;
 //	  m_vSplitPane.validate();
 //    m_hSplitPane.setLeftComponent(makePreviewPanel());
       m_hSplitPane.setLeftComponent(m_leftPanel);
       m_hSplitPane.getLeftComponent().repaint();
   }
 
 
   public void createMovie(String movieFile)
   {
   	Process p = null;
 	  String cmd = FFMPEG + " -r " + fps + INFILES + OPTIONS + movieFile;
 	  System.out.println("Executing: " + cmd);
 		  try {
 			p = Runtime.getRuntime().exec(cmd);
 		} catch (IOException e) {
 			System.out.println("Failed to execute ["+cmd+"]");
 		}
 		System.out.println("Running FFMPEG...");
 		// Apparently we will wait forever for ffmpeg to 
 //	    try {	p.waitFor(); } 
 //	    catch (InterruptedException e)
 //	    {
 //	    	System.out.println("Failed waiting for FFMPEG");
 //	    	e.printStackTrace();
 //	    }
 		try { Thread.sleep(4000); } catch(Exception e){}
 		
 		System.out.println("...(I hope) FFMPEG completed. "+ movieFile);
   }
   
   public void openCamera()
   {
 	  System.out.println("Opening Camera" +
 			              currentCameraIndex +
 			              " instance ID:" +
 			              cameraIds[currentCameraIndex]);
 	  
 	  if (m_prj != null)
 	  {
 		  System.out.println("Using [" + m_prj.getCapPlugin() +
 				  "] AND [" + m_prj.getCapDev1() + "]");
 		  	if(m_capturePlugins != null)
 		  	{
 		    	Cursor oldCursor = getCursor();
 		    	setCursor(new Cursor(Cursor.WAIT_CURSOR));
 		    	
 		  		for(int i = 0; i < m_capturePlugins.size(); i++)
 		  		{
 		  			CapturePlugin
 						  cp = (CapturePlugin)m_capturePlugins.elementAt(i);
 		  			
 		  			if(cp.getID().equals(m_currentPluginID))
 		  			{
   					  System.out.println("(RE)openCamera()");
 		  				try
 						{
 	    				  if(cp.selectCaptureDevice(this, m_currentDevName, false))
 	    					  setCapturePlugin(cp);
 	    				  else
 	    					  System.out.println("openCamera(): We didn't select a Capture Device");
 						}
 
 	  				catch(Exception e)
 						{
 						System.out.println("Failed trying to start Capture device");
 	  					e.printStackTrace();
 						}
 		  				
 		  			}
 		  		}
 		      setCursor(oldCursor);
 		  	}	  
 		  setTimer(FRAME_TIMEOUT);
 		  m_capturing = true;
 		  updateUI();
 	  }	
 	  else
 	  {
 		  System.out.println("Can't set capture device: no Project!");
 	  }
   }
   public void closeCamera()
   {
 	  System.out.println("Closing Camera" +
 			              currentCameraIndex +
 			              " instance ID:" +
 			              cameraIds[currentCameraIndex]);
 		
 	  if(m_curCapturePlugin != null && m_curCapturePlugin.isOk())
 	  {
 	    	Cursor oldCursor = getCursor();
 	    	setCursor(new Cursor(Cursor.WAIT_CURSOR));
 	    	if(m_timer != null)
 	    	{
 	    		m_timer.stop();
 	    		m_timer = null;
 	    	}
 			try {
 				m_curCapturePlugin.stopCapture();
 			} catch (CapturePluginException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     		m_curCapturePlugin.dispose();
 	    	m_curCapturePlugin.closeCaptureDevice();
 	    	m_curCapturePlugin = null;
 	    	setCursor(oldCursor);
 			System.out.println("Getting rid of old CapturePlugin");
   }
 
   }
 	  
   public void switchCamera(int camIndex)
   {
 	  cameraSwitchCommands(camIndex);
 	  doFrameClose();
   }
 
   public void eraseFiles()
     {
 		if(m_timer != null)	{ m_timer.stop(); m_timer = null;}
 		if(m_playtimer != null)	{ m_playtimer.stop(); m_playtimer = null;}
 
 		setCurFrame(0);
 		setPrevFrame(0);
 		resetMessage();
 		fps = 12;
   		newFilmStripSize(fps);  
     	Process p = null;
     	try {
 			p = Runtime.getRuntime().exec(
 "rm.exe c:/TEMP/defaultProject/dp/Frames/Frame*");
 		  } catch (IOException e) {
 			System.out.println("Failed to delete Frames");
 		  }
       //System.out.println("Ready to call 'waitFor' on RM process");
 	      try {	p.waitFor(); } catch (InterruptedException e) {}
 	      /*
       try {
 		p = Runtime.getRuntime().exec(
 "cp.exe c:/TEMP/defaultProject/dp/dp.new c:/TEMP/defaultProject/dp/dp.smp");
 	} catch (IOException e) {
 		System.out.println("Failed to copy initialized project file");
 	}
 
 //      System.out.println("Ready to call 'waitFor' on CP process");
       try {	p.waitFor(); } catch (InterruptedException e) {
 		System.out.println("Error waiting for file copy exec to complete");
       }
       System.out.println("Empty Project File should be in place "+ p.exitValue());
       */ 
 	  setPlayTimer(PLAY_FRAME_TIMEOUT);  
 	  try
 	  	{
 		  setProject(new Project(m_projectFileName));
 	  	}
 	  	catch (Exception e)
 	  	{
 	  		System.out.println("Failed to reload Empty Project");
 	  	}
     }
 
   protected void paintComponent(Graphics g)
   {
 	  System.out.println("CaptureFrame: paintComponent()");
 	  super.paintComponents(g);
 	  int w = getWidth( );
 	  int h = getHeight( );
 	  GradientPaint gp = new GradientPaint(
 			  0, 0, colorA,
 			  w, h, colorB );
 	  Graphics2D g2d = (Graphics2D)g;
 	  g2d.setPaint( gp );
 	  g2d.fillRect( 0, 0, w, h ); 
   }
 
   protected void unlink(int frame)
   {
 	  Process p = null;
 	  String cmd = String.format("rm.exe -rf c:/TEMP/defaultProject/dp/Frames/Frame%06d.jpg",frame);
 
 	  try { p = Runtime.getRuntime().exec(cmd);	}
 	  catch (IOException e)
 	  {
 		  System.out.println("Failed to delete Frames");
 	  }
 	  try {	p.waitFor(); } catch (InterruptedException e) {}
   }
 
   public void postMail( String recipients[ ],
 		  String subject,
 		  String msgtext,
 		  String from,
 		  String filename) throws MessagingException
   {
 	  int SMTP_PORT = 25;
       boolean debug = false;
       
       
       Properties props = new Properties();
       props.put("mail.smtp.user", "movieratios");
       props.put("mail.smtp.host", "smtp.gmail.com");
       props.put("mail.smtp.auth", "true");
       props.put("mail.smtp.starttls.enable","true");
       props.put("mail.debug", debug);
       props.put("mail.smtp.port", SMTP_PORT);
 
       Session session = Session.getDefaultInstance(props,
       new javax.mail.Authenticator() {
       protected PasswordAuthentication getPasswordAuthentication() {
       return new PasswordAuthentication("movieratios", "sc13nc3!");
       }
       });
       session.setDebug(debug);
       
       // Define message
       Message message = new MimeMessage(session);
       message.setFrom(new InternetAddress(from));
       message.addRecipient(Message.RecipientType.TO, 
     		  new InternetAddress(recipients[0]));
       message.setSubject("Hello JavaMail Attachment");
 
    // Create the message part 
       BodyPart messageBodyPart = new MimeBodyPart();
 
       // Fill the message
       messageBodyPart.setText(msgtext);
 
       Multipart multipart = new MimeMultipart();
       multipart.addBodyPart(messageBodyPart);
 
       // Part two is attachment
       messageBodyPart = new MimeBodyPart();
       FileDataSource source = new FileDataSource(filename);
       messageBodyPart.setDataHandler(new DataHandler(source));
       messageBodyPart.setFileName(filename);
       multipart.addBodyPart(messageBodyPart);
 
       // Put parts in message
       message.setContent(multipart);
 
 
       // create a message
       Message msg = new MimeMessage(session);
 
       // set the from and to address
       InternetAddress addressFrom = new InternetAddress(from);
       msg.setFrom(addressFrom);
 
       InternetAddress[] addressTo = new InternetAddress[recipients.length]; 
       for (int i = 0; i < recipients.length; i++)
       {
           addressTo[i] = new InternetAddress(recipients[i]);
       }
       msg.setRecipients(Message.RecipientType.TO, addressTo);
      
 
       // Optional : You can also set your custom headers in the Email if you Want
       msg.addHeader("MyHeaderName", "myHeaderValue");
 
       // Setting the Subject and Content Type
       msg.setSubject(subject);
       msg.setContent(multipart);
       Transport.send(msg);
   }
 
   public void sendMovie(String file)
   {
 	  System.out.println("Who called sendMovie");
 	String[] who = { "peter.reintjes@ncmls.org" };		
 	try {
 		postMail( who,
 				"NCMLS Animation",
 				"This letter has an animated movie",
 				"peterr@ncmls.org",
 				file );
 	} catch (MessagingException e) { e.printStackTrace(); }
   }
 
   public void setFPS(int f)
   {
 	  m_filmPanel.setFPS(f);
   }
   public void closeupPlugins()
   {
 	try
 	{
 	  m_pluginManager.disposeAll();
 	  m_pluginManager = null;
 	}
 	catch(Exception e)
 	{
 			System.out.println("PlugIns failed to UNload");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
   }
   
   public void openupPlugins()
   {
 	try
 	{
 	  m_pluginManager = new PluginManager();
 	  m_pluginManager.LoadPlugins("plugins");
 	}
 	catch(Exception e)
 	{
 			System.out.println("PlugIns failed to load");
 			e.printStackTrace();
 			System.exit(1);
 		}
 Vector<?> plugins = m_pluginManager.getPlugins();
 /*
 for(int i = 0; i < plugins.size(); i++)
 	System.out.println("Plugin " + i + ": " + ((Plugin)plugins.elementAt(i)).getDesc());
 */
 try
 	{
   m_capturePlugins = m_pluginManager.getPlugins(Class.forName("com.mondobeyondo.stopmojo.plugin.capture.CapturePlugin"));
 	}
 	catch(Exception e)
 	{
 		e.printStackTrace();
 		System.exit(1);
 	}
   }
   
   public static void devcon(String operation, int devnum)
   {
 	Process p = null;
   	String cmd = "devcon " + operation + " @USB\\VID_093A*" + devnum;
  	System.out.println(cmd);
   	try {
   		p = Runtime.getRuntime().exec(cmd);
   	} catch (IOException e) {
   		System.out.println("Failed to [" + operation + "] device["+devnum+"]");
   	}
   	System.out.println("Waiting for ["+operation+"]");
     try { p.waitFor(); } 
     catch (InterruptedException e) { System.out.println("Failed waiting for "+operation); }
   }
   
   /*
    * We will restart the Animation Station and the following commands
    * will be executed after we shut down (freeing up the USB Camera
    * device we are using) and before we start up again. At which time
    * we will see only one Camera is available -- e.g. the other one.
    */
   public void cameraSwitchCommands(int camIndex)
   {
 	  int otherCamIndex = ((camIndex+1)%cameraIds.length);
 	  // Write the desired active camera index followed by
 	  // the inactive one into the file dc.txt
 	  writeToFile("C:/TEMP/defaultProject/dp/dc.txt",camIndex+"\n"+otherCamIndex+"\n");
   }
   
   public void writeToFile(String file, String data)
   {
   		FileOutputStream fout;		
   		try
   		{
   		    fout = new FileOutputStream (file);
   		    new PrintStream(fout).print(data);
   		    fout.close();		
   		}
   		catch (IOException e)
   		{
   			System.err.println("Unable to write to file:"+file);
   			System.exit(-1);
   		}
   }	
 
  public void checkCameras(String file)
   {
 	  	String result;
 	  	byte[] buff = new byte[128];
   		FileInputStream fin;
   		m_oneCamera = false;
   		
   		try
   		{
   		    fin = new FileInputStream (file);
   		    if (fin.read(buff,0,127) < 127 )
   		    {
   		    	if (buff[0] == 'X')
   		    	{
   		    		m_oneCamera = true;
   		    	}
   		    }
   		    fin.close();		
   		}
   		catch (IOException e)
   		{
   			System.err.println("Unable to write to file:"+file);
   			System.exit(-1);
   		}
   }
 
 
   public void mailMovie(String movieFile)
   {
 	  String[] who = { "0msw0ywva5uh@m.youtube.com" };
 	  String message = "No Message Body";
 	  String subject = "NCMLS Animation ("+m_random+":"+m_seq+")";
 	  String from = "elizabethf@lifeandscience.org";
 
 	  try {
 		  postMail( who, subject, message, from, movieFile);
 	  } catch (MessagingException e) { e.printStackTrace(); }
 	  m_seq++;
   }
 
 
 public void keyPressed(KeyEvent e) {}
 public void keyReleased(KeyEvent e) {}
  
 public void keyTyped(KeyEvent e) {
       if (e.getID() == KeyEvent.KEY_TYPED) {
      	 
           char keyCode = e.getKeyChar();
           System.out.println("Key typed " + keyCode);
  			switch(keyCode)
  			{
  			case 'c':
  				takeAPicture();
  				break;
  			case 'd':
  				deletePicture();
  				break;
  			case 'p':
  				playPreview();
  				break;
  			case 's':
  				slower(); 
  				break;
  			case 'f':
  				faster();
  				break;
  			case '1':
  				System.out.println("Camera 1");
  				if (ccam == 0) 
  				{
  						System.out.println("No need to switch camera1");
  				}
  				else 
  				{
  					System.out.println("Switching to camera1");
  					switchCamera(0);
  					ccam = 0;
  				}
  				break;
  			case '2':
  				System.out.println("Camera 2");
  				if (ccam == 1) 
  				{
  					System.out.println("No need to switch camera2");
  				}
  				else 
  				{
  					ccam = 0;
  					System.out.println("Switching to camera2");
  					switchCamera(1);
  				}
  				break;
  				
  			case 'm':
  //				System.out.println("MLS_B_MOVIE");
  				movie();
  				break;
  				
  			case 'n':
  //				System.out.println("MLS_B_NEW");
  				nextMovie();
  				break;
  			case ( 'x' ):
  				doFrameClose();
  				break;
  			}
       }
 }
 
 } /* End of Class CaptureFrame */
 
 
 
 
 
 
