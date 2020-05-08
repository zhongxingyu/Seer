 package hx.bankcheck.accountvalidator.impl;
 
 import hx.bankcheck.accountvalidator.ChecksumValidator;
 import hx.bankcheck.accountvalidator.exceptions.IllegalAccountNumberException;
 import hx.bankcheck.accountvalidator.exceptions.ValidationException;
 import hx.bankcheck.accountvalidator.utils.ChecksumUtils;
 
 /**
  * Die Berechnung entspricht dem Verfahren 52, jedoch fr neunstellige
  * Kontonummern.<br/>
  * 
  * Bildung der Kontonummern des ESER-Altsystems aus angegebener Bankleitzahl und
  * angegebener neunstelliger Kontonummer:<br/>
  * 
  * BLZ: XXX5XXXX <br/>
  * Konto-Nr.: XTPXXXXXX (P = Prfziffer, T) <br/>
  * 
  * Kontonummer des ESER-Altsystems: XXTX-XP-XXXXXX <br/>
  * 
  * (XXXXXX = variable Lnge, da evtl. vorlaufende Nullen eliminiert werden).
  * 
  * Bei 10-stelligen, mit 9 beginnenden Kontonummern ist die Prfziffer nach
  * Verfahren 20 zu berechnen.
  * 
  * @author Sascha Dmer (sdo@lmis.de) - LM Internet Services AG
  * @version 1.0
  * 
  */
 public class Checksum53 implements ChecksumValidator {
 
 	private static final int[] WEIGHTS = { 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
 	private int checksumDigitIndex = -1;
 	private int[] weights;
 
 	public Checksum53(){
 		this(WEIGHTS);
 	}
 	
 	public Checksum53(int[] weights){
 		this.setWeights(weights);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see hx.bankcheck.accountvalidator.ChecksumValidator#validate(int[],
 	 * int[])
 	 */
 	@Override
 	public boolean validate(int[] accountNumber, int[] bankNumber)
 			throws ValidationException {
 		if (accountNumber[0] == 9) {
 			return new Checksum20().validate(accountNumber);
 		} else {
 			int[] eserAccountNumber = generateEserAccountNumber(accountNumber,
 					bankNumber);
 			int[] tmpAccountNumber = resetChecksumDigit(eserAccountNumber, 6);
 			if (getChecksumDigitIndex() != -1) {
 				return eserAccountNumber[getChecksumDigitIndex()] == calcChecksum(tmpAccountNumber);
 			} else {
 				throw new ValidationException(
 						"Checksum couldn't be checked, because position of checksum digit is not valid!");
 			}
 		}
 	}
 
 	protected int calcChecksum(int[] accountNumber) {
 		int sum = 0;
 		for (int i = 0; i < WEIGHTS.length; i++) {
			sum += accountNumber[i] * WEIGHTS[i];
 		}
 		int offcut = sum % 11;
 		for (int i = 0; i < 11; i++) {
 			if ((offcut + (i * WEIGHTS[getChecksumDigitIndex()]) % 11) == 10) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Generates the account number for the ESER system.
 	 * 
 	 * @author Sascha Dmer (sdo@lmis.de) - LM Internet Services AG
 	 * @version 1.0
 	 * 
 	 * @param accountNumber
 	 *            The account number.
 	 * @param bankNumber
 	 *            The bank number.
 	 * @return The account number for the ESER system
 	 * @throws ValidationException
 	 */
 	public int[] generateEserAccountNumber(int[] accountNumber, int[] bankNumber)
 			throws IllegalAccountNumberException {
 		long eserAccountNumber = 0l;
 		if ((accountNumber[0] != 0) || (accountNumber[1] == 0)) {
 			throw new IllegalAccountNumberException(
 					"Account number for generating old ESER-System account number need to have 9 relevant digits. First digits has to be 0, second digit has to be between 1 and 9.");
 		} else {
 			// First 4 digits are the last 4 digits of the bank number with the
 			// second digit of the account number at third place
 			for (int i = 0; i < 4; i++) {
 				if (i != 2) {
 					eserAccountNumber *= 10;
 					eserAccountNumber += bankNumber[(bankNumber.length)
 							- (4 - i)];
 				} else {
 					eserAccountNumber *= 10;
 					eserAccountNumber += accountNumber[2];
 				}
 			}
 
 			// ESER account number at 4,5 = first and third digit of the account
 			// number
 			// ESER account number is filled up with the rest of the account
 			// number. Leading digits which are 0, are skipped.
 			boolean foundDigitNotZero = false;
 			for (int i = 1; i < accountNumber.length; i++) {
 				if (i != 2) {
 					if ((i == 1) || (i == 3)
 							|| ((accountNumber[i] == 0) && (foundDigitNotZero))
 							|| (accountNumber[i] != 0)) {
 						eserAccountNumber *= 10;
 						eserAccountNumber += accountNumber[i];
 						if ((i != 2) && (i != 3)) {
 							foundDigitNotZero = true;
 						}
 					}
 				}
 			}
 
 		}
 
 		return ChecksumUtils.parseEserAccountNumber(eserAccountNumber);
 	}
 
 	/**
 	 * Resets the checksum digit at the given position to 0.
 	 * 
 	 * @author Sascha Dmer (sdo@lmis.de) - LM Internet Services AG
 	 * @version 1.0
 	 * 
 	 * @param accountNumber
 	 * @param checksumDigitPositon
 	 * @return
 	 */
 	public int[] resetChecksumDigit(int[] accountNumber,
 			int checksumDigitPositon) {
 		int[] tmpAccountNumber = accountNumber.clone();
 		int j = 0;
 		for (int i = 0; i < tmpAccountNumber.length; i++) {
 			if ((tmpAccountNumber[i] != 0) && (j >= 0)) {
 				j++;
 			}
 			if (j == checksumDigitPositon) {
 				tmpAccountNumber[i] = 0;
 				setChecksumDigitIndex(i);
 			}
 		}
 		return tmpAccountNumber;
 	}
 
 	/**
 	 * @param checksumDigitIndex
 	 *            the checksumDigitIndex to set
 	 */
 	public void setChecksumDigitIndex(int checksumDigitIndex) {
 		this.checksumDigitIndex = checksumDigitIndex;
 	}
 
 	/**
 	 * @return the checksumDigitIndex
 	 */
 	public int getChecksumDigitIndex() {
 		return checksumDigitIndex;
 	}
 
 	/**
 	 * @param weights the weights to set
 	 */
 	public void setWeights(int[] weights) {
 		this.weights = weights;
 	}
 
 	/**
 	 * @return the weights
 	 */
 	public int[] getWeights() {
 		return weights;
 	}
 
 }
