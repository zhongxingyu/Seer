 package computer;
 
 import programs.Factorial;
 import structure.LongMemory;
 import structure.Memory;
 import structure.Program;
 
 public class Computer {
 	public static Memory memory;
 	private Program currentProgram;
 	
 	public static void main(String[] args) {
 		Program factorial = new Factorial();
 		System.out.println(factorial);
 		Computer computer = new Computer(new LongMemory(64));
 		computer.load(factorial);
 		computer.run();
 	}
 
 	public Computer(Memory memory) {
 		Computer.memory = memory;
 	}
 	
 	public void load(Program p) {
 		memory.clear();
 		currentProgram = p;
 		
 		for(int i = 0; i < currentProgram.size(); i++) {
 			System.out.println(i + " " + currentProgram.get(i).toString());
 		}
 	}
 	
 	public void run() {
 		currentProgram.execute();
 	}
 }
