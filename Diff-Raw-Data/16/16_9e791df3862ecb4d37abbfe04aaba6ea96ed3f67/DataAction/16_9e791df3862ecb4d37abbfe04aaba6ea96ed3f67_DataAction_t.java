 package bengo.data_fetcher;
 
 import bengo.Bengo;
 
 class DataAction {
 	int startCycle;
 	int neededCyles;
 	int address; // memory address
 	
 	int data;
 	
 	public DataAction(int address, int startCycle, int neededCycles, int data) {
 		this.startCycle = startCycle;
 		this.neededCyles = neededCycles;
 		this.address = address;
 		
 		this.data = data;
 		
 	}
 	
 	public boolean isReady() {
 		if (startCycle + neededCyles -1 == Bengo.CURRENT_CYCLE) {
 			return true;
 		}
		if (startCycle + neededCyles -1 < Bengo.CURRENT_CYCLE) {
 			System.err.println("somthing is wrong, this check is bad." +
 					" The fetch instructino of address " + address
 					+ " was ready at least one cycle before the check");
 			return true;
 		}
 		return false;
 	}
 	
 	public int getRemaingCycles() {
 		return Bengo.CURRENT_CYCLE - startCycle - neededCyles;
 	}
 	
	public Integer getData() {
 		if (isReady()) {
 			return data;
 		}
 		else {
 			System.err.println("Data is not ready yet");
 			return (Integer) null;
 		}
 	}
 	
 	public String toString() {
 		String s = "address " + address;
 		s += "neededCcyles " + neededCyles ;
 		s += "data " + data;
 		return s;
 	}
	
	public static void main(String[] args) {
		DataAction d = new DataAction(123, 0, 2, 1213);
		if (d.getData() != null)
			System.out.println(d.getData());
		Bengo.CURRENT_CYCLE ++;
		if (d.getData() != null)
			System.out.println(d.getData());
		Bengo.CURRENT_CYCLE ++;
	}
 }
