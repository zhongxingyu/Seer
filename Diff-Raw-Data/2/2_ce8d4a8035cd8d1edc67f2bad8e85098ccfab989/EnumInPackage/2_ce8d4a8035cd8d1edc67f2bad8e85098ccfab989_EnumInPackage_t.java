 package P  {
     enum Test {
 		FOO, BAR, KAKE, RISKOKER;
     }
 
     class V {
         public static void main(String args[]) {
            Test y = Test.RISKOKER;
             switch( y ) {
                 case FOO: System.out.println( "foo" ); break;
                 case BAR: System.out.println( "bar" ); break;
                 case KAKE: System.out.println( "kake" ); break;
                 case RISKOKER: System.out.println( "riskoker" ); break;
             }
         }
     }
 
 }
     
