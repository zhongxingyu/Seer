 /**
  * Each StateNode object is a state of the puzzle
  */
 /**
  * @author LDS
  *
  */
 import java.lang.Math;
 
 public class StateNode {
 	
 	public int [] seq;//seq stands for sequence/ current arrangement
 	int rel;//rel stands for relevence
 	int space;//index of the empty space
 	//constructor
 	public StateNode(int [] seq){
 		//if seq.length==9;
 		this.seq=seq;
 		rel=difference(EightPuzzle.Goal);
 	}
 	
 	public StateNode(int [] seq, int index){
 		//if seq.length==9;
 		this.seq=seq;
 		rel=difference(EightPuzzle.Goal);
 		space=index;
 	}
 	
 	//tell how many digits different of two states
 	public int difference(StateNode sn)
 	{
 		int count=0;
 		for (int i=0;i<9;i++)
 			count=this.seq[i]==sn.seq[i]?count:count+1;
 		return count;
 	}
 	
 	//make movement according to user input	
 	public void operate(int nextmove){
 		int posO, pos_nextmove;//index number of 0 and nextmove
 		posO=9;
 		pos_nextmove=9;
 		for(int i=0;i<9;i++)
 		{
 			if(seq[i]==0) 
 			{
 				posO=i;
 				continue;
 			}
 			if(seq[i]==nextmove)
 			{
 				pos_nextmove=i;
 				continue;
 			}
 		}
 		if (posO==9||pos_nextmove==9||posO==pos_nextmove)
 		    {
 			System.out.println("Invalid block number");
 		    }
 		else{
 			if (posO%3==pos_nextmove%3||(int)(posO/3)==(int)(pos_nextmove/3))
 			    {
 				seq[posO]=seq[pos_nextmove];
 				seq[pos_nextmove]=0;
 			    }
 			else 	System.out.println("Invalid block number");
 		    }
	}
 	
 	void print()
 	{
 		System.out.println(seq[0]+" "+seq[1]+" "+seq[2]);
 		System.out.println(seq[3]+" "+seq[4]+" "+seq[5]);
 		System.out.println(seq[6]+" "+seq[7]+" "+seq[8]);
 	}	
 }
