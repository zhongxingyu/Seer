 package com.madalla.webapp.blog.admin;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.behavior.HeaderContributor;
 import org.apache.wicket.datetime.StyleDateConverter;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DatePicker;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.html.resources.CompressedResourceReference;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.joda.time.DateTime;
 
 import com.madalla.service.cms.AbstractBlog;
 import com.madalla.service.cms.IRepositoryService;
 import com.madalla.service.cms.IRepositoryServiceProvider;
 import com.madalla.service.cms.ocm.blog.BlogEntry;
 import com.madalla.webapp.blog.BlogEntryView;
 import com.madalla.webapp.scripts.tiny_mce.TinyMce;
 import com.madalla.wicket.form.AjaxValidationBehaviour;
 import com.madalla.wicket.form.ValidationStyleBehaviour;
 import com.madalla.wicket.form.ValidationStyleRequiredTextField;
 
 public class BlogEntryPanel extends Panel {
     private static final long serialVersionUID = 1L;
     private static final CompressedResourceReference JAVASCRIPT = new CompressedResourceReference(BlogEntryPanel.class, "BlogEntryPanel.js");
     private Log log = LogFactory.getLog(this.getClass());
     private final BlogEntryView blogEntry = new BlogEntryView();
     private Class<?> returnPage;
     
     /**
      * Constructor for creating a new Blog Entry
      * @param id
      * @param blogName
      * @param returnPage
      */
     public BlogEntryPanel(String id, String blogName, Class<? extends Page> returnPage){
     	this(id, returnPage);
         blogEntry.setBlog(blogName);
         log.debug("Created new Blog Entry");
        add(new BlogEntryForm("blogForm",this));
     }
     
     /**
      * Constructor for editing an existing blog entry
      * @param id
      * @param blogName
      * @param blogEntryId
      * @param returnPage
      */
     public BlogEntryPanel(String id, String blogName, String blogEntryId, Class<? extends Page> returnPage) {
     	this(id, returnPage);
         log.debug("Constructing Blog Entry. id="+blogEntryId);
         blogEntry.init(getRepositoryService().getBlogEntry(blogEntryId));
         log.debug("Retrieved Blog Entry from Service."+blogEntry);
        add(new BlogEntryForm("blogForm",this));
     }
     
     private BlogEntryPanel(String id, Class<? extends Page> returnPage){
     	super(id);
     	this.returnPage = returnPage;
     	add(HeaderContributor.forJavaScript(TinyMce.class,"tiny_mce.js"));
         add(HeaderContributor.forJavaScript(JAVASCRIPT));
     }
     
     final class BlogEntryForm extends Form{
         private static final long serialVersionUID = 1L;
         
        public BlogEntryForm(final String name, Component panel) {
             super(name);
 
             //Date
             if (blogEntry.getDate() == null){
             	blogEntry.setDate(new DateTime().toDate());
             }
             DateTextField dateTextField = new DateTextField("dateTextField", new PropertyModel(blogEntry,"date"), new StyleDateConverter("S-",true));
             dateTextField.setRequired(true);
             dateTextField.add(new ValidationStyleBehaviour());
             FeedbackPanel dateFeedback = new ComponentFeedbackPanel("dateFeedback", dateTextField);
             dateFeedback.setOutputMarkupId(true);
             add(dateFeedback);
             dateTextField.add(new AjaxValidationBehaviour(dateFeedback));
            dateTextField.setLabel(new Model(panel.getString("label.date")));
             add(dateTextField);
             dateTextField.add(new DatePicker(){
 
 				@Override
 				protected void configure(Map widgetProperties) {
 					super.configure(widgetProperties);
 					widgetProperties.put("title", Boolean.FALSE);
 					widgetProperties.put("close", Boolean.FALSE);
 				}
             	
             });
             
 
             //Category select
             List<String> categories = BlogEntry.getBlogCategories();
             FormComponent categoryDropDown = new DropDownChoice("category", new PropertyModel(blogEntry,"category"), categories);
             categoryDropDown.setRequired(true);
             categoryDropDown.add(new ValidationStyleBehaviour());
             categoryDropDown.add(new AjaxFormComponentUpdatingBehavior("onblur"){
 				private static final long serialVersionUID = 6968995998493101488L;
 
 				protected void onUpdate(AjaxRequestTarget target){
             		target.addComponent(getFormComponent());
             	}
             });
            categoryDropDown.setLabel(new Model(panel.getString("label.category")));
             add(categoryDropDown);
             add(new ComponentFeedbackPanel("categoryFeedback", categoryDropDown));
 
             //Title
             FeedbackPanel titleFeedback = new FeedbackPanel("titleFeedback");
             add(titleFeedback);
             TextField title = new ValidationStyleRequiredTextField("title",new PropertyModel(blogEntry,"title"), titleFeedback);
            title.setLabel(new Model(panel.getString("label.title")));
             add(title);
             
             add(new TextArea("description",new PropertyModel(blogEntry,"description")).setConvertEmptyInputStringToNull(false));
             add(new TextField("keywords",new PropertyModel(blogEntry,"keywords")).setConvertEmptyInputStringToNull(false));
             
             add(new TextArea("text", new PropertyModel(blogEntry, "text")));
             
             add(new SubmitLink("submitButton"));
             
             add(new Link("cancelButton"){
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void onClick() {
 					setResponsePage(returnPage);
 				}
             });
         }
         
         public void onSubmit() {
             log.debug("onSubmit - Saving populated Blog Entry to Blog service. " + blogEntry);
             try {
                 saveBlogEntry(blogEntry);
                 info("Success");
                 log.info("Blog Entry successfully saved. " + blogEntry);
                 setResponsePage(returnPage);
             } catch (Exception e) {
                info("There was a problem saving Entry.");
                info(e.getMessage());
                 log.error("Exception while saving entry to blog service.", e);
             }
         }
 
     }
     
     private IRepositoryService getRepositoryService(){
     	return ((IRepositoryServiceProvider)getApplication()).getRepositoryService();
     }
     
     private void saveBlogEntry(BlogEntryView view){
     	BlogEntry blogEntry;
     	if (StringUtils.isEmpty(view.getId())){
     		AbstractBlog blog = getRepositoryService().getBlog(view.getBlog());
     		blogEntry = getRepositoryService().getNewBlogEntry(blog, view.getTitle(), new DateTime(view.getDate())); 
     		view.populate(blogEntry);
     	} else {
     		blogEntry = getRepositoryService().getBlogEntry(view.getId());
     		view.populate(blogEntry);
     	}
     	getRepositoryService().saveBlogEntry(blogEntry);
     }
 
 }
