 package org.pillarone.riskanalytics.domain.utils.validation;
 
import org.pillarone.riskanalytics.core.parameterization.validation.ParameterValidationError;
 
 import java.util.Collection;
 
public class ParameterValidationErrorImpl extends ParameterValidationError {
 
    public ParameterValidationErrorImpl(String message, Collection arguments) {
        super(message, arguments);
     }
 }
