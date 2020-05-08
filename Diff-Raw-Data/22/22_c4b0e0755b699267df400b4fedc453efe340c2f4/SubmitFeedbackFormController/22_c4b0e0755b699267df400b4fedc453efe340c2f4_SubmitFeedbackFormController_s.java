 /*
  * Created on Feb 5, 2008
  *
  * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
  * (See licensing and redistribution disclosures at end of this file.)
  * 
  */
 package org.jasig.portlets.FeedbackPortlet.web;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletRequest;
 import javax.portlet.RenderRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jasig.portlets.FeedbackPortlet.FeedbackItem;
 import org.jasig.portlets.FeedbackPortlet.PortletUserPropertiesResolver;
 import org.jasig.portlets.FeedbackPortlet.UserProperties;
 import org.jasig.portlets.FeedbackPortlet.dao.FeedbackStore;
 import org.jasig.portlets.FeedbackPortlet.service.FeedbackSubmissionListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.portlet.ModelAndView;
 
 /**
  * SubmitFeedbackFormController allows a user to submit feedback via a portlet
  * form.
  * 
  * @author Jen Bourey
  */
 @Controller
 @RequestMapping("VIEW")
 public class SubmitFeedbackFormController {
 
 	private static Log log = LogFactory.getLog(SubmitFeedbackFormController.class);
 	private FeedbackStore feedbackStore;
 	
 	private int feedbackRows = 3;
 	private String feedbackWidth = "95%";
 	private int feedbackMaxChars = 500;
 
 	/*public SubmitFeedbackFormController() {
 		setCommandName("prefs");
 		setCommandClass(SubmitFeedbackForm.class);
 	}*/
 	
 	@Autowired
     public void setFeedbackRows(int feedbackRows) {
         this.feedbackRows = feedbackRows;
     }
 
 	@Autowired
     public void setFeedbackWidth(String feedbackWidth) {
         this.feedbackWidth = feedbackWidth;
     }
 
 	@Autowired
     public void setFeedbackMaxChars(int feedbackMaxChars) {
         this.feedbackMaxChars = feedbackMaxChars;
     }
 
     @RequestMapping(method = RequestMethod.POST)
 	protected void onSubmit(ActionRequest request,
             ActionResponse response,
             @ModelAttribute("submitFeedbackForm") SubmitFeedbackForm form) {
		/*processFormSubmission(ActionRequest request,
            ActionResponse response, Object command, BindException errors)*/
 		
 		// construct a new feedback object from the form data
 		FeedbackItem feedback = new FeedbackItem();
 		String text = form.getFeedback().trim();
 		if (text.length() > feedbackMaxChars) {
 		    text = text.substring(0, feedbackMaxChars);
 		}
 		feedback.setFeedback(text);
 		feedback.setUseragent(form.getUseragent());
 		feedback.setFeedbacktype(form.getLike());
 		feedback.setTabname(form.getTabname());
 		
 		// add information about the user to the feedback object
 		if (request.getRemoteUser() != null) {
 			UserProperties user = userPropertiesResolver.getProperties(request);
 			feedback.setUserrole(user.getUserrole());
 			if (form.getAnonymous() == null) {
 				feedback.setUserid(request.getRemoteUser());
 				feedback.setUsername(user.getUsername());
 				feedback.setUseremail(user.getUseremail());
 			}
 		}
 		
 		if (log.isDebugEnabled())
 			log.debug("User submitted new feedback item " + feedback.toString());
 		
 		// perform any requested listener actions
 		for (FeedbackSubmissionListener listener : feedbackSubmissionListeners) {
 			listener.performAction(feedback);
 		}
 		
 		// save the feedback to the data store
 		feedbackStore.storeFeedback(feedback);
		
 	}
 
     @SuppressWarnings("unchecked")
    @RequestMapping
     public ModelAndView getView(RenderRequest request) throws Exception {
 
        //Map<String,Object> map = super.referenceData(request, command, errors);
         Map<String,Object> map = new HashMap<String,Object>();
         
         // Add settings from feedback.properties
         map.put("feedbackRows", feedbackRows);
         map.put("feedbackWidth", feedbackWidth);
         map.put("feedbackMaxChars", feedbackMaxChars);
         
         if (request.getParameter("feedbackTabName") != null) {
             map.put("tabName", request.getParameter("feedbackTabName"));
         }
         
         // Adds a blank form to satisfy mapping
         map.put("submitFeedbackForm", new SubmitFeedbackForm());
 
        return new ModelAndView("submitFeedback", map);
 
     }
 
     @Autowired
 	public void setFeedbackStore(FeedbackStore feedbackStore) {
 		this.feedbackStore = feedbackStore;
 	}
 
     @Autowired
 	private PortletUserPropertiesResolver userPropertiesResolver;
 	public void setUserPropertiesResolver(PortletUserPropertiesResolver resolver) {
 		this.userPropertiesResolver = resolver;
 	}
 	
 	@Autowired
 	private List<FeedbackSubmissionListener> feedbackSubmissionListeners;
 	public void setFeedbackSubmissionListeners(List<FeedbackSubmissionListener> listeners) {
 		this.feedbackSubmissionListeners = listeners;
 	}
 
 }
 
 
 /*
  * SubmitFeedbackFormController.java
  * 
  * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
  * 
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
  * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * Redistribution and use of this software in source or binary forms, with or
  * without modification, are permitted, provided that the following conditions
  * are met.
  * 
  * 1. Any redistribution must include the above copyright notice and disclaimer
  * and this list of conditions in any related documentation and, if feasible, in
  * the redistributed software.
  * 
  * 2. Any redistribution must include the acknowledgment, "This product includes
  * software developed by Yale University," in any related documentation and, if
  * feasible, in the redistributed software.
  * 
  * 3. The names "Yale" and "Yale University" must not be used to endorse or
  * promote products derived from this software.
  */
