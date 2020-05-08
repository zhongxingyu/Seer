 /**
  * File description: Generates various output based on issues information in the GitHub.
  * At the moment it has options to get the output in a form of Cucumber features
  * or GitHub Wiki page with traceability matrix. 
  */
 package sirius.utils.retriever;
 
 import java.io.IOException;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
 
 /**
  * Generates various output based on issues information in the GitHub.
  * At the moment it has options to get the output in a form of Cucumber features
  * or GitHub Wiki page with traceability matrix.
  * @author Myk Kolisnyk
  * @version
  */
 @Mojo(name = "generate",defaultPhase=LifecyclePhase.GENERATE_SOURCES)
 public class IssueGetPlugin extends AbstractMojo {
 
     /**
      * The user name to connect to GitHub repository with
      */
     @Parameter(property = "issueget.user", defaultValue = "")
     private String   userName       = "";
 
     /**
      * The password to connect to GitHub repository
      */
     @Parameter(property = "issueget.password", defaultValue = "")
     private String   password       = "";
 
     /**
      * The GitHub repository name to retrieve issues for
      */
     @Parameter(property = "issueget.repository", defaultValue = "Sirius")
     private String   repository     = "";
 
     /**
      * Identifies the output type to be produced. At the moment the following values are supported:
      * <ul>
      * <li> <b>trace</b> - the output is the traceability matrix in a GitHub Wiki markdown format
      * <li> <b>mvn-trace</b> - the output is the traceability matrix in HTML format 
      * <li> <b>cucumber</b> - the output is the feature files containing the issues description
      * </ul>
      */
     @Parameter(property = "issueget.type", defaultValue = "trace")
     private String   outputType     = "";
 
     /**
      * The semi-colon separated list of labels which can be used as filter while quering
      * issues. If multiple values are specified the issues returned should have at least one 
      * of the specified labels.
      */
     @Parameter(property = "issueget.groups", defaultValue = "Test")
     private String groups;
 
     /**
      * The folder where output should be produced to
      */
     @Parameter(property = "issueget.output", defaultValue = ".")
     private String   outputLocation = "";
 
     /**
      * @param userName the userName to set
      */
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     /**
      * @param password the password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
 
 
     /**
      * @param repository the repository to set
      */
     public void setRepository(String repository) {
         this.repository = repository;
     }
 
 
 
     /**
      * @param outputType the outputType to set
      */
     public void setOutputType(String outputType) {
         this.outputType = outputType;
     }
 
 
 
     /**
      * @param groups the groups to set
      */
     public void setGroups(String groups) {
         this.groups = groups;
     }
 
 
 
     /**
      * @param outputLocation the outputLocation to set
      */
     public void setOutputLocation(String outputLocation) {
         this.outputLocation = outputLocation;
     }
 
 
 
     /**
      * Main runner method
      */
     public void execute() throws MojoExecutionException {
         String[] args = {
                 Program.REPO_NAME,
                 this.repository,
                 Program.USER_NAME,
                 this.userName,
                 Program.USER_PASS,
                 this.password,
                 Program.GROUPS,
                 this.groups,
                 Program.OUTPUT_TYPE,
                 this.outputType,
                 Program.OUTPUT_LOCATION,
                 this.outputLocation
         };
         try {
             Program.main(args);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
