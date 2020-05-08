 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.analyses.datarace;
 
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import joeq.Class.jq_Field;
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.Inst;
 import joeq.Compiler.Quad.Quad;
 
 import chord.util.ArraySet;
 import chord.util.graph.IPathVisitor;
 import chord.util.graph.ShortestPathBuilder;
 import chord.analyses.alias.ICSCG;
 import chord.analyses.alias.Ctxt;
 import chord.analyses.alias.Obj;
 import chord.analyses.alias.CtxtsAnalysis;
 import chord.analyses.alias.CSAliasAnalysis;
 import chord.analyses.alias.ThrSenAbbrCSCGAnalysis;
 import chord.analyses.alias.DomO;
 import chord.analyses.alias.DomC;
 import chord.bddbddb.Rel.RelView;
 import chord.program.Program;
 import chord.analyses.thread.DomA;
 import chord.doms.DomL;
 import chord.doms.DomE;
 import chord.doms.DomF;
 import chord.doms.DomH;
 import chord.doms.DomI;
 import chord.doms.DomM;
 import chord.project.Chord;
 import chord.project.Project;
 import chord.project.Properties;
 import chord.project.OutDirUtils;
 import chord.project.analyses.JavaAnalysis;
 import chord.project.analyses.ProgramDom;
 import chord.project.analyses.ProgramRel;
 import chord.util.SetUtils;
 import chord.util.tuple.object.Hext;
 import chord.util.tuple.object.Pair;
 import chord.util.tuple.object.Trio;
 
 /**
  * Static datarace analysis.
  * <p>
  * Outputs relation <tt>datarace</tt> containing each tuple
  * <tt>(a1,c1,e1,a2,c2,e2)</tt> denoting a possible race between
  * abstract threads <tt>a1</tt> and <tt>a2</tt> executing
  * accesses <tt>e1</tt> and <tt>e2</tt>, respectively, in
  * abstract contexts <tt>c1</tt> and <tt>c2</tt> of their
  * containing methods, respectively.
  * <p>
  * Recognized system properties:
  * <ul>
  * <li><tt>chord.exclude.escaping</tt> (default is false).</li>
  * <li><tt>chord.exclude.parallel</tt> (default is false).</li>
  * <li><tt>chord.exclude.nongrded</tt> (default is false).</li>
  * <li><tt>chord.publish.results</tt> (default is false).</li>
  * <li>All system properties recognized by abstract contexts analysis
  * (see {@link chord.analyses.alias.CtxtsAnalysis}).</li>
  * </ul>
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 @Chord(
 	name="datarace-java"
 )
 public class DataraceAnalysis extends JavaAnalysis {
 	private DomM domM;
 	private DomI domI;
 	private DomF domF;
 	private DomE domE;
 	private DomA domA;
 	private DomH domH;
 	private DomC domC;
 	private DomL domL;
 	private CSAliasAnalysis hybridAnalysis;
 	private ThrSenAbbrCSCGAnalysis thrSenAbbrCSCGAnalysis;
 
 	private void init() {
 		domM = (DomM) Project.getTrgt("M");
 		domI = (DomI) Project.getTrgt("I");
 		domF = (DomF) Project.getTrgt("F");
 		domE = (DomE) Project.getTrgt("E");
 		domA = (DomA) Project.getTrgt("A");
 		domH = (DomH) Project.getTrgt("H");
 		domC = (DomC) Project.getTrgt("C");
 		domL = (DomL) Project.getTrgt("L");
 		hybridAnalysis = (CSAliasAnalysis) Project.getTrgt("cs-alias-java");
 	    thrSenAbbrCSCGAnalysis = (ThrSenAbbrCSCGAnalysis)
 			Project.getTrgt("thrsen-abbr-cscg-java");
 	}
 
 	public void run() {
 		boolean excludeParallel = Boolean.getBoolean("chord.exclude.parallel");
 		boolean excludeEscaping = Boolean.getBoolean("chord.exclude.escaping");
 		boolean excludeNongrded = Boolean.getBoolean("chord.exclude.nongrded");
 
 		init();
 
 		Project.runTask(CtxtsAnalysis.getCspaKind());
 		Project.runTask("datarace-prologue-dlog");
 		if (excludeParallel)
 			Project.runTask("datarace-parallel-exclude-dlog");
 		else
 			Project.runTask("datarace-parallel-include-dlog");
 		if (excludeEscaping)
 			Project.runTask("datarace-escaping-exclude-dlog");
 		else
 			Project.runTask("datarace-escaping-include-dlog");
 		if (excludeNongrded)
 			Project.runTask("datarace-nongrded-exclude-dlog");
 		else
 			Project.runTask("datarace-nongrded-include-dlog");
 		Project.runTask("datarace-dlog");
 		Project.runTask("datarace-stats-dlog");
 
 		if (Properties.publishResults)
 			publishResults();
 	}
 
 	private void publishResults() {
 		Project.runTask(hybridAnalysis);
 		Project.runTask(thrSenAbbrCSCGAnalysis);
 	    final ICSCG thrSenAbbrCSCG = thrSenAbbrCSCGAnalysis.getCallGraph();
 		Project.runTask("datarace-epilogue-dlog");
 		final ProgramDom<Trio<Pair<Ctxt, jq_Method>, Ctxt, Quad>> domTCE =
 			new ProgramDom<Trio<Pair<Ctxt, jq_Method>, Ctxt, Quad>>();
 		domTCE.setName("TCE");
 		final DomO domO = new DomO();
 		domO.setName("O");
 
 		PrintWriter out;
 
 		out = OutDirUtils.newPrintWriter("dataracelist.xml");
 		out.println("<dataracelist>");
 		final ProgramRel relDatarace = (ProgramRel) Project.getTrgt("datarace");
 		relDatarace.load();
 		final ProgramRel relRaceCEC = (ProgramRel) Project.getTrgt("raceCEC");
 		relRaceCEC.load();
 		final Iterable<Hext<Pair<Ctxt, jq_Method>, Ctxt, Quad,
 				Pair<Ctxt, jq_Method>, Ctxt, Quad>> tuples = relDatarace.getAry6ValTuples();
 		for (Hext<Pair<Ctxt, jq_Method>, Ctxt, Quad,
 				  Pair<Ctxt, jq_Method>, Ctxt, Quad> tuple : tuples) {
 			int tce1 = domTCE.getOrAdd(new Trio<Pair<Ctxt, jq_Method>, Ctxt, Quad>(
 				tuple.val0, tuple.val1, tuple.val2));
 			int tce2 = domTCE.getOrAdd(new Trio<Pair<Ctxt, jq_Method>, Ctxt, Quad>(
 				tuple.val3, tuple.val4, tuple.val5));
 			RelView view = relRaceCEC.getView();
 			view.selectAndDelete(0, tuple.val1);
 			view.selectAndDelete(1, tuple.val2);
 			view.selectAndDelete(2, tuple.val4);
 			view.selectAndDelete(3, tuple.val5);
 			Set<Ctxt> pts = new ArraySet<Ctxt>(view.size());
 			Iterable<Ctxt> res = view.getAry1ValTuples();
 			for (Ctxt ctxt : res) {
 				pts.add(ctxt);
 			}
 			view.free();
 			int p = domO.getOrAdd(new Obj(pts));
 			jq_Field fld = Program.getField(tuple.val2);
 			int f = domF.indexOf(fld);
 			out.println("<datarace Oid=\"O" + p +
 				"\" Fid=\"F" + f + "\" " +
 				"TCE1id=\"TCE" + tce1 + "\" "  +
 				"TCE2id=\"TCE" + tce2 + "\"/>");
 		}
 		relDatarace.close();
 		relRaceCEC.close();
 		out.println("</dataracelist>");
 		out.close();
 
 		final ProgramRel relLI = (ProgramRel) Project.getTrgt("LI");
 		final ProgramRel relLE = (ProgramRel) Project.getTrgt("LE");
 		final ProgramRel relSyncCLC = (ProgramRel) Project.getTrgt("syncCLC");
 		relLI.load();
 		relLE.load();
 		relSyncCLC.load();
 
 		final Map<Pair<Ctxt, jq_Method>, ShortestPathBuilder> srcNodeToSPB =
 			new HashMap<Pair<Ctxt, jq_Method>, ShortestPathBuilder>();
 
 		final IPathVisitor<Pair<Ctxt, jq_Method>> visitor =
 			new IPathVisitor<Pair<Ctxt, jq_Method>>() {
 				public String visit(Pair<Ctxt, jq_Method> origNode,
 						Pair<Ctxt, jq_Method> destNode) {
 					Set<Quad> insts = thrSenAbbrCSCG.getLabels(origNode, destNode);
 					jq_Method srcM = origNode.val1;
 					int mIdx = domM.indexOf(srcM);
 					Ctxt srcC = origNode.val0;
 					int cIdx = domC.indexOf(srcC);
 					String lockStr = "";
 					Quad inst = insts.iterator().next();
 					int iIdx = domI.indexOf(inst);
 					RelView view = relLI.getView();
 					view.selectAndDelete(1, iIdx);
 					Iterable<Inst> locks = view.getAry1ValTuples();
 					for (Inst lock : locks) {
 						int lIdx = domL.indexOf(lock);
 						RelView view2 = relSyncCLC.getView();
 						view2.selectAndDelete(0, cIdx);
 						view2.selectAndDelete(1, lIdx);
 						Iterable<Ctxt> ctxts = view2.getAry1ValTuples();
 						Set<Ctxt> pts = SetUtils.newSet(view2.size());
 						for (Ctxt ctxt : ctxts)
 							pts.add(ctxt);
 						int oIdx = domO.getOrAdd(new Obj(pts));
 						view2.free();
 						lockStr += "<lock Lid=\"L" + lIdx + "\" Mid=\"M" +
 							mIdx + "\" Oid=\"O" + oIdx + "\"/>";
 					}
 					view.free();
 					return lockStr + "<elem Cid=\"C" + cIdx + "\" " +
 						"Iid=\"I" + iIdx + "\"/>";
 				}
 			};
 
 		out = OutDirUtils.newPrintWriter("TCElist.xml");
 		out.println("<TCElist>");
 		for (Trio<Pair<Ctxt, jq_Method>, Ctxt, Quad> tce : domTCE) {
 			Pair<Ctxt, jq_Method> srcCM = tce.val0;
 			Ctxt methCtxt = tce.val1;
 			Quad heapInst = tce.val2;
 			int cIdx = domC.indexOf(methCtxt);
 			int eIdx = domE.indexOf(heapInst);
 			out.println("<TCE id=\"TCE" + domTCE.indexOf(tce) + "\" " +
 				"Tid=\"A" + domA.indexOf(srcCM)    + "\" " +
 				"Cid=\"C" + cIdx + "\" " +
 				"Eid=\"E" + eIdx + "\">");
 			jq_Method dstM = Program.v().getMethod(heapInst);
 			int mIdx = domM.indexOf(dstM);
 			RelView view = relLE.getView();
 			view.selectAndDelete(1, eIdx);
 			Iterable<Inst> locks = view.getAry1ValTuples();
 			for (Inst lock : locks) {
 				int lIdx = domL.indexOf(lock);
 				RelView view2 = relSyncCLC.getView();
 				view2.selectAndDelete(0, cIdx);
 				view2.selectAndDelete(1, lIdx);
 				Iterable<Ctxt> ctxts = view2.getAry1ValTuples();
 				Set<Ctxt> pts = SetUtils.newSet(view2.size());
 				for (Ctxt ctxt : ctxts)
 					pts.add(ctxt);
 				int oIdx = domO.getOrAdd(new Obj(pts));
 				view2.free();
 				out.println("<lock Lid=\"L" + lIdx + "\" Mid=\"M" +
 					mIdx + "\" Oid=\"O" + oIdx + "\"/>");
 			}
 			view.free();
 			ShortestPathBuilder spb = srcNodeToSPB.get(srcCM);
 			if (spb == null) {
 				spb = new ShortestPathBuilder(thrSenAbbrCSCG, srcCM, visitor);
 				srcNodeToSPB.put(srcCM, spb);
 			}
 			Pair<Ctxt, jq_Method> dstCM =
 				new Pair<Ctxt, jq_Method>(methCtxt, dstM);
 			String path = spb.getShortestPathTo(dstCM);
 			out.println("<path>");
 			out.println(path);
 			out.println("</path>");
 			out.println("</TCE>");
 		}
 		out.println("</TCElist>");
 		out.close();
 
 		relLI.close();
 		relLE.close();
 		relSyncCLC.close();
 
 		domO.saveToXMLFile();
 		domC.saveToXMLFile();
 		domA.saveToXMLFile();
 		domH.saveToXMLFile();
 		domI.saveToXMLFile();
 		domM.saveToXMLFile();
 		domE.saveToXMLFile();
 		domF.saveToXMLFile();
 		domL.saveToXMLFile();
 
 		OutDirUtils.copyFileFromMainDir("src/web/Olist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Clist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Alist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Hlist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Ilist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Mlist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Elist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Flist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/Llist.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/style.css");
 		OutDirUtils.copyFileFromMainDir("src/web/misc.xsl");
 		OutDirUtils.copyFileFromMainDir("src/web/datarace/results.dtd");
 		OutDirUtils.copyFileFromMainDir("src/web/datarace/results.xml");
 		OutDirUtils.copyFileFromMainDir("src/web/datarace/group.xsl");
 		OutDirUtils.copyFileFromMainDir("src/web/datarace/paths.xsl");
 		OutDirUtils.copyFileFromMainDir("src/web/datarace/races.xsl");
 
 		OutDirUtils.runSaxon("results.xml", "group.xsl");
 		OutDirUtils.runSaxon("results.xml", "paths.xsl");
 		OutDirUtils.runSaxon("results.xml", "races.xsl");
 
 		Program.v().HTMLizeJavaSrcFiles();
 	}
 }
