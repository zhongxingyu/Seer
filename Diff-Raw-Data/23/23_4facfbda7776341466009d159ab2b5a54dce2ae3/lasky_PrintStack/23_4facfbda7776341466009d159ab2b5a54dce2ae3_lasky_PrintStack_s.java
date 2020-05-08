 public class lasky_PrintStack<T> extends LinkedStackClass<T>
 {
   
  public void printStack(T theStack)
   {
     LinkedStackClass<Integer> theTempStack = new LinkedStackClass<Integer>();
     while(!theStack.isEmptyStack())
     {
       System.out.print(theStack.peek() + " ");
       theTempStack.push(theStack.peek());
       theStack.pop();
     }
     while(!theTempStack.isEmptyStack())
     {
       theStack.push(theTempStack.peek());
       theTempStack.pop();
     }
       
     
    
   }
}
    

