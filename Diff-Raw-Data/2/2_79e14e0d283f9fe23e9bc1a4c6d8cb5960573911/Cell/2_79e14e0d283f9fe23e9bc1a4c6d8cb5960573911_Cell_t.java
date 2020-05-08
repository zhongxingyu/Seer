 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class Cell {
 	
 	private double pressure;
 	private double[] acceleration;
 	private int idNr;
 	
 	private double nextPressure;
 	
 	private double previousY = 0;
 	private double nextY = 0;
 	private double currentY = 0;
 	
 	private CellType cellType;
 	
 	ArrayList<Cell> neighbors;
 	
 	CellDelegate delegate;
 	
 	int neighborCounter;
 	
 	private double K = 0.04;
 	private double time = 0.0;
 	
 	
 	public Cell(CellDelegate delegate)
 	{
 		
 		this.delegate = delegate;
 		
 		pressure = 0;
 		acceleration = new double[2];
 		acceleration[0] = 0;
 		acceleration[1] = 0;
 		nextPressure = 0;
 		
 		K = 343.0 * 343.0 * delegate.getTimeH() * delegate.getTimeH() / (delegate.getSpaceH() * delegate.getSpaceH());
 		
 	}
 	
 	public void setCellType(CellType cellType)
 	{
 		this.cellType = cellType;
 	}
 	
 	public void setIdNr(int idNr) 
 	{
 		this.idNr = idNr;
 	}
 	
 	public void setupNeighbors()
 	{
 		this.neighbors = delegate.getNeighbors(idNr);
 	}
 	
 	public void changePressure(double d)
 	{
 		pressure = pressure + d;
 	}
 	
 	public double getPressure(double externalPrevious)
 	{
 		if(cellType == CellType.CellTypeBeyondEdgeCell)
 		{
 			return externalPrevious ;
 		}
 		
 		return pressure;
 	}
 	
 	public Cell getFirstNeighbor()
 	{
 		neighborCounter = 1;
 		return neighbors.get(0);
 		
 	}
 	
 	public Cell getNextNeighbor()
 	{
 		if(neighborCounter < neighbors.size()){
 			
 			neighborCounter++;
 			return neighbors.get(neighborCounter);
 			
 		}else
 		{
 			return null;
 		}
 		
 	}
 	
 	public void update(){
 		
 		previousY = pressure;
 		pressure = (nextPressure) - pressure;
 		
 	}
 	
 	public void prepareUpdate()
 	{
 		if (cellType == CellType.CellTypeSimulationCell) {
 			
 			nextPressure = 0;
 			double neightborValues = 0;
 			
 			int i = 0;
 			for (Cell neighbor : neighbors) {
 				
 				if ((i%2) == 0) {
 					//nextPressure += neighbor.getPressure()  ;
 				}else
 				{
 					neightborValues +=  neighbor.getPressure( previousY);
 				}
 				
 				i++;
 				
 
 			}
 			
 			nextPressure = (2 * pressure) - previousY + (K*( neightborValues - (4 * currentY)));
 		}else if(cellType == CellType.CellTypeSolidCell)
 		{
 			nextPressure = 0;
 		}else if(cellType == CellType.CellTypeGeneratorCell)
 		{
			nextPressure = 0.05;
 			time++;
 		}
 		
 	}
 
 	public double getPressure() {
 		return pressure;
 	}
 
 	public void setPressure(double pressure) {
 		this.pressure = pressure;
 	}
 
 	public CellType getCellType() {
 		return cellType;
 	}
 
 }
