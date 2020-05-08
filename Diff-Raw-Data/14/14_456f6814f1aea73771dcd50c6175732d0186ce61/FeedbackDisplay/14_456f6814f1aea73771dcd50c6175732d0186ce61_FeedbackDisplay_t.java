 package reevent.web.myEvents;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.ComponentPropertyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import reevent.domain.Feedback;
 import reevent.web.ReEventApplication;
 import reevent.web.ReEventSession;
 import reevent.web.StyledPanel;
 import reevent.web.myEvents.newEvent.detailEvent;
 
 /**
  * A block that shows the main picture and basic data about an event.
  */
 public class FeedbackDisplay extends StyledPanel {
     Label title;
     Label text;
     Label rating;
     Label createdBy;
     
     Link deleteFeedback;
 
     public FeedbackDisplay(String id, final IModel<Feedback> feedback) {
         super(id, new CompoundPropertyModel<Feedback>(feedback));
         this.add(title = new Label("title"));
         this.add(text = new Label("text"));
         this.add(rating = new Label("rating"));
         this.add(createdBy = new Label("createdBy", new ComponentPropertyModel("createdBy.username")));
         
         this.add(deleteFeedback = new Link("deleteFeedback", new Model()){
         	public void onClick() {
         		
                 setResponsePage(this.getPage());
         		
         	}
         });
         
        if (ReEventSession.get().getUserSignedIn() == null) {
        	deleteFeedback.setVisible(false);
        }
        
        
         if (ReEventSession.get().getUserSignedIn() != null){
        	System.out.println("USER LOGGED IN!");
         	deleteFeedback.setVisible(ReEventSession.get().getUserSignedIn().equals(feedback.getObject().getCreatedBy().getId()));
         }
         
     }
 }
