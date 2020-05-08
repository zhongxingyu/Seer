 
 public class ALUControl {
 	public final Pin func = new Pin();
 	public final Pin aluOp = new Pin();
 	public final Pin aluControl = new Pin();
 	
 	public ALUControl(){}
 	
 	public final void update(){
		String funcString = Long.toBinaryString(func.getValue());
 		int funcLen = funcString.length();
 		boolean f0 = funcString.substring(funcLen-1,funcLen).equals("1");
 		boolean f1 = funcString.substring(funcLen-2,funcLen-1).equals("1");
 		boolean f2 = funcString.substring(funcLen-3,funcLen-2).equals("1");
 		boolean f3 = funcString.substring(funcLen-4,funcLen-3).equals("1");
		String aluOpString = Long.toBinaryString(aluOp.getValue());
 		int aluOpLen = aluOpString.length();
 		boolean aluOp0 = aluOpString.substring(aluOpLen-1,aluOpLen).equals("1");
 		boolean aluOp1 = aluOpString.substring(aluOpLen-2,aluOpLen-1).equals("1");
 		boolean aluControl0 = aluOp1 && (f0 || f3);
 		boolean aluControl1 = !aluOp1 || !f2;
 		boolean aluControl2 = aluOp0 || (aluOp1 && f1);
 		String aluControlString = "";
 		if(aluControl2){
 			aluControlString += "1";
 		} else {
 			aluControlString += "0";
 		}
 		if(aluControl1){
 			aluControlString += "1";
 		} else {
 			aluControlString += "0";
 		}
 		if(aluControl0){
 			aluControlString += "1";
 		} else {
 			aluControlString += "0";
 		}
 		aluControl.setValue(Long.parseLong(aluControlString,2));
 	}
 }
