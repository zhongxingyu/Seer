 package at.ac.tuwien.complang.carfactory.domain;
 
 import java.awt.Color;
 import java.io.Serializable;
 
 public class Task implements Serializable {
 
 	//Static Fields
 	private static final long serialVersionUID = -818265961255247515L;
 
 	//Fields
 	private MotorType motortype;
 	private int amount;
 	private int amountCompleted;
 	private Color color;
 	private long id;
 	
 	public Task(long id) {
 		this.id = id;
 	}
 	
 	public MotorType getMotortype() {
 		return motortype;
 	}
 	
 	public void setMotortype(MotorType motortype) {
 		this.motortype = motortype;
 	}
 	
 	public int getAmount() {
 		return amount;
 	}
 	
 	public void setAmount(int amount) {
 		this.amount = amount;
 	}
 	
 	public Color getColor() {
 		return color;
 	}
 	
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public int getAmountCompleted() {
 		return amountCompleted;
 	}
 
 	public void setAmountCompleted(int amountCompleted) {
 		this.amountCompleted = amountCompleted;
 	}
 	
 	public Object[] getObjectData() {
 		String colorString;
 		if(color == null) {
 			colorString = "NONE";
 		} else if(color.equals(Color.RED)) {
 			colorString = "RED";
 		} else if(color.equals(Color.BLUE)) {
 			colorString = "BLUE";
 		} else if(color.equals(Color.GREEN)) {
 			colorString = "GREEN";
 		} else {
 			colorString = String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
 		}
 		return new Object[] {
 				motortype.getType(),
 				colorString,
 				amount,
 				amountCompleted
 		};
 	}
 }
