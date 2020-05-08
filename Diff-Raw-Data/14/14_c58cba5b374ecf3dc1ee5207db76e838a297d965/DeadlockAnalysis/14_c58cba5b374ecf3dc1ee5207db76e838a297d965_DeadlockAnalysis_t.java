 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  * Licensed under the terms of the New BSD License.
  */
 package chord.analyses.deadlock;
 
 import java.io.PrintWriter;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.Inst;
 import joeq.Compiler.Quad.Quad;
 
 import chord.project.Config;
 import chord.program.Program;
 import chord.project.Project;
 import chord.project.Chord;
 import chord.project.OutDirUtils;
 import chord.project.analyses.JavaAnalysis;
 import chord.project.analyses.ProgramDom;
 import chord.project.analyses.ProgramRel;
 
 import chord.util.ArraySet;
 import chord.util.graph.IPathVisitor;
 import chord.util.graph.ShortestPathBuilder;
 import chord.analyses.alias.Ctxt;
 import chord.analyses.alias.CSObj;
 import chord.analyses.alias.CtxtsAnalysis;
 import chord.analyses.alias.ICSCG;
 import chord.analyses.alias.ThrSenAbbrCSCGAnalysis;
 import chord.analyses.alias.DomO;
 import chord.analyses.alias.DomC;
 import chord.bddbddb.Rel.RelView;
 import chord.analyses.thread.DomA;
 import chord.doms.DomL;
 import chord.doms.DomH;
 import chord.doms.DomI;
 import chord.doms.DomM;
 import chord.util.SetUtils;
 import chord.util.tuple.object.Pair;
 
 /**
  * Static deadlock analysis.
  * <p>
  * Outputs relation <tt>deadlock</tt> containing each tuple
  * <tt>(a1,l1,l2,a2,l3,l4)</tt> denoting a possible deadlock between
  * abstract thread <tt>a1</tt>, which acquires abstract lock
  * <tt>l1</tt> followed by abstract lock <tt>l2</tt>, and
  * abstract thread <tt>a2</tt>, which acquires abstract lock
  * <tt>l3</tt> followed by abstract lock <tt>l4</tt>.
  * <p>
  * Recognized system properties:
  * <ul>
  * <li><tt>chord.exclude.escaping</tt> (default is false).</li>
  * <li><tt>chord.exclude.parallel</tt> (default is false).</li>
  * <li><tt>chord.exclude.nonreent</tt> (default is false).</li>
  * <li><tt>chord.exclude.nongrded</tt> (default is false).</li>
  * <li><tt>chord.publish.results</tt> (default is false).</li>
  * <li>All system properties recognized by abstract contexts analysis
  * (see {@link chord.analyses.alias.CtxtsAnalysis}).</li>
  * </ul>
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  * @author Zhifeng Lai (zflai@cse.ust.hk)
  */
 @Chord(
 	name="deadlock-java"
 )
 public class DeadlockAnalysis extends JavaAnalysis {
 	private DomA domA;
 	private DomC domC;	
 	private DomH domH;
 	private DomI domI;
     private DomL domL;
     private DomM domM;
     private ProgramDom<Pair<Ctxt, Inst>> domN;
 	
 	private ProgramRel relNC;
 	private ProgramRel relNL;
 	private ProgramRel relDeadlock;
 	private ProgramRel relSyncCLC;
 	
 	private ICSCG thrSenAbbrCSCG;
 	private ThrSenAbbrCSCGAnalysis thrSenAbbrCSCGAnalysis;
 	private final Map<CM, Set<CM>> CMCMMap = new HashMap<CM, Set<CM>>();
 
 	private void init() {
 		domA = (DomA) Project.getTrgt("A");
 		domC = (DomC) Project.getTrgt("C");
 		domH = (DomH) Project.getTrgt("H");
 		domI = (DomI) Project.getTrgt("I");
 		domL = (DomL) Project.getTrgt("L");
 		domM = (DomM) Project.getTrgt("M");
 		domN = (ProgramDom) Project.getTrgt("N");
 		
 		relNC = (ProgramRel) Project.getTrgt("NC");
 		relNL = (ProgramRel) Project.getTrgt("NL");
 		relDeadlock = (ProgramRel) Project.getTrgt("deadlock");
 		relSyncCLC = (ProgramRel) Project.getTrgt("syncCLC");
 		
 		thrSenAbbrCSCGAnalysis = (ThrSenAbbrCSCGAnalysis)
 			Project.getTrgt("thrsen-abbr-cscg-java");
 	}
 	
 	private void finish() {
 		if (relNC.isOpen()) {
 			relNC.isOpen();
 		}
 		if (relNL.isOpen()) {
 			relNL.close();
 		}
 		if (relDeadlock.isOpen()) {
 			relDeadlock.close();
 		}
 		if (relSyncCLC.isOpen()) {
 			relSyncCLC.close();
 		}
 	}
 	
 	public void run() {
 		boolean excludeParallel = Boolean.getBoolean("chord.exclude.parallel");
 		boolean excludeEscaping = Boolean.getBoolean("chord.exclude.escaping");
 		boolean excludeNonreent = Boolean.getBoolean("chord.exclude.nonreent");
 		boolean excludeNongrded = Boolean.getBoolean("chord.exclude.nongrded");
 
 		init();
 		
 		Project.runTask(domL);
 
		Project.runTask("ctxts-java");
 		Project.runTask(CtxtsAnalysis.getCspaKind());
 		Project.runTask(thrSenAbbrCSCGAnalysis);
 		thrSenAbbrCSCG = thrSenAbbrCSCGAnalysis.getCallGraph();
 		domN.clear();
 		Program program = Program.getProgram();
 		for (Inst i : domL) {
 			jq_Method m = i.getMethod();
 			Set<Ctxt> cs = thrSenAbbrCSCG.getContexts(m);
 			for (Ctxt c : cs) {
 				domN.getOrAdd(new Pair<Ctxt, Inst>(c, i));
 			}
 		}
 		domN.save();
 
 		relNC.zero();
 		relNL.zero();
 		for (Pair<Ctxt, Inst> cm : domN) {
 			int n = domN.indexOf(cm);
 			int c = domC.indexOf(cm.val0);
 			int l = domL.indexOf(cm.val1);
 			relNC.add(n, c);
 			relNL.add(n, l);
 		}
 		relNC.save();
 		relNL.save();
 
 		if (excludeParallel)
 			Project.runTask("deadlock-parallel-exclude-dlog");
 		else
 			Project.runTask("deadlock-parallel-include-dlog");
 		if (excludeEscaping)
 			Project.runTask("deadlock-escaping-exclude-dlog");
 		else
 			Project.runTask("deadlock-escaping-include-dlog");
 		if (excludeNonreent)
 			Project.runTask("deadlock-nonreent-exclude-dlog");
 		else
 			Project.runTask("deadlock-nonreent-include-dlog");
 		if (excludeNongrded)
 			Project.runTask("deadlock-nongrded-exclude-dlog");
 		else
 			Project.runTask("deadlock-nongrded-include-dlog");
 		Project.runTask("deadlock-dlog");
 		Project.runTask("deadlock-stats-dlog");
 
 		if (Config.publishResults) {
 			publishResults();
 		}
 		
 		finish();
 	}
 
 	private CSObj getPointsTo(int cIdx, int lIdx) {
 		RelView view = relSyncCLC.getView();
 		view.selectAndDelete(0, cIdx);
 		view.selectAndDelete(1, lIdx);
 		Iterable<Ctxt> ctxts = view.getAry1ValTuples();
 		Set<Ctxt> pts = SetUtils.newSet(view.size());
 		for (Ctxt ctxt : ctxts)
 			pts.add(ctxt);
 		view.free();
 		return new CSObj(pts);
 	}
 	
 	private void publishResults() {
         final DomO domO = new DomO();
         domO.setName("O");
         
         PrintWriter out;
 
 		relDeadlock.load();
 		relSyncCLC.load();
 
 		Program program = Program.getProgram();
 
 		out = OutDirUtils.newPrintWriter("deadlocklist.xml");
 		out.println("<deadlocklist>");
 		for (Object[] tuple : relDeadlock.getAryNValTuples()) {
 			Pair<Ctxt, jq_Method> t1Val = (Pair) tuple[0];
 			Pair<Ctxt, Inst> n1Val = (Pair) tuple[1];
 			Ctxt c1Val = n1Val.val0;
 			Inst l1Val = n1Val.val1;
 			Pair<Ctxt, Inst> n2Val = (Pair) tuple[2];
 			Ctxt c2Val = n2Val.val0;
 			Inst l2Val = n2Val.val1;
 			Pair<Ctxt, jq_Method> t2Val = (Pair) tuple[3];
 			Pair<Ctxt, Inst> n3Val = (Pair) tuple[4];
 			Ctxt c3Val = n3Val.val0;
 			Inst l3Val = n3Val.val1;
 			Pair<Ctxt, Inst> n4Val = (Pair) tuple[5];
 			Ctxt c4Val = n4Val.val0;
 			Inst l4Val = n4Val.val1;
 			int l1 = domL.indexOf(l1Val);
 			int l2 = domL.indexOf(l2Val);
 			int l3 = domL.indexOf(l3Val);
 			int l4 = domL.indexOf(l4Val);
 			// require l1,l2 <= l3,l4 and if not switch
 			if (l1 > l3 || (l1 == l3 && l2 > l4)) {
 				{
 					int tmp;
 					tmp = l1; l1 = l3; l3 = tmp;
 					tmp = l2; l2 = l4; l4 = tmp;
 				}
 				{
 					Inst tmp;
 					tmp = l1Val; l1Val = l3Val; l3Val = tmp;
 					tmp = l2Val; l2Val = l4Val; l4Val = tmp;
 				}
 				{
 					Ctxt tmp;
 					tmp = c1Val; c1Val = c3Val; c3Val = tmp; 
 					tmp = c2Val; c2Val = c4Val; c4Val = tmp;
 				}
 				{
 					Pair<Ctxt, jq_Method> tmp;
 					tmp = t1Val; t1Val = t2Val; t2Val = tmp;
 				}
 			}
 			int c1 = domC.indexOf(c1Val);
 			int c2 = domC.indexOf(c2Val);
 			int c3 = domC.indexOf(c3Val);
 			int c4 = domC.indexOf(c4Val);
 			Ctxt t1cVal = t1Val.val0;
 			Ctxt t2cVal = t2Val.val0;
 			int t1c = domC.indexOf(t1cVal);
 			int t2c = domC.indexOf(t2cVal);
 			jq_Method t1mVal = t1Val.val1;
 			jq_Method t2mVal = t2Val.val1;
 			int t1m = domM.indexOf(t1mVal);
 			int t2m = domM.indexOf(t2mVal);
 			jq_Method m1Val = l1Val.getMethod();
 			jq_Method m2Val = l2Val.getMethod();
 			jq_Method m3Val = l3Val.getMethod();
 			jq_Method m4Val = l4Val.getMethod();
 			int m1 = domM.indexOf(m1Val);
 			int m2 = domM.indexOf(m2Val);
 			int m3 = domM.indexOf(m3Val);
 			int m4 = domM.indexOf(m4Val);
 			CSObj o1Val = getPointsTo(c1, l1);
 			CSObj o2Val = getPointsTo(c2, l2);
 			CSObj o3Val = getPointsTo(c3, l3);
 			CSObj o4Val = getPointsTo(c4, l4);
 			int o1 = domO.getOrAdd(o1Val);
 			int o2 = domO.getOrAdd(o2Val);
 			int o3 = domO.getOrAdd(o3Val);
 			int o4 = domO.getOrAdd(o4Val);
 			addToCMCMMap(t1cVal, t1mVal, c1Val, m1Val);
 			addToCMCMMap(t2cVal, t2mVal, c3Val, m3Val);
 			addToCMCMMap(c1Val , m1Val , c2Val, m2Val);
 			addToCMCMMap(c3Val , m3Val , c4Val, m4Val);
 			out.println("<deadlock " +
 				"group=\"" + l1 + "_" + l2 + "_" + l3 + "_" + l4 + "\" " +
 				"T1Cid=\"C" + t1c + "\" T1Mid=\"M" + t1m + "\" " +
 				"T2Cid=\"C" + t2c + "\" T2Mid=\"M" + t2m + "\" " +
 				"C1id=\"C"  + c1 + "\" M1id=\"M" + m1 + "\" L1id=\"L" + l1 + "\" O1id=\"O" + o1 + "\" " +
 				"C2id=\"C"  + c2 + "\" M2id=\"M" + m2 + "\" L2id=\"L" + l2 + "\" O2id=\"O" + o2 + "\" " +
 				"C3id=\"C"  + c3 + "\" M3id=\"M" + m3 + "\" L3id=\"L" + l3 + "\" O3id=\"O" + o3 + "\" " +
 				"C4id=\"C"  + c4 + "\" M4id=\"M" + m4 + "\" L4id=\"L" + l4 + "\" O4id=\"O" + o4 + "\"/>");
 		}
 		out.println("</deadlocklist>");
 		out.close();		
 		
         IPathVisitor<Pair<Ctxt, jq_Method>> visitor =
 			new IPathVisitor<Pair<Ctxt, jq_Method>>() {
 				public String visit(Pair<Ctxt, jq_Method> origNode,
 						Pair<Ctxt, jq_Method> destNode) {
 					Ctxt ctxt = origNode.val0;
 					Set<Quad> insts = thrSenAbbrCSCG.getLabels(origNode, destNode);
 					for (Quad inst : insts) {
 						return "<elem Cid=\"C" + domC.indexOf(ctxt) + "\" " +
 							"Iid=\"I" + domI.indexOf(inst) + "\"/>";
 					}
 					return "";
 				}
 			};
 
         out = OutDirUtils.newPrintWriter("CMCMlist.xml");
         out.println("<CMCMlist>");
         
         for (CM cm1 : CMCMMap.keySet()) {
         	Ctxt ctxt1 = cm1.val0;
         	jq_Method meth1 = cm1.val1;
             int c1 = domC.indexOf(ctxt1);
             int m1 = domM.indexOf(meth1);
 			Set<CM> cmSet = CMCMMap.get(cm1);
 			ShortestPathBuilder<Pair<Ctxt, jq_Method>> builder =
 				new ShortestPathBuilder(thrSenAbbrCSCG, cm1, visitor);
 			for (CM cm2 : cmSet) {
 				Ctxt ctxt2 = cm2.val0;
 				jq_Method meth2 = cm2.val1;
 				int c2 = domC.indexOf(ctxt2);
 				int m2 = domM.indexOf(meth2);
 				out.println("<CMCM C1id=\"C" + c1 + "\" M1id=\"M" + m1 +
 					"\" C2id=\"C" + c2 + "\" M2id=\"M" + m2 + "\">");
            		String path = builder.getShortestPathTo(cm2);
 				out.println("<path>");
 				out.println(path);
 				out.println("</path>");
 				out.println("</CMCM>");
 			}
         }
         out.println("</CMCMlist>");
         out.close();
 		
 		domO.saveToXMLFile();
         domC.saveToXMLFile();
         domA.saveToXMLFile();
         domH.saveToXMLFile();
         domI.saveToXMLFile();
         domM.saveToXMLFile();
         domL.saveToXMLFile();
 
         OutDirUtils.copyFileFromMainDir("src/web/Olist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/Clist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/Alist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/Hlist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/Ilist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/Mlist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/Llist.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/style.css");
         OutDirUtils.copyFileFromMainDir("src/web/misc.xsl");
         OutDirUtils.copyFileFromMainDir("src/web/deadlock/results.dtd");
         OutDirUtils.copyFileFromMainDir("src/web/deadlock/results.xml");
         OutDirUtils.copyFileFromMainDir("src/web/deadlock/group.xsl");
         OutDirUtils.copyFileFromMainDir("src/web/deadlock/paths.xsl");
 
         OutDirUtils.runSaxon("results.xml", "group.xsl");
         OutDirUtils.runSaxon("results.xml", "paths.xsl");
 
         program.HTMLizeJavaSrcFiles();
 	}
 
 	private class CM extends Pair<Ctxt, jq_Method> {
 		public CM(Ctxt c, jq_Method m) {
 			super(c, m);
 		}
 	};
 
 	private void addToCMCMMap(Ctxt c1, jq_Method m1,
 			Ctxt c2, jq_Method m2) {
 		CM cm1 = new CM(c1, m1);
 		Set<CM> s = CMCMMap.get(cm1);
 		if (s == null) {
 			s = new ArraySet<CM>();
 			CMCMMap.put(cm1, s);
 		}
 		CM cm2 = new CM(c2, m2);
 		s.add(cm2);
 	}
 }
