 package com.avalchev.ide.webapp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.atmosphere.cpr.Broadcaster;
 import org.atmosphere.cpr.BroadcasterFactory;
 
 import com.avalchev.ide.model.BuildMessage;
 import com.avalchev.ide.model.Group;
 import com.avalchev.ide.model.Project;
 import com.avalchev.ide.model.ProjectFileAction;
 import com.avalchev.ide.model.Resource;
 import com.avalchev.ide.model.User;
 import com.avalchev.ide.model.UserTask;
 
 @Path("/user/{userId}")
 public class WebAPIUser {
 
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("project/{name}")
 	public Project getProject(@PathParam("userId") String userId, @PathParam("name") String name, @Context HttpServletRequest request) 
 	throws Exception {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		//TODO: return only of user is manager or currently logged.
 		return appContext.getWorkspaceService().getProject(userId, name);		
 	}
 	
 	/**
 	 * Returns list {@code userId} projects. Projects do not contain the 
 	 * resources. 
 	 * 
 	 * See {@link #getProjectContent(String, String, HttpServletRequest)} for 
 	 * getting project's content.
 	 * 
 	 * @param userId username
 	 * @param request {@link HttpServletRequest} injected by jersey.
 	 * @return list with user's projects.
 	 * @throws Exception
 	 */
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("project")
 	public List<Project> getProjects(@PathParam("userId") String userId, @Context HttpServletRequest request) 
 	throws Exception {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());		
 		return appContext.getWorkspaceService().getProjectList(userId);
 	}
 	
 	@POST @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	@Path("project/{name}/build")
 	public String getProjectBuild(
 			@PathParam("userId") String userId, 
 			@PathParam("name") String name,
 			@FormParam("goals") String goals,
 			@Context HttpServletRequest request) 
 	throws Exception {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		final Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(request.getSession().getId());
 		String projectRoot = appContext.getWorkspaceService().getProjectDirectory(userId, name).toString();
 		
 		goals = "mvn ".concat(goals);
 		final List<String> commandLine = Arrays.asList(goals.split(" "));
 		
 		ProcessBuilder pb = new ProcessBuilder(commandLine);
 		pb.redirectErrorStream(true); 
 		pb.directory(new File(projectRoot));
         Process process = pb.start();  
 
         InputStream stdout = process.getInputStream ();  
         BufferedReader reader = new BufferedReader (new InputStreamReader(stdout)); 
 
         String line = reader.readLine(); 
         while (line != null && ! line.trim().equals("--EOF--")) {
         	BuildMessage bm = new BuildMessage();
         	bm.setLine(line);
         	bm.setProject(name);
         	broadcaster.broadcast(bm);
             line = reader.readLine(); 
         }
 
 		return "OK";		
 	}
 	
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("project/{name}/content")
 	public Project getProjectContent(@PathParam("userId") String userId, @PathParam("name") String name, @Context HttpServletRequest request) 
 	throws Exception {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		
 		appContext.getWatchService().addWatcher(userId, name, request.getSession().getId());
 		
 		return appContext.getWorkspaceService().getProject(userId, name);		
 	}
 
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("project/{name}/close")
 	public void getProjectClose(@PathParam("userId") String userId, @PathParam("name") String name, @Context HttpServletRequest request) 
 	throws Exception {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		
 		appContext.getWatchService().removeWatcher(userId, name, request.getSession().getId());		
 	}
 
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("project/{name}/file")
 	public ProjectFileAction getProjectFile(
 			@PathParam("userId") String userId,
 			@PathParam("name") String projectId,
 			@QueryParam("file") String fileId,
 			@QueryParam("action") String action,
 			@QueryParam("type") String type,
 			@Context HttpServletRequest request
 			) 
 	throws Exception {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		
 		ProjectFileAction result = new ProjectFileAction();
 		if ("content".equals(action)) {
 			final Resource resource = appContext.getWorkspaceService().getFile(userId, projectId, fileId);
 			result.setResource(resource);
 			result.setAction(action);
 		}
 		if ("create".equals(action)) {
 			/* Notification is sent throught ProjectWatch */
             appContext.getWorkspaceService().createFile(userId, projectId, type, fileId);
 		}
 		if ("delete".equals(action)) {
             appContext.getWorkspaceService().deleteFile(userId, projectId, fileId);			
 		}
 		return result;
 	}
 
 	@POST @Consumes(MediaType.APPLICATION_FORM_URLENCODED) @Produces(MediaType.TEXT_PLAIN)
 	@Path("project/{name}/file")
 	public String save(
 			@PathParam("userId") String userId,
 			@PathParam("name") String projectId,
 			@FormParam("content") String content,
 			@FormParam("file") String file,
 			@Context HttpServletRequest request			
 			) {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		appContext.getWorkspaceService().save(userId, projectId, file, content);
 		
 		return "OK";
 	}
 	
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("/group")
 	public List<Group> getGroups(@Context HttpServletRequest request) {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		final User user = (User) request.getSession().getAttribute("user");
 
 		return appContext.getGroupService().getGroupsByUsername(user.getUsername());
 	}
 	
 	@GET @Produces(MediaType.APPLICATION_JSON)
 	@Path("/task")
 	public List<UserTask> getUserTasks(@Context HttpServletRequest request) {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		final User user = (User) request.getSession().getAttribute("user");
 		
 		return appContext.getTaskService().getUserTaskByUsername(user.getUsername());
 	}
 	
 	@POST @Produces(MediaType.TEXT_PLAIN) @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	@Path("/task")
 	public String saveTask(
 			@PathParam("userId") String userId,
 			@FormParam("id") String id,
 			@FormParam("group") String groupId,
 			@FormParam("username") String username,
 			@FormParam("name") String name,
 			@FormParam("manager") String manager,
 			@FormParam("description") String description,
 			@FormParam("project") String project,
 			@FormParam("status") String status,
 			@FormParam("comment") String comment,
 			@Context HttpServletRequest request) {
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 
 		UserTask task = appContext.getTaskService().getTaskById(id);
 		if (task == null) {
 			task = new UserTask();
 			task.setId(id);
 			task.setGroupId(groupId);
 			task.setComment(comment);
 			task.setUsername(username);
 			task.setName(name);
 			task.setManager(manager);
 			task.setDescription(description);
 			task.setProject(project);
 			task.setStatus(status);
 			task.setComment(comment);
 		} else {
 			if (name != null) {
 				task.setName(name);
 			}
 			if (description != null) {
 				task.setDescription(description);
 			}
 			if (status != null) {
 				task.setStatus(status);
 			}
 			if (project != null) {
 				task.setProject(project);
 			}
 		}
 		
 		appContext.getTaskService().save(task);
 		
 		return "OK";
 	}
 	
 }
