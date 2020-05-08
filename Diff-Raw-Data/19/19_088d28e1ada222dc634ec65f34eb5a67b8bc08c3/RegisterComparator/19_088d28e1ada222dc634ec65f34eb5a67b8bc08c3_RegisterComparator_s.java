 public class RegisterComparator implements java.util.Comparator<Register>
 {
   public int compare(Register a, Register b)
   {
    return a.sparcName.compareTo(b.sparcName);
   }
 }
