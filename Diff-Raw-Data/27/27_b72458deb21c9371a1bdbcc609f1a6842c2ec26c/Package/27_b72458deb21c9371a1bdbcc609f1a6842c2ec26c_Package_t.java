 package webAppBuilder.pkg;
 
 import webAppBuilder.*;
 import webAppBuilder.include.*;
 import java.util.*;
 import java.io.*;
 import org.json.*;
 
 /**
  * Represents a package of files to be concatenated.
  */
 public abstract class Package {
 
 	private String name;
 	private String filename;
 	private List<Include> includes;
 	private BuildOptions buildOptions;
 
 	private String combinedFileContents;
 	private String minifiedFileContents;
 
 
 	/**
 	 * Factory method to create the appropriate {@link Package} based on its JSONObject data.
 	 *
 	 * @param pkgJSON The JSONObject representation of the package.
 	 * @param buildOptions The configuration options for this build.
 	 * @return A Package based on the JSON provided.
 	 * @throws JSONException If there is an error parsing the JSON.
 	 */
 	public static Package fromJSON( JSONObject pkgJSON, BuildOptions buildOptions ) throws JSONException {
 		String name = pkgJSON.optString( "name" );
 		String filename = pkgJSON.optString( "filename" );
 
 		if( filename.equals( "" ) || filename.lastIndexOf( '.' ) == -1 ) {
 			throw new BuildFileException( "A `filename` property is required for the package '" + name + "', and must have a valid extension" );
 		}
 
 		String[] filenamePieces = filename.split( "\\." );  // split on a period
 		String fileExtension = '.' + filenamePieces[ filenamePieces.length - 1 ];
 
 		
 		Package pkg;
 		if( fileExtension.equals( ".js" ) ) {
 			pkg = new JavaScriptPackage( name, filename, buildOptions );
 		} else if( fileExtension.equals( ".css" ) ) {
 			pkg = new CSSPackage( name, filename, buildOptions );
 		} else {
 			throw new BuildFileException( "The package '" + name + "' must have a filename that ends in .js or .css" );
 		}
 
 		JSONArray includesArr;
 		try {
 			includesArr = pkgJSON.getJSONArray( "includes" );
 		} catch( JSONException ex ) {
 			throw new BuildFileException( "Could not read `includes` array in pkg '" + name + "'" );
 		}
 
 		// Loop over the includes, checking each one that it was valid, and adding it to the Package
 		for( int i = 0, len = includesArr.length(); i < len; i++ ) {
 			JSONObject includeJSON = includesArr.getJSONObject( i );
 			Include include = Include.fromJSON( pkg, includeJSON, buildOptions, fileExtension );
 
 			if( include == null ) {
 				throw new BuildFileException( "An Include Directive was not valid. The include directive is: " + includeJSON.toString() );
 			}
 			
 			pkg.addIncludeDirective( include );
 		}
 
 		return pkg;
 	}
 
 
 	/**
 	 * Creates a Package.
 	 */
 	public Package( String name, String filename, BuildOptions buildOptions ) {
 		this.name = name;
 		this.filename = filename;
 		this.includes = new ArrayList<Include>();
 		this.buildOptions = buildOptions;
 	}
 
 
 	/**
 	 * Creates a Package, providing all necessary pieces of information.
 	 */
 	public Package( String name, String filename, List<Include> includes, BuildOptions buildOptions ) {
 		this.name = name;
 		this.filename = filename;
 		this.includes = includes;
 		this.buildOptions = buildOptions;
 	}
 
 
 	/**
 	 * Adds an {@link Include Include Directive} to the Package.
 	 *
 	 * @param include The Include directive to add.
 	 */
 	public void addIncludeDirective( Include include ) {
 		if( include == null ) {
 			throw new RuntimeException( "The Include provided to addIncludeDirective() cannot be null" );
 		}
 
 		this.includes.add( include );
 	}
 
 
 	/**
 	 * Retrieves the name of the Package.
 	 * 
 	 * @return The name of the Package.
 	 */
 	public String getName() {
 		return name;
 	}
 
 
 	/**
 	 * Retrieves the combined (concatenated) content of all of the package's files.
 	 *
 	 * @throws FileNotFoundException If a file specified by an include directive is not found.
 	 * @throws IOException If there is an error reading a file.
 	 */
 	public String getCombinedContents() throws FileNotFoundException, IOException {
 		if( combinedFileContents != null ) {
 			return combinedFileContents;
 		}
 
 		Set<File> files = new LinkedHashSet<File>();
 		StringBuilder fileContents = new StringBuilder();
 		String lineSeparator = System.getProperty( "line.separator" );
 
 		// Put the files into the LinkedHashSet, to remove duplicates and leave them in order
 		for( Include include : includes ) {
 			files.addAll( include.getFiles() );
 		}
 
 		// Now take the files, read them, and concatenate their contents
 		for( File file : files ) {
 			fileContents.append( FileHelper.getContents( file ) );
 			fileContents.append( lineSeparator );  // make sure there is a line break after each file, to account for any last line in the file that maybe has a double slash comment. Don't want that accidentally commenting out the first line of the next file!
 		}
 
 		combinedFileContents = fileContents.toString();
 		return combinedFileContents;
 	}
 
 
 	/**
 	 * Retrieves the minified combined file contents of the Package.
 	 *
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public String getMinifiedContents() throws FileNotFoundException, IOException {
 		if( minifiedFileContents != null ) {
 			return minifiedFileContents;
 		}
 
 		minifiedFileContents = createMinifiedContents( getCombinedContents() );
 		return minifiedFileContents;
 	}
 
 
 	/**
 	 * Creates and returns the minified contents of the Package, provided the unminified
 	 * combined file contents.
 	 *
 	 * @param combinedContents The combined contents of the files in the package.
 	 * @throws IOException If there is an error creating the minified contents.
 	 */
 	public abstract String createMinifiedContents( String combinedContents ) throws IOException;
 
 
 	/**
 	 * Writes the output files for the Package: one for the concatenated but not
 	 * minified, and the other for the minified version. Either of these can be
 	 * turned off though in the {@link BuildOptions}.
 	 *
 	 * @param licenseHeader The license header to write into the output files.
 	 * @throws IOException If the file(s) could not be written.
 	 */
 	public void writeOutput( String licenseHeader ) throws IOException {
 		System.out.println( "Writing output for package: '" + name + "'..." );
 
 		String debugFilename = FileHelper.insertFileSuffix( filename, buildOptions.getDebugSuffix() );
 		String minifiedFilename = FileHelper.insertFileSuffix( filename, buildOptions.getMinifySuffix() );
 
		File debugFile = new File( buildOptions.getOutputDir() + debugFilename );
		File minifiedFile = new File( buildOptions.getOutputDir() + minifiedFilename );

		// ensure output dirs exist
		debugFile.mkdirs();
		minifiedFile.mkdirs();
 
 		// Delete the debugFile and minifiedFile before writing output, so that their generated content never accidentally
 		// gets included in the build files by including a directory or tree that encompasses them. They will be regenerated
 		// directly after.
 		debugFile.delete();
 		minifiedFile.delete();
 
 		// Only create a "debug" build if the "minifyOnly" flag is not set
 		if( !buildOptions.getMinifyOnly() ) {
 			FileHelper.setContents( debugFile, licenseHeader + getCombinedContents() );
 			System.out.println( "    Wrote: " + debugFile.getAbsolutePath() );
 		}
 
 		// Only create a "minified" build if the "debugOnly" flag is not set
 		if( !buildOptions.getDebugOnly() ) {
 			FileHelper.setContents( minifiedFile, licenseHeader + getMinifiedContents() );
 			System.out.println( "    Wrote: " + minifiedFile.getAbsolutePath() );
 		}
 	}
 }
