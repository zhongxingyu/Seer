 package org.openmrs.module.amrsregistration.test;
 
 import java.util.Map;
 
import org.junit.Assert;
 import org.junit.Test;
 import org.openmrs.module.Extension;
 import org.openmrs.module.amrsregistration.extension.html.AdminList;
 
public class AdminListExtensionTest {
     @Test
     public void testValidatesLinks() {
         AdminList localAdminList = new AdminList();
 
         Map<String, String> localMap = localAdminList.getLinks();
 
        Assert.assertNotNull("Some links should be returned", localMap);
 
        Assert.assertTrue("There should be a positive number of links", localMap
                 .values().size() > 0);
     }
 
     @Test
     public void testMediaTypeIsHtml() {
         AdminList localAdminList = new AdminList();
 
        Assert.assertTrue("The media type of this extension should be html",
                 localAdminList.getMediaType().equals(Extension.MEDIA_TYPE.html));
     }
 }
