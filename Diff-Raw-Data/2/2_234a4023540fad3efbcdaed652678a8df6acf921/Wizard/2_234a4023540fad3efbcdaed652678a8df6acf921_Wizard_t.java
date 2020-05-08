 /**
  * you can put a one sentence description of your tool here.
  *
  * ##copyright##
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General
  * Public License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  * Boston, MA  02111-1307  USA
  * 
  * @author		##author##
  * @modified	##date##
  * @version		##version##
  */
 
 package com.reefangel.tool;
 
 import processing.app.Base;
 import processing.app.Editor;
 import processing.app.Preferences;
 import processing.app.Serial;
 import processing.app.SerialException;
 import processing.app.debug.MessageConsumer;
 import processing.app.tools.Tool;
 import processing.core.PApplet;
 import processing.core.PConstants;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.LookAndFeel;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.WindowConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.mlc.swing.layout.LayoutConstraintsManager;
 
 import com.jgoodies.forms.factories.DefaultComponentFactory;
 import com.l2fprod.common.swing.JTaskPane;
 import com.l2fprod.common.swing.JTaskPaneGroup;
 import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
 import com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons;
 import com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons;
 
 
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.Paint;
 import java.awt.RenderingHints;
 import java.awt.SplashScreen;
 import java.awt.SystemColor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.Map;
 
 public class Wizard  implements Tool, MessageConsumer {
 	Editor editor;
 	Object[] options = { "Next","Cancel" };
 	boolean WaitUART;
 	boolean connected=false;
 	Serial serial;
 	LayoutConstraintsManager layoutConstraintsManager;
 int id=0;
 	private int relay=1;
 	private int window=1;
 	private int tempunit=0;
 	private int settingswidth=0;
 	private int displayPWM=0;
 
 	private int relayexpansion=0;
 	private int dimmingexpansion=0;
 	private int rfexpansion=0;
 	private int salinityexpansion=0;
 	private int ioexpansion=0;
 	private int orpexpansion=0;
 	private int phexpansion=0;
 	private int waterlevelexpansion=0;
 	
 	private int wifi=0;
 	private int ailed=0;
 
 	private String nextwindow="";
 	private String prevwindow="";
 	private boolean atolow=false;
 	private boolean atohigh=false;
 	private boolean buzzer=false;
 	private boolean upload=false;
 	public static JDialog frame;
 	JPanel insidePanel;
 	CardLayout cl;
 	TitleLabel Title;
 	String daylightwaveform="";
 	String actinicwaveform="";
 	Footer Footer;
 	JPanel Side;
 	JDialog dialog;
 	JTaskPaneGroup taskGroup;
 
 	public static JPanel disptemp;
 	public static JPanel memsettings;
 	public static JPanel expansionmods;
 	public static JPanel attachmentmods;
 	public static JPanel RFmods;
 	public static JPanel Buzzermods;
 	public static JPanel daylightpwm;
 	public static JPanel actinicpwm;
 	public static JPanel daylightpwmsettings;
 	public static JPanel actinicpwmsettings;
 	public static JPanel daylightpwmsettingspanel;
 	public static JPanel actinicpwmsettingspanel;
 	public static JPanel exppwmsettingspanel[] = new JPanel[6];
 	public static JPanel aisettingspanel[] = new JPanel[3];
 	public static JPanel rfsettingspanel[] = new JPanel[6];
 	public static JPanel wifiportal;
 	public static JPanel exppwm[] = new JPanel[6];
 	public static JPanel exppwmsettings[] = new JPanel[6];
 	public static JPanel aiport;
 	public static JPanel aipwm[] = new JPanel[3];
 	public static JPanel aipwmsettings[] = new JPanel[3];
 	public static JPanel rfpwm[] = new JPanel[6];
 	public static JPanel rfpwmsettings[] = new JPanel[6];
 
 	public static JPanel functionsettings[] = new JPanel[17];
 	public static JPanel functions[] = new JPanel[17];
 	public static JPanel ports[] = new JPanel[17];
 	public static JPanel Timed[] = new JPanel[17];
 	public static JPanel TimedMemory[] = new JPanel[17];
 	public static JPanel TimedMemorySettings[] = new JPanel[17];
 	public static JPanel Heater[]  = new JPanel[17];
 	public static JPanel Chiller[]  = new JPanel[17];
 	public static JPanel ATO[]  = new JPanel[17];
 	public static JPanel WM[]  = new JPanel[17];
 	public static JPanel CO2Control[]  = new JPanel[17];
 	public static JPanel pHControl[]  = new JPanel[17];
 	public static JPanel Dosing[]  = new JPanel[17];
 	public static JPanel Delayed[]  = new JPanel[17];
 	public static JPanel Opposite[]  = new JPanel[17];
 	public static JPanel AlwaysOn[]  = new JPanel[17];
 	public static JPanel NotUsed[]  = new JPanel[17];
 	public static JCheckBox feeding[] = new JCheckBox[17];
 	public static JCheckBox waterchange[] = new JCheckBox[17];
 	public static JCheckBox overheat[] = new JCheckBox[17];
 	public static JCheckBox lightson[] = new JCheckBox[17];
 	public static JSpinner Overheat;
 
 	Font font;
 	String bodyRule;
 	LookAndFeel lf = UIManager.getLookAndFeel();
 
 	public static String RegButtons[] = {"Time Schedule","Heater","Chiller/Fan","Auto Top Off","Wavemaker","CO2 Control","PH Control","Dosing Pump","Delayed Start","Opposite","Always On","Not Used"};
 
 	public static JRadioButton DisplayTemp = new JRadioButton();
 	public static JPanel OverheatSettings =  new JPanel();
 
 	public static String DescButtons[] = {
 		"This function turns on/off at specific times of the day. Example of devices that may use this function are light fixtures, refugium light, moonlight, powerheads and fans. Delayed Start is useful for MH ballasts.",
 		"This function turns on/off at specific temperatures. Example of device that may use this function is an electric heater.",
 		"This function turns on/off at specific temperatures. Example of devices that may use this function are fans, blowers, chillers and coolers.",
 		"This function turns on/off according to the state of one or more float switches. For more information on each ATO type, please check out the online guide at <a href='http://forum.reefangel.com/viewtopic.php?f=7&t=240'>ATO Float Switch Guide</a>.",
 		"This function turns on/off on a cycle at every specific or random number of seconds. Example of devices that may use this function are powerheads and circulation pumps.",
 		"This function turns on/off at specific pH readings. The device that uses this function must lower pH. Example of device that may use this function is a calcium reactor.",
 		"This function turns on/off at specific pH readings. The device that uses this function must increase pH. Example of device that may use this function is a Kalk based Doser.",
 		"This function turns on every specific minutes for a specific number of seconds. Use the offset when dosing different chemicals on same schedule, but with offset minutes apart. Example of device that may use this function is a dosing pump, kalk stirrers and feeders",
 		"This function turns on with a specific delay. The delayed start is triggered by controller reboot, Feeding and Water Change mode. Example of device that may use this function is a skimmer.",
 		"This function turns on/off in the opposite status of a specific Port. Example of device that may use this function are moonlights, powerheads and circulation pumps.",
 		"This function turns on at start-up. It does not turn off unless selected to turn off during Feeding or Water Change modes. Example of device that may use this function are return pumps, skimmers, powerheads and circulation pumps.",
 		"This Port is not being used.",
 		"Please choose the settings for your Daylight and Actinic schedule. Delayed Start is useful for MH ballasts."
 	};
 
 	public static String ExpModules[] = {"Relay","Dimming","RF","Salinity","I/O","ORP","pH","Water Level"};
 	public static String AttachModules[] = {"Wifi","Aqua Illuminaton Cable"};
 	public static String AIChannels[] = {"White","Blue","Royal Blue"};
 	public static String VortechModes[] = { "Constant","Lagoon","ReefCrest","Short Pulse","Long Pulse","Nutrient Transport","Tidal Swell" };
 	public static String RadionChannels[] = {"White","Royal Blue","Red","Green","Blue","Intensity"};
 	public static String Titles[] = {"Welcome","Memory Settings","Temperature Settings","Expansion Modules","Attachments",
 		"Main Relay Box", "Main Relay Box - Port 1", "Main Relay Box - Port 2", "Main Relay Box - Port 3", "Main Relay Box - Port 4", "Main Relay Box - Port 5", "Main Relay Box - Port 6", "Main Relay Box - Port 7", "Main Relay Box - Port 8",  
 		"Expansion Relay Box", "Expansion Relay Box - Port 1", "Expansion Relay Box - Port 2", "Expansion Relay Box - Port 3", "Expansion Relay Box - Port 4", "Expansion Relay Box - Port 5", "Expansion Relay Box - Port 6", "Expansion Relay Box - Port 7", "Expansion Relay Box - Port 8",
 		"Daylight Dimming Channel","Daylight Dimming Settings","Actinic Dimming Channel","Actinic Dimming Settings",
 		"Dimming Expansion Channel 0", "Dimming Channel 0 Settings", "Dimming Expansion Channel 1", "Dimming Channel 1 Settings", "Dimming Expansion Channel 2", "Dimming Channel 2 Settings", "Dimming Expansion Channel 3", "Dimming Channel 3 Settings", "Dimming Expansion Channel 4", "Dimming Channel 4 Settings", "Dimming Expansion Channel 5", "Dimming Channel 5 Settings",
 		"Aqua Illumination Port", "AI - White Channel", "AI - White Settings", "AI - Blue Channel", "AI - Blue Settings", "AI - Royal Blue Channel", "AI - Royal Blue Settings",
 		"Vortech Mode", "Radion White Channel", "Radion White Settings", "Radion Royal Blue Channel", "Radion Royal Blue Settings", "Radion Red Channel", "Radion Red Settings", "Radion Green Channel", "Radion Green Settings", "Radion Blue Channel", "Radion Blue Settings", "Radion Intensity Channel", "Radion Intensity Settings",
 		"Wifi Attachment","Buzzer"};
 	
 
 	public String getMenuTitle() {
 		return "Reef Angel Wizard";
 	}
 
 	public void init(Editor theEditor) {
 		this.editor = theEditor;
 	}
 	
 	private void ShowStep2()
 	{
 		JPanel panel = new JPanel();
 
 		AddPanel(panel);
 		JPanel panel2 = new JPanel();
 
 		panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Uploading code");
 		steps.setForeground(new Color(58,95,205));
 		steps.setFont(new Font("Arial", Font.BOLD, 24));
 		panel2.add(steps);
 		JLabel text = new JLabel("<HTML><br>Arduino is now compiling your code.<br>In a few seconds, it will start uploading it to your Reef Angel Controller.<br><br>Please wait...<br><br></HTML>");
 		panel2.add(text);
 		panel.add(panel2);
 		
 		JOptionPane pane = new JOptionPane(panel);
 		pane.setOptions(new Object[] {});
 		dialog = pane.createDialog(editor,"Uploading code"); 
 		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
 		dialog.setVisible(true);
 
 //		JOptionPane.showMessageDialog(editor,
 //				panel,
 //				"Step 2",JOptionPane.DEFAULT_OPTION);		
 	}
 
 	
 	public void setSelectedButton(JPanel j,int p)
 	{
 		((JRadioButton)j.getComponent(p)).setSelected(true);
 		ActionEvent e = new ActionEvent(j.getComponent(p),0,"Test");
 		for(ActionListener al: ((JRadioButton) j.getComponent(p)).getActionListeners()){
 			al.actionPerformed(e);
 		}
 	}
 	
 	public void run() {
 
 		if (editor.getSketch().getCode(0).isModified())
 		{
 			JOptionPane.showMessageDialog(editor,
 					"You must save the currect sketch before proceeding",
 					"Error",JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		Preferences.init(null);
 		UIManager.put("control", new Color(219,227,249));
 		UIManager.put("SimpleInternalFrame.activeTitleBackground",new Color(58,95,205));
 		InputStream is = null;
 		try {
 			is = new FileInputStream(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/layout.xml");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		layoutConstraintsManager = LayoutConstraintsManager.getLayoutConstraintsManager(is);		
 		if (!Base.isMacOS())
 		{
 			try {
 				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 					if ("Nimbus".equals(info.getName())) {
 						UIManager.setLookAndFeel(info.getClassName());
 
 						break;
 					}
 				}
 			} catch (Exception e) {
 				// If Nimbus is not available, you can set the GUI to another look and feel.
 			}
 		}
 		UIManager.getLookAndFeelDefaults().put("defaultFont", new
 				 Font("Arial", Font.PLAIN, 14));
 		font = UIManager.getFont("Label.font");
 		bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
 
 		relay=1;
 		window=1;
 		tempunit=0;
 		settingswidth=0;
 		window=1;
 		int w=0;
 		int wa=0;
 		int wra=0;
 		editor.getBase();
 		BuildSettings();
 
 		CurvesPanel panel = new CurvesPanel();
 		Title = new TitleLabel();
 		Title.SetText(Titles[0]);
 		Footer = new Footer();
 		Side = new JPanel();
 		Side.setLayout(new BorderLayout());
 		Side.add(BorderLayout.EAST, new JSeparator(JSeparator.VERTICAL));
 		JTaskPane taskPane = new JTaskPane();
 
 		taskGroup = new JTaskPaneGroup();
 		taskGroup.setTitle("Navigation");
 		taskPane.add(taskGroup);
 		taskPane.setOpaque(false);
 		taskPane.setBackground(new Color(255,255,245));
 
 		try {
 			LookAndFeelAddons.setAddon(MetalLookAndFeelAddons.class);
 		} catch (InstantiationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		SwingUtilities.updateComponentTreeUI(taskPane);
 		Side.add(taskPane);
 
 
 		panel.setLayout(layoutConstraintsManager.createLayout("panel", panel));
 		panel.add (new Header(),"headerPanel");
 		panel.add (Title,"titlePanel");
 		panel.add (Footer,"footerPanel");
 		panel.add (Side,"sidePanel");
 
 		cl = new CardLayout();
 		insidePanel = new JPanel();
 		insidePanel.setLayout(cl);
 		insidePanel.setOpaque(false);
 		insidePanel.setBorder(com.jgoodies.forms.factories.Borders.DIALOG_BORDER);
 		insidePanel.setPreferredSize(new Dimension(600,485));
 
 		prevwindow="Welcome";
 		nextwindow="Memory Settings";
 
 //		MessageBox(Titles[59]);
 		ShowWelcome();
 		ShowMemorySettings();
 		ShowTemperature();
 		ShowExpansion();
 		ShowAttachment();
 		ShowRelays();
 		ShowRelaySetup();
 		ShowPWM();
 		ShowPWMSettings();
 		ShowExpPWM();
 		ShowExpPWMSettings();
 		ShowAIPort();
 		ShowAIPWM();
 		ShowAIPWMSettings();
 		ShowVortech();
 		ShowRFPWM();
 		ShowRFPWMSettings();
 		ShowWifi();
 		ShowBuzzer();
 		ShowGenerate();
 		ShowInitialMemory();
 		ShowDone();
 		
 		panel.add (insidePanel,"insidePanel");
 		frame = new JDialog(editor,"Reef Angel Wizard",true);
 //		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.getContentPane().add(panel, BorderLayout.CENTER);
 		frame.setPreferredSize(new Dimension(900,685));
 //		frame.setMinimumSize(new Dimension(700,600));
 		frame.setResizable(false);
 		frame.pack();
 		frame.setLocationRelativeTo(editor);
 		frame.setVisible(true);
 		frame.addWindowListener(new WindowListener(){
 
 			@Override
 			public void windowOpened(WindowEvent e) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void windowActivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 				ReturnLF();	
 				saveInitialValues();
 			}
 
 		});
 	}
 
 	private void ConstructCode()
 	{
 		String d="";
 		JRadioButton jb1 = (JRadioButton) memsettings.getComponent(0);
 		int NumDosing=1;
 
 		d+="#include <ReefAngel_Features.h>\n" + 
 				"#include <Globals.h>\n" + 
 				"#include <RA_Wifi.h>\n" + 
 				"#include <Wire.h>\n" + 
 				"#include <OneWire.h>\n" + 
 				"#include <Time.h>\n" + 
 				"#include <DS1307RTC.h>\n" + 
 				"#include <InternalEEPROM.h>\n" + 
 				"#include <RA_NokiaLCD.h>\n" + 
 				"#include <RA_ATO.h>\n" + 
 				"#include <RA_Joystick.h>\n" + 
 				"#include <LED.h>\n" + 
 				"#include <RA_TempSensor.h>\n" + 
 				"#include <Relay.h>\n" + 
 				"#include <RA_PWM.h>\n" + 
 				"#include <Timer.h>\n" + 
 				"#include <Memory.h>\n" + 
 				"#include <InternalEEPROM.h>\n" + 
 				"#include <RA_Colors.h>\n" + 
 				"#include <RA_CustomColors.h>\n" + 
 				"#include <Salinity.h>\n" + 
 				"#include <RF.h>\n" + 
 				"#include <IO.h>\n" + 
 				"#include <ORP.h>\n" + 
 				"#include <AI.h>\n" + 
 				"#include <PH.h>\n" + 
 				"#include <WaterLevel.h>\n" + 
 				"#include <ReefAngel.h>\n\n"; 
 
 		if (buzzer)
 		{
 			d+="// Initialize Buzzer variables\n";
 			d+="byte buzzer=0;\n";
 			JCheckBox j = (JCheckBox)Buzzermods.getComponent(0);
 			if (j.isSelected()) d+="byte overheatflag=0;\n";
 			j = (JCheckBox)Buzzermods.getComponent(1);
 			if (j.isSelected()) d+="byte atoflag=0;\n";
 			j = (JCheckBox)Buzzermods.getComponent(2);
 			if (j.isSelected()) d+="byte highfloatflag=0;\n";
 			if (ioexpansion==1)
 			{
 				for (int y=0;y<6;y++)
 				{
 					j = (JCheckBox)Buzzermods.getComponent(3+y);
 					if (j.isSelected()) d+="byte iochannel" + y + "flag=0;\n";
 				}
 			}
 			d+="\n";
 		}		
 
 		d+="////// Place global variable code below here\n" + 
 				"\n" + 
 				"\n" + 
 				"////// Place global variable code above here\n" + 
 				"\n" + 
 				"\n" + 
 				"void setup()\n" + 
 				"{\n" + 
 				"    // This must be the first line\n" + 
 				"    ReefAngel.Init();  //Initialize controller\n";
 		if (tempunit==1) d+="    ReefAngel.SetTemperatureUnit( Celsius );  // set to Celsius Temperature\n\n";
 		if (wifi==0) d+="    ReefAngel.AddStandardMenu();  // Add Standard Menu\n\n";
 
 		String f="";
 		String w="";
 		String l="";
 		String o="";
 		String fe="";
 		String we="";
 		String le="";
 		String oe="";
 
 		for (int a=1;a<=8;a++)
 		{
 			if (feeding[a].isSelected()) f+= "Port"+a+"Bit | ";
 			if (waterchange[a].isSelected()) w+= "Port"+a+"Bit | ";
 			if (lightson[a].isSelected()) l+= "Port"+a+"Bit | ";
 			if (overheat[a].isSelected()) o+= "Port"+a+"Bit | ";
 		}
 		for (int a=9;a<=16;a++)
 		{
 			if (feeding[a].isSelected()) fe+= "Port"+(a-8)+"Bit | ";
 			if (waterchange[a].isSelected()) we+= "Port"+(a-8)+"Bit | ";
 			if (lightson[a].isSelected()) le+= "Port"+(a-8)+"Bit | ";
 			if (overheat[a].isSelected()) oe+= "Port"+(a-8)+"Bit | ";
 		}
 		if (f!="")
 			f=f.substring(0, f.length()-3);
 		else
 			f="0";
 
 		if (w!="")
 			w=w.substring(0, w.length()-3);
 		else
 			w="0";
 
 		if (l!="")
 			l=l.substring(0, l.length()-3);
 		else
 			l="0";
 
 		if (o!="")
 			o=o.substring(0, o.length()-3);
 		else
 			o="0";
 		if (fe!="")
 			fe=fe.substring(0, fe.length()-3);
 		else
 			fe="0";
 
 		if (we!="")
 			we=we.substring(0, we.length()-3);
 		else
 			we="0";
 
 		if (le!="")
 			le=le.substring(0, le.length()-3);
 		else
 			le="0";
 
 		if (oe!="")
 			oe=oe.substring(0, oe.length()-3);
 		else
 			oe="0";		
 		d+="    // Ports toggled in Feeding Mode\n" + 
 				"    ReefAngel.FeedingModePorts = " + f + ";\n";
 		if (relayexpansion==1)
 			d+="    ReefAngel.FeedingModePortsE[0] = " + fe + ";\n";
 		d+="    // Ports toggled in Water Change Mode\n" + 
 				"    ReefAngel.WaterChangePorts = " + w + ";\n";
 		if (relayexpansion==1)
 			d+="    ReefAngel.WaterChangePortsE[0] = " + we + ";\n";
 		d+="    // Ports toggled when Lights On / Off menu entry selected\n" + 
 				"    ReefAngel.LightsOnPorts = " + l + ";\n";
 		if (relayexpansion==1)
 			d+="    ReefAngel.LightsOnPortsE[0] = " + le + ";\n";
 		d+="    // Ports turned off when Overheat temperature exceeded\n" + 
 				"    ReefAngel.OverheatShutoffPorts = " + o + ";\n";
 		if (relayexpansion==1)
 			d+="    ReefAngel.OverheatShutoffPortsE[0] = " + oe + ";\n";
 		d+="    // Use T1 probe as temperature and overheat functions\n" + 
 				"    ReefAngel.TempProbe = T1_PROBE;\n" + 
 				"    ReefAngel.OverheatProbe = T1_PROBE;\n";
 		if (jb1.isSelected())
 			d+="    // Set the Overheat temperature setting\n" + 
 					"    InternalMemory.OverheatTemp_write( " + (int)((Double)Overheat.getValue() *10) + " );\n";
 
 		if (ailed==1)
 		{
 			d+="\n    // Setup ATO Port for AI communication\n";
 			JRadioButton jatc=(JRadioButton) aiport.getComponent(0);
 			if (jatc.isSelected()) d+="    ReefAngel.AI.SetPort( lowATOPin );\n"; 
 
 			jatc=(JRadioButton) aiport.getComponent(1);
 			if (jatc.isSelected()) d+="    ReefAngel.AI.SetPort( highATOPin );\n"; 
 
 		}
 
 		d+="\n" + 
 				"\n" + 
 				"    // Ports that are always on\n";
 
 		String sp;
 		int poffset;
 		for (int a=1;a<=16;a++)
 		{
 			if(a>8)
 			{
 				sp="Box1_Port";
 				poffset=8;
 			}
 			else
 			{
 				sp="Port";
 				poffset=0;
 			}
 			JRadioButton AOn = (JRadioButton) functions[a].getComponent(10);
 			if (AOn.isSelected()) d+= "    ReefAngel.Relay.On( " + sp + (a-poffset) + " );\n";
 		}
 		d+="\n";
 		d+="    ////// Place additional initialization code below here\n" + 
 				"    \n" + 
 				"\n" + 
 				"    ////// Place additional initialization code above here\n" + 
 				"}\n" + 
 				"\n" + 
 				"void loop()\n" + 
 				"{\n";
 		for (int a=1;a<=16;a++)
 		{
 			if(a>8)
 			{
 				sp="Box1_Port";
 				poffset=8;
 			}
 			else
 			{
 				sp="Port";
 				poffset=0;
 			}
 
 			for (int b=0;b<RegButtons.length;b++)
 			{
 				JRadioButton AOn = (JRadioButton) functions[a].getComponent(b);
 				if (AOn.isSelected())
 				{
 					switch (b)
 					{
 					case 0:
 						if (jb1.isSelected())
 						{
 							JSpinner j = null; 
 							j = (JSpinner) Timed[a].getComponent(6);
 							if ((Integer)j.getValue()>0)
 								d+= "    ReefAngel.MHLights( " + sp + (a-poffset);
 							else
 								d+= "    ReefAngel.StandardLights( " + sp + (a-poffset);
 							d+= ",";
 							Calendar toh= Calendar.getInstance();
 							java.util.Date dt=null; 
 							j = (JSpinner) Timed[a].getComponent(2);
 							dt = (java.util.Date)j.getValue();
 							toh.setTime(dt);
 							d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 							d+=toh.get(Calendar.MINUTE) + ",";
 							j = (JSpinner) Timed[a].getComponent(4);
 							dt = (java.util.Date)j.getValue();
 							toh.setTime(dt);
 							d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 							d+=toh.get(Calendar.MINUTE);
 							j = (JSpinner) Timed[a].getComponent(6);
 							if ((Integer)j.getValue()>0)
 								
 								d+= "," +j.getValue();
 							d+= " );\n";
 						}
 						else
 						{
 							JRadioButton jt1 = (JRadioButton) TimedMemory[a].getComponent(1);
 							if (jt1.isSelected())
 								if (((Integer) ((JSpinner)TimedMemorySettings[1].getComponent(6)).getValue()) >0)
 									d+= "    ReefAngel.DelayedStartLights( " + sp + (a-poffset) + " );\n";	
 								else
 									d+= "    ReefAngel.DayLights( " + sp + (a-poffset) + " );\n";	
 							jt1 = (JRadioButton) TimedMemory[a].getComponent(3);
 							if (jt1.isSelected())
 								d+= "    ReefAngel.ActinicLights( " + sp + (a-poffset) + " );\n";	
 							jt1 = (JRadioButton) TimedMemory[a].getComponent(5);
 							if (jt1.isSelected())
 								d+= "    ReefAngel.MoonLights( " + sp + (a-poffset) + " );\n";	
 						}
 						break;
 					case 1:
 						d+= "    ReefAngel.StandardHeater( " + sp + (a-poffset);
 						if (jb1.isSelected())
 						{
 							d+= ",";
 							JSpinner jh = null; 
 							jh = (JSpinner) Heater[a].getComponent(2);
 							d+= (int)((Double)jh.getValue() *10) + ",";
 							jh = (JSpinner) Heater[a].getComponent(4);
 							d+= (int)((Double)jh.getValue() *10);
 						}
 						d+= " );\n";
 						break;
 					case 2:
 						d+= "    ReefAngel.StandardFan( " + sp + (a-poffset);
 						if (jb1.isSelected())
 						{
 							d+= ",";
 							JSpinner jc = null; 
 							jc = (JSpinner) Chiller[a].getComponent(4);
 							d+= (int)((Double)jc.getValue() *10) + ",";
 							jc = (JSpinner) Chiller[a].getComponent(2);
 							d+= (int)((Double)jc.getValue() *10);
 						}
 						d+= " );\n";
 						break;
 					case 3:
 						JRadioButton jat = null;
 						JSpinner jats = null; 
 
 						jat=(JRadioButton) ATO[a].getComponent(1);
 						if (jat.isSelected())
 						{
 							d+= "    ReefAngel.StandardATO( " + sp + (a-poffset);
 							if (jb1.isSelected())
 							{
 								d+= ",";
 								jats = (JSpinner) ATO[a].getComponent(5);
 								d+= jats.getValue();
 							}
 							d+= " );\n";
 						}
 						jat=(JRadioButton) ATO[a].getComponent(2);
 						if (jat.isSelected())
 						{
 							if (jb1.isSelected())
 							{
 								d+= "    ReefAngel.SingleATO( true," + sp + (a-poffset);
 								d+= ",";
 								jats = (JSpinner) ATO[a].getComponent(5);
 								d+= jats.getValue() + ",0";
 							}
 							else
 							{
 								d+= "    ReefAngel.SingleATOLow( " + sp + (a-poffset);
 							}
 							d+= " );\n";
 						}
 						jat=(JRadioButton) ATO[a].getComponent(3);
 						if (jat.isSelected())
 						{
 							if (jb1.isSelected())
 							{
 								d+= "    ReefAngel.SingleATO( false," + sp + (a-poffset);
 								d+= ",";
 								jats = (JSpinner) ATO[a].getComponent(5);
 								d+= jats.getValue() + ",0";
 							}
 							else
 							{
 								d+= "    ReefAngel.SingleATOHigh( " + sp + (a-poffset);
 							}
 							d+=" );\n";
 						}
 						break;
 					case 4:
 						JRadioButton jwt = null;
 						JSpinner jwts = null; 
 
 						if (jb1.isSelected())
 						{
 							jwt=(JRadioButton) WM[a].getComponent(1);
 							if (jwt.isSelected())
 							{
 								d+= "    ReefAngel.Wavemaker( " + sp + (a-poffset) + ",";
 								jwts = (JSpinner) WM[a].getComponent(4);
 								d+= jwts.getValue() + " );";
 								d+="\n";
 							}
 							jwt=(JRadioButton) WM[a].getComponent(2);
 							if (jwt.isSelected())
 							{
 								d+= "    ReefAngel.WavemakerRandom( " + sp + (a-poffset) + ",";
 								jwts = (JSpinner) WM[a].getComponent(4);
 								d+= jwts.getValue() + ",";
 								jwts = (JSpinner) WM[a].getComponent(6);
 								d+= jwts.getValue() + " );";
 								d+="\n";
 							}
 						}
 						else
 						{
 //							if ((a-poffset)==5)
 								d+= "    ReefAngel.Wavemaker1( " + sp + (a-poffset) + " );\n";
 //							else
 //								d+= "    ReefAngel.Wavemaker2( " + sp + (a-poffset) + " );\n";
 						}
 						break;
 					case 5:
 						d+= "    ReefAngel.CO2Control( " + sp + (a-poffset);
 						if (jb1.isSelected())
 						{
 							d+= ",";
 							JSpinner jh = null; 
 							jh = (JSpinner) CO2Control[a].getComponent(4);
 							d+= (int)((Double)jh.getValue() *100) + ",";
 							jh = (JSpinner) CO2Control[a].getComponent(2);
 							d+= (int)((Double)jh.getValue() *100);
 						}
 						d+= " );\n";
 						break;
 					case 6:
 						d+= "    ReefAngel.PHControl( " + sp + (a-poffset);
 						if (jb1.isSelected())
 						{
 							d+= ",";
 							JSpinner jc = null; 
 							jc = (JSpinner) pHControl[a].getComponent(2);
 							d+= (int)((Double)jc.getValue() *100) + ",";
 							jc = (JSpinner) pHControl[a].getComponent(4);
 							d+= (int)((Double)jc.getValue() *100);
 						}
 						d+= " );\n";
 						break;
 					case 7:
 						if (jb1.isSelected())
 						{
 							JSpinner jdps = null; 
 							d+= "    ReefAngel.DosingPumpRepeat( " + sp + (a-poffset) + ",";
 							jdps = (JSpinner) Dosing[a].getComponent(6);
 							d+= jdps.getValue() + ",";
 							jdps = (JSpinner) Dosing[a].getComponent(2);
 							d+= jdps.getValue() + ",";
 							jdps = (JSpinner) Dosing[a].getComponent(4);
 							d+= jdps.getValue() + " );";
 							d+="\n";
 						}
 						else
 						{
 							if (NumDosing==1)
 							{
 								NumDosing++;
 								d+= "    ReefAngel.DosingPumpRepeat1( " + sp + (a-poffset) + " );\n";
 							}
 							else if (NumDosing==2)
 							{
 								NumDosing++;
 								d+= "    ReefAngel.DosingPumpRepeat2( " + sp + (a-poffset) + " );\n";
 							}
 							else if (NumDosing==3)
 							{
 								d+= "    ReefAngel.DosingPumpRepeat3( " + sp + (a-poffset) + " );\n";
 							}
 						}
 						break;
 					case 8:
 						d+= "    ReefAngel.Relay.DelayedOn( " + sp + (a-poffset);
 						if (jb1.isSelected())
 						{
 							JSpinner jdss = null; 
 							d+= ",";
 							jdss = (JSpinner) Delayed[a].getComponent(2);
 							d+= jdss.getValue();
 						}
 						d+= " );\n";
 						break;
 					case 9:
 						d+= "    ReefAngel.Relay.Set( " + sp + (a-poffset) + ", !ReefAngel.Relay.Status( ";
 						JComboBox jco = (JComboBox) Opposite[a].getComponent(2);
 						d+= jco.getSelectedItem().toString().replace("Main Box ", "").replace("Expansion Box ", "Box1_").replace(" ", "");
 						d+= " ) );\n";
 						break;
 					}
 				}
 			}
 		}
 
 		// daylight 
 
 		JRadioButton jpd= (JRadioButton) daylightpwm.getComponent(0);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 			if (jb1.isSelected())
 			{			
 				d+= "    ReefAngel.PWM.SetDaylight( PWMSlope(";
 				Calendar toh= Calendar.getInstance();
 				JSpinner j = null; 
 				java.util.Date dt=null; 
 				j = (JSpinner) daylightpwmsettings.getComponent(2);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(4);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(8);
 				d+= j.getValue() + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(10);
 				d+= j.getValue() + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(14);
 				d+= j.getValue() + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(8);
 				d+= j.getValue() + ") );";
 				d+="\n";
 			}
 			else
 			{
 				d+= "    ReefAngel.PWM.DaylightPWMSlope();\n";
 			}
 		}
 		jpd= (JRadioButton) daylightpwm.getComponent(1);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 			if (jb1.isSelected())
 			{			
 				d+= "    ReefAngel.PWM.SetDaylight( PWMParabola(";
 				Calendar toh= Calendar.getInstance();
 				JSpinner j = null; 
 				java.util.Date dt=null; 
 				j = (JSpinner) daylightpwmsettings.getComponent(2);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(4);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(8);
 				d+= j.getValue() + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(10);
 				d+= j.getValue() + ",";
 				j = (JSpinner) daylightpwmsettings.getComponent(8);
 				d+= j.getValue() + ") );";
 				d+="\n";
 			}
 			else
 			{
 				d+= "    ReefAngel.PWM.DaylightPWMParabola();\n";
 			}
 		}
 		jpd= (JRadioButton) daylightpwm.getComponent(2);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 			d+= "    ReefAngel.PWM.SetDaylight( MoonPhase() );\n";
 		}
 
 		jpd= (JRadioButton) daylightpwm.getComponent(3);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 		}		
 		// actinic
 
 		jpd= (JRadioButton) actinicpwm.getComponent(0);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 			if (jb1.isSelected())
 			{			
 				d+= "    ReefAngel.PWM.SetActinic( PWMSlope(";
 				Calendar toh= Calendar.getInstance();
 				JSpinner j = null; 
 				java.util.Date dt=null; 
 				j = (JSpinner) actinicpwmsettings.getComponent(2);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(4);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(8);
 				d+= j.getValue() + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(10);
 				d+= j.getValue() + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(14);
 				d+= j.getValue() + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(8);
 				d+= j.getValue() + ") );";
 				d+="\n";
 			}
 			else
 			{
 				d+= "    ReefAngel.PWM.ActinicPWMSlope();\n";
 			}
 		}
 		jpd= (JRadioButton) actinicpwm.getComponent(1);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 			if (jb1.isSelected())
 			{			
 				d+= "    ReefAngel.PWM.SetActinic( PWMParabola(";
 				Calendar toh= Calendar.getInstance();
 				JSpinner j = null; 
 				java.util.Date dt=null; 
 				j = (JSpinner) actinicpwmsettings.getComponent(2);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(4);
 				dt = (java.util.Date)j.getValue();
 				toh.setTime(dt);
 				d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 				d+=toh.get(Calendar.MINUTE) + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(8);
 				d+= j.getValue() + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(10);
 				d+= j.getValue() + ",";
 				j = (JSpinner) actinicpwmsettings.getComponent(8);
 				d+= j.getValue() + ") );";
 				d+="\n";
 			}
 			else
 			{
 				d+= "    ReefAngel.PWM.ActinicPWMParabola();\n";
 			}
 		}
 		jpd= (JRadioButton) actinicpwm.getComponent(2);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 			d+= "    ReefAngel.PWM.SetActinic( MoonPhase() );\n";
 		}
 		jpd= (JRadioButton) actinicpwm.getComponent(3);
 		if (jpd.isSelected())
 		{
 			displayPWM=1;
 		}
 
 		// dimming expansion
 		if (dimmingexpansion==1)
 		{
 			for (int i=0;i<6;i++)
 			{
 				jpd= (JRadioButton) exppwm[i].getComponent(0);
 				if (jpd.isSelected())
 				{
 					displayPWM=1;
 					if (jb1.isSelected())
 					{			
 						d+= "    ReefAngel.PWM.SetChannel( " + i + ", PWMSlope(";
 						Calendar toh= Calendar.getInstance();
 						JSpinner j = null; 
 						java.util.Date dt=null; 
 						j = (JSpinner) exppwmsettings[i].getComponent(2);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(4);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(8);
 						d+= j.getValue() + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(10);
 						d+= j.getValue() + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(14);
 						d+= j.getValue() + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(8);
 						d+= j.getValue() + ") );";
 						d+="\n";
 					}
 					else
 					{
 						d+= "    ReefAngel.PWM.Channel" + i + "PWMSlope();\n";
 					}
 				}
 				jpd= (JRadioButton) exppwm[i].getComponent(1);
 				if (jpd.isSelected())
 				{
 					displayPWM=1;
 					if (jb1.isSelected())
 					{			
 						d+= "    ReefAngel.PWM.SetChannel( " + i + ", PWMParabola(";
 						Calendar toh= Calendar.getInstance();
 						JSpinner j = null; 
 						java.util.Date dt=null; 
 						j = (JSpinner) exppwmsettings[i].getComponent(2);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(4);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(8);
 						d+= j.getValue() + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(10);
 						d+= j.getValue() + ",";
 						j = (JSpinner) exppwmsettings[i].getComponent(8);
 						d+= j.getValue() + ") );";
 						d+="\n";
 					}
 					else
 					{
 						d+= "    ReefAngel.PWM.Channel" + i + "PWMParabola();\n";
 					}
 				}
 				jpd= (JRadioButton) exppwm[i].getComponent(2);
 				if (jpd.isSelected())
 				{
 					displayPWM=1;
 					d+= "    ReefAngel.PWM.SetChannel( " + i + ", MoonPhase() );\n";
 				}
 				jpd= (JRadioButton) exppwm[i].getComponent(3);
 				if (jpd.isSelected())
 				{
 					displayPWM=1;
 				}
 			}
 		}
 
 		// AI
 		if (ailed==1)
 		{
 			for (int i=0;i<AIChannels.length;i++)
 			{
 				jpd= (JRadioButton) aipwm[i].getComponent(0);
 				if (jpd.isSelected())
 				{
 					if (jb1.isSelected())
 					{			
 						d+= "    ReefAngel.AI.SetChannel( " + AIChannels[i].replace(" ","") + ", PWMSlope(";
 						Calendar toh= Calendar.getInstance();
 						JSpinner j = null; 
 						java.util.Date dt=null; 
 						j = (JSpinner) aipwmsettings[i].getComponent(2);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(4);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(8);
 						d+= j.getValue() + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(10);
 						d+= j.getValue() + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(14);
 						d+= j.getValue() + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(8);
 						d+= j.getValue() + ") );";
 						d+="\n";
 					}
 					else
 					{
 						d+= "    ReefAngel.AI.Channel" + AIChannels[i].replace(" ","") + "Slope();\n";
 					}
 				}
 				jpd= (JRadioButton) aipwm[i].getComponent(1);
 				if (jpd.isSelected())
 				{
 					if (jb1.isSelected())
 					{			
 						d+= "    ReefAngel.AI.SetChannel( " + AIChannels[i].replace(" ","") + ", PWMParabola(";
 						Calendar toh= Calendar.getInstance();
 						JSpinner j = null; 
 						java.util.Date dt=null; 
 						j = (JSpinner) aipwmsettings[i].getComponent(2);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(4);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(8);
 						d+= j.getValue() + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(10);
 						d+= j.getValue() + ",";
 						j = (JSpinner) aipwmsettings[i].getComponent(8);
 						d+= j.getValue() + ") );";
 						d+="\n";
 					}
 					else
 					{
 						d+= "    ReefAngel.AI.Channel" + AIChannels[i].replace(" ","") + "Parabola();\n";
 					}
 				}
 				jpd= (JRadioButton) aipwm[i].getComponent(2);
 				if (jpd.isSelected())
 				{
 					d+= "    ReefAngel.AI.SetChannel( " + AIChannels[i].replace(" ","") + ", MoonPhase() );\n";
 				}
 
 			}
 		}
 
 		// RF Expansion
 
 		if (rfexpansion==1)
 		{
 			if (jb1.isSelected())
 			{			
 				JComboBox jc = (JComboBox) RFmods.getComponent(1);
 				JSpinner js = (JSpinner) RFmods.getComponent(3);
 				JSpinner jd = (JSpinner) RFmods.getComponent(5);
 				d+="    ReefAngel.RF.UseMemory = false;\n" + 
 						"    ReefAngel.RF.SetMode( " + jc.getSelectedItem().toString().replace(" ","") + "," + js.getValue() + "," + jd.getValue() + " );\n";
 			}
 			else
 			{
 				d+="    ReefAngel.RF.UseMemory = true;\n";
 			}
 			boolean radionset=false;
 			for (int i=0;i<RadionChannels.length;i++)
 			{
 				jpd= (JRadioButton) rfpwm[i].getComponent(0);
 				if (jpd.isSelected())
 				{
 					radionset=true;
 					if (jb1.isSelected())
 					{			
 						d+= "    ReefAngel.RF.SetChannel( Radion_" + RadionChannels[i].replace(" ","") + ", PWMSlope(";
 						Calendar toh= Calendar.getInstance();
 						JSpinner j = null; 
 						java.util.Date dt=null; 
 						j = (JSpinner) rfpwmsettings[i].getComponent(2);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(4);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(8);
 						d+= j.getValue() + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(10);
 						d+= j.getValue() + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(14);
 						d+= j.getValue() + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(8);
 						d+= j.getValue() + ") );";
 						d+="\n";
 					}
 					else
 					{
 						d+= "    ReefAngel.RF.Channel" + RadionChannels[i].replace(" ","") + "Slope();\n";
 					}
 				}
 				jpd= (JRadioButton) rfpwm[i].getComponent(1);
 				if (jpd.isSelected())
 				{
 					radionset=true;
 					if (jb1.isSelected())
 					{			
 						d+= "    ReefAngel.RF.SetChannel( Radion_" + RadionChannels[i].replace(" ","") + ", PWMParabola(";
 						Calendar toh= Calendar.getInstance();
 						JSpinner j = null; 
 						java.util.Date dt=null; 
 						j = (JSpinner) rfpwmsettings[i].getComponent(2);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(4);
 						dt = (java.util.Date)j.getValue();
 						toh.setTime(dt);
 						d+=toh.get(Calendar.HOUR_OF_DAY) + ",";
 						d+=toh.get(Calendar.MINUTE) + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(8);
 						d+= j.getValue() + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(10);
 						d+= j.getValue() + ",";
 						j = (JSpinner) rfpwmsettings[i].getComponent(8);
 						d+= j.getValue() + ") );";
 						d+="\n";
 					}
 					else
 					{
 						d+= "    ReefAngel.RF.Channel" + RadionChannels[i].replace(" ","") + "Parabola();\n";
 					}
 				}
 				jpd= (JRadioButton) rfpwm[i].getComponent(2);
 				if (jpd.isSelected())
 				{
 					radionset=true;
 					d+= "    ReefAngel.RF.SetChannel( Radion_" + RadionChannels[i].replace(" ","") + ", MoonPhase() );\n";
 				}
 			}
 			if (radionset) d+="    if ( second()==0 ) ReefAngel.RF.RadionWrite();\n\n";
 		}
 
 		if (buzzer)
 		{
 			String b="buzzer = ";
 			JCheckBox j = (JCheckBox)Buzzermods.getComponent(0);
 			if (j.isSelected())
 			{
 				b+="overheatflag + ";
 				d+="    overheatflag = InternalMemory.read( Overheat_Exceed_Flag );\n";
 			}
 			j = (JCheckBox)Buzzermods.getComponent(1);
 			if (j.isSelected())
 			{
 				b+="atoflag + ";
 				d+="    atoflag = InternalMemory.read( ATO_Exceed_Flag );\n";
 			}
 			j = (JCheckBox)Buzzermods.getComponent(2);
 			if (j.isSelected())
 			{
 				b+="highfloatflag + ";
 				d+="    highfloatflag = ReefAngel.HighATO.IsActive();\n";
 			}
 			if (ioexpansion==1)
 			{
 				for (int y=0;y<6;y++)
 				{
 					j = (JCheckBox)Buzzermods.getComponent(3+y);
 					if (j.isSelected())
 					{
 						b+="iochannel" + y + "flag + ";
 						d+="    iochannel" + y + "flag = ReefAngel.IO.GetChannel( " + y + " );\n";
 					}
 				}
 			}
 			if (b.length()>9)
 			{
 				b=b.substring(0, b.length()-3);
 				d+="    " + b + ";\n";
 				d+="    if ( buzzer >= 1 ) buzzer = 100;\n";
 				JRadioButton jpwmb = (JRadioButton) daylightpwm.getComponent(3);
 				if (jpwmb.isSelected()) d+="    ReefAngel.PWM.SetDaylight( buzzer );\n";
 				jpwmb = (JRadioButton) actinicpwm.getComponent(3);
 				if (jpwmb.isSelected()) d+="    ReefAngel.PWM.SetActinic( buzzer );\n";
 				for (int i=0;i<6;i++)
 				{
 					jpwmb = (JRadioButton) exppwm[i].getComponent(3);
 					if (jpwmb.isSelected()) d+="    ReefAngel.PWM.SetChannel( " + i + ", buzzer );\n";
 				}				
 			}
 			d+="\n";
 		}
 
 		d+="    ////// Place your custom code below here\n" + 
 				"    \n" + 
 				"\n" + 
 				"    ////// Place your custom code above here\n" + 
 				"\n" + 
 				"    // This should always be the last line\n";
 		if (wifi==1)
 		{
 			JTextField jt = (JTextField) wifiportal.getComponent(1);
 			String jtt=jt.getText();
 			if (jtt.length()>0)
 				d+="    ReefAngel.Portal( \"" + jtt + "\" );\n";
 			else
 				d+="    ReefAngel.AddWifi();\n";
 		}
 		d+="    ReefAngel.ShowInterface();\n" + 
 				"}\n\n";
 
 		if (relayexpansion==1 || dimmingexpansion==1 || ailed==1 || ioexpansion==1 || salinityexpansion==1 || orpexpansion==1 || phexpansion==1 || waterlevelexpansion==1) 
 		{
 			int h=83;
 			int y=2;
 			h-=relayexpansion*10;
 			h-=dimmingexpansion*28;
 			h-=ailed*10;
 			h-=ioexpansion*6;
 			if (salinityexpansion==1 || orpexpansion==1)
 				h-=10;
 			if (phexpansion==1 || waterlevelexpansion==1)
 				h-=10;
 
 			h/=(relayexpansion+dimmingexpansion+ailed+ioexpansion+(salinityexpansion|orpexpansion)+(phexpansion|waterlevelexpansion)+3);
 //			System.out.println(h);
 
 
 			d+="void DrawCustomMain()\n" + 
 					"{\n" + 
 					"    int x,y;\n" + 
 					"    char text[10];\n";
 
 			if (dimmingexpansion==1 && displayPWM==1)
 			{	
 				d+="    // Dimming Expansion\n" + 
 						"    x = 15;\n" + 
 						"    y = " + y + ";\n" + 
 						"    for ( int a=0;a<6;a++ )\n" + 
 						"    {\n" + 
 						"      if ( a>2 ) x = 75;\n" + 
 						"      if ( a==3 ) y = " + y + ";\n" + 
 						"      ReefAngel.LCD.DrawText( COLOR_DARKGOLDENROD,DefaultBGColor,x,y,\"Ch :\" );\n" + 
 						"      ReefAngel.LCD.DrawText( COLOR_DARKGOLDENROD,DefaultBGColor,x+12,y,a );\n" + 
 						"      ReefAngel.LCD.DrawText( COLOR_DARKGOLDENROD,DefaultBGColor,x+24,y,ReefAngel.PWM.GetChannelValue(a) );\n" + 
 						"      y += 10;\n" + 
 						"    }\n" + 
 						"    pingSerial();\n\n";
 				y+=28;
 			}
 			if ( ailed==1 )
 			{
 				y+=h;
 				d+="    // Aqua Illumination\n" + 
 						"    x = 10;\n" + 
 						"    y = " + y + ";\n" + 
 						"    ReefAngel.LCD.DrawText( COLOR_DODGERBLUE,DefaultBGColor,x,y,\"WH:\" );\n" + 
 						"    ReefAngel.LCD.DrawText( COLOR_DODGERBLUE,DefaultBGColor,x+38,y,\"BL:\" );\n" + 
 						"    ReefAngel.LCD.DrawText( COLOR_DODGERBLUE,DefaultBGColor,x+76,y,\"RB:\" );\n" + 
 						"    for ( int a=0;a<3;a++ )\n" + 
 						"    {\n" + 
 						"      ReefAngel.LCD.DrawText( COLOR_DODGERBLUE,DefaultBGColor,x+18,y,ReefAngel.AI.GetChannel(a) );\n" + 
 						"      x += 38;\n" + 
 						"    }\n" + 
 						"    pingSerial();\n\n";
 				y+=10;
 			}
 			if (ioexpansion==1)
 			{
 				y+=h;
 				d+="    // I/O Expansion\n" + 
 						"    byte bkcolor;\n" + 
 						"    x = 14;\n" + 
 						"    y = " + y + ";\n" + 
 						"    for ( int a=0;a<6;a++ )\n" + 
 						"    {\n" + 
 						"      ReefAngel.LCD.DrawCircleOutline( x+(a*20),y,4,COLOR_MEDIUMORCHID );\n" + 
 						"      if ( ReefAngel.IO.GetChannel(a) ) bkcolor=COLOR_WHITE; else bkcolor=COLOR_GRAY;\n" + 
 						"      ReefAngel.LCD.FillCircle( x+(a*20),y,2,bkcolor );\n" + 
 						"    }\n" + 
 						"    pingSerial();\n\n";
 				y+=5;
 			}
 
 			y+=h;
 			if (salinityexpansion==0 && orpexpansion==0 && phexpansion==0 && waterlevelexpansion==0 && ioexpansion==0 && ailed==0 && dimmingexpansion==0 && relayexpansion==1 )
 			{
 				y=62;
 				h=3;
 			}
 			d+="    // Parameters\n" + 
 					"#if defined DisplayLEDPWM && ! defined RemoveAllLights\n" + 
 					"    ReefAngel.LCD.DrawMonitor( 15, " + y + ", ReefAngel.Params,\n" + 
 					"    ReefAngel.PWM.GetDaylightValue(), ReefAngel.PWM.GetActinicValue() );\n" + 
 					"#else // defined DisplayLEDPWM && ! defined RemoveAllLights\n" + 
 					"    ReefAngel.LCD.DrawMonitor( 15, " + y + ", ReefAngel.Params );\n" + 
 					"#endif // defined DisplayLEDPWM && ! defined RemoveAllLights\n" + 
 					"    pingSerial();\n\n";
 			y+=28;
 
 
 			if (salinityexpansion==1 || orpexpansion==1)
 			{
 				y+=h;
 
 				if (salinityexpansion==1)
 				{
 					d+="    // Salinity\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_DARKKHAKI,DefaultBGColor,15," + y + ", \"SAL:\" );\n" + 
 							"    ReefAngel.LCD.DrawSingleMonitor( ReefAngel.Params.Salinity,COLOR_DARKKHAKI,39," + y + ", 10 );    \r\n" + 
 							"    pingSerial();\n\n";
 				}
 				if (orpexpansion==1)
 				{
 					d+="    // ORP\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_PALEVIOLETRED,DefaultBGColor,75," + y + ", \"ORP:\" );\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_PALEVIOLETRED,DefaultBGColor,99," + y + ", ReefAngel.Params.ORP );\n" + 
 							"    pingSerial();\n\n";
 				}
 
 				y+=9;
 			}
 
 			if (phexpansion==1 || waterlevelexpansion==1)
 			{
 				y+=h;
 
 				if (phexpansion==1)
 				{
 					d+="    // pH Expansion\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_MEDIUMSEAGREEN,DefaultBGColor,15," + y + ", \"PHE:\" );\n" + 
							"    ReefAngel.LCD.DrawSingleMonitor( ReefAngel.Params.PHExp,COLOR_MEDIUMSEAGREEN,39," + y + ", 100 );    \r\n" + 
 							"    pingSerial();\n\n";
 				}
 				if (waterlevelexpansion==1)
 				{
 					d+="    // Water Level\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_DARKGOLDENROD,DefaultBGColor,75," + y + ", \"WL:\" );\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_DARKGOLDENROD,DefaultBGColor,99," + y + ", ReefAngel.WaterLevel.GetLevel() );\n" + 
 							"    pingSerial();\n\n";
 				}
 
 				y+=8;
 			}
 
 			y+=h;
 
 			d+="    // Main Relay Box\n" + 
 					"    byte TempRelay = ReefAngel.Relay.RelayData;\n" + 
 					"    TempRelay &= ReefAngel.Relay.RelayMaskOff;\n" + 
 					"    TempRelay |= ReefAngel.Relay.RelayMaskOn;\n" + 
 					"    ReefAngel.LCD.DrawOutletBox( 12, " + y + ", TempRelay );\n" + 
 					"    pingSerial();\n\n";
 			y+=11;
 			if (relayexpansion==1)
 			{
 				y+=h;
 				d+="    // Relay Expansion\n" + 
 						"    TempRelay = ReefAngel.Relay.RelayDataE[0];\n" + 
 						"    TempRelay &= ReefAngel.Relay.RelayMaskOffE[0];\n" + 
 						"    TempRelay |= ReefAngel.Relay.RelayMaskOnE[0];\n" + 
 						"    ReefAngel.LCD.DrawOutletBox( 12, " + y + ", TempRelay );\n" +
 						"    pingSerial();\n\n";
 				y+=10;
 
 			}
 
 			y+=h;
 
 			d+="    // Date and Time\n" + 
 					"    ReefAngel.LCD.DrawDate( 6, 122 );\n" + 
 					"    pingSerial();\n";
 
 			d+="}\n" + 
 					"\n" + 
 					"void DrawCustomGraph()\n" + 
 					"{\n";  
 			if (salinityexpansion==0 && orpexpansion==0 && ioexpansion==0 && ailed==0 && dimmingexpansion==0 && relayexpansion==1 )
 				d+="    ReefAngel.LCD.DrawGraph( 5, 5 );\n";
 			d+="}\n";
 		}
 //		editor.getBase().handleNewReplace();
 		editor.handleSelectAll();
 		editor.setSelectedText(d);
 //		if (upload) 
 //		{
 //			editor.handleExport(false);
 //			ShowUploading();
 //		}
 	}
 	
 	
 	private void ConstructMemCode()
 	{
 		String s="";
 		boolean bfunction=false;
 		
 		s="#include <ReefAngel_Features.h>\n" + 
 				"#include <Globals.h>\n" + 
 				"#include <RA_Wifi.h>\n" + 
 				"#include <Wire.h>\n" + 
 				"#include <OneWire.h>\n" + 
 				"#include <Time.h>\n" + 
 				"#include <DS1307RTC.h>\n" + 
 				"#include <InternalEEPROM.h>\n" + 
 				"#include <RA_NokiaLCD.h>\n" + 
 				"#include <RA_ATO.h>\n" + 
 				"#include <RA_Joystick.h>\n" + 
 				"#include <LED.h>\n" + 
 				"#include <RA_TempSensor.h>\n" + 
 				"#include <Relay.h>\n" + 
 				"#include <RA_PWM.h>\n" + 
 				"#include <Timer.h>\n" + 
 				"#include <Memory.h>\n" + 
 				"#include <InternalEEPROM.h>\n" + 
 				"#include <RA_Colors.h>\n" + 
 				"#include <RA_CustomColors.h>\n" + 
 				"#include <Salinity.h>\n" + 
 				"#include <RF.h>\n" + 
 				"#include <IO.h>\n" + 
 				"#include <ORP.h>\n" + 
 				"#include <AI.h>\n" + 
 				"#include <PH.h>\n" + 
 				"#include <WaterLevel.h>\n" + 
 				"#include <ReefAngel.h>\n\n" +
 				"\r\n" + 
 				"RA_NokiaLCD e;\r\n" + 
 				"\r\n" + 
 				"void setup()\r\n" + 
 				"{\r\n" + 
 				"    e.Init();\r\n" + 
 				"    e.Clear(COLOR_WHITE,0,0,132,132);\r\n" + 
 				"    e.BacklightOn();\r\n" + 
 				"\r\n";
 		s+="    InternalMemory.OverheatTemp_write( " + (int)((Double)Overheat.getValue() *10) + " );\r\n";
 		
 		//Timed
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(0)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			Calendar calendar = GregorianCalendar.getInstance();  
 			calendar.setTime((Date) ((JSpinner)TimedMemorySettings[1].getComponent(2)).getValue());  
 			s+="    InternalMemory.StdLightsOnHour_write( " + calendar.get(Calendar.HOUR_OF_DAY) + " );\r\n";
 			s+="    InternalMemory.StdLightsOnMinute_write( " + calendar.get(Calendar.MINUTE) + " );\r\n";
 			calendar.setTime((Date) ((JSpinner)TimedMemorySettings[1].getComponent(4)).getValue());  
 			s+="    InternalMemory.StdLightsOffHour_write( " + calendar.get(Calendar.HOUR_OF_DAY) + " );\r\n";
 			s+="    InternalMemory.StdLightsOffMinute_write( " + calendar.get(Calendar.MINUTE) + " );\r\n";
 			if (((Integer) ((JSpinner)TimedMemorySettings[1].getComponent(6)).getValue()) >0)
 				s+="    InternalMemory.DelayedStart_write( " + ((JSpinner)TimedMemorySettings[1].getComponent(6)).getValue() + " );\r\n";
 				
 			s+="    InternalMemory.ActinicOffset_write( " + ((JSpinner)TimedMemorySettings[1].getComponent(8)).getValue() + " );\r\n";
 		}
 
 		//Heater
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(1)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.HeaterTempOn_write( " + (int)(((Double)((JSpinner)Heater[1].getComponent(2)).getValue()) * 10) + " );\r\n";
 			s+="    InternalMemory.HeaterTempOff_write( " + (int)(((Double)((JSpinner)Heater[1].getComponent(4)).getValue()) * 10) + " );\r\n";
 		}
 
 		//Chiller
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(2)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.ChillerTempOn_write( " + (int)(((Double)((JSpinner)Chiller[1].getComponent(2)).getValue()) * 10) + " );\r\n";
 			s+="    InternalMemory.ChillerTempOff_write( " + (int)(((Double)((JSpinner)Chiller[1].getComponent(4)).getValue()) * 10) + " );\r\n";
 		}
 
 		//ATO
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(3)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.ATOExtendedTimeout_write( " + ((JSpinner)ATO[1].getComponent(5)).getValue()  + " );\r\n";
 			s+="    InternalMemory.ATOHourInterval_write( 0 );\r\n";
 		}
 		
 		//WM
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(4)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.WM1Timer_write( " + ((JSpinner)WM[1].getComponent(4)).getValue()  + " );\r\n";
 			s+="    InternalMemory.WM2Timer_write( " + ((JSpinner)WM[1].getComponent(4)).getValue()  + " );\r\n";
 		}
 		
 		//CO2Control
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(5)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.CO2ControlOn_write( " + ((JSpinner)CO2Control[1].getComponent(2)).getValue()  + " );\r\n";
 			s+="    InternalMemory.CO2ControlOff_write( " + ((JSpinner)CO2Control[1].getComponent(4)).getValue()  + " );\r\n";
 		}
 		
 		//PHControl
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(6)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.PHControlOn_write( " + ((JSpinner)pHControl[1].getComponent(2)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PHControlOff_write( " + ((JSpinner)pHControl[1].getComponent(4)).getValue()  + " );\r\n";
 		}
 		
 		//Dosing
 		int numdosing=0;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(7)).isSelected())
 			{
 				numdosing++;
 				if (numdosing==1)
 				{
 					s+="    InternalMemory.DP1RepeatInterval_write( " + ((JSpinner)Dosing[a].getComponent(2)).getValue()  + " );\r\n";
 					s+="    InternalMemory.DP1Timer_write( " + ((JSpinner)Dosing[a].getComponent(4)).getValue()  + " );\r\n";
 				}
 				if (numdosing==2)
 				{
 					s+="    InternalMemory.DP2RepeatInterval_write( " + ((JSpinner)Dosing[a].getComponent(2)).getValue()  + " );\r\n";
 					s+="    InternalMemory.DP2Timer_write( " + ((JSpinner)Dosing[a].getComponent(4)).getValue()  + " );\r\n";
 				}
 				if (numdosing==3)
 				{
 					s+="    InternalMemory.DP3RepeatInterval_write( " + ((JSpinner)Dosing[a].getComponent(2)).getValue()  + " );\r\n";
 					s+="    InternalMemory.DP3Timer_write( " + ((JSpinner)Dosing[a].getComponent(4)).getValue()  + " );\r\n";
 				}
 			}
 		}
 
 		//Delayed
 		bfunction=false;
 		for (int a=1;a<16;a++)
 		{
 			if (((JRadioButton) functions[a].getComponent(8)).isSelected())
 				bfunction=true;
 		}
 		if (bfunction)
 		{
 			s+="    InternalMemory.DelayedStart_write( " + ((JSpinner)Delayed[1].getComponent(2)).getValue()  + " );\r\n";
 		}
 		
 		// Daylight PWM
 		if (((JRadioButton) daylightpwm.getComponent(0)).isSelected())
 		{
 			s+="    InternalMemory.PWMSlopeStartD_write( " + ((JSpinner)daylightpwmsettings.getComponent(8)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PWMSlopeEndD_write( " + ((JSpinner)daylightpwmsettings.getComponent(10)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PWMSlopeDurationD_write( " + ((JSpinner)daylightpwmsettings.getComponent(14)).getValue()  + " );\r\n";
 		}
 		if (((JRadioButton) daylightpwm.getComponent(1)).isSelected())
 		{
 			s+="    InternalMemory.PWMSlopeStartD_write( " + ((JSpinner)daylightpwmsettings.getComponent(8)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PWMSlopeEndD_write( " + ((JSpinner)daylightpwmsettings.getComponent(10)).getValue()  + " );\r\n";
 		}
 		
 		// Actinic PWM
 		if (((JRadioButton) actinicpwm.getComponent(0)).isSelected())
 		{
 			s+="    InternalMemory.PWMSlopeStartA_write( " + ((JSpinner)actinicpwmsettings.getComponent(8)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PWMSlopeEndA_write( " + ((JSpinner)actinicpwmsettings.getComponent(10)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PWMSlopeDurationA_write( " + ((JSpinner)actinicpwmsettings.getComponent(14)).getValue()  + " );\r\n";
 		}
 		if (((JRadioButton) actinicpwm.getComponent(1)).isSelected())
 		{
 			s+="    InternalMemory.PWMSlopeStartA_write( " + ((JSpinner)actinicpwmsettings.getComponent(8)).getValue()  + " );\r\n";
 			s+="    InternalMemory.PWMSlopeEndA_write( " + ((JSpinner)actinicpwmsettings.getComponent(10)).getValue()  + " );\r\n";
 		}
 
 		// Dimming Expansion
 		for (int a=0;a<6;a++)
 		{
 			if (((JRadioButton) exppwm[a].getComponent(0)).isSelected())
 			{
 				s+="    InternalMemory.PWMSlopeStart" + a + "_write( " + ((JSpinner)exppwmsettings[a].getComponent(8)).getValue()  + " );\r\n";
 				s+="    InternalMemory.PWMSlopeEnd" + a + "_write( " + ((JSpinner)exppwmsettings[a].getComponent(10)).getValue()  + " );\r\n";
 				s+="    InternalMemory.PWMSlopeDuration" + a + "_write( " + ((JSpinner)exppwmsettings[a].getComponent(14)).getValue()  + " );\r\n";
 			}
 			if (((JRadioButton) exppwm[a].getComponent(1)).isSelected())
 			{
 				s+="    InternalMemory.PWMSlopeStart" + a + "_write( " + ((JSpinner)exppwmsettings[a].getComponent(8)).getValue()  + " );\r\n";
 				s+="    InternalMemory.PWMSlopeEnd" + a + "_write( " + ((JSpinner)exppwmsettings[a].getComponent(10)).getValue()  + " );\r\n";
 			}
 		}
 		
 		// AI
 		String AIc[] = {"W","B","RB"};
 		for (int a=0;a<3;a++)
 		{
 			if (((JRadioButton) aipwm[a].getComponent(0)).isSelected())
 			{
 				s+="    InternalMemory.AISlopeStart" + AIc[a] + "_write( " + ((JSpinner)aipwmsettings[a].getComponent(8)).getValue()  + " );\r\n";
 				s+="    InternalMemory.AISlopeEnd" + AIc[a] + "_write( " + ((JSpinner)aipwmsettings[a].getComponent(10)).getValue()  + " );\r\n";
 				s+="    InternalMemory.AISlopeDuration" + AIc[a] + "_write( " + ((JSpinner)aipwmsettings[a].getComponent(14)).getValue()  + " );\r\n";
 			}
 			if (((JRadioButton) aipwm[a].getComponent(1)).isSelected())
 			{
 				s+="    InternalMemory.AISlopeStart" + AIc[a] + "_write( " + ((JSpinner)aipwmsettings[a].getComponent(8)).getValue()  + " );\r\n";
 				s+="    InternalMemory.AISlopeEnd" + AIc[a] + "_write( " + ((JSpinner)aipwmsettings[a].getComponent(10)).getValue()  + " );\r\n";
 			}
 		}
 
 		// RF
 		String RFc[] = {"W","RB","R","G","B","I"};
 		for (int a=0;a<6;a++)
 		{
 			if (((JRadioButton) rfpwm[a].getComponent(0)).isSelected())
 			{
 				s+="    InternalMemory.RadionSlopeStart" + RFc[a] + "_write( " + ((JSpinner)rfpwmsettings[a].getComponent(8)).getValue()  + " );\r\n";
 				s+="    InternalMemory.RadionSlopeEnd" + RFc[a] + "_write( " + ((JSpinner)rfpwmsettings[a].getComponent(10)).getValue()  + " );\r\n";
 				s+="    InternalMemory.RadionSlopeDuration" + RFc[a] + "_write( " + ((JSpinner)rfpwmsettings[a].getComponent(14)).getValue()  + " );\r\n";
 			}
 			if (((JRadioButton) rfpwm[a].getComponent(1)).isSelected())
 			{
 				s+="    InternalMemory.RadionSlopeStart" + RFc[a] + "_write( " + ((JSpinner)rfpwmsettings[a].getComponent(8)).getValue()  + " );\r\n";
 				s+="    InternalMemory.RadionSlopeEnd" + RFc[a] + "_write( " + ((JSpinner)rfpwmsettings[a].getComponent(10)).getValue()  + " );\r\n";
 			}
 		}
 		
 		s+="    InternalMemory.IMCheck_write(0xCF06A31E);\r\n" + 
 				"    e.DrawText(COLOR_BLACK, COLOR_WHITE, MENU_START_COL+20, MENU_START_ROW*3, \"Memory Updated\");\r\n" + 
 				"    e.DrawText(COLOR_BLACK, COLOR_WHITE, MENU_START_COL+25, MENU_START_ROW*6, \"You can now\");\r\n" + 
 				"    e.DrawText(COLOR_BLACK, COLOR_WHITE, MENU_START_COL+25, MENU_START_ROW*7, \"upload your\");\r\n" + 
 				"    e.DrawText(COLOR_BLACK, COLOR_WHITE, MENU_START_COL+33, MENU_START_ROW*8, \"INO code\");\r\n" + 
 				"}\r\n" + 
 				"\r\n" + 
 				"void loop()\r\n" + 
 				"{\r\n" + 
 				"}\r\n";
 		
 		editor.getBase().handleNewReplace();
 		editor.setSelectedText(s);
 
 		}
 	
 	private void ShowWelcome()
 	{
 		insidePanel.add(new Inside("<HTML>Welcome to the Reef Angel Wizard.<br><br>I'm going to walk you through the whole process of generating a code for your Reef Angel Controller.<br><br>Version: ##tool.version##<br><br></HTML>"), Titles[0]);	
 	}
 
 	private void ShowMemorySettings()
 	{
 		JPanel j = new JPanel();
 //		j.setLayout(new BoxLayout( j, BoxLayout.PAGE_AXIS));
 		j.setLayout(new GridLayout(2,1));
 		j.setOpaque(false);
 		Inside i = new Inside("<HTML>User settings can be stored within the code itself or inside the intenal memory of the controller.<br>Example of these settings are light schedule, heater/chiller temperature and many other settings.<br>Settings stored within the code itself is the easiest way to program your controller because it will not require you to use any other application. This is the recommended method for new users.<br>The code generated by the Reef Angel Wizard will contain all the information needed and the<br>controller will be ready to be used.<br>Settings that are stored inside the intenal memory will require you to use another application after you upload the code generated by the Reef Angel Wizard.<br>The benefit of using another application is that you will be able to change it at any time without having to generate a new code.<br>One good example is changing your light schedule using one of the smart phone apps.<br><br>Where would you like to store your settings?</HTML>");
 //		i.setAlignmentX(Component.LEFT_ALIGNMENT);
 //		memsettings.setAlignmentX(Component.LEFT_ALIGNMENT);
 		JPanel j1=new JPanel();
 //		j1.setLayout(new GridLayout(2,1));
 		memsettings.setOpaque(false);
 		j1.add(memsettings);
 		j1.setOpaque(false);
 //		j1.add(new JLabel("Test"));
 		
 		j.add(i);
 		j.add(j1);
 		insidePanel.add(j, Titles[1]);	
 	}
 
 	private void ShowTemperature()
 	{
 
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		//		j.setLayout(new BoxLayout( j, BoxLayout.PAGE_AXIS));
 		j.setLayout(layoutConstraintsManager.createLayout("temppanel", j));
 		Inside i = new Inside("<HTML>How would you like to see your temperature readings?<br><br></HTML>");
 		j.add(i,"i");
 		JPanel j1 = new JPanel();
 		j1.setOpaque(false);
 		disptemp.setOpaque(false);
 		j1.add(disptemp);
 		j.add(j1,"disptemp");
 		JLabel text1 = new JLabel("<HTML><br>Which temperature would you like to set the controller to flag for Overheat?<br><br></HTML>");
 		j.add(text1,"text1");
 		JPanel j2 = new JPanel();
 		j2.setOpaque(false);
 		OverheatSettings.setOpaque(false);
 		j2.add(OverheatSettings);
 		j.add(j2,"OverheatSettings");
 
 		insidePanel.add(j,Titles[2]);
 	}
 	
 	private void ShowExpansion()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("exppanel", j));
 		Inside i = new Inside("<HTML>Please select the expansion modules you have:<br><br></HTML>");
 		j.add(i,"i");
 		expansionmods.setOpaque(false);
 		j.add(expansionmods,"expansionmods");
 
 		insidePanel.add(j,Titles[3]);		
 	}	
 	
 	private void ShowAttachment()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("attchpanel", j));
 		Inside i = new Inside("<HTML>Please select the attachments you have:<br><br></HTML>");
 		j.add(i,"i");
 		attachmentmods.setOpaque(false);
 		j.add(attachmentmods,"attachmentmods");
 
 		insidePanel.add(j,Titles[4]);		
 	}	
 
 	private void ShowRelays()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("relaypanel", j));
 		Inside i = new Inside("<HTML>On the following 8 steps, we are going to be assigning a function for each port on the main relay box.<br><br>Each port number is identified on the picture below.<br><br></HTML>");
 		j.add(i,"i");
 		ImageIcon iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/relay_small.png");
 		JLabel c = new JLabel(iconnection);
 		j.add(c,"icon");
 
 		insidePanel.add(j,Titles[5]);	
 
 		j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("exprelaypanel", j));
 		i = new Inside("<HTML>On the following 8 steps, we are going to be assigning a function for each port on the expansion relay box.<br><br>Each port number is identified on the picture below.<br><br></HTML>");
 		j.add(i,"i");
 		iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/relay_small.png");
 		c = new JLabel(iconnection);
 		j.add(c,"icon");
 
 		insidePanel.add(j,Titles[14]);	
 	}
 
 	private void ShowRelaySetup()
 	{
 		for (int a=1;a<=8;a++)
 		{
 			JPanel j = new JPanel();
 			j.setOpaque(false);
 			j.setLayout(layoutConstraintsManager.createLayout("relaysetuppanel", j));
 			Inside i = new Inside("<HTML>Choose which function you would like to assign for Port " + a + " of your main relay box:<br><br></HTML>");
 			j.add(i,"i");
 			functions[a].setOpaque(false);
 			j.add(functions[a],"functions");
 			functionsettings[a].setOpaque(false);
 			j.add(functionsettings[a],"functionsettings");
 			ports[a].setOpaque(false);
 			j.add(ports[a],"ports");
 
 			insidePanel.add(j,Titles[a+5]);	
 		}		
 
 		for (int a=1;a<=8;a++)
 		{
 			JPanel j = new JPanel();
 			j.setOpaque(false);
 			j.setLayout(layoutConstraintsManager.createLayout("relaysetuppanel", j));
 			Inside i = new Inside("<HTML>Choose which function you would like to assign for Port " + a + " of your expansion relay box:<br><br></HTML>");
 			j.add(i,"i");
 			functions[a+8].setOpaque(false);
 			j.add(functions[a+8],"functions");
 			functionsettings[a+8].setOpaque(false);
 			j.add(functionsettings[a+8],"functionsettings");
 			ports[a+8].setOpaque(false);
 			j.add(ports[a+8],"ports");
 
 			insidePanel.add(j,Titles[a+14]);	
 		}			
 	}
 	
 	private void ShowPWM()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("pwmpanel", j));
 		Inside i = new Inside("<HTML>What type of waveform would you like to use on your daylight dimming channel?<br><br></HTML>");
 		j.add(i,"i");
 		ImageIcon iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_dimming.png");
 		JLabel c = new JLabel(iconnection);
 		j.add(c,"icon");
 		j.add(daylightpwm,"pwm");
 //		j.add(daylightpwm1,"pwm1");
 		
 		insidePanel.add(j,Titles[23]);
 		
 		j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("pwmpanel", j));
 		i = new Inside("<HTML>What type of waveform would you like to use on your actinic dimming channel?<br><br></HTML>");
 		j.add(i,"i");
 		iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_dimming.png");
 		c = new JLabel(iconnection);
 		j.add(c,"icon");
 		j.add(actinicpwm,"pwm");
 //		j.add(actinicpwm1,"pwm1");
 		
 		insidePanel.add(j,Titles[25]);
 	}	
 
 	private void ShowPWMSettings()
 	{
 		daylightpwmsettingspanel = new JPanel();
 		daylightpwmsettingspanel.setOpaque(false);
 		daylightpwmsettingspanel.setLayout(layoutConstraintsManager.createLayout("pwmsettingspaneld", daylightpwmsettingspanel));
 		Inside id = new Inside("<HTML>test</HTML>");
 		ImageIcon iconnectiond = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 		daylightpwmsettingspanel.add(id,"id");
 		JLabel cd = new JLabel(iconnectiond);
 		daylightpwmsettingspanel.add(cd,"icond");
 		daylightpwmsettingspanel.add(daylightpwmsettings,"pwmd");
 		daylightpwmsettingspanel.add(new JLabel(""),"memlabel");
 		insidePanel.add(daylightpwmsettingspanel,Titles[24]);
 		
 		actinicpwmsettingspanel = new JPanel();
 		actinicpwmsettingspanel.setOpaque(false);
 		actinicpwmsettingspanel.setLayout(layoutConstraintsManager.createLayout("pwmsettingspanela", actinicpwmsettingspanel));
 		Inside ia = new Inside("<HTML>test</HTML>");
 		ImageIcon iconnectiona = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 		actinicpwmsettingspanel.add(ia,"ia");
 		JLabel ca = new JLabel(iconnectiona);
 		actinicpwmsettingspanel.add(ca,"icona");
 		actinicpwmsettingspanel.add(actinicpwmsettings,"pwma");
 		actinicpwmsettingspanel.add(new JLabel(""),"memlabel");
 		insidePanel.add(actinicpwmsettingspanel,Titles[26]);
 	}
 	
 
 	private void ShowExpPWM()
 	{
 		for(int a=0;a<6;a++)
 		{
 			JPanel j = new JPanel();
 			j.setOpaque(false);
 			j.setLayout(layoutConstraintsManager.createLayout("pwmpanel", j));
 			Inside i = new Inside("<HTML>What type of waveform would you like to use on your dimming expansion channel " + a + "?<br><br></HTML>");
 			j.add(i,"i");
 			ImageIcon iconnection = null;
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/pwm_dimming.png");
 			JLabel c = new JLabel(iconnection);
 			j.add(c,"icon");
 			j.add(exppwm[a],"pwm");
 	//		j.add(daylightpwm1,"pwm1");
 			
 			insidePanel.add(j,Titles[27+(a*2)]);
 		}
 	}		
 	
 	private void ShowExpPWMSettings()
 	{
 		for(int a=0;a<6;a++)
 		{
 			exppwmsettingspanel[a] = new JPanel();
 			exppwmsettingspanel[a].setOpaque(false);
 			exppwmsettingspanel[a].setLayout(layoutConstraintsManager.createLayout("pwmsettingspaneld", exppwmsettingspanel[a]));
 			Inside id = new Inside("<HTML>test</HTML>");
 			ImageIcon iconnectiond = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 			exppwmsettingspanel[a].add(id,"id");
 			JLabel cd = new JLabel(iconnectiond);
 			exppwmsettingspanel[a].add(cd,"icond");
 			exppwmsettingspanel[a].add(exppwmsettings[a],"pwmd");
 			exppwmsettingspanel[a].add(new JLabel(""),"memlabel");			
 			insidePanel.add(exppwmsettingspanel[a],Titles[28+(a*2)]);
 		}		
 	}	
 	
 	private void ShowAIPort()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("aipanel", j));
 		Inside i = new Inside("<HTML>Which ATO port would you like to use to communicate with your AI fixture?<br><br>Port choice that has been disabled was assigned to ATO function previously.<br><br></HTML>");
 		j.add(i,"i");
 		ImageIcon iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_ato.png");
 		JLabel c = new JLabel(iconnection);
 		j.add(c,"icon");
 		aiport.setOpaque(false);
 		j.add(aiport,"aiport");
 
 		insidePanel.add(j,Titles[39]);			
 	}		
 	
 	private void ShowAIPWM()
 	{
 		for(int a=0;a<3;a++)
 		{
 			JPanel j = new JPanel();
 			j.setOpaque(false);
 			j.setLayout(layoutConstraintsManager.createLayout("pwmpanel", j));
 			Inside i = new Inside("<HTML>What type of waveform would you like to use on your " + AIChannels[a] + " channel of your Aqua Illumination fixture?<br><br></HTML>");
 			j.add(i,"i");
 			ImageIcon iconnection = null;
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ai.png");
 			JLabel c = new JLabel(iconnection);
 			j.add(c,"icon");
 			j.add(aipwm[a],"pwm");
 	//		j.add(daylightpwm1,"pwm1");
 			
 			insidePanel.add(j,Titles[40+(a*2)]);
 		}		
 	}		
 
 	private void ShowAIPWMSettings()
 	{
 		for(int a=0;a<3;a++)
 		{
 			aisettingspanel[a] = new JPanel();
 			aisettingspanel[a].setOpaque(false);
 			aisettingspanel[a].setLayout(layoutConstraintsManager.createLayout("pwmsettingspaneld", aisettingspanel[a]));
 			Inside id = new Inside("<HTML>test</HTML>");
 			ImageIcon iconnectiond = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 			aisettingspanel[a].add(id,"id");
 			JLabel cd = new JLabel(iconnectiond);
 			aisettingspanel[a].add(cd,"icond");
 			aisettingspanel[a].add(aipwmsettings[a],"pwmd");
 			aisettingspanel[a].add(new JLabel(""),"memlabel");			
 			insidePanel.add(aisettingspanel[a],Titles[41+(a*2)]);
 		}		
 	}		
 	
 
 	private void ShowVortech()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("relaypanel", j));
 		Inside i = new Inside("<HTML>Please choose the default vortech mode:<br><br></HTML>");
 		j.add(i,"i");
 		RFmods.setOpaque(false);
 		j.add(RFmods,"icon");
 
 		insidePanel.add(j,Titles[46]);	
 	}		
 	
 
 	private void ShowRFPWM()
 	{
 		for(int a=0;a<6;a++)
 		{
 			JPanel j = new JPanel();
 			j.setOpaque(false);
 			j.setLayout(layoutConstraintsManager.createLayout("pwmpanel", j));
 			Inside i = new Inside("<HTML>What type of waveform would you like to use on your " + RadionChannels[a] + " channel of your Ecotech Radion fixture?<br><br></HTML>");
 			j.add(i,"i");
 			ImageIcon iconnection = null;
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/radion.png");
 			JLabel c = new JLabel(iconnection);
 			j.add(c,"icon");
 			j.add(rfpwm[a],"pwm");
 			
 			insidePanel.add(j,Titles[47+(a*2)]);
 		}		
 	}		
 
 	private void ShowRFPWMSettings()
 	{
 		
 		for(int a=0;a<6;a++)
 		{
 			rfsettingspanel[a] = new JPanel();
 			rfsettingspanel[a].setOpaque(false);
 			rfsettingspanel[a].setLayout(layoutConstraintsManager.createLayout("pwmsettingspaneld", rfsettingspanel[a]));
 			Inside id = new Inside("<HTML>test</HTML>");
 			ImageIcon iconnectiond = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 			rfsettingspanel[a].add(id,"id");
 			JLabel cd = new JLabel(iconnectiond);
 			rfsettingspanel[a].add(cd,"icond");
 			rfsettingspanel[a].add(rfpwmsettings[a],"pwmd");
 			rfsettingspanel[a].add(new JLabel(""),"memlabel");			
 			insidePanel.add(rfsettingspanel[a],Titles[48+(a*2)]);
 		}		
 	}		
 
 	private void ShowWifi()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("relaypanel", j));
 		Inside i = new Inside("<HTML>Please enter your forum username to send data to the Portal :<br><br></HTML>");
 		j.add(i,"i");
 		wifiportal.setOpaque(false);
 		j.add(wifiportal,"icon");
 
 		insidePanel.add(j,Titles[59]);	
 	}	
 	
 	private void ShowBuzzer()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("relaypanel", j));
 		Inside i = new Inside("<HTML>Please choose when to turn buzzer on:<br><br></HTML>");
 		j.add(i,"i");
 		Buzzermods.setOpaque(false);
 		j.add(Buzzermods,"icon");
 
 		insidePanel.add(j,Titles[60]);	
 	}	
 	private void ShowGenerate()
 	{
 		
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(new GridLayout(2,1));
 		Inside i = new Inside("<HTML>We are now ready to upload your code to your Reef Angel Controller.<br><br>Please make sure the controller is powered up and connect the USB-TTL cable to your Reef Angel Controller.</HTML>");
 		JPanel j1=new JPanel();
 		ImageIcon iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/connection.png");
 		JLabel c = new JLabel(iconnection, JLabel.CENTER);
 		j1.add(c);
 		j1.setOpaque(false);
 		j.add(i);
 		j.add(j1);
 
 		insidePanel.add(j,"Ready to Generate");
 	}
 
 	private void ShowInitialMemory()
 	{
 		
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(new GridLayout(2,1));
 		Inside i = new Inside("<HTML>We are now going to upload your internal memory settings to your Reef Angel Controller.<br><br>Please make sure the controller is powered up and connect the USB-TTL cable to your Reef Angel Controller.</HTML>");
 		JPanel j1=new JPanel();
 		ImageIcon iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/connection.png");
 		JLabel c = new JLabel(iconnection, JLabel.CENTER);
 		j1.add(c);
 		j1.setOpaque(false);
 		j.add(i);
 		j.add(j1);
 
 		insidePanel.add(j,"Upload Memory Settings");
 	}
 	
 	private void ShowUploading()
 	{
 		JPanel panel = new JPanel();
 
 		AddPanel(panel);
 		JPanel panel2 = new JPanel();
 
 		panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Reef Angel Wizard");
 		steps.setForeground(new Color(58,95,205));
 		steps.setFont(new Font("Arial", Font.BOLD, 24));
 		panel2.add(steps);
 		JLabel text = new JLabel("<HTML><br>Arduino is now compiling your code.<br>In a few seconds, it will start uploading the code to your Reef Angel Controller.<br><br></HTML>");
 		panel2.add(text);
 		panel.add(panel2);
 		JOptionPane.showMessageDialog(editor,
 				panel,
 				"Reef Angel Wizard",JOptionPane.DEFAULT_OPTION);		
 	}	
 	
 	private void ShowDone()
 	{
 		JPanel j = new JPanel();
 		j.setOpaque(false);
 		j.setLayout(layoutConstraintsManager.createLayout("relaypanel", j));
 		Inside i = new Inside("<HTML>We are done. You code was uploaded successfully to your Reef Angel controller.<br><br>You can repeat these steps at any time if you want to change anything in the future.<br><br></HTML>");
 		j.add(i,"i");
 
 		insidePanel.add(j,"Done");	
 	}
 	
 	private static boolean isBrowsingSupported() { 
 		if (!Desktop.isDesktopSupported()) { 
 			return false; 
 		} 
 		boolean result = false; 
 		Desktop desktop = java.awt.Desktop.getDesktop(); 
 		if (desktop.isSupported(Desktop.Action.BROWSE)) { 
 			result = true; 
 		} 
 		return result; 
 
 	} 
 
 	public static void copy(String fromFileName, String toFileName)
 
 			throws IOException {
 		File fromFile = new File(fromFileName);
 		File toFile = new File(toFileName);
 
 		if (!fromFile.exists())
 			throw new IOException("FileCopy: " + "no such source file: "
 					+ fromFileName);
 		if (!fromFile.isFile())
 			throw new IOException("FileCopy: " + "can't copy directory: "
 					+ fromFileName);
 		if (!fromFile.canRead())
 			throw new IOException("FileCopy: " + "source file is unreadable: "
 					+ fromFileName);
 
 		if (toFile.isDirectory())
 			toFile = new File(toFile, fromFile.getName());
 
 		if (toFile.exists()) {
 			toFile.delete();
 			//		      if (!toFile.canWrite())
 			//		        throw new IOException("FileCopy: "
 			//		            + "destination file is unwriteable: " + toFileName);
 			//		      System.out.print("Overwrite existing file " + toFile.getName()
 			//		          + "? (Y/N): ");
 			//		      System.out.flush();
 			//		      BufferedReader in = new BufferedReader(new InputStreamReader(
 			//		          System.in));
 			//		      String response = in.readLine();
 			//		      if (!response.equals("Y") && !response.equals("y"))
 			//		        throw new IOException("FileCopy: "
 			//		            + "existing file was not overwritten.");
 		} else {
 			String parent = toFile.getParent();
 			if (parent == null)
 				parent = System.getProperty("user.dir");
 			File dir = new File(parent);
 			if (!dir.exists())
 				throw new IOException("FileCopy: "
 						+ "destination directory doesn't exist: " + parent);
 			if (dir.isFile())
 				throw new IOException("FileCopy: "
 						+ "destination is not a directory: " + parent);
 			if (!dir.canWrite())
 				throw new IOException("FileCopy: "
 						+ "destination directory is unwriteable: " + parent);
 		}
 
 		FileInputStream from = null;
 		FileOutputStream to = null;
 		try {
 			from = new FileInputStream(fromFile);
 			to = new FileOutputStream(toFile);
 			byte[] buffer = new byte[4096];
 			int bytesRead;
 
 			while ((bytesRead = from.read(buffer)) != -1)
 				to.write(buffer, 0, bytesRead); // write
 		} finally {
 			if (from != null)
 				try {
 					from.close();
 				} catch (IOException e) {
 					;
 				}
 			if (to != null)
 				try {
 					to.close();
 				} catch (IOException e) {
 					;
 				}
 		}
 	}
 
 	private void AddPanel(JPanel container)
 	{
     	JPanel panel1 = new JPanel(); 
     	panel1.setLayout(new BoxLayout( panel1, BoxLayout.PAGE_AXIS));
     	ImageIcon icon=null;
 		icon = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 		JLabel logo=new JLabel(icon);
 		panel1.add(logo);
 		logo.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 	    JEditorPane RA_link = new JEditorPane("text/html","<html><center><a href='http://www.reefangel.com'>www.reefangel.com</a></center></html>");   
 		RA_link.setEditable(false);   
 		RA_link.setOpaque(false);   
 		RA_link.setBorder(BorderFactory.createEmptyBorder()); 
 		RA_link.setBackground(new Color(0,0,0,0)); 
 	    ((HTMLDocument)RA_link.getDocument()).getStyleSheet().addRule(bodyRule);
 	    panel1.add(RA_link);
 	    RA_link.addHyperlinkListener(new HyperlinkListener() {   
 		public void hyperlinkUpdate(HyperlinkEvent hle)
 		{   
 			if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
 			{   
 				if (isBrowsingSupported()) { 
 				      try { 
 				          Desktop desktop = java.awt.Desktop.getDesktop(); 
 				          URI uri = new java.net.URI(hle.getURL().toString()); 
 				          desktop.browse(uri); 
 				      } catch (URISyntaxException use) { 
 				          throw new AssertionError(use); 
 				      } catch (IOException ioe) { 
 				          ioe.printStackTrace(); 
 				          JOptionPane.showMessageDialog(null, "Sorry, a problem occurred while trying to open this link in your system's standard browser.","A problem occured", JOptionPane.ERROR_MESSAGE); 
 				      } 							       
 				} 
 
 			}   
 		}
 		});
 		
 	    container.add(panel1);
 	}
 
 	private void AddPanelRelay(JPanel container)
 	{
 		JPanel panel1 = new JPanel(); 
 		panel1.setLayout(new BoxLayout( panel1, BoxLayout.PAGE_AXIS));
 		ImageIcon icon=null;
 		icon = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
 		JLabel logo=new JLabel(icon);
 		panel1.add(logo);
 		logo.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JEditorPane RA_link = new JEditorPane("text/html","<html><center><a href='http://www.reefangel.com'>www.reefangel.com</a></center></html>");   
 		RA_link.setEditable(false);   
 		RA_link.setOpaque(false);   
 		RA_link.setBorder(BorderFactory.createEmptyBorder()); 
 		RA_link.setBackground(new Color(0,0,0,0)); 
 		((HTMLDocument)RA_link.getDocument()).getStyleSheet().addRule(bodyRule);
 		panel1.add(RA_link);
 		RA_link.addHyperlinkListener(new HyperlinkListener() {   
 			public void hyperlinkUpdate(HyperlinkEvent hle)
 			{   
 				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
 				{   
 					if (isBrowsingSupported()) { 
 						try { 
 							Desktop desktop = java.awt.Desktop.getDesktop(); 
 							URI uri = new java.net.URI(hle.getURL().toString()); 
 							desktop.browse(uri); 
 						} catch (URISyntaxException use) { 
 							throw new AssertionError(use); 
 						} catch (IOException ioe) { 
 							ioe.printStackTrace(); 
 							JOptionPane.showMessageDialog(null, "Sorry, a problem occurred while trying to open this link in your system's standard browser.","A problem occured", JOptionPane.ERROR_MESSAGE); 
 						} 							       
 					} 
 
 				}   
 			}
 		});
 		JLabel emptylabel = new JLabel("<html><br><html>"); 
 
 		panel1.add(emptylabel);
 		emptylabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		ImageIcon iconnection = null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/relay_small.png");
 		JLabel c = new JLabel(iconnection);
 		panel1.add(c);    	
 		c.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JPanel p = new JPanel();
 		p.setLayout(new BoxLayout( p, BoxLayout.PAGE_AXIS));
 		p.add(new JLabel(" "));
 		for (int a=1;a<=8;a++)
 		{
 			for (int b=0;b<RegButtons.length;b++)
 			{
 				JRadioButton r=(JRadioButton)functions[a].getComponent(b);
 				if (r.isSelected())
 				{
 					p.add(new JLabel("Main Box Port " + a + " - " + r.getText()));
 				}
 			}
 		}
 		if (relayexpansion==1)
 		{
 			for (int a=9;a<=16;a++)
 			{
 				for (int b=0;b<RegButtons.length;b++)
 				{
 					JRadioButton r=(JRadioButton)functions[a].getComponent(b);
 					if (r.isSelected())
 					{
 						p.add(new JLabel("Exp. Box Port " + (a-8) + " - " + r.getText()));
 					}
 				}
 			}
 
 		}
 
 		panel1.add(p);    	
 		p.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		container.add(panel1);
 	}
 
 	private int NextCancelButton(JPanel panel, String title)
 	{
 		int result = JOptionPane.showOptionDialog(editor,
 				panel,
 				title,
 				JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.PLAIN_MESSAGE,
 				null,
 				options,
 				options[0]);
 		switch (result)
 		{
 		case -1:
 			return 100;
 		case 0:
 			return 1;
 		case 1:
 			return 100;
 		default:
 			JOptionPane.showMessageDialog(editor,
 					"Error Code: " + result,
 					"Error",JOptionPane.ERROR_MESSAGE);
 			return 100;
 		}		  
 	}
 
 	private int NextPrevButton(JPanel panel, String title)
 	{
 		Object[] options = { "Previous","Next","Cancel" };
 		int result = JOptionPane.showOptionDialog(editor,
 				panel,
 				title,
 				JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.PLAIN_MESSAGE,
 				null,
 				options,
 				options[1]);
 
 		switch (result)
 		{
 		case -1:
 			return 100;
 		case 0:
 			return -1;
 		case 1:
 			return 1;
 		case 2:
 			return 100;
 		default:
 			JOptionPane.showMessageDialog(editor,
 					"Error Code: " + result,
 					"Error",JOptionPane.ERROR_MESSAGE);
 			return 100;
 		}		  
 	}	  
 
 	private int GeneratePrevButton(JPanel panel, String title)
 	{
 		Object[] options = { "Previous","Generate & Upload","Generate Only","Cancel" };
 		int result = JOptionPane.showOptionDialog(editor,
 				panel,
 				title,
 				JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.PLAIN_MESSAGE,
 				null,
 				options,
 				options[1]);
 
 		switch (result)
 		{
 		case -1:
 			return 100;
 		case 0:
 			return -1;
 		case 1:
 			upload=true;
 			return 1;
 		case 2:
 			upload=false;
 			return 1;
 		case 3:
 			return 100;
 		default:
 			JOptionPane.showMessageDialog(editor,
 					"Error Code: " + result,
 					"Error",JOptionPane.ERROR_MESSAGE);
 			return 100;
 		}		  
 	}	  
 
 	private void UpdateTimeLabels()
 	{
 		String swindow=Title.GetText();
 		int r=Integer.parseInt(swindow.replace("Main Relay Box - Port", "").replace("Expansion Relay Box - Port","").replace(" ", ""));
 		if (swindow.indexOf("Expansion Relay Box - Port")==0) r+=8;
 		JSpinner TimerOn = (JSpinner)TimedMemorySettings[r].getComponent(2);
 		JSpinner TimerOff = (JSpinner)TimedMemorySettings[r].getComponent(4);
 		JSpinner TimerDelay = (JSpinner)TimedMemorySettings[r].getComponent(6);
 		JSpinner ActinicOffset = (JSpinner)TimedMemorySettings[r].getComponent(8);
 		
 		for (int a=1;a<=16;a++)
 		{
 			JSpinner TimerOn1 = (JSpinner)TimedMemorySettings[a].getComponent(2);
 			JSpinner TimerOff1 = (JSpinner)TimedMemorySettings[a].getComponent(4);
 			JSpinner TimerDelay1 = (JSpinner)TimedMemorySettings[a].getComponent(6);
 			JSpinner ActinicOffset1 = (JSpinner)TimedMemorySettings[a].getComponent(8);
 
 			TimerOn1.setValue(TimerOn.getValue());
 			TimerOff1.setValue(TimerOff.getValue());
 			TimerDelay1.setValue(TimerDelay.getValue());
 			ActinicOffset1.setValue(ActinicOffset.getValue());
 			
 			JLabel lact = (JLabel)TimedMemorySettings[a].getComponent(9);
 			JLabel lmoon = (JLabel)TimedMemorySettings[a].getComponent(10);
 			JLabel ltd = (JLabel)TimedMemory[a].getComponent(2);
 			JLabel lta = (JLabel)TimedMemory[a].getComponent(4);
 			JLabel ltm = (JLabel)TimedMemory[a].getComponent(6);
 
 	    	Date dt=null; 
 			DateFormat formatter = new SimpleDateFormat("h:mm a");
 
 			String s="";
 			String s1="Turns on at ";
 			dt = (Date) TimerOn.getValue();
 			s1+=formatter.format(dt);
 			s1+=" and turns off at ";
 			dt = (Date) TimerOff.getValue();
 			s1+=formatter.format(dt);
 			
 			ltd.setText(s1);
 			s1=s1.replace("Turns on at ", "Start Time is ");
 			s1=s1.replace(" and turns off at ", " and End Time is");
 			((JLabel)daylightpwmsettingspanel.getComponent(3)).setText(s1);
 			for (int b=0;b<6;b++)
 			{
 				((JLabel)exppwmsettingspanel[b].getComponent(3)).setText(s1);
 				((JLabel)rfsettingspanel[b].getComponent(3)).setText(s1);
 				((JLabel)rfsettingspanel[b].getComponent(3)).setText(s1);
 			}
 			for (int b=0;b<3;b++)
 			{
 				((JLabel)aisettingspanel[b].getComponent(3)).setText(s1);
 			}
 			
 			s="Your Actinics will turn on at ";
 			s1="Turns on at ";
 			dt = (Date) TimerOn.getValue();
 			dt.setTime(dt.getTime()-((Integer) ActinicOffset.getValue() * 60 * 1000)) ;
 			s+=formatter.format(dt);
 			s1+=formatter.format(dt);
 			s+=" and turn off at ";
 			s1+=" and turns off at ";
 			dt = (Date) TimerOff.getValue();
 			dt.setTime(dt.getTime()+((Integer) ActinicOffset.getValue() * 60 * 1000)) ;
 			s+=formatter.format(dt);
 			s1+=formatter.format(dt);
 			
 			lact.setText(s);
 			lta.setText(s1);
 			s1=s1.replace("Turns on at ", "Start Time is ");
 			s1=s1.replace(" and turns off at ", " and End Time is");
 			((JLabel)actinicpwmsettingspanel.getComponent(3)).setText(s1);
 
 			s="Your Moonlights will turn on at ";
 			s1="Turns on at ";
 			dt = (Date) TimerOff.getValue();
 			s+=formatter.format(dt);
 			s1+=formatter.format(dt);
 			s+=" and turn off at ";
 			s1+=" and turns off at ";
 			dt = (Date) TimerOn.getValue();
 			s+=formatter.format(dt);
 			s1+=formatter.format(dt);
 			
 			lmoon.setText(s);
 			ltm.setText(s1);
 		}
 	}
 	
 	private void BuildSettings()
 	{
 
 		ActionListener sliceActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 				String swindow=Title.GetText();
 				if (swindow.indexOf("Main Relay Box - Port")==0 || swindow.indexOf("Expansion Relay Box - Port")==0)
 				{
 					int r=Integer.parseInt(swindow.replace("Main Relay Box - Port", "").replace("Expansion Relay Box - Port","").replace(" ", ""));
 					if (swindow.indexOf("Expansion Relay Box - Port")==0) r+=8;
 					functionsettings[r].setBorder(BorderFactory.createTitledBorder(null, aButton.getText(),TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 					CardLayout cl = (CardLayout)(functionsettings[r].getLayout());
 					cl.show(functionsettings[r], aButton.getText());
 					JRadioButton j = (JRadioButton) memsettings.getComponent(1);
 
 					if (aButton.getText()=="Time Schedule" && j.isSelected())
 					{
 						cl.show(functionsettings[r], "Light Schedule");
 					}
 					RevalidateSettings();
 				}
 			}
 		};
 
 		ChangeListener MemSettingsListener = new ChangeListener(){
 		    public void stateChanged(ChangeEvent evt) {
 		    	if (((JRadioButton) memsettings.getComponent(1)).isSelected())
 		    	{
 					String swindow=Title.GetText();
 					int r=Integer.parseInt(swindow.replace("Main Relay Box - Port", "").replace("Expansion Relay Box - Port","").replace(" ", ""));
 					if (swindow.indexOf("Expansion Relay Box - Port")==0) r+=8;
 					for (int a=1;a<=16;a++)
 					{
 						((JSpinner)Heater[a].getComponent(2)).setValue(((JSpinner)Heater[r].getComponent(2)).getValue());
 						((JSpinner)Heater[a].getComponent(4)).setValue(((JSpinner)Heater[r].getComponent(4)).getValue());
 						((JSpinner)Chiller[a].getComponent(2)).setValue(((JSpinner)Chiller[r].getComponent(2)).getValue());
 						((JSpinner)Chiller[a].getComponent(4)).setValue(((JSpinner)Chiller[r].getComponent(4)).getValue());
 						((JSpinner)ATO[a].getComponent(5)).setValue(((JSpinner)ATO[r].getComponent(5)).getValue());
 						((JSpinner)WM[a].getComponent(4)).setValue(((JSpinner)WM[r].getComponent(4)).getValue());
 						((JSpinner)CO2Control[a].getComponent(2)).setValue(((JSpinner)CO2Control[r].getComponent(2)).getValue());
 						((JSpinner)CO2Control[a].getComponent(4)).setValue(((JSpinner)CO2Control[r].getComponent(4)).getValue());
 						((JSpinner)pHControl[a].getComponent(2)).setValue(((JSpinner)pHControl[r].getComponent(2)).getValue());
 						((JSpinner)pHControl[a].getComponent(4)).setValue(((JSpinner)pHControl[r].getComponent(4)).getValue());
 						((JSpinner)Delayed[a].getComponent(2)).setValue(((JSpinner)Delayed[r].getComponent(2)).getValue());
 					}		
 		    	}
 			}			
 		};
 		ActionListener DisplayTempListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 				if (aButton.getText()=="Celsius")
 				{
 					tempunit=1;
 					JLabel j=(JLabel)OverheatSettings.getComponent(0);
 					j.setText("Overheat Temperature (\u00b0C): ");
 				}
 				if (aButton.getText()=="Fahrenheit")
 				{
 					tempunit=0;
 					JLabel j=(JLabel)OverheatSettings.getComponent(0);
 					j.setText("Overheat Temperature (\u00b0F): ");
 				}
 
 			}
 		};		  
 
 		// Temperature unit  
 		disptemp = new JPanel(new GridLayout(1,2));
 		ButtonGroup group = new ButtonGroup();
 		disptemp.setBorder(BorderFactory.createTitledBorder(null, "Temperature Unit",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 
 		DisplayTemp = new JRadioButton("Celsius");
 		DisplayTemp.addActionListener(DisplayTempListener);
 		group.add(DisplayTemp);
 		disptemp.add(DisplayTemp);
 		DisplayTemp = new JRadioButton("Fahrenheit");
 		DisplayTemp.addActionListener(DisplayTempListener);
 		DisplayTemp.setSelected(true);
 		group.add(DisplayTemp);        
 		disptemp.add(DisplayTemp);
 
 		FlowLayout OverheatSettingsFlowLayout=new FlowLayout();
 		OverheatSettings = new JPanel(OverheatSettingsFlowLayout);
 		JLabel OverheatLabel = new JLabel("Overheat Temperature (\u00b0F): ");
 		OverheatSettings.add(OverheatLabel);
 		OverheatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		Overheat = new JSpinner( new SpinnerNumberModel(86.9,10.0,150.0,0.1));
 		JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )Overheat.getEditor();
 		jo.getTextField().setColumns(5);
 		jo.getFormat().applyPattern("###0.0");
 		Overheat.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		OverheatSettings.add(Overheat);
 		Overheat.setAlignmentX(Component.LEFT_ALIGNMENT);
 		OverheatSettings.setAlignmentX(Component.LEFT_ALIGNMENT);
 		OverheatSettings.setBorder(BorderFactory.createTitledBorder(null, "Overheat Temperature",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 
 		//TODO: Fix code
 		ActionListener MemListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 				if (aButton.getText()=="In the code (Recommended for new users) ")
 				{
 					ShowHardCodeSettings(true);
 				}
 				if (aButton.getText()=="In the internal memory")
 				{
 					ShowHardCodeSettings(false);            	  
 				}
 			}
 		};		  
 
 		// Settings Storage  
 		memsettings = new JPanel(new GridLayout(1,2));
 		ButtonGroup group1 = new ButtonGroup();
 		memsettings.setBorder(BorderFactory.createTitledBorder(null, "Settings Storage",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));		
 		JRadioButton DisplayMem = new JRadioButton();
 		DisplayMem = new JRadioButton("In the code (Recommended for new users) ");
 		DisplayMem.setSelected(true);
 		DisplayMem.addActionListener(MemListener);
 		group1.add(DisplayMem);
 		memsettings.add(DisplayMem);
 		DisplayMem = new JRadioButton("In the internal memory");
 		DisplayMem.addActionListener(MemListener);
 		memsettings.add(DisplayMem);
 		group1.add(DisplayMem);
 
 
 		// functions
 		for (int a=1;a<=16;a++)
 		{
 			functions[a]=new JPanel();
 			functions[a].setLayout(new GridLayout(12,1));
 			ButtonGroup funcgroup = new ButtonGroup();
 			JRadioButton option=null;
 			for (int c=0;c<RegButtons.length;c++)
 			{
 				option = new JRadioButton(RegButtons[c]);
 				option.addActionListener(sliceActionListener);
 
 				if (c==4)
 				{
 					if (a==5 || a==6 || a==13 || a==14)
 					{
 						option.setEnabled(true);
 					}
 					else
 					{
 //						option.setText("Wavemaker (Port 5 and 6 Only)");
 						option.setEnabled(false);
 					}
 				}
 				funcgroup.add(option);
 				functions[a].add(option);
 			}
 			option.setSelected(true);
 			functions[a].setBorder(BorderFactory.createTitledBorder(null, "Functions",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 			functions[a].setAlignmentX(Component.LEFT_ALIGNMENT);
 
 			// Time Schedule
 			Timed[a] = new JPanel();
 			
 			SpringLayout Timedlayout=new SpringLayout();
 			Timed[a].setLayout(Timedlayout);
 
 			JCustomEditorPane i = new JCustomEditorPane("<HTML>" + DescButtons[0] + "</HTML>");
 			Timed[a].add(i,"i");
 
 			Calendar calendar = new GregorianCalendar();
 			calendar.set(Calendar.HOUR_OF_DAY, 9);
 			calendar.set(Calendar.MINUTE, 0);
 			JLabel TimerOnLabel=new JLabel ("Turn on at: ",JLabel.TRAILING);
 			Timed[a].add(TimerOnLabel,"TimerOnLabel");
 
 			JSpinner TimerOn = new JSpinner( new SpinnerDateModel() ); 
 			JSpinner.DateEditor TimerOnEditor = new JSpinner.DateEditor(TimerOn, "hh:mm a"); 
 			TimerOn.setEditor(TimerOnEditor); 
 			TimerOn.setValue(calendar.getTime()); // will only show the current time
 			Timed[a].add(TimerOn,"TimerOn");
 
 			calendar.set(Calendar.HOUR_OF_DAY, 19);
 			JLabel TimerOffLabel=new JLabel ("Turn off at: ",JLabel.TRAILING);
 			Timed[a].add(TimerOffLabel,"TimerOffLabel");
 
 			JSpinner TimerOff = new JSpinner( new SpinnerDateModel() ); 
 			JSpinner.DateEditor TimerOffEditor = new JSpinner.DateEditor(TimerOff, "hh:mm a"); 
 			TimerOff.setEditor(TimerOffEditor); 
 			TimerOff.setValue(calendar.getTime()); // will only show the current time
 			Timed[a].add(TimerOff,"TimerOff");	
 
 			JLabel DelayedTimerLabel=null;
 			JSpinner DelayedTimer;
 			DelayedTimer = new JSpinner( new SpinnerNumberModel(0,0,30,1) );
 			DelayedTimerLabel=new JLabel ("Delayed Start (m): ",JLabel.TRAILING);
 			Timed[a].add(DelayedTimerLabel,"DelayedTimerLabel");
 			Timed[a].add(DelayedTimer,"DelayedTimer");	
 
 			ApplyLayout(Timed[a],Timedlayout);
 
 			// Timed Memory Settings
 			
 			ActionListener ButtonActionListener = new ActionListener() {
 				public void actionPerformed(ActionEvent actionEvent)
 				{
 					AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 					String swindow=Title.GetText();
 					int r=Integer.parseInt(swindow.replace("Main Relay Box - Port", "").replace("Expansion Relay Box - Port","").replace(" ", ""));
 					if (swindow.indexOf("Expansion Relay Box - Port")==0) r+=8;
 					CardLayout cl = (CardLayout)(functionsettings[r].getLayout());
 					if (aButton.getText()=="Change Time Schedule Settings")
 						cl.show(functionsettings[r], "Light Schedule Settings");
 					else
 						cl.show(functionsettings[r], "Light Schedule");
 						
 					RevalidateSettings();
 				}
 			};
 			
 			TimedMemorySettings[a] = new JPanel();
 			SpringLayout TimedSettingslayout=new SpringLayout();
 			TimedMemorySettings[a].setLayout(TimedSettingslayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[12] + "</HTML>");
 			TimedMemorySettings[a].add(i,"i");
 
 			calendar = new GregorianCalendar();
 			calendar.set(Calendar.HOUR_OF_DAY, 9);
 			calendar.set(Calendar.MINUTE, 0);
 			TimerOnLabel=new JLabel ("Turn on Daylights at: ",JLabel.TRAILING);
 			TimedMemorySettings[a].add(TimerOnLabel,"TimerOnLabel");
 
 			TimerOn = new JSpinner( new SpinnerDateModel() ); 
 			TimerOnEditor = new JSpinner.DateEditor(TimerOn, "hh:mm a"); 
 			TimerOn.setEditor(TimerOnEditor); 
 			TimerOn.setValue(calendar.getTime()); // will only show the current time
 			TimedMemorySettings[a].add(TimerOn,"TimerOn");
 			TimerOn.addChangeListener(new ChangeListener(){
 			    public void stateChanged(ChangeEvent evt) {
 			    	UpdateTimeLabels();
 			    }			
 			});
 
 			calendar.set(Calendar.HOUR_OF_DAY, 19);
 			TimerOffLabel=new JLabel ("Turn off Daylights at: ",JLabel.TRAILING);
 			TimedMemorySettings[a].add(TimerOffLabel,"TimerOffLabel");
 
 			TimerOff = new JSpinner( new SpinnerDateModel() ); 
 			TimerOffEditor = new JSpinner.DateEditor(TimerOff, "hh:mm a"); 
 			TimerOff.setEditor(TimerOffEditor); 
 			TimerOff.setValue(calendar.getTime()); // will only show the current time
 			TimedMemorySettings[a].add(TimerOff,"TimerOff");	
 			TimerOff.addChangeListener(new ChangeListener(){
 			    public void stateChanged(ChangeEvent evt) {
 			    	UpdateTimeLabels();
 			    }			
 			});
 			
 			DelayedTimerLabel=null;
 			DelayedTimer = new JSpinner( new SpinnerNumberModel(0,0,255,1) );
 			DelayedTimerLabel=new JLabel ("Delayed Start (m): ",JLabel.TRAILING);
 			TimedMemorySettings[a].add(DelayedTimerLabel,"DelayedTimerLabel");
 			TimedMemorySettings[a].add(DelayedTimer,"DelayedTimer");	
 			DelayedTimer.addChangeListener(new ChangeListener(){
 			    public void stateChanged(ChangeEvent evt) {
 			    	UpdateTimeLabels();
 			    }			
 			});
 			
 			JLabel ActinicLabel=null;
 			JSpinner ActinicOffset = new JSpinner( new SpinnerNumberModel(30,0,255,1) );
 			ActinicLabel=new JLabel ("Offset for Actinics (m): ",JLabel.TRAILING);
 			TimedMemorySettings[a].add(ActinicLabel,"ActinicLabel");
 			TimedMemorySettings[a].add(ActinicOffset,"ActinicOffset");	
 			ActinicOffset.addChangeListener(new ChangeListener(){
 			    public void stateChanged(ChangeEvent evt) {
 			    	UpdateTimeLabels();
 			    }			
 			});
 			
 			String s="Your Actinics will turn on at ";
 	    	Date dt=null; 
 			dt = (Date) TimerOn.getValue();
 			dt.setTime(dt.getTime()-((Integer) ActinicOffset.getValue() * 60 * 1000)) ;
 			DateFormat formatter = new SimpleDateFormat("h:mm a");
 			s+=formatter.format(dt);
 			s+=" and turn off at ";
 			dt = (Date) TimerOff.getValue();
 			dt.setTime(dt.getTime()+((Integer) ActinicOffset.getValue() * 60 * 1000)) ;
 			s+=formatter.format(dt);
 			
 			JLabel ActinicText=null;
 			ActinicText=new JLabel (s ,JLabel.TRAILING);
 			TimedMemorySettings[a].add(ActinicText,"ActinicText");
 
 			String s1="Your Moonlights will turn on at ";
 			dt = (Date) TimerOff.getValue();
 			s1+=formatter.format(dt);
 			s1+=" and turn off at ";
 			dt = (Date) TimerOn.getValue();
 			s1+=formatter.format(dt);
 			
 			JLabel MoonlightText=null;
 			MoonlightText=new JLabel (s1 ,JLabel.TRAILING);
 			TimedMemorySettings[a].add(MoonlightText,"MoonlightText");
 
 			JButton b = new JButton("Change Time Schecule Function");
 			b.addActionListener(ButtonActionListener);
 			TimedMemorySettings[a].add(b);
 			
 			ApplyTimedMemorySettingsLayout(TimedMemorySettings[a],TimedSettingslayout);
 
 			// TimedMemory
 
 			TimedMemory[a] = new JPanel();
 			SpringLayout TimedMemorylayout=new SpringLayout();
 			TimedMemory[a].setLayout(TimedMemorylayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[0] + "</HTML>");
 			TimedMemory[a].add(i,"i");
 
 			ButtonGroup TMgroup = new ButtonGroup();
 			JRadioButton TMOption;
 			TMOption = new JRadioButton("Daylight");		
 			TMgroup.add(TMOption);
 			TMOption.setSelected(true);
 			TimedMemory[a].add(TMOption);
 			
 			JSpinner TimerOn1 = (JSpinner)TimedMemorySettings[a].getComponent(2);
 			JSpinner TimerOff1 = (JSpinner)TimedMemorySettings[a].getComponent(4);
 			JSpinner ActinicOffset1 = (JSpinner)TimedMemorySettings[a].getComponent(8);
 
 //			DateFormat formatter = new SimpleDateFormat("h:mm a");
 
 			s="Turns on at ";
 //	    	Date dt=null; 
 			dt = (Date) TimerOn1.getValue();
 			s+=formatter.format(dt);
 			s+=" and turns off at ";
 			dt = (Date) TimerOff1.getValue();
 			s+=formatter.format(dt);
 			
 			TimedMemory[a].add(new JLabel(s));
 			
 			TMOption = new JRadioButton("Actinic (Turn on/off x minutes before and after Daylights)");		
 			TMgroup.add(TMOption);
 			TimedMemory[a].add(TMOption);
 
 			s="Turns on at ";
 			dt = (Date) TimerOn1.getValue();
 			dt.setTime(dt.getTime()-((Integer) ActinicOffset1.getValue() * 60 * 1000)) ;
 			s+=formatter.format(dt);
 			s+=" and turn off at ";
 			dt = (Date) TimerOff1.getValue();
 			dt.setTime(dt.getTime()+((Integer) ActinicOffset1.getValue() * 60 * 1000)) ;
 			s+=formatter.format(dt);
 			
 			TimedMemory[a].add(new JLabel(s));
 			
 			TMOption = new JRadioButton("Moonlights/Refugium (Opposite cycle of Daylights)");		
 			TMgroup.add(TMOption);
 			TimedMemory[a].add(TMOption);
 			s="Turns on at ";
 			dt = (Date) TimerOff1.getValue();
 			s+=formatter.format(dt);
 			s+=" and turn off at ";
 			dt = (Date) TimerOn1.getValue();
 			s+=formatter.format(dt);
 			
 			TimedMemory[a].add(new JLabel(s));
 
 			b = new JButton("Change Time Schedule Settings");
 			b.addActionListener(ButtonActionListener);
 			
 			TimedMemory[a].add(b);
 			TimedMemory[a].add(new JLabel(""));
 
 			ApplyTimedMemoryLayout(TimedMemory[a],TimedMemorylayout);
 
 			// Heater
 			Heater[a] = new JPanel();
 			SpringLayout Heaterlayout=new SpringLayout();
 			Heater[a].setLayout(Heaterlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[1] + "</HTML>");
 			Heater[a].add(i,"i");
 
 			JLabel HeaterOnLabel=null;
 			JSpinner HeaterOn;
 			HeaterOn = new JSpinner( new SpinnerNumberModel(75.1,10.0,150.0,0.1) );
 			HeaterOn.addChangeListener(MemSettingsListener);
 			HeaterOnLabel=new JLabel ("Turn on at (\u00b0F): ",JLabel.TRAILING);
 			Heater[a].add(HeaterOnLabel);
 			Heater[a].add(HeaterOn);
 			Heaterlayout.getConstraints(HeaterOn).setWidth(Spring.constant(70));
 			JSpinner.NumberEditor  jh = (JSpinner.NumberEditor )HeaterOn.getEditor();
 			jh.getTextField().setColumns(5);
 			jh.getFormat().applyPattern("###0.0");
 
 			JLabel HeaterOffLabel=null;
 			JSpinner HeaterOff;
 			HeaterOff = new JSpinner( new SpinnerNumberModel(76.1,10.0,150.0,0.1) );
 			HeaterOff.addChangeListener(MemSettingsListener);
 			HeaterOffLabel=new JLabel ("Turn off at (\u00b0F): ",JLabel.TRAILING);
 			Heater[a].add(HeaterOffLabel);
 			Heater[a].add(HeaterOff);	
 			Heaterlayout.getConstraints(HeaterOff).setWidth(Spring.constant(70));
 			jh = (JSpinner.NumberEditor )HeaterOff.getEditor();
 			jh.getTextField().setColumns(5);
 			jh.getFormat().applyPattern("###0.0");			
 			ApplyLayout(Heater[a],Heaterlayout);
 
 			// Chiller
 			Chiller[a] = new JPanel();
 			SpringLayout Chillerlayout=new SpringLayout();
 			Chiller[a].setLayout(Chillerlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[2] + "</HTML>");
 			Chiller[a].add(i,"i");
 
 			JSpinner ChillerOn;
 			JLabel ChillerOnLabel=null;
 			ChillerOnLabel=new JLabel ("Turn on at (\u00b0F): ",JLabel.TRAILING);
 			ChillerOn = new JSpinner( new SpinnerNumberModel(78.1,10.0,150.0,0.1) );
 			ChillerOn.addChangeListener(MemSettingsListener);
 			Chiller[a].add(ChillerOnLabel);
 			Chiller[a].add(ChillerOn);
 			Chillerlayout.getConstraints(ChillerOn).setWidth(Spring.constant(70));
 			JSpinner.NumberEditor  jc = (JSpinner.NumberEditor )ChillerOn.getEditor();
 			jc.getTextField().setColumns(5);
 			jc.getFormat().applyPattern("###0.0");
 
 			JLabel ChillerOffLabel=null;
 			JSpinner ChillerOff;
 			ChillerOffLabel=new JLabel ("Turn off at (\u00b0F): ",JLabel.TRAILING);
 			ChillerOff = new JSpinner( new SpinnerNumberModel(77.1,10.0,150.0,0.1) );
 			ChillerOff.addChangeListener(MemSettingsListener);
 			Chiller[a].add(ChillerOffLabel);
 			Chiller[a].add(ChillerOff);		
 			Chillerlayout.getConstraints(ChillerOff).setWidth(Spring.constant(70));
 			jc = (JSpinner.NumberEditor )ChillerOff.getEditor();
 			jc.getTextField().setColumns(5);
 			jc.getFormat().applyPattern("###0.0");
 
 			ApplyLayout(Chiller[a],Chillerlayout);
 
 			// Auto Top Off
 
 			ATO[a] = new JPanel();
 			SpringLayout ATOlayout=new SpringLayout();
 			ATO[a].setLayout(ATOlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[3] + "</HTML>");
 			ATO[a].add(i,"i");
 
 			ButtonGroup ATOgroup = new ButtonGroup();
 			JRadioButton ATOOption;
 			ATOOption = new JRadioButton("Standard ATO");		
 			ATOgroup.add(ATOOption);
 			ATOOption.setSelected(true);
 			ATO[a].add(ATOOption);
 			ATOOption = new JRadioButton("Single ATO (Low Port)");		
 			ATOgroup.add(ATOOption);
 			ATO[a].add(ATOOption);
 			ATOOption = new JRadioButton("Single ATO (High Port)");		
 			ATOgroup.add(ATOOption);
 			ATO[a].add(ATOOption);
 
 			JLabel ATOTimeoutLabel=new JLabel ("Timeout (s): ",JLabel.TRAILING);
 			JSpinner ATOTimeout=new JSpinner( new SpinnerNumberModel(60,0,32000,1) );
 			ATOTimeout.addChangeListener(MemSettingsListener);
 			ATO[a].add(ATOTimeoutLabel);
 			ATO[a].add(ATOTimeout);
 			i.addHyperlinkListener(new HyperlinkListener() {   
 				public void hyperlinkUpdate(HyperlinkEvent hle)
 				{   
 					if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
 					{   
 						if (isBrowsingSupported()) { 
 							try { 
 								Desktop desktop = java.awt.Desktop.getDesktop(); 
 								URI uri = new java.net.URI(hle.getURL().toString()); 
 								desktop.browse(uri); 
 							} catch (URISyntaxException use) { 
 								throw new AssertionError(use); 
 							} catch (IOException ioe) { 
 								ioe.printStackTrace(); 
 								JOptionPane.showMessageDialog(null, "Sorry, a problem occurred while trying to open this link in your system's standard browser.","A problem occured", JOptionPane.ERROR_MESSAGE); 
 							} 					               
 						} 
 					}   
 				}
 			});   
 
 			ApplyATOLayout(ATO[a],ATOlayout);
 
 			// Wavemaker
 
 			ActionListener WMActionListener = new ActionListener() {
 				public void actionPerformed(ActionEvent actionEvent) {
 					AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 					String swindow=Title.GetText();
 					if (swindow.indexOf("Main Relay Box - Port")==0)
 					{
 						int r=Integer.parseInt(swindow.replace("Main Relay Box - Port", "").replace("Expansion Relay Box - Port","").replace(" ", ""));
 						if (swindow.indexOf("Expansion Relay Box - Port")==0) r+=8;
 						JLabel j = (JLabel)WM[r].getComponent(3);
 						if (aButton.getText()=="Constant")
 						{
 							j.setText("Cycle every (s): ");
 							WM[r].getComponent(5).setVisible(false);
 							WM[r].getComponent(6).setVisible(false);
 						}
 						else
 						{
 							j.setText("Cycle between (s): ");
 							WM[r].getComponent(5).setVisible(true);
 							WM[r].getComponent(6).setVisible(true);
 						}
 
 						//	              CardLayout cl = (CardLayout)(functionsettings[relay].getLayout());
 						//	              cl.show(functionsettings[relay], aButton.getText());
 
 						RevalidateSettings();
 					}
 				}
 			};
 
 			WM[a] = new JPanel();
 			SpringLayout WMlayout=new SpringLayout();
 			WM[a].setLayout(WMlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[4] + "</HTML>");
 			WM[a].add(i,"i");
 
 			ButtonGroup WMgroup = new ButtonGroup();
 			JRadioButton WMOption;
 			WMOption = new JRadioButton("Constant");		
 			WMgroup.add(WMOption);
 			WMOption.setSelected(true);
 			WMOption.addActionListener(WMActionListener);
 			WM[a].add(WMOption);
 			WMOption = new JRadioButton("Random");		
 			WMgroup.add(WMOption);
 			WMOption.addActionListener(WMActionListener);
 			WM[a].add(WMOption);
 
 			JLabel WMLabel=new JLabel ("Cycle every (s): ",JLabel.TRAILING);
 			WM[a].add(WMLabel);
 			JSpinner WMOn;
 			WMOn = new JSpinner( new SpinnerNumberModel(60,0,32000,1) );
 			WMOn.addChangeListener(MemSettingsListener);
 			WM[a].add(WMOn);
 
 			JLabel WMLabel1=new JLabel ("and ",JLabel.TRAILING);
 			WM[a].add(WMLabel1);
 			WMLabel1.setVisible(false);
 			JSpinner WMOn1;
 			WMOn1 = new JSpinner( new SpinnerNumberModel(100,0,32000,1) );
 			WMOn1.setVisible(false);
 			WM[a].add(WMOn1);
 
 			ApplyWMLayout(WM[a],WMlayout);
 
 			// CO2 Control
 
 			CO2Control[a]= new JPanel();
 			SpringLayout CO2Controllayout=new SpringLayout();
 			CO2Control[a].setLayout(CO2Controllayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[5] + "</HTML>");
 			CO2Control[a].add(i,"i");
 
 			JLabel CO2ControlOnLabel=null;
 			JSpinner CO2ControlOn;
 			CO2ControlOn = new JSpinner( new SpinnerNumberModel(7.52,1.00,14.00,0.01) );
 			CO2ControlOnLabel=new JLabel ("Turn on at pH: ",JLabel.TRAILING);
 			CO2Control[a].add(CO2ControlOnLabel);
 			CO2Control[a].add(CO2ControlOn);
 			CO2ControlOn.addChangeListener(MemSettingsListener);
 			CO2Controllayout.getConstraints(CO2ControlOn).setWidth(Spring.constant(70));
 			JSpinner.NumberEditor jco = (JSpinner.NumberEditor )CO2ControlOn.getEditor();
 			jco.getTextField().setColumns(5);
 			jco.getFormat().applyPattern("###0.00");
 
 			JLabel CO2ControlOffLabel=null;
 			JSpinner CO2ControlOff;
 			CO2ControlOff = new JSpinner( new SpinnerNumberModel(7.48,1.00,14.00,0.01) );
 			CO2ControlOffLabel=new JLabel ("Turn off at pH: ",JLabel.TRAILING);
 			CO2Control[a].add(CO2ControlOffLabel);
 			CO2Control[a].add(CO2ControlOff);	
 			CO2ControlOff.addChangeListener(MemSettingsListener);
 			CO2Controllayout.getConstraints(CO2ControlOff).setWidth(Spring.constant(70));
 			jco = (JSpinner.NumberEditor )CO2ControlOff.getEditor();
 			jco.getTextField().setColumns(5);
 			jco.getFormat().applyPattern("###0.00");			
 			ApplyLayout(CO2Control[a],CO2Controllayout);	        
 
 			// pH Control
 
 			pHControl[a]= new JPanel();
 			SpringLayout pHControllayout=new SpringLayout();
 			pHControl[a].setLayout(pHControllayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[6] + "</HTML>");
 			pHControl[a].add(i,"i");
 
 			JLabel pHControlOnLabel=null;
 			JSpinner pHControlOn;
 			pHControlOn = new JSpinner( new SpinnerNumberModel(8.18,1.00,14.00,0.01) );
 			pHControlOnLabel=new JLabel ("Turn on at pH: ",JLabel.TRAILING);
 			pHControl[a].add(pHControlOnLabel);
 			pHControl[a].add(pHControlOn);
 			pHControlOn.addChangeListener(MemSettingsListener);
 			pHControllayout.getConstraints(pHControlOn).setWidth(Spring.constant(70));
 			JSpinner.NumberEditor jcp = (JSpinner.NumberEditor )pHControlOn.getEditor();
 			jcp.getTextField().setColumns(5);
 			jcp.getFormat().applyPattern("###0.00");
 
 			JLabel pHControlOffLabel=null;
 			JSpinner pHControlOff;
 			pHControlOff = new JSpinner( new SpinnerNumberModel(8.22,1.00,14.00,0.01) );
 			pHControlOffLabel=new JLabel ("Turn off at pH: ",JLabel.TRAILING);
 			pHControl[a].add(pHControlOffLabel);
 			pHControl[a].add(pHControlOff);	
 			pHControlOff.addChangeListener(MemSettingsListener);
 			pHControllayout.getConstraints(pHControlOff).setWidth(Spring.constant(70));
 			jcp = (JSpinner.NumberEditor )pHControlOff.getEditor();
 			jcp.getTextField().setColumns(5);
 			jcp.getFormat().applyPattern("###0.00");
 
 			ApplyLayout(pHControl[a],pHControllayout);
 
 			// Dosing
 			Dosing[a] = new JPanel();
 			SpringLayout Dosinglayout=new SpringLayout();
 			Dosing[a].setLayout(Dosinglayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[7] + "</HTML>");
 			Dosing[a].add(i,"i");
 
 			JLabel DosingOnLabel=new JLabel ("Turn on every (m): ",JLabel.TRAILING);
 			Dosing[a].add(DosingOnLabel);
 			JSpinner DosingOn;
 			DosingOn = new JSpinner( new SpinnerNumberModel(60,0,1440,1) );
 			Dosing[a].add(DosingOn);
 			DosingOn.addChangeListener(MemSettingsListener);
 
 			JLabel DosingOffLabel=new JLabel ("for (s): ",JLabel.TRAILING);
 			Dosing[a].add(DosingOffLabel);
 			JSpinner DosingOff;
 			DosingOff = new JSpinner( new SpinnerNumberModel(10,0,255,1) ); 
 			Dosing[a].add(DosingOff);
 			DosingOff.addChangeListener(MemSettingsListener);
 
 			JLabel DosingOffsetLabel=new JLabel ("Offset (m): ",JLabel.TRAILING);
 			Dosing[a].add(DosingOffsetLabel);
 			JSpinner DosingOffset;
 			DosingOffset = new JSpinner( new SpinnerNumberModel(0,0,1440,1) ); 
 			Dosing[a].add(DosingOffset);
 			DosingOffset.addChangeListener(MemSettingsListener);
 
 			ApplyDosingLayout(Dosing[a],Dosinglayout);	   
 
 			// Delayed On
 
 			Delayed[a] = new JPanel();
 			SpringLayout Delayedlayout=new SpringLayout();
 			Delayed[a].setLayout(Delayedlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[8] + "</HTML>");
 			Delayed[a].add(i,"i");
 
 			JLabel DelayedOnLabel=new JLabel ("Delayed Start (m): ",JLabel.TRAILING);
 			Delayed[a].add(DelayedOnLabel);
 			JSpinner DelayedOn;
 			DelayedOn = new JSpinner( new SpinnerNumberModel(10,0,255,1) );
 			Delayed[a].add(DelayedOn);
 			DelayedOn.addChangeListener(MemSettingsListener);
 
 			ApplyLayout(Delayed[a],Delayedlayout);
 
 			// Opposite
 
 			Opposite[a] = new JPanel();
 			SpringLayout Oppositelayout=new SpringLayout();
 			Opposite[a].setLayout(Oppositelayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[9] + "</HTML>");
 			Opposite[a].add(i,"i");
 			JLabel OppositeLabelOn=new JLabel ("Opposite of Port: ",JLabel.TRAILING);
 			Opposite[a].add(OppositeLabelOn);
 			JComboBox OppositeOn;
 			OppositeOn = new JComboBox();
 			for (int j=1;j<=8;j++)
 				OppositeOn.addItem("Main Box Port "+j);
 			for (int j=1;j<=8;j++)
 				OppositeOn.addItem("Expansion Box Port "+j);
 			OppositeOn.setSelectedIndex(0);
 			Opposite[a].add(OppositeOn);
 
 			ApplyLayout(Opposite[a],Oppositelayout);	        
 
 			// Always On
 
 			AlwaysOn[a] = new JPanel();
 			SpringLayout AlwaysOnlayout=new SpringLayout();
 			AlwaysOn[a].setLayout(AlwaysOnlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[10] + "</HTML>");
 			AlwaysOn[a].add(i,"i");
 
 			AlwaysOn[a].add(new JLabel(""));
 			AlwaysOn[a].add(new JLabel(""));
 
 			ApplyLayout(AlwaysOn[a],AlwaysOnlayout);	
 
 			// Not Used
 
 			NotUsed[a] = new JPanel();
 			SpringLayout NotUsedlayout=new SpringLayout();
 			NotUsed[a].setLayout(NotUsedlayout);
 
 			i = new JCustomEditorPane("<HTML>" + DescButtons[11] + "</HTML>");
 			NotUsed[a].add(i,"i");
 
 			NotUsed[a].add(new JLabel(""));
 			NotUsed[a].add(new JLabel(""));
 
 			ApplyLayout(NotUsed[a],NotUsedlayout);	
 
 			CardLayout cl = new CardLayout();
 			functionsettings[a]=new JPanel(cl);
 			functionsettings[a].setBorder(BorderFactory.createTitledBorder(null, RegButtons[11],TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 			functionsettings[a].add(new JPanel(),RegButtons[11]);
 			functionsettings[a].add(Timed[a],RegButtons[0]);
 			functionsettings[a].add(Heater[a],RegButtons[1]);
 			functionsettings[a].add(Chiller[a],RegButtons[2]);
 			functionsettings[a].add(ATO[a],RegButtons[3]);
 			functionsettings[a].add(WM[a],RegButtons[4]);
 			functionsettings[a].add(CO2Control[a],RegButtons[5]);
 			functionsettings[a].add(pHControl[a],RegButtons[6]);
 			functionsettings[a].add(Dosing[a],RegButtons[7]);
 			functionsettings[a].add(Delayed[a],RegButtons[8]);
 			functionsettings[a].add(Opposite[a],RegButtons[9]);
 			functionsettings[a].add(AlwaysOn[a],RegButtons[10]);
 			functionsettings[a].add(NotUsed[a],RegButtons[11]);
 			functionsettings[a].add(TimedMemory[a] ,"Light Schedule");
 			functionsettings[a].add(TimedMemorySettings[a] ,"Light Schedule Settings");
 
 			cl.show(functionsettings[a], "Not Used");
 
 			feeding[a] = new JCheckBox ("Turn off this port on \"Feeding\" Mode");
 			waterchange[a] = new JCheckBox ("Turn off this port on \"Water Change\" Mode");
 			lightson[a] = new JCheckBox ("Turn on this port on \"Lights On\" mode");
 			overheat[a] = new JCheckBox ("Turn off this port on \"Overheat\"");
 			ports[a] = new JPanel(); 
 			ports[a].setLayout(new BoxLayout( ports[a], BoxLayout.PAGE_AXIS));
 			ports[a].setBorder(BorderFactory.createTitledBorder(null, "Port Mode",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 			ports[a].add(feeding[a]);
 //			if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 			ports[a].add(waterchange[a]);
 //			if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 			ports[a].add(lightson[a]);
 //			if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 			ports[a].add(overheat[a]);
 //			if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 		}
 		expansionmods=new JPanel();
 		expansionmods.setLayout(new BoxLayout( expansionmods, BoxLayout.PAGE_AXIS));
 		JCheckBox jExp = null;
 		for (int j=0;j<ExpModules.length;j++)
 		{
 			jExp=new JCheckBox(ExpModules[j]);
 			expansionmods.add(jExp);
 		}
 
 		attachmentmods=new JPanel();
 		attachmentmods.setLayout(new BoxLayout( attachmentmods, BoxLayout.PAGE_AXIS));
 		JCheckBox jAttach = null;
 		for (int j=0;j<AttachModules.length;j++)
 		{
 			jAttach=new JCheckBox(AttachModules[j]);
 			attachmentmods.add(jAttach);
 		}
 
 
 		daylightpwm=new JPanel();
 		daylightpwm.setLayout(new GridLayout(2,5));
 		daylightpwm.setOpaque(false);
 		JRadioButton jDay = null;
 		ButtonGroup jgd = new ButtonGroup();
 		jDay=new JRadioButton("Slope");
 		jgd.add(jDay);
 		daylightpwm.add(jDay);
 		jDay=new JRadioButton("Parabola");
 		jgd.add(jDay);
 		daylightpwm.add(jDay);
 		jDay=new JRadioButton("Moon Phase");
 		jgd.add(jDay);
 		daylightpwm.add(jDay);
 		jDay=new JRadioButton("Buzzer");
 		jgd.add(jDay);
 		daylightpwm.add(jDay);
 		jDay=new JRadioButton("None");
 		jDay.setSelected(true);
 		jgd.add(jDay);
 		daylightpwm.add(jDay);
 		ImageIcon iconnection=null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 		JLabel ic = new JLabel(iconnection, JLabel.LEFT);
 		daylightpwm.add(ic);
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 		ic = new JLabel(iconnection, JLabel.LEFT);
 		daylightpwm.add(ic);
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 		ic = new JLabel(iconnection, JLabel.CENTER);
 		daylightpwm.add(ic);
 		daylightpwm.add(new JLabel(new ImageIcon()));
 		daylightpwm.add(new JLabel(new ImageIcon()));		
 
 		actinicpwm=new JPanel();
 		actinicpwm.setLayout(new GridLayout(2,5));
 		actinicpwm.setOpaque(false);
 		JRadioButton jAct = null;
 		ButtonGroup jga = new ButtonGroup();
 		jAct=new JRadioButton("Slope");
 		jga.add(jAct);
 		actinicpwm.add(jAct);
 		jAct=new JRadioButton("Parabola");
 		jga.add(jAct);
 		actinicpwm.add(jAct);
 		jAct=new JRadioButton("Moon Phase");
 		jga.add(jAct);
 		actinicpwm.add(jAct);
 		jAct=new JRadioButton("Buzzer");
 		jga.add(jAct);
 		actinicpwm.add(jAct);
 		jAct=new JRadioButton("None");
 		jAct.setSelected(true);
 		jga.add(jAct);
 		actinicpwm.add(jAct);
 		iconnection=null;
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 		ic = new JLabel(iconnection, JLabel.LEFT);
 		actinicpwm.add(ic);
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 		ic = new JLabel(iconnection, JLabel.LEFT);
 		actinicpwm.add(ic);
 		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 		ic = new JLabel(iconnection, JLabel.CENTER);
 		actinicpwm.add(ic);
 		actinicpwm.add(new JLabel(new ImageIcon()));
 		actinicpwm.add(new JLabel(new ImageIcon()));	
 		
 		for (int i=0;i<6;i++)
 		{
 			exppwm[i]=new JPanel();
 			exppwm[i].setLayout(new GridLayout(2,5));
 			exppwm[i].setOpaque(false);
 			jAct = null;
 			jga = new ButtonGroup();
 			jAct=new JRadioButton("Slope");
 			jga.add(jAct);
 			exppwm[i].add(jAct);
 			jAct=new JRadioButton("Parabola");
 			jga.add(jAct);
 			exppwm[i].add(jAct);
 			jAct=new JRadioButton("Moon Phase");
 			jga.add(jAct);
 			exppwm[i].add(jAct);
 			jAct=new JRadioButton("Buzzer");
 			jga.add(jAct);
 			exppwm[i].add(jAct);
 			jAct=new JRadioButton("None");
 			jAct.setSelected(true);
 			jga.add(jAct);
 			exppwm[i].add(jAct);	 
 			iconnection=null;
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 			ic = new JLabel(iconnection, JLabel.LEFT);
 			exppwm[i].add(ic);
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 			ic = new JLabel(iconnection, JLabel.LEFT);
 			exppwm[i].add(ic);
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 			ic = new JLabel(iconnection, JLabel.CENTER);
 			exppwm[i].add(ic);
 			exppwm[i].add(new JLabel(new ImageIcon()));
 			exppwm[i].add(new JLabel(new ImageIcon()));				
 		}
 
 		for (int i=0;i<AIChannels.length;i++)
 		{
 			aipwm[i]=new JPanel();
 			aipwm[i].setLayout(new GridLayout(2,4));
 			aipwm[i].setOpaque(false);
 			jAct = null;
 			jga = new ButtonGroup();
 			jAct=new JRadioButton("Slope");
 			jga.add(jAct);
 			aipwm[i].add(jAct);
 			jAct=new JRadioButton("Parabola");
 			jga.add(jAct);
 			aipwm[i].add(jAct);
 			jAct=new JRadioButton("Moon Phase");
 			jga.add(jAct);
 			aipwm[i].add(jAct);
 			jAct=new JRadioButton("None");
 			jAct.setSelected(true);
 			jga.add(jAct);
 			aipwm[i].add(jAct);	    		
 			iconnection=null;
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 			ic = new JLabel(iconnection, JLabel.LEFT);
 			aipwm[i].add(ic);
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 			ic = new JLabel(iconnection, JLabel.LEFT);
 			aipwm[i].add(ic);
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 			ic = new JLabel(iconnection, JLabel.CENTER);
 			aipwm[i].add(ic);
 			aipwm[i].add(new JLabel(new ImageIcon()));
 		}
 
 		for (int i=0;i<RadionChannels.length;i++)
 		{
 			rfpwm[i]=new JPanel();
 			rfpwm[i].setLayout(new GridLayout(2,4));
 			rfpwm[i].setOpaque(false);
 			jAct = null;
 			jga = new ButtonGroup();
 			jAct=new JRadioButton("Slope");
 			jga.add(jAct);
 			rfpwm[i].add(jAct);
 			jAct=new JRadioButton("Parabola");
 			jga.add(jAct);
 			rfpwm[i].add(jAct);
 			jAct=new JRadioButton("Moon Phase");
 			jga.add(jAct);
 			rfpwm[i].add(jAct);
 			jAct=new JRadioButton("None");
 			jAct.setSelected(true);
 			jga.add(jAct);
 			rfpwm[i].add(jAct);	    		
 			iconnection=null;
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 			ic = new JLabel(iconnection, JLabel.LEFT);
 			rfpwm[i].add(ic);
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 			ic = new JLabel(iconnection, JLabel.LEFT);
 			rfpwm[i].add(ic);
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 			ic = new JLabel(iconnection, JLabel.CENTER);
 			rfpwm[i].add(ic);
 			rfpwm[i].add(new JLabel(new ImageIcon()));
 		}
 
 		// daylight 
 		daylightpwmsettings=new JPanel();
 		daylightpwmsettings.setLayout(new GridLayout(3,6));
 		daylightpwmsettings.setOpaque(false);
 
 		daylightpwmsettings.add(new JLabel(""));
 
 		Calendar calendar = new GregorianCalendar();
 		calendar.set(Calendar.HOUR_OF_DAY, 9);
 		calendar.set(Calendar.MINUTE, 0);
 		JLabel PWMOnLabel=new JLabel ("Start Time: ",JLabel.TRAILING);
 		daylightpwmsettings.add(PWMOnLabel);
 		JSpinner PWMOn = new JSpinner( new SpinnerDateModel() ); 
 		JSpinner.DateEditor PWMOnEditor = new JSpinner.DateEditor(PWMOn, "HH:mm"); 
 		PWMOn.setEditor(PWMOnEditor); 
 		PWMOn.setValue(calendar.getTime()); // will only show the current time
 		daylightpwmsettings.add(PWMOn);
 
 		calendar.set(Calendar.HOUR_OF_DAY, 20);
 		calendar.set(Calendar.MINUTE, 0);
 		JLabel PWMOffLabel=new JLabel ("End Time: ",JLabel.TRAILING);
 		daylightpwmsettings.add(PWMOffLabel);
 		JSpinner PWMOff = new JSpinner( new SpinnerDateModel() ); 
 		JSpinner.DateEditor PWMOffEditor = new JSpinner.DateEditor(PWMOff, "HH:mm"); 
 		PWMOff.setEditor(PWMOffEditor); 
 		PWMOff.setValue(calendar.getTime()); // will only show the current time
 		daylightpwmsettings.add(PWMOff);
 
 		daylightpwmsettings.add(new JLabel(""));
 
 		daylightpwmsettings.add(new JLabel(""));
 
 		JLabel startpLabel=null;
 		JSpinner startp;
 		startp = new JSpinner( new SpinnerNumberModel(15,0,100,1) );
 		startpLabel=new JLabel ("Start %: ",JLabel.TRAILING);
 		daylightpwmsettings.add(startpLabel);
 		daylightpwmsettings.add(startp);
 
 		JLabel endpLabel=null;
 		JSpinner endp;
 		endp = new JSpinner( new SpinnerNumberModel(100,0,100,1) );
 		endpLabel=new JLabel ("End %: ",JLabel.TRAILING);
 		daylightpwmsettings.add(endpLabel);
 		daylightpwmsettings.add(endp);
 
 		daylightpwmsettings.add(new JLabel(""));
 
 		daylightpwmsettings.add(new JLabel(""));
 
 		JLabel durationpLabel=null;
 		JSpinner durationp;
 		durationp = new JSpinner( new SpinnerNumberModel(60,0,255,1) );
 		durationp.setVisible(false);
 		durationpLabel=new JLabel ("Duration (m): ",JLabel.TRAILING);
 		durationpLabel.setVisible(false);
 		daylightpwmsettings.add(durationpLabel);
 		daylightpwmsettings.add(durationp);
 
 		daylightpwmsettings.add(new JLabel(""));
 		daylightpwmsettings.add(new JLabel(""));
 		daylightpwmsettings.add(new JLabel(""));
 
 		// actinic
 
 		actinicpwmsettings=new JPanel();
 		actinicpwmsettings.setLayout(new GridLayout(3,6));
 		actinicpwmsettings.setOpaque(false);
 
 		actinicpwmsettings.add(new JLabel(""));
 
 		calendar.set(Calendar.HOUR_OF_DAY, 9);
 		calendar.set(Calendar.MINUTE, 0);
 		PWMOnLabel=new JLabel ("Start Time: ",JLabel.TRAILING);
 		actinicpwmsettings.add(PWMOnLabel);
 		PWMOn = new JSpinner( new SpinnerDateModel() ); 
 		PWMOnEditor = new JSpinner.DateEditor(PWMOn, "HH:mm"); 
 		PWMOn.setEditor(PWMOnEditor); 
 		PWMOn.setValue(calendar.getTime()); // will only show the current time
 		actinicpwmsettings.add(PWMOn);
 
 		calendar.set(Calendar.HOUR_OF_DAY, 20);
 		calendar.set(Calendar.MINUTE, 0);
 		PWMOffLabel=new JLabel ("End Time: ",JLabel.TRAILING);
 		actinicpwmsettings.add(PWMOffLabel);
 		PWMOff = new JSpinner( new SpinnerDateModel() ); 
 		PWMOffEditor = new JSpinner.DateEditor(PWMOff, "HH:mm"); 
 		PWMOff.setEditor(PWMOffEditor); 
 		PWMOff.setValue(calendar.getTime()); // will only show the current time
 		actinicpwmsettings.add(PWMOff);
 
 		actinicpwmsettings.add(new JLabel(""));
 
 		actinicpwmsettings.add(new JLabel(""));
 
 		startp = new JSpinner( new SpinnerNumberModel(15,0,100,1) );
 		startpLabel=new JLabel ("Start %: ",JLabel.TRAILING);
 		actinicpwmsettings.add(startpLabel);
 		actinicpwmsettings.add(startp);
 
 		endp = new JSpinner( new SpinnerNumberModel(100,0,100,1) );
 		endpLabel=new JLabel ("End %: ",JLabel.TRAILING);
 		actinicpwmsettings.add(endpLabel);
 		actinicpwmsettings.add(endp);
 
 		actinicpwmsettings.add(new JLabel(""));
 
 		actinicpwmsettings.add(new JLabel(""));
 
 		durationp = new JSpinner( new SpinnerNumberModel(60,0,255,1) );
 		durationp.setVisible(false);
 		durationpLabel=new JLabel ("Duration (m): ",JLabel.TRAILING);
 		durationpLabel.setVisible(false);
 		actinicpwmsettings.add(durationpLabel);
 		actinicpwmsettings.add(durationp);
 
 		actinicpwmsettings.add(new JLabel(""));
 		actinicpwmsettings.add(new JLabel(""));
 		actinicpwmsettings.add(new JLabel(""));	    	
 
 		// pwm expansion 
 		for (int i=0;i<6;i++)
 		{
 			exppwmsettings[i]=new JPanel();
 			exppwmsettings[i].setLayout(new GridLayout(3,6));
 			exppwmsettings[i].setOpaque(false);
 			
 			exppwmsettings[i].add(new JLabel(""));
 
 			calendar.set(Calendar.HOUR_OF_DAY, 9);
 			calendar.set(Calendar.MINUTE, 0);
 			PWMOnLabel=new JLabel ("Start Time: ",JLabel.TRAILING);
 			exppwmsettings[i].add(PWMOnLabel);
 			PWMOn = new JSpinner( new SpinnerDateModel() ); 
 			PWMOnEditor = new JSpinner.DateEditor(PWMOn, "HH:mm"); 
 			PWMOn.setEditor(PWMOnEditor); 
 			PWMOn.setValue(calendar.getTime()); // will only show the current time
 			exppwmsettings[i].add(PWMOn);
 
 			calendar.set(Calendar.HOUR_OF_DAY, 20);
 			calendar.set(Calendar.MINUTE, 0);
 			PWMOffLabel=new JLabel ("End Time: ",JLabel.TRAILING);
 			exppwmsettings[i].add(PWMOffLabel);
 			PWMOff = new JSpinner( new SpinnerDateModel() ); 
 			PWMOffEditor = new JSpinner.DateEditor(PWMOff, "HH:mm"); 
 			PWMOff.setEditor(PWMOffEditor); 
 			PWMOff.setValue(calendar.getTime()); // will only show the current time
 			exppwmsettings[i].add(PWMOff);
 
 			exppwmsettings[i].add(new JLabel(""));
 
 			exppwmsettings[i].add(new JLabel(""));
 
 			startp = new JSpinner( new SpinnerNumberModel(15,0,100,1) );
 			startpLabel=new JLabel ("Start %: ",JLabel.TRAILING);
 			exppwmsettings[i].add(startpLabel);
 			exppwmsettings[i].add(startp);
 
 			endp = new JSpinner( new SpinnerNumberModel(100,0,100,1) );
 			endpLabel=new JLabel ("End %: ",JLabel.TRAILING);
 			exppwmsettings[i].add(endpLabel);
 			exppwmsettings[i].add(endp);
 
 			exppwmsettings[i].add(new JLabel(""));
 
 			exppwmsettings[i].add(new JLabel(""));
 
 			durationp = new JSpinner( new SpinnerNumberModel(60,0,255,1) );
 			durationp.setVisible(false);
 			durationpLabel=new JLabel ("Duration (m): ",JLabel.TRAILING);
 			durationpLabel.setVisible(false);
 			exppwmsettings[i].add(durationpLabel);
 			exppwmsettings[i].add(durationp);
 
 			exppwmsettings[i].add(new JLabel(""));
 			exppwmsettings[i].add(new JLabel(""));
 			exppwmsettings[i].add(new JLabel(""));		
 
 		}
 
 		// AI 
 		for (int i=0;i<AIChannels.length;i++)
 		{
 			aipwmsettings[i]=new JPanel();
 			aipwmsettings[i].setLayout(new GridLayout(3,6));
 			aipwmsettings[i].setOpaque(false);
 			
 			aipwmsettings[i].add(new JLabel(""));
 
 			calendar.set(Calendar.HOUR_OF_DAY, 9);
 			calendar.set(Calendar.MINUTE, 0);
 			PWMOnLabel=new JLabel ("Start Time: ",JLabel.TRAILING);
 			aipwmsettings[i].add(PWMOnLabel);
 			PWMOn = new JSpinner( new SpinnerDateModel() ); 
 			PWMOnEditor = new JSpinner.DateEditor(PWMOn, "HH:mm"); 
 			PWMOn.setEditor(PWMOnEditor); 
 			PWMOn.setValue(calendar.getTime()); // will only show the current time
 			aipwmsettings[i].add(PWMOn);
 
 			calendar.set(Calendar.HOUR_OF_DAY, 20);
 			calendar.set(Calendar.MINUTE, 0);
 			PWMOffLabel=new JLabel ("End Time: ",JLabel.TRAILING);
 			aipwmsettings[i].add(PWMOffLabel);
 			PWMOff = new JSpinner( new SpinnerDateModel() ); 
 			PWMOffEditor = new JSpinner.DateEditor(PWMOff, "HH:mm"); 
 			PWMOff.setEditor(PWMOffEditor); 
 			PWMOff.setValue(calendar.getTime()); // will only show the current time
 			aipwmsettings[i].add(PWMOff);
 
 			aipwmsettings[i].add(new JLabel(""));
 
 			aipwmsettings[i].add(new JLabel(""));
 
 			startp = new JSpinner( new SpinnerNumberModel(15,0,100,1) );
 			startpLabel=new JLabel ("Start %: ",JLabel.TRAILING);
 			aipwmsettings[i].add(startpLabel);
 			aipwmsettings[i].add(startp);
 
 			endp = new JSpinner( new SpinnerNumberModel(100,0,100,1) );
 			endpLabel=new JLabel ("End %: ",JLabel.TRAILING);
 			aipwmsettings[i].add(endpLabel);
 			aipwmsettings[i].add(endp);
 
 			aipwmsettings[i].add(new JLabel(""));
 
 			aipwmsettings[i].add(new JLabel(""));
 
 			durationp = new JSpinner( new SpinnerNumberModel(60,0,255,1) );
 			durationp.setVisible(false);
 			durationpLabel=new JLabel ("Duration (m): ",JLabel.TRAILING);
 			durationpLabel.setVisible(false);
 			aipwmsettings[i].add(durationpLabel);
 			aipwmsettings[i].add(durationp);
 
 			aipwmsettings[i].add(new JLabel(""));
 			aipwmsettings[i].add(new JLabel(""));
 			aipwmsettings[i].add(new JLabel(""));		
 		}	        		
 
 		// Auto Top Off
 
 		aiport = new JPanel(new GridLayout(1,2));
 
 		ButtonGroup AIgroup = new ButtonGroup();
 		JRadioButton AIOption;
 		AIOption = new JRadioButton("Low ATO Port");		
 		AIgroup.add(AIOption);
 		aiport.add(AIOption);
 		AIOption = new JRadioButton("High ATO Port");		
 		AIgroup.add(AIOption);
 		aiport.add(AIOption);
 
 		// Wifi
 
 		wifiportal= new JPanel(new GridLayout(1,2));
 		wifiportal.add(new JLabel("Forum username:"));
 		wifiportal.add(new JTextField(20));
 
 		// RF Expansion
 
 		ActionListener RFListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				JComboBox cb = (JComboBox)actionEvent.getSource();
 				String vm = (String)cb.getSelectedItem();
 
 				if (vm=="Constant" || vm=="Lagoon" || vm=="ReefCrest" || vm=="Tidal Swell")
 				{
 					RFmods.getComponent(4).setVisible(false);
 					RFmods.getComponent(5).setVisible(false);
 				}
 				else
 				{
 					RFmods.getComponent(4).setVisible(true);
 					RFmods.getComponent(5).setVisible(true);
 				}
 				if (vm=="Short Pulse" || vm=="Nutrient Transport")
 				{
 					JLabel j=(JLabel) RFmods.getComponent(4);
 					j.setText("Vortech Duration (ms):");
 					JSpinner jS=(JSpinner)RFmods.getComponent(5);
 					jS.setModel(new SpinnerNumberModel(10,10,10000,1));
 				}
 				if (vm=="Long Pulse")
 				{
 					JLabel j=(JLabel) RFmods.getComponent(4);
 					j.setText("Vortech Duration (s):");
 					JSpinner jS=(JSpinner)RFmods.getComponent(5);
 					jS.setModel(new SpinnerNumberModel(10,1,10000,1));
 				}
 			}
 		};		
 
 		RFmods= new JPanel(new GridLayout(3,2));
 		RFmods.add(new JLabel("Vortech Mode:"));
 		JComboBox vm=new JComboBox(VortechModes);
 		vm.addActionListener(RFListener);
 		RFmods.add(vm);
 
 
 		RFmods.add(new JLabel("Vortech Speed (%):"));
 		RFmods.add(new JSpinner( new SpinnerNumberModel(50,0,100,1)));
 
 		durationp = new JSpinner( new SpinnerNumberModel(10,0,255,1) );
 		durationp.setVisible(false);
 		durationpLabel=new JLabel ("Vortech Duration:");
 		durationpLabel.setVisible(false);
 		RFmods.add(durationpLabel);
 		RFmods.add(durationp);
 
 		for (int i=0;i<RadionChannels.length;i++)
 		{
 			rfpwmsettings[i]=new JPanel();
 			rfpwmsettings[i].setLayout(new GridLayout(3,6));
 			rfpwmsettings[i].setOpaque(false);
 
 			rfpwmsettings[i].add(new JLabel(""));
 
 			calendar.set(Calendar.HOUR_OF_DAY, 9);
 			calendar.set(Calendar.MINUTE, 0);
 			PWMOnLabel=new JLabel ("Start Time: ",JLabel.TRAILING);
 			rfpwmsettings[i].add(PWMOnLabel);
 			PWMOn = new JSpinner( new SpinnerDateModel() ); 
 			PWMOnEditor = new JSpinner.DateEditor(PWMOn, "HH:mm"); 
 			PWMOn.setEditor(PWMOnEditor); 
 			PWMOn.setValue(calendar.getTime()); // will only show the current time
 			rfpwmsettings[i].add(PWMOn);
 
 			calendar.set(Calendar.HOUR_OF_DAY, 20);
 			calendar.set(Calendar.MINUTE, 0);
 			PWMOffLabel=new JLabel ("End Time: ",JLabel.TRAILING);
 			rfpwmsettings[i].add(PWMOffLabel);
 			PWMOff = new JSpinner( new SpinnerDateModel() ); 
 			PWMOffEditor = new JSpinner.DateEditor(PWMOff, "HH:mm"); 
 			PWMOff.setEditor(PWMOffEditor); 
 			PWMOff.setValue(calendar.getTime()); // will only show the current time
 			rfpwmsettings[i].add(PWMOff);
 
 			rfpwmsettings[i].add(new JLabel(""));
 
 			rfpwmsettings[i].add(new JLabel(""));
 
 			startp = new JSpinner( new SpinnerNumberModel(15,0,100,1) );
 			startpLabel=new JLabel ("Start %: ",JLabel.TRAILING);
 			rfpwmsettings[i].add(startpLabel);
 			rfpwmsettings[i].add(startp);
 
 			endp = new JSpinner( new SpinnerNumberModel(100,0,100,1) );
 			endpLabel=new JLabel ("End %: ",JLabel.TRAILING);
 			rfpwmsettings[i].add(endpLabel);
 			rfpwmsettings[i].add(endp);
 
 			rfpwmsettings[i].add(new JLabel(""));
 
 			rfpwmsettings[i].add(new JLabel(""));
 
 			durationp = new JSpinner( new SpinnerNumberModel(60,0,255,1) );
 			durationp.setVisible(false);
 			durationpLabel=new JLabel ("Duration (m): ",JLabel.TRAILING);
 			durationpLabel.setVisible(false);
 			rfpwmsettings[i].add(durationpLabel);
 			rfpwmsettings[i].add(durationp);
 
 			rfpwmsettings[i].add(new JLabel(""));
 			rfpwmsettings[i].add(new JLabel(""));
 			rfpwmsettings[i].add(new JLabel(""));		
 		}	   		
 
 		Buzzermods= new JPanel();
 		Buzzermods.setLayout(new BoxLayout( Buzzermods, BoxLayout.PAGE_AXIS));
 		Buzzermods.add(new JCheckBox ("Overheat"));
 		Buzzermods.add(new JCheckBox ("ATO Timeout"));
 		Buzzermods.add(new JCheckBox ("High ATO is activated"));
 		for (int a=0;a<6;a++)
 			Buzzermods.add(new JCheckBox ("I/O Channel " + a + " is activated"));
 
 		// TODO: BuildSettings
 	}
 
 	private void ApplyLayout(JPanel panel, SpringLayout layout)
 	{
 		panel.setOpaque(false);
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 		layout.getConstraints(panel).setHeight(Spring.constant(250));
 //		layout.getConstraints(panel).setWidth(Spring.constant(250));
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(0));
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 		for (int c=1;c<panel.getComponentCount();c+=2)
 			width=Spring.max(width, layout.getConstraints(panel.getComponent(c)).getWidth());
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		for (int c=1;c<panel.getComponentCount();c+=2)
 		{
 			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 			layout.getConstraints(panel.getComponent(c)).setY(y);
 			layout.getConstraints(panel.getComponent(c)).setWidth(width);
 			layout.getConstraints(panel.getComponent(c+1)).setX(x);
 			layout.getConstraints(panel.getComponent(c+1)).setY(Spring.sum(y, Spring.constant(-5)));
 			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c+1)).getHeight());
 			//			y=Spring.sum(y, Spring.constant(5));
 		}		
 	}	
 
 	private void ApplyTimedMemoryLayout(JPanel panel, SpringLayout layout)
 	{
 		panel.setOpaque(false);
 		Spring y = Spring.constant(0);
 
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 //		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 
 		for (int c=1;c<panel.getComponentCount();c+=2)
 		{
 			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 			layout.getConstraints(panel.getComponent(c)).setY(y);
 			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c)).getHeight());
 			layout.getConstraints(panel.getComponent(c+1)).setX(Spring.constant(5));
 			layout.getConstraints(panel.getComponent(c+1)).setY(y);
 			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c)).getHeight());
 			y=Spring.sum(y, Spring.constant(10));
 		}	
 
 		//		for (int c=1;c<panel.getComponentCount();c+=2)
 		//		{
 		//			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(0));
 		//			layout.getConstraints(panel.getComponent(c)).setY(y);
 		//			layout.getConstraints(panel.getComponent(c+1)).setX(Spring.constant(150));
 		//			layout.getConstraints(panel.getComponent(c+1)).setY(y);
 		//			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c+1)).getHeight());
 		//		}		
 	}	
 
 	private void ApplyTimedMemorySettingsLayout(JPanel panel, SpringLayout layout)
 	{
 		panel.setOpaque(false);
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(0));
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 		for (int c=1;c<panel.getComponentCount()-3;c+=2)
 			width=Spring.max(width, layout.getConstraints(panel.getComponent(c)).getWidth());
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		for (int c=1;c<panel.getComponentCount()-3;c+=2)
 		{
 			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 			layout.getConstraints(panel.getComponent(c)).setY(y);
 			layout.getConstraints(panel.getComponent(c)).setWidth(width);
 			layout.getConstraints(panel.getComponent(c+1)).setX(x);
 			layout.getConstraints(panel.getComponent(c+1)).setY(Spring.sum(y, Spring.constant(-5)));
 			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c+1)).getHeight());
 			//			y=Spring.sum(y, Spring.constant(5));
 		}		
 		y=Spring.sum(y, Spring.constant(5));
 		int c=panel.getComponentCount()-3;
 		layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(c)).setY(y);
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(c)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 		c++;
 		layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(c)).setY(y);
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(c)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 		c++;
 		layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(c)).setY(y);
 
 		//		for (int c=1;c<panel.getComponentCount();c+=2)
 		//		{
 		//			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(0));
 		//			layout.getConstraints(panel.getComponent(c)).setY(y);
 		//			layout.getConstraints(panel.getComponent(c+1)).setX(Spring.constant(150));
 		//			layout.getConstraints(panel.getComponent(c+1)).setY(y);
 		//			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c+1)).getHeight());
 		//		}		
 	}
 
 	private void ApplyDosingLayout(JPanel panel, SpringLayout layout)
 	{
 		panel.setOpaque(false);
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 //		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 
 
 		layout.getConstraints(panel.getComponent(1)).setX(x);
 		layout.getConstraints(panel.getComponent(1)).setY(y);
 		width=layout.getConstraints(panel.getComponent(1)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(2)).setX(x);
 		layout.getConstraints(panel.getComponent(2)).setY(Spring.sum(y, Spring.constant(-5)));
 
 		//		y=Spring.sum(y, layout.getConstraints(panel.getComponent(1)).getHeight());
 		//		y=Spring.sum(y, Spring.constant(5));
 
 		width=layout.getConstraints(panel.getComponent(2)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 
 		layout.getConstraints(panel.getComponent(3)).setX(x);
 		layout.getConstraints(panel.getComponent(3)).setY(y);
 		width=layout.getConstraints(panel.getComponent(3)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(4)).setX(x);
 		layout.getConstraints(panel.getComponent(4)).setY(Spring.sum(y, Spring.constant(-5)));
 
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(2)).getHeight());
 		//		y=Spring.sum(y, Spring.constant(15));
 
 		x=Spring.constant(5);
 		layout.getConstraints(panel.getComponent(5)).setX(x);
 		layout.getConstraints(panel.getComponent(5)).setY(y);
 		width=layout.getConstraints(panel.getComponent(5)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(6)).setX(x);
 		layout.getConstraints(panel.getComponent(6)).setY(Spring.sum(y, Spring.constant(-5)));
 	}
 
 	private void ApplyWMLayout(JPanel panel, SpringLayout layout)
 	{
 		panel.setOpaque(false);
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 //		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 
 
 		layout.getConstraints(panel.getComponent(1)).setX(Spring.constant(0));
 		layout.getConstraints(panel.getComponent(2)).setX(Spring.constant(150));
 		layout.getConstraints(panel.getComponent(1)).setY(y);
 		layout.getConstraints(panel.getComponent(2)).setY(y);
 
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(1)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 
 		width=layout.getConstraints(panel.getComponent(3)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 
 		layout.getConstraints(panel.getComponent(3)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(3)).setY(y);
 		layout.getConstraints(panel.getComponent(4)).setX(x);
 		layout.getConstraints(panel.getComponent(4)).setY(Spring.sum(y, Spring.constant(-5)));
 
 		width=layout.getConstraints(panel.getComponent(4)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(5)).setX(x);
 		layout.getConstraints(panel.getComponent(5)).setY(y);
 		width=layout.getConstraints(panel.getComponent(5)).getWidth();
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(6)).setX(x);
 		layout.getConstraints(panel.getComponent(6)).setY(Spring.sum(y, Spring.constant(-5)));
 	}
 
 	private void ApplyATOLayout(JPanel panel, SpringLayout layout)
 	{
 		panel.setOpaque(false);
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 //		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 
 		for (int c=1;c<4;c++)
 			width=Spring.max(width, layout.getConstraints(panel.getComponent(c)).getWidth());
 
 		layout.getConstraints(panel.getComponent(1)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(1)).setY(y);
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(1)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(2)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(2)).setY(y);
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(2)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 		layout.getConstraints(panel.getComponent(3)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(3)).setY(y);
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(3)).getHeight());
 		y=Spring.sum(y, Spring.constant(5));
 
 		width = Spring.constant(0);
 		for (int c=4;c<panel.getComponentCount();c+=2)
 			width=Spring.max(width, layout.getConstraints(panel.getComponent(c)).getWidth());
 		x=Spring.sum(x, width);
 		x=Spring.sum(x, Spring.constant(5));
 		for (int c=4;c<panel.getComponentCount();c+=2)
 		{
 			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 			layout.getConstraints(panel.getComponent(c)).setY(y);
 			layout.getConstraints(panel.getComponent(c)).setWidth(width);
 			layout.getConstraints(panel.getComponent(c+1)).setX(x);
 			layout.getConstraints(panel.getComponent(c+1)).setY(Spring.sum(y, Spring.constant(-5)));
 			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c+1)).getHeight());
 			y=Spring.sum(y, Spring.constant(5));
 		}			
 	}
 
 	private void RevalidateSettings()
 	{
 		if (settingswidth==0)
 		{
 			settingswidth = functionsettings[1].getWidth();
 			SpringLayout layout;
 
 			for (int a=1; a<=16;a++)
 			{
 				layout=(SpringLayout) Timed[a].getLayout();
 				layout.getConstraints(Timed[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(Timed[a]);
 				Timed[a].revalidate();
 
 				layout=(SpringLayout) TimedMemory[a].getLayout();
 				layout.getConstraints(TimedMemory[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(TimedMemory[a]);
 				TimedMemory[a].revalidate();
 
 				layout=(SpringLayout) TimedMemorySettings[a].getLayout();
 				layout.getConstraints(TimedMemorySettings[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(TimedMemorySettings[a]);
 				TimedMemorySettings[a].revalidate();
 				
 				layout=(SpringLayout) Heater[a].getLayout();
 				layout.getConstraints(Heater[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(Heater[a]);
 				Heater[a].revalidate();
 
 				layout=(SpringLayout) Chiller[a].getLayout();
 				layout.getConstraints(Chiller[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(Chiller[a]);
 				Chiller[a].revalidate();
 
 				layout=(SpringLayout) ATO[a].getLayout();
 				layout.getConstraints(ATO[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(ATO[a]);
 				ATO[a].revalidate();
 
 				layout=(SpringLayout) WM[a].getLayout();
 				layout.getConstraints(WM[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(WM[a]);
 				WM[a].revalidate();
 
 				layout=(SpringLayout) CO2Control[a].getLayout();
 				layout.getConstraints(CO2Control[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(CO2Control[a]);
 				CO2Control[a].revalidate();
 
 				layout=(SpringLayout) pHControl[a].getLayout();
 				layout.getConstraints(pHControl[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(pHControl[a]);
 				pHControl[a].revalidate();
 
 				layout=(SpringLayout) Dosing[a].getLayout();
 				layout.getConstraints(Dosing[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(Dosing[a]);
 				Dosing[a].revalidate();
 
 				layout=(SpringLayout) Delayed[a].getLayout();
 				layout.getConstraints(Delayed[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(Delayed[a]);
 				Delayed[a].revalidate();
 
 				layout=(SpringLayout) Opposite[a].getLayout();
 				layout.getConstraints(Opposite[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(Opposite[a]);
 				Opposite[a].revalidate();
 
 				layout=(SpringLayout) AlwaysOn[a].getLayout();
 				layout.getConstraints(AlwaysOn[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(AlwaysOn[a]);
 				AlwaysOn[a].revalidate();
 
 				layout=(SpringLayout) NotUsed[a].getLayout();
 				layout.getConstraints(NotUsed[a].getComponent(0)).setWidth(Spring.constant(settingswidth-30));
 				SwingUtilities.updateComponentTreeUI(NotUsed[a]);
 				NotUsed[a].revalidate();
 			}
 		}
 	}
 
 	private void ResetTempRange()
 	{
 		for (int a=1;a<=16;a++)
 		{
 			JPanel j = null;
 			JLabel jL = null;
 			JSpinner jS = null;
 			j=(JPanel)functionsettings[a].getComponent(2);
 			if (tempunit==0)
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0F): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0F): ");
 			}
 			else
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0C): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0C): ");
 			}
 			j=(JPanel)functionsettings[a].getComponent(3);
 			if (tempunit==0)
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0F): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0F): ");
 			}
 			else
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0C): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0C): ");
 			}
 		}
 	}
 
 	private void ShowHardCodeSettings(boolean bVisible)
 	{
 		int numdosing=0;
 		for (int a=1;a<=16;a++)
 		{
 			CardLayout cl = (CardLayout)(functionsettings[a].getLayout());
 			JRadioButton AOn = (JRadioButton) functions[a].getComponent(0);
 			if (AOn.isSelected())
 			{
 				if (!bVisible)
 					cl.show(functionsettings[a], "Light Schedule");
 				else
 					cl.show(functionsettings[a], "Time Schedule");
 
 			}
 //			functions[a].getComponent(7)
 			if (!bVisible)
 			{
 				((JLabel) Dosing[a].getComponent(5)).setText("This dosing pump will have 0 minutes offset.");
 //				if (numdosing==1)
 //					((JLabel) Dosing[a].getComponent(5)).setText("This dosing pump will have 5 minutes offset.");
 //				if (numdosing==2)
 //					((JLabel) Dosing[a].getComponent(5)).setText("This dosing pump will have 10 minutes offset.");
 //				if (numdosing>=3)
 //				{
 //					((JRadioButton) functions[a].getComponent(7)).setSelected(false);
 //					((JRadioButton) functions[a].getComponent(11)).setSelected(true);
 //					cl.show(functionsettings[a], "Not Used");
 //					numdosing--;
 //				}
 //				if (((JRadioButton) functions[a].getComponent(7)).isSelected())
 //					numdosing++;
 			}
 			else
 			{
 				((JLabel) Dosing[a].getComponent(5)).setText("Offset (m):");
 			}
 			Dosing[a].getComponent(6).setVisible(bVisible); 
 		}
 		daylightpwmsettings.getComponent(1).setVisible(bVisible);
 		daylightpwmsettings.getComponent(2).setVisible(bVisible);
 		daylightpwmsettings.getComponent(3).setVisible(bVisible);
 		daylightpwmsettings.getComponent(4).setVisible(bVisible);
 		daylightpwmsettingspanel.getComponent(3).setVisible(!bVisible);
 		actinicpwmsettings.getComponent(1).setVisible(bVisible);
 		actinicpwmsettings.getComponent(2).setVisible(bVisible);
 		actinicpwmsettings.getComponent(3).setVisible(bVisible);
 		actinicpwmsettings.getComponent(4).setVisible(bVisible);
 		actinicpwmsettingspanel.getComponent(3).setVisible(!bVisible);
 		for (int b=0;b<6;b++)
 		{
 			exppwmsettings[b].getComponent(1).setVisible(bVisible);
 			exppwmsettings[b].getComponent(2).setVisible(bVisible);
 			exppwmsettings[b].getComponent(3).setVisible(bVisible);
 			exppwmsettings[b].getComponent(4).setVisible(bVisible);
 			exppwmsettingspanel[b].getComponent(3).setVisible(!bVisible);
 			rfpwmsettings[b].getComponent(1).setVisible(bVisible);
 			rfpwmsettings[b].getComponent(2).setVisible(bVisible);
 			rfpwmsettings[b].getComponent(3).setVisible(bVisible);
 			rfpwmsettings[b].getComponent(4).setVisible(bVisible);
 			rfsettingspanel[b].getComponent(3).setVisible(!bVisible);
 		}
 		for (int b=0;b<3;b++)
 		{
 			aipwmsettings[b].getComponent(1).setVisible(bVisible);
 			aipwmsettings[b].getComponent(2).setVisible(bVisible);
 			aipwmsettings[b].getComponent(3).setVisible(bVisible);
 			aipwmsettings[b].getComponent(4).setVisible(bVisible);
 			aisettingspanel[b].getComponent(3).setVisible(!bVisible);
 		}		
 	}
 	
 	private void CheckDaylightPWM()
 	{
 		JRadioButton jm = (JRadioButton) memsettings.getComponent(0);
 		Inside i=(Inside)daylightpwmsettingspanel.getComponent(0);
 		ImageIcon iconnection = null;
 		JLabel c = (JLabel)daylightpwmsettingspanel.getComponent(1);;
 
 		JRadioButton jpd=(JRadioButton) daylightpwm.getComponent(0);
 		if (jpd.isSelected())
 		{
 			daylightpwmsettings.getComponent(13).setVisible(true);
 			daylightpwmsettings.getComponent(14).setVisible(true);
 			if (jm.isSelected())
 				i.SetText("<HTML>Please enter the start %, end %, duration, start time and end time of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 			else
 				i.SetText("<HTML>Please enter the start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 		}
 		jpd=(JRadioButton) daylightpwm.getComponent(1);
 		if (jpd.isSelected())
 		{
 			daylightpwmsettings.getComponent(13).setVisible(false);
 			daylightpwmsettings.getComponent(14).setVisible(false);
 			if (jm.isSelected())
 				i.SetText("<HTML>Please enter the start %, end %, start time and end time of the parabola.<br><br></HTML>");
 			else
 				i.SetText("<HTML>Please enter the start % and end % of the parabola.<br><br></HTML>");
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 		}
 		c.setIcon(iconnection);		
 	}
 
 	private void CheckActinicPWM()
 	{
 		JRadioButton jm = (JRadioButton) memsettings.getComponent(0);
 		Inside i=(Inside)actinicpwmsettingspanel.getComponent(0);
 		ImageIcon iconnection = null;
 		JLabel c = (JLabel)actinicpwmsettingspanel.getComponent(1);;
 
 		JRadioButton jpd=(JRadioButton) actinicpwm.getComponent(0);
 		if (jpd.isSelected())
 		{
 			actinicpwmsettings.getComponent(13).setVisible(true);
 			actinicpwmsettings.getComponent(14).setVisible(true);
 			if (jm.isSelected())
 				i.SetText("<HTML>Please enter the start %, end %, duration, start time and end time of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 			else
 				i.SetText("<HTML>Please enter the start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 		}
 		jpd=(JRadioButton) actinicpwm.getComponent(1);
 		if (jpd.isSelected())
 		{
 			actinicpwmsettings.getComponent(13).setVisible(false);
 			actinicpwmsettings.getComponent(14).setVisible(false);
 			if (jm.isSelected())
 				i.SetText("<HTML>Please enter the start %, end %, start time and end time of the parabola.<br><br></HTML>");
 			else
 				i.SetText("<HTML>Please enter the start % and end % of the parabola.<br><br></HTML>");
 			iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 		}
 		c.setIcon(iconnection);		
 	}
 	
 	private void CheckExpPWM()
 	{
 		for (int a=0;a<6;a++)
 		{
 			JRadioButton jm = (JRadioButton) memsettings.getComponent(0);
 			Inside i=(Inside)exppwmsettingspanel[a].getComponent(0);
 			ImageIcon iconnection = null;
 			JLabel c = (JLabel)exppwmsettingspanel[a].getComponent(1);;
 
 			JRadioButton jpd=(JRadioButton) exppwm[a].getComponent(0);
 			if (jpd.isSelected())
 			{
 				exppwmsettings[a].getComponent(13).setVisible(true);
 				exppwmsettings[a].getComponent(14).setVisible(true);
 				if (jm.isSelected())
 					i.SetText("<HTML>Please enter the start %, end %, duration, start time and end time of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 				else
 					i.SetText("<HTML>Please enter the start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 				iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			}
 			jpd=(JRadioButton) exppwm[a].getComponent(1);
 			if (jpd.isSelected())
 			{
 				exppwmsettings[a].getComponent(13).setVisible(false);
 				exppwmsettings[a].getComponent(14).setVisible(false);
 				if (jm.isSelected())
 					i.SetText("<HTML>Please enter the start %, end %, start time and end time of the parabola.<br><br></HTML>");
 				else
 					i.SetText("<HTML>Please enter the start % and end % of the parabola.<br><br></HTML>");
 				iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			}
 			c.setIcon(iconnection);				
 		}
 	}
 	
 	private void CheckAIPWM()
 	{
 		for (int a=0;a<3;a++)
 		{
 			JRadioButton jm = (JRadioButton) memsettings.getComponent(0);
 			Inside i=(Inside)aisettingspanel[a].getComponent(0);
 			ImageIcon iconnection = null;
 			JLabel c = (JLabel)aisettingspanel[a].getComponent(1);;
 
 			JRadioButton jpd=(JRadioButton) aipwm[a].getComponent(0);
 			if (jpd.isSelected())
 			{
 				aipwmsettings[a].getComponent(13).setVisible(true);
 				aipwmsettings[a].getComponent(14).setVisible(true);
 				if (jm.isSelected())
 					i.SetText("<HTML>Please enter the start %, end %, duration, start time and end time of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 				else
 					i.SetText("<HTML>Please enter the start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 				iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			}
 			jpd=(JRadioButton) aipwm[a].getComponent(1);
 			if (jpd.isSelected())
 			{
 				aipwmsettings[a].getComponent(13).setVisible(false);
 				aipwmsettings[a].getComponent(14).setVisible(false);
 				if (jm.isSelected())
 					i.SetText("<HTML>Please enter the start %, end %, start time and end time of the parabola.<br><br></HTML>");
 				else
 					i.SetText("<HTML>Please enter the start % and end % of the parabola.<br><br></HTML>");
 				iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			}
 			c.setIcon(iconnection);				
 		}
 	}	
 
 	private void CheckRFPWM()
 	{
 		for (int a=0;a<6;a++)
 		{
 			JRadioButton jm = (JRadioButton) memsettings.getComponent(0);
 			Inside i=(Inside)rfsettingspanel[a].getComponent(0);
 			ImageIcon iconnection = null;
 			JLabel c = (JLabel)rfsettingspanel[a].getComponent(1);;
 
 			JRadioButton jpd=(JRadioButton) rfpwm[a].getComponent(0);
 			if (jpd.isSelected())
 			{
 				rfpwmsettings[a].getComponent(13).setVisible(true);
 				rfpwmsettings[a].getComponent(14).setVisible(true);
 				if (jm.isSelected())
 					i.SetText("<HTML>Please enter the start %, end %, duration, start time and end time of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 				else
 					i.SetText("<HTML>Please enter the start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 				iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			}
 			jpd=(JRadioButton) rfpwm[a].getComponent(1);
 			if (jpd.isSelected())
 			{
 				rfpwmsettings[a].getComponent(13).setVisible(false);
 				rfpwmsettings[a].getComponent(14).setVisible(false);
 				if (jm.isSelected())
 					i.SetText("<HTML>Please enter the start %, end %, start time and end time of the parabola.<br><br></HTML>");
 				else
 					i.SetText("<HTML>Please enter the start % and end % of the parabola.<br><br></HTML>");
 				iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			}
 			c.setIcon(iconnection);				
 		}
 	}	
 	private void ShowHideComponent(JPanel c, boolean bVisible)
 	{
 		for (int a=1; a<c.getComponentCount();a++)
 		{
 			c.getComponent(a).setVisible(bVisible);
 		}
 
 	}
 
 	private void MessageBox(String msg)
 	{
 		JOptionPane.showMessageDialog(null,msg , "Message", JOptionPane.DEFAULT_OPTION);
 	}
 
 	private void MessageBox(int msg)
 	{
 		JOptionPane.showMessageDialog(null,msg , "Message", JOptionPane.DEFAULT_OPTION);
 	}
 
 	private void ReturnLF()
 	{
 		try {
 			UIManager.setLookAndFeel(lf);
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void OpenSerial()
 	{
 		try
 		{
 			serial = new Serial(Preferences.get("serial.port"),57600);
 			serial.addListener(this);
 		}
 		catch (SerialException e1)
 		{
 			e1.printStackTrace();
 		}
 	}
 
 	private void CloseSerial()
 	{
 		if (serial!=null)
 		{
 			serial.dispose();
 			serial=null;
 		}
 	}
 
 	public void message(final String s) {
 		SwingUtilities.invokeLater(new Runnable() 
 		{
 			public void run() {
 				System.out.print(s);
 				connected=true;
 			}
 		});
 	}
 
 	private void perform() {
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if (WaitUART)
 		{
 
 		}
 	}
 	public class JCustomEditorPane extends JEditorPane {
 		public JCustomEditorPane(String s) 
 		{
 			this.setContentType("text/html");
 			this.setText(s);
 			this.setEditable(false);   
 			this.setOpaque(false);
 			this.setBorder(BorderFactory.createEmptyBorder()); 
 			this.setBackground(new Color(0,0,0,0)); 	
 			((HTMLDocument)this.getDocument()).getStyleSheet().addRule(bodyRule);
 		}
 	
 	}
 
 	public class Header extends JPanel {
 		public Header()
 		{
 			ImageIcon icon=null;
 			icon = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_horizontal.png");
 			this.setLayout(new BorderLayout());
 			HeaderPanel header = new HeaderPanel("Reef Angel Wizard",icon,0.0f);
 			this.add(BorderLayout.NORTH, header);
 			this.add(BorderLayout.SOUTH, new JSeparator(JSeparator.HORIZONTAL));
 			this.setBorder(new EmptyBorder(0, 0, 0, 0));
 		}
 	}
 
 	public class Footer extends JPanel {
 		public Footer()
 		{
 			this.setLayout(new BorderLayout());
 			JPanel footer = new JPanel();
 			footer.setLayout(layoutConstraintsManager.createLayout("footer", footer));
 			footer.setPreferredSize(new Dimension(650,50));
 			footer.setBackground(new Color(255,253,234));
 			this.setBorder(new EmptyBorder(0, 0, 0, 0));
 			this.add(BorderLayout.SOUTH, footer);
 			this.add(BorderLayout.NORTH, new JSeparator(JSeparator.HORIZONTAL));
 
 			ActionListener ButtonActionListener = new ActionListener() {
 				public void actionPerformed(ActionEvent actionEvent)
 				{
 					AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 					String swindow=Title.GetText();
 					if (swindow.indexOf("Welcome")==0)
 					{
 						prevwindow="Welcome";
 						nextwindow="Memory Settings";
 						loadInitialValues();
 						CheckNav();
 					}
 					
 					if (swindow.indexOf("Memory Settings")==0)
 					{
 						prevwindow="Welcome";
 						nextwindow="Temperature Settings";
 						JRadioButton j = (JRadioButton) memsettings.getComponent(0);
 						if (j.isSelected())
 						{
 							for (int a=1;a<=16;a++)
 							{
 								WM[a].getComponent(2).setEnabled(true);
 							}
 						}
 						else
 						{
 							for (int a=1;a<=16;a++)
 							{
 								if (a<=8) Title.SetText ("Main Relay Box - Port "+a);
 								if (a>8) Title.SetText ("Expansion Relay Box - Port "+(a-8));
 								WM[a].getComponent(2).setEnabled(false);
 								setSelectedButton(WM[a],1);
 							}
 							Title.SetText ("Memory Settings");
 						}
 					}
 					
 					if (swindow.indexOf("Temperature Settings")==0)
 					{
 						prevwindow="Memory Settings";
 						nextwindow="Expansion Modules";
 						ResetTempRange();
 					}
 
 					if (swindow.indexOf("Expansion Modules")==0)
 					{
 						prevwindow="Temperature Settings";
 						nextwindow="Attachments";
 						JCheckBox jc=null;
 						jc=(JCheckBox) expansionmods.getComponent(0);
 						if (jc.isSelected())
 						{
 							relayexpansion=1;
 							for (int a=1;a<=16;a++)
 							{
 								JComboBox c=(JComboBox) Opposite[a].getComponent(2);
 								int oldindex = c.getSelectedIndex();
 								if (oldindex==-1) oldindex=0;
 								if (c.getItemCount()!=16)
 								{
 									c.removeAllItems();
 									for (int b=1;b<=8;b++)
 									{
 										c.addItem("Main Box Port "+b);
 									}
 									for (int b=1;b<=8;b++)
 									{
 										c.addItem("Exp. Box Port "+b);
 									}
 									c.setSelectedIndex(oldindex);
 								}
 							}
 						}	
 						else
 						{
 							relayexpansion=0;
 							for (int a=1;a<=8;a++)
 							{
 								JComboBox c=(JComboBox) Opposite[a].getComponent(2);
 								if (c.getItemCount()!=8)
 								{
 									int oldindex = c.getSelectedIndex();
 									if (oldindex>8) oldindex-=8;
 									if (oldindex==-1) oldindex=0;
 									c.removeAllItems();
 									for (int b=1;b<=8;b++)
 									{
 										c.addItem("Main Box Port "+b);
 									}
 									c.setSelectedIndex(oldindex);
 								}
 							}
 						}
 
 						jc=(JCheckBox) expansionmods.getComponent(1);
 						if (jc.isSelected()) dimmingexpansion=1; else dimmingexpansion=0;
 						jc=(JCheckBox) expansionmods.getComponent(2);
 						if (jc.isSelected()) rfexpansion=1; else rfexpansion=0;
 						jc=(JCheckBox) expansionmods.getComponent(3);
 						if (jc.isSelected()) salinityexpansion=1; else salinityexpansion=0;
 						jc=(JCheckBox) expansionmods.getComponent(4);
 						if (jc.isSelected()) ioexpansion=1; else ioexpansion=0;
 						jc=(JCheckBox) expansionmods.getComponent(5);
 						if (jc.isSelected()) orpexpansion=1; else orpexpansion=0;
 						jc=(JCheckBox) expansionmods.getComponent(6);
 						if (jc.isSelected()) phexpansion=1; else phexpansion=0;
 						jc=(JCheckBox) expansionmods.getComponent(7);
 						if (jc.isSelected()) waterlevelexpansion=1; else waterlevelexpansion=0;
 						if (ioexpansion==1)
 						{
 							for (int a=0;a<9;a++)
 								Buzzermods.getComponent(a).setVisible(true);
 						}
 						else
 						{
 							for (int a=0;a<3;a++)
 								Buzzermods.getComponent(a).setVisible(true);
 							for (int a=3;a<9;a++)
 								Buzzermods.getComponent(a).setVisible(false);
 						}	
 					}
 
 					if (swindow.indexOf("Attachments")==0)
 					{
 						prevwindow="Expansion Modules";
 						nextwindow="Main Relay Box";
 						JCheckBox jc=null;
 						jc=(JCheckBox) attachmentmods.getComponent(0);
 						if (jc.isSelected()) wifi=1; else wifi=0;
 						jc=(JCheckBox) attachmentmods.getComponent(1);
 						if (jc.isSelected()) ailed=1; else ailed=0;		
 					}
 
 					if (swindow.indexOf("Main Relay Box")==0)
 					{
 						prevwindow="Attachments";
 						nextwindow="Main Relay Box - Port 1";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 1")==0)
 					{
 						prevwindow="Main Relay Box";
 						nextwindow="Main Relay Box - Port 2";
 						UpdateTimeLabels();
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 2")==0)
 					{
 						prevwindow="Main Relay Box - Port 1";
 						nextwindow="Main Relay Box - Port 3";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 3")==0)
 					{
 						prevwindow="Main Relay Box - Port 2";
 						nextwindow="Main Relay Box - Port 4";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 4")==0)
 					{
 						prevwindow="Main Relay Box - Port 3";
 						nextwindow="Main Relay Box - Port 5";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 5")==0)
 					{
 						prevwindow="Main Relay Box - Port 4";
 						nextwindow="Main Relay Box - Port 6";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 6")==0)
 					{
 						prevwindow="Main Relay Box - Port 5";
 						nextwindow="Main Relay Box - Port 7";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 7")==0)
 					{
 						prevwindow="Main Relay Box - Port 6";
 						nextwindow="Main Relay Box - Port 8";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port 8")==0)
 					{
 						prevwindow="Main Relay Box - Port 7";
 						if (relayexpansion==1)
 							nextwindow="Expansion Relay Box";
 						else
 							nextwindow="Daylight Dimming Channel";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box")==0)
 					{
 						prevwindow="Main Relay Box - Port 8";
 						nextwindow="Expansion Relay Box - Port 1";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 1")==0)
 					{
 						prevwindow="Expansion Relay Box";
 						nextwindow="Expansion Relay Box - Port 2";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 2")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 1";
 						nextwindow="Expansion Relay Box - Port 3";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 3")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 2";
 						nextwindow="Expansion Relay Box - Port 4";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 4")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 3";
 						nextwindow="Expansion Relay Box - Port 5";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 5")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 4";
 						nextwindow="Expansion Relay Box - Port 6";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 6")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 5";
 						nextwindow="Expansion Relay Box - Port 7";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 7")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 6";
 						nextwindow="Expansion Relay Box - Port 8";
 					}
 					
 					if (swindow.indexOf("Expansion Relay Box - Port 8")==0)
 					{
 						prevwindow="Expansion Relay Box - Port 7";
 						nextwindow="Daylight Dimming Channel";
 					}
 					
 					if (swindow.indexOf("Main Relay Box - Port")==0 || swindow.indexOf("Expansion Relay Box - Port")==0)
 					{
 						int numdosing=0;
 						atolow=false;
 						atohigh=false;
 						for (int a=1;a<=16;a++)
 						{
 							ATO[a].getComponent(1).setEnabled(true);
 							ATO[a].getComponent(2).setEnabled(true);
 							ATO[a].getComponent(3).setEnabled(true);
 							functions[a].getComponent(3).setEnabled(true);
 						}				
 
 						for (int a=1;a<=16;a++)
 						{
 							JRadioButton AOn = (JRadioButton) functions[a].getComponent(3);
 							if (AOn.isSelected())
 							{
 								JRadioButton jat=(JRadioButton) ATO[a].getComponent(1);
 								if (jat.isSelected())
 								{
 									atolow=true;
 									atohigh=true;
 								}
 								jat=(JRadioButton) ATO[a].getComponent(2);
 								if (jat.isSelected()) atolow=true;
 								jat=(JRadioButton) ATO[a].getComponent(3);
 								if (jat.isSelected()) atohigh=true;
 							}
 							JRadioButton j = (JRadioButton) memsettings.getComponent(1);
 							if (j.isSelected())
 							{
 								if (numdosing==1)
 									((JLabel) Dosing[a].getComponent(5)).setText("This dosing pump will have 5 minutes offset.");
 								if (numdosing==2)
 									((JLabel) Dosing[a].getComponent(5)).setText("This dosing pump will have 10 minutes offset.");
 								if (((JRadioButton) functions[a].getComponent(7)).isSelected())
 									numdosing++;
 							}
 						}
 						if (numdosing>=3)
 						{
 							for (int a=1;a<=16;a++)
 								((JRadioButton) functions[a].getComponent(7)).setEnabled(((JRadioButton) functions[a].getComponent(7)).isSelected());
 							
 						}
 						else
 						{
 							for (int a=1;a<=16;a++)
 								((JRadioButton) functions[a].getComponent(7)).setEnabled(true);
 						}
 						if (atolow || atohigh)
 						{
 							for (int a=1;a<=16;a++)
 							{
 								JRadioButton AOn = (JRadioButton) functions[a].getComponent(3);
 								if (!AOn.isSelected())
 								{
 									AOn.setEnabled(false);
 								}
 							}
 						}
 						
 						atolow=false;
 						atohigh=false;
 						for (int a=1;a<=16;a++)
 						{
 							JRadioButton AOn = (JRadioButton) functions[a].getComponent(3);
 							if (AOn.isSelected())
 							{
 								JRadioButton jat=(JRadioButton) ATO[a].getComponent(1);
 								if (jat.isSelected())
 								{
 									atolow=true;
 									atohigh=true;
 								}
 								jat=(JRadioButton) ATO[a].getComponent(2);
 								if (jat.isSelected()) atolow=true;
 								jat=(JRadioButton) ATO[a].getComponent(3);
 								if (jat.isSelected()) atohigh=true;
 							}
 						}
 						aiport.getComponent(0).setEnabled(!atolow);
 						if (!atolow && atohigh)
 						{
 							JRadioButton jatc=(JRadioButton) aiport.getComponent(0);
 							jatc.setSelected(true);
 						}
 						aiport.getComponent(1).setEnabled(!atohigh);
 						if (atolow && !atohigh)
 						{
 							JRadioButton jatc=(JRadioButton) aiport.getComponent(1);
 							jatc.setSelected(true);
 						}
 						Buzzermods.getComponent(2).setEnabled(!atohigh);
 						
 					}
 
 					if (swindow.indexOf("Daylight Dimming Channel")==0)
 					{
 						if (relayexpansion==1)
 							prevwindow="Expansion Relay Box - Port 8";
 						else
 							prevwindow="Main Relay Box - Port 8";
 						if (((JRadioButton) daylightpwm.getComponent(2)).isSelected() || ((JRadioButton) daylightpwm.getComponent(3)).isSelected() || ((JRadioButton) daylightpwm.getComponent(4)).isSelected())
 						{
 							nextwindow="Actinic Dimming Channel";
 						}
 						else
 						{
 							CheckDaylightPWM();
 							nextwindow="Daylight Dimming Settings";
 						}
 					}
 
 					if (swindow.indexOf("Daylight Dimming Settings")==0)
 					{
 						prevwindow="Daylight Dimming Channel";
 						nextwindow="Actinic Dimming Channel";
 					}
 					
 					boolean actinicsettingsnext=false;
 					
 					if (swindow.indexOf("Actinic Dimming Channel")==0)
 					{
 						if (((JRadioButton) daylightpwm.getComponent(2)).isSelected() || ((JRadioButton) daylightpwm.getComponent(3)).isSelected() || ((JRadioButton) daylightpwm.getComponent(4)).isSelected())
 						{
 							prevwindow="Daylight Dimming Channel";
 						}
 						else
 						{
 							CheckDaylightPWM();
 							prevwindow="Daylight Dimming Settings";
 						}
 						if (((JRadioButton) actinicpwm.getComponent(2)).isSelected() || ((JRadioButton) actinicpwm.getComponent(3)).isSelected() || ((JRadioButton) actinicpwm.getComponent(4)).isSelected())
 						{
 							nextwindow="Dimming Expansion Channel 0";
 							actinicsettingsnext=true;
 						}
 						else
 						{
 							CheckActinicPWM();
 							nextwindow="Actinic Dimming Settings";
 						}
 					}	
 					
 					if (swindow.indexOf("Actinic Dimming Settings")==0)
 					{
 						prevwindow="Actinic Dimming Channel";
 						nextwindow="Dimming Expansion Channel 0";
 						actinicsettingsnext=true;
 					}
 
 					if (actinicsettingsnext)
 					{
 						JRadioButton jpwmbd = (JRadioButton) daylightpwm.getComponent(3);
 						JRadioButton jpwmba = (JRadioButton) actinicpwm.getComponent(3);
 						buzzer=jpwmba.isSelected() || jpwmbd.isSelected();
 						if (dimmingexpansion==0)
 							CheckNextAI();
 					}
 					
 					if (swindow.indexOf("Dimming Expansion Channel 0")==0)
 					{
 						if (((JRadioButton) actinicpwm.getComponent(2)).isSelected() || ((JRadioButton) actinicpwm.getComponent(3)).isSelected() || ((JRadioButton) actinicpwm.getComponent(4)).isSelected())
 						{
 							prevwindow="Actinic Dimming Channel";
 						}
 						else
 						{
 							CheckActinicPWM();
 							prevwindow="Actinic Dimming Settings";
 						}
 						if (((JRadioButton) exppwm[0].getComponent(2)).isSelected() || ((JRadioButton) exppwm[0].getComponent(3)).isSelected() || ((JRadioButton) exppwm[0].getComponent(4)).isSelected())
 						{
 							nextwindow="Dimming Expansion Channel 1";
 						}
 						else
 						{
 							CheckExpPWM();
 							nextwindow="Dimming Channel 0 Settings";
 						}
 					}
 
 					if (swindow.indexOf("Dimming Channel 0 Settings")==0)
 					{
 						prevwindow="Dimming Expansion Channel 0";
 						nextwindow="Dimming Expansion Channel 1";
 					}
 					
 					if (swindow.indexOf("Dimming Expansion Channel 1")==0)
 					{
 						if (((JRadioButton) exppwm[0].getComponent(2)).isSelected() || ((JRadioButton) exppwm[0].getComponent(3)).isSelected() || ((JRadioButton) exppwm[0].getComponent(4)).isSelected())
 						{
 							prevwindow="Dimming Expansion Channel 0";
 						}
 						else
 						{
 							CheckExpPWM();
 							prevwindow="Dimming Channel 0 Settings";
 						}
 						if (((JRadioButton) exppwm[1].getComponent(2)).isSelected() || ((JRadioButton) exppwm[1].getComponent(3)).isSelected() || ((JRadioButton) exppwm[1].getComponent(4)).isSelected())
 						{
 							nextwindow="Dimming Expansion Channel 2";
 						}
 						else
 						{
 							CheckExpPWM();
 							nextwindow="Dimming Channel 1 Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Dimming Channel 1 Settings")==0)
 					{
 						prevwindow="Dimming Expansion Channel 1";
 						nextwindow="Dimming Expansion Channel 2";
 					}
 					
 					if (swindow.indexOf("Dimming Expansion Channel 2")==0)
 					{
 						if (((JRadioButton) exppwm[1].getComponent(2)).isSelected() || ((JRadioButton) exppwm[1].getComponent(3)).isSelected() || ((JRadioButton) exppwm[1].getComponent(4)).isSelected())
 						{
 							prevwindow="Dimming Expansion Channel 1";
 						}
 						else
 						{
 							CheckExpPWM();
 							prevwindow="Dimming Channel 1 Settings";
 						}
 						if (((JRadioButton) exppwm[2].getComponent(2)).isSelected() || ((JRadioButton) exppwm[2].getComponent(3)).isSelected() || ((JRadioButton) exppwm[2].getComponent(4)).isSelected())
 						{
 							nextwindow="Dimming Expansion Channel 3";
 						}
 						else
 						{
 							CheckExpPWM();
 							nextwindow="Dimming Channel 2 Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Dimming Channel 2 Settings")==0)
 					{
 						prevwindow="Dimming Expansion Channel 2";
 						nextwindow="Dimming Expansion Channel 3";
 					}
 					
 					if (swindow.indexOf("Dimming Expansion Channel 3")==0)
 					{
 						if (((JRadioButton) exppwm[2].getComponent(2)).isSelected() || ((JRadioButton) exppwm[2].getComponent(3)).isSelected() || ((JRadioButton) exppwm[2].getComponent(4)).isSelected())
 						{
 							prevwindow="Dimming Expansion Channel 2";
 						}
 						else
 						{
 							CheckExpPWM();
 							prevwindow="Dimming Channel 2 Settings";
 						}
 						if (((JRadioButton) exppwm[3].getComponent(2)).isSelected() || ((JRadioButton) exppwm[3].getComponent(3)).isSelected() || ((JRadioButton) exppwm[3].getComponent(4)).isSelected())
 						{
 							nextwindow="Dimming Expansion Channel 4";
 						}
 						else
 						{
 							CheckExpPWM();
 							nextwindow="Dimming Channel 3 Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Dimming Channel 3 Settings")==0)
 					{
 						prevwindow="Dimming Expansion Channel 3";
 						nextwindow="Dimming Expansion Channel 4";
 					}
 										
 					if (swindow.indexOf("Dimming Expansion Channel 4")==0)
 					{
 						if (((JRadioButton) exppwm[3].getComponent(2)).isSelected() || ((JRadioButton) exppwm[3].getComponent(3)).isSelected() || ((JRadioButton) exppwm[3].getComponent(4)).isSelected())
 						{
 							prevwindow="Dimming Expansion Channel 3";
 						}
 						else
 						{
 							CheckExpPWM();
 							prevwindow="Dimming Channel 3 Settings";
 						}
 						if (((JRadioButton) exppwm[4].getComponent(2)).isSelected() || ((JRadioButton) exppwm[4].getComponent(3)).isSelected() || ((JRadioButton) exppwm[4].getComponent(4)).isSelected())
 						{
 							nextwindow="Dimming Expansion Channel 5";
 						}
 						else
 						{
 							CheckExpPWM();
 							nextwindow="Dimming Channel 4 Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Dimming Channel 4 Settings")==0)
 					{
 						prevwindow="Dimming Expansion Channel 4";
 						nextwindow="Dimming Expansion Channel 5";
 					}
 					
 					if (swindow.indexOf("Dimming Expansion Channel 5")==0)
 					{
 						if (((JRadioButton) exppwm[4].getComponent(2)).isSelected() || ((JRadioButton) exppwm[4].getComponent(3)).isSelected() || ((JRadioButton) exppwm[4].getComponent(4)).isSelected())
 						{
 							prevwindow="Dimming Expansion Channel 4";
 						}
 						else
 						{
 							CheckExpPWM();
 							prevwindow="Dimming Channel 4 Settings";
 						}
 						if (((JRadioButton) exppwm[5].getComponent(2)).isSelected() || ((JRadioButton) exppwm[5].getComponent(3)).isSelected() || ((JRadioButton) exppwm[5].getComponent(4)).isSelected())
 						{
 							JRadioButton jpwmbd = (JRadioButton) daylightpwm.getComponent(3);
 							JRadioButton jpwmba = (JRadioButton) actinicpwm.getComponent(3);
 							JRadioButton jpwmb0 = (JRadioButton) exppwm[0].getComponent(3);
 							JRadioButton jpwmb1 = (JRadioButton) exppwm[1].getComponent(3);
 							JRadioButton jpwmb2 = (JRadioButton) exppwm[2].getComponent(3);
 							JRadioButton jpwmb3 = (JRadioButton) exppwm[3].getComponent(3);
 							JRadioButton jpwmb4 = (JRadioButton) exppwm[4].getComponent(3);
 							JRadioButton jpwmb5 = (JRadioButton) exppwm[5].getComponent(3);
 							buzzer=jpwmba.isSelected() || jpwmbd.isSelected() || jpwmb0.isSelected() || jpwmb1.isSelected() || jpwmb2.isSelected() || jpwmb3.isSelected() || jpwmb4.isSelected() || jpwmb5.isSelected();
 							CheckNextAI();
 						}
 						else
 						{
 							CheckExpPWM();
 							nextwindow="Dimming Channel 5 Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Dimming Channel 5 Settings")==0)
 					{
 						prevwindow="Dimming Expansion Channel 5";
 						JRadioButton jpwmbd = (JRadioButton) daylightpwm.getComponent(3);
 						JRadioButton jpwmba = (JRadioButton) actinicpwm.getComponent(3);
 						JRadioButton jpwmb0 = (JRadioButton) exppwm[0].getComponent(3);
 						JRadioButton jpwmb1 = (JRadioButton) exppwm[1].getComponent(3);
 						JRadioButton jpwmb2 = (JRadioButton) exppwm[2].getComponent(3);
 						JRadioButton jpwmb3 = (JRadioButton) exppwm[3].getComponent(3);
 						JRadioButton jpwmb4 = (JRadioButton) exppwm[4].getComponent(3);
 						JRadioButton jpwmb5 = (JRadioButton) exppwm[5].getComponent(3);
 						buzzer=jpwmba.isSelected() || jpwmbd.isSelected() || jpwmb0.isSelected() || jpwmb1.isSelected() || jpwmb2.isSelected() || jpwmb3.isSelected() || jpwmb4.isSelected() || jpwmb5.isSelected();
 						CheckNextAI();
 					}
 
 					if (swindow.indexOf("Aqua Illumination Port")==0)
 					{
 						JRadioButton jah =(JRadioButton) aiport.getComponent(1);
 						if (jah.isSelected()) atohigh=true;
 						Buzzermods.getComponent(2).setEnabled(!atohigh);
 						CheckPrevDimming();
 						nextwindow="AI - White Channel";
 					}
 					
 					if (swindow.indexOf("AI - White Channel")==0)
 					{
 						prevwindow="Aqua Illumination Port";
 
 						if (((JRadioButton) aipwm[0].getComponent(2)).isSelected() || ((JRadioButton) aipwm[0].getComponent(3)).isSelected())
 						{
 							nextwindow="AI - Blue Channel";
 						}
 						else
 						{
 							CheckAIPWM();
 							nextwindow="AI - White Settings";
 						}
 					}					
 
 					if (swindow.indexOf("AI - White Settings")==0)
 					{
 						prevwindow="AI - White Channel";
 						nextwindow="AI - Blue Channel";
 					}
 
 					if (swindow.indexOf("AI - Blue Channel")==0)
 					{
 						if (((JRadioButton) aipwm[0].getComponent(2)).isSelected() || ((JRadioButton) aipwm[0].getComponent(3)).isSelected())
 						{
 							prevwindow="AI - White Channel";
 						}
 						else
 						{
 							CheckAIPWM();
 							prevwindow="AI - White Settings";
 						}
 						if (((JRadioButton) aipwm[1].getComponent(2)).isSelected() || ((JRadioButton) aipwm[1].getComponent(3)).isSelected())
 						{
 							nextwindow="AI - Royal Blue Channel";
 						}
 						else
 						{
 							CheckAIPWM();
 							nextwindow="AI - Blue Settings";
 						}
 					}					
 
 					if (swindow.indexOf("AI - Blue Settings")==0)
 					{
 						prevwindow="AI - Blue Channel";
 						nextwindow="AI - Royal Blue Channel";
 					}
 					
 
 					if (swindow.indexOf("AI - Royal Blue Channel")==0)
 					{
 						if (((JRadioButton) aipwm[1].getComponent(2)).isSelected() || ((JRadioButton) aipwm[1].getComponent(3)).isSelected())
 						{
 							prevwindow="AI - Blue Channel";
 						}
 						else
 						{
 							CheckAIPWM();
 							prevwindow="AI - Blue Settings";
 						}
 						if (((JRadioButton) aipwm[2].getComponent(2)).isSelected() || ((JRadioButton) aipwm[2].getComponent(3)).isSelected())
 						{
 							CheckNextRF();
 						}
 						else
 						{
 							CheckAIPWM();
 							nextwindow="AI - Royal Blue Settings";
 						}
 					}					
 
 					if (swindow.indexOf("AI - Royal Blue Settings")==0)
 					{
 						prevwindow="AI - Royal Blue Channel";
 						CheckNextRF();
 					}
 					
 					if (swindow.indexOf("Vortech Mode")==0)
 					{
 						
 						CheckPrevAI();
 						nextwindow="Radion White Channel";
 					}
 					
 					if (swindow.indexOf("Radion White Channel")==0)
 					{
 						prevwindow="Vortech Mode";
 						if (((JRadioButton) rfpwm[0].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[0].getComponent(3)).isSelected())
 						{
 							nextwindow="Radion Royal Blue Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							nextwindow="Radion White Settings";
 						}
 					}
 
 					if (swindow.indexOf("Radion White Settings")==0)
 					{
 						prevwindow="Radion White Channel";
 						nextwindow="Radion Royal Blue Channel";
 					}
 					
 					if (swindow.indexOf("Radion Royal Blue Channel")==0)
 					{
 						if (((JRadioButton) rfpwm[0].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[0].getComponent(3)).isSelected())
 						{
 							prevwindow="Radion White Channel";
 						}
 						else
 						{
 							CheckExpPWM();
 							prevwindow="Radion White Settings";
 						}
 						if (((JRadioButton) rfpwm[1].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[1].getComponent(3)).isSelected())
 						{
 							nextwindow="Radion Red Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							nextwindow="Radion Royal Blue Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Radion Royal Blue Settings")==0)
 					{
 						prevwindow="Radion Royal Blue Channel";
 						nextwindow="Radion Red Channel";
 					}
 					
 					if (swindow.indexOf("Radion Red Channel")==0)
 					{
 						if (((JRadioButton) rfpwm[1].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[1].getComponent(3)).isSelected())
 						{
 							prevwindow="Radion Royal Blue Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							prevwindow="Radion Royal Blue Settings";
 						}
 						if (((JRadioButton) rfpwm[2].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[2].getComponent(3)).isSelected())
 						{
 							nextwindow="Radion Green Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							nextwindow="Radion Red Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Radion Red Settings")==0)
 					{
 						prevwindow="Radion Red Channel";
 						nextwindow="Radion Green Channel";
 					}
 					
 					if (swindow.indexOf("Radion Green Channel")==0)
 					{
 						if (((JRadioButton) rfpwm[2].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[2].getComponent(3)).isSelected())
 						{
 							prevwindow="Radion Red Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							prevwindow="Radion Red Settings";
 						}
 						if (((JRadioButton) rfpwm[3].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[3].getComponent(3)).isSelected())
 						{
 							nextwindow="Radion Blue Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							nextwindow="Radion Green Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Radion Green Settings")==0)
 					{
 						prevwindow="Radion Green Channel";
 						nextwindow="Radion Blue Channel";
 					}
 										
 					if (swindow.indexOf("Radion Blue Channel")==0)
 					{
 						if (((JRadioButton) rfpwm[3].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[3].getComponent(3)).isSelected())
 						{
 							prevwindow="Radion Green Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							prevwindow="Radion Green Settings";
 						}
 						if (((JRadioButton) rfpwm[4].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[4].getComponent(3)).isSelected())
 						{
 							nextwindow="Radion Intensity Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							nextwindow="Radion Blue Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Radion Blue Settings")==0)
 					{
 						prevwindow="Radion Blue Channel";
 						nextwindow="Radion Intensity Channel";
 					}
 					
 					if (swindow.indexOf("Radion Intensity Channel")==0)
 					{
 						if (((JRadioButton) rfpwm[4].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[4].getComponent(3)).isSelected())
 						{
 							prevwindow="Radion Blue Channel";
 						}
 						else
 						{
 							CheckRFPWM();
 							prevwindow="Radion Blue Settings";
 						}
 						if (((JRadioButton) rfpwm[5].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[5].getComponent(3)).isSelected())
 						{
 							CheckNextWifi();
 						}
 						else
 						{
 							CheckRFPWM();
 							nextwindow="Radion Intensity Settings";
 						}
 					}					
 
 					if (swindow.indexOf("Radion Intensity Settings")==0)
 					{
 						prevwindow="Radion Intensity Channel";
 						CheckNextWifi();
 					}
 					
 					if (swindow.indexOf("Wifi Attachment")==0)
 					{
 						CheckPrevRF();
 						CheckNextBuzzer();
 					}					
 					
 					if (swindow.indexOf("Buzzer")==0)
 					{
 						CheckPrevWifi();
 						nextwindow="Ready to Generate";
 						JRadioButton j = (JRadioButton) memsettings.getComponent(1);
 						if (j.isSelected()) nextwindow="Upload Memory Settings";
 
 					}
 					
 					if (swindow.indexOf("Upload Memory Settings")==0)
 					{
 						CheckPrevBuzzer();
 						nextwindow="Uploading Memory Settings";
 					}
 	
 					if (swindow.indexOf("Ready to Generate")==0)
 					{
 						JRadioButton j = (JRadioButton) memsettings.getComponent(0);
 						if (j.isSelected())
 							CheckPrevBuzzer();						
 						else
 							prevwindow="Upload Memory Settings";
 						nextwindow="Uploading Code";
 					}
 					
 					if (aButton.getText()=="Cancel" || aButton.getText()=="Exit")
 					{
 						ReturnLF();
 						saveInitialValues();
 						frame.setVisible(false);
 					}
 					else if (aButton.getText()=="Prev")
 					{
 						cl.show(insidePanel, prevwindow);
 						Title.SetText(prevwindow);
 					}
 					else if (aButton.getText()=="Next")
 					{
 						cl.show(insidePanel, nextwindow);
 						Title.SetText(nextwindow);
 					}
 					else if (aButton.getText()=="Skip")
 					{
 						nextwindow="Ready to Generate";
 						cl.show(insidePanel, nextwindow);
 						Title.SetText(nextwindow);
 					}
 					else if (aButton.getText()=="Generate Only")
 					{
 						ConstructCode();
 						ReturnLF();
 						saveInitialValues();
 						frame.setVisible(false);						
 					}
 					else if (aButton.getText()=="Generate and Upload")
 					{
 						if (nextwindow=="Uploading Memory Settings")
 						{
 							ConstructMemCode();
 							class RAUploader extends SwingWorker<String, Object> {
 								@Override
 								public String doInBackground() {
 									editor.RAhandleExport();
 									Thread t = new Thread(editor.RAexportHandler);
 									t.start();
 									try { 
 										t.join(); 
 									} catch (InterruptedException e) { 
 										e.printStackTrace(); 
 									} 
 									return "";
 								}
 
 								@Override
 								protected void done() {
 									try {
 										dialog.dispose();
 									} catch (Exception ignore) {
 									}
 								}
 							}
 
 							(new RAUploader()).execute();
 
 							ShowStep2();
 							
 							if (editor.status.message == "Done uploading.")
 							{
 								nextwindow="Ready to Generate";
 								cl.show(insidePanel, nextwindow);
 								Title.SetText(nextwindow);
 							}
 							else
 								JOptionPane.showMessageDialog(null,new JLabel("<html>An error has occured.<br>Please try again.</html>") , "Error", JOptionPane.ERROR_MESSAGE);							
 						}
 						else if (nextwindow=="Uploading Code")
 						{
 							ConstructCode();
 							class RAUploader extends SwingWorker<String, Object> {
 								@Override
 								public String doInBackground() {
 									editor.RAhandleExport();
 									Thread t = new Thread(editor.RAexportHandler);
 									t.start();
 									try { 
 										t.join(); 
 									} catch (InterruptedException e) { 
 										e.printStackTrace(); 
 									} 
 									return "";
 								}
 
 								@Override
 								protected void done() {
 									try {
 										dialog.dispose();
 									} catch (Exception ignore) {
 									}
 								}
 							}
 
 							(new RAUploader()).execute();
 
 							ShowStep2();
 							
 							if (editor.status.message == "Done uploading.")
 							{
 								nextwindow="Done";
 								cl.show(insidePanel, nextwindow);
 								Title.SetText(nextwindow);
 							}
 							else
 								JOptionPane.showMessageDialog(null,new JLabel("<html>An error has occured.<br>Please try again.</html>") , "Error", JOptionPane.ERROR_MESSAGE);							
 							
 						}
 						else
 						{
 							cl.show(insidePanel, nextwindow);
 							Title.SetText(nextwindow);
 						}						
 					}
 					Title.repaint();
 					
 					if (Title.GetText().indexOf("Upload Memory Settings")==0 || Title.GetText().indexOf("Ready to Generate")==0)
 					{
 						JPanel j = (JPanel) Footer.getComponent(0);
 						JButton j1 = (JButton) j.getComponent(2);
 						j1.setText("Generate and Upload");
 						j.getComponent(3).setVisible(true);
 						if (Title.GetText().indexOf("Upload Memory Settings")==0)
 							((JButton) j.getComponent(3)).setText("Skip");
 						else
 							((JButton) j.getComponent(3)).setText("Generate Only");
 					}
 					else if (Title.GetText().indexOf("Done")==0)
 					{
 						JPanel j = (JPanel) Footer.getComponent(0);
 						JButton j1 = (JButton) j.getComponent(0);
 						j1.setText("Exit");
 						j.getComponent(1).setVisible(false);
 						j.getComponent(2).setVisible(false);
 						j.getComponent(3).setVisible(false);
 					}
 					else
 					{
 						JPanel j = (JPanel) Footer.getComponent(0);
 						JButton j1 = (JButton) j.getComponent(2);
 						j1.setText("Next");
 						j.getComponent(3).setVisible(false);
 					}
 					
 					CheckNav();
 				}
 			};
 
 			ImageIcon cancelicon = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/cancel.png");
 			ImageIcon previcon = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/arrow_back.png");
 			ImageIcon nexticon = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/arrow_next.png");
 			JButton Cancel = new JButton("Cancel",cancelicon);
 			Cancel.addActionListener(ButtonActionListener);
 			JButton Prev = new JButton("Prev",previcon);
 			Prev.addActionListener(ButtonActionListener);
 			JButton Next = new JButton("Next",nexticon);
 			Next.setHorizontalTextPosition(SwingConstants.LEFT);
 			Next.addActionListener(ButtonActionListener);
 			JButton Generate = new JButton("Generate Only",nexticon);
 			Generate.setHorizontalTextPosition(SwingConstants.LEFT);
 			Generate.addActionListener(ButtonActionListener);
 			Generate.setVisible(false);
 
 			footer.add(Cancel,"Cancel");
 			footer.add(Prev,"Prev");
 			footer.add(Next,"Next");			
 			footer.add(Generate,"Generate");			
 		}
 	}
 
 	
 	public void CheckPrevDimming()
 	{
 		if (dimmingexpansion==0)
 		{
 			if (((JRadioButton) actinicpwm.getComponent(2)).isSelected() || ((JRadioButton) actinicpwm.getComponent(3)).isSelected() || ((JRadioButton) actinicpwm.getComponent(4)).isSelected())
 			{
 				prevwindow="Actinic Dimming Channel";
 			}
 			else
 			{
 				CheckActinicPWM();
 				prevwindow="Actinic Dimming Settings";
 			}
 		}
 		else
 		{
 			if (((JRadioButton) exppwm[5].getComponent(2)).isSelected() || ((JRadioButton) exppwm[5].getComponent(3)).isSelected() || ((JRadioButton) exppwm[5].getComponent(4)).isSelected())
 			{
 				prevwindow="Dimming Expansion Channel 5";
 
 			}
 			else
 			{
 				CheckExpPWM();
 				prevwindow="Dimming Channel 5 Settings";
 			}
 		}		
 	}
 	
 	public void CheckPrevAI()
 	{
 		if (ailed==0)
 		{
 			CheckPrevDimming();
 		}
 		else
 		{
 			if (((JRadioButton) aipwm[2].getComponent(2)).isSelected() || ((JRadioButton) aipwm[2].getComponent(3)).isSelected())
 			{
 				prevwindow="AI - Royal Blue Channel";
 			}		
 			else
 			{
 				CheckAIPWM();
 				prevwindow="AI - Royal Blue Settings";
 			}
 		}
 		
 	}
 	
 	public void CheckNextAI()
 	{
 		nextwindow="Aqua Illumination Port";
 		if (ailed==0)
 			CheckNextRF();
 	}
 	
 	public void CheckNextRF()
 	{
 		nextwindow="Vortech Mode";
 		if (rfexpansion==0)
 			CheckNextWifi();
 	}
 	
 	public void CheckPrevRF()
 	{
 		if (rfexpansion==0)
 		{
 			CheckPrevAI();
 		}
 		else
 		{
 			if (((JRadioButton) rfpwm[5].getComponent(2)).isSelected() || ((JRadioButton) rfpwm[5].getComponent(3)).isSelected())
 			{
 				prevwindow="Radion Intensity Channel";
 			}		
 			else
 			{
 				CheckRFPWM();
 				prevwindow="Radion Intensity Settings";
 			}							
 		}		
 	}
 	
 	public void CheckNextWifi()
 	{
 		nextwindow="Wifi Attachment";
 		if (wifi==0) 
 			CheckNextBuzzer();		
 	}
 	
 	public void CheckPrevWifi()
 	{
 		if (wifi==0)
 		{
 			CheckPrevRF();
 		}		
 		else
 		{
 			prevwindow="Wifi Attachment";
 		}		
 	}
 	
 	public void CheckNextBuzzer()
 	{
 		nextwindow="Buzzer";
 		if (buzzer==false)
 		{
 			nextwindow="Ready to Generate";
 			JRadioButton j = (JRadioButton) memsettings.getComponent(1);
 			if (j.isSelected()) nextwindow="Upload Memory Settings";
 		}
 	}
 	
 	public void CheckPrevBuzzer()
 	{
 		if (buzzer==false)
 			CheckPrevWifi();											
 		else
 			prevwindow="Buzzer";
 	}
 	
 	public class Inside extends JPanel {
 		JCustomEditorPane t;
 		
 		public Inside(String s)
 		{
 			super();
 			this.setLayout(new BoxLayout( this, BoxLayout.PAGE_AXIS));
 			this.setOpaque(false);
 			t = new JCustomEditorPane(s);
 			this.add(t);
 		}
 		public void SetText(String s)
 		{
 			t.setText(s);
 		}
 		public String GetText()
 		{
 			return t.getText();
 		}
 	}	
 
 	public class TitleLabel extends JPanel {
 
 		JLabel t;
 		String s="";
 		public TitleLabel()
 		{
 			this.setLayout(new BorderLayout());
 			this.setOpaque(false);
 			JPanel inside = new JPanel();
 			inside.setOpaque(false);
 			t = new JLabel();
 			Font font = t.getFont().deriveFont(Font.BOLD, 40.0f);
 			t.setFont(font);
 			t.setForeground(new Color(51,0,153,100));
 			this.add(BorderLayout.NORTH, inside);
 			inside.add(t);
 		}
 		public void SetText(String s)
 		{
 			this.s=s+" ";
 			t.setText(s+" ");
 		}
 		public String GetText()
 		{
 			return this.s;
 		}
 		public void paintComponent(Graphics g) {
 			Color blender=new Color(0, 0, 0, 5);
 			super.paintComponent(g);
 
 
 			GraphicsEnvironment.getLocalGraphicsEnvironment();
 			Graphics2D g2 = (Graphics2D) g;
 			int width = getWidth();
 			int height = getHeight();
 			Paint storedPaint = g2.getPaint();
 			g2.setPaint(new GradientPaint(0, height, new Color(255, 255, 255, 30), 0, 0, new Color(0, 0, 0, 20)));
 			g2.setFont(new Font("LucidaSans", Font.BOLD, 100));
 			g2.drawString(this.s, 100, 40);
 			g2.setPaint(storedPaint);
 		}
 		public void repaint(){
 			super.repaint();
 		}
 	}
 
 	public static class Preferences {
 
 		  static Hashtable table = new Hashtable();
 		  static File preferencesFile=new File (Base.getSketchbookFolder().getPath() +"/tools/Wizard/data/wizard.ini");
 		  
 		  static protected void init(String commandLinePrefs) {
 
 			  try {
 				  load(new FileInputStream(preferencesFile));
 			  } catch (Exception e) {
 			  }
 
 			  String platformExt = "." + PConstants.platformNames[PApplet.platform];
 			  int platformExtLength = platformExt.length();
 			  Enumeration e = table.keys();
 			  while (e.hasMoreElements()) {
 				  String key = (String) e.nextElement();
 				  if (key.endsWith(platformExt)) {
 					  // this is a key specific to a particular platform
 					  String actualKey = key.substring(0, key.length() - platformExtLength);
 					  String value = get(key);
 					  table.put(actualKey, value);
 				  }
 			  }
 		  }    
 			  
 		  
 		  static protected void load(InputStream input) throws IOException {
 			  load(input, table);
 		  }
 
 		  static public void load(InputStream input, Map table) throws IOException {  
 			  String[] lines = PApplet.loadStrings(input);  // Reads as UTF-8
 			  for (String line : lines) {
 				  if ((line.length() == 0) ||
 						  (line.charAt(0) == '#')) continue;
 
 				  // this won't properly handle = signs being in the text
 				  int equals = line.indexOf('=');
 				  if (equals != -1) {
 					  String key = line.substring(0, equals).trim();
 					  String value = line.substring(equals + 1).trim();
 					  table.put(key, value);
 				  }
 			  }
 		  }
 
 		  static protected void save() {
 
 			  if (preferencesFile == null) return;
 
 			  // Fix for 0163 to properly use Unicode when writing preferences.txt
 			  PrintWriter writer = PApplet.createWriter(preferencesFile);
 
 			  Enumeration e = table.keys(); //properties.propertyNames();
 			  while (e.hasMoreElements()) {
 				  String key = (String) e.nextElement();
 				  writer.println(key + "=" + ((String) table.get(key)));
 			  }
 
 			  writer.flush();
 			  writer.close();
 		  }
 
 
 		  static public String get(String attribute /*, String defaultValue */) {
 			  return (String) table.get(attribute);
 		  }		  
 
 		  static public void set(String attribute, String value) {
 			  table.put(attribute, value);
 		  }
 
 		  static public boolean getBoolean(String attribute) {
 			  String value = get(attribute); //, null);
 			  return (new Boolean(value)).booleanValue();
 		  }
 
 
 		  static public void setBoolean(String attribute, boolean value) {
 			  set(attribute, value ? "true" : "false");
 		  }		 
 		  
 		  static public int getInteger(String attribute /*, int defaultValue*/) {
 			  return Integer.parseInt(get(attribute));
 		  }
 
 
 		  static public void setInteger(String key, int value) {
 			  set(key, String.valueOf(value));
 		  }		  
 
 		  static public Double getDouble(String attribute /*, int defaultValue*/) {
 			  return Double.parseDouble(get(attribute));
 		  }
 
 
 		  static public void setDouble(String key, Double value) {
 			  set(key, String.valueOf(value));
 		  }				  
 	}
 	public void loadInitialValues()
 	{
 		setSelected(memsettings,"memsettings");
 		setSelected(disptemp,"disptemp");
 		setDouble(Overheat,"Overheat");
 		relayexpansion=setChecked(expansionmods.getComponent(0),"relayexpansion");
 		dimmingexpansion=setChecked(expansionmods.getComponent(1),"dimmingexpansion");
 		rfexpansion=setChecked(expansionmods.getComponent(2),"rfexpansion");
 		salinityexpansion=setChecked(expansionmods.getComponent(3),"salinityexpansion");
 		ioexpansion=setChecked(expansionmods.getComponent(4),"ioexpansion");
 		orpexpansion=setChecked(expansionmods.getComponent(5),"orpexpansion");
 		phexpansion=setChecked(expansionmods.getComponent(6),"phexpansion");
 		waterlevelexpansion=setChecked(expansionmods.getComponent(7),"waterlevelexpansion");
 		wifi=setChecked(attachmentmods.getComponent(0),"wifi");
 		ailed=setChecked(attachmentmods.getComponent(1),"ailed");
 		for (int a=1;a<16;a++)
 		{
 			if (a<=8) Title.SetText ("Main Relay Box - Port "+a);
 			if (a>8) Title.SetText ("Expansion Relay Box - Port "+(a-8));
 			setSelected(functions[a],"functions"+a);
 			if (Preferences.getBoolean("portfeeding"+a)) ((JCheckBox)ports[a].getComponent(0)).setSelected(true);
 			if (Preferences.getBoolean("portwaterchange"+a)) ((JCheckBox)ports[a].getComponent(1)).setSelected(true);
 			if (Preferences.getBoolean("portlights"+a)) ((JCheckBox)ports[a].getComponent(2)).setSelected(true);
 			if (Preferences.getBoolean("portoverheat"+a)) ((JCheckBox)ports[a].getComponent(3)).setSelected(true);
 			setTime((JSpinner)Timed[a].getComponent(2),"TimedOn"+a);
 			setTime((JSpinner)Timed[a].getComponent(4),"TimedOff"+a);
 			setInteger((JSpinner)Timed[a].getComponent(6),"TimedDelayed"+a);
 			setSelected(TimedMemory[a],"TimedMemory"+a);
 			setTime((JSpinner)TimedMemorySettings[a].getComponent(2),"TimedMemorySettingsOn"+a);
 			setTime((JSpinner)TimedMemorySettings[a].getComponent(4),"TimedMemorySettingsOff"+a);
 			setInteger((JSpinner)TimedMemorySettings[a].getComponent(6),"TimedMemorySettingsDelayed"+a);
 			setInteger((JSpinner)TimedMemorySettings[a].getComponent(8),"TimedMemorySettingsActinicOffset"+a);
 			setDouble((JSpinner)Heater[a].getComponent(2),"HeaterOn"+a);
 			setDouble((JSpinner)Heater[a].getComponent(4),"HeaterOff"+a);
 			setDouble((JSpinner)Chiller[a].getComponent(2),"ChillerOn"+a);
 			setDouble((JSpinner)Chiller[a].getComponent(4),"ChillerOff"+a);
 			setSelected(ATO[a],"ATO"+a);
 			setInteger((JSpinner)ATO[a].getComponent(5),"ATOTimeout"+a);
 			setSelected(WM[a],"WM"+a);
 			setInteger((JSpinner)WM[a].getComponent(4),"WMTimer"+a);
 			setInteger((JSpinner)WM[a].getComponent(6),"WMTimerR"+a);
 			setDouble((JSpinner)CO2Control[a].getComponent(2),"CO2ControlOn"+a);
 			setDouble((JSpinner)CO2Control[a].getComponent(4),"CO2ControlOff"+a);
 			setDouble((JSpinner)pHControl[a].getComponent(2),"pHControlOn"+a);
 			setDouble((JSpinner)pHControl[a].getComponent(4),"pHControlOff"+a);
 			setInteger((JSpinner)Dosing[a].getComponent(2),"DPInterval"+a);
 			setInteger((JSpinner)Dosing[a].getComponent(4),"DPTimer"+a);
 			setInteger((JSpinner)Dosing[a].getComponent(6),"DPOffset"+a);
 			setInteger((JSpinner)Delayed[a].getComponent(2),"Delayed"+a);
 			if (Preferences.get("Opposite"+a)!=null) ((JComboBox)Opposite[a].getComponent(2)).setSelectedIndex(Preferences.getInteger("Opposite"+a));
 		}		
 		setSelected(daylightpwm,"daylightpwm");
 		setTime((JSpinner)daylightpwmsettings.getComponent(2),"daylightpwmon");
 		setTime((JSpinner)daylightpwmsettings.getComponent(4),"daylightpwmoff");
 		setInteger((JSpinner)daylightpwmsettings.getComponent(8),"daylightpwmstart");
 		setInteger((JSpinner)daylightpwmsettings.getComponent(10),"daylightpwmend");
 		setInteger((JSpinner)daylightpwmsettings.getComponent(14),"daylightpwmduration");
 		setSelected(actinicpwm,"actinicpwm");
 		setTime((JSpinner)actinicpwmsettings.getComponent(2),"actinicpwmon");
 		setTime((JSpinner)actinicpwmsettings.getComponent(4),"actinicpwmoff");
 		setInteger((JSpinner)actinicpwmsettings.getComponent(8),"actinicpwmstart");
 		setInteger((JSpinner)actinicpwmsettings.getComponent(10),"actinicpwmend");
 		setInteger((JSpinner)actinicpwmsettings.getComponent(14),"actinicpwmduration");
 		for (int a=0;a<9;a++)
 			if (Preferences.getBoolean("buzzermod"+a)) ((JCheckBox)Buzzermods.getComponent(a)).setSelected(true);
 		for (int a=0;a<6;a++)
 		{
 			setSelected(exppwm[a],"exppwm"+a);
 			setTime((JSpinner)exppwmsettings[a].getComponent(2),"exppwm"+a+"on");
 			setTime((JSpinner)exppwmsettings[a].getComponent(4),"exppwm"+a+"off");
 			setInteger((JSpinner)exppwmsettings[a].getComponent(8),"exppwm"+a+"start");
 			setInteger((JSpinner)exppwmsettings[a].getComponent(10),"exppwm"+a+"end");
 			setInteger((JSpinner)exppwmsettings[a].getComponent(14),"exppwm"+a+"duration");
 		}
 		setSelected(aiport,"aiport");
 		for (int a=0;a<3;a++)
 		{
 			setSelected(aipwm[a],"aipwm"+a);
 			setTime((JSpinner)aipwmsettings[a].getComponent(2),"aipwm"+a+"on");
 			setTime((JSpinner)aipwmsettings[a].getComponent(4),"aipwm"+a+"off");
 			setInteger((JSpinner)aipwmsettings[a].getComponent(8),"aipwm"+a+"start");
 			setInteger((JSpinner)aipwmsettings[a].getComponent(10),"aipwm"+a+"end");
 			setInteger((JSpinner)aipwmsettings[a].getComponent(14),"aipwm"+a+"duration");
 		}
 		if (Preferences.get("RFmodsMode")!=null) ((JComboBox)RFmods.getComponent(1)).setSelectedIndex(Preferences.getInteger("RFmodsMode"));
 		setInteger((JSpinner)RFmods.getComponent(3),"RFmodsSpeed");
 		setInteger((JSpinner)RFmods.getComponent(5),"RFmodsDuration");
 		for (int a=0;a<6;a++)
 		{
 			setSelected(rfpwm[a],"rfpwm"+a);
 			setTime((JSpinner)rfpwmsettings[a].getComponent(2),"rfpwm"+a+"on");
 			setTime((JSpinner)rfpwmsettings[a].getComponent(4),"rfpwm"+a+"off");
 			setInteger((JSpinner)rfpwmsettings[a].getComponent(8),"rfpwm"+a+"start");
 			setInteger((JSpinner)rfpwmsettings[a].getComponent(10),"rfpwm"+a+"end");
 			setInteger((JSpinner)rfpwmsettings[a].getComponent(14),"rfpwm"+a+"duration");
 		}
 		((JTextField) wifiportal.getComponent(1)).setText(Preferences.get("wifiportal"));
 		Title.SetText("Memory Settings");
 	}
 	
 	public void saveInitialValues()
 	{
 		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
 		 
 		Preferences.setInteger("memsettings", getSelected(memsettings));
 		Preferences.setInteger("disptemp", getSelected(disptemp));
 		Preferences.setDouble("Overheat",(Double)Overheat.getValue());
 		Preferences.setInteger("relayexpansion", relayexpansion);
 		Preferences.setInteger("dimmingexpansion", dimmingexpansion);
 		Preferences.setInteger("rfexpansion", rfexpansion);
 		Preferences.setInteger("salinityexpansion", salinityexpansion);
 		Preferences.setInteger("ioexpansion", ioexpansion);
 		Preferences.setInteger("orpexpansion", orpexpansion);
 		Preferences.setInteger("phexpansion", phexpansion);
 		Preferences.setInteger("waterlevelexpansion", waterlevelexpansion);
 		Preferences.setInteger("wifi", wifi);
 		Preferences.setInteger("ailed", ailed);
 		for (int a=1;a<16;a++)
 		{
 			Preferences.setInteger("functions"+a, getSelected(functions[a]));
 			Preferences.setBoolean("portfeeding"+a, ((JCheckBox)ports[a].getComponent(0)).isSelected());
 			Preferences.setBoolean("portwaterchange"+a, ((JCheckBox)ports[a].getComponent(1)).isSelected());
 			Preferences.setBoolean("portlights"+a, ((JCheckBox)ports[a].getComponent(2)).isSelected());
 			Preferences.setBoolean("portoverheat"+a, ((JCheckBox)ports[a].getComponent(3)).isSelected());
 			Preferences.set("TimedOn"+a, formatter.format((Date)((JSpinner)Timed[a].getComponent(2)).getValue()));
 			Preferences.set("TimedOff"+a, formatter.format((Date)((JSpinner)Timed[a].getComponent(4)).getValue()));
 			Preferences.set("TimedDelayed"+a, ((JSpinner)Timed[a].getComponent(6)).getValue().toString());
 			Preferences.setInteger("TimedMemory"+a, getSelected(TimedMemory[a]));
 			Preferences.set("TimedMemorySettingsOn"+a, formatter.format((Date)((JSpinner)TimedMemorySettings[a].getComponent(2)).getValue()));
 			Preferences.set("TimedMemorySettingsOff"+a, formatter.format((Date)((JSpinner)TimedMemorySettings[a].getComponent(4)).getValue()));
 			Preferences.set("TimedMemorySettingsDelayed"+a, ((JSpinner)TimedMemorySettings[a].getComponent(6)).getValue().toString());
 			Preferences.set("TimedMemorySettingsActinicOffset"+a, ((JSpinner)TimedMemorySettings[a].getComponent(8)).getValue().toString());
 			Preferences.set("HeaterOn"+a, ((JSpinner)Heater[a].getComponent(2)).getValue().toString());
 			Preferences.set("HeaterOff"+a, ((JSpinner)Heater[a].getComponent(4)).getValue().toString());
 			Preferences.set("ChillerOn"+a, ((JSpinner)Chiller[a].getComponent(2)).getValue().toString());
 			Preferences.set("ChillerOff"+a, ((JSpinner)Chiller[a].getComponent(4)).getValue().toString());
 			Preferences.setInteger("ATO"+a, getSelected(ATO[a]));
 			Preferences.set("ATOTimeout"+a, ((JSpinner)ATO[a].getComponent(5)).getValue().toString());
 			Preferences.setInteger("WM"+a, getSelected(WM[a]));
 			Preferences.set("WMTimer"+a, ((JSpinner)WM[a].getComponent(4)).getValue().toString());
 			Preferences.set("WMTimerR"+a, ((JSpinner)WM[a].getComponent(6)).getValue().toString());
 			Preferences.set("CO2ControlOn"+a, ((JSpinner)CO2Control[a].getComponent(2)).getValue().toString());
 			Preferences.set("CO2ControlOff"+a, ((JSpinner)CO2Control[a].getComponent(4)).getValue().toString());
 			Preferences.set("pHControlOn"+a, ((JSpinner)pHControl[a].getComponent(2)).getValue().toString());
 			Preferences.set("pHControlOff"+a, ((JSpinner)pHControl[a].getComponent(4)).getValue().toString());
 			Preferences.set("DPInterval"+a, ((JSpinner)Dosing[a].getComponent(2)).getValue().toString());
 			Preferences.set("DPTimer"+a, ((JSpinner)Dosing[a].getComponent(4)).getValue().toString());
 			Preferences.set("DPOffset"+a, ((JSpinner)Dosing[a].getComponent(6)).getValue().toString());
 			Preferences.set("Delayed"+a, ((JSpinner)Delayed[a].getComponent(2)).getValue().toString());
 			Preferences.setInteger("Opposite"+a, ((JComboBox)Opposite[a].getComponent(2)).getSelectedIndex());
 		}
 		Preferences.setInteger("daylightpwm", getSelected(daylightpwm));
 		Preferences.set("daylightpwmon", formatter.format((Date)((JSpinner)daylightpwmsettings.getComponent(2)).getValue()));
 		Preferences.set("daylightpwmoff", formatter.format((Date)((JSpinner)daylightpwmsettings.getComponent(4)).getValue()));
 		Preferences.set("daylightpwmstart", ((JSpinner)daylightpwmsettings.getComponent(8)).getValue().toString());
 		Preferences.set("daylightpwmend", ((JSpinner)daylightpwmsettings.getComponent(10)).getValue().toString());
 		Preferences.set("daylightpwmduration", ((JSpinner)daylightpwmsettings.getComponent(14)).getValue().toString());
 		Preferences.setInteger("actinicpwm", getSelected(actinicpwm));
 		Preferences.set("actinicpwmon", formatter.format((Date)((JSpinner)actinicpwmsettings.getComponent(2)).getValue()));
 		Preferences.set("actinicpwmoff", formatter.format((Date)((JSpinner)actinicpwmsettings.getComponent(4)).getValue()));
 		Preferences.set("actinicpwmstart", ((JSpinner)actinicpwmsettings.getComponent(8)).getValue().toString());
 		Preferences.set("actinicpwmend", ((JSpinner)actinicpwmsettings.getComponent(10)).getValue().toString());
 		Preferences.set("actinicpwmduration", ((JSpinner)actinicpwmsettings.getComponent(14)).getValue().toString());
 		for (int a=0;a<9;a++)
 			Preferences.setBoolean("buzzermod"+a,((JCheckBox)Buzzermods.getComponent(a)).isSelected());
 		for (int a=0;a<6;a++)
 		{
 			Preferences.setInteger("exppwm"+a, getSelected(exppwm[a]));
 			Preferences.set("exppwm"+a+"on", formatter.format((Date)((JSpinner)exppwmsettings[a].getComponent(2)).getValue()));
 			Preferences.set("exppwm"+a+"off", formatter.format((Date)((JSpinner)exppwmsettings[a].getComponent(4)).getValue()));
 			Preferences.set("exppwm"+a+"start", ((JSpinner)exppwmsettings[a].getComponent(8)).getValue().toString());
 			Preferences.set("exppwm"+a+"end", ((JSpinner)exppwmsettings[a].getComponent(10)).getValue().toString());
 			Preferences.set("exppwm"+a+"duration", ((JSpinner)exppwmsettings[a].getComponent(14)).getValue().toString());
 		}
 		Preferences.setInteger("aiport", getSelected(aiport));
 		for (int a=0;a<3;a++)
 		{
 			Preferences.setInteger("aipwm"+a, getSelected(aipwm[a]));
 			Preferences.set("aipwm"+a+"on", formatter.format((Date)((JSpinner)aipwmsettings[a].getComponent(2)).getValue()));
 			Preferences.set("aipwm"+a+"off", formatter.format((Date)((JSpinner)aipwmsettings[a].getComponent(4)).getValue()));
 			Preferences.set("aipwm"+a+"start", ((JSpinner)aipwmsettings[a].getComponent(8)).getValue().toString());
 			Preferences.set("aipwm"+a+"end", ((JSpinner)aipwmsettings[a].getComponent(10)).getValue().toString());
 			Preferences.set("aipwm"+a+"duration", ((JSpinner)aipwmsettings[a].getComponent(14)).getValue().toString());
 		}
 		Preferences.setInteger("RFmodsMode", ((JComboBox)RFmods.getComponent(1)).getSelectedIndex());
 		Preferences.set("RFmodsSpeed", ((JSpinner)RFmods.getComponent(3)).getValue().toString());
 		Preferences.set("RFmodsDuration", ((JSpinner)RFmods.getComponent(5)).getValue().toString());
 		for (int a=0;a<6;a++)
 		{
 			Preferences.setInteger("rfpwm"+a, getSelected(rfpwm[a]));
 			Preferences.set("rfpwm"+a+"on", formatter.format((Date)((JSpinner)rfpwmsettings[a].getComponent(2)).getValue()));
 			Preferences.set("rfpwm"+a+"off", formatter.format((Date)((JSpinner)rfpwmsettings[a].getComponent(4)).getValue()));
 			Preferences.set("rfpwm"+a+"start", ((JSpinner)rfpwmsettings[a].getComponent(8)).getValue().toString());
 			Preferences.set("rfpwm"+a+"end", ((JSpinner)rfpwmsettings[a].getComponent(10)).getValue().toString());
 			Preferences.set("rfpwm"+a+"duration", ((JSpinner)rfpwmsettings[a].getComponent(14)).getValue().toString());
 		}
 		Preferences.set("wifiportal",((JTextField) wifiportal.getComponent(1)).getText());
 		Preferences.save();
 	}
 	
 	public int getSelected(JPanel j)
 	{
 		int r=-1;
 		for (int a=0;a<j.getComponentCount();a++)
 			if (j.getComponent(a) instanceof JRadioButton)
 				if (((JRadioButton)j.getComponent(a)).isSelected()) 
 					r=a;
 		return r;
 	}
 	
 	public void setSelected(JPanel j,String p)
 	{
 		if (Preferences.get(p)!=null)
 		{
 			if (Integer.parseInt(Preferences.get(p))>=0)
 			{
 				((JRadioButton)j.getComponent(Integer.parseInt(Preferences.get(p)))).setSelected(true);
 				ActionEvent e = new ActionEvent(j.getComponent(Integer.parseInt(Preferences.get(p))),0,"Test");
 				for(ActionListener al: ((JRadioButton) j.getComponent(Integer.parseInt(Preferences.get(p)))).getActionListeners()){
 					al.actionPerformed(e);
 				}
 			}
 		}
 	}
 	
 	public void setDouble(JSpinner j,String p)
 	{
 		if (Preferences.get(p)!=null)
 		{
 			j.setValue( new Double(Preferences.get(p)));
 		}
 	}	
 	
 	public void setInteger(JSpinner j,String p)
 	{
 		if (Preferences.get(p)!=null)
 		{
 			j.setValue( new Integer(Preferences.get(p)));
 		}
 	}	
 
 	public int setChecked(Component j, String p)
 	{
 		int r=0;
 		if (Preferences.get(p)!=null)
 			r=Preferences.getInteger(p);
 			if (r!=0) ((JCheckBox)j).setSelected(true);
 		return r;
 	}
 	
 	public void setTime(JSpinner j,String p)
 	{
 		if (Preferences.get(p)!=null)
 		{
 			SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
 			try {
 				Date date = (Date)formatter.parse(Preferences.get(p));
 				j.setValue(date);
 			} catch (ParseException e) {
 				Title.SetText("Memory Settings");
 				e.printStackTrace();
 			}  
 		}		
 	}
 	public void CheckNav()
 	{
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
 				String s ="";
 				if (aButton.getText() == "Welcome") s = "Welcome";
 				if (aButton.getText() == "Memory") s = "Memory Settings";
 				if (aButton.getText() == "Temperature") s = "Temperature Settings";
 				if (aButton.getText() == "Expansion") s = "Expansion Modules";
 				if (aButton.getText() == "Attachments") s = "Attachments";
 				if (aButton.getText() == "Main Relay") s = "Main Relay Box";
 				if (aButton.getText() == "Expansion Relay" ) s = "Expansion Relay Box";
 				if (aButton.getText() == "Standard Dimming") s = "Daylight Dimming Channel";
 				if (aButton.getText() == "Dimming Expansion") s = "Dimming Expansion Channel 0";
 				if (aButton.getText() == "Aqua Illumination") s = "Aqua Illumination Port";
 				if (aButton.getText() == "RF Expansion") s = "Vortech Mode";
 				if (aButton.getText() == "Wifi") s = "Wifi Attachment";
 				if (s!="")
 				{
 					cl.show(insidePanel, s);
 					Title.SetText(s);
 				}
 			}
 		};		
 		taskGroup.removeAll();
 		JButton jb;
 		jb=new JButton("Welcome");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		jb=new JButton("Memory");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		jb=new JButton("Temperature");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		jb=new JButton("Expansion");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		jb=new JButton("Attachments");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		jb=new JButton("Main Relay");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		if (relayexpansion==1)
 		{
 			jb=new JButton("Expansion Relay");
 			jb.addActionListener(al);
 			taskGroup.add(jb);
 		}
 		jb=new JButton("Standard Dimming");
 		jb.addActionListener(al);
 		taskGroup.add(jb);
 		if (dimmingexpansion==1)
 		{
 			jb=new JButton("Dimming Expansion");
 			jb.addActionListener(al);
 			taskGroup.add(jb);
 		}
 		if (ailed==1)
 		{
 			jb=new JButton("Aqua Illumination");
 			jb.addActionListener(al);
 			taskGroup.add(jb);
 		}
 		if (rfexpansion==1)
 		{
 			jb=new JButton("RF Expansion");
 			jb.addActionListener(al);
 			taskGroup.add(jb);
 		}
 		if (wifi==1)
 		{
 			jb=new JButton("Wifi");
 			jb.addActionListener(al);
 			taskGroup.add(jb);
 		}		
 	}
 }
 
 
 
 
 
