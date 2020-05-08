 package gameBoy.cpu;
 
 public class Opcodes {
 	public static int NOP = 0x00;
 	public static int LD_BC_NN = 0x01;
 	public static int LD_ADDR_BC_A = 0x02; // put A at address contained in BC
 	public static int INC_BC = 0x03;
 	public static int INC_B = 0x04;
 	public static int DEC_B = 0x05;
 	public static int LD_B_N = 0x06;
 	public static int RLC_A = 0x07;
 	public static int LD_ADDR_NN_SP = 0x08;
 	public static int ADD_HL_BC = 0x09;
 	public static int LD_A_ADDR_BC = 0x0A;
	public static int DEC_BC = 0x0B;
	
 }
