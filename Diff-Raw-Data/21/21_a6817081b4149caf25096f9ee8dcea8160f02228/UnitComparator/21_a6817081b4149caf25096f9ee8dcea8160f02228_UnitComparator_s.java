 package logic;
 
 import java.util.Comparator;
 
public class UnitComparator implements Comparator<Unit> {

     @Override
    public int compare(Unit arg0, Unit arg1) {
    // TODO: Change this implementation to the one of EventComparator
        if (arg0.IsSameType(arg1)) return 0;
 
         for (UnitType unt : UnitType.values())
         {
            if (arg0.Is(unt)) return -1;
            else if (arg1.Is(unt)) return 1;
         }
         return 0;
     }

 }
