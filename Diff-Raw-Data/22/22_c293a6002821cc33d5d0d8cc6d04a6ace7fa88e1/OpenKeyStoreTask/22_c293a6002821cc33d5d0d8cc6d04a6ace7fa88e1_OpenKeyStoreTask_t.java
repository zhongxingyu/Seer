 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.admin.vbs.cube.core.impl.scauthmodule;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.security.KeyStore;
 import java.security.KeyStore.Builder;
 import java.security.Security;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.security.pkcs11.SunPKCS11;
 import ch.admin.vbs.cube.common.Chronos;
 import ch.admin.vbs.cube.core.AuthModuleEvent;
 import ch.admin.vbs.cube.core.AuthModuleEvent.AuthEventType;
 import ch.admin.vbs.cube.core.impl.CaValidation;
 import ch.admin.vbs.cube.core.impl.scauthmodule.AbstractState.ScAuthStateTransition;
 
 class OpenKeyStoreTask implements Runnable, CallbackHandler {
 	private static final Logger LOG = LoggerFactory.getLogger(OpenKeyStoreTask.class);
 	private final ScAuthModule scAuthModule;
 	private SunPKCS11 provider;
 	private Builder builder;
 	private KeyStore keystore;
 	private boolean keyStoreOpeningLock;
 	private boolean killed;
 	private char[] password;
 	private CaValidation caValid = new CaValidation();
 
 	/**
 	 * @param scAuthModule
 	 */
 	public OpenKeyStoreTask(ScAuthModule scAuthModule) {
 		this.scAuthModule = scAuthModule;
 	}
 
 	public void finalizeKeyStoreOpening() {
 		keyStoreOpeningLock = false;
 	}
 
 	@Override
 	public void run() {
 		Chronos cronos = new Chronos();
 		cronos.zap("Run OpenKeyStoreTask...");
 		scAuthModule.setAbortReason(null);
 		try {
 			/*
 			 * cleanup already register provider seems needed in order to reset
 			 * smart-card driver.
 			 */
 			if (provider != null) {
 				Security.removeProvider(provider.getName());
 				provider = null;
 			}
 			cronos.zap("Old provider removed");
 			// initialize provider
 			StringBuilder buf = new StringBuilder();
 			buf.append("library = ").append(this.scAuthModule.pkcs11LibraryPath).append("\nname = Cube\n");
			LOG.debug("## PKCS11 Config ##########\n"+buf.toString()+"\n############");
 			provider = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(buf.toString().getBytes()));
 			Security.addProvider(provider);
 			cronos.zap("New provider added");
 			// create builder
 			builder = KeyStore.Builder.newInstance("PKCS11", provider, new KeyStore.CallbackHandlerProtection(this));
 			cronos.zap("Builder created");
 			// request keystore
 			// getKeyStore will block until user gave its password via
 			// method "handle(Callback[] callbacks)" and
 			// "handle(Callback[] callbacks)" will block until OpenKeyStoreState
 			// will call 'finalizeKeyStoreOpening'
 			keystore = builder.getKeyStore();
 			cronos.zap("Got keystore"); // may take long since it will wait user input
 			// check certificates chain
 			caValid.validate(keystore);
 			cronos.zap("Certificate chain validate");
 			// next transition / state
 			this.scAuthModule.enqueue(ScAuthStateTransition.KEYSTORE_READY);
 		} catch (Exception e) {
 			/* try to guess error cause in order to give the user a better feedback. */
 			if (this.scAuthModule.handlePinIncorrect(e)) {
 				LOG.debug("OpenKeyStoreTask failed: Incorrect PIN (CKR_PIN_INCORRECT)");
 				scAuthModule.setAbortReason(new AuthModuleEvent(AuthEventType.FAILED_WRONGPIN, null, null, null));
 			} else if (this.scAuthModule.handleCanceled(e)) {
 				LOG.debug("OpenKeyStoreTask failed: PKCS11 login canceled");
 				scAuthModule.setAbortReason(new AuthModuleEvent(AuthEventType.FAILED, null, null, null));
 			} else if (this.scAuthModule.handleUserNotLoggedIn(e)) {
 				LOG.debug("OpenKeyStoreTask failed: User did not enter its password (CKR_USER_NOT_LOGGED_IN)");
 				scAuthModule.setAbortReason(new AuthModuleEvent(AuthEventType.FAILED_USERTIMEOUT, null, null, null));
 			} else if (this.scAuthModule.handleNoSuchAlgo(e)) {
 				LOG.debug("OpenKeyStoreTask failed: Unable to read smart-Card (NoSuchAlgorithmException)");
 				scAuthModule.setAbortReason(new AuthModuleEvent(AuthEventType.FAILED, null, null, null));
 			} else if (this.scAuthModule.handleFunctionFailed(e)) {
 				LOG.debug("OpenKeyStoreTask failed: Unable to use smart-Card (CKR_FUNCTION_FAILED)");
 				scAuthModule.setAbortReason(new AuthModuleEvent(AuthEventType.FAILED, null, null, null));
 			} else {
 				LOG.debug("OpenKeyStoreTask failed: general failure", e);
 				scAuthModule.setAbortReason(new AuthModuleEvent(AuthEventType.FAILED, null, null, null));
 			}
 			this.scAuthModule.enqueue(ScAuthStateTransition.ABORT_AUTH);
 		}
 	}
 
 	@Override
 	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
 		keyStoreOpeningLock = true;
 		this.scAuthModule.enqueue(ScAuthStateTransition.PASSWORD_REQUEST);
 		/* wait until user entered its password */
 		while (!killed && keyStoreOpeningLock) {
 			try {
 				Thread.sleep(400);
 			} catch (InterruptedException e) {
 				LOG.error("", e);
 			}
 		}
 		((PasswordCallback) callbacks[0]).setPassword(password);
 	}
 
 	public KeyStore getKeyStore() {
 		return keystore;
 	}
 
 	public Builder getBuilder() {
 		return builder;
 	}
 
 	public char[] getPassword() {
 		return password;
 	}
 
 	public void setPassword(char[] password) {
 		this.password = password;
 	}
 }
