 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.queue;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.TemporalType;
 import javax.persistence.TypedQuery;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.grabber.DatafileMetadata;
 import au.edu.uq.cmm.paul.grabber.DatasetMetadata;
 import au.edu.uq.cmm.paul.status.Facility;
 
 /**
  * This class is responsible for low-level management of the ingestion queue.
  * 
  * @author scrawley
  */
 public class QueueManager {
     public static class DateRange {
         private final Date fromDate;
         private final Date toDate;
         
         
         public DateRange(Date fromDate, Date toDate) {
             super();
             this.fromDate = fromDate;
             this.toDate = toDate;
         }
 
         public final Date getFromDate() {
             return fromDate;
         }
         
         public final Date getToDate() {
             return toDate;
         }
 
         @Override
         public String toString() {
             return "DateRange [fromDate=" + fromDate + ", toDate=" + toDate
                     + "]";
         }
     }
     
     public static enum Slice {
         HELD, INGESTIBLE, ALL;
     }
     
     public static enum Removal {
         DELETE, ARCHIVE, DRY_RUN
     }
     
     private static final Logger LOG = LoggerFactory.getLogger(QueueManager.class);
     private final QueueFileManager fileManager;
     private EntityManagerFactory emf;
 
     public QueueManager(Paul services) {
         this(services.getConfiguration(), services.getEntityManagerFactory());
     }
 
     public QueueManager(PaulConfiguration config, EntityManagerFactory emf) {
         this.emf = emf;
         this.fileManager = new CopyingQueueFileManager(config);
     }
 
     public List<DatasetMetadata> getSnapshot(Slice slice, String facilityName) {
         EntityManager em = createEntityManager();
         try {
             String whereClause;
             switch (slice) {
             case HELD:
                 whereClause = "where m.userName is null ";
                 break;
             case INGESTIBLE:
                 whereClause = "where m.userName is not null ";
                 break;
             default:
                 whereClause = "";
             }
             TypedQuery<DatasetMetadata> query;
             if (facilityName == null) {
                 query = em.createQuery("from DatasetMetadata m " +
                    "left join fetch m.datafiles " +
                     whereClause + "order by m.id", DatasetMetadata.class);
             } else {
                 if (whereClause.isEmpty()) {
                     whereClause = "where ";
                 } else {
                     whereClause += "and ";
                 }
                 query = em.createQuery("from DatasetMetadata m " +
                     whereClause + "facilityName = :name " +
                         "order by m.id", DatasetMetadata.class);
                 query.setParameter("name", facilityName);
             }
             List<DatasetMetadata> res = query.getResultList();
             return res;
         } finally {
             em.close();
         }
     }
 
     public List<DatasetMetadata> getSnapshot(Slice slice) {
         return getSnapshot(slice, null);
     }
 
     public DateRange getQueueDateRange(Facility facility) {
         EntityManager em = createEntityManager();
         Date fromDate, toDate;
         DateRange res;
         try {
             TypedQuery<Date> query = em.createQuery(
                     "SELECT MIN(d.captureTimestamp) FROM DatasetMetadata d " +
                             "GROUP BY d.facilityId HAVING d.facilityId = :id", 
                             Date.class);
             query.setParameter("id", facility.getId());
             fromDate = query.getSingleResult();
             query = em.createQuery(
                     "SELECT MAX(d.captureTimestamp) FROM DatasetMetadata d " +
                             "GROUP BY d.facilityId HAVING d.facilityId = :id", 
                             Date.class);
             query.setParameter("id", facility.getId());
             toDate = query.getSingleResult();
             res = new DateRange(fromDate, toDate);
         } catch (NoResultException ex) {
             res = null;
         } finally {
             em.close();
         }
         LOG.info("getQueueDateRange(" + facility.getFacilityName() + ") -> " + res);
         return res;
     }
 
     public void addEntry(DatasetMetadata dataset, boolean mayExist) 
             throws JsonGenerationException, IOException, 
                 QueueFileException, InterruptedException {
         saveToFileSystem(new File(dataset.getMetadataFilePathname()), 
                     dataset, mayExist);
         saveToDatabase(dataset);
     }
 
     private void saveToDatabase(DatasetMetadata dataset) {
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             if (dataset.getId() == null) {
                 em.persist(dataset);
             } else {
                 em.merge(dataset);
             }
             em.getTransaction().commit();
         } finally {
             em.close();
         }
     }
 
     private void saveToFileSystem(File metadataFile, DatasetMetadata metadata, 
                 boolean mayExist)
             throws IOException, JsonGenerationException, QueueFileException,
                 InterruptedException {
         StringWriter sw = new StringWriter();
         metadata.serialize(sw);
         fileManager.enqueueFile(sw.toString(), metadataFile, mayExist);
         LOG.info("Saved admin metadata to " + metadataFile);
     }
 
     public int expireAll(Removal removal, String facilityName, Slice slice,
                 Date olderThan) 
             throws InterruptedException {
         // FIXME - should expiration adjust the LWM?
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             String andPart;
             switch (slice) {
             case HELD:
                 andPart = " and m.userName is null";
                 break;
             case INGESTIBLE:
                 andPart = " and m.userName is not null";
                 break;
             default:
                 andPart = "";
             }
             if (facilityName != null && !facilityName.isEmpty()) {
                 andPart += " and m.facilityName = :facility";
             }
             String queryString = "from DatasetMetadata m " +
             		"left join fetch m.datafiles " +
                     "where m.updateTimestamp < :cutoff" + andPart;
             TypedQuery<DatasetMetadata> query = 
                     em.createQuery(queryString, DatasetMetadata.class);
             query.setParameter("cutoff", olderThan, TemporalType.TIMESTAMP);
             if (facilityName != null && !facilityName.isEmpty()) {
                 query.setParameter("facility", facilityName);
             }
             List<DatasetMetadata> datasets = query.getResultList();
             for (DatasetMetadata dataset : datasets) {
                 doDelete(removal, em, dataset);
             }
             em.getTransaction().commit();
             return datasets.size();
         } finally {
             em.close();
         }
     }
 
     public int deleteAll(Removal removal, String facilityName, Slice slice) 
             throws InterruptedException {
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             String whereClause;
             switch (slice) {
             case HELD:
                 whereClause = " where m.userName is null";
                 break;
             case INGESTIBLE:
                 whereClause = " where m.userName is not null";
                 break;
             default:
                 whereClause = "";
             }
             if (facilityName != null && !facilityName.isEmpty()) {
                 if (whereClause.isEmpty()) {
                     whereClause = " where ";
                 } else {
                     whereClause += " and ";
                 }
                 whereClause += "m.facilityName = :facility";
             }
             TypedQuery<DatasetMetadata> query = em.createQuery(
                     "from DatasetMetadata m " +
                     "left join fetch m.datafiles " + whereClause, 
                     DatasetMetadata.class);
             query.setParameter("facility", facilityName);
             List<DatasetMetadata> datasets = query.getResultList();
             for (DatasetMetadata dataset : datasets) {
                 doDelete(removal, em, dataset);
             }
             em.getTransaction().commit();
             return datasets.size();
         } finally {
             em.close();
         }
     }
     
     public int delete(String[] ids, Removal removal) 
             throws InterruptedException {
         int count = 0;
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             for (String idStr : ids) {
                 long id = Long.parseLong(idStr);
                 TypedQuery<DatasetMetadata> query =
                         em.createQuery("from DatasetMetadata m " +
                         		"left join fetch m.datafiles where m.id = :id", 
                                 DatasetMetadata.class);
                 query.setParameter("id", id);
                 List<DatasetMetadata> datasets = query.getResultList();
                 for (DatasetMetadata dataset : datasets) {
                     doDelete(removal, em, dataset);
                     count++;
                 }
             }
             em.getTransaction().commit();
         } finally {
             em.close();
         }
         return count;
     }
 
     private void doDelete(Removal removal, EntityManager entityManager,
             DatasetMetadata dataset) throws InterruptedException {
         // FIXME - should we do the file removal after committing the
         // database update?
         for (DatafileMetadata datafile : dataset.getDatafiles()) {
             disposeOfFile(datafile.getCapturedFilePathname(), removal);
         }
         disposeOfFile(dataset.getMetadataFilePathname(), removal);
         switch (removal) {
         case DELETE:
         case ARCHIVE:
             entityManager.remove(dataset);
             break;
         default:
             LOG.debug("Dry run: would have removed record for dataset " + 
                     dataset.getId());
         }
     }
 
     private void disposeOfFile(String pathname, Removal removal) 
             throws InterruptedException {
         File file = new File(pathname);
         try {
             switch (removal) {
             case DELETE: 
                 fileManager.removeFile(file);
                 break;
             case ARCHIVE:
                 fileManager.archiveFile(file);
                 break;
             default:
                 LOG.debug("Dry run: would have disposed of file " + file);
             }
         } catch (QueueFileException ex) {
             LOG.warn("Problem disposing of file", ex);
         }
     }
     
     public DatasetMetadata fetchDataset(long id) {
         EntityManager entityManager = createEntityManager();
         try {
             TypedQuery<DatasetMetadata> query = entityManager.createQuery(
                     "from DatasetMetadata d where d.id = :id", 
                     DatasetMetadata.class);
             query.setParameter("id", id);
             return query.getSingleResult();
         } catch (NoResultException ex) {
             return null;
         } finally {
             entityManager.close();
         }
     }
 
     public int changeUser(String[] ids, String userName, boolean reassign) 
             throws JsonGenerationException, IOException, QueueFileException, 
                 InterruptedException {
         EntityManager em = createEntityManager();
         int nosChanged = 0;
         try {
             em.getTransaction().begin();
             TypedQuery<DatasetMetadata> query =
                     em.createQuery("from DatasetMetadata d where d.id = :id", 
                     DatasetMetadata.class);
             for (String idstr : ids) {
                 query.setParameter("id", new Long(idstr));
                 DatasetMetadata dataset = query.getSingleResult();
                 if (reassign || dataset.getUserName() == null) {
                     dataset.setUserName(userName.isEmpty() ? null : userName);
                     dataset.setUpdateTimestamp(new Date());
                     saveToFileSystem(new File(dataset.getMetadataFilePathname()),
                                 dataset, true);
                     nosChanged++;
                 }
             }
             em.getTransaction().commit();
         } catch (NoResultException ex) {
             LOG.info("Records not found", ex);
         } finally {
             em.close();
         }
         return nosChanged;
     }
     
     private EntityManager createEntityManager() {
         return emf.createEntityManager();
     }
 
     public QueueFileManager getFileManager() {
         return fileManager;
     }
 }
