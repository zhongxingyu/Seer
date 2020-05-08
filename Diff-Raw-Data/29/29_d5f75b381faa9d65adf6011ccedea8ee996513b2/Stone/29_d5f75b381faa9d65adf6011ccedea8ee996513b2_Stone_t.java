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
 	

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
 			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stone other = (Stone) obj;
		if (finishNum != other.finishNum)
			return false;
		if (startNum != other.startNum)
			return false;
		return true;
 	}

	@Override
	public int hashCode() {
		return startNum * 10 + finishNum;
 	}
 	
 	public boolean canAttach(Stone stone){
 		return this.hasNumber(stone.getStart()) || this.hasNumber(stone.getFinish());
 	}
 }
