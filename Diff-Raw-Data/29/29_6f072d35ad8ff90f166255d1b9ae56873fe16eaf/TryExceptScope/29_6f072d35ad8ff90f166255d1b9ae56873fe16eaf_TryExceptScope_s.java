 package uk.ac.ic.doc.cfg.model.scope;
 
 import org.python.pydev.parser.jython.ast.TryExcept;
 import org.python.pydev.parser.jython.ast.excepthandlerType;
 
 class TryExceptScope extends ScopeWithParent {
 
 	private TryExcept node;
 
 	protected TryExceptScope(TryExcept node, Statement previousStatement,
 			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
 		super(parent, previousStatement, trajectory, startInNewBlock);
 		this.node = node;
 	}
 
 	@Override
 	protected Statement doProcess() throws Exception {
 
 		Statement exits = new Statement();
 
 		// try
 
 		Statement body = delegate(node.body);
 		if (body.canRaise()) {
 			boolean foundCatchAll = false;
 			for (excepthandlerType handler : node.handlers) {
 
 				if (handler.type == null)
 					foundCatchAll = true;
 
 				Statement handlerBody = buildGraphForceNewBlock(handler.body,
 						body, body.raises());
 
 				exits.inheritInlinksFrom(handlerBody);
 
 				exits.inheritExitsFrom(handlerBody);
 			}
 
 			// Unless a handler catches _all_ exception types, we must still
 			// propagate the possibility of exceptions upwards
 			if (!foundCatchAll) {
 				exits.inheritRaisesFrom(body);
 			}
 		}
 
		// Don't process the else branch if the body is certain to always
		// raise an exception - the else is dead code in this case
 		if (node.orelse != null && body.canFallThrough()) {
 
 			Statement elseBody = buildGraphForceNewBlock(node.orelse.body,
 					body, body.fallthroughs());
 			body.linkFallThroughsTo(elseBody);
 
 			exits.inheritExitsFrom(elseBody);
 		} else {
 			exits.inheritFallthroughsFrom(body);
 		}
 
 		exits.inheritInlinksFrom(body);
 		return exits;
 	}
 }
