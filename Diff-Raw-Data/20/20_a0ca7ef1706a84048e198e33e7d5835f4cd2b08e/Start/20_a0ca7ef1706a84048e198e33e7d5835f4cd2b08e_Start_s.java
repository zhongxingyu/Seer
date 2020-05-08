 package vm;
 
 public class Start {
 	public static void main(String[] args) {
 		byte[] instructions = 
 			{0x00, 0x00, 0x00, 0x00, // 4-bytes header 
 				0x02, 0x01, //load 1 
 				0x02, 0x01, //load 1 
 				0x01,	// add
 				0x16}; 	// halt
 		
 		VM vm = new VM();
 		
 		int processID = vm.createProcess(instructions);
 		Process process = vm.getProcess(processID);
 			
 		while(process.isRunning()) {
 			vm.iterate();
 		}
 		
 		System.out.println("1 + 1 = " + process.getStack()[process.getSp()]);
 	}
 }
