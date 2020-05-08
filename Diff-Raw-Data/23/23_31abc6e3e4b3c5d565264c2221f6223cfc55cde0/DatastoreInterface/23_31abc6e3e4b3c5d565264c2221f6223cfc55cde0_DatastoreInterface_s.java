 package ch.ethz.inf.dbproject.model;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ch.ethz.inf.dbproject.database.DatabaseConnection;
 import ch.ethz.inf.utils.StringUtils;
 
 /**
  * This class should be the interface between the web application and the database. Keeping all the
  * data-access methods here will be very helpful for part 2 of the project.
  */
 public final class DatastoreInterface {
 	private static final Logger logger = Logger.getLogger(DatastoreInterface.class.getName());
 
 	private static final String SELECT_ALL_CATEGORIES = "SELECT * FROM category";
 	private static final String SELECT_CATEGORY_BY_ID_PREP = "SELECT * FROM category WHERE id = ?";
 	private PreparedStatement categoryById;
 
 	private static final String SELECT_ALL_CITIES = "SELECT * FROM city";
 	private static final String SELECT_CITY_BY_ID_PREP = "SELECT * FROM city WHERE id = ?";
 	private PreparedStatement cityById;
 
 	private static final String SELECT_ALL_PROJECTS = "SELECT * FROM project";
 
	private static final String SELECT_PROJECTS_BY_NAME_PREP = "select p.id, p.title, p.description, p.goal, "
			+ "p.start_date, p.end_date, c.name as city, cat.name as category from project p "
			+ "inner join city c on p.city_id = c.id inner join category cat on p.category_id = cat.id "
			+ "where p.title like ?";
 	private PreparedStatement projectsByName;
 
 	private static final String SELECT_PROJECTS_BY_CATEGORY_PREP = "SELECT p.id, p.title, p.description, p.goal, "
 			+ "p.start_date, p.end_date, p.category_id, p.city_id, p.owner_id, c.name AS city, cat.name AS category "
 			+ "FROM project p INNER JOIN city c ON p.city_id = c.id INNER JOIN category cat ON p.category_id = cat.id "
 			+ "WHERE cat.name LIKE ?";
 	private PreparedStatement projectsByCategory;
 
 	private static final String SELECT_PROJECTS_BY_CITY_PREP = "select p.id, p.title, p.description, p.goal, "
			+ "p.start_date, p.end_date, c.name as city, cat.name as category from project p "
			+ "inner join city c on p.city_id = c.id inner join category cat on p.category_id = cat.id "
			+ "where c.name like ?";
 	private PreparedStatement projectsByCity;
 
	private static final String SELECT_FUNDS_BY_USER = "select p.title, fa.amount, fa.reward from user u "
			+ "inner join funds f on u.id = f.user_id inner join funding_amount fa on f.fa_id = fa.id "
			+ "inner join project p on fa.project_id = p.id where u.id = ?";
 	private PreparedStatement fundsByUser;
 
 	/**
 	 * {@link PreparedStatement} for retrieving the funding amounts for a single project
 	 */
 	private static final String SELECT_FUNDING_AMOUNT_PREP = "SELECT * FROM funding_amount WHERE project_id = ?";
 	private PreparedStatement faByProject;
 
 	/**
 	 * {@link PreparedStatement} for retrieving a single project
 	 */
 	private static final String SELECT_PROJECY_BY_ID_PREP = "SELECT * FROM project WHERE id = ?";
 	private PreparedStatement projectById;
 
 	/**
 	 * {@link PreparedStatement} for retrieving a single user
 	 */
 	private static final String SELECT_USER_BY_NAME_PREP = "SELECT * FROM user WHERE username = ?";
 	private PreparedStatement userByName;
 
 	private static final String SELECT_USER_BY_ID_PREP = "SELECT * FROM user WHERE id = ?";
 	private PreparedStatement userById;
 
 	private static final String INSERT_USER_PREP = "INSERT INTO user (username, password) VALUES (?, ?)";
 	private PreparedStatement insertUser;
 
 	private static final String INSERT_FUND_PREP = "INSERT INTO funds (user_id, fa_id) values(?, ?)";
 	private PreparedStatement insertFund;
 
 	private static final String INSERT_CITY_PREP = "INSERT INTO city (name) VALUES (?)";
 	private PreparedStatement insertCity;
 
 	private static final String INSERT_CATEGORY_PREP = "INSERT INTO category (name) VALUES (?)";
 	private PreparedStatement insertCategory;
 
 	private static final String INSERT_PROJECT_PREP = "INSERT INTO project (title, description, goal, start_date, "
 			+ "end_date, city_id, category_id, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
 	private PreparedStatement insertProject;
 
 	private static final String SELECT_COMMENTS_BY_PROJECT_ID = "SELECT * "
 			+ "FROM comment c INNER JOIN user u ON c.user_id = u.id "
 			+ "WHERE c.project_id = ? "
 			+ "ORDER BY c.id ASC";
 	private PreparedStatement commentsByProjectId;
 
 	private static final String INSERT_COMMENT_PREP = "INSERT INTO comment(text, create_date, user_id, project_id)"
 			+ "VALUES(?, ?, ?, ?)";
 	private PreparedStatement insertComment;
 
 	/** Specify the days we want to show */
 	private static final int DAYS_TILL_ENDING = 10;
 	private static final String SELECT_SOON_ENDING_PROJECTS_PREP = "SELECT * FROM project WHERE end_date BETWEEN ? "
 			+ "AND ? ORDER BY end_date";
 	private PreparedStatement selectSoonEndingProjects;
 
 	private static final int MOST_FUNDED_PROJECT_COUNT = 10;
 	private static final String SELECT_MOST_FUNDED_PROJECTS = "SELECT p.*, SUM(amount) AS total_amount "
 			+ "FROM project p INNER JOIN (funding_amount fa INNER JOIN funds f ON fa.id = f.fa_id) ON p.id = fa.project_id "
 			+ "GROUP BY project_id "
 			+ "ORDER BY SUM(amount) DESC "
 			+ "LIMIT " + MOST_FUNDED_PROJECT_COUNT;
 
 	private static final int MOST_POPULAR_PROJECTS_COUNT = 10;
 	private static final String SELECT_MOST_POPULAR_PROJECTS = "SELECT p.*, COUNT(DISTINCT user_id) AS user_count "
 			+ "FROM project p INNER JOIN (funding_amount fa INNER JOIN funds f ON fa.id = f.fa_id) ON p.id = fa.project_id "
 			+ "GROUP BY project_id "
 			+ "ORDER BY COUNT(DISTINCT user_id) DESC "
 			+ "LIMIT " + MOST_POPULAR_PROJECTS_COUNT;
 	
 	private static final String INSERT_FUNDING_AMOUNT_PREP = "INSERT INTO funding_amount(amount, reward, project_id) "
 			+ "VALUES(?, ?, ?)";
 	private PreparedStatement insertFundingAmount;
 
 	private static final String SELECT_STRETCHED_GOALS_PREP = "SELECT * FROM stretched_goal WHERE project_id = ? "
 			+ "ORDER BY goal";
 	private PreparedStatement selectStretchedGoals;
 
 	private static final String INSERT_STRETCHED_GOAL_PREP = "INSERT INTO stretched_goal(goal, bonus, project_id) "
 			+ "VALUES(?, ?, ?)";
 	private PreparedStatement insertStretchedGoal;
 
 	private Connection sqlConnection = null;
 
 	public DatastoreInterface() {
 		// get the connection to the DB
 		try {
 			this.sqlConnection = DatabaseConnection.getMySQLInstance().getConnection();
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Errro while connection to DB!", e);
 		}
 
 		// in here we specify all the prepared statements we need
 		try {
 			this.faByProject = sqlConnection.prepareStatement(SELECT_FUNDING_AMOUNT_PREP);
 			this.projectById = sqlConnection.prepareStatement(SELECT_PROJECY_BY_ID_PREP);
 			this.userByName = sqlConnection.prepareStatement(SELECT_USER_BY_NAME_PREP);
 			this.insertUser = sqlConnection.prepareStatement(INSERT_USER_PREP);
 			this.projectsByName = sqlConnection.prepareStatement(SELECT_PROJECTS_BY_NAME_PREP);
 			this.projectsByCategory = sqlConnection.prepareStatement(SELECT_PROJECTS_BY_CATEGORY_PREP);
 			this.projectsByCity = sqlConnection.prepareStatement(SELECT_PROJECTS_BY_CITY_PREP);
 			this.fundsByUser = sqlConnection.prepareStatement(SELECT_FUNDS_BY_USER);
 			this.insertFund = sqlConnection.prepareStatement(INSERT_FUND_PREP);
 			this.insertCity = sqlConnection.prepareStatement(INSERT_CITY_PREP, PreparedStatement.RETURN_GENERATED_KEYS);
 			this.insertCategory = sqlConnection.prepareStatement(INSERT_CATEGORY_PREP,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			this.insertProject = sqlConnection.prepareStatement(INSERT_PROJECT_PREP,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			this.categoryById = sqlConnection.prepareStatement(SELECT_CATEGORY_BY_ID_PREP);
 			this.userById = sqlConnection.prepareStatement(SELECT_USER_BY_ID_PREP);
 			this.cityById = sqlConnection.prepareStatement(SELECT_CITY_BY_ID_PREP);
 			this.commentsByProjectId = sqlConnection.prepareStatement(SELECT_COMMENTS_BY_PROJECT_ID);
 			this.insertComment = sqlConnection.prepareStatement(INSERT_COMMENT_PREP,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			this.selectSoonEndingProjects = sqlConnection.prepareStatement(SELECT_SOON_ENDING_PROJECTS_PREP);
 			this.insertFundingAmount = sqlConnection.prepareStatement(INSERT_FUNDING_AMOUNT_PREP,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			this.selectStretchedGoals = sqlConnection.prepareStatement(SELECT_STRETCHED_GOALS_PREP);
 			this.insertStretchedGoal = sqlConnection.prepareStatement(INSERT_STRETCHED_GOAL_PREP,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to create prepared statements", e);
 		}
 	}
 
 	public final Project getProjectById(final int id) {
 		Project res = null;
 
 		try {
 			projectById.setInt(1, id);
 
 			try (ResultSet rs = projectById.executeQuery();) {
 				if (rs.next()) {
 					res = new Project(rs);
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve Project with id = '" + id + "'.", e);
 		}
 
 		return res;
 	}
 
 	public final List<Project> getAllProjects() {
 		List<Project> projects = new ArrayList<>();
 
 		try (Statement stmt = this.sqlConnection.createStatement();
 				ResultSet rs = stmt.executeQuery(SELECT_ALL_PROJECTS);) {
 			while (rs.next()) {
 				projects.add(new Project(rs));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the projects!", e);
 		}
 
 		return projects;
 	}
 
 	/**
 	 * Returns a list of {@link Project}s that have a similar name
 	 */
 	public List<Project> getProjectsByName(String name) {
 		return retrieveProjects(projectsByName, name);
 	}
 
 	/**
 	 * @return {@link Project} with the identical name or null
 	 */
 	public Project getProjectByName(String name) {
 		Project res = null;
 		if (StringUtils.isNotNullNorEmpty(name)) {
 			for (Project p : getProjectsByName(name)) {
 				// here we only care about the identical names
 				if (name.equals(p.getTitle())) {
 					res = p;
 					break;
 				}
 			}
 		}
 		return res;
 	}
 
 	public List<Project> getProjectsByCategory(String name) {
 		return retrieveProjects(projectsByCategory, name);
 	}
 
 	public List<Project> getProjectsByCity(String name) {
 		return retrieveProjects(projectsByCity, name);
 	}
 
 	private List<Project> retrieveProjects(PreparedStatement retrieveProjectsStatement, String name) {
 		List<Project> projects = new ArrayList<>();
 		try {
 			retrieveProjectsStatement.setString(1, "%" + name + "%");
 			ResultSet resultSet = retrieveProjectsStatement.executeQuery();
 			while (resultSet.next()) {
 				projects.add(new Project(resultSet));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve projects", e);
 		}
 		return projects;
 	}
 
 	public List<FundingAmount> getAmountsOfProject(int projectId) {
 		List<FundingAmount> res = new ArrayList<>();
 
 		// set the projectId into the prepared statement
 		try {
 			faByProject.setInt(1, projectId);
 
 			try (ResultSet rs = faByProject.executeQuery();) {
 				while (rs.next()) {
 					FundingAmount fundingAmount = new FundingAmount();
 					fundingAmount.setId(rs.getInt("id"));
 					fundingAmount.setAmount(rs.getBigDecimal("amount"));
 					fundingAmount.setReward(rs.getString("reward"));
 
 					Project project = new Project();
 					project.setId(rs.getInt("project_id"));
 					fundingAmount.setProject(project);
 					res.add(fundingAmount);
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, e.getMessage(), e);
 		}
 		return res;
 	}
 
 	public User getUserBy(String name) {
 		User user = null;
 		try {
 			userByName.setString(1, name);
 			ResultSet resultSet = userByName.executeQuery();
 			if (resultSet.first()) {
 				user = new User();
 				user.setId(resultSet.getInt("id"));
 				user.setUsername(resultSet.getString("username"));
 				user.setPassword(resultSet.getString("password"));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve user by name", e);
 		}
 		return user;
 	}
 
 	public User createUser(String username, String password) {
 		try {
 			insertUser.setString(1, username);
 			insertUser.setString(2, password);
 			insertUser.executeUpdate();
 			return getUserBy(username);
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to insert new user", e);
 		}
 		return null;
 	}
 
 	public List<FundingAmount> getFundsByUserId(int id) {
 		List<FundingAmount> fundingAmounts = new ArrayList<>();
 		try {
 			fundsByUser.setInt(1, id);
 			ResultSet resultSet = fundsByUser.executeQuery();
 			while (resultSet.next()) {
 				FundingAmount fundingAmount = new FundingAmount();
 				fundingAmount.setAmount(resultSet.getBigDecimal("amount"));
 				fundingAmount.setReward(resultSet.getString("reward"));
 
 				Project project = new Project();
 				project.setTitle(resultSet.getString("title"));
 				fundingAmount.setProject(project);
 				fundingAmounts.add(fundingAmount);
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve user funds", e);
 		}
 		return fundingAmounts;
 	}
 
 	public void fundProject(int userId, int fundingAmountId) {
 		try {
 			insertFund.setInt(1, userId);
 			insertFund.setInt(2, fundingAmountId);
 			insertFund.executeUpdate();
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to insert fund with userId = " + userId + " and fundingAmountId = "
 					+ fundingAmountId, e);
 		}
 	}
 
 	public List<Category> getAllCategories() {
 		List<Category> categories = new ArrayList<>();
 
 		try (Statement stmt = this.sqlConnection.createStatement();
 				ResultSet rs = stmt.executeQuery(SELECT_ALL_CATEGORIES);) {
 			while (rs.next()) {
 				categories.add(new Category(rs));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the categories!", e);
 		}
 
 		return categories;
 	}
 
 	public List<City> getAllCities() {
 		List<City> cities = new ArrayList<>();
 
 		try (Statement stmt = this.sqlConnection.createStatement(); ResultSet rs = stmt.executeQuery(SELECT_ALL_CITIES);) {
 			while (rs.next()) {
 				cities.add(new City(rs));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the cities!", e);
 		}
 
 		return cities;
 	}
 
 	public City createCity(String name) {
 		City res = null;
 
 		try {
 			insertCity.setString(1, name);
 			if (insertCity.executeUpdate() == 0) {
 				throw new SQLException("ExecuteUpdate returned 0!");
 			}
 
 			// now we create the city including the generated ID
 			try (ResultSet rs = insertCity.getGeneratedKeys()) {
 				if (rs.next()) {
 					// first make sure that we have all the values needed for the class
 					// so if no ID is retrieved we will return null
 					City tmp = new City(name);
 					tmp.setId(rs.getInt(1));
 
 					res = tmp;
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to create the city \"" + name + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public Category createCategory(String name) {
 		Category res = null;
 
 		try {
 			insertCategory.setString(1, name);
 			if (insertCategory.executeUpdate() == 0) {
 				throw new SQLException("ExecuteUpdate returned 0!");
 			}
 
 			// now we create the category object including the generated ID
 			try (ResultSet rs = insertCategory.getGeneratedKeys()) {
 				if (rs.next()) {
 					// first make sure that we have all the needed values for the new category
 					// so if no ID is retrieved we will return null
 					Category tmp = new Category(name);
 					tmp.setId(rs.getInt(1));
 
 					res = tmp;
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to create the category \"" + name + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public Project createProject(Project project) {
 		Project res = null;
 
 		try {
 			insertProject.setString(1, project.getTitle());
 			insertProject.setString(2, project.getDescription());
 			insertProject.setBigDecimal(3, project.getGoal());
 			insertProject.setDate(4, new Date(project.getStartDate().getTime()));
 			insertProject.setDate(5, new Date(project.getEndDate().getTime()));
 			insertProject.setInt(6, project.getCity() != null ? project.getCity().getId() : null);
 			insertProject.setInt(7, project.getCategory() != null ? project.getCategory().getId() : null);
 			insertProject.setInt(8, project.getOwner() != null ? project.getOwner().getId() : null);
 
 			if (insertProject.executeUpdate() == 0) {
 				throw new SQLException("ExecuteUpdate returned 0!");
 			}
 
 			try (ResultSet rs = insertProject.getGeneratedKeys()) {
 				if (rs.next()) {
 					// when we are here we get the generated ID for the project and add it to the
 					// object we will return if the retrieval of the ID fails we will not return the
 					// project, to indicate that something went
 					project.setId(rs.getInt(1));
 					res = project;
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to create the project \"" + project.getTitle() + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public Category getCategoryById(int id) {
 		Category res = null;
 
 		try {
 			categoryById.setInt(1, id);
 
 			try (ResultSet rs = categoryById.executeQuery()) {
 				if (rs.next()) {
 					res = new Category(rs);
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the category with \"" + id + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public User getUserById(int id) {
 		User res = null;
 
 		try {
 			userById.setInt(1, id);
 
 			try (ResultSet rs = userById.executeQuery()) {
 				if (rs.next()) {
 					res = new User(rs);
 				}
 
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve user with \"" + id + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public City getCityById(int id) {
 		City res = null;
 
 		try {
 			cityById.setInt(1, id);
 
 			try (ResultSet rs = cityById.executeQuery()) {
 				if (rs.next()) {
 					res = new City(rs);
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve city with \"" + id + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public List<Comment> getCommentsByProjectId(int id) {
 		List<Comment> res = new ArrayList<>();
 
 		try {
 			commentsByProjectId.setInt(1, id);
 
 			try (ResultSet rs = commentsByProjectId.executeQuery()) {
 				while (rs.next()) {
 					res.add(new Comment(rs));
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve comments for project with id=\"" + id + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public Comment createComment(Comment comment) {
 		Comment res = null;
 
 		try {
 			insertComment.setString(1, comment.getComment());
 			insertComment.setTimestamp(2, new Timestamp(comment.getCreateDate().getTime()));
 			insertComment.setInt(3, comment.getUserId());
 			insertComment.setInt(4, comment.getProjectId());
 
 			if (insertComment.executeUpdate() == 0) {
 				throw new SQLException("ExecuteUpdate returned 0!");
 			}
 
 			try (ResultSet rs = insertComment.getGeneratedKeys()) {
 				if (rs.next()) {
 					comment.setId(rs.getInt(1));
 					// When we are here this means that the comment was successfully created and
 					// that we
 					// were able to retrieve the key, so we can return it
 					res = comment;
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to create new comment for project with id=\"" + comment.getProjectId()
 					+ "\"!", e);
 		}
 
 		return res;
 	}
 
 	public List<Project> getSoonEndingProjects() {
 		List<Project> res = new ArrayList<>();
 
 		try {
 			long diff = DAYS_TILL_ENDING * 24 * 60 * 60 * 1000;
 			selectSoonEndingProjects.setDate(1, new Date(System.currentTimeMillis()));
 			selectSoonEndingProjects.setDate(2, new Date(System.currentTimeMillis() + diff));
 
 			try (ResultSet rs = selectSoonEndingProjects.executeQuery()) {
 				while (rs.next()) {
 					res.add(new Project(rs));
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the soon ending Projects!", e);
 		}
 
 		return res;
 	}
 
 	public List<Project> getMostFundedProjects() {
 		List<Project> res = new ArrayList<>();
 
 		try (Statement stmt = sqlConnection.createStatement();
 				ResultSet rs = stmt.executeQuery(SELECT_MOST_FUNDED_PROJECTS)) {
 			while (rs.next()) {
 				Project p = new Project(rs);
 				p.setTotalAmount(rs.getBigDecimal("total_amount"));
 
 				res.add(p);
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the most popular projects!", e);
 		}
 
 		return res;
 	}
 
 	public List<Project> getMostPopularProjects() {
 		List<Project> res = new ArrayList<>();
 
 		try (Statement stmt = sqlConnection.createStatement();
 				ResultSet rs = stmt.executeQuery(SELECT_MOST_POPULAR_PROJECTS)) {
 			while (rs.next()) {
 				Project p = new Project(rs);
 				p.setUserCount(rs.getInt("user_count"));
 				res.add(p);
 
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the most popular projects!", e);
 		}
 		return res;
 	}
 
 	public FundingAmount createFundingAmount(FundingAmount fa) {
 		FundingAmount res = null;
 
 		try {
 			// fill the gaps in the statement
 			insertFundingAmount.setBigDecimal(1, fa.getAmount());
 			insertFundingAmount.setString(2, fa.getReward());
 			insertFundingAmount.setInt(3, fa.getProjectId());
 
 			if (insertFundingAmount.executeUpdate() == 0) {
 				throw new SQLException("ExecuteUpdate returned 0!");
 			}
 
 			// now retrieve the generated key
 			try (ResultSet rs = insertFundingAmount.getGeneratedKeys()) {
 				if (rs.next()) {
 					// first check that we really have the ID and then set it as a return value
 					fa.setId(rs.getInt(1));
 
 					res = fa;
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING,
 					"Failed to create a new funding amount for project with id=\"" + fa.getProjectId() + "\"!", e);
 		}
 
 		return res;
 	}
 
 	public StretchedGoals createStretchedGoal(StretchedGoals sg) {
 		StretchedGoals res = null;
 
 		try {
 			// fill the gaps in the statement
 			insertStretchedGoal.setBigDecimal(1, sg.getGoal());
 			insertStretchedGoal.setString(2, sg.getBonus());
 			insertStretchedGoal.setInt(3, sg.getProjectId());
 
 			if (insertStretchedGoal.executeUpdate() == 0) {
 				throw new SQLException("ExecuteUpdate returned 0!");
 			}
 
 			// now retrieve teh generated key
 			try (ResultSet rs = insertStretchedGoal.getGeneratedKeys()) {
 				if (rs.next()) {
 					// first try to get the generated ID and then make sure that the updated object
 					// will be returned
 					sg.setId(rs.getInt(1));
 
 					res = sg;
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING,
 					"Failed to retrieve the stretched goals for project with id=\"" + sg.getProjectId() + "\"!", e);
 		}
 		return res;
 	}
 
 	public List<StretchedGoals> getStretchedGoals(int projectId) {
 		List<StretchedGoals> res = new ArrayList<>();
 
 		try {
 			selectStretchedGoals.setInt(1, projectId);
 			try (ResultSet rs = selectStretchedGoals.executeQuery()) {
 				while (rs.next()) {
 					res.add(new StretchedGoals(rs));
 				}
 			}
 		} catch (SQLException e) {
 			logger.log(Level.WARNING, "Failed to retrieve the stretched goals for project with id=\"" + projectId
 					+ "\"!", e);
 		}
 
 		return res;
 	}
 }
