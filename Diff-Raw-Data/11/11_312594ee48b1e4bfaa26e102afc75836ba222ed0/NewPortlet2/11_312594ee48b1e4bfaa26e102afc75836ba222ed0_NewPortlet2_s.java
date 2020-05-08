 package com.test;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 import javax.portlet.ProcessAction;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.liferay.portal.kernel.servlet.ServletResponseUtil;
 import com.liferay.portal.kernel.util.ParamUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 
 /**
  * Portlet implementation class NewPortlet2
  */
 public class NewPortlet2 extends MVCPortlet {
  
 	public void updateBook(ActionRequest actionRequest,
 			ActionResponse actionResponse)
 			throws IOException, PortletException {
 			String bookTitle = ParamUtil.getString(actionRequest, "bookTitle");
 			String author = ParamUtil.getString(actionRequest, "author");
 			System.out.println("Your inputs ==> " + bookTitle + ", " + author);
 			}
 	
 
 	/**
 	 * 
 	 * @param request Ignored
 	 * @param response Will contain a JSONArray, containing JSONObjects like:<br>
 	 * category_id (int)<br>
 	 * category_name (String)
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "getCategories")
 	public void getCategories(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		JSONArray list = DataBaseFunctions.getCategories(DataBaseFunctions.getWebConnection());
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 * 				Possible parameters:<br>
 	 * 				drug_id (int),<br>
 	 * 				category_id (int)
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "getDrugs")
 	public void getDrugs(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		JSONArray list = DataBaseFunctions.getDrugs(DataBaseFunctions.getWebConnection(),parameters);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 
 	/**
 	 * 
 	 * @param request
 	 *            Possible parameters:<br>
 	 *            order_id (int),<br>
 	 *            order_start (String/Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
 	 *            order_end (String/Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
 	 *            order_status (String, one of: 'initiated','sent','delivered','canceled'<br>
 	 *            facility_id (int),<br>
 	 *            facility_name (String), <br>
 	 *            summarized (Boolean)
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "getOrderSummary")
 	public void getOrderSummary(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		JSONArray list = DataBaseFunctions.getOrderSummary(DataBaseFunctions.getWebConnection(),parameters);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 
 	/**
 	 * 
 	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "sent" as order_status to achieve same functionality.
 	 */
 	@Deprecated
 	@ProcessAction(name = "getSentOrderSummary")
 	public void getSentOrderSummary(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		parameters.put("order_status", "sent");
 		JSONArray list = DataBaseFunctions.getOrderSummary(DataBaseFunctions.getWebConnection(),parameters);
 		
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	/**
 	 * 
 	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "order_id" and "summarize = false" as parameters to achieve same functionality.
 	 */
 	@Deprecated
 	@ProcessAction(name = "getOrderItems")
 	public void getOrderItems(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		parameters.put("summarize", "false");
 		JSONArray list = DataBaseFunctions.getOrderSummary(DataBaseFunctions.getWebConnection(),parameters);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 
 	/**
 	 * 
 	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "order_status : sent", "order_id" and "summarize : false" as parameters to achieve same functionality.
 	 */
 	@Deprecated
 	@ProcessAction(name = "getSentOrderItems")
 	public void getSentOrderItems(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		parameters.put("order_status", "sent");
 		parameters.put("summarize", "false");
 		JSONArray list = DataBaseFunctions.getOrderSummary(DataBaseFunctions.getWebConnection(),parameters);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param request
 	 *            Parameters:<br>
 	 *            facility_id : (int),<br>
 	 * <br>
 	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
 	 *            difference (int)) will have to be added
 	 * @param response
 	 *            1 if query successful, 0 otherwise
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "updateStock")
 	public void updateStock(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		boolean result = DataBaseFunctions.updateInventory(DataBaseFunctions.getWebConnection(),parameters);
 		
 
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, result?"1":"0");
 		
 		
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 *            Parameters:<br>
 	 *            order_id (int),<br>
 	 *            status (String),<br>
 	 * @param response
 	 *            1 if query successful, 0 otherwise
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "updateOrder")
 	public void updateOrder(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		boolean result = DataBaseFunctions.updateOrderStatus(DataBaseFunctions.getWebConnection(),parameters);
 		
 
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, result?"1":"0");
 		
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 *            Parameters:<br>
 	 *            Mandatory:<br>
 	 *            msdcode (int),<br>
 	 *            category_id (int),<br>
 	 *            med_name (String),<br>
 	 *            unit_price (Double)<br>
 	 *            Optional:<br>
 	 *            common_name (String),<br>
 	 *            unit (String),<br>
 	 *            unit_details (String)
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "addNewDrug")
 	public void addNewDrug(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		boolean result = DataBaseFunctions.addDrug(DataBaseFunctions.getWebConnection(),parameters);
 		
 
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, result?"1":"0");
 		
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 *            Parameters:<br>
 	 *            Mandatory:<br>
 	 *            id (int)<br>
 	 *            Optional:<br>
 	 *            msdcode (int),<br>
 	 *            category_id (int),<br>
 	 *            med_name (String),<br>
 	 *            common_name (String),<br>
 	 *            unit (String),<br>
 	 *            unit_details (String),<br>
 	 *            unit_price (Double)<br>
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "updateDrug")
 	public void updateDrug(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		boolean result = DataBaseFunctions.updateDrug(DataBaseFunctions.getWebConnection(),parameters);
 		
 
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, result?"1":"0");
 	}
 	
 }
