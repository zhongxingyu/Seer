 package flask.codes.checker;
 
import flask.BitUtil;
 import flask.codes.generator.CRCGenerator;
 import flask.type.Bit;
 import flask.type.BitPattern;
 
 public class CRCChecker {
 
 	public static boolean check(BitPattern codeword, Bit[] polynomial, int crcLength) {
		BitPattern polynomialPattern = new BitPattern();
		polynomialPattern.set(BitUtil.toInteger(polynomial), crcLength);

 		Bit[] code = codeword.values();
		Bit[] poly = polynomialPattern.values();
 
 		for (int i = 0; i < code.length - crcLength; i++)
			if (code[i].is1()) CRCGenerator.divideAt(code, poly, i);
 
 		boolean syndrome = CRCGenerator.isAllZero(code, 0);
 		return syndrome;
 	}
 
 }
