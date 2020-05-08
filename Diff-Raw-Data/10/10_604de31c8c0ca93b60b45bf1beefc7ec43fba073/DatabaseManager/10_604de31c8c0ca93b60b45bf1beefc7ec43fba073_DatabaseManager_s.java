 package poker;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.javatuples.Pair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import poker.entities.Estimate;
 import poker.entities.Story;
 import poker.entities.Task;
 import poker.entities.UnitType;
 import poker.entities.User;
 import poker.entities.UserEstimate;
 
 public class DatabaseManager {
 	private static final String	JDBC_SQLITE_POKER_DB	= "jdbc:sqlite::memory:";
 	private SimpleDateFormat	dateFormat				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	private OutputStream		debug;
 	private boolean				debugging				= false;
 	private Connection			connection				= null;
 
 	private void debug(String msg) {
 		if (debug == null) {
 			System.out.println(msg);
 			return;
 		}
 
 		try {
 			debug.write(msg.getBytes(Charset.forName("UTF-8")));
 			debug.write('\n');
 			debug.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public DatabaseManager(OutputStream stream) {
 		init();
 		this.debug = stream;
 	}
 
 	public synchronized void init() {
 		try {
 			connection = DriverManager.getConnection(JDBC_SQLITE_POKER_DB);
 
 			createTables(connection);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void createTables(Connection connection) throws Exception {
 		Statement statement = connection.createStatement();
 		statement.setQueryTimeout(30); // timeout after 30 seconds
 
 		// this table will store the individual tasks
 		statement.execute("drop table if exists tasks");
 		statement.execute("create table tasks ( " + "id integer primary key autoincrement, "
 		// task's name
 				+ "name text, "
 				// task's description
 				+ "description text, "
 				// When it was created
 				+ "created_at datetime DEFAULT (datetime('now', 'localtime')), "
 				// When it was published
 				+ "published_at datetime " + ")");
 
 		// This table will store the individual estimations created by each
 		// task
 		statement.execute("drop table if exists estimations");
 		statement.execute("create table estimations (" + "id integer primary key autoincrement, "
 		// task.id
 				+ "task_id integer, "
 				// the complexity symbol (can be special, like coffee mug) [what
 				// will be shown on the card]
 				+ "complexity_symbol text, "
 				// the unit, we should create an enum for this that uses the
 				// same integer values
 				+ "unit integer DEFAULT 1, "
 				// the value of this complexity in its unit
 				+ "unit_value REAL " + ")");
 
 		// This table will store the individual users
 		statement.execute("drop table if exists users");
 		statement.execute("create table users ( " + "id integer primary key autoincrement, "
 		// user's name
 				+ "name text)");
 
 		// This table will store the team of users for each task
 		statement.execute("drop table if exists task_team");
 		statement.execute("create table task_team ( " + "id integer primary key autoincrement, "
 		// users.id
 				+ "user_id integer, "
 				// task.id
 				+ "task_id integer " + ")");
 
 		// This table will store the individual stories
 		statement.execute("drop table if exists stories");
 		statement.execute("create table stories ( " + "id integer primary key autoincrement, "
 		// tasks.id
 				+ "task_id integer, "
 				// story's name
 				+ "name text, "
 				// story's description
 				+ "description text, "
 				// will be set to the final value after consensus has been
 				// reached
 				+ "consensus integer DEFAULT -1,"
 				// current iteration, so we can figure out consensus and how
 				// long it took etc. etc.
 				+ "iteration integer DEFAULT 0)");
 
 		// This table will store the estimations for each user
 		statement.execute("drop table if exists story_user_estimations");
 		statement.execute("create table story_user_estimations ( " + "id integer primary key autoincrement, "
 		// story.id
 				+ "story_id integer, "
 				// users.id
 				+ "user_id integer, "
 				// estimations.id
 				+ "estimation_id integer, "
 				// the value of the current iteration of the story id when
 				// insert (so we can keep track of during which iteration the
 				// estimate was made)
 				+ "story_iteration integer" + ")");
 
 		if (true) {
 			insertUser(new User("Bengt"));
 			insertUser(new User("Soheil"));
 			insertUser(new User("Alexander"));
 			insertUser(new User("Anders"));
 			insertUser(new User("Daniel"));
 
 			insertTask(new Task("Planning Poker", "Implement Planning Poker"));
 			insertTask(new Task("Write manual", "Write the manual for our implementation of PLanning Poker"));
 
 			createFibonacciEstimations(1);
 			createFibonacciEstimations(2);
 
 			insertStory(new Story(1, "Database Operations", "Implement the database operations"));
 			insertStory(new Story(1, "Write templates", "Implement the templates for the different pages of the game"));
 			insertStory(new Story(1, "Set up the routes", "Create the rotues for the different pages of the game"));
 
 			insertStory(new Story(2, "Create LaTeX document", "Create the LaTeX document"));
 
 			addUserToTask(1, 1);
 			addUserToTask(1, 2);
 			addUserToTask(1, 3);
 
 			addUserToTask(2, 4);
 			addUserToTask(2, 5);
 
 			// addEstimateToStory(1, 1, 4);
 			// addEstimateToStory(1, 2, 4);
 			// addEstimateToStory(1, 2, 3);
 		}
 	}
 
 	public synchronized void createFibonacciEstimations(int task_id) {
 		insertEstimate(new Estimate(task_id, "0", UnitType.PERSON_DAYS, 0));
 		insertEstimate(new Estimate(task_id, "1/2", UnitType.PERSON_DAYS, 0.5f));
 		insertEstimate(new Estimate(task_id, "1", UnitType.PERSON_DAYS, 1f));
 		insertEstimate(new Estimate(task_id, "2", UnitType.PERSON_DAYS, 2f));
 		insertEstimate(new Estimate(task_id, "3", UnitType.PERSON_DAYS, 3f));
 		insertEstimate(new Estimate(task_id, "5", UnitType.PERSON_DAYS, 5f));
 		insertEstimate(new Estimate(task_id, "8", UnitType.PERSON_DAYS, 8f));
 		insertEstimate(new Estimate(task_id, "13", UnitType.PERSON_DAYS, 13f));
 		insertEstimate(new Estimate(task_id, "20", UnitType.PERSON_DAYS, 20f));
 		insertEstimate(new Estimate(task_id, "40", UnitType.PERSON_DAYS, 40f));
 		insertEstimate(new Estimate(task_id, "100", UnitType.PERSON_DAYS, 100f));
 		insertEstimate(new Estimate(task_id, "?", UnitType.PERSON_DAYS, -1));
 		insertEstimate(new Estimate(task_id, "coffee", UnitType.PERSON_DAYS, -1));
 	}
 
 	public synchronized Task getTask(int id) {
 		Task task = null;
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT id, name, description, datetime(created_at), datetime(published_at) FROM tasks where id=? LIMIT 1");
 			ps.setInt(1, id);
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				try {
 					task = new Task(res.getInt("id"), res.getString("name"), res.getString("description"),
 							new java.sql.Date(dateFormat.parse(res.getString("datetime(created_at)")).getTime()),
 							(res.getString("datetime(published_at)")) == null ? null : new java.sql.Date(dateFormat
 									.parse(res.getString("datetime(published_at)")).getTime()));
 					// debug("Fetching task: " + task.toString());
 				} catch (ParseException e) {
 					System.err.println("Error parsing tasks.created_at using task id: " + res.getInt("id"));
 					e.printStackTrace();
 				}
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return task;
 	}
 
 	public synchronized void setTask(Task task) {
 		try {
 
 			PreparedStatement ps = connection
					.prepareStatement("UPDATE tasks set name=?, created_at=?, published_at=? where id=?");
 			ps.setString(1, task.getName());
			ps.setDate(2, task.getCreatedAt());
			ps.setDate(3, task.getPublishedAt());
			ps.setInt(4, task.getId());
 
 			// debug("Setting task: " + task.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized int insertTask(Task task) {
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("INSERT into tasks (name, description) values (?,?)");
 			ps.setString(1, task.getName());
 			ps.setString(2, task.getDescription());
 
 			// debug("Inserting task: " + task.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return getLatestTask();
 	}
 
 	public synchronized void deleteTask(int id) {
 		try {
 
 			// Clean stories
 			List<Story> stories = getStoriesFromTask(id);
 			for (Story story : stories) {
 				deleteStory(story.getId());
 			}
 
 			// Clean users
 			List<User> users = getUsersFromTask(id);
 			for (User user : users) {
 				deleteUserFromTask(id, user.getId());
 			}
 
 			PreparedStatement ps = connection.prepareStatement("DELETE FROM tasks where id=?");
 			ps.setInt(1, id);
 
 			// debug("Deleting task id: " + id);
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized Story getStory(int id) {
 		Story story = null;
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT id, task_id, name, description, consensus, iteration FROM stories where id=? LIMIT 1");
 			ps.setInt(1, id);
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				story = new Story(res.getInt("id"), res.getInt("task_id"), res.getString("name"),
 						res.getString("description"), res.getInt("consensus"), res.getInt("iteration"));
 
 				// debug("Fetching story: " + story.toString());
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return story;
 	}
 
 	public synchronized void setStory(Story story) {
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("UPDATE stories SET name=?, description=?, consensus=?, iteration=? where id=?");
 			ps.setString(1, story.getName());
 			ps.setString(2, story.getDescription());
 			ps.setInt(3, story.getConsensus());
 			ps.setInt(4, story.getIteration());
 			ps.setInt(5, story.getId());
 
 			// debug("Setting story: " + story.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized int insertStory(Story story) {
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("INSERT into stories (task_id, name, description) values (?,?,?)");
 			ps.setInt(1, story.getTaskId());
 			ps.setString(2, story.getName());
 			ps.setString(3, story.getDescription());
 
 			// debug("Insering story: " + story.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return getLatestStory();
 	}
 
 	public synchronized void deleteStory(int id) {
 		try {
 
 			HashMap<User, List<Estimate>> storyEstimates = getEstimatesFromStory(id);
 
 			// Delete all story estimates
 			for (List<Estimate> estimates : storyEstimates.values()) {
 				for (Estimate estimate : estimates) {
 					deleteEstimateFromStory(id, estimate.getId());
 				}
 			}
 
 			if (connection.isClosed()) {
 				System.out.println("Connection is still closed..");
 			}
 
 			Statement ps = connection.createStatement();
 			if (ps.execute("DELETE FROM stories WHERE id=" + id)) {
 				// debug("deleted story " + id + " successfully");
 			}
 
 			// debug("Deleting story with id: " + id);
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized User getUser(int id) {
 		User user = null;
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id, name FROM users where id=? LIMIT 1");
 			ps.setInt(1, id);
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				user = new User(res.getInt("id"), res.getString("name"));
 				// debug("Fetching user: " + user.toString());
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return user;
 	}
 
 	public synchronized int insertUser(User user) {
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("INSERT into users (name) values (?)");
 			ps.setString(1, user.getName());
 
 			// debug("Inserting user: " + user.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return getLatestUser();
 	}
 
 	public synchronized void deleteUser(int id) {
 		try {
 
 			// Should delete from task_team
 			PreparedStatement ps = connection.prepareStatement("DELETE FROM task_team where user_id=?");
 			ps.setInt(1, id);
 
 			// debug("Deleting user from all tasks: " + id);
 
 			ps.executeUpdate();
 
 			// Should delete from story_user_estimations
 			ps = connection.prepareStatement("DELETE FROM story_user_estimations where user_id=?");
 			ps.setInt(1, id);
 
 			// debug("Deleting all estimations for user: " + id);
 
 			ps.executeUpdate();
 
 			// Delete user at last
 			ps = connection.prepareStatement("DELETE FROM users where id=?");
 			ps.setInt(1, id);
 
 			// debug("Deleting user with id: " + id);
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized void setUser(User user) {
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("UPDATE users SET name=? where id=?");
 			ps.setString(1, user.getName());
 			ps.setInt(2, user.getId());
 
 			// debug("Setting user: " + user.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized Estimate getEstimate(int id) {
 		Estimate estimate = null;
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT id, task_id, complexity_symbol, unit, unit_value FROM estimations where id=? LIMIT 1");
 			ps.setInt(1, id);
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				estimate = new Estimate(res.getInt("id"), res.getInt("task_id"), res.getString("complexity_symbol"),
 						UnitType.values()[res.getInt("unit")], res.getInt("unit_value"));
 				// debug("Fetching estimate: " + estimate.toString());
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return estimate;
 	}
 
 	public synchronized void setEstimate(Estimate estimate) {
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("UPDATE estimations SET complexity_symbol=?, unit=?, unit_value=? where id=?");
 			ps.setString(1, estimate.getComplexitySymbol());
 			ps.setInt(2, estimate.getUnit().getCode());
 			ps.setFloat(3, estimate.getUnitValue());
 			ps.setInt(4, estimate.getId());
 
 			// debug("Setting estimate: " + estimate.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized int insertEstimate(Estimate estimate) {
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("INSERT into estimations (task_id, complexity_symbol, unit, unit_value) values (?,?,?,?)");
 			ps.setInt(1, estimate.getTaskId());
 			ps.setString(2, estimate.getComplexitySymbol());
 			ps.setInt(3, estimate.getUnit().getCode());
 			ps.setFloat(4, estimate.getUnitValue());
 
 			// debug("Inserting estimate: " + estimate.toString());
 
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return getLatestEstimate();
 	}
 
 	public synchronized void deleteEstimate(int id) {
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("DELETE FROM estimations where id=?");
 			ps.setInt(1, id);
 
 			// debug("Deleting estimate with id: " + id);
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public synchronized List<Story> getStoriesFromTask(int task_id) {
 		List<Story> stories = new ArrayList<Story>();
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT id, task_id, name, description, consensus, iteration FROM stories where task_id=?");
 			ps.setInt(1, task_id);
 
 			Story story = null;
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				story = new Story(res.getInt("id"), res.getInt("task_id"), res.getString("name"),
 						res.getString("description"), res.getInt("consensus"), res.getInt("iteration"));
 				stories.add(story);
 				// debug("Fetching story: " + story.toString());
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return stories;
 	}
 
 	public synchronized List<User> getUsersFromTask(int task_id) {
 		List<User> users = new ArrayList<User>();
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT users.id, users.name FROM users JOIN task_team ON users.id=task_team.user_id WHERE task_team.task_id=?");
 			ps.setInt(1, task_id);
 
 			User user = null;
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				user = new User(res.getInt("id"), res.getString("name"));
 				users.add(user);
 				// debug("Fetching user: " + user.toString());
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return users;
 	}
 
 	public synchronized HashMap<User, List<Estimate>> getEstimatesFromStory(int story_id) {
 		HashMap<User, List<Estimate>> storyEstimations = new HashMap<User, List<Estimate>>();
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT estimations.id, task_id, complexity_symbol, unit, unit_value FROM estimations where id=? LIMIT 1");
 			ps.setInt(1, story_id);
 
 			List<User> users = getUsersFromTask(getStory(story_id).getTaskId());
 
 			for (User user : users) {
 				storyEstimations.put(user, getEstimatesFromUser(user.getId()));
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return storyEstimations;
 	}
 
 	public synchronized List<Estimate> getEstimatesFromUser(int user_id) {
 		List<Estimate> estimations = new ArrayList<Estimate>();
 
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT estimations.id, estimations.task_id, estimations.complexity_symbol, estimations.unit, "
 							+ "estimations.unit_value FROM estimations join story_user_estimations on story_user_estimations.estimation_id=estimations.id where user_id=?");
 			ps.setInt(1, user_id);
 
 			Estimate estimate = null;
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				estimate = new Estimate(res.getInt("id"), res.getInt("task_id"), res.getString("complexity_symbol"),
 						UnitType.values()[res.getInt("unit")], res.getInt("unit_value"));
 				// debug("Fetching estimate: " + estimate.toString());
 				estimations.add(estimate);
 			}
 
 			;
 
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		return estimations;
 	}
 
 	public synchronized void deleteEstimateFromStory(int story_id, int estimate_id) {
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("DELETE FROM story_user_estimations where story_id = ? and estimate_id=?");
 			ps.setInt(1, story_id);
 			ps.setInt(2, estimate_id);
 
 			//debug(String.format("Deleting estimate [%d] from story [%d]", estimate_id, story_id));
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void deleteUserFromTask(int task_id, int user_id) {
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("DELETE FROM task_team where task_id = ? and user_id=?");
 			ps.setInt(1, task_id);
 			ps.setInt(2, user_id);
 
 			//debug(String.format("Deleting user [%d] from task [%d]", user_id, task_id));
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void addUserToTask(int task_id, int user_id) {
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("INSERT INTO task_team (user_id, task_id) VALUES (?,?)");
 			ps.setInt(1, user_id);
 			ps.setInt(2, task_id);
 
 			// debug("Adding user [" + user_id + "] to task [" + task_id + "]");
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void addEstimateToStory(int story_id, int user_id, int estimate_id) {
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("INSERT INTO story_user_estimations (story_id, user_id, estimation_id, story_iteration) VALUES (?,?,?,?)");
 			ps.setInt(1, story_id);
 			ps.setInt(2, user_id);
 			ps.setInt(3, estimate_id);
 			ps.setInt(4, getStory(story_id).getIteration());
 
 			// debug("Adding estimate [" + estimate_id + "] to story [" +
 			// story_id + "] for user [" + user_id + "]");
 			ps.executeUpdate();
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public synchronized List<Estimate> getEstimationsForTask(int task_id) {
 		List<Estimate> estimations = new ArrayList<Estimate>();
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("SELECT id, task_id, complexity_symbol, unit, unit_value from estimations where task_id = ? ");
 			ps.setInt(1, task_id);
 
 			// debug("Fetching estimations for task with id: " + task_id);
 
 			Estimate estimate = null;
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				estimate = new Estimate(res.getInt("id"), res.getInt("task_id"), res.getString("complexity_symbol"),
 						UnitType.values()[res.getInt("unit") - 1], res.getInt("unit_value"));
 				// debug("Fetching estimate: " + estimate.toString());
 				estimations.add(estimate);
 			}
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return estimations;
 	}
 
 	public synchronized List<Task> getTasks() {
 		List<Task> tasks = new ArrayList<Task>();
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id from tasks ORDER BY id ASC");
 
 			// debug("Getting all tasks");
 
 			ResultSet res = ps.executeQuery();
 			while (res.next()) {
 				tasks.add(getTask(res.getInt("id")));
 			}
 			;
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 		}
 
 		return tasks;
 	}
 
 	private int getLatestTask() {
 		int id = Integer.MIN_VALUE;
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id from tasks ORDER BY id DESC LIMIT 1");
 
 			ResultSet res = ps.executeQuery();
 			while (res.next()) {
 				//debug(String.format("Getting newest task [%d]", res.getInt("id")));
 				id = res.getInt("id");
 			}
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 
 	private int getLatestStory() {
 		int id = Integer.MIN_VALUE;
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id from stories ORDER BY id DESC LIMIT 1");
 
 			ResultSet res = ps.executeQuery();
 			while (res.next()) {
 				//debug(String.format("Getting newest story [%d]", res.getInt("id")));
 				id = res.getInt("id");
 			}
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 
 	private int getLatestUser() {
 		int id = Integer.MIN_VALUE;
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id from users ORDER BY id DESC LIMIT 1");
 
 			ResultSet res = ps.executeQuery();
 			while (res.next()) {
 				//debug(String.format("Getting newest user [%d]", res.getInt("id")));
 				id = res.getInt("id");
 			}
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 
 	private int getLatestEstimate() {
 		int id = Integer.MIN_VALUE;
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id from estimations ORDER BY id DESC LIMIT 1");
 
 			ResultSet res = ps.executeQuery();
 			while (res.next()) {
 				//debug(String.format("Getting newest estimate [%d]", res.getInt("id")));
 				id = res.getInt("id");
 			}
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 
 	public synchronized int getLatestIteration(int story_id) {
 		int iteration = -1;
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT iteration FROM stories WHERE id=?");
 			ps.setInt(1, story_id);
 
 			//debug(String.format("Fetching latest iteration for story [%d]", story_id));
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				iteration = res.getInt("iteration");
 			}
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return iteration;
 	}
 
 	public synchronized void increaseStoryIteration(int story_id) {
 
 		try {
 
 			PreparedStatement ps = connection
 					.prepareStatement("UPDATE stories SET iteration=((select iteration from stories where id=? order by iteration desc limit 1)+1) where id=?");
 			ps.setInt(1, story_id);
 			ps.setInt(2, story_id);
 
 			//debug(String.format("Increasing iteration for story [%d]", story_id));
 
 			ps.executeUpdate();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public synchronized List<UserEstimate> getUserEstimatesForStoryWithIteration(int story_id, int iteration) {
 		List<UserEstimate> estimations = new ArrayList<UserEstimate>();
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("select e.id as 'estimate_id', sue.user_id as 'user_id'"
 					+ "from story_user_estimations sue " + "inner join estimations e on sue.estimation_id=e.id "
 					+ "where sue.story_id=? and sue.story_iteration=? " + "order by sue.user_id asc");
 
 			ps.setInt(1, story_id);
 			ps.setInt(2, iteration);
 
 			//debug(String.format("Fetching user estimates from story [%d] with iteration [%d]", story_id, iteration));
 
 			Estimate estimate = null;
 			User user = null;
 
 			ResultSet res = ps.executeQuery();
 
 			while (res.next()) {
 				estimate = getEstimate(res.getInt("estimate_id"));
 				user = getUser(res.getInt("user_id"));
 
 				estimations.add(new UserEstimate(user, estimate));
 			}
 
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return estimations;
 	}
 
 	public synchronized List<UserEstimate> getLatestEstimatesForStory(int story_id) {
 		return getUserEstimatesForStoryWithIteration(story_id, getLatestIteration(story_id));
 	}
 
 	public synchronized List<User> getUsers() {
 		List<User> users = new ArrayList<User>();
 		try {
 
 			PreparedStatement ps = connection.prepareStatement("SELECT id from users ORDER BY id ASC");
 
 			// debug("Getting all users");
 
 			ResultSet res = ps.executeQuery();
 			while (res.next()) {
 				users.add(getUser(res.getInt("id")));
 			}
 			;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return users;
 	}
 
 }
