 /*
  * Copyright (C) 2003-2008 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.faq.webui.popup;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.faq.service.FAQService;
 import org.exoplatform.faq.service.FAQSetting;
 import org.exoplatform.faq.service.Question;
 import org.exoplatform.faq.service.QuestionLanguage;
 import org.exoplatform.faq.service.impl.MultiLanguages;
 import org.exoplatform.faq.webui.FAQUtils;
 import org.exoplatform.faq.webui.UIFAQContainer;
 import org.exoplatform.faq.webui.UIFAQPortlet;
 import org.exoplatform.faq.webui.UIQuestions;
 import org.exoplatform.faq.webui.ValidatorDataInput;
 import org.exoplatform.forum.service.ForumService;
 import org.exoplatform.forum.service.Post;
 import org.exoplatform.portal.application.PortalRequestContext;
 import org.exoplatform.portal.webui.util.Util;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.web.application.ApplicationMessage;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.UIApplication;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.core.model.SelectItemOption;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.EventListener;
 import org.exoplatform.webui.form.UIForm;
 import org.exoplatform.webui.form.UIFormStringInput;
 import org.exoplatform.webui.form.UIFormWYSIWYGInput;
 
 /**
  * Created by The eXo Platform SARL
  * Author : Ha Mai
  *					mai.ha@exoplatform.com
  * Oct 20, 2008, 3:12:37 PM
  */
 
 @ComponentConfig(
     lifecycle = UIFormLifecycle.class ,
     template =  "app:/templates/faq/webui/popup/UICommentForm.gtmpl",
     events = {
       @EventConfig(listeners = UICommentForm.SaveActionListener.class),
       @EventConfig(listeners = UICommentForm.CancelActionListener.class)
     }
 )
 
 public class UICommentForm extends UIForm implements UIPopupComponent {
 	private QuestionLanguage questionLanguage_ = null;
 	private String languageSelected = null;
 	private Question question_ = new Question();
 	private String questionContent = new String();
 	private String questionDetail = new String();
 	private String currentUser_ = "";
 	private final String TITLE_USERNAME = "UserName";
 	private final String COMMENT_CONTENT = "CommentContent";
 	private List<String> listComments_ = new ArrayList<String>();
 	private List<String> listUserNames_ = new ArrayList<String>();
 	private List<Date> listDates_ = new ArrayList<Date>();
 	private int pos = 0;
 	private boolean isAddNew = false;
 	private FAQSetting faqSetting_ = null;
 	private Date date_ = new java.util.Date();
 	
 	private String link_ = "";
 
 	public void activate() throws Exception { }
 	public void deActivate() throws Exception { }
 	
 	public UICommentForm() throws Exception{
 		currentUser_ = FAQUtils.getCurrentUser();
 		this.addChild((new UIFormStringInput(TITLE_USERNAME, TITLE_USERNAME, currentUser_)).setEditable(false));
 		this.addChild(new UIFormWYSIWYGInput(COMMENT_CONTENT, COMMENT_CONTENT, null, true));
 	}
 	
 	public void setInfor(Question question, int posCommentEdit, FAQSetting faqSetting, String languageView) throws Exception{
 		if(languageView.trim().length() > 0) languageSelected = languageView;
 		else languageSelected = question.getLanguage();
 		listComments_ = new ArrayList<String>();
 		listUserNames_ = new ArrayList<String>();
 		listDates_ = new ArrayList<Date>();
 		
 		this.question_ = question;
 		this.questionContent = question.getQuestion();
 		this.questionDetail = question.getDetail();
 		this.faqSetting_ = faqSetting;
 		FAQUtils.getEmailSetting(faqSetting_, false, false);
 		
 		if(languageView.trim().length() < 1 || languageView.equals(question.getLanguage())) {
 			try{
 				listComments_.addAll(Arrays.asList(question_.getComments()));
 				listUserNames_.addAll(Arrays.asList(question_.getCommentBy()));
 				listDates_.addAll(Arrays.asList(question_.getDateComment()));
 			} catch (NullPointerException nullPointerException){
 				listComments_ = new ArrayList<String>();
 				listUserNames_ = new ArrayList<String>();
 				listDates_ = new ArrayList<Date>();
 			}
 			if(posCommentEdit >= 0){
 				pos = posCommentEdit;
 				((UIFormWYSIWYGInput)this.getChildById(COMMENT_CONTENT)).setValue(question_.getComments()[posCommentEdit]);
 			} else {
 				isAddNew = true;
 				listComments_.add(" ");
 				listUserNames_.add(currentUser_);
 				listDates_.add(date_);
 				pos = listComments_.size() - 1;
 			}
 		}
 		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
 		SessionProvider sProvider = FAQUtils.getSystemProvider();
 		List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
 		listLanguageToReponse.add(new SelectItemOption<String>(question.getLanguage(), question.getLanguage())) ;
 		String language = null;
 		for(QuestionLanguage questionLanguage : faqService.getQuestionLanguages(question.getId(), sProvider)){
 			language = questionLanguage.getLanguage();
 			listLanguageToReponse.add(new SelectItemOption<String>(language, language)) ;
 			if(language.equals(languageView)){
 				questionLanguage_ = questionLanguage;
 				try{
 					listComments_.addAll(Arrays.asList(questionLanguage.getComments()));
 					listUserNames_.addAll(Arrays.asList(questionLanguage.getCommentBy()));
 					listDates_.addAll(Arrays.asList(questionLanguage.getDateComment()));
 				} catch (Exception e){
 					listComments_ = new ArrayList<String>();
 					listUserNames_ = new ArrayList<String>();
 					listDates_ = new ArrayList<Date>();
 				}
 				if(posCommentEdit >= 0){
 					pos = posCommentEdit;
 					((UIFormWYSIWYGInput)this.getChildById(COMMENT_CONTENT)).setValue(questionLanguage.getComments()[posCommentEdit]);
 				} else {
 					listComments_.add(" ");
 					listUserNames_.add(currentUser_);
 					listDates_.add(date_);
 					pos = listComments_.size() - 1;
 				}
 				questionContent = questionLanguage.getQuestion();
 				questionDetail = questionLanguage.getDetail();
 			}
 		}
 		
 		sProvider.close();
 	}
 	
 	public void setLink(String link) { this.link_ = link;}
 	
 	public String getLink() {return link_;}
 
 	static public class CancelActionListener extends EventListener<UICommentForm> {
     public void execute(Event<UICommentForm> event) throws Exception {
     	UICommentForm commentForm = event.getSource() ;
     	UIFAQPortlet portlet = commentForm.getAncestorOfType(UIFAQPortlet.class) ;
       UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
       popupAction.deActivate() ;
       event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
     }
   }
 	
 	static public class SaveActionListener extends EventListener<UICommentForm> {
 		public void execute(Event<UICommentForm> event) throws Exception {
 			UICommentForm commentForm = event.getSource() ;
 			String comment = ((UIFormWYSIWYGInput)commentForm.getChildById(commentForm.COMMENT_CONTENT)).getValue();
 			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
 			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
 			UIFAQPortlet portlet = commentForm.getAncestorOfType(UIFAQPortlet.class) ;
       UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
       UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
 			try{
 				commentForm.question_ = faqService_.getQuestionById(commentForm.question_.getId(), sessionProvider);
 				
 				//link
 	      String link = commentForm.getLink().replaceFirst("UICommentForm", "UIQuestions").replaceFirst("Cancel", "ViewQuestion").replaceAll("&amp;", "&");
 	      String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
 	      String portalName = "/" + Util.getUIPortal().getName() ;
 	      if(link.indexOf(portalName) > 0) {
 			    if(link.indexOf(portalName + "/" + selectedNode) < 0){
 			      link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
 			    }									
 				}	
 				PortalRequestContext portalContext = Util.getPortalRequestContext();
 				String url = portalContext.getRequest().getRequestURL().toString();
 				url = url.replaceFirst("http://", "") ;
 				url = url.substring(0, url.indexOf("/")) ;
 				url = "http://" + url;
 				String path = "" ;
 				if(FAQUtils.isFieldEmpty(commentForm.question_.getId())) path = questions.getPathService(commentForm.question_.getCategoryId())+"/"+commentForm.question_.getCategoryId() ;
 				else path = questions.getPathService(commentForm.question_.getCategoryId())+"/"+commentForm.question_.getCategoryId() ;
 				link = link.replaceFirst("OBJECTID", path);
 				link = url + link;
 				commentForm.question_.setLink(link) ;
 				ValidatorDataInput validatorDataInput = new ValidatorDataInput();
 				if(comment != null && comment.trim().length() > 0 && validatorDataInput.fckContentIsNotEmpty(comment)){
 					commentForm.listComments_.set(commentForm.pos, comment);
 					if(commentForm.questionLanguage_ == null){
 						commentForm.question_.setComments(commentForm.listComments_.toArray(new String[]{}));
 						commentForm.question_.setCommentBy(commentForm.listUserNames_.toArray(new String[]{}));
 						commentForm.question_.setDateComment(commentForm.listDates_.toArray(new Date[]{}));
 						faqService_.saveQuestion(commentForm.question_, false, sessionProvider, commentForm.faqSetting_);
 					} else {
 						commentForm.questionLanguage_.setComments(commentForm.listComments_.toArray(new String[]{}));
 						commentForm.questionLanguage_.setCommentBy(commentForm.listUserNames_.toArray(new String[]{}));
 						commentForm.questionLanguage_.setDateComment(commentForm.listDates_.toArray(new Date[]{}));
 						
 						MultiLanguages multiLanguages = new MultiLanguages();
 						multiLanguages.addLanguage(faqService_.getQuestionNodeById(commentForm.question_.getId(), sessionProvider), commentForm.questionLanguage_) ;
 					}
 					if(commentForm.isAddNew) {
 						String pathTopic = commentForm.question_.getPathTopicDiscuss();
						ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
						String []ids = pathTopic.split("/");
						Post post = new Post();
						post.setOwner(commentForm.currentUser_);
						post.setIcon("ViewIcon");
						post.setName("Re: " + commentForm.question_.getQuestion());
						post.setMessage(comment);
						forumService.savePost(sessionProvider, ids[0], ids[1], ids[2], post, true, "");
 					}
 				} else {
 					UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
 	        uiApplication.addMessage(new ApplicationMessage("UICommentForm.msg.comment-is-null", null, ApplicationMessage.WARNING)) ;
 	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
 	        return;
 				}
 			} catch(Exception e){
 				e.printStackTrace();
 				UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
         uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
         event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
 			}
 			sessionProvider.close();
       questions.setIsNotChangeLanguage() ;
       event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
       popupAction.deActivate() ;
       event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
 		}
 	}
 	
 }
