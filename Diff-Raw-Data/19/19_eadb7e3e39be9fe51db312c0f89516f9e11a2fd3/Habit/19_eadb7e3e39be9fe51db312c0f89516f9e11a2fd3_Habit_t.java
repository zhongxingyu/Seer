 package com.magicmicky.habitrpgmobileapp.habits;
 
 
 /**
  * An habit item. It contains the item called "Habits" on the website
  * @author MagicMicky
  *
  */
 public class Habit extends HabitItem{
 	private final static HabitType type = HabitType.habit;
 	private boolean up;
 	private boolean down;
 	/**
 	 * Create a new Habit based on all the information needed
 	 * @param id the id of the habit
 	 * @param notes the notes associated to a habit
 	 * @param priority the priority of the habit
 	 * @param text the text of the habit
 	 * @param value the value (points) of the habit
 	 * @param up whether or not the habit can be "upped"
 	 * @param down whether or not the habit can be "downed"
 	 */
 	public Habit(String id, String notes, String priority, String text, double value
 			, boolean up, boolean down) {
 		super(id, notes, priority, text, value);
 		this.setUp(up);
 		this.setDown(down);
 	}
 	public Habit() {
 		super();
 		this.setDown(true);
 		this.setUp(true);
 	}
 	/**
 	 * @return whether or not the habit can be "upped"
 	 */
 	public boolean isUp() {
 		return up;
 	}
 	/**
 	 * Set the Up value
 	 * @param up
 	 */
 	public void setUp(boolean up) {
 		this.up = up;
 	}
 	/**
 	 * @return whether or not the habit can be "down"
 	 */
 	public boolean isDown() {
 		return down;
 	}
 	/**
 	 * Set the Down value
 	 * @param down 
 	 */
 	public void setDown(boolean down) {
 		this.down = down;
 	}
 	public String getJSONString() {
 		StringBuilder json = new StringBuilder();
 		json.append("{\"type\":\"" + this.getType() + "\"," );
 		json.append("\"text\":\"" + this.getText() + "\"," );
 		json.append("\"value\":0," );
 		if(this.getNotes()!=null && !this.getNotes().contentEquals(""))
 			json.append("\"note\":\"" + this.getNotes() + "\"," );
 		json.append("\"up\":" + (this.isUp() ? "true":"false") + "," );
 		json.append("\"down\":" + (this.isDown() ? "true":"false") + "}" );
 		return json.toString();
 	}
 	@Override
 	protected String getType() {
 		return type.toString();
 	}
 
 }
