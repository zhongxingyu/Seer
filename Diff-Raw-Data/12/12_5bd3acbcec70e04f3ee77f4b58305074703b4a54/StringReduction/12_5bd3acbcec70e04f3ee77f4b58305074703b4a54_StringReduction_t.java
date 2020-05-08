 import java.io.File;
 import java.util.Scanner;
 
 
 public class StringReduction {
 
 	public static void main(String[] args) throws Exception{
 	
 		int n;
		File f = new File("./StringReduction/input01.txt");
 		Scanner in = new Scanner(f);
 		//Scanner in = new Scanner(System.in);
 		n = in.nextInt();
 		
 		for(int i=0;i<n;i++){
 			String s = in.next();
 			int j=0;
 			//System.out.println(s);
 			  while(j<s.length()-1){
                      if (s.charAt(j)!=s.charAt(j+1)){
                 	 
                      char d = getchar(s.charAt(j),s.charAt(j+1)); 
                 	 String before = s.substring(0,j);
                 	 //System.out.println("before "+before);
                 	 String after = s.substring(j+2,s.length());
                 	 //System.out.println("d "+d);
                 	 //System.out.println("after "+after);
                 	 s=before+d+after;  
                 	 //System.out.println("s == "+s);
                 	 j=0;
                      
                     }else{
                    	 j++;
                    }
                 }
 		    System.out.println(s.length());
 		}
		 in.close();  
 	}
 
 public static char getchar(char x,char y){
 
     if(x=='a' && y=='b')
         return 'c';
     else if(x=='b' && y=='c')
         return 'a';
     else if(x=='c' && y=='a')
         return 'b';
     else if(x=='a' && y=='c')
         return 'b';
     else if (x=='b' && y=='a')
     	return 'c';
     else if (x=='c' && y=='b')
         return 'a';
 	return 'z';
 
 
 
 }
 }
