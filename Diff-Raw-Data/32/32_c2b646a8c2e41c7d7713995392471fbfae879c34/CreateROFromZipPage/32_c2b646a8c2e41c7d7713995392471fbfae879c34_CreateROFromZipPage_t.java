 package pl.psnc.dl.wf4ever.portal.pages;
 
 import java.io.InputStream;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.util.time.Duration;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.pages.ro.RoPage;
 import pl.psnc.dl.wf4ever.portal.services.CreateROThread;
 import pl.psnc.dl.wf4ever.portal.services.CreateROThread.ProgressModel;
 import pl.psnc.dl.wf4ever.portal.services.CreateROThread.State;
 
 /**
  * Page that displays the progress of creating a new RO from a ZIP archive of resources.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public class CreateROFromZipPage extends BasePage {
 
     /**
      * A model that returns a "?" instead of null.
      * 
      * @author piotrekhol
      * 
      */
     static class QuestionMarkModel extends AbstractReadOnlyModel<String> {
 
         /** id. */
         private static final long serialVersionUID = -3997633235115647568L;
 
         /** The original model. */
         private IModel<?> model;
 
 
         /**
          * Constructor.
          * 
          * @param model
          *            the original model
          */
         public QuestionMarkModel(IModel<?> model) {
             this.model = model;
         }
 
 
         @Override
         public String getObject() {
             Object value = model.getObject();
             return value != null ? value.toString() : "?";
         }
 
     }
 
 
     /** id. */
     private static final long serialVersionUID = -3233388849667095897L;
 
     /** feedback panel. */
     private MyFeedbackPanel feedbackPanel;
 
     /** The progress of creating the RO. */
     private ProgressModel progressModel;
 
 
     /**
      * Constructor.
      * 
      * @param zip
      *            the zip file
      * @param zipName
      *            the filename of the zip file
      * 
      */
     public CreateROFromZipPage(InputStream zip, String zipName) {
         super(new PageParameters());
         feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         add(feedbackPanel);
         add(new Label("name", zipName));
 
         CreateROThread create = new CreateROThread(zip, zipName, MySession.get().getRosrs());
         progressModel = create.getProgressModel();
 
         final Label completeLabel = new Label("complete", new QuestionMarkModel(new PropertyModel<String>(
                 progressModel, "complete")));
         add(completeLabel.setOutputMarkupId(true));
         final Label totalLabel = new Label("total", new QuestionMarkModel(new PropertyModel<String>(progressModel,
                 "total")));
         add(totalLabel.setOutputMarkupId(true));
         final Label timeElapsed = new Label("time-elapsed", new QuestionMarkModel(new PropertyModel<String>(
                 progressModel, "timeElapsedFormatted")));
         add(timeElapsed.setOutputMarkupId(true));
         final Label timeRemaining = new Label("time-remaining", new QuestionMarkModel(new PropertyModel<String>(
                 progressModel, "timeRemainingFormatted")));
         add(timeRemaining.setOutputMarkupId(true));
 
         final TextArea<String> console = new TextArea<>("console", new PropertyModel<String>(progressModel,
                 "outputString"));
         add(console);
 
         final Component placeholder = new WebMarkupContainer("go-to-ro").setOutputMarkupPlaceholderTag(true)
                 .setVisible(false);
         add(placeholder);
 
         console.add(new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(500)) {
 
             /** id. */
             private static final long serialVersionUID = 5987637208986630961L;
 
 
             @Override
             protected void onPostProcessTarget(AjaxRequestTarget target) {
                 super.onPostProcessTarget(target);
                 //                target.appendJavaScript("$(\"#progressbar\").progressbar(\"value\", " + model.getProgressInPercent()
                 //                        + ");");
 
                 target.add(completeLabel);
                 target.add(totalLabel);
                 target.add(timeElapsed);
                 target.add(timeRemaining);
                 target.appendJavaScript("$('#console').scrollTop($('#console')[0].scrollHeight);");
                 if (progressModel.getThreadState() == State.TERMINATED) {
                     stop(target);
                     console.remove(this);
                    if (progressModel.getRoUri() != null) {
                        PageParameters params = new PageParameters();
                        params.add("ro", progressModel.getRoUri());
                        BookmarkablePageLink<String> link = new BookmarkablePageLink<>("go-to-ro", RoPage.class, params);
                        placeholder.replaceWith(link);
                        link.setOutputMarkupId(true);
                        link.setVisible(true);
                        target.add(link);
                    }
                 }
             }
         });
 
         create.start();
     }
 
 }
