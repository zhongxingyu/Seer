 package com.tantaman.ferox.webfinger.priv;
 
 import java.util.logging.Level;
 
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpResponseStatus;
 
 import com.tantaman.ferox.api.request_response.IHttpContent;
 import com.tantaman.ferox.api.request_response.IRequestChainer;
 import com.tantaman.ferox.api.request_response.IResponse;
 import com.tantaman.ferox.api.router.RouteHandlerAdapter;
 import com.tantaman.ferox.webfinger.IResourceProvider;
 import com.tantaman.ferox.webfinger.Serializer;
 import com.tantaman.ferox.webfinger.WebfingerInitializer;
 import com.tantaman.ferox.webfinger.entry.IStaticWebfingerEntry;
 import com.tantaman.ferox.webfinger.entry.IWebfingerEntry;
 
 public class IdentityHandler extends RouteHandlerAdapter {
 	private final IResourceProvider resourceProvider;
 	private final Serializer serializer = new Serializer();
 	
 	public IdentityHandler(IResourceProvider resourceProvider) {
 		this.resourceProvider = resourceProvider;
 	}
 
 	@Override
 	public void lastContent(IHttpContent content, IResponse response,
 			IRequestChainer next) {
 		String resource = content.getQueryParam("resource").get(0);
 		WebfingerInitializer.logger.log(Level.INFO, "Retrieving identity: " + resource);
 		response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
 		try {
 			if (resourceProvider == null) {
 				send404(response);
 				return;
 			}
 			IWebfingerEntry r = resourceProvider.getIdentity(resource);
 			if (r == null) {
 				send404(response);
 			} else {
 				if (r instanceof IStaticWebfingerEntry) {
 					response.send(((IStaticWebfingerEntry)r).getContents(), "application/jrd+json");
 				} else {
					
 				}
 			}
 		} catch (Exception e) {
 			response.send("Internal server error", HttpResponseStatus.INTERNAL_SERVER_ERROR);
 		}
 	}
 	
 	private void send404(IResponse response) {
 		response.send("Not found", HttpResponseStatus.NOT_FOUND);
 	}
 }
