 package model.items;
 
 import model.geometrical.*;
 
 /**
  * 
  * @author Vidar Eriksson, Jonatan Magnusson
  *
  */
 public class Supply extends Item {
 	
 	/**
 	 * Describes the different types of supply.
 	 * @author Vidar Eriksson
 	 *
 	 */
 	public static enum Type {
 		FOOD (20, "Food"),
 		AMMO (21, "Ammo"),
 		HEALTH (22, "Health");
 		
 		private final int iconNumber;
 		private final String name;
 		
 		/**
 		 * Creates a new type with the specified icon number and name.
 		 * @param iconnumber the number of the icon to display.
 		 * @param name the name.
 		 */
 		Type (int iconnumber, String name){
 			this.iconNumber = iconnumber;
 			this.name = name;
 		}
 
 		/**
 		 * Gives the number of the icon to display.
 		 * @return the number of the icon to display.
 		 */
 		public int getIconNumber() {
 			return this.iconNumber;
 		}
 		
 		@Override
 		public String toString(){
 			return name;
 		}
 
 		/**
 		 * Gives the Type which has the same text as the text provided.
 		 * @param text the text to check with.
 		 * @return the Type which has the same text as the text provided.
 		 */
 		public static Type fromString(String text) {
 			if (text != null) {
 				for (Type b : Type.values()) {
 					if (text.equalsIgnoreCase(b.toString())) {
 						return b;
 					}
 				}
 			}
 			throw new IllegalArgumentException("No enum with name " + text + " found");
 		}
 	}
 	
 	private int amount;
 	private Type type;
 
 	/**
 	 * 
 	 * @param amount the amount the Supply holds
 	 * @param type the type the Supply is
 	 * @param position
 	 */
 	protected Supply(int amount, Type type, Position pos){
 		super(pos, type.getIconNumber());
 		this.amount=amount;
 		this.type=type;
 	}
 	
 	/**
 	 * returns the amount gained from the Supply.
 	 * @return the amount gained from the Supply.
 	 */
 	public int getAmount(){
 		return amount;
 	}
 	
 	/**
 	 * returns the type of Supply.
 	 * @return the type of Supply.
 	 */
 	public Type getType(){
 		return type;
 		
 	}
 	/**
 	 * Gives the name of the supply.
 	 */
 	public String toString(){
 		return amount + " " + type.toString();
 	}
 
 	@Override
 	public void restore(String[] data) {
 		Position pos = new Position(Float.parseFloat(data[1]), Float.parseFloat(data[2]));
 		this.setPosition(pos);
 		this.setIconNumber(Integer.parseInt(data[0]));
 		this.type = Type.fromString(data[3]);
 	}
 
 	@Override
 	public String[] getData() {
 		return new String[] {
 				this.getIconNumber() + "",
 				this.getPosition().getX() + "",
 				this.getPosition().getY() + "",
				this.getType().toString()
 		};
 	}
 }
