 /*
  * $Id: EditContactPage.java 634 2006-03-26 18:28:10 -0800 (Sun, 26 Mar 2006) ivaynberg $
  * $Revision: 634 $
  * $Date: 2006-03-26 18:28:10 -0800 (Sun, 26 Mar 2006) $
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.phonebook.web.page;
 
 import wicket.Page;
 import wicket.contrib.phonebook.Contact;
 import wicket.contrib.phonebook.ContactDao;
 import wicket.markup.html.form.Button;
 import wicket.markup.html.form.Form;
 import wicket.markup.html.form.RequiredTextField;
 import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.EmailAddressPatternValidator;
import wicket.markup.html.form.validation.StringValidator;
 import wicket.model.CompoundPropertyModel;
 import wicket.model.IModel;
 import wicket.spring.injection.annot.SpringBean;
 import wicket.util.collections.MicroMap;
 import wicket.util.string.interpolator.MapVariableInterpolator;
 
 /**
  * Edit the Contact. Display details if an existing contact, then persist them
  * if saved.
  * 
  * @author igor
  * 
  */
 public class EditContactPage extends BasePage {
 	private Page backPage;
 
 	@SpringBean
 	private ContactDao contactDao;
 
 	/**
 	 * Constructor. Create or edit the contact. Note that if you don't need the
 	 * page to be bookmarkable, you can use whatever constructor you need, such
 	 * as is done here.
 	 * 
 	 * @param backPage
 	 *            The page that the user was on before coming here
 	 * @param contactModel
 	 *            Model that contains the contact we will edit
 	 */
 	public EditContactPage(Page backPage, IModel contactModel) {
 		this.backPage = backPage;
 
		Contact contact = (Contact) contactModel.getObject(this);
 		Form form = new Form("contactForm", new CompoundPropertyModel(contact));
 		add(form);
 
 		form.add(new RequiredTextField("firstname").add(StringValidator
 				.maximumLength(32)));
 
 		form.add(new RequiredTextField("lastname").add(StringValidator
 				.maximumLength(32)));
 
 		form.add(new RequiredTextField("phone").add(StringValidator
 				.maximumLength(16)));
 
 		form.add(new TextField("email").add(StringValidator.maximumLength(128))
				.add(EmailAddressPatternValidator.getInstance()));
 
 		form.add(new Button("cancel") {
 			public void onSubmit() {
 				String msg = getLocalizer().getString("status.cancel", this);
 				getSession().info(msg);
 				setResponsePage(EditContactPage.this.backPage);
 			}
 		}.setDefaultFormProcessing(false));
 
 		form.add(new Button("save") {
 			public void onSubmit() {
 				Contact contact = (Contact) getForm().getModelObject();
 				contactDao.save(contact);
 
 				String msg = MapVariableInterpolator.interpolate(getLocalizer()
 						.getString("status.save", this), new MicroMap("name",
 						contact.getFullName()));
 
 				getSession().info(msg);
 
 				setResponsePage(EditContactPage.this.backPage);
 			}
 		});
 	}
 }
