 /*
 Author: Michele Pierini
 Date: 05/06/13
 Program Name: DrawingBoard.java
 Objective: Create a program that produces a drawing board.
 */
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 
 @SuppressWarnings("unchecked")
 public class DrawingBoard extends JFrame implements Runnable
 {
     //TOOL TIPS!!
     private JButton save, load, clear, color, brush, erase, draw, 
                     line, rectangle, oval, polygon;
     private JPanel upperButtonPanel, lowerButtonPanel;
     private JToggleButton fill;
     
     private Color c;
     private Point p;
     private String input = "*";
     private String str;
     
     private ArrayList<PointObj> pointObjs = new ArrayList<PointObj>();
     private ArrayList<Line> lines = new ArrayList<Line>();
     private ArrayList<Polygon> polygons = new ArrayList<Polygon>();
     private ArrayList<Integer> polygonXs = new ArrayList<Integer>();
     private ArrayList<Integer> polygonYs = new ArrayList<Integer>();
     private ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
     private ArrayList<Rectangle> ovals = new ArrayList<Rectangle>();
     private ArrayList<Point> ovalPoints = new ArrayList<Point>();
 
     private MouseHandler mh;
     private boolean clearScreen, shouldErase, drawPoints, endRect, endOval, 
                     drawLine, endLine, drawRect, drawPolygon, drawOval, shouldFill;  
     
     //******************************DrawingBoard()**********************
     public DrawingBoard()
     {
         Thread t = new Thread(this, "Drawing Board");
         t.start();
     }
     
     //******************************run()******************************* 
     public void run()
     {
         new JFrame();
         setTitle("Drawing Board");
         setLayout(new BorderLayout());
         getContentPane().setBackground(Color.WHITE);
         setSize(700,700);
         
         buildUpperButtonPanel();
         buildLowerButtonPanel();
 
         add(upperButtonPanel, BorderLayout.NORTH);
         add(lowerButtonPanel, BorderLayout.SOUTH);
         
         mh = new MouseHandler();
         add(mh);
 
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setVisible(true);
     }
     
     //******************************buildUpperButtonPanel()*******************************
     private void buildUpperButtonPanel()
     {
         upperButtonPanel = new JPanel();
         upperButtonPanel.setSize(200,100);
         
         save = new JButton("Save");
         save.setToolTipText("Save the file to be reopened later on this program.");
         load = new JButton("Load");
         load.setToolTipText("Load a file previously saved by this program.");
         clear = new JButton("Clear");
         clear.setToolTipText("Clear the screen of the current drawing.");
         color = new JButton("Color");
         color.setToolTipText("<html>Choose a color before drawing to start with something <br>other than the default black.</html>");
         brush = new JButton("Brush");
         brush.setToolTipText("<html>Enter characters to create a brushstroke to draw in something <br>other than the default star (*).</html>");
         erase = new JButton("Erase");
         //erase is currently the wrong color ....
         erase.setToolTipText("Erases the current drawing using the current brushstroke.");
         draw = new JButton("Draw");
         draw.setToolTipText("<html>Draw in freehand with the default star (*) or choose a different <br>brushstroke with the Brush button.</html>");
         
         JButtonListener jbl = new JButtonListener();
         save.addActionListener(jbl);
         load.addActionListener(jbl);
         clear.addActionListener(jbl);
         color.addActionListener(jbl);
         brush.addActionListener(jbl);
         erase.addActionListener(jbl);
         draw.addActionListener(jbl);
         
         upperButtonPanel.add(save);
         upperButtonPanel.add(load);
         upperButtonPanel.add(clear);
         upperButtonPanel.add(color);
         upperButtonPanel.add(brush);
         upperButtonPanel.add(erase);
         upperButtonPanel.add(draw);
     }
     
     //******************************buildLowerButtonPanel()*******************************
     private void buildLowerButtonPanel()
     {
         lowerButtonPanel = new JPanel();
         lowerButtonPanel.setSize(200,100);
         
         line = new JButton("Line");
         line.setToolTipText("<html>Single click the point where the line should start. <br>Double click the end point.</html>");
         rectangle = new JButton("Rectangle");
         rectangle.setToolTipText("<html>Single click the point that should be the rectangle's top left corner. <br>Double click the point that should be the rectangle's lower right corner to finish it.</html>");
         oval = new JButton("Oval");
         oval.setToolTipText("<html>Single click the point that should be where the oval begins. <br>Double click the point that should give the desired radius and finish the oval.</html>");
         polygon = new JButton("Polygon");
         polygon.setToolTipText("<html>Single click each vertice of the polygon. <br>The last point should be double clicked.");
         fill = new JToggleButton("Fill");
         fill.setToolTipText("<html>Select the Fill button if the figure drawn should be filled with color. <br>Choose a color after clicking the Fill button.</html>");
         
         JButtonListener jbl = new JButtonListener();
         line.addActionListener(jbl);
         rectangle.addActionListener(jbl);
         oval.addActionListener(jbl);
         polygon.addActionListener(jbl);
         fill.addActionListener(jbl);
         
         lowerButtonPanel.add(line);
         lowerButtonPanel.add(rectangle);
         lowerButtonPanel.add(oval);
         lowerButtonPanel.add(polygon);
         lowerButtonPanel.add(fill);
     }
     
     public class JButtonListener implements ActionListener
     {
         //******************************actionPerformed()***********************
         public void actionPerformed(ActionEvent ae)
         {
             if (ae.getSource()==color)
             {
                 c = JColorChooser.showDialog(null, "Color Picker", Color.BLACK);
                 if (c == null)
                     c = Color.BLACK;
             }
             
             if (ae.getSource()==save)
                 save();
             
             if (ae.getSource()==load)
                 load();
             
             if (ae.getSource()==brush)
             {
                 input = JOptionPane.showInputDialog(null, "Enter a brush character or string.");
                 if (input == null)
                     input = "*";
             }
             
             if (ae.getSource()==clear)
                 clearScreen = true;
             
             if (ae.getSource()==erase)
                 shouldErase = true;
             
             if (ae.getSource()==draw)
             {
                 drawPoints = true;
                 c = mh.getLastColor();
             }
             
             if (ae.getSource()==line)
             {
                 str = "line";
                 c = mh.getLastColor();
             }
             
             if (ae.getSource()==rectangle)
             {
                 str = "rectangle";
                 c = mh.getLastColor();
             }
             
             if (ae.getSource()==oval)
             {
                 str = "oval";
                 c = mh.getLastColor();
             }
             
             if (ae.getSource()==polygon)
             {
                 str = "polygon";
                 c = mh.getLastColor();
             }
             
             if (fill.isSelected())
             {
                 shouldFill = true;
             }
             else
             {
                 shouldFill = false;
             }
         }
         
         //******************************save()******************************* 
         public void save()
         {
             JFileChooser jfc = new JFileChooser();
             jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             int returnValue = jfc.showSaveDialog(jfc);
 
             if (returnValue==JFileChooser.APPROVE_OPTION)
             {
                 File f = jfc.getSelectedFile();
                 
                 if (f == null || f.getName().equals(""))
                     JOptionPane.showMessageDialog(null, "Error while trying to save file " + f.getName() + ".");
                 else
                 //serialize
                 {
                     try
                     {
                         FileOutputStream fos = new FileOutputStream(f);
                         ObjectOutputStream oos = new ObjectOutputStream(fos);
 
                         oos.writeObject(pointObjs);
                         oos.writeObject(lines);
                         oos.writeObject(rectangles);
                         oos.writeObject(ovals);
                         oos.writeObject(polygons);
                         
                         oos.flush();
                         oos.close();
                         fos.close();
                     } catch (IOException e) 
                     {
                         e.printStackTrace();
                     }
                 }     
             }
         }
         
         //******************************load()******************************* 
         public void load()
         {
             JFileChooser jfc = new JFileChooser();
             jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             int returnValue = jfc.showOpenDialog(jfc);
             if (returnValue==JFileChooser.APPROVE_OPTION)
             {
                 File f = jfc.getSelectedFile();
                 if (f == null || f.getName().equals(""))
                     JOptionPane.showMessageDialog(null, "Error while trying to open file " + f.getName() + ".");
 
                 else
                 //deserialize
                 {
                     try
                     {
                         FileInputStream fis = new FileInputStream(f);
                         ObjectInputStream ois = new ObjectInputStream(fis);
                         
                         pointObjs = (ArrayList<PointObj>) ois.readObject();
                         lines = (ArrayList<Line>) ois.readObject();
                         rectangles = (ArrayList<Rectangle>) ois.readObject();
                         ovals = (ArrayList<Rectangle>) ois.readObject();
                         polygons = (ArrayList<Polygon>) ois.readObject();
                         
                         if (pointObjs.size() > 0)
                             drawPoints = true;
                         
                         //objects are redisplayed upon click
                         if (lines.size() > 0)
                             str = "line";
                         
                         if (rectangles.size() > 0)
                             str = "rectangle";
                         
                         if (ovals.size() > 0)
                             str = "oval";
                         
                         if (polygons.size() > 0)
                             str = "polygon";    
 
                         fis.close();
                         ois.close();
                     } catch (Exception e) 
                     {
                         e.printStackTrace();
                     }
                 } 
             }
         }
     }
     
     public class PointObj implements Serializable
     {
         private Color c;
         private String brushStroke;
         private Point p;
         //******************************PointObj()*******************************
         public PointObj(Point p, String brushStroke, Color c)
         {
             this.p = p;
             this.brushStroke = brushStroke;
             this.c = c;
         }
     }
     
     public class Line implements Serializable
     {
         private Color c;
         private Point start;
         private Point end;
         //******************************Line()*******************************
         public Line(Point start, Point end, Color c)
         {
             this.start = start;
             this.end = end;
             this.c = c;
         }
     }
     
     public class Rectangle implements Serializable
     {
         private Color c;
         private Point p1;
         private Point p2;
         //******************************Rectangle()*******************************
         public Rectangle(Point p1, Point p2, Color c)
         {
             this.p1 = p1;
             this.p2 = p2;
             this.c = c;
         }
         //******************************getWidth()*******************************
         public int getWidth(Point pt, Point pt2)
         {
             return Math.abs(pt.x - pt2.x);
         }
         //******************************getHeight()*******************************
         public int getHeight(Point pt, Point pt2)
         {
             return Math.abs(pt.y - pt2.y);
         }
     }
 
     public class Polygon implements Serializable
     {
         private Color c;
         private ArrayList<Integer> xCoord = new ArrayList<Integer>();
         private ArrayList<Integer> yCoord = new ArrayList<Integer>();
         
         
         //******************************Polygon()*******************************
         public Polygon(ArrayList<Integer> xCoord, ArrayList<Integer> yCoord, Color c)
         {
             this.xCoord = xCoord;
             this.yCoord = yCoord;
             this.c = c;
         }
     }
     
     public class MouseHandler extends JPanel
     {
         private int mx = 0, my = 0;
         private Color last_color;
         private Point q, start, end;
         private Line line;
         
         //******************************MouseHandler()*************************
         public MouseHandler()
         {
             addMouseListener(new MouseAdapter()
             {
                 //******************************mouseClicked()*******************************
                 public void mouseClicked(MouseEvent me)
                 {
                     mx=me.getX(); 
                     my=me.getY();
                     
                     switch (str)
                     {
                         case "line": drawLine = true; drawRect = false; drawOval = false;
                                      drawPolygon = false; drawPoints = false; break;
                             
                         case "rectangle": drawRect = true; drawLine = false; drawOval = false;
                                           drawPolygon = false; drawPoints = false; break;
                             
                         case "oval": drawOval = true; drawLine = false; drawRect = false;
                                      drawPolygon = false; drawPoints = false; break;
                             
                         case "polygon": drawPolygon = true; drawLine = false; drawOval = false;
                                         drawRect = false; drawPoints = false; break;
                     }
                     
                     //each event ends shape on double click
                     if (drawLine)
                         lineEvent(me);
 
                     if (drawRect)
                         rectEvent(me);
                     
                     if (drawOval)
                         ovalEvent(me);
                     
                     if (drawPolygon)
                         polygonEvent(me);
                 }
                
                 //******************************lineEvent()*******************************
                 public void lineEvent(MouseEvent me)
                 {
                     if (!endLine)
                     { 
                         start = new Point(mx,my);
                         endLine = true;
                     }
                         
                     if (endLine && me.getClickCount()==2)
                     {
                         int endX = me.getX();
                         int endY = me.getY(); 
                         end = new Point(endX, endY);
                             
                         //finish line
                         line = new Line(start, end, c);
                         lines.add(line);
                         endLine = false;
                     }
                 }
                 
                 //******************************rectEvent()*******************************
                 public void rectEvent(MouseEvent me)
                 {
                     if (!endRect)
                     {
                         q = new Point(mx, my);
                         endRect = true; 
                     }
                     
                     if (endRect && me.getClickCount()==2)
                     {
                         int endX = me.getX();
                         int endY = me.getY();
                         Point r = new Point(endX, endY);
                         
                         //finish rect
                         Rectangle rect = new Rectangle(q, r, c);
                         rectangles.add(rect); 
                         endRect = false;
                     }
                 }
                 
                 //******************************ovalEvent()*******************************
                 public void ovalEvent(MouseEvent me)
                 {
                     if (!endOval)
                     {
                         q = new Point(mx, my);
                         endOval = true; 
                     }
                     
                     if (endOval && me.getClickCount()==2)
                     {
                         int endX = me.getX();
                         int endY = me.getY();
                         Point o = new Point(endX, endY);
                         
                         //finish oval
                         Rectangle ov = new Rectangle(q, o, c);
                         ovals.add(ov); 
                         endOval = false;
                     }
                 }
                 
                 //******************************polygonEvent()*******************************
                 public void polygonEvent(MouseEvent me)
                 {
                     polygonXs.add(mx);
                     polygonYs.add(my);
                     
                     if (me.getClickCount()==2) //finish polygon
                     {
                         ArrayList<Integer> finalX = new ArrayList<Integer>();
                         ArrayList<Integer> finalY = new ArrayList<Integer>();
                         finalX.addAll(polygonXs);
                         finalY.addAll(polygonYs);
                         Polygon shape = new Polygon(finalX, finalY, c);
                         polygons.add(shape);
                         polygonXs.clear();
                         polygonYs.clear();
                     }
                 }
                
             });
             
             addMouseMotionListener(new MouseMotionAdapter()
             {
                 //******************************mouseDragged()*******************************
                 public void mouseDragged(MouseEvent me)
                 {
                     mx=me.getX(); 
                     my=me.getY(); 
                     
                     Point p = new Point(mx,my);
                     
                     if (drawPoints)
                     {
                         PointObj pointObj = new PointObj(p, input, c);
                         pointObjs.add(pointObj);
                     }
                     repaint();
                 }
             });
         }
         
         //******************************update()*******************************
         public void update(Graphics g)
         {
             paintComponent(g);
         }
         
         //******************************getLastColor()******************************* 
         public Color getLastColor()
         {
             return last_color;
         }
         
         //******************************paintComponent()*******************************
         public void paintComponent(Graphics g)
         {
             if (clearScreen)
             {
                 g.setColor(Color.WHITE);
                 g.fillRect(0, 0, this.getSize().width, this.getSize().height);
                
                 pointObjs.clear();
                 lines.clear();
                 rectangles.clear();
                 ovals.clear();
                 polygons.clear();
                 
                 clearScreen = false;
             }
             
             if (shouldErase)
             {
                 last_color = c;
                c = Color.WHITE;
                 shouldErase = false;
             }
             
             if (drawPoints)
                 drawPoints(g);
             
             if (drawLine)
                 drawLines(g);
             
             if (drawRect)
                 drawRectangle(g);
             
             if (drawOval)
                 drawOval(g);
             
             if (drawPolygon)
                 drawPolygons(g); 
             
             repaint();
         }
         
         //******************************drawPoints()*******************************
         public void drawPoints(Graphics g)
         {
             int length = pointObjs.size();
             
             if (length > 0)
             {
                 for (PointObj po : pointObjs)
                 {
                     g.setColor(po.c);
                     g.drawString(po.brushStroke,po.p.x - 5, po.p.y - 5);
                 }
             }
         }
         
         //******************************drawLines()*******************************
         public void drawLines(Graphics g)
         {
             int length = lines.size();
             
             if (length > 0)
             {
                 for (Line l : lines)
                 {
                     Point p1 = l.start;
                     Point p2 = l.end;
                     g.setColor(l.c);
                     g.drawLine(p1.x, p1.y, p2.x, p2.y);
                 }
             }
         }
         
         //******************************drawOval()*******************************
         public void drawOval(Graphics g)
         {
             //rectangle is bounds
             int length = ovals.size();
             
             if (length > 0)
             {
                 for (Rectangle o : ovals)
                 {
                     Point q1 = o.p1;
                     Point q2 = o.p2;
                     int w = o.getWidth(q1, q2);
                     int h = o.getHeight(q1, q2);
                     g.setColor(o.c);
                     if (shouldFill)
                         g.fillOval(q1.x, q1.y, w, h);
                     else 
                         g.drawOval(q1.x, q1.y, w, h);
                 }
             }         
         }
         
         //******************************drawRectangle()*******************************
         public void drawRectangle(Graphics g)
         {
             int length = rectangles.size();
             
             if (length > 0)
             {
                 for (Rectangle r : rectangles)
                 {
                     Point q1 = r.p1;
                     Point q2 = r.p2;
                     int w = r.getWidth(q1, q2);
                     int h = r.getHeight(q1, q2);
                     g.setColor(r.c);
                     if (shouldFill)
                         g.fillRect(q1.x, q1.y, w, h);
                     else
                         g.drawRect(q1.x, q1.y, w, h);
                 }
             }         
         }
         
         //******************************drawPolygons()*******************************
         public void drawPolygons(Graphics g)
         {
             int length = polygons.size();
             
             if (length > 0)
             {
                 for (Polygon p: polygons)
                 {
                     int pointCt = p.xCoord.size();
                     int x[] = new int[pointCt];
                     for (int i = 0; i < pointCt; i++)
                     {
                         x[i] = p.xCoord.get(i);
                     }
                     
                     int y[] = new int[pointCt];
                     for (int i = 0; i < pointCt; i++)
                     {
                         y[i] = p.yCoord.get(i);
                     }
                     
                     g.setColor(p.c);
                     if (shouldFill)
                         g.fillPolygon(x, y, pointCt);
                     else 
                         g.drawPolygon(x, y, pointCt);
                 }
             }
         }
     }
 }
