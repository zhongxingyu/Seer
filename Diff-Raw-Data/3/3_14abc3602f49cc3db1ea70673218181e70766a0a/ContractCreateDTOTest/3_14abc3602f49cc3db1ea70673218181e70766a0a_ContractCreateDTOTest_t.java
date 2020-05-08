 package ee.ttu.ecomm.core.webservice.soap;
 
 import ee.ttu.ecomm.core.domain.Contract;
 import org.junit.Test;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import static junit.framework.Assert.assertEquals;
 
 public class ContractCreateDTOTest {
 
     @Test
     public void testToContract() throws Exception {
         ContractCreateDTO contractDTO = new ContractCreateDTO();
         contractDTO.setName("baz");
         contractDTO.setDescription("bar");
         contractDTO.setValidFrom(new Date());
         contractDTO.setValueAmount(BigDecimal.TEN);
         contractDTO.setCustomerId(100L);
         Contract contract = contractDTO.toContract();
         assertEquals(contract.getCustomer().getId(), contractDTO.getCustomerId());
     }
 
 }
