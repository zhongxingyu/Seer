 package ligangty.common.pagination.web.tag;
 
 import ligangty.common.pagination.PageBean;
 
 import org.junit.Test;
 
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
        String result = isStra.generatePaging(bean, 5, "", "");
     }
 }
