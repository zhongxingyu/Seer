 package com.madalla.webapp.site;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 import org.apache.wicket.validation.validator.UrlValidator;
 
 import com.madalla.bo.SiteData;
 import com.madalla.bo.SiteLanguage;
 import com.madalla.webapp.css.Css;
 import com.madalla.webapp.panel.CmsPanel;
 import com.madalla.wicket.form.AjaxValidationBehaviour;
 import com.madalla.wicket.form.AjaxValidationStyleRequiredTextField;
 import com.madalla.wicket.form.AjaxValidationStyleSubmitButton;
 
 public class SiteAdminPanel extends CmsPanel{
 
     private static final long serialVersionUID = 1L;
     private static final Log log = LogFactory.getLog(SiteAdminPanel.class);
     
     public class SiteForm extends Form<SiteData> {
 
         public SiteForm(String id, IModel<SiteData> model) {
             super(id, model);
             
             FeedbackPanel emailFeedback = new FeedbackPanel("emailFeedback");
             add(emailFeedback);
             TextField<String> email = new AjaxValidationStyleRequiredTextField("adminEmail",emailFeedback);
             email.add(EmailAddressValidator.getInstance());
             add(email);
 
             add(new TextField<String>("metaDescription"));
            
             add(new TextField<String>("metaKeywords"));
             
            TextField<String> url = new TextField<String>("url");
            FeedbackPanel urlFeedback = new ComponentFeedbackPanel("urlFeedback", url);
             urlFeedback.setOutputMarkupId(true);
             add(urlFeedback);
            
             url.add(new AjaxValidationBehaviour(urlFeedback));
             url.add(new UrlValidator(UrlValidator.NO_FRAGMENTS));
             add(url);
             
             add(new CheckBoxMultipleChoice<SiteLanguage>("localeList", Model.ofList(SiteData.getAvailableLocales()), 
             		new ChoiceRenderer<SiteLanguage>("displayName")));
             
         }
 
         private static final long serialVersionUID = 1L;
         
     }
     
     public SiteAdminPanel(String id) {
         super(id);
         add(Css.CSS_FORM);
         
         final Form<SiteData> form = new SiteForm("siteForm", new CompoundPropertyModel<SiteData>(getSiteData()));
         add(form);
         
         final FeedbackPanel siteFeedback = new ComponentFeedbackPanel("siteFeedback", form);
         siteFeedback.setOutputMarkupId(true);
         form.add(siteFeedback);
         
         AjaxButton submitLink = new AjaxValidationStyleSubmitButton("submitLink", form) {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 super.onError(target, form);
             }
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 saveSiteData((SiteData)form.getModelObject());
                 form.info(getString("message.success"));
             }
             
         };
         submitLink.setOutputMarkupId(true);
         form.add(submitLink);
         
     }
     
     private SiteData getSiteData(){
     	return getRepositoryService().getSiteData();
     }
     
     private void saveSiteData(SiteData data){
         log.debug("saveSiteData - + " + data);
         saveData(data);
     }
 
     
 }
