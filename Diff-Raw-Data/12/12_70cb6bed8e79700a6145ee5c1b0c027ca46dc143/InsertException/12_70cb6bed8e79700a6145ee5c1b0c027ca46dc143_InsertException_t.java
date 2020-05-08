 package cz.cuni.mff.odcleanstore.engine.ws.scraper.ifaces;
 
 public class InsertException extends Exception {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final InsertException SERVICE_BUSY = new InsertException("Service busy", 1, "Service busy");
 	public static final InsertException BAD_CREDENTIALS = new InsertException("Bad credentials", 2, "Bad credentials");
 	public static final InsertException NOT_AUTHORIZED = new InsertException("Not authorized", 3, "Not authorized");
 	public static final InsertException DUPLICATED_UUID = new InsertException("Duplicated uuid", 4, "Duplicated uuid");
 	public static final InsertException UUID_BAD_FORMAT = new InsertException("Uuid bad format", 5, "Uuid bad format");
	public static final InsertException OTHER_ERROR = new InsertException("Other error", 7, "Other error");
	public static final InsertException FATAL_ERROR = new InsertException("Fatal error", 8, "Fatal error");
 
 	private int id;
 	private String moreInfo;
 
 	private InsertException(String message, int id, String moreInfo) {
 		super(message);
 		this.id = id;
 		this.moreInfo = moreInfo;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getMoreInfo() {
 		return moreInfo;
 	}
 }
