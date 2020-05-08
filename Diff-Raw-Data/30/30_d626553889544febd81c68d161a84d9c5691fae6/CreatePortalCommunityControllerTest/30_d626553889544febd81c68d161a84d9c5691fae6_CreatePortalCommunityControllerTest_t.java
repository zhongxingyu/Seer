 package gov.nih.nci.cagrid.portal.portlet.browse.community;
 
import gov.nih.nci.cagrid.portal.dao.catalog.CatalogEntryDao;
import gov.nih.nci.cagrid.portal.domain.catalog.CommunityCatalogEntry;
 import gov.nih.nci.cagrid.portal.portlet.PortalPortletIntegrationTestBase;
import gov.nih.nci.cagrid.portal.portlet.UserModel;
 import gov.nih.nci.cagrid.portal.portlet.browse.CreateCatalogEntryController;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
 
    public void testCreate() throws Exception {
        CreateCatalogEntryController controller = (CreateCatalogEntryController) getApplicationContext().getBean("createCatalogEntryController");
        assertNotNull(controller);

        CommunityCatalogEntry ce = (CommunityCatalogEntry) Class.forName("gov.nih.nci.cagrid.portal.domain.catalog.CommunityCatalogEntry").newInstance();
        CatalogEntryDao mockDao = mock(CatalogEntryDao.class);
        when(mockDao.getById(anyInt())).thenReturn(ce);
        UserModel mockUserModel = mock(UserModel.class);
        when(mockUserModel.getCurrentCatalogEntry()).thenReturn(ce);
        controller.setUserModel(mockUserModel);

        aReq.setParameter("entryType", "COMMUNITY");

        controller.handleActionRequest(aReq, aRes);
 
 
     }
 }
