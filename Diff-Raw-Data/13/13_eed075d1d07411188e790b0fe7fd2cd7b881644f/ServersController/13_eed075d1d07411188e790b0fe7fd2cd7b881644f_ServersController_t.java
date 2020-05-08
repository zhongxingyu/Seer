 package br.com.ahrpius.blank.controller;
 
 import br.com.ahrpius.blank.dao.ServerDao;
 import br.com.ahrpius.blank.interceptor.UserInfo;
 import br.com.ahrpius.blank.model.Server;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 
 @Resource
 public class ServersController {
 	
 	private final Result result;
 	private final Validator validator;
 	private final UserInfo userInfo;
 	private final ServerDao serverDao;
 
	public ServersController(Result result, Validator validator, 
 			UserInfo userInfo, ServerDao serverDao) {
 		this.result = result;
 		this.validator = validator;
 		this.userInfo = userInfo;
 		this.serverDao = serverDao;
 	}
 	
 	@Get("/servers/{server.id}")
 	public Server view(Server server) {
 		return serverDao.find(server.getId());
 	}
 
 }
