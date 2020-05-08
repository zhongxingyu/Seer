 template SimpleTemplate {
 
     class A {
         int k;
 
         A getA() {
             return new A();
         }
     }
 
     class B {
         int k;
     }
 }
 
 package AddPackage {
     inst SimpleTemplate with A => Xylofon, B => Coil;
 
     class Xylofon adds {
         int x;
         
         Xylofon() {
            tsuper[A]();
         }
     }
     
     class Coil adds {
         int kkkk;
 
         Coil() {
            tsuper[B]();
         }
     }
 }
