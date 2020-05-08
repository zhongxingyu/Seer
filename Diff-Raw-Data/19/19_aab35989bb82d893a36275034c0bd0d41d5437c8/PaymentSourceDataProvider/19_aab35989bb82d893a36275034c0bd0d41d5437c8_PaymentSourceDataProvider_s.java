 package com.orangeleap.tangerine.test.dataprovider;
 
 import org.testng.annotations.DataProvider;
 
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Site;
 
 public class PaymentSourceDataProvider {
 
     @DataProvider(name = "setupPaymentSource")
     public static Object[][] createPaymentSource() {
         Site site1 = new Site();
         site1.setName("setupPaymentSourceSite-1");
 
         Constituent constituent1 = new Constituent();
         constituent1.setFirstName("createPaymentSourceFirstName-1");
         constituent1.setLastName("createPaymentSourceLastName-1");
 
         PaymentSource ps1 = new PaymentSource();
         ps1.setPaymentType(PaymentSource.ACH);
 
         Site site2 = new Site();
         site2.setName("setupPaymentSourceSite-2");
 
         Constituent constituent2 = new Constituent();
         constituent2.setFirstName("createPaymentSourceFirstName-2");
         constituent2.setLastName("createPaymentSourceLastName-2");
 
         PaymentSource ps2 = new PaymentSource();
         ps2.setPaymentType(PaymentSource.ACH);
 
         Site site3 = new Site();
         site3.setName("setupPaymentSourceSite-3");
 
         Constituent constituent3 = new Constituent();
         constituent3.setFirstName("createPaymentSourceFirstName-3");
         constituent3.setLastName("createPaymentSourceLastName-3");
 
         PaymentSource ps3 = new PaymentSource();
         ps3.setPaymentType(PaymentSource.ACH);
 
         Site site4 = new Site();
         site4.setName("setupPaymentSourceSite-4");
 
         Constituent constituent4 = new Constituent();
         constituent4.setFirstName("createPaymentSourceFirstName-4");
         constituent4.setLastName("createPaymentSourceLastName-4");
 
         PaymentSource ps4 = new PaymentSource();
         ps4.setPaymentType(PaymentSource.CREDIT_CARD);
         ps4.setProfile("MyProfile");
 
         return new Object[][] { new Object[] { site1, constituent1, ps1 }, new Object[] { site2, constituent2, ps2 }, new Object[] { site3, constituent3, ps3 }, new Object[] { site4, constituent4, ps4 }  };
     }
 
 
     @DataProvider(name = "setupEtcPaymentSource")
     public static Object[][] createEtcPaymentSource() {
         Site site5 = new Site();
         site5.setName("setupPaymentSourceSite-5");
 
         Constituent constituent5 = new Constituent();
         constituent5.setFirstName("createPaymentSourceFirstName-5");
         constituent5.setLastName("createPaymentSourceLastName-5");
 
         PaymentSource ps5 = new PaymentSource();
         ps5.setPaymentType(PaymentSource.CASH);
         ps5.setProfile("MyCash");
 
         PaymentSource ps6 = new PaymentSource();
         ps6.setPaymentType(PaymentSource.CHECK);
         ps6.setProfile("MyCheck");
 
         PaymentSource ps7 = new PaymentSource();
         ps6.setPaymentType(PaymentSource.CREDIT_CARD);
         ps6.setProfile("MyCreditCard");
 
         return new Object[][] { new Object[] { site5, constituent5, ps5 }, new Object[] { site5, constituent5, ps6 }, new Object[] { site5, constituent5, ps7 }  };
     }
 }
