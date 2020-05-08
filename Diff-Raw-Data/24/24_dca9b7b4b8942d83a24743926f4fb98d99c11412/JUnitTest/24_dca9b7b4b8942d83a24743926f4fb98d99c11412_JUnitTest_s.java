 package com.tictactoe.junit;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.tictactoe.table.Table;
 
 public class JUnitTest {
 
 	@Test
 	public void test() {
 		Table t=new Table(3,3);
		//t.updateTable(0,'O');
		//t.updateTable(1,'O');
		//t.updateTable(2,'X');
 		//t.updateTable(3,'X');
		//t.updateTable(4,'O');
		//t.updateTable(5,'X');
		//t.updateTable(6,'X');
		//t.updateTable(7,'X');
		//t.updateTable(8,'X');
 		System.out.println(t.getScore());
 		assertEquals("Done",-4000,t.getScore(),10000);
 		t.printTable();
 	}
 
 }
