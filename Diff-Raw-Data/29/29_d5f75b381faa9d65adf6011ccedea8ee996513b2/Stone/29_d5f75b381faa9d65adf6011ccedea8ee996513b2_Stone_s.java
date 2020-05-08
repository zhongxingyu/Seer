 package domino;
 
 final public class Stone {
 	final private int startNum;
 	final private int finishNum;
 	
 	public Stone(int startNum, int finishNum){
 		this.startNum = startNum;
 		this.finishNum = finishNum;
 	}
 	
 	public Stone(){
 		startNum = 0;
 		finishNum = 0;
 	}
 	
  	int getStart(){
 		return startNum;
 	}
 	
 	int getFinish(){
 		return finishNum;
 	}
 	
 /*	private void setStart(int startNum){
 		this.startNum = startNum;
 	}
 	
 	private void setFinish(int finishNum){
 		this.finishNum = finishNum;
 	}*/
 	
 	public boolean hasNumber(int number){
 		return ((getStart() == number) || (getFinish() == number));
 	}
 	
 	public boolean isDoubles(){
 		return getStart() == getFinish();
 	}
 	
 	public int getScore(){
 		return getStart() + getFinish();
 	}
 	
	public boolean equals(Stone stone){
		if(stone == null)
			return false;
		if(this.hashCode() == stone.hashCode())
 			return true;
		else return false;
 	}
	
	public int hashCode(){
		return getStart() * 10 + getFinish();
 	}
 	
 	public boolean canAttach(Stone stone){
 		return this.hasNumber(stone.getStart()) || this.hasNumber(stone.getFinish());
 	}
 }
