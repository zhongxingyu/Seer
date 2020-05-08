 package org.gwtapp.startapp.server;
 
 import org.gwtapp.io.server.IOHtmlRpcSerializer;
 import org.gwtapp.startapp.rpc.data.user.register.UserRegister;
 import org.gwtapp.startapp.rpc.data.user.register.UserRegisterModelImpl;
 
 public class UserRegisterBean implements UserRegister {
 	
 	private final UserRegisterModelImpl model = new UserRegisterModelImpl();
 
 	public String getAsHtmlRpc() {
 		IOHtmlRpcSerializer bean = new IOHtmlRpcSerializer();
 		bean.setValue(model);
 		return bean.getValue();
 	}
 
 	@Override
 	public String getEmail() {
 		return model.getEmail();
 	}
 
 	@Override
 	public String getLogin() {
 		return model.getLogin();
 	}
 
 	@Override
 	public String getPassword() {
 		return model.getPassword();
 	}
 
 	@Override
 	public void setEmail(String email) {
 		model.setEmail(email);
 	}
 
 	@Override
 	public void setLogin(String login) {
 		model.setLogin(login);
 	}
 
 	@Override
 	public void setPassword(String password) {
 		model.setPassword(password);
 	}
 }
