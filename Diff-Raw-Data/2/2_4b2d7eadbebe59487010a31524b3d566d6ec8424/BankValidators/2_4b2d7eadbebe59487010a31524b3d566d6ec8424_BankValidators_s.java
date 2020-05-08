 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.bank.controller.validation;
 
 import static br.octahedron.commons.util.DateUtil.SHORT;
 import static br.octahedron.cotopaxi.controller.Converter.Builder.bigDecimalNumber;
 import static br.octahedron.cotopaxi.controller.Converter.Builder.date;
 import static br.octahedron.cotopaxi.validation.Input.Builder.attribute;
 import static br.octahedron.cotopaxi.validation.Rule.Builder.greaterThan;
 import static br.octahedron.cotopaxi.validation.Rule.Builder.regex;
 import static br.octahedron.cotopaxi.validation.Rule.Builder.required;
 import static br.octahedron.cotopaxi.validation.Rule.Builder.type;
 
 import java.math.BigDecimal;
 
 import br.octahedron.cotopaxi.validation.Validator;
 
 /**
  * @author Vítor Avelino
  */
 public class BankValidators {
 
 	private static Validator valueValidator;
 	private static Validator requiredValidator;
 	private static Validator dateValidator;
 
 	/**
 	 * A validator for transfers that checks the required fields
 	 */
 	public static synchronized Validator getTransferValidator() {
 		if (requiredValidator == null) {
 			requiredValidator = new Validator();
 			requiredValidator.add("userId", required("REQUIRED_TRANSASCTION_USERID"),
					regex("([a-zA-ZáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûÃÕãõçÇ] *){2,}", "INVALID_USERID"));
 			requiredValidator.add("amount", required("REQUIRED_TRANSACTION_AMOUT"), type(bigDecimalNumber()));
 			requiredValidator.add("type", required("REQUIRED_TRANSACTION_TYPE"), new ExistentTransferTypeRule());
 		}
 		return requiredValidator;
 	}
 	
 	/**
 	 * A validator for ballast that checks the required fields
 	 */
 	public static synchronized Validator getBallastValidator() {
 		if (requiredValidator == null) {
 			requiredValidator = new Validator();
 			requiredValidator.add("userId", required("REQUIRED_TRANSASCTION_USERID"),
 					regex("([a-zA-ZáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûÃÕãõçÇ] *){2,}", "INVALID_USERID"));
 			requiredValidator.add("amount", required("REQUIRED_TRANSACTION_AMOUT"), type(bigDecimalNumber()));
 		}
 		return requiredValidator;
 	}
 
 	/**
 	 * A validator that check if the transfer's amount is positive
 	 */
 	public static synchronized Validator getAmountValidator() {
 		if (valueValidator == null) {
 			valueValidator = new Validator();
 			valueValidator.add("amount", type("NOT_VALID_VALUE", bigDecimalNumber()),
 					greaterThan(bigDecimalNumber(), new BigDecimal(0), "NOT_VALID_VALUE"));
 		}
 		return valueValidator;
 	}
 
 	/**
 	 * A validator that check if start date is less than end date
 	 */
 	public static Validator getDateValidator() {
 		if (dateValidator == null) {
 			dateValidator = new Validator();
 			dateValidator.add("startDate", type("NOT_VALID_DATE", date(SHORT)));
 			dateValidator.add("endDate", type("NOT_VALID_DATE", date(SHORT)), greaterThan(attribute("startDate"),date(SHORT),"NOT_VALID_DATE"));
 		}
 		return dateValidator;
 	}
 
 }
