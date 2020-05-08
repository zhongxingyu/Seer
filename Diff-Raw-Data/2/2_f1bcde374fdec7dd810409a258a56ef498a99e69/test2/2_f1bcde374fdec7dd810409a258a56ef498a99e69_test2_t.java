 
 import java.util.*;
 
 class B {
 	A<B> a = new A<B>();
 }
 
 class C extends B{
 	A<C> a = new A<C>();
	//A<B> a2 = new A<B>(); //this line should be wrong
 }
 
 class A<T> ownedby T {
 	T t2;
 
 	void test() {
 		T b = ownerof this;
 	}
 }
 
