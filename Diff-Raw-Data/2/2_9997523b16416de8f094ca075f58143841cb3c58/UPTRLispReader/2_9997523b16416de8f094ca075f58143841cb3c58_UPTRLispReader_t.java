 package clojure.lang;
 
 /**
  *   Copyright (c) Rich Hickey. All rights reserved.
  *   The use and distribution terms for this software are covered by the
  *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
  *   which can be found in the file epl-v10.html at the root of this distribution.
  *   By using this software in any fashion, you are agreeing to be bound by
  * 	 the terms of this license.
  *   You must not remove this notice, or any other, from this software.
  **/
 
 import static org.rascalmpl.values.clojure.FormAdapter.getLineNumber;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import lang.synclj.meta.EBNFParser;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IListWriter;
 import org.eclipse.imp.pdb.facts.IMapWriter;
 import org.eclipse.imp.pdb.facts.INode;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.ISetWriter;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.visitors.VisitorException;
 import org.rascalmpl.parser.gtd.IGTD;
 import org.rascalmpl.parser.gtd.exception.ParseError;
 import org.rascalmpl.parser.uptr.NodeToUPTR;
 import org.rascalmpl.values.Message;
 import org.rascalmpl.values.clojure.FormAdapter;
 import org.rascalmpl.values.uptr.Factory;
 import org.rascalmpl.values.uptr.TreeAdapter;
 import org.rascalmpl.values.uptr.visitors.TreeVisitor;
 
 public class UPTRLispReader extends LispReader {
 
 	private static final Keyword META_GRAMMAR = Keyword.intern("meta-grammar");
 	private static final Object DISCARD = new Object();
 	private final IValueFactory vf;
 	private final ISetWriter errors;
 	
 	private static Bridge2Rascal bridge = null;
 	
 	public UPTRLispReader(IValueFactory vf, ISetWriter errors) {
 		this.vf = vf;
 		if (bridge == null) {
 			bridge = new Bridge2Rascal(vf);
 		}
 		this.errors = errors;
 	}
 	
 	// TODO: unreadable reader (?), eval reader, data readers, ctor reader, record
 	// TODO: communicate tot the RascalMonitor.
 	
 	// Result tuples.
 	public static class Pair {
 		public final IConstructor tree;
 		public final Object obj;
 
 		public Pair(IConstructor tree, Object obj) {
 			this.tree = tree;
 			this.obj = obj;
 		}
 	}
 	
 	public static class ListPair {
 		public final IList trees;
 		public final List<Object> objs;
 
 		public ListPair(IList trees, List<Object> objs) {
 			this.trees = trees;
 			this.objs = objs;
 		}
 	}
 
 	private Object error(String message, IConstructor tree) {
 		return error(message, TreeAdapter.getLocation(tree));
 	}
 	
 	private Object error(String message, ISourceLocation loc) {
 		if (runningInIDE()) {
 			recordError(message, loc);
 			return null;
 		}
 		throw new ReaderException(loc.getBeginLine(), message);
 	}
 
 	private void recordError(String message, ISourceLocation loc) {
		errors.insert(vf.constructor(Message.Message_error, vf.string(message), loc));
 	}
 
 	private boolean runningInIDE() {
 		return errors != null;
 	}
 	
 	public Pair read(IConstructor tree) {
 		if (TreeAdapter.isAmb(tree)) {
 			error("Ambiguous tree: >" + tree + "<, \"" + TreeAdapter.yield(tree) + "\"", tree);
 			return new Pair(tree, null);
 		}
 		if (FormAdapter.isNumber(tree)) {
 			return new Pair(tree, matchNumber(TreeAdapter.yield(tree)));
 		}
 		if (FormAdapter.isChar(tree)) {
 			return new Pair(tree, matchCharacter(TreeAdapter.yield(tree).substring(1)));
 		}
 		if (FormAdapter.isString(tree)) {
 			return new Pair(tree, readString(tree));
 		}
 		if (FormAdapter.isRegexp(tree)) {
 			return new Pair(tree, readRegexp(tree));
 		}
 		if (FormAdapter.isMeta(tree)) {
 			return readMeta(tree);
 		}
 		if (FormAdapter.isSymbol(tree)) {
 			return new Pair(tree, interpretToken(TreeAdapter.yield(tree)));
 		}
 		if (FormAdapter.isList(tree)) {
 			return readList(tree, getLineNumber(tree));
 		}
 		if (FormAdapter.isSet(tree)) {
 			ListPair lp = readForms(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, PersistentHashSet.createWithCheck(lp.objs));
 		}
 		if (FormAdapter.isVector(tree)) {
 			ListPair lp = readForms(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, LazilyPersistentVector.create(lp.objs));
 		}
 		if (FormAdapter.isMap(tree)) {
 			return readMap(tree);
 		}
 		if (FormAdapter.isQuote(tree)) {
 			ListPair lp = readArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.list(QUOTE, lp.objs.get(0)));
 		}
 		if (FormAdapter.isDeref(tree)) {
 			ListPair lp = readArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.list(DEREF, lp.objs.get(0)));
 		}
 		if (FormAdapter.isFn(tree)) {
 			return readFn(tree);
 		}
 		if (FormAdapter.isArg(tree)) {
 			return new Pair(tree, readArg(tree));
 		}
 		if (FormAdapter.isVar(tree)) {
 			ListPair lp = readArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.list(THE_VAR, lp.objs.get(0)));
 		}
 		if (FormAdapter.isDiscard(tree)) {
 			return new Pair(tree, DISCARD);
 		}
 		if (FormAdapter.isQQuote(tree)) {
 			return readQuasi(tree);
 		}
 		if (FormAdapter.isUnquote(tree)) {
 			ListPair lp = readArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.list(UNQUOTE, lp.objs.get(0)));
 		}
 		if (FormAdapter.isUnquotes(tree)) {
 			ListPair lp = readArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.list(UNQUOTE_SPLICING, lp.objs.get(0)));
 		}
 		error("Unknown read error " + TreeAdapter.yield(tree), tree);
 		return new Pair(tree, null);
 	}
 
 	private Pair readQuasi(IConstructor tree) {
 		try {
 			Var.pushThreadBindings(RT.map(GENSYM_ENV, PersistentHashMap.EMPTY));
 			ListPair lp = readArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, SyntaxQuoteReader.syntaxQuote(lp.objs.get(0)));
 		}
 		finally {
 			Var.popThreadBindings();
 		}
 	}
 
 	private Object readArg(IConstructor tree) {
 		String token = TreeAdapter.yield(tree);
 		if (ARG_ENV.deref() == null) {
 			return interpretToken(token);
 		}
 		if ("%".equals(token)) {
 			return registerArg(1);
 		}
 		if ("%&".equals(token)) {
 			return registerArg(-1);
 		}
 		if (token.startsWith("%")) {
 			String num = token.substring(1);
 			if (num.matches("^[1-9][0-9]*$")) {
 				return registerArg(Integer.parseInt(num));
 			}
 		}
 		return error("arg literal must be %, %& or %integer", tree);
 	}
 
 	private Pair readFn(IConstructor tree) {
 		if(ARG_ENV.deref() != null) {
 			error("Nested #()s are not allowed", tree);
 			return new Pair(tree, null);
 		}
 		try {
 			Var.pushThreadBindings(RT.map(ARG_ENV, PersistentTreeMap.EMPTY));
 			ListPair lp = readForms(TreeAdapter.getArgs(tree));
 
 			PersistentVector args = PersistentVector.EMPTY;
 			PersistentTreeMap argsyms = (PersistentTreeMap) ARG_ENV.deref();
 			ISeq rargs = argsyms.rseq();
 			if (rargs != null) {
 				@SuppressWarnings("rawtypes")
 				int higharg = (Integer) ((Map.Entry) rargs.first()).getKey();
 				if (higharg > 0) {
 					for (int i = 1; i <= higharg; ++i) {
 						Object sym = argsyms.valAt(i);
 						if(sym == null) {
 							sym = garg(i);
 						}
 						args = args.cons(sym);
 					}
 				}
 				Object restsym = argsyms.valAt(-1);
 				if(restsym != null) {
 					args = args.cons(Compiler._AMP_);
 					args = args.cons(restsym);
 				}
 			}
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.list(Compiler.FN, args, PersistentList.create(lp.objs)));
 		}
 		finally {
 			Var.popThreadBindings();
 		}
 	}
 
 	private Pair readMap(IConstructor tree) {
 		ListPair pl = readForms(TreeAdapter.getArgs(tree));
 		tree = tree.set("args", pl.trees);
 		if ((pl.objs.size() & 1) == 1) {
 			pl.objs.add(null);
 		}
 		return new Pair(tree, RT.map(pl.objs.toArray()));
 	}
 
 	private Pair readList(IConstructor tree, int line) {
 		IList args = TreeAdapter.getArgs(tree);
 		ListPair lp = readForms(args);
 		if (lp.objs.isEmpty()) {
 			return new Pair(tree, PersistentList.EMPTY);
 		}
 		ISeq seq = PersistentList.create(lp.objs).seq();
 		Object key = seq.first();
 		if (key instanceof Symbol) {
 			Object grammar = getGrammar(key);
 			if (grammar != null) {
 				ListPair emb = parseEmbedding(grammar, key, lp.trees);
 				if (emb != null) { // no parse error
 					tree = tree.set("args", emb.trees);
 					seq = RT.list(emb.objs.get(0), emb.objs.get(1));
 				}
 				else {
 					// quote the token as a list to prevent arity exception.
 					seq = RT.list(seq.first(), RT.list(QUOTE, seq.next()));
 				}
 //				IConstructor embedding = (IConstructor) lp.trees.get(2);
 //				String src = TreeAdapter.yield(embedding);
 //				ISourceLocation embeddingLoc = TreeAdapter.getLocation(embedding);
 //				
 //				try {
 //					IConstructor pt = parseUsingGrammar(grammar, ((Symbol) key).getName(), src, embeddingLoc);
 //					pt = fixLocs(pt, embeddingLoc);
 //					// This is right vvvvvvv
 //					args = lp.trees;
 //					Pair lowered = lower(pt);
 //					tree = tree.set("args", vf.list(args.get(0), args.get(1), lowered.tree, args.get(3), args.get(4)));
 //					seq = RT.list(key, lowered.obj); //RT.list(QUOTE, lowered.obj));
 //				}
 //				catch (ParseError e) {
 //					ISourceLocation loc = vf.sourceLocation(embeddingLoc.getURI(), e.getOffset(),
 //							e.getLength(), e.getBeginLine(), e.getEndLine(), 
 //							e.getBeginColumn(), e.getEndColumn());
 //					error("Parse error", fixLoc(loc, embeddingLoc));
 //				}
 			}
 			else {
 				tree = tree.set("args", lp.trees);
 			}
 		}
 		IObj s = (IObj) seq;
 		return new Pair(tree, s.withMeta(RT.map(RT.LINE_KEY, line)));
 	}
 
 	
 	private ListPair parseEmbedding(Object grammar, Object key, IList args) {
 		// here we need full args, not just AST args.
 		// start at 4 and stop early to skip name and pre/post layout
 		// "(" _ Form* _ ")"
 		//  0  1  2    3   4 
 		// can both use args and lp.trees here.
 		
 		IConstructor embedding = (IConstructor) args.get(2);
 		String src = TreeAdapter.yield(embedding);
 		ISourceLocation embeddingLoc = TreeAdapter.getLocation(embedding);
 		
 		try {
 			IConstructor pt = parseUsingGrammar(grammar, ((Symbol) key).getName(), src, embeddingLoc);
 			pt = fixLocs(pt, embeddingLoc);
 			// This is right vvvvvvv
 			Pair lowered = lower(pt);
 			return new ListPair(vf.list(args.get(0), args.get(1), lowered.tree, args.get(3), args.get(4)),
 					Arrays.asList(key, lowered.obj));
 			//tree = tree.set("args", vf.list(args.get(0), args.get(1), lowered.tree, args.get(3), args.get(4)));
 //			seq = RT.list(key, lowered.obj); //RT.list(QUOTE, lowered.obj));
 		}
 		catch (ParseError e) {
 			ISourceLocation loc = vf.sourceLocation(e.getLocation(), e.getOffset(),
 					e.getLength(), e.getBeginLine() + 1, e.getEndLine() + 1, 
 					e.getBeginColumn(), e.getEndColumn());
 			error("Parse error", fixLoc(loc, embeddingLoc));
 		}
 		return null;
 	}
 	
 	private IConstructor fixLocs(IConstructor tree, ISourceLocation off) {
 		try {
 			return (IConstructor) tree.accept(new LocFixer(off));
 		} catch (VisitorException e) {
 			error("INTERNAL: Could not fix locations in " + tree, tree);
 			return tree;
 		}
 	}
 	
 	class LocFixer extends TreeVisitor {
 		private final ISourceLocation off;
 
 		public LocFixer(ISourceLocation off) {
 			this.off = off;
 		}
 		
 		private IConstructor fix(IConstructor tree) {
 			ISourceLocation loc = TreeAdapter.getLocation(tree);
 			if (loc != null) {
 				loc = fixLoc(loc, off);
 				return tree.setAnnotation(Factory.Location, loc);
 			}
 			return tree;
 		}
 		
 		@Override
 		public IConstructor visitTreeAppl(IConstructor arg) throws VisitorException {
 			IList args = TreeAdapter.getArgs(arg);
 			IListWriter newArgs = vf.listWriter();
 			for (IValue x: args) {
 				newArgs.append(x.accept(this));
 			}
 			arg.set("args", newArgs.done());
 			return fix(arg);
 		}
 
 		@Override
 		public IConstructor visitTreeAmb(IConstructor arg)
 				throws VisitorException {
 			ISet alts = TreeAdapter.getAlternatives(arg);
 			ISetWriter newAlts = vf.setWriter();
 			for (IValue x: alts) {
 				newAlts.insert(x.accept(this));
 			}
 			arg.set("alternatives", newAlts.done());
 			return fix(arg);
 		}
 
 		@Override
 		public IConstructor visitTreeChar(IConstructor arg)
 				throws VisitorException {
 			return arg;
 		}
 
 		@Override
 		public IConstructor visitTreeCycle(IConstructor arg)
 				throws VisitorException {
 			return fix(arg);
 		}
 
 		@Override
 		public IConstructor visitTreeError(IConstructor arg)
 				throws VisitorException {
 			throw new AssertionError("No support for error trees");
 		}
 
 		@Override
 		public IConstructor visitTreeErrorAmb(IConstructor arg)
 				throws VisitorException {
 			throw new AssertionError("No support for error trees");
 		}
 
 		@Override
 		public IConstructor visitTreeErrorCycle(IConstructor arg)
 				throws VisitorException {
 			throw new AssertionError("No support for error trees");
 		}
 
 		@Override
 		public IConstructor visitTreeExpected(IConstructor arg)
 				throws VisitorException {
 			throw new AssertionError("No support for error trees");
 		}
 
 	}
 	
 	private ISourceLocation fixLoc(ISourceLocation l, ISourceLocation off) {
 		l = vf.sourceLocation(l.getURI(), l.getOffset() + off.getOffset(), l.getLength(),
 				l.getBeginLine(), l.getEndLine(), l.getBeginColumn(), l.getEndColumn());
 		if (l.getBeginLine() == 0) {
 			if (l.getBeginLine() == l.getEndLine()) {
 				l = vf.sourceLocation(l.getURI(), l.getOffset(), l.getLength(),
 						l.getBeginLine(), l.getEndLine(), l.getBeginColumn() + off.getBeginColumn(), 
 						l.getEndColumn() + off.getBeginColumn());
 			}
 			else {
 				l = vf.sourceLocation(l.getURI(), l.getOffset(), l.getLength(),
 						l.getBeginLine(), l.getEndLine(), l.getBeginColumn() + off.getBeginColumn(), l.getEndColumn());
 			}
 		}
 		l = vf.sourceLocation(l.getURI(), l.getOffset(), l.getLength(),
 				l.getBeginLine() + off.getBeginLine() - 1, 
 				l.getEndLine() + off.getBeginLine() - 1, 
 				l.getBeginColumn(), l.getEndColumn());
 		return l;
 	}
 	
 	private IConstructor parseMetaGrammar(String src, ISourceLocation loc) {
 		IGTD parser = new EBNFParser();
 		IConstructor pt = (IConstructor) parser.parse("EBNF", loc.getURI(), src.toCharArray(), new NodeToUPTR());
 		return fixLocs(pt, loc);
 	}
 	
 	private IConstructor parseUsingGrammar(Object grammar, String key, String src, ISourceLocation loc) {
 		// TODO: pass current namespace to parser functions.
 		// TODO: communicate with RascalMonitor here.
 		if (grammar == META_GRAMMAR) {
 			return parseMetaGrammar(src, loc);
 		}
 		else {
 			INode ast = (INode) clojure2node(grammar);
 			System.err.println(ast);
 			IConstructor pt = bridge.parse(ast, "bla", key, src, loc);
 			return pt;
 		}
 	}
 	
 	private IValue clojure2node(Object ast) {
 		if (ast instanceof IPersistentList || ast instanceof Cons) {
 			ISeq seq = ((ISeq)ast).seq();
 			String name = ((Symbol)seq.first()).getName();
 			seq = seq.next();
 			if (seq == null) {
 				return vf.node(name);
 			}
 			IValue[] arr = new IValue[seq.count()];
 			for (int i = 0; seq != null; i++) {
 				IValue kid = clojure2node(seq.first());
 				arr[i] = kid;
 				seq = seq.next();
 			}
 			return vf.node(name, arr);
 		}
 		if (ast instanceof IPersistentVector) {
 			ISeq seq = ((IPersistentVector)ast).seq();
 			IListWriter w = vf.listWriter();
 			while (seq != null) {
 				IValue kid = clojure2node(seq.first());
 				w.append(kid);
 				seq = seq.next();
 			}
 			return w.done();
 		}
 		if (ast instanceof IPersistentSet) {
 			ISeq seq = ((IPersistentSet)ast).seq();
 			ISetWriter w = vf.setWriter();
 			while (seq != null) {
 				IValue kid = clojure2node(seq.first());
 				w.insert(kid);
 				seq = seq.next();
 			}
 			return w.done();
 		}
 		if (ast instanceof IPersistentMap) {
 			ISeq seq = ((IPersistentMap)ast).seq();
 			IMapWriter w = vf.mapWriter();
 			while (seq != null) {
 				IMapEntry entry = (IMapEntry) seq.first();
 				w.put(clojure2node(entry.getKey()),clojure2node(entry.getValue()));
 				seq = seq.next();
 			}
 			return w.done();
 		}
 		if (ast instanceof Integer) {
 			return vf.integer(ast.toString());
 		}
 		if (ast instanceof Long) {
 			return vf.integer(ast.toString());
 		}
 		if (ast instanceof BigDecimal) {
 			return vf.real(ast.toString());
 		}
 		if (ast instanceof String) {
 			return vf.string((String)ast);
 		}
 		if (ast instanceof Symbol) {
 			return vf.node("$symbol", vf.string(((Symbol)ast).getName()));
 		}
 		if (ast instanceof Keyword) {
 			return vf.node("$keyword", vf.string(((Symbol)ast).getName()));
 		}
 		if (ast instanceof Pattern) {
 			return vf.node("$regexp", vf.string(((Pattern)ast).toString()));
 		}
 		throw new AssertionError("Could not lift " + ast + " to Rascal value");
 	}
 
 
 	private ListPair lowerArgs(IList args) {
 		List<Object> elts = new ArrayList<Object>();
 		IListWriter newArgs = vf.listWriter();
 		for (int i = 0; i < args.length(); i++) {
 			IConstructor kid = (IConstructor) args.get(i);
 			if (!TreeAdapter.isLiteral(kid) && !TreeAdapter.isCILiteral(kid)) {
 				Pair p = lower(kid);
 				elts.add(p.obj);
 				newArgs.append(p.tree);
 			}
 			else {
 				newArgs.append(kid);
 			}
 			if (i < args.length() - 2) {
 				i++;
 				newArgs.append(args.get(i)); // layout
 			}
 		}			
 		return new ListPair(newArgs.done(), elts);
 	}
 	
 	@SuppressWarnings("unused")
 	private ListPair lowerList(IList args) {
 		List<Object> elts = new ArrayList<Object>();
 		IListWriter newArgs = vf.listWriter();
 		for (int i = 0; i < args.length(); i++) {
 			IConstructor kid = (IConstructor) args.get(i);
 			if (TreeAdapter.isList(kid) || TreeAdapter.isOpt(kid)) {
 				IList kidArgs = TreeAdapter.getArgs(kid);
 				IListWriter newKidArgs = vf.listWriter();
 				
 				for (int j = 0; j < kidArgs.length(); j++) {
 					IConstructor kidArg = (IConstructor) kidArgs.get(j);
 					if (!TreeAdapter.isLiteral(kidArg) && !TreeAdapter.isCILiteral(kidArg)) {
 						Pair p = lower(kidArg);
 						elts.add(p.obj);
 						newKidArgs.append(p.tree);
 					}
 					else {
 						newKidArgs.append(kidArg);
 					}
 					if (j < kidArgs.length() - 2) {
 						j++;
 						newKidArgs.append(kidArgs.get(j));
 					}
 				}
 				
 				kid = kid.set("args", newKidArgs.done());
 				newArgs.append(kid);		
 			}
 			else {
 				newArgs.append(kid);
 			}
 			if (i < args.length() - 2) {
 				i++;
 				newArgs.append(args.get(i)); // layout
 			}
 		}			
 		return new ListPair(newArgs.done(), elts);
 	}
 
 	
 	private Pair lower(IConstructor tree) {
 		if (TreeAdapter.isList(tree) || TreeAdapter.isOpt(tree)) {
 			// make vector for lists and optionals			
 			// TODO: deal with sep lists
 			ListPair lp = lowerArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, RT.vector(lp.objs.toArray()));
 		}
 		if (TreeAdapter.isLexical(tree)) {
 			String kind = TreeAdapter.getSortName(tree);
 			if (kind.equals("Number")) {
 				return new Pair(tree, matchNumber(TreeAdapter.yield(tree)));
 			}
 			if (kind.equals("Char")) {
 				return new Pair(tree, matchCharacter(TreeAdapter.yield(tree).substring(1)));
 			}
 			if (kind.equals("String")) {
 				return new Pair(tree, readString(tree));
 			}
 			if (kind.equals("RegExp")) {
 				return new Pair(tree, readRegexp(tree));
 			}
 			if (kind.equals("Symbol")) {
 				return new Pair(tree, interpretToken(TreeAdapter.yield(tree)));
 			}
 			throw new AssertionError("Unsupported lexical " + kind + ": " + tree);
 		}
 		if (TreeAdapter.getSortName(tree).equals("Form")) {
 			return read(tree);
 		}
 		if (TreeAdapter.getConstructorName(tree) == null) {
 			// can only occur at start
 			ListPair lp = lowerArgs(TreeAdapter.getArgs(tree));
 			tree = tree.set("args", lp.trees);
 			return new Pair(tree, lp.objs.get(0));
 		}
 		// an appl with a non-clojure label (e.g. literal, call in EBNF)
 		ListPair lp = lowerArgs(TreeAdapter.getArgs(tree));
 		String name = TreeAdapter.getConstructorName(tree);
 		tree = tree.set("args", lp.trees);
 		// TODO: namespaces;
 		ISourceLocation location = TreeAdapter.getLocation(tree);
 		IPersistentMap locAnno = RT.map(
 				RT.LINE_KEY, location.getBeginLine(), // mimick clojure
 				Keyword.intern("offset"), location.getOffset(),
 				Keyword.intern("length"), location.getLength(),
 				Keyword.intern("begin-line"), location.getBeginLine(),
 				Keyword.intern("end-line"), location.getEndLine(),
 				Keyword.intern("begin-column"), location.getBeginColumn(),
 				Keyword.intern("end-column"), location.getEndColumn()
 				);
 		IPersistentList args = PersistentList.create(lp.objs);
 		ISeq appl = RT.cons(Symbol.intern(name), args);
 		return new Pair(tree, ((IObj)appl).withMeta(locAnno));
 	}
 	
 	private Object getGrammar(Object op) {
 		Var var = Compiler.isMacro(op);
 		if (var != null) {
 			return var.meta().valAt(Keyword.intern("grammar")); 
 		}
 		return null;
 	}
 
 	private ListPair readArgs(IList list) {
 		List<Object> jlist = new ArrayList<Object>();
         IListWriter newList = vf.listWriter();
 
         for (int i = 0; i < list.length(); i++) {
                 IConstructor kid = (IConstructor) list.get(i);
                 if (!TreeAdapter.isLiteral(kid) && !TreeAdapter.isCILiteral(kid)) {
                         Pair p = read(kid);
                         if (p.obj != DISCARD) {
                                 jlist.add(p.obj);
                         }
                         newList.append(p.tree);
                 }
                 else {
                         newList.append(kid);
                 }
                 if (i < list.length() - 2) {
                 	i++;
                 	newList.append(list.get(i)); // layout
                 }
         }
         return new ListPair(newList.done(), jlist);
 	}
 	
 	private ListPair readForms(IList list) {
 		List<Object> jlist = new ArrayList<Object>();
 		IListWriter newList = vf.listWriter();
 		
 		for (int i = 0; i < list.length(); i++) {
 			IConstructor kid = (IConstructor) list.get(i);
 			if (TreeAdapter.isList(kid)) {
 				// Form*
 				IListWriter newKidArgs = vf.listWriter();
 				IList kidArgs = TreeAdapter.getArgs(kid);
 				for (int j = 0; j < kidArgs.length(); j++) {
 					IConstructor kidArg = (IConstructor)kidArgs.get(j);
 					if (!TreeAdapter.isLiteral(kidArg) && !TreeAdapter.isCILiteral(kidArg)) {
 						Pair p = read(kidArg);
 						if (p.obj != DISCARD) {
 							jlist.add(p.obj);
 						}
 						newKidArgs.append(p.tree);
 					}
 					else {
 						newKidArgs.append(kidArg);
 					}
 					if (j < kidArgs.length() - 2) { // layout never at end
 						j++;
 						newKidArgs.append(kidArgs.get(j));
 					}
 				}
 				kid = kid.set("args", newKidArgs.done());
 				newList.append(kid);
 			}
 			else {
 				newList.append(kid);
 			}
 			if (i < list.length() - 2) {
 				i++;
 				newList.append(list.get(i)); // layout
 			}
 		}
 		return new ListPair(newList.done(), jlist);
 	}
 
 	private Pair readMeta(IConstructor tree) {
 		IList args = TreeAdapter.getArgs(tree);
 		// ^ _ Form _ Form
 		IConstructor metaTree = (IConstructor) args.get(2);
 		IConstructor argTree = (IConstructor) args.get(4);
 		
 		Pair p = read(metaTree);
 		Object meta = p.obj;
 		
 		
 		if (meta instanceof Symbol || meta instanceof String) {
 			meta = RT.map(RT.TAG_KEY, meta);
 		} else if (meta instanceof Keyword) {
 			meta = RT.map(meta, RT.T);
 		} else if (!(meta instanceof IPersistentMap)) {
 			error("Metadata must be Symbol, Keyword, String or Map", metaTree);
 			meta = null;
 		}
 
 		Pair argP = read(argTree);
 		Object o = argP.obj;
 
 		
 		IListWriter newArgs = vf.listWriter();
 		for (int i = 0; i < args.length(); i++) {
 			if (i == 2) {
 				newArgs.append(p.tree);
 			}
 			else if (i == 4) {
 				newArgs.append(argP.tree);
 			}
 			else {
 				newArgs.append(args.get(i));
 			}
 		}
 		tree = tree.set("args", newArgs.done());
 
 		if (meta == null) {
 			// if meta wasn't of the correct type, just skip it.
 			return new Pair(tree, o);
 		}
 		
 		
 		if (o instanceof IMeta) {
 			int line = getLineNumber(argTree);
 			if (line != -1 && o instanceof ISeq) {
 				meta = ((IPersistentMap) meta).assoc(RT.LINE_KEY, line);
 			}
 			if (o instanceof IReference) {
 				((IReference) o).resetMeta((IPersistentMap) meta);
 				return new Pair(tree, o);
 			}
 			Object ometa = RT.meta(o);
 			for (ISeq s = RT.seq(meta); s != null; s = s.next()) {
 				IMapEntry kv = (IMapEntry) s.first();
 				ometa = RT.assoc(ometa, kv.getKey(), kv.getValue());
 			}
 			return new Pair(tree, ((IObj) o).withMeta((IPersistentMap) ometa));
 		} else {
 			error("Metadata can only be applied to IMetas", argTree);
 			return new Pair(tree, o);
 		}
 	}
 
 	private Object readRegexp(IConstructor tree) {
 		String str = TreeAdapter.yield(tree).substring(2);
 		StringBuilder sb = new StringBuilder();
 		int i = 0;
 		for (int ch = str.charAt(i); ch != '"'; ch = str.charAt(++i)) {
 			sb.append((char) ch);
 			if (ch == '\\') // escape
 			{
 				ch = str.charAt(++i);
 				sb.append((char) ch);
 			}
 		}
 		try {
 			return Pattern.compile(sb.toString());
 		}
 		catch (PatternSyntaxException e) {
 			error(e.getMessage(), tree);
 			return null;
 		}
 	}
 
 	private Object readString(IConstructor tree) {
 		/*
 		 * as per the Clojure grammar, the exceptions here 
 		 * can never occur.
 		 */
 		String str = TreeAdapter.yield(tree).substring(1);
 		StringBuilder sb = new StringBuilder();
 		int i = 0;
 		for (int ch = str.charAt(i); ch != '"'; ch = str.charAt(++i)) {
 			if (ch == '\\') // escape
 			{
 				ch = str.charAt(++i);
 				if (ch == -1)
 					throw Util.runtimeException("EOF while reading string");
 				switch (ch) {
 				case 't':
 					ch = '\t';
 					break;
 				case 'r':
 					ch = '\r';
 					break;
 				case 'n':
 					ch = '\n';
 					break;
 				case '\\':
 					break;
 				case '"':
 					break;
 				case 'b':
 					ch = '\b';
 					break;
 				case 'f':
 					ch = '\f';
 					break;
 				case 'u': {
 					ch = str.charAt(++i);
 					if (Character.digit(ch, 16) == -1) {
 						throw Util.runtimeException("Invalid unicode escape: \\u" + (char) ch);
 					}
 					ch = readUnicodeChar(str.substring(i, i + 4), 0, 4, 16);
 					i += 4;
 					break;
 				}
 				default: {
 					if (Character.isDigit(ch)) {
 						ch = readUnicodeChar(str.substring(i, i + 3), 0, 3, 8);
 						i += 3;
 						if (ch > 0377) {
 							throw Util.runtimeException("Octal escape sequence must be in range [0, 377].");
 						}
 					} else {
 						throw Util.runtimeException("Unsupported escape character: \\" + (char) ch);
 					}
 				}
 				}
 			}
 			sb.append((char) ch);
 		}
 		return sb.toString();
 	}
 
 	private Object matchCharacter(String token) {
 		if (token.length() == 1)
 			return Character.valueOf(token.charAt(0));
 		else if (token.equals("newline"))
 			return '\n';
 		else if (token.equals("space"))
 			return ' ';
 		else if (token.equals("tab"))
 			return '\t';
 		else if (token.equals("backspace"))
 			return '\b';
 		else if (token.equals("formfeed"))
 			return '\f';
 		else if (token.equals("return"))
 			return '\r';
 		else if (token.startsWith("u")) {
 			char c = (char) readUnicodeChar(token, 1, 4, 16);
 			if (c >= '\uD800' && c <= '\uDFFF') // surrogate code unit?
 				throw Util.runtimeException("Invalid character constant: \\u"
 						+ Integer.toString(c, 16));
 			return c;
 		} else if (token.startsWith("o")) {
 			int len = token.length() - 1;
 			if (len > 3)
 				throw Util
 						.runtimeException("Invalid octal escape sequence length: "
 								+ len);
 			int uc = readUnicodeChar(token, 1, len, 8);
 			if (uc > 0377)
 				throw Util
 						.runtimeException("Octal escape sequence must be in range [0, 377].");
 			return (char) uc;
 		}
 		throw Util.runtimeException("Unsupported character: \\" + token);
 	}
 
 	private int readUnicodeChar(String token, int offset, int length,
 			int base) {
 		if (token.length() != offset + length)
 			throw new IllegalArgumentException("Invalid unicode character: \\"
 					+ token);
 		int uc = 0;
 		for (int i = offset; i < offset + length; ++i) {
 			int d = Character.digit(token.charAt(i), base);
 			if (d == -1)
 				throw new IllegalArgumentException("Invalid digit: "
 						+ token.charAt(i));
 			uc = uc * base + d;
 		}
 		return (char) uc;
 	}
 
 	private Object interpretToken(String s) {
 		if (s.equals("nil")) {
 			return null;
 		} else if (s.equals("true")) {
 			return RT.T;
 		} else if (s.equals("false")) {
 			return RT.F;
 		} else if (s.equals("/")) {
 			return SLASH;
 		} else if (s.equals("clojure.core//")) {
 			return CLOJURE_SLASH;
 		}
 		Object ret = null;
 
 		ret = matchSymbol(s);
 		if (ret != null)
 			return ret;
 
 		throw Util.runtimeException("Invalid token: " + s);
 	}
 
 	private Object matchSymbol(String s) {
 		Matcher m = symbolPat.matcher(s);
 		if (m.matches()) {
 			String ns = m.group(1);
 			String name = m.group(2);
 			if (ns != null && ns.endsWith(":/") || name.endsWith(":")
 					|| s.indexOf("::", 1) != -1)
 				return null;
 			if (s.startsWith("::")) {
 				Symbol ks = Symbol.intern(s.substring(2));
 				Namespace kns;
 				if (ks.ns != null)
 					kns = Compiler.namespaceFor(ks);
 				else
 					kns = Compiler.currentNS();
 				// auto-resolving keyword
 				if (kns != null)
 					return Keyword.intern(kns.name.name, ks.name);
 				else
 					return null;
 			}
 			boolean isKeyword = s.charAt(0) == ':';
 			Symbol sym = Symbol.intern(s.substring(isKeyword ? 1 : 0));
 			if (isKeyword)
 				return Keyword.intern(sym);
 			return sym;
 		}
 		return null;
 	}
 
 	private Object matchNumber(String s) {
 		Matcher m = intPat.matcher(s);
 		if (m.matches()) {
 			if (m.group(2) != null) {
 				if (m.group(8) != null)
 					return BigInt.ZERO;
 				return Numbers.num(0);
 			}
 			boolean negate = (m.group(1).equals("-"));
 			String n;
 			int radix = 10;
 			if ((n = m.group(3)) != null)
 				radix = 10;
 			else if ((n = m.group(4)) != null)
 				radix = 16;
 			else if ((n = m.group(5)) != null)
 				radix = 8;
 			else if ((n = m.group(7)) != null)
 				radix = Integer.parseInt(m.group(6));
 			if (n == null)
 				return null;
 			BigInteger bn = new BigInteger(n, radix);
 			if (negate)
 				bn = bn.negate();
 			if (m.group(8) != null)
 				return BigInt.fromBigInteger(bn);
 			return bn.bitLength() < 64 ? Numbers.num(bn.longValue()) : BigInt
 					.fromBigInteger(bn);
 		}
 		m = floatPat.matcher(s);
 		if (m.matches()) {
 			if (m.group(4) != null)
 				return new BigDecimal(m.group(1));
 			return Double.parseDouble(s);
 		}
 		m = ratioPat.matcher(s);
 		if (m.matches()) {
 			return Numbers.divide(Numbers.reduceBigInt(BigInt
 					.fromBigInteger(new BigInteger(m.group(1)))), Numbers
 					.reduceBigInt(BigInt.fromBigInteger(new BigInteger(m
 							.group(2)))));
 		}
 		return null;
 	}
 
 	@SuppressWarnings("serial")
 	public class ReaderException extends RuntimeException {
 		final int line;
 
 		public ReaderException(int line, String message) {
 			super(message);
 			this.line = line;
 		}
 	}
 
 }
