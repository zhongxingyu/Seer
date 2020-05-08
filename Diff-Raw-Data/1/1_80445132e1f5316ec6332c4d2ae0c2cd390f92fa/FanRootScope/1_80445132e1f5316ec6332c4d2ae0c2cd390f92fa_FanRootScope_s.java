 /*
  * Thibaut Colar Nov 24, 2009
  */
 package net.colar.netbeans.fan.ast;
 
 import fan.sys.Type;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 import net.colar.netbeans.fan.FanParserResult;
 import net.colar.netbeans.fan.antlr.LexerUtils;
 import org.antlr.runtime.tree.CommonTree;
 import org.netbeans.modules.csl.api.Error;
 import org.netbeans.modules.csl.api.Hint;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.api.Severity;
 import org.netbeans.modules.csl.spi.DefaultError;
 
 /**
  * RootScope for a Fan file
  * Also holds errors (unresolvable imports, undefined vars and so on)
  * @author thibautc
  */
 public class FanRootScope extends FanAstScope
 {
 	// using statements. type=null means unresolvable

 	private Hashtable<String, Type> usedTypes = new Hashtable<String, Type>();
 	// types (classes/enums/mixins)
 	private Vector<FanAstScope> types = new Vector<FanAstScope>();
 	// Root node holds errors and hints, to be used by HintsProvider
 	// For example unesolvable pods, undefined vars and so on
 	List<Error> errors = new ArrayList();
 	List<Hint> hints = new ArrayList();
 
 	public FanRootScope()
 	{
 		super(null);
 	}
 
 	public void addUsedType(String name, Type type)
 	{
 		usedTypes.put(name, type);
 	}
 
 	public void addType(FanAstScope type)
 	{
 		if (type != null)
 		{
 			types.add(type);
 		}
 	}
 
 	public Hashtable<String, Type> getUsedTypes()
 	{
 		return usedTypes;
 	}
 
 	@Override
 	public void dump()
 	{
 		System.out.println("---Root Scope---");
 		for (String key : usedTypes.keySet())
 		{
 			System.out.println("Using: " + key + " (" + usedTypes.get(key) + ")");
 		}
 		for (FanAstScope node : types)
 		{
 			node.dump();
 		}
 	}
 
 	public List<Error> getErrors()
 	{
 		return errors;
 	}
 
 	public List<Hint> getHints()
 	{
 		return hints;
 	}
 
 	public void addError(FanParserResult result, String info, CommonTree node)
 	{
 		String key = "FanASTParser";
 		OffsetRange range = LexerUtils.getNodeRange(result, node);
 		int start = range.getStart();
 		int end = range.getEnd();
 		//System.out.println("Start: "+start+"End:"+end);
 		Error error = DefaultError.createDefaultError(key, info, "Syntax Error", null, start, end, false, Severity.ERROR);
 		errors.add(error);
 	}
 }
