 public class lasky_PrintStack<T> extends LinkedStackClass<T>
 {
   
  public void printStack()
   {
    LinkedStackClass<Integer> theStack =  StackNode<T> info;
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
