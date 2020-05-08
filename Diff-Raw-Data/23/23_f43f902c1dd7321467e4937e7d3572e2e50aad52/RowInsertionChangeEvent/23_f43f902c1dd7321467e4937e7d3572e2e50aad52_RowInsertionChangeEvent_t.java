 package com.edinarobotics.scouting.definitions.database.events;
 
 import com.edinarobotics.scouting.definitions.database.changes.RowInsertionChange;
 
 /**
  * This event is fired by {@link com.edinarobotics.scouting.definitions.database.Database Database}
  * implementations whenever a table is modified by a {@link RowInsertionChange} object in a
  * {@link com.edinarobotics.scouting.definitions.database.changes.Transaction Transaction}.
  */
 public class RowInsertionChangeEvent extends DatabaseChangeEvent{
 	
 	/**
 	 * Constructs a RowInsertionChangeEvent representing the given
 	 * {@link RowInsertionChange} object.
 	 * @param change The RowInsertionChange to be stored in this
 	 * RowInsertionChangeEvent.
 	 */
 	public RowInsertionChangeEvent(RowInsertionChange change){
 		super(change);
 	}
 	
 	/**
 	 * Constructs a RowInsertionChangeEvent representing the given {@link RowInsertionChange} object and
 	 * having the given String ID value.
 	 * @param change The RowInsertionChange to be stored in this RowInsertionChangeEvent.
 	 * @param id The String ID value to be assigned to this event.
	 * @see com.edinarobotics.scouting.definitions.event.CancellableEvent#CancellableEvent(String)
 	 */
 	public RowInsertionChangeEvent(RowInsertionChange change, String id){
 		super(change, id);
 	}
 	
 	/**
 	 * Allows access to the {@link RowInsertionChange} object stored in this RowInsertionChangeEvent.
 	 * @return The RowInsertionChange object stored in this RowInsertionChangeEvent.
 	 */
 	public RowInsertionChange getChange(){
 		return (RowInsertionChange)super.getChange();
 	}
 }
