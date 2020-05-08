 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package calliope.export;
 import java.io.File;
 import java.io.IOException;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import org.xeustechnologies.jtar.*;
 
 /**
  * Make a tar.gz archive
  * @author desmond
  */
 public class TarArchive implements Compressor
 {
     /** folder to archive */
     File folder;
     static final int BUFFER = 2048;
     /**
      * Create a tar archive object
      * @param folder the folder to tar
      */
     public TarArchive( File folder )
     {
         this.folder = folder;
     }
     /**
      * Add a single folder to the tar archive recursively
      * @param parent the parent folder name or null
      * @param path the path to the folder
      * @param out the tar output streeam to write to
      * @throws IOException 
      */
     private void tarFolder( String parent, String path, TarOutputStream out ) 
         throws IOException 
     {
         BufferedInputStream origin = null;
         File f = new File( path );
         String files[] = f.list();
         if (files == null) 
         {
             files = new String[1];
             files[0] = f.getName();
         }
         parent = ( ( parent == null ) ? ( f.isFile() ) ? "" 
             : f.getName() + "/" : parent + f.getName() + "/" );
         for (int i = 0; i < files.length; i++) 
         {
             File fe = f;
             byte data[] = new byte[BUFFER];
 
             if (f.isDirectory()) {
                 fe = new File( f, files[i] );
             }
 
             if (fe.isDirectory()) {
                 String[] fl = fe.list();
                 if (fl != null && fl.length != 0)
                     tarFolder( parent, fe.getPath(), out );
                 else 
                 {
                     TarEntry entry = new TarEntry( fe, parent + files[i] + "/" );
                     out.putNextEntry( entry );
                 }
                 continue;
             }
             FileInputStream fi = new FileInputStream( fe );
             origin = new BufferedInputStream( fi );
             TarEntry entry = new TarEntry( fe, parent + files[i] );
             out.putNextEntry( entry );
             int count;
             //int bc=0;
             while ( (count=origin.read(data)) != -1 ) 
             {
                 out.write( data, 0, count );
                 //bc+=count;
             }
             //System.out.println("Wrote "+bc+" bytes from "+files[i]+" to out");
             out.flush();
             origin.close();
         }
     }
     /**
      * Actually produce the compressed tar archive
      * @return a file object representing the tar file
      * @throws Exception 
      */
     @Override
     public File compress() throws Exception
     {
        File dest = File.createTempFile("TMP",".tar.gz");
         FileOutputStream fos = new FileOutputStream( dest );
         BufferedOutputStream bos = new BufferedOutputStream(fos);
         TarOutputStream out = new TarOutputStream(bos);
         tarFolder( null, folder.getAbsolutePath(), out );
         out.close();
        return dest;
     }
 }
