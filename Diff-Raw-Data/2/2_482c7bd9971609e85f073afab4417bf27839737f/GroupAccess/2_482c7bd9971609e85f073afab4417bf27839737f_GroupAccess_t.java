 package com.myrontuttle.fin.trade.runner;
 
 import java.util.List;
 
 import org.osgi.framework.ServiceReference;
 
 import com.myrontuttle.fin.trade.adapt.Group;
 import com.myrontuttle.fin.trade.adapt.GroupDAO;
 
 public class GroupAccess {
 
     private GroupDAO groupDAO;
     
     public void setGroupDAO(GroupDAO sdao) {
     	System.out.println("Setting GroupDAO for GroupAccess");
     	this.groupDAO = sdao;
     }
     
     public GroupDAO getGroupDAO() {
     	return groupDAO;
     }
 
     public void bind(ServiceReference<?> reference) {
         System.out.println("GroupAccess, service bound: " + reference);
         
         if (groupDAO == null) {
 			System.out.println("No GroupDAO");
 			return;
 		}
 		
 		try {
 			System.out.println("Finding Groups");
 			List<Group> existingGroups = groupDAO.findGroups();
 			if (existingGroups != null && existingGroups.size() > 0) {
     			System.out.println("Groups (id - start date): ");
 				for (Group group : existingGroups) {
 					System.out.println(group.getGroupId() + ": " + group.getStartTime().toString());
 				}
 			} else {
 				System.out.println("No existing groups. Creating new group");
 				Group group = new Group();
 				System.out.println("New group created");
 				group.setAlertUser("wsodinvestor@gmail.com");
 				group.setAlertHost("imap.google.com");
 				group.setAlertReceiver("EmailAlert");
 				group.setAlertPassword("");
 				group.setActive(true);
 				group.setAlertsPerSymbol(2);
 				group.setEliteCount(3);
 				group.setSize(15);
 				group.setEvaluationStrategy(Group.RANDOM_EVALUATOR);
				group.setExpressionStrategy(Group.SAT_EXPRESSION);
 				group.setFrequency(Group.DAILY);
 				group.setNumberOfScreens(1);
 				group.setMaxSymbolsPerScreen(5);
 				group.setMutationFactor(.05);
 				groupDAO.saveGroup(group);
 				System.out.println("Group " + group.getGroupId() + ", created on: " + 
 									group.getStartTime().toString());
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			return;
 		}
 		
     }
 
     public void unbind(ServiceReference<?> reference) {
         System.out.println("GroupAccess, service unbound: " + reference);
 
         try {
     		List<Group> existingGroups = groupDAO.findGroups();
     		if (existingGroups != null && existingGroups.size() > 0) {
     			System.out.println("Groups (id - start date): ");
     			for (Group group : existingGroups) {
     				System.out.println(group.getGroupId() + " - " + group.getStartTime().toString());
     			}
     		} else {
     			System.out.println("No existing groups.");
     		}
         } catch (Exception e) {
         	System.out.println(e.getMessage());
         }
     }
 
 }
