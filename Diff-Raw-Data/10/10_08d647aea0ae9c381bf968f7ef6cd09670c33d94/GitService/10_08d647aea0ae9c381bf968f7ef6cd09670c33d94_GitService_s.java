 package com.internetitem.gitdown.service;
 
 import java.net.URI;
 
import javax.annotation.Resource;
 import javax.servlet.ServletContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import com.internetitem.gitdown.FileData;
 import com.internetitem.gitdown.GitHelper;
 
 @Path("/")
 public class GitService {
 
	@Resource
 	private ServletContext servletContext;
 
 	private GitHelper gitHelper;
 
 	public GitService(GitHelper gitHelper) {
 		this.gitHelper = gitHelper;
 	}
 
 	@GET
 	@Path("")
 	public Response serveFile() throws Exception {
 		return serveFile("");
 	}
 
 	@GET
 	@Path("{page: .+}")
 	public Response serveFile(@PathParam("page") String path) throws Exception {
 		FileData data = gitHelper.getData(path);
 		switch (data.getFileDataType()) {
 		case NotFound:
 			return Response.status(Status.NOT_FOUND).build();
 		case Redirect:
 			Response.status(Status.MOVED_PERMANENTLY).location(new URI(data.getActualName())).build();
 		case File:
 		case IndexFile:
 		default:
 			String contentType = getContentType(data.getActualName());
 			byte[] bytes = data.getData();
 			return Response.ok(bytes, contentType).build();
 		}
 	}
 
 	private String getContentType(String actualName) {
 		if (actualName.endsWith(".md")) {
 			return "text/html";
 		} else {
 			return servletContext.getMimeType(actualName);
 		}
 	}
 
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
 }
