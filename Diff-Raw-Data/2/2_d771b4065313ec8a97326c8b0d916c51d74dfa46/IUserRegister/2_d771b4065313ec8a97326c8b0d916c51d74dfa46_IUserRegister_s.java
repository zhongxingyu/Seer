 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 /**
  *
  * @author Group4
  */
 public interface IUserRegister {
     
     public void add(User t);
 
     public void remove(String id);
 
     public void update(User t);
 
    public User find(Long id);
     
 }
