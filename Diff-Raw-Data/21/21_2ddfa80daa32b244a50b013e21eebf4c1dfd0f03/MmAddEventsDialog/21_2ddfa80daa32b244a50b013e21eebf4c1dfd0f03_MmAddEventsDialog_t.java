 package mmEvents;
 import java.awt.BorderLayout;
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.peer.ComponentPeer;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import mmEvents.MmAddEvents;
 import mmStationEvents.MmGlobalMarkerEvents;
 import mmStationEvents.MmStationEvents;
 import mmLanguage.MmLanguage;
 import mmMap.*;
 import mmMediaViewer_Player.*;
 
 
 
 import javax.swing.*;
 
 import org.jdesktop.swingx.JXMapKit;
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 
 public class MmAddEventsDialog extends JPanel implements ActionListener {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	public JDialog frame;
 	
 	Image image;
 	
 	JPanel p,buttonPanel;
 	
 	int locationx,locationy;
 	
 	String selectedText=null;
 	JLabel textLabel;
 	JPanel textPanel;
 	String[] textStrings;
 	
 	int componentIndex;
 	
 	MmStationEvents currentStation;
 	MmGlobalMarkerEvents currentMarker;
 	
 	int panelWidth,panelHeight;
 	
 	JLabel mapMarkerLabel;
 	
 	Point orignalWindowLocation,currentWindowLocation;
 	public boolean windowDisplaced=false;
 	
 	JCheckBox chkbx,panoramaChkbx,cmbgChkbx;
 	JButton bt,bt1,bt2;
 	String choosenText;
 	
 	int eventIndex;
 	
 	MmAudioVideoPlayer audioPlayer = new MmAudioVideoPlayer();
 	
 	MmMapViewer mapViewer;
 	
 	JFrame mainWindow;
 
     int language;
 	
 
 
 	public MmAddEventsDialog(JDialog frame1,int width,int height,MmMapViewer mapView)
 	{
 		super();
 		p = new JPanel();
 		
 		mapViewer = mapView;
 		
 		panelWidth=width;
 		panelHeight=height;
 		
 		image = new ImageIcon(getClass().getResource("/frame.png")).getImage();
 		
 		
 		
 		this.frame = frame1;
 		
 		ImageIcon icon = new ImageIcon("/Users/Umapathi/Desktop/MinnesmarkEditor/images/map_marker.png");
 		
 		
 				
 		this.frame.setUndecorated(true);
 		this.frame.setAlwaysOnTop(true);
 		this.frame.setBackground(new Color(255,255,255,0));
 		
 		MmAddEventsDialog.setWindowAlpha(this.frame, 0.0f);
 			
 		setOpaque(true);
 		
 		mapMarkerLabel = new JLabel(icon);
 		
 		p.add(mapMarkerLabel);
 		
 		p.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 			    windowDisplaced=true;	
 			}
 
 			@Override
 			public void mousePressed(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				Point pnt = new Point(event.getX()-300,event.getY()-300);
 				mapViewer.geoPos.add(mapViewer.getGeoPosition(pnt));
 				System.out.println(mapMarkerLabel.getLocation()+"frame loocation  "+frame.getLocation());
 				mapViewer.setFocusable(true);
 				
 				frame.setVisible(false);
 				frame.setFocusable(false);
 				
 			}
 			
 		});
 
 		add(p);
 
 	}
 	
 	
 	public JFrame getMainWindow() {
 		return mainWindow;
 	}
 
 	public void setMainWindow(JFrame mainWindow) {
 		this.mainWindow = mainWindow;
 	}
 	
 	public int getLanguage() {
 		return language;
 	}
 
 
 	public void setLanguage(int language) {
 		this.language = language;
 	}
 	
 	
 	public MmAddEventsDialog(JDialog frame1,int index,JPanel panel,MmGlobalMarkerEvents marker,boolean focusEvents)
 	{
 			
 		super();
 		p = new JPanel();
 		
 		panelWidth=300;
 		panelHeight=200;
 		componentIndex=index;
 		
 		setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
 		
 		textPanel = panel;
 		
 		currentMarker = marker;
 		
 		panoramaChkbx = new JCheckBox(MmLanguage.language_mediaevents[language][0]);
 		
 		cmbgChkbx  = new JCheckBox(MmLanguage.language_mediaevents[language][1]);
 		
 		panoramaChkbx.setEnabled(false);
 		cmbgChkbx.setEnabled(false);
 		
 		chkbx = new JCheckBox(MmLanguage.language_mediaevents[language][2]);
 		
 		bt = new JButton("Ok");
 		
         bt1 = new JButton();
         
         bt1.setEnabled(false);
         
         bt2 = new JButton(MmLanguage.language_mediaevents[language][3]);
         
         bt2.setEnabled(false);
 		
 		textStrings = ((JLabel) textPanel.getComponent(componentIndex)).getText().split(":");
 		
 		
 				
 		if(isImageFile(textStrings[0]))
 		{
 			panoramaChkbx.setEnabled(true);
 			bt1.setText(MmLanguage.language_mediaevents[language][10]);
 		}
 		
 		if(isAudio_VideoFile(textStrings[0]))
 		{
 			if(isAudioFile(textStrings[0]))
 			    bt1.setText(MmLanguage.language_mediaevents[language][4]);
 			else
 				bt1.setText(MmLanguage.language_mediaevents[language][5]);
			
			bt1.setEnabled(false);
 		}
 		
 		if((textStrings[0].contains(".txt")))
 		{
 			bt1.setText(MmLanguage.language_mediaevents[language][6]);
 			
 		}
 		
 		String label = ((JLabel) textPanel.getComponent(componentIndex)).getText();
 		
 		if(label.contains(MmLanguage.language_mediaevents[language][0]))
 		{
 			panoramaChkbx.setSelected(true);
 			cmbgChkbx.setSelected(false);
 		}
 		
 		if(label.contains(MmLanguage.language_mediaevents[language][7]))
 		{
 			chkbx.setSelected(true); 
 		}
 		
 		if(label.contains(MmLanguage.language_mediaevents[language][8])||label.contains(MmLanguage.language_mediaevents[language][9]))
 		{
 			panoramaChkbx.setSelected(false);
 			cmbgChkbx.setSelected(true);
 			chkbx.setEnabled(false);	
 		}
 		
 		textStrings = ((JLabel) textPanel.getComponent(componentIndex)).getText().split(":");
 		
 		panoramaChkbx.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				if(panoramaChkbx.isSelected())
 					cmbgChkbx.setEnabled(false);
 				else
 					cmbgChkbx.setEnabled(true);
 			}
 			
 		});
 		
 		cmbgChkbx.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 				JLabel lb = (JLabel) textPanel.getComponent(componentIndex);
 				ImageIcon image1 = new ImageIcon(lb.getName());
 				if(image1.getIconWidth()<=512 && image1.getIconHeight()<=512)
 				{	
 				    if(cmbgChkbx.isSelected())
 				    {	
 					    panoramaChkbx.setEnabled(false);
 					    chkbx.setEnabled(false);
 				    }    
 				    else 
 				    {	
 					    panoramaChkbx.setEnabled(true);
 					    chkbx.setEnabled(true);
 				    }    
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, MmLanguage.language_mediaException[language][0]+" "+image1.getIconWidth()+" och "+image1.getIconHeight());
 					cmbgChkbx.setSelected(false);
 					
 				}
 				
 				if(lb.getName().contains(".obj")||lb.getName().contains(".osg"))
 				{
 					 if(cmbgChkbx.isSelected())
 					 {		 
 						 panoramaChkbx.setEnabled(false);
 						 chkbx.setEnabled(false);
 					 }		 
 					 else 
 					 {	 
 					   panoramaChkbx.setEnabled(true);
 					   chkbx.setEnabled(true);
 					 }  
 				}
 				
 			}
 			
 		});
 		
 		if(isImageFile(textStrings[0]))
 		{
 			
 			panoramaChkbx.setEnabled(true);
 			cmbgChkbx.setEnabled(true);
 		}
 		
 		if(textStrings[0].contains(".obj")||textStrings[0].contains(".osg"))
 		{
 			cmbgChkbx.setEnabled(true);
 		}
 		
 		
 		System.out.println("data "+textStrings.length);
 	
 		p.setLayout(new GridLayout(0,2));
 				
 		MediaTracker mt = new MediaTracker(this);
 		image = new ImageIcon(getClass().getResource("/frame.png")).getImage();
 		
 		mt.addImage(image,0);
 		
 		this.frame = frame1;
 		
 		
 				
 		this.frame.setUndecorated(true);
 		
 		this.frame.setBackground(new Color(255,255,255));
 		
 		
 			
 		setOpaque(true);
 		
 		bt.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				frame.setVisible(false);
 				JLabel lb = (JLabel) textPanel.getComponent(componentIndex);
 				lb.setText(textStrings[0]);
 				if(panoramaChkbx.isSelected() && !chkbx.isSelected())
 				{	
 					lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][0]);
 				}
 				
 				if(cmbgChkbx.isSelected() && !chkbx.isSelected())
 				{	
 					if(isImageFile(textStrings[0]))
 				        lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][8]);
 				    else
 			            lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][9]);
 				}
 				
 				if(chkbx.isSelected())
 				{
 					if(panoramaChkbx.isSelected())
 					   lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][0]+":"+MmLanguage.language_mediaevents[language][7]);
 					else if(cmbgChkbx.isSelected())
 						lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][8]+":"+MmLanguage.language_mediaevents[language][7]);	
 					else
 						lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][7]);
 				}
 				
 				currentMarker.getLabels().get(componentIndex).setText(lb.getText());
 				
 				textPanel.revalidate();
 				textPanel.repaint();
 			}
 			
 		});
 		
 		
 		bt1.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				JLabel lb = (JLabel) textPanel.getComponent(componentIndex);
 				
                 
 				
 				if(isImageFile(lb.getText()))
 				{	
 				   JDialog frame2 = new JDialog(mainWindow);
 				   MmImageViewer imageViewer = new MmImageViewer(frame2,lb.getName());
 				   imageViewer.setOpaque(true);
 			       frame2.setContentPane(imageViewer);
 			       frame2.pack();
 			       frame2.setSize(425, 475);
 			       frame2.setVisible(true);
 				}   
 				else if(isAudio_VideoFile(lb.getText()))
 				{
					//bt2.setEnabled(true);
					bt2.setEnabled(false);
 				    audioPlayer.setAudioFile(lb.getName());
 				    
 				    if(isAudioFile(lb.getText()))
 				       audioPlayer.audioPlay(mainWindow);
 				    else
 				       audioPlayer.videoPlay(mainWindow);
 				    
 				}
 				if((lb.getText().equalsIgnoreCase(".txt")))
 				{	
 				   JDialog frame = new JDialog(mainWindow);
 				   MmMessageViewer messageViewer = new MmMessageViewer(frame,lb.getName());
 				   messageViewer.setOpaque(true);
 			       frame.setContentPane(messageViewer);
 			       frame.pack();
 			       frame.setSize(425, 475);
 			       frame.setVisible(true);
 				}
 			}
 			
 		});
 		
 		bt2.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				audioPlayer.audioStop();
 				bt2.setEnabled(false);
 			}
 			
 		});
 		
 				
 		p.add(panoramaChkbx);
 		p.add(chkbx);
 		p.add(cmbgChkbx);
 		p.add(bt);
 		p.add(bt1);
 		p.add(bt2);
 		p.setBorder(BorderFactory.createEtchedBorder());
 		add(p);
 		
 		
 	}
 	
 	
 	
 	public MmAddEventsDialog(JDialog frame1,int index,JPanel panel,MmStationEvents station,boolean focusEvents,int lang)
 	{
 			
 		super();
 		p = new JPanel();
 		
 		panelWidth=300;
 		panelHeight=200;
 		componentIndex=index;
 		
 		this.language = lang;
 		
 		setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
 		
 		textPanel = panel;
 		
 		currentStation = station;
 		
 		panoramaChkbx = new JCheckBox(MmLanguage.language_mediaevents[language][0]);
 		
 				
 		panoramaChkbx.setEnabled(false);
 		
 		chkbx = new JCheckBox(MmLanguage.language_mediaevents[language][2]);
 		
 		bt = new JButton("Ok");
 		
 		bt1 = new JButton();
 		
 		bt2 = new JButton(MmLanguage.language_mediaevents[language][3]);
 		
 		bt2.setEnabled(false);
 		
 		textStrings = ((JLabel) textPanel.getComponent(componentIndex)).getText().split(":");
 		
 					
 		if(isImageFile(textStrings[0]))
 		{
 			panoramaChkbx.setEnabled(true);
 			bt1.setText(MmLanguage.language_mediaevents[language][10]);
 		}
 		
 		if(isAudio_VideoFile(textStrings[0]))
 		{
 			bt1.setText(MmLanguage.language_mediaevents[language][5]);
			bt1.setEnabled(false);
 		}
 		
 		if(isAudioFile(textStrings[0]))
		{	
 		    bt1.setText(MmLanguage.language_mediaevents[language][4]);
		    bt1.setEnabled(false);
		}    
 		
 		if((textStrings[0].contains(".txt")))
 		{
 			bt1.setText(MmLanguage.language_mediaevents[language][6]);
 			
 		}
 		
 		if(textStrings.length==2)
 		{	
 			if(textStrings[1].contains(MmLanguage.language_mediaevents[language][0]))
 				panoramaChkbx.setSelected(true);
 			if(textStrings[1].contains("skatt"))
 				chkbx.setSelected(true);
 		}
 		
 		if(textStrings.length==3)
 		{	
 			if(textStrings[1].contains(MmLanguage.language_mediaevents[language][0]))
 				panoramaChkbx.setSelected(true);
 			if(textStrings[2].contains(MmLanguage.language_mediaevents[language][7]))
 				chkbx.setSelected(true);
 		}
 		
 		
 		p.setLayout(new GridLayout(0,2));
 				
 		MediaTracker mt = new MediaTracker(this);
 		image = new ImageIcon(getClass().getResource("/frame.png")).getImage();
 		
 		mt.addImage(image,0);
 		
 		this.frame = frame1;
 				
 		this.frame.setUndecorated(true);
 		
 		this.frame.setBackground(new Color(255,255,255));
 		
 			
 		setOpaque(true);
 		
 		bt.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				frame.setVisible(false);
 				JLabel lb = (JLabel) textPanel.getComponent(componentIndex);
 				lb.setText(textStrings[0]);
 				if(panoramaChkbx.isSelected() && !chkbx.isSelected())
 				{	
 					lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][0]);
 					
 				}   
 				
 				if(chkbx.isSelected())
 				{
 					if(panoramaChkbx.isSelected())
 					{
 						lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][0]+":"+MmLanguage.language_mediaevents[language][7]);
 					}   
 					else
 						lb.setText(lb.getText()+":"+MmLanguage.language_mediaevents[language][7]);	
 				}
 				
 				currentStation.getLabels().get(componentIndex).setText(lb.getText());
 				
 				textPanel.revalidate();
 				textPanel.repaint();
 			}
 			
 		});
 
 		
 		bt1.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				JLabel lb = (JLabel) textPanel.getComponent(componentIndex);
 				if(isImageFile(lb.getText()))
 				{	
 				   JDialog frame = new JDialog(mainWindow);
 				   MmImageViewer imageViewer = new MmImageViewer(frame,lb.getName());
 				   imageViewer.setOpaque(true);
 			       frame.setContentPane(imageViewer);
 			       frame.pack();
 			       frame.setSize(425, 475);
 			       frame.setVisible(true);
 				}   
 				else if(isAudio_VideoFile(lb.getText()))
 				{
 				    audioPlayer.setAudioFile(lb.getName());
 				    audioPlayer.videoPlay(mainWindow);	
				    //bt2.setEnabled(true);
				    bt2.setEnabled(false);
 							    
 				}
 				else if(isAudioFile(lb.getName()))
 				{
 					 audioPlayer.setAudioFile(lb.getName());  
 					 audioPlayer.audioPlay(mainWindow);
					 //bt2.setEnabled(true);
					 bt2.setEnabled(false);
 				}
 				
 				if((lb.getText().contains(".txt")))
 				{	
 				   JDialog frame = new JDialog(mainWindow);
 				   MmMessageViewer messageViewer = new MmMessageViewer(frame,lb.getName());
 				   messageViewer.setOpaque(true);
 			       frame.setContentPane(messageViewer);
 			       frame.pack();
 			       frame.setSize(425, 475);
 			       frame.setVisible(true);
 				}
 				
 				
 			}
 			
 		});
 
 		bt2.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				audioPlayer.audioStop();
 				bt2.setEnabled(false);
 			}
 			
 		}); 
 				
 		p.add(panoramaChkbx);
 		p.add(chkbx);
 		p.add(bt);
 		p.add(bt1);
 		p.add(bt2);
 		p.setBorder(BorderFactory.createEtchedBorder());
 		add(p);
 		
 		
 	}
 	
 	public void setLanguageText()
 	{
 
 		panoramaChkbx.setText(MmLanguage.language_mediaevents[language][0]);
 		
 		chkbx.setText(MmLanguage.language_mediaevents[language][2]);
 		
 		bt2.setText(MmLanguage.language_mediaevents[language][3]);
 		
 					
 		if(isImageFile(textStrings[0]))
 		{
 			
 			bt1.setText(MmLanguage.language_mediaevents[language][10]);
 		}
 		
 		if(isAudio_VideoFile(textStrings[0]))
 		{
 			bt1.setText(MmLanguage.language_mediaevents[language][5]);
 		}
 		
 		if(isAudioFile(textStrings[0]))
 		    bt1.setText(MmLanguage.language_mediaevents[language][4]);
 		
 		if((textStrings[0].contains(".txt")))
 		{
 			bt1.setText(MmLanguage.language_mediaevents[language][6]);
 			
 		}
 		
 		
 	}
 	
 	public boolean isImageFile(String mediaFile)
 	{
 		String[] extensions = { "bmp","BMP","jpg","JPG","jpeg","JPEG","png","PNG","tiff","TIFF","tif","TIF","gif","GIF"};
 		
 		for(int i=0;i<extensions.length;i++)
 		{	
 		   if(mediaFile.contains(extensions[i]))
 		   {
 			    return true;
 		   }
 		}	   
 		
 		return false;
 		
 		
 	}
 	
 	public boolean isAudio_VideoFile(String mediaFile)
 	{
 		if(mediaFile.contains(".m4v")||mediaFile.contains(".M4V"))
 		{
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	public boolean isAudioFile(String mediaFile)
 	{
 		if(mediaFile.contains(".mp3")||mediaFile.contains(".MP3")||mediaFile.contains(".m4a")||mediaFile.contains(".M4A")||mediaFile.contains(".aiff")||mediaFile.contains(".AIFF"))
 		{
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	public JPanel getTextPanel() {
 		return textPanel;
 	}
 
 	public void setTextPanel(JPanel textPanel) {
 		this.textPanel = textPanel;
 	}
 
 	public int getEventIndex() {
 		return eventIndex;
 	}
 
 	public void setEventIndex(int eventIndex) {
 		this.eventIndex = eventIndex;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public static void setWindowAlpha(Window w, float alpha) {
 		    
 			ComponentPeer peer = w.getPeer();
 		    if (peer == null) {
 		        return;
 		    }
 		    Class< ? extends ComponentPeer> peerClass = peer.getClass();
 
 		    //noinspection EmptyCatchBlock
 		    try {
 		        Class< ?> nativeClass = Class.forName("apple.awt.CWindow");
 		        if (nativeClass.isAssignableFrom(peerClass)) {
 		            Method setAlpha = nativeClass.getMethod(
 		                    "setAlpha", float.class);
 		            setAlpha.invoke(peer, Math.max(0.0f, Math.min(alpha, 1.0f)));
 		      }
 		    } catch (ClassNotFoundException e) {
 		    } catch (NoSuchMethodException e) {
 		    } catch (IllegalAccessException e) {
 		    } catch (InvocationTargetException e) {
 		    }
 	 }
 	 
 	 public boolean getWindowDisplacment()
 	 {
 		 return windowDisplaced;
 	 }
 	 
 
 }
