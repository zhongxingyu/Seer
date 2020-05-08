 import java.util.Scanner;
 
 class BaseProgram {
	static int STACK_SIZE = 1000000;
    static int[] stack = new int[STACK_SIZE];
     static int sp = 0;
     
     static void Read() {
       while(true){
           try{
         	stack[stack[sp]] = new Scanner(System.in).nextInt();
             System.out.printf("Read: %d\n",stack[stack[sp]]);
             return;
           } catch(Exception e){ System.out.printf("Didn't catch that, come again?\n");}
       }
     }
     
     static void Write(){
       System.out.printf("%d ",stack[sp]);
     }
     
     static void WriteLn(){
       System.out.printf("\n");
     }
 }
