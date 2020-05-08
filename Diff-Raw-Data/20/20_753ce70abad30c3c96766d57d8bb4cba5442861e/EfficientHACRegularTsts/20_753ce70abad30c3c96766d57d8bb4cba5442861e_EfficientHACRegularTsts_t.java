 package sce.finalprojects.sceprojectbackend.algorithms;
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.xml.crypto.Data;
 
 import org.apache.poi.hssf.record.DBCellRecord;
 import org.junit.Before;
 import org.junit.Test;
 
 import sce.finalprojects.sceprojectbackend.database.DatabaseOperations;
 import sce.finalprojects.sceprojectbackend.datatypes.Acell;
 import sce.finalprojects.sceprojectbackend.datatypes.ArrayOfCommentsDO;
 import sce.finalprojects.sceprojectbackend.datatypes.Cluster;
 import sce.finalprojects.sceprojectbackend.datatypes.Comment;
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.factories.ArrayOfCommentsFactory;
 import sce.finalprojects.sceprojectbackend.managers.MaintenanceDataManager;
 import sce.finalprojects.sceprojectbackend.utils.MarkupUtility;
 
 
 
 
 
 public class EfficientHACRegularTsts {
 
 	private EfficientHAC efh;
 	private ArrayList<Comment> ArrayOfComment22s;
 	private double[] vectRep;
 	
 	//@Before
 	public void initGaacTests() throws SQLException{
 		
 		ArrayOfComments = DatabaseOperations.getAllComentsWithoutHTML("1");
 		vectRep = new double[9];
 	}
 	
 	
 	@Test
 	public void testTheAlgorithmIsRunning() throws Exception {
 		
 		this.efh = new EfficientHAC(ArrayOfComments, vectRep);
 		this.efh.runAlgorithm();
 		
 		assertEquals(true,efh.calculated);
 	}
 	
 	@Test 
 	public void testRunningAgainTheAlgorithmWithoutReset(){
 		
 		try {
 			this.efh = new EfficientHAC(ArrayOfComments, vectRep);
 			this.efh.runAlgorithm();
 			assertEquals(efh.calculated, true);
 			this.efh.runAlgorithm();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertEquals("Please reset the fields", e.getMessage());
 		}
 
 	}
 	
 	@Test
 	public void testThePriorityQueueIsStilSorting() throws Exception {
 		
 		this.efh = new EfficientHAC(ArrayOfComments, vectRep);
 		this.efh.runAlgorithm();
 		assertEquals(true,efh.calculated);
 		
 		String log = "";
 		for(int i = 0 ; i < efh.clustersArray.size(); i++)
 		{
 			double last = Double.MAX_VALUE;
 			while(!efh.p.get(i).isEmpty())
 			{
 				if((last < efh.p.get(i).peek().sim) )
 				{
 					log += "The Priority queue of Cluste : "+i+" wasn't sorted because "+last+" < ";  
 					last = efh.p.get(i).poll().sim;
 					log += last+"\n";		
 				}
 				else
 					last = efh.p.get(i).poll().sim;
 			}
 		}
 		
 		System.out.println(log);
 		assertEquals("", log);
 	}
 	
 	
 	@Test
 	public void testTheMergesArrayIsSortedBySim() throws Exception {
 		
 		this.efh = new EfficientHAC(ArrayOfComments, vectRep);
 		this.efh.runAlgorithm();
 		assertEquals(true,efh.calculated);
 		
 		String log = "SUCCESS";
 		double last = Double.MAX_VALUE;
 		for (Acell merge : efh.a) {
 			if(merge.mergeSim > last)
 			{
 				log = "FAIL";
 				break;
 			}
 			last = merge.mergeSim;	
 		}
 		assertEquals("SUCCESS", log);
 	}
 
 	@Test
 	public void testNormalization() throws Exception {
 		
 		Comment com1 = new Comment("1", new double[] {0.37,0.25,0.47});
 		Comment.nomalizeCommentVector(com1);
 		Comment com2 = new Comment("2", new double[] {0.25,0.17,0.12});
 		Comment.nomalizeCommentVector(com2);
 		Cluster cl1 = new Cluster(com1);
 		Cluster cl2 = new Cluster(com2);
 		System.out.println(cl1.GAAC(cl1, cl2, new double[com1.vector.size()]));
 		
 	}
 	
 	
 	@Test
 	public void testInitiateFlow() throws Exception {
 		
 		DatabaseOperations.cleaArticleFromDB("123");
		int numOfCom = 10;
 		String url = "http://news.yahoo.com/_xhr/contentcomments/get_comments/?content_id=5ba15d36-0b2f-34bd-8950-11e69eab2ba0&_device=full&count=10&sortBy=highestRated&isNext=true&offset=10&pageNumber=1&_media.modules.content_comments.switches._enable_view_others=1&_media.modules.content_comments.switches._enable_mutecommenter=1&enable_collapsed_comment=1";
 		
 		DatabaseOperations.addNewArticle("123", url, numOfCom,"dummy" );
 		
 		ArrayOfCommentsFactory commentsFactory = new ArrayOfCommentsFactory();
 		ArrayOfCommentsDO arrayOfComments = commentsFactory.get("123");
 		commentsFactory.save(arrayOfComments);
 		
 		EfficientHAC efh = new EfficientHAC(arrayOfComments.arrayOfComment, arrayOfComments.vect);
 		efh.runAlgorithm();
 		xmlGenerator xxx = new xmlGenerator("123", efh.a, numOfCom);
 		Maintenance maint = new Maintenance();
 		maint.mapXmlHacToClusters("123");
 				
 	}
 	
 	@Test
 	public void testReConstructHACFlow() throws Exception {
 		String articleID = "123";
 		String articleUrl = DatabaseOperations.getUrl(articleID);
 		String newNumUrl = DatabaseOperations.getNewNumberOfCommentsUrl(articleID);
 		ArrayList<String> articleCommentsMarkup = DatabaseOperations.getAllArticleCommentsHtml(articleID);
		int newNumOfComments = /*MarkupUtility.getLatestCommentAmount(newNumUrl);*/ 15;
 		
 		//1.Retrieve only the new comments + replace the old vectors + set the new words (SARIT)
 		ArrayList<CommentEntityDS> updatedArticleComments =  MaintenanceDataManager.gettingCommentsForMaintenance(articleUrl, articleID, newNumOfComments, DatabaseOperations.getArticleNumOfComments(articleID), articleCommentsMarkup);
 		//2.save to DB the new comments
 		DatabaseOperations.setComments(articleID, updatedArticleComments);
 		//3.save the newNumberOfComments to article table
 		DatabaseOperations.setArticleNumOfComments(articleID, newNumOfComments);
 		//4.retrieve all the comments from DB
 		ArrayOfCommentsDO commentsDO = new ArrayOfCommentsDO(articleID,DatabaseOperations.getAllComentsWithoutHTML(articleID));
 		//5.save to cache
 		ArrayOfCommentsFactory commentFactory = new ArrayOfCommentsFactory();
 		commentFactory.save(commentsDO);
 		//6.run efficient	
 		EfficientHAC effHAC = new EfficientHAC(commentsDO.arrayOfComment, commentsDO.vect);
 		effHAC.runAlgorithm();
 		xmlGenerator xmlGen = new xmlGenerator(articleID, effHAC.a, newNumOfComments);
 		Maintenance maintenance = new Maintenance();
 		maintenance.mapXmlHacToClusters(articleID);
 		
 	}
 	
 	@Test
 	public void testMaintenanceFlow() throws Exception {
 		String articleID = "123";
 		int newNumOfComments = /*getLatestNumberOfCommentsInTheArticle()*/ 10;
 		//1. get the new comments
 		//2. get the old comments
 		//3.get the mapping that existing
 		
 		Maintenance maint = new Maintenance();
 		maint.addNewElementsToHAC(MaintenanceDataManager.gettingCommentsForMaintenance(DatabaseOperations.getUrl(articleID), articleID, newNumOfComments, DatabaseOperations.getArticleNumOfComments(articleID), null), articleID);
 		
 	}
 	
 	@Test
 	public void testDBfunc(){
 		
		//int a = MarkupUtility.getLatestCommentAmount("http://news.yahoo.com/_xhr/contentcomments/get_all/?5dfccac3-8873-3941-845c-c9e1de3d20cc&_device=full&done=http%3A%2F%2Fnews.yahoo.com%2Fobama-boehner-locked-another-budget-battle-deadlines-loom-004717091--business.html&_media.modules.content_comments.switches._enable_view_others=1&_media.modules.content_comments.switches._enable_mutecommenter=1&enable_collapsed_comment=1");
		//System.out.println(a);
 		ArrayList<ArrayList<Double>> replacedVector = new ArrayList<ArrayList<Double>>();
 		ArrayList<Double> nea = new ArrayList<Double>();
 		nea.add(0.0);
 		nea.add(1.1);
 		replacedVector.add(nea);
 		nea.add(3.5);
 		replacedVector.add(nea);
 		DatabaseOperations.replaceVectorsForComments("123", replacedVector);
 		
 		
 	}
 
 }
