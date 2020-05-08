 package ee.ttu.ecomm.core.service;
 
 import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
 import ee.ttu.ecomm.core.domain.Contract;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.stereotype.Service;
 
import javax.persistence.FetchType;
 import java.util.List;
 
 @Service
 public class ContractServiceImpl implements ContractService {
 
     @Autowired
     JmsTemplate jmsTemplate;
 
     @Override
     public List<Contract> findContracts() {
         return Ebean.find(Contract.class).findList();
     }
 
     @Override
     public Contract getContract(long contractId) {
         return Ebean.find(Contract.class, contractId);
     }
 
     @Override
     public Long saveContract(Contract contract) {
         boolean isNew = contract.getId() == null;
         Ebean.save(contract);
         if (isNew) {
             jmsTemplate.convertAndSend("Created new contract with ID=" + contract.getId());
         } else {
             jmsTemplate.convertAndSend("Updated contract with ID=" + contract.getId());
         }
         return contract.getId();
     }
 
 }
