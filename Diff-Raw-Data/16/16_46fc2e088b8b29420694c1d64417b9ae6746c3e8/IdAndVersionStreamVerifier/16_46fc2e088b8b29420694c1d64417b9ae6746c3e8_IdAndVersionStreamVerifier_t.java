 package com.aconex.scrutineer;
 
 import org.apache.log4j.Logger;
 
 import java.util.Iterator;
 
 public class IdAndVersionStreamVerifier {
 
     private static final Logger LOG = LogUtils.loggerForThisClass();
 
    @SuppressWarnings("PMD.NcssMethodCount")
     public void verify(IdAndVersionStream primaryStream, IdAndVersionStream secondayStream, IdAndVersionStreamVerifierListener idAndVersionStreamVerifierListener) {
         try {
             primaryStream.open();
             secondayStream.open();
             
             Iterator<IdAndVersion> primaryIterator = primaryStream.iterator();
             Iterator<IdAndVersion> secondaryIterator = secondayStream.iterator();
             
             while (primaryIterator.hasNext() && secondaryIterator.hasNext()) {
                 compareStreams(idAndVersionStreamVerifierListener, primaryIterator, secondaryIterator);
             }
 
             logDifferencesAtEndOfStreams(idAndVersionStreamVerifierListener, primaryIterator, secondaryIterator);
         }
         finally {
             closeWithoutThrowingException(primaryStream);
             closeWithoutThrowingException(secondayStream);
         }
     }
 
     private void closeWithoutThrowingException(IdAndVersionStream idAndVersionStream) {
         try {
             idAndVersionStream.close();
         }
         catch(Exception e) {
             LogUtils.warn(LOG,"Unable to close IdAndVersionStream",e);
         }
     }
 
     private void compareStreams(IdAndVersionStreamVerifierListener idAndVersionStreamVerifierListener, Iterator<IdAndVersion> primaryIterator, Iterator<IdAndVersion> secondaryIterator) {
         
         IdAndVersion primaryItem = primaryIterator.next();
         IdAndVersion secondaryItem = secondaryIterator.next();
 
         if(!primaryItem.equals(secondaryItem)) {
             fireEventForMisMatchedItems(idAndVersionStreamVerifierListener, primaryIterator, secondaryIterator, primaryItem, secondaryItem);
         }
     }
 
    @SuppressWarnings("PMD.NcssMethodCount")
     private void fireEventForMisMatchedItems(IdAndVersionStreamVerifierListener idAndVersionStreamVerifierListener, Iterator<IdAndVersion> primaryIterator, Iterator<IdAndVersion> secondaryIterator, IdAndVersion primaryItem, IdAndVersion secondaryItem) {
         if (primaryItem.getId().equals(secondaryItem.getId())) {
             idAndVersionStreamVerifierListener.onVersionMisMatch(primaryItem, secondaryItem);
         }
         else if (primaryItem.compareTo(secondaryItem) < 0) {
             idAndVersionStreamVerifierListener.onMissingInSecondaryStream(primaryItem);
             primaryIterator.next();
         }
         else {
             idAndVersionStreamVerifierListener.onMissingInPrimaryStream(secondaryItem);
             secondaryIterator.next();
         }
     }
 
     private void logDifferencesAtEndOfStreams(IdAndVersionStreamVerifierListener idAndVersionStreamVerifierListener, Iterator<IdAndVersion> primaryIterator, Iterator<IdAndVersion> secondaryIterator) {
 
         while (primaryIterator.hasNext()) {
             idAndVersionStreamVerifierListener.onMissingInSecondaryStream(primaryIterator.next());
         }
 
         while (secondaryIterator.hasNext()) {
             idAndVersionStreamVerifierListener.onMissingInPrimaryStream(secondaryIterator.next());
         }
     }
 }
