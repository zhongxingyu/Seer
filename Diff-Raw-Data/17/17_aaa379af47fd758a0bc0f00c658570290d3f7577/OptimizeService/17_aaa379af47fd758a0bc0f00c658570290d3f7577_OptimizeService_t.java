 package jajascript;
 
 import net.sf.json.JSONArray;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
  */
 public class OptimizeService {
 
     public Planing optimize(String json) {
         return  optimize(Booking.from(JSONArray.fromObject(json)));
     }
 
     public Planing optimize(List<Booking> bookings) {
         int best = 0;
 
         Planing p = new Planing();
 
         return optimize(p, bookings);
     }
 
     public Planing optimize(Planing p, List<Booking> bookings) {
         int i = bookings.size();
         int best_benefit = 0;
         Planing best = null;
 
         while(i-- > 0) {
             Planing test = p.clone();
             List<Booking> l = new LinkedList<Booking>(bookings);
             test.add(l.remove(i));
             if (!l.isEmpty()) {
                 test = optimize(test, l);
             }
            if (test.benefits > best_benefit) {
                best = test;
                best_benefit = test.benefits;
            }
 
         }
         return best;
     }
 
 }
