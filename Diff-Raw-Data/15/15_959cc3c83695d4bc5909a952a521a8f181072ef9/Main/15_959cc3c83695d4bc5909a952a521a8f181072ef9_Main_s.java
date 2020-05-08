 package main;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.security.GuardedObject;
 
 import bank.Bank;
 import bank.security.SKonto;
 
 import security.SecurityCallback;
 import security.SecurityObjectException;
 
 
 
 
 
 
 
 public class Main extends SecurityCallback{
 	private Object[] sput;
 
 	/**
 	 * @param args
 	 * @throws NoSuchMethodException 
 	 * @throws SecurityObjectException 
 	 * @throws InvocationTargetException 
 	 * @throws IllegalAccessException 
 	 * @throws IllegalArgumentException 
 	 * @throws NoSuchFieldException 
 	 */
 	public static void main(String[] args) throws SecurityObjectException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
 		Main m = new Main();
 		m.doSomething();
 	}
 	
 	public void doSomething(){
 		Bank bank = new Bank();
 		SKonto konto= bank.getKonto();
 		bank.doPrivileged(konto);
 		//SecurityException
 //		try{
 //			konto.getGeld(this);
 //		}catch(Exception e){
 //			System.out.println("SOE caught");
 //		}
 //		konto.setGeld(5000,this);
		Integer foo = konto.getGeld(bank,createTan());
 		System.out.println(foo);
 	}
 
 
 
 
 }
