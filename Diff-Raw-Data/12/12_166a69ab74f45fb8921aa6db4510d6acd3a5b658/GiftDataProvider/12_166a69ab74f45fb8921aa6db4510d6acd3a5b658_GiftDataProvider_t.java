 package com.orangeleap.tangerine.test.dataprovider;
 
 import java.math.BigDecimal;
 
 import org.testng.annotations.DataProvider;
 
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 
 public class GiftDataProvider {
 
     @DataProvider(name = "setupGift")
     public static Object[][] createGift() {
         Site site = new Site();
        site.setName("company1");
         site.setMerchantNumber("700000007668");
         site.setMerchantBin("000002");
         
         Person person = new Person();
         person.setFirstName("firstname");
         person.setLastName("lastname");
         person.setSite(site);
 
         PaymentSource paymentSource = new PaymentSource();
         paymentSource.setCreditCardExpirationMonth(11);
         paymentSource.setCreditCardExpirationYear(2010);
         paymentSource.setCreditCardHolderName("John Q. Test");
         paymentSource.setCreditCardNumber("4111111111111111");
         paymentSource.setPaymentType("CREDIT_CARD");
         
         Gift gift = new Gift();
         gift.setComments("My gift to you");
         gift.setPaymentType("Cash");
         gift.setAmount(BigDecimal.valueOf(1000L));
         gift.setPerson(person);
         gift.setId(100L);
         gift.setPaymentSource(paymentSource);
         gift.setSelectedPaymentSource(paymentSource);
         return new Object[][] { new Object[] { site, person, gift } };
     }
 }
