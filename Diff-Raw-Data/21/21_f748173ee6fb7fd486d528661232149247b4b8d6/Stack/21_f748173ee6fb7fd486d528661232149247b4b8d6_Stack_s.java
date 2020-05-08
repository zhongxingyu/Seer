 /**
  * Created with IntelliJ IDEA.
  * User: msbaek
  * Date: 13. 07. 12.금요일
  * Time: 오후 5:32
  * To change this template use File | Settings | File Templates.
  */
 public interface Stack {
     Boolean isEmpty();
 
     Integer getSize();
 
     void push(int element);
 
     int pop();
 
     Integer top();
 }
