 package jamm.backend;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class AliasInfo implements Serializable
 {
     public AliasInfo(String name, List destinations, boolean active,
                        boolean administrator)
     {
         mName = name;
         mDestinations = destinations;
         mActive = active;
         mAdministrator = administrator;
     }
 
     public String getName()
     {
         return mName;
     }
 
     public List getDestinations()
     {
         return mDestinations;
     }
 
     public boolean isActive()
     {
         return mActive;
     }
 
     public boolean isAdministrator()
     {
         return mAdministrator;
     }
 
     private String mName;
     private List mDestinations;
     private boolean mActive;
     private boolean mAdministrator;
 }
