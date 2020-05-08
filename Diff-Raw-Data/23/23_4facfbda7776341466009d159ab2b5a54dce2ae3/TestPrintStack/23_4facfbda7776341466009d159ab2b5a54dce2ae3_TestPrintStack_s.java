 public class TestPrintStack
 {
   public static void main(String[] args)
   {
   LinkedStackClass<Integer> origStack = new LinkedStackClass<Integer>();
   origStack.push(1);
   origStack.push(2);
   origStack.push(3);
   origStack.push(4);
   origStack.push(5);
   System.out.println("Starting with the top of the stack the contents of each stack in order are: ");
   try
   {
    printStack(LinkedStackClass<T> origStack);
   }
   catch (StackUnderflowException l)
   {
     System.out.println(l.toString());
     System.exit(0);
   }
   }
 }
             
         
     
