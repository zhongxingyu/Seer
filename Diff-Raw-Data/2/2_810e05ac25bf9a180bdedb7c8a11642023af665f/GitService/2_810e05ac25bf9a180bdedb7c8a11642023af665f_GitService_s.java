 package com.internetitem.gitdown.service;
 
 import java.net.URI;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import com.internetitem.gitdown.FileData;
 import com.internetitem.gitdown.GitHelper;
 import com.internetitem.gitdown.MarkdownHelper;
 
 @Path("/")
 public class GitService {
 
 	@Context
 	private ServletContext servletContext;
 
 	private GitHelper gitHelper;
 
 	private MarkdownHelper markdownHelper;
 
 	public GitService(GitHelper gitHelper, MarkdownHelper markdownHelper) {
 		this.gitHelper = gitHelper;
 		this.markdownHelper = markdownHelper;
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
 			String actualName = data.getActualName();
 			byte[] bytes;
 			String contentType;
 			if (markdownHelper.isMarkdown(actualName)) {
 				bytes = markdownHelper.convertMarkdown(data.getData());
 				contentType = "text/html; charset=utf-8";
 			} else {
 				bytes = data.getData();
 				contentType = getContentType(actualName);
 			}
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
 
 }
