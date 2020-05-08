 /*
  * JBoss, Home of Professional Open Source Copyright 2011 Red Hat Inc. and/or
  * its affiliates and other contributors as indicated by the @authors tag. All
  * rights reserved. See the copyright.txt in the distribution for a full listing
  * of individual contributors.
  * 
  * This copyrighted material is made available to anyone wishing to use, modify,
  * copy, or redistribute it subject to the terms and conditions of the GNU
  * Lesser General Public License, v. 2.1.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  */
 package org.jboss.ircbot.plugins.jira;
 
 import static org.jboss.ircbot.Character.GREATER_THAN;
 import static org.jboss.ircbot.Character.LESS_THAN;
 import static org.jboss.ircbot.Character.SLASH;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.StringTokenizer;
 
 import org.jboss.logging.Logger;
 
 /**
  * @author <a href="ropalka@redhat.com">Richard Opalka</a>
  */
 final class JIRAIssuePageScraper {
 
     private static final Logger LOGGER = Logger.getLogger( JIRAIssuePageScraper.class );
     private static final JIRAIssuePageScraper INSTANCE = new JIRAIssuePageScraper();
     private static final String ASSIGNEE_KEYWORD = "assignee-val";
     private static final String SUMMARY_KEYWORD = "summary-val";
     private static final String IMG_TAG = "<img";
     private static final String PRIORITY_KEYWORD = "priority-val";
     private static final String RESOLUTION_KEYWORD = "resolution-val";
     private static final String STATUS_KEYWORD = "status-val";
     private static final String TYPE_KEYWORD = "type-val";
 
     static {
         CookieHandler.setDefault( new CookieManager() );
     }
 
     private JIRAIssuePageScraper() {
         // forbidden inheritance
     }
 
     static JIRAIssuePageScraper getInstance() {
         return INSTANCE;
     }
 
     JIRAIssue scrape( final String jiraId, final String trackerURL ) {
         final JIRAIssue jiraIssue = new JIRAIssue( jiraId, trackerURL + SLASH + jiraId );
         try {
             final URL jiraIssuePage = new URL( jiraIssue.getPageURL() );
             final URLConnection conn = jiraIssuePage.openConnection();
             final BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
             String line;
             while ( ( line = br.readLine() ) != null ) {
                 scrapeAssignee( jiraIssue, line, br );
                 scrapeDescription( jiraIssue, line, br );
                 scrapePriority( jiraIssue, line, br );
                 scrapeResolution( jiraIssue, line, br );
                 scrapeStatus( jiraIssue, line, br );
                 scrapeType( jiraIssue, line, br );
             }
         } catch ( final Exception e ) {
             LOGGER.fatal( e.getMessage(), e );
         }
         return jiraIssue.getDescription() != null ? jiraIssue : null;
     }
 
     private StringTokenizer newTokenizer( final String line ) {
         return new StringTokenizer( line, LESS_THAN + GREATER_THAN );
     }
 
     private void scrapeDescription( final JIRAIssue issue, final String line, final BufferedReader br ) throws IOException {
         if ( line.indexOf( SUMMARY_KEYWORD ) != -1 ) {
             br.readLine();
             final StringTokenizer st = newTokenizer( br.readLine() );
             st.nextToken();
             st.nextToken();
             issue.setDescription( st.nextToken() );
         }
     }
 
     private void scrapeAssignee( final JIRAIssue issue, final String line, final BufferedReader br ) throws IOException {
         if ( line.indexOf( ASSIGNEE_KEYWORD ) != -1 ) {
             final StringTokenizer st = newTokenizer( line.trim() );
            if ( st.countTokens() > 4 ) {
                // assignee defined
                st.nextToken();
                st.nextToken();
                st.nextToken();
                issue.setAssignee( st.nextToken() );
            } else {
                issue.setAssignee( "Unassigned" );
            }
         }
     }
 
     private void scrapeStatus( final JIRAIssue issue, final String line, final BufferedReader br ) throws IOException {
         if ( line.indexOf( STATUS_KEYWORD ) != -1 ) {
             final StringTokenizer st = newTokenizer( br.readLine() );
             st.nextToken();
             st.nextToken();
             final String status = st.nextToken();
             issue.setStatus( status );
         }
     }
 
     private void scrapePriority( final JIRAIssue issue, final String line, final BufferedReader br ) throws IOException {
         if ( line.indexOf( PRIORITY_KEYWORD ) != -1 ) {
             final StringTokenizer st = newTokenizer( br.readLine() );
             st.nextToken();
             st.nextToken();
             final String priority = st.nextToken();
             issue.setPriority( priority );
         }
     }
 
     private void scrapeType( final JIRAIssue issue, final String line, final BufferedReader br ) throws IOException {
         if ( line.indexOf( TYPE_KEYWORD ) != -1 ) {
             final StringTokenizer st = newTokenizer( br.readLine() );
             st.nextToken();
             st.nextToken();
             final String type = st.nextToken();
             issue.setType( type );
         }
     }
 
     private void scrapeResolution( final JIRAIssue issue, final String line, final BufferedReader br ) throws IOException {
         if ( line.indexOf( RESOLUTION_KEYWORD ) != -1 ) {
             final String resolution = br.readLine();
             issue.setResolution( resolution );
         }
     }
 }
