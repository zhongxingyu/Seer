 /* net.relet.freimap.VisorLayer.java
 
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
 import java.awt.geom.*;
 import java.awt.event.MouseEvent;
 import java.text.DateFormat;
 import java.util.*;
 import java.util.regex.*;
 
 import javax.swing.border.LineBorder;
 
 public class NodeLayer implements VisorLayer, DataSourceListener {
 
   Converter converter;
 
   double scale;  // current scaling
   int zoom;       // zoom factor, according to OSM tiling
   int w, h;    //screen width, hight
   int cx, cy; //center of screen
 
   Vector<FreiNode> nodes; //vector of known nodes
   Vector<FreiLink> links; //vector of currently displayed links
   Hashtable<String, Float> availmap; //node availability in percent (0f-1f)
   Hashtable<String, NodeInfo> nodeinfo=new Hashtable<String, NodeInfo>(); //stores nodeinfo upon right click
   Hashtable<FreiLink, LinkInfo> linkinfo=new Hashtable<FreiLink, LinkInfo>(); //stores linkinfo upon right click
 
   //FIXME the following paragraph is identical and static in VisorFrame. Use these definitions and remove the paragraph.
   public static Font mainfont = new Font("SansSerif", 0, 12),
                      smallerfont = new Font("SansSerif", 0, 9);
   public Color fgcolor = new Color(20,200,20),     //used for text, lines etc.
                bgcolor = new Color(64,128,64,196),       //used for transparent backgrounds of most status boxes
                fgcolor2 = new Color(150,150,255),       //used for foreground of link status boxes
                bgcolor2 = new Color(40,40,192,196),       //used for transparent backgrounds of link status boxes
                activeblue = Color.cyan,
                activeyellow = Color.yellow,
                activegreen = Color.green,
                activewhite = Color.white,
                transred    = new Color(255,0,0,127),
                transgreen  = new Color(0,255,0,127),
                transyellow = new Color(255,255,0,127),
                transwhite  = new Color(255,255,255,127);
   public int   currentalpha = 255;
   //ColorScheme cs = ColorScheme.NO_MAP;
 
   DataSource source;
 
   FreiNode selectedNode;
   FreiLink selectedLink;
   double selectedNodeDistance,
          selectedLinkDistance;
 
   private FreiNode uplink = new FreiNode("0.0.0.0/0.0.0.0");
 
   private boolean transparent = true;
 
   long crtTime;
 
   int visible = VISIBILITY_FULL;
 
   String  filter=null;
   int     filterType  = 0;
   boolean filterCase  = false;
   boolean filterRegEx = false;
 
   public NodeLayer(DataSource source) {
     this.source=source;
     this.source.addDataSourceListener(this);
 
     System.out.println("reading node list.");
     nodes=source.getNodeList();
     System.out.println("reading node availability.");
     availmap=source.getNodeAvailability(0);
     System.out.print("reading link list.");
     long now = System.currentTimeMillis();
     links = new Vector<FreiLink>();//source.getLinks(firstUpdateTime);
     System.out.println("("+(System.currentTimeMillis()-now)+"ms)");
     
   }
 
   /* datasourcelistener */
   public void timeRangeAvailable(long from, long until) {
     //obsolete.
   }
   public void nodeListUpdate(FreiNode node) {
     if (nodes!=null) { //this really should not happen
       nodes.add(node);
     }
   }
 
   /**
    * returns the DataSource of this layer. If the layer is just decorative, returns null.
    * 
    * @return null or DataSource
    */
 
   public DataSource getSource() {
     return source;
   }
 
   /**
    * Paints the layer.
    * 
    * @param g, a Graphics2D object.
    */
   public void paint(Graphics2D g) {
     if (visible == 0) return;
 
     if (!transparent) {
       g.setColor(Color.black);
       g.fillRect(0,0,w,h);
     } 
 
     g.setFont(VisorFrame.mainfont);
 
     //draw links
     if (scale < 0) {
       System.err.println("DEBUG scale < 0");
       scale = 1;
     }
 
     Stroke linkStroke = new BasicStroke((float)(Math.min(2,0.00005 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
     Stroke linkStrokeThick = new BasicStroke((float)(Math.min(4,0.00010 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
     Stroke cableStroke = new BasicStroke((float)(Math.min(10,0.00015 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
     Stroke selectedStroke = new BasicStroke((float)(Math.min(20,0.00030 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
 
     //draw selected link extra thick
     if ((selectedLink != null) && ((selectedLink.from.lat == selectedLink.from.DEFAULT_LAT) || (selectedLink.to.lat == selectedLink.to.DEFAULT_LAT))) {
       selectedLink = null;
     } 
     if (selectedLink != null) {
       g.setStroke(selectedStroke);
       g.setColor(fgcolor2);
       if (selectedLink.to.equals(uplink)) {
         double nsize = Math.min(25,Math.round(0.0015 * scale));
         g.drawOval((int)(converter.lonToViewX(selectedLink.from.lon)-nsize/2), (int)(converter.latToViewY(selectedLink.from.lat)-nsize/2), (int)(nsize), (int)(nsize));
       } else {
         g.drawLine(converter.lonToViewX(selectedLink.from.lon),
         	   converter.latToViewY(selectedLink.from.lat),
         	   converter.lonToViewX(selectedLink.to.lon), 
         	   converter.latToViewY(selectedLink.to.lat));
       }
     }
 
     //draw other links 
     g.setStroke(linkStroke);
     if ((links != null) && (links.size()>0)) {
       for(int i = 0; i < links.size(); i++) {
         FreiLink link = links.elementAt(i);
         boolean isneighbourlink = (link.from.equals(selectedNode)||link.to.equals(selectedNode));
         if (link.to.equals(uplink)) {
           g.setColor(activeblue);
           g.setStroke(cableStroke);
           double nsize = Math.min(25,Math.round(0.0015 * scale));
           g.drawOval((int)(converter.lonToViewX(link.from.lon)-nsize/2), (int)(converter.latToViewY(link.from.lat)-nsize/2), (int)(nsize), (int)(nsize));
           g.setStroke(linkStroke);
         } else if (link.packets>0) { 
           if ((link.from.lat != link.from.DEFAULT_LAT) && (link.to.lat != link.to.DEFAULT_LAT)) {//ignore links to truly unlocated nodes (at default position)
               float value=0.000005f * (float)Math.log(link.packets);
               linkStroke = new BasicStroke((float)Math.min(15,value * scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
               g.setStroke(linkStroke);
               double ratiotcp   = (double)link.tcp / (double)link.packets;
               double ratioudp   = (double)link.udp / (double)link.packets + ratiotcp;
               double ratioicmp  = (double)link.icmp / (double)link.packets + ratioudp;
               double ratioother = (double)link.other / (double)link.packets + ratioicmp;
               g.setColor(transred);
               drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, 0.0, ratiotcp);
               g.setColor(transyellow);
               drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, ratiotcp, ratioudp);
               g.setColor(transgreen);
               drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, ratioudp, ratioicmp);
               g.setColor(transwhite);
               drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, ratioicmp, 1.0f);
           }
         } else {
           if ((link.from.lat != link.from.DEFAULT_LAT) && (link.to.lat != link.to.DEFAULT_LAT)) {//ignore links to truly unlocated nodes (at default position)
             float green = 1;
             if (link.HNA || (link.etx < 0)) {
               g.setColor(activeblue);
             } else if (link.etx<1) {
               g.setColor(activewhite);
             } else {
               green=1/link.etx;
               g.setColor(new Color(1-green, green, 0.5f, currentalpha/255.0f));
             }
             g.setStroke(linkStroke);
             if (isneighbourlink) g.setStroke(linkStrokeThick);
             g.drawLine(converter.lonToViewX(link.from.lon), converter.latToViewY(link.from.lat), converter.lonToViewX(link.to.lon), converter.latToViewY(link.to.lat));
           }
         }
         if (link.to.unlocated && (link.from.lat != link.from.DEFAULT_LAT)) {
           double netx = (link.etx<1)?0d:1d/link.etx;
           link.to.lonsum+=link.from.lon*netx;
           link.to.latsum+=link.from.lat*netx;
           link.to.nc+= netx;
         }
         if (link.from.unlocated && (link.to.lat != link.to.DEFAULT_LAT)) {
           double netx = (link.etx<1)?0d:1d/link.etx;
           link.from.lonsum+=link.to.lon * netx;
           link.from.latsum+=link.to.lat * netx;
           link.from.nc+= netx;
         }
       }
     }
 
     //draw nodes
     g.setColor(activeblue);
     if (nodes != null)
     for (int i=0; i<nodes.size(); i++) {
       FreiNode node=(FreiNode)nodes.elementAt(i);
       if (node.equals(uplink)) continue;
       if (!matchFilter(node)) continue;
       
       if (node.unlocated) {
         g.setColor(activeyellow);
       } else if (availmap!=null) {
         Object oavail = availmap.get(node.id);
         if (oavail==null) {
           g.setColor(activewhite);
         } else {
           float avail = (float)Math.sqrt(((Float)oavail).floatValue()); 
           g.setColor(new Color(1.0f-avail, avail, 0.5f, currentalpha/255.0f));
         }
       }
       double nsize = Math.max(1,Math.min(8,Math.round(0.0003 * scale)));
       if (node.unlocated) nsize = Math.max(1,Math.min(4,Math.round(0.0001 * scale)));
       double nx = converter.lonToViewX(node.lon) - nsize/2,
              ny = converter.latToViewY(node.lat) - nsize/2;
              
       g.fillOval((int)nx, (int)ny, (int)nsize, (int)nsize);
       if (node.unlocated) {
         if (node.nc > 1) {
           node.lon=node.lonsum / node.nc;
           node.lat=node.latsum / node.nc;
         } else if (node.nc == 1) {
           node.lon=node.lonsum + 0.00003;
           node.lat=node.latsum + 0.00003;
         } /*else {
           System.err.println("Node unlocated with no neighbours: "+node.id);
         }*/
         /*node.lonsum=0; node.latsum=0; node.nc=0;*/
       }
     }
 
     //draw highlight
     if ((selectedNode != null)||(selectedLink != null)) {
     	g.setPaint(fgcolor);
     	g.setStroke(new BasicStroke((float)1f));
 
         double nx=0, ny=0;
         boolean showNodeInfo = true;
 
         //draw selected node
         if (selectedNode != null) {
     	  double nsize = Math.min(15,Math.round(0.0006 * scale));
     	  nx = converter.lonToViewX(selectedNode.lon);
           ny = converter.latToViewY(selectedNode.lat);
     	  g.draw(new Ellipse2D.Double(nx - nsize/2, ny - nsize/2, nsize, nsize));
         } 
         
         if ((selectedLink!=null) && (selectedLinkDistance < selectedNodeDistance)) {
           if (selectedLink.to.equals(uplink)) {
             nx = converter.lonToViewX(selectedLink.from.lon);
             ny = converter.latToViewY(selectedLink.from.lat);
           } else {
             nx = converter.lonToViewX((selectedLink.from.lon + selectedLink.to.lon)/2);
             ny = converter.latToViewY((selectedLink.from.lat + selectedLink.to.lat)/2);
             int oof = 8; //an obscure offscreen compensation factor
             int ooc = 0;
             while ((ooc<10)&&((nx<0)||(ny<0)||(nx>w)||(ny>h))) { //do not draw boxes offscreen too easily
               nx = converter.lonToViewX((selectedLink.from.lon * (oof-1) + selectedLink.to.lon)/oof);
               ny = converter.latToViewY((selectedLink.from.lat * (oof-1) + selectedLink.to.lat)/oof);
               oof *= 8;
               ooc++;
             }
           }
           showNodeInfo = false; //show linkInfo instead
           g.setPaint(fgcolor2);
     	}
 
         double boxw;
 
         String label;
         Vector<String> infos= new Vector<String>();
         if (showNodeInfo) {
           label = "Node: "+selectedNode.fqid;
           if (!selectedNode.fqid.equals(selectedNode.id)) infos.add("IP address: "+selectedNode.id);
         	boxw = Math.max(180, g.getFontMetrics(VisorFrame.mainfont).stringWidth(label)+20);
 
           if (availmap!=null) {
             Float favail = availmap.get(selectedNode.id);
 	          String savail=(favail==null)?"N/A":Math.round(favail.floatValue()*100)+"%";
 	          infos.add ("Availability: "+savail);
           }
 
           Iterator<String> atts=selectedNode.attributes.keySet().iterator();
           while (atts.hasNext()) {
             String key = atts.next();
             infos.add(key+": "+selectedNode.attributes.get(key));
           }
 
 	    NodeInfo info = nodeinfo.get(selectedNode.id);
       if (info!=null) {
 	      if (info.status == info.STATUS_AVAILABLE) {
 	        infos.add("min. links: " + info.minLinks );
 	        infos.add("max. links: " + info.maxLinks );
 
 	        if (info.linkCountChart != null) {
 		      info.linkCountChart.draw(g, new Rectangle2D.Float(20, 180, 500, 300));
 	      }
             } else if (info.status == info.STATUS_FETCHING) {
               infos.add("retrieving information");
 	    }
 	  } else {
 	    infos.add("+ right click for more +");
 	  }
 
         } else {
           boxw = g.getFontMetrics(VisorFrame.mainfont).stringWidth("Link: 999.999.999.999 -> 999.999.999.999/999.999.999.999");
 
           label = "Link: "+selectedLink.toString();
 
           if (selectedLink.packets > 0) {
             infos.add("packets: "+selectedLink.packets);
             infos.add("bytes  : "+selectedLink.bytes);
             infos.add("icmp   : "+selectedLink.icmp);
             infos.add("tcp    : "+selectedLink.tcp);
             infos.add("udp    : "+selectedLink.udp);
             infos.add("other  : "+selectedLink.other);
           }
 
           LinkInfo info = linkinfo.get(selectedLink);
           
           if (info != null) {
             if (info.status==info.STATUS_AVAILABLE) { 
 	      if (info.linkChart != null) {
 		  info.linkChart.draw(g, new Rectangle2D.Float(20, 180, 500, 300));
 	      }
             } else if (info.status == info.STATUS_FETCHING) {
               infos.add("retrieving information");
             }
 	  } else {
 	    infos.add("+ right click for more +");
 	  }
 
         }
 
         // Put box at fixed location.
         double boxx = w - 10 - boxw / 2;
         double boxy = 80;
 
         double labelh = g.getFontMetrics(VisorFrame.mainfont).getHeight(),
         infoh = g.getFontMetrics(smallerfont).getHeight(),
         boxh = labelh + infoh * infos.size() + 10;
 
 	    // Connect with the bottom line of the box.
     		g.draw(new Line2D.Double(nx, ny, boxx, boxy+boxh));
 
         Shape box = new RoundRectangle2D.Double(boxx-boxw/2, boxy, boxw, boxh, 10, 10);
         Color mybgcolor = showNodeInfo?bgcolor:bgcolor2;
 	      Color myfgcolor = showNodeInfo?fgcolor:fgcolor2;
 	      g.setPaint(mybgcolor);
     	  g.fill(box); 
         g.setPaint(myfgcolor);
     	  g.draw(box);
 	      g.setColor(showNodeInfo?activegreen:activeblue); 
         g.setFont(VisorFrame.mainfont);
       	g.drawString(label, (int)(boxx - boxw/2 + 10), (int)(boxy + labelh));
 	      g.setColor(myfgcolor); 
 	      g.setFont(smallerfont);
 	      for (int i=0; i<infos.size(); i++) {
 		      g.drawString(infos.elementAt(i), (int)(boxx - boxw/2 + 10), (int)(boxy + labelh + infoh*i + 15));
 	      }
     }
 
 
   }
 
   private void drawLineSeg(Graphics2D g, double lon1, double lat1, double lon2, double lat2, double r1, double r2) {
     int x1 = converter.lonToViewX(lon1 + (lon2-lon1)*r1);
     int x2 = converter.lonToViewX(lon1 + (lon2-lon1)*r2);
     int y1 = converter.latToViewY(lat1 + (lat2-lat1)*r1);
     int y2 = converter.latToViewY(lat1 + (lat2-lat1)*r2);
     g.drawLine(x1,y1,x2,y2);
   }
 
   public FreiNode getSelectedNode() {
     return selectedNode;
   }
 
   public double sqr(double x) { 
     return x*x;
   }
   public double dist (double x1, double x2, double y1, double y2) {
     return Math.sqrt(sqr(x1-x2)+sqr(y1-y2));
   }
   public FreiLink getClosestLink(double lon, double lat) {
     if (links==null) return null;
     double dmin=Double.POSITIVE_INFINITY;
     FreiLink closest=null, link;
     boolean within;
     for (int i=0; i<links.size(); i++) {
       link=links.elementAt(i);
       within=true;
       if (link.from.lon < link.to.lon) { 
         if ((lon < link.from.lon) || (lon > link.to.lon)) within = false;
       } else {
         if ((lon > link.from.lon) || (lon < link.to.lon)) within = false;
       }
       if (link.from.lat < link.to.lat) { 
         if ((lat < link.from.lat) || (lat > link.to.lat)) within = false;
       } else {
         if ((lat > link.from.lat) || (lat < link.to.lat)) within = false;
       }
       if (within) {
         if (dist(lat, link.from.lat, lon, link.from.lon) > dist(lat, link.to.lat, lon, link.to.lon)) continue; 
            //we will then select the other link direction.
         double x1 = link.from.lat, 
                x2 = link.to.lat,
                y1 = link.from.lon,
                y2 = link.to.lon;
         double d = Math.abs((x2-x1)*(y1-lon) - (x1-lat)*(y2-y1)) / Math.sqrt(sqr(x2-x1)+sqr(y2-y1));
         if (d<dmin) {
 	  dmin=d;
 	  closest=link;
         }
       }
     }
     selectedLinkDistance=dmin;
     return closest;
   }
 
   public FreiNode getClosestNode(double lon, double lat) {
     double dmin=Double.POSITIVE_INFINITY;
     FreiNode closest=null, node;
     for (int i=0; i<nodes.size(); i++) {
       node=nodes.elementAt(i);
       double d = Math.abs(node.lon - lon) + Math.abs(node.lat - lat); //no need to sqrt here
       if (d<dmin) {
 	      dmin=d;
 	      closest=node;
       }
     }
     selectedNodeDistance = (closest==null)?dmin:dist(closest.lon, lon, closest.lat, lat); //recalculate exact distance
     return closest;
   }
 
 
   /**
    * Indiciates whether this VisorLayer instance is transparent. 
    * 
    * @return true
    */
 
   public boolean isTransparent() {
     return transparent;
   }
   public int getVisibility() {
     return visible;
   }
 
   /**
    * Attempts to set transparency to this VisorLayer.
    */
 
   public void setTransparent(boolean t) {
     this.transparent=t;
   }
 
   public void toggleVisibility() {
     visible = (visible + 1) %3;
     resetColorScheme();
   }
 
   void resetColorScheme() {
     if (visible == VISIBILITY_DIM) {
       currentalpha = 63;
       fgcolor = new Color(20,200,20,currentalpha);     //used for text, lines etc.
       bgcolor = new Color(64,128,64,currentalpha/2);       //used for transparent backgrounds of most status boxes
       fgcolor2 = new Color(150,150,255,currentalpha);       //used for foreground of link status boxes
       bgcolor2 = new Color(40,40,192,currentalpha/2);       //used for transparent backgrounds of link status boxes
       activeblue = new Color(0,0,255,currentalpha);
       activewhite = new Color(255,255,255,currentalpha);
       activegreen = new Color(0,255,0,currentalpha);
       activeyellow = new Color(255,255,0,currentalpha);
     } else {
       currentalpha = 255;
       fgcolor = new Color(20,200,20);     //used for text, lines etc.
       bgcolor = new Color(64,128,64,196);       //used for transparent backgrounds of most status boxes
       fgcolor2 = new Color(150,150,255);       //used for foreground of link status boxes
       bgcolor2 = new Color(40,40,192,196);       //used for transparent backgrounds of link status boxes
       activeblue = Color.cyan;
       activewhite = Color.white;
       activegreen = Color.green;
       activeyellow = Color.yellow;
     }
   }
 
 
  /**
    * Sets the scaling converter for this background.
    */
 
   public void setConverter(Converter c) {
     this.converter = c;
   }
 
  /**
   * Sets the width and height of the section the layer is
   * showing.
   * 
   * <p>This method must be called whenever the size changes
   * otherwise calculations will get incorrect and drawing problems
   * may occur.</p>
   * 
   * @param w
   * @param h
   */
  public void setDimension(int w, int h) {
    this.w=w; this.h=h;
    cx = w/2; cy = h/2;
  }
 
  /**
   * Sets the <code>VisorLayer</code>s zoom.
   * 
   * <p>This method must be called whenever the zoom changes
   * otherwise calculations will get incorrect and drawing problems
   * may occur.</p>
   * 
   * @param zoom
   */
  public void setZoom(int zoom) {
    this.zoom=zoom;
    this.scale=converter.getScale();
  }
 
  /**
   * Sets the current point in time to be displayed
   * 
   * @param crtTime, an unix time stamp
   * @return true, if the layer has to be repainted
   */
  public boolean setCurrentTime(long crtTime) {
    long adjusted=source.getClosestUpdateTime(crtTime);
    //FIXME: if the interval between crtTime and the closest Display time is too high, display nothing.
    if (adjusted != this.crtTime) {
      links = source.getLinks(this.crtTime);
      this.crtTime = adjusted;
      return true;
    }
    return false;
  }
 
  public void mouseMoved(double lat, double lon) {
    if ((lon==0) && (lat==0)) {
      selectedNode = null;
      selectedLink = null;
    } else {
      selectedNode = getClosestNode(lon, lat); 
      selectedLink = getClosestLink(lon, lat);
      if (selectedNodeDistance * scale < 10) selectedLinkDistance=Double.POSITIVE_INFINITY; //when close to a node, select a node
    }
  }
  public void mouseClicked(double lat, double lon, int button) {
    if (button==MouseEvent.BUTTON3) {
      if (selectedNodeDistance < selectedLinkDistance) {
        if (selectedNode != null) {
          NodeInfo info=new NodeInfo();
          source.getLinkCountProfile(selectedNode, info);
          nodeinfo.put(selectedNode.id, info);
        }
      } else if (selectedLink != null) {
        LinkInfo info=new LinkInfo();
        source.getLinkProfile(selectedLink, info);
        linkinfo.put(selectedLink, info);
      } 
    } 
  }
 
  public void setDisplayFilter(String match, int type, boolean cases, boolean regex) {
    filter=match;
    filterType=type;
    filterCase=cases;
    filterRegEx=regex;
  }
 
  boolean matchFilter(FreiNode node) {
    if (filter==null) return true;
    int regexCase = filterCase?0:Pattern.CASE_INSENSITIVE;
    try {
      switch (filterType) {
        case FILTER_IP: {
          if (filterRegEx) return Pattern.compile(filter, regexCase).matcher(node.id).matches();
          if (filterCase) {
            return (node.id.indexOf(filter)>-1);
          } else {
            return (node.id.toLowerCase().indexOf(filter.toLowerCase())>-1);
          }
        } 
        default: {
          if (filterRegEx) return Pattern.compile(filter, regexCase).matcher(node.fqid).matches();
          if (filterCase) {
            return (node.fqid.indexOf(filter)>-1);
          } else {
            return (node.fqid.toLowerCase().indexOf(filter.toLowerCase())>-1);
          }
        }
      }
    } catch (Exception ex) {
      return true;
    }
  }
 }
