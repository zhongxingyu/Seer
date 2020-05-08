 class IntQueue {
     //Grace Monk and Elana Simon
 
 
     private int[] q;
     private int front=0;
     private int back=0;
 
 
     public IntQueue() {
     }
 
     public IntQueue(int size)
     {
 	q = new int[size];
     }
 
     void put(int i){
 	q[back]=i;
 	back= (back+1)%q.length;
 	if(front==back) resize();
     }
 
     int get() {
 	int temp = q[front];
 	front = (front+1)%q.length;
 	return temp;
     }
 
     boolean isEmpty()
     {
 	return front==back;
     }
 
     private void resize()
     {
 	//to be completed
     }
 
     int size()
     {
 	return Math.abs(front-back);
     }
 
    /*
     * getAll
     * gets the entire queue
     * @author Allegra Simon and Alex Fisher
     */
 
     int[] getAll()
 	{
 	    int[] fullQueue = new int[this.size()];
 
 	    for(int i=0; i<this.size(); i++)
 		{
 			
 			fullQueue[i] = q[i];
 			this.get();
 		}
 
 		return fullQueue;
 	}
 
     /*
      *Casey, Rachel
      *Converts queue to a string
      */
     String convertString()
     {
 	String s = "";
 
 	//go through the whole array
 	//    add  it to s
 
        	if(front < back)
 	{
 	    for(int i=front; i<back; i++) {
 		s+=q[i];
 	    }
 	  
 	 }
 	else if (back < front)
 	    {
 		for(int i=front; i<q.length; i++) {
 		    s+=q[i];
 		}
 		for (int i=0; i<back; i++){
 		    s+=q[i];
 		}
 	 
 	    }
 	
 	return s;
     }
 
 
 
 
     //Grace Monk Elana Simon
     int[] get(int x)
     {
 	int[] mini = new int[x];
 	int counter=0;
 	while(counter<x)
 	    {
 		mini[counter++]= this.get();
 	    }
 	return mini;
     }
 
     /*
      *Subque
      *Allows the user to create a subque.
      *@author Zachary Buttenwieser
      *@author Adam Phillips
      *@param s the place you want to start
      *@param e the place you want to end
      */
     public IntQueue subQ(int s, int e) {
 	IntQueue subQ = new IntQueue(40);
 	if(s<e){
 	    for (int i = s; i < e; i++){
 		subQ.put(q[i]);
 	    }
 	}
 	else if (s> e ) {
 	    for (int i = s; i < q.length; i++) {
 		subQ.put(q[i]);
 	    }
 	    for (int i = 0; i < e; i++) {
 		subQ.put(q[i]);
 	    }
 	}
 	return subQ;
     }
 <<<<<<< HEAD
 
     //Sonia and Jenna
     //Mean - gets the average of the Queue 
 
     /**
 	int mean()
 	{
 	    int size = front-1;
 	    int total = 0;
 	    for (int i=0; i < top+1; i++);
 	    {
 		total+=stack[i];
 	    }
 	    total = total/top+1;
 	    return total;
 	}
 		    
 		
     **/
 
 	/*Cut
 	 *Allows the user to add an int to the front of the queue
 	 *@author Matt Durkin
 	 *@Author Alek Kiprovski
 	 *@Author Richard Chen
 	 */
     public void cut(int p) {
 	boolean run = true;
 	while (run == true){
 
 	    if  (front > 0) {
 		q[front-1] = p; 
 		front--;
 		run = false;
 	        }
 	    else { //shifts everything by 1
 		for (int i = back; i >= front; i--) {
 		    q[i+1] = q[i];
 		}
 		 front++;
 	    } //else
 	}//while
     }//cut method
 	
 
 
 =======
 >>>>>>> Revert "Average - Jenna and Sonia"
 
 }
