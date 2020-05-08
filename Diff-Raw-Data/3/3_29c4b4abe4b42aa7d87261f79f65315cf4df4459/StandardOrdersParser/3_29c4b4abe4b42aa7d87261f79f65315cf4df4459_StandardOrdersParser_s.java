 package net.craigrm.dip.scanners;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.Set;
 
 import net.craigrm.dip.gameturn.IOrdersDataSource;
 import net.craigrm.dip.map.ProvinceIdentifier;
 import net.craigrm.dip.map.properties.Powers;
 import net.craigrm.dip.map.properties.PowersFormatException;
 import net.craigrm.dip.orders.Adjustment;
 import net.craigrm.dip.orders.AdjustmentFormatException;
 import net.craigrm.dip.orders.AdjustmentStateException;
 import net.craigrm.dip.orders.Order;
 import net.craigrm.dip.orders.OrdersDefinitionException;
 import net.craigrm.dip.orders.SupportingOrder;
 import net.craigrm.dip.orders.properties.AdjustmentType;
 import net.craigrm.dip.orders.properties.OrderType;
 import net.craigrm.dip.state.TurnIdentifier;
 import net.craigrm.dip.state.TurnIdentifierFormatException;
 import net.craigrm.dip.state.Unit;
 import net.craigrm.dip.state.properties.SeasonFormatException;
 import net.craigrm.dip.state.properties.UnitType;
 import net.craigrm.dip.state.properties.YearFormatException;
 
 public class StandardOrdersParser implements IOrdersDataSource {
 	
 	private static final String COMMENT_PREFIX = "#";
 	private static final String TURN_PREFIX = "=";
 	private static final String POWER_PREFIX = "-";
 	private static final String WHITESPACE_REGEX =  "\\s*";
 	private static final String ADJUSTMENT_REGEX =  "(BUILD|DISBAND)S?:";
 
 	private File ordersFile;
 	private TurnIdentifier turnID;
 	private Set<Order> orders = new LinkedHashSet<Order>(); // Use Linked to preserve order
 	private Set<Adjustment> adjustments = new LinkedHashSet<Adjustment>(); // Use Linked to preserve order
 	
 	public StandardOrdersParser(File ordersFile) {
 		this.ordersFile = ordersFile;
 		checkFile();
 		parseOrders();
 	}
 
 	public StandardOrdersParser(String ordersFileName) {
 		if (ordersFileName == null)	{
 			throw new IllegalArgumentException("Orders file name not specified.");
 		}
 		this.ordersFile = new File(ordersFileName);
 		checkFile();
 		parseOrders();
 	}
 	
 	public TurnIdentifier getTurnID() {
 		return turnID;
 	}
 
 	public Set<Order> getOrders() {
 		return orders;
 	}
 
 	public Set<Adjustment> getAdjustments() {
 		return adjustments;
 	}
 
 	private void parseOrders() {
 		FileReader fr = null;
 		int lineNo = 0;
 		String line = null;		
 		try {
 			fr = new FileReader(ordersFile);
 		}
 		catch (FileNotFoundException fnfe) {
 			throw new IllegalArgumentException("Position file " + ordersFile.getAbsolutePath() + " cannot be found.");
 		}
 
 		BufferedReader br = new BufferedReader(fr);
 
 		try {
 			Powers currentPower = null;
 			
 			while ((line = br.readLine()) != null) {
 				lineNo ++;
 				//Skip comment line.
 				if (line.startsWith(COMMENT_PREFIX)) {
 					continue;
 				}
 				
 				//Skip blank line.
 				if (line.matches(WHITESPACE_REGEX)) {
 					continue;
 				}
 				
 				//Handle turn designation line.
 				if (line.startsWith(TURN_PREFIX)) {
 					turnID = getTurnID(turnID, lineNo, line);
 					continue;
 				}
 				
 				//Handle power designation line.
 				if (line.startsWith(POWER_PREFIX)) {
 					currentPower = getCurrentPower(lineNo, line, currentPower);
 					continue;
 				}
 				
 				//Handle adjustment line.
 				if (line.toUpperCase().matches(ADJUSTMENT_REGEX)) {
 					try {
 						adjustments.addAll(getAdjustment(lineNo, line, currentPower));
 					}
 					catch (AdjustmentStateException ase) {
 						//AdjustmentStateException indicates that we are not in an adjustment turn.
 						//Ignore the invalid adjustment line and keep going.
 					}
 					catch (AdjustmentFormatException afe) {
 						//Adjustment line is not in correct format. Too bad.
 						//Ignore the invalid adjustment line and keep going.
 					}
 					continue;
 				}
 				
 				//Assume it is an order line. Handle order line.
 				orders.add(getOrder(lineNo, line, currentPower));
 			}
 			
 			// Finished. We must have a turn ID.
 			if (turnID == null) {
 				throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), 0, null, "a turn ID in the orders defintion file");
 			}
 
 			// Don't care if there are no orders: all units will hold by default.
 			// Don't care if there are no adjustments: no units are built by default, disbands will be assigned by the system if omitted.  
 		}
 		catch (IOException ioe) {
 			throw new IllegalArgumentException("Orders file " + ordersFile.getAbsolutePath() + " cannot be read.", ioe);
 		}
 		finally{
 			if (br != null) {
 				try {
 					br.close();
 				}
 				catch(IOException ioe) {
 					//TODO Log exception. Continue anyway: we read the file successfully.
 				}
 			}
 		}
 
 	}
 
 	private void checkFile() {
 		if (ordersFile == null) {
 			throw new IllegalArgumentException("Map file not specified.");
 		}
 		
 		if (!ordersFile.isFile()) {
 			try {
 				String mapFileName = ordersFile.getCanonicalPath();
 				throw new IllegalArgumentException("Map file " + mapFileName + " cannot be accessed.");
 			}
 			catch (IOException ioe) {
 				String mapFileName = ordersFile.getAbsolutePath();
 				throw new IllegalArgumentException ("Map file " + mapFileName + " cannot be accessed.");
 			}
 			
 		}
 	}
 
 	private TurnIdentifier getTurnID(TurnIdentifier turnID, int lineNo, String line) {
 		if (turnID != null) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a single Turn designation");
 		}
 		try {
 			return new TurnIdentifier(line.substring(TURN_PREFIX.length()));
 		}
 		catch (TurnIdentifierFormatException tife) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a correctly formatted turn ID", tife);
 		}
 		catch (YearFormatException yfe) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a valid turn year", yfe );
 		}
 		catch (SeasonFormatException sfe) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a valid turn season", sfe );
 		}
 	}
 	
 	private Powers getCurrentPower(int lineNo, String line, Powers currentPower) {
 		//Check we've got a turn designation.
 		if (turnID == null) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a Turn designation before this point");
 		}
 		try {
 			currentPower = Powers.getPowerFromName(line.substring(POWER_PREFIX.length()));
 		}
 		catch (PowersFormatException pfe) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a correctly formatted Power name", pfe);
 		}
 		return currentPower;
 	}
 
 	private Set<Adjustment> getAdjustment(int lineNo, String line, Powers currentPower) {
 		//Check we've got a turn designation.
 		if (turnID == null) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a Turn designation before this point");
 		}
 		
 		//Check that this is an adjustment turn.
 		if (!turnID.isAdjustmentTurn()) {
 			throw new AdjustmentStateException(ordersFile.getAbsolutePath(), lineNo, line, "adjustments only on an adjustment turn");
 		}
 
 		//Check we've got a Power.
 		if (currentPower == null) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a Power designation before this point");
 		}
 		
 		//Check format of adjustment line. 
 		// Expected format: 
 		// Build(s): A VEN[, F TRI]... 
 		// Disband(s): A VEN[, F TRI]...
 		if (line.indexOf(":") == -1) {
 			throw new AdjustmentFormatException(ordersFile.getAbsolutePath(), lineNo, line, "adjustment line with a colon");
 		}
 		
 		if (line.indexOf(":") != line.lastIndexOf(":")) {
 			throw new AdjustmentFormatException(ordersFile.getAbsolutePath(), lineNo, line, "adjustment line with a single colon");
 		}
 		
 		//Handle adjustment line.
 		AdjustmentType adjustmentType = AdjustmentType.getAdjustmentType(line.split(":")[0]);
 		String adjustmentList = line.split(":")[1];
 
 		Set<Adjustment> adjustments = Collections.emptySet();
 		Scanner adjustmentListScanner = new Scanner(adjustmentList);
 		adjustmentListScanner.useDelimiter(",");
 
 		String adjustmentText = null;
 		ProvinceIdentifier id = null;
 		Unit unit = null;
 		Adjustment adjustment = null;
 		while ((adjustmentText = adjustmentListScanner.next()) != null) {
 			try {
 				Scanner adjustmentScanner = new Scanner(adjustmentText.trim());
 				adjustmentListScanner.useDelimiter(" ");
 				UnitType unitType = UnitType.getType(adjustmentScanner.next());
 				id = new ProvinceIdentifier(adjustmentScanner.next());
 				unit = new Unit(id, currentPower, unitType);
 				adjustment = new Adjustment(adjustmentText, Adjustment.WELL_FORMED, adjustmentType, unit);
 				adjustmentScanner.close();
 			}
 			catch (IllegalArgumentException iae) {
 				//IllegalArgumentException indicates that the adjustment is not well-formed.
 				//Create the adjustment anyway so that it can be reported on later, but mark it as bad.
 				//Continue getting the next adjustment.
 				adjustment = new Adjustment(adjustmentText, Adjustment.NOT_WELL_FORMED, adjustmentType, unit);
 			}
 			catch (NoSuchElementException nsee) {
 				//NoSuchElementException indicates that the adjustment is not well-formed.
 				//Create the adjustment anyway so that it can be reported on later, but mark it as bad.
 				//Continue getting the next adjustment.
 				adjustment = new Adjustment(adjustmentText, Adjustment.NOT_WELL_FORMED, adjustmentType, unit);
 			}
 			finally {
 				//Save the adjustment, whether good or bad.
 				adjustments.add(adjustment);
 				adjustmentListScanner.close();
 			}
 		}
 		return adjustments;
 	}
 
 	private Order getOrder(int lineNo, String line, Powers currentPower) {
 		//Check we've got a turn designation.
 		if (turnID == null) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a Turn designation before this point");
 		}
 		
 		//Check we've got a Power.
 		if (currentPower == null) {
 			throw new OrdersDefinitionException(ordersFile.getAbsolutePath(), lineNo, line, "a Power designation before this point");
 		}
 		
 		//Handle order line
 		Scanner lineScanner = new Scanner(line);
 		lineScanner.useDelimiter(" ");
 		Order order = null;
 		Unit unit = null;
 		UnitType unitType = null;
 		ProvinceIdentifier id = null;
 		OrderType orderType = null;
 		ProvinceIdentifier destination = null;
 		UnitType supportedUnitType = null;
 		ProvinceIdentifier supportedUnitPosition = null;
 		OrderType supportedOrderType = null;
 		Unit supportedUnit = null;
 		
 		try {
 			// FIXME: This is bugged!!
 			// UnitType.getType and ProvinceIdentifier can both throw IllegalArgumentExceptions which results
 			// in a new SupportingOrder being created when caught. But we don't know at this point if we need
 			// an Order or a SupportingOrder.
 			
 			unitType = UnitType.getType(lineScanner.next());
 			id = new ProvinceIdentifier(lineScanner.next());
 			unit = new Unit(id, currentPower, unitType);
 		
 			orderType = OrderType.getOrderType(lineScanner.next());
 		
 			if (orderType == OrderType.HOLD) {
 				order = new Order(line, Order.WELL_FORMED, unit, orderType, destination);
 				return order;
 			}
 
 			if (orderType == OrderType.MOVE) {
 				destination = new ProvinceIdentifier(lineScanner.next());
 				order = new Order(line, Order.WELL_FORMED, unit, orderType, destination);
 				return order;
 			}
 
 			// orderType is SUPPORT or CONVOY which are supporting orders.
 			supportedUnitType = UnitType.getType(lineScanner.next());
 			supportedUnitPosition = new ProvinceIdentifier(lineScanner.next());
 			supportedUnit = new Unit(supportedUnitPosition, supportedUnitType);
 			
 			supportedOrderType = OrderType.getOrderType(lineScanner.next());
 
 			destination = new ProvinceIdentifier(lineScanner.next());
 
 			order = new SupportingOrder(line, Order.WELL_FORMED, unit, orderType, supportedUnit, supportedOrderType, destination);
 			return order;
 		}
 		catch (IllegalArgumentException iae) {
 			//IllegalArgumentException indicates that the order is not well-formed.
 			//Save the order anyway so that it can be reported on later, but mark it as bad.
 			//Continue getting the next order.
 			order = new SupportingOrder(line, Order.NOT_WELL_FORMED, unit, orderType, supportedUnit, supportedOrderType, destination);
 			return order;
 		}
 		catch (NoSuchElementException nsee) {
 			//NoSuchElementException indicates that the order is not well-formed.
 			//Save the order anyway so that it can be reported on later, but mark it as bad.
 			//Continue getting the next order.
 			order = new SupportingOrder(line, Order.NOT_WELL_FORMED, unit, orderType, supportedUnit, supportedOrderType, destination);
 			return order;
 		}
 		finally {
 			lineScanner.close();
 		}
 
 	}
}
