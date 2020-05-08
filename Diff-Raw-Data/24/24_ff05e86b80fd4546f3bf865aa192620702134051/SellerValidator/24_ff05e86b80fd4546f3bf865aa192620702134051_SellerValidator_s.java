 package it.univaq.mwt.j2ee.kmZero.presentation;
 
 import it.univaq.mwt.j2ee.kmZero.business.model.Seller;
 import it.univaq.mwt.j2ee.kmZero.common.spring.validation.ValidationUtility;
 
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 @Component
 public class SellerValidator implements Validator {
 
 	@Override
 	public boolean supports(Class<?> klass) {
 		return Seller.class.isAssignableFrom(klass);
 	}
 
 	@Override
 	public void validate(Object target, Errors errors) {
 		Seller seller = (Seller) target;
 		
 		//ValidationUtility.checkIdentity(errors,"id","errors.idInjection", seller.getId());
 		
 		ValidationUtils.rejectIfEmpty(errors, "name", "errors.required");
 		ValidationUtils.rejectIfEmpty(errors, "surname", "errors.required");
 		ValidationUtils.rejectIfEmpty(errors, "email", "errors.required");
 		ValidationUtils.rejectIfEmpty(errors, "date_of_birth", "errors.required");
 		ValidationUtils.rejectIfEmpty(errors, "address", "errors.required");
 		// Questi campi vengono validati solo alla creazione o se  l'admin a modificare un venditore
 		if (seller.getP_iva() != null) {
 			ValidationUtils.rejectIfEmpty(errors, "p_iva", "errors.required");
 			ValidationUtils.rejectIfEmpty(errors, "cod_fisc", "errors.required");
 			ValidationUtils.rejectIfEmpty(errors, "company", "errors.required");
 			ValidationUtility.rejectIfMaxLength(errors, "cod_fisc", "errors.maxlength", seller.getCod_fisc(), 16);
 		}
 		ValidationUtils.rejectIfEmpty(errors, "url", "errors.required");
 		ValidationUtils.rejectIfEmpty(errors, "phone", "errors.required");
 		
 		ValidationUtility.rejectIfMaxLength(errors, "name", "errors.maxlength", seller.getName(), 255);
 		ValidationUtility.rejectIfMaxLength(errors, "surname", "errors.maxlength", seller.getSurname(), 255);
 		ValidationUtility.rejectIfMaxLength(errors, "address", "errors.maxlength", seller.getAddress(), 255);
 		// Se la Password esiste, ci si trova nel createuserform ed allora fa questa validazione
 		if (seller.getPassword() != null){
 			ValidationUtils.rejectIfEmpty(errors, "password.password", "errors.required");
 			ValidationUtils.rejectIfEmpty(errors, "password.confirm_password", "errors.required");
 			ValidationUtility.checkPassword(errors, "password.confirm_password", "errors.password", seller.getPassword().getPassword(), seller.getPassword().getConfirm_password());
 		}
 		ValidationUtility.checkEmail(errors, "email", "errors.email", seller.getEmail());
		ValidationUtility.rejectIfMaxLength(errors, "address", "errors.maxlength", seller.getP_iva(), 11);
 	}
 
 }
