 package indeece;
 
 import grammar.ASTwalker;
 import grammar.booleanGrammarLexer;
 import grammar.booleanGrammarParser;
 import grammar.booleanGrammarParser.prog_return;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import java.util.Iterator;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 public class BoolModel extends Model {
 
 	public BoolModel(Index index)
 	{
 		super(index);
 	}
 	
 	@Override
 	public Set<Result> search(String rawQuery)
 	throws Exception
 	{	
 		// parse query.
 		CharStream charStream = new ANTLRStringStream(rawQuery);
 	    booleanGrammarLexer lexer = new booleanGrammarLexer(charStream);
 	    TokenStream tokenStream = new CommonTokenStream(lexer);
 		booleanGrammarParser parser = new booleanGrammarParser(tokenStream);
 		
 		// build AST.
 		prog_return AST = parser.prog();
 		
 		// get AST nodes.
 		CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(AST.getTree());
 		
 		// create an AST traversal tool.
 		ASTwalker walker = new ASTwalker(nodeStream);
 		
 		// create the results set from the posting list.
 		HashSet<Result> 			results 	= new HashSet<Result>();
 		Set<Doc>					docSet		= walker.prog();
 		if(docSet == null)
 			return new HashSet<Result>();
 		
 		Iterator<Doc> 	docSetIter	= docSet.iterator();
 		while(docSetIter.hasNext())
 			results.add(new Result(docSetIter.next()));
 	
 		return results;
 	}
 
 }
