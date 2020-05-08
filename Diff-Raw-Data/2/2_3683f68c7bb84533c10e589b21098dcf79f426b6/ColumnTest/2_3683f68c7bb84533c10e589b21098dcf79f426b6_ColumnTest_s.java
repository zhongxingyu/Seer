 package com.alexrnl.commons.database;
 
 import static org.hamcrest.core.IsNot.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link Column} class.
  * @author Alex
  */
 public class ColumnTest {
 	/** A column representing an id column */
 	private Column			idColumn;
 	/** A column which is not and id column */
 	private Column			otherColumn;
 	/** The list with the columns */
 	private List<Column>	columns;
 	
 	/**
 	 * Build the column required for the tests.
 	 */
 	@Before
 	public void setUp () {
 		idColumn = new Column(Integer.class, "id", true);
 		otherColumn = new Column(String.class, "name");
 		
 		columns = new LinkedList<>();
 		columns.add(idColumn);
 		columns.add(otherColumn);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.Column#getType()}.
 	 */
 	@Test
 	public void testGetType () {
 		assertEquals(Integer.class, idColumn.getType());
 		assertEquals(String.class, otherColumn.getType());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.Column#getName()}.
 	 */
 	@Test
 	public void testGetName () {
 		assertEquals("id", idColumn.getName());
 		assertEquals("name", otherColumn.getName());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.Column#isID()}.
 	 */
 	@Test
 	public void testIsID () {
 		assertEquals(true, idColumn.isID());
 		assertEquals(false, otherColumn.isID());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.Column#toString()}.
 	 */
 	@Test
 	public void testToString () {
 		assertEquals("name: id, type: Integer, is id: true", idColumn.toString());
 		assertEquals("name: name, type: String, is id: false", otherColumn.toString());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.Column#hashCode()}.
 	 */
 	@Test
 	public void testHashCode () {
 		for (final Column column : columns) {
 			final Column newIdColumn = new Column(column.getType(), column.getName(), column.isID());
 			assertNotSame(column, newIdColumn);
 			assertEquals(column.hashCode(), newIdColumn.hashCode());
 			
 			// Check that hash code changes on any field update
 			assertThat(column.hashCode(), not(new Column(column.getType(), column.getName(), !column.isID()).hashCode()));
 			assertThat(column.hashCode(), not(new Column(null, column.getName(), !column.isID()).hashCode()));
 			assertThat(column.hashCode(), not(new Column(Number.class, column.getName(), !column.isID()).hashCode()));
 			assertThat(column.hashCode(), not(new Column(column.getType(), null, !column.isID()).hashCode()));
 			assertThat(column.hashCode(), not(new Column(column.getType(), column.getName() + ".//", !column.isID()).hashCode()));
 		}
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.Column#hashCode()}.
 	 */
 	@Test
 	public void testEquals () {
 		for (final Column column : columns) {
 			final Column newIdColumn = new Column(column.getType(), column.getName(), column.isID());
 			assertNotSame(column, newIdColumn);
			assertEquals(column.hashCode(), newIdColumn.hashCode());
 			
 			// Basic cases
 			assertFalse(column.equals(null));
 			assertTrue(column.equals(column));
 			assertFalse(column.equals(new Object()));
 			assertEquals(column, column);
 			
 			// Check equals on a field change
 			assertNotEquals(column, new Column(null, column.getName(), !column.isID()));
 			assertNotEquals(column, new Column(Number.class, column.getName(), !column.isID()));
 			assertNotEquals(column, new Column(column.getType(), null, !column.isID()));
 			assertNotEquals(column, new Column(column.getType(), column.getName() + ".//", !column.isID()));
 		}
 	}
 	
 }
