 package com.preppa.web.components;
 
 import com.preppa.web.data.VoteDAO;
 import com.preppa.web.entities.User;
 import com.preppa.web.entities.Vote;
import com.preppa.web.utils.Constants;
 import com.preppa.web.utils.ContentType;
 import java.sql.Timestamp;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONObject;
 import org.apache.tapestry5.services.Request;
 import org.chenillekit.tapestry.core.components.Hidden;
 import org.springframework.security.annotation.Secured;
 
 /**
  *
  * @author nwt
  */
 @IncludeJavaScriptLibrary(value = {"context:js/vote.js"})
 public class VoteSeal {
 
     @ApplicationState
     private User user;
     @Persist
     private Integer pcount;
     @Property
     private Integer count;
     @Property
     @Persist
     private String voted;
     @Parameter(required = true)
     private Integer contentId;
     @Parameter(required = true)
     private ContentType contenttype;
     @Parameter(required = true)
     private String votes;
     @Persist
     private Boolean hasVoted;
     @Persist
     private Boolean up;
     @Inject
     private VoteDAO voteDAO;
     @Inject
     private HttpServletRequest _request;
     private int ctype;
     @Persist
     private int cid;
     @Persist
     private ContentType ctid;
     @Inject
     private ComponentResources cresources;
     @Property
     @Persist
     private Integer hcontid;
     @Persist
     @Property
     private Integer hconttypeid;
     @Component(parameters = {"value=hcontid"})
     private Hidden contIdHidden;
     @Component(parameters = {"value=hconttypeid"})
     private Hidden contTypeHidden;
     @Inject
     private Request arequest;
 
     @SetupRender
     void setDefault() {
         //cresources.discardPersistentFieldChanges();
 
         System.out.println(contentId);
         this.count = voteDAO.findVoteByContentId(contenttype, contentId);
         hasVoted = voteDAO.checkVoted(contenttype, contentId, user);
         if (count == null) {
             count = 0;
             pcount = 0;
         }
         pcount = count;
         cid = contentId;
         ctid = contenttype;
 
         voted = "false";
         up = false;
         hcontid = contentId;
         hconttypeid = contenttype.ordinal();
     }
 
     @CommitAfter
     @Secured("ROLE_USER")
     JSONObject onActionFromVoteUp() {
 
         //get request parameters
         String contId = arequest.getParameter("contId");
         String contTypeId = arequest.getParameter("contTypeId");
         String counter = arequest.getParameter("count");
         Integer acount  = Integer.parseInt(counter.trim());
 
 
         JSONObject json = new JSONObject();
         ContentType contype = contenttype;
         for (ContentType t : ContentType.values()) {
             if (t.ordinal() == Integer.parseInt(contTypeId)) {
 
                 contype = t;
                 break;
             }
         }
         //if the user hasn't voted on the content + add the vote
         if (!voteDAO.checkVoted(contype, Integer.parseInt(contId), user)) {
             acount++;
             voted = "true";
             up = true;
             json.put("voted", "true");
             json.put("count", acount);
             Vote v = new Vote();
             if (contId != null) {
                 v.setContentId(Integer.parseInt(contId));
             } else {
                 v.setContentId(contentId);
             }
             if (contTypeId != null) {
 
                 v.setContentTypeId(contype);
 
             } else {
                 v.setContentTypeId(contenttype);
             }
             v.setValue(1);
             v.setUser(user);
             Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
             v.setCreatedAt(now);
             String hostname = _request.getRemoteHost();
             System.out.println("here " + contenttype + " " + contentId + " " + user.getFirstName());
 
             v.setSource(hostname);
             voteDAO.doSave(v);
         }
         else {
 
             json.put("voted", "false");
             json.put("count", acount);
         }
 
         return json;
     }
 
     @CommitAfter
     @Secured("ROLE_USER")
     JSONObject onActionFromVoteDown() {
         String contId = arequest.getParameter("contId");
         String contTypeId = arequest.getParameter("contTypeId");
         String counter = arequest.getParameter("count");
         Integer acount  = Integer.parseInt(counter.trim());
 
 
         JSONObject json = new JSONObject();
         ContentType contype = contenttype;
         for (ContentType t : ContentType.values()) {
             if (t.ordinal() == Integer.parseInt(contTypeId)) {
 
                 contype = t;
                 break;
             }
         }
         //if the user hasn't voted on the content + add the vote
         if (!voteDAO.checkVoted(contype, Integer.parseInt(contId), user)) {
             acount--;
             voted = "true";
             up = true;
             json.put("voted", "true");
             json.put("count", acount);
             Vote v = new Vote();
             if (contId != null) {
                 v.setContentId(Integer.parseInt(contId));
             } else {
                 v.setContentId(contentId);
             }
             if (contTypeId != null) {
 
                 v.setContentTypeId(contype);
 
             } else {
                 v.setContentTypeId(contenttype);
             }
             v.setValue(-1);
             v.setUser(user);
             Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
             v.setCreatedAt(now);
             String hostname = _request.getRemoteHost();
           
             v.setSource(hostname);
             voteDAO.doSave(v);
         }
         else
         {
             json.put("voted", "false");
             json.put("count", acount);
         }
 
         return json;
     }

    public boolean getApproved() {
        if (count >= Constants.getApprovalThreshhold() /*1*/) {
            System.out.println("COUNT IS " + count);
            return true;
        }
        else {
            System.out.println("COUNT IS " + count);
            return false;
        }
    }
 }
