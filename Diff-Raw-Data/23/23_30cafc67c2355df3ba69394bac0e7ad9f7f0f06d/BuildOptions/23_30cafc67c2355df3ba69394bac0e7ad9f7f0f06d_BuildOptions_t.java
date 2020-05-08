 package webAppBuilder;
 
 import java.io.File;
 
 
 /**
  * A data container for the options that the project is being built with.
  */
 public class BuildOptions {
 
 	private String buildFileDir;
	private String outputDir = "";
 	private Boolean debugOnly;
 	private String debugSuffix;
 	private Boolean minifyOnly;
 	private String minifySuffix;
 	private Boolean verbose;
 
 	public BuildOptions() {}
 
 
 	/**
 	 * Sets the build file's directory, so files can be read relative to it.
 	 * Automatically appends the pathSeparator character if it is not present.
 	 *
 	 * @param buildFileDir
 	 */
 	public void setBuildFileDir( String buildFileDir ) {
 		// If the projectFileDir does not end with the
 		if( !buildFileDir.endsWith( File.separator ) ) {
 			buildFileDir += File.separator;
 		}
 		this.buildFileDir = buildFileDir;
 	}
 	public String getBuildFileDir() { return this.buildFileDir; }
 
	public void setOutputDir( String outputDir ) { 
		// normalize the output dir path
		if( !outputDir.endsWith( File.separator ) ) {
			outputDir += File.separator;
		}
		if( outputDir.startsWith("./" ) ){
			outputDir.replace("./", "");                    
		}	
		this.outputDir = outputDir;
	}
 	public String getOutputDir() { return this.outputDir; }
 
 	public void setDebugOnly( Boolean debugOnly ) { this.debugOnly = debugOnly; }
 	public Boolean getDebugOnly() { return this.debugOnly; }
 
 	public void setDebugSuffix( String debugSuffix ) { this.debugSuffix = debugSuffix; }
 	public String getDebugSuffix() { return this.debugSuffix; }
 
 	public void setMinifyOnly( Boolean minifyOnly ) { this.minifyOnly = minifyOnly; }
 	public Boolean getMinifyOnly() { return this.minifyOnly; }
 
 	public void setMinifySuffix( String minifySuffix ) { this.minifySuffix = minifySuffix; }
 	public String getMinifySuffix() { return this.minifySuffix; }
 
 	public void setVerbose( Boolean verbose ) { this.verbose = verbose; }
 	public Boolean getVerbose() { return this.verbose; }
 
 }
