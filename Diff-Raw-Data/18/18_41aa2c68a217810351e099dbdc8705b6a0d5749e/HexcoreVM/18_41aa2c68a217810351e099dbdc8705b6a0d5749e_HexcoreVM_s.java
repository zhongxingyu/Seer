 package com.hexcore.cas.rulesystems;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Stack;
 
 import com.hexcore.cas.model.Cell;
 
 public class HexcoreVM
 {
 	//CPU Functional Registers
 	int					PC;
 	String 				IR;
 	
 	//Memory
 	Stack<Integer>		oStack;
 	ArrayList<String>	instructions;
 	Cell				currentCell;
 
 	
 	//CPU Operational Registers
 	int					FP;			//Frame pointer
 	int					SP;			//Stack Pointer
 	int					r1;			//Temp Register 1
 	int 				r2;			//Temp Register 2
 	boolean				end;		//End flag
 	
 	private enum OpCodes{	LDC, LDA, LDV, STO, DSP,
 							ADD, SUB, MUL, DIV, REM,
 							CEQ, CNE, CGT, CLT, CLE, CGE,
 							AND, OR, NOT,
 							BRN, BZE,
 							END
 						};
 	
 	
 	
 	public HexcoreVM()
 	{
 		oStack = new Stack<Integer>();
 		SP = 0;
 		FP = -1;
 		end = false;
 	}
 	
 	public void loadRules(String filename)
 	{
 		try
 		{
 			Scanner in = new Scanner(new File(filename));
 			
 			while(in.hasNext())
 				instructions.add(in.nextLine());
 			
 		} catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void run(Cell cell, Cell[] neighbours)
 	{
 		PC = 0;
 		end = false;
 		
 		int[] v;
 		int[][] n;
 		v = cell.getValues();
 		n = new int[neighbours.length][v.length];
 		
 		for(int i = 0; i < neighbours.length; i++)
 		{
 			n[i] = neighbours[i].getValues();
 		}		
 		
 		while(!end)
 		{
 			IR = instructions.get(PC);
 			execute();
 			PC++;
 		}
 	}
 	
 	private void execute()
 	{
 		int index = IR.indexOf(' ');
 		
 		String operation;
 		String operand = "0";
 		
 		if(index != -1)
 		{
 			operation = IR.substring(0, index);
 			operand = IR.substring(index + 1);
 		}
 		else
 		{
 			operation = IR;
 		}
 		
 		
 		switch(OpCodes.valueOf(operation))
 		{
 			case LDC:	oStack.push(new Integer(Integer.parseInt(operand)));
 						SP++;
 						break;
 						
 			case LDA:	oStack.push(FP + 1 + Integer.parseInt(operand));
 						SP++;
 						break;
 						
 			case LDV:	r1 = oStack.pop();
 						oStack.push(oStack.get(r1));
 						break;
 						
 			case STO:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						SP -= 2;
 						oStack.set(r2, r1);
 						break;
 						
 			case DSP:	r1 = Integer.parseInt(operand);
 						for(int i = 0; i < r1; i++)
 							oStack.push(new Integer(0));
 						SP += r1;
 						break;
 						
 			case ADD:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r1 + r2);
 						SP--;
 						break;
 						
 			case SUB:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r2 - r1);
 						SP--;
 						break;
 						
 			case MUL:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r2 * r1);
 						SP--;
 						break;
 						
 			case DIV:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r2 / r1);
 						SP--;
 						break;
 						
 			case REM:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r2 % r1);
 						SP--;
 						break;
 						
 			case CEQ:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						if(r1 == r2)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						SP--;
 						break;
 						
 			case CNE:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						if(r1 != r2)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						SP--;
 						break;
 						
 			case CGT:	r1 = oStack.pop();
 						r2 = oStack.pop();
						if(r1 > r2)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						SP--;
 						break;
 						
 			case CLT:	r1 = oStack.pop();
 						r2 = oStack.pop();
						if(r1 < r2)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						SP--;
 						break;
 						
 			case CLE:	r1 = oStack.pop();
 						r2 = oStack.pop();
						if(r1 <= r2)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						SP--;
 						break;
 						
 			case CGE:	r1 = oStack.pop();
 						r2 = oStack.pop();
						if(r1 >= r2)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						SP--;
 						break;
 						
 			case AND:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r1 & r2);
 						SP--;
 						break;
 						
 			case OR:	r1 = oStack.pop();
 						r2 = oStack.pop();
 						oStack.push(r1 | r2);
 						SP--;
 						break;
 						
 			case NOT:	r1 = oStack.pop();
 						if(r1 == 0)
 							oStack.push(1);
 						else
 							oStack.push(0);
 						break;
 						
 			case BRN:	PC = Integer.parseInt(operand);
 						break;
 						
 			case BZE:	r1 = oStack.pop();
 						SP--;
 						if(r1 == 0)
 							PC = Integer.parseInt(operand);
 						break;
 			
 			case END:	end = true;
 						break;
 		}
 	}
 }
