 package com.intro.compintro.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.test.AndroidTestCase;
 
 import com.intro.compintro.db.Column;
 import com.intro.compintro.db.DBAdapter;
 
 public class DBAdapterTest extends AndroidTestCase {
 
 	DBAdapter adapter;
 
 	@Override
 	protected void setUp() throws Exception {
 		adapter = new DBAdapter(getContext());
 		adapter.open();
 		super.setUp();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		adapter.close();
 		super.tearDown();
 	}
 
 	public void testInsertAndQuery() {
 		List<Column> inserts = new ArrayList<Column>();
 		for (int i = 0; i < 5; i++) {
 			Column column = new Column(i, "TEST" + i, "Rest" + i, "Github" + i);
 			inserts.add(column);
 			adapter.insert(column);
 		}
 		List<Column> querys = adapter.queryAll();
 		for (int i = 0; i < querys.size(); i++) {
 			assertEquals(inserts.get(i), querys.get(i));
 		}
 	}
 
 	public void testUpdate() {
 		Column column = new Column(12, "TEST", "Rest", "Github");
 		adapter.insert(column);
 		column.set_id(1);
 		column.setColumn_1(13);
 		column.setColumn_2("TSET");
 		column.setColumn_3("tseR");
 		column.setColumn_4("buhtiG");
 		adapter.update(column);
 		assertEquals(column, adapter.queryById(1));
 	}
 
 	public void testDelete() {
 		Column column = new Column(12, "TEST", "Rest", "Github");
 		adapter.insert(column);
 		adapter.delete(1);
		assertEquals(0, adapter.queryAll().size());
 	}
 
 	private void assertEquals(Column expected, Column actual) {
 		assertEquals(expected.getColumn_1(), actual.getColumn_1());
 		assertEquals(expected.getColumn_2(), actual.getColumn_2());
 		assertEquals(expected.getColumn_3(), actual.getColumn_3());
 		assertEquals(expected.getColumn_4(), actual.getColumn_4());
 	}
 
 }
