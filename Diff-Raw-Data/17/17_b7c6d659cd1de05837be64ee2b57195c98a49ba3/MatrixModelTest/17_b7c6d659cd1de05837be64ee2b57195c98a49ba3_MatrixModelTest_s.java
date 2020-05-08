 package org.vojtajina.sudoku;
 
 import static org.junit.Assert.*;
 import static org.easymock.EasyMock.*;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class MatrixModelTest {
 
 	private MatrixModel model;
 	
 	@Before
 	public void setUp() throws Exception {
		model = new MatrixModel(new Field(9), 9);
 	}
 
 	@Test
 	public void testGetColumnCount() {
 		assertEquals(9L, model.getColumnCount());
 	}
 
 	@Test
 	public void testGetRowCount() {
 		assertEquals(9L, model.getRowCount());
 	}
 
 	@Test
 	public void testGetValueAt() {
 		assertNull(model.getValueAt(0, 0));
 		IField f = new Field(9);
 		f.setValue(3);
		model = new MatrixModel(f, 1);
 		assertEquals(3, model.getValueAt(0, 0));
 	}
 
 	@Test
 	public void testFireTableCellUpdated() {
 		Field f = new Field(9);
		model = new MatrixModel(f, 9);
 		
 		TableModelListener mockListener = createMock(TableModelListener.class);
 		mockListener.tableChanged(anyObject(TableModelEvent.class));
 		model.addTableModelListener(mockListener);
 		replay(mockListener);
 		f.setValue(2);
 		verify(mockListener);
 	}
 
 }
