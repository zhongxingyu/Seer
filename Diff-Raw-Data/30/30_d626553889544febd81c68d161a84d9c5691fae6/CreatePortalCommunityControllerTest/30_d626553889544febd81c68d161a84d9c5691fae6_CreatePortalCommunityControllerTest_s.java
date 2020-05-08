 package gov.nih.nci.cagrid.portal.portlet.browse.community;
 
 import gov.nih.nci.cagrid.portal.portlet.PortalPortletIntegrationTestBase;
 import gov.nih.nci.cagrid.portal.portlet.browse.CreateCatalogEntryController;
import org.springframework.mock.web.portlet.MockPortletRequest;
import org.springframework.mock.web.portlet.MockPortletResponse;
 import org.springframework.mock.web.portlet.MockActionRequest;
 import org.springframework.mock.web.portlet.MockActionResponse;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class CreatePortalCommunityControllerTest extends PortalPortletIntegrationTestBase {
 
 
     protected MockActionRequest aReq = new MockActionRequest();
     protected MockActionResponse aRes = new MockActionResponse();
 
    public void testCreate() throws Exception{
        CreateCatalogEntryController createCatalogEntryController  = (CreateCatalogEntryController)getApplicationContext().getBean("createCatalogEntryController");
        assertNotNull(createCatalogEntryController);
        
        createCatalogEntryController.handleActionRequest(aReq,aRes);
 
 
     }
 }
