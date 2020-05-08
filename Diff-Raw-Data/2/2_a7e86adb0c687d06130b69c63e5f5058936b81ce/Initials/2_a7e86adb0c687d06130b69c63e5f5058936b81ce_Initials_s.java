 public class Initials {
   public static String getInitials(String fullName) {
     String result = "";
    String[] words = fullName.split(" ");
     for (int i = 0; i < words.length; i++) {
        String nextInitial = "" + words[i].charAt(0);
        result = result + nextInitial.toUpperCase();
     }
     return result;
   }
   public static void main(String[] args) {
     System.out.print("Enter full name: ");
     String fullName = System.console().readLine();
     System.out.println("initials: " + getInitials(fullName));
   }
 }
