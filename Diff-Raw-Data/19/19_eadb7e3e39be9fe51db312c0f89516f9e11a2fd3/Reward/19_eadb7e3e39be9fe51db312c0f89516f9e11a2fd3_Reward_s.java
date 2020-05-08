 package com.magicmicky.habitrpgmobileapp.habits;
 
import java.util.List;
 
 /**
  * A reward. Contain a reward that you can see on the website
  * @author MagicMicky
  *
  */
 public class Reward extends HabitItem{
 	private final static HabitType type = HabitType.reward;
 	/**
 	 * Create a new Reward
 	 * @param id the id of the habit
 	 * @param notes the notes associated to a habit
 	 * @param priority the priority of the habit
 	 * @param text the text of the habit
 	 * @param value the value (points) of the habit
 	 */
 	public Reward(String id, String notes, String priority, String text,
 			double value) {
 		super(id, notes, priority, text, value);
 	}
 
 	public Reward() {
 		super();
 	}
 
 	@Override
 	protected String getType() {
 		return type.toString();
 	}
 
 	@Override
 	public String getJSONString() {
 			StringBuilder json = new StringBuilder();
 			json.append("{\"type\":\"" + this.getType() + "\"," );
 			json.append("\"text\":\"" + this.getText() + "\"," );
 			if(this.getNotes()!=null && !this.getNotes().contentEquals(""))
 				json.append("\"notes\":\"" + this.getNotes() + "\"," );
 			json.append("\"value\":" + this.getValue() + "}" );
 			return json.toString();
 	}
 
 }
