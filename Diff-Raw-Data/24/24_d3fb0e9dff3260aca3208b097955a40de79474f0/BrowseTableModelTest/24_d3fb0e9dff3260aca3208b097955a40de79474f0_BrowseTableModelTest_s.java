 package com.fs.humanResources.browse.model;
 
 import com.fs.common.BaseUnitTest;
 import com.fs.humanResources.browse.view.BrowseViewBean;
 import com.fs.humanResources.dto.employee.EmployeeDTO;
 import com.fs.humanResources.employee.view.employee.EmployeeViewBean;
 import com.fs.humanResources.model.address.entities.Address;
 import com.fs.humanResources.model.address.helper.AddressHelper;
 import com.fs.humanResources.model.employee.entities.Employee;
 import com.fs.humanResources.service.HumanResourcesService;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.primefaces.model.LazyDataModel;
 
 import javax.persistence.NoResultException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.when;
 
 public class BrowseTableModelTest extends BaseUnitTest {
 
     @InjectMocks
     BrowseTableModel browseTableModel;
 
     @Mock
     HumanResourcesService humanResourcesService;
 
     List<EmployeeDTO> employeeList;
 
     EmployeeDTO employee;
 
     @Before
     public void setUp() throws NoResultException {
 
         employee = new EmployeeDTO(createEmployee());
 
         employeeList = new ArrayList<EmployeeDTO>();
         employeeList.add(employee);
 
         when(humanResourcesService.findEmployees(anyInt(), anyInt())).thenReturn(employeeList);
     }
 
     @Test
    public void postConstruction_buildsDataModel_asExpected() {
         browseTableModel.init();
         LazyDataModel<BrowseViewBean> lazyDataModel = browseTableModel.getDataModel();
 
         Assert.assertEquals(1,lazyDataModel.load(0,0,"",null,null).size());
         Assert.assertEquals(employee.getId(),lazyDataModel.load(0,0,"",null,null).get(0).getEmployee().getEmployeeId());
     }
 
     @Test
     public void lazyDataModel_load_returnsEmployeeAsExpected() {
         browseTableModel.init();
         LazyDataModel<BrowseViewBean> lazyDataModel = browseTableModel.getDataModel();
 
         Assert.assertEquals(1,lazyDataModel.load(0,0,"",null,null).size());
         Assert.assertEquals(employee.getId(),lazyDataModel.load(0,0,"",null,null).get(0).getEmployee().getEmployeeId());
     }
 
     @Test
     public void lazyDataModel_getRowKey_returnsExpectedId() {
         EmployeeViewBean employeeViewBean = new EmployeeViewBean(employee);
         BrowseViewBean browseViewBean = new BrowseViewBean(employeeViewBean);
 
         browseTableModel.init();
         LazyDataModel<BrowseViewBean> lazyDataModel = browseTableModel.getDataModel();
 
         Assert.assertEquals(new Long(12345),lazyDataModel.getRowKey(browseViewBean));
     }
 
     @Test
     public void lazyDataModel_getRowData_returnsExpectedId() {
         browseTableModel.init();
 
         LazyDataModel<BrowseViewBean> lazyDataModel = browseTableModel.getDataModel();
         lazyDataModel.load(0,0,"",null,null);
 
         BrowseViewBean actualBean = lazyDataModel.getRowData(employee.getId().toString());
 
         Assert.assertEquals(new Long(12345),actualBean.getEmployee().getEmployeeId());
     }
 
     @Test
     public void lazyDataModel_getRowData_returnsNullWhenIdNotFound() {
         browseTableModel.init();
 
         LazyDataModel<BrowseViewBean> lazyDataModel = browseTableModel.getDataModel();
         lazyDataModel.load(0,0,"",null,null);
 
         BrowseViewBean actualBean = lazyDataModel.getRowData("68799");
 
         Assert.assertNull("Expected Null!",actualBean);
     }
 
     private Employee createEmployee() {
         String firstName = "Joe";
         String lastName = "Smith";
         Date dataOfBirth = new Date();
         Long employeeId = 12345l;
 
         String houseNumber = "50";
         ;
         String addressFirstLine = "Test Driven Way";
         String addressSecondLine = "Domain Court";
         String townCity = "Progammer City";
         String postCode = "AB1 CDXY";
 
         Address address = new Address();
         address.setHouseNumber(houseNumber);
         address.setAddressFirstLine(addressFirstLine);
         address.setAddressSecondLine(addressSecondLine);
         address.setTownCity(townCity);
         address.setPostCode(postCode);
         address.setPrimaryAddress(true);
 
         Employee employee = new Employee();
         employee.setId(employeeId);
         employee.setFirstName(firstName);
         employee.setLastName(lastName);
         employee.setDateOfBirth(dataOfBirth);
 
         AddressHelper addressHelper = new AddressHelper();
         employee.setAddressList(addressHelper.convertToList(address));
 
         address.setEmployee(employee);
 
         return employee;
     }
 }
