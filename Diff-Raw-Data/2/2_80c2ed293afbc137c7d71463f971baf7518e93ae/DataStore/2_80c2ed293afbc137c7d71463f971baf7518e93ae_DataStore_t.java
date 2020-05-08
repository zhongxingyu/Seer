 package in.com.tw.jellybean;
 
 import in.com.tw.jellybean.models.Consultant;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: somisetn
  * Date: 31/07/13
  * Time: 3:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DataStore {
 
     public  List<Consultant> getConsultants() {
         return consultants;
     }
 
 
     List<Consultant> consultants = new ArrayList<Consultant>();
 
     public boolean saveConsultant(Consultant consultant) {
          consultants.add(consultant);
        return true;
     }
 }
