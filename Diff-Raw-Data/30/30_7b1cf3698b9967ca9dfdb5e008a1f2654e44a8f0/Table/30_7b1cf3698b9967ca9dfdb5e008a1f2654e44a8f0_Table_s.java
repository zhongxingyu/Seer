 package model;
 
 import java.util.HashMap;
 import java.lang.Integer;
public class Table<Integer, Column> extends HashMap<Integer, Column> {
 	
	private int index;
 	
 	public Table()
 	{
		index = 0;
 	}
 	
 	public Table(String[] columnNames)
 	{
		index = 0;
		Integer indexO = new Integer(index);
 	}
 }
