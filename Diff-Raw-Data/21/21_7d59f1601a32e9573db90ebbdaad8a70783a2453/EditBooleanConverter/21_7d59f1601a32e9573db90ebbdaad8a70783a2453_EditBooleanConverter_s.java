 package jpaoletti.jpm.struts.converter;
 
 import jpaoletti.jpm.converter.ConverterException;
 import jpaoletti.jpm.core.PMContext;
 
 /**
 * Edit a boolean. with-null property indicates a third option with "null"
 * value
 * 
  * @author jpaoletti
  */
 public class EditBooleanConverter extends StrutsEditConverter {
 
     @Override
     public Object build(PMContext ctx) throws ConverterException {
         final String res = (String) ctx.getFieldValue();
         if (res.compareTo("true") == 0) {
             return true;
         }
         if (res.compareTo("false") == 0) {
             return false;
         }
        throw new ConverterException("pm.struts.converter.invalid.boolean");
     }
 
     @Override
     public String visualize(PMContext ctx) throws ConverterException {
         final Boolean p = (Boolean) getValue(ctx.getEntityInstance(), ctx.getField());
        final boolean withnull = Boolean.parseBoolean(getConfig("with-null", "false"));
        if (!withnull) {
             return super.visualize("boolean_converter.jsp?checked=" + ((p != null && p.booleanValue()) ? "checked" : ""));
         } else {
             return super.visualize("nboolean_converter.jsp?checked=" + ((p == null) ? "null" : (p.booleanValue()) ? "true" : "false"));
         }
     }
 }
