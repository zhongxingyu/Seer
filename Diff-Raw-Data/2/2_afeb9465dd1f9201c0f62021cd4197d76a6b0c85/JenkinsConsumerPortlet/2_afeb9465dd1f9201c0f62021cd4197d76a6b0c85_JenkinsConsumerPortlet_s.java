 package com.liferay.ci.portlet;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.liferay.ci.http.JenkinsConnectUtil;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.util.ParamUtil;
 import com.liferay.portal.kernel.util.StringPool;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 
 public class JenkinsConsumerPortlet extends MVCPortlet {
 
 	@Override
 	public void init() throws PortletException {
 		super.init();
 
 		_jsonCache = new HashMap<String, JSONArray>();
 	}
 
 	public void getBuilds(
 			ActionRequest actionRequest, ActionResponse actionResponse)
 		throws Exception {
 
 		String jobName = ParamUtil.get(
 			actionRequest, "jobName", StringPool.BLANK);
 
 		_log.debug("Getting builds for " + jobName);
 
 		if (!_jsonCache.containsKey(jobName)) {
 			try {
 				JSONArray testResults = JenkinsConnectUtil.getBuilds(jobName);
 
 				_log.debug("Caching test result");
 
 				_jsonCache.put(jobName, testResults);
 			}
 			catch (IOException ioe) {
 				_log.error("The job was not available", ioe);
 			}
 			catch (JSONException e) {
				
 			}
 		}
 
 		actionRequest.setAttribute("TEST_RESULTS", _jsonCache.get(jobName));
 	}
 
 	private static Map<String, JSONArray> _jsonCache;
 
 	private static Log _log = LogFactoryUtil.getLog(
 		JenkinsConsumerPortlet.class);
 
 }
