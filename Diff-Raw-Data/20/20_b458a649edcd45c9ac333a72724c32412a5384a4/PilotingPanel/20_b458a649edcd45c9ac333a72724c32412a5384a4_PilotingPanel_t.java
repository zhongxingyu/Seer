 package org.lsfn.console_pc;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import org.lsfn.console_pc.STS.STSdown;
 import org.lsfn.console_pc.STS.STSup;
 import org.lsfn.console_pc.SpaceObject.ObjectType;
 
 public class PilotingPanel extends JPanel implements MouseListener, KeyListener {
     
     // TODO zoom
     private static int scale = 10;
     
     private PilotingDisplay pilotingDisplay;
     private List<SpaceObject> objectList;
     private boolean[] keyStates, keyStatesChanged;
     
     public PilotingPanel(PilotingDisplay pilotingDisplay) {
         this.pilotingDisplay = pilotingDisplay;
         this.objectList = new ArrayList<SpaceObject>();
         
         /* Key state array indices correspondence chart:
             0: Turn Anti-clockwise
             1: Turn Clockwise
             2: Strafe Left
             3: Strafe Right
             4: Forward Thrust
             5: Backwards Thrust
         */
         this.keyStates = new boolean[6];
         for(int i = 0; i < this.keyStates.length; i++) {
             this.keyStates[i] = false;
         }
         this.keyStatesChanged = new boolean[6];
         for(int i = 0; i < this.keyStatesChanged.length; i++) {
             this.keyStatesChanged[i] = false;
         }
         
         this.addMouseListener(this);
     }
     
     private void paintCalibration(Graphics g) {
         int squareSize = 10;
         Rectangle bounds = this.getBounds();
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, squareSize, squareSize);
         g.fillRect(bounds.width - squareSize - 1, 0, squareSize, squareSize);
         g.fillRect(0, bounds.height - squareSize - 1, squareSize, squareSize);
         g.fillRect(bounds.width - squareSize - 1, bounds.height - squareSize - 1, squareSize, squareSize);
         g.setColor(Color.RED);
         g.drawRect(0, 0, squareSize, squareSize);
         g.drawRect(bounds.width - squareSize - 1, 0, squareSize, squareSize);
         g.drawRect(0, bounds.height - squareSize - 1, squareSize, squareSize);
         g.drawRect(bounds.width - squareSize - 1, bounds.height - squareSize - 1, squareSize, squareSize);
     }
     
     private void paintBackground(Graphics2D g2d) {
         int width = this.getBounds().width;
         int height = this.getBounds().height;
        g2d.setColor(new Color(0, 0, 128));
         if(width > height) {
             g2d.fillOval(width/2 - height/2, 0, height, height);
         } else {
             g2d.fillOval(0, height/2 - width/2, width, width);
         }        
     }
     
     private void paintObjects(Graphics2D g2d) {
         for(SpaceObject so : this.objectList) {
             if(so.getObjectType() == ObjectType.SHIP) {
                 int[] xs = new int[3];
                 int[] ys = new int[3];
                xs[0] = (int)(convertX((-Math.sin(so.getTheta()) * 2) + so.getX()));
                ys[0] = (int)(convertY((Math.cos(so.getTheta()) * 2) + so.getY()));
                xs[1] = (int)(convertX(Math.cos(so.getTheta()) + so.getX()));
                ys[1] = (int)(convertY(Math.sin(so.getTheta()) + so.getY()));
                xs[2] = (int)(convertX(-Math.cos(so.getTheta()) + so.getX()));
                ys[2] = (int)(convertY(-Math.sin(so.getTheta()) + so.getY()));
                 g2d.setColor(Color.GREEN);
                 g2d.fillPolygon(xs, ys, 3);
             } else if(so.getObjectType() == ObjectType.ASTEROID) {
                 g2d.setColor(Color.YELLOW);
                g2d.fillOval((int)convertX(so.getX()) - scale, (int)convertY(so.getY()) - scale, scale*2, scale*2);
             }
         }
     }
     
     private void paintOwnShip(Graphics2D g2d) {
         int centreX = this.getBounds().width / 2;
         int centreY = this.getBounds().height / 2;
         int[] xs = new int[3];
         int[] ys = new int[3];
         xs[0] = centreX;
         ys[0] = -(2 * scale) + centreY;
         xs[1] = -scale + centreX;
         ys[1] = centreY;
         xs[2] = scale + centreX;
         ys[2] = centreY;
         g2d.setColor(Color.GREEN);
         g2d.fillPolygon(xs, ys, 3);
     }
     
     private void paintElements(Graphics g) {
         Graphics2D g2d = (Graphics2D) g;
         paintBackground(g2d);
         paintObjects(g2d);
         paintOwnShip(g2d);
     }
     
     private void clearPanel(Graphics g) {
         Rectangle bounds = this.getBounds();
         Graphics2D g2d = (Graphics2D) g;
         g2d.setColor(Color.BLACK);
         g2d.fill(bounds);
     }
     
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponents(g);
         clearPanel(g);
         //paintCalibration(g);
         paintElements(g);
     }
     
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(1280, 1024);
     }
 
     public void processVisualSensors(STSdown.VisualSensors visualSensors) {
         this.objectList.clear();
         for(STSdown.VisualSensors.SpaceObject spaceObject : visualSensors.getSpaceObjectsList()) {
             SpaceObject.ObjectType objectType = ObjectType.SHIP;
             if(spaceObject.getType() == STSdown.VisualSensors.SpaceObject.Type.SHIP) {
                 objectType = ObjectType.SHIP;
             } else if(spaceObject.getType() == STSdown.VisualSensors.SpaceObject.Type.ASTEROID) {
                 objectType = ObjectType.ASTEROID;
             }
             SpaceObject so = new SpaceObject(objectType, spaceObject.getPosition().getX(), spaceObject.getPosition().getY(), spaceObject.getOrientation());
             this.objectList.add(so);
         }
     }
     
     public void generateOutput() {
         boolean anyChanged = false;
         for(int i = 0; i < this.keyStatesChanged.length; i++) {
             anyChanged |= this.keyStatesChanged[i];
         }
         if(anyChanged) {
             STSup.Builder stsUp = STSup.newBuilder();
             STSup.Piloting.Builder stsUpPiloting = STSup.Piloting.newBuilder();
             if(this.keyStatesChanged[0]) {
                 stsUpPiloting.setTurnAnti(this.keyStates[0]);
                 this.keyStatesChanged[0] = false;
             }
             if(this.keyStatesChanged[1]) {
                 stsUpPiloting.setTurnClock(this.keyStates[1]);
                 this.keyStatesChanged[1] = false;
             }
             if(this.keyStatesChanged[2]) {
                 stsUpPiloting.setThrustLeft(this.keyStates[2]);
                 this.keyStatesChanged[2] = false;
             }
             if(this.keyStatesChanged[3]) {
                 stsUpPiloting.setThrustRight(this.keyStates[3]);
                 this.keyStatesChanged[3] = false;
             }
             if(this.keyStatesChanged[4]) {
                 stsUpPiloting.setThrustForward(this.keyStates[4]);
                 this.keyStatesChanged[4] = false;
             }
             if(this.keyStatesChanged[5]) {
                 stsUpPiloting.setThrustBackward(this.keyStates[5]);
                 this.keyStatesChanged[5] = false;
             }
             stsUp.setPiloting(stsUpPiloting);
             this.pilotingDisplay.getConsolePC().getStarshipConnection().sendMessageToStarship(stsUp.build());
         }
     }
     
     @Override
     public void keyPressed(KeyEvent e) {
         if(e.getKeyCode() == KeyEvent.VK_Q) {
             this.keyStates[0] = true;
             this.keyStatesChanged[0] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_E) {
             this.keyStates[1] = true;
             this.keyStatesChanged[1] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_A) {
             this.keyStates[2] = true;
             this.keyStatesChanged[2] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_D) {
             this.keyStates[3] = true;
             this.keyStatesChanged[3] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_W) {
             this.keyStates[4] = true;
             this.keyStatesChanged[4] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_S) {
             this.keyStates[5] = true;
             this.keyStatesChanged[5] = true;
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         if(e.getKeyCode() == KeyEvent.VK_Q) {
             this.keyStates[0] = false;
             this.keyStatesChanged[0] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_E) {
             this.keyStates[1] = false;
             this.keyStatesChanged[1] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_A) {
             this.keyStates[2] = false;
             this.keyStatesChanged[2] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_D) {
             this.keyStates[3] = false;
             this.keyStatesChanged[3] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_W) {
             this.keyStates[4] = false;
             this.keyStatesChanged[4] = true;
         } else if(e.getKeyCode() == KeyEvent.VK_S) {
             this.keyStates[5] = false;
             this.keyStatesChanged[5] = true;
         }
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
         
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         
     }
     
     private double convertX(double x) {
         return (x * scale) + (this.getBounds().width/2);
     }
     
     private double convertY(double y) {
         return -(y * scale) + (this.getBounds().height/2);
     }
 }
