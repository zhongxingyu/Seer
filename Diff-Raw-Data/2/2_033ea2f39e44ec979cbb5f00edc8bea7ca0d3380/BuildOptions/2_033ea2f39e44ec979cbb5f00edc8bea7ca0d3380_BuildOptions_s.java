 package webAppBuilder;
 
 public class BuildOptions {
 	
     private String outputDir;
 	private Boolean debugOnly;
 	private String debugSuffix;
 	private Boolean minifyOnly;
    private String minifySuffix;
 	private Boolean verbose;
 	
 	public BuildOptions() {}
 	
 	
 	public void setOutputDir( String outputDir ) { this.outputDir = outputDir; }
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
