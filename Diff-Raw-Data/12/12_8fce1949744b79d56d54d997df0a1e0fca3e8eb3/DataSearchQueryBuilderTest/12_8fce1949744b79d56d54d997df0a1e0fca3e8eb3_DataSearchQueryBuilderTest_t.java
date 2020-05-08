 package org.iplantc.core.uicommons.client.services.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.models.search.DateInterval;
 import org.iplantc.core.uicommons.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.core.uicommons.client.models.search.FileSizeRange;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.gwtmockito.GwtMockitoTestRunner;
 import com.sencha.gxt.core.client.util.DateWrapper;
 import com.sencha.gxt.core.client.util.Format;
 
 /**
  * 
  * TODO Verify that if a field in the given {@link DiskResourceQueryTemplate} is null or empty, that its
  * corresponding term will be ommitted.
  * 
  * @author jstroot
  * 
  */
 @RunWith(GwtMockitoTestRunner.class)
 public class DataSearchQueryBuilderTest {
     
     @Mock DiskResourceQueryTemplate dsf;
 
     @Before public void setUp() {
         
     } 
 
     @Test
     public void testBuildQuery() {
         final String expectedFileQuery = setFileQuery("some* file* query*", dsf);
         final String expectedModifiedWithin = setModifiedWithin(new Date(), new DateWrapper().addDays(1)
                 .asDate(), dsf);
         final String expectedCreatedWithin = setCreatedWithin(new Date(), new DateWrapper().addMonths(1)
                 .asDate(), dsf);
         final String expectedNegatedFile = setNegatedFileQuery(
                 Lists.newArrayList("term1*", "term2*", "term3*"), dsf);
        final String expectedMetadataAttributeQuery = setMetadataAttributeQuery(
                "some* metadata* query*", dsf);
        final String expectedMetadataValueQuery = setMetadataValueQuery("some* metadata* query*", dsf);
         final String expectedOwnedBy = setOwnedBy("someUser", dsf);
         final String expectedFileSizeRange = setFileSizeRange(0.1, 100.78763, dsf);
         final String expectedSharedWith = setSharedWith("some users who were shared with", dsf);
 
         String result = new DataSearchQueryBuilder(dsf).buildFullQuery();
 
         assertTrue(result.contains(expectedFileQuery));
         assertTrue(result.contains(expectedModifiedWithin));
         assertTrue(result.contains(expectedCreatedWithin));
         assertTrue(result.contains(expectedNegatedFile));
         assertTrue(result.contains(expectedMetadataAttributeQuery));
         assertTrue(result.contains(expectedMetadataValueQuery));
         assertTrue(result.contains(expectedOwnedBy));
         assertTrue(result.contains(expectedFileSizeRange));
         assertTrue(result.contains(expectedSharedWith));
     }
 
     @Test
     public void testOwnedBy() {
         final String expectedValue = setOwnedBy("someUser", dsf);
 
         String result = new DataSearchQueryBuilder(dsf).ownedBy().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test public void testCreatedWithin() {
         final String expectedValue = setCreatedWithin(new Date(), new DateWrapper().addDays(1).asDate(), dsf);
 
         String result = new DataSearchQueryBuilder(dsf).createdWithin().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
     
     @Test public void testFile() {
         final String expectedValue = setFileQuery("some* words* in* query*", dsf);
 
         String result = new DataSearchQueryBuilder(dsf).file().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test public void testFileSizeRange() {
         final String expectedValue = setFileSizeRange(1.0, 100.0, dsf);
 
         String result = new DataSearchQueryBuilder(dsf).fileSizeRange().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test
     public void testMetadataAttribute() {
        final String expectedValue = setMetadataAttributeQuery("some* metadata* to* search* for*", dsf);
 
         String result = new DataSearchQueryBuilder(dsf).metadataAttribute().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test
     public void testMetadataValue() {
        final String expectedValue = setMetadataValueQuery("some* metadata* to* search* for*", dsf);
 
         String result = new DataSearchQueryBuilder(dsf).metadataValue().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test public void testModifiedWithin() {
         final Date fromDate = new Date();
         final Date toDate = new DateWrapper(fromDate).addDays(2).asDate();
         final String expectedValue = setModifiedWithin(fromDate, toDate, dsf);
 
         String result = new DataSearchQueryBuilder(dsf).modifiedWithin().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test public void testNegatedFile() {
         final String term1 = "term1*";
         final String term2 = "term2*";
         final String term3 = "term3*";
         final ArrayList<String> newArrayList = Lists.newArrayList(term1, term2, term3);
         final String expectedValue = setNegatedFileQuery(newArrayList, dsf);
 
         String result = new DataSearchQueryBuilder(dsf).negatedFile().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     @Test public void testSharedWith() {
         final String retVal = "user that are shared with";
         final String expectedValue = setSharedWith(retVal, dsf);
 
         String result = new DataSearchQueryBuilder(dsf).sharedWith().toString();
         assertEquals(wrappedQuery(expectedValue), result);
     }
 
     /**
      * @param givenValue
      * @param drqt
      * @return the expected value
      */
     private String setOwnedBy(final String givenValue, final DiskResourceQueryTemplate drqt) {
         when(dsf.getOwnedBy()).thenReturn(givenValue);
         return "{\"nested\":{\"query\":{\"bool\":{\"must\":[{\"term\":{\"permission\":\"own\"}},{\"wildcard\":{\"user\":\""
                 + givenValue + "#*\"}}]}},\"path\":\"userPermissions\"}}";
     }
 
     /**
      * @param fromDate
      * @param toDate
      * @param drqt
      * @return the expected value
      */
     private String setCreatedWithin(final Date fromDate, final Date toDate, final DiskResourceQueryTemplate drqt) {
         DateInterval di = mock(DateInterval.class);
         when(di.getFrom()).thenReturn(fromDate);
         when(di.getTo()).thenReturn(toDate);
 
         when(dsf.getCreatedWithin()).thenReturn(di);
 
         return "{\"range\":{\"dateCreated\":{\"gte\":\"" + fromDate.getTime() / 1000 + "\",\"lte\":\""
                 + toDate.getTime() / 1000 + "\"}}}";
     }
 
     /**
      * @param givenQuery
      * @param drqt
      * @return the expected value
      */
     private String setFileQuery(final String givenQuery, final DiskResourceQueryTemplate drqt) {
         when(dsf.getFileQuery()).thenReturn(givenQuery);
         return "{\"wildcard\":{\"label\":\"" + givenQuery + "\"}}";
     }
 
     /**
      * @param min
      * @param max
      * @param drqt
      * @return the expected value
      */
     private String setFileSizeRange(final Double min, final Double max, final DiskResourceQueryTemplate drqt) {
         FileSizeRange fsr = mock(FileSizeRange.class);
         when(fsr.getMin()).thenReturn(min);
         when(fsr.getMax()).thenReturn(max);
 
         when(dsf.getFileSizeRange()).thenReturn(fsr);
         return "{\"range\":{\"fileSize\":{\"gte\":\"" + min.longValue() + "\",\"lte\":\""
                 + max.longValue() + "\"}}}";
     }
 
     /**
      * @param givenQuery
      * @param drqt
      * @return the expected value
      */
     private String setMetadataAttributeQuery(final String givenQuery, final DiskResourceQueryTemplate drqt) {
         when(dsf.getMetadataAttributeQuery()).thenReturn(givenQuery);
         return "{\"nested\":{\"query\":{\"wildcard\":{\"attribute\":\"" + givenQuery
                 + "\"}},\"path\":\"metadata\"}}";
     }
 
     /**
      * @param givenQuery
      * @param drqt
      * @return the expected value
      */
     private String setMetadataValueQuery(final String givenQuery, final DiskResourceQueryTemplate drqt) {
         when(dsf.getMetadataValueQuery()).thenReturn(givenQuery);
         return "{\"nested\":{\"query\":{\"wildcard\":{\"value\":\"" + givenQuery
                 + "\"}},\"path\":\"metadata\"}}";
     }
 
     /**
      * @param from
      * @param to
      * @param drqt
      * @return the expected value
      */
     private String setModifiedWithin(final Date from, final Date to, final DiskResourceQueryTemplate drqt) {
         DateInterval di = mock(DateInterval.class);
         when(di.getFrom()).thenReturn(from);
         when(di.getTo()).thenReturn(to);
 
         when(dsf.getModifiedWithin()).thenReturn(di);
         return "{\"range\":{\"dateModified\":{\"gte\":\"" + from.getTime() / 1000 + "\",\"lte\":\""
                 + to.getTime() / 1000 + "\"}}}";
     }
 
     /**
      * @param givenSearchTerms
      * @param drqt
      * @return the expected value
      */
     private String setNegatedFileQuery(final List<String> givenSearchTerms, final DiskResourceQueryTemplate drqt) {
         when(dsf.getNegatedFileQuery()).thenReturn(Joiner.on(" ").join(givenSearchTerms));
 
         return "{\"field\":{\"label\":\"" + "-" + Joiner.on(" -").join(givenSearchTerms) + "\"}}";
     }
 
     /**
      * @param givenValue
      * @param drqt
      * @return the expected value
      */
     private String setSharedWith(final String givenValue, final DiskResourceQueryTemplate drqt) {
         when(dsf.getSharedWith()).thenReturn(givenValue);
         return "{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"bool\":{\"must\":[{\"term\":{\"permission\":\"own\"}},{\"wildcard\":{\"user\":\"\"}}]}},\"path\":\"userPermissions\"}},{\"nested\":{\"query\":{\"wildcard\":{\"user\":\""
                 + givenValue + "#*\"}},\"path\":\"userPermissions\"}}]}}";
     }
 
     private String wrappedQuery(String query) {
         return Format.substitute("{\"bool\":{\"must\":[{0}]}}", query);
     }
 
 }
