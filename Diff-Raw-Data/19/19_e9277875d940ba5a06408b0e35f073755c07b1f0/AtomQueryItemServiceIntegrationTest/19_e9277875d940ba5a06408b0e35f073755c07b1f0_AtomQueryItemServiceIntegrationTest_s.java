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
 
    private static final String ITEM_ID = "280689686";
 
     @Autowired
     @Qualifier("atomQueryItemServiceImpl")
     private QueryItemService atomQueryItemService;
 
     private ItemResponse response;
 
     @Before
     public void setup() {
        response = atomQueryItemService.extractItem(ITEM_ID);
     }
 
     @Test
     public void testItemResponseSeller() {
         assertEquals("taatatapio", response.getSeller());
     }
 
     @Test
     public void testItemResponseLocation() {
         assertEquals("Vantaa", response.getLocation());
     }
 
     @Test
     public void testItemResponseCondition() {
         assertEquals("NEW", response.getCondition());
     }
 
     @Test
     public void testItemResponsePrice() {
         assertEquals("25.00", response.getPrice());
     }
 
     @Test
     public void testItemResponseImgSrc() {
         assertEquals("http://kuvat2.huuto.net/a/3d/0e4e968231d4df107b1faefddde30", response.getImgBaseSrc());
     }
 
     @Test
     public void testParseImgBaseSrcWithoutSuffix() {
         assertFalse("Src: " + response.getImgBaseSrc(), response.getImgBaseSrc().contains(".jpg"));
     }
 
 }
