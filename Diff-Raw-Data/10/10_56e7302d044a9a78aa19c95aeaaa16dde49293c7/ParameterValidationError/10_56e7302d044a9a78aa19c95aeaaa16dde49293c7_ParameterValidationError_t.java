 package org.pillarone.riskanalytics.core.parameterization.validation;
 
 import java.util.Collection;
 import java.util.Locale;
 
 public abstract class ParameterValidationError {
 
     protected final Collection args;
     protected final String msg;
     private String path;
    private int periodIndex;
 
     public ParameterValidationError(String message, Collection arguments) {
         this.msg = message;
         this.args = arguments;
     }
 
     public Collection getArgs() {
         return args;
     }
 
     public String getMsg() {
         return msg;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

     public abstract String getLocalizedMessage(Locale locale);
 
     @Override
     public String toString() {
         return msg;
     }
 }
