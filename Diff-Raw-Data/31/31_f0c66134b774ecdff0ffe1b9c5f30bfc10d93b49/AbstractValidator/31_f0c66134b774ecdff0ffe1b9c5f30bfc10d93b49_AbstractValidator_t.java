 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.validation;
 
 import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
 import org.gravidence.gravifon.exception.ValidationException;
import org.gravidence.gravifon.exception.error.GravifonError;
 
 /**
  * Abstract implementation of {@link Validator}.<p>
  * {@link Validator#validate(javax.ws.rs.core.MultivaluedMap, java.lang.Object) Validator.validate} method is finalized.
  * Descendants must implement separate {@link #validateQueryParams(javax.ws.rs.core.MultivaluedMap) validateQueryParams}
  * and {@link #validateEntity(java.lang.Object) validateEntity} methods (asc ordered by execution priority).
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public abstract class AbstractValidator<T> implements Validator<T> {
 
     @Override
     public final void validate(MultivaluedMap<String, String> queryParams, T entity) throws ValidationException {
         validateQueryParams(queryParams);
         validateEntity(entity);
     }
     
     /**
      * Validates HTTP request query params.
      * 
      * @param queryParams request query params
      * @throws ValidationException in case supplied argument doesn't pass validation rule(s)
      */
     protected abstract void validateQueryParams(MultivaluedMap<String, String> queryParams) throws ValidationException;
     
     /**
      * Validates HTTP request body.
      * 
      * @param entity request body.
      * @throws ValidationException in case supplied argument doesn't pass validation rule(s)
      */
     protected abstract void validateEntity(T entity) throws ValidationException;
     
    /**
     * Validates that required query param is actually presented.
     * 
     * @param queryParamName name of required query param
     * @param queryParams request query params
     * @throws ValidationException in case required query param is not found or empty
     */
    protected void validateRequiredQueryParam(String queryParamName, MultivaluedMap<String, String> queryParams) throws ValidationException {
        if (StringUtils.isEmpty(queryParams.getFirst(queryParamName))) {
            throw new ValidationException(GravifonError.REQUIRED,
                    String.format("Query param '%s' is required.", queryParamName));
        }
    }
    
 }
