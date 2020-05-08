 package org.provoysa12th.directory.rest.impl;
 
 import java.util.List;
 
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.Variant;
 import javax.ws.rs.core.Variant.VariantListBuilder;
 
 import org.apache.abdera.model.Feed;
 import org.provoysa12th.directory.rest.UriInfoLinkBuilderFactory;
 import org.provoysa12th.directory.rest.WellknownResources;
 
 import com.google.common.net.HttpHeaders;
 
 public class WellknownResourcesImpl implements WellknownResources {
 
 	public static final List<Variant> VARIANTS;
 
 	private static final int MAX_AGE = 24 * 60 * 60;	// 24 hours
 
 	static {
 		VARIANTS = VariantListBuilder.newInstance()
 			.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
 			.build();
 	}
 
 	@Context
 	Request request;
 
 	@Context
 	UriInfo uriInfo;
 
 	public Response heartbeat() {
 		return Response.ok().build();
 	}
 
 	public Response serviceMeta() {
 		Variant variant = request.selectVariant(VARIANTS);
 
 		String baseURI = new UriInfoLinkBuilderFactory(uriInfo)
 							.newBuilder()
 							.build()
 							.getHref();
 
 		Feed discovery = new ServiceDiscoveryBuilder(baseURI).build();
 
 		CacheControl cacheControl = new CacheControl();
 		cacheControl.setMaxAge(MAX_AGE);
 
 		return Response.ok(discovery)
 				.cacheControl(cacheControl)
 				.variant(variant)
 				.header(HttpHeaders.VARY, HttpHeaders.ACCEPT)
 				.build();
 	}
 
 }
