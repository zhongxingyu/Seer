 package client;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import rental.CarType;
 import rental.Reservation;
 
 /**
  * Small scriptable testing system. Reads a scenario from file and executes it.
  * 
  * Supported commands
  * 
 * <command>C add to other command to indicate that <command> is expected to
 * fail <client> S Session: start a client session for client named <client>
 * <client> A <from> <till> Availability: check which car types are available
 * from <from> until <till>. <client> B <from> <till> <car type> Book: add a
 * quote to the session of <client>, for a car of type <car type> from <from>
 * until <till>. <client> F Finalize: finalize the quotes made for client
 * <client> <client> MR Manager, Reservations: print all reservations of client
 * <client> <company> M <type:nr>* Manager: check total number of reservations
 * per car type, in company <company>, each type <type> has <nr> reservations
  */
 public abstract class AbstractScriptedTripTest<ReservationSession, ManagerSession> {
 
 	/**
 	 * Create a new reservation session for the user with the given name.
 	 * 
 	 * @param name
 	 *            name of the client owning this session
 	 * @return the new reservation session
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract ReservationSession getNewReservationSession(String name)
 			throws Exception;
 
 	/**
 	 * Create a new manager session for the user with the given name.
 	 * 
 	 * @param name
 	 *            name of the client owning this session
 	 * @return the new manager session
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract ManagerSession getNewManagerSession(String name)
 			throws Exception;
 
 	/**
 	 * Check which car types are available in the given period.
 	 * 
 	 * @param session
 	 *            the session to do the request from
 	 * @param start
 	 *            start time of the period
 	 * @param end
 	 *            end time of the period
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract void checkForAvailableCarTypes(
 			ReservationSession session, Date start, Date end) throws Exception;
 
 	/**
 	 * Add a quote for a given car type to the session.
 	 * 
 	 * @param session
 	 *            the session to add the reservation to
 	 * @param start
 	 *            start time of the reservation
 	 * @param end
 	 *            end time of the reservation
 	 * @param carType
 	 *            type of car to be reserved
 	 * @param carRentalName
 	 *            name of the rental company by which the reservation should be
 	 *            done
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract void addQuoteToSession(ReservationSession session,
 			Date start, Date end, String carType, String carRentalName)
 			throws Exception;
 
 	/**
 	 * Confirm the quotes in the given session.
 	 * 
 	 * @param session
 	 *            the session to finalize
 	 * @return Returns the list of confirmed reservations.
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract List<Reservation> confirmQuotes(
 			ReservationSession session) throws Exception;
 
 	/**
 	 * Get the number of reservations made by the given client (across whole
 	 * rental agency).
 	 * 
 	 * @param ms
 	 *            manager session
 	 * @param clientName
 	 *            name of the client
 	 * @return the number of reservations of the given client (across whole
 	 *         rental agency)
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract int getNumberOfReservationsBy(ManagerSession ms,
 			String clientName) throws Exception;
 
 	/**
 	 * Get the number of reservations for a particular car type in the given car
 	 * rental company.
 	 * 
 	 * @param ms
 	 *            manager session
 	 * @param carRentalCompanyName
 	 *            The name of the car rental company.
 	 * @param carType
 	 *            name of the car type
 	 * @return number of reservations for this car type in the given car rental
 	 *         company
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract int getNumberOfReservationsForCarType(ManagerSession ms,
 			String carRentalCompanyName, String carType) throws Exception;
 
 	/**
 	 * Get the most popular car rental company (i.e. it has the highest number
 	 * of reservations).
 	 * 
 	 * @param ms
 	 *            manager session
 	 * @return the name of the most popular car rental company
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract String getMostPopularCarRentalCompany(ManagerSession ms)
 			throws Exception;
 
 	/**
 	 * Get the most popular car type in the given car rental company.
 	 * 
 	 * @param ms
 	 *            manager session
 	 * @param carRentalCompanyName
 	 *            The name of the car rental company.
 	 * @return the most popular car type in the given car rental company
 	 * 
 	 * @throws Exception
 	 *             if things go wrong, throw exception
 	 */
 	protected abstract CarType getMostPopularCarTypeIn(ManagerSession ms,
 			String carRentalCompanyName) throws Exception;
 
 	// date format to parse dates from file
 	private static final DateFormat datef = new SimpleDateFormat("d/M/y");
 	// name of the file containing the test script
 	private String scriptFile;
 	// open sessions
 	private HashMap<String, ReservationSession> sessions = new HashMap<String, ReservationSession>();
 
 	public AbstractScriptedTripTest(String scriptFile) {
 		this.scriptFile = scriptFile;
 	}
 
 	public void run() throws Exception {
 		// read script
 		BufferedReader in = new BufferedReader(new FileReader(scriptFile));
 		try {
 			int lnr = 0;
 			// while we have lines
 			while (in.ready()) {
 				lnr++;
 
 				// read line
 				String line = in.readLine();
 				// tokenize
 				StringTokenizer scriptReader = new StringTokenizer(line, " ");
 
 				// get command and name
 				String name = scriptReader.nextToken();
 				String cmd = scriptReader.nextToken();
 
 				// dispatch according to command
 				if (cmd.contains("S"))
 					sessions.put(name, getNewReservationSession(name));
 				else if (cmd.contains("M"))
 					if (cmd.contains("R")) {
 						ManagerSession rental = getNewManagerSession("CarRent");
 						System.out.println("Number of reservations by " + name
 								+ ":\t"
 								+ getNumberOfReservationsBy(rental, name));
 					} else if (cmd.contains("F")) {
 						getFavorites(name, scriptReader);
 					} else {
 						check(name, scriptReader);
 					}
 				else {
 					ReservationSession session = sessions.get(name);
 					if (session == null)
 						throw new IllegalArgumentException(
 								"script broken: no session" + line
 										+ " on line " + lnr);
 					if (cmd.contains("A")) {
 						Date start = datef.parse(scriptReader.nextToken());
 						Date end = datef.parse(scriptReader.nextToken());
 						checkForAvailableCarTypes(session, start, end);
 					} else if (cmd.contains("B")) {
 						Date start = datef.parse(scriptReader.nextToken());
 						Date end = datef.parse(scriptReader.nextToken());
 						String type = scriptReader.nextToken();
 						String agent = scriptReader.nextToken();
 						Exception be = null;
 						try {
 							addQuoteToSession(session, start, end, type, agent);
 						} catch (Exception e) {
 							be = e;
 						}
 						boolean shouldfail = cmd.contains("C");
 						if (be == null && shouldfail)
 							System.err.println("command should have failed: "
 									+ line + " on line " + lnr);
 						if (be != null && !shouldfail) {
 							System.err.println("command failed: " + line
 									+ " on line " + lnr);
 							be.printStackTrace();
 						}
 					} else if (cmd.contains("F")) {
 						Exception be = null;
 						try {
 							List<Reservation> reservations = confirmQuotes(session);
 							System.out.println("Confirmed reservations:");
 							int i = 1;
 							for (Reservation r : reservations) {
 								System.out.println(i++ + ") " + r.toString()
 										+ "\n");
 							}
 						} catch (Exception e) {
 							be = e;
 						}
 						boolean shouldfail = cmd.contains("C");
 						if (be == null && shouldfail)
 							System.err.println("command should have failed: "
 									+ line + " on line " + lnr);
 						if (be != null && !shouldfail) {
 							System.err.println("command failed: " + line
 									+ " on line " + lnr);
 							be.printStackTrace();
 						}
 					} else
 						throw new IllegalArgumentException("unknown command"
 								+ line + " on line " + lnr);
 				}
 			}
 		} finally {
 			in.close();
 		}
 	}
 
 	private void check(String name, StringTokenizer scriptReader)
 			throws Exception {
 		ManagerSession rental = getNewManagerSession(name);
 		while (scriptReader.hasMoreTokens()) {
 			String pars = scriptReader.nextToken();
 			String[] pair = pars.split(":");
 			int nr = getNumberOfReservationsForCarType(rental, name, pair[0]);
 			if (Integer.parseInt(pair[1]) == nr) {
 				System.out.println(name + " has correct totals " + pars + " "
 						+ nr);
 			} else {
 				System.err.println(name + " has wrong totals " + pars + " "
 						+ nr);
 			}
 		}
 
 	}
 
 	private void getFavorites(String name, StringTokenizer scriptReader)
 			throws Exception {
 		ManagerSession rental = getNewManagerSession(name);
 		while (scriptReader.hasMoreTokens()) {
 			String pars = scriptReader.nextToken();
 			String favorite = null;
 			List<String> favorites = Arrays.asList(pars.split("/"));
 			if (name.equals("CRA")) {
 				favorite = getMostPopularCarRentalCompany(rental);
 				if (favorites.contains(favorite)) {
 					System.out.println(name
 							+ " has correct favorite car rental company: "
 							+ pars + " " + favorite);
 				} else {
 					System.err.println(name
 							+ " has wrong favorite car rental company: " + pars
 							+ " " + favorite);
 				}
 			} else {
 				favorite = getMostPopularCarTypeIn(rental, name).getName();
 				if (favorites.contains(favorite)) {
 					System.out.println(name
 							+ " has correct favorite car type: " + pars + " "
 							+ favorite);
 				} else {
 					System.err.println(name + " has wrong favorite car type: "
 							+ pars + " " + favorite);
 				}
 			}
 		}
 	}
 }
