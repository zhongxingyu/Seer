 package fr.esiea.esieaddress.service.validation.implementation;
 
 import fr.esiea.esieaddress.model.IModel;
 import fr.esiea.esieaddress.service.validation.IValidationService;
 import org.springframework.stereotype.Service;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Service
 public class ValidationService<T extends IModel> implements IValidationService<T> {
 
 	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
 
 	@Override
 	public Map<Object, String> validate(T model) {
 
 		Set<ConstraintViolation<T>> violations = validator.validate(model);
 
 		if (violations.isEmpty())
 			return Collections.EMPTY_MAP;
 
 		HashMap<Object, String> errorMap = new HashMap<Object, String>();
 		for (ConstraintViolation<T> violation : violations) {
			errorMap.put(violation.getInvalidValue(), violation.getMessage());
 		}
 
 		return errorMap;
 	}
 }
