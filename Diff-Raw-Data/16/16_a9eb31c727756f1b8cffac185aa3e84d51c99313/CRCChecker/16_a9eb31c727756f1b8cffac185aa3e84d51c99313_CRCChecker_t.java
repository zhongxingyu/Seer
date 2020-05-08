 package flask.codes.checker;
 
 import flask.codes.generator.CRCGenerator;
 import flask.type.Bit;
 import flask.type.BitPattern;
 
 public class CRCChecker {
 
 	public static boolean check(BitPattern codeword, Bit[] polynomial, int crcLength) {
 		Bit[] code = codeword.values();
 
 		for (int i = 0; i < code.length - crcLength; i++)
			if (code[i].is1()) CRCGenerator.divideAt(code, polynomial, i);
 
 		boolean syndrome = CRCGenerator.isAllZero(code, 0);
 		return syndrome;
 	}
 
 }
