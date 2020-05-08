 package mazeEscapeApp;
 
 import java.util.Scanner;
 
 public class GCellCoordinate {
 	private int row;
 	private int col;
 	private GCellArea area;
 	
 	public GCellCoordinate(int r, int c)
 	{
 		row=r;
 		col=c;
 		area=null;
 	}
 	
 	public GCellCoordinate(int r, int c, GCellArea a)
 	{
 		row=r;
 		col=c;
 		area=a;
 	}
 		
 	
 	public GCellCoordinate(String s)
 	{
 		Scanner scan = new Scanner(s);
 		row=scan.nextInt();
 		col=scan.nextInt();
 		area=null;
 		scan.close();
 	}
 	
 	public GCellCoordinate(GCellArea cell)
 	{
 		row=cell.getRow();
 		col=cell.getColumn();
 		area=cell;
 	}
 
 	public int getRow() {
 		return row;
 	}
 
 	public void setRow(int row) {
 		this.row = row;
 	}
 
 	public int getCol() {
 		return col;
 	}
 
 	public void setCol(int col) {
 		this.col = col;
 	}
 	
 	public GCellArea getGCellArea()
 	{
 		if(area!=null)
 			return area;
 		else
 			return null;
 	}
 	
 	public String toString()
 	{
 		return row + " " + col;
 	}
 	
 	public boolean isGCellAreaValid()
 	{
 		return (area!=null);
 	}
 	
 	
 
 }
