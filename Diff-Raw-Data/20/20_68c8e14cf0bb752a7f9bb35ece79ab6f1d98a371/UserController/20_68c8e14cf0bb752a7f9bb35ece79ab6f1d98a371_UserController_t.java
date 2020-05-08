 package br.com.zeng.controller;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.zeng.dao.UserDao;
 import br.com.zeng.model.User;
 import br.com.zeng.session.UserSession;
 import br.com.zeng.validator.UserValidator;
 
 @Resource
 public class UserController {
 	
 	private final Result result;
 	private final UserSession userSession;
 	private final UserValidator userValidator;
 	private final UserDao userDao;
 
 	public UserController(Result result, UserDao userDao, UserSession userSession, UserValidator userValidator) {
 		this.result = result;
 		this.userDao = userDao;
 		this.userSession = userSession;
 		this.userValidator = userValidator;
 	}
 
 	@Get("/")
	public void home() {
 	}
 	
 	@Post("/register")
 	public void register(User user) {
 		userDao.register(user);
 		logIn(user);
 	}
 	
 	@Post("/login/")
 	public void logIn(User user) {
 		User registredUser = userDao.getRegistredUser(user.getEmail(),user.getPassword()) ;
 		userValidator.validate(registredUser);
 		userSession.logIn(registredUser);
 		result.redirectTo(ProjectController.class).listProjects();
 	}
 
 	
 }
