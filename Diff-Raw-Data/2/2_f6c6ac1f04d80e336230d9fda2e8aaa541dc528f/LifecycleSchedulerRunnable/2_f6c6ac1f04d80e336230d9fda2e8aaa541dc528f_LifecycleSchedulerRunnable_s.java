 package sce.finalprojects.sceprojectbackend.runnables;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import sce.finalprojects.sceprojectbackend.algorithms.EfficientHAC;
 import sce.finalprojects.sceprojectbackend.algorithms.Maintenance;
 import sce.finalprojects.sceprojectbackend.algorithms.xmlGenerator;
 import sce.finalprojects.sceprojectbackend.database.DatabaseOperations;
 import sce.finalprojects.sceprojectbackend.datatypes.ArrayOfCommentsDO;
 import sce.finalprojects.sceprojectbackend.datatypes.ArticleSetupRequestDO;
 import sce.finalprojects.sceprojectbackend.datatypes.ClusterRepresentationDO;
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.datatypes.LifecycleStageDO;
 import sce.finalprojects.sceprojectbackend.factories.ArrayOfCommentsFactory;
 import sce.finalprojects.sceprojectbackend.managers.LifecycleScheduleManager;
 import sce.finalprojects.sceprojectbackend.managers.MaintenanceDataManager;
 import sce.finalprojects.sceprojectbackend.utils.MarkupUtility;
 
 public class LifecycleSchedulerRunnable implements Callable<Set<ClusterRepresentationDO>>{
 	private String articleID;
 	private String articleUrl;
 	private String commentsAmountURL;
 	private String maintenanceURL;
 	private int runsCounter;
 	private int intialAmountOfComments;
 	private long createTimestamp;
 	
 
 	public LifecycleSchedulerRunnable(ArticleSetupRequestDO request) {
 		super();
 		this.articleID = request.getArticleID();
 		this.articleUrl = request.getUrl();
 		this.intialAmountOfComments = request.getCommentsCount();
 		this.commentsAmountURL = request.getCommentsAmountRetrievalURL();
 		this.createTimestamp = System.currentTimeMillis();
 		this.maintenanceURL = request.getMaintenanceURL();
 
 	}
 	
 	private void setupNextRun(){
 		
 		long delay = calculateDelay();
 		if(delay > 0){
 			LifecycleScheduleManager.scheduleRun(this, delay);
 		}
 	}
 	
 	private long calculateDelay(){
 		long age = (System.currentTimeMillis() - this.createTimestamp)/1000;
 		for(LifecycleStageDO lcs: LifecycleScheduleManager.stages){
 			if(age >= lcs.getFrom() && age < lcs.getTo())
 				return (long) lcs.getInterval();
 		}
 		
 		return -1;
 	}
 	
 	private void complete(){
 		this.intialAmountOfComments = 0;
 	}
 
 	@Override
 	public Set<ClusterRepresentationDO> call() throws Exception {
 		try{
 			if(runsCounter == 0){
 				System.out.println("LIFECYCLE: Initial Run");
 				DatabaseOperations.addNewArticle(this.articleID, this.articleUrl, this.intialAmountOfComments, this.commentsAmountURL,this.maintenanceURL);
 				ArrayOfCommentsFactory commentFactory = new ArrayOfCommentsFactory();
 				ArrayOfCommentsDO articleCommentsArray = commentFactory.get(this.articleID);
 				commentFactory.save(articleCommentsArray);
 				EfficientHAC effHAC = new EfficientHAC(articleCommentsArray.arrayOfComment, articleCommentsArray.vect);
 				effHAC.runAlgorithm();
				xmlGenerator xmlGen = new xmlGenerator(this.articleID, effHAC.a, this.intialAmountOfComments);
 				Maintenance maintenance = new Maintenance();
 				maintenance.mapXmlHacToClusters(this.articleID);
 				
 				this.runsCounter++;
 				
 				return DatabaseOperations.getHACRootID(this.articleID);
 			
 	
 			}else{
 				String newNumUrl = DatabaseOperations.getNewNumberOfCommentsUrl(this.articleID);
 				int newNumOfComments = MarkupUtility.getLatestCommentAmount(newNumUrl);
 				int currentAmountOfComments = DatabaseOperations.getArticleNumOfComments(articleID);
 				
 				if(currentAmountOfComments >= newNumOfComments){
 					System.out.println("LIFECYCLE: No new comments. Nothing to do");
 					return null;
 				}
 				
 				this.runsCounter++;
 				
 				if(runsCounter%3 == 0){
 					System.out.println("LIFECYCLE: Rebuild run");
 				
 					
 					//String articleUrl = DatabaseOperations.getUrl(this.articleID);
 					ArrayList<String> articleCommentsMarkup = DatabaseOperations.getAllArticleCommentsHtml(this.articleID);
 					String maintenanceUrl = DatabaseOperations.getMaintenanceUrl(this.articleID);
 
 					//1.Retrieve only the new comments + replace the old vectors + set the new words (SARIT)
 					ArrayList<CommentEntityDS> updatedArticleComments =  MaintenanceDataManager.gettingCommentsForReBuilding(maintenanceUrl, articleID, newNumOfComments, currentAmountOfComments, articleCommentsMarkup);
 					//2.save to DB the new comments
 					DatabaseOperations.setComments(this.articleID, updatedArticleComments);
 					//3.save the newNumberOfComments to article table
 					//DatabaseOperations.setArticleNumOfComments(this.articleID, newNumOfComments);
 					//4.retrieve all the comments from DB
 					ArrayOfCommentsDO commentsDO = new ArrayOfCommentsDO(this.articleID,DatabaseOperations.getAllComentsWithoutHTML(this.articleID));
 					//5.save to cache
 					ArrayOfCommentsFactory commentFactory = new ArrayOfCommentsFactory();
 					commentFactory.save(commentsDO);
 					//6.run efficient	
 					EfficientHAC effHAC = new EfficientHAC(commentsDO.arrayOfComment, commentsDO.vect);
 					effHAC.runAlgorithm();
 					xmlGenerator xmlGen = new xmlGenerator(this.articleID, effHAC.a, newNumOfComments);
 					Maintenance maintenance = new Maintenance();
 					maintenance.mapXmlHacToClusters(this.articleID);
 					
 				}else{
 					System.out.println("LIFECYCLE: Maintenance run");
 					Maintenance maint = new Maintenance();
 					maint.addNewElementsToHAC(MaintenanceDataManager.gettingCommentsForMaintenance(DatabaseOperations.getMaintenanceUrl(articleID), articleID, newNumOfComments, currentAmountOfComments), articleID);
 					
 				}
 				
 				
 			}
 			
 		
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			setupNextRun();
 			complete();
 		}
 		
 		return new HashSet<ClusterRepresentationDO>();
 	}
 	
 	
 }
