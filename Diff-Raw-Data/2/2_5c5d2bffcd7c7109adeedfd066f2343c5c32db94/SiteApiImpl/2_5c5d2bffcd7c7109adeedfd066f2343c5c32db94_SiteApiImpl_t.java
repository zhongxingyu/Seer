 package com.webs.api;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 
 import com.webs.api.exception.UsageErrorApiException;
 import com.webs.api.http.AbstractHttpApiClientAware;
 import com.webs.api.model.Site;
 import com.webs.api.model.id.SiteId;
 
 
 /**
  * @author Patrick Carroll
  */
 public class SiteApiImpl extends AbstractHttpApiClientAware implements SiteApi {
 	public SiteApiImpl() {
 	}
 
 
 	public Site getSite(final SiteId siteId) {
 		return httpApiClient.httpRequestMapper(
 				new GetMethod(httpApiClient.getApiPath() + "sites/" 
 					+ siteId.toString()),
 				HttpStatus.SC_OK, new WebsApiModelMapper<Site>(Site.class));
 	}
 
 	public void updateSite(final Site site) {
 		String identifier;
 		if (site.getId() != null) 
 			identifier = site.getId().toString();
 		else if (site.getUsername() != null) 
 			identifier = site.getUsername().toString();
 		else 
 			throw new UsageErrorApiException("updateSite requires either site.id or site.username to be set");
 
 		PutMethod put = new PutMethod(httpApiClient.getApiPath() 
 				+ "sites/" + identifier);
 		try {
 			put.setRequestBody(jsonMapper.writeValueAsString(site));
 		} catch (IOException e) {
 			throw new UsageErrorApiException("Error mapping object");
 		}
 
 		httpApiClient.httpRequest(put, HttpStatus.SC_NO_CONTENT);
 	}
 
 
 	public void deleteSite(final SiteId siteId) {
		httpApiClient.httpRequest(new DeleteMethod(httpApiClient.getApiPath() + "sites/" + siteId.toString()), HttpStatus.SC_NO_CONTENT);
 	}
 }
