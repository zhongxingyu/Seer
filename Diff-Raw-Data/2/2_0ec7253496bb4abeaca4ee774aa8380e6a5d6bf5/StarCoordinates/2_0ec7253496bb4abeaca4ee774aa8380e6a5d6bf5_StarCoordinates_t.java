 package fr.larez.rampin.starcoordinates;
 
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import processing.core.PApplet;
 
 /**
  * The applet, implemented as a Processing PApplet.
  *
  * @author Rmi Rampin
  */
 public class StarCoordinates extends PApplet implements ComponentListener {
 
     private static final long serialVersionUID = -1501548717704067038L;
 
     private Axis[] m_Axes;
     private Vector<Thing> m_Things;
     private String m_ObjectType;
 
     Point2D.Float m_Origin;
     float m_ScaleX;
     float m_ScaleY;
 
     private Axis m_ActiveAxis = null;
     private static final int ACT_NONE = 0;
     private static final int ACT_HOVER = 1;
     private static final int ACT_DRAGGING = 2;
     private int m_Action = ACT_NONE;
 
     private Axis m_ColorAxis = null;
 
     private OptionsPanel m_OptionsPanel;
 
     /** Display type: default */
     public static final int FILTER_NORMAL = 0x0;
     /** Display type: filtered out */
     public static final int FILTER_NOTSHOWN = 0x1;
     /** Display type: brushed (highlighted) */
     public static final int FILTER_BRUSHED = 0x2;
 
     /**
      * Setup the applet.
      *
      * This method is called initially by Processing to set everything up; it
      * acts as a constructor.
      */
     @Override
     public void setup()
     {
         size(800, 800);
         try
         {
             loadData("cars.csv");
         }
         catch(IOException e)
         {
             JOptionPane.showMessageDialog(this, "Impossible de charger le fichier", "Erreur", JOptionPane.ERROR_MESSAGE);
         }
 
         m_ScaleX = getWidth() * 0.4f;
         m_ScaleY = getHeight() * 0.4f;
         m_Origin = new Point2D.Float(getWidth()*0.5f, getHeight()*0.5f);
 
         m_OptionsPanel = new OptionsPanel(m_Axes, this);
         m_OptionsPanel.layout(getWidth(), getHeight());
 
         rectMode(CORNER);
         noLoop();
         frame.setResizable(true);
         addComponentListener(this);
     }
 
     /**
      * Rendering method.
      *
      * This method is called everytime the applet needs to be drawn again. It
      * calls other draw() methods to show all the different parts of the
      * visualization.
      */
     @Override
     public void draw()
     {
         // Clear!
         background(255, 255, 255);
 
         // Autoscaling -- 'cause we can!
         m_ScaleX = width * 0.4f;
         m_ScaleY = height * 0.4f;
         m_Origin = new Point2D.Float(width*0.5f, height*0.5f);
         int ox = (int)m_Origin.x;
         int oy = (int)m_Origin.y;
 
         // Calibrate the axes, if they want to
         for(Axis axis : m_Axes)
             axis.calibrate();
 
         // The things -- gotta draw'em all
         for(Thing thing : m_Things)
         {
             // Project it someplace funny
             Point2D.Float pos = new Point2D.Float();
             int filters = thing.isBrushed()?FILTER_BRUSHED:FILTER_NORMAL;
             for(Axis axis : m_Axes)
             {
                 if(!axis.isShown())
                     continue;
                 if(axis.filter(thing))
                     filters |= FILTER_NOTSHOWN;
                 Point2D.Float proj = axis.project(thing);
                 pos.x += proj.x;
                 pos.y += proj.y;
             }
 
             // Draw it
             switch(filters)
             {
             case FILTER_NORMAL:
                 stroke(0, 0, 0);
                 break;
             case FILTER_BRUSHED:
                 stroke(255, 0, 0);
                 break;
             case FILTER_BRUSHED | FILTER_NOTSHOWN:
                 stroke(255, 191, 191);
                 break;
             case FILTER_NOTSHOWN:
                 stroke(191, 191, 191);
                 break;
             }
             int x = ox + (int)(pos.x * m_ScaleX);
             int y = oy + (int)(pos.y * m_ScaleY);
             line(x-2, y, x+2, y);
             line(x, y-2, x, y+2);
         }
 
         // And the axes too
         final float ascent = textAscent();
         final float descent = textDescent();
         for(Axis axis : m_Axes)
         {
             if(!axis.isShown())
                 continue;
 
             // The axis
             int x = ox + (int)(axis.getEndPoint().x * m_ScaleX);
             int y = oy + (int)(axis.getEndPoint().y * m_ScaleY);
 
             stroke(0, 0, 0);
             line(ox, oy, x, y);
 
             // The handle
             noStroke();
             if(m_ActiveAxis == axis && m_Action == ACT_DRAGGING)
                 fill(255, 0, 0);
             else if(m_ActiveAxis == axis && m_Action == ACT_HOVER)
                 fill(255, 255, 0);
            else
                fill(0, 0, 0);
             rect(x-3, y-3, 6, 6);
 
             // Draw the label, a little away from the handle
             fill(0, 0, 0);
             float dx = -axis.getEndPoint().y;
             float dy = axis.getEndPoint().x;
             float il = 1.0f/(float)Math.sqrt(dx*dx + dy*dy);
             int lx = (int)(dx * il * 10.0f) + x;
             int ly = (int)(dy * il * 10.0f) + y;
             String label = axis.getLabel() + " (" + axis.getEndValue() + ")";
             lx -= textWidth(label)/2;
             ly += ascent/2 - descent/2;
             text(axis.getLabel() + " (" + axis.getEndValue() + ")", lx, ly);
         }
 
         // Hover on things
         Thing closest = null;
         float closest_sqdist = 999999.0f;
         for(Thing t : m_Things)
         {
             Point2D.Float pos = new Point2D.Float();
             boolean filtered = false;
             for(Axis axis : m_Axes)
             {
                 if(!axis.isShown())
                     continue;
                 if(axis.filter(t))
                 {
                     filtered = true;
                     break;
                 }
                 Point2D.Float proj = axis.project(t);
                 pos.x += proj.x;
                 pos.y += proj.y;
             }
             if(!filtered)
             {
                 pos.x = m_Origin.x + pos.x * m_ScaleX;
                 pos.y = m_Origin.y + pos.y * m_ScaleY;
                 float dx = pos.x - mouseX;
                 float dy = pos.y - mouseY;
                 float sqdist = dx*dx + dy*dy;
                 if(closest == null || sqdist < closest_sqdist)
                 {
                     closest = t;
                     closest_sqdist = sqdist;
                 }
             }
         }
         if(closest_sqdist < 100.0f)
         {
             // Some lines to show the coordinates
             {
                 float x = m_Origin.x, y = m_Origin.y;
                 for(int i = 0; i < m_Axes.length; i++)
                 {
                     final Axis axis = m_Axes[i];
                     if(!axis.isShown())
                         continue;
                     Point2D.Float proj = axis.project(closest);
                     float x2 = x + proj.x * m_ScaleX;
                     float y2 = y + proj.y * m_ScaleY;
                     stroke(191, 255, 191);
                     line(x, y, x2, y2);
                     stroke(191, 191, 255);
                     float ex = m_Origin.x + proj.x * m_ScaleX;
                     float ey = m_Origin.y + proj.y * m_ScaleY;
                     line(m_Origin.x, m_Origin.y, ex, ey);
                     fill(0, 0, 255);
                     Utils.centeredText(g, String.valueOf(closest.getCoordinate(i)), ex, ey);
                     x = x2;
                     y = y2;
                 }
             }
             // Tooltip
             Utils.drawTooltip(g, closest.getName(), mouseX, mouseY);
         }
 
         // The OptionsPanel
         m_OptionsPanel.draw(g);
     }
 
     /**
      * Initial import from a file.
      *
      * Loads data from a CSV file, to be shown in the applet.
      *
      * @param file The filename to use.
      * @throws IOException On I/O problem or syntax error.
      */
     private void loadData(String file) throws IOException
     {
         m_Things = new Vector<Thing>();
 
         String[] lines = loadStrings(file);
 
         // First line contains the columns names
         StringTokenizer names = new StringTokenizer(lines[0], ";");
         int nb_tokens = names.countTokens();
 
         // Second line contains the datatypes for each column
         StringTokenizer types = new StringTokenizer(lines[1], ";");
         if(types.countTokens() != nb_tokens)
             throw new IOException("Unexpected number of types on line 2");
 
         // First field should be the object tags
         String labeltype = types.nextToken();
         if(!labeltype.toLowerCase().equals("string"))
             throw new IOException("First row doesn't have STRING type");
         m_ObjectType = names.nextToken();
 
         // Create the axes
         m_Axes = new Axis[nb_tokens-1];
         int i = 0;
         while(names.hasMoreTokens())
         {
             m_Axes[i] = new Axis(names.nextToken(), types.nextToken(), i);
             i++;
         }
 
         // Useful infos are useful
         System.out.println("Things are " + m_ObjectType + ". There are " + (lines.length - 2) + " things with " + (nb_tokens - 1) + " dimensions :");
         for(int j = 0; j < m_Axes.length; j++)
         {
             final Axis a = m_Axes[j];
             System.out.print(a.getLabel() + " (" + ((a.getType() == Axis.NUMBER)?"number":"category") + ")");
             if(j != m_Axes.length - 1)
                 System.out.print(", ");
             else
                 System.out.println();
         }
 
         // Read ALL the things!
         for(i = 2; i < lines.length; i++)
         {
             StringTokenizer fields = new StringTokenizer(lines[i], ";");
             if(fields.countTokens() != nb_tokens)
                 throw new IOException("Unexpected number of values on line " + (i+1));
             String name = fields.nextToken();
             float[] coordinates = new float[nb_tokens-1];
             int j = 0;
             while(fields.hasMoreTokens())
             {
                 String c = fields.nextToken();
                 if(m_Axes[j].getType() == Axis.NUMBER)
                 {
                     float v = Float.parseFloat(c); // May throw NumberFormatException
                     m_Axes[j].value(v);
                     coordinates[j] = v;
                 }
                 else if(m_Axes[j].getType() == Axis.CATEGORY)
                     coordinates[j] = m_Axes[j].category(c);
                 j++;
             }
             m_Things.add(new Thing(name, coordinates));
         }
 
         // Don't show all the axes -- THEY ARE OVER 9000!
         int shown = Math.min(m_Axes.length, 10);
         float angle_inc = (float)(Math.PI*2.0/shown);
         for(int j = 0; j < m_Axes.length; j++)
         {
             if(j < shown)
                 m_Axes[j].setEndPoint(-(float)Math.sin(j*angle_inc), -(float)Math.cos(j*angle_inc));
             else
                 m_Axes[j].setShown(false);
         }
         System.out.println(shown + " axes are displayed");
     }
 
     /**
      * Set the axis which is used for color assignation.
      */
     public void setColorAxis(Axis axis)
     {
         m_ColorAxis = axis;
     }
 
     /**
      * Retrieve the axis which is used for color assignation, or null.
      */
     public Axis getColorAxis()
     {
         return m_ColorAxis;
     }
 
     private Axis findAxisUnder(int x, int y)
     {
         int ox = (int)m_Origin.x;
         int oy = (int)m_Origin.y;
         for(Axis axis : m_Axes)
         {
             int ax = (int)(axis.getEndPoint().x * m_ScaleX) + ox;
             int ay = (int)(axis.getEndPoint().y * m_ScaleY) + oy;
 
             int dx = ax - x;
             int dy = ay - y;
             if(dx*dx + dy*dy <= 100)
                 return axis;
         }
         return null;
     }
 
     @Override
     public void mouseDragged()
     {
         if(m_OptionsPanel.active())
             m_OptionsPanel.drag(mouseX, mouseY);
 
         // Drag axes endpoints
         if(m_Action == ACT_DRAGGING)
         {
             float x = (mouseX - m_Origin.x)/m_ScaleX;
             float y = (mouseY - m_Origin.y)/m_ScaleY;
             m_ActiveAxis.setEndPoint(x, y);
             redraw();
         }
     }
 
     @Override
     public void mouseMoved()
     {
         // Hover on axes
         if(m_Action == ACT_NONE || m_Action == ACT_HOVER)
         {
             m_ActiveAxis = findAxisUnder(mouseX, mouseY);
             if(m_ActiveAxis == null)
                 m_Action = ACT_NONE;
             else
                 m_Action = ACT_HOVER;
             redraw();
         }
     }
 
     @Override
     public void mousePressed()
     {
         int button;
         switch(mouseButton)
         {
         case LEFT:
             button = 1;
             break;
         case RIGHT:
             button = 2;
             break;
         default:
             button = 3;
             break;
         }
         if(m_OptionsPanel.click(mouseX, mouseY, button))
             return;
 
         m_ActiveAxis = findAxisUnder(mouseX, mouseY);
         if(m_ActiveAxis == null)
             m_Action = ACT_NONE;
         else
             m_Action = ACT_DRAGGING;
         redraw();
     }
 
     @Override
     public void mouseReleased()
     {
         if(m_OptionsPanel.active())
             m_OptionsPanel.release(mouseX, mouseY);
 
         if(m_Action == ACT_DRAGGING)
         {
             m_Action = ACT_NONE;
             mouseMoved();
             redraw();
         }
     }
 
     @Override
     public void componentResized(ComponentEvent e)
     {
         m_OptionsPanel.layout(getWidth(), getHeight());
                 // PApplet#width and PApplet#height are not set at this point
         redraw();
     }
 
     @Override
     public void componentMoved(ComponentEvent e) {}
 
     @Override
     public void componentShown(ComponentEvent e) {}
 
     @Override
     public void componentHidden(ComponentEvent e) {}
 
 }
