 package org.gwtapp.extension.user.server.local.service;
 
 import net.tanesha.recaptcha.ReCaptchaImpl;
 import net.tanesha.recaptcha.ReCaptchaResponse;
 
 import org.apache.log4j.Logger;
 import org.gwtapp.extension.user.client.data.ReCaptcha;
 import org.gwtapp.extension.user.server.local.stub.ReCaptchaVerify;
 
 /*
  * http://code.google.com/apis/recaptcha/docs/verify.html
  */
 public class ReCaptchaVerifyImpl implements ReCaptchaVerify {
 
 	private final static Logger log = Logger
 			.getLogger(ReCaptchaVerifyImpl.class);
 
 	@Override
 	public boolean verify(ReCaptcha reCaptcha, String privateKey,
 			String remoteIP) {
 		ReCaptchaImpl service = new ReCaptchaImpl();
 		service.setPrivateKey(privateKey);
 		ReCaptchaResponse response = service.checkAnswer(remoteIP,
 				reCaptcha.getChallenge(), reCaptcha.getResponse());
 		if (!response.isValid()) {
 			log.error(response.getErrorMessage());
 		}
 		return response.isValid();
 	}
 }
