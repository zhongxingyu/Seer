 package edu.cs319.client;
 
 import java.util.Collection;
 import java.util.List;
 
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.server.CoLabPrivilegeLevel;
 
 public interface IClient {
 
 	public boolean coLabRoomMemberArrived(String username);
 
 	public boolean coLabRoomMemberLeft(String username);
 
 	public boolean updateAllSubsections(String documentId, List<DocumentSubSection> allSections);
 
 	public boolean updateSubsection(String usernameSender, String documentname,
 			DocumentSubSection section, String sectionID);
 
 	public boolean subsectionLocked(String usernameSender, String documentName, String sectionID);
 
 	public boolean subsectionUnLocked(String usernameSender, String documentName, String sectionID);
 
	public boolean newSubSection(String username, String String documentName, String sectionId,
 			DocumentSubSection section, int idx);
 
 	public boolean newDocument(String username, String documentName);
 
 	public boolean removeDocument(String username, String documentName);
 
 	public boolean subSectionRemoved(String username, String documentName, String sectionId);
 
 	public boolean newChatMessage(String usernameSender, String message);
 
 	public boolean newChatMessage(String usernameSender, String message, String recipiant);
 
 	public boolean changeUserPrivilege(String username, CoLabPrivilegeLevel newPriv);
 
 	public boolean allUsersInRoom(List<String> usernames, List<CoLabPrivilegeLevel> privs);
 
 	public boolean allCoLabRooms(Collection<String> roomNames);
 
 	public String getUserName();
 }
