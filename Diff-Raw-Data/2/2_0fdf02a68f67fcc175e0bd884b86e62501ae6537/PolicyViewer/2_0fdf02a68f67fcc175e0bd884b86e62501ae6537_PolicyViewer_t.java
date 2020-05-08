 package org.wyona.security.util;
 
 import org.wyona.security.core.AuthorizationException;
 import org.wyona.security.core.GroupPolicy;
 import org.wyona.security.core.UsecasePolicy;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.Policy;
 import org.wyona.security.core.api.PolicyManager;
 
 import org.apache.log4j.Logger;
 
 import java.util.Vector;
 
 /**
  * Utility class to view policies
  */
 public class PolicyViewer {
 
     private static Logger log = Logger.getLogger(PolicyViewer.class);
 
     public static int ORDERED_BY_USECASES = 0;
     public static int ORDERED_BY_IDENTITIES = 1;
 
     /**
      * Get XHTML view of policies
      * @param pm Policy Manager
      * @param path Content path which is associated with an access policy
      * @param contentItemId Content Item ID which allows a unique association with an access policy and an item within the content
      * @param orderedBy Allows ordering by usecases or identities
      * @param showParents Show the policies of the parent nodes, which allows to figure out how the policy has been aggregated
      */
     static public String getXHTMLView (PolicyManager pm, String path, String contentItemId, int orderedBy, boolean showParents) {
         try {
             StringBuffer sb = new StringBuffer("<html><body>");
 	    if(showParents) {
                 // Show also all parent policies
                 sb.append("<p><a href=\"?yanel.policy=read&amp;orderedBy=" + orderedBy + "&amp;showParents=false\">Tab: Node Policy</a> | Tab: Parent Policies</p>");
 
                 sb.append("<p>Access Policies for Path (and its parents) <i>" + path);
                 if (contentItemId != null) sb.append("#" + contentItemId);
                 sb.append("</i>:</p>");
                 sb.append("<p>(Policy Repository: "+pm.getPoliciesRepository().getName()+", "+pm.getPoliciesRepository().getConfigFile()+")</p>");
 
                 sb.append(getOrderByLink(orderedBy, showParents));
                 sb.append("<p><table border=\"1\">");
                 sb.append("<tr><td>Path</td>" + getSplittedPath(pm, path, contentItemId) + "</tr>");
                 sb.append("<tr valign=\"top\"><td>Policy</td>" + getPolicies(pm, path, contentItemId, false, orderedBy) + "</tr>");
                 sb.append("<tr valign=\"top\"><td>Aggregated Policy</td>" + getPolicies(pm, path, contentItemId, true, orderedBy) + "</tr>");
                 sb.append("</table></p>");
             } else {
                 // Show policy of this node only
                 sb.append("<p>Tab: Node Policy | <a href=\"?yanel.policy=read&amp;orderedBy=" + orderedBy + "&amp;showParents=true\">Tab: Parent Policies</a></p>");
 
                 sb.append("<p>Aggregated Access Policy for Path <i>" + path);
                 if (contentItemId != null) sb.append("#" + contentItemId);
                 sb.append("</i>:</p>");
 
                 sb.append(getOrderByLink(orderedBy, showParents));
                 boolean aggregate = true;
                 Policy p = pm.getPolicy(path, aggregate);
                 sb.append("<p><table border=\"1\"><tr>");
 		sb.append(getPolicy(p, aggregate, orderedBy, null));
                 if (contentItemId != null) {
                     sb.append("<td>contentItemId (" + contentItemId + ") not implemented yet into API!</td>");
                 }
                 sb.append("</tr></table></p>");
             }
             sb.append("</body></html>");
             return sb.toString();
         } catch(Exception e) {
             log.error(e, e);
             return "<html><body>Exception: " + e.getMessage() + "</body></html>";
         }
     }
 
     /**
      * Get splitted path
      */
     static public StringBuffer getSplittedPath (PolicyManager pm, String path, String contentItemId) {
         String[] names = path.split("/");
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < names.length -1; i++) {
             sb.append("<td>" + names[i] + "/</td>");
         }
 	if (log.isDebugEnabled()) log.debug("Length: " + names.length);
         if (path.endsWith("/")) {
             if (names.length > 0) {
                 sb.append("<td>" + names[names.length -1] + "/</td>");
             } else {
                 sb.append("<td>/</td>");
             }
         } else {
             sb.append("<td>" + names[names.length -1] + "</td>");
         }
         if (contentItemId != null) {
             sb.append("<td>#" + contentItemId + "</td>");
         }
         return sb;
     }
 
     /**
      * Get policies
 
      * @param pm Policy manager
      * @param path Path of node
      * @param contentItemId Content item ID
      * @param aggregate If aggregate true, then the policy will be aggregated/merged with existing parent policies, otherwise only the node specific policy will be returned
      * @param orderedBy Ordered by identities or usecases
      */
     static public StringBuffer getPolicies(PolicyManager pm, String path, String contentItemId, boolean aggregate, int orderedBy) throws AuthorizationException {
 
         String[] names = path.split("/");
         StringBuffer sb = new StringBuffer();
         StringBuffer currentPath = new StringBuffer();
         // Show policies of the parents of the node
         for (int i = 0; i < names.length - 1; i++) {
             currentPath.append(names[i] + "/");
             Policy p = pm.getPolicy(currentPath.toString(), aggregate);
 
 
             String back = "";
             if (path.endsWith("/")) {
                 for (int k = i; k < names.length - 1; k++) {
                     back = back + "../";
                 } 
             } else {
                 if (i == names.length -2) {
                     back ="./";
                 } else {
                     for (int k = i; k < names.length - 2; k++) {
                         back = back + "../";
                     } 
                 }
             }
            //log.debug("Back path: " + i + ", " + names[i] + ", " + back);
 
 
             sb.append(getPolicy(p, aggregate, orderedBy, back));
         }
 
         // Show policy of the actual node
         Policy p = pm.getPolicy(path, aggregate);
         sb.append(getPolicy(p, aggregate, orderedBy, null));
 
         // Show policy according to content id
         if (contentItemId != null) {
             sb.append("<td>Not implemented yet into API!</td>");
         }
 
         return sb;
     }
 
     /**
      * Get policy as XHTML list ordered by usecases
      */
     static public StringBuffer getPolicyAsXHTMLListOrderedByUsecases(Policy p) {
         StringBuffer sb = new StringBuffer();
         UsecasePolicy[] up = p.getUsecasePolicies();
         if (up != null && up.length > 0) {
             sb.append("<ul>");
             for (int i = 0; i < up.length; i++) {
                 sb.append("<li>Usecase: " + up[i].getName());
                 sb.append("<ol>");
                 Identity[] ids = up[i].getIdentities();
                 for (int j = 0; j < ids.length; j++) {
                     if (ids[j].isWorld()) {
                         sb.append("<li>WORLD</li>");
                     } else {
                         sb.append("<li>User: " + ids[j].getUsername() + "</li>");
                     }
                 }
                 GroupPolicy[] gps = up[i].getGroupPolicies();
                 for (int j = 0; j < gps.length; j++) {
                     sb.append("<li>Group: " + gps[j].getId() + "</li>");
                 }
                 sb.append("</ol>");
                 sb.append("</li>");
             }
             sb.append("</ul>");
         } else {
             sb.append("No policy usecases!");
         }
         return sb;
     }
 
     /**
      * Get policy as XHTML list ordered by identities
      */
     static public StringBuffer getPolicyAsXHTMLListOrderedByIdentities(Policy p) {
         Vector worldRights = new Vector();
         java.util.HashMap users = new java.util.HashMap();
         java.util.HashMap groups = new java.util.HashMap();
 
         UsecasePolicy[] up = p.getUsecasePolicies();
         if (up != null && up.length > 0) {
             for (int i = 0; i < up.length; i++) {
                 Identity[] ids = up[i].getIdentities();
                 for (int j = 0; j < ids.length; j++) {
                     if (ids[j].isWorld()) {
                         worldRights.add(up[i].getName());
                     } else {
                         Vector userRights;
                         if ((userRights = (Vector) users.get(ids[j].getUsername())) != null) {
                             log.debug("User has already been added: " + ids[j].getUsername());
                         } else {
                             userRights = new Vector();
                             users.put(ids[j].getUsername(), userRights);
                         }
                         userRights.add(up[i].getName());
                     }
                 }
 
                 GroupPolicy[] gps = up[i].getGroupPolicies();
                 for (int j = 0; j < gps.length; j++) {
                     Vector groupRights;
                     if ((groupRights = (Vector) groups.get(gps[j].getId())) != null) {
                         log.debug("Group has already been added: " + gps[j].getId());
                     } else {
                         groupRights = new Vector();
                         groups.put(gps[j].getId(), groupRights);
                     }
                     groupRights.add(up[i].getName());
                 }
             }
         } else {
             log.warn("No policy usecases!");
         }
 
         StringBuffer sb = new StringBuffer();
         sb.append("<ul>");
         if (worldRights.size() > 0) {
             sb.append("<li>WORLD (" + getCommaSeparatedList(worldRights) + ")</li>");
         }
 
         // Users
         java.util.Iterator userIterator = users.keySet().iterator();
         while (userIterator.hasNext()) {
             String userName = (String) userIterator.next();
             sb.append("<li>User: " + userName + " (" + getCommaSeparatedList((Vector) users.get(userName)) + ")</li>");
         }
 
         //Groups 
         java.util.Iterator groupIterator = groups.keySet().iterator();
         while (groupIterator.hasNext()) {
             String groupName = (String) groupIterator.next();
             sb.append("<li>Group: " + groupName + " (" + getCommaSeparatedList((Vector) groups.get(groupName)) + ")</li>");
         }
 
 // TODO: Also add hosts
 /*
         for (int i = 0; i < hosts.length; i++) {
             sb.append("<li>Host: 192.168.1.34 (view, open, write)</li>");
         }
 */
 
         sb.append("</ul>");
         return sb;
     }
 
     /**
      * @param rights
      */
     private static String getCommaSeparatedList(Vector rights) {
         StringBuffer sb = new StringBuffer();
         if (rights.size() > 0) {
             sb.append((String) rights.elementAt(0));
             for (int i = 1; i < rights.size(); i++) {
                 sb.append(", " + (String) rights.elementAt(i));
             }
         } else {
             if(log.isDebugEnabled()) log.debug("No rights asigned!");
             return null;
         }
         return sb.toString();
     }
 
     /**
      *
      */
     private static String getOrderByLink(int orderedBy, boolean showParents) {
         if (orderedBy == ORDERED_BY_USECASES) {
             return "<p>Order by <a href=\"?yanel.policy=read&amp;orderedBy=" + ORDERED_BY_IDENTITIES + "&amp;showParents=" + showParents + "\">Identities</a></p>";
         } else if (orderedBy == ORDERED_BY_IDENTITIES) {
             return "<p>Order by <a href=\"?yanel.policy=read&amp;orderedBy=" + ORDERED_BY_USECASES + "&amp;showParents=" + showParents + "\">Usecases</a></p>";
         } else {
             log.error("No such order by value implemented: " + orderedBy);
             return "";
         }
     }
 
     /**
      * Get policy
      * @param aggregate If aggregate true, then the policy will be aggregated/merged with existing parent policies, otherwise only the node specific policy will be returned
      * @param back ../../../
      */
     static public StringBuffer getPolicy(Policy policy, boolean aggregate, int orderedBy, String back) throws AuthorizationException {
         StringBuffer sb = new StringBuffer("<td>");
         if (policy != null) {
             String showUseInheritedPolicies = "";
             String editPolicy = "";
             if (!aggregate) {
                 showUseInheritedPolicies = "<p>Use inherited policies: " + policy.useInheritedPolicies() + "</p>";
                 if (back != null) {
                     editPolicy = "<p><a href=\"" + back + "?yanel.policy=update\">Edit policy.</a></p>";
                 } else {
                     editPolicy = "<p><a href=\"?yanel.policy=update\">Edit policy.</a></p>";
                 }
             }
 
             if (orderedBy == ORDERED_BY_USECASES) {
                 sb.append(editPolicy + showUseInheritedPolicies + getPolicyAsXHTMLListOrderedByUsecases(policy));
             } else if (orderedBy == ORDERED_BY_IDENTITIES) {
                 sb.append(editPolicy + showUseInheritedPolicies + getPolicyAsXHTMLListOrderedByIdentities(policy));
             } else {
                 sb.append("No such orderedBy implemented: " + orderedBy);
             }
         } else {
             sb.append("<p>No policy yet!</p>");
             if (back != null) {
 	        sb.append("<p><a href=\"" + back + "?yanel.policy=update\">Create new policy.</a></p>");
             } else {
 	        sb.append("<p><a href=\"?yanel.policy=update\">Create new policy.</a></p>");
             }
         }
         sb.append("</td>");
         return sb;
     }
 }
