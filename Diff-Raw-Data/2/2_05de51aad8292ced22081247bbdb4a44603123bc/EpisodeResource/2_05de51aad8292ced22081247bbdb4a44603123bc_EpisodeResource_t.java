 package com.mulesoft.summit.service;
 
 import java.math.BigInteger;
 import java.util.Date;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBElement;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.namespace.QName;
 
 import com.mulesoft.summit.adm.Episode;
 
 @Path("/api")
 public class EpisodeResource {
 
 	@GET
 	@Path("/{departmentName}/episodes")
 	@Produces(MediaType.APPLICATION_XML)
 	public JAXBElement<Episode> createEpisode(@PathParam("departmentName") String clinicName, @QueryParam("patientId") String patientId) {
 		Episode episode = new Episode();
 		episode.setEpisodeId("EP123412341");
 		episode.setMinDurationDays(BigInteger.valueOf(6));
 		episode.setPatientId(patientId);
 		episode.setStartDate(new Date());
		return new JAXBElement<Episode>(new QName("http://www.mule-health.com/HospitalInformation/", "Episode"), Episode.class, episode);
 	}
 
 }
