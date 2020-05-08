 public class LOGOIterationNode extends LOGONode{
 
 	public LOGOIterationNode(String id, LOGONode... args) {
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
 			LOGOPP.errorhandler.setRunTime(id, "null argument in iteration condition...");
 			return null;
 		}
 		if (!(nodeVal instanceof Double)) {
 			LOGOPP.errorhandler.setRunTime(id, "wrong argument type for the expression in iteration condition...");
 			return null;
 		}
 		return (Double) nodeVal;
 	}
 	public Object run() {
 		double bool;
 		double true_ = (double) 1;
 		Object ret = null;
 		int we_run_it = 0;
 		if (id.equals("while")) {
 			bool = runAndCheck(children.get(0));
 			if (LOGOPP.errorhandler.error())
 				return null;
 			while (bool == true_) {
 				we_run_it = 1;
 				ret = children.get(1).run();
 				bool = runAndCheck(children.get(0));
 				if (LOGOPP.errorhandler.error())
 					return null;
 			}
 			if (we_run_it == 1)
 				return ret;
 			return null;
 		}
 		if (id.equals("repeat")) {
 			double iterator = runAndCheck(children.get(0));
 			if (LOGOPP.errorhandler.error())
 				return null;
 			double i = 1;
 			for (; i <= iterator; i++) {
 				we_run_it = 1;
 				ret = children.get(1).run();
 				if (LOGOPP.errorhandler.error())
 					return null;
 			}
 			if (we_run_it == 1)
 				return ret;
 			return null;
 		}
 	
 		if (id.equals("for")) {
 			double init = 1.0, pace = 1.0, fini = 1.0;
 			LOGONode for_expr = children.get(1);
 			
 			if (for_expr.id.equals("for_expr1")) {
 				init = runAndCheck(for_expr.children.get(0));
 				fini = runAndCheck(for_expr.children.get(1));
 			}
 			else if (for_expr.id.equals("for_expr2")) {
 				init = runAndCheck(for_expr.children.get(0));
 				pace = runAndCheck(for_expr.children.get(1));
 				fini = runAndCheck(for_expr.children.get(2));
 			} else {
 				LOGOPP.errorhandler.setRunTime(id, "Wrong condition in for loop");
 			}
 			if (LOGOPP.errorhandler.error())
 				return null;
 			double iterator;
			for (iterator = init; iterator <= fini; iterator += pace) {
				LOGOPP.symboltable.set(children.get(0).id, iterator);
				if (LOGOPP.errorhandler.error())
					return null;
				we_run_it = 1;
				ret = children.get(2).run();
				if (LOGOPP.errorhandler.error())
					return null;
 			}
 			if (we_run_it == 1)
 				return ret;
 			return null;
 		}
 		LOGOPP.errorhandler.setRunTime(id, "unrecognised iteration command");
 		return null;		
 
 	}
 
 }
