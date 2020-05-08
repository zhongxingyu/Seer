 package com.b4e.test_maker.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.b4e.test_maker.providers.GenericProvider;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
 
 public class CategoryRedirectServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final String PAGE_URL_BLANK_CATEGORY = "jsp/blank_category.jsp";
 	public static final String PAGE_URL_EDIT_CATEGORY = "jsp/edit_category.jsp";
 	public static final String PAGE_URL_SHOW_TESTS = "jsp/show_tests.jsp";
 	public static final String PAGE_URL_REDIRECT = "/category_redirect";
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		processRequest(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		processRequest(req, resp);
 	}
 
 	private void processRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String keyAsString = req.getParameter(GenericProvider.PARAM_KEY);
 		if (keyAsString != null && !keyAsString.isEmpty()) {
 			GenericProvider genericProvider = new GenericProvider();
 			int entitySchildStatus = genericProvider
 					.getEntitySchildStatus(keyAsString);
 
 			switch (entitySchildStatus) {
 			case GenericProvider.CHILD_STATUS_NO_CHILDREN:
 				resp.sendRedirect(GenericProvider.getUrlWithKeyParameter(
 						PAGE_URL_BLANK_CATEGORY, keyAsString));
 				break;
 			case GenericProvider.CHILD_STATUS_CATEGORIES:
 				resp.sendRedirect(GenericProvider.getUrlWithKeyParameter(
 						PAGE_URL_EDIT_CATEGORY, keyAsString));
 				break;
 			case GenericProvider.CHILD_STATUS_TESTS:
 				resp.sendRedirect(GenericProvider.getUrlWithKeyParameter(
 						PAGE_URL_SHOW_TESTS, keyAsString));
 				break;
 			case GenericProvider.CHILD_STATUS_IT_IS_TEST:
				Key key = KeyFactory.stringToKey(keyAsString);
				Key parentKeyAsString = key.getParent();
 				resp.sendRedirect(GenericProvider.getUrlWithKeyParameter(
						PAGE_URL_SHOW_TESTS, KeyFactory.keyToString(parentKeyAsString)));
 				break;
 			default:
 				resp.sendRedirect(AddPredmetServlet.PAGE_URL_EDIT_PREDMET);
 				break;
 			}
 		} else {
 			resp.sendRedirect(AddPredmetServlet.PAGE_URL_EDIT_PREDMET);
 		}
 	}
 }
