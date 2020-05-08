 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.posabro.ocsys.services.jpa;
 
 import com.posabro.ocsys.domain.Address;
 import com.posabro.ocsys.domain.Branch;
 import com.posabro.ocsys.domain.BranchPK;
 import com.posabro.ocsys.domain.Carrier;
 import com.posabro.ocsys.domain.Company;
 import com.posabro.ocsys.domain.Container;
 import com.posabro.ocsys.domain.ContainerType;
 import com.posabro.ocsys.domain.ConversionFactor;
 import com.posabro.ocsys.domain.ConversionFactorPK;
 import com.posabro.ocsys.domain.Country;
 import com.posabro.ocsys.domain.Customer;
 import com.posabro.ocsys.domain.CustomerPK;
 import com.posabro.ocsys.domain.Facility;
 import com.posabro.ocsys.domain.FacilityPK;
 import com.posabro.ocsys.domain.OutboundBol;
 import com.posabro.ocsys.domain.OutboundBolPK;
 import com.posabro.ocsys.domain.Product;
 import com.posabro.ocsys.domain.ProductType;
 import com.posabro.ocsys.domain.State;
 import com.posabro.ocsys.domain.StatePK;
 import com.posabro.ocsys.domain.UnitOfMeasurement;
 import com.posabro.ocsys.services.BranchService;
 import com.posabro.ocsys.services.CarrierService;
 import com.posabro.ocsys.services.CompanyService;
 import com.posabro.ocsys.services.ContainerService;
 import com.posabro.ocsys.services.ConversionFactorService;
 import com.posabro.ocsys.services.CountryService;
 import com.posabro.ocsys.services.CustomerService;
 import com.posabro.ocsys.services.FacilityService;
 import com.posabro.ocsys.services.OutboundBolService;
 import com.posabro.ocsys.services.ProductService;
 import com.posabro.ocsys.services.StateService;
 import com.posabro.services.AbstractServiceTest;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.validation.constraints.AssertTrue;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.After;
 import org.junit.Before;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author Carlos Juarez
  */
 public class OutboundBolServiceTest extends AbstractServiceTest{
     
     final org.slf4j.Logger logger = LoggerFactory.getLogger(OutboundBolServiceTest.class);
     
     @Autowired
     private OutboundBolService outboundBolService;
     
     @Autowired
     private BranchService branchService;
     
     @Autowired
     private CompanyService companyService;
     
     @Autowired
     private ContainerService containerService;
     
     @Autowired
     private ProductService productService;
     
     @Autowired
     private CustomerService customerService;
     
     @Autowired
     private CountryService countryService;
     
     @Autowired
     private StateService stateService;
     
     @Autowired
     private CarrierService carrierService;
     
     @Autowired
     private ConversionFactorService conversionFactorService;
     
     @Autowired
     private FacilityService facilityService;
     
     @Autowired
     private ObjectMapper objectMapper;
     
     @Before
     public void loadData(){
         Company company = new Company("BALA", "By the way");
         companyService.saveCompany(company);
         Branch branch = new Branch("B1", company);
         branchService.saveBranch(branch);
         containerService.saveContainer(new Container("CON1", new BigDecimal("20.21") , new BigDecimal("22.1"), new BigDecimal("200.20")));
         productService.saveProduct(new Product(ProductType.NACNL, "NaCN liquido", "NaCN liquido bol"));
         productService.saveProduct(new Product(ProductType.NACNB, "NaCNB liquido", "NaCNB liquido bol"));
         Country uruguay = new Country("UR", "Uruguay");
         countryService.saveCountry(uruguay);
         State monteVideo = new State("MO","Monte video", uruguay);
         stateService.saveState(new State("MO","Monte video", uruguay));
         Address address = new Address("Gardel 500", "", 19876, "Cordoba Cd", monteVideo);
         customerService.saveCustomer(new Customer("CUST1", "Juan Glz S.A", "TX1", company, address));
         facilityService.saveFacility(new Facility("FAC1","CUST1","BALA","Warehouse",address));
         carrierService.saveCarrier(new Carrier("CAR1", "Transporter estrella blanca", "TAX3", address));
         conversionFactorService.saveConversionFactor(new ConversionFactor(UnitOfMeasurement.LTS, UnitOfMeasurement.GALS, new BigDecimal("0.26")));
         conversionFactorService.saveConversionFactor(new ConversionFactor(UnitOfMeasurement.KGS, UnitOfMeasurement.LBS,  new BigDecimal("2.20")));
     }
     
     @After
     public void removeData(){
         branchService.removeBranch(new BranchPK("B1", "BALA"));
         containerService.removeContainer("CON1");
         productService.removeProduct(ProductType.NACNL);
         productService.removeProduct(ProductType.NACNB);
         facilityService.removeFacility(new FacilityPK("FAC1", "CUST1", "BALA"));
         customerService.removeCustomer(new CustomerPK("CUST1", "BALA"));
         companyService.removeCompany("BALA");
         carrierService.removeCarrier("CAR1");
         stateService.removeState(new StatePK("MO", "UR"));
         countryService.removeCountry("UR");
         conversionFactorService.removeConversionFactor(new ConversionFactorPK(UnitOfMeasurement.LTS, UnitOfMeasurement.GALS));
         conversionFactorService.removeConversionFactor(new ConversionFactorPK(UnitOfMeasurement.KGS, UnitOfMeasurement.LBS));
     }
     
     @Test
     public void testSaveIsoLiquidOutboundBol(){
         int year = Calendar.getInstance().get(Calendar.YEAR);
         //add unit of measurement
         OutboundBol outboundBol = new OutboundBol();
         outboundBol.setBolDate(new Date());
         outboundBol.setShipmentDate(new Date());
         outboundBol.setCustomerId("CUST1");
         outboundBol.setFacilityId("FAC1");
         outboundBol.setCarrierId("CAR1");
         outboundBol.setDriver("the driver");
         outboundBol.getInboundBolData().setInbouundContId1("CON1");
         outboundBol.setContainerType(ContainerType.ISO);
         //
         outboundBol.setProductId(ProductType.NACNL);
         outboundBol.setContainerId("CON1");
         outboundBolService.saveOutboundBol(new BranchPK("B1", "BALA"), outboundBol);
         OutboundBol foundOutboundBol = outboundBolService.findOutboundBol(new OutboundBolPK("BALA", "B1", year + "0001"));
         assertNotNull(foundOutboundBol);
        assertTrue( (new Date().getTime()-foundOutboundBol.getAuditData().getCreatedDate().getTime())<1000*1);
         try {
             System.out.println("printing outbounbol");
             objectMapper.writeValue(System.out, foundOutboundBol);
             System.out.println();
         } catch (IOException ex) {
             logger.error("Exception while serialization",ex);
         } catch (Exception ex) {
             logger.error("Exception while serialization",ex);
         }
         assertEquals(new BigDecimal("5.25"), foundOutboundBol.getContent().getContainedGallons());
         //cleanup
         outboundBolService.removeOutboundBol(outboundBol.getOutboundBolPK());
     }
     
 
     @Test
     @Ignore
     public void testSaveRailcarLiquidOutboundBol(){
         int year = Calendar.getInstance().get(Calendar.YEAR);
         //add unit of measurement
         OutboundBol outboundBol = new OutboundBol();
         outboundBol.setBolDate(new Date());
         outboundBol.setShipmentDate(new Date());
         outboundBol.setCustomerId("CUST1");
         outboundBol.setFacilityId("FAC1");
         outboundBol.setCarrierId("CAR1");
         outboundBol.setDriver(null);
         outboundBol.getInboundBolData().setInbouundContId1("CON1");
         outboundBol.setContainerType(ContainerType.RAILCAR);
         //
         outboundBol.setProductId(ProductType.NACNL);
         outboundBol.setContainerId("no registered cont");
         outboundBol.getContent().setContainedKgs(new BigDecimal("10.00"));
         outboundBolService.saveOutboundBol(new BranchPK("B1", "BALA"), outboundBol);
         OutboundBol foundOutboundBol = outboundBolService.findOutboundBol(new OutboundBolPK("BALA", "B1", year + "0001"));
         assertNotNull(foundOutboundBol);
         assertEquals(new BigDecimal("10.00"), foundOutboundBol.getContent().getContainedKgs());
         assertEquals(new BigDecimal("22.00"), foundOutboundBol.getContent().getContainedLbs());
         assertNull(foundOutboundBol.getContent().getContainedLts());
         assertNull(foundOutboundBol.getContent().getContainedGallons());
         //cleanup
         outboundBolService.removeOutboundBol(outboundBol.getOutboundBolPK());
     }
     
     @Test
     public void testSaveRailcarNoLiquidOutboundBol(){
         int year = Calendar.getInstance().get(Calendar.YEAR);
         //add unit of measurement
         OutboundBol outboundBol = new OutboundBol();
         outboundBol.setBolDate(new Date());
         outboundBol.setShipmentDate(new Date());
         outboundBol.setCustomerId("CUST1");
         outboundBol.setFacilityId("FAC1");
         outboundBol.setCarrierId("CAR1");
         outboundBol.getInboundBolData().setInbouundContId1("CON1");
         outboundBol.setContainerType(ContainerType.RAILCAR);
         outboundBol.getContent().setContainedKgs(new BigDecimal("2000.20"));
         outboundBol.setProductId(ProductType.NACNB);
         outboundBol.setContainerId("no registered cont");
         outboundBolService.saveOutboundBol(new BranchPK("B1", "BALA"), outboundBol);
         OutboundBol foundOutboundBol = outboundBolService.findOutboundBol(new OutboundBolPK("BALA", "B1", year + "0001"));
         assertNotNull(foundOutboundBol);
         assertEquals(null, foundOutboundBol.getContent().getContainedGallons());
         //cleanup
         outboundBolService.removeOutboundBol(outboundBol.getOutboundBolPK());
     }
     
     @Test
     public void testSaveIsoNoLiquidOutboundBol(){
         int year = Calendar.getInstance().get(Calendar.YEAR);
         //add unit of measurement
         OutboundBol outboundBol = new OutboundBol();
         outboundBol.setBolDate(new Date());
         outboundBol.setShipmentDate(new Date());
         outboundBol.setCustomerId("CUST1");
         outboundBol.setFacilityId("FAC1");
         outboundBol.setCarrierId("CAR1");
         outboundBol.setDriver("the driver");
         outboundBol.getInboundBolData().setInbouundContId1("CON1");
         outboundBol.setContainerType(ContainerType.ISO);
         outboundBol.getContent().setContainedKgs(new BigDecimal("2000.20"));
         outboundBol.setProductId(ProductType.NACNB);
         outboundBol.setContainerId("CON1");
         outboundBolService.saveOutboundBol(new BranchPK("B1", "BALA"), outboundBol);
         OutboundBol foundOutboundBol = outboundBolService.findOutboundBol(new OutboundBolPK("BALA", "B1", year + "0001"));
         assertNotNull(foundOutboundBol);
         assertEquals(null, foundOutboundBol.getContent().getContainedGallons());
         //cleanup
         outboundBolService.removeOutboundBol(outboundBol.getOutboundBolPK());
     }
 }
