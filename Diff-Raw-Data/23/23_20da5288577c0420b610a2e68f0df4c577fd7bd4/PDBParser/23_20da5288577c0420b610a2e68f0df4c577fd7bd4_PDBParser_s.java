 package edu.allatom;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.math.Vector;
 
 public class PDBParser {
 	
 	public static Protein parseFile(String filename) throws FileNotFoundException, IOException {
 		// read file
 		StringBuilder sb = new StringBuilder();
 		FileReader fr = new FileReader(filename);
 		int c;
 		while((c = fr.read()) != -1) {
 			sb.append((char) c);
 		}
 		String data = sb.toString();
 		
 		// get first model data
 		int firstModelStart = data.indexOf("\nMODEL ");
 		int secondModelStart = data.indexOf("\nMODEL ", firstModelStart + 1);
 		if (firstModelStart ==-1 ) {
 			// if no MODEL exist, just begin from first atom
 			firstModelStart = data.indexOf("\nATOM ");
			secondModelStart = data.length();
 		}
		String firstModel = data.substring(firstModelStart, secondModelStart);
 
 		List<String> atomLines = new LinkedList<String>();
 		for(String line : firstModel.split("\n")) {
 			if(line.startsWith("ATOM ")) {
 				atomLines.add(line);
 			}
 		}
 
 		int aaSeqNo = -1;
 		List<AminoAcid> aaSeq = new LinkedList<AminoAcid>();
 		AminoAcid aminoAcid = null;
 		for(String line : atomLines) {
 			int currentAASeqNo = getResSeqNo(line);
 			if(aaSeqNo != currentAASeqNo) {
 				aaSeqNo = currentAASeqNo;
 				if(aminoAcid != null) {
 					aaSeq.add(aminoAcid);
 				}
 				AminoAcidType aaType = AminoAcidType.valueOf(getAAName(line));
 				aminoAcid = new AminoAcid(aaType);
 
 			}
 			aminoAcid.addAtom(parseAtom(line));
 		}
 		if(aminoAcid != null) {
 			aaSeq.add(aminoAcid);
 		}
 		return new Protein(aaSeq);
 	}
 
 	private static int getResSeqNo(String s) {
 		return Integer.parseInt(s.substring(23, 26).trim());
 	}
 	private static String getAAName(String s) {
 		return s.substring(17, 20).trim();
 	}
 	
 	private static Vector getPosition(String s) {
 		float x = Float.parseFloat(s.substring(31, 38));
 		float y = Float.parseFloat(s.substring(39, 46));
 		float z = Float.parseFloat(s.substring(47, 54));
 		return new Vector(x,y,z);
 	}
 	
 	public static Atom parseAtom(String atomLine) {
 		Vector position = getPosition(atomLine);
 		String typeString = atomLine.substring(77, 78).trim();
 		// WARNING: in the next line, the 12 should actually be 13 to parse
 		// correct pdb files. 12 is used because some pdb files are incorrect 
 		String name = atomLine.substring(12, 16).trim();
 		Atom.Type type = Atom.Type.fromName(typeString);
 		return new Atom(type, name, position);
 	}	
 }
