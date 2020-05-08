 package com.avalchev.ide.webapp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.atmosphere.cpr.Broadcaster;
 import org.atmosphere.cpr.BroadcasterFactory;
 
 import com.avalchev.ide.model.User;
 
 @Path("project")
 public class WebAPIProject {
 	
 	@POST @Produces(MediaType.TEXT_PLAIN) @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String createProject(@FormParam("artifact") String artifact, @FormParam("group") String group, @Context HttpServletRequest request) 
 	throws Exception {
 		Broadcaster b = BroadcasterFactory.getDefault().lookup(request.getSession().getId());
 		final ApplicationContext appContext = ApplicationContext.getAppContext(request.getServletContext());
 		final User user = (User) request.getSession().getAttribute("user");
 
 		ProcessBuilder pb = new ProcessBuilder("mvn", "archetype:generate", "-DgroupId=" + group, "-DartifactId=" + artifact, "-DarchetypeArtifactId=maven-archetype-quickstart", "-DinteractiveMode=false");
 		pb.redirectErrorStream(true); 
 		pb.directory(new File(appContext.getWorkspaceService().getUserWorkspace(user.getUsername())));
         Process process = pb.start();  
         
         InputStream stdout = process.getInputStream ();  
         BufferedReader reader = new BufferedReader (new InputStreamReader(stdout)); 
         
         String line = reader.readLine(); 
         while (line != null && ! line.trim().equals("--EOF--")) { 
        	b.broadcast(" { line: '" + line + "' } ");
        	System.out.println(line);
            line = reader.readLine(); 
        }        
         
 		return "OK";
 	}	
 	
 }
