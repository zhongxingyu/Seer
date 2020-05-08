 package nz.org.nesi.appmanage;
 
 import com.beust.jcommander.Parameter;
 import nz.org.nesi.appmanage.model.Documentation;
 
 import java.util.List;
 
 abstract class CreateDocumentationCliParameters extends AppManageModule {
 
 	@Parameter(names = { "-o", "--output-dir" }, description = "the path to where the documentation is supposed to end up", required = false)
	private String outputFolder = null;
 
     @Parameter(names = {"--applications"}, description = "only create documentation for the specified application(s) (comma-separated list)")
     private List<String> applications;
 
    @Parameter(names = {"--template"}, description = "the velocity template to create the application page (optional)")
     private String applicationPageTemplate = Documentation.APPLICATION_PAGE_TEMPLATE_FILE_NAME;
 
     @Parameter(names = {"--ignore"}, description = "Blacklist (file or comma-separated list of strings) for applications to not include")
     private String ignore;
 
     @Parameter(names = {"--create-doc"}, description = "Create documentation stub for applications with no documentation")
     private boolean createDocumentationStub = false;
 
     public String getApplicationPageTemplate() {
         return applicationPageTemplate;
     }
 
 	public String getOutputFolder() {
 		return outputFolder;
 	}
 
     public List<String> getApplications() {
         return applications;
     }
 
     public String getIgnore() { return ignore; }
 
     public boolean isCreateStub() { return createDocumentationStub; }
 
 
 }
