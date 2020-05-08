 package hx.bankcheck.accountvalidator.impl;
 
 import hx.bankcheck.accountvalidator.ChecksumValidator;
 import hx.bankcheck.accountvalidator.exceptions.ValidationException;
 
 /**
  * <b>Variante 1: </b><br/>
  * 
  * Modulus 11, Gewichtung 2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,3<br/>
  * 
  * Kontonummern,die an der 1. Stelle der 10-stelligen Kontonummer den Wert 1-9
  * beinhalten, sind nach der Methode 20 zu prfen. Alle anderen Kontonummern
  * sind nach der Variante 2 zu prfen. <br/>
  * 
  * Testkontonummer (richtig): 9110000000<br/>
  * Testkontonummer (falsch): 9111000000 <br/>
  * 
  * <b>Variante 2: </b><br/>
  * 
  * Modulus 11, Gewichtung 2, 4,8, 5, 10, 9, 7, 3, 6, 1, 2, 4 <br/>
  * 
  * Die Berechnung erfolgt nach der Methode 53.<br/>
  * 
  * Testkontonummer (richtig) mit BLZ 80053782: 487310018 <br/>
  * Testkontonummer (falsch) mit BLZ 80053762: 467310018 <br/>
  * Testkontonummer (falsch) mit BLZ 80053772: 477310018<br/>
  * 
  * @author Sascha Dmer (sdo@lmis.de) - LM Internet Services AG
  * @version 1.0
  * 
  */
 public class ChecksumB6 implements ChecksumValidator {
 
 	private static final int[] WEIGHTS_ALTERANTIVE1 = { 3, 9, 8, 7, 6, 5, 4, 3,
 			2 };
 	private static final int[] WEIGHTS_ALTERANTIVE2 = { 4, 2, 1, 6, 3, 7, 9,
 			10, 5, 8, 4, 2 };
 	private int alternative = 0;
 
 	@Override
 	public boolean validate(int[] accountNumber, int[] bankNumber)
 			throws ValidationException {
 		if (accountNumber[0] == 0) {
 			return new Checksum53(WEIGHTS_ALTERANTIVE2).validate(accountNumber,
 					bankNumber);
 		} else {
 			return new Checksum20(WEIGHTS_ALTERANTIVE1).validate(accountNumber);
 		}
 	}
 
 	/**
 	 * @param alternative
 	 *            the alternative to set
 	 */
 	public void setAlternative(int alternative) {
 		this.alternative = alternative;
 	}
 
 	/**
 	 * @return the alternative
 	 */
 	public int getAlternative() {
 		return alternative;
 	}
 
 }
