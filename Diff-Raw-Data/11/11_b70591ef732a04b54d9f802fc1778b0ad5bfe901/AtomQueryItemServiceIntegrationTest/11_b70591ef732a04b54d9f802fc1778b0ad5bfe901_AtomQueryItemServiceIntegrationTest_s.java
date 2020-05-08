 package com.abudko.reseller.huuto.query.service.item.atom;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring/test-app-config.xml" })
 public class AtomQueryItemServiceIntegrationTest {
 
     private static final String ITEM_URL = "haalari--koko-104cm---peuhu--ulkohaalari/290890172";
 
     @Autowired
     @Qualifier("atomQueryItemServiceImpl")
     private QueryItemService atomQueryItemService;
 
     private ItemResponse response;
 
     @Before
     public void setup() {
         response = atomQueryItemService.extractItem(ITEM_URL);
     }
 
     @Test
     public void testItemResponseSeller() {
         assertEquals("gabca123", response.getSeller());
     }
 
     @Test
     public void testItemResponseLocation() {
         assertEquals("Espoo", response.getLocation());
     }
 
     @Test
     public void testItemResponseCondition() {
         assertEquals("NOT_DEFINED", response.getCondition());
     }
 
     @Test
     public void testItemResponsePrice() {
         assertEquals("39.00", response.getPrice());
     }
 
     @Test
     public void testItemResponseImgSrc() {
         assertEquals("http://kuvat2.huuto.net/2/4d/be5f666c8903ae0cc2fdb75bb10f6", response.getImgBaseSrc());
     }
     
     @Test
     public void testItemResponseID() {
         assertEquals("290890172", response.getId());
     }
 
     @Test
     public void testItemResponseItemStatus() {
        assertEquals("open", response.getItemStatus());
     }
 
     @Test
     public void testParseImgBaseSrcWithoutSuffix() {
         assertFalse("Src: " + response.getImgBaseSrc(), response.getImgBaseSrc().contains(".jpg"));
     }
 
 }
