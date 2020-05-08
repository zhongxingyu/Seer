 package com.alexrnl.commons.database;
 
 import java.util.EnumMap;
 import java.util.Map;
 
 /**
  * A fake class for entities (no ID column defined).
  * @author Alex
  */
 public class Fake extends Entity {
 	/** Serial version UID */
 	private static final long			serialVersionUID	= 3298514054651237074L;
 	
 	/** Map with the columns */
 	private EnumMap<FakeColumn, Column>	map;
 	/** The id of the fake class */
 	private int							id;
 	
 	@Override
 	public final String getEntityName () {
 		return "FakeEntity";
 	}
 	
 	/** Dummy columns enumeration */
 	public enum FakeColumn implements EntityColumn {
 		/** The id column */
 		ID ("Id");
 		
 		/** The name of the attribute */
 		private final String	attributeName;
 		
 		/**
 		 * Constructor #1.<br />
 		 * Unique constructor.
 		 * @param attributeName
 		 *        the name of the attribute.
 		 */
 		private FakeColumn (final String attributeName) {
 			this.attributeName = attributeName;
 		}
 		
 		@Override
 		public String getFieldName () {
 			return attributeName;
 		}
 		
 	}
 	
 	@Override
 	protected void setEntityColumns () {
 		map = new EnumMap<>(FakeColumn.class);
 		map.put(FakeColumn.ID, new Column(Integer.class, "id"));
 	}
 	
 	@Override
 	public Map<? extends Enum<? extends EntityColumn>, Column> getEntityColumns () {
 		return map;
 	}
 	
 	/**
 	 * Constructor #1.<br />
 	 */
 	public Fake () {
 		super();
 	}
 
 	@Override
 	public String getID () {
 		return Integer.toString(id);
 	}

	@Override
	public Fake clone () throws CloneNotSupportedException {
		final Fake clone = new Fake();
		clone.id = id;
		return clone;
	}
 	
 }
