 /* net.relet.freimap.VisorFrame.java
 
   This file is part of the freimap software available at freimap.berlios.de
 
   This software is copyright (c)2007 Thomas Hirsch <thomas hirsch gmail com>
 
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License with
   the Debian GNU/Linux distribution in file /doc/gpl.txt
   if not, write to the Free Software Foundation, Inc., 59 Temple Place,
   Suite 330, Boston, MA 02111-1307 USA
 */
 
 package net.relet.freimap;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.io.File;
 import java.text.DateFormat;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 
 import org.ho.yaml.Yaml;
 
 /*
 todo dimension -> configfile
      sensible method names
      rename x and y into lon and lat
 */
 
 public class VisorFrame extends JPanel implements ActionListener, ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener {
   double scale=1.0d;  // current scaling
   int zoom = 0;       // zoom factor, according to OSM tiling
   int w=800,h=600;    //screen width, hight
   int cx=400, cy=300; //center of screen
 
   int timelinex0=w/3, timelinex1=11*w/12;
   long crtTime;
   boolean playing=false;
   //crtTime = the second which ought to be displayed, according to user gestures or the flow of time
   //adjustedTime = the above, adjusted to a nearby time which can actually be displayed
   
   ImageIcon logo1   = new ImageIcon(getClass().getResource("/gfx/logo1.png"));
   ImageIcon logo2   = new ImageIcon(getClass().getResource("/gfx/logo2.png"));
   ImageIcon play    = new ImageIcon(getClass().getResource("/gfx/play.png"));
   ImageIcon stop    = new ImageIcon(getClass().getResource("/gfx/stop.png"));
   ImageIcon visible = new ImageIcon(getClass().getResource("/gfx/eyeopen.png"));
   ImageIcon dimmed  = new ImageIcon(getClass().getResource("/gfx/eyedim.png"));
   ImageIcon hidden  = new ImageIcon(getClass().getResource("/gfx/eyeclosed.png"));
 
   public static Font mainfont = new Font("SansSerif", 0, 12),
                      mainfontbold = new Font("SansSerif", Font.BOLD, 12),
                      smallerfont = new Font("SansSerif", 0, 9);
 
   public static Color fgcolor = new Color(20,200,20),     //used for text, lines etc., accessed globally! FIXME move these into colorscheme!
                 bgcolor = new Color(64,128,64,196),       //used for transparent backgrounds of most status boxes
                 fgcolor2 = new Color(150,150,255),        //used for foreground of link status boxes
                 bgcolor2 = new Color(40,40,192,196);      //used for transparent backgrounds of link status boxes
   ColorScheme cs = ColorScheme.NO_MAP;
 
   int mousex=0, mousey=0;
 
   int      selectedTime; //a pixel value, describing the position of the mouse where it currently touches the timebar
   
   Runtime runtime;
 
   DateFormat dfdate=DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY),
              dftime=DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
   
   Vector<VisorLayer> layers = new Vector<VisorLayer>();
   Vector<String> layerids = new Vector<String>();
   VisorLayer activeLayer;
 
   Image buf; //double buffer
 
   Converter converter = new Converter();
 
   String location = "http://relet.net/trac/freimap";
   NameFinder namefinder = new NameFinder();
 
   public VisorFrame() {
 
     this.addComponentListener(this);
     this.addMouseListener(this);
     this.addMouseMotionListener(this);
     this.addMouseWheelListener(this);
     runtime=Runtime.getRuntime();
     
     initDialogs();
     initZoom(0, cx, cy);
   }
 
   public void addLayer(String id, VisorLayer layer) {
     addLayer(id, layer, false);
   }
   public void addLayer(String id, VisorLayer layer, boolean active) {
     layer.setConverter(converter);
     layer.setDimension(w,h);
     layer.setZoom(zoom);
     if (active) activeLayer=layer;
     layers.add(layer);
     layerids.add(id);
   }
   public void removeLayer(VisorLayer layer) {
     layerids.remove(layers.indexOf(layer));
     layers.remove(layer);
   }
   public Dimension getPreferredSize() {
     return new Dimension(w,h);
   }
   
   public void componentHidden(ComponentEvent e) {}
   public void componentMoved(ComponentEvent e) {}
   public void componentShown(ComponentEvent e) {}
   public void componentResized(ComponentEvent e) {
     w = this.getWidth();
     h = this.getHeight();
     cx = w / 2;
     cy = h / 2;
 
     buf=this.createImage(w,h);
 
     for (int i=0;i<layers.size();i++) {
       layers.elementAt(i).setDimension(w, h);
     }
   }
 
   public void paint(Graphics gra) {
     if (buf==null) {
       buf=this.createImage(w,h);
     }
     Graphics2D g=(Graphics2D)buf.getGraphics();
     g.setColor(cs.getColor(ColorScheme.Key.MAP_BACKGROUND));
     g.fillRect(0,0,w,h);
 
     //draw all layers
     for (int i=0;i<layers.size();i++) {
       layers.elementAt(i).paint(g);
     }
 
     //draw logos
     g.drawImage(logo1.getImage(), new AffineTransform(1d,0d,0d,1d,20d,-40d+h-logo1.getIconHeight()), this);
     g.drawImage(logo2.getImage(), new AffineTransform(1d,0d,0d,1d,20d+logo1.getIconWidth(),-40d+h-logo2.getIconHeight()), this);
 
     //draw time line
 
     g.setPaint(bgcolor);
     g.setStroke(new BasicStroke((float)3f));
     int x0=timelinex0, x1=timelinex1; 
     g.draw(new Line2D.Double(x0, h-57, x1, h-57));
 
     long fUT = Long.MAX_VALUE, lUT = -1;
     for (int i=0;i<layers.size();i++) {
       DataSource source=layers.elementAt(i).getSource();
       if (source==null) continue;
       long sfUT = source.getFirstUpdateTime(),    //the time interval which can theoretically be fetched by the source
            slUT = source.getLastUpdateTime();
       if ((sfUT>1) && (sfUT<fUT)) fUT=sfUT;
       if ((slUT>1) && (slUT>lUT)) lUT=slUT;
     }
 
     for (int i=0;i<layers.size();i++) {
       VisorLayer layer=layers.elementAt(i);
       DataSource source=layer.getSource();
       if (source==null) continue;
 
       long fAT = source.getFirstAvailableTime(), //the time interval which can actually displayed
            lAT = source.getLastAvailableTime();
 
       if ((fAT > 0 ) && (fUT != lUT)) {
         int tmin = (int)Math.round((double)(fAT - fUT) / (lUT - fUT) * (x1-x0) + x0),
             tmax = (int)Math.round((double)(lAT - fUT) / (lUT - fUT) * (x1-x0) + x0);
         g.setPaint(fgcolor);
         g.setStroke(new BasicStroke((float)3f));
         g.draw(new Line2D.Double(tmin, h-57-i*3, tmax, h-57-i*3));
 
         if ((selectedTime>0) && (selectedTime < tmax)) {
           g.setPaint(Color.green);
           g.setStroke(new BasicStroke((float)2f));
           g.draw(new Line2D.Double(selectedTime, h-70, selectedTime, h-50));
         }
       }
 
       g.setPaint(fgcolor);
       g.setStroke(new BasicStroke((float)1f));
       g.draw(new Line2D.Double(x0, h-65, x0, h-55));
       g.draw(new Line2D.Double(x1, h-65, x1, h-55));
       g.setFont(smallerfont);
       g.drawString(dfdate.format(fUT*1000), x0, h-45);
       g.drawString(dftime.format(fUT*1000), x0, h-35);
       g.drawString(dfdate.format(lUT*1000), x1, h-45);
       g.drawString(dftime.format(lUT*1000), x1, h-35);
 
       g.setPaint(Color.green);
       if (lUT>fUT) {
         int xc = (int)Math.round((double)(crtTime - fUT) / (lUT - fUT) * (x1-x0) + x0);
         g.draw(new Line2D.Double(xc, h-65, xc, h-55));
         g.drawString(dfdate.format(crtTime*1000), xc, h-75);
         g.drawString(dftime.format(crtTime*1000), xc, h-65);
       }
     }
 
     //draw layer name + visibility toggle
     for (int i=0;i<layers.size();i++) {
       VisorLayer layer=layers.elementAt(i);
       g.setFont(mainfont);
       g.setColor(fgcolor);
       Image image;
       switch(layer.getVisibility()) {
         case VisorLayer.VISIBILITY_NOT: {
           image = hidden.getImage();
           break;
         }
         case VisorLayer.VISIBILITY_DIM: {
           image = dimmed.getImage();
           break;
         }
         default: {
           image = visible.getImage();
           break;
         }
       }
       g.drawImage(image, 20, 40+i*20, this);
       if (layer.equals(activeLayer)) {
         g.setFont(mainfontbold);
       }
       g.drawString(layerids.elementAt(i), 50, 55+i*20);
     }
  
     //draw play/stop button
     if (playing) {
       g.drawImage(play.getImage(), x0-30, h-70, this);
     } else {
       g.drawImage(stop.getImage(), x0-30, h-70, this);
     }
 
     //draw status bars
     g.setStroke(new BasicStroke((float)1f));
     Shape header = new Rectangle2D.Double(-1,-1,w+2,30);
     //Shape menus = new Rectangle2D.Double(-1,30,w+2,27);
     Shape footer = new Rectangle2D.Double(-1,h-30,w+2,30);
     g.setColor(bgcolor);
     g.fill(header);
     //g.fill(menus);
     g.fill(footer);
     g.setColor(fgcolor);
     g.draw(header);
     //g.draw(menus);
     g.draw(footer);
     g.setFont(mainfont);
     //location = namefinder.getLocation();
     g.drawString("{ "+location+" };", 10, 20); //TODO: replace this string by location information gathered from osm namefinder api :) 
     g.drawString("zoom " + zoom, w/4, 20);
     g.drawString("lon " + converter.viewXToLon(mousex), w/2, 20);
     g.drawString("lat " + converter.viewYToLat(mousey), 3*w/4, 20);
     g.drawString(new Date(crtTime*1000).toString(), 10, h-10);
 
     long free  =runtime.freeMemory(),
          //total =runtime.totalMemory(),
          max   =runtime.maxMemory();
     g.drawString("resource usage: "+free*100/max+"%", 3*w/4, h-10);
 
     gra.drawImage(buf, 0, 0, this);
     paintChildren(gra);
   }
 
   double refx, refy, refscale;
   public void saveScale() {
     refscale=scale;
   }
   
   private void initZoom(int zoom, int viewX, int viewY)
   {
 	  converter.initZoom(zoom, viewX, viewY);
 	  for (int i=0;i<layers.size();i++) {
             layers.elementAt(i).setZoom(zoom);
           }
   }
   
   private void centerOn(Point p) {
     converter.setWorld(
     		converter.viewToWorldX(p.x) - cx,
     		converter.viewToWorldY(p.y) - cy);
   }
 
   public double sqr(double x) { 
     return x*x;
   }
   public double dist (double x1, double x2, double y1, double y2) {
     return Math.sqrt(sqr(x1-x2)+sqr(y1-y2));
   }
   
   public void mouseMoved(MouseEvent e) {
     mousex = e.getX();
     mousey = e.getY();
     
     double lon = converter.viewXToLon(mousex);
     double lat = converter.viewYToLat(mousey);
 
     if ((mousey>h-100)&&(mousex >= timelinex0)&&(mousex <= timelinex1)) {
       activeLayer.mouseMoved(0,0); //unset any mouse motion      
       selectedTime = mousex;
     } else {
       activeLayer.mouseMoved(lat, lon);
       selectedTime = 0;
     }
     repaint();
   }
 
   public void mouseClicked(MouseEvent e) { 
     mousex = e.getX();
     mousey = e.getY();
     
     double lon = converter.viewXToLon(mousex);
     double lat = converter.viewYToLat(mousey);
 
     switch (e.getButton()) {
       case MouseEvent.BUTTON1: {
         switch (e.getClickCount()) {
           case 1: {
            if ((mousex>20) && (mousex<40)&& (mousey>40) && (mousey<40+layers.size()*20)) {
              int selectedLayer = (mousey-40) / 20;
               layers.elementAt(selectedLayer).toggleVisibility();
            } else if ((mousex>40) && (mousex<150)&& (mousey>40) && (mousey<40+layers.size()*20)) {
              int selectedLayer = (mousey-40) / 20;
               activeLayer.mouseMoved(0,0);
               activeLayer = layers.elementAt(selectedLayer);
             } else if ((mousey>h-100)&&(mousex >= timelinex0)&&(mousex <= timelinex1)) {
               setCurrentTime(mousex-timelinex0);
             } else if ((mousey>h-70)&&(mousey<h-45)&&(mousex >= timelinex0-30)&&(mousex <= timelinex1)) {
               playing=!playing;
               if (crtTime==0) setCurrentTime(0);
             }
             this.repaint();
             break;
           }
           case 2: {
               //zoom=zoom+1; //DEBUG only
               //initZoom(zoom, mousex, mousey);
               System.out.println("Clicked on: "+lat+" "+lon);
 //            centerOn(e.getPoint());
             break;
           }
         }
         break;
       }
       case MouseEvent.BUTTON3: {
         activeLayer.mouseClicked(lat, lon, MouseEvent.BUTTON3);
       }
     }
   }
 
   void setCurrentTime(int x) {
     long fUT = Long.MAX_VALUE, lUT = 0; //move this to an extra function
     for (int i=0;i<layers.size();i++) {
       DataSource source=layers.elementAt(i).getSource();
       if (source==null) continue;
       long sfUT = source.getFirstUpdateTime(),    //the time interval which can theoretically be fetched by the source
            slUT = source.getLastUpdateTime();
       if ((sfUT>1) && (sfUT<fUT)) fUT=sfUT;
       if ((slUT>1) && (slUT>lUT)) lUT=slUT;
     }
 
     long time = (long)(fUT + (lUT - fUT) * ((double)x) / (timelinex1-timelinex0));
     crtTime=time;
   }
 
   int mrefx, mrefy, mouseMode, refPosition, refZoom;
 
   public void mousePressed(MouseEvent e) {
     mrefx = e.getX();
     mrefy = e.getY();
     mouseMode = e.getButton();
    refPosition = (mrefy-40) / 20;
     refZoom = zoom;
   }
 
   public void mouseDragged(MouseEvent e) {
     mousex = e.getX();
     mousey = e.getY();
     if ((mousey>h-100)&&(mousex >= timelinex0)&&(mousex <= timelinex1)) {
       setCurrentTime(mousex-timelinex0);
    } else if ((mousex>20) && (mousex<150)&& (mousey>40) && (mousey<40+layers.size()*20)) {
      //int targPosition = (mousey-40) / 20;
     } else {
       switch(mouseMode) {
         case MouseEvent.BUTTON1: {
           converter.setWorldRel(mrefx - mousex, mrefy - mousey);
           //namefinder.setLocation(converter.viewYToLat(cy), converter.viewXToLon(cx), zoom);
           
           mrefx = mousex;
           mrefy = mousey;
           
           repaint();
         	  
           break;
         }
         case MouseEvent.BUTTON3: {
           int steps = (mousey - mrefy) / 20;
           zoom = Math.min(22, Math.max(0, refZoom + steps));
    	  initZoom(zoom, mrefx, mrefy);
 	  scale = converter.getScale();
           repaint();
        }
       }
     }
   }
    
   public void mouseWheelMoved(MouseWheelEvent e) {
     saveScale();
     
     Point p = e.getPoint();
     
 	zoom += ((e.getWheelRotation() < 0) ? +1 : -1);
 	zoom = Math.min(22, Math.max(0, zoom));
 	
 	initZoom(zoom, p.x, p.y);
 	
     // Calculate the unit size
 	scale = converter.getScale();
 
 	repaint();
 
   }
   public void mouseEntered(MouseEvent e) {}
   public void mouseExited(MouseEvent e) {}
   public void mouseReleased(MouseEvent e) {
    if ((mousex>20) && (mousex<150)&& (mousey>40) && (mousey<40+layers.size()*20)) {
       if (refPosition >= layers.size()) return;
      int targPosition = (mousey-40) / 20;
       VisorLayer ref = layers.elementAt(refPosition),
 		 swap = layers.elementAt(targPosition);
       String     refID = layerids.elementAt(refPosition),
 		 swapID = layerids.elementAt(targPosition);
       layers.setElementAt(ref, targPosition);
       layerids.setElementAt(refID, targPosition);
       layers.setElementAt(swap, refPosition);
       layerids.setElementAt(swapID, refPosition);
     }
   }
 
   public void nextFrame() {
     if (playing) crtTime += 1;
     boolean repaint = false;
     for (int i=0; i<layers.size(); i++) {
       DataSource source = layers.elementAt(i).getSource();
       if (source!=null) {
         repaint = repaint || layers.elementAt(i).setCurrentTime(crtTime);
       }
     }
     if (repaint) this.repaint();
   }
 
 /* DIALOGS ***************************************************************************/ 
   JDialog about=new JDialog();
   JDialog filter=new JDialog();
 
   JCheckBox  filterEnable   = new JCheckBox("Enable Display Filter");
   JTextField filterText     = new JTextField(30);
   JComboBox  filterRange    = new JComboBox(new String[]{"IP address", "identifier"});
   JCheckBox  caseSens       = new JCheckBox("Case Sensitive");
   JCheckBox  regEx          = new JCheckBox("Regular Expression");
   JButton    filterApply    = new JButton("Apply");
   public void initDialogs() {
       //about dialog foo
       about.setTitle("About freimap");
       Container c = about.getContentPane();
       JTextArea text = new JTextArea("\n  freimap - \n" + 
                        "  Free and open source software since 2006.\n" + 
                        "  Licence: GPL.\n\n" + 
                        "  Credits: \n"+
                        "\tThomas Hirsch \t- Coding\n" + 
                        "\tAlexander Morlang \t- Networking\n" + 
                        "\tFraunhofer FOKUS \t- Support\n" + 
                        "\tRobert Schuster \t- Coding\n" + 
                        "\tFrithjof Hammer \t- Testing\n" + 
                        "\tand all of the freifunk communities.\n");
       text.setEditable(false);
       text.setColumns(40);
       text.setRows(10);
       text.setBackground(Color.black);
       text.setForeground(Color.green);
       c.add(text);
 
       //display filter dialog
       filter.setTitle("Display Filter");
       c = filter.getContentPane();
       c.setBackground(Color.black);
       c.setLayout(new GridLayout(6,1));
       
       c.add(filterEnable);
       c.add(filterText);
       c.add(filterRange);
       c.add(caseSens);
       c.add(regEx);
       c.add(filterApply);
 
       filterApply.addActionListener(this);
   }
 
   public void actionPerformed (ActionEvent e) {
     if (e.getSource().equals(filterApply)) {
       boolean doFilter = filterEnable.isSelected();
       String match = doFilter?filterText.getText():null;
       Iterator<VisorLayer> i = layers.iterator();
       while(i.hasNext()) {
         i.next().setDisplayFilter(match, filterRange.getSelectedIndex(), caseSens.isSelected(), regEx.isSelected());
       }
     } else 
     if (e.getActionCommand().equals("About")) {
       about.pack();
       about.setVisible(true);
     } else 
     if (e.getActionCommand().equals("Display Filter...")) {
       filter.pack();
       filter.setVisible(true);
     }
   }
 }
