 package ca.etsmtl.log430.lab3;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Observable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Assigns drivers to deliveries.
  * 
  * @author A.J. Lattanze, CMU
  * @version 1.4, 2012-Jun-19
  */
 
 /*
  * Modification Log **********************************************************
  * v1.4, R. Champagne, 2012-Jun-19 - Various refactorings for new lab.
  * 
  * v1.3, R. Champagne, 2012-Feb-14 - Various refactorings for new lab.
  * 
  * v1.2, R. Champagne, 2011-Feb-24 - Various refactorings, conversion of
  * comments to javadoc format.
  * 
  * v1.1, R. Champagne, 2002-Jun-19 - Adapted for use at ETS.
  * 
  * v1.0, A.J. Lattanze, 12/29/99 - Original version.
  * ***************************************************************************
  */
 public class AssignDriverToDelivery extends Communication {
 
 	public AssignDriverToDelivery(Integer registrationNumber, String componentName) {
 		super(registrationNumber, componentName);
 	}
 
 	/**
 	 * The update() method is an abstract method that is called whenever the
 	 * notifyObservers() method is called by the Observable class. First we
 	 * check to see if the NotificationNumber is equal to this thread's
 	 * RegistrationNumber. If it is, then we execute.
 	 * 
 	 * @see ca.etsmtl.log430.lab3.Communication#update(java.util.Observable,
 	 *      java.lang.Object)
 	 */
 	public void update(Observable thing, Object notificationNumber) {
 		Menus menu = new Menus();
 		Driver myDriver = new Driver();
 		Delivery myDelivery = new Delivery();
 
 		if (registrationNumber.compareTo((Integer) notificationNumber) == 0) {
 			addToReceiverList("ListDriversComponent");
 			addToReceiverList("ListDeliveriesComponent");
 
 			// Display the drivers and prompt the user to pick a driver
 
 			signalReceivers("ListDriversComponent");
 
 			myDriver = menu.pickDriver(CommonData.theListOfDrivers.getListOfDrivers());
 
 			if (myDriver != null) {
 				/*
 				 * Display the deliveries that are available and ask the user to
 				 * pick a delivery to register for
 				 */
 				signalReceivers("ListDeliveriesComponent");
 
 				myDelivery = menu.pickDelivery(CommonData.theListOfDeliveries.getListOfDeliveries());
 
 				if (myDelivery != null) {
 					/*
 					 * If the selected delivery and driver exist and the
 					 * delivery is assigned to no one,then complete the
 					 * assignment process.
 					 */
 					if (myDelivery.getDriversAssigned().isEmpty()) {
 						if (noConflictsWillHappenUponAssignation(myDriver, myDelivery)) {
 							if (driverCanDoSomething(myDriver, myDelivery)) {
 								myDelivery.assignDriver(myDriver);
 								myDriver.assignDelivery(myDelivery);
 							} else {
 								System.out.println("\n\n *** Maximum number of hours allowed already allocated to driver ***");
 							}
 						} else {
 							System.out.println("\n\n *** Delivery in conflict with already assigned deliveries ***");
 						}
 
 					} else {
 						System.out.println("\n\n *** Delivery already assigned to a driver ***");
 					}
 
 				} else {
 					System.out.println("\n\n *** Delivery not found ***");
 				}
 			} else {
 				System.out.println("\n\n *** Driver not found ***");
 			}
 		}
 	}
 
 	private boolean noConflictsWillHappenUponAssignation(Driver driver, Delivery delivery) {
 		Date deliveryMinTime = getDeliveryMinTime(delivery);
 		Date deliveryMaxTime = getDeliveryMaxTime(delivery);
 
 		for (Delivery d : driver.getDeliveriesAssigned()) {
 			Date minTime = getDeliveryMinTime(d);
 			Date maxTime = getDeliveryMaxTime(d);
 			if (!isBetweenTwoDates(deliveryMinTime, minTime, maxTime) || !isBetweenTwoDates(deliveryMaxTime, minTime, maxTime)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean driverCanDoSomething(Driver driver, Delivery delivery) {
 		Calendar calDuration = Calendar.getInstance();
 		Calendar cal = Calendar.getInstance();
 
 		if (driver == null || delivery == null)
 			return false;
 		
 		calDuration.clear();
 		
 		cal.clear();
 		cal.setTime(parseFourCharFormatDateString(delivery.getEstimatedDeliveryDuration()));
 		calDuration.add(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) * 2);
 		calDuration.add(Calendar.MINUTE, cal.get(Calendar.MINUTE) * 2);
 
 		for (Delivery d : driver.getDeliveriesAssigned()) {
 			cal.clear();
 			cal.setTime(parseFourCharFormatDateString(d.getEstimatedDeliveryDuration()));
 			calDuration.add(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) * 2);
 			calDuration.add(Calendar.MINUTE, cal.get(Calendar.MINUTE) * 2);
 		}
 		
 		int days = calDuration.get(Calendar.DAY_OF_YEAR);
 		int hours = calDuration.get(Calendar.HOUR_OF_DAY);
		int minutes = calDuration.get(Calendar.MINUTE);
		double totalHours = days * 24 + hours + minutes / 100.0;
 		
 		if (driver.getType() == "JNR")
 			if (totalHours <= 12) 
 				return true;
 
 		else if (driver.getType() == "SNR") 
 			if (totalHours <= 8)
 				return true;
 
 		return false;
 	}
 
 	private boolean isBetweenTwoDates(Date date, Date dateMin, Date dateMax) {
 		return dateMin.before(date) && dateMax.after(dateMax);
 	}
 
 	private Date getDeliveryMinTime(Delivery d) {
 		return addDurationFactorToDeliveryTime(d, -1);
 	}
 
 	private Date getDeliveryMaxTime(Delivery d) {
 		return addDurationFactorToDeliveryTime(d, 1);
 	}
 
 	private Date addDurationFactorToDeliveryTime(Delivery d, int factor) {
 		Date deliveryTime = parseFourCharFormatDateString(d.getDesiredDeliveryTime());
 		Date deliveryDuration = parseFourCharFormatDateString(d.getEstimatedDeliveryDuration());
 
 		Calendar calDuration = Calendar.getInstance();
 		calDuration.clear();
 		calDuration.setTime(deliveryDuration);
 
 		Calendar cal = Calendar.getInstance();
 		cal.clear();
 		cal.setTime(deliveryTime);
 
 		cal.add(Calendar.HOUR_OF_DAY, factor * calDuration.get(Calendar.HOUR_OF_DAY));
 		cal.add(Calendar.MINUTE, factor * calDuration.get(Calendar.MINUTE));
 
 		return cal.getTime();
 	}
 
 	private Date parseFourCharFormatDateString(String s) {
 		SimpleDateFormat sd = new SimpleDateFormat("HHmm");
 		Date d = null;
 		try {
 			d = sd.parse(s);
 		} catch (ParseException ex) {
 			Logger.getLogger(AssignDriverToDelivery.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 		return d;
 	}
 }
