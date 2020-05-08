 package bakersoftware.maven_replacer_plugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Goal replaces token with value inside file
  * 
  * @goal replace
  * 
  * @phase compile
  */
 public class ReplacerMojo extends AbstractMojo implements StreamFactory {
 	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
 	/**
 	 * File to check and replace tokens
 	 * 
 	 * @parameter expression=""
 	 */
 	private String file;
 
 	/**
 	 * Token
 	 * 
 	 * @parameter expression=""
 	 */
 	private String token;
 
 	/**
 	 * Ignore missing files
 	 * 
 	 * @parameter expression=""
 	 */
 	private boolean ignoreMissingFile;
 
 	/**
 	 * Value to replace token with
 	 * 
 	 * @parameter expression=""
 	 */
 	private String value;
 
 	/**
 	 * Token uses regex
 	 * 
 	 * @parameter expression=""
 	 */
 	private boolean regex = true;
 
 	/**
 	 * Output to another file
 	 * 
 	 * @parameter expression=""
 	 */
 	private String outputFile;
 
 	public void execute() throws MojoExecutionException {
 		try {
 			if (ignoreMissingFile && !fileExists(file)) {
 				getLog().info("Ignoring missing file");
 				return;
 			}
       if (value != null) {
           getLog().info("Replacing " + token + " with " + value + " in " + file);
 		  } else {
          getLog().info("Removing all instances of" + token + " in " + file);
 			}
 			if (outputFile != null) {
 				getLog().info("Outputting to: " + outputFile);
 			}
 
 			getTokenReplacer().replaceTokens(token, value, isRegex());
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
 	}
 
 	public boolean isRegex() {
 		return regex;
 	}
 
 	public void setRegex(boolean regex) {
 		this.regex = regex;
 	}
 
 	private boolean fileExists(String filename) {
 		return new File(filename).exists();
 	}
 
 	public TokenReplacer getTokenReplacer() {
 		return new TokenReplacer(this, LINE_SEPARATOR);
 	}
 
 	public void setFile(String file) {
 		this.file = file;
 	}
 
 	public void setToken(String token) {
 		this.token = token;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public void setIgnoreMissingFile(boolean ignoreMissingFile) {
 		this.ignoreMissingFile = ignoreMissingFile;
 	}
 
 	public InputStream getNewInputStream() {
 		try {
 			return new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public OutputStream getNewOutputStream() {
 		try {
 			if (outputFile != null) {
 				if (!fileExists(outputFile)) {
 					ensureFolderStructureExists(outputFile);
 				}
 				return new FileOutputStream(outputFile);
 			} else {
 				return new FileOutputStream(file);
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void ensureFolderStructureExists(String file) throws MojoExecutionException {
 		File outputFile = new File(file);
 		if (!outputFile.isDirectory()) {
 			File parentPath = new File(outputFile.getParent());
 			if (!parentPath.exists()) {
 				parentPath.mkdirs();
 			}
 		} else {
 			String errorMsg = "Parameter outputFile cannot be a directory: " + file;
 			throw new MojoExecutionException(errorMsg);
 		}
 	}
 
 	public String getOutputFile() {
 		return outputFile;
 	}
 
 	public void setOutputFile(String outputFile) {
 		this.outputFile = outputFile;
 	}
 }
