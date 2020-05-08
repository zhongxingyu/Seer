 package server;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.security.AccessControlException;
 import java.security.InvalidParameterException;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.TrustManagerFactory;
 
 import util.Division;
 import util.Doctor;
 import util.Entity;
 import util.EntityType;
 import util.EntityWithAccessControl;
 import util.GovernmentAgent;
 import util.NetworkCommunication;
 import util.Nurse;
 import util.PasswordManager;
 import util.Patient;
 import util.Record;
 import util.logger.EntityAccessDeniedLogEvent;
 import util.logger.EntityAccessLogEvent;
 import util.logger.Log;
 import util.logger.LogEvent;
 
 public class Server {
 
 	// Initiate system before starting the server
 	private static HashMap<Integer, Division> divisions;
 	private static ArrayList<Doctor> docs;
 	private static ArrayList<Nurse> nurses;
 	private static ArrayList<Patient> patients;
 	private static ArrayList<Record> records;
 	private static GovernmentAgent agent;
 	private static Log log = new Log(System.out);
 	private static Entity currentEntityUser;
 	private static final int PORT = 5678;
 	private static PasswordManager pwMn = new PasswordManager();
 
 	public static void main(String[] args) {
 
 		System.setProperty("javax.net.ssl.trustStore",
 				"./certificates/CA/truststore");
 
 		/*
 		 * Create hospital divisions according to UUH (Uppsala University
 		 * Hospital).
 		 * http://en.wikipedia.org/wiki/Uppsala_University_Hospital#Divisions
 		 */
 		divisions = new HashMap<Integer, Division>();
 		divisions.put(0, new Division(
 				"Diagnostics, Anesthesia and Technology Division"));
 		divisions.put(1, new Division("Emergency and Rehabilitation Division"));
 		divisions.put(2, new Division("Oncology, Thorax and Medical Division"));
 		divisions.put(3, new Division("Neurology Division"));
 		divisions.put(4, new Division("Psychiatry Division"));
 		divisions.put(5, new Division("Surgery Division"));
 		divisions
 				.put(6, new Division("Women's Health and Pediatrics Division"));
 		divisions.put(7, new Division("Socialstyrelsen"));
 
 		/*
 		 * Create a "Doctors"-list where all working doctors at the hospital are
 		 * put in, together with their division.
 		 */
 		docs = new ArrayList<Doctor>();
 		docs.add(new Doctor("doctor00", divisions.get(0)));
 		docs.add(new Doctor("doctor01", divisions.get(0)));
 		docs.add(new Doctor("doctor10", divisions.get(1)));
 
 		// creating the hashed passwords for doctors
 		pwMn.createPassword("doctor00", "doc0");
 		pwMn.createPassword("doctor01", "doc1");
 		pwMn.createPassword("doctor10", "doc2");
 
 		/*
 		 * Create a "Nurse"-list where all working nurses at the hospital are
 		 * put in, together with their division.
 		 */
 		nurses = new ArrayList<Nurse>();
 		nurses.add(new Nurse("nurse00", divisions.get(0)));
 		nurses.add(new Nurse("nurse01", divisions.get(0)));
 		nurses.add(new Nurse("nurse10", divisions.get(1)));
 
 		// creating the hashed passwords for nurses
 		pwMn.createPassword("nurse00", "nur0");
 		pwMn.createPassword("nurse01", "nur1");
 		pwMn.createPassword("nurse10", "nur2");
 
 		/*
 		 * Create a "Patient" -list where patients with injuries are put in,
 		 * together with their injury and what division they will need to visit.
 		 */
 		patients = new ArrayList<Patient>();
 		patients.add(new Patient("patient00", "Broken back", divisions.get(0)));
 		patients.add(new Patient("patient01", "Broken toe", divisions.get(0)));
 		patients.add(new Patient("patient10", "Fractured skull", divisions
 				.get(1)));
 
 		// creating the hashed passwords for patients
 		pwMn.createPassword("patient00", "pat0");
 		pwMn.createPassword("patient01", "pat1");
 		pwMn.createPassword("patient10", "pat2");
 
 		/*
 		 * Create a agent who is working for the government, is this case
 		 * "Socialstyrelsen".
 		 */
 		agent = new GovernmentAgent("agent", divisions.get(7));
 
 		// creating hashed password for gov agent
 		pwMn.createPassword("agent", "age0");
 
 		/*
 		 * Create medical "Records"-list where all medicial journals are stored.
 		 */
 		records = new ArrayList<Record>();
 		records.add(createJournal(patients.get(0), docs.get(0), nurses.get(0)));
 		records.add(createJournal(patients.get(1), docs.get(1), nurses.get(1)));
 		records.add(createJournal(patients.get(2), docs.get(2), nurses.get(2)));
 
 		// Start the server
 		Server s = new Server();
 		s.run();
 	}
 
 	private void run() {
 
 		try {
 			// Create SSL Socket which will wait and listen for a request.
 			// BufferedReader and -Writer are instantiated
 			// for transmitting and receiving data.
 			SSLSocket client;
 			String readLine = null;
 
 			commands = new HashMap<String, Pattern>();
 
 			/*
 			 * Commands:
 			 * 
 			 * "All": list records read record [record_id]
 			 * 
 			 * "Nurse": list patients write record [record_id] [data]
 			 * 
 			 * "Doctor": list patients list nurses write record [record_id]
 			 * [data] create record [patient_id] [nurse_id] [data] assign
 			 * [nurse_id] to [patient_id]
 			 * 
 			 * "Government Agent": delete [record_id]
 			 */
 
 			commands.put("help", Pattern.compile("help"));
 			commands.put("list records", Pattern.compile("list records"));
 			commands.put("list nurses", Pattern.compile("list nurses"));
 			commands.put("list patients", Pattern.compile("list patients"));
 			commands.put("read record", Pattern.compile("read record (\\d+)"));
 			commands.put("write record",
 					Pattern.compile("write record (\\d+) (.*)"));
 			commands.put("delete record",
 					Pattern.compile("delete record (\\d+)"));
 			commands.put(
 					"create record",
 					Pattern.compile("create record for patient (\\d+) with nurse (\\d+) and data (.*)"));
 			commands.put("assign nurse",
 					Pattern.compile("assign nurse (\\d+) to patient (\\d+)"));
 
 			char[] passphrase = "password".toCharArray();
 
 			SSLContext ctx = SSLContext.getInstance("TLS");
 			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
 			KeyStore ks = KeyStore.getInstance("JKS");
 			TrustManagerFactory tmf = TrustManagerFactory
 					.getInstance("SunX509");
 
 			ks.load(new FileInputStream("./certificates/server/server.jks"),
 					passphrase);
 
 			kmf.init(ks, passphrase);
 			tmf.init(ks);
 			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
 
 			SSLServerSocketFactory factory = ctx.getServerSocketFactory();
 			SSLServerSocket ss = (SSLServerSocket) factory
 					.createServerSocket(PORT);
 			ss.setNeedClientAuth(true);
 
 			System.out.println("Running server ...");
 			log.updateLog(new LogEvent(Log.LVL_INFO, "Server",
 					"Server is running"));
 			System.out.println(ss);
 			System.out.println("Server is listening on port " + PORT);
 
 			while (true) {
 
 				client = (SSLSocket) ss.accept();
 				printSocketInfo(client);
 				System.out.println("Client connected ...");
 
 				log.updateLog(new LogEvent(Log.LVL_INFO, "Client",
 						"A client has connected to the server"));
 
 				PrintWriter toClient = new PrintWriter(new PrintWriter(
 						client.getOutputStream()));
 				BufferedReader fromClient = new BufferedReader(
 						new InputStreamReader(client.getInputStream()));
 
 				NetworkCommunication nc = new NetworkCommunication(toClient,
 						fromClient);
 
 				String name = nc.receive();
				// byte[] userSalt = pwMn.getSalt(name);
				//
				// nc.sendByteArray(userSalt);
				//
				// log.updateLog(new LogEvent(Log.LVL_INFO, "Client",
				// "name has been received from client"));
 
 				System.out.println("Client connected ...");
 
 				System.out.println("Logging in client ...");
 
 				currentEntityUser = findEntity(name);
 
 				log.updateLog(new LogEvent(Log.LVL_INFO, "Client",
 						"Client password match its entity"));
 				System.out.println(String.format("Welcome %s! %s",
 						currentEntityUser.getName(), currentEntityUser
 								.getClass().getName()));
 
 				nc.send(String.format("Welcome %s! %s", currentEntityUser
 						.getName(), currentEntityUser.getClass().getName()));
 				log.updateLog(new LogEvent(Log.LVL_INFO, "Server",
 						"Welcome has been sent"));
 
 				do {
 					readLine = nc.receive();
 					log.updateLog(new LogEvent(Log.LVL_INFO, "Client info",
 							readLine));
 					System.out.println("read: " + readLine);
 
 					if (readLine == null) {
 						log.updateLog(new LogEvent(Log.LVL_INFO, "Client info",
 								"readLine was null"));
 						break;
 					}
 
 					boolean parsedCmd = false;
 
 					for (Entry<String, Pattern> e : commands.entrySet()) {
 						if (e.getValue().matcher(readLine).matches()) {
 							nc.send(handleCommand(currentEntityUser,
 									e.getKey(), e.getValue(), readLine));
 							parsedCmd = true;
 
 						}
 					}
 
 					if (!parsedCmd) {
 						nc.send("Unrecognized command. \nUse 'help' for a list of available commands.");
 					}
 
 				} while (readLine != null && !readLine.equals("quit"));
 
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			log.updateLog(new LogEvent(Log.LVL_ERROR, "IOException", e
 					.toString()));
 		} catch (Exception e) {
 			e.printStackTrace();
 			log.updateLog(new LogEvent(Log.LVL_ERROR, "Exception", e.toString()));
 		}
 	}
 
 	// Finds the correct entity.
 	private static Entity findEntity(String userName) {
 
 		for (int i = 0; i < docs.size(); i++) {
 			if (docs.get(i).getName().equals(userName)
 					|| docs.get(i).getId() == Integer.parseInt(userName)) {
 				return docs.get(i);
 			}
 		}
 		for (int i = 0; i < nurses.size(); i++) {
 			if (nurses.get(i).getName().equals(userName)
 					|| nurses.get(i).getId() == Integer.parseInt(userName)) {
 				return nurses.get(i);
 			}
 		}
 		for (int i = 0; i < patients.size(); i++) {
 			if (patients.get(i).getName().equals(userName)
 					|| patients.get(i).getId() == Integer.parseInt(userName)) {
 				return patients.get(i);
 			}
 		}
 
 		if (agent.getName().equals(userName))
 			return agent;
 
 		return null;
 	}
 
 	public interface CommandHandler {
 		public String handleCommand(Entity entity, Pattern p, String value);
 	}
 
 	public List<Record> getReadableRecords(Entity entity) {
 		List<Record> result = new ArrayList<Record>();
 
 		for (Record r : records) {
 			if (entity.canAccess(r, EntityWithAccessControl.READ))
 				result.add(r);
 		}
 
 		return result;
 	}
 
 	private List<Nurse> getNursesForEntity(Entity entity) {
 		List<Nurse> result = new ArrayList<Nurse>();
 
 		for (Nurse n : nurses) {
 			if (entity.getDivision().getId().equals(n.getDivision().getId()))
 				result.add(n);
 		}
 
 		return result;
 	}
 
 	private List<Patient> getPatientsForEntity(Entity entity) {
 		List<Patient> result = new ArrayList<Patient>();
 
 		for (Patient p : patients) {
 			if (entity.getDivision().getId().equals(p.getDivision().getId()))
 				result.add(p);
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings("serial")
 	HashMap<String, CommandHandler> m = new HashMap<String, CommandHandler>() {
 		{
 			put("help", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 					StringBuilder sb = new StringBuilder();
 
 					sb.append("Command: \t\t\tFormat:\n");
 
 					for (Map.Entry<String, Pattern> command : commands
 							.entrySet())
 						sb.append(String.format("%s\t\t\t%s\n",
 								command.getKey(), command.getValue().pattern()));
 
 					return sb.toString();
 				}
 
 			});
 
 			put("list records", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 					StringBuilder sb = new StringBuilder();
 					sb.append("Rec#\tPat#\tPatient\t\tNur#\tNurse\tDoc#\tDoctor\n");
 					sb.append("################################################################\n");
 
 					for (Record r : getReadableRecords(entity))
 						if (!r.toDelete()) {
 							sb.append(String.format(
 									"%d\t%d\t%s\t%d\t%s\t%d\t%s\n", r.getId(),
 									r.getPatient().getId(), r.getPatient()
 											.getName(), r.getNurse().getId(), r
 											.getNurse().getName(), r
 											.getDoctor().getId(), r.getDoctor()
 											.getName()));
 						}
 
 					return sb.toString();
 				}
 
 			});
 
 			put("list nurses", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 					StringBuilder sb = new StringBuilder();
 
 					sb.append("Nurse #id\tName\n");
 					sb.append("################################\n");
 
 					for (Nurse r : getNursesForEntity(entity))
 						sb.append(String.format("%d\t%s\n", r.getId(),
 								r.getName()));
 
 					return sb.toString();
 				}
 			});
 
 			put("list patients", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 					StringBuilder sb = new StringBuilder();
 
 					sb.append("Patient #id\tName\t\tData\n");
 					sb.append("######################################\n");
 
 					for (Patient r : getPatientsForEntity(entity))
 						sb.append(String.format("%d\t\t%s\t%s\n", r.getId(),
 								r.getName(), r.getData()));
 
 					return sb.toString();
 				}
 
 			});
 
 			put("read record", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 					StringBuilder sb = new StringBuilder();
 
 					Matcher matcher = p.matcher(value);
 					try {
 						for (Record r : records) {
 							if (matcher.matches()
 									&& r.getId() == Integer.parseInt(matcher
 											.group(1))
 									&& entity.canAccess(r,
 											EntityWithAccessControl.READ)) {
 								sb.append(r.toString());
 							}
 						}
 					} catch (AccessControlException e) {
 						return "Access Denied";
 					}
 
 					return sb.toString();
 				}
 
 			});
 
 			put("write record", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 
 					Matcher m = p.matcher(value);
 
 					try {
 						for (Record r : records) {
 							if (m.matches()
 									&& r.getId() == Integer.parseInt(m.group(1))) {
 								r.writeData(entity, m.group(2));
 							}
 						}
 					} catch (AccessControlException e) {
 						return "Access Denied";
 					}
 
 					return "Wrote to record";
 				}
 			});
 
 			put("delete record", new CommandHandler() {
 
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 
 					Matcher m = p.matcher(value);
 
 					try {
 						for (Record r : records) {
 							if (m.matches()
 									&& r.getId() == Integer.parseInt(m.group(1))) {
 								r.delete(entity);
 							}
 						}
 					} catch (AccessControlException e) {
 						return "Access Denied";
 					}
 
 					return "Deleted record";
 				}
 			});
 
 			put("create record", new CommandHandler() {
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 
 					Matcher m = p.matcher(value);
 
 					try {
 						if (m.matches()) {
 							Patient pat = (Patient) findEntity(m.group(1));
 							Nurse n = (Nurse) findEntity(m.group(2));
 
 							if (pat == null || n == null) {
 								throw new InvalidParameterException();
 							}
 
 							if (currentEntityUser.getType() != EntityType.Doctor)
 								throw new AccessControlException(
 										"Current user is not allowed to create records");
 
 							Record r = createJournal(pat,
 									(Doctor) currentEntityUser, n);
 
 							if (currentEntityUser.canAccess(r,
 									EntityWithAccessControl.EXECUTE)) {
 								records.add(r);
 							}
 						}
 					} catch (AccessControlException e) {
 						return "Access Denied";
 					} catch (ClassCastException e) {
 						return "Invalid parameters";
 					} catch (InvalidParameterException e) {
 						return "Invalid parameters";
 					}
 
 					return "Created record";
 				}
 			});
 
 			put("assign nurse", new CommandHandler() {
 				@Override
 				public String handleCommand(Entity entity, Pattern p,
 						String value) {
 					System.out.println(String.format("Handling [%s] ...",
 							p.pattern()));
 
 					Matcher m = p.matcher(value);
 
 					try {
 						if (m.matches()) {
 							Nurse n = (Nurse) findEntity(m.group(1));
 							Patient pat = (Patient) findEntity(m.group(2));
 
 							if (pat == null || n == null) {
 								throw new InvalidParameterException();
 							}
 
 							if (currentEntityUser.getType() != EntityType.Doctor)
 								throw new AccessControlException(
 										"Current user is not allowed to assign nurses");
 
 							for (Record r : records) {
 								if (r.getPatient().getId() == pat.getId()) {
 									r.setNurse((Doctor) currentEntityUser, n);
 								}
 							}
 						}
 					} catch (AccessControlException e) {
 						return "Access Denied";
 					} catch (ClassCastException e) {
 						return "Invalid parameters";
 					} catch (InvalidParameterException e) {
 						return "Invalid parameters";
 					}
 
 					return "Assigned nurse";
 				}
 			});
 
 		}
 	};
 	private HashMap<String, Pattern> commands;
 
 	private String handleCommand(Entity entity, String command, Pattern p,
 			String value) {
 		System.out.println(String.format("Handling command [%s] for [#%d, %s]",
 				command, entity.getId(), entity.getName()));
 		return m.get(command).handleCommand(entity, p, value);
 	}
 
 	/*
 	 * Creates private medical journal for a patient. In the journal the
 	 * assigned doctor, who treated the patient, and assisted nurse are store as
 	 * well.
 	 */
 	public static Record createJournal(Patient patient, Doctor doctor,
 			Nurse nurse) throws InvalidParameterException {
 		// Checks if all entities are in the same division
 		if (patient.getDivision() != nurse.getDivision()
 				&& nurse.getDivision() != doctor.getDivision()) {
 			// logs in false case
 			log.updateLog(new LogEvent(
 					Log.LVL_ERROR,
 					"RUNTIME ERROR",
 					String.format(
 							"Patient #%d, Nurse #%d, Doctor #%d not in same division",
 							patient.getId(), nurse.getId(), doctor.getId())));
 
 			throw new InvalidParameterException(
 					String.format(
 							"Nurse [%s] is not from the same division [%s] as doctor [%s]",
 							nurse.getName(), patient.getDivision().getId(),
 							doctor.getName()));
 		}
 
 		// If they are create new medical journal
 		Record record = new Record(patient, doctor, nurse, patient.getData());
 
 		// logs in true case
 		log.updateLog(new EntityAccessLogEvent(doctor, record,
 				EntityWithAccessControl.EXECUTE));
 
 		return record;
 	}
 
 	public String readData(Record record, Entity entity)
 			throws AccessControlException {
 		try {
 			return record.readData(entity);
 		} catch (AccessControlException e) {
 
 			// logs in false case
 			log.updateLog(new EntityAccessDeniedLogEvent(entity, record,
 					EntityWithAccessControl.READ));
 			e.printStackTrace();
 		}
 		// logs in true case
 		log.updateLog(new EntityAccessLogEvent(entity, record,
 				EntityWithAccessControl.READ));
 		return null;
 	}
 
 	public void writeData(Record record, Entity entity, String data)
 			throws AccessControlException {
 		try {
 			record.writeData(entity, data);
 		} catch (AccessControlException e) {
 			// logs in false case
 			log.updateLog(new EntityAccessDeniedLogEvent(entity, record,
 					EntityWithAccessControl.READ));
 			e.printStackTrace();
 		}
 		// logs in true case
 		log.updateLog(new EntityAccessLogEvent(entity, record,
 				EntityWithAccessControl.READ));
 	}
 
 	/*
 	 * Deletes the medical journal
 	 */
 	public void deleteJournal(Record record, Entity entity)
 			throws AccessControlException {
 		try {
 			record.delete(entity);
 		} catch (AccessControlException e) {
 			// logs in false case
 			log.updateLog(new EntityAccessDeniedLogEvent(entity, record,
 					EntityWithAccessControl.READ));
 			e.printStackTrace();
 		}
 		// logs in true case
 		log.updateLog(new EntityAccessLogEvent(entity, record,
 				EntityWithAccessControl.READ));
 	}
 
 	/*
 	 * Prints out information about a newly connected client
 	 */
 	private static void printSocketInfo(SSLSocket s) {
 		System.out.println("-> New client connecting:");
 		System.out.println("Socket class: " + s.getClass());
 		System.out.println("   Remote address = "
 				+ s.getInetAddress().toString());
 		System.out.println("   Remote port = " + s.getPort());
 		System.out.println("   Local socket address = "
 				+ s.getLocalSocketAddress().toString());
 		System.out.println("   Local address = "
 				+ s.getLocalAddress().toString());
 		System.out.println("   Local port = " + s.getLocalPort());
 		System.out.println("   Need client authentication = "
 				+ s.getNeedClientAuth());
 		SSLSession ss = s.getSession();
 		System.out.println("   Cipher suite = " + ss.getCipherSuite());
 		System.out.println("   Protocol = " + ss.getProtocol());
 	}
 }
