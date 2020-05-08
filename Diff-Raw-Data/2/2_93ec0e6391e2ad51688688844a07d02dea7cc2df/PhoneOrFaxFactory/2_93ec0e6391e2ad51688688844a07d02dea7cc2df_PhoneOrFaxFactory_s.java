 package au.com.sensis.mobile.web.component.clicktocall.showcase.business;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import au.com.sensis.mobile.web.component.clicktocall.model.PhoneOrFax;
 
 /**
  * Simple factory for showcase data.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class PhoneOrFaxFactory {
 
     /**
      * @return a List of {@link PhoneOrFax} instances.
      */
     public List<PhoneOrFax> createPhoneOrFaxList() {
         final List<PhoneOrFax> phoneOrFaxList = new ArrayList<PhoneOrFax>();
         phoneOrFaxList.add(createPhone1());
         phoneOrFaxList.add(createFax1());
         phoneOrFaxList.add(createPhone2());
         phoneOrFaxList.add(createFax2());
         return phoneOrFaxList;
     }
 
     private PhoneOrFax createPhone1() {
         return new PhoneOrFax("(03) 9001 0001", "+61390010001", false);
     }
 
     private PhoneOrFax createPhone2() {
         return new PhoneOrFax("(03) 9001 0002", "+61390010002", false);
     }
 
     private PhoneOrFax createFax1() {
         return new PhoneOrFax("(02) 8001 0001", "+61280010001", true);
     }
 
     private PhoneOrFax createFax2() {
         return new PhoneOrFax("(02) 8001 0002", "+61280010002", true);
     }
 
     public PhoneOrFax createDefaultPhone() {
        return new PhoneOrFax("(02) 7001 0002", "+61570010002", false);
     }
 
 }
