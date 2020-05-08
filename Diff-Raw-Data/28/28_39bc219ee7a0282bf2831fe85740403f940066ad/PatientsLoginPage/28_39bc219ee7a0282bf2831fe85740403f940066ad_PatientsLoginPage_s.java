 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.radar.web.pages.login;
 
import org.patientview.radar.model.user.PatientUser;
import org.patientview.radar.service.UserManager;
import org.patientview.radar.web.RadarSecuredSession;
import org.patientview.radar.web.components.RadarRequiredDateTextField;
import org.patientview.radar.web.components.RadarRequiredPasswordTextField;
import org.patientview.radar.web.components.RadarRequiredTextField;
import org.patientview.radar.web.pages.BasePage;
import org.patientview.radar.web.pages.patient.srns.SrnsPatientPageReadOnly;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PatientsLoginPage extends BasePage {
 
     public static final String LOGIN_FAILED_MESSAGE = "Login failed";
     @SpringBean
     private UserManager userManager;
 
     public PatientsLoginPage() {
         // Patients log in form
         CompoundPropertyModel<PatientUser> model = new CompoundPropertyModel<PatientUser>(new PatientUser());
         final Model<String> passwordModel = new Model<String>();
 
         // components to update on ajax
         final List<Component> componentsToUpdateList = new ArrayList<Component>();
 
         Form<PatientUser> form = new Form<PatientUser>("form", model) {
             @Override
             protected void onSubmit() {
                 RadarSecuredSession session = RadarSecuredSession.get();
                 PatientUser user = getModelObject();
                 boolean loginFailed = false;
                PatientUser patientUser = userManager.getPatientUserWithUsername(user.getUsername(),
                        user.getDateOfBirth());
 
                 if (patientUser != null) {
                     if (session.signIn(user.getUsername(), passwordModel.getObject())) {
                         session.setUser(patientUser);
                         // If we haven't been diverted here from a page request (i.e. we clicked login),
                         // redirect to logged in page
                         setResponsePage(SrnsPatientPageReadOnly.class, SrnsPatientPageReadOnly.getParameters(
                                 patientUser.getRadarNumber()));
 
                     } else {
                         loginFailed = true;
                     }
                 } else {
                     loginFailed = true;
                 }
 
                 if (loginFailed) {
                     // Show that the login failed if we couldn't authenticate
                     error(LOGIN_FAILED_MESSAGE);
                 }
             }
         };
         add(form);
 
         // Add components to form
         form.add(new RadarRequiredTextField("username", form, componentsToUpdateList));
         RadarRequiredPasswordTextField password = new RadarRequiredPasswordTextField("password", form,
                 componentsToUpdateList);
         form.add(password);
         password.setModel(passwordModel);
 
        // Date of birth with picker
        DateTextField dateOfBirth = new RadarRequiredDateTextField("dateOfBirth",
                form, componentsToUpdateList);
        form.add(dateOfBirth);

         // Construct feedback panel
         final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback", new IFeedbackMessageFilter() {
             public boolean accept(FeedbackMessage feedbackMessage) {
                 String message = feedbackMessage.getMessage().toString();
                 return message.contains(LOGIN_FAILED_MESSAGE);
             }
         });
         form.add(feedbackPanel);
         componentsToUpdateList.add(feedbackPanel);
         feedbackPanel.setOutputMarkupPlaceholderTag(true);
 
         form.add(new IndicatingAjaxButton("submit") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
             }
         });
 
 
         // Add links for forgotten password and register
         add(new BookmarkablePageLink<PatientForgottenPasswordPage>("forgottenPasswordLink",
                 PatientForgottenPasswordPage.class));
     }
 }
