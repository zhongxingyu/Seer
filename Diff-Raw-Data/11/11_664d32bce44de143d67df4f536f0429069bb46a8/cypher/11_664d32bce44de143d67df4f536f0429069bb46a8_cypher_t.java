 class cypher {
     public static void main(String[] args) {
             if(args.length!=4){
            System.err.println("Incorrect usage. -[e,d] even odd message");
             System.err.println("If your message has spaces, you need to use quotes.");
             System.exit(0);
         }
         int a=0;
         int b=0;
         if(args[0].equals("-e"))
         {a = Integer.parseInt(args[1]);
             b = Integer.parseInt(args[2]);}
         else if (args[0].equals("-d")) {
             a = -Integer.parseInt(args[1]);
             b = -Integer.parseInt(args[2]);
         }
         else {
            System.err.println("Incorrect usage. -[e,d] even odd message");
             System.exit(0);
         }
         String t = args[3];
         char[] ct = t.toCharArray();
         boolean even = true;
         int diff =0;
         System.out.println(ct);
         for(int l=0;l<t.length();l++){
             if(ct[l]!=' '){
                 if(even){
                    ct[l] = (char)(a + (int)ct[l]);
                 }
                 else {
                    ct[l] = (char)(b + (int)ct[l]);
                 }
                 
                 if((int)ct[l]>(int)'z')
                 {
                     diff = (int)ct[l] - ((int)'z'+1);
                     ct[l] = (char)(diff+(int)'a');}
                 if((int)ct[l]<(int)'a')
                 {
                     diff = (int)ct[l] - ((int)'a'-1);
                     ct[l] = (char)(diff+(int)'z');}
             }
             even=!even;
         }
         System.out.println(ct);
         
     }
 }
