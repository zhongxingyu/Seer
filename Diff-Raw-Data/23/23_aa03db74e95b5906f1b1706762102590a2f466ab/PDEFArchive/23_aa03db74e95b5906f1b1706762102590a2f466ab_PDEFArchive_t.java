 /* This file is part of calliope.
  *
  *  calliope is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  calliope is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
  */
 package calliope.export;
 import calliope.constants.Database;
 import calliope.Connector;
 import calliope.constants.JSONKeys;
 import calliope.constants.Formats;
 import calliope.json.JSONDocument;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.FileOutputStream;
 import calliope.exception.AeseException;
 import static calliope.export.Format.MVD;
 import static calliope.export.Format.TEXT;
 import static calliope.export.Format.XML;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.ArrayList;
 import edu.luc.nmerge.mvd.MVD;
 import edu.luc.nmerge.mvd.MVDFile;
 
 /**
  * Represent a hierarchical set of files as a set of nested folders
  * @author desmond
  */
 public class PDEFArchive 
 {
     public static String CORTEX_MVD = "cortex.mvd";
     public static String CORTEX_TXT = "cortex.txt";
     public static String NAME = "archive";
     static String[] IMG_KEYS = { "img","src" };
     final String[] tabooFields = {JSONKeys._ID,JSONKeys.DOCID,JSONKeys.REV};
     static String[] IMG_SUFFIXES = {".bmp",".eps",".gif",".jpg",".png",
         ".tif","jpeg","tiff"};
     static String[] requiredCorforms = {"default","diffs/default",
        "html","list/default","list/twin-list","table/default"};
     /** unique wrapper for archive */
     File root;
     /** actual archive directory inside root */
     File archive;
     /** export formats to generate */
     Format[] formats;
     /**
      * Delete a directory and its contents recursively
      * @param dir the dir to delete
      */
     private void deleteDir( File dir )
     {
         File[] contents = dir.listFiles();
         for ( int i=0;i<contents.length;i++ )
         {
             if ( contents[i].isFile() )
                 contents[i].delete();
             else if ( !contents[i].getName().equals("..")
                 && !contents[i].getName().equals(".") )
                 deleteDir( contents[i] );
         }
         dir.delete();
     }
     /**
      * Make a temporary directory
      * @return the directory in tmpdir
      * @throws IOException 
      */
     private File createTempDirectory() throws IOException
     {
         final File temp;
         temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
         if ( !temp.delete() )
             throw new IOException("Could not delete temp file: " 
                 +temp.getAbsolutePath());
         if ( !temp.mkdir() )
             throw new IOException("Could not create temp directory: " 
                 +temp.getAbsolutePath());
         return temp;
     }
     /**
      * Create a PDEF archive
      * @param name the name for the archive
      * @param formats an array of formats of type
      * @param host
      */
     public PDEFArchive( String name, Format[] formats, String host, 
         boolean addRequired ) throws AeseException
     {
         this.formats = formats;
         try
         {
             root = createTempDirectory();
             archive = new File( root, name );
             archive.mkdir();
             File archiveConf = new File( archive, "archive.conf" );
             archiveConf.createNewFile();
             FileWriter fw = new FileWriter( archiveConf );
             fw.write( "{\n\t\"" );
             fw.write( JSONKeys.BASE_URL );
             fw.write( "\": \"" );
             fw.write( host );
             fw.write( "\"\n}\n" );
             fw.close();
             if ( addRequired )
             {
                 addAllConfigs();
                 addRequiredCorforms();
             }
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
     /**
      * Add all configs to be on the safe side
      */
     private void addAllConfigs() throws Exception
     {
         String[] configs = Connector.getConnection().listCollection(
             Database.CONFIG );
         for ( int i=0;i<configs.length;i++ )
             addToAbsolutePath( Database.CONFIG, configs[i] );
     }
     /**
      * Add required corforms
      */
     private void addRequiredCorforms() throws Exception
     {
         for ( int i=0;i<requiredCorforms.length;i++ )
             addToAbsolutePath( Database.CORFORM, requiredCorforms[i] );
     }
     /**
      * Remove fields that will be added by the database after import
      * @param json the original JSON string
      * @return a possibly modified JSON string with the fields removed 
      */
     private String removeTabooFields( String json )
     {
         boolean changed = false;
         DBObject jdoc = (DBObject) JSON.parse(json);
         for ( int i=0;i<tabooFields.length;i++ )
         {
             if ( jdoc.containsField(tabooFields[i]) )
             {
                 jdoc.removeField(tabooFields[i]);
                 changed = true;
             }
         }
         if ( changed )
             json = jdoc.toString();
         return json;
     }
     /**
      * Add a resource from the server to the root directory
      * @param db the database to fetch it from
      * @param path the path within that database
      */
     private void addToAbsolutePath( String db, String docID ) throws Exception
     {
         File parent = new File( archive, "@"+db );
         if ( !parent.exists() )
             parent.mkdir();
         String json = Connector.getConnection().getFromDb( db, docID );
         json = removeTabooFields( json );
         String[] parts = docID.split("/");
         File current = parent;
         for ( int i=0;i<parts.length;i++ )
         {
             if ( i == parts.length-1 )
             {
                 File child = new File( current, parts[i] );
                 if ( !child.exists() )
                 {
                     child.createNewFile();
                     FileWriter fw = new FileWriter( child );
                     fw.write( json, 0, json.length() );
                     fw.close();
                 }
             }
             else
             {
                 current = new File( current, parts[i] );
                 if ( !current.exists() )
                     current.mkdir();
             }
         }
     }
     /**
      * Write the corform specified in a cortex or corcode
      * @param jdoc the parsed json document
      * @throws Exception 
      */
     private void writeCorform( JSONDocument jdoc ) throws Exception
     {
         String corformID = (String)jdoc.get( JSONKeys.STYLE );
         if ( corformID != null )
         {
             String json;
             try
             {
                 json = Connector.getConnection().getFromDb(
                     Database.CORFORM,corformID );
             }
             catch ( Exception e )
             {
                 corformID = "default";
             }
             addToAbsolutePath( Database.CORFORM, corformID );
         }
     }
     /**
      * Save the split versions of an MVD as separate files
      * @param dir the directory to write to
      * @param texts the array of texts to write
      * @param versionIDs their version IDs in order
      * @param longNames their long names in order
      * @throws IOException 
      */
     private void saveSplitVersions( File dir, String[] texts, 
         ArrayList<String> versionIDs, ArrayList<String> longNames ) 
         throws IOException
     {
         FileWriter  fw;
         for ( int j=0;j<texts.length;j++ )
         {
             File current = dir;
             String versionID = versionIDs.get(j);
             String[] parts = versionID.split("/");
             // create group directory structure
             for ( int k=0;k<parts.length-1;k++ )
             {
                 current = new File( current, parts[k] );
                 if ( !current.exists() )
                     current.mkdir();
             }
             current = new File( current, parts[parts.length-1] );
             current.createNewFile();
             fw = new FileWriter( current );
             fw.write( texts[j] );
             fw.close();
         }
         // write out the versions.conf file (for version long names)
         File versionsFile = new File( dir, "versions.conf" );
         versionsFile.createNewFile();
         fw = new FileWriter( versionsFile );
         fw.write( "{\n\"versions\": [\n" );
         for ( int i=0;i<versionIDs.size()&&i<longNames.size();i++ )
         {
             String key = versionIDs.get( i );
             String value = longNames.get( i );
             String line = "\t{ \"key\": \""+key+"\", \"value\": \""+value+"\" }";
             fw.write( line );
             if ( i < versionIDs.size()-1 )
                 fw.write( ",");
             fw.write( "\n" );
         }
         fw.write( "] }\n" );
         fw.close();
     }
     /**
      * Write a config file to a directory with the keys of the json doc
      * @param jdoc the jdoc
      * @param fname name of the config file
      * @param dir the directory to write to
      */
     private void writeConfigRest( JSONDocument jdoc, String fname, File dir ) 
         throws Exception
     {
         File f = new File( dir, fname );
         f.createNewFile();
         FileWriter fw = new FileWriter( f );
         JSONDocument jdoc2 = new JSONDocument();
         if ( jdoc.containsKey(JSONKeys.STYLE) )
             jdoc2.put( JSONKeys.STYLE, (String)jdoc.get(JSONKeys.STYLE) );
         if ( jdoc.containsKey(JSONKeys.FORMAT) )
             jdoc2.put( JSONKeys.FORMAT, (String)jdoc.get(JSONKeys.FORMAT) );
         if ( jdoc.containsKey(JSONKeys.TITLE) )
             jdoc2.put( JSONKeys.TITLE, (String)jdoc.get(JSONKeys.TITLE) );
         if ( jdoc.containsKey(JSONKeys.AUTHOR) )
             jdoc2.put( JSONKeys.AUTHOR, (String)jdoc.get(JSONKeys.AUTHOR) );
         if ( jdoc.containsKey(JSONKeys.SECTION) )
             jdoc2.put( JSONKeys.SECTION, (String)jdoc.get(JSONKeys.SECTION) );
         if ( jdoc.containsKey(JSONKeys.VERSION1) )
             jdoc2.put( JSONKeys.VERSION1, (String)jdoc.get(JSONKeys.VERSION1) );
         fw.write( jdoc2.toString() );
         fw.write( "\n" );
         fw.close();
     }
     /**
      * Write a cortex to disk
      * @param dir the "%" directory to create it in
      * @param docID the file's docID
      * @return the dir in which the CorTex resides
      */
     private File writeRawCortexToDir( File dir, String docID ) 
         throws Exception
     {
         FileWriter fw;
         String json = Connector.getConnection().getFromDb( 
             Database.CORTEX, docID );
         JSONDocument jdoc = JSONDocument.internalise( json );
         String fmt = (String)jdoc.get(JSONKeys.FORMAT);
         if ( fmt == null )
             throw new AeseException("Missing format field for "+docID);
         File destDir = null;
         String corTexName = "";
         if ( fmt.startsWith(Formats.MVD) )
         {
             corTexName = CORTEX_MVD;
             destDir = new File( dir, Format.MVD.toString() );
         }
         else if ( fmt.equals(Formats.TEXT) )
         {
             corTexName = CORTEX_TXT;
             destDir = new File( dir, Format.TEXT.toString() );
         }
         else
             throw new AeseException("Unknown format "+fmt );
         if ( !destDir.exists() )
             destDir.mkdir();
         writeCorform( jdoc );
         writeConfigRest( jdoc, "cortex.conf", destDir );
         File cor = new File( destDir, corTexName );
         cor.createNewFile();
         fw = new FileWriter( cor );
         String mvd = (String)jdoc.get(JSONKeys.BODY);
         fw.write( mvd, 0, mvd.length() );
         fw.close();
         return destDir;
     }
     /**
      * Write the text versions of a cortex to disk
      * @param dir the "%" directory to create it in
      * @param docID the file's docID
      */
     private void writeSplitCortexToDir( File dir, String docID ) throws AeseException
     {
         try
         {
             String json = Connector.getConnection().getFromDb( 
                 Database.CORTEX, docID );
             JSONDocument jdoc = JSONDocument.internalise( json );
             File textDir = new File( dir, Format.TEXT.toString() );
             if ( !textDir.exists() )
                 textDir.mkdir();
             // make a big dir for all the versions
             File ctDir = new File( textDir, Database.CORTEX );
             if ( !ctDir.exists() )
                 ctDir.mkdir();
             ArrayList<String> versionIDs = new ArrayList<String>();
             ArrayList<String> longNames = new ArrayList<String>();
             String[] texts = splitVersions( 
                 (String)jdoc.get(JSONKeys.BODY), versionIDs, longNames );
             saveSplitVersions( ctDir, texts, versionIDs, longNames );
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
     /**
      * Split a corcode into its constituent versions
      * @param corcode the merged mvd
      * @param versionIDs an array of versionIDs or null
      * @param longNames an array of long version names or null
      * @return an array of split corcodes
      * @throws AeseException if the stil couldn't be encoded as a string
      */
     private String[] splitVersions( String corcode, 
         ArrayList<String> versionIDs, ArrayList<String> longNames ) 
         throws AeseException
     {
         try
         {
             if ( corcode.charAt(0)==123 )
                 throw new AeseException("Not an MVD");
             MVD mvd = MVDFile.internalise( corcode );
             int nVersions = mvd.numVersions();
             String[] array = new String[nVersions];
             for ( int i=1;i<=nVersions;i++ )
             {
                 byte[] data = mvd.getVersion( i );
                 if ( versionIDs != null )
                     versionIDs.add( mvd.getGroupPath((short)i)+"/"
                         +mvd.getVersionShortName(i));
                 if ( longNames != null )
                     longNames.add( mvd.getVersionLongName(i));
                 array[i-1] = new String( data, mvd.getEncoding() );
             }
             return array;
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
     /**
      * Do a binary lookup on a sorted array of strings
      * @param suffix the suffix to test
      * @return true if it was an image suffix else false
      */
     private boolean isInList( String suffix, String[] list )
     {
         int top,bot;
         top = 0;
         bot = list.length-1;
         while ( top <= bot )
         {
             int mid = (top+bot)/2;
             int res = list[mid].compareTo(suffix);
             if ( res == 0 )
                 return true;
             else if ( res > 0 )
                 top = mid+1;
             else
                 bot = mid-1;
         }
         return false;
     }
     /**
      * Strip image tags from STIL corcodes
      * @param images a set in which to store the image docIDs
      * @param jdoc a json document containing the corcode
      */
     private void stripFromStil( HashSet<String> images, JSONDocument jdoc )
     {
         Set<String> keys = jdoc.keySet();
         Iterator<String> iter = keys.iterator();
         while ( iter.hasNext() )
         {
             String key = iter.next();
             if ( isInList(key,IMG_KEYS) )
             {
                 String value = (String)jdoc.get(key);
                 String suffix = "";
                 if ( value.length()>4 )
                     suffix = value.substring(value.length()-4);
                 if ( isInList(suffix,IMG_SUFFIXES) && !images.contains(value) )
                     images.add( value );
             }
             else
             {
                 Object value = jdoc.get( key );
                 if ( value instanceof JSONDocument )
                     stripFromStil( images, (JSONDocument)value );
             }
         }
     }
     /**
      * Add an image to the archive
      * @param imgID the image to add
      */
     private void addImage( String imgID ) throws AeseException, IOException
     {
         byte[] imageData = Connector.getConnection().getImageFromDb( 
             Database.CORPIX, imgID );
         File corpixDir = new File( archive, Database.CORPIX );
         if ( !corpixDir.exists() )
             corpixDir.mkdir();
         String[] parts = imgID.split("/");
         File current = corpixDir;
         for ( int i=0;i<parts.length;i++ )
         {
             if ( i == parts.length-1 )
             {
                 File child = new File( current, parts[i] );
                 child.createNewFile();
                 FileOutputStream fos = new FileOutputStream( child );
                 fos.write( imageData );
                 fos.close();
             }
             else
             {
                 current = new File( current, parts[i] );
                 current.mkdir();
             }
         }
     }
     /**
      * Write a corcode to the given directory
      * @param dir the directory with the cortex already in it
      * @param docID its docid minus the file name
      * @param name its file name
      * @throws Exception 
      */
     private void writeRawCorcodeToDir( File dir, String docID ) 
         throws Exception
     {
         FileWriter fw;
         String tail;
         String regex = docID+"/[^/]*";
         String[] corcodes = Connector.getConnection().listDocuments( 
             Database.CORCODE, regex );
         File ccDir = new File( dir, Database.CORCODE );
         if ( !ccDir.exists() )
             ccDir.mkdir();
         // use this to save imageIDs
         HashSet<String> images = new HashSet<String>();
         // put each corcode in ccDir
         for ( int i=0;i<corcodes.length;i++ )
         {
             String json = Connector.getConnection().getFromDb(
                 Database.CORCODE, corcodes[i] );
             // save the corform first if not already there
             JSONDocument jdoc = JSONDocument.internalise( json );
             writeCorform( jdoc );
             // now split each corcode MVD into separate STIL files 
             String body = (String)jdoc.get(JSONKeys.BODY);
             String fmt = (String)jdoc.get(JSONKeys.FORMAT);
             if ( fmt == null || body == null )
                 throw new AeseException("Empty corcode "+docID );
             String[] versions = null;
             // we need to extract the image IDs if any
             if ( fmt.startsWith(Formats.MVD) )
                 versions = splitVersions( body, null, null );
             else
             {
                 versions = new String[1];
                 versions[0] = body;
             }
             // extract the image IDs for each version of the corcode
             for ( int j=0;j<versions.length;j++ )
                 stripFromStil( images, JSONDocument.internalise(versions[j]) );
             // find the individual corcode's file name (tail)
             tail = corcodes[i].substring(docID.length()+1);
             File ccFile = new File( ccDir, tail );
             writeConfigRest( jdoc, tail+".conf", ccDir );
             ccFile.createNewFile();
             fw = new FileWriter( ccFile );
             fw.write( body );
             fw.close();
         }
         // finally, write out the images from that corcode
         String[] imgs = new String[images.size()];
         images.toArray( imgs );
         for ( int j=0;j<imgs.length;j++ )
             addImage( imgs[j] );
     }
     /**
      * Write a Corcode split into its component versions
      * @param dir the directory to write to
      */
     private void writeSplitCorcodeToDir( File dir, String docID ) 
         throws Exception
     {
         String tail;
         String regex = docID+"/[^/]*";
         String[] corcodes = Connector.getConnection().listDocuments( 
             Database.CORCODE, regex );
         for ( int i=0;i<corcodes.length;i++ )
         {
             String json = Connector.getConnection().getFromDb(
                 Database.CORCODE, corcodes[i] );
             // save the corform first
             JSONDocument jdoc = JSONDocument.internalise( json );
             writeCorform( jdoc );
             // now split each corcode MVD into separate STIL files 
             String body = (String)jdoc.get(JSONKeys.BODY);
             String fmt = (String)jdoc.get(JSONKeys.FORMAT);
             if ( fmt == null || body == null )
                 throw new AeseException("Missing format or body for "+docID);
             ArrayList<String> versionIDs = new ArrayList<String>();
             ArrayList<String> longNames = new ArrayList<String>();
             String[] versions = null;
             if ( fmt.startsWith(Formats.MVD) )
                 versions = splitVersions( body, versionIDs, longNames );
             else
             {
                 versions = new String[1];
                 versions[0] = body;
             }
             // extract the image IDs for each version of the corcode
             HashSet<String> images = new HashSet<String>();
             for ( int j=0;j<versions.length;j++ )
                 stripFromStil( images, JSONDocument.internalise(versions[j]) );
             // make a big dir for all the corcodes
             File ccDir = new File( dir, Database.CORCODE );
             if ( !ccDir.exists() )
                 ccDir.mkdir();
             // now create ONE dir for each corcode
             tail = corcodes[i].substring(docID.length()+1);
             File oneDir = new File( ccDir, tail );
             if ( !oneDir.exists() )
                 oneDir.mkdir();
             // and put each version of the corcode into it. phew!
             saveSplitVersions( oneDir, versions, versionIDs, longNames );
             // finally, write out the collected images
             String[] imgs = new String[images.size()];
             images.toArray( imgs );
             for ( int j=0;j<imgs.length;j++ )
                 addImage( imgs[j] );
         }
     }
     /**
      * Build an MVD record within a folder
      * @param dir the directory to store everything in
      * @param docID the docID of the cortex and corcodes
      */
     private void buildMVDDoc( File dir, String docID ) throws AeseException
     {
         try
         {
             File destDir = writeRawCortexToDir( dir, docID );
             //System.out.println("wrote cortex");
             writeRawCorcodeToDir( destDir, docID );
            // System.out.println("wrote corcode");
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }   
     /**
      * Split an MVD into its components versions
      * @param dir the directory to store everythign in
      * @param docID the docID of the cortex and corcodes
      */
     private void buildTextDoc( File dir, String docID ) throws AeseException
     {
         try
         {
             writeSplitCortexToDir( dir, docID );
             writeSplitCorcodeToDir( dir, docID );
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     } 
     /**
      * Put a document into the given directory using all the PDEF's formats
      * @param dir the directory to contain the doc
      * @param docID the ID of the document in cortex and corcode
      */
     private void buildDoc( File dir, String docID ) throws AeseException
     {
         for ( int i=0;i<formats.length;i++ )
         {
             switch ( formats[i] )
             {
                 case TEXT:
                     buildTextDoc( dir, docID );
                     break;
                 case MVD:
                     buildMVDDoc( dir, docID );
                     break;
                 case XML:
                     // unimplemented
                     break;
             }
         }
     }
     /**
      * Add a CorTex to the archive by creating appropriate directories
      * @param docID the docID of the cortex
      */
     public void addCorTex( String docID ) throws AeseException
     {
         String[] parts = docID.split("/");
         if ( parts.length > 0 )
         {
             File last,current;
             current = archive;
             for ( int i=0;i<parts.length;i++ )
             {
                 if ( i == parts.length-1 )
                 {
                     last = new File( current, "%"+parts[i] );
                     if ( !last.exists() )
                         last.mkdir();
                     buildDoc( last, docID );
                 }
                 else
                 {
                     String name = (i==0)?"+"+parts[0]:parts[i];
                     current = new File( current, name );
                     if ( !current.exists() )
                         current.mkdir();
                 }
             }
         }
     }
     /**
      * Convert this PDEF archive into a ZIP file
      * @param zipType the type of zipping to use
      * @return a file being a complete zip archive
      */
     public File zip( ZipType zipType ) throws AeseException
     {
         try
         {
             File compressed = null;
             Compressor comp = null;
             switch ( zipType )
             {
                 case TAR_GZ:
                     comp = new TarArchive( archive );
                     break;
                 case ZIP:
                     comp = new ZipArchive( archive );
                     break;
             }
             compressed = comp.compress();
             deleteDir( root );
             return compressed;
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
 }
