 package de.wak_sh.client.backend.service;
 
 import java.io.IOException;
 
 import de.wak_sh.client.Utils;
 import de.wak_sh.client.backend.model.UserInformation;
 
 /*
  * Parts of this file are based on the work of Patrick Gotthard:
  * http://www.patrick-gotthard.de/4659/wakclient
  */
 public class UserInformationService {
 	private DataService dataService;
 	private UserInformation userInformation;
 
 	public UserInformationService() {
 		dataService = DataService.getInstance();
 	}
 
 	public UserInformation getUserInformation() throws IOException {
 		if (userInformation == null) {
 			userInformation = new UserInformation(getUsername(),
					getStudyPath(), getStudyGroup(), getMatriculationNumber());
 		}
 		return userInformation;
 	}
 
 	public String getUsername() throws IOException {
 		return Utils.match("<b>Hallo&nbsp;(.*?)!</b>",
 				dataService.getOverviewPage()).replaceAll("&nbsp;", " ");
 	}
 
 	public String getStudyPath() throws IOException {
 		String group = getStudyGroup();
 		String path = null;
 		if (group.contains("WINF")) {
 			path = "Wirtschaftsinformatik";
 		} else if (group.contains("WING")) {
 			path = "Wirtschaftsingenieurwesen";
 		} else {
 			path = "Betriebswirtschaft";
 		}
 		return path;
 	}
 
 	public String getStudyGroup() throws IOException {
 		return Utils.match("<a.*?>(BA.*?)</a>", dataService.getFileDepotPage());
 	}
 
 	public String getMatriculationNumber() throws IOException {
 		return Utils.match("<td>Studierendennummer: (.*?) </td>",
 				dataService.getGradesPage());
 	}
 
 }
