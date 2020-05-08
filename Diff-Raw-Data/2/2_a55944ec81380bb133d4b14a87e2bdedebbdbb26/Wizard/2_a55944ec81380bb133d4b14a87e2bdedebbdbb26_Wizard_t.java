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
 import processing.app.tools.Tool;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.LookAndFeel;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.html.HTMLDocument;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
  
  public class Wizard implements Tool {
 	 Editor editor;
 	 Object[] options = { "Next","Cancel" };
 	 private int relay=1;
 	 private int window=1;
 	 private int tempunit=0;
 	 private int settingswidth=0;
 	 private int displayPWM=0;
 	 private int wifi=0;
 	 private int ailed=0;
 	 private int relayexpansion=0;
 	 private int dimmingexpansion=0;
 	 private int rfexpansion=0;
 	 private int salinityexpansion=0;
 	 private int ioexpansion=0;
 	 private int orpexpansion=0;
 	 private boolean atolow=false;
 	 private boolean atohigh=false;
 	 private boolean buzzer=false;
 	 private boolean upload=false;
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
 	 
 	 Font font = UIManager.getFont("Label.font");
      String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
      LookAndFeel lf = UIManager.getLookAndFeel();
 	 
 	public static String RegButtons[] = {"Time Schedule","Heater","Chiller/Fan","Auto Top Off","Wavemaker","CO2 Control","PH Control","Dosing Pump","Delayed Start","Opposite","Always On","Not Used"};
 	 
 	public static JRadioButton DisplayTemp = new JRadioButton();
 	public static JPanel OverheatSettings =  new JPanel();
 
 	public static String DescButtons[] = {
 				"This function turns on/off at specific times of the day. Example of devices that may use this function are light fixtures, refugium light, moonlight, powerheads and fans. Delayed Start is useful for MH ballasts.",
 				"This function turns on/off at specific temperatures. Example of device that may use this function is an electric heater.",
 				"This function turns on/off at specific temperatures. Example of devices that may use this function are fans, blowers, chillers and coolers.",
 				"This function turns on/off according to the state of one or more float switches. For more information on each ATO type, please visit ",
 				"This function turns on/off on a cycle at every specific or random number of seconds. Example of devices that may use this function are powerheads and circulation pumps.",
 				"This function turns on/off at specific pH readings. The device that uses this function must lower pH. Example of device that may use this function is a calcium reactor.",
 				"This function turns on/off at specific pH readings. The device that uses this function must increase pH. Example of device that may use this function is a Kalk based Doser.",
 				"This function turns on every specific minutes for a specific number of seconds. Use the offset when dosing different chemicals on same schedule, but with offset minutes apart. Example of device that may use this function is a dosing pump, kalk stirrers and feeders",
 				"This function turns on with a specific delay. The delayed start is triggered by controller reboot, Feeding and Water Change mode. Example of device that may use this function is a skimmer.",
 				"This function turns on/off in the opposite status of a specific Port. Example of device that may use this function are moonlights, powerheads and circulation pumps.",
 				"This function turns on at start-up. It does not turn off unless selected to turn off during Feeding or Water Change modes. Example of device that may use this function are return pumps, skimmers, powerheads and circulation pumps.",
 				"This Port is not being used."
 		};
 	 
 	public static String ExpModules[] = {"Relay","Dimming","RF","Salinity","I/O","pH/ORP"};
 	public static String AttachModules[] = {"Wifi","Aqua Illuminaton Cable"};
 	public static String AIChannels[] = {"White","Blue","Royal Blue"};
 	public static String VortechModes[] = { "Constant","Lagoon","ReefCrest","Short Pulse","Long Pulse","Nutrient Transport","Tidal Swell" };
 	public static String RadionChannels[] = {"White","Royal Blue","Red","Green","Blue","Intensity"};
 	
 	public String getMenuTitle() {
 		return "Reef Angel Wizard";
 	}
  
 	public void init(Editor theEditor) {
 		this.editor = theEditor;
 	}
  
 	public void run() {
 
     	if (editor.getSketch().getCode(0).isModified())
     	{
     		JOptionPane.showMessageDialog(editor,
                     "You must save the currect sketch before proceeding",
                     "Error",JOptionPane.ERROR_MESSAGE);
     		return;
     	}
 
     	UIManager.put("control", new Color(219,227,249));
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
 		while(window<100)
 		{
 			switch (window)
 			{
 			case 1:
 				window += ShowWelcome();
 				break;
 			case 2:
 				window += ShowMemorySettings();
 				break;
 			case 3:
 				window += ShowTemperature();
 				ResetTempRange();
 				break;
 			case 4:
 				window += ShowExpansion();
 				JCheckBox jc=null;
 				jc=(JCheckBox) expansionmods.getComponent(0);
 				if (jc.isSelected())
 				{
 					relayexpansion=1;
 					for (int a=1;a<=16;a++)
 					{
 						JComboBox c=(JComboBox) Opposite[a].getComponent(2);
 						c.removeAllItems();
 						for (int b=1;b<=8;b++)
 						{
 							c.addItem("Main Box Port "+b);
 						}
 						for (int b=1;b<=8;b++)
 						{
 							c.addItem("Exp. Box Port "+b);
 						}
 					}
 				}	
 				else
 				{
 					relayexpansion=0;
 					for (int a=1;a<=8;a++)
 					{
 						JComboBox c=(JComboBox) Opposite[a].getComponent(2);
 						c.removeAllItems();
 						for (int b=1;b<=8;b++)
 						{
 							c.addItem("Main Box Port "+b);
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
 		        if (ioexpansion==1)
 		        {
 		            Buzzermods= new JPanel();
 		            Buzzermods.setLayout(new BoxLayout( Buzzermods, BoxLayout.PAGE_AXIS));
 		            Buzzermods.add(new JCheckBox ("Overheat"));
 		            Buzzermods.add(new JCheckBox ("ATO Timeout"));
 		            Buzzermods.add(new JCheckBox ("High ATO is activated"));
 		            Buzzermods.add(new JCheckBox ("I/O Channel 0 is activated"));
 		        	Buzzermods.add(new JCheckBox ("I/O Channel 1 is activated"));
 		        	Buzzermods.add(new JCheckBox ("I/O Channel 2 is activated"));
 		        	Buzzermods.add(new JCheckBox ("I/O Channel 3 is activated"));
 		        	Buzzermods.add(new JCheckBox ("I/O Channel 4 is activated"));
 		        	Buzzermods.add(new JCheckBox ("I/O Channel 5 is activated"));
 		        }
 		        else
 		        {
 		            Buzzermods= new JPanel();
 		            Buzzermods.setLayout(new BoxLayout( Buzzermods, BoxLayout.PAGE_AXIS));
 		            Buzzermods.add(new JCheckBox ("Overheat"));
 		            Buzzermods.add(new JCheckBox ("ATO Timeout"));
 		            Buzzermods.add(new JCheckBox ("High ATO is activated"));
 		        }
 				
 				break;
 			case 5:
 				window += ShowAttachment();
 				jc=null;
 				jc=(JCheckBox) attachmentmods.getComponent(0);
 				if (jc.isSelected()) wifi=1; else wifi=0;
 				jc=(JCheckBox) attachmentmods.getComponent(1);
 				if (jc.isSelected()) ailed=1; else ailed=0;
 				
 				break;
 			case 6:
 				window += ShowRelays();
 				break;
 			case 7:
 			{
 				int r=0;
 				int relaystatus=ShowRelaySetup();
 				switch (relaystatus)
 				{
 				case -1:
 					if (relay-- == 1) 
 					{
 						relay=1;
 						window--;					
 					}
 					r=0;
 					if (relayexpansion==1 && relay==8)
 					{
 						relay++;
 						r = ShowRelays();
 					}
 					if (r==-1)
 						relay=8;
 					if (r==100) window+=r;
 					
 					break;
 				case 1:
 					r=0;
 					if (relay++ == 16) window++;
 					if (relayexpansion==0 && relay==9) window++;
 					if (relayexpansion==1 && relay==9) r = ShowRelays();
 					if (r==-1)
 						relay=8;
 					if (r==100) window+=r;
 					break;
 				case 100:
 					window+=100;
 					return;
 				default:
 					JOptionPane.showMessageDialog(editor,
 							"Error Code: " + relaystatus,
 		                    "Error",JOptionPane.ERROR_MESSAGE);
 					window+=100;
 					return;
 				}
 				
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
 		
 				break;
 			}
 
 			case 8:
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
 				window += ShowPWM("Daylight",daylightpwm);
 				if (window==9)
 				{
 					JRadioButton jpwm = (JRadioButton) daylightpwm.getComponent(0);
 					
 					if (jpwm.isSelected())
 					{
 				    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 				    	if (jb.isSelected())
 				    	{
 							int i=ShowPWMSettings("Daylight",daylightpwmsettings,daylightpwm);
 							if (i!=1) window+=i;
 				    	}
 					}
 	
 					jpwm = (JRadioButton) daylightpwm.getComponent(1);
 					if (jpwm.isSelected())
 					{
 				    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 				    	if (jb.isSelected())
 				    	{
 							int i=ShowPWMSettings("Daylight",daylightpwmsettings,daylightpwm);
 							if (i!=1) window+=i;
 				    	}
 					}
 				}
 				if (window==7)
 				{
 					if (relayexpansion==0) relay=8;
 					if (relayexpansion==1) relay=16;
 				}
 				break;
 			case 9:
 				window += ShowPWM("Actinic",actinicpwm);
 				if (window==10)
 				{
 					JRadioButton jpwm = (JRadioButton) actinicpwm.getComponent(0);
 					
 					if (jpwm.isSelected())
 					{
 				    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 				    	if (jb.isSelected())
 				    	{
 							int i=ShowPWMSettings("Actinic",actinicpwmsettings,actinicpwm);
 							if (i!=1) window+=i;
 				    	}
 					}
 	
 					jpwm = (JRadioButton) actinicpwm.getComponent(1);
 					if (jpwm.isSelected())
 					{
 				    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 				    	if (jb.isSelected())
 				    	{
 							int i=ShowPWMSettings("Actinic",actinicpwmsettings,actinicpwm);
 							if (i!=1) window+=i;
 				    	}
 					}
 					
 					JRadioButton jpwmbd = (JRadioButton) daylightpwm.getComponent(3);
 					JRadioButton jpwmba = (JRadioButton) actinicpwm.getComponent(3);
 					buzzer=jpwmba.isSelected() || jpwmbd.isSelected();
 					if (dimmingexpansion==0)
 					{
 						window++;
 						if (ailed==0)
 						{
 							window+=2;
 							if (rfexpansion==0)
 							{
 								window+=2;
 								if (wifi==0) 
 								{
 									window++;
 									if (buzzer==false) window++;
 								}
 							}
 						}
 					}
 				}
 				if (window==8)
 				{
 					if (relayexpansion==0) relay=8;
 					if (relayexpansion==1) relay=16;
 				}
 				break;
 			case 10:
 				while (w<100)
 				{
 					int wr = ShowExpPWM("Channel "+w,exppwm[w]);
 					int ws = 0;
 					if (wr==1)
 					{
 						JRadioButton jpwm0 = (JRadioButton) exppwm[w].getComponent(0);
 						JRadioButton jpwm1 = (JRadioButton) exppwm[w].getComponent(1);
 						JRadioButton jpwm2 = (JRadioButton) exppwm[w].getComponent(2);
 						JRadioButton jpwm3 = (JRadioButton) exppwm[w].getComponent(3);
 						JRadioButton jpwm4 = (JRadioButton) exppwm[w].getComponent(4);
 						
 						if (jpwm0.isSelected())
 						{
 					    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 					    	if (jb.isSelected())
 								ws=ShowExpPWMSettings("Channel "+w,exppwmsettings[w],exppwm[w]);
 					    	else
 					    		w++;
 					    }
 		
 						if (jpwm1.isSelected())
 						{
 					    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 					    	if (jb.isSelected())
 								ws=ShowExpPWMSettings("Channel "+w,exppwmsettings[w],exppwm[w]);
 					    	else
 					    		w++;
 						}
 						if (jpwm2.isSelected()) w++;
 						if (jpwm3.isSelected()) w++;
 						if (jpwm4.isSelected()) w++;
 						
 						if (ws==1) w++;
 						if (ws==100) w+=100;
 					}
 					if (wr==-1)
 					{
 						w--;
 					}
 					if (wr==100)
 					{
 						w=100;
 						window=100;
 						break;
 					}
 					if (w<0)
 					{
 						w=0;
 						window--;
 						break;
 
 					}
 					if (w>=100)
 					{
 						w=100;
 						window=100;
 						break;
 					}						
 					if (w>5)
 					{
 						window++;
 						JRadioButton jpwmbd = (JRadioButton) daylightpwm.getComponent(3);
 						JRadioButton jpwmba = (JRadioButton) actinicpwm.getComponent(3);
 						JRadioButton jpwmb0 = (JRadioButton) exppwm[0].getComponent(3);
 						JRadioButton jpwmb1 = (JRadioButton) exppwm[1].getComponent(3);
 						JRadioButton jpwmb2 = (JRadioButton) exppwm[2].getComponent(3);
 						JRadioButton jpwmb3 = (JRadioButton) exppwm[3].getComponent(3);
 						JRadioButton jpwmb4 = (JRadioButton) exppwm[4].getComponent(3);
 						JRadioButton jpwmb5 = (JRadioButton) exppwm[5].getComponent(3);
 						buzzer=jpwmba.isSelected() || jpwmbd.isSelected() || jpwmb0.isSelected() || jpwmb1.isSelected() || jpwmb2.isSelected() || jpwmb3.isSelected() || jpwmb4.isSelected() || jpwmb5.isSelected();
 						if (ailed==0)
 						{
 							window+=2;
 							if (rfexpansion==0)
 							{
 								window+=2;
 								if (wifi==0) 
 								{
 									window++;
 									if (buzzer==false) window++;
 								}
 							}
 						}
 						w=100;
 					}
 				}
 				break;
 			case 11:
 				window+=ShowAIPort();
 				if (window==10)
 					if (dimmingexpansion==0)
 					{
 						window--;
 					}
 					else
 					{
 						w=5;
 					}
 				if (window==12) wa=0;
 				JRadioButton jah =(JRadioButton) aiport.getComponent(1);
 				if (jah.isSelected()) atohigh=true;
 				Buzzermods.getComponent(2).setEnabled(!atohigh);
 				break;
 			case 12:
 				while (wa<100)
 				{
 					int wr = ShowAIPWM(AIChannels[wa],aipwm[wa]);
 					int ws = 0;
 					if (wr==1)
 					{
 						JRadioButton jpwm0 = (JRadioButton) aipwm[wa].getComponent(0);
 						JRadioButton jpwm1 = (JRadioButton) aipwm[wa].getComponent(1);
 						JRadioButton jpwm2 = (JRadioButton) aipwm[wa].getComponent(2);
 						JRadioButton jpwm3 = (JRadioButton) aipwm[wa].getComponent(3);
 						
 						if (jpwm0.isSelected())
 						{
 					    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 					    	if (jb.isSelected())
 					    		ws=ShowAIPWMSettings(AIChannels[wa],aipwmsettings[wa],aipwm[wa]);
 					    	else
 					    		wa++;
 						}
 		
 						if (jpwm1.isSelected())
 						{
 					    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 					    	if (jb.isSelected())
 								ws=ShowAIPWMSettings(AIChannels[wa],aipwmsettings[wa],aipwm[wa]);
 					    	else
 					    		wa++;
 						}
 						if (jpwm2.isSelected()) wa++;
 						if (jpwm3.isSelected()) wa++;
 						
 						if (ws==1) wa++;
 						if (ws==100) wa+=100;
 					}
 					if (wr==-1)
 					{
 						wa--;
 					}
 					if (wr==100)
 					{
 						wa=100;
 						window=100;
 						break;
 					}
 					if (wa<0)
 					{
 						wa=0;
 						window--;
 						break;
 
 					}
 					if (wa>=100)
 					{
 						wa=100;
 						window=100;
 						break;
 					}						
 					if (wa>=AIChannels.length)
 					{
 						window++;
 						if (rfexpansion==0)
 						{
 							window+=2;
 							if (wifi==0) 
 							{
 								window++;
 								if (buzzer==false) window++;
 							}
 						}
 							
 						wa=100;
 					}
 				}
 				break;
 			case 13:
 		    	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
 		    	if (jb.isSelected())
 		    		window+=ShowVortech();
 		    	else
 		    		window++;
 				if (window==12)
 				{
 					if (ailed==0)
 					{
 						window-=2;
 						if (dimmingexpansion==0)
 						{
 							window--;
 						}
 						else
 						{
 							w=5;
 						}
 						
 					}
 					else
 					{
 						wa=2;
 					}
 				}
 				if (window==14) wra=0;
 				break;
 			case 14:
 				while (wra<100)
 				{
 					int wr = ShowRFPWM(RadionChannels[wra],rfpwm[wra]);
 					int ws = 0;
 					if (wr==1)
 					{
 						JRadioButton jpwm0 = (JRadioButton) rfpwm[wra].getComponent(0);
 						JRadioButton jpwm1 = (JRadioButton) rfpwm[wra].getComponent(1);
 						JRadioButton jpwm2 = (JRadioButton) rfpwm[wra].getComponent(2);
 						JRadioButton jpwm3 = (JRadioButton) rfpwm[wra].getComponent(3);
 						
 						if (jpwm0.isSelected())
 						{
 					    	JRadioButton jb1 = (JRadioButton) memsettings.getComponent(0);
 					    	if (jb1.isSelected())
 								ws=ShowRFPWMSettings(RadionChannels[wra],rfpwmsettings[wra],rfpwm[wra]);
 					    	else
 					    		wra++;
 						}
 		
 						if (jpwm1.isSelected())
 						{
 					    	JRadioButton jb1 = (JRadioButton) memsettings.getComponent(0);
 					    	if (jb1.isSelected())
 								ws=ShowRFPWMSettings(RadionChannels[wra],rfpwmsettings[wra],rfpwm[wra]);
 					    	else
 					    		wra++;
 						}
 						if (jpwm2.isSelected()) wra++;
 						if (jpwm3.isSelected()) wra++;
 						
 						if (ws==1) wra++;
 						if (ws==100) wra+=100;
 					}
 					if (wr==-1)
 					{
 						wra--;
 					}
 					if (wr==100)
 					{
 						wra=100;
 						window=100;
 						break;
 					}
 					if (wra<0)
 					{
 						wra=0;
 				    	JRadioButton jb1 = (JRadioButton) memsettings.getComponent(0);
 				    	if (jb1.isSelected())
 				    	{
 							window--;
 				    	}
 				    	else
 				    	{
 							if (ailed==0)
 							{
 								window-=4;
 								if (dimmingexpansion==0)
 								{
 									window--;
 								}
 								else
 								{
 									w=5;
 								}
 								
 							}
 							else
 							{
 								window-=2;
 								wa=2;
 							}
 				    	}
 						break;
 					}
 					if (wra>=100)
 					{
 						wra=100;
 						window=100;
 						break;
 					}						
 					if (wra>=RadionChannels.length)
 					{
 						window++;
 						if (wifi==0) 
 						{
 							window++;
 							if (buzzer==false) window++;
 						}
 						wra=100;
 					}
 				}
 				break;
 			case 15:
 				window += ShowWifi();	
 				if (window==14)
 				{
 					if (rfexpansion==0)
 					{
 						window-=2;
 						if (ailed==0)
 						{
 							window-=2;
 							if (dimmingexpansion==0)
 							{
 								window--;
 							}
 							else
 							{
 								w=5;
 							}
 							
 						}
 						else
 						{
 							wa=2;
 						}
 					}
 					else
 					{
 						wra=5;
 					}
 				}
 				if (window==16 && buzzer==false) window++;
 				break;
 			case 16:
 				window+=ShowBuzzer();
 				if (window==15)
 				{
 					if (wifi==0)
 					{
 						window--;
 						if (rfexpansion==0)
 						{
 							window-=2;
 							if (ailed==0)
 							{
 								window-=2;
 								if (dimmingexpansion==0)
 								{
 									window--;
 								}
 								else
 								{
 									w=5;
 								}
 								
 							}
 							else
 							{
 								wa=2;
 							}
 						}
 						else
 						{
 							wra=5;
 						}
 					}
 				}				
 				break;
 			case 17:
 				window+=ShowGenerate();
 				if (window==16)
 				{
 					if (buzzer==false)
 					{
 						window--;
 						if (wifi==0)
 						{
 							window--;
 							if (rfexpansion==0)
 							{
 								window-=2;
 								if (ailed==0)
 								{
 									window-=2;
 									if (dimmingexpansion==0)
 									{
 										window--;
 									}
 									else
 									{
 										w=5;
 									}
 									
 								}
 								else
 								{
 									wa=2;
 								}
 							}
 							else
 							{
 								wra=5;
 							}
 						}
 					}
 				}
 				break;
 			case 18:
 				//TODO: Finish Code
 				window += 200;
 				break;
 			default:
 			break;
 			}
 		}
 		if (window<200)
 		{
 			RetundLF();
 	        return;
 		}
 		ConstructCode();
 		RetundLF();
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
 							d+= "    ReefAngel.StandardLights( " + sp + (a-poffset);
 							d+= ",";
 							Calendar toh= Calendar.getInstance();
 							JSpinner j = null; 
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
 							d+= " );\n";
 						}
 						else
 						{
 							JRadioButton jt1 = (JRadioButton) TimedMemory[a].getComponent(1);
 							if (jt1.isSelected())
 								d+= "    ReefAngel.StandardLights( " + sp + (a-poffset) + " );\n";	
 							jt1 = (JRadioButton) TimedMemory[a].getComponent(2);
 							if (jt1.isSelected())
 								d+= "    ReefAngel.DelayedStartLights( " + sp + (a-poffset) + " );\n";	
 							jt1 = (JRadioButton) TimedMemory[a].getComponent(3);
 							if (jt1.isSelected())
 								d+= "    ReefAngel.ActinicLights( " + sp + (a-poffset) + " );\n";	
 							jt1 = (JRadioButton) TimedMemory[a].getComponent(4);
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
 							if ((a-poffset)==5)
 								d+= "    ReefAngel.Wavemaker1( " + sp + (a-poffset) + " );\n";
 							else
 								d+= "    ReefAngel.Wavemaker2( " + sp + (a-poffset) + " );\n";
 						}
 						break;
 					case 5:
 						d+= "    ReefAngel.CO2Control( " + sp + (a-poffset);
 						if (jb1.isSelected())
 						{
 							d+= ",";
 							JSpinner jh = null; 
 							jh = (JSpinner) CO2Control[a].getComponent(2);
 							d+= (int)((Double)jh.getValue() *100) + ",";
 							jh = (JSpinner) CO2Control[a].getComponent(4);
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
 							jc = (JSpinner) pHControl[a].getComponent(4);
 							d+= (int)((Double)jc.getValue() *100) + ",";
 							jc = (JSpinner) pHControl[a].getComponent(2);
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
 							else
 							{
 								d+= "    ReefAngel.DosingPumpRepeat2( " + sp + (a-poffset) + " );\n";
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
 						d+= jco.getSelectedItem().toString().replace("Main Box ", "").replace("Exp. Box ", "Box1_").replace(" ", "");
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
 		}
 		d+="    ReefAngel.ShowInterface();\n" + 
 				"}\n\n";
 
 		if (relayexpansion==1 || dimmingexpansion==1 || ailed==1 || ioexpansion==1 || salinityexpansion==1 || orpexpansion==1) 
 		{
 			int h=82;
 			int y=2;
 			h-=relayexpansion*10;
 			h-=dimmingexpansion*28;
 			h-=ailed*10;
 			h-=ioexpansion*6;
 			if (salinityexpansion==1 || orpexpansion==1)
 				h-=10;
 			
 			if (salinityexpansion==1 || orpexpansion==1)
 				h/=(relayexpansion+dimmingexpansion+ailed+ioexpansion+salinityexpansion+3);
 			else
 				h/=(relayexpansion+dimmingexpansion+ailed+ioexpansion+3);
 			//System.out.println(h);
 			
 			
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
 				y+=6;
 			}
 			
 			y+=h;
 			if (salinityexpansion==0 && orpexpansion==0 && ioexpansion==0 && ailed==0 && dimmingexpansion==0 && relayexpansion==1 )
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
 							"    ReefAngel.LCD.DrawText( COLOR_DARKKHAKI,DefaultBGColor,39," + y + ", ReefAngel.Params.Salinity );\n" + 
 							"    pingSerial();\n\n";
 				}
 				if (orpexpansion==1)
 				{
 					d+="    // ORP\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_PALEVIOLETRED,DefaultBGColor,75," + y + ", \"ORP:\" );\n" + 
 							"    ReefAngel.LCD.DrawText( COLOR_PALEVIOLETRED,DefaultBGColor,99," + y + ", ReefAngel.Params.ORP );\n" + 
 							"    pingSerial();\n\n";
 				}
 
 				y+=10;
 			}
 
 			y+=h;
 			
 			d+="    // Main Relay Box\n" + 
 					"    byte TempRelay = ReefAngel.Relay.RelayData;\n" + 
 					"    TempRelay &= ReefAngel.Relay.RelayMaskOff;\n" + 
 					"    TempRelay |= ReefAngel.Relay.RelayMaskOn;\n" + 
 					"    ReefAngel.LCD.DrawOutletBox( 12, " + y + ", TempRelay );\n" + 
 					"    pingSerial();\n\n";
 			y+=10;
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
 		editor.getBase().handleNewReplace();
 		editor.setSelectedText(d);
 		if (upload) 
 		{
 			editor.handleExport(false);
 			ShowUploading();
 		}
 		// TODO: Finish Code
 //		String featurefile="// AutoGenerated file by Reef Angel Wizard\n" + 
 //				"\n" + 
 //				"/*\n" + 
 //				" * Copyright 2012 Reef Angel\n" + 
 //				" *\n" + 
 //				" * Licensed under the Apache License, Version 2.0 (the \"License\")\n" + 
 //				" * you may not use this file except in compliance with the License.\n" + 
 //				" * You may obtain a copy of the License at\n" + 
 //				" *\n" + 
 //				" * http://www.apache.org/licenses/LICENSE-2.0\n" + 
 //				" *\n" + 
 //				" * Unless required by applicable law or agreed to in writing, software\n" + 
 //				" * distributed under the License is distributed on an \"AS IS\" BASIS,\n" + 
 //				" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" + 
 //				" * See the License for the specific language governing permissions and\n" + 
 //				" * limitations under the License.\n" + 
 //				" */\n" + 
 //				"\n" + 
 //				"\n" + 
 //				"#ifndef __REEFANGEL_FEATURES_H__\n" + 
 //				"#define __REEFANGEL_FEATURES_H__\n" + 
 //				"\n" + 
 //				"\n" + 
 //				"#define VersionMenu\n" + 
 //				"#define WDT\n" + 
 //				"#define SIMPLE_MENU\n";
 //		if (relayexpansion==1) featurefile+="#define RelayExp\n" + 
 //				"#define InstalledRelayExpansionModules 1\n";
 //		if (displayPWM==1) featurefile+="#define DisplayLEDPWM\n";
 //		if (wifi==1) featurefile+="#define wifi\n";
 //		if (salinityexpansion==1) featurefile+="#define SALINITYEXPANSION\n";
 //		if (orpexpansion==1) featurefile+="#define ORPEXPANSION\n";
 //		if (ioexpansion==1) featurefile+="#define IOEXPANSION\n";
 //		if (rfexpansion==1) featurefile+="#define RFEXPANSION\n";
 //		if (dimmingexpansion==1) featurefile+="#define PWMEXPANSION\n";
 //		if (ailed==1) featurefile+="#define AI_LED\n";
 //		if (buzzer)
 //		{
 //			JCheckBox jo = (JCheckBox)Buzzermods.getComponent(0);
 //			JCheckBox ja = (JCheckBox)Buzzermods.getComponent(1);
 //			if (jo.isSelected() || ja.isSelected()) featurefile+="#define ENABLE_EXCEED_FLAGS\n";
 //		}		
 //		if (relayexpansion==1 || dimmingexpansion==1 || ailed==1 || ioexpansion==1 || salinityexpansion==1 || orpexpansion==1) featurefile+="#define CUSTOM_MAIN\n";
 //		
 //		featurefile+="\n" + 
 //				"\n" + 
 //				"#endif  // __REEFANGEL_FEATURES_H__";
 //		try {
 //			Base.saveFile(featurefile, new File(Base.getSketchbookLibrariesPath()+"/ReefAngel_Features/ReefAngel_Features.h"));
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 		
 	}
 
 	private int ShowBuzzer()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Buzzer Settings");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	JLabel text = new JLabel("<HTML><br>Please choose when to turn buzzer on:<br><br></HTML>");
     	text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	Buzzermods.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(Buzzermods);
     	panel.add(panel2);
     	return NextPrevButton(panel, "Buzzer Settings");
 	}	
 	
 	private int ShowVortech()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Vortech Mode");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	JLabel text = new JLabel("<HTML><br>Please choose the default vortech mode:<br><br></HTML>");
     	text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	RFmods.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(RFmods);
     	panel.add(panel2);
     	return NextPrevButton(panel, "RF Expansion");
 		
 	}		
 	
 	private int ShowWifi()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Portal Settings");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	JLabel text = new JLabel("<HTML><br>Please enter your forum username to send data to the Portal :<br><br></HTML>");
     	text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	wifiportal.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(wifiportal);
     	panel.add(panel2);
     	return NextPrevButton(panel, "Portal Settings");
 	}	
 
 	private int ShowAIPort()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Aqua Illumination Port");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text=null;
    		text = new JLabel("<HTML><br>Which ATO port would you like to use to communicate with your AI fixture?<br><br>Port choice that has been disabled was assigned to ATO function previously.<br><br></HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_ato.png");
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		text = new JLabel("<HTML><br</HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	aiport.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(aiport);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Aqua Illumination Port");
 	}	
 
 	private int ShowPWM(String channel, JPanel jpwm)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Standard " + channel + " Dimming Channel");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text=null;
    		text = new JLabel("<HTML><br>What type of waveform would you like to use on your " + channel + " dimming channel?<br><br></HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_dimming.png");
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		text = new JLabel("<HTML><br</HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	JPanel jpic = new JPanel(new GridLayout(1,5));
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 		JLabel ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
 		jpic.add(new JLabel(""));
 		jpic.add(new JLabel(""));
 
 		
 		jpic.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpic);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Standard Dimming Channels");
 	}
 
 	private int ShowExpPWM(String channel, JPanel jpwm)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Dimming Expansion " + channel);
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text=null;
    		text = new JLabel("<HTML><br>What type of waveform would you like to use on your " + channel + " of your dimming expansion module?<br><br></HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_dimming.png");
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		text = new JLabel("<HTML><br</HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	JPanel jpic = new JPanel(new GridLayout(1,5));
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 		JLabel ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
 		jpic.add(new JLabel(""));
 		jpic.add(new JLabel(""));
 
 		
 		jpic.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpic);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Dimming Expansion Channels");
 	}	
 	
 	private int ShowRFPWM(String channel, JPanel jpwm)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Ecotech Radion " + channel + " Channel");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text=null;
    		text = new JLabel("<HTML><br>What type of waveform would you like to use on your " + channel + " channel of your Ecotech Radion fixture?<br><br></HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/radion.png");
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		text = new JLabel("<HTML><br</HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	JPanel jpic = new JPanel(new GridLayout(1,4));
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 		JLabel ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
 		jpic.add(new JLabel(""));
 
 		
 		jpic.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpic);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Ecotech Radion Channels");
 	}		
 	
 	private int ShowAIPWM(String channel, JPanel jpwm)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Aqua Illumination " + channel + " Channel");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text=null;
    		text = new JLabel("<HTML><br>What type of waveform would you like to use on your " + channel + " channel of your Aqua Illumination fixture?<br><br></HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ai.png");
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		text = new JLabel("<HTML><br</HTML>");
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	JPanel jpic = new JPanel(new GridLayout(1,4));
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
 		JLabel ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
 		ic = new JLabel(iconnection);
 		jpic.add(ic);
 		jpic.add(new JLabel(""));
 
 		
 		jpic.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpic);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Aqua Illumination Channels");
 	}		
 	
 	private int ShowPWMSettings(String channel, JPanel jpwm, JPanel jpwmchoice)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Standard " + channel + " Dimming Channel");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	
     	JLabel text=null;
     	ImageIcon iconnection = null;
     	JRadioButton jpd=(JRadioButton) jpwmchoice.getComponent(0);
     	if (jpd.isSelected())
     	{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			jpwm.getComponent(13).setVisible(true);
 			jpwm.getComponent(14).setVisible(true);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 		}
 
 		jpd=(JRadioButton) jpwmchoice.getComponent(1);
 		if (jpd.isSelected())
 		{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			jpwm.getComponent(13).setVisible(false);
 			jpwm.getComponent(14).setVisible(false);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start % and end % of the parabola.<br><br></HTML>");
 		}
     	
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Standard Dimming Channels");
 	}
 	
 	private int ShowExpPWMSettings(String channel, JPanel jpwm, JPanel jpwmchoice)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Dimming Expansion " + channel);
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	
     	JLabel text=null;
     	ImageIcon iconnection = null;
     	JRadioButton jpd=(JRadioButton) jpwmchoice.getComponent(0);
     	if (jpd.isSelected())
     	{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			jpwm.getComponent(13).setVisible(true);
 			jpwm.getComponent(14).setVisible(true);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 		}
 
 		jpd=(JRadioButton) jpwmchoice.getComponent(1);
 		if (jpd.isSelected())
 		{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			jpwm.getComponent(13).setVisible(false);
 			jpwm.getComponent(14).setVisible(false);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start % and end % of the parabola.<br><br></HTML>");
 		}
     	
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Dimming Expansion Channels");
 	}	
 
 	private int ShowRFPWMSettings(String channel, JPanel jpwm, JPanel jpwmchoice)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Ecotech Radion " + channel + " Channel");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	
     	JLabel text=null;
     	ImageIcon iconnection = null;
     	JRadioButton jpd=(JRadioButton) jpwmchoice.getComponent(0);
     	if (jpd.isSelected())
     	{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			jpwm.getComponent(13).setVisible(true);
 			jpwm.getComponent(14).setVisible(true);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 		}
 
 		jpd=(JRadioButton) jpwmchoice.getComponent(1);
 		if (jpd.isSelected())
 		{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			jpwm.getComponent(13).setVisible(false);
 			jpwm.getComponent(14).setVisible(false);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start % and end % of the parabola.<br><br></HTML>");
 		}
     	
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Ecotech Radion Channels");
 	}		
 	
 	private int ShowAIPWMSettings(String channel, JPanel jpwm, JPanel jpwmchoice)
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
    		steps = new JLabel("Aqua Illumination " + channel + " Channel");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	
     	JLabel text=null;
     	ImageIcon iconnection = null;
     	JRadioButton jpd=(JRadioButton) jpwmchoice.getComponent(0);
     	if (jpd.isSelected())
     	{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");
 			jpwm.getComponent(13).setVisible(true);
 			jpwm.getComponent(14).setVisible(true);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start %, end % and duration of the slope.<br>This waveform is symetrical on both ends of the cycle.<br><br></HTML>");
 		}
 
 		jpd=(JRadioButton) jpwmchoice.getComponent(1);
 		if (jpd.isSelected())
 		{
     		iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
 			jpwm.getComponent(13).setVisible(false);
 			jpwm.getComponent(14).setVisible(false);
 	    	text = new JLabel("<HTML><br>Please enter the start time, end time, start % and end % of the parabola.<br><br></HTML>");
 		}
     	
 		JLabel c = new JLabel(iconnection);
 		c.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(c);
    		
    		text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(text);
     	
     	jpwm.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(jpwm);
     	
     	panel.add(panel2);
    		return NextPrevButton(panel, "Aqua Illumination Channels");
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
     	JLabel text = new JLabel("<HTML><br>Arduino is now compiling your sketch.<br>In a few seconds, it will start uploading the code to your Reef Angel Controller.<br><br></HTML>");
     	panel2.add(text);
     	panel.add(panel2);
 		JOptionPane.showMessageDialog(editor,
     			panel,
                 "Reef Angel Wizard",JOptionPane.DEFAULT_OPTION);		
 	}	
 	
 	private int ShowGenerate()
 	{
     	JPanel panel = new JPanel();
     	
     	AddPanel(panel);
 	    
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Reef Angel Wizard");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	JLabel text = new JLabel("<HTML><br>You have provided all information needed to generate this code.<br>Would you like to generate and upload this code?<br><br></HTML>");
     	panel2.add(text);
     	text = new JLabel("<HTML><br>Please connect the USB-TTL cable to your Reef Angel Controller.<br>Make sure the controller is powered up.<br><br></HTML>");
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/connection.png");
 		JLabel c = new JLabel(iconnection);
 		panel2.add(c);
     	panel.add(panel2);
 
     	return GeneratePrevButton(panel, "Reef Angel Wizard");
     	
 	}
 	
 	private int ShowWelcome()
 	{
     	JPanel panel = new JPanel();
     	
     	AddPanel(panel);
 	    
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Reef Angel Wizard");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	JLabel text = new JLabel("<HTML><br>Welcome to the Reef Angel Wizard<br><br>I'm going to walk you through the whole process of generating a code for<br>your Reef Angel Controller<br><br>Version: ##tool.version##<br><br></HTML>");
     	panel2.add(text);
     	panel.add(panel2);
 
     	return NextCancelButton(panel, "Welcome");
     	
 	}
 	
 	private int ShowMemorySettings()
 	{
     	JPanel panel = new JPanel();
     	
     	AddPanel(panel);
 	    
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Reef Angel Wizard");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text = new JLabel("<HTML><br>User settings can be stored within the code itself or inside the intenal memory of the controller.<br>Example of these settings are light schedule, heater/chiller temperature and many other settings.<br><br>Settings stored within the code itself is the easiest way to program your controller because it will not<br>require you to use any other application. This is the recommended method for new users.<br>The code generated by the Reef Angel Wizard will contain all the information needed and the<br>controller will be ready to be used.<br><br>Settings that are stored inside the intenal memory will require you to use another application after<br>you upload the code generated by the Reef Angel Wizard.<br>The benefit of using another application is that you will be able to change it at any time without<br>having to generate a new code.<br>One good example is changing your light schedule using one of the smart phone apps.<br><br>Where would you like to store your settings?<br><br></HTML>");
     	panel2.add(text);
     	text.setAlignmentX(Component.LEFT_ALIGNMENT);
 
     	panel2.add(memsettings);
     	memsettings.setAlignmentX(Component.LEFT_ALIGNMENT);
 
     	panel.add(panel2);
 
     	return NextPrevButton(panel, "User Settings");	
 	
 	}
 
 	private int ShowTemperature()
 	{
 		
     	JPanel panel = new JPanel();
     	
     	AddPanel(panel);
 	    
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Reef Angel Wizard");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(steps);
     	JLabel text = new JLabel("<HTML><br>How would you like to see your temperature readings?<br><br></HTML>");
     	panel2.add(text);
     	text.setAlignmentX(Component.LEFT_ALIGNMENT);
 
     	panel2.add(disptemp);
     	disptemp.setAlignmentX(Component.LEFT_ALIGNMENT);
 
     	JRadioButton jb = (JRadioButton) memsettings.getComponent(0);
     	if (jb.isSelected())
     	{
 	    	JLabel text1 = new JLabel("<HTML><br>Which temperature would you like to set the controller to flag for Overheat?<br><br></HTML>");
 	    	panel2.add(text1);
 	    	text1.setAlignmentX(Component.LEFT_ALIGNMENT);
 	    	
 	    	panel2.add(OverheatSettings);
     	}    	
     	
     	panel.add(panel2);
 
     	return NextPrevButton(panel, "Displayed Temperature");	
 	
 	}
 
 	private int ShowRelays()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
     	if (relay<9)
     		steps = new JLabel("Main Relay Box");
     	else
     		steps = new JLabel("Expansion Relay Box");
     		
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	JLabel text=null;
     	if (relay<9)
     		text = new JLabel("<HTML><br>On the following 8 steps, we are going to be assigning a function for each port<br>on the main relay box.<br><br>Each port number is identified on the picture below.<br><br></HTML>");
     	else
     		text = new JLabel("<HTML><br>On the following 8 steps, we are going to be assigning a function for each port<br>on the expansion relay box.<br><br>Each port number is identified on the picture below.<br><br></HTML>");
     	panel2.add(text);
     	ImageIcon iconnection = null;
     	iconnection = new ImageIcon(Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/relay_small.png");
 		JLabel c = new JLabel(iconnection);
     	panel2.add(c);
     	panel.add(panel2);
     	if (relay<9)
     		return NextPrevButton(panel, "Main Relay Box");
     	else
     		return NextPrevButton(panel, "Expansion Relay Box");
 	}
 
 	private int ShowAttachment()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Attachments");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	JLabel text = new JLabel("<HTML><br>Please select the attachments you have:<br><br></HTML>");
     	panel2.add(text);
     	panel2.add(attachmentmods);
     	panel.add(panel2);
     	return NextPrevButton(panel, "Attachments");
 		
 	}
 	
 	private int ShowExpansion()
 	{
     	JPanel panel = new JPanel();
 
     	AddPanel(panel);
 
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
 		JLabel steps = new JLabel("Expansion Modules");
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	JLabel text = new JLabel("<HTML><br>Please select the expansion modules you have:<br><br></HTML>");
     	panel2.add(text);
     	panel2.add(expansionmods);
     	panel.add(panel2);
     	return NextPrevButton(panel, "Expansion Modules");
 		
 	}
 	
 	private int ShowRelaySetup()
 	{
     	JPanel panel = new JPanel();
     	AddPanelRelay(panel);
 	    
     	JPanel panel2 = new JPanel();
     	panel2.setLayout(new BoxLayout( panel2, BoxLayout.PAGE_AXIS));
     	JLabel steps=null;
     	if (relay<9)
     		steps = new JLabel("Main Relay Box - Port " + relay);
     	else
     		steps = new JLabel("Expansion Relay Box - Port " + (relay-8));
     		
     	steps.setForeground(new Color(58,95,205));
     	steps.setFont(new Font("Arial", Font.BOLD, 24));
     	panel2.add(steps);
     	steps.setAlignmentX(Component.LEFT_ALIGNMENT);
     	panel2.add(new JLabel(" "));
     	JLabel text=null;
     	if (relay<9)
     		text = new JLabel("<HTML><br>Choose which function you would like to assign for Port " + relay + " of your main relay box:<br><br></HTML>");
     	else
     		text = new JLabel("<HTML><br>Choose which function you would like to assign for Port " + (relay-8) + " of your expansion relay box:<br><br></HTML>");
     	panel2.add(text);
     	text.setAlignmentX(Component.LEFT_ALIGNMENT);
     	
     	JPanel functionpanel = new JPanel();
     	functionpanel.setLayout(new GridLayout( 3,1));
         functionpanel.add(functions[relay]);
         functionpanel.add(functionsettings[relay]);
         functionpanel.add(ports[relay]);
         functionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
         
         panel2.add(functionpanel);
         functionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
         
     	panel.add(panel2);
     	if (relay<9)
     		return NextPrevButton(panel, "Main Relay Box - Port " + relay);
     	else
     		return NextPrevButton(panel, "Expansion Relay Box - Port " + (relay-8));
     		
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
 	private void BuildSettings()
 	{
 	      
 		  ActionListener sliceActionListener = new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
               AbstractButton aButton = (AbstractButton) actionEvent.getSource();
               functionsettings[relay].setBorder(BorderFactory.createTitledBorder(null, aButton.getText(),TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
               CardLayout cl = (CardLayout)(functionsettings[relay].getLayout());
               cl.show(functionsettings[relay], aButton.getText());
               JRadioButton j = (JRadioButton) memsettings.getComponent(1);
               
               if (aButton.getText()=="Time Schedule" && j.isSelected())
               {
             	  cl.show(functionsettings[relay], "Light Schedule");
               }
               RevalidateSettings();
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
             	  JSpinner js=(JSpinner)OverheatSettings.getComponent(1);
             	  js.setModel(new SpinnerNumberModel(30.9,10.0,50.0,0.1));
               	  JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )js.getEditor();
             	  jo.getTextField().setColumns(5);
             	  jo.getFormat().applyPattern("###0.0");  
             	  OverheatSettings.revalidate();
               }
               if (aButton.getText()=="Fahrenheit")
               {
             	  tempunit=0;
             	  JLabel j=(JLabel)OverheatSettings.getComponent(0);
             	  j.setText("Overheat Temperature (\u00b0F): ");
             	  JSpinner js=(JSpinner)OverheatSettings.getComponent(1);
             	  js.setModel(new SpinnerNumberModel(86.9,60.0,150.0,0.1));
             	  OverheatSettings.revalidate();
               	  JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )js.getEditor();
             	  jo.getTextField().setColumns(5);
             	  jo.getFormat().applyPattern("###0.0");  
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
     	Overheat = new JSpinner( new SpinnerNumberModel(86.9,60.0,150.0,0.1));
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
               if (aButton.getText()=="In the code (Recommended for new users)")
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
 		DisplayMem = new JRadioButton("In the code (Recommended for new users)");
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
 	    	functions[a].setLayout(new GridLayout(6,2));
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
 	                	option.setText("Wavemaker (Port 5 and 6 Only)");
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
 
 	        JEditorPane description = new JEditorPane("text/html","<html><p>" + DescButtons[0] + "</p></html>");   
 	        description.setEditable(false);   
 	        description.setOpaque(false);
 	        description.setBorder(BorderFactory.createEmptyBorder()); 
 	        description.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)description.getDocument()).getStyleSheet().addRule(bodyRule);
 		    Timed[a].add(description);
 
 	        Calendar calendar = new GregorianCalendar();
 	        calendar.set(Calendar.HOUR_OF_DAY, 9);
 	        calendar.set(Calendar.MINUTE, 0);
 	        JLabel TimerOnLabel=new JLabel ("Turn on at: ",JLabel.TRAILING);
 	        Timed[a].add(TimerOnLabel);
 	        
 	        JSpinner TimerOn = new JSpinner( new SpinnerDateModel() ); 
 	        JSpinner.DateEditor TimerOnEditor = new JSpinner.DateEditor(TimerOn, "HH:mm"); 
 	        TimerOn.setEditor(TimerOnEditor); 
 	        TimerOn.setValue(calendar.getTime()); // will only show the current time
 	        Timed[a].add(TimerOn);
 	        
 	        calendar.set(Calendar.HOUR_OF_DAY, 19);
 	        JLabel TimerOffLabel=new JLabel ("Turn off at: ",JLabel.TRAILING);
 	        Timed[a].add(TimerOffLabel);
 	        
 	        JSpinner TimerOff = new JSpinner( new SpinnerDateModel() ); 
 	        JSpinner.DateEditor TimerOffEditor = new JSpinner.DateEditor(TimerOff, "HH:mm"); 
 	        TimerOff.setEditor(TimerOffEditor); 
 	        TimerOff.setValue(calendar.getTime()); // will only show the current time
 	        Timed[a].add(TimerOff);	
 	        
 			JLabel DelayedTimerLabel=null;
 			JSpinner DelayedTimer;
 			DelayedTimer = new JSpinner( new SpinnerNumberModel(0,0,30,1) );
 			DelayedTimerLabel=new JLabel ("Delayed Start (m): ",JLabel.TRAILING);
 			Timed[a].add(DelayedTimerLabel);
 			Timed[a].add(DelayedTimer);	
 	        
 	        ApplyLayout(Timed[a],Timedlayout);
 	        
 	        // TimedMemory
 	        
 	        TimedMemory[a] = new JPanel();
 	        SpringLayout TimedMemorylayout=new SpringLayout();
 	        TimedMemory[a].setLayout(TimedMemorylayout);
 	        JEditorPane tmdescription = new JEditorPane("text/html","<html><p>" + DescButtons[0] + "</p></html>");   
 	        tmdescription.setEditable(false);   
 	        tmdescription.setOpaque(false);
 	        tmdescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        tmdescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)tmdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 	        TimedMemory[a].add(tmdescription);
 	        
 		    ButtonGroup TMgroup = new ButtonGroup();
 		    JRadioButton TMOption;
 		    TMOption = new JRadioButton("Daylight");		
 		    TMgroup.add(TMOption);
 		    TMOption.setSelected(true);
 		    TimedMemory[a].add(TMOption);
 		    TMOption = new JRadioButton("Delayed Start Daylight (For MH ballasts)");		
 		    TMgroup.add(TMOption);
 		    TimedMemory[a].add(TMOption);
 		    TMOption = new JRadioButton("Actinic (Turn on/off x minutes before and after Daylights)");		
 		    TMgroup.add(TMOption);
 		    TimedMemory[a].add(TMOption);
 		    TMOption = new JRadioButton("Moonlights/Refugium (Opposite cycle of Daylights)");		
 		    TMgroup.add(TMOption);
 		    TimedMemory[a].add(TMOption);
 	        
 		    ApplyTimedMemoryLayout(TimedMemory[a],TimedMemorylayout);
 	        
 	        // Heater
 			Heater[a] = new JPanel();
 			SpringLayout Heaterlayout=new SpringLayout();
 			Heater[a].setLayout(Heaterlayout);
 
 			JEditorPane hdescription = new JEditorPane("text/html","<html><p>" + DescButtons[1] + "</p></html>");   
 			hdescription.setEditable(false);   
 			hdescription.setOpaque(false);   
 			hdescription.setBorder(BorderFactory.createEmptyBorder()); 
 			hdescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)hdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    Heater[a].add(hdescription);
 
 			JLabel HeaterOnLabel=null;
 			JSpinner HeaterOn;
 			HeaterOn = new JSpinner( new SpinnerNumberModel(75.1,60.0,150.0,0.1) );
 			HeaterOnLabel=new JLabel ("Turn on at (\u00b0F): ",JLabel.TRAILING);
 			Heater[a].add(HeaterOnLabel);
 			Heater[a].add(HeaterOn);
 			Heaterlayout.getConstraints(HeaterOn).setWidth(Spring.constant(70));
 	    	JSpinner.NumberEditor  jh = (JSpinner.NumberEditor )HeaterOn.getEditor();
 	    	jh.getTextField().setColumns(5);
 	    	jh.getFormat().applyPattern("###0.0");
 	    	
 			JLabel HeaterOffLabel=null;
 			JSpinner HeaterOff;
 			HeaterOff = new JSpinner( new SpinnerNumberModel(76.1,60.0,150.0,0.1) );
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
 
 		    JEditorPane cdescription = new JEditorPane("text/html","<html><p>" + DescButtons[2] + "</p></html>");   
 			cdescription.setEditable(false);   
 			cdescription.setOpaque(false);   
 			cdescription.setBorder(BorderFactory.createEmptyBorder()); 
 			cdescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)cdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    Chiller[a].add(cdescription);
 
 			JSpinner ChillerOn;
 			JLabel ChillerOnLabel=null;
 			ChillerOnLabel=new JLabel ("Turn on at (\u00b0F): ",JLabel.TRAILING);
 			ChillerOn = new JSpinner( new SpinnerNumberModel(78.1,60.0,150.0,0.1) );
 			Chiller[a].add(ChillerOnLabel);
 			Chiller[a].add(ChillerOn);
 			Chillerlayout.getConstraints(ChillerOn).setWidth(Spring.constant(70));
 	    	JSpinner.NumberEditor  jc = (JSpinner.NumberEditor )ChillerOn.getEditor();
 	    	jc.getTextField().setColumns(5);
 	    	jc.getFormat().applyPattern("###0.0");
 			
 			JLabel ChillerOffLabel=null;
 			JSpinner ChillerOff;
 			ChillerOffLabel=new JLabel ("Turn off at (\u00b0F): ",JLabel.TRAILING);
 			ChillerOff = new JSpinner( new SpinnerNumberModel(77.1,60.0,150.0,0.1) );
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
 		  
 	        JEditorPane ATOdescription = new JEditorPane("text/html","<html><p>" + DescButtons[3] + "<a href='http://forum.reefangel.com/viewtopic.php?f=7&t=240'> ATO Float Switch Guide</a>.</p></html>");   
 	        ATOdescription.setEditable(false);   
 	        ATOdescription.setOpaque(false);   
 	        ATOdescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        ATOdescription.setBackground(new Color(0,0,0,0)); 
 	        ((HTMLDocument)ATOdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 	        ATO[a].add(ATOdescription);
 
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
 	        ATO[a].add(ATOTimeoutLabel);
 	        ATO[a].add(ATOTimeout);
 	        ATOdescription.addHyperlinkListener(new HyperlinkListener() {   
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
 	              JLabel j = (JLabel)WM[relay].getComponent(3);
 	              if (aButton.getText()=="Constant")
 	              {
 		              j.setText("Cycle every (s): ");
 		              WM[relay].getComponent(5).setVisible(false);
 		              WM[relay].getComponent(6).setVisible(false);
 	              }
 	              else
 	              {
 		              j.setText("Cycle between (s): ");
 		              WM[relay].getComponent(5).setVisible(true);
 		              WM[relay].getComponent(6).setVisible(true);
 	              }
 	              
 //	              CardLayout cl = (CardLayout)(functionsettings[relay].getLayout());
 //	              cl.show(functionsettings[relay], aButton.getText());
 
 	              RevalidateSettings();
 	              }
 	        };
 	        
 	        WM[a] = new JPanel();
 	        SpringLayout WMlayout=new SpringLayout();
 	        WM[a].setLayout(WMlayout);
 
 	        JEditorPane wdescription = new JEditorPane("text/html","<html><p>" + DescButtons[4] + "</p></html>");   
 	        wdescription.setEditable(false);   
 	        wdescription.setOpaque(false);   
 	        wdescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        wdescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)wdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    WM[a].add(wdescription);
 
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
 
 			JEditorPane codescription = new JEditorPane("text/html","<html><p>" + DescButtons[5] + "</p></html>");   
 			codescription.setEditable(false);   
 			codescription.setOpaque(false);   
 			codescription.setBorder(BorderFactory.createEmptyBorder()); 
 			codescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)codescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    CO2Control[a].add(codescription);
 
 			JLabel CO2ControlOnLabel=null;
 			JSpinner CO2ControlOn;
 			CO2ControlOn = new JSpinner( new SpinnerNumberModel(7.52,1.00,14.00,0.01) );
 			CO2ControlOnLabel=new JLabel ("Turn on at pH: ",JLabel.TRAILING);
 			CO2Control[a].add(CO2ControlOnLabel);
 			CO2Control[a].add(CO2ControlOn);
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
 			CO2Controllayout.getConstraints(CO2ControlOff).setWidth(Spring.constant(70));
 	    	jco = (JSpinner.NumberEditor )CO2ControlOff.getEditor();
 	    	jco.getTextField().setColumns(5);
 	    	jco.getFormat().applyPattern("###0.00");			
 			ApplyLayout(CO2Control[a],CO2Controllayout);	        
 	        
 	        // pH Control
 	        
 	        pHControl[a]= new JPanel();
 	        SpringLayout pHControllayout=new SpringLayout();
 	        pHControl[a].setLayout(pHControllayout);
 
 			JEditorPane pdescription = new JEditorPane("text/html","<html><p>" + DescButtons[6] + "</p></html>");   
 			pdescription.setEditable(false);   
 			pdescription.setOpaque(false);   
 			pdescription.setBorder(BorderFactory.createEmptyBorder()); 
 			pdescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)pdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    pHControl[a].add(pdescription);
 
 			JLabel pHControlOnLabel=null;
 			JSpinner pHControlOn;
 			pHControlOn = new JSpinner( new SpinnerNumberModel(8.18,1.00,14.00,0.01) );
 			pHControlOnLabel=new JLabel ("Turn on at pH: ",JLabel.TRAILING);
 			pHControl[a].add(pHControlOnLabel);
 			pHControl[a].add(pHControlOn);
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
 			pHControllayout.getConstraints(pHControlOff).setWidth(Spring.constant(70));
 	    	jcp = (JSpinner.NumberEditor )pHControlOff.getEditor();
 	    	jcp.getTextField().setColumns(5);
 	    	jcp.getFormat().applyPattern("###0.00");
 	    	
 			ApplyLayout(pHControl[a],pHControllayout);
 	        
 	        // Dosing
 	        Dosing[a] = new JPanel();
 	        SpringLayout Dosinglayout=new SpringLayout();
 	        Dosing[a].setLayout(Dosinglayout);
 
 	        JEditorPane ddescription = new JEditorPane("text/html","<html><p>" + DescButtons[7] + "</p></html>");   
 	        ddescription.setEditable(false);   
 	        ddescription.setOpaque(false);   
 	        ddescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        ddescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)ddescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    Dosing[a].add(ddescription);
 
 	        JLabel DosingOnLabel=new JLabel ("Turn on every (m): ",JLabel.TRAILING);
 	        Dosing[a].add(DosingOnLabel);
 	        JSpinner DosingOn;
 	        DosingOn = new JSpinner( new SpinnerNumberModel(60,0,1440,1) );
 	        Dosing[a].add(DosingOn);
 	        
 	        JLabel DosingOffLabel=new JLabel ("for (s): ",JLabel.TRAILING);
 	        Dosing[a].add(DosingOffLabel);
 	        JSpinner DosingOff;
 	        DosingOff = new JSpinner( new SpinnerNumberModel(10,0,255,1) ); 
 	        Dosing[a].add(DosingOff);
 
 	        JLabel DosingOffsetLabel=new JLabel ("Offset (m): ",JLabel.TRAILING);
 	        Dosing[a].add(DosingOffsetLabel);
 	        JSpinner DosingOffset;
 	        DosingOffset = new JSpinner( new SpinnerNumberModel(0,0,1440,1) ); 
 	        Dosing[a].add(DosingOffset);
 
 	        ApplyDosingLayout(Dosing[a],Dosinglayout);	   
 	        
 	        // Delayed On
 	        
 	        Delayed[a] = new JPanel();
 	        SpringLayout Delayedlayout=new SpringLayout();
 	        Delayed[a].setLayout(Delayedlayout);
 
 	        JEditorPane dsdescription = new JEditorPane("text/html","<html><p>" + DescButtons[8] + "</p></html>");   
 	        dsdescription.setEditable(false);   
 	        dsdescription.setOpaque(false);   
 	        dsdescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        dsdescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)dsdescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    Delayed[a].add(dsdescription);
 
 	        JLabel DelayedOnLabel=new JLabel ("Delayed Start (m): ",JLabel.TRAILING);
 	        Delayed[a].add(DelayedOnLabel);
 	        JSpinner DelayedOn;
 	        DelayedOn = new JSpinner( new SpinnerNumberModel(10,0,255,1) );
 	        Delayed[a].add(DelayedOn);
 	        
 	        ApplyLayout(Delayed[a],Delayedlayout);
 
 	        // Opposite
 	        
 	        Opposite[a] = new JPanel();
 	        SpringLayout Oppositelayout=new SpringLayout();
 	        Opposite[a].setLayout(Oppositelayout);
 
 	        JEditorPane odescription = new JEditorPane("text/html","<html><p>" + DescButtons[9] + "</p></html>");   
 	        odescription.setEditable(false);   
 	        odescription.setOpaque(false);   
 	        odescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        odescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)odescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    Opposite[a].add(odescription);
 
 	        JLabel OppositeLabelOn=new JLabel ("Opposite of Port: ",JLabel.TRAILING);
 	        Opposite[a].add(OppositeLabelOn);
 	        JComboBox OppositeOn;
 	        OppositeOn = new JComboBox();
 	        Opposite[a].add(OppositeOn);
 	        
 	        ApplyLayout(Opposite[a],Oppositelayout);	        
 
 	        // Always On
 	        
 	        AlwaysOn[a] = new JPanel();
 	        SpringLayout AlwaysOnlayout=new SpringLayout();
 	        AlwaysOn[a].setLayout(AlwaysOnlayout);
 
 	        JEditorPane adescription = new JEditorPane("text/html","<html><p>" + DescButtons[10] + "</p></html>");   
 	        adescription.setEditable(false);   
 	        adescription.setOpaque(false);   
 	        adescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        adescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)adescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    AlwaysOn[a].add(adescription);
 
 		    AlwaysOn[a].add(new JLabel(""));
 		    AlwaysOn[a].add(new JLabel(""));
 	        
 	        ApplyLayout(AlwaysOn[a],AlwaysOnlayout);	
 
 	        // Not Used
 	        
 	        NotUsed[a] = new JPanel();
 	        SpringLayout NotUsedlayout=new SpringLayout();
 	        NotUsed[a].setLayout(NotUsedlayout);
 
 	        JEditorPane ndescription = new JEditorPane("text/html","<html><p>" + DescButtons[11] + "</p></html>");   
 	        ndescription.setEditable(false);   
 	        ndescription.setOpaque(false);   
 	        ndescription.setBorder(BorderFactory.createEmptyBorder()); 
 	        ndescription.setBackground(new Color(0,0,0,0)); 
 		    ((HTMLDocument)ndescription.getDocument()).getStyleSheet().addRule(bodyRule);
 		    NotUsed[a].add(ndescription);
 
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
 
          	cl.show(functionsettings[a], "Not Used");
 		    
 	        feeding[a] = new JCheckBox ("Turn off this port on \"Feeding\" Mode");
 	        waterchange[a] = new JCheckBox ("Turn off this port on \"Water Change\" Mode");
 	        lightson[a] = new JCheckBox ("Turn on this port on \"Lights On\" mode");
 	        overheat[a] = new JCheckBox ("Turn off this port on \"Overheat\"");
 	    	ports[a] = new JPanel(); 
 	    	ports[a].setLayout(new BoxLayout( ports[a], BoxLayout.PAGE_AXIS));
 	    	ports[a].setBorder(BorderFactory.createTitledBorder(null, "Port Mode",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font.deriveFont(font.getStyle() ^ Font.BOLD)));
 	        ports[a].add(feeding[a]);
 	        if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 	        ports[a].add(waterchange[a]);
 	        if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 	        ports[a].add(lightson[a]);
 	        if (Base.isMacOS()) ports[a].add(new JLabel(" "));
 	        ports[a].add(overheat[a]);
 	        if (Base.isMacOS()) ports[a].add(new JLabel(" "));
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
     	daylightpwm.setLayout(new GridLayout(1,5));
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
 
     	actinicpwm=new JPanel();
     	actinicpwm.setLayout(new GridLayout(1,5));
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
     	
     	for (int i=0;i<6;i++)
     	{
 	    	exppwm[i]=new JPanel();
 	    	exppwm[i].setLayout(new GridLayout(1,5));
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
     	}
     	
     	for (int i=0;i<AIChannels.length;i++)
     	{
 	    	aipwm[i]=new JPanel();
 	    	aipwm[i].setLayout(new GridLayout(1,4));
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
     	}
 
     	for (int i=0;i<RadionChannels.length;i++)
     	{
 	    	rfpwm[i]=new JPanel();
 	    	rfpwm[i].setLayout(new GridLayout(1,4));
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
     	}
     	
     	// daylight 
     	daylightpwmsettings=new JPanel();
         daylightpwmsettings.setLayout(new GridLayout(3,6));
         
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
 		
 		// TODO: BuildSettings
 	}
 	  
 	private void ApplyLayout(JPanel panel, SpringLayout layout)
 	{
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 		layout.getConstraints(panel).setHeight(Spring.constant(130));
 		layout.getConstraints(panel).setWidth(Spring.constant(400));
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(-10));
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
 		Spring y = Spring.constant(0);
 		
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 		
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(-10));
 
 		for (int c=1;c<panel.getComponentCount();c++)
 		{
 			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 			layout.getConstraints(panel.getComponent(c)).setY(y);
 //			layout.getConstraints(panel.getComponent(c+1)).setX(Spring.constant(150));
 //			layout.getConstraints(panel.getComponent(c+1)).setY(y);
 			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c)).getHeight());
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
 	
 	private void ApplyDosingLayout(JPanel panel, SpringLayout layout)
 	{
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 		
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 		
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(-15));
 		
 		
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
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 		
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 		
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(-15));
 		
 		
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
 		Spring y = Spring.constant(5);
 		Spring x = Spring.constant(5);
 		Spring width = Spring.constant(0);
 		
 		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 		
 		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 		y=Spring.sum(y, Spring.constant(-15));
 		
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
 //		Spring y = Spring.constant(5);
 //		Spring x = Spring.constant(5);
 //		Spring width = Spring.constant(0);
 //		
 //		layout.getConstraints(panel.getComponent(0)).setX(Spring.constant(5));
 //		layout.getConstraints(panel.getComponent(0)).setY(Spring.constant(-15));
 //		
 //		y=Spring.sum(y, layout.getConstraints(panel.getComponent(0)).getHeight());
 //		y=Spring.sum(y, Spring.constant(-15));
 //		
 //		
 //		layout.getConstraints(panel.getComponent(1)).setX(Spring.constant(0));
 //		width=layout.getConstraints(panel.getComponent(1)).getWidth();
 //		width=Spring.sum(Spring.constant(5),width);
 //		layout.getConstraints(panel.getComponent(2)).setX(width);
 //		width=Spring.sum(layout.getConstraints(panel.getComponent(2)).getWidth(),width);
 //		width=Spring.sum(Spring.constant(5),width);
 //		layout.getConstraints(panel.getComponent(3)).setX(width);
 //		layout.getConstraints(panel.getComponent(1)).setY(y);
 //		layout.getConstraints(panel.getComponent(2)).setY(y);
 //		layout.getConstraints(panel.getComponent(3)).setY(y);
 //		
 //		y=Spring.sum(y, layout.getConstraints(panel.getComponent(1)).getHeight());
 //		y=Spring.sum(y, Spring.constant(5));
 //
 //		width = Spring.constant(0);
 //		for (int c=4;c<panel.getComponentCount();c+=2)
 //			width=Spring.max(width, layout.getConstraints(panel.getComponent(c)).getWidth());
 //		x=Spring.sum(x, width);
 //		x=Spring.sum(x, Spring.constant(5));
 //		for (int c=4;c<panel.getComponentCount();c+=2)
 //		{
 //			layout.getConstraints(panel.getComponent(c)).setX(Spring.constant(5));
 //			layout.getConstraints(panel.getComponent(c)).setY(y);
 //			layout.getConstraints(panel.getComponent(c)).setWidth(width);
 //			layout.getConstraints(panel.getComponent(c+1)).setX(x);
 //			layout.getConstraints(panel.getComponent(c+1)).setY(Spring.sum(y, Spring.constant(-2)));
 //			y=Spring.sum(y, layout.getConstraints(panel.getComponent(c+1)).getHeight());
 //			y=Spring.sum(y, Spring.constant(5));
 //		}		
 	}
 	
 	private void RevalidateSettings()
 	{
 		if (settingswidth==0)
 		{
 			settingswidth = functionsettings[relay].getWidth();
 		  	SpringLayout layout;
 		  	
 		  	for (int a=1; a<=16;a++)
 		  	{
 		      	layout=(SpringLayout) Timed[a].getLayout();
 		      	layout.getConstraints(Timed[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(Timed[a]);
 		      	Timed[a].revalidate();
 	
 		      	layout=(SpringLayout) TimedMemory[a].getLayout();
 		      	layout.getConstraints(TimedMemory[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(TimedMemory[a]);
 		      	TimedMemory[a].revalidate();
 		      	
 		      	layout=(SpringLayout) Heater[a].getLayout();
 		      	layout.getConstraints(Heater[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(Heater[a]);
 		      	Heater[a].revalidate();
 	
 		      	layout=(SpringLayout) Chiller[a].getLayout();
 		      	layout.getConstraints(Chiller[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(Chiller[a]);
 		      	Chiller[a].revalidate();
 	
 		      	layout=(SpringLayout) ATO[a].getLayout();
 		      	layout.getConstraints(ATO[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(ATO[a]);
 		      	ATO[a].revalidate();
 	
 		      	layout=(SpringLayout) WM[a].getLayout();
 		      	layout.getConstraints(WM[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(WM[a]);
 		      	WM[a].revalidate();
 	
 		      	layout=(SpringLayout) CO2Control[a].getLayout();
 		      	layout.getConstraints(CO2Control[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(CO2Control[a]);
 		      	CO2Control[a].revalidate();
 		      	
 		      	layout=(SpringLayout) pHControl[a].getLayout();
 		      	layout.getConstraints(pHControl[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(pHControl[a]);
 		      	pHControl[a].revalidate();
 		      	
 		      	layout=(SpringLayout) Dosing[a].getLayout();
 		      	layout.getConstraints(Dosing[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(Dosing[a]);
 		      	Dosing[a].revalidate();
 	
 		      	layout=(SpringLayout) Delayed[a].getLayout();
 		      	layout.getConstraints(Delayed[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(Delayed[a]);
 		      	Delayed[a].revalidate();
 	
 		      	layout=(SpringLayout) Opposite[a].getLayout();
 		      	layout.getConstraints(Opposite[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(Opposite[a]);
 		      	Opposite[a].revalidate();
 	
 		      	layout=(SpringLayout) AlwaysOn[a].getLayout();
 		      	layout.getConstraints(AlwaysOn[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
 		      	SwingUtilities.updateComponentTreeUI(AlwaysOn[a]);
 		      	AlwaysOn[a].revalidate();
 	
 		      	layout=(SpringLayout) NotUsed[a].getLayout();
 		      	layout.getConstraints(NotUsed[a].getComponent(0)).setWidth(Spring.constant(settingswidth-35));
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
 				jS=(JSpinner)j.getComponent(2);
 				jS.setModel(new SpinnerNumberModel(75.1,60.0,150.0,0.1));
             	JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 				jS=(JSpinner)j.getComponent(4);
 				jS.setModel(new SpinnerNumberModel(76.1,60.0,150.0,0.1));
             	jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 			}
 			else
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0C): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0C): ");
 				jS=(JSpinner)j.getComponent(2);
 				jS.setModel(new SpinnerNumberModel(25.1,10.0,50.0,0.1));
             	JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 				jS=(JSpinner)j.getComponent(4);
 				jS.setModel(new SpinnerNumberModel(26.1,10.0,50.0,0.1));
             	jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 			}
 			j=(JPanel)functionsettings[a].getComponent(3);
 			if (tempunit==0)
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0F): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0F): ");
 				jS=(JSpinner)j.getComponent(2);
 				jS.setModel(new SpinnerNumberModel(79.1,60.0,150.0,0.1));
             	JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 				jS=(JSpinner)j.getComponent(4);
 				jS.setModel(new SpinnerNumberModel(78.1,60.0,150.0,0.1));
             	jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 			}
 			else
 			{
 				jL=(JLabel)j.getComponent(1);
 				jL.setText("Turn on at (\u00b0C): ");
 				jL=(JLabel)j.getComponent(3);
 				jL.setText("Turn off at (\u00b0C): ");
 				jS=(JSpinner)j.getComponent(2);
 				jS.setModel(new SpinnerNumberModel(28.1,10.0,50.0,0.1));
             	JSpinner.NumberEditor  jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 				jS=(JSpinner)j.getComponent(4);
 				jS.setModel(new SpinnerNumberModel(27.1,10.0,50.0,0.1));
             	jo = (JSpinner.NumberEditor )jS.getEditor();
           	    jo.getTextField().setColumns(5);
           	    jo.getFormat().applyPattern("###0.0");  
 			}
 		}
 	}
 
 	private void ShowHardCodeSettings(boolean bVisible)
 	{
 		for (int a=1;a<=16;a++)
 		{
 			ShowHideComponent(Timed[a],bVisible);
 			ShowHideComponent(Heater[a],bVisible);
 			ShowHideComponent(Chiller[a],bVisible);
 //			ShowHideComponent(ATO[a],bVisible);
 			ShowHideComponent(WM[a],bVisible);
 			ShowHideComponent(CO2Control[a],bVisible);
 			ShowHideComponent(pHControl[a],bVisible);
 			ShowHideComponent(Dosing[a],bVisible);
 			ShowHideComponent(Delayed[a],bVisible);
 //			ShowHideComponent(Opposite[a],bVisible);
 			
 			ATO[a].getComponent(4).setVisible(bVisible);
 			ATO[a].getComponent(5).setVisible(bVisible);
 					
             JRadioButton j = (JRadioButton) memsettings.getComponent(1);
             JRadioButton AOn = (JRadioButton) functions[a].getComponent(0);
 			if (AOn.isSelected())
 			{
 	            CardLayout cl = (CardLayout)(functionsettings[relay].getLayout());
 				
 	            if (j.isSelected())
 	          	  	cl.show(functionsettings[relay], "Light Schedule");
 	            else
 	          	  	cl.show(functionsettings[relay], "Time Schedule");
 	            	
 			}
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
 
 	private void RetundLF()
 	{
 		try {
 			UIManager.setLookAndFeel(lf);
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	}
  
  
 
 
 
