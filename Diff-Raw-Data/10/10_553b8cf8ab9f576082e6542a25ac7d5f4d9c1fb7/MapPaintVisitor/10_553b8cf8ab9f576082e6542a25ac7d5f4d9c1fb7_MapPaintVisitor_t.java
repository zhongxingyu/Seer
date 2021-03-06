 // License: GPL. Copyright 2007 by Immanuel Scholz and others
 package org.openstreetmap.josm.data.osm.visitor;
 
 import static org.openstreetmap.josm.tools.I18n.marktr;
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Stroke;
 import java.awt.geom.GeneralPath;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Locale;
 
 import javax.swing.ImageIcon;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.data.osm.Node;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.data.osm.Relation;
 import org.openstreetmap.josm.data.osm.RelationMember;
 import org.openstreetmap.josm.data.osm.Way;
 import org.openstreetmap.josm.data.osm.visitor.SimplePaintVisitor;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.data.coor.EastNorth;
 import org.openstreetmap.josm.gui.mappaint.AreaElemStyle;
 import org.openstreetmap.josm.gui.mappaint.ElemStyle;
 import org.openstreetmap.josm.gui.mappaint.ElemStyles;
 import org.openstreetmap.josm.gui.mappaint.IconElemStyle;
 import org.openstreetmap.josm.gui.mappaint.LineElemStyle;
 import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;
 
 public class MapPaintVisitor extends SimplePaintVisitor {
     protected boolean useRealWidth;
     protected boolean zoomLevelDisplay;
     protected int fillAreas;
     protected boolean drawMultipolygon;
     protected int showNames;
     protected int showIcons;
     protected int useStrokes;
     protected int fillAlpha;
     protected Color untaggedColor;
     protected Color textColor;
     protected boolean currentDashed = false;
     protected int currentWidth = 0;
     protected Stroke currentStroke = null;
     protected Font orderFont;
     protected ElemStyles.StyleSet styles;
     protected double circum;
     protected double dist;
     protected String regionalNameOrder[];
     protected Boolean selectedCall;
     protected Boolean useStyleCache;
     private static int paintid = 0;
     private static int viewid = 0;
     private EastNorth minEN;
     private EastNorth maxEN;
 
     protected int profilerVisibleNodes;
     protected int profilerVisibleWays;
     protected int profilerVisibleAreas;
     protected int profilerSegments;
     protected int profilerVisibleSegments;
     protected boolean profilerOmitDraw;
 
     protected boolean isZoomOk(ElemStyle e) {
         if (!zoomLevelDisplay) /* show everything if the user wishes so */
             return true;
 
         if(e == null) /* the default for things that don't have a rule (show, if scale is smaller than 1500m) */
             return (circum < 1500);
 
         // formula to calculate a map scale: natural size / map size = scale
         // example: 876000mm (876m as displayed) / 22mm (roughly estimated screen size of legend bar) = 39818
         //
         // so the exact "correcting value" below depends only on the screen size and resolution
         // XXX - do we need a Preference setting for this (if things vary widely)?
         return !(circum >= e.maxScale / 22 || circum < e.minScale / 22);
     }
 
     public ElemStyle getPrimitiveStyle(OsmPrimitive osm) {
         if(!useStyleCache)
             return (styles != null) ? (IconElemStyle)styles.get(osm) : null;
 
         if(osm.mappaintStyle == null && styles != null) {
             osm.mappaintStyle =  styles.get(osm);
             if(osm instanceof Way)
                 ((Way)osm).isMappaintArea = styles.isArea(osm);
         }
         return osm.mappaintStyle;
     }
 
     public boolean isPrimitiveArea(Way osm) {
         if(!useStyleCache)
             return styles.isArea(osm);
 
         if(osm.mappaintStyle == null && styles != null) {
             osm.mappaintStyle = styles.get(osm);
             if(osm instanceof Way)
                 osm.isMappaintArea = styles.isArea(osm);
         }
         return osm.isMappaintArea;
     }
 
     /**
      * Draw a small rectangle.
      * White if selected (as always) or red otherwise.
      *
      * @param n The node to draw.
      */
     public void visit(Node n) {
         // check, if the node is visible at all
         if((n.eastNorth.east()  > maxEN.east() ) ||
            (n.eastNorth.north() > maxEN.north()) ||
            (n.eastNorth.east()  < minEN.east() ) ||
            (n.eastNorth.north() < minEN.north()))
         {
             n.mappaintVisibleCode = viewid;
             return;
         }
         n.mappaintVisibleCode = 0;
 
         IconElemStyle nodeStyle = (IconElemStyle)getPrimitiveStyle(n);
 
         if(profilerOmitDraw)
             return;
 
         if (nodeStyle != null && isZoomOk(nodeStyle) && showIcons > dist)
             drawNode(n, nodeStyle.icon, nodeStyle.annotate, n.selected);
         else if (n.selected)
             drawNode(n, selectedColor, selectedNodeSize, selectedNodeRadius, fillSelectedNode);
         else if (n.tagged)
             drawNode(n, nodeColor, taggedNodeSize, taggedNodeRadius, fillUnselectedNode);
         else
             drawNode(n, nodeColor, unselectedNodeSize, unselectedNodeRadius, fillUnselectedNode);
     }
 
     /**
      * Draw a line for all segments, according to tags.
      * @param w The way to draw.
      */
     public void visit(Way w) {
         if(w.nodes.size() < 2)
         {
             w.mappaintVisibleCode = viewid;
             return;
         }
 
         // check, if the way is visible at all
         double minx = 10000;
         double maxx = -10000;
         double miny = 10000;
         double maxy = -10000;
 
         for (Node n : w.nodes)
         {
             if(n.eastNorth.east() > maxx) maxx = n.eastNorth.east();
             if(n.eastNorth.north() > maxy) maxy = n.eastNorth.north();
             if(n.eastNorth.east() < minx) minx = n.eastNorth.east();
             if(n.eastNorth.north() < miny) miny = n.eastNorth.north();
         }
         
         if ((minx > maxEN.east()) ||
             (miny > maxEN.north()) ||
             (maxx < minEN.east()) ||
             (maxy < minEN.north()))
         {
             w.mappaintVisibleCode = viewid;
             return;
         }
 
         ElemStyle wayStyle = getPrimitiveStyle(w);
 
         if(!isZoomOk(wayStyle))
         {
             w.mappaintVisibleCode = viewid;
             return;
         }
 
         w.mappaintVisibleCode = 0;
         if(fillAreas > dist)
             w.clearErrors();
 
         if(wayStyle==null)
         {
             // way without style
             profilerVisibleWays++;
             if(!profilerOmitDraw)
                 drawWay(w, null, untaggedColor, w.selected);
         }
         else if(wayStyle instanceof LineElemStyle)
         {
             // way with line style
             profilerVisibleWays++;
             if(!profilerOmitDraw)
                 drawWay(w, (LineElemStyle)wayStyle, untaggedColor, w.selected);
         }
         else if (wayStyle instanceof AreaElemStyle)
         {
             // way with area style
             if(!profilerOmitDraw)
             {
                 if (fillAreas > dist)
                 {
                     profilerVisibleAreas++;
                     drawArea(w, w.selected ? selectedColor : ((AreaElemStyle)wayStyle).color);
                     if(!w.isClosed())
                         w.putError(tr("Area style way is not closed."), true);
                 }
                 drawWay(w, ((AreaElemStyle)wayStyle).line, ((AreaElemStyle)wayStyle).color, w.selected);
             }
         }
     }
 
     public void drawWay(Way w, LineElemStyle l, Color color, Boolean selected) {
         // show direction arrows, if draw.segment.relevant_directions_only is not set,
         // the way is tagged with a direction key
         // (even if the tag is negated as in oneway=false) or the way is selected
         boolean showDirection = w.selected || ((!useRealWidth) && (showDirectionArrow
         && (!showRelevantDirectionsOnly || w.hasDirectionKeys)));
         int width = defaultSegmentWidth;
         int realWidth = 0; //the real width of the element in meters
         boolean dashed = false;
         Node lastN;
 
         if(l != null)
         {
             color = l.color;
             width = l.width;
             realWidth = l.realWidth;
             dashed = l.dashed;
         }
         if(selected)
             color = selectedColor;
         if (realWidth > 0 && useRealWidth && !showDirection)
         {
             int tmpWidth = (int) (100 /  (float) (circum / realWidth));
             if (tmpWidth > width) width = tmpWidth;
         }
         if(w.selected)
             color = selectedColor;
 
         // draw overlays under the way
         if(l != null && l.overlays != null)
         {
             for(LineElemStyle s : l.overlays)
             {
                 if(!s.over)
                 {
                     lastN = null;
                     for(Node n : w.nodes)
                     {
                         if(lastN != null)
                         {
                             drawSeg(lastN, n, s.color != null  && !w.selected ? s.color : color,
                             false, s.getWidth(width), s.dashed);
                         }
                         lastN = n;
                     }
                 }
             }
         }
 
         // draw the way
         lastN = null;
         for(Node n : w.nodes)
         {
             if(lastN != null)
                 drawSeg(lastN, n, color, showDirection, width, dashed);
             lastN = n;
         }
 
         // draw overlays above the way
         if(l != null && l.overlays != null)
         {
             for(LineElemStyle s : l.overlays)
             {
                 if(s.over)
                 {
                     lastN = null;
                     for(Node n : w.nodes)
                     {
                         if(lastN != null)
                         {
                             drawSeg(lastN, n, s.color != null && !w.selected ? s.color : color,
                             false, s.getWidth(width), s.dashed);
                         }
                         lastN = n;
                     }
                 }
             }
         }
 
         if(showOrderNumber)
         {
             int orderNumber = 0;
             lastN = null;
             for(Node n : w.nodes)
             {
                 if(lastN != null)
                 {
                     orderNumber++;
                     drawOrderNumber(lastN, n, orderNumber);
                 }
                 lastN = n;
             }
         }
         displaySegments();
     }
 
     public Collection<Way> joinWays(Collection<Way> join, OsmPrimitive errs)
     {
         Collection<Way> res = new LinkedList<Way>();
         Object[] joinArray = join.toArray();
         int left = join.size();
         while(left != 0)
         {
             Way w = null;
             Boolean selected = false;
             ArrayList<Node> n = null;
             Boolean joined = true;
             while(joined && left != 0)
             {
                 joined = false;
                 for(int i = 0; i < joinArray.length && left != 0; ++i)
                 {
                     if(joinArray[i] != null)
                     {
                         Way c = (Way)joinArray[i];
                         if(w == null)
                         { w = c; selected = w.selected; joinArray[i] = null; --left; }
                         else
                         {
                             int mode = 0;
                             int cl = c.nodes.size()-1;
                             int nl;
                             if(n == null)
                             {
                                 nl = w.nodes.size()-1;
                                 if(w.nodes.get(nl) == c.nodes.get(0)) mode = 21;
                                 else if(w.nodes.get(nl) == c.nodes.get(cl)) mode = 22;
                                 else if(w.nodes.get(0) == c.nodes.get(0)) mode = 11;
                                 else if(w.nodes.get(0) == c.nodes.get(cl)) mode = 12;
                             }
                             else
                             {
                                 nl = n.size()-1;
                                 if(n.get(nl) == c.nodes.get(0)) mode = 21;
                                 else if(n.get(0) == c.nodes.get(cl)) mode = 12;
                                 else if(n.get(0) == c.nodes.get(0)) mode = 11;
                                 else if(n.get(nl) == c.nodes.get(cl)) mode = 22;
                             }
                             if(mode != 0)
                             {
                                 joinArray[i] = null;
                                 joined = true;
                                 if(c.selected) selected = true;
                                 --left;
                                 if(n == null) n = new ArrayList(w.nodes);
                                 n.remove((mode == 21 || mode == 22) ? nl : 0);
                                 if(mode == 21)
                                     n.addAll(c.nodes);
                                 else if(mode == 12)
                                     n.addAll(0, c.nodes);
                                 else if(mode == 22)
                                 {
                                     for(Node node : c.nodes)
                                         n.add(nl, node);
                                 }
                                 else /* mode == 11 */
                                 {
                                     for(Node node : c.nodes)
                                         n.add(0, node);
                                 }
                             }
                         }
                     }
                 } /* for(i = ... */
             } /* while(joined) */
             if(n != null)
             {
                 w = new Way(w);
                 w.nodes.clear();
                 w.nodes.addAll(n);
                 w.selected = selected;
             }
             if(!w.isClosed())
             {
                 if(errs != null)
                 {
                     errs.putError(tr("multipolygon way ''{0}'' is not closed.",
                     w.getName()), true);
                 }
             }
             res.add(w);
         } /* while(left != 0) */
 
         return res;
     }
 
     public void drawSelectedRelation(Relation r)
     {
         for (RelationMember m : r.members)
         {
             if (m.member != null && !m.member.incomplete && !m.member.deleted
             && !(m.member instanceof Relation))
             {
                 /* nodes drawn on second call */
                 if(!(m.member instanceof Node))
                     drawSelectedMember(m.member, styles != null ? styles.get(m.member)
                     : null, true, true);
             }
         }
     }
 
     public void drawSelectedMember(OsmPrimitive osm, ElemStyle style, Boolean area,
     Boolean areaselected)
     {
         if(osm instanceof Way)
         {
             if(style instanceof AreaElemStyle)
             {
                 drawWay((Way)osm, ((AreaElemStyle)style).line, selectedColor, true);
                 if(area)
                     drawArea((Way)osm, areaselected ? selectedColor
                     : ((AreaElemStyle)style).color);
             }
             else
             {
                 drawWay((Way)osm, (LineElemStyle)style, selectedColor, true);
             }
         }
         else if(osm instanceof Node)
         {
             if(style != null && isZoomOk(style))
                 drawNode((Node)osm, ((IconElemStyle)style).icon,
                 ((IconElemStyle)style).annotate, true);
             else
                 drawNode((Node)osm, selectedColor, selectedNodeSize, selectedNodeRadius, fillSelectedNode);
         }
         osm.mappaintDrawnCode = paintid;
     }
 
     public void visit(Relation r) {
 
         r.mappaintVisibleCode = 0;
         /* TODO implement visible handling for relations too */
 
         // TODO: is it possible to do this like the nodes/ways code?
         if(profilerOmitDraw)
             return;
 
         // draw multipolygon relations including their ways
         // other relations are only drawn when selected
         
         // we are in the "draw selected" phase
         // TODO: is it necessary to check for r.selected?
         if(r.selected && selectedCall)
         {
             for (RelationMember m : r.members)
             {
                 if (m.member != null && !m.member.incomplete && !m.member.deleted
                 && m.member instanceof Node)
                 {
                     drawSelectedMember(m.member, styles != null ? styles.get(m.member) : null, true, true);
                 }
             }
             return;
         }
         
         if (drawMultipolygon && r.keys != null && "multipolygon".equals(r.keys.get("type")))
         {
             drawMultipolygon(r);
             return;
         }
         
         if(r.selected)
             drawSelectedRelation(r);
     }
 
     public void drawMultipolygon(Relation r) {
         Collection<Way> inner = new LinkedList<Way>();
         Collection<Way> outer = new LinkedList<Way>();
         Collection<Way> innerclosed = new LinkedList<Way>();
         Collection<Way> outerclosed = new LinkedList<Way>();
         Boolean incomplete = false;
 
         r.clearErrors();
 
         for (RelationMember m : r.members)
         {
             if(m.member == null) 
                 r.putError(tr("Empty member in relation."), true);
             else if(m.member.deleted)
                 r.putError(tr("Deleted member ''{0}'' in relation.",
                 m.member.getName()), true);
             else if(m.member.incomplete)
                 incomplete = true;
             else
             {
                 if(m.member instanceof Way)
                 {
                     Way w = (Way) m.member;
                     if(w.nodes.size() < 2)
                     {
                         r.putError(tr("Way ''{0}'' with less than two points.",
                         w.getName()), true);
                     }
                     else if("inner".equals(m.role))
                         inner.add(w);
                     else if("outer".equals(m.role))
                         outer.add(w);
                     else
                     {
                         r.putError(tr("No useful role ''{0}'' for Way ''{1}''.",
                         m.role == null ? "" : m.role, w.getName()), true);
                         if(m.role == null || m.role.length() == 0)
                             outer.add(w);
                         else if(r.selected)
                             drawSelectedMember(m.member, styles != null ? styles.get(m.member) : null, true, true);
                     }
                 }
                 else
                 {
                     r.putError(tr("Non-Way ''{0}'' in multipolygon.",
                     m.member.getName()), true);
                 }
             }
         }
 
         ElemStyle wayStyle = styles != null ? styles.get(r) : null;
         if(styles != null && (wayStyle == null || !(wayStyle instanceof AreaElemStyle)))
         {
             for (Way w : outer)
             {
                if(wayStyle == null || !(wayStyle instanceof AreaElemStyle))
                    wayStyle = styles.get(w);
             }
         }
 
         if(wayStyle != null && wayStyle instanceof AreaElemStyle)
         {
             Boolean zoomok = isZoomOk(wayStyle);
             Boolean visible = false;
             Collection<Way> join = new LinkedList<Way>();
 
             for (Way w : outer)
             {
                 if(w.isClosed()) outerclosed.add(w);
                 else join.add(w);
             }
             if(join.size() != 0)
             {
                 for(Way w : joinWays(join, incomplete ? null : r))
                     outerclosed.add(w);
             }
 
             join.clear();
             for (Way w : inner)
             {
                 if(w.isClosed()) innerclosed.add(w);
                 else join.add(w);
             }
             if(join.size() != 0)
             {
                 for(Way w : joinWays(join, incomplete ? null : r))
                     innerclosed.add(w);
             }
 
             if(outerclosed.size() == 0)
             {
                 r.putError(tr("No outer way for multipolygon ''{0}''.",
                 r.getName()), true);
                 visible = true; /* prevent killing remaining ways */
             }
             else if(zoomok)
             {
                 class PolyData {
                     public Polygon poly = new Polygon();
                     public Way way;
                     private Point p = null;
                     private Collection<Polygon> inner = null;
                     PolyData(Way w)
                     {
                         way = w;
                         for (Node n : w.nodes)
                         {
                             p = nc.getPoint(n.eastNorth);
                             poly.addPoint(p.x,p.y);
                         }
                     }
                     public int contains(Polygon p)
                     {
                         int contains = p.npoints;
                         for(int i = 0; i < p.npoints; ++i)
                         {
                             if(poly.contains(p.xpoints[i],p.ypoints[i]))
                                 --contains;
                         }
                         if(contains == 0) return 1; 
                         if(contains == p.npoints) return 0; 
                         return 2; 
                     }
                     public void addInner(Polygon p)
                     {
                         if(inner == null)
                             inner = new ArrayList<Polygon>();
                         inner.add(p);
                     }
                     public Polygon get()
                     {
                         if(inner != null)
                         {
                             for (Polygon pp : inner)
                             {
                                 for(int i = 0; i < pp.npoints; ++i)
                                     poly.addPoint(pp.xpoints[i],pp.ypoints[i]);
                                 poly.addPoint(p.x,p.y);
                             }
                             inner = null;
                         }
                         return poly;
                     }
                 }
                 LinkedList<PolyData> poly = new LinkedList<PolyData>();
                 for (Way w : outerclosed)
                 {
                     poly.add(new PolyData(w));
                 }
                 for (Way wInner : innerclosed)
                 {
                     Polygon polygon = new Polygon();
 
                     for (Node n : wInner.nodes)
                     {
                         Point pInner = nc.getPoint(n.eastNorth);
                         polygon.addPoint(pInner.x,pInner.y);
                     }
                     if(!wInner.isClosed())
                     {
                         Point pInner = nc.getPoint(wInner.nodes.get(0).eastNorth);
                         polygon.addPoint(pInner.x,pInner.y);
                     }
                     PolyData o = null;
                     for (PolyData pd : poly)
                     {
                         Integer c = pd.contains(polygon);
                         if(c >= 1)
                         {
                             if(c > 1 && pd.way.isClosed())
                             {
                                 r.putError(tr("Intersection between ways ''{0}'' and ''{1}''.",
                                 pd.way.getName(), wInner.getName()), true);
                             }
                             if(o == null || o.contains(pd.poly) > 0)
                                 o = pd;
                         }
                     }
                     if(o == null)
                     {
                         if(!incomplete)
                         {
                             r.putError(tr("Inner way ''{0}'' is outside.",
                             wInner.getName()), true);
                         }
                         o = poly.get(0);
                     }
                     o.addInner(polygon);
                 }
                 for (PolyData pd : poly)
                 {
                     if(isPolygonVisible(pd.get()))
                     {
                        drawAreaPolygon(pd.get(), (pd.way.selected || r.selected) ? selectedColor
                         : ((AreaElemStyle)wayStyle).color);
                         visible = true;
                     }
                 }
             }
             if(!visible) /* nothing visible, so disable relation and all its ways */
             {
                 r.mappaintVisibleCode = viewid;
                 for (Way wInner : inner)
                     wInner.mappaintVisibleCode = viewid;
                 for (Way wOuter : outer)
                     wOuter.mappaintVisibleCode = viewid;
                 return;
             }
             for (Way wInner : inner)
             {
                 ElemStyle innerStyle = styles.get(wInner);
                 if(innerStyle == null)
                 {
                     if(zoomok && (wInner.mappaintDrawnCode != paintid
                     || outer.size() == 0))
                     {
                         drawWay(wInner, ((AreaElemStyle)wayStyle).line,
                         ((AreaElemStyle)wayStyle).color, wInner.selected
                         || r.selected);
                     }
                     wInner.mappaintDrawnCode = paintid;
                 }
                 else
                 {
                     if(r.selected)
                     {
                         drawSelectedMember(wInner, innerStyle,
                         !wayStyle.equals(innerStyle), wInner.selected);
                     }
                     if(wayStyle.equals(innerStyle))
                     {
                         r.putError(tr("Style for inner way ''{0}'' equals multipolygon.",
                         wInner.getName()), false);
                         if(!r.selected)
                             wInner.mappaintDrawnAreaCode = paintid;
                     }
                 }
             }
             for (Way wOuter : outer)
             {
                 ElemStyle outerStyle = styles.get(wOuter);
                 if(outerStyle == null)
                 {
                     if(zoomok)
                     {
                         drawWay(wOuter, ((AreaElemStyle)wayStyle).line,
                         ((AreaElemStyle)wayStyle).color, wOuter.selected
                         || r.selected);
                     }
                     wOuter.mappaintDrawnCode = paintid;
                 }
                 else
                 {
                     if(outerStyle instanceof AreaElemStyle
                     && !wayStyle.equals(outerStyle))
                     {
                         r.putError(tr("Style for outer way ''{0}'' mismatches.",
                         wOuter.getName()), true);
                     }
                     if(r.selected)
                     {
                         drawSelectedMember(wOuter, outerStyle, false, false);
                     }
                     else if(outerStyle instanceof AreaElemStyle)
                         wOuter.mappaintDrawnAreaCode = paintid;
                 }
             }
         }
         else if(r.selected)
             drawSelectedRelation(r);
     }
 
     protected Polygon getPolygon(Way w)
     {
         Polygon polygon = new Polygon();
 
         for (Node n : w.nodes)
         {
             Point p = nc.getPoint(n.eastNorth);
             polygon.addPoint(p.x,p.y);
         }
         return polygon;
     }
 
     protected void drawArea(Way w, Color color)
     {
         Polygon polygon = getPolygon(w);
         
         // set the opacity (alpha) level of the filled polygon
         g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
         g.fillPolygon(polygon);
     }
 
    protected void drawAreaPolygon(Polygon polygon, Color color)
    {
        // set the opacity (alpha) level of the filled polygon
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
        g.fillPolygon(polygon);
    }

     protected void drawNode(Node n, ImageIcon icon, boolean annotate, Boolean selected) {
         Point p = nc.getPoint(n.eastNorth);
         if ((p.x < 0) || (p.y < 0) || (p.x > nc.getWidth()) || (p.y > nc.getHeight())) return;
 
         profilerVisibleNodes++;
 
         int w = icon.getIconWidth(), h=icon.getIconHeight();
         icon.paintIcon ( Main.map.mapView, g, p.x-w/2, p.y-h/2 );
         if(showNames > dist)
         {
             String name = getNodeName(n);
             if (name!=null && annotate)
             {
                 g.setColor(textColor);
                 Font defaultFont = g.getFont();
                 g.setFont (orderFont);
                 g.drawString (name, p.x+w/2+2, p.y+h/2+2);
                 g.setFont(defaultFont);
             }
         }
         if (selected)
         {
             g.setColor (  selectedColor );
             g.drawRect (p.x-w/2-2,p.y-w/2-2, w+4, h+4);
         }
     }
 
     protected String getNodeName(Node n) {
         String name = null;
         if (n.keys != null) {
             for (int i = 0; i < regionalNameOrder.length; i++) {
                 name = n.keys.get(regionalNameOrder[i]);
                 if (name != null) break;
             }
         }
         return name;
     }
 
     private void drawSeg(Node n1, Node n2, Color col, boolean showDirection, int width, boolean dashed) {
         profilerSegments++;
         if (col != currentColor || width != currentWidth || dashed != currentDashed) {
             displaySegments(col, width, dashed);
         }
         Point p1 = nc.getPoint(n1.eastNorth);
         Point p2 = nc.getPoint(n2.eastNorth);
 
         if (!isSegmentVisible(p1, p2)) {
             return;
         }
         profilerVisibleSegments++;
         currentPath.moveTo(p1.x, p1.y);
         currentPath.lineTo(p2.x, p2.y);
 
         if (showDirection) {
             double t = Math.atan2(p2.y-p1.y, p2.x-p1.x) + Math.PI;
             currentPath.lineTo((int)(p2.x + 10*Math.cos(t-PHI)), (int)(p2.y + 10*Math.sin(t-PHI)));
             currentPath.moveTo((int)(p2.x + 10*Math.cos(t+PHI)), (int)(p2.y + 10*Math.sin(t+PHI)));
             currentPath.lineTo(p2.x, p2.y);
         }
     }
 
     protected void displaySegments() {
         displaySegments(null, 0, false);
     }
 
     protected void displaySegments(Color newColor, int newWidth, boolean newDash) {
         if (currentPath != null) {
             Graphics2D g2d = (Graphics2D)g;
             g2d.setColor(inactive ? inactiveColor : currentColor);
             if (currentStroke == null && useStrokes > dist) {
                 if (currentDashed)
                     g2d.setStroke(new BasicStroke(currentWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0,new float[] {9},0));
                 else
                     g2d.setStroke(new BasicStroke(currentWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
             }
             g2d.draw(currentPath);
             if(useStrokes > dist)
                 g2d.setStroke(new BasicStroke(1));
 
             currentPath = new GeneralPath();
             currentColor = newColor;
             currentWidth = newWidth;
             currentDashed = newDash;
             currentStroke = null;
         }
     }
 
     /**
      * Draw the node as small rectangle with the given color.
      *
      * @param n  The node to draw.
      * @param color The color of the node.
      */
     public void drawNode(Node n, Color color, int size, int radius, boolean fill) {
         if (isZoomOk(null) && size > 1) {
             Point p = nc.getPoint(n.eastNorth);
             if ((p.x < 0) || (p.y < 0) || (p.x > nc.getWidth())
                     || (p.y > nc.getHeight()))
                 return;
 
             profilerVisibleNodes++;
 
             g.setColor(color);
             if (fill) {
                 g.fillRect(p.x - radius, p.y - radius, size, size);
                 g.drawRect(p.x - radius, p.y - radius, size, size);
             } else
                 g.drawRect(p.x - radius, p.y - radius, size, size);
 
             if(showNames > dist)
             {
                 String name = getNodeName(n);
                 if (name!=null /* && annotate */)
                 {
                     g.setColor(textColor);
                     Font defaultFont = g.getFont();
                     g.setFont (orderFont);
                     g.drawString (name, p.x+radius+2, p.y+radius+2);
                     g.setFont(defaultFont);
                 }
             }
         }
     }
 
     public void getColors()
     {
         super.getColors();
         untaggedColor = Main.pref.getColor(marktr("untagged"),Color.GRAY);
         textColor = Main.pref.getColor (marktr("text"), Color.WHITE);
     }
 
     // Shows areas before non-areas
     public void visitAll(DataSet data, Boolean virtual) {
 
         boolean profiler = Main.pref.getBoolean("mappaint.profiler",false);
         profilerOmitDraw = Main.pref.getBoolean("mappaint.profiler.omitdraw",false);
         
         useStyleCache = Main.pref.getBoolean("mappaint.cache",true);
         fillAreas = Main.pref.getInteger("mappaint.fillareas", 100000);
         fillAlpha = Math.min(255, Math.max(0, Integer.valueOf(Main.pref.getInteger("mappaint.fillalpha", 50))));
         showNames = Main.pref.getInteger("mappaint.shownames", 100000);
         showIcons = Main.pref.getInteger("mappaint.showicons", 100000);
         useStrokes = Main.pref.getInteger("mappaint.strokes", 100000);
         LatLon ll1 = nc.getLatLon(0,0);
         LatLon ll2 = nc.getLatLon(100,0);
         dist = ll1.greatCircleDistance(ll2);
         
         long profilerStart = java.lang.System.currentTimeMillis();
         long profilerLast = profilerStart;
         int profilerN;
         if(profiler)
             System.out.println("Mappaint Profiler (" +
                 (useStyleCache ? "cache=true, " : "cache=false, ") +
                 "fillareas " + fillAreas + ", " +
                 "fillalpha=" + fillAlpha + "%, " +
                 "dist=" + (int)dist + "m)");
 
         getSettings(virtual);
         useRealWidth = Main.pref.getBoolean("mappaint.useRealWidth",false);
         zoomLevelDisplay = Main.pref.getBoolean("mappaint.zoomLevelDisplay",false);
         circum = Main.map.mapView.getScale()*100*Main.proj.scaleFactor()*40041455; // circumference of the earth in meter
         styles = MapPaintStyles.getStyles().getStyleSet();
         drawMultipolygon = Main.pref.getBoolean("mappaint.multipolygon",true);
         orderFont = new Font(Main.pref.get("mappaint.font","Helvetica"), Font.PLAIN, Main.pref.getInteger("mappaint.fontsize", 8));
         String currentLocale = Locale.getDefault().getLanguage();
         regionalNameOrder = Main.pref.get("mappaint.nameOrder", "name:"+currentLocale+";name;int_name;ref;operator;brand").split(";");
         minEN = nc.getEastNorth(0,nc.getHeight()-1);
         maxEN = nc.getEastNorth(nc.getWidth()-1,0);
         
 
         selectedCall = false;
         ++paintid;
         viewid = nc.getViewID();
 
         profilerVisibleNodes = 0;
         profilerVisibleWays = 0;
         profilerVisibleAreas = 0;
         profilerSegments = 0;
         profilerVisibleSegments = 0;
 
         if(profiler)
         {
             System.out.format("Prepare  : %5dms\n", (java.lang.System.currentTimeMillis()-profilerLast));
             profilerLast = java.lang.System.currentTimeMillis();
         }
 
         if (fillAreas > dist && styles != null && styles.hasAreas()) {
             Collection<Way> noAreaWays = new LinkedList<Way>();
 
             /*** RELATIONS ***/
             profilerN = 0;
             for (final Relation osm : data.relations)
             {
                 if(!osm.deleted && !osm.incomplete && osm.mappaintVisibleCode != viewid)
                 {
                     osm.visit(this);
                     profilerN++;
                 }
             }
 
             if(profiler)
             {
                 System.out.format("Relations: %5dms, calls=%7d\n", (java.lang.System.currentTimeMillis()-profilerLast), profilerN);
                 profilerLast = java.lang.System.currentTimeMillis();
             }
 
             /*** AREAS ***/
             profilerN = 0;
             for (final Way osm : data.ways)
             {
                 if (!osm.incomplete && !osm.deleted
                 && osm.mappaintVisibleCode != viewid && osm.mappaintDrawnCode != paintid)
                 {
                     if(isPrimitiveArea(osm) && osm.mappaintDrawnAreaCode != paintid)
                     {
                         osm.visit(this);
                         profilerN++;
                     } else
                         noAreaWays.add((Way)osm);
                 }
             }
 
             if(profiler)
             {
                 System.out.format("Areas    : %5dms, calls=%7d, visible=%d\n",
                     (java.lang.System.currentTimeMillis()-profilerLast), profilerN, profilerVisibleAreas);
                 profilerLast = java.lang.System.currentTimeMillis();
             }
 
             /*** WAYS ***/
             profilerN = 0;
             fillAreas = 0;
             for (final OsmPrimitive osm : noAreaWays)
             {
                 osm.visit(this);
                 profilerN++;
             }
 
             if(profiler)
             {
                 System.out.format("Ways     : %5dms, calls=%7d, visible=%d\n",
                     (java.lang.System.currentTimeMillis()-profilerLast), profilerN, profilerVisibleWays);
                 profilerLast = java.lang.System.currentTimeMillis();
             }
         }
         else
         {
             /*** WAYS (filling disabled)  ***/
             profilerN = 0;
             for (final OsmPrimitive osm : data.ways)
                 if (!osm.incomplete && !osm.deleted && !osm.selected
                 && osm.mappaintVisibleCode != viewid )
                 {
                     osm.visit(this);
                     profilerN++;
                 }
 
             if(profiler)
             {
                 System.out.format("Ways     : %5dms, calls=%7d, visible=%d\n",
                     (java.lang.System.currentTimeMillis()-profilerLast), profilerN, profilerVisibleWays);
                 profilerLast = java.lang.System.currentTimeMillis();
             }
         }
 
         /*** SELECTED  ***/
         selectedCall = true;
         profilerN = 0;
         for (final OsmPrimitive osm : data.getSelected()) {
             if (!osm.incomplete && !osm.deleted && !(osm instanceof Node)
             && osm.mappaintVisibleCode != viewid && osm.mappaintDrawnCode != paintid)
             {
                 osm.visit(this);
                 profilerN++;
             }
         }
 
         if(profiler)
         {
             System.out.format("Selected : %5dms, calls=%7d\n", (java.lang.System.currentTimeMillis()-profilerLast), profilerN);
             profilerLast = java.lang.System.currentTimeMillis();
         }
 
         /*** DISPLAY CACHED SEGMENTS (WAYS) NOW ***/
         displaySegments();
 
         /*** NODES ***/
         profilerN = 0;
         for (final OsmPrimitive osm : data.nodes)
             if (!osm.incomplete && !osm.deleted
             && osm.mappaintVisibleCode != viewid && osm.mappaintDrawnCode != paintid)
             {
                 osm.visit(this);
                 profilerN++;
             }
 
         if(profiler)
         {
             System.out.format("Nodes    : %5dms, calls=%7d, visible=%d\n",
                 (java.lang.System.currentTimeMillis()-profilerLast), profilerN, profilerVisibleNodes);
             profilerLast = java.lang.System.currentTimeMillis();
         }
 
         /*** VIRTUAL  ***/
         if (virtualNodeSize != 0)
         {
             profilerN = 0;
             currentColor = nodeColor;
             for (final OsmPrimitive osm : data.ways)
                 if (!osm.incomplete && !osm.deleted
                 && osm.mappaintVisibleCode != viewid )
                 {
                     // TODO: move this into the SimplePaint code?
                     if(!profilerOmitDraw)
                         visitVirtual((Way)osm);
                     profilerN++;
                 }
 
             if(profiler)
             {
                 System.out.format("Virtual  : %5dms, calls=%7d\n", (java.lang.System.currentTimeMillis()-profilerLast), profilerN);
                 profilerLast = java.lang.System.currentTimeMillis();
             }
 
             displaySegments(null);
         }
 
         if(profiler)
         {
             System.out.format("Segments :          calls=%7d, visible=%d\n", profilerSegments, profilerVisibleSegments);
             System.out.format("All      : %5dms\n", (profilerLast-profilerStart));
         }
     }
 
     /**
      * Draw a number of the order of the two consecutive nodes within the
      * parents way
      */
     protected void drawOrderNumber(Node n1, Node n2, int orderNumber) {
         Point p1 = nc.getPoint(n1.eastNorth);
         Point p2 = nc.getPoint(n2.eastNorth);
         drawOrderNumber(p1, p2, orderNumber);
     }
 }
