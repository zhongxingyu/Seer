 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2005-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 package macromedia.asc.embedding;
 
 import macromedia.abc.AbcParser;
 import macromedia.abc.Optimizer;
 
 import macromedia.asc.embedding.avmplus.ActionBlockEmitter;
 import macromedia.asc.embedding.avmplus.Features;
 import macromedia.asc.embedding.avmplus.GlobalBuilder;
 import macromedia.asc.parser.*;
 import macromedia.asc.semantics.*;
 import macromedia.asc.util.*;
 import macromedia.asc.util.graph.DependencyGraph;
 import macromedia.asc.util.graph.Algorithms;
 import macromedia.asc.util.graph.Visitor;
 import macromedia.asc.util.graph.Vertex;
 
 import static macromedia.asc.embedding.avmplus.Features.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * asc batch compiler
  */
 public class ScriptCompiler
 {
 	private static List<File> file;
 	private static List<Context> cx;
 	private static List<ActionBlockEmitter> emitter;
 	private static List<ProgramNode> node;
 	private static List<FlowAnalyzer> fa;
 	private static Set<Pair> inheritance;
 	private static Set<Pair> type;
     private static Set<Pair> expr;
 
     private static ContextStatics s;
 
 	private static ActionBlockEmitter mainEmitter;
     private static Context mainContext;
     private static File mainFile;
 
     private static String outputFile;
     private static String outputDir;
 
     private static boolean builtinFlag;
 	private static boolean debugFlag;
 	private static boolean optimize;
 	
 	public static void main(String[] args) throws Throwable
 	{
 		long startTime = System.currentTimeMillis();
 
 		init(args);
 
 		int start = 0, end = file.size();
 
 		while (start < end)
 		{
 			parse(start, end);
 			fa_part1(start, end);
 			resolveInheritance(start, end);
 
 			start = end;
 			end = file.size();
 
 			if (start < end) continue;
 
 			sortInheritance();
 			fa_part2();
 			resolveType();
 
 
             start = end;
 			end = file.size();
 
 			if (start < end) continue;
 
 			importType();
             // In theory, this should be in a second compile loop in order to reduce memory usage.
             resolveExpression();
             importExpr();
             md();
 			ce();
 			cg();
 
 
 			start = end;
 			end = file.size();
 		}
 
 		clear();
 
 		System.err.println("Files: " + file.size() + " Time: " + (System.currentTimeMillis() - startTime) + "ms");
 	}
 
     static ObjectList<ConfigVar> config_vars = new ObjectList<ConfigVar>();
 	private static void init(String[] args) throws Throwable
 	{
 		ObjectList<String> filespecs = new ObjectList<String>();
 		ObjectList<Boolean> imported = new ObjectList<Boolean>();
 
 		boolean use_static_semantics = false;
 		boolean decimalFlag = false;
         boolean useas3 = false;
 
         for (int i = 0, length = args.length; i < length; i++)
 		{
 			if (args[i].equals("-builtin"))
 			{
 				builtinFlag = true;
 			}
 			else if (args[i].equals("-abcfuture"))
 			{
 				FUTURE_ABC = true;
 			}
 			else if (args[i].equals("-optimize"))
 			{
 				optimize = true;
 			}
 			else if (args[i].equals("-strict"))
 			{
 				use_static_semantics = true;
 			}
 			else if (args[i].equals("-d"))
 			{
 				debugFlag = true;
 			}
 			else if (args[i].equals("-m"))
 			{
 				decimalFlag = true;
 			}
             else if (args[i].equals("-out"))
             {
                 outputFile = args[++i];
             }
             else if (args[i].equals("-outdir"))
             {
                 outputDir = args[++i];
             }
             else if (args[i].equals("-import"))
 			{
 				filespecs.add(args[++i]);
 				imported.add(new Boolean(true));
 			}
             else if(args[i].equals("-config"))
             {
                 ++i;
                 String temp = args[i];
                 ConfigVar cv = Main.parseConfigVar(temp);
                 if( cv != null)
                     config_vars.push_back(cv);
                 else
                 	System.err.println("ERROR: couldn't parse config var "+temp);
             }
             else if(args[i].equals("-AS3"))
             {
                 useas3 = true;
             }
             else
 			{
 				filespecs.add(args[i]);
 				imported.add(new Boolean(false));
 			}
 		}
 
 		TypeValue.init();
 		ObjectValue.init();
 		s = new ContextStatics();
 		s.use_static_semantics = use_static_semantics;
         if( useas3 )
             s.dialect = Features.DIALECT_AS3;
         s.es4_numerics = decimalFlag;	// to make decimal things work
 
 		file = new ArrayList<File>(filespecs.size());
         cx = new ArrayList<Context>(filespecs.size());
         emitter = new ArrayList<ActionBlockEmitter>(filespecs.size());
 
 		for (int i = 0, length = filespecs.size(); i < length; i++)
 		{
 			boolean importFlag = imported.get(i).booleanValue();
 
 			File f = new File(filespecs.get(i));
 			if (f.exists() && f.isFile())
 			{
 				f = f.getCanonicalFile();
 				file.add(f);
 
 				Context cxFile = new Context(s);
 				cx.add(cxFile);
 				
 				cxFile.config_vars.addAll(config_vars);
 				
                 if (!importFlag)
                 {
 					// last non-imported file will be "main file"
                     mainFile = f;
                     mainContext = cxFile;
                 }
 
 				if (importFlag)
 				{
 					emitter.add(new ActionBlockEmitter(cxFile, f.getPath(),
 													   new StringPrintWriter(),
 													   new StringPrintWriter(),
 													   false, false, false, debugFlag));
 				}
 				else
 				{
 					if (mainEmitter == null)
 					{
 						mainEmitter = new ActionBlockEmitter(cxFile, f.getPath(),
 															 new StringPrintWriter(),
 															 new StringPrintWriter(),
 															 false, false, false, debugFlag);
 					}
 					emitter.add(mainEmitter);
 				}
 			}
             else
             {
                 System.err.println("Warning, unable to open file " + f.getPath());
             }
         }
 
 		node = new ArrayList<ProgramNode>(file.size());
 		fa = new ArrayList<FlowAnalyzer>(file.size());
 		inheritance = new HashSet<Pair>();
 		type = new HashSet<Pair>();
         expr = new HashSet<Pair>();
     }
 
 	private static void parse(int start, int end) throws Throwable
 	{
 		for (int i = start; i < end; i++)
 		{
 			Context cxi = cx.get(i);
 			cxi.setEmitter(emitter.get(i));
 			cxi.setScriptName(file.get(i).getName());
 			cxi.setPath(file.get(i).getParent());
 
 			ProgramNode program;
 			if (file.get(i).getName().endsWith(".as"))
 			{
 				program = new Parser(cxi, new FileInputStream(file.get(i)), file.get(i).getPath(), null).parseProgram();
 			}
 			else
 			{
 				program = new AbcParser(cxi, file.get(i).getPath()).parseAbc();
 			}
 			node.add(program);
 
 			cxi.getNodeFactory().pkg_defs.clear();
             cxi.getNodeFactory().compound_names.clear();
             
         	ConfigurationEvaluator ce = new ConfigurationEvaluator();
         	program.evaluate(cxi, ce);
 		}
 	}
 
 	private static void fa_part1(int start, int end)
 	{
 		for (int i = start; i < end; i++)
 		{
 			if (cx.get(i).errorCount() == 0 && node.get(i).state == ProgramNode.Inheritance)
 			{
 				cx.get(i).pushScope(new ObjectValue(cx.get(i), new GlobalBuilder(), null));
 				FlowGraphEmitter fgEmitter = new FlowGraphEmitter(cx.get(i), file.get(i).getPath(), false);
 				fa.add(new FlowAnalyzer(fgEmitter));
 				node.get(i).evaluate(cx.get(i), fa.get(i));
 				cx.get(i).popScope();
 			}
 		}
 	}
 
 	private static void resolveInheritance(int start, int end) throws Throwable
 	{
 		for (int i = start; i < end; i++)
 		{
 			for (Iterator<ReferenceValue> k = node.get(i).fa_unresolved.iterator(); k.hasNext();)
 			{
 				ReferenceValue ref = k.next();
 				boolean found = false;
 				for (int j = 0, size = (ref.getImmutableNamespaces() != null) ? ref.getImmutableNamespaces().size() : 0; j < size; j++)
 				{
 					QName qname = new QName(ref.getImmutableNamespaces().get(j), ref.name);
 					int where = findClass(qname);
 					if (where != -1)
 					{
 						if (i != where)
 						{
 							Pair p = new Pair(i, where);
 							if (!inheritance.contains(p))
 							{
 								inheritance.add(p);
 							}
 						}
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					System.err.println(ref.toMultiName() + " on line " + cx.get(i).getInputLine(ref.getPosition()) + " of file " + file.get(i) + " not resolved");
 				}
 			}
 
 			node.get(i).fa_unresolved.clear();
 		}
 	}
 
 	private static void sortInheritance() throws Throwable
 	{
 		for (Iterator<Pair> i = inheritance.iterator(); i.hasNext();)
 		{
 			Pair p = i.next();
 			if (!p.processed)
 			{
 				fa.get(p.i).inheritSlots(node.get(p.where).frame, node.get(p.i).frame, node.get(p.i).frame.builder, cx.get(p.i));
 				p.processed = true;
 			}
 		}
 
 		final DependencyGraph<Integer> g = new DependencyGraph<Integer>();
 
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			String path = file.get(i).getPath();
 			g.put(path, i);
 
 			if (!g.containsVertex(path))
 			{
 				g.addVertex(new Vertex<String>(path));
 			}
 
 			for (Iterator<Pair> j = inheritance.iterator(); j.hasNext();)
 			{
 				Pair p = j.next();
 				if (p.i == i)
 				{
 					g.addDependency(path, file.get(p.where).getPath());
 				}
 			}
 		}
 
 		final List<Integer> tsort = new ArrayList<Integer>(node.size());
 
 		Algorithms.topologicalSort(g, new Visitor<String>()
 		{
 			public void visit(Vertex<String> v)
 			{
 				String name = v.getWeight();
 				tsort.add(g.get(name));
 			}
 		});
 
 		if (node.size() > tsort.size())
 		{
 			for (int i = 0, length = node.size(); i < length; i++)
 			{
 				int j = 0;
 				for (; j < tsort.size(); j++)
 				{
 					if (tsort.get(j) == i)
 					{
 						break;
 					}
 				}
 				if (j == tsort.size())
 				{
 					String path = file.get(i).getPath();
 					System.out.println(path + " in circular reference");
 				}
 			}
 		}
 		else
 		{
 			List<File> tempFile = new ArrayList<File>(file.size());
 			List<Context> tempCX = new ArrayList<Context>(cx.size());
 			List<ActionBlockEmitter> tempEmitter = new ArrayList<ActionBlockEmitter>(emitter.size());
 			List<ProgramNode> tempNode = new ArrayList<ProgramNode>(node.size());
 			List<FlowAnalyzer> tempFA = new ArrayList<FlowAnalyzer>(fa.size());
 
 			for (int i = 0, length = tsort.size(); i < length; i++)
 			{
 				int loc = tsort.get(i);
 
 				tempFile.add(file.get(loc));
 				tempCX.add(cx.get(loc));
 				tempEmitter.add(emitter.get(loc));
 				tempNode.add(node.get(loc));
 				tempFA.add(fa.get(loc));
 			}
 
 			file = tempFile;
 			cx = tempCX;
 			emitter = tempEmitter;
 			node = tempNode;
 			fa = tempFA;
 
 			for (Iterator<Pair> i = type.iterator(); i.hasNext();)
 			{
 				Pair p = i.next();
 				for (int j = 0, length = tsort.size(); j < length; j++)
 				{
 					if (tsort.get(j) == p.i)
 					{
 						p.i = j;
 						break;
 					}
 				}
 				for (int j = 0, length = tsort.size(); j < length; j++)
 				{
 					if (tsort.get(j) == p.where)
 					{
 						p.where = j;
 						break;
 					}
 				}
 			}
 
 			for (Iterator<Pair> i = inheritance.iterator(); i.hasNext();)
 			{
 				Pair p = i.next();
 				for (int j = 0, length = tsort.size(); j < length; j++)
 				{
 					if (tsort.get(j) == p.i)
 					{
 						p.i = j;
 						break;
 					}
 				}
 				for (int j = 0, length = tsort.size(); j < length; j++)
 				{
 					if (tsort.get(j) == p.where)
 					{
 						p.where = j;
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	private static void fa_part2()
 	{
 		for (int i = 0, length = file.size(); i < length; i++)
 		{
 			if (cx.get(i).errorCount() == 0 && node.get(i).state == ProgramNode.Else)
 			{
 				cx.get(i).pushScope(node.get(i).frame);
 				node.get(i).evaluate(cx.get(i), fa.get(i));
 				cx.get(i).popScope();
 			}
 		}
 	}
 
 	private static void resolveType() throws Throwable
 	{
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			for (Iterator<ReferenceValue> k = node.get(i).ce_unresolved.iterator(); k.hasNext();)
 			{
 				ReferenceValue ref = k.next();
 				boolean found = false;
 				for (int j = 0, size = (ref.getImmutableNamespaces() != null) ? ref.getImmutableNamespaces().size() : 0; j < size; j++)
 				{
 					QName qname = new QName(ref.getImmutableNamespaces().get(j), ref.name);
 					int where = findClass(qname);
 					if (where != -1)
 					{
 						if (i != where)
 						{
 							Pair p = new Pair(i, where);
 							if (!type.contains(p))
 							{
 								type.add(p);
 							}
 						}
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					System.err.println(ref.toMultiName() + " on line " + cx.get(i).getInputLine(ref.getPosition()) + " of file " + file.get(i) + " not resolved");
 				}
 			}
 
 			node.get(i).ce_unresolved.clear();
 		}
 
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			for (Iterator<ReferenceValue> k = node.get(i).body_unresolved.iterator(); k.hasNext();)
 			{
 				ReferenceValue ref = k.next();
 				boolean found = false;
 				for (int j = 0, size = (ref.getImmutableNamespaces() != null) ? ref.getImmutableNamespaces().size() : 0; j < size; j++)
 				{
 					QName qname = new QName(ref.getImmutableNamespaces().get(j), ref.name);
 					int where = findClass(qname);
 					if (where != -1)
 					{
 						if (i != where)
 						{
 							Pair p = new Pair(i, where);
 							if (!type.contains(p))
 							{
 								type.add(p);
 							}
 						}
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					System.err.println(ref.toMultiName() + " on line " + cx.get(i).getInputLine(ref.getPosition()) + " of file " + file.get(i) + " not resolved");
 				}
 			}
 
 			node.get(i).body_unresolved.clear();
 		}
 
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			for (Iterator<ReferenceValue> k = node.get(i).ns_unresolved.iterator(); k.hasNext();)
 			{
 				ReferenceValue ref = k.next();
 				boolean found = false;
 				for (int j = 0, size = (ref.getImmutableNamespaces() != null) ? ref.getImmutableNamespaces().size() : 0; j < size; j++)
 				{
 					QName qname = new QName(ref.getImmutableNamespaces().get(j), ref.name);
 					int where = findDefinition(qname);
 					if (where != -1)
 					{
 						if (i != where)
 						{
 							Pair p = new Pair(i, where);
 							if (!type.contains(p))
 							{
 								type.add(p);
 							}
 						}
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					System.err.println(ref.toMultiName() + " on line " + cx.get(i).getInputLine(ref.getPosition()) + " of file " + file.get(i) + " not resolved");
 				}
 			}
 
 			node.get(i).ns_unresolved.clear();
 		}
 	}
 
 	private static void importType() throws Throwable
 	{
 		for (Iterator<Pair> i = type.iterator(); i.hasNext();)
 		{
 			Pair p = i.next();
 			if (!p.processed)
 			{
 				if (!inheritance.contains(p))
 				{
 					fa.get(p.i).inheritSlots(node.get(p.where).frame, node.get(p.i).frame, node.get(p.i).frame.builder, cx.get(p.i));
 				}
 				p.processed = true;
 			}
 		}
 	}
 
     private static void importExpr() throws Throwable
     {
         for (Iterator<Pair> i = expr.iterator(); i.hasNext();)
         {
             Pair p = i.next();
             if (!p.processed)
             {
                 if (!inheritance.contains(p))
                 {
                     fa.get(p.i).inheritSlots(node.get(p.where).frame, node.get(p.i).frame, node.get(p.i).frame.builder, cx.get(p.i));
                 }
                 p.processed = true;
             }
         }
     }
 
     private static void md()
 	{
 		for (int i = 0, length = file.size(); i < length; i++)
 		{
 			if (cx.get(i).errorCount() == 0 && file.get(i).getName().endsWith(".as") && emitter.get(i) != null)
 			{
 				cx.get(i).pushScope(node.get(i).frame);
 				MetaDataEvaluator analyzer = new MetaDataEvaluator();
 				node.get(i).evaluate(cx.get(i), analyzer);
 				cx.get(i).popScope();
 			}
 		}
 
 	}
 
 	private static void ce()
 	{
         ArrayList<ConstantEvaluator> ces = new ArrayList();
         for (int i = 0, length = file.size(); i < length; i++)
 		{
 			if (cx.get(i).errorCount() == 0)
 			{
 				cx.get(i).pushScope(node.get(i).frame);
 				ces.add(new ConstantEvaluator(cx.get(i)));
                 ces.get(i).PreprocessDefinitionTypeInfo(cx.get(i), node.get(i));
                 //node.get(i).evaluate(cx.get(i), analyzer);
 				cx.get(i).popScope();
 			}
 		}
         for (int i = 0, length = file.size(); i < length; i++)
         {
             if (cx.get(i).errorCount() == 0)
             {
                 cx.get(i).pushScope(node.get(i).frame);
                 ConstantEvaluator analyzer = ces.get(i);
                 node.get(i).evaluate(cx.get(i), analyzer);
                 cx.get(i).popScope();
             }
         }
 	}
 
 	private static void cg() throws Throwable
 	{
 		for (int i = 0, length = file.size(); i < length; i++)
 		{
 			if (cx.get(i).errorCount() == 0 && file.get(i).getName().endsWith(".as") && emitter.get(i) != null)
 			{
 				cx.get(i).setEmitter(emitter.get(i));
 				cx.get(i).pushScope(node.get(i).frame);
 				CodeGenerator generator = new CodeGenerator(cx.get(i).getEmitter());
 				generator.emitScriptNames = true;
 				node.get(i).evaluate(cx.get(i), generator);
 
 				/*ByteList bytes = new ByteList();
 				cx.get(i).getEmitter().emit(bytes);
 				// String str = ((ActionBlockEmitter) cx.get(i).getEmitter()).il_str();
 				emitter.set(i, null);
 				FileOutputStream out = new FileOutputStream(new File(file.get(i).getParentFile(), file.get(i).getName().substring(0, file.get(i).getName().length() - "as".length()) + "abc"));
 				System.err.println(file.get(i).getName() + ": " + bytes.size());
 				out.write(bytes.toByteArray());
 				out.flush();
 				out.close();*/
 
 				/*
 				FileWriter fout = new FileWriter(new File(file.get(i).getParentFile(), file.get(i).getName().substring(0, file.get(i).getName().length() - "as".length()) + "il"));
 				fout.write(str);
 				fout.flush();
 				fout.close();
                 */
 
 				cx.get(i).popScope();
 			}
 		}
 
         if (builtinFlag)
         {
             // If compiling builtin, change the order of
             // scripts so the first script is the last script.
             mainEmitter.reorderMainScript();
         }
 		ByteList bytes = new ByteList();
 		mainEmitter.emit(bytes);
 		// String str = ((ActionBlockEmitter) cx.get(i).getEmitter()).il_str();
         if (outputFile == null)
         {
             outputFile = mainFile.getName().substring(0, mainFile.getName().length() - ".as".length());
         }
         File out_dir = null;
         if( outputDir == null )
         {
             out_dir = mainFile.getParentFile();
         }
         else
         {
             out_dir = new File(outputDir);
         }
 
         FileOutputStream out = new FileOutputStream(new File(out_dir, outputFile + ".abc"));
 
         if (optimize)
 			bytes = Optimizer.optimize(bytes);
 		byte[] abc = bytes.toByteArray(false);
 
 		System.err.println(outputFile + ": " + abc.length);
 		out.write(abc);
 		out.close();
 
         // Reset the path so printNative outputs to the right directory
         mainContext.setPath(out_dir.getPath());
 
         Compiler.printNative(mainContext, outputFile, mainEmitter, abc);
 	}
 
 	private static void resolveExpression() throws Throwable
 	{
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			for (Iterator<ReferenceValue> k = node.get(i).rt_unresolved.iterator(); k.hasNext();)
 			{
 				ReferenceValue ref = k.next();
				boolean found = false;
 				for (int j = 0, size = (ref.getImmutableNamespaces() != null) ? ref.getImmutableNamespaces().size() : 0; j < size; j++)
 				{
 					QName qname = new QName(ref.getImmutableNamespaces().get(j), ref.name);
					if (qname.ns instanceof UnresolvedNamespace && ((UnresolvedNamespace) qname.ns).resolved)
 					{
 						found = true;
 						break;
 					}
 					int where = findDefinition(qname);
 					if (where != -1)
 					{
                         Pair p = new Pair(i, where);
                         if (!expr.contains(p))
                         {
                             expr.add(p);
                         }
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					System.err.println(ref.toMultiName() + " on line " + cx.get(i).getInputLine(ref.getPosition()) + " of file " + file.get(i) + " not resolved");
 				}
			}
 
 			node.get(i).rt_unresolved.clear();
 		}
 	}
 
 	private static void clear()
 	{
 		s.clear();
 
 		ObjectValue.clear();
 		TypeValue.clear();
 	}
 
 	private static int findDefinition(QName defName) throws Throwable
 	{
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			Names names = node.get(i).frame.builder.getNames();
 			
 			for (int j = 0; j < 4; j++)
 			{
 				if (names != null && names.containsKey(defName.name, defName.ns, j))
 				{
 					return i;
 				}
 			}
 		}
 
 		return searchClasspath(defName);
 	}
 
 	private static int findClass(QName className) throws Throwable
 	{
 		for (int i = 0, length = node.size(); i < length; i++)
 		{
 			for (int j = 0, size = (node.get(i).clsdefs != null) ? node.get(i).clsdefs.size() : 0; j < size; j++)
 			{
 				ClassDefinitionNode clsdef = node.get(i).clsdefs.get(j);
 				if (clsdef.cframe.builder.classname.equals(className))
 				{
 					return i;
 				}
 			}
 		}
 
 		return searchClasspath(className);
 	}
 
 	private static int searchClasspath(QName qname) throws Throwable
 	{
 		String path = qname.ns.name.replace('.', File.separatorChar) + File.separatorChar + qname.name;
 		File f = new File(path + ".as");
 		if (f.exists() && f.isFile())
 		{
 			f = f.getCanonicalFile();
 			Context context = new Context(s);
 
 			int where = file.indexOf(f);
 			if (where == -1)
 			{
 				file.add(f);
 				cx.add(context);
 				emitter.add(new ActionBlockEmitter(context, f.getPath(), new StringPrintWriter(), new StringPrintWriter(), false, false, false, debugFlag));
 				where = file.size() - 1;
 			}
 			return where;
 		}
 
 		return -1;
 	}
 
 	static class Pair
 	{
 		Pair(int i, int where)
 		{
 			this.i = i;
 			this.where = where;
 			processed = false;
 		}
 
 		int i, where;
 		boolean processed;
 
 		public boolean equals(Object obj)
 		{
 			if (obj instanceof Pair)
 			{
 				return i == ((Pair) obj).i && where == ((Pair) obj).where;
 			}
 			else
 			{
 				return false;
 			}
 		}
 
 		public int hashCode()
 		{
 			return (((1 * 17) + i) * 17) + where;
 		}
 	}
 }
