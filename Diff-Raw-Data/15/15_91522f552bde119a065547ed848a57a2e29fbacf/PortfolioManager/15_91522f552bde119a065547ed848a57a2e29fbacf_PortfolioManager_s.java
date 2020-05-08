 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.portfolio.manager;
 
 import it.sayservice.platform.client.DomainEngineClient;
 import it.sayservice.platform.client.DomainObject;
 import it.sayservice.platform.client.InvocationException;
 import it.sayservice.platform.core.common.util.ServiceUtil;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import eu.trentorise.smartcampus.portfolio.models.Portfolio;
 import eu.trentorise.smartcampus.portfolio.models.UserProducedData;
 
 @Component
 public class PortfolioManager {
 
 	public static String STUDENT_EDU_TITLE = "University�of Trento";
 	public static String STUDENT_EDU_TYPE = "sys_simple";
 	public static String STUDENT_EDU_CATEGORY = "education";
 
 	private static final Logger log = Logger.getLogger(PortfolioManager.class);
 
 	@Autowired
 	private DomainEngineClient domainClient;
 
 	public boolean createStudent(String userId, String unitnId) throws InvocationException, IOException,
 			ClassNotFoundException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("newUserId", userId);
 		pars.put("newIdAda", unitnId);
 		byte[] b = (byte[]) domainClient.invokeDomainOperationSync("createStudent",
 				"smartcampus.services.esse3.StudentFactory", "smartcampus.services.esse3.StudentFactory.0", pars,
 				"vas_portfolio_subscriber");
 		String result = (String) ServiceUtil.deserializeObject(b);
 		if (Boolean.parseBoolean(result)) {
 			pars.clear();
 			pars.put("newUserId", userId);
 			pars.put("newCategory", STUDENT_EDU_CATEGORY);
 			pars.put("newType", STUDENT_EDU_TYPE);
 			pars.put("newTitle", STUDENT_EDU_TITLE);
 			pars.put("newSubtitle", "");
 			pars.put("newContent", "");
 			domainClient.invokeDomainOperation("createUserProducedData",
 					"smartcampus.services.esse3.UserProducedDataFactory",
 					"smartcampus.services.esse3.UserProducedDataFactory.0", pars, userId, "vas_portfolio_subscriber");
 			return true;
 		}
 		return false;
 	}
 
 	public String getStudentInfo(String userId) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		List<String> result = domainClient.searchDomainObjects("smartcampus.services.esse3.StudentInfo", pars,
 				"vas_portfolio_subscriber");
 		if (result != null && result.size() > 0) {
 			return result.get(0);
 		} else {
 			return "";
 		}
 	}
 
 	public String getStudentExams(String userId) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		List<String> result = domainClient.searchDomainObjects("smartcampus.services.esse3.StudentExams", pars,
 				"vas_portfolio_subscriber");
 		if (result != null && result.size() > 0) {
 			return result.get(0);
 		} else {
 			return "";
 		}
 	}
 
 	public List<String> getStudentPortfolios(String userId) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		pars.put("deleted", false);
 		List<String> res = domainClient.searchDomainObjects("smartcampus.services.esse3.Portfolio", pars,
 				"vas_portfolio_subscriber");
 
 		return res;
 	}
 
 	public List<String> getStudentPortfolio(String userId, String name) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		pars.put("name", name);
 		List<String> res = domainClient.searchDomainObjects("smartcampus.services.esse3.Portfolio", pars,
 				"vas_portfolio_subscriber");
 
 		return res;
 	}
 
 	public String getStudentPortfolio(String portfolioId) throws InvocationException {
 		return domainClient.searchDomainObject("smartcampus.services.esse3.Portfolio", portfolioId,
 				"vas_portfolio_subscriber");
 	}
 
 	public String getPortfolioByEntityId(Long id) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("entityId", id);
 		List<String> res = domainClient.searchDomainObjects("smartcampus.services.esse3.Portfolio", pars,
 				"vas_portfolio_subscriber");
 		if (res != null && !res.isEmpty())
 			return res.get(0);
 		return null;
 	}
 
 	// public String createPortfolio(Map<String, Object> json, String userId,
 	// long socialUserId)
 	// throws InvocationException {
 	// if (json != null) {
 	// Map<String, Object> pars = new TreeMap<String, Object>();
 	// pars.put("newUserId", userId);
 	// pars.put("newName", json.get("name"));
 	// pars.put("newUserSocialId", socialUserId);
 	//
 	// if (!pars.containsValue(null)) {
 	// domainClient.invokeDomainOperation("createPortfolio",
 	// "smartcampus.services.esse3.PortfolioFactory",
 	// "smartcampus.services.esse3.PortfolioFactory.0", pars, userId,
 	// "vas_portfolio_subscriber");
 	// }
 	// pars.clear();
 	// pars.put("userId", userId);
 	// pars.put("name", json.get("name"));
 	// List<String> res =
 	// domainClient.searchDomainObjects("smartcampus.services.esse3.Portfolio",
 	// pars,
 	// "vas_portfolio_subscriber");
 	// if (res != null && !res.isEmpty()) {
 	// return res.get(0);
 	// }
 	// }
 	// return null;
 	// }
 
 	public String createPortfolio(Portfolio portfolio, String userId, long socialUserId) throws InvocationException {
 		if (portfolio != null && !portfolio.getName().isEmpty()) {
 			Map<String, Object> pars = new TreeMap<String, Object>();
 			pars.put("newUserId", userId);
 			pars.put("newName", portfolio.getName());
 			pars.put("newUserSocialId", socialUserId);
 
 			boolean nameAlreadyExists = getStudentPortfolio(userId, portfolio.getName()).size() > 0 ? true : false;
 
 			if (!nameAlreadyExists && !pars.containsValue(null)) {
 				domainClient.invokeDomainOperation("createPortfolio", "smartcampus.services.esse3.PortfolioFactory",
 						"smartcampus.services.esse3.PortfolioFactory.0", pars, userId, "vas_portfolio_subscriber");
 
 				List<String> res = getStudentPortfolio(userId, portfolio.getName());
 				if (res != null && !res.isEmpty()) {
 					return res.get(0);
 				}
 			}
 		}
 		return null;
 	}
 
 	// public void updatePortfolio(Map<String, Object> json, String portfolioId,
 	// String userId) throws Exception {
 	// if (json != null) {
 	// String s =
 	// domainClient.searchDomainObject("smartcampus.services.esse3.Portfolio",
 	// portfolioId,
 	// "vas_portfolio_subscriber");
 	// if (s != null) {
 	// DomainObject o = new DomainObject(s);
 	// if (!userId.equals(o.getContent().get("userId"))) {
 	// throw new SecurityException("Incorrect user");
 	// }
 	// }
 	//
 	// Map<String, Object> pars = new TreeMap<String, Object>();
 	// pars.put("newName", json.get("name"));
 	//
 	// // IMPORTANT: these arrays CANNOT BE NULL
 	// ArrayList<String> showPart = (ArrayList<String>)
 	// json.get("showUserGeneratedData");
 	//
 	// Object[] showPartArray = showPart.toArray();
 	// pars.put("newShowUserGeneratedData", showPartArray);
 	//
 	// showPart = (ArrayList<String>) json.get("highlightUserGeneratedData");
 	// showPartArray = showPart.toArray();
 	// pars.put("newHighlightUserGeneratedData", showPartArray);
 	//
 	// showPart = (ArrayList<String>) json.get("showStudentInfo");
 	// showPartArray = showPart.toArray();
 	// pars.put("newShowStudentInfo", showPartArray);
 	//
 	// if (json.get("tags") != null) {
 	// pars.put("newTags", json.get("tags"));
 	// }
 	//
 	// if (!pars.containsValue(null)) {
 	// domainClient.invokeDomainOperation("updatePortfolio",
 	// "smartcampus.services.esse3.Portfolio",
 	// portfolioId, pars, portfolioId, "vas_portfolio_subscriber");
 	// }
 	// }
 	// }
 
 	public void updatePortfolio(Portfolio portfolio, String portfolioId, String userId) throws Exception {
 		if (portfolio != null) {
 			String s = domainClient.searchDomainObject("smartcampus.services.esse3.Portfolio", portfolioId,
 					"vas_portfolio_subscriber");
 			if (s != null) {
 				DomainObject o = new DomainObject(s);
 				if (!userId.equals(o.getContent().get("userId"))) {
 					throw new SecurityException("Incorrect user");
 				}
 			}
 
 			Map<String, Object> pars = new TreeMap<String, Object>();
 			pars.put("newName", portfolio.getName());
 
 			// IMPORTANT: these arrays CANNOT BE NULL
 			ArrayList<String> showPart = (ArrayList<String>) portfolio.getShowUserGeneratedData();
 
 			Object[] showPartArray = showPart.toArray();
 			pars.put("newShowUserGeneratedData", showPartArray);
 
 			showPart = (ArrayList<String>) portfolio.getHighlightUserGeneratedData();
 			showPartArray = showPart.toArray();
 			pars.put("newHighlightUserGeneratedData", showPartArray);
 
 			showPart = (ArrayList<String>) portfolio.getShowStudentInfo();
 			showPartArray = showPart.toArray();
 			pars.put("newShowStudentInfo", showPartArray);
 
 			if (portfolio.getTags() != null) {
				pars.put("newTags", portfolio.getTags());
 			}
 
 			if (!pars.containsValue(null)) {
 				domainClient.invokeDomainOperation("updatePortfolio", "smartcampus.services.esse3.Portfolio",
 						portfolioId, pars, portfolioId, "vas_portfolio_subscriber");
 			}
 		}
 	}
 
 	public void deletePortfolio(String portfolioId, String userId) throws Exception {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		// domainClient.invokeDomainOperation("deletePortfolio",
 		// "smartcampus.services.esse3.Portfolio", id, pars, id,
 		// "vas_portfolio_subscriber");
 
 		String s = domainClient.searchDomainObject("smartcampus.services.esse3.Portfolio", portfolioId,
 				"vas_portfolio_subscriber");
 		if (s != null) {
 			DomainObject o = new DomainObject(s);
 			if (!userId.equals(o.getContent().get("userId"))) {
 				throw new SecurityException("Incorrect user");
 			}
 			domainClient.invokeDomainOperation("deletePortfolio", "smartcampus.services.esse3.Portfolio", portfolioId,
 					pars, portfolioId, "vas_portfolio_subscriber");
 		}
 
 	}
 
 	public List<String> getUserProducedData(String userId, String category) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		if (category != null) {
 			pars.put("category", category);
 		}
 		List<String> res = domainClient.searchDomainObjects("smartcampus.services.esse3.UserProducedData", pars,
 				"vas_portfolio_subscriber");
 
 		return res;
 	}
 
 	public String getUserUNITNData(String userId) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		pars.put("category", STUDENT_EDU_CATEGORY);
 		pars.put("type", STUDENT_EDU_TYPE);
 
 		List<String> res = domainClient.searchDomainObjects("smartcampus.services.esse3.UserProducedData", pars,
 				"vas_portfolio_subscriber");
 		if (res != null && !res.isEmpty())
 			return res.get(0);
 		return null;
 	}
 
 	// public void createUserProducedData(Map<String, Object> json, String
 	// userId) throws InvocationException {
 	// if (json != null) {
 	// Map<String, Object> pars = new TreeMap<String, Object>();
 	// pars.put("newUserId", userId);
 	// pars.put("newCategory", json.get("category"));
 	// pars.put("newType", json.get("type"));
 	// pars.put("newTitle", json.get("title"));
 	// pars.put("newSubtitle", json.get("subtitle"));
 	// pars.put("newContent", json.get("content"));
 	// if (!pars.containsValue(null)) {
 	// domainClient.invokeDomainOperation("createUserProducedData",
 	// "smartcampus.services.esse3.UserProducedDataFactory",
 	// "smartcampus.services.esse3.UserProducedDataFactory.0", pars, userId,
 	// "vas_portfolio_subscriber");
 	// }
 	// }
 	// }
 
 	public void createUserProducedData(UserProducedData upd, String userId) throws InvocationException {
 		if (upd != null) {
 			Map<String, Object> pars = new TreeMap<String, Object>();
 			pars.put("newUserId", userId);
 			pars.put("newCategory", upd.getCategory());
 			pars.put("newType", upd.getType());
 			pars.put("newTitle", upd.getTitle());
 			pars.put("newSubtitle", upd.getSubtitle());
 			pars.put("newContent", upd.getContent());
 			if (!pars.containsValue(null)
 					&& (!upd.getTitle().isEmpty() || !upd.getSubtitle().isEmpty() || !upd.getContent().isEmpty())) {
 				domainClient.invokeDomainOperation("createUserProducedData",
 						"smartcampus.services.esse3.UserProducedDataFactory",
 						"smartcampus.services.esse3.UserProducedDataFactory.0", pars, userId,
 						"vas_portfolio_subscriber");
 			}
 		}
 	}
 
 	public void updateUserProducedData(UserProducedData upd, String userDataId, String userId) throws Exception {
 		if (upd != null) {
 			String s = domainClient.searchDomainObject("smartcampus.services.esse3.UserProducedData", userDataId,
 					"vas_portfolio_subscriber");
 			if (s != null) {
 				DomainObject o = new DomainObject(s);
 				if (!userId.equals(o.getContent().get("userId"))) {
 					throw new SecurityException("Incorrect user");
 				}
 			}
 
 			Map<String, Object> pars = new TreeMap<String, Object>();
 			pars.put("newType", upd.getType());
 			pars.put("newTitle", upd.getTitle());
 			pars.put("newSubtitle", upd.getSubtitle());
 			pars.put("newContent", upd.getContent());
 			if (!pars.containsValue(null)
 					&& (!upd.getTitle().isEmpty() || !upd.getSubtitle().isEmpty() || !upd.getContent().isEmpty())) {
 				domainClient.invokeDomainOperation("updateUserProducedData",
 						"smartcampus.services.esse3.UserProducedData", userDataId, pars, userDataId,
 						"vas_portfolio_subscriber");
 			}
 		}
 	}
 
 	public void deleteUserProducedData(String userDataId, String userId) throws Exception {
 		String s = domainClient.searchDomainObject("smartcampus.services.esse3.UserProducedData", userDataId,
 				"vas_portfolio_subscriber");
 		if (s != null) {
 			DomainObject o = new DomainObject(s);
 			if (!userId.equals(o.getContent().get("userId"))) {
 				throw new SecurityException("Incorrect user");
 			}
 		}
 
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		domainClient.invokeDomainOperation("deleteUserProducedData", "smartcampus.services.esse3.UserProducedData",
 				userDataId, pars, userDataId, "vas_portfolio_subscriber");
 	}
 
 	public String getDomainObject(String type, String id) throws InvocationException {
 		return domainClient.searchDomainObject(type, id, "vas_portfolio_subscriber");
 	}
 
 	public String getUserProducedData(String id) throws InvocationException {
 		return domainClient.searchDomainObject("smartcampus.services.esse3.UserProducedData", id,
 				"vas_portfolio_subscriber");
 	}
 
 	public List<String> getAllUserProducedData(String userId) throws InvocationException {
 		Map<String, Object> pars = new TreeMap<String, Object>();
 		pars.put("userId", userId);
 		return domainClient.searchDomainObjects("smartcampus.services.esse3.UserProducedData", pars,
 				"vas_portfolio_subscriber");
 	}
 }
