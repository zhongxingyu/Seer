 package banjo.parser.ast;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Collection;
 
 import banjo.parser.BanjoParser;
 import banjo.parser.BanjoParser.BanjoParseException;
 import banjo.parser.util.FilePos;
 import banjo.parser.util.ParserReader;
 import banjo.parser.util.Token;
 
 public class NumberLiteral extends Expr {
 	final Token token;
 	final Number number;
 	
 	public NumberLiteral(Token token, Number number) {
 		this.token = token;
 		this.number = number;
 	}
 	
 	public static NumberLiteral parseNumberLiteral(ParserReader in, Collection<BanjoParseException> errors) throws IOException {
 		FilePos startPos = in.getFilePos();
 		String ignored = BanjoParser.consumeWhitespace(in);
 		FilePos tokenStartPos = in.getFilePos();
 		int cp = in.read();
 		boolean negative = false;
 		boolean isNumber = false;
 		if(cp == '-') {
 			// Leading '-' for a negative number of any kind
 			negative = true;
 			cp = in.read();
 		} else if(cp == '+') {
 			// Leading + is OK (but useless)
 			cp = in.read();
 		}
 		// Check for a different base
 		int radix = 10;
 		String formatName = "decimal";
 		int maxLongDigits = 18; // the most decimal digits that can SAFELY be kept in a 62-bit integer (we leave one for the sign bit)
 		if(cp == '0') {
 			isNumber = true;
 			cp = in.read();
 			if(cp == 'x' || cp == 'X') { radix = 16; maxLongDigits=15; formatName="hexadecimal"; }
 			else if(cp == 'b' || cp == 'B') { radix = 2; maxLongDigits=62; formatName="binary"; }
 			else if(cp == 'o' || cp == 'O') { radix = 8; maxLongDigits=20; formatName="octal"; }
 		}
 		FilePos afterDigits = in.getFilePos();
 		int digits = 0;
 		int digitsLeftOfDecimalPoint = -1;
 		int exp = 0;
 		long intValLong = 0;
 		BigInteger intValBig=null;
 		BigInteger radixBig=null;
 		for(;;) {
 			if(cp == '.') {
 				if(radix != 10) {
 					errors.add(new BanjoParser.UnexpectedDecimalPoint("Decimal point found in "+formatName+" number", in.getFileRange(afterDigits)));
 					radix = 10;
 				}
 				if(digitsLeftOfDecimalPoint != -1) {
 					if(!isNumber) {
 						in.seek(startPos);
 						return null;
 					} else {
 						errors.add(new BanjoParser.UnexpectedSecondDecimalPoint("Second decimal point in number", in.getFileRange(afterDigits)));
 						in.seek(afterDigits);
 						break;
 					}
 				} else {
 					digitsLeftOfDecimalPoint = digits;
 					cp = in.read();
 				}
 			} else if(isNumber && cp == '_') {
 				// Allow underscore to "break up" long numbers, like in Java
 			} else if(cp == 'e' || cp == 'E') {
 				// Can't start a number with an exponent
 				if(!isNumber) {
 					in.seek(startPos);
 					return null;
 				}
 				// Exponent
 				if(radix != 10) {
 					errors.add(new BanjoParser.UnexpectedExponent("Exponent found in "+formatName+" number", in.getFileRange(afterDigits)));
 					// Continue to consume any number that follow anyway, we might recover somewhat
 				}
 				cp = in.read();
 				boolean negexp = cp == '-';
 				if(negexp || cp == '+')
 					cp = in.read();
 				for(int expDigits = 0; expDigits < 10; expDigits++) {
 					int digitValue = Character.digit(cp, 10); // Always base 10
 					if(digitValue == -1) {
 						in.seek(afterDigits);
 						break;
 					}
 					exp = exp * 10 + digitValue;
 					afterDigits = in.getFilePos();
 					cp = in.read();
 				}
 				if(negexp) exp = -exp;
 				break;
 			} else {
 				int digitValue = Character.digit(cp, radix);
 				if(digitValue == -1) {
 					if(!isNumber) {
 						in.seek(startPos);
 						return null;
 					} else {
 						// Number ends when we find any non-number character
 						in.seek(afterDigits);
 						break;
 					}
 				} else {
 					if(intValBig != null) {
 						intValBig = intValBig.multiply(radixBig).add(BigInteger.valueOf(digitValue));
 					} else if(digits == maxLongDigits) {
 						radixBig = BigInteger.valueOf(radix);
 						intValBig = BigInteger.valueOf(intValLong).multiply(radixBig).add(BigInteger.valueOf(digitValue));
 					} else {
 						intValLong = (intValLong * radix) + digitValue;
 					}
 					digits ++;
 					isNumber = true;
 					afterDigits = in.getFilePos();
 					cp = in.read();
 				}
 			}
 		}
 		final boolean isInteger = digitsLeftOfDecimalPoint == -1;
 		final int scale = (isInteger ? 0 : digits - digitsLeftOfDecimalPoint) - exp; // TODO Check for overflow on the scale; we might have to restrict scale to 32 bits
 		Number number;
 		if(intValBig == null) {
 			if(negative) {
 				intValLong = -intValLong;
 			}
 			if(isInteger) {
 				number = Long.valueOf(intValLong);
 			} else {
 				number = BigDecimal.valueOf(intValLong, scale);
 			}
 		} else {
 			if(negative) {
 				intValBig = intValBig.negate();
 			}
 			if(isInteger) {
 				number = intValBig;
 			} else {
 				number = new BigDecimal(intValBig, scale);
 			}
 		}
 		Token token = in.readTokenFrom(tokenStartPos, ignored);
 		return new NumberLiteral(token, number);
 	}
 
 	public Number getNumber() {
 		return number;
 	}
 }
