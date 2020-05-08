 public class MEMWB implements Clockable{
 	// inputs
 	public Pin readData = new Pin();
 	public Pin genALUresult = new Pin();
 	public Pin rd = new Pin();
 	// outputs
 	public Pin outReadData = new Pin();
 	public Pin outGenALUresult = new Pin();
 	public Pin outRd = new Pin();
 	// control signals (WB)
 	public Pin regWrite = new Pin();
 	public Pin memToReg = new Pin();
 	public Pin outregWrite = new Pin();
 	public Pin outmemToReg = new Pin();
 	
 	public void clockEdge(){
 		outReadData.setValue(readData.getValue());
		outGenALUresult.setValue(readData.getValue());
 		outregWrite.setValue(regWrite.getValue());
 		outmemToReg.setValue(memToReg.getValue());
 		outRd.setValue(rd.getValue());
 	}
 }
