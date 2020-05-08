 package eu.alertproject.iccs.events.api;
 
 /**
  * User: fotis
  * Date: 05/11/11
  * Time: 17:43
  */
 public class Topics {
 
 
     public static final String ICCS_STARDOM_Identity_Updated= "ICCS.IdentityUpdated";
    public static final String ICCS_STARDOM_Component_Updated= "ICCS.ComponentUpdated";
     public static final String ICCS_STARDOM_Issue_Updated= "ICCS.IssueUpdated";
 
 
     public static final String ALERT_STARDOM_Identity_Updated= "ALERT.STARDOM.IdentityUpdate";
     public static final String ALERT_STARDOM_New_Identity= "ALERT.STARDOM.IdentityNew";
     public static final String ALERT_STARDOM_Issue_Updated= "ALERT.STARDOM.IssueUpdated";
     public static final String ALERT_STARDOM_TextToAnnotate ="ALERT.STARDOM.TextToAnnotate";
     public static final String ALERT_STARDOM_LoginVerify="ALERT.STARDOM.LoginVerify";
 
 
     public static final String ALERT_ALL_STARDOM_LoginVerifyRequest="ALERT.*.LoginVerifyRequest";
     public static final String ALERT_ALL_SOCRATES_Issue_Recommendation_Request = "ALERT.*.Recommender.IssueRecommendationRequest";
     public static final String ALERT_ALL_SOCRATES_Identity_Recommendation_Request= "ALERT.*.Recommender.IdentitiesRecommendationRequest";
     public static final String ALERT_ALL_SOCRATES_Identity_Verification_Request= "ALERT.*.Recommender.VerifyIdentitiesRequest";
 
 
     public static final String ALERT_SOCRATES_Issue_Recommendation= "ALERT.Recommender.IssueRecommendation";
     public static final String ALERT_SOCRATES_Identity_Recommendation= "ALERT.Recommender.IdentityRecommendation";
     public static final String ALERT_SOCRATES_Identity_Verification= "ALERT.Recommender.IdentityVerification";
 
 
     public static final String ALERT_KEUI_TextToAnnotate_Annotated ="ALERT.KEUI.TextToAnnotate.Annotated";
 
     public static final String ALERT_MLSensor_Mail_New ="ALERT.MLSensor.MailNew";
 
     public static final String ALERT_METADATA_IdentityUpdate_Stored=     "ALERT.Metadata.IdentityUpdate.Stored";
     public static final String ALERT_METADATA_CommitNew_Updated=     "ALERT.Metadata.CommitNew.Updated";
     public static final String ALERT_METADATA_IssueNew_Updated =     "ALERT.Metadata.IssueNew.Updated";
     public static final String ALERT_METADATA_IssueUpdate_Updated =  "ALERT.Metadata.IssueUpdate.Updated";
     public static final String ALERT_METADATA_MailNew_Updated =      "ALERT.Metadata.MailNew.Updated";
     public static final String ALERT_METADATA_ForumPost_Updated =    "ALERT.Metadata.ForumPost.Updated";
 
 
 }
