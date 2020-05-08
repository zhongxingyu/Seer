 /*
 Authors:
 Charles Forster
 Isabella Giovannini
 Jason Shein
 Katherine Odom
 Malina Buturovic
 Derek Whang -_- hohoho
 Annelise Steele <3
 Sophia Edelstein 
  */
 
 public class DaltonQueue extends java.util.Queue
 {
     /*
       Author: Malina
       Function: peek_all
       /*
     public String peek_all()
     {
 	String input="";
 	for (i=front; i<back; i++)
 	    {
 		input=input ", " + Array[index];
 	    }
 	return input;
     }
 
 
 
     /*
       Author: Sophia
       Function: length
     */
     public int length()
     {
 	offer("end of array");
 	int length =0;
 	
 	while(peek().toString().!equals("end of array")) 
 	    {
 		offer(poll());
 		length++;
 	    }
 	return length; 
     }
     /*
       AUthor: Sophia 
       Function: Search
     */
     public int search(Object o) 
     { 
 	offer("end of array");
 	int where = 0;
 	
         while(!peek().toString().equals("end of array"))
 	    {
 		if(!peek().toString().equals(o))	
 		    {
 			return where;
 		    }
 		offer(poll());
 		where++;
 	    }  
 	return -1; 
     }
     
 
     /*                                                                          
                                                           
   
 
 
     /* 
        Author: ISABELLA and malina. 
 
      */
 
     void ReplaceAt(int index; int new)
     {
 	Array[index] = new;
     }
 
 <<<<<<< HEAD
 =======
 
 
 >>>>>>> origin/master
     /*
       Author: Isabella
      */
     
 
     //in progress
     void sort()
     {
 	//make the queue into the array:
 	Object[] queue = new Object[queue.size()]; 
 	int counter = 0;
 	while (!this.isEmpty()) //go through the queue
 	    {
 		queue[counter]= this.poll();
 	    }
 
 	//sort the array:
 	if(queue[counter]<queue[counter--])	 
 	    {
 		int greater = queue[counter--];
 		int smaller = queue[counter];
 		queue[counter]=greater;
 		queue[counter--]=smaller;
 	    }
 	
 	//return it to the queue:
 	for (counter=0; counter<queue.size(); counter++)
 	    {
 		offer(queue[counter]);
 	    }
     }
 
     /*
      Author: Katherine 
      Function: Reverse (in ABC, out CBA)
    /*

    {
     
 	/*
 	  Author: Katherine
 	  Function: Dump (delete everything from the queue)
 	*/
 	void dump()
 	{
 	    while(!this.isEmpty())
 		{
 		    this.remove();
 		} 
 	}
	


    }
 
     /*
       Author: Katherine
       Function: Dump (delete everything from the queue)
     */
 
     void dump()
     {
 	 while(!this.isEmpty())
 	     {
 		this.remove();
 	     } 
     }
 	
 
 
     
 
 
 /*
       Author: Shein
       Function: switch(x,y)
       parameters: x and y are both distances from the front of the queue
       return: nothing
     */
     void switch(int x, int y)
 	{
 	    java.util.Queue queue2 = new java.util.LinkedList();
 	    java.util.Queue queue3 = new java.util.LinkedList();
 	    int object1;
 
 	    for(int i=0; i<x; i++)
 		{
 		    queue2.offer(this.poll());
 		    
 		}
 	    
 	    this.poll
 	}
 }
