 package org.connid.openam.methods;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.connid.openam.OpenAMConfiguration;
 import org.connid.openam.OpenAMConnection;
 import org.connid.openam.utilities.AdminToken;
 import org.connid.openam.utilities.Constants;
 import org.identityconnectors.common.logging.Log;
 import org.identityconnectors.framework.common.exceptions.ConnectorException;
 import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
 import org.identityconnectors.framework.common.objects.ObjectClass;
 import org.identityconnectors.framework.common.objects.ResultsHandler;
 import org.identityconnectors.framework.common.objects.Uid;
 
 public class OpenAMExecuteQuery extends CommonMethods {
 
     private static final Log LOG = Log.getLog(OpenAMSearch.class);
     private OpenAMConfiguration openAMConfiguration = null;
     private OpenAMConnection connection = null;
     private String ldapFilter = null;
     private ResultsHandler handler = null;
     private String uid = "";
     private String token = "";
 
     public OpenAMExecuteQuery(final OpenAMConfiguration configuration,
             final String ldapFilter, final ResultsHandler rh) {
         openAMConfiguration = configuration;
         connection = OpenAMConnection.openConnection(configuration);
         this.ldapFilter = ldapFilter;
         handler = rh;
         token = AdminToken.getAdminToken(configuration).token;
     }
 
     public final void executeQuery() {
         try {
             doExecuteQuery();
         } catch (Exception e) {
             LOG.error(e, "error during execute query operation");
             throw new ConnectorException(e);
         }
     }
 
     private void doExecuteQuery() throws IOException {
         String[] uidResults = null;
 
         if (isAlive(connection)) {
             uidResults =
                     connection.search(searchParameters(
                     cleanLdapFilter())).split("string=");
             LOG.ok("Search committed");
         }
 
         if (uidResults == null || uidResults.length == 1) {
             LOG.error("User " + ldapFilter + " not exists");
            return;
         }
 
         List<String[]> usersList = new ArrayList<String[]>();
 
         for (int i = 1; i < uidResults.length; i++) {
             if (!uidResults[i].startsWith("anonymous")
                     && !uidResults[i].startsWith("amAdmin")) {
                 usersList.add(connection.read(readParameters(
                         uidResults[i])).split("identitydetails."));
             }
         }
 
         List<String> attributesList = new ArrayList<String>();
         ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
         String name = "";
         for (Iterator<String[]> it = usersList.iterator(); it.hasNext();) {
             String[] userDetails = it.next();
             for (int i = 0; i < userDetails.length; i++) {
                 if (userDetails[i].contains("name")) {
                     attributesList.clear();
                     String[] names = userDetails[i].split("=");
                     name = names[1];
                     for (int j = i + 1; j < userDetails.length; j++) {
                         if (userDetails[j].contains("name")) {
                             break;
                         }
                         if (userDetails[j].contains("value")) {
                             String[] value = userDetails[j].split("=");
                             attributesList.add(value[1].trim());
                         }
                     }
                 }
                 if (name != null && name.contains("uid")) {
                     bld.setUid(attributesList.get(0));
                     bld.setName(attributesList.get(0));
                 }
 
                 if (name != null && name.contains("objectclass")) {
                     for (int j = 0; j < attributesList.size(); j++) {
                         bld.setObjectClass(
                                 new ObjectClass(attributesList.get(j)));
                     }
                 }
 
                 if (name != null && !name.isEmpty()) {
                     bld.addAttribute(name, attributesList);
                 }
             }
             handler.handle(bld.build());
         }
     }
 
     private boolean cleanLdapFilter() {
         boolean searchForUid = false;
         if (ldapFilter.contains(Uid.NAME)) {
             ldapFilter = ldapFilter.split("=")[1];
             ldapFilter = ldapFilter.substring(0, ldapFilter.length() - 1);
             searchForUid = true;
         } else if (ldapFilter.contains("&")) {
             ldapFilter = ldapFilter.replace("&", "%26");
         } else if (ldapFilter.contains("|")) {
             ldapFilter = ldapFilter.replace("|", "%7C");
         }
         return searchForUid;
     }
 
     private String searchParameters(final boolean searchForUid)
             throws UnsupportedEncodingException {
         StringBuilder parameters = new StringBuilder();
         if (searchForUid) {
             parameters.append("&filter=").append(ldapFilter);
         } else {
             parameters.append("&filter=*");
             if (ldapFilter != null && !ldapFilter.isEmpty()) {
                 parameters.append(")").append(ldapFilter);
             }
         }
         parameters.append("&attributes_names=realm&attributes_values_realm=")
                 .append(openAMConfiguration.getOpenamRealm()).append("&admin=")
                 .append(URLEncoder.encode(
                 token, Constants.ENCODING));
         return parameters.toString();
     }
 
     private String readParameters(final String name)
             throws UnsupportedEncodingException {
         StringBuilder readParameters = new StringBuilder();
         readParameters.append("&name=").append(name)
                 .append("&attributes_names=realm&attributes_values_realm=")
                 .append(openAMConfiguration.getOpenamRealm()).append("&admin=")
                 .append(URLEncoder.encode(
                 token, Constants.ENCODING));
         return readParameters.toString();
     }
 }
