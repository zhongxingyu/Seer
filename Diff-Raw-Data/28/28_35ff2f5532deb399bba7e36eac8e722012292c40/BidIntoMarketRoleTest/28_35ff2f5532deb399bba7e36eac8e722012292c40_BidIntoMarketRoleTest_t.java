 package polep.role;
 
import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.template.Neo4jOperations;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import polep.domain.agent.EnergyProducer;
 import polep.domain.market.Bid;
 import polep.domain.technology.Fuel;
 import polep.domain.technology.PowerPlant;
 import polep.repository.BidRepository;
 import polep.repository.EnergyProducerRepository;
 import polep.repository.PowerPlantRepository;
 
 /**
  * @author JCRichstein
  * 
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"/polep-test-context.xml"})
 @Transactional
 public class BidIntoMarketRoleTest {
     
     @Autowired Neo4jOperations template;
     @Autowired PowerPlantRepository powerPlantRepository;
     @Autowired EnergyProducerRepository energyProducerRepository;
     @Autowired BidRepository bidRepository; 
     
     Logger logger = Logger.getLogger(BidIntoMarketRoleTest.class);
 	
     @Before
     @Transactional
     public void setUp() throws Exception {
     }
     
     @Test
     public void checkBidIntoMarketFunctionality(){
     	
     	EnergyProducer producer = new EnergyProducer();
     	PowerPlant plant = new PowerPlant();
     	Fuel fuel = new Fuel();
     	fuel.setPrice(20);
     	fuel.persist(); // Saves it to the database
     	plant.setCapacity(500);
     	plant.setThisFuel(fuel);
     	plant.setEfficiency(0.33);
     	plant.persist(); // Saves it to the database
     	plant.getMarginalCost();
     	
     	Set<PowerPlant> setPowerPlant = producer.getPowerPlantSet();
     	setPowerPlant.add(plant);
     	producer.persist();
     	
     	BidIntoMarketRole bidIntoMarketRole = new BidIntoMarketRole();
     	bidIntoMarketRole.act(producer);
     	
     	
     	Bid bid = bidRepository.findAll().iterator().next();
     	
     	logger.warn("Price: " + bid.getPrice());
     	logger.warn("Volume: " + bid.getVolume());
     	
     	assertTrue(bid.getPrice()==20);
     	assertTrue(bid.getVolume()==500);
     	
     	
     }
     
 
 }	
