 
 /**
     *EntityFactory class.
     *This class provide Entites prefilled with some random data 
     *this class used by Data Generation class to make some test data.
     *then this data putted to DB.
  */
 
 
 package com.mne.advertmanager.util;
 
 import com.mne.advertmanager.model.AffProgram;
 import com.mne.advertmanager.model.AccessLog;
 import com.mne.advertmanager.model.AccessSource;
 import com.mne.advertmanager.model.Affiliate;
 import com.mne.advertmanager.model.AffProgramGroup;
 import com.mne.advertmanager.model.PurchaseOrder;
 import java.util.Calendar;
 import java.util.Random;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 public class EntityFactory {
 
     
     private int magicNum = -1;
     
     public EntityFactory() {
         magicNum = new Random(System.currentTimeMillis()).nextInt();
         if (magicNum < 0 )
             magicNum*=-1;
     }
 //========================== makeAffProgram =======================================
 /**this function create new AffProgram prefilled with some random data.
  * params: none
  * return: AffProgram object;
  */
     public AffProgram  makeAffProgram() {
         
         AffProgram result = new AffProgram();
 
         
         result.setName("AffProgramName"+ ++magicNum);
         result.setDescription("AffProgramDescription"+ ++magicNum);
         result.setUserName("user");
         result.setPassword("password");
         result.setAffProgramLink("http://site/AffProgramLink/"+ ++magicNum);
         result.setRedirectLink("http://site/redirectLink/"+ ++magicNum);
         result.setSyncStatus(++magicNum%2);
 
         return result;
     }
 //========================== makeAffiliate =====================================
 /**
  */
     public Affiliate  makeAffiliate() {
         Affiliate result = new Affiliate();
         result.setAffiliateName("AffiliateName"+ ++magicNum);
         result.setPassword(result.getAffiliateName());
         result.setEnabled((++magicNum%2)==1);
         result.setEmail("AffiliateEmail"+ ++magicNum);
         return result;
 
     }
 
 
 //========================== makeAffProgramGroup ==================================
 /**
  */
     public AffProgramGroup  makeAffProgramGroup() {
         
         AffProgramGroup result = new AffProgramGroup();
 
         result.setDescription("AffProgramGroupDescription"+ ++magicNum);
         result.setGroupName("AffProgramGroupName"+ ++magicNum);
 
         return result;
 
     }
 //========================== makeAccessSource ==================================
 /**
  */
     public AccessSource  makeAccessSource() {
     
         AccessSource result = new AccessSource();
 
         result.setAccessSourceDomain("www.domain"+ ++magicNum+".com");
         result.setDescription("AccessSourceDescription"+ ++magicNum);
 
         return result;
 
     }
 //========================== makeAccessLog =====================================
 /**
  */
     public AccessLog  makeAccessLog() {
 
         AccessLog result = new AccessLog();
 
         result.setAccessTime(Calendar.getInstance().getTime());
         result.setIpAddress(++magicNum%256+"."+ ++magicNum%256+"."+ ++magicNum%256+"."+ ++magicNum%256);
        result.setLocation("AccessLocation"+ ++magicNum);
         result.setTargetURL("http://wwww.access.com/url"+ ++magicNum);
 
         return result;
     }
 //========================== makePurchaseOrder =================================
 /**
  */
     public PurchaseOrder makePurchaseOrder() {
 
         PurchaseOrder result = new PurchaseOrder();
 
         result.setOrdertime(Calendar.getInstance().getTime());
         result.setStatus("Status"+(++magicNum)%2);
 
         return result;
 
     }
 }
