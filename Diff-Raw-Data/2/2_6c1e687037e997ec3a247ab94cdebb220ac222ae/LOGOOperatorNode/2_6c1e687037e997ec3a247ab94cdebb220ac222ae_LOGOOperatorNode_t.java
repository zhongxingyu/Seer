 public class LOGOOperatorNode extends LOGONode{
 	public LOGOOperatorNode(String id, LOGONode... args) {
 		super(id, args);
 	}
 
 	Double runAndCheck(LOGONode node) {
 		if (LOGOPP.errorhandler.error())
 			return null;
 		if (node == null) {
 			LOGOPP.errorhandler.setRunTime(id, "no argument");
 			return null;
 		}
 		Object nodeVal = node.run();
 		if (LOGOPP.errorhandler.error())
 			return null;
 		if (nodeVal == null) {
 			LOGOPP.errorhandler.setRunTime(id, "null argument");
 			return null;
 		}
 		if (!(nodeVal instanceof Double)) {
 			LOGOPP.errorhandler.setRunTime(id, "wrong argument type");
 			return null;
 		}
 		return (Double) nodeVal;
 	}
 
 	public Object run() {
 		Double ret;
 		Double arg0, arg1;
 		if (id.equals("u-")) {
 			arg0 = runAndCheck(children[0]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			ret = arg0 * -1;
 			LOGOPP.io.debug("unary minus");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("*")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			ret = arg0 * arg1;
 			LOGOPP.io.debug("multiply");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("/")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 == 0.) {
 				LOGOPP.errorhandler.setRunTime(id, "devide by zero");
 				return null;
 			}
 			ret = arg0 / arg1;
 			LOGOPP.io.debug("division");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("^")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			ret = Math.pow(arg0, arg1);
 			LOGOPP.io.debug("power");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("+")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			ret = arg0 + arg1;
 			LOGOPP.io.debug("plus");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("-")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			ret = arg0 - arg1;
 			LOGOPP.io.debug("minus");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("<")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 < arg1)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("less than");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals(">")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 > arg1)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("greater than");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("<=")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 <= arg1)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("less than or equal to");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals(">=")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 >= arg1)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("greater than or equal to");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("=")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 == arg1)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("equal to");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("!=")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 != arg1)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("not equal to");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("&&")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
 			if (arg0 == 0 || arg1 == 0)
 				ret = (double) 0;
 			else
 				ret = (double) 1;
 			LOGOPP.io.debug("logical and");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		if (id.equals("||")) {
 			arg0 = runAndCheck(children[0]);
 			arg1 = runAndCheck(children[1]);
 			if (LOGOPP.errorhandler.error())
 				return null;
			if (arg0 != 0 || arg1 != 0)
 				ret = (double) 1;
 			else
 				ret = (double) 0;
 			LOGOPP.io.debug("logical or");
 			LOGOPP.io.debug(ret.toString());
 			return ret;
 		}
 		LOGOPP.errorhandler.setRunTime(id, "unrecognised operator");
 		return null;		
 
 	}
 }
