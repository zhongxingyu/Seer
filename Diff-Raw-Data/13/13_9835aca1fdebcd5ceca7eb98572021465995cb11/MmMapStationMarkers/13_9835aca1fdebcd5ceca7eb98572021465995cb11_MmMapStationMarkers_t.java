 package mmEvents;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.peer.ComponentPeer;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import mmGPSCoordinates.MmMousePoints;
 import mmLanguage.MmLanguage;
 import mmMap.MmMapViewer;
 
 public class MmMapStationMarkers extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	public JFrame markerFrame;
     ArrayList<JLabel> mapMarkers;
 	
 	public JLabel moveableLabel,markerLabel;
 	
 	public boolean moveLabel,labelMoved;
 	
 	Image image;
 	
 	ImageIcon[] icon = new ImageIcon[6];
 	ImageIcon[]	icon_gray = new ImageIcon[6];
 	
 	Image img;
 	
 	public int markerIndex=0;
 	
 	JPanel labelPanel,mainPanel;
 	
 	JPanel p;
 	int panelWidth,panelHeight;
 	
 	public boolean isDeleted; 
 	
 	MmMapViewer mapViewer;
 	
 	ArrayList<Point> pnts = new ArrayList<Point>();
 	
 	public ArrayList<JWindow> markerWindows = new ArrayList<JWindow>();
 	
 	JPanel markerPanel;
 	
 	boolean isSaved,markerSelected=false;
 	
 	public JDialog dialog;
 	
 	public final JWindow window = new JWindow();
 	
     JPanel  titlePanel,titlePanel1;
 	
 	JLabel titleLabel,titleLabel1;
 	
 	int language;
 	
 	int markersLeft = 6;
 	
 	public MmMapStationMarkers(JDialog markerDialog,MmMapViewer mapView)
 	{
 		
 		 super();
 		 
 		 	 
 		 //this.markerFrame = frame;
 		 
 		 this.dialog = markerDialog;
 		 
 		 mapViewer = mapView;
 		
 		 /*this.markerFrame.setLocation(425,75);
 		 //this.markerFrame.setAlwaysOnTop(true);
 		 this.markerFrame.setUndecorated(false);
 		 this.markerFrame.setTitle("Stationer");
 		 
 		 
 		 this.markerFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		 this.markerFrame.setResizable(false);*/
 		 
 		 
 		 p = new JPanel();
 		 
 		 //MediaTracker mt = new MediaTracker(this);
 		 //image = Toolkit.getDefaultToolkit().getImage("images/frame.png");
 		 
 		 //image = new ImageIcon(getClass().getResource("/frame.png")).getImage();
 		 
 		 //mt.addImage(image,0);
 		 		 
 		 //this.markerFrame.setBackground(new Color(255,255,255,255));
 		 //MmMapStationMarkers.setWindowAlpha(this.markerFrame, 0.0f);
 		 
 		 
 		 setOpaque(true);
 		 
 		 mapMarkers = new ArrayList<JLabel>();
 		 
 		 mainPanel=this;
 		 
 		for(int i=0;i<6;i++) 
 		{
 		   //icon[i] = new ImageIcon(System.getProperty("user.dir")+"/images/map_marker_"+Integer.toString((i+1))+".png");
 		   icon[i]  = new ImageIcon(getClass().getResource("/map_marker_"+Integer.toString(i+1)+".png"));
 		}   
 		 
 		
 		 
 		 for(int i=0;i<6;i++) 
 	     {
 		 	//icon_gray[i] = new ImageIcon(System.getProperty("user.dir")+"/images/map_marker_gray_"+Integer.toString((i+1))+".png");
 		 	icon_gray[i] = new ImageIcon(getClass().getResource("/map_marker_gray_"+Integer.toString(i+1)+".png"));
 	     }
 		 
 		 moveableLabel = new JLabel(icon[0]);
 		 
 		 moveableLabel.setVisible(false);
 		 
 		 mapMarkers.add(new JLabel(icon[0]));
 		 mapMarkers.add(new JLabel(icon_gray[1]));
 		 mapMarkers.add(new JLabel(icon_gray[2]));
 		 mapMarkers.add(new JLabel(icon_gray[3]));
 		 mapMarkers.add(new JLabel(icon_gray[4]));
 		 mapMarkers.add(new JLabel(icon_gray[5]));
 		 //mapMarkers.add(moveableLabel);
 		 
 		 
 		 
 		 markerIndex=0;
 		 
 		 selectMarker(markerIndex); 
 		 		 
 		    		
 		 //this.markerFrame.setBackground(new Color(255,255,255,255));
 		 
 		 		 		 
 		 JPanel framePanel = new JPanel();
 		 
 	     setBackground(new Color(230,242,252));
 		 
 		 framePanel.setLayout(new BorderLayout());
 		 framePanel.setBackground(new Color(230,242,252));
 		 
 		 JPanel panel = new JPanel();
 		 panel.setLayout(new GridLayout(0,1));
 		 
 		 titlePanel = new JPanel();
 		 titlePanel.setBackground(new Color(230,242,252));
 		 titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
 		 titleLabel = new JLabel(MmLanguage.language[language][0]);
 		 
 		 titleLabel.setOpaque(false);
 		 titleLabel.setForeground(new Color(24,24,24));
 		 titlePanel.add(titleLabel);
 		 
 		 panel.add(titlePanel);
 		 //framePanel.add(titlePanel,BorderLayout.NORTH);
 		 
 		 
 		 titlePanel1 = new JPanel();
 		 titlePanel1.setBackground(new Color(230,242,252));
 		 titlePanel1.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
 		 titleLabel1 = new JLabel(MmLanguage.language[language][1]);
 		 titleLabel1.setOpaque(false);
 		 titleLabel1.setForeground(new Color(24,24,24));
 		 titlePanel1.add(titleLabel1);
 		 
 		 panel.add(titlePanel1);
 		 
 		 framePanel.add(panel,BorderLayout.NORTH);
 		 
 		 markerPanel = new JPanel()
 		 {
 			 /**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			protected void paintComponent(Graphics g)
 			 {
 				 //super.paintComponent(g);
 				 Graphics2D graphics = (Graphics2D) g;
 				 graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
 							RenderingHints.VALUE_ANTIALIAS_ON);
 				 
 				 Image img = new ImageIcon(getClass().getResource("/frame.png")).getImage(); 				 
 				 graphics.drawImage(img, 0, 0,null);
 				 //repaint();
 			 }
 		 };
 		 
 		 GridLayout grid = new GridLayout(1,0);
 		 //grid.setVgap(2);
 		 
 		 markerPanel.setPreferredSize(new Dimension(425,75));
 		 //markerPanel.setSize(250, 75);
 		 //markerPanel.setLocation(0, 0);
 		 //markerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
 		 
 		 //markerPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		 markerPanel.setLayout(grid);
 		 
 		 for(int i=0;i<mapMarkers.size();i++)
 		      markerPanel.add(mapMarkers.get(i));
 		 
 		 markerPanel.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent event) {
 				// TODO Auto-generated method stub
 				System.out.println("mouse entered");
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 								
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 			}
 			 
 		 });
 		 
 		 markerPanel.addMouseMotionListener(new MouseMotionListener(){
 
 			@Override
 			public void mouseDragged(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				
 				
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				
 			}
 			 
 		 });
 		 
 		  
 		 
 		
 		 //markerPanel.add(moveableLabel);
 		 
 		 framePanel.add(markerPanel,BorderLayout.SOUTH);
 		 
 		 for(int i=0;i<6;i++)
 		 {
 			JWindow window1 = new JWindow(); 
 		    window1.setBackground(new Color(1,1,1,0));
 		    window1.pack();
 		 
 		    window1.setAlwaysOnTop(true);
 		    window1.setVisible(false);
 		    markerWindows.add(window1);
 		 }    
 		 
 		 add(framePanel);
 		 //add(markerPanel);
 		    
 	}
 	
 	public void selectMarker(int index)
 	{
 		    
 		        
 		    /*for(int i=0;i<mapMarkers.size();i++)
 		    	if(!mapMarkers.get(i).isVisible())
 		    		count++;
 		    if(markerIndex!=0 && isDeleted)
 		        markerIndex=count-1;*/
 		    
 		    
 		    
 		    mapMarkers.get(markerIndex).addMouseListener(new MouseListener(){
 		      
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				//Point pt = new Point();
 				//Component cmpt = getComponentAt(event.getPoint());
 				//SwingUtilities.convertPointToScreen(pt, mapMarkers.get(markerIndex));
 							
 			
 		 	    //window.setLocation(pt);
 		 	    
 		 	    //window.setFocusable(true);
 			   
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent event) {
 				// TODO Auto-generated method stub
 				//moveableLabel =  (JLabel) mapMarkers.get(0);
 				//markerFrame.setAlwaysOnTop(false);
 				//markerFrame.repaint();
 				JLabel lb = (JLabel) markerPanel.getComponentAt(event.getPoint());
 				
                 
 			    if(!isDeleted)
 			    {	
 				   int x = event.getPoint().x - moveableLabel.getWidth()/2;
 				   int y = event.getPoint().y - moveableLabel.getHeight()/2;
 				   Point pt = new Point(x,y);
 				
				   
 				
 				   SwingUtilities.convertPointToScreen(pt, mapMarkers.get(markerIndex));
 					
				   System.out.println("pts "+pt);
 			    				   
 				   moveableLabel = new JLabel(mapMarkers.get(markerIndex).getIcon());
 			    
 				   moveableLabel.setLocation(event.getPoint());
 			    
 				   moveableLabel.setIcon(icon[markerIndex]);
 			    
 				   markerWindows.get(markerIndex).add(moveableLabel);
 			    
 				   markerWindows.get(markerIndex).setVisible(true);
 				   markerWindows.get(markerIndex).pack();
 		
 			    
 				   markerWindows.get(markerIndex).setBackground(new Color(1,1,1,0));
 				   markerWindows.get(markerIndex).setVisible(true);
 				   markerWindows.get(markerIndex).setLocation(pt);
 				   markerWindows.get(markerIndex).setAlwaysOnTop(true);
 				   markerWindows.get(markerIndex).repaint();
 			    
 			    }
 			    
 			   /* if(isDeleted)
 			    {	
 				   int x = event.getPoint().x - moveableLabel.getWidth()/2;
 				   int y = event.getPoint().y - moveableLabel.getHeight()/2;
 				   Point pt = new Point(x,y);
 				
 				   if(markerIndex!=0)
 				       markerIndex = mapViewer.geoPos.size()-1;
 				
 				   SwingUtilities.convertPointToScreen(pt, mapMarkers.get(markerIndex));
 					
 			    				   
 				   moveableLabel = new JLabel(mapMarkers.get(markerIndex).getIcon());
 			    
 				   moveableLabel.setLocation(event.getPoint());
 			    
 			    
 				   markerWindows.get(markerIndex).add(moveableLabel);
 			    
 				   markerWindows.get(markerIndex).setVisible(true);
 				   markerWindows.get(markerIndex).pack();
 		
 			    
 				   markerWindows.get(markerIndex).setBackground(new Color(1,1,1,0));
 				   markerWindows.get(markerIndex).setVisible(true);
 				   markerWindows.get(markerIndex).setLocation(pt);
 				   markerWindows.get(markerIndex).toFront();
 				   markerWindows.get(markerIndex).repaint();
 			    
 			    }*/
 			    
 			    
 				repaint();
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				//Point pnt = new Point((markerWindows.get(markerIndex).getLocation().x+window.getWidth())-370,(markerWindows.get(markerIndex).getLocation().y+window.getHeight())-80);
 				//Point pnt = new Point((markerWindows.get(markerIndex).getLocation().x+window.getWidth()),(markerWindows.get(markerIndex).getLocation().y+window.getHeight()));
				Point pnt = new Point((markerWindows.get(markerIndex).getLocation().x+window.getWidth())-415,(markerWindows.get(markerIndex).getLocation().y+window.getHeight())-15);
 				
 				//JOptionPane.showMessageDialog(mainPanel, "marker panel "+Integer.toString(markerIndex));
 				
 				if(mapViewer.checkDistances(mapViewer.getGeoPosition(pnt))||mapViewer.geoPos.isEmpty() && markerSelected)
 				{
 					//JOptionPane.showMessageDialog(null, pnt);
 					//markerFrame.setAlwaysOnTop(true);
 					mapViewer.geoPos.add(mapViewer.getGeoPosition(pnt));	
 					mapViewer.initialGeoPos.add(mapViewer.getGeoPosition(pnt));
 					MmMousePoints pnts = new MmMousePoints(pnt);
 				    mapViewer.mousePos.add(pnts);
 				    int numStations = mapViewer.geoPos.size(); 
 				    if(mapViewer.swingPos.isEmpty() || mapViewer.swingPos.size()==1)
 				    {	
 				    	mapViewer.events.addStation(mapViewer.geoPos.get(numStations-1).getLatitude(), mapViewer.geoPos.get(numStations-1).getLongitude(), numStations-1,numStations,20,false);
 				    }    
 				    else
 				    	mapViewer.events.addStation(mapViewer.geoPos.get(numStations-1).getLatitude(), mapViewer.geoPos.get(numStations-1).getLongitude(), mapViewer.swingPos.size(),numStations,20,false);
 				   
 				    pnts.drawRegion();
 					mapViewer.indexPos=0;
 					mapViewer.drawPoints();
 					markerWindows.get(markerIndex).setVisible(false);
 					mapMarkers.get(markerIndex).setVisible(false);
 					
 					if(mapViewer.isStationDelete)
 						mapViewer.isStationDelete = false;
 						
 					if(mapViewer.mousePos.size()>=2)
 					{
 						mapViewer.addEdge();
 					}
 					
 					markerSelected=false;
 					
 					if(markerIndex+1<mapMarkers.size())
 					{
 						markerIndex++;
 						mapMarkers.get(markerIndex).setIcon(icon[markerIndex]);
 						selectMarker(markerIndex);
 						isSaved=false;
 						
 					}
 					else if(markerIndex+1==mapMarkers.size())
 						markersLeft = 0;
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(mainPanel, MmLanguage.language_exception[language][1]);
 					markerWindows.get(markerIndex).setVisible(false);
 				}
 				
 				
 				    
 			}
 			 
 		 });
 		 
 		 mapMarkers.get(markerIndex).addMouseMotionListener(new MouseMotionListener(){
 		   //window.addMouseMotionListener(new MouseMotionListener(){   
 
 			@Override
 			public void mouseDragged(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 		        //JLabel lb = (JLabel) mapMarkers.get(markerIndex);
 		        //lb.setLocation(event.getX()+icon.getIconWidth()/2,event.getY()+icon.getIconHeight());
 				
 		        
 		        //System.out.println("clicked "+event.getX()+"  "+event.getY()+"  "+lb.getLocation());
 		        //lb.repaint();
 		        //frame.repaint();
 				int x = event.getPoint().x - moveableLabel.getWidth()/2;
 				int y = event.getPoint().y - moveableLabel.getHeight()/2;
 				Point pt = new Point(x,y);
 				//Component cmpt = getComponentAt(event.getPoint());
 				SwingUtilities.convertPointToScreen(pt, mapMarkers.get(markerIndex));
 				
 				
 				//markerWindows.get(markerIndex).setAlwaysOnTop(true);		
 		 	    markerWindows.get(markerIndex).setLocation(pt);
 		 	    markerWindows.get(markerIndex).repaint();
 		 	    markerSelected = true;
 		 	   //JOptionPane.showMessageDialog(mainPanel, window.getLocation());
 		 	    
 				//moveableLabel.setLocation(pt.x+icon.getIconWidth()/2,pt.y+icon.getIconHeight());
 				//moveableLabel.repaint();
 		 	    
 		 	    repaint();
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				/*mapMarkers.get(markerIndex).setCursor(new Cursor(Cursor.HAND_CURSOR));
 				//if(moveLabel)
 				{	
 				   moveableLabel.setVisible(true);
 				   moveableLabel.setLocation(event.getX()-icon.getIconWidth()/2, event.getY()-icon.getIconHeight());
 				   System.out.println("move label "+moveableLabel.getLocation()+ "  "+event.getX()+" "+event.getY());
 				} 
 				
 				System.out.println("Entered "+event.getX());
 				if(event.getX()>=0)
 				{
 					moveLabel=false;
 					labelMoved=true;
 					moveableLabel.setVisible(false);
 				}*/
 				
 				JComponent cmp = (JComponent)event.getSource();
 			    cmp.setToolTipText("Drag och släppa station på kartan");
 				
 			}
 			 
 		 });
 
 	}
 	
 	public int getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(int language) {
 		this.language = language;
 	}
 	
 	public void setTitleLabel()
 	{
 		titleLabel.setText(MmLanguage.language[language][0]);
 		titleLabel1.setText(MmLanguage.language[language][1]);
 		
 	}
 
 	
 		
 	public void setLabelIcon(int index)
 	{
 		mapMarkers.get(index).setIcon(icon[index]);
 	}
 	
 	public void showMarker(int index)
 	{
 		
 		
 		//JOptionPane.showMessageDialog(mainPanel, markerWindows.get(index));
 		//moveableLabel = new JLabel(icon[index]);
 		 
 		// moveableLabel.setVisible(true);
 		 
 		 
 		// mapMarkers.add(moveableLabel);
 		 
 		markerWindows.get(index).setAlwaysOnTop(true);
 		markerWindows.get(index).setVisible(true);
 		markerWindows.get(index).repaint();
 		
 		
 		
 		mapViewer.revalidate();
 	}
 	
 	public JLabel getMarkerLabel(int index)
 	{
 		return mapMarkers.get(index);
 	}
 	
 	public void showMarkers()
 	{
 		for(int i=0;i<markerWindows.size();i++)
 		{
 			markerWindows.get(markerIndex).toFront();
 			markerWindows.get(markerIndex).repaint();
 		}
 	}
 	
 	public void setMarkerIndex(int index)
 	{
 		markerIndex=index;
 	}
 	
 	public void hideMarkers()
 	{
 		markerIndex=0;
 		
 		//JOptionPane.showMessageDialog(null, "mapMarkers "+mapMarkers.size());
 		
 		
 						
 		markerPanel.removeAll();
 		
 		markerPanel.revalidate();
 		
 		markerPanel.validate();
 		
 		for(int i=0;i<6;i++)
 		{
 			
 			//markerWindows.get(0).setVisible(false);
 			//markerWindows.get(0).remove(moveableLabel);
 			JLabel lb = new JLabel();
 			mapMarkers.get(i).add(lb);
 			
 			markerIndex++;
 			
 		}
 		
 		for(int i=0;i<6;i++)
 		{
 			
 			//markerWindows.get(markerIndex).setVisible(false);
 			//markerWindows.get(0).remove(moveableLabel);
 			markerPanel.add(mapMarkers.get(i));
 			markerPanel.getComponent(i).setVisible(false);
 			mapMarkers.get(i).setVisible(false);
 			
 			
 		}
 		
 		markerPanel.revalidate();
 		
 		//markerWindows.clear();
 	
 		//mapMarkers.clear();				
 		//markerPanel.removeAll();
 		
 		//markerPanel.revalidate();
 		markerIndex = 5; 
 		
 		markersLeft=0;
 		
 		selectMarker(markerIndex);
 	}
 	
 	
 	public void hideMarker(int index)
 	{
 	
 		if(markerIndex!=markerWindows.size())
 		{	
 		   //JOptionPane.showMessageDialog(null, index+"  "+markerIndex);	
 		   markerWindows.get(index).setVisible(false);
 		   markerWindows.get(index).remove(moveableLabel);
 		   mapMarkers.get(markerIndex).setIcon(icon_gray[index]);
 		}   
 		
 		 
 				
 		markerIndex--;
 		
 		
 		if(markerIndex==0)
 		{
 			//JOptionPane.showMessageDialog(null, "markers "+mapMarkers.size()+"  "+markerIndex);
 			mapMarkers.remove(markerIndex);
 		    mapMarkers.add(new JLabel(icon[0]));
 		    //markerIndex=index;
 		}
 		
 		if(markerIndex!=0)
 		{
 			//JOptionPane.showMessageDialog(null, "markers "+mapMarkers.size()+"  "+markerIndex);
 			mapMarkers.remove(markerIndex);
 		    mapMarkers.add(new JLabel(icon[index]));
 		    //markerIndex=index;
 		}     
 		
 		if(markerIndex<0)
 		{
 			
 			mapMarkers.remove(0);
 		    mapMarkers.add(0, new JLabel(icon[0]));
 			markerIndex=0;
 		}		
 		
 		
 		
 		mapMarkers.clear();				
 		markerPanel.removeAll();
 		
 		markerPanel.revalidate();
 		
 		System.out.println("markers left "+markersLeft);
 		
 		if(markersLeft==0)
 			index=5;
 		
 		if(markerIndex>=1)
 		{
 			if(index==5)
 				markerIndex = 5;
 			
 			for(int i=0;i<markerIndex;i++)
 			{
 				JLabel lb = new JLabel();
 				mapMarkers.add(lb);
 			}
 		}
 		
 		System.out.println("markerIndex "+markerIndex);
 		
 		mapMarkers.add(new JLabel(icon[markerIndex]));
 		
 		System.out.println("marker index "+index);
 		
 		if(index!=5)
 		{	
 		    for(int i=markerIndex+1;i<6;i++)
 		    {
 			    JLabel lb = new JLabel(icon_gray[i]);
 			    mapMarkers.add(lb);
 		    }
 		
 		    for(int i= 0;i<6;i++)
 			    markerPanel.add(mapMarkers.get(i));
 		
 		    markerPanel.revalidate();
 		
 		    //JOptionPane.showMessageDialog(null, "markers "+mapMarkers.size()+"  "+markerIndex);
 		
 		   mapMarkers.get(markerIndex).setVisible(true);
 		
 		   markersLeft++;
 		   selectMarker(markerIndex);
 		
 		   //markerWindows.get(markerIndex).remove(moveableLabel);
 		}
 		
 		if(index==5)
 		{	
 		    /*for(int i= markerIndex+1;i<6;i++)
 		    {
 			    JLabel lb = new JLabel(icon_gray[i]);
 			    mapMarkers.add(lb);
 		    } */
 		
 		    for(int i= 0;i<6;i++)
 			    markerPanel.add(mapMarkers.get(i));
 		
 		    markerPanel.revalidate();
 		
 		    //JOptionPane.showMessageDialog(null, "markers "+mapMarkers.size()+"  "+markerIndex);
 		
 		   mapMarkers.get(markerIndex).setVisible(true);
 		
 		   markersLeft++;
 		   selectMarker(markerIndex);
 		
 		   //markerWindows.get(markerIndex).remove(moveableLabel);
 		}		
 	}
 	
 	
 	
 	
 	public int getMapMarkersIndex()
 	{
 		return markerIndex;
 	}
 	
 	public void resetMapMarkers()
 	{
 	    markerIndex=0;
 	    setLabelIcon(markerIndex);
 	    if(!mapMarkers.get(markerIndex).isVisible())
 	    	mapMarkers.get(markerIndex).setVisible(true);
 	    
 	    for(int i=1;i<mapMarkers.size();i++)
 	    {	
 	    	mapMarkers.get(i).setVisible(true);
 	    	mapMarkers.get(i).setIcon(icon_gray[i]);
 	    	
 	    }
 	    
 	    mapMarkers.clear();				
 		markerPanel.removeAll();
 		
 		markerPanel.revalidate();
 		
 		JLabel lb = new JLabel(icon[0]);
 		mapMarkers.add(lb);
 		
 		for(int i=1;i<6;i++)
 		{
 			lb = new JLabel(icon_gray[i]);
 			mapMarkers.add(lb);
 		}
 	    
 	    
 	    for(int i=0;i<mapMarkers.size();i++)
 		      markerPanel.add(mapMarkers.get(i));
 	    markerPanel.revalidate();
 	    markerIndex=0;
 	    selectMarker(markerIndex);
 	    
 	}
 	
 	
 	public void setSaved(boolean save)
 	{
 		isSaved = save;
 	}
 	
 	public boolean getSavedState()
 	{
 		return isSaved;
 	}
 	
 	
 	
 	/*public static void setWindowAlpha(Window w, float alpha) {
 	    
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
 	}*/
 	
 
 }
