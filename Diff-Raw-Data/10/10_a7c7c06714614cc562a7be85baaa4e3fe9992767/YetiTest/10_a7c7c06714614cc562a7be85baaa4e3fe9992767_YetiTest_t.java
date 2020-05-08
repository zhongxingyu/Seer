 package yeti.test;
 
 import java.util.Vector;
 
 import yeti.YetiRoutine;
 
 /**
  * Class used to test the Yeti implementation.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  *
  * @param <T>
  */
 public class YetiTest<T extends YetiRoutine> {
 	
 	public YetiTest(){
 		System.out.println("YetiTest constructor called");
 	}
 	@SuppressWarnings("unchecked")
 	public YetiTest(YetiTest yt){
 		System.out.println("YetiTest(YetiTest yt) constructor called");
 	}
 
 	@SuppressWarnings("unchecked")
 	public YetiTest(YetiTest yt, YetiTest yt2){
 		System.out.println("YetiTest(YetiTest yt, YetiTest yt2) constructor called");
 	}
 
 	
 	public void printRandomDouble(){
 		System.out.println(Math.random());
 	}
 	public void printRandomFloat(float f){
 		System.out.println((float)(Math.random()));
 	}
 
 	public void printByte(byte f){
 		byte v3=57;
 		System.out.println(f);
 		System.out.println(v3);
 	}
 
 	public void printChar(char c){
		assert c!='m';
 		System.out.println(c);
 	}
 
 	public void printInt(int a){
		assert a!=1;
 		System.out.println((int)(1/a));
 	}
 	public T genT(Vector<String> t){
 	
 	return null;
 	}
 
 	
 	public static int [] genInt2(){
 		int [] is = {10};
 		return is ;
 	}
 	
 	public int genInt(){
 		return 0;
 	}
 	
 	public String toString(){
 		return "a YetiTest";
 	}
 
 }
