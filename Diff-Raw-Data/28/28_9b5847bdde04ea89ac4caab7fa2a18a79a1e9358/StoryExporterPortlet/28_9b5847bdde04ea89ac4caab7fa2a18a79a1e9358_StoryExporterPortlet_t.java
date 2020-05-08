 package org.goodreturn.story.exporter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 
 import javax.portlet.PortletException;
 import javax.portlet.RenderRequest;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.liferay.portal.kernel.servlet.ServletResponseUtil;
 import com.liferay.portal.kernel.util.ContentTypes;
 import com.liferay.portal.kernel.util.ParamUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 
 /**
  * Portlet implementation class StoryExporterPortlet
  */
 public class StoryExporterPortlet extends MVCPortlet {
 	
 	
 	public void serveResource(ResourceRequest req, ResourceResponse res) throws PortletException, IOException {
 		
		boolean downloadCsv = ParamUtil.getBoolean(req, "downloadCsv");
 		boolean approvedOnly = ParamUtil.getBoolean(req, "approvedOnly");
		
		//TODO Other options
		
 		if (downloadCsv) {
 			//Retrieves data as CSV data.
 			LinkedList<String[]> data = StoryExporter.loadData(approvedOnly);
 			LinkedList<String> csvData = StoryExporter.createCsvData(data);
 
 			//Creates stream for CSV data.
 			InputStream in = new LinesInputStream(csvData);
 
 			//Sends data to user.
 			HttpServletRequest request = PortalUtil.getHttpServletRequest(req);
 			HttpServletResponse response = PortalUtil.getHttpServletResponse(res);
 			ServletResponseUtil.sendFile(request,response, "stories.csv", in, ContentTypes.TEXT_CSV);
 
 			in.close();
 		}
 	}
 }
