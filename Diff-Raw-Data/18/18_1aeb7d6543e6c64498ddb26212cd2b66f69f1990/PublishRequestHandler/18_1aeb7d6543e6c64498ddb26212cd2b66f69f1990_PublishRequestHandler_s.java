 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.requesthandler.matchmaking;
 
 import static com.mymed.utils.GsonUtils.gson;
 import static com.mymed.utils.MiscUtils.makePrefix;
 
 import java.lang.reflect.Type;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.pubsub.PubSubManager;
 import com.mymed.controller.core.manager.statistics.StatisticsManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessageOut;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Servlet implementation class PubSubRequestHandler
  */
 @MultipartConfig
 @WebServlet("/PublishRequestHandler")
 public class PublishRequestHandler extends AbstractMatchMaking {
 
 	/**
 	 * Generated serial ID.
 	 */
 	private static final long serialVersionUID = 7612306539244045439L;
 
 	/**
 	 * JSON 'predicate' attribute.
 	 */
 	private static final String JSON_PREDICATE = JSON.get("json.predicate");
 
 	protected PubSubManager pubsubManager;
 	protected ProfileManager profileManager;
 	protected StatisticsManager statisticsManager;
 	protected static final String PUBLISH_ARG = "pub";
 
 	public PublishRequestHandler() throws InternalBackEndException {
 		super();
 		profileManager = new ProfileManager();
 		pubsubManager = new PubSubManager();
 		statisticsManager = new StatisticsManager();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mymed.controller.core.requesthandler.AbstractRequestHandler#getParameters
 	 * (javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected Map<String, String> getParameters(final HttpServletRequest request) throws AbstractMymedException {
 
 		if ((request.getContentType() != null) && !request.getContentType().matches("multipart/form-data.*")) {
 			return super.getParameters(request);
 		}
 
 		final Map<String, String> parameters = new HashMap<String, String>();
 
 		try {
 			for (final Part part : request.getParts()) {
 				final String key = part.getName();
 				final Scanner s = new Scanner(part.getInputStream());
 				final String value = URLDecoder.decode(s.nextLine(), ENCODING);
 				parameters.put(key, value);
 			}
 		} catch (final Exception e) {
 			LOGGER.debug("Error retrieving arguments", e);
 			throw new InternalBackEndException("Error in getting arguments");
 		}
 
 		if (!parameters.containsKey(JSON_CODE)) {
 			throw new InternalBackEndException("code argument is missing!");
 		}
 
 		return parameters;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mymed.controller.core.requesthandler.AbstractRequestHandler#doGet
 	 * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
 		final JsonMessageOut<Object> message = new JsonMessageOut<Object>(200, this.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			// Check the access token
 			checkToken(parameters);
 
 			final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
 			final String application, predicateListJson, user, namespace = parameters.get(JSON_NAMESPACE);
 			user = parameters.get(JSON_USERID) != null ? parameters.get(JSON_USERID) : parameters.get(JSON_USER);
 			String userID;
 
 			switch (code) {
 			case READ :
 				break;
 			case DELETE :
 				message.setMethod(JSON_CODE_DELETE);
 				if ((application = parameters.get(JSON_APPLICATION)) == null) {
 					throw new InternalBackEndException("missing application argument!");
 				} else if ((predicateListJson = parameters.get(JSON_PREDICATE)) == null) {
 					throw new InternalBackEndException("missing predicate argument!");
 				} else if (user == null) {
 					throw new InternalBackEndException("missing user argument!");
 				}
 
 				try {
 					final MUserBean userBean = gson.fromJson(user, MUserBean.class);
 					userID = userBean.getId();
 				} catch (final JsonSyntaxException e) {
 					userID = user;
 				}

 				try {
 					final Type dataType = new TypeToken<List<MDataBean>>() {
 					}.getType();
 					final List<MDataBean> predicateListObject = gson.fromJson(predicateListJson, dataType);
 
 					// construct the subPredicate
 					final StringBuffer bufferSubPredicate = new StringBuffer(150);
 					for (final MDataBean element : predicateListObject) {
 						bufferSubPredicate.append(element.getKey());
 						bufferSubPredicate.append(element.getValue());
 					}
 					bufferSubPredicate.trimToSize();
 
 					int level = predicateListObject.size();
 					if (parameters.get("level") != null){
 						level = Integer.parseInt(parameters.get("level"));
 					}
 
 					// Generate list of predicates
 					List<String> predicates = getPredicate(
 							predicateListObject, 
 							1, level);
 
 					LOGGER.info("deleting "+bufferSubPredicate.toString()+" with level: "+level);
 					for(String predicate : predicates) {
 						pubsubManager.delete(makePrefix(application, namespace), predicate, bufferSubPredicate.toString(), userID);
 					}
 
 				} catch (final JsonSyntaxException e) {
 					throw new InternalBackEndException("jSon format is not valid");
 				} catch (final JsonParseException e) {
 					throw new InternalBackEndException(e.getMessage());
 				}
 				break;
 			default :
 				throw new InternalBackEndException("PublishRequestHandler(" + code + ") not exist!");
 			}
 
 		} catch (final AbstractMymedException e) {
 			LOGGER.debug("Error in doGet operation", e);
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		}
 
 		printJSonResponse(message, response);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mymed.controller.core.requesthandler.AbstractRequestHandler#doPost
 	 * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
 		final JsonMessageOut<Object> message = new JsonMessageOut<Object>(200, this.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			// Check the access token
 			checkToken(parameters);
 
 			final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
 			final String application, predicateListJson, user, data, namespace = parameters.get(JSON_NAMESPACE);
 
 			// get userID
 			user = parameters.get(JSON_USERID) != null ? parameters.get(JSON_USERID) : parameters.get(JSON_USER);
 
 			if (code.equals(RequestCode.CREATE)) {
 
 				message.setMethod(JSON_CODE_CREATE);
 
 				if ((application = parameters.get(JSON_APPLICATION)) == null) {
 					throw new InternalBackEndException("missing application argument!");
 				} else if ((predicateListJson = parameters.get(JSON_PREDICATE)) == null) {
 					throw new InternalBackEndException("missing predicate argument!");
 				} else if (user == null) {
 					throw new InternalBackEndException("missing userID argument!");
 				} else if ((data = parameters.get(JSON_DATA)) == null) {
 					throw new InternalBackEndException("missing data argument!");
 				} 
 
 				MUserBean userBeanTmp;
 				try {
 					userBeanTmp = gson.fromJson(user, MUserBean.class);
 				} catch (final JsonSyntaxException e) {
 					userBeanTmp = profileManager.read(user);
 				}
 				final MUserBean userBean = userBeanTmp;
 
 				try {
 
 					// Parse Data & Predicate lists JSON 
 					final Type dataType = new TypeToken<List<MDataBean>>() {}.getType();
 					final List<MDataBean> dataList = gson.fromJson(data, dataType);
 					final List<MDataBean> predicateList = gson.fromJson(predicateListJson, dataType);
 
 					// Construct the subPredicate
 					final StringBuffer bufferSubPredicate = new StringBuffer(150);
 					for (final MDataBean element : predicateList) {
 						bufferSubPredicate.append(element.getKey());
 						bufferSubPredicate.append(element.getValue());
 					}
 					bufferSubPredicate.trimToSize();
 
 					// Construct indexes 
 					int level = predicateList.size();
 					if (parameters.get("level") != null){
 						level = Integer.parseInt(parameters.get("level"));
 					}
 					final List<String> predicates = getPredicate(
 							predicateList, 
 							1, 
 							level);
 
 					// Store indexes for this data 
 					LOGGER.info("indexing "+bufferSubPredicate.toString()+" with level: "+level+", nb of rows:"+predicates.size());
 
 					//  Broadcast
 					for (final String predicate : predicates) {
 						// ASCYN EXEC
 						if (parameters.get(JSON_ASYNCEXEC) != null) {
 							new Thread(new Runnable() {
 								@Override
 								public void run() {
 										pubsubManager.create(
 												makePrefix(application, namespace),
 												predicate, 
 												bufferSubPredicate.toString(),
 												userBean,
 												dataList,
 												predicateList);
 									}
 							}).start();
 						} else {
 							pubsubManager.create(
 									makePrefix(application, namespace),
 									predicate, 
 									bufferSubPredicate.toString(),
 									userBean,
 									dataList,
 									predicateList);
 						}
 					}
 
 					// ASCYN EXEC - update statistics
 					new Thread(new Runnable() {
 						@Override
 						public void run() {
 							statisticsManager.update(application, PUBLISH_ARG);
 						}}).start();
 
 
 				} catch (final JsonSyntaxException e) {
 					throw new InternalBackEndException(e, "Error in Json format");
 				} catch (final JsonParseException e) {
 					throw new InternalBackEndException(e, "Error in Json format");
 				}
 			} else {
 				throw new InternalBackEndException("PublishRequestHandler(" + code + ") not exist!");
 			}
 		} catch (final AbstractMymedException e) {
 			LOGGER.debug("Error in doPost operation", e);
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		}
 
 		printJSonResponse(message, response);
 	}
 }
