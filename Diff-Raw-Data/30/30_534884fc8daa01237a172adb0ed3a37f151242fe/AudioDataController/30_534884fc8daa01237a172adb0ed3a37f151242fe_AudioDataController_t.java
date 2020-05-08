 package com.company.annotation.audio.web.controller;
 
 import com.company.annotation.audio.api.IPersistenceEngine;
 import com.company.annotation.audio.pojos.*;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

import static org.apache.commons.io.IOUtils.copy;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tbeauvais
  * Date: 7/4/12
  * Time: 1:02 AM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/audioData")
 public class AudioDataController extends DefaultSpringController {
 
     @Autowired
     public IPersistenceEngine persistenceEngine;
 
     private static Logger logger = Logger.getLogger( "com.company.annotation.audio" );
    private static final int BUFF_SIZE = 4096;
 
     @RequestMapping( value = "/music", method = RequestMethod.GET )
     public ModelAndView showIndexFiles( ) {
         return new ModelAndView( "music" );
     }
 
     @RequestMapping( method = RequestMethod.GET )
     @Transactional
    public void doGet( @RequestParam String uidIndexFile, @RequestParam(defaultValue = "0" ) int startPos, HttpServletResponse response ) throws Exception {
         final IndexSummary indexSummary     = persistenceEngine.load( uidIndexFile, IndexSummary.class );
 
         final AudioFile audioFile           = persistenceEngine.load( indexSummary.getAudioFileUid(), AudioFile.class );
 
         final byte[] bytes                  = audioFile.getBytes();
 
        final int len                       = bytes.length;

         response.setContentType( "audio/mpeg" );
        response.setContentLength( len );

        copy( new ByteArrayInputStream( bytes ), response.getOutputStream() );
 
 
        // ATTEMPT ONE
//        for ( int i = startPos; i < bytes.length; i+= BUFF_SIZE ) {
//            final int length = Math.min( i + BUFF_SIZE, bytes.length - 1 );
//            final byte[] writeBuffer = Arrays.copyOfRange( bytes, i, i + length );
//            response.getOutputStream().write( writeBuffer );
//            response.getOutputStream().flush();
//        }

        // LOAD FROM A FILE
 //        final File song = new File( "/home/tbeauvais/Development/personal/Media-Markup/server/data/songs/test-file-large.mp3" );
 //        final InputStream inputStream = new FileInputStream( song );
 //
 //        response.setContentType( "audio/mpeg" );
 //        response.setContentLength( inputStream.available() );
 //
 //        final long skip = startPos;
 //
 //        byte[] buff = new byte[ 4000 ];
 //        int read = -1;
 //        long count = 0;
 //        while ( (read = inputStream.read(buff) ) > -1 ) {
 //            count += read;
 //            if ( count > skip ) {
 //                response.getOutputStream().write( buff, 0, read );
 //                response.flushBuffer();
 //            }
 //        }
     }
 }
