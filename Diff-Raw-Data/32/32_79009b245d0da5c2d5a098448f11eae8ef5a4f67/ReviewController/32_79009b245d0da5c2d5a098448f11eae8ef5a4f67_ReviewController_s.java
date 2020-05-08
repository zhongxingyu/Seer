 package controllers;
 
 import helpers.CustomExcelHelper;
 import helpers.ExcelHelper;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.ReviewDetail;
 import models.User;
 
 import org.codehaus.jackson.node.ObjectNode;
 import play.data.*;
 import static play.data.Form.*;
 import play.db.DB;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import views.html.*;
 
 public class ReviewController extends Controller{
 
 
 	  final static Form<ReviewDetail> reviewForm = form(ReviewDetail.class);
 	  final static Connection connection = DB.getConnection();
 	
 	 public static Result index(User user) {
 		 String username =user.getUsername();
 		
 
 		 // DataSource ds = DB.getDataSource();
 		 // Connection connection = DB.getConnection();
 		  System.out.println(connection);
 		  List<ReviewDetail> reviewList = listReview();
 		  System.out.println("--listby--"+username);
 
 	   //return ok(home.render(filledForm,reviewList,username));
 	    return ok(home.render(reviewList,username));
 	  }
 	 
 
 	 public static Result allReviews() {
 		 String username =session("username");
 		  ReviewDetail review = new ReviewDetail();
 		
 		  
 		  String reviewId = ReviewDetail.getLastAddition(connection, username);
 		   review = ReviewDetail.getReviewById(connection, reviewId);
 		   review.setReviewerId(username);
 		   review.setAuthor(username);
 		   review.setSummery("");
 		   review.setDescription("");
 		  
 		  Form<ReviewDetail> filledForm =  form(ReviewDetail.class).fill(review);
 
 		 // DataSource ds = DB.getDataSource();
 		 // Connection connection = DB.getConnection();
 		  System.out.println(connection);
 		  List<ReviewDetail> reviewList = listReview();
 		  System.out.println("--listby--"+username);
 
 	  //  return ok(home.render(filledForm,reviewList,username));
 	    return ok(home.render(reviewList,username));
 	  }
 	 
 	 
 	 public static Result createReview() {
 	     
 	     String username = session("username");
 	     ReviewDetail review = new ReviewDetail();
 		
 		  
 	     String reviewId = ReviewDetail.getLastAddition(connection, username);
 	     review = ReviewDetail.getReviewById(connection, reviewId);
 		   review.setReviewerId(username);
 		   review.setAuthor(username);
 		   review.setSummery("");
 		   review.setDescription("");
 		  
 		  Form<ReviewDetail> filledForm =  form(ReviewDetail.class).fill(review);
 	     
 	     return ok(createReview.render(filledForm));
 	 }
 	 
 	 public static Result add() {
 		 Form<ReviewDetail> filledReviewForm = reviewForm.bindFromRequest();
 		 ReviewDetail review = filledReviewForm.get();
 		 if(review.getReviewerId()==null){
 		     review.setReviewerId(session("username"));
 		 }
 		  createReview(review);
 		  
 		  ReviewDetail newReview = new ReviewDetail();
 		  newReview.setReviewerId(review.getReviewerId());
 		  
 		  
 		  //review.setReviewerId(username);
 		 // review.setAuthor(username);
 		 //ReviewDetail review = new ReviewDetail();
 		//  Form<ReviewDetail> filledForm =  form(ReviewDetail.class).fill(review);
 		  List<ReviewDetail> reviewList = listReview();
 	   // return ok(home.render(filledReviewForm.fill(newReview),reviewList,session("username")));
 	    
 	    return ok(home.render(reviewList,session("username")));
 	  }
 	 
 	 
 	
 	 public static Result newReview() {
 		 Form<ReviewDetail> filledReviewForm = reviewForm.bindFromRequest();
 		 ReviewDetail review = filledReviewForm.get();
 		  createReview(review);
 		  ObjectNode result = Json.newObject();
 		  result.put("status", "OK");
 		  
 		  ObjectNode notifier = Json.newObject();
 		  notifier.put("msg", "Review Added");
 		  notifier.put("data", "<tr><td>"+review.getReviewId()+"</td><td>"+review.getAuthor()+"</td><td>"+ review.getSummery()+"</td><td>"+review.getAssignedTo()+"</td><td>"+review.getFileName()+"</td><td>"+review.getStatus()+"</td></tr> ");
 		  SocketController.outter.write(notifier);
 	    return ok(result);
 	  }
 
 
 
 
 	   public static Result getReview(String id) {
 		ReviewDetail reviewDtl = ReviewDetail.getReviewById(connection,id);
 	    return ok(reviewItem.render(reviewDtl));
 	  	}
 
 
 
 
 	   public static Result getReviewForEdit(String id) {
 		ReviewDetail reviewDtl = ReviewDetail.getReviewById(connection,id);
 		 Form<ReviewDetail> filledForm =  form(ReviewDetail.class).fill(reviewDtl);
 	    return ok(newReview.render(filledForm));
 	  	}
 
 	  	  public static Result updateReview(String id) {
 		Form<ReviewDetail> filledReviewForm = reviewForm.bindFromRequest();
 		 ReviewDetail review = filledReviewForm.get();
 		if(review.getReviewId()==null){
 			review.setReviewId(id);
 		}
 		
 		 System.out.println("update request  "+ id);
 		 ReviewDetail.updateReview(connection, review, session("username"));
 	  //  return ok(home.render(filledReviewForm,listReview(),session("username")));
 	    
 	    return ok(home.render(listReview(),session("username")));
 	  	}
 
 
 	 
 
 	   public static Result excelExport() throws Exception{
 		 File file =   ExcelHelper.createExcel(listReview());
 		   return ok(file);
 	   }
 	   
 	   public static Result excelExportCustomFormat() throws Exception{
 		 File file =   CustomExcelHelper.createExcel(listReview());
 		   return ok(file);
 	   }
 	   
 	   
 	   
 
 	private static void createReview(ReviewDetail review) {
 		//DataSource ds = DB.getDataSource();
 		//  Connection connection = DB.getConnection();
 		  try{
		  PreparedStatement ps = connection.prepareStatement("insert into review_dtl(reviewId,reviewerId,type,severity,fileName,summery,description,phase,assignedTo,author,creation_dt,modification_dt,modifier,line,revision,status,groupname) values (review_dtl_seq.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
 		 // ps.setString(1, String.valueOf(Math.round(Math.random()*1000)));//Temp
 		  ps.setString(1, review.getReviewerId());
 		  ps.setString(2, review.getType());
 		  ps.setString(3, review.getSeverity());
 		  ps.setString(4, review.getFileName());
 		  ps.setString(5, review.getSummery());
 		  ps.setString(6, review.getDescription());
 		  ps.setString(7, review.getPhase());
 		  ps.setString(8, review.getAssignedTo());
 		  ps.setString(9, review.getAuthor());
 		  ps.setTimestamp(10,new Timestamp(System.currentTimeMillis()));
 		  ps.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
 		  ps.setString(12, review.getModifier());
 		  ps.setString(13, review.getLine());
 		  ps.setString(14, review.getRevision());
 		  ps.setString(15, "Open");
 		  ps.setString(16, review.getGroup());
 		  
 		  boolean result = ps.execute();
 		  System.out.println("executed for insert ["+result+"]"+ session("username") );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 	}
 	
 	
 	
 	public static List<ReviewDetail> listReview() {
 		//DataSource ds = DB.getDataSource();
 	//	  Connection connection = DB.getConnection();
 		List<ReviewDetail>  reviewList = new ArrayList<ReviewDetail>();
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("select reviewId,reviewerId,type,severity,fileName,summery,description,phase,assignedTo,author,creation_dt,modification_dt,modifier,line,revision,status,groupname from review_dtl order by creation_dt desc ");
 		 
 		  boolean result = ps.execute();
 		  ResultSet rs =  ps.getResultSet();
 	
 		  
 		  
 		 while(rs.next()){
 			 ReviewDetail review = new ReviewDetail();
 			 review.setReviewId(rs.getString(1));
 			 review.setReviewerId(rs.getString(2));
 			 review.setType(rs.getString(3));
 			 review.setSeverity(rs.getString(4));
 			 review.setFileName(rs.getString(5));
 			 review.setSummery(rs.getString(6));
 			 review.setDescription(rs.getString(7));
 			 review.setPhase(rs.getString(8));
 			 review.setAssignedTo(rs.getString(9));
 			 review.setAuthor(rs.getString(10));
 			 review.setCreationDate(rs.getTimestamp(11));
 			 review.setModificationDate(rs.getTimestamp(12));
 			 review.setModifier(rs.getString(13));
 			 review.setLine(rs.getString(14));
 			 review.setRevision(rs.getString(15));
 			 review.setStatus(rs.getString(16));
 			 review.setGroup(rs.getString(17));
 			 reviewList.add(review);
 		 }
 		  System.out.println("list selected ["+result+"] --" + session("username") );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return reviewList;
 	}
 
 
 
 }
