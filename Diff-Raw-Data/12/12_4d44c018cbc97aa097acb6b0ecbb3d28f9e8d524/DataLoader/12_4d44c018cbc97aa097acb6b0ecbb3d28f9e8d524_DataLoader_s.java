 package BackEnd;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Set;
 
 public class DataLoader {
 
 	private static ArrayList<Film> filmSet = new ArrayList();
 	private static ArrayList<Album> albumSet = new ArrayList();
 	private static ArrayList<Audiobook> audiobookSet = new ArrayList();
 	private static boolean UpdatedList = false;
 	private static int sumtotal = 0;
 	private static int maxID = 0;
 	private static HashMap<Integer, Item> itemSet = new HashMap<>();
 	private static HashMap<String, User> users = new HashMap<>();
 
 	public static ArrayList<Film> getFilm() {
 		if (!UpdatedList) {
 			UpdateLists();
 		}
 		return filmSet;
 	}
 
 	public static ArrayList<Album> getAlbums() {
 		if (!UpdatedList) {
 			UpdateLists();
 		}
 		return albumSet;
 	}
 
 	public static ArrayList<Audiobook> getAudiobooks() {
 		if (!UpdatedList) {
 			UpdateLists();
 		}
 		return audiobookSet;
 	}
 
 	public static int getUserCount() {
 		return users.size();
 	}
 
 	private static void UpdateLists() {
 		Iterator<Integer> i = itemSet.keySet().iterator();
 		while (i.hasNext()) {
 			Item item = itemSet.get(i.next());
 			if (item instanceof Film && !filmSet.contains((Film) item) /*&& !item.isVisible()*/) {
 				filmSet.add((Film) item);
 			} else if (item instanceof Album && !albumSet.contains((Album) item) /*&& !item.isVisible()*/) {
 				albumSet.add((Album) item);
 			} else if (item instanceof Audiobook && !audiobookSet.contains((Audiobook) item) /*&& !item.isVisible()*/) {
 				audiobookSet.add((Audiobook) item);
 			}
 		}
 		UpdatedList = true;
 	}
 
 	public static Item getItemById(int index) {
 		Integer id = new Integer(index);
 		// return items after search
 		if (itemSet.containsKey(id)) {
 			return itemSet.get(id);
 		}
 		return null;
 	}
 
 	public static HashMap getItemSet() {
 		return itemSet;
 	}
 
 	public static HashMap getUsers() {
 		return users;
 	}
 	private static Connection SqlConnection;
 	private static ResultSet Results;
 	private static DatabaseMetaData SqlMetaData;
 	private static Statement SqlStatement;
 
 	public static void loadFromFile() {
 		System.out.println("Loading...");
 		try {
 			// Create instance of the driver for the database
 			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
 
 			//Connect to the database.
 			SqlConnection = DriverManager.getConnection("jdbc:derby://localhost:1527/group2mediastoredb;create=true", "root", "root");
 
 			//Create statement object to communicate.
 			SqlStatement = SqlConnection.createStatement();
 
 
 			SqlMetaData = SqlConnection.getMetaData();
 			String[] t = {"TABLE"};
 			Results = SqlMetaData.getTables(null, null, "%", t);
 			if (!Results.next()) {
 				System.out.println("Initialization Needed.");
 
 				InputStream InFile = DataLoader.class.getResourceAsStream("InitSql.sql");
 				//InputStreamReader InFileReader = new InputStreamReader(InFile);
 				BufferedReader Reader = new BufferedReader(new InputStreamReader(InFile));
 				StringBuilder StringBuilder = new StringBuilder();
 				String line;
 				line = Reader.readLine();
 				String SqlCommand;
 				while (line != null) {
 					StringBuilder.append(line);
 
 					// Once a full command is found (meaning it ends in a semicolon), execute that command.
 					if (line.contains(";")) {
 						SqlCommand = StringBuilder.toString();
 						StringBuilder = new StringBuilder();
 
 						// Remove the semicolon.
 						SqlCommand = SqlCommand.substring(0, SqlCommand.indexOf(';'));
 
 						// If the command is a select, use execute query. Otherwise, use execute.
 						if (SqlCommand.toUpperCase().contains("SELECT")) {
 							SqlStatement.executeQuery(SqlCommand);
 						} else {
 							SqlStatement.execute(SqlCommand);
 						}
 					}
 					line = Reader.readLine();
 				}
 				InFile.close();
 			}
 
 			Results = SqlStatement.executeQuery("select * from users");
 			while (Results.next()) {
 				User x = new User(Results.getString("username"), Results.getString("password"),
 						Results.getString("billing_name"), Results.getString("address"), Results.getFloat("credit"),
 						Results.getBoolean("administrator"));
 				ArrayList<Integer> purchases = new ArrayList();
 				ArrayList<Integer> ratings = new ArrayList();
 				String read = Results.getString("purchase_history");
 				Pattern pattern = Pattern.compile("\\d+");
 				Matcher matcher = pattern.matcher(read);
 				while (matcher.find()) {
 					purchases.add(Integer.parseInt(matcher.group()));
 				}
 				x.setPurchaseHistory(purchases);
 				read = Results.getString("ratings");
 				matcher = pattern.matcher(read);
 				while (matcher.find()) {
 					ratings.add(Integer.parseInt(matcher.group()));
 				}
 				x.setRatings(ratings);
 				System.out.println("Loading user: " + Results.getString("username") + ", Password: " + Results.getString("password"));
 				users.put(Results.getString("username"), x);
 			}
 
 			Results = SqlStatement.executeQuery("select SUM(NUMBER_SOLD) as sumtotal from ROOT.ITEMS");
 			while (Results.next()) {
 				sumtotal = Results.getInt("sumtotal");
 			}
 			Results = SqlStatement.executeQuery("select MAX(ID) as maxID from ROOT.ITEMS");
 			while (Results.next()) {
 				maxID = Results.getInt("maxID");
 			}
 
 			Results = SqlStatement.executeQuery("select * from items");
 			while (Results.next()) {
 				if ("Album".equals(Results.getString("item_type"))) {
 					System.out.println("\tLoading album: " + Results.getString("item_name"));
 					Album newAlbum = new Album();
 					newAlbum.dataLoaderInit(Results.getInt("id"), Results.getString("item_name"), Results.getInt("year_of_release"), Results.getInt("duration"),
 							Results.getString("genre"), Results.getString("preview"), Results.getInt("number_sold"), Results.getInt("price"), Results.getBoolean("hidden"),
 							Results.getInt("cumulative_ratings"), Results.getInt("num_ratings"), Results.getString("creator"), sumtotal, maxID);
 					DataLoader.addItemToList(newAlbum);
 				} else if ("Audiobook".equals(Results.getString("item_type"))) {
 					System.out.println("\tLoading audiobook: " + Results.getString("item_name"));
 					Audiobook newAudio = new Audiobook();
 					newAudio.dataLoaderInit(Results.getInt("id"), Results.getString("item_name"), Results.getInt("year_of_release"), Results.getInt("duration"),
 							Results.getString("genre"), Results.getString("preview"), Results.getInt("number_sold"), Results.getInt("price"), Results.getBoolean("hidden"),
 							Results.getInt("cumulative_ratings"), Results.getInt("num_ratings"), Results.getString("creator"), sumtotal, maxID);
 					DataLoader.addItemToList(newAudio);
 				} else if ("Film".equals(Results.getString("item_type"))) {
 					System.out.println("\tLoading film: " + Results.getString("item_name"));
 					Film newFilm = new Film();
 					newFilm.dataLoaderInit(Results.getInt("id"), Results.getString("item_name"), Results.getInt("year_of_release"), Results.getInt("duration"),
 							Results.getString("genre"), Results.getString("preview"), Results.getInt("number_sold"), Results.getInt("price"), Results.getBoolean("hidden"),
 							Results.getInt("cumulative_ratings"), Results.getInt("num_ratings"), Results.getString("creator"), sumtotal, maxID);
 					DataLoader.addItemToList(newFilm);
 				}
 			}
 		} catch (SQLException e) {
 			System.out.println("Problem with SQL.");
			System.out.println("\tSQLException: " + e.getMessage());
			e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Unforeseen Exception.");
 			System.out.println("\t" + e.getClass().getSimpleName() + ": " + e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void saveToFile() {
 		try {
 			System.out.println("Saving...");
 			Set<String> iterableSet = users.keySet();
 			Iterator<String> i = iterableSet.iterator();
 			while (i.hasNext()) {
 				User x = users.get(i.next());
 				PreparedStatement preparedStatement = null;
 				String strQuery = "update users set username = ? , password = ?, billing_name = ?, address = ?,credit = ?, administrator = ?, purchase_history = ?, ratings = ? "
 						+ "where username =?";
 				preparedStatement = SqlConnection.prepareStatement(strQuery);
 				preparedStatement.setString(1, x.getUsername());
 				preparedStatement.setString(2, x.getPassword());
 				preparedStatement.setString(3, x.getName());
 				preparedStatement.setString(4, x.getAddress());
 				preparedStatement.setDouble(5, x.getCredit());
 				preparedStatement.setBoolean(6, x.getAdministrator());
 				String purchasesString = "";
 				ArrayList<Integer> purchases = x.getPurchaseHistory();
 				for (Integer n : purchases) {
 					purchasesString = purchasesString + n + ",";
 				}
 				preparedStatement.setString(7, purchasesString);
 				String ratingsString = "";
 				ArrayList<Integer> ratings = x.getRatings();
 				for (Integer n : ratings) {
 					ratingsString = ratingsString + n + ",";
 				}
 				preparedStatement.setString(8, ratingsString);
 				preparedStatement.setString(9, x.getUsername());
 				if (preparedStatement.executeUpdate() == 0) {
 					preparedStatement = null;
 					strQuery = "insert into users (username, password, billing_name, address, credit, administrator, purchase_history, ratings) values (?,?,?,?,?,?,?,?)";
 					preparedStatement = SqlConnection.prepareStatement(strQuery);
 					preparedStatement.setString(1, x.getUsername());
 					preparedStatement.setString(2, x.getPassword());
 					preparedStatement.setString(3, x.getName());
 					preparedStatement.setString(4, x.getAddress());
 					preparedStatement.setDouble(5, x.getCredit());
 					preparedStatement.setBoolean(6, x.getAdministrator());
 					purchasesString = "";
 					purchases = x.getPurchaseHistory();
 					for (Integer n : purchases) {
 						purchasesString = purchasesString + n + ",";
 					}
 					preparedStatement.setString(7, purchasesString);
 					ratingsString = "";
 					ratings = x.getRatings();
 					for (Integer n : ratings) {
 						ratingsString = ratingsString + n + ",";
 					}
 					preparedStatement.setString(8, ratingsString);
 					preparedStatement.setString(7, "");
 					preparedStatement.setString(8, "");
 					preparedStatement.executeUpdate();
 				} else {
 					preparedStatement.executeUpdate();
 				}
 
 			}
 			HashMap<Integer, Item> set = DataLoader.itemSet;
 			Set<Integer> iterableSetItem = set.keySet();
 			Iterator<Integer> iItem = iterableSetItem.iterator();
 			while (iItem.hasNext()) {
 				Item xItem = set.get(iItem.next());
 				PreparedStatement preparedStatement = null;
 				String strQuery = "update items set item_name = ?, id = ?, year_of_release =  ?, duration = ?, "
 						+ "genre = ?, preview = ?, number_sold = ?, price = ?, hidden = ?, cumulative_ratings = ?,"
 						+ " num_ratings = ?, creator = ?, item_type = ? where id =?";
 				preparedStatement = SqlConnection.prepareStatement(strQuery);
 				preparedStatement.setString(1, xItem.getName());
 				preparedStatement.setInt(2, xItem.getId());
 				preparedStatement.setInt(3, xItem.getYearOfRelease());
 				preparedStatement.setInt(4, xItem.getTotalDuration());
 				preparedStatement.setString(5, xItem.getGenre());
 				preparedStatement.setString(6, xItem.getPreview());
 				preparedStatement.setInt(7, xItem.getNumSold());
 				preparedStatement.setDouble(8, xItem.getPrice());
 				preparedStatement.setBoolean(9, xItem.isVisible());
 				preparedStatement.setInt(10, xItem.getCumulativeRating());
 				preparedStatement.setInt(11, xItem.getNumRatings());
 				preparedStatement.setString(12, xItem.getCreator());
 				preparedStatement.setString(13, (xItem instanceof Album) ? "Album" : ((xItem instanceof Film) ? "Film" : "Audiobook"));
 				preparedStatement.setInt(14, xItem.getId());
 				if (preparedStatement.executeUpdate() == 0) {
 					preparedStatement = null;
 					strQuery = "insert into items (item_name, id, year_of_release, duration, genre, preview, number_sold, price, hidden, cumulative_ratings, "
 							+ "num_ratings, creator, item_type) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
 					preparedStatement = SqlConnection.prepareStatement(strQuery);
 					preparedStatement.setString(1, xItem.getName());
 					preparedStatement.setInt(2, xItem.getId());
 					preparedStatement.setInt(3, xItem.getYearOfRelease());
 					preparedStatement.setInt(4, xItem.getTotalDuration());
 					preparedStatement.setString(5, xItem.getGenre());
 					preparedStatement.setString(6, xItem.getPreview());
 					preparedStatement.setInt(7, xItem.getNumSold());
 					preparedStatement.setDouble(8, xItem.getPrice());
 					preparedStatement.setBoolean(9, xItem.isVisible());
 					preparedStatement.setInt(10, xItem.getCumulativeRating());
 					preparedStatement.setInt(11, xItem.getNumRatings());
 					preparedStatement.setString(12, xItem.getCreator());
 					preparedStatement.setString(13, (xItem instanceof Album) ? "Album" : ((xItem instanceof Film) ? "Film" : "Audiobook"));
 					preparedStatement.executeUpdate();
 				} else {
 					preparedStatement.executeUpdate();
 				}
 			}
 			Results.close();
 			SqlStatement.close();
 			SqlConnection.close();
 		} catch (Exception e) {
			System.out.println("Problem Closing SQL Database: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace();
 		}
 	}
 
 	public static User getUserFromUsername(String username, String password) {
 		String lCaseUsername = username.toLowerCase();
 		if (users.containsKey(lCaseUsername)) {
 			User x = users.get(lCaseUsername);
 			if (x.checkPassword(password)) {
 				return x;
 			}
 		}
 		return null;
 	}
 
 	public static User getUserFromUsername(String username, User administrator) {
 		String lCaseUsername = username.toLowerCase();
 		if (administrator.getAdministrator() && users.containsKey(lCaseUsername)) {
 			return users.get(lCaseUsername);
 		}
 		return null;
 	}
 
 	public static ArrayList<Item> searchForItemArtist(String artist) {
 		artist = artist.toLowerCase(); // Compare everything in the lower case.
 		ArrayList<Item> results = new ArrayList(); // Results set.
 		HashMap<Integer, Item> set = DataLoader.itemSet; // All possible.
 		Set<Integer> iterableSet = set.keySet();
 		Iterator<Integer> i = iterableSet.iterator();
 		String name;
 		while (i.hasNext()) {
 			Item x = set.get(i.next());
 			name = x.getCreator().toLowerCase();
 			if (!x.isVisible() && ((artist.indexOf(name) != -1) || (name.indexOf(artist) != -1))) {
 				results.add(x);
 			}
 		}
 		return results;
 	}
 
 	public static ArrayList<Item> searchForItemTitle(String title) {
 		title = title.toLowerCase(); // Compare everything in the lower case.
 		ArrayList<Item> results = new ArrayList(); // Results set.
 		HashMap<Integer, Item> set = DataLoader.itemSet; // All possible.
 		Set<Integer> iterableSet = set.keySet();
 		Iterator<Integer> i = iterableSet.iterator();
 		String name;
 		while (i.hasNext()) {
 			Item x = set.get(i.next());
 			name = x.getName().toLowerCase();
 			if (!x.isVisible() && ((title.indexOf(name) != -1) || (name.indexOf(title) != -1))) {
 				results.add(x);
 			}
 		}
 		return results;
 	}
 
 	public static boolean addItemToList(Item newItem) {
 		Integer id = new Integer(newItem.getId());
 		if (!itemSet.containsKey(id)) {
 			itemSet.put(id, newItem);
 			UpdatedList = false;
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean addUserToList(User newUser) {
 		String s = newUser.getUsername().toLowerCase();
 		if (!users.containsKey(s)) {
 			users.put(s, newUser);
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean removeUserFromList(User user) {
 		try {
 			String s = user.getUsername().toLowerCase();
 			if (users.containsKey(s)) {
 				System.out.println("delete user: " + s);
 				String strQuery = "delete from users where username =?";
 				PreparedStatement preparedStatement = SqlConnection.prepareStatement(strQuery);
 				preparedStatement.setString(1, s);
 				preparedStatement.executeUpdate();
 				users.remove(s);
 				return true;
 			}
 		} catch (Exception e) {
 			System.out.println("Problem deleting user: " + e.getClass().getSimpleName() + ": " + e.getMessage());
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static double getSales() {
 		double sales = 0.0;
 		Iterator<Integer> i = itemSet.keySet().iterator();
 		while (i.hasNext()) {
 			Item a = itemSet.get(i.next());
 			if (a != null) {
 				sales += a.getPrice() * a.getNumSold();
 			}
 		}
 		return sales;
 	}
 }
