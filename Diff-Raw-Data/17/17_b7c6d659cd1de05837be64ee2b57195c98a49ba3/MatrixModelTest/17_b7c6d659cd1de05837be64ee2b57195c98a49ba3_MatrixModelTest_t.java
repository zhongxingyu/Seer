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
		RowIndexConverter mockConverter = createMock(RowIndexConverter.class);
		model = new MatrixModel(new Field(9), mockConverter, 9);
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
		RowIndexConverter mockConverter = createMock(RowIndexConverter.class);
 		f.setValue(3);
		model = new MatrixModel(f, mockConverter, 1);
 		assertEquals(3, model.getValueAt(0, 0));
 	}
 
 	@Test
 	public void testFireTableCellUpdated() {
 		Field f = new Field(9);
		RowIndexConverter mockConverter = createMock(RowIndexConverter.class);
		
		expect(mockConverter.getRow(anyInt())).andStubReturn(0);
		expect(mockConverter.getCol(anyInt())).andStubReturn(0);
		model = new MatrixModel(f, mockConverter, 9);
 		
 		TableModelListener mockListener = createMock(TableModelListener.class);
 		mockListener.tableChanged(anyObject(TableModelEvent.class));
 		model.addTableModelListener(mockListener);
 		replay(mockListener);
		replay(mockConverter);
 		f.setValue(2);
 		verify(mockListener);
 	}
 
 }
