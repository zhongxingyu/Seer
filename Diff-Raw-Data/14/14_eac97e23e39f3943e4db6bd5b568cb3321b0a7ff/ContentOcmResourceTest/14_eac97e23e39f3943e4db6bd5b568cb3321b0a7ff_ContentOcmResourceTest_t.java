 package com.madalla.cms.bo.impl.ocm.page;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.madalla.cms.bo.impl.ocm.AbstractContentOcmTest;
 import com.madalla.cms.service.ocm.RepositoryInfo.RepositoryType;
 
 public class ContentOcmResourceTest extends AbstractContentOcmTest{
 
 	private Log log = LogFactory.getLog(this.getClass());
 	
 	public void testContentOcmResource() throws IOException{
 		String parentPath = getCreateParentNode(RepositoryType.RESOURCE);
 		
 		String nodePath ;
 		{
 	        String name = getRandomName("Page");
 	        Page page = new Page(parentPath, name);
 	        nodePath = page.getId();
 	        ocm.insert(page);
 	        ocm.save();
 	        
 	        Page testPage = (Page) ocm.getObject(Page.class, page.getId());
 	        InputStream pdf = this.getClass().getResourceAsStream("EugeneMalan.pdf");
 	        
 	        Resource resource = new Resource(testPage, "testpdf", pdf);
 	        ocm.insert(resource);
 	        ocm.save();
 	        
 	        Resource testResource = (Resource) ocm.getObject(Resource.class, resource.getId());
 	        assertNotNull(testResource);
	        //assertNotNull(testResource.getInputStream());
 	        //assertNotNull(testResource.getResource());
 	        
 		}
 		
 		ocm.remove(nodePath);
 		ocm.save();
 		
 	}
 	
 }
