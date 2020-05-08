 class Pluralize {
 
   static String pluralize( int n, String noun ) {
     return n + " " + noun + (n==1 ? "" : "s");
     }
 
  /* Fails on n==1 */
   static String pluralize_bug_A( int n, String noun ) {
    return n + " " + noun + "s";
     }
 
  /* Fails on n==0 */
   static String pluralize_bug_B( int n, String noun ) {
    return n + " " + noun + (n<1 ? "" : "s");
     }
 
  /* Fails on all -- missing number */
   static String pluralize_bug_C( int n, String noun ) {
    return noun + (n==1 ? "" : "s");
     }
 
   /* Is this one really buggy? */
   /*
   static String pluralize_bug_D( int n, String noun ) {
     return (n==0 ? "no" : n) + " " + noun + (n==1 ? "" : "s");
     }
   */
 
   /* Fails on negatives (arguably) */
   /*
   static String pluralize_bug_E( int n, String noun ) {
     return (n < 0 ? "negative " : "") + Math.abs(n) + " " + noun + (n==1 ? "" : "s");
     }
   */
   }
