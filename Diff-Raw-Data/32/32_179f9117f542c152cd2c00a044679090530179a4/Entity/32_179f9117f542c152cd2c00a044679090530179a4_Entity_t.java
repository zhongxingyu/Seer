 package com.alexrnl.commons.database;
 
 import java.io.Serializable;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Represent an abstract entity of a database.<br />
  * @author Alex
  */
public abstract class Entity implements Cloneable, Serializable {
 	/** Logger */
 	private static Logger		lg					= Logger.getLogger(Entity.class.getName());
 	
 	/** Serial Version UID */
 	private static final long	serialVersionUID	= 1337771320210441402L;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Unique constructor of the class. Ensure to call the {@link #setEntityColumns()} method.
 	 */
 	protected Entity () {
 		if (getEntityColumns() == null) {
 			if (lg.isLoggable(Level.FINE)) {
 				lg.fine("Calling the #setColumn() method.");
 			}
 			setEntityColumns();
 			int nbIdColumns = 0;
 			for (final Column column : getEntityColumns().values()) {
 				if (column.isID()) {
 					++nbIdColumns;
 				}
 			}
 			assert nbIdColumns == 1;
 		}
 		
 	}
 	
 	/**
 	 * Return the name of the represented entity.
 	 * @return the name of the represented entity.
 	 */
 	public abstract String getEntityName ();
 	
 	/**
 	 * Method that creates the columns (or fields) of the entity that is represent.
 	 */
 	protected abstract void setEntityColumns ();
 	
 	/**
 	 * Return the list of the columns of the table.
 	 * @return the list of the columns.
 	 */
 	public abstract Map<? extends Enum<? extends EntityColumn>, Column> getEntityColumns ();
 	
 	/**
 	 * Return the id field (as a {@link String}) of the entity.
 	 * @return the id of the object.
 	 */
 	public abstract String getID ();
 	
 	@Override
	public abstract Entity clone () throws CloneNotSupportedException;
 }
