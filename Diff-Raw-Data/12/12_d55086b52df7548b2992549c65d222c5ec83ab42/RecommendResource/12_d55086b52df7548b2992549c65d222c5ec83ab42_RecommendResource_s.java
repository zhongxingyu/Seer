 package com.successfactors.library.rest.resource;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import org.json.JSONException;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.stringtree.json.JSONReader;
 import org.stringtree.json.JSONValidatingReader;
 
 import com.successfactors.library.rest.dao.SLBookDao;
 import com.successfactors.library.rest.dao.SLRecommendHistoryDao;
 import com.successfactors.library.rest.dao.SLRecommendedBookDao;
 import com.successfactors.library.rest.model.RecommendedBookPage;
 import com.successfactors.library.rest.model.SLRecommendHistory;
 import com.successfactors.library.rest.model.SLRecommendedBook;
 import com.successfactors.library.rest.model.SLUser;
 import com.successfactors.library.rest.utils.RestCallInfo;
 import com.successfactors.library.rest.utils.SLEmailUtil;
 import com.successfactors.library.rest.utils.SLSessionManager;
 import com.successfactors.library.rest.utils.RestCallInfo.RestCallErrorCode;
 import com.successfactors.library.rest.utils.RestCallInfo.RestCallStatus;
 
 @SuppressWarnings({"rawtypes", "unchecked"})
@Path("order")
 public class RecommendResource {
 
 	private SLBookDao bookDao = SLBookDao.getDao();
 	private SLRecommendedBookDao dao = SLRecommendedBookDao.getDao();
 	private SLRecommendHistoryDao recommendHistoryDao = SLRecommendHistoryDao.getDao();
 
 
 
 	/**
 	 * 获取推荐图书信息
 	 * */
 	@GET
 	@Path("getrecommendedbook/{bookISBN}")
 	@Produces("application/json")
	public Representation getRecommendedBook(String bookISBN) {
 		SLRecommendedBook book = dao.queryByISBN(bookISBN);
 		if (book == null) {
 			HashMap returnInfo = new HashMap();
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.no_such_recommended_book);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		JsonRepresentation ret = new JsonRepresentation(book);
 		try {
 			ret.getJsonObject().put(RestCallInfo.REST_STATUS, RestCallStatus.success);
 			ret.getJsonObject().put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.no_error);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	/**
 	 * 获取所有推荐图书列表
 	 * */
 	@GET
 	@Path("getallrecbooklistpage/{itemsPerPage}/{pageNum}")
 	@Produces("application/json")
 	public Representation getAllRecBookList(
 			@PathParam("itemsPerPage") int itemsPerPage,
 			@PathParam("pageNum") int pageNum) {
 		
 		ArrayList<SLRecommendedBook> listBooks = (ArrayList<SLRecommendedBook>) dao.queryAll(
 				itemsPerPage, pageNum);
 		RecommendedBookPage bookPage = new RecommendedBookPage(itemsPerPage, pageNum);
 		bookPage.setTheBooks(listBooks);
 		long totalNum = dao.getCountAll();
 		if (totalNum % itemsPerPage == 0) {
 			bookPage.setTotalPageNum((int) totalNum / itemsPerPage);
 		} else {
 			bookPage.setTotalPageNum((int) totalNum / itemsPerPage + 1);
 		}
 		
 		JsonRepresentation ret = new JsonRepresentation(bookPage);
 		try {
 			ret.getJsonObject().put(RestCallInfo.REST_STATUS, RestCallStatus.success);
 			ret.getJsonObject().put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.no_error);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return ret;	
 	}
 
 	/**
 	 * 获取最新推荐图书列表
 	 * */
 	@GET
 	@Path("getnewrecbooklistpage/{num}")
 	@Produces("application/json")
 	public Representation getNewRecBookList(@PathParam("num") int num) {
 
 		RecommendedBookPage page = new RecommendedBookPage(num, 1);
 		ArrayList<SLRecommendedBook> ret = (ArrayList<SLRecommendedBook>) dao.getLatestRecBooks(num);
 		page.setTheBooks(ret);
 		page.setTotalPageNum(1);
 
 		JsonRepresentation retRep = new JsonRepresentation(page);
 		try {
 			retRep.getJsonObject().put(RestCallInfo.REST_STATUS, RestCallStatus.success);
 			retRep.getJsonObject().put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.no_error);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return retRep;	
 		
 	}
 	
 	/**
 	 * 推荐图书
 	 * */
 	@PUT
 	@Path("recommendbook")
 	@Produces("application/json")
 	public Representation recommendBook(Representation entity) {
 		
 		JSONReader reader = new JSONValidatingReader();
 		HashMap result = null;
 		
 		HashMap returnInfo = new HashMap();
 		
 		try {
 			result = (HashMap) reader.read(entity.getText());
 		} catch (IOException e) {
 			e.printStackTrace();
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.json_format_error);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		if (result == null || !result.containsKey("sessionKey")) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.json_format_error);
 			return new JsonRepresentation(returnInfo);
 		}
 		String sessionKey = result.get("sessionKey").toString();
 		SLRecommendedBook recommendedBook = new SLRecommendedBook();
 		recommendedBook.parseMap(result);
 		
 		SLUser slUser = SLSessionManager.getSession(sessionKey);
 		if (slUser == null) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.need_login);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		// 检查是否已有
 		if (bookDao.queryByISBN(recommendedBook.getBookISBN()) != null) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.already_instore);
 			return new JsonRepresentation(returnInfo);
 		}
 		// 检查其是否已推荐
 		if (recommendHistoryDao.isRecommend(recommendedBook.getBookISBN(), recommendedBook.getRecUserEmail())) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.already_recommended);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		SLRecommendedBook allready = dao.queryByISBN(recommendedBook.getBookISBN());
 		SLRecommendHistory history = SLRecommendHistory.parse(recommendedBook);
 		if (allready != null) {
 			allready.setRecRate(allready.getRecRate() + 1);
 			dao.updateRecBook(allready);
 			recommendHistoryDao.insertRecHistory(history);
 		} else if (dao.insertRecBook(recommendedBook)) {
 			recommendHistoryDao.insertRecHistory(history);
 		} else {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.db_operate_error);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.success);
 		returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.no_error);
 		return new JsonRepresentation(returnInfo);
 		
 	}
 	
 	
 	// ----------------------------------------- Waiting -----------------------------------------------
 
 	public boolean updateRecommendedBookStatus(SLRecommendedBook recommendedBook) {
 		return dao.updateRecBook(recommendedBook);
 	}
 
 	public boolean buyBookList(ArrayList<String> bookISBNList) {
 		
 		ArrayList<SLRecommendedBook> buyList = new ArrayList<SLRecommendedBook>();
 		for (String bookISBN : bookISBNList) {
 			SLRecommendedBook buyBook = dao.queryByISBN(bookISBN);
 			buyBook.setRecStatus("已购买");
 			dao.updateRecBook(buyBook);
 			buyList.add(buyBook);
 		}
 
 		// 发送邮件
 		SLEmailUtil emailUtil = new SLEmailUtil();
 		emailUtil.sendBuyListEmail(buyList);
 		
 		return true;
 	}
 }
