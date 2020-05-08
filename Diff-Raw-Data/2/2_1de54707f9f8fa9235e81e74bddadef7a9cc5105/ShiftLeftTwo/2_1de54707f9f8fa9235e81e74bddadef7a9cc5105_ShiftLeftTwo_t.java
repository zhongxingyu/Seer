 public class ShiftLeftTwo implements Clockable{
 	Pin in = new Pin();
 	Pin out = new Pin();
 	int outputBits;
 	
 	ShiftLeftTwo(int outputBits){
 		this.outputBits = outputBits;
 	}
 
 	public void clockEdge() {
 		String myStr = BinaryUtil.pad(Long.toBinaryString(in.getValue()), outputBits);
	    out.setValue(Long.parseLong(myStr.substring(2,outputBits) + "00", 2));
 		System.out.println("[DEBUG] Class: ShiftLeftTwo" + 
 						  "\nin:" + BinaryUtil.pad(Long.toBinaryString(in.getValue()), 32) +
 						  "\nout:" + BinaryUtil.pad(Long.toBinaryString(out.getValue()), 32));
 	}
 	
 	
 }
