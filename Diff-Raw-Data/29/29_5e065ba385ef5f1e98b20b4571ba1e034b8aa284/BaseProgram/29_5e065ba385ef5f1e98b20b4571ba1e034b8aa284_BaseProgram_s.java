 import java.util.Scanner;
 
 class BaseProgram {
	static final int INIT_MAX_MEM = 1000000;
	static int stackSize = INIT_MAX_MEM;
    static int[] stack = new int[stackSize];
     static int sp = 0;
    static int $eq (int a,int b) { return a == b ? 1 : 0; }
    static int $neq(int a,int b) { return a != b ? 1 : 0; }
    static int $lt (int a,int b) { return a < b  ? 1 : 0; }
    static int $gt (int a,int b) { return a > b  ? 1 : 0; }
    static int $leq(int a,int b) { return a <= b ? 1 : 0; }
    static int $geq(int a,int b) { return a >= b ? 1 : 0; }    
    
    static void copy_(int destPointer, int sourcePointer, int size) {
    	for(int i = 0 ; i < size ; i++){
    			stack[destPointer+i] = stack[sourcePointer+i];
    	}
    }
    
    static void assureStack_(int size) {
    	if(sp + size > stackSize ) {
    			int[] newStack = new int[stackSize * 2];
    			for(int i = 0 ; i < stackSize ; i++){
    				newStack[i] = stack[i];
    			}
    			stackSize*=2;
    			stack = newStack;
    	}
    }
     
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
