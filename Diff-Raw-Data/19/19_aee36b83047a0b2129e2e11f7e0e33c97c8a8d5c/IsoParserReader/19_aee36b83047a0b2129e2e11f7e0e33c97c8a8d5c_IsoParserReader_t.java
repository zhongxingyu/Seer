 package com.lucastheisen.autotagger.tag.mp4parser;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import com.coremedia.iso.IsoFile;
 import com.coremedia.iso.boxes.Box;
 import com.coremedia.iso.boxes.ContainerBox;
 import com.coremedia.iso.boxes.MovieBox;
 import com.coremedia.iso.boxes.UserDataBox;
 import com.lucastheisen.autotagger.tag.Reader;
 import com.lucastheisen.autotagger.tag.TagInfo;
 
 
 /**
  * Uses ISO parser provided by the mp4parser project.
  * 
  * Details on fields can be found <a
  * href="http://atomicparsley.sourceforge.net/mpeg-4files.html">here</a>.
  * 
  * @author ltheisen
  */
 public class IsoParserReader implements Reader {
     private static Logger log = LoggerFactory.getLogger( IsoParserReader.class );
 
     private TypeSafeParserMap parserMap;
 
     @Override
     public TagInfo read( File file ) {
         TagInfo tagInfo = new TagInfo();
         try ( FileInputStream fileInputStream = new FileInputStream( file ) ;
                 FileChannel fileChannel = fileInputStream.getChannel() ;) {
             IsoFile isoFile = new IsoFile( fileChannel );
             UserDataBox userDataBox = isoFile.getBoxes( MovieBox.class ).get( 0 ).getBoxes( UserDataBox.class ).get( 0 );
             recurse( userDataBox, tagInfo );
         }
         catch ( IOException e ) {
             log.error( "Unable to parse {}: {}", file, e );
         }
 
         return tagInfo;
     }
     
    private <T extends Box> void parse( Class<T> clazz, Box box, TagInfo tagInfo ) {
        parserMap.get( clazz ).parse( clazz.cast( box ), tagInfo );
    }
    
     public void recurse( ContainerBox containerBox, TagInfo tagInfo ) {
         for ( Box box : containerBox.getBoxes() ) {
             Class<? extends Box> clazz = box.getClass();
             if ( parserMap.containsKey( clazz ) ) {
                parse( clazz, containerBox, tagInfo );
             }
             else if ( box instanceof ContainerBox ) {
                 recurse( containerBox, tagInfo );
             }
         }
     }
     
 }
