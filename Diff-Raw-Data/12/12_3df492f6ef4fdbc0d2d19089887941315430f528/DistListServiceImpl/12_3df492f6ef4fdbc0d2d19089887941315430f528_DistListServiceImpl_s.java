 /**
  * 
  */
 package org.mdkt.maildist.server.rpc;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 import org.mdkt.library.security.users.GaeUser;
 import org.mdkt.maildist.client.dto.Alias;
 import org.mdkt.maildist.client.dto.DistList;
 import org.mdkt.maildist.client.dto.DistListMember;
 import org.mdkt.maildist.client.rpc.DistListService;
 import org.mdkt.maildist.server.DistListRegistry;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * @author trung
  *
  */
 public class DistListServiceImpl implements DistListService {
 	
 	private final Logger logger = Logger.getLogger(DistListServiceImpl.class);
 	
 	private DistListRegistry distListRegistry;
 	
 	public void setDistListRegistry(DistListRegistry distListRegistry) {
 		this.distListRegistry = distListRegistry;
 	}
 	
 	@Override
 	public void deleteDistLists(ArrayList<String> distListIds) {
 		distListRegistry.deleteDistLists(distListIds);
 	}
 
 	@Override
 	public void createDistList(DistList distList) {
 		distList.setUserId(getCurrentUser().getEmail());
 		distListRegistry.saveDistList(distList);
 	}
 	
 	@Override
 	public void deleteDistListMembers(ArrayList<String> distListMemberIds) {
 		distListRegistry.deleteDistListMembers(distListMemberIds);
 	}
 	
 	@Override
 	public ArrayList<DistListMember> getDistListMembers(String distListId) {
 		return distListRegistry.findDistListMembers(distListId);
 	}
 	
 	@Override
 	public ArrayList<DistList> getDistLists() {
 		return distListRegistry.findAllDistLists(getCurrentUser().getEmail());
 	}
 	
 	@Override
 	public void updateDistListMember(ArrayList<DistListMember> distListMembers) {
 		distListRegistry.updateDistListMembers(distListMembers);
 	}
 	
 	private GaeUser getCurrentUser() {
 		return (GaeUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 	}
 	
 	@Override
 	public boolean findDistList(String distListName) {
 		return distListRegistry.findDistList(distListName) != null;
 	}
 	
 	@Override
 	public void deleteAliasEmails(ArrayList<String> aliasIds) {
 		distListRegistry.deleteAliases(aliasIds);
 	}
 	
 	@Override
 	public ArrayList<Alias> getAliasEmails() {
 		return distListRegistry.findAllAliases(getCurrentUser().getEmail());
 	}
 	
 	@Override
 	public void addEmailAlias(String email) {
 		Alias alias = new Alias();
 		String userEmail = getCurrentUser().getEmail();
		alias.setAliasId(userEmail + email);
 		alias.setEmail(email);
 		alias.setUserEmail(userEmail);
 		distListRegistry.saveEmailAlias(alias);
 	}
 	
 	@Override
 	public boolean findAlias(String aliasName) {
		String aliasId = getCurrentUser().getEmail() + aliasName;
 		return distListRegistry.aliasExists(aliasId) != null;
 	}
 }
