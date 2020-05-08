 package com.tracker.crime.rest.endpoint;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
 import com.tracker.crime.domain.Incident;
 import com.tracker.crime.domain.Location;
 import com.tracker.crime.domain.service.IncidentManager;
 import com.tracker.crime.dto.IncidentReport;
 
 @Component
 @Path("/incident")
 public class IncidentController {
 
 	
 	public IncidentManager manager ;
 	
 	
 	@Autowired
 	public void setManager(IncidentManager manager) {
 		this.manager = manager;
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/top10")
 	public List<IncidentReport> getTopTenIncidents() {
 		
 		return manager.getTopTenIncidents();
 	}
 	
 	@POST
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	@Produces("text/plain")
 	public String uploadFile( @FormDataParam("file") InputStream uploadedInputStream,
 	        @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("latitude") String latitude,
 	        @FormDataParam("longitude") String longitude, @FormDataParam("description") String description) {
  
 		String uploadedFileLocation = "/Users/amaloo/dev/apache-tomcat-6.0.36/webapps/servicearchitecture/images/" + fileDetail.getFileName();
  
 		// save it
 		writeToFile(uploadedInputStream, uploadedFileLocation);
 		
 		Incident inc = createNewIncident(description,latitude,longitude,uploadedFileLocation);
 		
 		
 		
 		manager.addUpdateIncident(inc);
  
 		String output = "File uploaded to : " + uploadedFileLocation;
  
 		return output;
  
 	}
 	
 	
 	
 	
 	private Incident createNewIncident(String description, String latitude,
 			String longitude, String uploadedFileLocation) {
 		Incident inc = new Incident();
 		inc.setActive(true);
 		inc.setDescription(description);
 		Location loc = new Location();
 		loc.setLattitude(latitude);
 		loc.setLongitude(longitude);
 		inc.setLocation(loc);
 		inc.setReportingDate(new Date());
 		inc.setUrl(uploadedFileLocation);
 		inc.setSpamCount(0);
 		return inc;
 	}
 
 	private void writeToFile(InputStream uploadedInputStream,
 			String uploadedFileLocation) {
 	 
 			try {
 				OutputStream out = new FileOutputStream(new File(
 						uploadedFileLocation));
 				int read = 0;
 				byte[] bytes = new byte[1024];
 	 
 				out = new FileOutputStream(new File(uploadedFileLocation));
 				while ((read = uploadedInputStream.read(bytes)) != -1) {
 					out.write(bytes, 0, read);
 				}
 				out.flush();
 				out.close();
 			} catch (IOException e) {
 	 
 				e.printStackTrace();
 			}
 	 
 		}
 	 
  
 	
 }
