 package com.joestelmach.zipper.plugin;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.commons.configuration.BaseConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.javascript.jscomp.CompilationLevel;
 import com.joestelmach.util.FileSearcher;
 
 /**
  * @author Joe Stelmach
  * 
  * @goal zipper 
  * @phase package 
  */
 public class ZipperMojo extends AbstractMojo {
   
   private List<String> _jsSourceFileNames;
   private List<String> _cssSourceFileNames;
   private JSOptimizerClosure _jsOptimizer;
   private CSSMinifierYUI _cssMinifier;
   private CSSCacheBuster _cssCacheBuster;
   private Configuration _configuration;
   private FileSearcher _fileSearcher = new FileSearcher();
   
   private static final String PROP_FILE_NAME = "zipper.properties";
   private static final String JS_EXTENSION = ".js";
   private static final String CSS_EXTENSION = ".css";
   private static final String DEFAULT_OUTPUT_DIR = "assets";
   private static final String DEFAULT_JS_OPTIMIZE_LEVEL= "WHITESPACE_ONLY";
   
   /**
    * The maven project.
    * @parameter expression="${project}"
    * @readonly
    * @required
    */
   private MavenProject _project;
   
   /**
    * Executes the zipper plugin 
    */
   public void execute() throws MojoExecutionException, MojoFailureException {
     configure();
     deleteWorkDir();
     new File(getOutputDir()).mkdirs();
     jsLintCheck();
     jsOptimize();
     cssMinify();
     concatenate();
   }
   
   /**
    * Finds and reads the zipper.properties configuration
    * 
    * @throws MojoFailureException if the file cannot be loaded 
    */
   private void configure() throws MojoFailureException {
     // search for zipper.properties in the project's base directory
     List<String> paths = _fileSearcher.search("**/" + PROP_FILE_NAME, 
         _project.getBasedir().getAbsolutePath());
     
     // and load it up into a configuration, or use an empty configuration 
     // if no zipper.properties file is found
     _configuration = new BaseConfiguration();
     if(paths.size() > 0) {
       try {
         String path = paths.get(0);
         getLog().info(PROP_FILE_NAME + " found: " + path);
         _configuration = new PropertiesConfiguration(paths.get(0));
       
       } catch (ConfigurationException e) {
         throw new MojoFailureException("Could not load your " + PROP_FILE_NAME + " file.");
       }
     }
     
     // find all the files we'll be working with inside the configured
     // web root (relative to the project's base dir)
     _jsSourceFileNames = _fileSearcher.search("**/*" + JS_EXTENSION, getWebrootPath());
     _cssSourceFileNames = _fileSearcher.search("**/*" + CSS_EXTENSION, getWebrootPath());
     
     _cssMinifier = new CSSMinifierYUI();
     _cssCacheBuster = new CSSCacheBuster(getLog());
   }
   
   /**
    * Runs all the javascript files through the linter
    * 
    * @throws MojoExecutionException
    */
   public void jsLintCheck() throws MojoFailureException, MojoExecutionException {
     boolean lintSkip = _configuration.getBoolean(ConfigKey.LINT_SKIP.getKey(), false);
     if(lintSkip) return;
     
     // find the list of files to exclude from lint checking 
     @SuppressWarnings("unchecked")
     List<String> excludedPatterns = _configuration.getList(ConfigKey.LINT_EXCLUDES.getKey());
     List<String> excludedFiles = new ArrayList<String>();
     for(String pattern:excludedPatterns) {
       excludedFiles.addAll(_fileSearcher.search(pattern, getWebrootPath()));
     }
     
     Map<String, String> lintOptionMap = getOptionsForPrefix(ConfigKey.LINT_OPTION_PREFIX.getKey());
     LinterJSLint linter = new LinterJSLint(_configuration, getLog(), lintOptionMap);
     for(String fileName:_jsSourceFileNames) {
       if(!excludedFiles.contains(fileName)) linter.check(fileName);
     }
   }
   
   /**
    * Runs all the javascript files through the closure compiler
    * 
    * @throws MojoFailureException
    */
   private void jsOptimize() throws MojoFailureException {
     if(_jsOptimizer == null) {
       _jsOptimizer = new JSOptimizerClosure();
     }
     
     CompilationLevel level = CompilationLevel.WHITESPACE_ONLY;
     String levelString = null;
     try {
       levelString = _configuration.getString(ConfigKey.JS_OPTIMIZE_LEVEL.getKey(), DEFAULT_JS_OPTIMIZE_LEVEL);
       level = CompilationLevel.valueOf(levelString);
       
     } catch(Exception e) {
       getLog().warn("Invalid compilation level: " + levelString + ".  Defaulting to " + level.toString());
     }
     
     // optimize each file from {webroot}/foo/bar.js to {outputdir}/foo/bar.js
     try {
       for(String fileName:_jsSourceFileNames) {
         getLog().info("optimizing " + fileName.substring(fileName.lastIndexOf('/') + 1) + " with " + level);
         _jsOptimizer.optimize(fileName, getOutputPathFromSourcePath(fileName), level);
       }
       
     } catch (Exception e) {
       getLog().error(e);
       throw new MojoFailureException(e.getMessage());
     }
   }
   
   /**
    * Runs all the css files through the YUI minifier, and optionally cache-busts
    * any url references
    */
   private void cssMinify() throws MojoFailureException {
     boolean bustCache = _configuration.getBoolean(ConfigKey.BUST_CACHE.getKey(), true);
     try {
       for(String fileName:_cssSourceFileNames) {
         getLog().info("minifying " + fileName);
         String outputFileName = getOutputPathFromSourcePath(fileName);
         int lineBreak = _configuration.getInt(ConfigKey.CSS_LINE_BREAK.getKey(), -1);
         _cssMinifier.minify(fileName, outputFileName, lineBreak);
         if(bustCache) _cssCacheBuster.bustIt(outputFileName);
       }
     } catch(IOException e) {
       throw new MojoFailureException(e.getMessage());
     }
   }
   
   /**
    * Concatenates the configured asset.js and asset.css groups
    * 
    * @throws MojoExecutionException
    */
   private void concatenate() throws MojoExecutionException {
     File outputDirectory = new File(getOutputDir());
     if(!outputDirectory.exists()) {
       outputDirectory.mkdirs();
     }
     
     processGroups(getGroups(ConfigKey.JS_ASSET_PREFIX), 
         outputDirectory.getAbsolutePath(), JS_EXTENSION);
     
     processGroups(getGroups(ConfigKey.CSS_ASSET_PREFIX), 
         outputDirectory.getAbsolutePath(), CSS_EXTENSION);
   }
   
   /**
    * Processes the given asset group, combining the optimized version of each 
    * included file into a new file with the group's name, stored in the given 
    * directory.
    * 
    * @param groups
    * @throws MojoExecutionException 
    */
   private void processGroups(List<? extends AssetGroup> groups, 
       String outputDirectory, String outputSuffix) throws MojoExecutionException {
     
     // for each group, we'll create a list of files that should
     // be included, and attempt to combine them with the configured name
     for(AssetGroup group:groups) {
       List<String> includedOptimizedFiles = new ArrayList<String>();
       getLog().info("building " + outputSuffix + " asset " + group.getName());
       
       for(String include:group.getIncludes()) {
         for(String fileName:_fileSearcher.search(include, getOutputDir())) {
           includedOptimizedFiles.add(fileName);
         }
       }
       
       String outputFileName = outputDirectory + "/" + group.getName() + outputSuffix;
       if(includedOptimizedFiles.size() > 0) {
         combineAssets(includedOptimizedFiles, outputFileName, group.getGzip());
       }
     }
   }
   
   /**
    * Combines the given assets into a file with the given name
    * 
    * @param assets
    * @param outputFileName
    * @throws MojoExecutionException if an asset cannot be found, or an IOException occurs
    */
   private void combineAssets(Collection<String> assets, String outputFileName, boolean gzip) 
       throws MojoExecutionException {
     
     File outputFile = new File(outputFileName);
     File gzipOutputFile = new File(outputFileName + ".gz");
     
     OutputStream output = null;
     GZIPOutputStream gzipOutput = null;
     boolean success = true;
     try {
       if(!outputFile.exists()) outputFile.createNewFile();
       output = new BufferedOutputStream(new FileOutputStream(outputFile));
       
       if(gzip) {
         if(!gzipOutputFile.exists()) gzipOutputFile.createNewFile();
         gzipOutput = new GZIPOutputStream(new BufferedOutputStream(
             new FileOutputStream(gzipOutputFile)));
       }
       
       for(String asset:assets) {
         File file = new File(asset);
         if(!file.exists()) {
           success = false;
           throw new MojoExecutionException("couldn't find file " + asset +
             ".  Please specify the path using the standard ant patterns: " +
             "http://ant.apache.org/manual/dirtasks.html#patterns");
         }
         else {
          getLog().info("adding file: " + asset + " to " + outputFileName);
           writeAssetToStream(asset, output);
          if(gzip) writeAssetToStream(asset, gzipOutput);
         }
       }
         
    } catch (Exception e) {
       getLog().error("couldn't create asset file " + outputFileName, e);
       throw new MojoExecutionException("Something went wrong combining assets.", e);
       
     } finally {
       try {
         if(output != null) output.close();
         if(gzipOutput != null) gzipOutput.close();
         if(!success) {
           outputFile.delete();
           gzipOutputFile.delete();
         }
         
       } catch (IOException e) {
         getLog().error("couldn't close writer", e);
       }
     }
   }
   
   /**
    * 
    * @param assetFileName
    * @param writer
    */
   private void writeAssetToStream(String assetFileName, OutputStream output) throws IOException {
     InputStream input = null;
     try {
       input = new BufferedInputStream(new FileInputStream(new File(assetFileName)));
       while(input.available() > 0) {
         output.write(input.read());
       }
       
     } finally {
       if(input != null) input.close();
     }
   }
   
   /**
    * 
    */
   @SuppressWarnings("unchecked")
   private List<AssetGroup>getGroups(ConfigKey prefix) {
     Iterator<String> iter = (Iterator<String>) _configuration.getKeys(prefix.getKey());
     List<AssetGroup>groups = new ArrayList<AssetGroup>();
     String defaultAssetName = prefix.equals(ConfigKey.JS_ASSET_PREFIX) ? "script" : "style";
     
     // if no groups were given for this asset prefix, we create a default group
     // consisting of all files of the prefix's type with an asset name of 'all'
     if(!iter.hasNext()) {
       AssetGroup group = new AssetGroup();
       group.setName(defaultAssetName);
       String includes = prefix.equals(ConfigKey.JS_ASSET_PREFIX) ? "**/*.js" : "**/*.css";
       group.setIncludes(Arrays.asList(new String[]{includes}));
       group.setGzip(_configuration.getBoolean(ConfigKey.GZIP.getKey(), true));
       groups.add(group);
     }
     else {
       while(iter.hasNext()) {
         String key = iter.next();
         AssetGroup group = new AssetGroup();
         String name = defaultAssetName;
         if(key.length() > prefix.getKey().length()) {
           name = key.substring(prefix.getKey().length() + 1);
         }
         group.setName(name);
         group.setIncludes(_configuration.getList(key));
         group.setGzip(_configuration.getBoolean(ConfigKey.GZIP.getKey(), true));
         groups.add(group);
       }
     }
     return groups;
   }
   
   /**
    * 
    * @return
    */
   private String getOutputDir() {
     String folderName = _configuration.getString(ConfigKey.OUTPUT_DIR.getKey(), DEFAULT_OUTPUT_DIR);
     if(folderName.startsWith("/")) folderName = folderName.substring(1);
    return _project.getBuild().getFinalName() + "/" + folderName;
    //return _project.getBuild().getOutputDirectory() + "/" + folderName;
   }
   
   /**
    * 
    * @return
    */
   private String getWebrootPath() {
     String basePath = _project.getBasedir().getAbsolutePath();
     String webroot = _configuration.getString(ConfigKey.WEB_ROOT.getKey(), "src/main/webapp");
     return basePath + (webroot.startsWith("/") ? "" : "/") + webroot;
   }
   
   /**
    * 
    */
   private void deleteWorkDir() {
     deleteDir(new File(getOutputDir()));
   }
   
   /**
    * 
    * @param sourcePath
    * @return
    */
   private String getOutputPathFromSourcePath(String sourcePath) {
     return getOutputDir() + "/" + sourcePath.substring(getWebrootPath().length() + 1);
   }
   
   /**
    * 
    * @param dir
    */
   private boolean deleteDir(File dir) {
     if(dir.isDirectory()) {
       for (String fileName:dir.list()) {
         boolean success = deleteDir(new File(dir, fileName));
         if(!success) return false;
       }
     }
     // The directory is now empty
     return dir.delete();
   }
   
   /**
    * @return a map of options found in the configuration that have
    * the given prefix
    */
   private Map<String, String> getOptionsForPrefix(String prefix) {
     Map<String, String> map = new HashMap<String, String>();
     
     @SuppressWarnings("unchecked")
     Iterator<String> iter = _configuration.getKeys(prefix);
     while(iter.hasNext()) {
       String key = iter.next();
       String option = key.substring(prefix.length() + 1);
       String value = _configuration.getString(key);
       map.put(option, value);
       getLog().info("set lint option " + option + " to " + value);
     }
     
     return map;
   }
 }
