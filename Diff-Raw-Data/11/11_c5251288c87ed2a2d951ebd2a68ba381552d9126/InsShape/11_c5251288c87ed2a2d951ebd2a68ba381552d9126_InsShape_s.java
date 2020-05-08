 package com.inspedio.entity;
 
 import javax.microedition.lcdui.Graphics;
 
 import com.inspedio.entity.primitive.InsSize;
 import com.inspedio.enums.ShapeType;
 import com.inspedio.system.helper.InsUtil;
 
 /**
  * <code>InsShape</code> is a basic shape, which can be dynamically changed.<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsShape extends InsBasic{
 
 	/**
 	 * Type of Shape, either Rectangle, Rounded Rectangle, or Circle
 	 */
 	protected ShapeType shapeType;
 	/**
 	 * The color to be drawn inside of shape
 	 */
 	protected int fillColor;
 	/**
 	 * TRUE if you want to draw filled rectangle. FALSE if only border
 	 */
 	protected boolean isFill;
 	/**
 	 * The color of shape border
 	 */
 	protected int borderColor;
 	/**
 	 * The width of Border
 	 */
 	protected int borderWidth;
 	
 	protected InsSize roundArc;
 	protected int startAngle;
 	protected int arcAngle;
 	
 	public InsShape(int X, int Y, int Width, int Height){
 		super(X, Y, Width, Height);
 		this.setColor(0x000000, true);
 		this.setBorder(0xFFFFFF, 2);
 		this.setRectangle(Width, Height);
 		this.roundArc = new InsSize(10,10);
 	}
 	
 	public void setColor(int Color, boolean IsFill){
 		this.fillColor = Color;
 		this.isFill = IsFill;
 	}
 	
 	public void setBorder(int Color, int Width){
 		this.borderColor = Color;
 		this.borderWidth = Width;
 	}
 	
 	public void setRoundedRect(int Width, int Height, int ArcWidth, int ArcHeight){
 		this.setSize(Width, Height);
 		this.roundArc.setSize(ArcWidth, ArcHeight);
 		this.shapeType = ShapeType.ROUNDED_RECT;
 	}
 	
 	public void setCircle(int Radius, int StartAngle, int ArcAngle){
 		this.setSize(Radius * 2, Radius * 2);
 		this.startAngle = StartAngle;
 		this.arcAngle = ArcAngle;
 		this.shapeType = ShapeType.CIRCLE;
 	}
 	
 	public void setRectangle(int Width, int Height){
 		this.setSize(Width, Height);
 		this.shapeType = ShapeType.RECTANGLE;
 	}
 	
 	public void draw(Graphics g)
 	{
 		if(this.isFill)
 		{
 			g.setColor(this.borderColor);
 			if(this.shapeType == ShapeType.RECTANGLE){
 				g.fillRect(this.getLeft(), this.getTop(), this.size.width, this.size.height);
 			} else if(this.shapeType == ShapeType.ROUNDED_RECT){
 				g.fillRoundRect(this.getLeft(), this.getTop(), this.size.width, this.size.height, this.roundArc.width, this.roundArc.height);
 			} else if(shapeType == ShapeType.CIRCLE){
 				g.fillArc(this.getLeft(), this.getTop(), this.size.width / 2, this.size.height / 2, this.startAngle, this.arcAngle);
 			}
 			
 			g.setColor(this.fillColor);
 			if(this.shapeType == ShapeType.RECTANGLE){
 				g.fillRect(this.getLeft() + this.borderWidth, this.getTop() + this.borderWidth, this.size.width - (this.borderWidth * 2), this.size.height - (this.borderWidth * 2));
 			} else if(this.shapeType == ShapeType.ROUNDED_RECT){
 				g.fillRoundRect(this.getLeft() + this.borderWidth, this.getTop() + this.borderWidth, this.size.width - (this.borderWidth * 2), this.size.height - (this.borderWidth * 2), this.roundArc.width, this.roundArc.height);
 			} else if(this.shapeType == ShapeType.CIRCLE){
				g.fillArc(this.getLeft() + this.borderWidth, this.getTop() + this.borderWidth, (this.size.width / 2) - this.borderWidth, (this.size.height / 2) - this.borderWidth, this.startAngle, this.arcAngle);
 			}
 			
 		}
 		else
 		{
 			g.setColor(this.borderColor);
 			if(this.shapeType == ShapeType.RECTANGLE){
 				for(int i = 0; i < this.borderWidth; i ++){
 					g.drawRect(this.getLeft() + i, this.getTop() + i, this.size.width - (i * 2), this.size.height - (i * 2));
 				}
 			} if(this.shapeType == ShapeType.ROUNDED_RECT){
 				for(int i = 0; i < this.borderWidth; i ++){
 					g.drawRoundRect(this.getLeft() + i, this.getTop() + i, this.size.width - (i * 2), this.size.height - (i * 2), this.roundArc.width, this.roundArc.height);
 				}
 			} if(this.shapeType == ShapeType.CIRCLE){
 				for(int i = 0; i < this.borderWidth; i ++){
					g.drawArc(this.getLeft() + i, this.getTop() + i, (this.size.width / 2) - i, (this.size.height / 2) - i, this.startAngle, this.arcAngle);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Whether X and Y pointer is inside object
 	 */
 	public boolean isOverlap(int X, int Y){
 		if(this.shapeType == ShapeType.CIRCLE){
 			return (InsUtil.Distance(X, this.getMiddleX(), Y, this.getMiddleY()) <= (this.size.width / 2));
 		}
 		
 		return super.isOverlap(X, Y);
 	}
 }
