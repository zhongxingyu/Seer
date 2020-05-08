 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.auxiliar;
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 import java.util.zip.Adler32;
 import java.util.zip.CheckedInputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 public class File extends java.io.File {
 
     // next constants are use to unzip the seleccted files and folders in ead jar and LO import
     
     /**
      * Store the prefix for chapter file 
      */
     private static final String CHAPTER = "chapter";
     
     /**
      * Store the name of descriptor file
      */
     private static final String DESCRIPTOR = "descriptor.xml";
     
     /**
      * Store the prefix for assets folder file 
      */
     private static final String ASSETS = "assets";
     
 
     /**
      * Store the prefix for assets folder file 
      */
     private static final String GUI = "gui";
     
     /**
      * Store the prefix for assessment folder file 
      */
     private static final String ASSESS = "assessment";
     
     /**
      * Store the prefix for adaptation folder file 
      */
     private static final String ADAPT = "adaptation";
     
     /**
      * Store the suffix to identify a chapter
      */
     private static final String CHAPTER_SUFFIX = ".xml";
     
     /**
      * Store the suffix to identify a dtd files
      */
     private static final String DTD_SUFFIX = ".dtd";
     
     // constants to identify eadventure jars and LO
 
     private static final String JAR_CHECKER= "es.eucm.eadventure";
     
     private static final String LO_CHECKER = "manifest.xml";
     
     private static final String EAD_LO_CHECKER = ".jar";
 
     
     /**
      * Required
      */
     private static final long serialVersionUID = -756962356996164247L;
 
     private static String lastSlash( String absolutePath ) {
 
         if( absolutePath != null && ( absolutePath.endsWith( "/" ) || absolutePath.endsWith( "\\" ) ) ) {
             return absolutePath.substring( 0, Math.max( absolutePath.lastIndexOf( "\\" ), absolutePath.lastIndexOf( "/" ) ) );
         }
         else {
             return absolutePath;
         }
     }
 
     public File( String parent, String path ) {
 
         super( lastSlash( parent ), path );
     }
 
     public File( String path ) {
 
         super( path );
     }
 
     public File( File parent, String name ) {
 
         this( parent.getAbsolutePath( ), name );
     }
 
     public static boolean copyTo( java.io.File origin, java.io.File destiny ) {
 
         boolean copied = true;
         try {
             InputStream in = new FileInputStream( origin );
             if( destiny.getParentFile( ) != null && !destiny.getParentFile( ).exists( ) ) {
                 destiny.getParentFile( ).mkdirs( );
             }
             if( !destiny.exists( ) )
                 destiny.createNewFile( );
             OutputStream out = new FileOutputStream( destiny );
 
             // Transfer bytes from in to out
             byte[] buf = new byte[ 1024 ];
             int len;
             while( ( len = in.read( buf ) ) > 0 ) {
                 out.write( buf, 0, len );
             }
             in.close( );
             out.close( );
         }
         catch( Exception e ) {
             copied = false;
             //Controller.getInstance().showErrorDialog( TextConstants.getText("Error.Title"), TextConstants.getText("Error.CopyingFiles", new String[]{origin.getAbsolutePath( ), destiny.getName( )}) );
         }
 
         return copied;
 
     }
 
     public boolean copyTo( java.io.File destiny ) {
 
         return copyTo( this, destiny );
     }
 
     public static boolean copyAllTo( java.io.File origin, java.io.File destiny ) {
 
         boolean copied = true;
         if( !origin.exists( ) ) {
             //System.out.println( "ORIGIN NO CREADO: "+origin.getAbsolutePath( ) );
             return false;
         }
         try {
             if( origin.isDirectory( ) ) {
                 //boolean copiedAux2 = destiny.mkdirs( );
                 //copied &= copiedAux2;
                 //if (!copiedAux2){
                 //System.out.println( "DIRECTORIO NO CREADO(MKDIRS): "+destiny.getAbsolutePath( ) );
                 //}
                 for( java.io.File childOrigin : origin.listFiles( ) ) {
                     if( childOrigin.isDirectory( ) ) {
                         File childDestiny = new File( destiny.getAbsolutePath( ), childOrigin.getName( ) + "/" );
                         //boolean copiedAux =childDestiny.mkdirs( ); 
                         //copied &= copiedAux;
                         //if (!copiedAux){
                         //System.out.println( "DIRECTORIO HIJO NO CREADO(MKDIRS): "+childDestiny.getAbsolutePath( ) );
                         //}
                         copied &= copyAllTo( childOrigin, childDestiny );
                     }
                     else {
                         File childDestiny = new File( destiny.getAbsolutePath( ), childOrigin.getName( ) );
                         boolean copiedAux = copyTo( childOrigin, childDestiny );
                         copied &= copiedAux;
                         if( !copiedAux ) {
                             //System.out.println( "ARCHIVO NO COPIADO: "+childOrigin.getAbsolutePath( ) + "--->"
                             //      +childDestiny.getAbsolutePath( ) );
                         }
                     }
                 }
             }
 
         }
         catch( Exception e ) {
             copied = false;
             //System.out.println("QUE EXCEPCIN MS CHUNGA!");
             //Controller.getInstance().showErrorDialog( TextConstants.getText("Error.Title"), TextConstants.getText("Error.CopyingFiles", new String[]{origin.getAbsolutePath( ), destiny.getName( )}) );
         }
 
         return copied;
     }
 
     public boolean copyAllTo( java.io.File destiny ) {
 
         return copyAllTo( this, destiny );
     }
 
     public boolean create( ) {
 
         boolean created = false;
         if( getParentFile( ) != null && !this.getParentFile( ).exists( ) ) {
             getParentFile( ).mkdirs( );
         }
         if( !getName( ).contains( "." ) && !exists( ) ) {
             created = mkdirs( );
         }
         else if( !exists( ) ) {
             try {
                 created = this.createNewFile( );
             }
             catch( IOException e ) {
             }
         }
         return created;
     }
 
     @Override
     public File[] listFiles( ) {
 
         create( );
 
         java.io.File[] files = super.listFiles( );
         File[] filesConverted = new File[ files.length ];
         for( int i = 0; i < files.length; i++ ) {
             filesConverted[i] = new File( files[i].getAbsolutePath( ) );
         }
         return filesConverted;
     }
 
     @Override
     public File[] listFiles( FileFilter filter ) {
 
         create( );
 
         java.io.File[] files = super.listFiles( filter );
         File[] filesConverted = new File[ files.length ];
         for( int i = 0; i < files.length; i++ ) {
             filesConverted[i] = new File( files[i].getAbsolutePath( ) );
         }
         return filesConverted;
     }
 
     public boolean deleteAll( ) {
 
         boolean deleted = true;
         if( this.isDirectory( ) ) {
             for( File child : this.listFiles( ) ) {
                 if( child.isDirectory( ) ) {
                     deleted &= child.deleteAll( );
                 }
                 else {
                     deleted &= child.delete( );
                 }
             }
             deleted &= this.delete( );
         }
         return deleted;
     }
 
     ////////////////// ZIP AND JAR METHODS
     public static void zipDirectory( String temp, String zipFile ) {
 
         try {
             //create a ZipOutputStream to zip the data to 
             ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipFile ) );
             zipDir( temp, "", zos );
 
             zos.close( );
         }
         catch( Exception e ) {
             //handle exception 
         }
     }
 
     public static void zipDir( String dirOrigen, String relPath, ZipOutputStream zos ) {
 
         try {
             java.io.File zipDir = new java.io.File( dirOrigen );
             String[] dirList = zipDir.list( );
             //byte[] readBuffer = new byte[2156];
 
             for( int i = 0; i < dirList.length; i++ ) {
                 java.io.File f = new java.io.File( zipDir, dirList[i] );
                 
                 if( f.isDirectory( ) ) {
                     String filePath = f.getAbsolutePath( );
 
                     if( relPath != null && !relPath.equals( "" ) )
                         zipDir( filePath, relPath + "/" + f.getName( ), zos );
                     else
                         zipDir( filePath, f.getName( ), zos );
                     //loop again 
                     continue;
                 }
 
                 FileInputStream fis = new FileInputStream( f );
 
                 // Take the path of the file relative to the dirOrigen
                 String entryName = f.getName( );
                 if( relPath != null && !relPath.equals( "" ) ) {
                     entryName = relPath + "/" + entryName;
                 }
 
                 ZipEntry anEntry = new ZipEntry( entryName );
                 //anEntry.setSize( f.length( ) );
                 //anEntry.setTime( f.lastModified( ) );
                 
                 // Write the file into the ZIP. It is surrounded by a try-catch block to allow the loop to continue if the file
                 // cannot be written (Otherwise the external try-catch will capture the exception and no more files in the directory
                 // would be put into the ZIP
                 try {
                     zos.putNextEntry( anEntry );
                     byte[] readBuffer = new byte[ 1024 ];
                     int bytesIn = 0;
                     while( ( bytesIn = fis.read( readBuffer ) ) != -1 ) {
                         zos.write( readBuffer, 0, bytesIn );
                     }                    
                 } catch (ZipException zipException){
                     
                 }
  
                 //close the Stream 
                 fis.close( );
                 zos.closeEntry( );
             }
         }
         catch( Exception e ) {
             //handle exception 
         }
     }
     
     public static boolean importEadventureJar(String zipFile, String destiny){
         
         try{
           FileInputStream fis;
           fis = new FileInputStream( zipFile);
          return importEadventureJar(fis, destiny);
         }catch (FileNotFoundException e){
             
         }
         return false;
       }
       
      
     private static boolean isEadJar(Manifest man, JarInputStream jis){
         if (man==null){
             JarEntry entry = null;
             try {
                 while( ( entry = jis.getNextJarEntry( ) ) != null ) {
                     if (isFileToUnzip(entry.getName( )))
                        return true;      
                 }
 
             }
             catch( FileNotFoundException e ) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch( IOException e ) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }  
             
             return false;
         } else {
             Attributes atr = man.getMainAttributes();
             return (atr!=null&&atr.getValue("Main-Class")!=null&&atr.getValue("Main-Class").contains( JAR_CHECKER ));
         
         }
         
     }
     
        private static boolean importEadventureJar(FileInputStream fis, String destiny){
         try{
         CheckedInputStream checksum = new CheckedInputStream( fis, new Adler32( ) );
            JarInputStream jis = new JarInputStream( new BufferedInputStream( checksum ) );
            Manifest man = jis.getManifest();
            
          if (isEadJar(man, jis)){
              unzipDir( jis, destiny, true);
              
              
              
              return true;
         }else  {
             jis.close( );
             return false;
         }
              
         }catch( IOException e ) {
               
               e.printStackTrace();
           }
         return false; 
       }
       
       public static boolean importEadventureLO(String zipFile, String destiny){
         boolean eadLO=false;
         boolean lo = false;
         FileInputStream fis=null;
         java.io.File newFile=null;
         try{
           final int BUFFER = 2048;
           fis = new FileInputStream( zipFile);
           CheckedInputStream checksum = new CheckedInputStream( fis, new Adler32( ) );
           ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum)); 
           ZipEntry entry = null;
           while( ( entry = zis.getNextEntry( ) ) != null ) {
               String name = entry.getName().substring(0,entry.getName().indexOf("."));
               if (entry.getName( ).contains( LO_CHECKER ))
                   lo = true;
               else if (entry.getName( ).endsWith( EAD_LO_CHECKER )){
                   eadLO = true;
                   name = entry.getName().substring(0,entry.getName().indexOf("."));
                   if (name.contains( "/" ))
                       name = name.substring( name.lastIndexOf( "/" )+1);
                   newFile = java.io.File.createTempFile(name , ".jar");
                   FileOutputStream fos = new FileOutputStream( newFile );
                   BufferedOutputStream dest = new BufferedOutputStream( fos, BUFFER );
                   int count;
                   byte data[] = new byte[ BUFFER ];
                   while( ( count = zis.read( data, 0, BUFFER) ) != -1 ) {
                       dest.write( data, 0, count );
                   }
                   dest.flush( );
                   dest.close( );
                  newFile.deleteOnExit();
               } // to look for .jar file in webCT packages
               else{
                 /*  name = entry.getName().substring(0,entry.getName().indexOf("."));
                   newFile = java.io.File.createTempFile(name, null );
                   if (newFile.isDirectory( )){
                       if (newFile.getName( ).equals( "CMD_6988980_M" )){
                           
                       }
                       
                   }*/
               }
                   
           }
           
           return eadLO&&lo&&importEadventureJar(new FileInputStream(newFile),destiny);
         }catch (FileNotFoundException e){
             
         }
         catch( IOException e ) {
           
           e.printStackTrace();
       }
         return false;
           
       }
       
     
     
     public static boolean isFileToUnzip(String name){
         
         if (name.startsWith(CHAPTER)&&name.contains(CHAPTER_SUFFIX))
             return true;
         else if (name.startsWith(ASSETS)||name.startsWith(GUI)||name.startsWith(ADAPT)||name.startsWith(ASSESS))
             return true;
         else if (name.contains(DTD_SUFFIX))
             return true;
         else if (name.equals(DESCRIPTOR))
             return true;
         else
             return false;
     }
     
     public static void unzipDir( String zipFile, String destinyDir ) {
         
         try {
             FileInputStream fis = new FileInputStream(zipFile);
             CheckedInputStream checksum = new CheckedInputStream( fis, new Adler32( ) );
             JarInputStream jis = new JarInputStream( new BufferedInputStream( checksum ) );
             unzipDir(jis, destinyDir, false);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void unzipDir( JarInputStream jis, String destinyDir, boolean importFiles) {
 
         try {
             final int BUFFER = 2048;
             BufferedOutputStream dest = null;
             JarEntry entry = null;
             while( ( entry = jis.getNextJarEntry( ) ) != null ) {
                 boolean unzip;
                 if (importFiles&&isFileToUnzip(entry.getName())||!importFiles)
                     unzip = true;
                 else 
                     unzip = false;
                 if (unzip){
                 int count;
                 byte data[] = new byte[ BUFFER ];
                 // write the files to the disk
                 File newFile = new File( destinyDir, entry.getName( ) );
                 newFile.create( );
                 if( !newFile.isDirectory( ) ) {
                     FileOutputStream fos = new FileOutputStream( newFile );
                     dest = new BufferedOutputStream( fos, BUFFER );
                     while( ( count = jis.read( data, 0, BUFFER ) ) != -1 ) {
                         dest.write( data, 0, count );
                     }
                     dest.flush( );
                     dest.close( );
                     
                 } 
                 }
             }
             
             jis.close( );
             //System.out.println("Checksum: "+checksum.getChecksum().getValue());
         }
         catch( Exception e ) {
          //   ReportDialog.GenerateErrorReport( e, true, "UNKNOWERROR" );
         }
     }
 
     /**
      * Merges the contents of a zip file and a directory to a resultant zip
      * file. It is used when exporting (standalone and LO versions)
      * 
      * @param originZipFile
      *            Complete path of the origin zip file (source 1)
      * @param originDir
      *            Complete path of the origin dir (source 2)
      * @param destinyJarFile
      *            Complete path of the destiny zip file (product)
      */
     public static void mergeZipAndDirToJar( ZipOutputStream zos, String originZipFile, String ... originDirs ) {
         try {
             FileInputStream fis = new FileInputStream( originZipFile );
             CheckedInputStream checksum = new CheckedInputStream( fis, new Adler32( ) );
             ZipInputStream zis = new ZipInputStream( new BufferedInputStream( checksum ) );
             ZipEntry entry = null;
 
             // Write the contents of the origin zip file to the destiny jar file
             while( ( entry = zis.getNextEntry( ) ) != null ) {
                 //System.out.println("Extracting: " +entry);
                 // write the files to the disk
                 JarEntry newEntry = new JarEntry( entry.getName( ) );
 
                 zos.putNextEntry( newEntry );
                 byte[] readBuffer = new byte[ 1024 ];
                 int bytesIn = 0;
                 while( ( bytesIn = zis.read( readBuffer ) ) != -1 ) {
                     zos.write( readBuffer, 0, bytesIn );
                 }
                 //close the Stream
                 zos.closeEntry( );
             }
             zis.close( );
 
             // Write the contents of the origin dir to the jar
             for (String originDir: originDirs){
                 File.zipDir( originDir, "", zos );
             }
 
         }
         catch( Exception e ) {
            ReportDialog.GenerateErrorReport( e, true, "UNKNOWERROR" );
         }
     }
 
     public static void addJarContentsToZip(String library, ZipOutputStream zos) {
         try {
             FileInputStream fis = new FileInputStream( library );
             ZipInputStream zis = new ZipInputStream( new BufferedInputStream( fis/*checksum*/ ) );
             ZipEntry entry = null;
 
             while( ( entry = zis.getNextEntry( ) ) != null ) {
                 if (!entry.getName( ).contains( "META-INF/" ) || entry.getName( ).contains( "services" )) {
                     try {
                         zos.putNextEntry( entry );
                         byte[] readBuffer = new byte[ 1024 ];
                         int bytesIn = 0;
                         while( ( bytesIn = zis.read( readBuffer ) ) != -1 ) {
                             zos.write( readBuffer, 0, bytesIn );
                         }
                         zos.closeEntry( );
                     } catch( ZipException e) {
                         //if (!e.getMessage( ).contains( "duplicate entry" ))
                           //  ReportDialog.GenerateErrorReport( e, true, "Problem adding library: " + library );
                     }
                 }
             }
             zis.close( );
         }
         catch( Exception e ) {
           //  ReportDialog.GenerateErrorReport( e, true, "Problem adding library: " + library );
         }
     }
     
     public static void addFileToZip(File file, String destiniy, ZipOutputStream zos) {
         try {
             FileInputStream fis = new FileInputStream( file );
             
             ZipEntry entry = new ZipEntry(destiniy);
             zos.putNextEntry( entry );
             byte[] readBuffer = new byte[1024];
             int bytesIn = 0;
             while((bytesIn = fis.read( readBuffer)) != -1) {
                 zos.write( readBuffer, 0, bytesIn );
             }
             zos.closeEntry();
         }
         catch( Exception e ) {
             //ReportDialog.GenerateErrorReport( e, true, "UNKNOWERROR" );
         }
     }
     
     public static String getTemporalFileFolder(){
         return System.getProperty("java.io.tmpdir");
     }
  
     /**
      * Looks in folder if file exists taking into account the upper and lower case characters
      * 
      * @param folder
      * @param file
      * @return
      */
     public boolean existsSameFile(){
         
         for (String f: new File(this.getParent( )).list( ))
             if (f.equals( this.getName( )))
                 return true;
         
         return false;
         
     }
     
     /**
      * Returns the file that match param "file" ignoring case characters. This returns the file which exits in 
      * the real directory, not the file who calls this method.
      */
     public File existsIgnoreCase(){
         for (String f: new File(this.getParent( )).list( ))
             if (f.equalsIgnoreCase( this.getName( )))
                 return new File(this.getParent( )+"\\"+f);
         return null;
     }
 }
