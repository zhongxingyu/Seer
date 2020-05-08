 package org.openrdf.server.metadata;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.ext.Providers;
 
 import org.openrdf.model.URI;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.ObjectConnection;
 import org.openrdf.repository.object.ObjectRepository;
 import org.openrdf.server.metadata.annotations.OPTIONS;
 import org.openrdf.server.metadata.annotations.TRACE;
 
 import com.sun.jersey.api.core.ResourceContext;
 
 @Path("{path:.*}")
 public class MetaDataResource {
 	private ObjectRepository repository;
 	private File dataDir;
 
 	public MetaDataResource(ObjectRepository repository, File dataDir) {
 		this.repository = repository;
 		this.dataDir = dataDir;
 	}
 
 	@GET
 	public Response get(@Context Request request, @Context ResourceContext ctx,
 			@Context Providers providers, @Context HttpHeaders headers) throws Throwable {
 		SubResource subResource = subResource(request, ctx, providers);
 		ResponseBuilder rb = subResource.get();
 		addLinks(request, ctx, providers, rb);
 		List<String> prefer = headers.getRequestHeader("Prefer");
		if (prefer != null && prefer.contains("return-no-content")
				&& rb.build().getStatus() == 200) {
			rb = rb.entity(null).status(204);
 		}
 		return rb.build();
 	}
 
 	@PUT
 	public Response put(@Context Request request, @Context ResourceContext ctx,
 			@Context Providers providers, @Context HttpHeaders headers,
 			InputStream in) throws Throwable {
 		SubResource subResource = subResource(request, ctx, providers);
 		ResponseBuilder rb = subResource.put(headers, in);
 		List<String> prefer = headers.getRequestHeader("Prefer");
 		if (prefer != null && prefer.contains("return-content")
 				&& rb.build().getStatus() == 204) {
			rb = subResource.get();
			addLinks(request, ctx, providers, rb);
 		}
 		return rb.build();
 	}
 
 	@POST
 	public Response post(@Context Request request,
 			@Context ResourceContext ctx, @Context Providers providers,
 			@Context HttpHeaders headers, InputStream in) throws Throwable {
 		ResponseBuilder rb = subResource(request, ctx, providers).post(headers, in);
 		List<String> prefer = headers.getRequestHeader("Prefer");
		if (prefer != null && prefer.contains("return-no-content")
				&& rb.build().getStatus() == 200) {
			rb = rb.entity(null).status(204);
 		}
 		return rb.build();
 	}
 
 	@DELETE
 	public Response delete(@Context Request request,
 			@Context ResourceContext ctx, @Context Providers providers)
 			throws Throwable {
 		return subResource(request, ctx, providers).delete().build();
 	}
 
 	@OPTIONS
 	public Response options(@Context Request request,
 			@Context ResourceContext ctx, @Context Providers providers,
 			@Context UriInfo info) throws RepositoryException {
 		String path = info.getPath();
 		ResponseBuilder rb = Response.ok();
 		if ("*".equals(path)) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("OPTIONS, TRACE, GET, HEAD, PUT, POST, DELETE");
 			rb.header("Allow", sb.toString());
 		} else {
 			StringBuilder sb = new StringBuilder();
 			sb.append("OPTIONS, TRACE");
 			SubResource subResource = subResource(request, ctx, providers);
 			for (String method : subResource.getAllowedMethods()) {
 				sb.append(", ").append(method);
 			}
 			rb.header("Allow", sb.toString());
 			addLinks(request, ctx, providers, rb);
 		}
 		return rb.build();
 	}
 
 	@TRACE
 	public Response trace(@Context UriInfo info, @Context HttpHeaders headers)
 			throws RepositoryException {
 		StringBuilder sb = new StringBuilder();
 		sb.append("TRACE ").append(info.getRequestUri()).append("\r\n");
 		for (Map.Entry<String, List<String>> e : headers.getRequestHeaders()
 				.entrySet()) {
 			for (String value : e.getValue()) {
 				sb.append(e.getKey()).append(": ");
 				sb.append(value).append("\r\n");
 			}
 		}
 		sb.append("\r\n");
 		return Response.ok().type("message/http").entity(sb.toString()).build();
 	}
 
 	private SubResource subResource(Request request, ResourceContext ctx,
 			Providers providers) throws RepositoryException {
 		ValueFactoryResource resolver;
 		resolver = ctx.getResource(ValueFactoryResource.class);
 		ObjectConnection con = getConnection(ctx);
 		resolver.setValueFactory(con.getValueFactory());
 		resolver.setDataDir(dataDir);
 		File file = resolver.getFile();
 		URI uri = resolver.getURI();
 		MultivaluedMap<String, String> p = resolver.getQueryParameters();
 		if (!request.getMethod().equals("POST") && p.isEmpty()) {
 			return new DataResource(request, ctx, providers, file, con, uri, p);
 		} else {
 			return new MetaResource(request, ctx, providers, file, con, uri, p);
 		}
 	}
 
 	private void addLinks(Request request, ResourceContext ctx,
 			Providers providers, ResponseBuilder rb) throws RepositoryException {
 		ValueFactoryResource resolver;
 		resolver = ctx.getResource(ValueFactoryResource.class);
 		ObjectConnection con = getConnection(ctx);
 		resolver.setValueFactory(con.getValueFactory());
 		resolver.setDataDir(dataDir);
 		File file = resolver.getFile();
 		URI uri = resolver.getURI();
 		MultivaluedMap<String, String> p = resolver.getQueryParameters();
 		MetaResource meta;
 		meta = new MetaResource(request, ctx, providers, file, con, uri, p);
 		String anchor = null;
 		if (!p.isEmpty()) {
 			anchor = "; anchor=<" + resolver.getURI().stringValue() + ">";
 		}
 		for (String link : meta.getLinks()) {
 			rb.header("Link", anchor == null ? link : link + anchor);
 		}
 	}
 
 	private ObjectConnection getConnection(ResourceContext ctx)
 			throws RepositoryException {
 		ConnectionResource closer = ctx.getResource(ConnectionResource.class);
 		ObjectConnection con = closer.getConnection();
 		if (con == null) {
 			con = repository.getConnection();
 			con.setAutoCommit(false);
 			closer.setConnection(con);
 		}
 		return con;
 	}
 }
