 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.core.security;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 import com.kk_electronic.kkportal.core.persistence.Storage;
 import com.kk_electronic.kkportal.core.rpc.FrameEncoder;
 import com.kk_electronic.kkportal.core.rpc.RpcRequest;
 import com.kk_electronic.kkportal.core.security.NewPrimaryIdentityEvent.Handler;
 import com.kk_electronic.kkportal.core.ui.InputDialog;
 import com.kk_electronic.kkportal.core.util.Pair;
 
 /**
  * No UI until we have both call and identity
  * @author Jes Andersen
  */
 
 public class Digest implements SecurityMethod, Handler,
 		com.kk_electronic.kkportal.core.ui.InputDialog.Handler {
 
 	private final Hasher hasher;
 	private Identity identity;
 	private String password;
 	private final FrameEncoder<JSONValue> encoder;
 	private final InputDialog dialog;
 	private final IdentityProvider identityProvider;
 	private final SecurityContext context;
 	private final SRP srp;
 	private final Storage storage;
 	private String sessionId;
 	private final EventBus eventBus;
 
 	@Inject
 	public Digest(Hasher hasher, FrameEncoder<JSONValue> encoder,
 			InputDialog dialog, IdentityProvider identityProvider,
 			SecurityContext context, SRP srp, Storage storage,EventBus eventBus) {
 		this.hasher = hasher;
 		this.encoder = encoder;
 		this.dialog = dialog;
 		this.identityProvider = identityProvider;
 		this.context = context;
 		this.srp = srp;
 		this.storage = storage;
 		this.eventBus = eventBus;
 		setIdentity(identityProvider.getPrimaryIdentity());
 		identityProvider.addNewPrimaryIdentityEventHandler(this);
 		dialog.setHandler(this);
 	}
 
 	List<Pair<RpcRequest, AsyncCallback<RpcRequest>>> queue;
 	private List<String> methods = Arrays.asList(new String[] { "password" });
 	private String secret;
 	private Challange challange;
 
 	@Override
 	public void sign(RpcRequest request, AsyncCallback<RpcRequest> asyncCallback) {
 		if(secret != null){
 			signInternal(request, asyncCallback);
 		} else {
 			queue(request,asyncCallback);
 			makeSecret();
 		}
 	}
 
 	private void queue(RpcRequest request,
 			AsyncCallback<RpcRequest> asyncCallback) {
 		if (queue == null)
 			queue = new ArrayList<Pair<RpcRequest, AsyncCallback<RpcRequest>>>();
 		queue.add(new Pair<RpcRequest, AsyncCallback<RpcRequest>>(request,
 				asyncCallback));
 		GWT.log("DIGEST-Queued call for signing");
 	}
 
 	private void signInternal(RpcRequest request,
 			AsyncCallback<RpcRequest> asyncCallback) {
 		String requestSignature = request.getSignature(encoder);
 		GWT.log("DIGEST-Signing: " + requestSignature);
 		String digestinput = secret + ':' + requestSignature;
 		request.setParams(new Object[] { hasher.hash(digestinput),
 				request.getParams() });
 		asyncCallback.onSuccess(request);
 	}
 
 	public void setIdentity(Identity newidentity) {
 		if (newidentity == null) {
 			this.identity = null;
 			setPassword(null);
 			return;
 		} else {
 			GWT.log("DIGEST-New identity " + newidentity);
 			this.identity = newidentity;
 			context.setIdentity(identity.toString());
 			setPassword(null);
 			makeSecret();
 		}
 	}
 
 	private void show() {
 		if (!dialog.isShowing()) {
 			dialog.setText("Need password for " + identity.toString());
 			dialog.setValue("");
 			dialog.show();
 			setPassword(null);
 			context.setIdentity(identity.toString());
 		}
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		context.setPassword(password);
 	}
 
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	public void setSessionId(String sessionId) {
 		this.sessionId = sessionId;
 		storage.put("sessionid", sessionId);
 	}
 
 	public String getSecret() {
 		return secret;
 	}
 
 	public void setSecret(String secret) {
 		this.secret = secret;
 		setPassword(null);
 		storage.put("secret", secret);
 		sendQueue();
 		eventBus.fireEventFromSource(new LoginEvent(identity), this);
 	}
 
 	public Identity getIdentity() {
 		return identity;
 	}
 
 	@Override
 	public void onNewPrimaryIdentity(NewPrimaryIdentityEvent event) {
 		setIdentity(event.getIdentity());
 	}
 
 	@Override
 	public void onInput(final String input) {
 		GWT.log("DIGEST-Got password");
 		eventBus.fireEventFromSource(new LoginEvent(null), this);
 		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 			
 			@Override
 			public void execute() {
 				setPassword(input);
 				sendAnswer(context.calc_answer(challange));
 			}
 		});
 	}
 
 	boolean connecting = false;
 	private void makeSecret() {
 		if(connecting || identity == null || queue == null || queue.isEmpty()) return;
 		connecting = true;
 		final String storedSecret = storage.get("secret");
 		final String storedSessionId = storage.get("sessionid");
 		srp.requestChallange(null, context.getIdentity(), methods, ((storedSecret != null)?storedSessionId:null),
 				new AsyncCallback<Challange>() {
 
 					@Override
 					public void onSuccess(Challange result) {
 						if(result.sessionId.equals(storedSessionId)){
 							setSessionId(result.sessionId);
 							setSecret(storedSecret);
 						} else {
 							setSessionId(result.sessionId);
 							askPassword(result);
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						GWT.log("Login failed", caught);
 						connecting = false;
 					}
 				});
 	}
 
 	protected void sendAnswer(Answer answer) {
 		srp.answerChallange(answer.a.toString(16), answer.m1,
 				new AsyncCallback<String>() {
 
 					@Override
 					public void onSuccess(String result) {
 						GWT.log("DIGEST-Has shared secret with server");
 						setSecret(hasher.hash(context.getSecret()));
 						connecting = false;
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						GWT.log("Login failed", caught);
 						connecting = false;
						identityProvider.invalidate(identity, "Wrong password");
 					}
 				});
 	}
 
 	private void sendQueue() {
 		if (queue != null && !queue.isEmpty()) {
 			for (Pair<RpcRequest, AsyncCallback<RpcRequest>> pair : queue) {
 				signInternal(pair.getA(), pair.getB());
 			}
 		}
 		queue = null;
 	}
 
 	@Override
 	public void invalid() {
 		setPassword(null);
 		setIdentity(null);
 		setSecret(null);
 		setSessionId(null);
 		connecting = false;
 		identityProvider.invalidate(identity);
 	}
 
 	private void askPassword(Challange challange) {
 		this.challange = challange;
 		if(getPassword() == null){
 			show();
 		} else {
 			sendAnswer(context.calc_answer(challange));
 		}
 	}
 	
 	public HandlerRegistration addLoginEventHandler(LoginEvent.Handler handler){
 		return eventBus.addHandler(LoginEvent.TYPE, handler);
 	}
 
 	public boolean hasSecret() {
 		return secret != null;
 	}
 }
