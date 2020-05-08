 package Register;
 
 public class Ins_rsrt extends RegisterInstruction {
	public static final int[] FUNCTION_CODE = {24, 25, 26, 27, 48, 49, 50, 51, 52, 54, 0, 1, 4, 5};
	public static final String[] FUNCTION_NAME = { "mult", "multu", "div", "divu", "tge", "tgeu", "tlt", "tltu", "teq", "tne", "madd", "maddu", "msub", "msubu"};
 	
 	public Ins_rsrt(String binaryString) {
 		super(binaryString);
 		// TODO Auto-generated constructor stub
 		_rs = binaryToReg(getRs());
 		_rt = binaryToReg(getRt());
 	}
 
 	@Override
	public void printMnemonic() { 
		_functionName = getNameFromCode(FUNCTION_NAME, FUNCTION_CODE, Integer.valueOf(_functionCode, 2));
 		System.out.println(_functionName + " " + _rs  + " " + _rt);
 	}
 
 }
