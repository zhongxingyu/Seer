 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2011 Jeroen Steenbeeke and Ties van de Ven
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
 
 import java.util.List;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.odlabs.wiquery.ui.dialog.Dialog;
 import org.odlabs.wiquery.ui.tabs.Tabs;
 
 import wicket.contrib.tinymce.TinyMceBehavior;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.auth.TysanRankSecured;
 import com.tysanclan.site.projectewok.beans.RoleService;
 import com.tysanclan.site.projectewok.components.MemberListItem;
 import com.tysanclan.site.projectewok.components.TysanTinyMCESettings;
 import com.tysanclan.site.projectewok.entities.Rank;
 import com.tysanclan.site.projectewok.entities.Role;
 import com.tysanclan.site.projectewok.entities.Role.RoleType;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.dao.RoleDAO;
 import com.tysanclan.site.projectewok.entities.dao.UserDAO;
 import com.tysanclan.site.projectewok.entities.dao.filters.RoleFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.UserFilter;
 
 /**
  * @author Jeroen Steenbeeke
  */
 @TysanRankSecured(Rank.CHANCELLOR)
 public class RolesManagementPage extends AbstractMemberPage {
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private RoleDAO roleDAO;
 
 	/**
 	 * 
 	 */
 	public RolesManagementPage() {
 		super("Roles management");
 
 		Tabs tabs = new Tabs("tabs");
 
 		RoleFilter filter = new RoleFilter();
		filter.addOrderBy("roleType", true);
 		filter.addOrderBy("name", true);
 
 		List<Role> roles = roleDAO.findByFilter(filter);
 
 		tabs.add(new ListView<Role>("roles", ModelMaker.wrap(roles)) {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private UserDAO userDAO;
 
 			@Override
 			protected void populateItem(ListItem<Role> item) {
 				Role role = item.getModelObject();
 
 				item.add(new Label("name", role.getName()));
 
 				if (role.getAssignedTo() != null) {
 					item.add(new MemberListItem("member", role.getAssignedTo()));
 				} else {
 					item.add(new Label("member", "-"));
 				}
 
 				Dialog reassignDialog = new Dialog("assigndialog");
 
 				Form<Role> reassignForm = new Form<Role>("assign", ModelMaker
 						.wrap(role)) {
 					private static final long serialVersionUID = 1L;
 
 					@SpringBean
 					private RoleService roleService;
 
 					/**
 					 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
 					 */
 					@SuppressWarnings("unchecked")
 					@Override
 					protected void onSubmit() {
 						DropDownChoice<User> userChoice = (DropDownChoice<User>) get("user");
 
 						User user = userChoice.getModelObject();
 
 						if (user != null) {
 							roleService.assignTo(getUser().getId(),
 									getModelObject().getId(), user.getId());
 							setResponsePage(new RolesManagementPage());
 						}
 					}
 				};
 
 				reassignDialog.setAutoOpen(false);
 				reassignDialog.setTitle("Assign role " + role.getName());
 
 				UserFilter f = new UserFilter();
 				f.addRank(Rank.CHANCELLOR);
 				f.addRank(Rank.SENATOR);
 				f.addRank(Rank.TRUTHSAYER);
 				f.addRank(Rank.REVERED_MEMBER);
 				f.addRank(Rank.SENIOR_MEMBER);
 				f.addRank(Rank.FULL_MEMBER);
 
 				List<User> users = userDAO.findByFilter(f);
 
 				reassignForm.add(new DropDownChoice<User>("user", ModelMaker
 						.wrap(users.get(0), true), ModelMaker.wrap(users))
 						.setNullValid(false));
 
 				reassignDialog.add(reassignForm);
 
 				item.add(reassignDialog);
 
 				AjaxLink<Dialog> reassignLink = new AjaxLink<Dialog>(
 						"reassign", new Model<Dialog>(reassignDialog)) {
 					private static final long serialVersionUID = 1L;
 
 					/**
 					 * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
 					 */
 					@Override
 					public void onClick(AjaxRequestTarget target) {
 						Dialog dlg = getModelObject();
 
 						if (target != null) {
 							dlg.open(target);
 						}
 
 					}
 
 				};
 
 				reassignLink.add(new ContextImage("icon",
 						"images/icons/vcard.png"));
 
 				reassignLink.setVisible(role.isReassignable());
 
 				Link<Role> editLink = new Link<Role>("edit", ModelMaker
 						.wrap(role)) {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick() {
 						setResponsePage(new EditRolePage(getModelObject()));
 
 					}
 
 				};
 
 				editLink.add(new ContextImage("icon",
 						"images/icons/vcard_edit.png"));
 
 				editLink.setVisible(role.isReassignable());
 
 				Dialog deletedialog = new Dialog("deletedialog");
 				deletedialog.setTitle("Delete role " + role.getName());
 				deletedialog.setAutoOpen(false);
 
 				deletedialog.add(new Label("name", role.getName()));
 
 				Link<Role> yesDeleteLink = new Link<Role>("yes", ModelMaker
 						.wrap(role)) {
 					private static final long serialVersionUID = 1L;
 
 					@SpringBean
 					private RoleService roleService;
 
 					@Override
 					public void onClick() {
 						roleService.deleteRole(getUser(), getModelObject());
 						setResponsePage(new RolesManagementPage());
 					}
 
 				};
 
 				yesDeleteLink.add(new ContextImage("icon",
 						"images/icons/tick.png"));
 
 				deletedialog.add(yesDeleteLink);
 
 				AjaxLink<Dialog> noDeleteLink = new AjaxLink<Dialog>("no",
 						new Model<Dialog>(deletedialog)) {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick(AjaxRequestTarget target) {
 						Dialog dlg = getModelObject();
 
 						if (target != null) {
 							dlg.close(target);
 						}
 
 					}
 
 				};
 
 				noDeleteLink.add(new ContextImage("icon",
 						"images/icons/cross.png"));
 
 				deletedialog.add(noDeleteLink);
 
 				item.add(deletedialog);
 
 				AjaxLink<Dialog> deleteLink = new AjaxLink<Dialog>("delete",
 						new Model<Dialog>(deletedialog)) {
 					private static final long serialVersionUID = 1L;
 
 					/**
 					 * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
 					 */
 					@Override
 					public void onClick(AjaxRequestTarget target) {
 						Dialog dlg = getModelObject();
 
 						if (target != null) {
 							dlg.open(target);
 						}
 					}
 
 				};
 
 				deleteLink.add(new ContextImage("icon",
 						"images/icons/vcard_delete.png"));
 
 				deleteLink.setVisible(role.isReassignable());
 
 				item.add(new WebMarkupContainer("nonreassignable")
 						.setVisible(!role.isReassignable()));
 				item.add(new WebMarkupContainer("noneditable").setVisible(!role
 						.isReassignable()));
 				item.add(new WebMarkupContainer("nondeletable")
 						.setVisible(!role.isReassignable()));
 
 				item.add(reassignLink);
 				item.add(deleteLink);
 				item.add(editLink);
 			}
 
 		});
 
 		Form<Role> addRoleForm = new Form<Role>("addRoleForm") {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private RoleService roleService;
 
 			/**
 			 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
 			 */
 			@SuppressWarnings("unchecked")
 			@Override
 			protected void onSubmit() {
 				TextField<String> nameField = (TextField<String>) get("name");
 				TextArea<String> descriptionArea = (TextArea<String>) get("description");
 
 				String name = nameField.getModelObject();
 				String description = descriptionArea.getModelObject();
 
 				roleService.createRole(getUser(), name, description,
 						RoleType.NORMAL);
 
 				setResponsePage(new RolesManagementPage());
 			}
 
 		};
 
 		addRoleForm.add(new TextField<String>("name", new Model<String>("")));
 		addRoleForm.add(new TextArea<String>("description", new Model<String>(
 				"")).add(new TinyMceBehavior(new TysanTinyMCESettings())));
 
 		tabs.add(addRoleForm);
 
 		add(tabs);
 	}
 }
