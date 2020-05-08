 package de.htwds.sableccmavenplugin;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.maven.project.MavenProjectHelper;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.DefaultMavenProjectHelper;
 import org.apache.maven.project.MavenProject;
 import org.sablecc.sablecc.SableCC;
 
 /**
  * Call SableCC to generate Java file from ObjectMacro file.
  *
  * @author Hong Phuc Bui
  * @version 2.0-SNAPSHOT
  *
  * @phase generate-resources
  */
 @Mojo(name = "sablecc", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
 public class SableccCaller extends AbstractMojo {
 	
	@Parameter(defaultValue="${basedir}/target/generated-sources/sablecc")
 	private String destination ;
 	
 	@Parameter(defaultValue="false")
 	private boolean noInline;
 
 	@Parameter(defaultValue="20")
 	private int inlineMaxAlts;	
 	
 	//@Parameter(required=true)
 	//private List<Map> grammars;
 
 	@Parameter(required=true)
 	private String grammar;
 
 	
 	@Parameter(defaultValue="${component.org.apache.maven.project.MavenProjectHelper}")
 	private MavenProjectHelper projectHelper;
 	
 	@Parameter(defaultValue="${project}")
 	private MavenProject project;
 	
 	@Override
 	public void execute() throws MojoFailureException {
 		try {
 			if (projectHelper==null){
 				projectHelper = new DefaultMavenProjectHelper();
 			}
 			if (project == null){
 				getLog().warn("project is null");
 			}
 			if (noInline){// this warning will be removed when I can set this option
 				getLog().warn("--no-inline is set by default to TRUE !!!!!!!!!!!");
 			}
 			Set<String> dirs = new HashSet<String>();
 			try{
 				// TODO: because the method SableCC.main(String[] argv)
 				// does not throw any exception to tell/signal the Client
 				// but just calls System.exit(1) for any error, I can not
 				// use these method to conpile the grammar file. Therefore
 				// these options don't take any effect:
 				// --no-inline
 				// --inline-max-alts
 				ArgumentVerifier arg = new ArgumentVerifier();
 				String validedGrammarPath = arg.verifyGrammarPath(grammar);
 				String validedDirPath = arg.verifyDestinationPath(destination);
 				SableCC.processGrammar(validedGrammarPath, validedDirPath);
 				dirs.add(validedDirPath);
 				projectHelper.addResource( project, validedDirPath, 
 						Collections.singletonList("**/**.dat"), new ArrayList() );
 			}catch(Exception ex){
 				getLog().error("Cannot compile the file " + grammar);
 				getLog().error(ex.getMessage());
 				throw new MojoFailureException("Cannot compile the file " + grammar, ex);
 			}
 			for(String d: dirs){
 				getLog().info("add " + d + " to generated source files");
 				project.addCompileSourceRoot(d);
 			}
 			
 		} catch (RuntimeException ex) {
 			throw new MojoFailureException("Compile grammar file error: " + ex.getMessage(), ex);
 		} catch (Exception ex) {
 			throw new MojoFailureException("Compile grammar file error: " + ex.getMessage(), ex);
 		}
 	}
 }
