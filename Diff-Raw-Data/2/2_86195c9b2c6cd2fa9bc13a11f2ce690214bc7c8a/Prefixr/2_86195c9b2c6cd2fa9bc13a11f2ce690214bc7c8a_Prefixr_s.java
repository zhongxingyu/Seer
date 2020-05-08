 package org.apache.tools.ant.taskdefs;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 
 /**
 * Prefixr task for to convert HTML 5 entities to corresponding browser specific entities.
  *
  * This task makes use of the Prefixr API and thus requires to connect to an external url.
  * 
  * @author Jaco Nel
  * @since 13 February 2012
  */
 public class Prefixr extends Task {
 
 	/**
 	 * The location of the Prefixr API
 	 */
 	private final String prefixrUrl = "http://prefixr.com/api/index.php";
 
 	/**
 	 * Vector containing all the filesets added for the taskdef.
 	 */
 	private Vector filesets = new Vector();
 
 	/**
 	 * The suffix that will be used when saving the response from the Prefixr api.
 	 *
 	 * If no suffix is set we will make use of the default of 'prefixr' causing a file
 	 * name to follow the pattern 'styles.prefixr.css'.
 	 *
 	 * Defaults to prefixr.
 	 */
 	private String suffix = "prefixr";
 
 	/**
 	 * Variable to decide whether or not to over ride the existing style sheet file
 	 * with the new contents.
 	 *
 	 * Defaults to false.
 	 */
 	private boolean override = false;
 
 	/**
 	 * Sets the override property allowing the user to override the existing files
 	 * or if set to false, to set the suffix of the file to be used.
 	 * 
 	 * @param override
 	 */
 	public void setSuffix( String suffix ) {
 		this.suffix = suffix;
 	}
 
 	/**
 	 * Sets the override property. If override is set to true, the suffix will
 	 * be ignored and the current css files will be overridden with the response from
 	 * the Prefixr API.
 	 *
 	 * @param override 
 	 */
 	public void setOverride( boolean override ) {
 		this.override = override;
 	}
 
 	/**
 	 * Adds a File set to be parsed for retrieval of content to be passed in to the
 	 * Prefixr Api.
 	 * 
 	 * @param fileset The file set to add
 	 */
 	public void addFileset( FileSet fileset ) {
 		filesets.add( fileset );
 	}
 
 	/**
 	 * Executes the task.
 	 *
 	 * This task reads through the file sets for and filters the files dependant
 	 * on the included and excluded files.
 	 *
 	 * For each file, it then executes a call to the Prefixr API to retrieve the
 	 * cross browser version.
 	 *
 	 * It then saves this to either a new file or the same file dependant on the
 	 * value of the override property.
 	 */
 	public void execute() {
 		// check that we have atleast one file set
 		if ( filesets.size() < 1 ) {
 			throw new BuildException( "No fileset set for the taskdef." );
 		}
 
 		// check that we either have the override property set, or a valid suffix
 		if ( ( !this.override ) && ( this.suffix.equals( "" ) ) ) {
 			throw new BuildException( "You need to set either the override property to true or supply a valid suffix for the daskdef." );
 		}
 
 		// loop throug the items in the fileset using the build in iterator
 		for ( Iterator itFileSets = filesets.iterator(); itFileSets.hasNext(); ) {
 			FileSet fs = ( FileSet ) itFileSets.next();
 
 			// retrieve a directory scanner and get a list of all included and excluded files
 			DirectoryScanner ds = fs.getDirectoryScanner( getProject() );
 
 			List includedFilesList = Arrays.asList( ds.getIncludedFiles() );
 			List excludedFilesList = Arrays.asList( ds.getExcludedFiles() );
 
 			// remove the excluded file from the included list
 			includedFilesList.removeAll( excludedFilesList );
 
 			// iterate over the included files
 			Iterator itr = includedFilesList.iterator();
 			while ( itr.hasNext() ) {
 				String path = ( String ) itr.next();
 
 				String filename = path.replace( '\\', '/' );
 				File cssFile = new File( ds.getBasedir(), filename );
 
 				if ( cssFile.exists() ) {
 					String fullPathToCss = cssFile.getAbsolutePath();
 					String css = "";
 					
 					// open file and read contents
 					try {
 						css = readFile( fullPathToCss );
 					} catch ( IOException fileException ) {
 						throw new BuildException( "Unable to open input css file, received error message: " + fileException.getMessage() );
 					}
 
 					String cssOut = "";
 					// contact the api and read the response
 					try {
 						cssOut = contactPrefixr( css );
 					} catch (Exception prefixrException) {
 						throw new BuildException( "Error while attempting to contact the Prefixr Api, received error message: " + prefixrException.toString() );
 					}
 
 					String outputFilPath = "";
 					if (this.override == true) {
 						outputFilPath = fullPathToCss;
 					} else {
 						outputFilPath = fullPathToCss.replaceFirst( "\\.css", "." + this.suffix + ".css");
 					}
 					// writeFile( cssOut, "/var/www/Personal/style.prefixr.css" );
 					System.out.println( outputFilPath );
 				}
 			}
 		}
 	}
 
 	/**
 	 * Connects to the Prefixr API and transforms the css input into cross browser compatible properties.
 	 * 
 	 * @throws Exception when issues arise with the connection and retrieval
 	 * 
 	 * @param cssToConvert The css to convert
 	 * 
 	 * @return converted css
 	 */
 	public String contactPrefixr( String cssToConvert ) throws Exception {
 		// setup a connection to the API
 		URL url = new URL( this.prefixrUrl );
 		URLConnection connection = url.openConnection();
 		connection.setDoOutput( true );
 
 		// set up a writer to pass parameters to the API
 		OutputStreamWriter out = new OutputStreamWriter(
 				connection.getOutputStream() );
 		// pass in the css as a parameter
 		out.write( "css=" + URLEncoder.encode( cssToConvert, "UTF-8" ) );
 		out.close();
 
 		// setup a reader to retrieve the converted css
 		BufferedReader in = new BufferedReader(
 				new InputStreamReader(
 				connection.getInputStream() ) );
 
 		// read each line from the API and append to a string for return
 		String eol = System.getProperty( "line.separator" );
 		String decodedString = "";
 		String outputCss = "";
 		while ( ( decodedString = in.readLine() ) != null ) {
 			outputCss = outputCss + decodedString + eol;
 		}
 		in.close();
 
 		// return the output css
 		return outputCss;
 	}
 
 	/**
 	 * Public function read the contents of the css files.
 	 * 
 	 * @param path The path to the file to read
 	 * @throws IOException When unable to access or read the file
 	 * 
 	 * @return String the file content
 	 */
 	private String readFile( String path ) throws IOException {
 		FileInputStream stream = new FileInputStream( new File( path ) );
 		try {
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map( FileChannel.MapMode.READ_ONLY, 0, fc.size() );
 			/* Instead of using default, pass in a decoder. */
 			return Charset.defaultCharset().decode( bb ).toString();
 		} finally {
 			stream.close();
 		}
 	}
 
 	/**
 	 * Writes content to a file.
 	 *
 	 * @param content The content to write to the file
 	 * @param fileName The file name to write the content to
 	 * 
 	 * @throws IOException
 	 */
 	private void writeFile( String content, String fileName ) throws IOException {
 		// Create file/ open file for write
 		FileWriter fstream = new FileWriter( fileName );
 		BufferedWriter out = new BufferedWriter( fstream );
 
 		// write to file
 		out.write( content );
 
 		//Close the output stream
 		out.close();
 	}
 }
