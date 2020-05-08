 package org.iplantc.de.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 import org.iplantc.de.shared.services.ConfluenceService;
 import org.swift.common.cli.CliClient.ClientException;
 import org.swift.common.cli.CliClient.ExitCode;
 import org.swift.common.soap.confluence.RemoteComment;
 import org.swift.common.soap.confluence.RemotePage;
 import org.swift.confluence.cli.ConfluenceClient;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 
 /**
  * A service for interfacing with Confluence via SOAP.
  * 
  * @author hariolf
  * 
  */
 public class ConfluenceServlet extends SessionManagementServlet implements ConfluenceService {
     private static final long serialVersionUID = -7866351808664668611L;
     private static final Logger LOG = Logger.getLogger(ConfluenceServlet.class);
     private static final String BLACK_STAR = "\u2605"; //$NON-NLS-1$
     private static final String WHITE_STAR = "\u2606"; //$NON-NLS-1$
     private static final String AUTH_TOKEN_ATTR = "confluenceToken"; //$NON-NLS-1$
 
     /** a wiki template file containing Confluence markup */
     private static final String TEMPLATE_FILE = "wiki_template"; //$NON-NLS-1$
 
     /** a string in the template that is replaced with the tool name */
     private static final String TOOL_NAME_PLACEHOLDER = "@TOOLNAME"; //$NON-NLS-1$
 
     /** a string in the template that is replaced with the tool description */
     private static final String DESCRIPTION_PLACEHOLDER = "@DESCRIPTION"; //$NON-NLS-1$
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String addPage(String toolName, String description) {
         String url = DiscoveryEnvironmentProperties.getConfluenceBaseUrl();
         String parent = DiscoveryEnvironmentProperties.getConfluenceParentPage();
         String user = DiscoveryEnvironmentProperties.getConfluenceUser();
         String password = DiscoveryEnvironmentProperties.getConfluencePassword();
         String space = DiscoveryEnvironmentProperties.getConfluenceSpaceName();
         String content;
         try {
             content = getTemplate();
             content = replaceTemplate(content, toolName, description);
         } catch (IOException e) {
             LOG.error("Can't read wiki template file.", e); //$NON-NLS-1$
             // if the template cannot be read, use the raw description text instead
             content = description;
         }
 
         // build command line for ConfluenceClient
         String[] args;
         try {
             String token = getConfluenceClient().getToken();
             args = new String[] {
                     "--server", url, "--user", user, "--password", password, "--login", token, "-a", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                     "addPage", "--space", space, "--title", toolName, "--content", content, "--parent", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
                     parent};
         } catch (Exception e) {
             throw new RuntimeException("Can't create Confluence page!", e); //$NON-NLS-1$
         }
         ExitCode code = new ConfluenceClient().doWork(args);
 
         if (code != ExitCode.SUCCESS) {
             throw new RuntimeException("Can't create Confluence page! Error code = " + code); //$NON-NLS-1$
         }
         return DiscoveryEnvironmentProperties.getConfluenceSpaceUrl() + toolName;
     }
 
     @Override
     public void updatePage(String toolName, int avgRating) {
         String space = DiscoveryEnvironmentProperties.getConfluenceSpaceName();
         // String content = client.getPageContent(toolName, space);
         try {
             IPlantConfluenceClient client = getConfluenceClient();
             long pageId = client.getContentId(toolName, space);
             RemotePage page = client.getPage(pageId);
             String content = page.getContent();
 
             int start = content.indexOf(BLACK_STAR);
             if (start < 0) {
                 start = content.indexOf(WHITE_STAR);
             }
             if (start < 0) {
                 return;
             }
 
             // remove all stars
             content = content.replaceAll(BLACK_STAR, ""); //$NON-NLS-1$
             content = content.replaceAll(WHITE_STAR, ""); //$NON-NLS-1$
 
             // add the new stars
             StringBuilder stars = new StringBuilder();
             for (int i = 0; i < avgRating; i++) {
                 stars.append(BLACK_STAR);
             }
             for (int i = 0; i < 5 - avgRating; i++) {
                 stars.append(WHITE_STAR);
             }
             content = content.substring(0, start) + stars + content.substring(start);
 
             // update page
             client.storePage(page, toolName, space, null, content, true, false);
         } catch (Exception e) {
             throw new RuntimeException("Can't update Confluence page!", e); //$NON-NLS-1$
         }
     }
 
     /**
      * Substitutes placeholders in a template with a tool name and a tool description.
      * 
      * @param template the contents of the template file in one string
      * @param toolName name of the DE tool
      * @param description description of the DE Tool
      * @return a string with the tool name and description filled in
      */
     private String replaceTemplate(String template, String toolName, String description) {
        return template.replaceAll(TOOL_NAME_PLACEHOLDER, toolName).replaceAll(DESCRIPTION_PLACEHOLDER,
                description);
     }
 
     /**
      * Reads the template file from a Resource in the same directory as this class. Although
      * ConfluenceClient can read a file via the --file option, this option is not used here because the
      * file is not guaranteed to be in the file system (it could be in a .jar).
      * 
      * @return the contents of TEMPLATE_FILE
      * @throws IOException
      */
     private String getTemplate() throws IOException {
         InputStream stream = ConfluenceServlet.class.getResourceAsStream(TEMPLATE_FILE);
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
             StringBuilder builder = new StringBuilder();
             char[] buffer = new char[1024];
             while (true) {
                 int charsRead = reader.read(buffer);
                 if (charsRead < 0) {
                     break;
                 }
                 builder.append(Arrays.copyOf(buffer, charsRead));
             }
             return builder.toString();
         } finally {
             if (stream != null) {
                 stream.close();
             }
         }
     }
 
     @Override
     public String addComment(String toolName, String comment) {
         String space = DiscoveryEnvironmentProperties.getConfluenceSpaceName();
         try {
             RemoteComment confluenceComment = getConfluenceClient().addComment(space, toolName, comment);
             return String.valueOf(confluenceComment.getId());
         } catch (Exception e) {
             throw new RuntimeException("Can't add comment to page '" + toolName + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
 
     @Override
     public void removeComment(String toolName, Long commentId) {
         try {
             getConfluenceClient().removeComment(commentId);
         } catch (Exception e) {
             throw new RuntimeException(
                     "Can't remove comment with id " + commentId + " from page '" + toolName + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         }
     }
 
     @Override
     public void editComment(String toolName, Long commentId, String newComment) {
         try {
             getConfluenceClient().editComment(commentId, newComment);
         } catch (Exception e) {
             throw new RuntimeException(
                     "Can't remove comment with id " + commentId + " from page '" + toolName + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         }
     }
 
     private IPlantConfluenceClient getConfluenceClient() throws SerializationException, ClientException {
         String authToken = getAttribute(AUTH_TOKEN_ATTR);
         return new IPlantConfluenceClient(authToken);
     }
 }
