 package reevent.web;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import reevent.web.signinout.SignInPanel;
 import reevent.web.signinout.SignOutPanel;
 
 public class Template extends WebPage {
     FeedbackPanel feedback;
     Panel signInOutPanel;
     {
         add(feedback = new FeedbackPanel("feedback"));
         feedback.setFilter(new IFeedbackMessageFilter() {
             @Override
             public boolean accept(FeedbackMessage message) {
                 Component reporter = message.getReporter();
                 if (reporter == null) return true;
                if (reporter == signInOutPanel) return true;
                return signInOutPanel.contains(reporter, true);
             }
         });
     }
     @Override
     protected void onBeforeRender() {
         super.onBeforeRender();
         ReEventSession session = ReEventSession.get();
         if (session.isSignedIn() && !(signInOutPanel instanceof SignOutPanel)) {
             // logged in and no logout panel
             this.addOrReplace(signInOutPanel = new SignOutPanel("signInOutPanel"));
         } else if (!session.isSignedIn() && !(signInOutPanel instanceof SignInPanel)) {
             // logged out and no login panel
             this.addOrReplace(signInOutPanel = new SignInPanel("signInOutPanel"));
         }
     }
 }
