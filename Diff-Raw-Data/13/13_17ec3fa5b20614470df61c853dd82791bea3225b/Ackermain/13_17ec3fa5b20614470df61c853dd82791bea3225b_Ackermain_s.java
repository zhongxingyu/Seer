 class Ackermain {
 	public static void main(String[] args){
 		System.out.println(new Ackermann().calculate(3,11));
 	}
 }
 
 class Ackermann{
 	public int calculate(int m, int n){
 		int ret;
 		boolean unused;
 
 		{}{}{}{{{}}}
 
		while(false){
			m = 0;
			n = 999;
		}

 		if(m < 1){
 			ret = n+1;
 		}else{
 			if(0 < m && n < 1){
 				ret = this.calculate(m-1,1);
 			}else{
 				ret = this.calculate(m-1, this.calculate(m, n-1));
 			}
 		}
 		return ret;
 	}
 }
 
 class Random{
 	public int[] getArray(int value){
 		int[] ret;
 		ret = new int[5];
 		ret[0] = 3;
 		ret[1] = 1;
 		ret[2] = 4;
 		ret[3] = ret[ret[ret[ret[1]]]]*ret[1]+0;
 		ret[4] = ret.length;
 		return ret;
 	}
 }
