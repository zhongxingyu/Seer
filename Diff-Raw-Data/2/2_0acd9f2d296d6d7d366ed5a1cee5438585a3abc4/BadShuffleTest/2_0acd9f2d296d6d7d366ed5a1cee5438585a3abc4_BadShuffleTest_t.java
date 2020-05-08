 // Oliver Kullmann, 29.11.2010 (Swansea)
 
 /* Usage:
 
      BadShuffleTest M N
 
    where M, N are integers with M >= 0 will create N random permutation of
    the sequence 0, ..., M-1, and prints to standard output in line i
    for i = 0, ..., M-1 how often i ended up in position j for j = 0,...,M-1
    (in this order).
 */
 
 class BadShuffleTest {
 
   public static final int
     error_missing_arguments = 1,
     error_not_an_int = 2,
     error_negative_number = 3;
   static final String error_header = "ERROR[BadShuffleTest]: ";
 
   public static void main(final String[] args) {
     if (args.length <= 1) {
       System.err.println(error_header + "Two arguments are needed (the number M of integers to be shuffled, and the number N of trials).");
       System.exit(error_missing_arguments);
     }
     try {
       final int M = Integer.parseInt(args[0]);
       if (M < 0) {
         System.err.println(error_header + "The number M of items must not be negative.");
         System.exit(error_negative_number);
       }
       final int N = Integer.parseInt(args[1]);
 
       // running the experiment:
       final int[][] count = new int[M][M];
       {final int[] a = new int[M];
        for (int exp = 0; exp < N; ++exp) { // experiment-counter
          for (int i = 0; i < M; ++i) a[i] = i;
          // shuffling:
          for (int i = 0; i < M; ++i) {
            final int rand = (int) (Math.random() * M); // CHANGE!
            final int t = a[rand];
            a[rand] = a[i];
            a[i] = t;
          }
         for (int i = 0; i < M; ++i) ++count[a[i]][i];
        }
       }
 
       // output of counts:
       for (int i = 0; i < M; ++i) {
         for (int j = 0; j < M; ++j) System.out.print(count[i][j] + " ");
         System.out.println();
       }
     }
     catch (final RuntimeException e) {
       System.err.println(error_header + "The command-line argument \"" + args[0] + "\" or \"" + args[1] + "\" is not an integer.");
       System.exit(error_not_an_int);
     }
   }
 }
