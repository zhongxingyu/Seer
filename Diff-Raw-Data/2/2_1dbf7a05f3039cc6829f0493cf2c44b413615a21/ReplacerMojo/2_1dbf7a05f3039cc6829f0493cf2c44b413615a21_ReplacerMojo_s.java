 package bakersoftware.maven_replacer_plugin;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Goal replaces token with value inside file
  * 
  * @goal replace
  * 
  * @phase compile
  */
 public class ReplacerMojo extends AbstractMojo {
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
      * Value to replace token with
      *
      * @parameter expression=""
      */
     private String value;
 
 	public void execute() throws MojoExecutionException {
 		getLog().info("Replacing " + token + " with " + value + " in " + file);
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
 			StringBuffer buffer = new StringBuffer();
 			String line = reader.readLine();
 			if (line == null) {
 				throw new IOException("Could not read");
 			}
 			while (line != null) {
				buffer.append(line.replaceAll(token, value));
 				line = reader.readLine();
 			}
 			reader.close();
 			
 			Writer writer = new OutputStreamWriter(new FileOutputStream(this.file));
 			writer.write(buffer.toString());
 			writer.close();
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
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
 }
