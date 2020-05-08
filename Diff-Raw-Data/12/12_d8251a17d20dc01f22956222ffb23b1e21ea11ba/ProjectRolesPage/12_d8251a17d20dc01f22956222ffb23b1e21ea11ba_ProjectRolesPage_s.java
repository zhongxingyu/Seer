 package net.alpha01.jwtest.pages.project;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.alpha01.jwtest.beans.Role;
 import net.alpha01.jwtest.beans.User;
 import net.alpha01.jwtest.dao.ProjectMapper;
 import net.alpha01.jwtest.dao.ProjectMapper.ProjectUserRoles;
 import net.alpha01.jwtest.dao.RoleMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.UserMapper;
 import net.alpha01.jwtest.pages.HomePage;
 import net.alpha01.jwtest.pages.LayoutPage;
 
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 @AuthorizeInstantiation(value={Roles.ADMIN,"PROJECT_ADMIN"})
 public class ProjectRolesPage extends LayoutPage {
 	
 	class UserRoleForm extends Form<Role>{
 		private static final long serialVersionUID = 1L;
 		List<Role> allRoles;
 		private ArrayList<Role> selectedRoles;
 		private  IModel<User> userModel;
 		public UserRoleForm(String id, List<Role> allRoles, IModel<User> model) {
 			super(id);
 			this.allRoles=allRoles;
 			this.userModel=model;
 			SqlSessionMapper<ProjectMapper> sesMapper = SqlConnection.getSessionMapper(ProjectMapper.class);
 			selectedRoles = (ArrayList<Role>) sesMapper.getMapper().getRoles(new ProjectUserRoles(ProjectRolesPage.this.getSession().getCurrentProject().getId(), model.getObject().getId()));
 			sesMapper.close();
 			add (new Label("userName",model.getObject().getUsername()));
 			add (new Label("name",model.getObject().getName()));
 			add(new ListMultipleChoice<Role>("rolesFld",new Model<ArrayList<Role>>(selectedRoles),allRoles));
 		}
 		
 		@Override
 		protected void onSubmit() {
 			SqlSessionMapper<ProjectMapper> sesMapper = SqlConnection.getSessionMapper(ProjectMapper.class);
			sesMapper.getMapper().deassociateRoles(ProjectRolesPage.this.getSession().getCurrentProject());
 			boolean sqlOk=true;
 			for (Role selRole : selectedRoles){
 				if (!sesMapper.getMapper().associateRole(new ProjectUserRoles(ProjectRolesPage.this.getSession().getCurrentProject().getId(), userModel.getObject().getId(), selRole.getId())).equals(1)){
 					sqlOk=false;
 				}
 			}
 			if (sqlOk){
 				sesMapper.commit();
 			}else{
 				sesMapper.rollback();
 				error("SQL Error");
 			}
 			sesMapper.close();
 		}
 	};
 	
 	public ProjectRolesPage() {
 		if (getSession().getCurrentProject() == null) {
 			error("Nessun progetto selezionato");
 			setResponsePage(HomePage.class);
 			return;
 		}
 		
 		SqlSessionMapper<RoleMapper> sesMapper = SqlConnection.getSessionMapper(RoleMapper.class);
 		final List<Role> allRoles = sesMapper.getMapper().getAll();
 		List<User> allUsers = sesMapper.getSqlSession().getMapper(UserMapper.class).getAll();
 		allUsers.remove(getSession().getUser());
 		sesMapper.close();
 		add (new ListView<User>("userRoleRow",allUsers){
 			private static final long serialVersionUID = 1L;
 			@Override
 			protected void populateItem(ListItem<User> item) {
 				item.add(new UserRoleForm("userRoleForm",allRoles,item.getModel()));
 			}
 		});
 	}
 }
