 package models;
 
 import javax.persistence.Transient;
 
 import models.iface.Collidable;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Player
 {
 	private static Integer PLAYER_INDEX = 0;
 	private static final Double PLAYER_RADIUS = 20D;
 	private static final String[] PLAYER_COLOR = new String[]{ "#ff0000", "#00ff00", "#0000ff", "#0fff00", "#ffff00" };
 
 	private final String name;
 	private final String color;
 
 	private Date time;
 	private Double a;
 	private Double v;
 	private Double x;
 	private Double y;
 	private Integer direction;
 	private List<Collidable> parts;
     private boolean isReady;
 
 	public Player(String name)
 	{
 		this.name = name;
 		this.color = PLAYER_COLOR[PLAYER_INDEX % PLAYER_COLOR.length];
 		this.direction = 0;
 		this.parts = new ArrayList<Collidable>();
 
 		PLAYER_INDEX++;
 	}
 
 	public String getColor()
 	{
 		return color;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public Date getTime()
 	{
 		return time;
 	}
 
 	public void setTime(Date time)
 	{
 		this.time = time;
 	}
 
 	public Double getA()
 	{
 		return a;
 	}
 
 	public void setA(Double a)
 	{
 		this.a = a;
 	}
 
 	public Double getX()
 	{
 		return x;
 	}
 
 	public void setX(Double x)
 	{
 		this.x = x;
 	}
 
 	public Double getY()
 	{
 		return y;
 	}
 
 	public void setY(Double y)
 	{
 		this.y = y;
 	}
 
 	public Double getR()
 	{
 		return PLAYER_RADIUS;
 	}
 
 	public Double getV()
 	{
 		return v;
 	}
 
 	public void setV(Double v)
 	{
 		this.v = v;
 	}
 
 	public Integer getDirection()
 	{
 		return direction;
 	}
 
 	public void setDirection(Integer direction)
 	{
 		this.direction = direction;
 	}
 
     public boolean isReady()
 	{
         return isReady;
     }
 
     public void setReady(boolean ready)
 	{
         isReady = ready;
     }
 
 	@Transient
 	public List<Collidable> getParts()
 	{
 		return parts;
 	}
 
 	public void addPart(Collidable part)
 	{
 		if ((part instanceof Arc) || (part instanceof Line))
 		{
 			parts.add(part);
 		}
 		else
 		{
 			throw new IllegalArgumentException("Can only add Line or Arc to part list.");
 		}
 	}
 
 	public Object flush(Board.PlayerState extrapolated)
 	{
 		Object retval =  null;
 
 		if (this.direction == 0) // Line
 		{
 			Line line = new Line();
 			line.setX(this.x);
 			line.setY(this.y);
 			line.setDx(extrapolated.getX() - this.x);
 			line.setDy(extrapolated.getY() - this.y);
 			retval = line;
 		}
 		else
 		{
 			Arc arc = new Arc();
 
 			arc.setX(this.getR() * Math.cos(this.getA() + this.getDirection() * Math.PI / 2) + this.getX());
 			arc.setY(this.getR() * Math.sin(this.getA() + this.getDirection() * Math.PI / 2) + this.getY());
 			arc.setR(this.getR());
 			arc.setAs(this.getA() - this.getDirection() * Math.PI / 2);
			arc.setAe(arc.getAs() + this.getDirection() * (extrapolated.getA() - this.getA()));
 			arc.setDirection(this.getDirection());
 
 			retval = arc;
 		}
 
 		this.setA(extrapolated.getA());
 		this.setX(extrapolated.getX());
 		this.setY(extrapolated.getY());
 
 		return retval;
 	}
 }
