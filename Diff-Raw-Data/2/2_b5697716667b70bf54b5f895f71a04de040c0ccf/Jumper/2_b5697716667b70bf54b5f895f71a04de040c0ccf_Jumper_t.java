 package com.greenteam.huntjumper.model;
 
 import com.greenteam.huntjumper.Camera;
 import com.greenteam.huntjumper.IVisibleObject;
 import com.greenteam.huntjumper.contoller.IJumperController;
 import com.greenteam.huntjumper.utils.*;
 import net.phys2d.math.ROVector2f;
 import net.phys2d.raw.Body;
 import net.phys2d.raw.shapes.Circle;
 import org.newdawn.slick.*;
 
 import static com.greenteam.huntjumper.utils.Vector2D.fromRadianAngleAndLength;
 
 /**
  * User: GreenTea Date: 14.01.12 Time: 21:11
  */
 public class Jumper implements IVisibleObject
 {
    private static Image lighting;
 
    static
    {
       try
       {
          lighting = new Image("lighting.png").getScaledCopy(1.12f);
       }
       catch (SlickException e)
       {
          e.printStackTrace();
       }
    }
 
    private String playerName;
 
    private Color color;
    private Circle bodyCircle;
    private Body body;
 
    private JumperRole jumperRole = JumperRole.Hunting;
    private IJumperController controller;
 
 
    public Jumper(String playerName, Color color, ROVector2f startPos,
                  IJumperController controller)
    {
       this.playerName = playerName;
       this.color = color;
       bodyCircle = new Circle(GameConstants.JUMPER_RADIUS);
       body = new Body(bodyCircle, GameConstants.JUMPER_MASS);
       body.setMaxVelocity(GameConstants.MAX_VELOCITY, GameConstants.MAX_VELOCITY);
       body.setPosition(startPos.getX(), startPos.getY());
       body.setRestitution(1.0f);
       this.controller = controller;
    }
 
    public Body getBody()
    {
       return body;
    }
 
    public JumperRole getJumperRole()
    {
       return jumperRole;
    }
 
    public void setJumperRole(JumperRole jumperRole)
    {
       this.jumperRole = jumperRole;
    }
 
    public String getPlayerName()
    {
       return playerName;
    }
 
 
    public Color getColor()
    {
       return color;
    }
 
    public void setColor(Color color)
    {
       this.color = color;
    }
 
    public Circle getBodyCircle()
    {
       return bodyCircle;
    }
 
    public void setBodyCircle(Circle bodyCircle)
    {
       this.bodyCircle = bodyCircle;
    }
 
    public void update(int delta)
    {
      controller.update(this);
    }
 
    public void draw(Graphics g)
    {
       Point viewCenter = Camera.instance().toView(getBody().getPosition());
       float radius = getBodyCircle().getRadius();
 
       org.newdawn.slick.geom.Circle viewCircle = new org.newdawn.slick.geom.Circle(
               viewCenter.getX(), viewCenter.getY(), radius);
 
       g.drawImage(lighting, viewCenter.getX() - lighting.getWidth()/2 + 0.6f,
               viewCenter.getY() - lighting.getHeight()/2 + 0.6f, jumperRole.getRoleColor());
 
       g.setColor(getColor());
       g.fill(viewCircle);
       g.setColor(ViewConstants.jumperBorderColor);
       g.draw(viewCircle);
 
       g.setColor(Color.black);
       final int segmentsCount = 6;
       final float anglePerSegment = 360 / segmentsCount;
       Vector2D rotationDirection = fromRadianAngleAndLength(getBody().getRotation(), 0.9f*radius);
       for (int i = 0; i < segmentsCount; ++i)
       {
          Vector2D vectorFromCenter = new Vector2D(rotationDirection);
          vectorFromCenter.plus(new Vector2D(viewCenter));
 
          g.drawLine(viewCenter.getX(), viewCenter.getY(),
                  vectorFromCenter.getX(), vectorFromCenter.getY());
          rotationDirection.rotate(anglePerSegment);
       }
 
    }
 }
