 package final_project.model;
 
 import java.util.*;
 
 public interface IPerson extends IData {
     String getPhoneNumber();
     IPerson setPhoneNumber(String phoneNumber);
     String getCarrier();
    IPerson setCarrier(String carrier);
     String getGroup();
    IPerson setGroup(String group);
     Collection<Integer> getWatched();
     IPerson addWatched(int watched);
     IPerson removeWatched(int watched);
     IPerson clearWatched();
 }
