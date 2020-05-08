 package com.company.annotation.audio.services;
 
 import com.company.annotation.audio.api.IPersistenceEngine;
 import com.company.annotation.audio.pojos.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * Created by IntelliJ IDEA.
  * User: tbeauvais
  * Date: 3/6/12
  * Time: 3:25 PM
  * To change this template use File | Settings | File Templates.
  */
 @Service
 public class AudioAnnotationService implements IAnnotationService {
 
     private Map<String, IndexWithSamples> cache = new HashMap<String, IndexWithSamples>();
 
     public AudioAnnotationService() {
     }
 
     private IPersistenceEngine persistenceEngine;
 
     @Autowired
     public void setPersistenceEngine( IPersistenceEngine persistenceEngine ) {
         this.persistenceEngine = persistenceEngine;
     }
 
     public IndexSummary[] loadAll() {
         return persistenceEngine.loadAll( IndexSummary.class );
     }
 
     public SampleList loadSamples( String indexFileUid ) {
         final IndexWithSamples indexSummary = persistenceEngine.load( indexFileUid , IndexWithSamples.class );
         return indexSummary.getSampleList();
     }
 
     public void save( IndexSummary indexSummary ) {
         persistenceEngine.save( indexSummary );
     }
 
     public VisualData loadVisualData( String idContentFile, VisualParameters visualParameters ) {
         final VisualData visualData = new VisualData();
 
 //        if ( cache.get( idContentFile ) == null ) {
 //            cache.put( idContentFile, (IndexWithSamples) loadIndexSummary(idContentFile));
 //        }
 
 //        final IndexWithSamples indexSummary = cache.get( idContentFile );
         final IndexWithSamples indexSummary = (IndexWithSamples) loadIndexSummary(idContentFile);
         final SampleList sampleList         = indexSummary.getSampleList();
 
        final int[] visualSamples       = new int[ visualParameters.getWidth() ];
        final long[] samplePositions    = new long[ visualParameters.getWidth() ];
 
         visualData.setVisualSamples( visualSamples );
         visualData.setVisualPositions( samplePositions );
 
         if ( sampleList != null && sampleList.getSamples() != null ) {
            int startX              = 0;

             Sample[] sampleArray    = sampleList.getSamples();
 
            int step                = sampleArray.length / visualParameters.getWidth();
             double scale            = sampleList.getMax() / visualParameters.getHeight();
 
            for ( int i = 0; i < sampleArray.length; i++, startX++ ) {
                 int max = 0;
                 int min = 0;
 
                samplePositions[ startX ]   = sampleArray[ i ].getPosition();
 
                 for ( int s = 0; s < step && i < sampleArray.length; s++, i++ ) {
                     max             = Math.max( max, sampleArray[i].getValue() );
                     min             = Math.min( min, sampleArray[i].getValue() );
                 }
 
                 int value           = (int) ((max + (min * -1) ) / scale);
                 int y               = value / 2;
 
                visualSamples[ startX ] = y;
             }
         }
 
         final List<VisualRegion> visualRegions  = new Vector<VisualRegion>();
         final List<Comment> comments            = indexSummary.getComments();
         for ( Comment comment : comments ) {
             int startX = 0;
             int endX = 0;
             for (int i = 0, samplePositionsLength = samplePositions.length; i < samplePositionsLength; i++) {
                 long samplePosition = samplePositions[i];
                 if (startX == 0 && comment.getStartPos() < samplePosition) {
                     startX = i;
                 } else if ( endX == 0 && comment.getEndPos() < samplePosition) {
                     endX = i;
                 }
             }
 
             // If the end was never smaller, then it went to the entire end.. strange.. but a possible case
             if ( endX == 0 ) {
                endX = visualParameters.getWidth();
             }
 
             visualRegions.add( new VisualRegion( comment.getUid(), startX, endX, 0, 0, 0 ) );
         }
 
         visualData.setVisualRegions( visualRegions.toArray( new VisualRegion[ 0 ] ) );
 
         return visualData;
     }
 
     public IndexSummary loadIndexSummary(String uid) {
         return persistenceEngine.load( uid, IndexSummary.class );
     }
 }
