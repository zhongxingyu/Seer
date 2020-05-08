 package ligangty.common.pagination.web.tag;
 
 import ligangty.common.pagination.PageBean;
 
 import org.junit.Test;
import static org.junit.Assert.*;
 
 /**
  * @author: gli Date: 6/3/13 Time: 3:36 PM
  */
 public class ItemsStylePageGenStrategyTest {
     private ItemsStylePageGenStrategy isStra = new ItemsStylePageGenStrategy();
 
     @Test
     public void testGeneratePaging() {
         PageBean bean = new PageBean();
         bean.setTotalRecords(1000L);
         bean.setCurrentPage(2);
         bean.setTotalPage(20);
         bean.setPageSize(50);
         bean.addSearchingParams("param", "paramValue");
        String result = isStra.generatePaging(bean, 5, "action", "testClass");
        assertTrue(result.contains("<div class=\"testClass\">"));
        assertTrue(result.contains("<form action=\"action\">"));
        assertTrue(result
                .contains("<a href=\"#\" onclick=\"this.parentNode.page.value=1;this.parentNode.submit()\"><span>first</span></a>"));
        assertTrue(result
                .contains("<a href=\"#\" onclick=\"this.parentNode.page.value=1;this.parentNode.submit()\"><span>previous</span></a>"));
        assertTrue(result
                .contains("<a href=\"#\" onclick=\"this.parentNode.page.value=1;this.parentNode.submit()\"><span>1</span></a>"));
        assertTrue(result.contains("<strong>2</strong>"));
        assertTrue(result
                .contains("<a href=\"#\" onclick=\"this.parentNode.page.value=5;this.parentNode.submit()\"><span>5</span></a>"));
        assertTrue(result
                .contains("<a href=\"#\" onclick=\"this.parentNode.page.value=3;this.parentNode.submit()\"><span>next</span></a>"));
        assertTrue(result
                .contains("<a href=\"#\" onclick=\"this.parentNode.page.value=20;this.parentNode.submit()\"><span>last</span></a>"));

     }
 }
