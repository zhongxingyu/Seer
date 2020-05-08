 package ua.cn.dmitrykrivenko.factory.factorymethod;
 
 /**
  *
  * @author Dmitry Krivenko <dmitrykrivenko@gmail.com>
  */
 public abstract class Creator {
 
    //Factory mathod
     public abstract Product createProduct(String productType);
 }
