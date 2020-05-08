 package org.motechproject.ananya.kilkari.performance.tests.service.db;
 
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.obd.repository.AllCampaignMessages;
 import org.motechproject.ananya.kilkari.performance.tests.utils.ContextUtils;
 
import java.util.List;

 public class OBDDbService {
     private AllCampaignMessages allCampaignMessages;
 
     public OBDDbService() {
         allCampaignMessages = ContextUtils.getConfiguration().getAllCampaignMessages();
     }
 
     public void add(CampaignMessage campaignMessage) {
         allCampaignMessages.add(campaignMessage);
     }
 
    public List<CampaignMessage> getAll() {
        return allCampaignMessages.getAll();
    }

     public void warmIndexes() {
         for (int i = 0; i < 10; i++) {
             try {
                 allCampaignMessages.findBySubscriptionId("asdasd");
             } catch (Exception e) {
                 System.out.println("Exception warming indexes : " + e.toString());
             }
         }
     }
 }
