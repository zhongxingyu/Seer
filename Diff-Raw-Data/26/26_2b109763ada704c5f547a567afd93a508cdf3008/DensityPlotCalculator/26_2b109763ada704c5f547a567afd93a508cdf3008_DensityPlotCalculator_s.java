 package dk.dma.aiscoverage.calculator;
 
 import java.util.Collection;
 import dk.dma.aiscoverage.data.BaseStation;
 import dk.dma.aiscoverage.data.Cell;
 import dk.dma.aiscoverage.data.CustomMessage;
 import dk.dma.aiscoverage.data.Ship;
 import dk.dma.aiscoverage.data.BaseStation.ReceiverType;
 import dk.dma.aiscoverage.project.AisCoverageProject;
 
 public class DensityPlotCalculator extends AbstractCalculator {
 
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private BaseStation basestation;
 
 
 	public DensityPlotCalculator(AisCoverageProject project){
 		super(project);
 	}
 	
 	
 	public void calculate(CustomMessage message) {
 		CustomMessage lastMessage = message.getShip().getLastMessage();
 		if(lastMessage == null){
 			message.getShip().setLastMessage(message);
 			return;
 		}
 		
 		//Test 
 //		double x0 = this.projection.lon2x(11, 55);
 //		double y0 = this.projection.lat2y(11, 55);
 //		double x1 = this.projection.lon2x(12, 55);
 //		double y1 = this.projection.lat2y(12, 55);
 //		
 		double x0 = this.projection.lon2x(lastMessage.getLongitude(), lastMessage.getLatitude());
 		double y0 = this.projection.lat2y(lastMessage.getLongitude(), lastMessage.getLatitude());
 		double x1 = this.projection.lon2x(message.getLongitude(), message.getLatitude());
 		double y1 = this.projection.lat2y(message.getLongitude(), message.getLatitude());
 		Ship ship = message.getShip();
 		
 		if(!filterMessage(message)){
 			calculateLine(x0, y0, x1, y1, ship);
 		}
 		
 		message.getShip().setLastMessage(message);
 
 		
 		
 	}
 	
 	@Override
 	public boolean filterMessage(CustomMessage message){
 		boolean filterMessage = false;
 		CustomMessage lastMessage = message.getShip().getLastMessage();
 		double distance = projection.distBetweenPoints(message.getLongitude(), message.getLatitude(), lastMessage.getLongitude(), lastMessage.getLatitude());
 		if(distance > 2000)
 			filterMessage = true;
 		
 		return filterMessage;
 	}
 	
 	private void addShipToCell(Cell c, Ship s){
 
 		c.getShips().put(s.getMmsi(), s);
 		basestation.incrementMessageCount();
 		
 		// If ship is not in same cell anymore, we increment shipcount for  cell
 		if(c.getShipCount() == 0)
 			c.incrementShipCount();
 		else if(s.getLastCell() == null)
 			c.incrementShipCount();
 		else if(s.getLastCell() != c)
 			c.incrementShipCount();
 		
 		s.setLastCell(c);
 
 	}
 	/*
 	 * Bresenham's line algorithm
 	 */
 	public void calculateLine(double x0, double y0, double x1, double y1, Ship ship){
 		boolean steep = false;
 		if(Math.abs(y1 - y0) > Math.abs(x1 - x0))
 			steep = true;
 			
 		if(steep){
 			//swap x0, y0
 			double newX0 = y0;
 			double newY0 = x0;
 			x0 = newX0;
 			y0 = newY0;
 			//swap x1, y1
 			double newX1 = y1;
 			double newY1 = x1;
 			x1 = newX1;
 			y1 = newY1;
 		}
 		if(x0 > x1){
 			//swap x0, x1
 			double newX0 = x1;
 			double newX1 = x0;
 			x0 = newX0;
 			x1 = newX1;
 			//swap y0, y1
 			double newY0 = y1;
 			double newY1 = y0;
 			y0 = newY0;
 			y1 = newY1;
 		}		     
 		double deltax = x1 - x0;		     
 		double deltay = Math.abs(y1 - y0);		     
 		double error = 0;		     
 		double deltaerr = deltay / deltax;		     
 		double ystep;   
 		double y = y0;
 		if(y0 < y1)
 			ystep = getCellSize();
 		else
 			ystep = -1*getCellSize();
			 
 		for (double x = x0; x < x1; x += getCellSize()) {
 			double lon;
 			double lat;
 			if(steep){
 				lon = projection.x2Lon(y, x);
 				lat = projection.y2Lat(y, x);
 			}else{
 				lon = projection.x2Lon(x, y);
 				lat = projection.y2Lat(x, y);
 			}
 			Cell cell = basestation.getCell(lat, lon);
 			if(cell == null){
 				cell = basestation.createCell(lat, lon);
 			}
 			addShipToCell(cell, ship);
 			error +=deltaerr;
 			if(error >= getCellSize()/2){
 				y += ystep;
 				error = error - getCellSize();
 			}
 				
 		}
 	}
 
 	
 	/*
 	 *
 	 * Number of ship in each cell can be used to draw density plot
 	 */
 	public Collection<Cell> getDensityPlotCoverage() {
 		if(basestation == null) return null;
 		return basestation.getGrid().values();
 	}
 	
 	
 	/*
 	 * In this calculator all messages belong to same grid
 	 */
 	@Override
 	protected BaseStation extractBaseStation(String baseId, ReceiverType receiverType){
 		if(basestation == null)
 			basestation = getBaseStationHandler().createGrid("density");
 		
 		return basestation;
 	}
 
 }
