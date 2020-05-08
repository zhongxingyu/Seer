 package hx.bankcheck.accountvalidator;
 
 import hx.bankcheck.accountvalidator.exceptions.IllegalAccountNumberException;
 import hx.bankcheck.accountvalidator.exceptions.IllegalBankNumberException;
 import hx.bankcheck.accountvalidator.exceptions.ValidationException;
 import hx.bankcheck.accountvalidator.exceptions.ValidatorUnknownException;
 
 /**
  * 
  * @author Tobias Mayer (bankcheck@tobiasm.de)
  * 
  * 
  *         $Id$
  */
 public class ChecksumValidatorFactory {
 	private final static String CLASS_BASE_NAME = ChecksumValidatorFactory.class
 			.getPackage().getName()
 			+ ".impl.Checksum";
 
 	/**
 	 * 
 	 * @author Tobias Mayer (bankcheck@tobiasm.de)
 	 * 
 	 * 
 	 *         $Id$
 	 */
 	class NumberValidatingWrapper implements ChecksumValidator {
 
 		private final ChecksumValidator wrappedValidator;
 
 		public NumberValidatingWrapper(ChecksumValidator wrappedValidator) {
 			this.wrappedValidator = wrappedValidator;
 		}
 
 		@Override
 		public boolean validate(int[] accountNumber, int[] bankNumber)
 				throws ValidationException {
 			checkAccountNumber(accountNumber);
 			checkBankNumber(bankNumber);
 
 			return wrappedValidator.validate(accountNumber, bankNumber);
 		}
 
 		/**
 		 * Checks the accountNumber for plausibility
 		 * 
 		 * @param accountNumber
 		 * @throws ValidationException
 		 */
 		private void checkAccountNumber(int[] accountNumber)
 				throws ValidationException {
 			if (accountNumber == null)
 				throw new IllegalAccountNumberException(
 						"accountNumber may not be null");
 
 			if (accountNumber.length != 10)
 				throw new IllegalAccountNumberException(
 						"accountNumber has to have 10 digits");
 
 			for (int i = 0; i < 10; i++) {
 				if (accountNumber[i] > 9 || accountNumber[i] < 0) {
 					throw new IllegalAccountNumberException("Value "
 							+ accountNumber[i] + " for digit " + (i + 1)
 							+ " not allowed");
 				}
 			}
 		}
 
 		/**
 		 * Checks the bankNumber for plausibility
 		 * 
 		 * @param bankNumber
 		 * @throws ValidationException
 		 */
 		private void checkBankNumber(int[] bankNumber)
 				throws ValidationException {
 			if (bankNumber != null) {
 				if (bankNumber.length != 8)
 					throw new IllegalBankNumberException(
 							"bankNumber has to have 8 digits");
 
				for (int i = 0; i < 8; i++) {
 					if (bankNumber[i] > 9 || bankNumber[i] < 0) {
 						throw new IllegalAccountNumberException("Value "
 								+ bankNumber[i] + " for digit " + (i + 1)
 								+ " not allowed");
 					}
 				}
 			}
 		}
 
 		/**
 		 * Returns the wrapped ChecksumValidator
 		 * 
 		 * @return
 		 */
 		public ChecksumValidator getWrappedValidator() {
 			return wrappedValidator;
 		}
 
 		@Override
 		public int getAlternative() {
 			return wrappedValidator.getAlternative();
 		}
 
 		@Override
 		public boolean isException() {
 			return wrappedValidator.isException();
 		}
 
 	}
 
 	/**
 	 * Return validators, that checks the accountNumber for common mistakes
 	 */
 	private boolean numberValidating = false;
 
 	/**
 	 * Returns whether created Validators check the accountNumber for common
 	 * mistakes or not
 	 * 
 	 * @return
 	 */
 	public boolean isNumberValidating() {
 		return numberValidating;
 	}
 
 	/**
 	 * Sets whether created Validators should check the accountNumber for common
 	 * mistakes or not
 	 * 
 	 * @param numberValidating
 	 */
 	public void setNumberValidating(boolean numberValidating) {
 		this.numberValidating = numberValidating;
 	}
 
 	/**
 	 * Creates the corresponding ChecksumValidator
 	 * 
 	 * @param code
 	 *            Code, usually consists of two letters
 	 * @return
 	 * @throws ValidatorUnknownException
 	 *             Thrown when code is unknown
 	 */
 	public ChecksumValidator createValidatorFor(String code)
 			throws ValidatorUnknownException {
 		String className = CLASS_BASE_NAME + code;
 
 		try {
 			@SuppressWarnings("unchecked")
 			Class<ChecksumValidator> clazz = (Class<ChecksumValidator>) Class
 					.forName(className);
 
 			ChecksumValidator validator = clazz.newInstance();
 
 			if (isNumberValidating()) {
 				validator = new NumberValidatingWrapper(validator);
 			}
 
 			return validator;
 		} catch (ClassNotFoundException e) {
 			throw new ValidatorUnknownException("Unknown Code: " + code, e);
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e.getMessage(), e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e.getMessage(), e);
 		}
 	}
 
 }
