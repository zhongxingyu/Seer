 package org.gwtapp.startapp.server;
 
 import org.gwtapp.extension.user.client.data.ReCaptchaUser;
 import org.gwtapp.extension.user.client.data.ReCaptchaUserImpl;
 import org.gwtapp.extension.user.client.data.User;
 import org.gwtapp.extension.user.client.data.UserImpl;
 import org.gwtapp.extension.user.client.data.UserPassword;
 import org.gwtapp.extension.user.client.data.UserPasswordImpl;
 import org.gwtapp.extension.user.server.local.service.ReCaptchaVerifyImpl;
 import org.gwtapp.extension.user.server.local.stub.ReCaptchaPrivateKey;
 import org.gwtapp.extension.user.server.local.stub.ReCaptchaVerify;
 import org.gwtapp.extension.user.server.local.stub.UserAdd;
 import org.gwtapp.extension.user.server.remote.service.ReCaptchaUserRemoteService;
 import org.gwtapp.startapp.server.service.UserServiceImpl;
 import org.gwtapp.startapp.server.servlet.EnumValidationCssServlet;
 import org.gwtapp.startapp.server.servlet.GroupValidationCssServlet;
 
 import com.google.inject.servlet.ServletModule;
 import com.wideplay.warp.jpa.JpaUnit;
 import com.wideplay.warp.persist.PersistenceService;
 import com.wideplay.warp.persist.UnitOfWork;
 
 public class StartAppModule extends ServletModule {
 
 	@Override
 	protected void configureServlets() {
 		setupTransactional();
 		setupServlets();
 		setupServices();
 		setupModels();
 	}
 
 	private void setupTransactional() {
 		install(PersistenceService.usingJpa().across(UnitOfWork.TRANSACTION)
 				.buildModule());
 		bindConstant().annotatedWith(JpaUnit.class).to("derby-startapp-manual-test-x");
 	}
 
 	private void setupServlets() {
 		serve("/gwt.startappmanualtestentry/extension.recaptchauser.rpc").with(
 				ReCaptchaUserRemoteService.class);
 		serve("/css/validation-by-group.css").with(
 				GroupValidationCssServlet.class);
 		serve("/css/validation-by-enum.css").with(
 				EnumValidationCssServlet.class);
 	}
 
 	private void setupServices() {
 		bind(UserAdd.class).to(UserServiceImpl.class);
 		bind(ReCaptchaUser.class).to(ReCaptchaUserImpl.class);
 		bind(ReCaptchaVerify.class).to(ReCaptchaVerifyImpl.class);
 		bind(ReCaptchaPrivateKey.class).to(ReCaptchaPrivateKeyProvider.class);
 	}
 
 	private void setupModels() {
 		bind(User.class).to(UserImpl.class);
 		bind(UserPassword.class).to(UserPasswordImpl.class);
 	}
 
 	private static class ReCaptchaPrivateKeyProvider implements
 			ReCaptchaPrivateKey {
 		@Override
 		public String getPrivateKey() {
			return System.getenv("StartAppReCaptchaPrivateKey");
 		}
 	}
 }
