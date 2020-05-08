 package beans;
 
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 
 import objects.DatabaseTable;
 import objects.QueryResult;
 import objects.Question;
 import utilities.JDBC_Abstract_Connection;
 import utilities.JDBC_MySQL_Connection;
 import utilities.JDBC_PostgreSQL_Connection;
 
 import edu.gatech.sqltutor.IQueryTranslator;
 
 @ManagedBean
 @ViewScoped
 public class SampleNLPPageBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@ManagedProperty(value="#{userBean}")
 	private UserBean userBean;
 	private JDBC_Abstract_Connection connection;
 	private String selectedDatabase;
 	private List<DatabaseTable> tables;
 	private String query;
 	private String feedbackNLP;
 	private QueryResult queryResult;
 
 	@PostConstruct
 	public void init() {
 		String[] databaseAttributes;
 		if(getUserBean().getSelectedSchema() != null) {
 			databaseAttributes = getUserBean().getSelectedSchema().split(" ");
 		} else {
 			return; //eventually redirect to session expired page.
 		}
 		final String databaseConnector = databaseAttributes[0];
 		if(databaseConnector.equalsIgnoreCase("PostgreSQL")) {	
 			connection = new JDBC_PostgreSQL_Connection();
 		} else if (databaseConnector.equalsIgnoreCase("MySQL")) {
 			connection = new JDBC_MySQL_Connection();
 		} else {
 			return; //eventually redirect to message about connector not being supported
 		}
 		selectedDatabase = databaseAttributes[1];
 		tables = connection.getTables(selectedDatabase);
 		setPrepopulatedQuery();
 	}
 	
 	public void processSQL() {
 		try {
 			queryResult = connection.getQueryResult(selectedDatabase, query);
 			IQueryTranslator question = new Question(query, tables);
 			String nlp = question.getTranslation();
			feedbackNLP = "The question you answered was: \n" + nlp;
 		} catch(SQLException e) {
 			feedbackNLP = "Your query was malformed. Please try again.\n" + e.getMessage();
 		}
 	}
 	
 	public void setPrepopulatedQuery() {
 		if(selectedDatabase.equals("company")) {
 			query = "SELECT first_name, last_name, salary FROM employee";
 		} else if(selectedDatabase.equals("sales")) {
 			query = "SELECT CUST_NUM FROM customers";
 		} else {
 			query = "";
 		}
 	}
 	
 	public String getQuery() {
 		return query;
 	}
 	
 	public void setQuery(String query) {
 		this.query = query;
 	}
 
 	public QueryResult getQueryResult() {
 		return queryResult;
 	}
 
 	public UserBean getUserBean() {
 		return userBean;
 	}
 
 	public void setUserBean(UserBean userBean) {
 		this.userBean = userBean;
 	}
 
 	public String getFeedbackNLP() {
 		return feedbackNLP;
 	}
 	
 	public String getSelectedDatabase() {
 		return selectedDatabase;
 	}
 
 	public List<DatabaseTable> getTables() {
 		return tables;
 	}
 }
