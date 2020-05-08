 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package trampoline;
 
 /**
  *
  * @author Kieran
  */
 public class ErrorHandler {
     
     private boolean flag_;   // Flag if there is an error
     private String currentErrorShort_; //Short version for display of current error;
     private String currentErrorLong_; //Long version of current error;
     private String moreDetails_;
     
     private static final String[] shortErrors_ =   {"","Error: Incorrect password supplied for club management tab",
                                                     "Error: Database connection error",
                                                     "Error: ToF not attached",
                                                    "Logout Successful"
                                                    };
     
     private static final String[] longErrors_ =    {"","The password supplied for access to the Club Managment tab was incorrect. Please try again.",
                                                     "An error occured whilst trying to initialise the database connection.",
                                                     "No time of flight device was found attached to the computer. Please check all cable connections are secure and the device is powered on.",
                                                    "You have successfully logged out of the Club Management panel."
                                                    };
     
     ErrorHandler(){
         this.flag_ = false;
         this.currentErrorShort_ = "";
         this.currentErrorLong_ = "";
         this.moreDetails_ = "";
     }
     
     boolean isError(){
         return this.flag_;
     }
     
     String getCurrentErrorShort(){
         return this.currentErrorShort_;
     }
     
     String getCurrentErrorLong(){
         return this.currentErrorLong_;
     }
     
     String getMoreDetails(){
         return this.moreDetails_;
     }
             
     void setError(int errno){
         this.flag_ = true;
         this.currentErrorShort_ = this.shortErrors_[errno];
         this.currentErrorLong_ = this.longErrors_[errno];
         this.moreDetails_ = "";
     }
     
     void setMoreDetails(String details){
         this.moreDetails_ = details;
     }
     
     void clearError(){
         setError(0);
         this.flag_ = false;
     }
 }
