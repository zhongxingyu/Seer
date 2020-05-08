 public class RegisterFile implements Clockable{
 	
 	public Register[] regFile;
 	
 	public Pin writeReg = new Pin();
 	public Pin readReg1 = new Pin();
 	public Pin readReg2 = new Pin();	
 	public Pin writeData = new Pin();
 	public Pin regWrite = new Pin();
 	public Pin readData1 = new Pin();
 	public Pin readData2 = new Pin();
 	
 	public RegisterFile(){
 		
 		// initializes with 32 words of memory set to 0
 		regFile = new Register[32];
 		for(int x=0; x<32; x++){
 			regFile[x] = new Register(0);
 		}
 	}
 	
 	// gets the value stored at register $'index'
 	public long getVal(int index){
 		return regFile[index].getValue();
 	}
 	
 	// updates value at register $'index' with 'val'
 	public void setRegister(int index, long val){
 		if(index >=1 && index < 32){
 			regFile[index].setValue(val);
 		}
 		else throw new Error("Cannot access $r" + index);
 	}
 	
	public void clockEdge(){
		System.out.println("[DEBUG] Class:RegisterFile");
 		// if regWrite is 1, then we write to the register
 		// which is stored in writeReg. Data is stored in writeData;
		if(regWrite.getValue() == new Long(1)){
 			setRegister(writeReg.getValue().intValue(), writeData.getValue());
			System.out.println("writeData:" + BinaryUtil.pad(Long.toBinaryString(writeData.getValue()), 32));
		}else System.out.println("writeData: null");
 		readData1.setValue(readReg1.getValue());
 		readData2.setValue(readReg2.getValue());
		
		System.out.println("readRegister1:" + BinaryUtil.pad(Long.toBinaryString(readReg1.getValue()), 32) +
					      "\nreadRegister2:" + BinaryUtil.pad(Long.toBinaryString(readReg2.getValue()), 32) +
					      "\nwriteRegister:" + BinaryUtil.pad(Long.toBinaryString(writeReg.getValue()), 32) +
					      "\nreadData1:" + BinaryUtil.pad(Long.toBinaryString(readData1.getValue()), 32) +
					      "\nreadData2:" + BinaryUtil.pad(Long.toBinaryString(readData2.getValue()), 32));
 	}
 	
 }
