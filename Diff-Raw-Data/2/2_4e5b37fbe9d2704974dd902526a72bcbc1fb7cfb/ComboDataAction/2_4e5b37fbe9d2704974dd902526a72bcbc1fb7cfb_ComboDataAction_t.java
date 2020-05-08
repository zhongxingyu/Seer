 
 package edu.common.dynamicextensions.util.global;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.common.dynamicextensions.ui.webui.action.BaseDynamicExtensionsAction;
 import edu.common.dynamicextensions.util.DynamicExtensionsCacheManager;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * @author kunal_kamble
  * This action is called in the auto complete dropdown combo box
  *
  */
 public class ComboDataAction extends BaseDynamicExtensionsAction
 {
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		String limit = request.getParameter("limit");
 		String query = request.getParameter("query");
 		String start = request.getParameter("start");
 		String controlId = request.getParameter("controlId").split("~")[0].trim();
 		String containerId = request.getParameter("controlId").split("~")[1].split("=")[1].trim();
 
 		Integer limitFetch = Integer.parseInt(limit);
 		Integer startFetch = Integer.parseInt(start);
 
 		JSONArray jsonArray = new JSONArray();
 		JSONObject mainJsonObject = new JSONObject();
 
 		DynamicExtensionsCacheManager deCacheManager = DynamicExtensionsCacheManager.getInstance();
 		ContainerInterface container = (ContainerInterface) ((HashMap) deCacheManager.getObjectFromCache(Constants.LIST_OF_CONTAINER)).get(Long
 				.parseLong(containerId));
 
 		List<NameValueBean> nameValueBeans = null;
 		for (ControlInterface control : container.getControlCollection())
 		{
 			if (Long.parseLong(controlId) == control.getId())
 			{
 				nameValueBeans = ControlsUtility.populateListOfValues(control);
 			}
 		}
 
 		Integer total = limitFetch + startFetch;
 
 		List<NameValueBean> querySpecificNVBeans = new ArrayList<NameValueBean>();
 		populateQuerySpecificNameValueBeansList(querySpecificNVBeans, nameValueBeans, query);
 		mainJsonObject.put("totalCount", querySpecificNVBeans.size());
 
 		for (int i = startFetch; i < total && i < querySpecificNVBeans.size(); i++)
 		{
 			JSONObject jsonObject = new JSONObject();
 			if (query == null || querySpecificNVBeans.get(i).getName().toLowerCase().startsWith(query.toLowerCase()) || query.length() == 0)
 			{
 				jsonObject.put("id", querySpecificNVBeans.get(i).getValue());
 				jsonObject.put("field", querySpecificNVBeans.get(i).getName());
 				jsonArray.put(jsonObject);
 			}
 		}
 
 		mainJsonObject.put("row", jsonArray);
 		response.flushBuffer();
 		PrintWriter out = response.getWriter();
 		out.write(mainJsonObject.toString());
 
 		return null;
 	}
 
 	/**
 	 * This method populates name value beans list as per query,
 	 * i.e. word typed into the auto-complete drop-down text field.
 	 * @param querySpecificNVBeans
 	 * @param nameValueBeans
 	 * @param query
 	 */
 	private void populateQuerySpecificNameValueBeansList(List<NameValueBean> querySpecificNVBeans, List<NameValueBean> nameValueBeans, String query)
 	{
 		for (NameValueBean nvb : nameValueBeans)
 		{
			if (nvb.getName().toLowerCase().startsWith(query.toLowerCase()))
 			{
 				querySpecificNVBeans.add(nvb);
 			}
 		}
 	}
 
 }
