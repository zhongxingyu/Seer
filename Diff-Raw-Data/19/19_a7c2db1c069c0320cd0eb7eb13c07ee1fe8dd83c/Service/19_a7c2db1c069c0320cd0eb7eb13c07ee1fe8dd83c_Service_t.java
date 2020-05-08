 package data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import config.GuiConfig;
 
 /**
  * If user field is null, then this offer is inactive, otherwise it's active. If
  * time is 0, this offer is expired. If price is 0, this offer is a demand,
  * otherwise it's supply.
  * 
  * A service that have one of the statuses : TRANSFER_* should have only one
  * user entry, he will be the user who accepted the transfer, the rest of the
  * users specified in <code>users</code> field will be ignored.
  * 
  * @author Ghennadi Procopciuc
  * @see Status
  */
 public class Service implements Comparable<Service> {
 	private String					name;
 	private long					time;
 	private double					price;
 	private ArrayList<UserEntry>	users;
 	private Status					status;
 
 	/**
 	 * This field will be used only with a TRANSFER_* status, otherwise it means
 	 * nothing.
 	 */
 	private int						progress;
 
 	/**
 	 * Service status :
 	 * 
 	 * INACTIVE - if no one offers this service
 	 * 
 	 * ACTIVE - if someone offers this service
 	 * 
 	 * TRANSFER_* - Transaction statuses
 	 * 
 	 */
 	public enum Status {
		ACTIVE, INACTIVE, TRANSFER_STARTED, TRANSFER_IN_PROGRESS, TRANSFER_COMPLETE, TRANSFER_FAILED, DROP
 	};
 
 	public Service(String name, ArrayList<UserEntry> users, Status status) {
 		this.name = name;
 		this.users = users;
 		this.time = 0;
 		this.price = 0;
 		this.status = status;
 	}
 
 	public Service(Service service) {
 		this.name = service.getName();
 		this.users = service.getUsers();
 		this.time = service.getTime();
 		this.price = service.getPrice();
 		this.status = service.getStatus();
 	}
 
 	public Service(String serviceName) {
 		this(serviceName, null, Status.INACTIVE);
 	}
 
 	/**
 	 * Note, if <code>status</code> is not <code>Status.INACTIVE</code>, after
 	 * instantiation you have to add at least one
 	 * <code>UserEntry<code>, otherwise you will get unpredictable results
 	 * 
 	 * @param serviceName
 	 *            Name of the service
 	 * @param status
 	 *            Service/Transaction status
 	 * @see Status
 	 */
 	public Service(String serviceName, Status status) {
 		this(serviceName, null, status);
 	}
 
 	public Service(String name, long time, double price, ArrayList<UserEntry> users, Status status,
 			int progress) {
 		super();
 		this.name = name;
 		this.time = time;
 		this.price = price;
 		this.users = users;
 		this.status = status;
 		this.progress = progress;
 	}
 
 	public void addUserEntry(UserEntry user) {
 		if (users == null) {
 			users = new ArrayList<UserEntry>();
 		}
 
 		users.add(user);
 	}
 
 	public ArrayList<UserEntry> getUsers() {
 		return users;
 	}
 
 	public void setUsers(ArrayList<UserEntry> users) {
 		this.users = users;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String serviceName) {
 		this.name = serviceName;
 	}
 
 	public long getTime() {
 		return time;
 	}
 
 	public void setTime(long time) {
 		this.time = time;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public void setStatus(Status status) {
 		this.status = status;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public int getProgress() {
 		return progress;
 	}
 
 	public void setProgress(int progress) {
 		this.progress = progress;
 	}
 
 	public ArrayList<ArrayList<Object>> getAsTable() {
 		ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
 		Boolean first = true;
 
 		/**
 		 * Columns from row are : Service Name, Status, User, Offer made, Time,
 		 * Price
 		 */
 		ArrayList<Object> row;
 
 		switch (status) {
 		case INACTIVE:
 			row = new ArrayList<Object>(Arrays.asList(getName(),
 					GuiConfig.getValue(GuiConfig.INACTIVE), "", "", "", ""));
 			data.add(row);
 			break;
 		case ACTIVE:
 			if (users == null) {
 				row = new ArrayList<Object>(Arrays.asList(getName(),
 						GuiConfig.getValue(GuiConfig.ACTIVE), "", "", "", ""));
 				data.add(row);
 				break;
 			}
 			for (UserEntry user : users) {
 				row = new ArrayList<Object>();
 				if (first) {
 					first = false;
 
 					row = new ArrayList<Object>(Arrays.asList(getName(),
 							GuiConfig.getValue(GuiConfig.ACTIVE), user.getName(), user.getOffer(),
 							user.getTime(), user.getPrice()));
 				} else {
 					row = new ArrayList<Object>(Arrays.asList("", "", user.getName(),
 							user.getOffer(), user.getTime(), user.getPrice()));
 				}
 
 				data.add(row);
 			}
 			break;
 		case TRANSFER_IN_PROGRESS:
 			row = new ArrayList<Object>(Arrays.asList(getName(),
 					GuiConfig.getValue(GuiConfig.TRANSFER_IN_PROGRESS), progress, "", "", ""));
 			data.add(row);
 			break;
 		case TRANSFER_STARTED:
 			row = new ArrayList<Object>(Arrays.asList(getName(),
 					GuiConfig.getValue(GuiConfig.TRANSFER_STARTED), 0, "", "", ""));
 			data.add(row);
 			break;
 		case TRANSFER_COMPLETE:
 			row = new ArrayList<Object>(Arrays.asList(getName(),
 					GuiConfig.getValue(GuiConfig.TRANSFER_COMPLETE), progress, "", "", ""));
 			data.add(row);
 			break;
 		case TRANSFER_FAILED:
 			// TODO
 			break;
 		default:
 			System.err.println("[Service, getAsTable] Unexpected Status :|");
 			break;
 		}
 
 		return data;
 	}
 
 	public Service clone() {
 		return new Service(name, time, price, users, status, progress);
 	}
 
 	@Override
 	public boolean equals(Object arg0) {
 		if (!(arg0 instanceof Service)) {
 			return false;
 		}
 
 		return ((Service) arg0).getName().equals(name);
 	}
 
 	@Override
 	public String toString() {
 		return "" + name + " " + users;
 	}
 
 	@Override
 	public int compareTo(Service o) {
 		return getName().compareTo(o.getName());
 	}
 }
