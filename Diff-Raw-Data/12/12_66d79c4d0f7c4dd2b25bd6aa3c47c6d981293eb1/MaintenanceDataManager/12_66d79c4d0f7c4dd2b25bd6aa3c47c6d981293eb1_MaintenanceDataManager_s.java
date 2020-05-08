 package sce.finalprojects.sceprojectbackend.managers;
 
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
<<<<<<< HEAD
=======
<<<<<<< HEAD
import DataTypes.StatisticData;
=======
>>>>>>> 843f2a88802ebaf246869df41aeedaf8deaf2517
>>>>>>> origin/Packaging-changes
 import sce.finalprojects.sceprojectbackend.database.DatabaseOperations;
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.utils.HelperFunctions;
 import sce.finalprojects.sceprojectbackend.utils.MarkupUtility;
 import sce.finalprojects.sceprojectbackend.utils.UrlHelper;
import sce.finalprojects.sceprojectbackend.textProcessing.TextProcessingManager;
 
 
 public class MaintenanceDataManager {
 	
 	public static String[] commentsString;
 	public static CommentEntityDS[] commentsArray;
 	private static CommentsDownloadManager cdm;
 	public static String[] arrayOfKeys;
 	/**
 	 * this function called when we want to update the HAC tree to get more comments of the article
 	 * @param articleId- by this article id we will get data from the DB about all we done with this article
 	 * @throws SQLException 
 	 * @throws FileNotFoundException 
 	 */
 	public static ArrayList<CommentEntityDS> gettingCommentsForMaintenance(String urlString, String articleId, int newNumOfComments, int lastComment) throws SQLException, FileNotFoundException{
 
 		System.out.println("num of comments: " + newNumOfComments + "last comment: " + lastComment);
 
 		int amountOfComments = newNumOfComments - lastComment;
 		commentsArray = new CommentEntityDS[amountOfComments];
 		int numOfThreads = HelperFunctions.getNumOfThreads(newNumOfComments, lastComment);
 		commentsString = new String[numOfThreads];
 		ArrayList<ArrayList<Double>> commentsVectors = new ArrayList<ArrayList<Double>>();
 		ArrayList<CommentEntityDS> arrayListOfComments = new ArrayList<CommentEntityDS>();
 		cdm = new CommentsDownloadManager();
 		int numOfKeys = newNumOfComments/100 + 1;
 		arrayOfKeys = new String[numOfKeys]; 
 		UrlHelper uh = new UrlHelper();
 				
 		try {
 			URL url = new URL(urlString);
 			
 			arrayOfKeys[0] = getKeyFromURL(urlString);
 			if(lastComment >= 110)	
 				for(int i = 0; i < (lastComment / 100); i++){
 					String tempKey = MarkupUtility.getNextPaginationKey(cdm.getJsonObjectFromYahoo(uh.getFixUrlForMaintenance(uh.buildUrlForMaintenance(url), arrayOfKeys[0])));
 					if(tempKey != null)
 						arrayOfKeys[0] = tempKey;
 				}
 			
 			
 			if((newNumOfComments / 100) == (lastComment / 100)){
 				cdm.getCommentsByUrlForMaintenance(url, newNumOfComments, 1, lastComment, arrayOfKeys[0]);
 				commentsString[0] = cdm.getCommentString();
 			}
 			else{
 				int num = newNumOfComments - lastComment;
 				for(int i = 0; i < numOfThreads; i++){
 					if(i == 0){
 						int temp = ((lastComment/100) + 1) * 100;
 						cdm.getCommentsByUrlForMaintenance(url, (temp - lastComment), i+1, lastComment, arrayOfKeys[i]);
 						commentsString[i] = cdm.getCommentString();
 						num -= (temp - lastComment);
 					}
 					else{
 						if(num > 100){
 							num -= 100;
 							cdm.getCommentsByUrlForMaintenance(url, 100, i+1, lastComment, arrayOfKeys[i]);
 							commentsString[i] = cdm.getCommentString();
 						}
 						else{
 							cdm.getCommentsByUrlForMaintenance(url, num, i+1, lastComment, arrayOfKeys[i]);
 							commentsString[i] = cdm.getCommentString();
 						}
 					}
 				}
 			}
 				
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		StringBuilder finalString = new StringBuilder();
 		for(int i=0; i<numOfThreads; i++)
 			finalString.append(commentsString[i]);
 
 		TextProcessingManager cst = new TextProcessingManager();
 		StatisticData[][] sd = cst.getTextResult(finalString.toString(),newNumOfComments - lastComment);
 		ArrayList<String> newWordsArray = TextProcessingManager.wordsArray;
 		commentsVectors = cst.vectorsCompletionForMaintenance(newWordsArray, sd, (newNumOfComments - lastComment), articleId);
 		
 		for(int i=0; i<commentsArray.length; i++){
 			if(commentsArray[i] != null){
 				commentsArray[i].setVector(commentsVectors.get(i));
 				arrayListOfComments.add(commentsArray[i]);
 			}
 			else
 				System.out.println(i + ": null"); //TODO delete
 		}
 		//the words saved in the data base in the method vectorsCompletionForMaintenance
 
 		DatabaseOperations.setArticleNumOfComments(articleId, (DatabaseOperations.getArticleNumOfComments(articleId) + arrayListOfComments.size()));
 
 		return arrayListOfComments;	
 	}
 	
 	
 	/**
 	 * this function called when we want to update the HAC tree to get more comments of the article
 	 * @param articleId- by this article id we will get data from the DB about all we done with this article
 	 * @throws SQLException 
 	 * @throws FileNotFoundException 
 	 */
 	public static ArrayList<CommentEntityDS> gettingCommentsForReBuilding(String urlString, String articleId, int newNumOfComments, int lastComment, ArrayList<String> htmlArr) throws SQLException, FileNotFoundException{
 
 		System.out.println("num of comments: " + newNumOfComments + "last comment: " + lastComment);
 		
 		int amountOfComments = newNumOfComments - lastComment;
 		commentsArray = new CommentEntityDS[amountOfComments];
 		int numOfThreads = HelperFunctions.getNumOfThreads(newNumOfComments, lastComment);
 		commentsString = new String[numOfThreads];
 		ArrayList<ArrayList<Double>> commentsVectors = new ArrayList<ArrayList<Double>>();
 		ArrayList<CommentEntityDS> arrayListOfComments = new ArrayList<CommentEntityDS>();
 		cdm = new CommentsDownloadManager();
 		int numOfKeys = newNumOfComments/100 + 1;
 		arrayOfKeys = new String[numOfKeys]; 
 		UrlHelper uh = new UrlHelper();
 						
 		arrayOfKeys[0] = getKeyFromURL(urlString);
 		
 		try {
 			URL url = new URL(urlString);
 
 			arrayOfKeys[0] = getKeyFromURL(urlString);
 			if(lastComment >= 110)	
 				for(int i = 0; i < (lastComment / 100); i++){
 					String tempKey = MarkupUtility.getNextPaginationKey(cdm.getJsonObjectFromYahoo(uh.getFixUrlForMaintenance(uh.buildUrlForMaintenance(url), arrayOfKeys[0])));
 					if(tempKey != null)
 						arrayOfKeys[0] = tempKey;
 				}
 			
 			if((newNumOfComments / 100) == (lastComment / 100)){
 				cdm.getCommentsByUrlForMaintenance(url, newNumOfComments, 1, lastComment, arrayOfKeys[0]);
 				commentsString[0] = cdm.getCommentString();
 			}
 			else{
 				int num = newNumOfComments - lastComment;
 				for(int i = 0; i < numOfThreads; i++){
 					if(i == 0){
 						int temp = ((lastComment/100) + 1) * 100;
 						cdm.getCommentsByUrlForMaintenance(url, (temp - lastComment), i+1, lastComment, arrayOfKeys[i]);
 						commentsString[i] = cdm.getCommentString();
 						num -= (temp - lastComment);
 					}
 					else{
 						if(num > 100){
 							num -= 100;
 							cdm.getCommentsByUrlForMaintenance(url, 100, i+1, lastComment, arrayOfKeys[i]);
 							commentsString[i] = cdm.getCommentString();
 						}
 						else{
 							cdm.getCommentsByUrlForMaintenance(url, num, i+1, lastComment, arrayOfKeys[i]);
 							commentsString[i] = cdm.getCommentString();
 						}
 					}
 				}
 			}
 				
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		StringBuilder finalString = new StringBuilder();
 		CommentsDownloadManager cdm = new CommentsDownloadManager();
 		int htmlArrSize = htmlArr.size();
 		for(int i=0; i<htmlArrSize; i++)
 			finalString.append(cdm.prepareCommentToTextProcessing(MarkupUtility.getCommentBodyFromMarkup(htmlArr.get(i))) + " ");
 
 		for(int i=0; i<numOfThreads; i++)
 			finalString.append(commentsString[i]);
 		TextProcessingManager cst = new TextProcessingManager();
 		StatisticData[][] sd = cst.getTextResult(finalString.toString(),newNumOfComments);
 		Double[][] wordCommentsMatrix = cst.buildWordCommentMatrix(sd, newNumOfComments);
 		commentsVectors = cst.buildCommentsVector(wordCommentsMatrix, newNumOfComments);
 		
 		ArrayList<ArrayList<Double>> oldCommentsVectors = new ArrayList<ArrayList<Double>>();
 		for(int i=0; i<lastComment;i++)
 			oldCommentsVectors.add(commentsVectors.get(i));
 		DatabaseOperations.replaceVectorsForComments(articleId, oldCommentsVectors);
 		
 		System.out.println("array: " + commentsArray.length + "numOfComments: " + amountOfComments + "  vectors: " + commentsVectors.size());
 		for(int i=0; i<amountOfComments; i++){
 			if(commentsArray[i] != null){
 				commentsArray[i].setVector(commentsVectors.get(i + lastComment));
 				arrayListOfComments.add(commentsArray[i]);
 			}
 			else 
 				System.out.println(i + ": null"); //TODO delete
 		}
 		
 		DatabaseOperations.setArticleWords(articleId, TextProcessingManager.wordsArray);
 
 		DatabaseOperations.setArticleNumOfComments(articleId, (DatabaseOperations.getArticleNumOfComments(articleId) + arrayListOfComments.size()));
 
 		return arrayListOfComments;
 	}
 	
 	public static String getKeyFromURL(String url)
 	{
 		String[] temp = url.split("exprKey=Ascending");
 		temp = temp[1].split("isNext=true");
 		
 		return temp[0];
 	}
 }
