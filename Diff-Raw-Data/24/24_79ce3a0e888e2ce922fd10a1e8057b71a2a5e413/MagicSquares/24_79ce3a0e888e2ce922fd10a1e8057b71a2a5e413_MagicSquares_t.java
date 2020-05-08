 package Java;
 
 import java.util.ArrayList;
 
 public class MagicSquares {
 	
 	public static void main(String[] args){
		int[][] ms = magicSquare(5*5);
 		for(int i=0;i<ms.length;i++){
			for(int j=0;j<ms[i].length;j++)
 				System.out.print(ms[i][j]+" ");
			System.out.println();
 		}
 	}
 	
 	public static int[][] magicSquare(int num){
 		int[][] res=new int[(int) Math.sqrt(num)][(int) Math.sqrt(num)];
 		ArrayList<Integer> nums = new ArrayList<Integer>();
		for(int i=1;i<=num;i++)
 			nums.add(i);
 		System.out.println(nums);
 		int midNum=(num+1)/2;
		int magicNum=(int) ((res.length*(num+1))/2);
 		int x=0,y=res.length/2,count=0;
 		//Evens
 		while(count<=num-1){
 			if(res[x][y]==0){
 				res[x][y]=nums.get(count);
 				count++;
 				x--;
 				y++;
 			}
 			else{
 				x+=2;
 				y--;
 			}
 			if(x<0)
 				x=res.length-1;
 			if(x>=res.length)
				x=x-res.length;
 			if(y<0)
 				y=res.length-1;
 			if(y>=res.length)
				y=y-res.length;
 		}
 		/*
 		for(int i=0;i<res.length-1;i++){
 			for(int j=i%2;j<res[i].length;j+=2){
 				res[i][j]=nums.get(count);
 				res[res.length-1-i][res[i].length-1-j]=nums.get(nums.size()-1-count);
 				count+=2;
 			}
 		}count=0;
 		//Odds
 		for(int i=res.length-1;i>0;i--){
 			for(int j=(res[i].length-2)+i%2;j>0;j-=2){
 				res[i][j]=nums.get(count);
 				res[res.length-1-i][res[i].length-1-j]=nums.get(nums.size()-1-count);
 				count+=2;
 			}
 		}*/
 		//res[0][0]=midNum;
 		System.out.println("Magic Num is: "+magicNum);
 		return res;
 		
 	}
 	
 }
