 package com.mymed.controller.core.manager.registration;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.authentication.IAuthenticationManager;
 import com.mymed.controller.core.manager.pubsub.IPubSubManager;
 import com.mymed.controller.core.manager.pubsub.PubSubManager;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.HashFunction;
 import com.mymed.utils.Mail;
 
 /**
  * 
  * @author lvanni
  */
 public class RegistrationManager implements IRegistrationManager {
 
 	private IPubSubManager pubSubManager;
 	private IAuthenticationManager authenticationManager;
 	private Gson gson;
 
 	public RegistrationManager() throws InternalBackEndException {
 		pubSubManager = new PubSubManager();
 		authenticationManager = new AuthenticationManager();
 		gson = new Gson();
 	}
 
 	@Override
 	public void create(MUserBean user, MAuthenticationBean authentication)
 			throws AbstractMymedException {
 
 		// PUBLISH A NEW REGISTATION PENDING TASK
 		List<MDataBean> dataList = new ArrayList<MDataBean>();
 		try {
 			MDataBean dataUser = new MDataBean();
 			dataUser.setKey("user");
 			dataUser.setOntologyID("0");
 			dataUser.setValue(gson.toJson(user));
 	
 			MDataBean dataAuthentication = new MDataBean();
 			dataAuthentication.setKey("authentication");
 			dataAuthentication.setOntologyID("0");
 			dataAuthentication.setValue(gson.toJson(authentication));
 		
 			dataList.add(dataUser);
 			dataList.add(dataAuthentication);
 		} catch (final JsonSyntaxException e) {
 			throw new InternalBackEndException("User/Authentication jSon format is not valid");
 		}
 
 		final HashFunction h = new HashFunction("myMed");
 		String accessToken = h.SHA1ToString(user.getLogin() + System.currentTimeMillis());
 
 		pubSubManager.create("myMed", accessToken, accessToken, user, dataList);
 
 		// SEND MAIL
		String content = "Bienvenu sur myMed.\n"
				+ "Pour finaliser votre inscription cliquez sur le lien: http://mymed2.sophia.inria.fr/mobile?registration=ok&accessToken=" + accessToken + "\n";
		content += "\n------\nL'équipe myMed";
		new Mail("mymed.subscribe@gmail.com", user.getEmail(), "Bienvenu sur myMed", content);
 	}
 
 	@Override
 	public void read(String accessToken) throws AbstractMymedException {
 		// Reteive the user profile
 		String userID = pubSubManager.read("myMed", accessToken).get(0).get("user");
 		List<Map<String, String>> dataList = pubSubManager.read("myMed", accessToken, userID);
 		MUserBean userBean = null;
 		MAuthenticationBean authenticationBean = null;
 
 		try{
 			for(Map<String, String> dataEntry : dataList){
 				if(dataEntry.get("key").equals("user")){
 					userBean = gson.fromJson(dataEntry.get("value"), MUserBean.class);
 				} else if(dataEntry.get("key").equals("authentication")){
 					authenticationBean = gson.fromJson(dataEntry.get("value"), MAuthenticationBean.class);
 				}
 			}
 		} catch (final JsonSyntaxException e) {
 			throw new InternalBackEndException("User/Authentication jSon format is not valid");
 		}
 
 		// register the new user
 		if(userBean != null && authenticationBean != null) {
 			authenticationManager.create(userBean, authenticationBean);
 			// delete pending tasks
 			pubSubManager.delete("mymed", accessToken, userBean);
 		}
 	}
 
 }
