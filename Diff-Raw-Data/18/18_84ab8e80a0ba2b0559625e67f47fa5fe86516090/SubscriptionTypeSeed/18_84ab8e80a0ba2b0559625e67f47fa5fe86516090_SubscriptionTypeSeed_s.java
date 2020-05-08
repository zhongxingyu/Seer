 package org.motechproject.ghana.mtn.tools.seed;
 
 import org.apache.log4j.Logger;
 import org.motechproject.ghana.mtn.domain.SubscriptionType;
 import org.motechproject.ghana.mtn.domain.builder.SubscriptionTypeBuilder;
 import org.motechproject.ghana.mtn.repository.AllSubscriptionTypes;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SubscriptionTypeSeed extends Seed {
     @Autowired
     private AllSubscriptionTypes allSubscriptionTypes;
     private Logger log = Logger.getLogger(SubscriptionTypeSeed.class);
 
     @Override
     protected void load() {
         SubscriptionType pregnancySubscriptionType = new SubscriptionTypeBuilder()
                 .withProgramName("Pregnancy")
                 .withShortCode("P")
                 .withShortCode("p")
                 .withMaxWeek(35).withMinWeek(5).build();
 
         SubscriptionType childCareSubscriptionType = new SubscriptionTypeBuilder()
                 .withProgramName("Child Care")
                 .withShortCode("C")
                 .withShortCode("c")
                 .withMaxWeek(52).withMinWeek(1).build();
 
         allSubscriptionTypes.add(pregnancySubscriptionType);
     }
 }
