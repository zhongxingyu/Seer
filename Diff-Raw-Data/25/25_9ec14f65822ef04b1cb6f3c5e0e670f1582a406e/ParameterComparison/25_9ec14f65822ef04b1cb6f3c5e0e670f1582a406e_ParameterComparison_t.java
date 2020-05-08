 package confdb.diff;
 
 
 import confdb.data.Parameter;
 import confdb.data.PSetParameter;
 import confdb.data.VPSetParameter;
 
 
 /**
  * ParameterComparison
  * -------------------
  * @author Philipp Schieferdecker
  *
  */
 public class ParameterComparison extends Comparison
 {
     //
     // member data
     //
     
     /** old parameter */
     private Parameter oldParameter = null;
 
     /** new parameter */
     private Parameter newParameter = null;
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public ParameterComparison(Parameter oldParameter,Parameter newParameter)
     {
 	this.oldParameter = oldParameter;
 	this.newParameter = newParameter;
     }
 
     
     //
     // member functions
     //
 
     /** indicate if [V]Pset parameters are being compared */
     public boolean isPSet()
     {
 	Parameter p = (newParameter==null) ? oldParameter : newParameter;
 	if ((p instanceof PSetParameter)||(p instanceof VPSetParameter)) return true;
 	return false;
     }
     
     /** determine the result of the comparison */
     public int result()
     {
 	if      (oldParameter==null&&newParameter!=null) return RESULT_ADDED;
 	else if (oldParameter!=null&&newParameter==null) return RESULT_REMOVED;
 	else if (comparisonCount()==0&&
 		 oldParameter.name().equals(newParameter.name())&&
 		 oldParameter.type().equals(newParameter.type())&&
		 (isPSet()||oldParameter.valueAsString()
		  .equals(newParameter.valueAsString())))
 	    return RESULT_IDENTICAL;
	else
 	    return RESULT_CHANGED;
     }
     
     /** plain-text representation of the comparison */
     public String toString()
     {
 	StringBuffer result = new StringBuffer();
 	Parameter p = (newParameter==null) ? oldParameter : newParameter;
 	result.append(p.type()).append(" ").append(p.fullName());
 	if (!(p instanceof PSetParameter)&&
 	    !(p instanceof VPSetParameter)) {
 	    result
 		.append(" = ")
 		.append(p.valueAsString());
 	    if (isChanged()) {
 		if (!oldParameter.type().equals(newParameter.type()))
 		    result
 			.append(" {")
 			.append(oldParameter.type())
 			.append("}");
 		result
 		    .append(" [")
 		    .append(oldParameter.valueAsString())
 		    .append("]");
 	    }
 	    else
 		result
 		    .append(" [")
 		    .append(resultAsString())
 		    .append("]");
 	}
 	else {
 	    result
 		.append(" [")
 		.append(resultAsString())
 		.append("]");
 	}
 	return result.toString();
     }
     
     /** html representation of the comparison */
     public String toHtml()
     {
 	StringBuffer result = new StringBuffer();
 	Parameter p = (newParameter==null) ? oldParameter : newParameter;
 	result
 	    .append("<html>")
 	    .append(p.type())
 	    .append(" <b>")
 	    .append(p.name())
 	    .append("</b>");
 	if (!(p instanceof PSetParameter)&&
 	    !(p instanceof VPSetParameter)) {
 	    result
 		.append(" = <font color=#00ff00>")
 		.append(p.valueAsString())
 		.append("</font>");
 	    if (isChanged()) {
 		if (!oldParameter.type().equals(newParameter.type()))
 		    result
 			.append(" {<font color=#0000ff>")
 			.append(oldParameter.type())
 			.append("</font>}");
 		result
 		    .append(" [<font color=#ff0000>")
 		    .append(oldParameter.valueAsString())
 		    .append("</font>]");
 	    }
 	}
 	result.append("</html>");
 	return result.toString();
     }
     
 }
