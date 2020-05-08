 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.instr;
 
 import gnu.trove.TIntObjectHashMap;
 import javassist.*;
 import javassist.expr.*;
 
 import chord.doms.DomE;
 import chord.doms.DomF;
 import chord.doms.DomH;
 import chord.doms.DomI;
 import chord.doms.DomM;
 import chord.doms.DomL;
 import chord.doms.DomR;
 import chord.doms.DomP;
 import chord.doms.DomB;
 import chord.doms.DomW;
 import chord.instr.InstrScheme.EventFormat;
 import chord.project.CFGLoopFinder;
 import chord.project.ChordRuntimeException;
 import chord.project.Program;
 import chord.project.ProgramDom;
 import chord.project.Project;
 import chord.project.Properties;
 import chord.project.Runtime;
 import chord.util.FileUtils;
 import chord.util.IndexHashMap;
 import chord.util.IndexMap;
 import chord.util.IndexSet;
 
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.ControlFlowGraph;
 import joeq.Compiler.Quad.Operator;
 import joeq.Compiler.Quad.Quad;
 import joeq.Util.Templates.ListIterator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Instrumentor {
 	protected static final String runtimeClassName = "chord.project.Runtime.";
 
 	protected static final String enterMethodEventCall = runtimeClassName + "enterMethodEvent(";
 	protected static final String leaveMethodEventCall = runtimeClassName + "leaveMethodEvent(";
 	protected static final String enterLoopEventCall = runtimeClassName + "enterLoopEvent(";
 	protected static final String leaveLoopEventCall = runtimeClassName + "leaveLoopEvent(";
 
 	protected static final String befNewEventCall = runtimeClassName + "befNewEvent(";
 	protected static final String aftNewEventCall = runtimeClassName + "aftNewEvent(";
 	protected static final String newEventCall = runtimeClassName + "newEvent(";
 	protected static final String newArrayEventCall = runtimeClassName + "newArrayEvent(";
 
 	protected static final String getstaticPriEventCall = runtimeClassName + "getstaticPrimitiveEvent(";
 	protected static final String putstaticPriEventCall = runtimeClassName + "putstaticPrimitiveEvent(";
 	protected static final String getstaticRefEcentCall = runtimeClassName + "getstaticReferenceEvent(";
 	protected static final String putstaticRefEventCall = runtimeClassName + "putstaticReferenceEvent(";
 
 	protected static final String getfieldPriEventCall = runtimeClassName + "getfieldPrimitiveEvent(";
 	protected static final String putfieldPriEventCall = runtimeClassName + "putfieldPrimitiveEvent(";
 	protected static final String getfieldReference = runtimeClassName + "getfieldReferenceEvent(";
 	protected static final String putfieldRefEventCall = runtimeClassName + "putfieldReferenceEvent(";
 
 	protected static final String aloadPriEventCall = runtimeClassName + "aloadPrimitiveEvent(";
 	protected static final String aloadRefEventCall = runtimeClassName + "aloadReferenceEvent(";
 	protected static final String astorePriEventCall = runtimeClassName + "astorePrimitiveEvent(";
 	protected static final String astoreRefEventCall = runtimeClassName + "astoreReferenceEvent(";
 
 	protected static final String methodCallEventCall = runtimeClassName + "methodCallEvent(";
 	protected static final String returnPriEventCall = runtimeClassName + "returnPrimtiveEvent(";
 	protected static final String returnRefEventCall = runtimeClassName + "returnReferenceEvent(";
 	protected static final String explicitThrowEventCall = runtimeClassName + "explicitThrowEvent(";
 	protected static final String implicitThrowEventCall = runtimeClassName + "implicitThrowEvent(";
 	protected static final String quadEventCall = runtimeClassName + "quadEvent(";
 	protected static final String basicBlockEventCall = runtimeClassName + "basicBlockEvent(";
 
 	protected static final String threadStartEventCall = runtimeClassName + "threadStartEvent(";
 	protected static final String threadJoinEventCall = runtimeClassName + "threadJoinEvent(";
 	protected static final String waitEventCall = runtimeClassName + "waitEvent(";
 	protected static final String notifyEventCall = runtimeClassName + "notifyEvent(";
 	protected static final String acquireLockEventCall = runtimeClassName + "acquireLockEvent(";
 	protected static final String releaseLockEventCall = runtimeClassName + "releaseLockEvent(";
 
 	protected InstrScheme scheme;
 	protected Program program;
 
 	protected DomF domF;
 	protected DomM domM;
 	protected DomH domH;
 	protected DomE domE;
 	protected DomI domI;
 	protected DomL domL;
 	protected DomR domR;
 	protected DomP domP;
 	protected DomB domB;
 	protected DomW domW;
 
 	protected IndexMap<String> Fmap;
 	protected IndexMap<String> Mmap;
 	protected IndexMap<String> Hmap;
 	protected IndexMap<String> Emap;
 	protected IndexMap<String> Imap;
 	protected IndexMap<String> Lmap;
 	protected IndexMap<String> Rmap;
 	protected IndexMap<String> Pmap;
 	protected IndexMap<String> Bmap;
 	protected IndexMap<String> Wmap;
 
 	private ClassPool pool;
 	private CtClass exType;
 	private MyExprEditor exprEditor = new MyExprEditor();
 	private CFGLoopFinder finder = new CFGLoopFinder();
 
 	protected boolean convert;
 	protected boolean genBasicBlockEvent;
 	protected boolean genQuadEvent;
 	protected boolean genEnterAndLeaveMethodEvent;
 	protected boolean genEnterAndLeaveLoopEvent;
 	protected EventFormat newAndNewArrayEvent;
 	protected EventFormat getstaticPrimitiveEvent;
 	protected EventFormat getstaticReferenceEvent;
 	protected EventFormat putstaticPrimitiveEvent;
 	protected EventFormat putstaticReferenceEvent;
 	protected EventFormat getfieldPrimitiveEvent;
 	protected EventFormat getfieldReferenceEvent;
 	protected EventFormat putfieldPrimitiveEvent;
 	protected EventFormat putfieldReferenceEvent;
 	protected EventFormat aloadPrimitiveEvent;
 	protected EventFormat aloadReferenceEvent;
 	protected EventFormat astorePrimitiveEvent;
 	protected EventFormat astoreReferenceEvent;
 	protected EventFormat threadStartEvent;
 	protected EventFormat threadJoinEvent;
 	protected EventFormat acquireLockEvent;
 	protected EventFormat releaseLockEvent;
 	protected EventFormat waitEvent;
 	protected EventFormat notifyEvent;
 	protected EventFormat methodCallEvent;
 	protected EventFormat returnPrimitiveEvent;
 	protected EventFormat returnReferenceEvent;
 	protected EventFormat explicitThrowEvent;
 	protected EventFormat implicitThrowEvent;
 
 	protected String mStr;
 	protected TIntObjectHashMap<String> bciToInstrMap =
 		new TIntObjectHashMap<String>();
 
 	public InstrScheme getInstrScheme() { return scheme; }
 
 	public DomF getDomF() { return domF; }
 	public DomM getDomM() { return domM; }
 	public DomH getDomH() { return domH; }
 	public DomE getDomE() { return domE; }
 	public DomI getDomI() { return domI; }
 	public DomL getDomL() { return domL; }
 	public DomR getDomR() { return domR; }
 	public DomP getDomP() { return domP; }
 	public DomB getDomB() { return domB; }
 	public DomW getDomW() { return domW; }
 
 	public IndexMap<String> getFmap() { return Fmap; }
 	public IndexMap<String> getMmap() { return Mmap; }
 	public IndexMap<String> getHmap() { return Hmap; }
 	public IndexMap<String> getEmap() { return Emap; }
 	public IndexMap<String> getImap() { return Imap; }
 	public IndexMap<String> getLmap() { return Lmap; }
 	public IndexMap<String> getRmap() { return Rmap; }
 	public IndexMap<String> getPmap() { return Pmap; }
 	public IndexMap<String> getBmap() { return Bmap; }
 	public IndexMap<String> getWmap() { return Wmap; }
 
 	public Instrumentor(Program program, InstrScheme scheme) {
 		this.program = program;
 		this.scheme = scheme;
 	}
 	public void run() {
		String fullClassPathName = Properties.mainClassPathName +
			File.pathSeparator + Properties.classPathName;
 		pool = new ClassPool();
		try {
			pool.appendPathList(System.getProperty("sun.boot.class.path"));
		} catch (NotFoundException ex) {
			throw new ChordRuntimeException(ex);
		}
 		String[] pathElems = fullClassPathName.split(File.pathSeparator);
 		for (String pathElem : pathElems) {
 			File file = new File(pathElem);
 			if (!file.exists()) {
 				System.out.println("WARNING: Ignoring: " + pathElem);
 				continue;
 			}
 			try {
 				pool.appendClassPath(pathElem);
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
		// pool.appendSystemPath();
		System.out.println("POOL: " + pool);
 		convert = scheme.isConverted();
 		genBasicBlockEvent = scheme.hasBasicBlockEvent();
 		genQuadEvent = scheme.hasQuadEvent();
 		genEnterAndLeaveMethodEvent = scheme.getCallsBound() > 0 ||
 			scheme.hasEnterAndLeaveMethodEvent();
 		genEnterAndLeaveLoopEvent = scheme.getItersBound() > 0 ||
 			scheme.hasEnterAndLeaveLoopEvent();
 		newAndNewArrayEvent = scheme.getEvent(InstrScheme.NEW_AND_NEWARRAY);
 		getstaticPrimitiveEvent = scheme.getEvent(InstrScheme.GETSTATIC_PRIMITIVE);
 		getstaticReferenceEvent = scheme.getEvent(InstrScheme.GETSTATIC_REFERENCE);
 		putstaticPrimitiveEvent = scheme.getEvent(InstrScheme.PUTSTATIC_PRIMITIVE);
 		putstaticReferenceEvent = scheme.getEvent(InstrScheme.PUTSTATIC_REFERENCE);
 		getfieldPrimitiveEvent = scheme.getEvent(InstrScheme.GETFIELD_PRIMITIVE);
 		getfieldReferenceEvent = scheme.getEvent(InstrScheme.GETFIELD_REFERENCE);
 		putfieldPrimitiveEvent = scheme.getEvent(InstrScheme.PUTFIELD_PRIMITIVE);
 		putfieldReferenceEvent = scheme.getEvent(InstrScheme.PUTFIELD_REFERENCE);
 		aloadPrimitiveEvent = scheme.getEvent(InstrScheme.ALOAD_PRIMITIVE);
 		aloadReferenceEvent = scheme.getEvent(InstrScheme.ALOAD_REFERENCE);
 		astorePrimitiveEvent = scheme.getEvent(InstrScheme.ASTORE_PRIMITIVE);
 		astoreReferenceEvent = scheme.getEvent(InstrScheme.ASTORE_REFERENCE);
 		threadStartEvent = scheme.getEvent(InstrScheme.THREAD_START);
 		threadJoinEvent = scheme.getEvent(InstrScheme.THREAD_JOIN);
 		acquireLockEvent = scheme.getEvent(InstrScheme.ACQUIRE_LOCK);
 		releaseLockEvent = scheme.getEvent(InstrScheme.RELEASE_LOCK);
 		waitEvent = scheme.getEvent(InstrScheme.WAIT);
 		notifyEvent = scheme.getEvent(InstrScheme.NOTIFY);
 		methodCallEvent = scheme.getEvent(InstrScheme.METHOD_CALL);
 		
 		if (scheme.needsFmap()) {
 			if (convert) {
 				domF = (DomF) Project.getTrgt("F");
 				Project.runTask(domF);
 				Fmap = getUniqueStringMap(domF);
 			} else
 				Fmap = new IndexHashMap<String>();
 		}
 		if (scheme.needsMmap()) {
 			if (convert) {
 				domM = (DomM) Project.getTrgt("M");
 				Project.runTask(domM);
 				Mmap = getUniqueStringMap(domM);
 			} else
 				Mmap = new IndexHashMap<String>();
 		}
 		if (scheme.needsHmap()) {
 			if (convert) {
 				domH = (DomH) Project.getTrgt("H");
 				Project.runTask(domH);
 				Hmap = getUniqueStringMap(domH);
 			} else
 				Hmap = new IndexHashMap<String>();
 		}
 		if (scheme.needsEmap()) {
 			if (convert) {
 				domE = (DomE) Project.getTrgt("E");
 				Project.runTask(domE);
 				Emap = getUniqueStringMap(domE);
 			} else
 				Emap = new IndexHashMap<String>();
 		}
 		if (scheme.needsImap()) {
 			if (convert) {
 				domI = (DomI) Project.getTrgt("I");
 				Project.runTask(domI);
 				Imap = getUniqueStringMap(domI);
 			} else
 				Imap = new IndexHashMap<String>();
 		}
 		if (scheme.needsLmap()) {
 			if (convert) {
 				domL = (DomL) Project.getTrgt("L");
 				Project.runTask(domL);
 				Lmap = getUniqueStringMap(domL);
 			} else
 				Lmap = new IndexHashMap<String>();
 		}
 		if (scheme.needsRmap()) {
 			if (convert) {
 				domR = (DomR) Project.getTrgt("R");
 				Project.runTask(domR);
 				Rmap = getUniqueStringMap(domR);
 			} else
 				Rmap = new IndexHashMap<String>();
 		}
 		if (scheme.needsPmap()) {
 			assert (convert);
 			domP = (DomP) Project.getTrgt("P");
 			Project.runTask(domP);
 		}
 		if (scheme.needsBmap()) {
 			assert (convert);
 			domB = (DomB) Project.getTrgt("B");
 			Project.runTask(domB);
 		}
 		if (scheme.needsWmap()) {
 			domW = (DomW) Project.getTrgt("W");
 			domW.init();
 		}
 		if (genEnterAndLeaveMethodEvent || releaseLockEvent.present()) {
 			try {
 				exType = pool.get("java.lang.Throwable");
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			assert (exType != null);
 		}
 		
 		String bootClassesDirName = Properties.bootClassesDirName;
 		String classesDirName = Properties.classesDirName;
 		IndexSet<jq_Class> classes = program.getPreparedClasses();
		String instrExcludedPckgs = Properties.instrExcludedPckgs;
		String[] excluded = instrExcludedPckgs.equals("") ? new String[0] :
			instrExcludedPckgs.split(Properties.LIST_SEPARATOR);
 
 		for (jq_Class c : classes) {
 			String cName = c.getName();
 			if (cName.equals("java.lang.J9VMInternals") ||
 				cName.startsWith("java.lang.ref.") ||
 				cName.equals("sun.util.resources.TimeZoneNames")) {
 				System.out.println("WARNING: Not instrumenting class: " + cName);
 				continue;
 			}
 			boolean match = false;
 			for (String s : excluded) {
 				if (cName.startsWith(s)) {
 					match = true;
 		 			break;
 				}
 			}
 			if (match) {
 				System.out.println("WARNING: Not instrumenting class: " + cName);
 				continue;
 			}
 			CtClass clazz;
 			try {
 				clazz = pool.get(cName);
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			List<jq_Method> methods = program.getReachableMethods(c);
 			CtBehavior[] inits = clazz.getDeclaredConstructors();
 			CtBehavior[] meths = clazz.getDeclaredMethods();
			System.out.println("CLASS: " + cName);
 			for (jq_Method m : methods) {
				System.out.println("XXX: " + m + " sign: " + m.getDesc().toString());
 				CtBehavior method = null;
 				String mName = m.getName().toString();
 				if (mName.equals("<clinit>")) {
 					method = clazz.getClassInitializer();
 				} else if (mName.equals("<init>")) {
 					String mDesc = m.getDesc().toString();
 					for (CtBehavior x : inits) {
						System.out.println("\t" + x.getSignature());
 						if (x.getSignature().equals(mDesc)) {
 							method = x;
 							break;
 						}
 					}
 				} else {
 					String mDesc = m.getDesc().toString();
 					for (CtBehavior x : meths) {
 						if (x.getName().equals(mName) &&
 							x.getSignature().equals(mDesc)) {
 							method = x;
 							break;
 						}
 					}
 				}
 				assert (method != null);
 				try {
 					process(method, m);
 				} catch (ChordRuntimeException ex) {
 					System.err.println("WARNING: Ignoring instrumenting method: " +
 						method.getLongName());
 					ex.printStackTrace();
 				}
 			}
 			System.out.println("Writing class: " + cName);
 			try {
 				String dirName = c.isSystemClass() ? bootClassesDirName : classesDirName;
 				clazz.writeFile(dirName);
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			} catch (IOException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 
 		String outDirName = Properties.outDirName;
 		if (Fmap != null) {
 			FileUtils.writeMapToFile(Fmap,
 				(new File(outDirName, "F.dynamic.txt")).getAbsolutePath());
 		}
 		if (Mmap != null) {
 			FileUtils.writeMapToFile(Mmap,
 				(new File(outDirName, "M.dynamic.txt")).getAbsolutePath());
 		}
 		if (Hmap != null) {
 			FileUtils.writeMapToFile(Hmap,
 				(new File(outDirName, "H.dynamic.txt")).getAbsolutePath());
 		}
 		if (Emap != null) {
 			FileUtils.writeMapToFile(Emap,
 				(new File(outDirName, "E.dynamic.txt")).getAbsolutePath());
 		}
 		if (Imap != null) {
 			FileUtils.writeMapToFile(Imap,
 				(new File(outDirName, "I.dynamic.txt")).getAbsolutePath());
 		}
 		if (Lmap != null) {
 			FileUtils.writeMapToFile(Lmap,
 				(new File(outDirName, "L.dynamic.txt")).getAbsolutePath());
 		}
 		if (Rmap != null) {
 			FileUtils.writeMapToFile(Rmap,
 				(new File(outDirName, "R.dynamic.txt")).getAbsolutePath());
 		}
 		if (domB != null) {
 			Bmap = getUniqueStringMap(domB);
 			FileUtils.writeMapToFile(Bmap,
 				(new File(outDirName, "B.dynamic.txt")).getAbsolutePath());
 		}
 		if (domW != null) {
 			Wmap = getUniqueStringMap(domW);
 			FileUtils.writeMapToFile(Wmap,
 				(new File(outDirName, "W.dynamic.txt")).getAbsolutePath());
 		}
 		if (domP != null) {
 			Pmap = getUniqueStringMap(domP);
 			FileUtils.writeMapToFile(Pmap,
 				(new File(outDirName, "P.dynamic.txt")).getAbsolutePath());
 		}
 	}
 
 	protected IndexMap<String> getUniqueStringMap(ProgramDom dom) {
 		IndexMap<String> map = new IndexHashMap<String>(dom.size());
 		for (int i = 0; i < dom.size(); i++) {
 			String s = dom.toUniqueString(dom.get(i));
 			if (map.contains(s))
 				throw new RuntimeException("Map for domain " + dom +
 					" already contains: " + s);
 			map.getOrAdd(s);
 		}
 		return map;
 	}
 	protected int getBCI(BasicBlock b, jq_Method m) {
 		int n = b.size();
 		for (int i = 0; i < n; i++) {
 			Quad q = b.getQuad(i);
 	        int bci = m.getBCI(q);
 	        if (bci != -1)
 	            return bci;
 		}
 		throw new ChordRuntimeException();
 	}
 	// order must be tail -> head -> rest
 	protected void attachInstrToBCIAft(String str, int bci) {
 		String s = bciToInstrMap.get(bci);
 		bciToInstrMap.put(bci, (s == null) ? str : s + str);
 	}
 	protected void attachInstrToBCIBef(String str, int bci) {
 		String s = bciToInstrMap.get(bci);
 		bciToInstrMap.put(bci, (s == null) ? str : str + s);
 	}
 	protected void process(CtBehavior javassistMethod, jq_Method joeqMethod) {
 		int mods = javassistMethod.getModifiers();
 		if (Modifier.isNative(mods) || Modifier.isAbstract(mods))
 			return;
 		int mId = -1;
 		String mName;
 		if (javassistMethod instanceof CtConstructor) {
 			mName = ((CtConstructor) javassistMethod).isClassInitializer() ?
 				"<clinit>" : "<init>";
 		} else
 			mName = javassistMethod.getName();
 		String mDesc = javassistMethod.getSignature();
 		String cName = javassistMethod.getDeclaringClass().getName();
 		mStr = Program.toString(mName, mDesc, cName);
 		Map<Quad, Integer> bcMap;
 		try {
 			bcMap = joeqMethod.getBCMap();
 		} catch (RuntimeException ex) {
 			System.out.println("WARNING: Skipping instrumenting body of method: " + mStr +
 				"; reason follows:");
 			ex.printStackTrace();
 			return;
 		}
 		if (Mmap != null) {
 			if (convert) {
 				mId = Mmap.indexOf(mStr);
 				if (mId == -1) {
 					System.out.println("WARNING: Skipping method: " + mStr +
 						"; not found by static analysis.");
 					return;
 				}
 			} else {
 				int n = Mmap.size();
 				mId = Mmap.getOrAdd(mStr);
 				assert (mId == n);
 			}
 		}
 		if (bcMap == null) {
 			System.out.println("WARNING: Skipping instrumenting body of method: " + mStr +
 				"; bytecode does not exist.");
 		} else {
 			if (genEnterAndLeaveLoopEvent || genQuadEvent || genBasicBlockEvent) {
 				ControlFlowGraph cfg = joeqMethod.getCFG();
 				bciToInstrMap.clear();
 				if (genQuadEvent || genBasicBlockEvent) {
 					for (ListIterator.BasicBlock it = cfg.reversePostOrderIterator();
 							it.hasNext();) {
 						BasicBlock bb = it.nextBasicBlock();
 						if (bb.isEntry() || bb.isExit())
 							continue;
 						if (genBasicBlockEvent) {
 							int bId = domB.indexOf(bb);
 							assert (bId != -1);
 							String instr = basicBlockEventCall + bId + ");";
 							int bci = getBCI(bb, joeqMethod);
 							attachInstrToBCIAft(instr, bci);
 						}
 						if (genQuadEvent) {
 							int n = bb.size();
 							for (int i = 0; i < n; i++) {
 								Quad q = bb.getQuad(i);
 								if (isRelevant(q)) {
 									int bci = joeqMethod.getBCI(q);
 									assert (bci != -1);
 									int pId = domP.indexOf(q);
 									assert (pId != -1);
 									String instr = quadEventCall + pId + ");";
 									attachInstrToBCIAft(instr, bci);
 								}
 							}
 						}
 					}
 				}
 				if (genEnterAndLeaveLoopEvent) {
 					finder.visit(cfg);
 					Set<BasicBlock> heads = finder.getLoopHeads();
 					for (BasicBlock head : heads) {
 						int n = domW.size();
 						int wId = domW.getOrAdd(head, joeqMethod);
 						assert (wId == n);
 						String headInstr = enterLoopEventCall + wId + "," + mId + ");";
 						int headBCI = getBCI(head, joeqMethod);
 						attachInstrToBCIBef(headInstr, headBCI);
 					}
 					for (BasicBlock head : heads) {
 						Set<BasicBlock> exits = finder.getLoopExits(head);
 						int wId = domW.indexOf(head);
 						assert (wId != -1);
 						for (BasicBlock exit : exits) {
 							String exitInstr = leaveLoopEventCall + wId + "," + mId + ");";
 							int exitBCI = getBCI(exit, joeqMethod);
 							attachInstrToBCIBef(exitInstr, exitBCI);
 						}
 					}
 				}
 			}
 			try {
 				javassistMethod.instrument(exprEditor);
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 		// NOTE: do not move insertBefore or insertAfter or addCatch
 		// calls to a method to before bytecode instrumentation, else
 		// bytecode instrumentation offsets could get messed up 
 		String enterStr = "";
 		String leaveStr = "";
 		if (Modifier.isSynchronized(mods) &&
 				(acquireLockEvent.present() || releaseLockEvent.present())) {
 			String syncExpr;
 			if (Modifier.isStatic(mods))
 				syncExpr = cName + ".class";
 			else
 				syncExpr = "$0";
 			if (acquireLockEvent.present()) {
 				int lId = set(Lmap, -1);
 				enterStr = acquireLockEventCall + lId + "," +
 					syncExpr + ");";
 			}
 			if (releaseLockEvent.present()) {
 				int rId = set(Rmap, -2);
 				leaveStr = releaseLockEventCall + rId + "," +
 					syncExpr + ");";
 			}
 		}
 		if (genEnterAndLeaveMethodEvent) {
 			enterStr = enterMethodEventCall + mId + ");" + enterStr;
 			leaveStr = leaveStr + leaveMethodEventCall + mId + ");";
 		}
 		if (!enterStr.equals("")) {
 			try {
 				javassistMethod.insertBefore("{" + enterStr + "}");
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 		if (!leaveStr.equals("")) {
 			try {
 				javassistMethod.insertAfter("{" + leaveStr + "}");
 				String eventCall = "{" + leaveStr + "throw($e);" + "}";
 				javassistMethod.addCatch(eventCall, exType);
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 	}
 
 	public static boolean isRelevant(Quad q) {
 		Operator op = q.getOperator();
 		return
 			op instanceof Operator.Getfield ||
 			op instanceof Operator.Invoke ||
 			op instanceof Operator.Putfield ||
 			op instanceof Operator.New ||
 			op instanceof Operator.ALoad ||
 			op instanceof Operator.AStore ||
 			op instanceof Operator.Getstatic ||
 			op instanceof Operator.Putstatic ||
 			op instanceof Operator.NewArray ||
 			op instanceof Operator.Return ||
 			op instanceof Operator.Monitor;
 	}
 
 	protected int set(IndexMap<String> map, Expr e) {
 		return set(map, e.indexOfOriginalBytecode());
 	}
 
 	protected int set(IndexMap<String> map, int bci) {
 		String s = bci + "!" + mStr;
 		int id;
 		if (convert) {
 			id = map.indexOf(s);
 			if (id == -1) {
 				// throw new ChordRuntimeException("Element " + s +
 				//	" not found in map.");
 				return Runtime.UNKNOWN_FIELD_VAL;
 			}
 		} else {
 			int n = map.size();
 			id = map.getOrAdd(bci + "!" + mStr);
 			assert (id == n);
 		}
 		return id;
 	}
 
 	class MyExprEditor extends ExprEditor {
 		public String insertBefore(int pos) {
 			String s = bciToInstrMap.get(pos);
 			// s may be null in which case this method won't
 			// add any instrumentation
 			if (s != null)
 				s = "{ " + s + " }";
 			// System.out.println("XXX: " + pos + ":" + s);
 			return s;
 		}
 		public void edit(NewExpr e) {
 			if (newAndNewArrayEvent.present()) {
 				int hId = newAndNewArrayEvent.hasLoc() ? set(Hmap, e) :
 					Runtime.MISSING_FIELD_VAL;
 				String instr1, instr2;
 				if (newAndNewArrayEvent.hasObj()) {
 					instr1 = befNewEventCall + hId + ");";
 					instr2 = aftNewEventCall + hId + ",$_);";
 				} else {
 					instr1 = newEventCall + hId + ");";
 					instr2 = "";
 				}
 				try {
 					e.replace("{ " + instr1 + " $_ = $proceed($$); " +
 						instr2 + " }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(NewArray e) {
 			if (newAndNewArrayEvent.present()) {
 				int hId = newAndNewArrayEvent.hasLoc() ? set(Hmap, e) :
 					Runtime.MISSING_FIELD_VAL;
 				String instr = newArrayEventCall + hId + ",$_);";
 				try {
 					e.replace("{ $_ = $proceed($$); " + instr + " }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(FieldAccess e) {
 			boolean isStatic = e.isStatic();
 			CtField field;
 			CtClass type;
 			try {
 				field = e.getField();
 				type = field.getType();
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			boolean isPrim = type.isPrimitive();
 			boolean isWr = e.isWriter();
 			String instr;
 			if (isStatic) {
 				if (!scheme.hasStaticEvent())
 					return;
 				if (isWr) {
 					instr = isPrim ? putstaticPrimitive(e, field) :
 						putstaticReference(e, field);
 				} else {
 					instr = isPrim ? getstaticPrimitive(e, field) :
 						getstaticReference(e, field);
 				}
 			} else {
 				if (!scheme.hasFieldEvent())
 					return;
 				if (isWr) {
 					instr = isPrim ? putfieldPrimitive(e, field) :
 						putfieldReference(e, field);
 				} else {
 					instr = isPrim ? getfieldPrimitive(e, field) :
 						getfieldReference(e, field);
 				}
 			}
 			if (instr != null) {
 				try {
 					e.replace(instr);
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(ArrayAccess e) {
 			if (scheme.hasArrayEvent()) {
 				boolean isWr = e.isWriter();
 				boolean isPrim = e.getElemType().isPrimitive();
 				String instr;
 				if (isWr) {
 					instr = isPrim ? astorePrimitive(e) : astoreReference(e);
 					} else {
 					instr = isPrim ? aloadPrimitive(e) : aloadReference(e);
 				}
 				if (instr != null) {
 					try {
 						e.replace(instr);
 					} catch (CannotCompileException ex) {
 						throw new ChordRuntimeException(ex);
 					}
 				}
 			}
 		}
 		public void edit(MethodCall e) {
 			String instr = processMethodCall(e);
 			if (instr != null) {
 				try {
 					// NOTE: added $_ recently
 					e.replace("{ " + instr + " $_ = $proceed($$); }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(MonitorEnter e) {
 			if (acquireLockEvent.present()) {
 				int lId = acquireLockEvent.hasLoc() ? set(Lmap, e) :
 					Runtime.MISSING_FIELD_VAL;
 				String o = acquireLockEvent.hasObj() ? "$0" : "null";
 				String instr = acquireLockEventCall + lId + "," + o + ");";
 				try {
 					e.replace("{ $proceed(); " + instr + " }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(MonitorExit e) {
 			if (releaseLockEvent.present()) {
 				int rId = releaseLockEvent.hasLoc() ? set(Rmap, e) :
 					Runtime.MISSING_FIELD_VAL;
 				String o = releaseLockEvent.hasObj() ? "$0" : "null";
 				String instr = releaseLockEventCall + rId + "," + o + ");";
 				try {
 					e.replace("{ " + instr + " $proceed(); }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 	}
 	protected int getFid(CtField field) {
 		String fName = field.getName();
 		String fDesc = field.getSignature();
 		String cName = field.getDeclaringClass().getName();
 		String s = Program.toString(fName, fDesc, cName);
 		return Fmap.getOrAdd(s);
 	}
 	protected String getstaticPrimitive(FieldAccess e, CtField f) {
 		if (getstaticPrimitiveEvent.present()) {
 			int eId = getstaticPrimitiveEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			int fId = getstaticPrimitiveEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			return "{ $_ = $proceed($$); " + getstaticPriEventCall + eId +
 				"," + fId + "); }"; 
 		}
 		return null;
 	}
 	protected String getstaticReference(FieldAccess e, CtField f) {
 		if (getstaticReferenceEvent.present()) {
 			int eId = getstaticReferenceEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			int fId = getstaticReferenceEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			String oId = getstaticReferenceEvent.hasObj() ? "$_" : "null";
 			return "{ $_ = $proceed($$); " + getstaticRefEcentCall + eId +
 				"," + fId + "," + oId + "); }"; 
 		}
 		return null;
 	}
 	protected String putstaticPrimitive(FieldAccess e, CtField f) {
 		if (putstaticPrimitiveEvent.present()) {
 			int eId = putstaticPrimitiveEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			int fId = putstaticPrimitiveEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			return "{ $proceed($$); " + putstaticPriEventCall + eId +
 				"," + fId + "); }"; 
 		}
 		return null;
 	}
 	protected String putstaticReference(FieldAccess e, CtField f) {
 		if (putstaticReferenceEvent.present()) {
 			int eId = putstaticReferenceEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			int fId = putstaticReferenceEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			String oId = putstaticReferenceEvent.hasObj() ? "$1" : "null";
 			return "{ $proceed($$); " + putstaticRefEventCall + eId +
 				"," + fId + "," + oId + "); }";
 		}
 		return null;
 	}
 	protected String getfieldPrimitive(FieldAccess e, CtField f) {
 		if (getfieldPrimitiveEvent.present()) {
 			int eId = getfieldPrimitiveEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = getfieldPrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			int fId = getfieldPrimitiveEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			return "{ $_ = $proceed($$); " + getfieldPriEventCall +
 				eId + "," + bId + "," + fId + "); }"; 
 		}
 		return null;
 	}
 	protected String getfieldReference(FieldAccess e, CtField f) {
 		if (getfieldReferenceEvent.present()) {
 			int eId = getfieldReferenceEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = getfieldReferenceEvent.hasBaseObj() ? "$0" : "null";
 			int fId = getfieldReferenceEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			String oId = getfieldReferenceEvent.hasObj() ? "$_" : "null";
 			return "{ $_ = $proceed($$); " + getfieldReference +
 				eId + "," + bId + "," + fId + "," + oId + "); }"; 
 		}
 		return null;
 	}
 	protected String putfieldPrimitive(FieldAccess e, CtField f) {
 		if (putfieldPrimitiveEvent.present()) {
 			int eId = putfieldPrimitiveEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = putfieldPrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			int fId = putfieldPrimitiveEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			return "{ $proceed($$); " + putfieldPriEventCall + eId +
 				"," + bId + "," + fId + "); }"; 
 		}
 		return null;
 	}
 	protected String putfieldReference(FieldAccess e, CtField f) {
 		if (putfieldReferenceEvent.present()) {
 			int eId = putfieldReferenceEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = putfieldReferenceEvent.hasBaseObj() ? "$0" : "null";
 			int fId = putfieldReferenceEvent.hasFld() ? getFid(f) :
 				Runtime.MISSING_FIELD_VAL;
 			String oId = putfieldReferenceEvent.hasObj() ? "$1" : "null";
 			return "{ $proceed($$); " + putfieldRefEventCall +
 				eId + "," + bId + "," + fId + "," + oId + "); }"; 
 		}
 		return null;
 	}
 	protected String aloadPrimitive(ArrayAccess e) {
 		if (aloadPrimitiveEvent.present()) {
 			int eId = aloadPrimitiveEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = aloadPrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			String iId = aloadPrimitiveEvent.hasIdx() ? "$1" : "-1";
 			return "{ $_ = $proceed($$); " + aloadPriEventCall +
 				eId + "," + bId + "," + iId + "); }"; 
 		}
 		return null;
 	}
 	protected String aloadReference(ArrayAccess e) {
 		if (aloadReferenceEvent.present()) {
 			int eId = aloadReferenceEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = aloadReferenceEvent.hasBaseObj() ? "$0" : "null";
 			String iId = aloadReferenceEvent.hasIdx() ? "$1" : "-1";
 			String oId = aloadReferenceEvent.hasObj() ? "$_" : "null";
 			return "{ $_ = $proceed($$); " + aloadRefEventCall +
 				eId + "," + bId + "," + iId + "," + oId + "); }"; 
 		}
 		return null;
 	}
 	protected String astorePrimitive(ArrayAccess e) {
 		if (astorePrimitiveEvent.present()) {
 			int eId = astorePrimitiveEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = astorePrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			String iId = astorePrimitiveEvent.hasIdx() ? "$1" : "-1";
 			return "{ $proceed($$); " + astorePriEventCall +
 				eId + "," + bId + "," + iId + "); }"; 
 		}
 		return null;
 	}
 	protected String astoreReference(ArrayAccess e) {
 		if (astoreReferenceEvent.present()) {
 			int eId = astoreReferenceEvent.hasLoc() ? set(Emap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			String bId = astoreReferenceEvent.hasBaseObj() ? "$0" : "null";
 			String iId = astoreReferenceEvent.hasIdx() ? "$1" : "-1";
 			String oId = astoreReferenceEvent.hasObj() ? "$2" : "null";
 			return "{ $proceed($$); " + astoreRefEventCall +
 				eId + "," + bId + "," + iId + "," + oId + "); }"; 
 		}
 		return null;
 	}
 	protected String processMethodCall(MethodCall e) {
 		// Part 1: add METHOD_CALL event if present
 		String instr1;
 		if (methodCallEvent.present()) {
 			int iId = methodCallEvent.hasLoc() ? set(Imap, e) :
 				Runtime.MISSING_FIELD_VAL;
 			instr1 = " " + methodCallEventCall + iId + ");";
 		} else
 			instr1 = null;
 		// Part 2: add THREAD_START, THREAD_JOIN, WAIT, or NOTIFY event
 		// if present and applicable
 		String instr2 = null;
 		CtMethod m;
 		try {
 			m = e.getMethod();
 		} catch (NotFoundException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 		String cName = m.getDeclaringClass().getName();
 		if (cName.equals("java.lang.Object")) {
 			String mName = m.getName();
 			String mDesc = m.getSignature();
 			if (mName.equals("wait") && (mDesc.equals("()V") ||
 					mDesc.equals("(L)V") || mDesc.equals("(LI)V"))) {
 				if (waitEvent.present()) {
 					int iId = waitEvent.hasLoc() ? set(Imap, e) :
 						Runtime.MISSING_FIELD_VAL;
 					String oId = waitEvent.hasObj() ? "$0" : "null";
 					instr2 = waitEventCall + iId + "," + oId + ");";
 				}
 			} else if ((mName.equals("notify") ||
 					mName.equals("notifyAll")) && mDesc.equals("()V")) {
 				if (notifyEvent.present()) {
 					int iId = notifyEvent.hasLoc() ? set(Imap, e) :
 						Runtime.MISSING_FIELD_VAL;
 					String oId = notifyEvent.hasObj() ? "$0" : "null";
 					instr2 = notifyEventCall + iId + "," + oId + ");";
 				}
 			}
 		} else if (cName.equals("java.lang.Thread")) {
 			String mName = m.getName();
 			String mDesc = m.getSignature();
 			if (mName.equals("start") && mDesc.equals("()V")) {
 				if (threadStartEvent.present()) {
 					int iId = threadStartEvent.hasLoc() ? set(Imap, e) :
 						Runtime.MISSING_FIELD_VAL;
 					String oId = threadStartEvent.hasObj() ? "$0" : "null";
 					instr2 = threadStartEventCall + iId + "," + oId + ");";
 				}
 			} else if (mName.equals("join") && (mDesc.equals("()V") ||
 					mDesc.equals("(L)V") || mDesc.equals("(LI)V"))) {
 				if (threadJoinEvent.present()) {
 					int iId = threadJoinEvent.hasLoc() ? set(Imap, e) :
 						Runtime.MISSING_FIELD_VAL;
 					String oId = threadJoinEvent.hasObj() ? "$0" : "null";
 					instr2 = threadJoinEventCall + iId + "," + oId + ");";
 				}
 			}
 		}
 		if (instr1 == null)
 			return instr2;
 		if (instr2 == null)
 			return instr1;
 		return instr1 + instr2;
 	}
 }
