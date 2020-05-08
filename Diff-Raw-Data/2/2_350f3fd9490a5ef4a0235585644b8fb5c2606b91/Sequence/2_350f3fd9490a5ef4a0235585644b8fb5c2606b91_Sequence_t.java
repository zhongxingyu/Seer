 
 public class Sequence {
 	private final boolean DEBUG = false;
 	private String basepairs;
 	private String foldedStruct;
 	private int maxPairings;
 	private int [][] completedMatrix = null;
 	public String getBasepairs() {
 		return basepairs;
 	}
 
 	public void setBasepairs(String basepairs) {
 		this.basepairs = basepairs;
 		this.foldedStruct = "";
 		this.maxPairings = -1;
 		this.completedMatrix = null;
 	}
 
 	public Sequence(String basepairs) {
 		this.basepairs = basepairs.toLowerCase();
 	}
 	public String getFoldedStruct(){
 		return foldedStruct;
 	}
 	public int getMaxPairings(){
 		return maxPairings;
 	}
 	public void fold(){
 		int [][] s = getPairingMatrix();
 		maxPairings = s[0][s.length-1];
 		print(s);
 		
 	}
 	private void print(int[][]a){
 		if(!DEBUG)return;
 		for(int i =0;i<a.length;i++){
 			for(int j =0;j<a[0].length;j++){
 				//System.out.print("start:"+i+","+"end:"+j+"-"+a[i][j]+"  ");
 				System.out.print(a[i][j]+" ");
 			}
 			System.out.println();
 		}
 	}
 
 	private int [][] getPairingMatrix(){
 		int [][] m = new int [basepairs.length()][basepairs.length()];
 		//initialization
 		for(int i = 0;i<m.length;i++){
 			m[i][i]=0;
 			if(i>0){
 				m[i][i-1]=0;
 			}
 			if(i<m.length-1){
 				m[i][i+1]=(isValidPair(i,i+1)?1:0);
 			}
 		}
		for(int end=1;end<m.length;end++){
 			for(int start =end-1;start>-1;start--){
 				int max = 0;
 				if(isValidPair(start,end)){
 					if(m[start+1][end-1]+1>max)
 						max = m[start+1][end-1]+1;
 					System.out.println(start+":"+end+" is valid pair "+max);
 				}
 
 				if(m[start+1][end]>max)
 					max = m[start+1][end];
 				if(m[start][end-1]>max)
 					max = m[start][end-1];
 				for(int i =start+1;i<end;i++){
 					int curBpCount = m[start][i]+m[i+1][end];
 					if(curBpCount>max)
 						max = curBpCount;
 				}
 
 				m[start][end]=max;
 				print(m);
 			}
 		}
 		this.completedMatrix=m;
 		return m;
 	}
 	private boolean isValidPair(int x,int y){
 		char b1 = basepairs.charAt(x);
 		char b2 = basepairs.charAt(y);
 		switch(b1){
 		case 'u':
 			return b2=='g'||b2=='a';
 		case 'a':
 			return b2=='u';
 		case 'g':
 			return b2=='u'||b2=='c';
 		case 'c':
 			return b2=='g';
 		}
 		return false;
 	}
 	@Override
 	public String toString() {
 		return "Sequence [basepairs=" + basepairs + "]";
 	}
 
 }
