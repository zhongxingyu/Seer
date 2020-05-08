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
                 boolean aggregated = true;
                 Policy p = pm.getPolicy(path, aggregated);
                 sb.append("<p><table border=\"1\"><tr>");
 		sb.append(getPolicy(p, aggregated, orderedBy));
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
      * @param aggregated If aggregated true, then the policy will be aggregated/merged with existing parent policies, otherwise only the node specific policy will be returned
      * @param path Path of node
      */
     static public StringBuffer getPolicies (PolicyManager pm, String path, String contentItemId, boolean aggregated, int orderedBy) throws AuthorizationException {
 
         String[] names = path.split("/");
         StringBuffer sb = new StringBuffer();
         StringBuffer currentPath = new StringBuffer();
         // Show policy of this node
         for (int i = 0; i < names.length -1; i++) {
             currentPath.append(names[i] + "/");
             Policy p = pm.getPolicy(currentPath.toString(), aggregated);
             sb.append(getPolicy(p, aggregated, orderedBy));
         }
 
         // Show policy of this node
         Policy p = pm.getPolicy(path, aggregated);
         sb.append(getPolicy(p, aggregated, orderedBy));
 
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
         Vector world = new Vector();
         java.util.HashMap users = new java.util.HashMap();
         java.util.HashMap groups = new java.util.HashMap();
 
         UsecasePolicy[] up = p.getUsecasePolicies();
         if (up != null && up.length > 0) {
             for (int i = 0; i < up.length; i++) {
                 Identity[] ids = up[i].getIdentities();
                 for (int j = 0; j < ids.length; j++) {
                     if (ids[j].isWorld()) {
                         world.add(up[i].getName());
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
         sb.append("<li>WORLD (" + getCommaSeparatedList(world) + ")</li>");
 
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
      *
      */
     private static String getCommaSeparatedList(Vector v) {
         StringBuffer sb = new StringBuffer();
         if (v.size() > 0) {
             sb.append((String) v.elementAt(0));
             for (int i = 1; i < v.size(); i++) {
                 sb.append(", " + (String) v.elementAt(i));
             }
         } else {
             sb.append("No rights asigned!");
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
      * @param aggregated If aggregated true, then the policy will be aggregated/merged with existing parent policies, otherwise only the node specific policy will be returned
      * @param path Path of node
      */
     static public StringBuffer getPolicy (Policy policy, boolean aggregated, int orderedBy) throws AuthorizationException {
         StringBuffer sb = new StringBuffer("<td>");
         if (policy != null) {
             String showUseInheritedPolicies = "";
             if (!aggregated) {
                 showUseInheritedPolicies = "<p>Use inherited policies: " + policy.useInheritedPolicies() + "</p>";
             }
             if (orderedBy == ORDERED_BY_USECASES) {
                 sb.append(showUseInheritedPolicies + getPolicyAsXHTMLListOrderedByUsecases(policy));
             } else if (orderedBy == ORDERED_BY_IDENTITIES) {
                 sb.append(showUseInheritedPolicies + getPolicyAsXHTMLListOrderedByIdentities(policy));
             } else {
                 sb.append("No such orderedBy implemented: " + orderedBy);
             }
         } else {
             sb.append("No policy yet!");
         }
         sb.append("</td>");
         return sb;
     }
 }
