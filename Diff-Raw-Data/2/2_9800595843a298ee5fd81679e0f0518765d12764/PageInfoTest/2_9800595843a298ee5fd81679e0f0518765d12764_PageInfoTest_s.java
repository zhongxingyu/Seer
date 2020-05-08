 package name.pehl.taoki.rest.paging;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author $Author: lfstad-pehl $
  * @version $Date: 2009-01-12 17:31:45 +0100 (Mo, 12 Jan 2009) $ $Revision:
  *          61016 $
  */
 public class PageInfoTest
 {
     @Test
     public void testOffsetLimit()
     {
         PageInfo underTest = new PageInfo(0, 50);
         assertEquals(underTest.getOffset(), 0);
         assertEquals(underTest.getPageSize(), 50);
         assertDefaultSortInfo(underTest);
     }
 
 
     @Test
     public void testOffsetLimitSortInfo()
     {
         PageInfo underTest = new PageInfo(0, 50, new SortInfo("foo", SortDir.DESC));
         assertEquals(underTest.getOffset(), 0);
         assertEquals(underTest.getPageSize(), 50);
         assertNotNull(underTest.getSortInfo());
         assertEquals(underTest.getSortInfo().getSortField(), "foo");
         assertEquals(underTest.getSortInfo().getSortDir(), SortDir.DESC);
     }
 
 
     @Test
     public void testBounds()
     {
         PageInfo underTest = new PageInfo(PageInfo.MIN_OFFSET - 1, 50);
         assertEquals(underTest.getOffset(), PageInfo.MIN_OFFSET);
         assertEquals(underTest.getPageSize(), 50);
         assertNotNull(underTest.getSortInfo());
         assertNull(underTest.getSortInfo().getSortField());
         assertEquals(underTest.getSortInfo().getSortDir(), SortDir.NONE);
 
         underTest = new PageInfo(PageInfo.MAX_OFFSET + 1, 50);
         assertEquals(underTest.getOffset(), PageInfo.MAX_OFFSET);
         assertEquals(underTest.getPageSize(), 50);
         assertNotNull(underTest.getSortInfo());
         assertNull(underTest.getSortInfo().getSortField());
         assertTrue(underTest.getSortInfo().getSortDir() == SortDir.NONE);
 
         underTest = new PageInfo(0, PageInfo.MIN_PAGE_SIZE - 1);
         assertEquals(underTest.getOffset(), 0);
         assertEquals(underTest.getPageSize(), PageInfo.MIN_PAGE_SIZE);
         assertNotNull(underTest.getSortInfo());
         assertNull(underTest.getSortInfo().getSortField());
         assertTrue(underTest.getSortInfo().getSortDir() == SortDir.NONE);
 
         underTest = new PageInfo(0, PageInfo.MAX_PAGE_SIZE + 1);
         assertEquals(underTest.getOffset(), 0);
         assertEquals(underTest.getPageSize(), PageInfo.MAX_PAGE_SIZE);
         assertNotNull(underTest.getSortInfo());
         assertNull(underTest.getSortInfo().getSortField());
         assertTrue(underTest.getSortInfo().getSortDir() == SortDir.NONE);
     }
 
 
     @Test
     public void testPrevious()
     {
         PageInfo underTest = new PageInfo(10, 10).previous();
         assertEquals(0, underTest.getOffset());
         assertEquals(10, underTest.getPageSize());
         assertDefaultSortInfo(underTest);
 
        underTest = new PageInfo(10, 10).previous();
         assertEquals(0, underTest.getOffset());
         assertEquals(10, underTest.getPageSize());
         assertDefaultSortInfo(underTest);
     }
 
 
     @Test
     public void testNext()
     {
         PageInfo underTest = new PageInfo(0, 10).next();
         assertEquals(10, underTest.getOffset());
         assertEquals(10, underTest.getPageSize());
         assertDefaultSortInfo(underTest);
     }
 
     
     // --------------------------------------------------------- helper methods
 
     private void assertDefaultSortInfo(PageInfo pageInfo)
     {
         assertNotNull(pageInfo.getSortInfo());
         assertNull(pageInfo.getSortInfo().getSortField());
         assertEquals(pageInfo.getSortInfo().getSortDir(), SortDir.NONE);
     }
 }
