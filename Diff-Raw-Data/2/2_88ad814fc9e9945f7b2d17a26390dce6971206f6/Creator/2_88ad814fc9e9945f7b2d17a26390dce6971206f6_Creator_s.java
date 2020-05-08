 package pl.finsys.creational.factory.factoryMethod2;
 
 /**
  * (c) 2013 agilecoders.pl
  * User: jarek
  * Date: 03.05.13
  * Time: 11:14
  */
public abstract class Creator extends ConcreteProduct {
     public String anOperation(){
         Product product = factoryMethod();
         return product.getName();
     }
 
     protected abstract Product factoryMethod();
 }
