 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.m4us.handlers;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import org.group.utils.qo.UserProfileUpdate;
 import org.m4us.controller.FlowContext;
 import org.m4us.movielens.utils.dto.*;
 import org.m4us.workers.UserRatingsWorker;
 
 /**
  *
  * @author arka
  */
 public class UserRatingsHandler implements IHandler{
 
     
     
     @Override
     public void handleRequest(FlowContext flowCtx) 
     {
         List<DataTransferObject>updateList=new ArrayList<DataTransferObject>();                
         List<DataTransferObject>insertList=new ArrayList<DataTransferObject>();        
         UserInfoTableObject userInfo = (UserInfoTableObject)flowCtx.get("userInfo");
         List<DataTransferObject> similarMoviesList = (List<DataTransferObject>)flowCtx.get("similarMoviesList");
         for(DataTransferObject object : similarMoviesList){
         {
             MoviesRatingsComposite movieObj = (MoviesRatingsComposite)object;
             MoviesTableObject mtObject = movieObj.getMovieObj();
             RatingsTableObject rtObject = movieObj.getRatingsObj();
            float newRating=Float.parseFloat((String)flowCtx.get("movieId"+mtObject.getMovieId()));
            
             if(newRating>0)
             {
                 if(rtObject.getRating()==0)
                 {
                     RatingsTableObject newRatingObject=new RatingsTableObject();
                     newRatingObject.setMovieId(mtObject.getMovieId());
                     newRatingObject.setRating(newRating);
                     newRatingObject.setUserId(userInfo.getUserId());
                     newRatingObject.setRatingDate(new Timestamp(System.currentTimeMillis()));
                     
                     insertList.add(newRatingObject);
                 }
                 else if(rtObject.getRating() != newRating)
                 {
                     RatingsTableObject newRatingObject=new RatingsTableObject();
                     newRatingObject.setMovieId(mtObject.getMovieId());
                     newRatingObject.setRating(newRating);
                     newRatingObject.setUserId(userInfo.getUserId());
                     newRatingObject.setRatingDate(new Timestamp(System.currentTimeMillis()));
                     
                     updateList.add(newRatingObject);
                 }
             }
            
          }
         UserRatingsWorker userRatingsWorker=new UserRatingsWorker();
         userRatingsWorker.insertUserRatings(insertList);
         userRatingsWorker.updateUserRatings(updateList);
         UserProfileUpdate upu=new UserProfileUpdate();
         upu.updateProfile(insertList);
         flowCtx.remove("similarMoviesList");
         }
     }
 }
