 /*
  * JScroll - the scrollable desktop pane for Java.
  * Copyright (C) 2003 Tom Tessier
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  */
 
 package org.jscroll;
 
 import models.Arteriole;
 import models.Artery;
 import models.Capillary;
 import models.ElasticTube;
 import models.FirstArtery;
 import models.FourthVentricle;
 import models.Hemisphere;
 import models.SAS;
 import models.SpinalCord;
 import models.ThirdVentricle;
 import models.Vein;
 import models.Veinule;
 import models.VenousSinus;
 import models.Ventricle;
 
 import org.jscroll.widgets.*;
 
 import params.SystemParams;
 import params.WindowManager;
 
 import com.display.LineLink;
 import com.display.TubePanel;
 import com.display.TubePanel.TubeClass;
 import com.display.images.IconLibrary;
 
 import display.SEBWindow;
 
 import javax.swing.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 
 /**
  * The main scrollable desktop class.
  * <BR><BR>
  * JScrollDesktopPane builds upon JDesktopPane and JScrollPane to provide
  * a complete virtual desktop environment that enables easy access to internal
  * frames that may have been positioned offscreen. This access is made
  * possible via real-time creation and manipulation of the desktop preferred size.
  * <BR><BR>
  * A toolbar provides a set of buttons along the top of the screen, with each
  * button matched to a corresponding internal frame. When one of these buttons
  * is clicked, the associated frame is centered upon the virtual desktop and
  * selected. The buttons within the toolbar automatically resize as more buttons
  * are added beyond the width of the container.
  * <BR><BR>
  * A JMenuBar may be registered with the scrollable desktop so that the
  * application can provide access to the internal frames via its own menu bar.
  * When the registration is complete, a new JMenu entitled "Window" is added to
  * the supplied JMenuBar, a menu containing <code>Tile</code>,
  * <code>Cascade</code>, and <code>Close</code> options along with dynamically
  * updated shortcuts to any internal frames currently upon the scrollable
  * desktop. The <code>Tile</code> and <code>Cascade</code> options provided by
  * the "Window" menu affect the positions of the internal frames upon the
  * scrollable desktop. <code>Cascade</code> positions each internal frame one
  * after the other in a diagonal sequence crosswise the screen, while
  * <code>Tile</code> positions and resizes the internal frames to fill up
  * all available screen real estate, with no single frame overlapping any other.
  * <BR><BR>
  * JScrollDesktopPane is simply a JPanel and as such may be added to any
  * suitable JPanel container, such as a JFrame. The addition of new internal
  * frames to the JScrollDesktopPane and the registration of menu bars for
  * use by the scrollable desktop is relatively simple: The <code>add</code>
  * method creates a new internal frame and returns a reference to the
  * JInternalFrame instance that was created, while the
  * <code>registerMenuBar</code> method registers the menubar for use by the
  * scrollable desktop. A JMenuBar object may also be registered by passing it
  * as a constructor parameter to the JScrollDesktopPane.
  * <BR><BR>
  * An example usage follows:
  * <BR><BR>
  * <code><pre>
  *    JFrame f = new JFrame("Scrollable Desktop");
  *    f.setSize(300,300);
  *    // prepare the menuBar
  *    JMenuBar menuBar = new JMenuBar();
  *    f.setJMenuBar(menuBar);
  *
  *    // create the scrollable desktop instance and add it to the JFrame
  *    JScrollDesktopPane scrollableDesktop =
  *          new JScrollDesktopPane(menuBar);
  *    f.getContentPane().add(scrollableDesktop);
  *    f.setVisible(true);
  *
  *    // add a frame to the scrollable desktop
  *    JPanel frameContents = new JPanel();
  *    frameContents.add(
  *          new JLabel("Hello and welcome to JScrollDesktopPane."));
  *
  *    scrollableDesktop.add(frameContents);
  * </pre></code>
  *
  * JScrollDesktopPane has been tested under Java 2 JDK versions
  * 1.3.1-b24 on Linux and jdk1.3.0_02 on Windows and Intel Solaris.
  *
  * As of March 14, 2003 it has also been tested on JDK 1.4.1 for Windows.
  *
  * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
  * @version 1.0  12-Aug-2001
  */
 public class JScrollDesktopPane extends JPanel implements DesktopConstants, MouseListener{
 	private LineLink lineClicked = null;
 	private ArrayList<JScrollInternalFrame> internalFrames;
 	private JScrollInternalFrame firstArteryFrame;
 	private JScrollInternalFrame venousSinousFrame;
 	private JScrollInternalFrame ventricleleftFrame;
 	private JScrollInternalFrame ventriclerightFrame;
 	private JScrollInternalFrame thirdVentFrame;
 	private JScrollInternalFrame fourthVentFrame;
 	private JScrollInternalFrame sasFrame;
 	private JScrollInternalFrame spinalFrame;
 
     private static int count; // count used solely to name untitled frames
     private DesktopMediator desktopMediator;
     private ImageIcon defaultFrameIcon;
 
     /**
      * creates the JScrollDesktopPane object, registers a menubar, and assigns
      *      a default internal frame icon.
      *
      * @param mb the menubar with which to register the scrollable desktop
      * @param defaultFrameIcon the default icon to use within the title bar of
      *      internal frames.
      */
     public JScrollDesktopPane(JMenuBar mb, ImageIcon defaultFrameIcon) {
         this();
     	internalFrames = new ArrayList<JScrollInternalFrame>();
         //registerMenuBar(mb);
         this.defaultFrameIcon = defaultFrameIcon;
     }
 
     /**
      * creates the JScrollDesktopPane object and registers a menubar.
      *
      * @param mb the menubar with which to register the scrollable desktop
      */
     public JScrollDesktopPane(JMenuBar mb) {
         this();
     	internalFrames = new ArrayList<JScrollInternalFrame>();
         //registerMenuBar(mb);
     }
 
     /**
      * creates the JScrollDesktopPane object.
      */
     public JScrollDesktopPane() {
         internalFrames = new ArrayList<JScrollInternalFrame>();
         setLayout(new BorderLayout());
         desktopMediator = new DesktopMediator(this);
         addMouseListener(this);
 
     }
 
     /**
      * adds an internal frame to the scrollable desktop
      *
      * @param frameContents the contents of the internal frame
      *
      * @return the JInternalFrame that was created
      */
     public JInternalFrame add(TubePanel frameContents) {
         return add("Untitled " + count++, defaultFrameIcon, frameContents,
             true, -1, -1);
     }
 
     /**
      * adds an internal frame to the scrollable desktop
      *
      * @param title the title displayed in the title bar of the internal frame
      * @param frameContents the contents of the internal frame
      *
      * @return the JInternalFrame that was created
      */
     public JInternalFrame add(String title, TubePanel frameContents) {
         return add(title, defaultFrameIcon, frameContents, true, -1, -1);
     }
 
     /**
      * adds an internal frame to the scrollable desktop
      *
      * @param title the title displayed in the title bar of the internal frame
      * @param frameContents the contents of the internal frame
      * @param isClosable <code>boolean</code> indicating whether internal frame
      *          is closable
      *
      * @return the JInternalFrame that was created
      */
     public JInternalFrame add(String title, TubePanel frameContents,
         boolean isClosable) {
         return add(title, defaultFrameIcon, frameContents, isClosable, -1, -1);
     }
 
     /**
      * adds an internal frame to the scrollable desktop
      *
      * @param title the title displayed in the title bar of the internal frame
      * @param icon the icon displayed in the title bar of the internal frame
      * @param frameContents the contents of the internal frame
      * @param isClosable <code>boolean</code> indicating whether internal frame
      *          is closable
      *
      * @return the JInternalFrame that was created
      */
     public JInternalFrame add(String title, ImageIcon icon,
     		TubePanel frameContents, boolean isClosable) {
         return add(title, icon, frameContents, isClosable, -1, -1);
     }
     
 
     public JInternalFrame addArtery(Hemisphere hemi) {
     	if(hemi != Hemisphere.LEFT && hemi != Hemisphere.RIGHT)
     		return null;
     	Artery art = new Artery("", hemi);
     	return add(art.getName(), new ImageIcon(IconLibrary.ARTERY.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(art, TubeClass.Artery), false);
     }
     
     public JInternalFrame addArteriole(Hemisphere hemi) {
     	if(hemi != Hemisphere.LEFT && hemi != Hemisphere.RIGHT)
     		return null;
     	Arteriole art = new Arteriole("", hemi);
     	return add(art.getName(),new ImageIcon(IconLibrary.ARTERIOLE.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(art, TubeClass.Arteriole), false);
 
     }
     
     public JInternalFrame addCapillary(Hemisphere hemi) {
     	if(hemi != Hemisphere.LEFT && hemi != Hemisphere.RIGHT)
     		return null;
     	Capillary cap = new Capillary("", hemi);
 		return add(cap.getName(),new ImageIcon(IconLibrary.CAPILLARY.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(cap, TubeClass.Capillary), false);
 
     }
     
     public JInternalFrame addVeinule(Hemisphere hemi) {
     	if(hemi != Hemisphere.LEFT && hemi != Hemisphere.RIGHT)
     		return null;
     	Veinule vl = new Veinule("", hemi);
     	return add(vl.getName(),new ImageIcon(IconLibrary.VEINULE.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(vl, TubeClass.Veinule), false);
     }
     
     public JInternalFrame addVein(Hemisphere hemi) {
     	if(hemi != Hemisphere.LEFT && hemi != Hemisphere.RIGHT)
     		return null;
     	Vein v = new Vein("", hemi);
     	return add(v.getName(),new ImageIcon(IconLibrary.VEIN.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(v, TubeClass.Vein), false);
     }
  
 
     /**
      * adds an internal frame to the scrollable desktop.
      * <BR><BR>
      * Propogates the call to DesktopMediator.
      *
      * @param title the title displayed in the title bar of the internal frame
      * @param icon the icon displayed in the title bar of the internal frame
      * @param frameContents the contents of the internal frame
      * @param isClosable <code>boolean</code> indicating whether internal frame
      *          is closable
      * @param x x coordinates of internal frame within the scrollable desktop.
      * @param y y coordinates of internal frame within the scrollable desktop
      *
      * @return the JInternalFrame that was created
      */
     public JInternalFrame add(String title, ImageIcon icon,
     		TubePanel frameContents, boolean isClosable, int x, int y) {
         return desktopMediator.add(title, icon, frameContents,this, isClosable,true, x, y);
     }
     
     public JInternalFrame add(String title, ImageIcon icon,
     		TubePanel frameContents, boolean isClosable, boolean visible, int x, int y) {
         return desktopMediator.add(title, icon, frameContents,this, isClosable,visible, x, y);
     }
 
     /**
      * adds a JInternalFrame to the scrollable desktop.
      *
      * @param f the internal frame of class JScrollInternalFrame to add
      */
     public void add(JInternalFrame f) {
         add(getWrappedFrame(f), -1, -1);
     }
 
     /**
      * adds a JInternalFrame to the scrollable desktop.
      *
      * @param f the internal frame of class JScrollInternalFrame to add
      * @param x x coordinates of internal frame within the scrollable desktop.
      * @param y y coordinates of internal frame within the scrollable desktop
      */
     public void add(JInternalFrame f, int x, int y) {
         desktopMediator.add(getWrappedFrame(f), x, y);
     }
 
     /**
      *  wraps a given internal frame in a JScrollInternalFrame for use
      *  by JScrollDesktopPane
      *
      * @param f the JInternalFrame reference
      *
      * @return
      */
     private JScrollInternalFrame getWrappedFrame(JInternalFrame f) {
         // wrap it in a JScrollInternalFrame
         JScrollInternalFrame b = new JScrollInternalFrame();
         b.setContentPane(f.getContentPane());
         b.setTitle(f.getTitle());
         b.setResizable(f.isResizable());
         b.setClosable(f.isClosable());
         b.setMaximizable(f.isMaximizable());
         b.setIconifiable(f.isIconifiable());
         b.setFrameIcon(f.getFrameIcon());
         b.pack();
         b.saveSize();
 
         b.setVisible(f.isVisible());
 
         return b;
     }
 
     /**
      * removes the specified internal frame from the scrollable desktop
      *
      * @param f the internal frame to remove
      */
     public void remove(JInternalFrame f) {
         f.doDefaultCloseAction();
     }
 
     /**
      * registers a menubar to which the "Window" menu may be applied.
      * <BR><BR>
      * Propogates the call to DesktopMediator.
      *
      * @param mb the menubar to register
      */
     public void registerMenuBar(JMenuBar mb) {
         desktopMediator.registerMenuBar(mb);
     }
 
     /**
      * registers a default icon for display in the title bars of
      *    internal frames
      *
      * @param defaultFrameIcon the default icon
      */
     public void registerDefaultFrameIcon(ImageIcon defaultFrameIcon) {
         this.defaultFrameIcon = defaultFrameIcon;
     }
 
     /**
      * returns the internal frame currently selected upon the
      * virtual desktop.
      * <BR><BR>
      * Propogates the call to DesktopMediator.
      *
      * @return a reference to the active JInternalFrame
      */
     public JInternalFrame getSelectedFrame() {
         return desktopMediator.getSelectedFrame();
     }
 
     /**
      * selects the specified internal frame upon the virtual desktop.
      * <BR><BR>
      * Propogates the call to DesktopMediator.
      *
      * @param f the internal frame to select
      */
     public void setSelectedFrame(JInternalFrame f) {
         desktopMediator.setSelectedFrame(f);
     }
 
     /**
      *  flags the specified internal frame as "contents changed." Used to
      * notify the user when the contents of an inactive internal frame
      * have changed.
      * <BR><BR>
      * Propogates the call to DesktopMediator.
      *
      * @param f the internal frame to flag as "contents changed"
      */
     public void flagContentsChanged(JInternalFrame f) {
         desktopMediator.flagContentsChanged(f);
     }
     
     
     public ArrayList<JScrollInternalFrame> getInternalFrames() {
 		return internalFrames;
 	}
 
 	public void setInternalFrames(ArrayList<JScrollInternalFrame> internalFrames) {
 		this.internalFrames = internalFrames;
 	}
 
 	public void mousePressed(MouseEvent e) {
         
     }
 
     public LineLink getClickedLine(int x, int y) {
    	
     	for(JScrollInternalFrame jsf : internalFrames){
 	    	for (LineLink line : jsf.getLineLinks()) {
 	    		float dist = Math.abs((float) line.getLine().ptSegDist(new Point(x,y)));
 	    		if(dist<20.0f){
 	    			return line;
 	    		}	
 	    	}
     	}
     	return null;
     }
 
     @Override
 	public void paint(Graphics g) {  
         super.paint(g);  
         if(internalFrames == null)
         	return;
         Graphics2D g2 = (Graphics2D)g;  
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                             RenderingHints.VALUE_ANTIALIAS_ON);  
         ArrayList<LineLink> drawedLines = new ArrayList<LineLink>();
         for(JScrollInternalFrame jsf : internalFrames){
         	for(LineLink ll : jsf.getLineLinks()){ 
         		if(drawedLines.contains(ll))// on empeche de dessiner deux fois la meme ligne
         			continue;
             	if(lineClicked == ll){
     	        	g2.setPaint(Color.red);  
             	}else{
             		g2.setPaint(Color.white); 
             	}
             	g2.draw(ll.getLine());
             } 
         }
 
     }
 
 	public void addJIFrameToList(JScrollInternalFrame jScrollInternalFrame) {
 		internalFrames.add(jScrollInternalFrame);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		switch(e.getModifiers()) {
 		case InputEvent.BUTTON1_MASK: {
 			if(internalFrames.size()<2 && e.getButton() == MouseEvent.BUTTON1)
 				return;
 			int x = e.getX();
 			int y = e.getY();
 
 			lineClicked = getClickedLine(x, y);
 			repaint(); 
 			break;
 		}
 		case InputEvent.BUTTON2_MASK: { 
 			if(lineClicked != null){
 				lineClicked.delete();
 				lineClicked = null;
 				repaint();
 			}
 			break;
 		}
 		case InputEvent.BUTTON3_MASK: {
 			lineClicked = null;
 			repaint(); 
 			break;
 		}
 		}
 	}
 	 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public DesktopMediator getDesktopMediator() {
 		return desktopMediator;
 	}
 
 	public void setDesktopMediator(DesktopMediator desktopMediator) {
 		this.desktopMediator = desktopMediator;
 	}
 
 	public void updateLineLink(){
 		for(JScrollInternalFrame jsf : internalFrames){
 			jsf.updateLineLink();
 		}
 	}
 
 	/**
 	 * Remove previous content and init new model
 	 */
 	public void initNew() {
 		removeAllFrame();
 		Dimension panelSize = getSize();
 		Dimension iframeDim = JScrollInternalFrame.DEFAULT_DIMENSION;
 		Point fart_location = new Point((int) (panelSize.getWidth()/2 - iframeDim.getWidth()/2), 10);
 		Point vsinous_location = new Point((int) (panelSize.getWidth()/2 - iframeDim.getWidth()/2), (int) (panelSize.getHeight()-iframeDim.getHeight()-25));
 		
 		FirstArtery fart = new FirstArtery("");
 		setFirstArteryFrame((JScrollInternalFrame) add(fart.getName(),new ImageIcon(IconLibrary.FIRSTARTERY.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(fart, TubeClass.FirstArtery), false,fart_location.x,fart_location.y));
 		VenousSinus vsinous = new VenousSinus("");
 		setVenousSinousFrame((JScrollInternalFrame) add(vsinous.getName(),new ImageIcon(IconLibrary.VENOUSSENOUS.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(vsinous, TubeClass.VenousSinus), false,vsinous_location.x,vsinous_location.y));
 		WindowManager.MAINWINDOW.getTabbedPane().setSelectedIndex(1);
 		
 		
 		// ------------ Definition des blocs LCR -----------
 		// ventricle
 		Ventricle ventricleleft = new Ventricle("", Hemisphere.LEFT);
 		Ventricle ventricleright = new Ventricle("", Hemisphere.RIGHT);
 		// 3V
 		ThirdVentricle thirdVent = new ThirdVentricle("");
 		// 4V
 		FourthVentricle fourthVent = new FourthVentricle("");
 		// SAS
 		SAS sas = new SAS("");
 		// sp. cord
 		SpinalCord spinal = new SpinalCord("");
 		
 		// ---------------- Liaisons des blocs CSF -------
 		// 3V
 		/*thirdVent.addParent(ventricleleft);
 		thirdVent.addParent(ventricleright);
 		// 4V
 		fourthVent.addParent(thirdVent);
 		// SAS
 		sas.addParent(fourthVent);
 		// sp. cord
 		spinal.addParent(sas);*/
 		
 		//BUGGUE L'INTERFACE DE LIAISON
 		setVentricleleftFrame((JScrollInternalFrame) add(ventricleleft.getName(),new ImageIcon(IconLibrary.VENTRICLE.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(ventricleleft, TubeClass.Ventricle), false,false,fart_location.x,fart_location.y));		
 		setVentriclerightFrame((JScrollInternalFrame) add(ventricleright.getName(),new ImageIcon(IconLibrary.VENTRICLE.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(ventricleright, TubeClass.Ventricle), false,false,fart_location.x,fart_location.y));		
 		setThirdVentFrame((JScrollInternalFrame) add(thirdVent.getName(),new ImageIcon(IconLibrary.THIRDVENTRICLE.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(thirdVent, TubeClass.ThirdVentricle), false,false,fart_location.x,fart_location.y));
 		setFourthVentFrame((JScrollInternalFrame) add(fourthVent.getName(),new ImageIcon(IconLibrary.FOURTHVENTRICLE.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(fourthVent, TubeClass.FourthVentricle), false,false,fart_location.x,fart_location.y));		
 		setSasFrame((JScrollInternalFrame) add(sas.getName(),new ImageIcon(IconLibrary.SAS.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(sas, TubeClass.SAS), false,false,fart_location.x,fart_location.y));		
 		setSpinalFrame((JScrollInternalFrame) add(spinal.getName(),new ImageIcon(IconLibrary.SPINAL.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)), new TubePanel(spinal, TubeClass.SpinalCord), false,false,fart_location.x,fart_location.y));	
 		
 		// liens de l'interface
 		getThirdVentFrame().getTubePanel().addLineLinkAsChild(new LineLink(getVentricleleftFrame(),getThirdVentFrame()));
 		getThirdVentFrame().getTubePanel().addLineLinkAsChild(new LineLink(getVentriclerightFrame(),getThirdVentFrame()));
 		getFourthVentFrame().getTubePanel().addLineLinkAsChild(new LineLink(getThirdVentFrame(),getFourthVentFrame()));
 		getSasFrame().getTubePanel().addLineLinkAsChild(new LineLink(getFourthVentFrame(),getSasFrame()));
 		getSpinalFrame().getTubePanel().addLineLinkAsChild(new LineLink(getSasFrame(),getSpinalFrame()));
 		// on ne veut pas que ces frame apparaisse dans cette liste
 		getInternalFrames().remove(getVentricleleftFrame());
 		getInternalFrames().remove(getVentriclerightFrame());
 		getInternalFrames().remove(getThirdVentFrame());
 		getInternalFrames().remove(getFourthVentFrame());
 		getInternalFrames().remove(getSasFrame());
 		getInternalFrames().remove(getSpinalFrame());
 	}
 
 
	private void removeAllFrame() {
 		if(!internalFrames.isEmpty()){
 			for(JScrollInternalFrame jsf : internalFrames){
 				jsf.delete();
 			}
 			internalFrames.clear();
 		}
 	}
 
 	/**
 	 * @return the venousSinousFrame
 	 */
 	public JScrollInternalFrame getVenousSinousFrame() {
 		return venousSinousFrame;
 	}
 
 	/**
 	 * @param venousSinousFrame the venousSinousFrame to set
 	 */
 	public void setVenousSinousFrame(JScrollInternalFrame venousSinousFrame) {
 		this.venousSinousFrame = venousSinousFrame;
 	}
 
 	/**
 	 * @return the firstArteryFrame
 	 */
 	public JScrollInternalFrame getFirstArteryFrame() {
 		return firstArteryFrame;
 	}
 
 	/**
 	 * @param firstArteryFrame the firstArteryFrame to set
 	 */
 	public void setFirstArteryFrame(JScrollInternalFrame firstArteryFrame) {
 		this.firstArteryFrame = firstArteryFrame;
 	}
 	
 	public JScrollInternalFrame getVentricleleftFrame() {
 		return ventricleleftFrame;
 	}
 
 	public void setVentricleleftFrame(JScrollInternalFrame ventricleleftFrame) {
 		this.ventricleleftFrame = ventricleleftFrame;
 	}
 
 	public JScrollInternalFrame getVentriclerightFrame() {
 		return ventriclerightFrame;
 	}
 
 	public void setVentriclerightFrame(JScrollInternalFrame ventriclerightFrame) {
 		this.ventriclerightFrame = ventriclerightFrame;
 	}
 
 	public JScrollInternalFrame getThirdVentFrame() {
 		return thirdVentFrame;
 	}
 
 	public void setThirdVentFrame(JScrollInternalFrame thirdVentFrame) {
 		this.thirdVentFrame = thirdVentFrame;
 	}
 
 	public JScrollInternalFrame getFourthVentFrame() {
 		return fourthVentFrame;
 	}
 
 	public void setFourthVentFrame(JScrollInternalFrame fourthVentSinousFrame) {
 		this.fourthVentFrame = fourthVentSinousFrame;
 	}
 
 	public JScrollInternalFrame getSasFrame() {
 		return sasFrame;
 	}
 
 	public void setSasFrame(JScrollInternalFrame sasFrame) {
 		this.sasFrame = sasFrame;
 	}
 
 	public JScrollInternalFrame getSpinalFrame() {
 		return spinalFrame;
 	}
 
 	public void setSpinalFrame(JScrollInternalFrame spinalFrame) {
 		this.spinalFrame = spinalFrame;
 	}
 
 	public void removeInternalFrame(JScrollInternalFrame jsf){
 		internalFrames.remove(jsf);
 	}
 
 	/**
 	 * Met a jours les frames a partir des info dans les tubes
 	 */
 	public void updateInternalFrame() {
 		for(JScrollInternalFrame frame : getInternalFrames()){
 			frame.updateButtons();
 		}
 		getThirdVentFrame().updateButtons();
 		getThirdVentFrame().updateButtons();
 		getFourthVentFrame().updateButtons();
 		getSasFrame().updateButtons();
 		getSpinalFrame().updateButtons();
 	}
 }
