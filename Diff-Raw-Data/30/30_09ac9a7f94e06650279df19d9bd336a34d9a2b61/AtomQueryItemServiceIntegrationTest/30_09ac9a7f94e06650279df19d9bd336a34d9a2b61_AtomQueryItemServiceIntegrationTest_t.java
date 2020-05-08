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
 import com.abudko.reseller.huuto.query.service.item.ItemStatus;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring/test-app-config.xml" })
 public class AtomQueryItemServiceIntegrationTest {
 
    private static final String ITEM_URL = "uusi-hm-silk-bend-silkkisekoite-paita-98-cm-katso/303523873";
 
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
        assertEquals("Mimmasa", response.getSeller());
     }
 
     @Test
     public void testItemResponseLocation() {
        assertEquals("Helsinki", response.getLocation());
     }
 
     @Test
     public void testItemResponseCondition() {
        assertEquals("NEW", response.getCondition());
     }
 
     @Test
     public void testItemResponsePrice() {
        assertEquals("13.50", response.getPrice());
     }
 
     @Test
     public void testItemResponseImgSrc() {
        assertEquals("http://kuvat2.huuto.net/5/e7/a893515cfb7ab3652f45b4725e5fb", response.getImgBaseSrc());
     }
     
     @Test
     public void testItemResponseID() {
        assertEquals("303523873", response.getId());
     }
 
     @Test
     public void testItemResponseItemStatus() {
        assertEquals(ItemStatus.OPENED, response.getItemStatus());
     }
 
     @Test
     public void testParseImgBaseSrcWithoutSuffix() {
         assertFalse("Src: " + response.getImgBaseSrc(), response.getImgBaseSrc().contains(".jpg"));
     }
 
 }
