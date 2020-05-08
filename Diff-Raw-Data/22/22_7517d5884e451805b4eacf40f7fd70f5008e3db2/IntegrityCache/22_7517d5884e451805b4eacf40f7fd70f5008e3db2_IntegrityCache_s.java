 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 
 package org.bitrepository.integrityservice.cache;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.jcs.JCS;
 import org.apache.jcs.access.exception.CacheException;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Adds caching functionality for all the simple read methods. Read operations returning set or lists of results
  * are still delegated directly to the actual model.
  */
 public class IntegrityCache implements IntegrityModel {
     private Logger log = LoggerFactory.getLogger(this.getClass());
     private final IntegrityModel integrityModel;
     private JCS missingFilesCache;
     private JCS filesCache;
     private JCS corruptFilesCache;
     private JCS collectionSizeCache;
     private JCS collectionTotalFilesCache;
     private JCS collectionLatestFileDateCache;
     /** Seconds to wait from a cache elements has been updated to the elements is reloaded from the model **/
     protected int refreshPeriodAfterDirtyMark = 5;
 
     public IntegrityCache(IntegrityModel integrityModel) {
         this.integrityModel = integrityModel;
         try {
             missingFilesCache = JCS.getInstance("MissingFilesCache");
             filesCache = JCS.getInstance("FilesCache");
             corruptFilesCache = JCS.getInstance("CorruptFilesCache");
             collectionSizeCache = JCS.getInstance("collectionSizeCache");
             collectionTotalFilesCache = JCS.getInstance("collectionTotalFilesCache");
             collectionLatestFileDateCache = JCS.getInstance("collectionLatestFileDateCache");
         } catch (CacheException ce) {
             throw new IllegalStateException("Failed to initialise caches", ce);
         }
     }
 
     @Override
     public void addFileIDs(FileIDsData data, String pillarId, String collectionId) {
         integrityModel.addFileIDs(data, pillarId, collectionId);
         markPillarsDirty(filesCache, Arrays.asList(new String[]{pillarId}), collectionId);
         markDirty(collectionSizeCache, collectionId);
         markDirty(collectionTotalFilesCache, collectionId);
         markDirty(collectionLatestFileDateCache, collectionId);
     }
 
     @Override
     public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
         integrityModel.addChecksums(data, pillarId, collectionId);
     }
 
     @Override
     public Collection<FileInfo> getFileInfos(String fileId, String collectionId) {
         return integrityModel.getFileInfos(fileId, collectionId);
     }
 
     @Override
     public Collection<String> getAllFileIDs(String collectionId) {
         return integrityModel.getAllFileIDs(collectionId);
     }
     
     @Override
     public long getNumberOfFilesInCollection(String collectionId) {
         Long numberOfFilesInCollection = (Long) collectionTotalFilesCache.get(collectionId);
         if(numberOfFilesInCollection == null) {
             numberOfFilesInCollection = integrityModel.getNumberOfFilesInCollection(collectionId);
             updateCache(collectionTotalFilesCache, collectionId, numberOfFilesInCollection);
         }
         return numberOfFilesInCollection;
     }
 
     @Override
     public long getNumberOfFiles(String pillarId, String collectionId) {
         Long numberOfFiles = (Long)filesCache.get(getCacheID(pillarId, collectionId));
         if (numberOfFiles == null) {
             numberOfFiles = integrityModel.getNumberOfFiles(pillarId, collectionId);
             updateCache(filesCache, getCacheID(pillarId, collectionId), Long.valueOf(numberOfFiles));
         }
         return numberOfFiles.longValue();
     }
 
     @Override
     public List<String> getFilesOnPillar(String pillarId, long minId, long maxId, String collectionId) {
         return integrityModel.getFilesOnPillar(pillarId, minId, maxId, collectionId);
     }
     
     @Override
     public Long getCollectionFileSize(String collectionId) {
         Long collectionSizeVal = (Long) collectionSizeCache.get(collectionId); 
         if(collectionSizeVal == null) {
             collectionSizeVal = integrityModel.getCollectionFileSize(collectionId);
             updateCache(collectionSizeCache, collectionId, collectionSizeVal);
         }
         return collectionSizeVal;
     }
 
     @Override
     public long getNumberOfMissingFiles(String pillarId, String collectionId) {
         Long numberOfMissingFiles = (Long)missingFilesCache.get(getCacheID(pillarId, collectionId));
         if (numberOfMissingFiles == null) {
             numberOfMissingFiles = integrityModel.getNumberOfMissingFiles(pillarId, collectionId);
             updateCache(missingFilesCache, getCacheID(pillarId, collectionId), Long.valueOf(numberOfMissingFiles));
         }
         return numberOfMissingFiles.longValue();
     }
 
     @Override
     public List<String> getMissingFilesAtPillar(String pillarId, long minId, long maxId, String collectionId) {
         return integrityModel.getMissingFilesAtPillar(pillarId, minId, maxId, collectionId);
     }
 
     @Override
     public long getNumberOfChecksumErrors(String pillarId, String collectionId) {
         Long numberOfCorruptFiles = (Long)corruptFilesCache.get(getCacheID(pillarId, collectionId));
         if (numberOfCorruptFiles == null) {
             numberOfCorruptFiles = integrityModel.getNumberOfChecksumErrors(pillarId, collectionId);
             updateCache(corruptFilesCache, getCacheID(pillarId, collectionId), Long.valueOf(numberOfCorruptFiles));
         }
         return numberOfCorruptFiles.longValue();
     }
 
     @Override
     public List<String> getFilesWithChecksumErrorsAtPillar(String pillarId, long minId, long maxId, String collectionId) {
         return integrityModel.getFilesWithChecksumErrorsAtPillar(pillarId, minId, maxId, collectionId);
     }
 
     @Override
     public void setFileMissing(String fileId, Collection<String> pillarIds, String collectionId) {
         integrityModel.setFileMissing(fileId, pillarIds, collectionId);
         markPillarsDirty(missingFilesCache, pillarIds, collectionId);
         markPillarsDirty(filesCache, pillarIds, collectionId);
         markDirty(collectionSizeCache, collectionId);
         markDirty(collectionTotalFilesCache, collectionId);
         markDirty(collectionLatestFileDateCache, collectionId);
     }
 
     @Override
     public void setChecksumError(String fileId, Collection<String> pillarIds, String collectionId) {
         integrityModel.setChecksumError(fileId, pillarIds, collectionId);
         markPillarsDirty(corruptFilesCache, pillarIds, collectionId);
     }
 
     @Override
     public void setChecksumAgreement(String fileId, Collection<String> pillarIds, String collectionId) {
         integrityModel.setChecksumAgreement(fileId, pillarIds, collectionId);
         markPillarsDirty(corruptFilesCache, pillarIds, collectionId);
     }
 
     @Override
     public void deleteFileIdEntry(String fileId, String collectionId) {
         try {
             missingFilesCache.clear();
             filesCache.clear();
             corruptFilesCache.clear();
             collectionSizeCache.clear();
             collectionTotalFilesCache.clear();
             collectionLatestFileDateCache.clear();
         } catch (CacheException ce) {
             log.warn("Failed to clear cache.", ce);
         }
         integrityModel.deleteFileIdEntry(fileId, collectionId);
     }
 
     @Override
     public List<String> findMissingChecksums(String collectionId) {
         return integrityModel.findMissingChecksums(collectionId);
     }
 
     @Override
     public Collection<String> findChecksumsOlderThan(Date date, String pillarID, String collectionId) {
         return integrityModel.findChecksumsOlderThan(date, pillarID, collectionId);
     }
 
     @Override
     public List<String> findMissingFiles(String collectionId) {
         return integrityModel.findMissingFiles(collectionId);
     }
 
     @Override
     public List<String> getPillarsMissingFile(String fileId, String collectionId) {
         return integrityModel.getPillarsMissingFile(fileId, collectionId);
     }
 
     @Override
     public List<String> getFilesWithInconsistentChecksums(String collectionId) {
         return integrityModel.getFilesWithInconsistentChecksums(collectionId);
     }
 
     @Override
     public void setFilesWithConsistentChecksumToValid(String collectionId) {
         try {
             corruptFilesCache.clear();
         } catch (CacheException ce) {
             log.warn("Failed to clear cache.", ce);
         }
         integrityModel.setFilesWithConsistentChecksumToValid(collectionId);
     }
 
     @Override
     public void setAllFilesToUnknownFileState(String collectionId) {
         integrityModel.setAllFilesToUnknownFileState(collectionId);
     }
 
     @Override
     public void setOldUnknownFilesToMissing(String collectionId) {
         try {
             missingFilesCache.clear();
             collectionSizeCache.clear();
             collectionTotalFilesCache.clear();
             collectionLatestFileDateCache.clear();
         } catch (CacheException ce) {
             log.warn("Failed to update cache.", ce);
         }
         integrityModel.setOldUnknownFilesToMissing(collectionId);
     }
 
     @Override 
     public Date getDateForNewestFileEntryForCollection(String collectionId) {
         Date latestDate = (Date) collectionLatestFileDateCache.get(collectionId);
         if(latestDate == null) {
            latestDate = integrityModel.getDateForNewestFileEntryForCollection(collectionId); 
            updateCache(collectionLatestFileDateCache, collectionId, latestDate);
         }
         return latestDate;
     }
     
     @Override
     public Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId) {
         return integrityModel.getDateForNewestFileEntryForPillar(pillarId, collectionId);
     }
 
     @Override
     public Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId) {
         return integrityModel.getDateForNewestChecksumEntryForPillar(pillarId, collectionId);
     }
 
     @Override
     public void close() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * Will cause the cache to return null after <code>refreshPeriodAfterDirtyMark</code> has passed, since
      * the value was updated in the cache. This will ensure that the backend model isn't read more than
      *  each <code>refreshPeriodAfterDirtyMark</code> second.
      * @param cache The cache to use.
      * @param pillarIds The pillars to mark dirty
      * @param collectionID The collection to mark dirty
      */
    private void markPillarsDirty(JCS cache, Collection<String> pillarIds, String collectionId) {
         for (String pillarID:pillarIds) {
             try {
                cache.getElementAttributes(getCacheID(pillarID, collectionId))
                         .setMaxLifeSeconds(refreshPeriodAfterDirtyMark);
             } catch (CacheException ce) {
                 log.warn("Failed to read cache.", ce);
             }
         }
     }
     
     /**
      * Will cause the cache to return null after <code>refreshPeriodAfterDirtyMark</code> has passed, since
      * the value was updated in the cache. This will ensure that the backend model isn't read more than
      *  each <code>refreshPeriodAfterDirtyMark</code> second.
      * @param cache The cache to use.
      * @param collectionID The collection to mark dirty
      */
    private void markDirty(JCS cache, String collectionId) {
         try {
            cache.getElementAttributes(collectionId).setMaxLifeSeconds(refreshPeriodAfterDirtyMark);
         } catch (CacheException ce) {
             log.warn("Failed to read cache.", ce);
         }
     }
 
     /**
      * Updates the cache with the given value.
      */
     private void updateCache(JCS cache, String cacheID, Object value) {
         try {
             cache.put(cacheID, value);
         } catch (CacheException ce) {
             log.warn("Failed to update cache.", ce);
         }
     }
 
     /**
      * Creates IDs for elemets in the cache consistently.
      *
      * Note that the current implementations is trivial but will evolve.
      */
     private String getCacheID(String pillarID, String collectionID) {
         return pillarID + "-" + collectionID;
     }
 
 
 }
