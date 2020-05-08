 package mc.inappbilling;
 
 public interface OnCompleteListener<T>
 {
     void complete(T res);
    void error(int errorCode);
 }
