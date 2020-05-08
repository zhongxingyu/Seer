 package com.edinarobotics.scouting.definitions.database.events;
 
 import com.edinarobotics.scouting.definitions.database.changes.ColumnInsertionChange;
 
 /**
  * This event is fired by {@link com.edinarobotics.scouting.definitions.database.Database Database}
  * implementations whenever a table is modified by a {@link ColumnInsertionChange} object in a
  * {@link com.edinarobotics.scouting.definitions.database.changes.Transaction Transaction}.
  */
 public class ColumnInsertionChangeEvent extends DatabaseChangeEvent{
 	
 	/**
 	 * Constructs a ColumnInsertionChangeEvent representing the given
 	 * {@link ColumnInsertionChange} object.
 	 * @param change The ColumnInsertionChange to be stored in this
 	 * ColumnInsertionChangeEvent.
 	 */
 	public ColumnInsertionChangeEvent(ColumnInsertionChange change){
 		super(change);
 	}
 	
 	/**
 	 * Constructs a ColumnInsertionChangeEvent representing the given {@link ColumnInsertionChange} object and
 	 * having the given String ID value.
 	 * @param change The ColumnInsertionChange to be stored in this ColumnInsertionChangeEvent.
 	 * @param id The String ID value to be assigned to this event.
	 * @see com.edinarobotics.scouting.definitions.event.CancellableEvent#CancellableEvent(String)
 	 */
 	public ColumnInsertionChangeEvent(ColumnInsertionChange change, String id){
 		super(change, id);
 	}
 	
 	/**
 	 * Allows access to the {@link ColumnInsertionChange} object stored in this ColumnInsertionChangeEvent.
 	 * @return The ColumnInsertionChange object stored in this ColumnInsertionChangeEvent.
 	 */
 	public ColumnInsertionChange getChange(){
 		return (ColumnInsertionChange)super.getChange();
 	}
 }
