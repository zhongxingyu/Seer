 package com.scs.security;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 public class InstructionTable {
 	/* Instruction Table */
 	private List<InstructionTableEntry> entries = new ArrayList<InstructionTableEntry>(Constants.M);
 
 	public enum Position {
 		ALPHA, BETA, BOTH;
 	};
 
 	/* load instruction table from file */
 	public static InstructionTable loadTable(String filePath) {
 		final InstructionTable iTable = new InstructionTable();
 
 		IOUtils.readFile(filePath, new IOUtils.Callback() {
 			public void processLine(String data) {
 				/* each line in the instruction table has "i alpha(i) beta(i)" */
 				String[] tokens = data.split("\\s+");
 				InstructionTableEntry entry = new InstructionTableEntry(
 						Integer.valueOf(tokens[0]), new BigInteger(tokens[1]), new BigInteger(tokens[2]));
 				iTable.addEntry(entry);
 			}
 		});
 
 		return iTable;
 	}
 
 	/* write the Instruction Table to a file */
 	public void writeToFile(String filePath){
 		List<String> lines = new ArrayList<String>(entries.size());
 		for (InstructionTableEntry ite : entries) {
 			String entry = new String(ite.getIndex() + " " + ite.getAlpha() + " " + ite.getBeta());
 			lines.add(entry);
 		}
 		IOUtils.writeToFile(lines, filePath);
 	}
 
 	/* generate Instruction Table from distinguishing features and a randomly generated polynomial */
 	public static InstructionTable generateInstructionTable(Position[] positions, Polynomial poly) {
 		InstructionTable iTable = new InstructionTable();
 		
 		for (int i = 0; i < positions.length; i++) {
 			Position position = positions[i];
 			
 			int index = i + 1;
 			BigInteger alpha = poly.evaluate(Authenticator.p_function.execute(2 * index))
 					.add(Authenticator.g_function.execute(2 * index).mod(Constants.Q));
 			BigInteger beta = poly.evaluate(Authenticator.p_function.execute(2 * index + 1))
 					.add(Authenticator.g_function.execute(2 * index + 1).mod(Constants.Q));
 
 //			System.out.println("IT: " + alpha + " , " + beta);
 			
 			// System.out.println(Authenticator.p_function.execute(2 * index) + " , " + poly.evaluate(Authenticator.p_function.execute(2 * index)));
 			
 			switch (position) {
 			case ALPHA:
 				beta = Generator.getRandomBigInteger();
 				System.out.println("Beta " + (i+1) + " corrupted!");
 				break;
 			case BETA:
 				alpha = Generator.getRandomBigInteger();
 				System.out.println("Alpha " + (i+1) + " corrupted!");
 				break;
 			case BOTH:
 				break;
 			}
 			iTable.addEntry(new InstructionTableEntry(index, alpha, beta));
 		}
 
 		return iTable;
 	}
 
 	public static Position getPosition(long feature, int index) {
		return ((feature < Constants.THRESHOLD_FEATURE_VALUES[index]) ? Position.ALPHA : Position.BETA);
 	}
 
 	public boolean addEntry(InstructionTableEntry entry) {
 		return entries.add(entry);
 	}
 
 	public InstructionTableEntry get(int i) {
 		return entries.get(i);
 	}
 }
 
 class InstructionTableEntry {
 	private int index;			/* feature index 1 to m */
 	private BigInteger alpha;	/* alpha entry for feature */
 	private BigInteger beta;	/* beta entry for feature */
 
 	public InstructionTableEntry(int index, BigInteger alpha, BigInteger beta) {
 		this.index = index;
 		this.alpha = alpha;
 		this.beta = beta;
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 
 	public BigInteger getAlpha() {
 		return alpha;
 	}
 
 	public void setAlpha(BigInteger alpha) {
 		this.alpha = alpha;
 	}
 
 	public BigInteger getBeta() {
 		return beta;
 	}
 
 	public void setBeta(BigInteger beta) {
 		this.beta = beta;
 	}
 }
