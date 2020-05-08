 package org.nuthatchery.pgf.rascal.uptr;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.nuthatchery.pgf.plumbing.ForwardStream;
 import org.nuthatchery.pgf.tokens.Category;
 import org.nuthatchery.pgf.tokens.CategoryStore;
 import org.nuthatchery.pgf.tokens.TextToken;
 import org.nuthatchery.pgf.tokens.Token;
 import org.rascalmpl.values.uptr.ProductionAdapter;
 import org.rascalmpl.values.uptr.SymbolAdapter;
 import org.rascalmpl.values.uptr.TreeAdapter;
 import org.rascalmpl.values.uptr.visitors.TreeVisitor;
 
 public class UPTRTokenizer {
 
 	protected final CategoryStore categories;
 	protected final Category HSPC;
 	protected final Category VSPC;
 	protected final Category TXT;
 	protected final TokenizerConfig config;
 
 
 	public UPTRTokenizer(TokenizerConfig config) {
 		this.config = config;
 		this.categories = config.cfgCategories();
 		this.HSPC = config.cfgCatHorizSpace();
 		this.VSPC = config.cfgCatVertSpace();
 		this.TXT = config.cfgCatText();
 	}
 
 
 	public void tokenize(IConstructor parseTree, ForwardStream<Token> output) {
 		Visitor visitor = new Visitor();
 		visitor.output = output;
 		parseTree.accept(visitor);
 	}
 
 
 	class Visitor extends TreeVisitor<RuntimeException> {
 		ForwardStream<Token> output;
 		Pattern patLayout = Pattern.compile("^(\\s*)(.*\\S)(\\s*)$", Pattern.DOTALL);
 
 
 		@Override
 		public IConstructor visitTreeAmb(IConstructor arg) throws RuntimeException {
 			((IConstructor) TreeAdapter.getAlternatives(arg).iterator().next()).accept(this);
 
 			return null;
 		}
 
 
 		@Override
 		public IConstructor visitTreeAppl(IConstructor arg) throws RuntimeException {
 			IConstructor prod = TreeAdapter.getProduction(arg);
 			IConstructor sym = ProductionAdapter.getType(prod);
 			String sort = ProductionAdapter.getSortName(prod);
 			ISourceLocation loc = TreeAdapter.getLocation(arg);
 
 			sym = SymbolAdapter.delabel(sym);
 
 			if(SymbolAdapter.isKeyword(sym)) {
 				output.put(new TextToken(TreeAdapter.yield(arg), TXT));
 				return null;
 			}
 			else if(SymbolAdapter.isLiteral(sym) || SymbolAdapter.isCILiteral(sym)) {
 				String s = TreeAdapter.yield(arg);
 				output.put(new TextToken(s, config.getCatForLiteral(s)));
 				return null;
 			}
 			else if(SymbolAdapter.isLex(sym)) {
 				String s = TreeAdapter.yield(arg);
 				output.put(new TextToken(s, config.getCatForLexical(s)));
 				return null;
 			}
 			else if(TreeAdapter.isComment(arg)) {
 				String s = TreeAdapter.yield(arg);
 				output.put(new TextToken(s, TXT));
 				return null;
 			}
 			else if(SymbolAdapter.isLayouts(sym)) {
 				splitComment(TreeAdapter.yield(arg));
 				return null;
 			}
 
 			IList children = (IList) arg.get("args");
 			for(IValue child : children) {
 				child.accept(this);
 			}
 			return null;
 		}
 
 
 		@Override
 		public IConstructor visitTreeChar(IConstructor arg) throws RuntimeException {
 			output.put(new TextToken(TreeAdapter.yield(arg), null));
 			return null;
 		}
 
 
 		@Override
 		public IConstructor visitTreeCycle(IConstructor arg) throws RuntimeException {
 			return null;
 		}
 
 
 		private void splitComment(String s) {
 			Matcher matcher = patLayout.matcher(s);
 			if(matcher.matches()) {
 				String t = matcher.group(1);
 				if(!t.equals("")) {
 					splitLines(t, config.cfgCatHorizSpace());
 				}
 				t = matcher.group(2);
 				if(!t.equals("")) {
 					splitLines(t, config.cfgCatComment());
 				}
 				t = matcher.group(3);
 				if(!t.equals("")) {
 					splitLines(t, config.cfgCatHorizSpace());
 				}
 			}
 			else if(!s.equals("")) {
 				splitLines(s, config.cfgCatHorizSpace());
 			}
 		}
 
 
 		private void splitLines(String str, Category cat) {
			String[] split = str.split("\n|\f");
 			boolean first = true;
 			for(String s : split) {
 				if(!first) {
 					output.put(new TextToken("\n", config.cfgCatVertSpace()));
 				}
 				if(!s.equals("")) {
 					output.put(new TextToken(s, cat));
 				}
 				first = false;
 			}
 		}
 	}
 }
