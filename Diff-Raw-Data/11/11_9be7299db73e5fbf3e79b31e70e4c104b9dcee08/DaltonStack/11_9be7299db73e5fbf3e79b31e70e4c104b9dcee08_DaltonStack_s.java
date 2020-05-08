 /*
   This is the Dalton Stack assignment for the CSA class
 
   Authors:
   Xander Chase
   Charles Forster
   Jeffrey Stern
   Emily Lovett
   Jeffrey Stern
   Taylor Wright
   Harry DiPirinzio
   Josh Kaye
   Alex Mayer
   Maya Klabin
   Michael Dubin
   Michael Zhao
 
 
   /*delete all(Xander), substack(jeff, alex), convert(), delete one(michael, josh), reverse (taylor, harry), frequency(emily), length/size (maya/dubin)*/
 
 
 
 /*
 DONE:
 - Length
 - Reverse
 - Clear All
 -Convert
 */
 
 
 
 
 public class DaltonStack extends java.util.Stack
 {
 
    
     /*
       takes a portion (from i to j) of the original Stack
 
     */
     //SUBSTRING FOR THE MYSTRING PROGRAM (NOT STACK)
     /* public DaltonStack  subStack(int i, int j)
        {
        int [] substack = new int [j-i+1];//new substrings length
        if (i<j)
        {
        for (int c=i; c<=j; c++)
        {
        substack [c-i]=m[c];
        }
        }
        return new DaltonStack (substack);
     */
     /*create a top stack, create the substack (it would be backward-->need to reverse) put the removed items back on the original stack, return the substack.
      */
     }
 /*                                                                                                                                                            
 Function: reveress the order of the stack                                                                                                                  
 Description: reverses the order of the stack                                                           
 Author: Harry DiPrinizio & Taylor Wright                                                                                                                     
 Output: A stack w/ the contents reversed                                                                                                                  
 Input: a Stack                                                                                                                                               
 */
   public void reverse()
     {
 	java.util.Queue<Object> myQueue = new java.util.LinkedList<Object>();
 
     //pop the stack into a queue (until it is empty):
     while(!this.isEmpty())
 	{
 	    myQueue.add(this.pop());
 	}
 
     //get the queue into the stack
 
     while(! myQueue.isEmpty())
 	{
 	    this.push(myQueue.remove());
 	}
 
 }
 
 /*
 Function: converts the Stack to a Queue
 Description: Makes it so that the last item into the stack will be the first item out of the queue.
 Author: Harry DiPrinizio & Taylor Wright
 Output: a Queue w/ the contents of the stack
 Input: a Stack 
 */
     public void  convert()
     {
 	java.util.Queue<Object> myQueue = new java.util.LinkedList<Object>();
 
         //pop the stack into a queue (until it is empty):                                                                                                     
         while(!this.isEmpty())
             {
                 myQueue.add(this.pop());
             }
 
     }
 
 
  /*
       Function: deleteTop
       description:  pops the top item and doesn't return it.
       Author: Charlie Forster
       Input: nothing
       Output: nothing
       Notes: This is an example from charlie
     */
     public void deleteTop()
     {
 	this.pop();
     }
 
     /* Function: shows length of stack
        Description: Pops everything out of the stack onto a new stack, counting the items, and returns them to the original stack
        Author: Maya Klabin
        Input: Nothing 
        Output: The number of items in the stack
        Notes: 
      */
     
 public int length()
 {
     return this.size();
 }
 
 /*
       Function: ClearAll
       Description: Clears the Stack
       Author: Xander Chase
       Input:Nothing 
       Output:Nothing
     */
 
 
 
 /*
 Fuction: Randomize
 Description: Changes the order randomly
 Author: Maya Klabin
 Input: Nothing
 Output: Nothing
 
 */
 public void randomize()
 {
 
  	List list = new ArrayList();
 
 	
 	while length() > 0
 			{
 
 			    ArrayList.add(stack.pop());
 			    
 			}
 
 Collections.shuffle(list);
 
 while (!list.isEmpty())
     {
 	this.push(list.get(0))
 	    } 
 
 Return
 }
 
 
     public void clearAll()
     {
 	int x = 0;
 	while (x<this.length)
 	    {
 		this.pop();
 		x++:
 	    }//ends while
     }//ends clear all
     /*
       Function: DelOne
       description: Deletes an item from anywhere in the stack & replaces all that were above it
       Authors: Josh Kaye and Michael Zhao
       Input: Desired item to delete
       Output: none
       Notes: none
     */
     public void deleteOne(int blah)
    {
 
 	java.util.Stack popped = new java.util.Stack();
     
    for (int i=0; i<this.size(); i++;)
 	{//for
 	    if (this[i]!=blah)
 		{//if
 		    popped.push(this[i]);
 		    this.pop();
 		}//if
 	    else 
 		{//else
 		    this.pop();
		    for (int j=0; j<popped.size; j++;)
 			{//for
			    this.push(popped.size-j);
 			    popped.pop;
 
 			}//for
 	       	}//else
 	}//for
 
 }//method
 
 /*alex and emily*/
 int frequency(int number)
 {
     int[] jesus = new int[this.size()];
     int buddha = 0;
 
     while(this.empty() = false)
 	{
 	    for (int i = 0; i <= this.size(); int++)
 		{
 		    jesus[i] = this.pop();
 		}
 
 	}
 
     for (int x = 0; x<=jesus.length(); x++)
 	{
 	    if(jesus[x]=number)
 		{
 		    buddha++;
 		}
 
 	}
 
     //push everything back into the stack starting from the top                                                                                               
 
 
 
 }
 
 
 
 }//class
 
 
