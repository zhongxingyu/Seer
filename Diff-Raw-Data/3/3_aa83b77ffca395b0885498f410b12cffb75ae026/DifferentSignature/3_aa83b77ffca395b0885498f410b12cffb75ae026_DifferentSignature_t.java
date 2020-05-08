 template T {
 
     class A { int f(int x) { return 1; } }
     class B { int f(int x, int y) { return 2; } }
 
 }
 
 package P {
     inst T with A => X, B => X;
 
     class X adds { }
}
