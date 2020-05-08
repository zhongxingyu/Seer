 package com.edinarobotics.scouting.definitions.database.events;
 
 import com.edinarobotics.scouting.definitions.database.changes.ColumnDeletionChange;
 
 /**
  * This event is fired by {@link com.edinarobotics.scouting.definitions.database.Database Database}
  * implementations whenever a table is modified by a {@link ColumnDeletionChange} object in a
  * {@link com.edinarobotics.scouting.definitions.database.changes.Transaction Transaction}.
  */
 public class ColumnDeletionChangeEvent extends DatabaseChangeEvent{
 
 	/**
 	 * Constructs a ColumnDeletionChangeEvent representing the given
 	 * {@link ColumnDeletionChange} object.
 	 * @param change The ColumnDeletionChange to be stored in this
 	 * ColumnDeletionChangeEvent.
 	 */
 	public ColumnDeletionChangeEvent(ColumnDeletionChange change) {
 		super(change);
 	}
 	
 	/**
 	 * Constructs a ColumnDeletionChangeEvent representing the given {@link ColumnDeletionChange} object and
 	 * having the given String ID value.
 	 * @param change The ColumnDeletionChange to be stored in this ColumnDeletionChangeEvent.
 	 * @param id The String ID value to be assigned to this event.
	 * @see CancellableEvent#CancellableEvent(String)
 	 */
 	public ColumnDeletionChangeEvent(ColumnDeletionChange change, String id){
 		super(change, id);
 	}
 	
 	/**
 	 * Allows access to the {@link ColumnDeletionChange} object stored in the ColumnDeletionChangeEvent.
 	 * @return The ColumnDeletionChange object stored in this ColumnDeletionChangeEvent.
 	 */
 	public ColumnDeletionChange getChange(){
 		return (ColumnDeletionChange)super.getChange();
 	}
 }
