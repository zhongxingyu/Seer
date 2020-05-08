  
 package net.sf.rcer.example.rfcgen.rr;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import java.text.MessageFormat;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.sap.conn.jco.JCoDestination;
 import com.sap.conn.jco.JCoException;
 import com.sap.conn.jco.JCoFunction;
 import com.sap.conn.jco.JCoRecord;
 import com.sap.conn.jco.JCoTable;
 
 /**
  * A class representing the SAP R/3 data structure BAPISFLIST.
  */
 public class FlightData {
 
 	private PropertyChangeSupport _pcs;
 
 	private String carrierID;
 	private String connectionID;
 	private Date flightDate;
 	private Date departureTime;
 	private String origin;
 	private String destination;
 	private int maximumSeats;
 	private int occupiedSeats;
 	
 	/**
 	 * Default constructor to create an instance with initial values.
 	 */
 	public FlightData() {
 		_pcs = new PropertyChangeSupport(this);
 	}
 
 	/**
 	 * Copy constructor to create an instance and copy the values from an existing record.
 	 * @param source the {@link JCoRecord} to copy the values from
 	 * @throws UnsupportedOperationException if any other structure is passed as a source record
 	 */
 	public FlightData(JCoRecord source) throws UnsupportedOperationException {
 		checkStructure(source);
 		_pcs = new PropertyChangeSupport(this);
 		this.carrierID = source.getString("CARRID");
 		this.connectionID = source.getString("CONNID");
 		this.flightDate = source.getDate("FLDATE");
 		this.departureTime = source.getDate("DEPTIME");
 		this.origin = source.getString("AIRPFROM");
 		this.destination = source.getString("AIRPTO");
 		this.maximumSeats = source.getInt("SEATSMAX");
 		this.occupiedSeats = source.getInt("SEATSOCC");
 	}
 
 	/**
 	 * Copy the values into a target record.
 	 * @param target the {@link JCoRecord} to copy the values to
 	 * @throws UnsupportedOperationException if any other structure is passed as a target record
 	 */
 	public void toStructure(JCoRecord target) throws UnsupportedOperationException {
 		checkStructure(target);
 		target.clear();
 		target.setValue("CARRID", this.carrierID);
 		target.setValue("CONNID", this.connectionID);
 		target.setValue("FLDATE", this.flightDate);
 		target.setValue("DEPTIME", this.departureTime);
 		target.setValue("AIRPFROM", this.origin);
 		target.setValue("AIRPTO", this.destination);
 		target.setValue("SEATSMAX", this.maximumSeats);
 		target.setValue("SEATSOCC", this.occupiedSeats);
 	}
 
 	/**
 	 * Transfers the contents of a typed list to a {@link JCoTable}.
 	 * @param source
 	 * @param destination
 	 */
 	public static void toTable(List<FlightData> source, JCoTable destination) {
 		destination.deleteAllRows();
 		for (FlightData entry: source) {
 			destination.appendRow();
 			entry.toStructure(destination);
 		}
 	}
 	
 	/**
 	 * Transfers the contents of a {@link JCoTable} into a typed list.
 	 * @param source
 	 * @param destination
 	 */
 	public static List<FlightData> fromTable(JCoTable source) {
 		List<FlightData> list = new ArrayList<FlightData>();
 		if (!source.isEmpty()) {
			for (int i = 1; i <= source.getNumRows(); i++) {
				source.setRow(i);
 				list.add(new FlightData(source));
			} 
 		}
 		return list;
 	}
 	
 	/**
 	 * @throws UnsupportedOperationException if any structure other than BAPISFLIST is passed
 	 */
 	private void checkStructure(JCoRecord structure) throws UnsupportedOperationException {
 		final String structureName = structure.getMetaData().getName(); 
 		if (!structureName.equals("BAPISFLIST")) {
 			throw new UnsupportedOperationException(
 				MessageFormat.format("Unsupported structure {0} (expected BAPISFLIST).", structureName));
 		}
 	}
 	
 	/**
 	 * @return the carrier ID (BAPISFLIST-CARRID)
 	 */
 	public String getCarrierID() {
 		return this.carrierID;
 	}
 	
 	/**
 	 * Changes the carrier ID (BAPISFLIST-CARRID).
 	 * @param newCarrierID the new carrier ID to set
 	 */
 	public void setCarrierID(String newCarrierID) {
 		_pcs.firePropertyChange("carrierID", this.carrierID, newCarrierID);
 		this.carrierID = newCarrierID;
 	}
 	
 	/**
 	 * @return the connection ID (BAPISFLIST-CONNID)
 	 */
 	public String getConnectionID() {
 		return this.connectionID;
 	}
 	
 	/**
 	 * Changes the connection ID (BAPISFLIST-CONNID).
 	 * @param newConnectionID the new connection ID to set
 	 */
 	public void setConnectionID(String newConnectionID) {
 		_pcs.firePropertyChange("connectionID", this.connectionID, newConnectionID);
 		this.connectionID = newConnectionID;
 	}
 	
 	/**
 	 * @return the flight date (BAPISFLIST-FLDATE)
 	 */
 	public Date getFlightDate() {
 		return this.flightDate;
 	}
 	
 	/**
 	 * Changes the flight date (BAPISFLIST-FLDATE).
 	 * @param newFlightDate the new flight date to set
 	 */
 	public void setFlightDate(Date newFlightDate) {
 		_pcs.firePropertyChange("flightDate", this.flightDate, newFlightDate);
 		this.flightDate = newFlightDate;
 	}
 	
 	/**
 	 * @return the departure time (BAPISFLIST-DEPTIME)
 	 */
 	public Date getDepartureTime() {
 		return this.departureTime;
 	}
 	
 	/**
 	 * Changes the departure time (BAPISFLIST-DEPTIME).
 	 * @param newDepartureTime the new departure time to set
 	 */
 	public void setDepartureTime(Date newDepartureTime) {
 		_pcs.firePropertyChange("departureTime", this.departureTime, newDepartureTime);
 		this.departureTime = newDepartureTime;
 	}
 	
 	/**
 	 * @return the originating airport (BAPISFLIST-AIRPFROM)
 	 */
 	public String getOrigin() {
 		return this.origin;
 	}
 	
 	/**
 	 * Changes the originating airport (BAPISFLIST-AIRPFROM).
 	 * @param newOrigin the new originating airport to set
 	 */
 	public void setOrigin(String newOrigin) {
 		_pcs.firePropertyChange("origin", this.origin, newOrigin);
 		this.origin = newOrigin;
 	}
 	
 	/**
 	 * @return the destination airport (BAPISFLIST-AIRPTO)
 	 */
 	public String getDestination() {
 		return this.destination;
 	}
 	
 	/**
 	 * Changes the destination airport (BAPISFLIST-AIRPTO).
 	 * @param newDestination the new destination airport to set
 	 */
 	public void setDestination(String newDestination) {
 		_pcs.firePropertyChange("destination", this.destination, newDestination);
 		this.destination = newDestination;
 	}
 	
 	/**
 	 * @return the maximum number of seats (BAPISFLIST-SEATSMAX)
 	 */
 	public int getMaximumSeats() {
 		return this.maximumSeats;
 	}
 	
 	/**
 	 * Changes the maximum number of seats (BAPISFLIST-SEATSMAX).
 	 * @param newMaximumSeats the new maximum number of seats to set
 	 */
 	public void setMaximumSeats(int newMaximumSeats) {
 		_pcs.firePropertyChange("maximumSeats", this.maximumSeats, newMaximumSeats);
 		this.maximumSeats = newMaximumSeats;
 	}
 	
 	/**
 	 * @return the number of seats occupied (BAPISFLIST-SEATSOCC)
 	 */
 	public int getOccupiedSeats() {
 		return this.occupiedSeats;
 	}
 	
 	/**
 	 * Changes the number of seats occupied (BAPISFLIST-SEATSOCC).
 	 * @param newOccupiedSeats the new number of seats occupied to set
 	 */
 	public void setOccupiedSeats(int newOccupiedSeats) {
 		_pcs.firePropertyChange("occupiedSeats", this.occupiedSeats, newOccupiedSeats);
 		this.occupiedSeats = newOccupiedSeats;
 	}
 	
 	/**
 	 * @param listener
 	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		_pcs.addPropertyChangeListener(listener);
 	}
 
 	/**
 	 * @param propertyName
 	 * @param listener
 	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
 	 */
 	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
 		_pcs.addPropertyChangeListener(propertyName, listener);
 	}
 
 	/**
 	 * @return
 	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
 	 */
 	public PropertyChangeListener[] getPropertyChangeListeners() {
 		return _pcs.getPropertyChangeListeners();
 	}
 
 	/**
 	 * @param propertyName
 	 * @return
 	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
 	 */
 	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
 		return _pcs.getPropertyChangeListeners(propertyName);
 	}
 
 	/**
 	 * @param propertyName
 	 * @return
 	 * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
 	 */
 	public boolean hasListeners(String propertyName) {
 		return _pcs.hasListeners(propertyName);
 	}
 
 	/**
 	 * @param listener
 	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		_pcs.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * @param propertyName
 	 * @param listener
 	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
 	 */
 	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
 		_pcs.removePropertyChangeListener(propertyName, listener);
 	}
 
 }
