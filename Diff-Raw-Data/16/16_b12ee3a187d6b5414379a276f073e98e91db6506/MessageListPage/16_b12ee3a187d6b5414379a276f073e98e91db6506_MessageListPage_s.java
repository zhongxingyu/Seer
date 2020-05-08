 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2013 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.pages.member;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.auth.TysanMemberSecured;
 import com.tysanclan.site.projectewok.beans.MessageService;
 import com.tysanclan.site.projectewok.beans.UserService;
 import com.tysanclan.site.projectewok.components.BBCodeTextArea;
 import com.tysanclan.site.projectewok.components.IconLink;
 import com.tysanclan.site.projectewok.components.IconLink.DefaultClickResponder;
 import com.tysanclan.site.projectewok.dataaccess.FilterProvider;
 import com.tysanclan.site.projectewok.entities.Conversation;
 import com.tysanclan.site.projectewok.entities.ConversationParticipation;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.dao.ConversationDAO;
 import com.tysanclan.site.projectewok.entities.dao.filters.ConversationFilter;
 
 /**
  * @author Jeroen Steenbeeke
  */
 @TysanMemberSecured
 public class MessageListPage extends AbstractMemberPage {
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private UserService userService;
 
 	private IModel<User> firstSelect;
 
 	private int selectedTab;
 
 	public MessageListPage() {
 		this(null);
 	}
 
 	public MessageListPage(User recipient) {
 		super("Messages");
 
 		firstSelect = ModelMaker.wrap(recipient);
 
 		this.selectedTab = recipient != null ? 1 : 0;
 
 		ConversationFilter filter = new ConversationFilter();
 		filter.addParticipant(getUser());
 		filter.setSortByLastResponse(true);
 
 		DataView<Conversation> messageView = new DataView<Conversation>(
 				"messages", FilterProvider.of(ConversationDAO.class, filter)) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(Item<Conversation> item) {
 				Conversation conv = item.getModelObject();
 
 				ConversationParticipation cp = null;
 
 				StringBuilder builder = new StringBuilder();
 				int i = 0;
 				for (ConversationParticipation cp_ : conv.getParticipants()) {
 					if (cp_.getUser().equals(getUser())) {
 						cp = cp_;
 					} else {
 						i++;
 						if (i == 3) {
 							builder.append(" and others");
 						}
 						if (i <= 2) {
 							if (i > 1) {
 								builder.append(", ");
 							}
 							builder.append(cp_.getUser().getUsername());
 						}
 					}
 				}
 
 				item.add(new ContextImage("unread",
 						"images/icons/email_error.png").setVisible(cp != null
 						&& cp.getReadMessages().size() != conv.getMessages()
 								.size()));
 
 				Link<ConversationParticipation> link = new Link<ConversationParticipation>(
 						"link", ModelMaker.wrap(cp)) {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick() {
 						setResponsePage(new ViewConversationPage(
 								getModelObject()));
 					}
 				};
 
 				link.add(new Label("title", conv.getTitle()));
 
 				item.add(link);
 				item.add(new Label("participants", builder.toString()));
 				item.add(new IconLink.Builder("images/icons/delete.png",
 						new DefaultClickResponder<ConversationParticipation>(
 								ModelMaker.wrap(cp)) {
 
 							private static final long serialVersionUID = 1L;
 
 							@SpringBean
 							private MessageService messageService;
 
 							@Override
 							public void onClick() {
 								messageService
 										.ceaseParticipation(getModelObject());
 								setResponsePage(new MessageListPage());
 							}
 
 						}).newInstance("remove"));
 
 			}
 
 		};
 
 		messageView.setItemsPerPage(15);
 
 		add(messageView);
 
 		add(new PagingNavigator("paging", messageView));
 
 		add(createNewMessageForm("newMessageForm"));
 	}
 
 	@Override
 	protected Integer getAutoTabIndex() {
 		return selectedTab;
 	}
 
 	private Form<Conversation> createNewMessageForm(String id) {
 		final TextField<String> titleField = new TextField<String>("title",
 				new Model<String>(""));
 		titleField.setRequired(true);
 
 		List<User> users = userService.getMembers();
 		Collections.sort(users, new User.CaseInsensitiveUserComparator());
 
 		final ListMultipleChoice<User> userSelect = new ListMultipleChoice<User>(
 				"userselect", new IModel<Collection<User>>() {
 					private static final long serialVersionUID = 1L;
 
 					private IModel<List<User>> wrapped = null;
 
 					@Override
 					public Collection<User> getObject() {
 						if (wrapped == null) {
 							List<User> start = new LinkedList<User>();
 							if (firstSelect.getObject() != null) {
 								start.add(firstSelect.getObject());
 							}
 
 							wrapped = ModelMaker.wrap(start);
 						}
 
 						return wrapped.getObject();
 					}
 
 					@Override
 					public void setObject(Collection<User> object) {
 
 						List<User> userList = new LinkedList<User>();
 						userList.addAll(object);
 						wrapped = ModelMaker.wrap(userList);
 					}
 
 					@Override
 					public void detach() {
 						if (wrapped != null) {
 							wrapped.detach();
 						}
 
 					}
 
 				}, ModelMaker.wrap(users));
 
 		final TextArea<String> editor = new BBCodeTextArea("messagecontent", "");
 		editor.setRequired(true);
 
 		Form<Conversation> newMessageForm = new Form<Conversation>(id) {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private MessageService _messageService;
 
 			@Override
 			protected void onSubmit() {
 
 				List<User> selectedUsers = new LinkedList<User>();
 				selectedUsers.addAll(userSelect.getModelObject());
 
 				_messageService.createConversation(getUser(), selectedUsers,
 						titleField.getModel().getObject(), editor.getModel()
 								.getObject());
 
 				setResponsePage(new MessageListPage());
 
 			}
 
 		};
 
 		newMessageForm.add(userSelect);
 
 		newMessageForm.add(titleField);
 
 		editor.setOutputMarkupId(true);
 		editor.setMarkupId("editor");
 		newMessageForm.add(editor);
 
 		return newMessageForm;
 	}
 }
