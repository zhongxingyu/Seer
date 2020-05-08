 package com.sample;
 
 import com.domain.commerce.Money;
 import com.domain.customer.EmailAddress;
 import com.domain.customer.Gender;
 import com.domain.customer.User;
 import com.domain.customer.UserRepository;
 import com.domain.shop.Product;
 import com.domain.shop.ProductRepository;
 import org.springframework.stereotype.Component;
 
 import javax.inject.Inject;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * @author Stanislav Kurilin
  */
 @Component
 public class SampleDataCreator {
     private final UserRepository userRepository;
     private final ProductRepository productRepository;
 
     @Inject
     public SampleDataCreator(UserRepository userRepository, ProductRepository productRepository) {
         this.userRepository = userRepository;
         this.productRepository = productRepository;
     }
 
     public void create() {
         fillUser(createUser("Elena", "pass", "elena.fabina@mail.ru", evaluateDate(1991, 10, 10), Gender.FEMALE),
                 createProduct("chair", 300.25),
                 createProduct("bedroom", 1230.20),
                 createProduct("towel", 5.5)
         );
         fillUser(createUser("Vasiliy", "pass", "vasil.luchhkevich@gmail.com", evaluateDate(1968, 3, 14), Gender.MALE),
                 createProduct("monitor", 1700.00),
                 createProduct("keyboard", 12.525)
         );
         fillUser(createUser("Petr", "pass", "petrAndVasik@yahoo.ru", evaluateDate(1991, 10, 10), Gender.FEMALE),
                 createProduct("book of Robinson Crusoe", 35.755),
                 createProduct("table", 350.454)
         );
         fillUser(createUser("Klara", "pass", "klara.petrova@mail.ru", evaluateDate(1958, 1, 4), Gender.FEMALE),
                 createProduct("T-Shirt with Harry Potter", 50),
                 createProduct("Lamp with Jennie", 5000.00)
         );
         fillUser(createUser("John", "pass", "john.smith@gmail.com", evaluateDate(1986, 5, 6), Gender.MALE),
                 createProduct("Banana with tomato", 1230.20),
                 createProduct("Banana", 12.20),
                 createProduct("Tomato", 130.20),
                 createProduct("April mix", 230.20),
                 createProduct("Apple", 30.20),
                 createProduct("Clock", 130.20),
                 createProduct("Nokia 1100", 230.20),
                 createProduct("Nokia 1110", 20.10),
                 createProduct("IPhone 2", 200.10),
                 createProduct("IPhone 3", 300.10),
                 createProduct("IPhone 4", 700.10),
                 createProduct("IPhone 5", 1700.10),
                 createProduct("Headphones with microphone", 15.40)
         );
         fillUser(createUser("Karl", "pass", "karl.marks@mail.ru", evaluateDate(1965, 23, 12), Gender.MALE));
         fillUser(createUser("Bob Marley", "pass", "bob.marley@gmail.com", evaluateDate(1986, 5, 6), Gender.MALE),
                 createProduct("Banana", 123089.20)
         );
     }
 
     private void fillUser(User seller, Product... products) {
         final User entity = userRepository.save(seller);
         for (Product product : products) {
            product.setSeller(entity);
             productRepository.save(product);
         }
     }
 
     private Product createProduct(String title, double price) {
         final Product result = new Product();
         result.setTitle(title);
         result.setPrice(new Money(price));
         return result;
     }
 
     private User createUser(String name, String pass, String email, Date birthday, Gender gender) {
         User result = new User();
         result.setName(name);
         result.setPassword(pass);
         result.setEmail(new EmailAddress(email));
         result.setBirthday(birthday);
         result.setGender(gender);
         return result;
     }
 
     private Date evaluateDate(int year, int month, int date) {
         return new Date(new GregorianCalendar(year, month, date).getTimeInMillis());
     }
 }
