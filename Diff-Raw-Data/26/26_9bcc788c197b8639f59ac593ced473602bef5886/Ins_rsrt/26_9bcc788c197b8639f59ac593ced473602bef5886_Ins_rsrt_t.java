 package Register;
 
 public class Ins_rsrt extends RegisterInstruction {
	
	public static final int[] FUNCTION_CODE_OPCODE0 = {24, 25, 26, 27, 48, 49, 50, 51, 52, 54};
	public static final String[] FUNCTION_NAME_OPCODE0 = {"mult", "multu", "div", "divu", "tge", "tgeu", "tlt", "tltu", "teq", "tne"};
	public static final int[] FUNCTION_CODE_OPCODE28 = {0, 1, 4, 5};
	public static final String[] FUNCTION_NAME_OPCODE28 = {  "madd", "maddu", "msub", "msubu"};
 	
 	public Ins_rsrt(String binaryString) {
 		super(binaryString);
 		// TODO Auto-generated constructor stub
 		_rs = binaryToReg(getRs());
 		_rt = binaryToReg(getRt());
 	}
 
 	@Override
	public void printMnemonic() {
		if(Integer.parseInt(_opCode, 2) == 0){
			_functionName = getNameFromCode(FUNCTION_NAME_OPCODE0, FUNCTION_CODE_OPCODE0, Integer.valueOf(_functionCode, 2));
		}else{
			_functionName = getNameFromCode(FUNCTION_NAME_OPCODE28, FUNCTION_CODE_OPCODE28, Integer.valueOf(_functionCode, 2));
		}
 		System.out.println(_functionName + " " + _rs  + " " + _rt);
 	}
 
 }
