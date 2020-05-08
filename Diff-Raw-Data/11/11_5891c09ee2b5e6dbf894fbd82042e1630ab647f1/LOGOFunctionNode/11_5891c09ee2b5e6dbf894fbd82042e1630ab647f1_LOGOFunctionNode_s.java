 class LOGOFunctionNode extends LOGONode {
 	Object[] arguments;
 	public LOGOFunctionNode(LOGONode name, String type, Object argList, LOGONode... args) {
 		super(name.id, args);
 		arguments = new Object[2];
 		arguments[0] = type;
 		arguments[1] = argList;
 	}
 	public Object run() {
 		if (arguments[0].equals("define")) {
 			LOGOFunction function = new LOGOFunction(id, (LOGOIdList)arguments[1], (LOGOStatementNode)children.get(0));
 			LOGOPP.symboltable.set(id, function);
 			return null;
 		}
 		if (arguments[0].equals("execute")) {
 			LOGOPP.io.debug("execute " + id);
 			Object function = LOGOPP.symboltable.get(id);
 			if (function instanceof LOGOFunction)
 				return ((LOGOFunction)function).run((LOGOExprListNode)arguments[1]);
 			LOGOPP.errorhandler.setRunTime(id, "cannot find function definition.");
 			return null;
 		}
 		LOGOPP.io.err("LOGOFunctionNode: unreachable location");
 		return null;
 	}
 }
 
 class LOGOFunction {
 	String id;
 	LOGOIdList args;
 	LOGOStatementNode cmd;
 	LOGOFunction(String id, LOGOIdList args, LOGOStatementNode cmd) {
 		this.id = id;
 		this.args = args;
 		this.cmd = cmd;
 	}
 
 	void setArg(Object[] argList) {
 		LOGOPP.io.debug("function.setArg");
 		if (this.args == null && argList == null)
 			return;
 		if (this.args != null && argList == null) {
 			LOGOPP.errorhandler.setRunTime(id, "unmatched number of arguments");
 			return;
 		}
 		if (this.args == null && argList != null) {
 			LOGOPP.errorhandler.setRunTime(id, "unmatched number of arguments");
 			return;
 		}
 		Object[] idList = this.args.getList();
 		LOGOPP.io.debug("size of args " + idList.length);
 		LOGOPP.io.debug("size of input " + argList.length);
 		if (LOGOPP.errorhandler.error())
 			return;
 		if (idList.length != argList.length) {
 			LOGOPP.errorhandler.setRunTime(id, "unmatched number of arguments");
 			return;
 		}
 		for (int i = 0; i < idList.length; i++) {
 			LOGOPP.symboltable.set((String)idList[i], argList[i]);
 		}
 	}
 	
 	Object run(LOGOExprListNode args) {
 		LOGOPP.io.debug("function.run");
 		if (LOGOPP.errorhandler.error())
 			return null;
		Object[] argList = (Object[])args.run();
 		LOGOPP.symboltable.push();
 		setArg(argList);
 		if (LOGOPP.errorhandler.error()) {
 			LOGOPP.symboltable.pop();
 			return null;
 		}
 		Object ret = cmd.run();
 		LOGOPP.symboltable.pop();
 		return ret;
 	}
 }
