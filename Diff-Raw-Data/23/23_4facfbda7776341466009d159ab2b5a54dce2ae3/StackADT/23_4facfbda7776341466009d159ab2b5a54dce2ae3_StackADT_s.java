
 public interface StackADT<T>
 {
     public void initializeStack();
       
 
     public boolean isEmptyStack();
        
 
     public boolean isFullStack();
 
 
     public void push(T newItem) throws StackOverflowException;
 
 
     public T peek() throws StackUnderflowException;
   
 
     public void pop() throws StackUnderflowException;
    
 }
 
