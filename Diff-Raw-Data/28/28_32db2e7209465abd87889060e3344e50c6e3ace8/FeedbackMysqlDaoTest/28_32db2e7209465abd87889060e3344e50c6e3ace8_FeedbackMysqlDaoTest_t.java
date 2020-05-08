 package org.weso.acota.persistence.mysql;
 
 import static org.junit.Assert.assertEquals;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Timestamp;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.dbunit.database.DatabaseConfig;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.ext.mysql.MySqlDataTypeFactory;
 import org.dbunit.operation.DatabaseOperation;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.weso.acota.core.FeedbackConfiguration;
 import org.weso.acota.core.entity.Feedback;
 import org.weso.acota.persistence.FeedbackDAO;
 
 public class FeedbackMysqlDaoTest {
 
 	protected FeedbackDAO feedbackDao;
 	
 	@Before
 	public void init() throws Exception {
 		DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
 		this.feedbackDao = new FeedbackMysqlDAO();
 	}
 
 	@After
 	public void after() throws Exception {
 		DatabaseOperation.DELETE_ALL.execute(getConnection(), getDataSet());
 	}
 
 	protected IDatabaseConnection getConnection() throws Exception {
 		Class.forName("com.mysql.jdbc.Driver");
		FeedbackConfiguration configuration = new FeedbackConfiguration();
 		Connection jdbcConnection = DriverManager.getConnection(
				"jdbc:mysql://"+ configuration.getDatabaseUrl() +"/"+configuration.getDatabaseName(), configuration.getDatabaseUser(), configuration.getDatabasePassword());
 		
 		IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
 		
 		DatabaseConfig dbConfig = connection.getConfig();
 		dbConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
 		
 		return connection;
 	}
 
 	protected DatabaseOperation getSetUpOperation() throws Exception {
 		return DatabaseOperation.REFRESH;
 	}
 
 	protected IDataSet getDataSet() throws Exception {
 		return new FlatXmlDataSetBuilder().build(this.getClass().getResource(
 				"/resources/dbunit.xml"));
 	}
 	
 	@Test
 	public void saveFeedbackTest() throws Exception {
 		Feedback feedback = new Feedback(4, 4, "demo", "http://www.example.es",
 				new Timestamp(1351099447000L));
 		feedbackDao.saveFeedback(feedback);
 		
         IDataSet databaseDataSet = getConnection().createDataSet();
         ITable actualTable = databaseDataSet.getTable("feedbacks");
 
         IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(this.getClass().getResource(
 				"/resources/dbunitExpected.xml"));
         ITable expectedTable = expectedDataSet.getTable("feedbacks");
 
         assertEquals(expectedTable.getRowCount(), actualTable.getRowCount());
 	
 	}
 
 	@Test
 	public void getAllFeedbacksTest() throws Exception {
 		Set<Feedback> feedbacks = new HashSet<Feedback>();
 		feedbacks.add(new Feedback(3, 2, "group", "http://www.example.es",
 				new Timestamp(1351099447000L)));
 		feedbacks.add(new Feedback(1, 1, "research", "http://www.weso.es",
 				new Timestamp(1351099447000L)));
 		feedbacks.add(new Feedback(2, 2, "research", "http://www.weso.es",
 				new Timestamp(1351099447000L)));
 		assertEquals(feedbacks, feedbackDao.getAllFeedbacks());
 	}
 
 	@Test
 	public void getAllFeedbacksByUserIdEmpty() throws Exception {
 		assertEquals(Collections.EMPTY_SET, feedbackDao.getFeedbacksByUserId(0));
 	}
 
 	@Test
 	public void getAllFeedbacksByUserIdTest() throws Exception {
 		Set<Feedback> feedbacks = new HashSet<Feedback>();
 		feedbacks.add(new Feedback(1, 1, "research", "http://www.weso.es",
 				new Timestamp(1351099447000L)));
 
 		assertEquals(feedbacks, feedbackDao.getFeedbacksByUserId(1));
 	}
 
 	@Test
 	public void getAllFeedbacksByLabelEmpty() throws Exception {
 		assertEquals(Collections.EMPTY_SET,
 				feedbackDao.getFeedbacksByLabel("nolabel"));
 	}
 
 	@Test
 	public void getAllFeedbacksByLabelTest() throws Exception {
 		Set<Feedback> feedbacks = new HashSet<Feedback>();
 		feedbacks.add(new Feedback(1, 1, "research", "http://www.weso.es",
 				new Timestamp(1351099447000L)));
 		feedbacks.add(new Feedback(2, 2, "research", "http://www.weso.es",
 				new Timestamp(1351099447000L)));
 
 		assertEquals(feedbacks, feedbackDao.getFeedbacksByLabel("research"));
 	}
 
 	@Test
 	public void getAllFeedbacksByDocumentEmpty() throws Exception {
 		assertEquals(Collections.EMPTY_SET,
 				feedbackDao.getFeedbacksByDocument("http://www.nodocument.es"));
 	}
 
 	@Test
 	public void getAllFeedbacksByDocumentTest() throws Exception {
 		Set<Feedback> feedbacks = new HashSet<Feedback>();
 		feedbacks.add(new Feedback(3, 2, "group", "http://www.example.es",
 				new Timestamp(1351099447000L)));
 
 		assertEquals(feedbacks,
 				feedbackDao.getFeedbacksByDocument("http://www.example.es"));
 	}
 	
 	@Test
 	public void closeConnectionNullTest() throws Exception {
 		feedbackDao.closeConnection(null);
 	}
 	
 	@Test
 	public void closeResultnNullTest() throws Exception {
 		feedbackDao.closeResult(null);
 	}
 	
 	@Test
 	public void closeStatementNullTest() throws Exception {
 		feedbackDao.closeStatement(null);
 	}
 }
