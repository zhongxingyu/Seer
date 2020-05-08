 package asgn2Train;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import asgn2Exceptions.TrainException;
 import asgn2RollingStock.FreightCar;
 import asgn2RollingStock.Locomotive;
 import asgn2RollingStock.PassengerCar;
 import asgn2RollingStock.RollingStock;
 
 /**
  * A train is a sequence of carriages. This class defines various operations
  * that can be performed to prepare a long-distance train for departure.
  * 
  * We assume that a train can be assembled from any available rolling stock,
  * including locomotives, passenger cars and freight cars. However, they may be
  * configured in only a certain sequence:
  * 
  * The first carriage must be a locomotive (and there can be only one locomotive
  * per train). This may be followed by zero or more passenger cars. These may be
  * followed by zero or more freight cars.
  * 
  * Any other configurations of rolling stock are disallowed.
  * 
  * The process of preparing the train for departure occurs in two stages:
  * 
  * The train is assembled from individual carriages. New carriages may be added
  * to the rear of the train only. (Similarly, carriages may be removed from the
  * rear of the train only.) Passengers board the train. For safety reasons, no
  * carriage shunting operations may be performed when any passengers are on
  * board the train.
  * 
  * 
  * @author Charleston
  * 
  */
 public class DepartingTrain {
 
 	/**
 	 * For convenience the Locomotive object will be stored in a separate
 	 * variable rather than the carriages List
 	 */
 	private Locomotive locomotive;
 
 	/**
 	 * Stores all carriages for a departing trains excluding the locomotive
 	 */
 	private List<RollingStock> carriages;
 
 	/**
 	 * Variable used by firstCarriage and nextCarriage methods in order to
 	 * manage the current carriage to be retrieved
 	 */
 	private int carriagePointer;
 
 	/**
 	 * Error messages to be used by train Exception
 	 */
 	private static final String LOCOMOTIVE_ALREADY_EXISTS = "Locomotive already exists";
 	private static final String CANNOT_BE_ADDED_TRAIN_HAS_PASSENGER = "Carriage cannot be added because train has passengers onboard";
 	private static final String CANNOT_BE_REMOVED_TRAIN_HAS_PASSENGER = "Carriage cannot be removed because train has passengers onboard";
 	private static final String NO_ROLLING_STOCK_TO_REMOVED = "There is no Rolling Stock in the train";
 	private static final String CANNOT_ADD_PASSENGERCAR_AFTER_FREIGHTCAR = "Passenger Car cannot be added after a Freight Car";
 	private static final String CANNOT_BOARD_NEGATIVE_NUMBER_OF_PASSANGER = "Negative number of passengers cannot be boarded";
 
 	/**
 	 * Constructs a (potential) train object containing no carriages (yet).
 	 * 
 	 * CarriagePointer is initialized with -1 which means no carriages and no
 	 * locomotive.
 	 */
 	public DepartingTrain() {
 		this.carriages = new ArrayList<RollingStock>();
 		this.carriagePointer = -1;
 	}
 
 	/**
 	 * Adds a new carriage to the end of the train. However, a new carriage may
 	 * be added only if the resulting train configuration is valid, as per the
 	 * rules listed above. Furthermore, shunting operations may not be performed
 	 * if there are passengers on the train.
 	 * 
 	 * Hint: You may find Java's in-built instanceof operator useful when
 	 * implementing this method (and others in this class).
 	 * 
 	 * @param newCarriage
 	 *            the new carriage to be added
 	 * 
 	 * @throws TrainException
 	 *             if adding the new carriage would produce an invalid train
 	 *             configuration, or if there are passengers on the train
 	 */
 	public void addCarriage(RollingStock newCarriage) throws TrainException {
 		if (hasPassenger())
 			throw new TrainException(CANNOT_BE_ADDED_TRAIN_HAS_PASSENGER);
 
 		if (newCarriage instanceof Locomotive) {
 			if (locomotive != null)
 				throw new TrainException(LOCOMOTIVE_ALREADY_EXISTS);
 			locomotive = (Locomotive) newCarriage;
 		} else if (newCarriage instanceof FreightCar) {
 			carriages.add(newCarriage);
 		} else if (newCarriage instanceof PassengerCar) {
 			if (carriages.size() >= 1
 					&& carriages.get(carriages.size() - 1) instanceof FreightCar)
 				throw new TrainException(
 						CANNOT_ADD_PASSENGERCAR_AFTER_FREIGHTCAR);
 			carriages.add(newCarriage);
 		}
 	}
 
 	/**
 	 * Adds the given number of people to passenger carriages on the train. We
 	 * do not specify where the passengers must sit, so they can be allocated to
 	 * any vacant seat in any passenger car.
 	 * 
 	 * @param newPassengers
 	 *            the number of people wish to board the train
 	 * @return the number of people who were unable to board the train because
 	 *         they couldn't get a seat
 	 * @throws TrainException
 	 *             if the number of new passengers is negative
 	 */
 	public Integer board(Integer newPassengers) throws TrainException {
 		if (newPassengers < 0)
 			throw new TrainException(CANNOT_BOARD_NEGATIVE_NUMBER_OF_PASSANGER);
 		int remainingPassenger = newPassengers;
 		for (RollingStock rs : carriages) {
 			if (rs instanceof PassengerCar
 					&& ((PassengerCar) rs).numberOnBoard() < ((PassengerCar) rs)
 							.numberOfSeats()) {
 				remainingPassenger = ((PassengerCar) rs)
 						.board(remainingPassenger);
 			}
 			if (remainingPassenger == 0)
 				break;
 		}
 		return remainingPassenger;
 	}
 
 	/**
 	 * Returns the first carriage on the train (which must be a locomotive).
 	 * Special value null is returned if there are no carriages on the train at
 	 * all.
 	 * 
 	 * NB: When combined with method nextCarriage, this method gives us a simple
 	 * ability to iteratively examine each of the train's carriages.
 	 * 
 	 * @return the first carriage in the train, or null if there are no
 	 *         carriages
 	 */
 	public RollingStock firstCarriage() {
 		if (locomotive != null) {
 			carriagePointer = 0;
 			return locomotive;
 		} else {
 			if (carriages.size() > 0) {
 				carriagePointer = 1;
 				return carriages.get(0);
 			} else {
 				carriagePointer = 0;
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Returns the next carriage in the train after the one returned by the
 	 * immediately preceding call to either this method or method firstCarriage.
 	 * Special value null is returned if there is no such carriage. If there has
 	 * been no preceding call to either firstCarriage or nextCarriage, this
 	 * method behaves like firstCarriage, i.e., it returns the first carriage in
 	 * the train, if any.
 	 * 
 	 * NB: When combined with method firstCarriage, this method gives us a
 	 * simple ability to iteratively examine each of the train's carriages.
 	 * 
 	 * @return the train's next carriage after the one returned by the
 	 *         immediately preceding call to either firstCarriage or
 	 *         nextCarriage, or null if there is no such carriage
 	 */
 	public RollingStock nextCarriage() {
 		if (carriagePointer++ == -1)
 			return locomotive;
 		else
 			return carriagePointer > carriages.size() ? null : carriages
 					.get(carriagePointer - 1);
 	}
 
 	/**
 	 * Returns the total number of seats on the train (whether occupied or not),
 	 * counting all passenger cars.
 	 * 
 	 * @return the number of seats on the train
 	 */
 	public Integer numberOfSeats() {
 		Integer totalSeats = 0;
 		for (RollingStock rs : carriages) {
 			if (rs instanceof PassengerCar) {
 				totalSeats += ((PassengerCar) rs).numberOfSeats();
 			}
 
 		}
 		return totalSeats;
 	}
 
 	/**
 	 * Returns the total number of passengers currently on the train, counting
 	 * all passenger cars.
 	 * 
 	 * @return the number of passengers on the train
 	 */
 	public Integer numberOnBoard() {
 		Integer totalOnBoard = 0;
 		for (RollingStock rs : carriages) {
 			if (rs instanceof PassengerCar) {
 				totalOnBoard += ((PassengerCar) rs).numberOnBoard();
 			}
 
 		}
 		return totalOnBoard;
 	}
 
 	/**
 	 * Removes the last carriage from the train. (This may be the locomotive if
 	 * it is the only item of rolling stock on the train.) However, shunting
 	 * operations may not be performed if there are passengers on the train.
 	 * 
 	 * @throws TrainException
 	 *             if there is no rolling stock on the "train", or if there are
 	 *             passengers on the train.
 	 */
 	public void removeCarriage() throws TrainException {
 		if (locomotive == null && carriages.size() == 0)
 			throw new TrainException(NO_ROLLING_STOCK_TO_REMOVED);
 		if (hasPassenger())
 			throw new TrainException(CANNOT_BE_REMOVED_TRAIN_HAS_PASSENGER);
 		if (carriages.size() == 0 && locomotive != null)
 			locomotive = null;
 		else
 			carriages.remove(carriages.size() - 1);
 	}
 
 	/**
 	 * Returns a human-readable description of the entire train. This has the
 	 * form of a hyphen-separated list of carriages, starting with the
 	 * locomotive on the left. The description is thus a string "a-b-...-z",
 	 * where a is the human-readable description of the first carriage (the
 	 * locomotive), b is the description of the second carriage, etc, until the
 	 * description of the last carriage z. (Note that there should be no hyphen
 	 * after the last carriage.) For example, a possible train description may
 	 * be "Loco(6D)-Passenger(13/24)-Passenger(16/16)-Freight(G)".
 	 * 
 	 * In the degenerate case of a "train" with no carriages, the empty string
 	 * is returned.
 	 * 
 	 * @return a human-readable description of the entire train
 	 */
 	public String toString() {
 		String trainString = "";
 		if (locomotive != null)
 			trainString += locomotive;
 		for (RollingStock rs : carriages) {
 			trainString += "-" + rs;
 		}
 		return trainString;
 	}
 
 	/**
 	 * Returns whether or not the train is capable of moving. A train can move
 	 * if its locomotive's pulling power equals or exceeds the train's total
 	 * weight (including the locomotive itself).
 	 * 
 	 * In the degenerate case of a "train" which doesn't have any rolling stock
 	 * at all yet, the method returns true.
 	 * 
 	 * @return boolean - true if the train can move (or contains no carriages), false
 	 *         otherwise
 	 */
 	public boolean trainCanMove() {
 		if (locomotive == null)
 			return false;
 		if (carriages.size() == 0)
 			return true;
		return locomotive.power() > getTotalWeigth();
 	}
 
 	/**
 	 * Verifies the departing trains has passengers on board.
 	 * 
 	 * @return true if departing train has passengers on board, and returns
 	 *         false otherwise.
 	 */
 	private boolean hasPassenger() {
 		for (RollingStock rs : carriages) {
 			if (rs instanceof PassengerCar
 					&& ((PassengerCar) rs).numberOnBoard() > 0) {
 				return true;
 			}
 
 		}
 		return false;
 	}
 
 	/**
 	 * Sum up weights for each carriage.
 	 * 
 	 * @return int - total weight of the departing train.
 	 */
 	private int getTotalWeigth() {
 		int totalWeight = 0;
 		if (locomotive != null)
 			totalWeight += locomotive.getGrossWeight();
 		for (RollingStock rs : carriages) {
 			totalWeight += rs.getGrossWeight();
 		}
 		return totalWeight;
 	}
 }
