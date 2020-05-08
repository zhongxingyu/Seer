 package com.orangeleap.tangerine.test.dao.ibatis;
 
 import com.orangeleap.tangerine.dao.PostBatchDao;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.PostBatchEntry;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import junit.framework.Assert;
 import org.apache.commons.logging.Log;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 public class IBatisPostBatchDaoTest extends AbstractIBatisTest {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     private PostBatchDao postBatchDao;
 
     @BeforeMethod
     public void setup() {
         postBatchDao = (PostBatchDao)super.applicationContext.getBean("postBatchDAO");
     }
 
     @Test(groups = { "testMaintainPostBatch" }, dependsOnGroups = { "testReadPostBatch" })
     public void testMaintainPostBatch() throws Exception {
         // Insert
         PostBatch batch = new PostBatch();
         batch.setBatchCreatedById(100L);
         batch.setBatchCreatedDate(new Date());
         batch.setBatchType("gift");
         batch.setBatchDesc("This is my gift batch");
         batch.setSiteName("company1");
 
         PostBatchEntry entry = new PostBatchEntry();
         entry.setSegmentationId(550L);
         batch.addPostBatchEntry(entry);
 
         entry = new PostBatchEntry();
         entry.setSegmentationId(650L);
         batch.addPostBatchEntry(entry);
 
         batch.addUpdateField("postedDate", "12/10/2010 00:00:00");
         batch.addUpdateField("giftStatus", "Not Paid");
         batch.addUpdateField("customFieldMap[source]", "Mail");
 
         batch = postBatchDao.maintainPostBatch(batch);
         Assert.assertTrue(batch.getId() > 0);
 
         PostBatch readBatch = postBatchDao.readPostBatchById(batch.getId());
         Assert.assertNotNull(readBatch);
         Assert.assertEquals(batch.getId(), readBatch.getId());
         Assert.assertEquals("This is my gift batch", readBatch.getBatchDesc());
         Assert.assertEquals("gift", readBatch.getBatchType());
         Assert.assertEquals(new Long(100L), readBatch.getBatchCreatedById());
         Assert.assertNotNull(readBatch.getBatchCreatedDate());
         Assert.assertEquals("company1", readBatch.getSiteName());
         Assert.assertFalse(readBatch.isExecuted());
         Assert.assertNull(readBatch.getExecutedById());
         Assert.assertNull(readBatch.getExecutedDate());
         Assert.assertFalse(readBatch.isPosted());
         Assert.assertNull(readBatch.getPostedById());
         Assert.assertNull(readBatch.getPostedDate());
         Assert.assertNotNull(readBatch.getCreateDate());
         Assert.assertNotNull(readBatch.getUpdateDate());
         Assert.assertNotNull(readBatch.getPostBatchEntries());
         Assert.assertEquals(2, readBatch.getPostBatchEntries().size());
         for (PostBatchEntry thisEntry : readBatch.getPostBatchEntries()) {
             Assert.assertEquals(batch.getId(), thisEntry.getPostBatchId());
             Assert.assertTrue(thisEntry.getSegmentationId() == 550L || thisEntry.getSegmentationId() == 650L);
         }
 
         Assert.assertNotNull(readBatch.getUpdateFields());
         Assert.assertEquals(3, readBatch.getUpdateFields().size());
         Assert.assertEquals("12/10/2010 00:00:00", readBatch.getUpdateFieldValue("postedDate"));
         Assert.assertEquals("Not Paid", readBatch.getUpdateFieldValue("giftStatus"));
         Assert.assertEquals("Mail", readBatch.getUpdateFieldValue("customFieldMap[source]"));
     }
 
     @Test(groups = { "testReadPostBatch" })
     public void testReadPostBatchById() throws Exception {
         PostBatch batch = postBatchDao.readPostBatchById(0L);
         Assert.assertNull(batch);
 
         batch = postBatchDao.readPostBatchById(1L);
         Assert.assertNotNull(batch);
         Assert.assertEquals(new Long(1L), batch.getId());
         Assert.assertEquals("Batch 1", batch.getBatchDesc());
         Assert.assertEquals("gift", batch.getBatchType());
         Assert.assertTrue(batch.isExecuted());
         Assert.assertEquals(new Long(100L), batch.getExecutedById());
         Assert.assertNotNull(batch.getExecutedDate());
         Assert.assertFalse(batch.isAnErrorBatch());
         Assert.assertTrue(batch.isPosted());
         Assert.assertEquals(new Long(100L), batch.getPostedById());
         Assert.assertNotNull(batch.getPostedDate());
         Assert.assertEquals(new Long(100L), batch.getBatchCreatedById());
         Assert.assertNotNull(batch.getBatchCreatedDate());
         Assert.assertEquals("company1", batch.getSiteName());
         Assert.assertNotNull(batch.getCreateDate());
         Assert.assertNotNull(batch.getUpdateDate());
         Assert.assertNotNull(batch.getPostBatchEntries());
         Assert.assertEquals(3, batch.getPostBatchEntries().size());
         for (PostBatchEntry entry : batch.getPostBatchEntries()) {
             Assert.assertEquals(new Long(1L), entry.getPostBatchId());
             Assert.assertTrue(entry.getSegmentationId() == 100L || entry.getSegmentationId() == 200L || entry.getSegmentationId() == 300L);
         }
 
         batch = postBatchDao.readPostBatchById(2L);
         Assert.assertNotNull(batch);
         Assert.assertEquals(new Long(2L), batch.getId());
         Assert.assertEquals("Batch 2", batch.getBatchDesc());
         Assert.assertEquals("gift", batch.getBatchType());
         Assert.assertFalse(batch.isExecuted());
         Assert.assertNull(batch.getExecutedById());
         Assert.assertNull(batch.getExecutedDate());
         Assert.assertEquals(new Long(100L), batch.getBatchCreatedById());
         Assert.assertNotNull(batch.getBatchCreatedDate());
         Assert.assertFalse(batch.isPosted());
         Assert.assertTrue(batch.isAnErrorBatch());
         Assert.assertNull(batch.getPostedById());
         Assert.assertNull(batch.getPostedDate());
         Assert.assertEquals("company1", batch.getSiteName());
         Assert.assertNotNull(batch.getCreateDate());
         Assert.assertNotNull(batch.getUpdateDate());
         Assert.assertNotNull(batch.getPostBatchEntries());
         Assert.assertEquals(1, batch.getPostBatchEntries().size());
         for (PostBatchEntry entry : batch.getPostBatchEntries()) {
             Assert.assertEquals(new Long(2L), entry.getPostBatchId());
             Assert.assertTrue(entry.getSegmentationId() == 9000L);
         }
 
         batch = postBatchDao.readPostBatchById(3L);
         Assert.assertNotNull(batch);
         Assert.assertEquals(new Long(3L), batch.getId());
         Assert.assertEquals("Batch 3", batch.getBatchDesc());
         Assert.assertEquals("constituent", batch.getBatchType());
         Assert.assertTrue(batch.isExecuted());
         Assert.assertFalse(batch.isAnErrorBatch());
         Assert.assertEquals(new Long(200L), batch.getExecutedById());
         Assert.assertEquals("pablo@company1.com", batch.getExecutedByUser());
         Assert.assertEquals(new Long(100L), batch.getBatchCreatedById());
         Assert.assertNotNull(batch.getBatchCreatedDate());
         Assert.assertNotNull(batch.getPostBatchEntries());
         Assert.assertTrue(batch.getPostBatchEntries().isEmpty());
     }
 
     @Test(groups = { "testReadPostBatch" })
     public void testReadBatches() throws Exception {
         List<PostBatch> batches = postBatchDao.readBatchesByStatus(StringConstants.OPEN, "createDate", "ASC", 0, 100, Locale.getDefault());
         Assert.assertNotNull(batches);
        Assert.assertEquals(batches.size(), 1);
        Assert.assertEquals(batches.get(0).getId(), new Long(4L));
 
         Assert.assertEquals(1, postBatchDao.countBatchesByStatus(StringConstants.OPEN));
 
         batches = postBatchDao.readBatchesByStatus(StringConstants.ERRORS, "createDate", "ASC", 0, 100, Locale.getDefault());
         Assert.assertNotNull(batches);
        Assert.assertEquals(batches.size(), 1);
        Assert.assertEquals(batches.get(0).getId(), new Long(2L));
 
         Assert.assertEquals(1, postBatchDao.countBatchesByStatus(StringConstants.ERRORS));
 
         batches = postBatchDao.readBatchesByStatus(StringConstants.EXECUTED, "createDate", "ASC", 0, 100, Locale.getDefault());
         Assert.assertNotNull(batches);
        Assert.assertEquals(batches.size(), 2);
         for (PostBatch batch : batches) {
             Assert.assertTrue(1L == batch.getId() || 3L == batch.getId());
         }
 
        Assert.assertEquals(postBatchDao.countBatchesByStatus(StringConstants.EXECUTED), 2);
     }
 }
