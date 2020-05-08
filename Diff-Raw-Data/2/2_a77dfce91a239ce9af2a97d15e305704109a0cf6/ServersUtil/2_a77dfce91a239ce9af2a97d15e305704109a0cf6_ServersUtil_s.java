 package org.zeroturnaround.jenkins.util;
 
 import com.google.common.collect.Lists;
 import com.zeroturnaround.liverebel.api.CommandCenter;
 import com.zeroturnaround.liverebel.api.ServerInfo;
 import com.zeroturnaround.liverebel.api.impl.ServerGroup;
 import org.zeroturnaround.jenkins.LiveRebelDeployBuilder;
 import org.zeroturnaround.jenkins.ServerCheckbox;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 public class ServersUtil {
 
   private static final Logger LOGGER = Logger.getLogger(ServersUtil.class.getName());
   private List<ServerCheckbox> servers;
 
   public ServersUtil(List<ServerCheckbox> servers) {
     this.servers = servers;
   }
 
   public List<ServerCheckbox> getServers() {
     if (servers == null) {
       return getDefaultServers();
     }
     else {
       List<ServerCheckbox> newServers = getDefaultServers();
       Map<String, ServerCheckbox> oldServersMap = new HashMap<String, ServerCheckbox>();
 
       for (ServerCheckbox oldServer : servers) {
         oldServersMap.put(oldServer.getServer(), oldServer);
       }
 
       servers.clear();
       for(ServerCheckbox newServer : newServers) {
         if (oldServersMap.containsKey(newServer.getServer()))
           servers.add(new ServerCheckbox(newServer.getServer(), newServer.getTitle(),newServer.getParentNames(), newServer.getIndentDepth(), oldServersMap.get(newServer.getServer()).isSelected(), newServer.isOnline(), newServer.isGroup()));
         else
           servers.add(newServer);
       }
       return servers;
     }
   }
 
   public List<ServerCheckbox> getDefaultServers() {
     CommandCenter commandCenter = LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter();
     List<ServerCheckbox> serversLoc = new ArrayList<ServerCheckbox>();
     if (commandCenter != null) {
       String currentVersion = commandCenter.getVersion();
       if (isServerGroupsSupported(currentVersion)) {
         serversLoc = showServerGroups(commandCenter);
       }
       else {
         serversLoc = showServers(commandCenter);
       }
     }
     else {
       LOGGER.warning("Couldn't connect to the command center!");
     }
     return serversLoc;
   }
 
  public boolean isServerGroupsSupported(String currentVersion) {return !currentVersion.equals("2.0");}
 
   public List<ServerCheckbox> showServerGroups(CommandCenter commandCenter) {
     List<ServerGroup> topLevelServerGroups = commandCenter.getGroups("");
     List<ServerCheckbox> allCheckBoxes = new ArrayList<ServerCheckbox>();
     for (ServerGroup serverGroup : topLevelServerGroups) {
       if (hasServers(serverGroup).contains(true)) //do not add empty groups
         allCheckBoxes.addAll(processSiblings(serverGroup, "", 0));
     }
 
     return allCheckBoxes;
   }
 
   public List<Boolean> hasServers(ServerGroup serverGroup) {
     if (!serverGroup.getServers().isEmpty()) {
       return Lists.newArrayList(true);
     }
     ArrayList<Boolean> hasServers = new ArrayList<Boolean>();
     for (ServerGroup child : serverGroup.getChildren()) {
       hasServers.addAll(hasServers(child));
     }
     return hasServers;
   }
 
   public List<ServerCheckbox> processSiblings(ServerGroup serverGroup, String parentNames, int indentDepth) {
     ServerCheckbox serverCheckbox = new ServerCheckbox(serverGroup.getName(), serverGroup.getName(), parentNames, indentDepth, false, false, true);
     ArrayList<ServerCheckbox> serverCheckboxes = new ArrayList<ServerCheckbox>();
     serverCheckboxes.add(serverCheckbox);
     if (serverGroup.getChildren().size() != 0) {
       for (ServerGroup child : serverGroup.getChildren()) {
         if (hasServers(child).contains(true)) //do not add empty groups
           serverCheckboxes.addAll(processSiblings(child, "lr-" + serverGroup.getName().replaceAll("[^A-Za-z0-9]", "_"), indentDepth + 1));
       }
     }
 
     if (serverGroup.getServers().size() != 0) {
       for (ServerInfo server : serverGroup.getServers()) {
         serverCheckboxes.add(new ServerCheckbox(server.getId(), server.getName(), "lr-" + serverGroup.getName().replaceAll("[^A-Za-z0-9]", "_"), indentDepth + 1, false, server.isConnected(), false));
       }
     }
 
     return serverCheckboxes;
   }
 
   public List<ServerCheckbox> showServers(CommandCenter commandCenter) {
     List<ServerCheckbox> servers = new ArrayList<ServerCheckbox>();
     for (ServerInfo server : commandCenter.getServers().values()) {
       servers.add(new ServerCheckbox(server.getId(), server.getName(), "", 0, false, server.isConnected(), false));
     }
     return servers;
   }
 }
