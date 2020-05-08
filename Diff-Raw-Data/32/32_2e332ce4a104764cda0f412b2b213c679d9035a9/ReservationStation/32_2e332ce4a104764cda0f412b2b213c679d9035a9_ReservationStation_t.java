 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Esentially, tomasulo algorithm issues and stalls
  * instructions based on reservation station condition check
  * so it will read instructions and do couple of optimization 
  * filling into registeration station and then execute 
  * @author chen
  *
  */
 /**
  * @author chen
  *
  */
 public class ReservationStation {
 	
 	public enum STATUS {
 	    UNISSUED, ISSUE, READ,EXECUTE, WRITEBACK, DONE
 	}
 	
 	public static int clockcounter = 1;
 
 	
 	static class Execution{
 		public static MemoryType regFile = new MemoryType();
		public static MemoryType memFile = new MemoryType();
 		/**
 		 * check if destination register is occupied, if not
 		 * okay to issue
 		 * @param inst
 		 * @param status
 		 */
 		public static void tryissue(Instruction inst,ReservationEntry entry){
 			entry.tryissueInstruction();
 			//TODO : here can issue by register renaming if addational registers are avaliable
 			//trick is it will need to scan instructions list for further name updates
 		}
 		
 		public static void tryread(Instruction inst, ReservationEntry entry){
 			if(entry.isReadyToRead()){

 				if(inst.isImmediate()){
					inst.rtValue = inst.immediate + regFile.getValue(inst.rt);
					inst.rsValue = regFile.getValue(inst.rs);
				}else{
					inst.rtValue = regFile.getValue(inst.rt);
					inst.rsValue = regFile.getValue(inst.rs);
				}
 				entry.status = STATUS.READ;
 			}
 		}
 		
 		/**
 		 * check if reading registers are occupied
 		 * if not okay to read
 		 * @param inst
 		 */
 		
 		public static void tryexecute(Instruction inst,ReservationEntry entry){
 			if(entry.isReadyToRead()){
 				Instruction myInstruction = inst;
 				String str;
 		          switch(myInstruction.opcode) {
 		            case 24: // MUL -- 8 bit 
 		                  myInstruction.rdValue = (myInstruction.rsValue & 0x00FF) * 
 		                  (myInstruction.rtValue & 0x00FF);      
 		                  break;         
 		            case 26: // DIV -- 8 bit
 		                  myInstruction.rdValue = (myInstruction.rsValue & 0x00FF) / 
 		                  (myInstruction.rtValue & 0x00FF);               
 		                  break;         
 		            case 32: // ADD
 		                  myInstruction.rdValue = myInstruction.rsValue + myInstruction.rtValue;
 		                  break;         
 		            case 34: // SUB
 		                  myInstruction.rdValue = myInstruction.rsValue - myInstruction.rtValue; 
 		                  break;         
 		            case 35: // LW
		            	myInstruction.rdValue = memFile.getValue(myInstruction.rsValue + myInstruction.immediate);
		            	break;
 		            case 43: // SW
 		                  // rdValue becomes the memory address with which to load/store. 
 		                  // For SW, rtValue is the value to store into memory.
		                  myInstruction.rdValue = myInstruction.rsValue + myInstruction.immediate;
		                  memFile.putValue(myInstruction.rtValue, myInstruction.rdValue);
		                  break;         
 		            case 50: // SLL
 		                  myInstruction.rdValue = myInstruction.rsValue << myInstruction.rtValue;               
 		                  break;         
 		            case 51: // SRL              
 		                 myInstruction.rdValue = myInstruction.rsValue >> myInstruction.rtValue;               
 		                  break;         
 		            case 36: // AND               
 		                  myInstruction.rdValue = myInstruction.rsValue & myInstruction.rtValue;               
 		                  break;         
 		            case 37: // OR              
 		                  myInstruction.rdValue = myInstruction.rsValue | myInstruction.rtValue;               
 		                  break;         
 		            case 38: // XOR              
 		                  myInstruction.rdValue = myInstruction.rsValue ^ myInstruction.rtValue; 
 		                  break;         
 		            case 60: // SLT              
 		                  if (myInstruction.rsValue < myInstruction.rtValue) 
 		                  	myInstruction.rdValue = 1;
 		                  else
 		                        myInstruction.rdValue = 0;               
 		                  break;         
 		            case 61: // SLE              
 		                  if (myInstruction.rsValue <= myInstruction.rtValue) 
 		                  	myInstruction.rdValue = 1;
 		                  else
 		                        myInstruction.rdValue = 0;               
 		                  break;         
 		            case 62: // SEQ              
 		                  if (myInstruction.rsValue == myInstruction.rtValue) 
 		                  	myInstruction.rdValue = 1;
 		                  else
 		                        myInstruction.rdValue = 0;               
 		                  break;         
 		            case 63: // SGT              
 		                  if (myInstruction.rsValue > myInstruction.rtValue) 
 		                  	myInstruction.rdValue = 1;
 		                  else
 		                        myInstruction.rdValue = 0;               
 		                  break;         
 		            case 64: // SGE              
 		                  if (myInstruction.rsValue >= myInstruction.rtValue) 
 		                  	myInstruction.rdValue = 1;
 		                  else
 		                        myInstruction.rdValue = 0;               
 		                  break;         
 		            case 70: // BEQ              
 		            case 71: // BNE 
 		                  if (((myInstruction.opcode == 71) && (myInstruction.rsValue != myInstruction.rtValue)) 
 		                      || ((myInstruction.opcode == 70)&&(myInstruction.rsValue == myInstruction.rtValue))){
 //		                          myFetch.PC = myInstruction.immediate - 1; // next instruct will be br addr
 //		                          myFetch.myInstruction.flush = true;
 //		                          myDecode.myInstruction.flush = true;
 		                  }
 		                  break;
 		         }
 				entry.status = STATUS.EXECUTE;
 			}else
 				Logger.getLogger("execute").log(Level.INFO, "execute stall");
 		}
 		
 		/**
 		 * check if destination is occupied, 
 		 * if not , update map to current entry, okay to write back
 		 * @param inst
 		 */
 		public static void trywriteback(Instruction inst,ReservationEntry entry){
 			//ReservationEntry des = ReservationStation.getStation()._regmap.get(Integer.valueOf(inst.rd));
 			
 			//if this instruction is the one who occupy register
 			//if(des == entry){
 				Instruction myInstruction = inst;
 				switch( myInstruction.opcode) {
 	            case 0:   // NOP
 	            case 43:  // SW
 	            case 70:  // BEQ
 	            case 71:  // BNE
 	                break;
 	            default:
 	                 // write to dest register (if not $0)
 	                 if ((myInstruction.rd != 0) && (myInstruction.opcode != 0))
 	                      if (myInstruction.flush == false) {
 	                    		regFile.putValue( myInstruction.rdValue,myInstruction.rd );
 	                    		//if (myInstruction.rd < 10)
 	                       		//	str = "R0"+myInstruction.rd+": "+myInstruction.rdValue;
 	                    		//else
 	                       		//	str = "R"+myInstruction.rd+": "+myInstruction.rdValue;
 	                    		//lb.RF.replaceItem(str, myInstruction.rd);
 	                    		//System.out.println(regFile.getValue(myInstruction.rd));
 	                    		System.out.println(myInstruction.rdValue);
 	                  		}
 				}
 				entry.status = STATUS.DONE;
 				//release the register
 				ReservationStation.getStation()._regmap.remove(Integer.valueOf(inst.rd));
 			//}else
 			//	Logger.getLogger("writeback").log(Level.INFO, "writeback stall");
 		}
 	}
 	
 	class ReservationEntry{
 		int opcode;
 		Instruction Instr;
 		public STATUS status;
 		ReservationEntry rs;
 		ReservationEntry rt;
 		boolean qj;
 		boolean qk;
 		
 		public ReservationEntry(Instruction instroc){
 			Instr = instroc;
 			opcode = Instr.opcode;
 			this.status = STATUS.UNISSUED;
 		}
 		
 		/**
 		 * try to issue a instruction by checking if dest register has been occupied
 		 * if so , mark virtal dependecy otherwise issue this and occupy the register
 		 * @return
 		 */
 		public boolean tryissueInstruction(){
 			ReservationEntry dep = ReservationStation.getStation()._regmap.get(Integer.valueOf(Instr.rd));
 			if(dep == null || dep.status == STATUS.DONE){
 				Logger.getLogger("tryissue").log(Level.INFO, "try to ssue instruction sucess");
 				ReservationStation.getStation()._regmap.put(Integer.valueOf(Instr.rd), this);
 				this.status = STATUS.ISSUE;
 				rs = ReservationStation.getStation()._regmap.get(Integer.valueOf(Instr.rs));
 				rt = ReservationStation.getStation()._regmap.get(Integer.valueOf(Instr.rt));
 				return true;
 			}else
 				Logger.getLogger("tryissue").log(Level.INFO, "try to ssue instruction fail");
 			return false;
 		}
 		
 		/**
 		 * check if source registers are available to read
 		 * if so, read and execute
 		 * @return
 		 */
 		public boolean isReadyToRead(){
 			if(rs == null && rt == null) return true;
 			
 			if((rs != null && rs.status == STATUS.DONE) || rs == this){
 				qj = true;
 				if(rt == null || rt == this || rt.status == STATUS.DONE)
 					return true;
 			}
 			
 			if((rt != null && rt.status == STATUS.DONE) || rt == this){
 				qk = true;
 				if(rs == null || rs == this || rs.status == STATUS.DONE)
 					return true;
 			}
 			if(qj == true && qk == true)
 				return true;
 			
 			return false;
 		}
 		
 		
 		public void update(){
 			switch(this.status){
 				case UNISSUED:
 					Execution.tryissue(Instr, this);
 					break;
 				case ISSUE:
 					Execution.tryread(Instr, this);
 					break;
 				case READ:
 					Execution.tryexecute(this.Instr,this);
 					break;
 				case EXECUTE:
 					Execution.trywriteback(this.Instr,this);
 					break;
 				default:
 					break;
 			}
 				
 		}
 	}
 	
 	/**
 	 * entries in reservation station
 	 * each map to one instruction
 	 */
 	List<ReservationEntry> _resentries;
 	
 	/**
 	 * usage of each register
 	 */
 	Map<Integer,ReservationEntry> _regmap;
 	List<Instruction> _instructions;
 
 	protected static ReservationStation instance;
 	
 	/**
 	 * @return reservation station singleton instance
 	 */
 	public static ReservationStation getStation(){
 		if(instance == null)
 			instance = new ReservationStation();
 		return instance;
 	}
 
 	private ReservationStation() {
 		_resentries = new ArrayList<ReservationEntry>();
 		_instructions = new ArrayList<Instruction>();
 		_regmap = new HashMap<Integer, ReservationEntry>();
 	}
 	
 	/**
 	 * add instruction to insturction queue
 	 * @param newinst
 	 * @throws Exception
 	 */
 	public void add(Instruction newinst) throws Exception{
 		if(newinst.valid()){
 			_instructions.add(newinst);
 			_resentries.add(new ReservationEntry(newinst));
 		}else
 			throw new Exception("Instruction not valid");
 	}
 	
 	/**
 	 * called by simulator to trigger reservation internal instructions
 	 * stages change
 	 */
 	public void step(){
 		System.out.print("current cycle");
 		System.out.println(clockcounter++);
 		//update all entries in this clock, hum, may be some entries is more reasonable
 		for(ReservationEntry rentry : _resentries)
 			rentry.update();
 	}
 
 	/**
 	 * check if all instructions has been executed
 	 * if so, exit
 	 * @return
 	 */
 	public boolean isrunning() {
 		for(ReservationEntry rentry : _resentries)
 			if(rentry.status != STATUS.DONE) return true;
 		return false;
 	}
 }
