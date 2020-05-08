 package nl.astraeus.persistence.model;
 
 import nl.astraeus.persistence.Persistent;
 import nl.astraeus.persistence.PersistentReference;
 import org.junit.Ignore;
 
 /**
  * Employee: rnentjes
  * Date: 3/24/12
  * Time: 9:59 PM
  */
 @Ignore
 public class Employee implements Persistent<Long> {
     public final static long serialVersionUID = 1L;
 
     private long id;
     private String name;
     private String description;
     private long [] randomData;
     private PersistentReference<Long, Company> company = new PersistentReference<Long, Company>(Company.class);
 
     public Employee(long id, String name, Company company) {
         this.id = id;
         this.name = name;
         this.company.set(company);
         company.getEmployees().add(this);
         
         createRandomData();
     }
     
     private void createRandomData() {
         randomData = new long[100];
 
         for (int i=0; i<randomData.length; i++) {
             randomData[i] = i;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public Company getCompany() {
         return company.get();
     }
 
     @Override
     public String toString() {
         return "Employee{" +
                 "name='" + name + '\'' +
                 ", description='" + description + '\'' +
                 ", company=" + company +
                 '}';
     }
 
     @Override
     public Long getId() {
         return id;
     }
 
     @Override
     public int compareTo(Object o) {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Employee clone() throws CloneNotSupportedException {
         return (Employee)super.clone();
     }
 }
