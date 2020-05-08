 package com.thoughtworks.repository;
 
 import com.thoughtworks.constant.Constant;
 import com.thoughtworks.database.DatabaseHelper;
 import com.thoughtworks.model.Customer;
 import com.thoughtworks.model.Menu;
 import com.thoughtworks.model.Rule;
 import org.junit.Test;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 
 import static com.thoughtworks.matcher.ContainsOnlySpecificNameOfNodes.containsOnlyNodeNames;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class CustomerTest extends BaseTest {
 
     @Test
     public void shouldReturnAllCustomers() throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         List<Customer> customers = customerRepository.getCustomers();
         Collection<String> nodeNames = getNodeNames(customers);
         assertThat(nodeNames, containsOnlyNodeNames("Mary", "Joy", "John"));
     }
 
     @Test
     public void shouldReturnPersonalisedMenu() throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         Node mary = customerRepository.getCustomer("Mary");
         List<Menu> dishes = customerRepository.getPersonalisedMenu(mary);
         Collection<String> nodeNames = getNodeNames(dishes);
 
         assertThat(nodeNames, containsOnlyNodeNames("pasta salad", "sandwiches"));
     }
 
     @Test
     public void shouldReturnPersonalisedMenuFromCustomerNotAnswerQuestion() throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         Node mary = customerRepository.getCustomer("Joy");
         List<Menu> dishes = customerRepository.getPersonalisedMenu(mary);
         Collection<String> nodeNames = getNodeNames(dishes);
 
         assertThat(nodeNames, containsOnlyNodeNames("tuna salad", "nut salad", "grilled chicken potsu", "fried rice", "pasta salad", "sandwiches"));
     }
 
     @Test
     public void shouldReturnACustomerByName() {
         CustomerRepository customerRepository = new CustomerRepository();
         Node mary = customerRepository.getCustomer("Mary");
 
         assertThat(mary.getProperty(DatabaseHelper.NODE_NAME).toString(), is("Mary"));
 
     }
 
     @Test
     public void shouldCalculateCustomerAge() throws ParseException {
         CustomerRepository customerRepository = new CustomerRepository();
 
         Calendar calendarInstance = Calendar.getInstance();
         calendarInstance.add(Calendar.YEAR, -10);
 
         String stringDate = Constant.SIMPLE_DATE_FORMAT.format(calendarInstance.getTime());
 
         customerRepository.createCustomer("Jo", stringDate);
 
         Node jo = customerRepository.getCustomer("Jo");
         Calendar customerAge = customerRepository.getCustomerDateOfBirth(jo);
 
         int age = customerRepository.calculateAge(customerAge);
 
         assertThat(age, is(10));
     }
 
     @Test
     public void shouldReturnChildNodeForAgeUnderChildThreshold() {
         DatabaseHelper db = DatabaseHelper.getInstance();
         GraphDatabaseService graphDb = db.getDatabaseService();
 
         Transaction transaction = graphDb.beginTx();
         try {
             CustomerRepository customerRepository = new CustomerRepository();
             customerRepository.createCustomer("Bob","01/01/2000");
             Node bob = customerRepository.getCustomer("Bob");
 
             RuleRepository ruleRepository = new RuleRepository();
            Node child = ruleRepository.evaluateRuleBasedOn("Dob").withValue(customerRepository.getAge(bob));
 
             assertThat(child.getProperty(DatabaseHelper.NODE_NAME).toString(), is("Child"));
         } finally {
             transaction.failure();
         }
     }
 
     @Test
     public void shouldGetAllRulesAppliedToCustomer() throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         List<Rule> listOfRules = customerRepository.getCustomerRules();
 
         assertThat(listOfRules.size(), is(1));
     }
 
 
 }
