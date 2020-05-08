 package model;
 
 import java.util.HashMap;
 import java.lang.Integer;
public class Table<indexO, columns> extends HashMap<Integer, Column> {
 	
	private Integer index;
 	
 	public Table()
 	{
		this.index = new Integer(0);
 	}
 	
 	public Table(String[] columnNames)
 	{
		this.index = new Integer(0);
		this.create(columnNames);
	}
	
	private void create(String[] columnNames)
	{
		for (String s : columnNames)
		{
			System.out.println("Création d'une colonne avec un label : "+s);
			Column buffCol = new Column(s);
			this.put(index, buffCol);
		}
 	}
 }
