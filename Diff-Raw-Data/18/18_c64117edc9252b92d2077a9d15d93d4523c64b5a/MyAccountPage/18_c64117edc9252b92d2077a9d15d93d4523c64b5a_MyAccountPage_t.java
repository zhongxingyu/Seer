 package de.atomfrede.mate.application.wicket.account;
 
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import de.atomfrede.mate.application.wicket.BasePage;
 import de.atomfrede.mate.application.wicket.model.AbstractEntityModel;
 import de.atomfrede.mate.domain.entities.user.User;
 import de.atomfrede.mate.service.user.UserService;
 
 public class MyAccountPage extends BasePage<User>{
 	
 	@SpringBean
 	UserService userService;
 
 	public MyAccountPage(){
 		super();
 		User mUser = userService.findById(getSession().getUser().entity.getId());
		add(new MyAccountPanel("myAccount", new AbstractEntityModel<User>(User.class, mUser.getId())));
		add(new MyPersonalDataPanel("myData", new AbstractEntityModel<User>(User.class, mUser.getId())));
		add(new MyNotificationsPanel("myNotifications", new AbstractEntityModel<User>(User.class, mUser.getId())));
 	}
 }
