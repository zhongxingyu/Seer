 package edu.tum.lua;
 
 import java.io.FileNotFoundException;
 import java.util.Collections;
 import java.util.List;
 
 import util.ParserUtil;
 import edu.tum.lua.ast.Block;
 
 public class LuaInterpreter {
 
 	public static List<Object> eval(Block block, GlobalEnvironment environment) {
 		LocalEnvironment localEnv = new LocalEnvironment(environment);
 		return eval(block, localEnv);
 	}
 
 	public static List<Object> eval(Block block, LocalEnvironment environment) {
		BlockVisitor blockVisitor = new BlockVisitor(environment);
		blockVisitor.visit(block);
		List<Object> ret = blockVisitor.getReturn();
		if (ret == null) {
 			return Collections.emptyList();
 		}
		return blockVisitor.getReturn();
 	}
 
 	public static void main(String[] args) throws FileNotFoundException, Exception {
 		Block block = ParserUtil.loadFile("../Frontend/testinput/homework/salomon_2sat/runSelfCheck.lua");
 		GlobalEnvironment ge = new GlobalEnvironment();
 		ge.getLuaTable("package").set("path",
 				ge.getLuaTable("package").get("path") + ";../Frontend/testinput/homework/salomon_2sat/?.lua");
 		eval(block, ge);
 	}
 }
