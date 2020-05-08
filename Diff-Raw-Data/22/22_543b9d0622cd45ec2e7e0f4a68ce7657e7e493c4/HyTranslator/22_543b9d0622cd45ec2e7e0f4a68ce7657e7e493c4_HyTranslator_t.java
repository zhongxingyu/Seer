 package de.psi.alloy4smt.ast;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.junit.rules.TemporaryFolder;
 
 import kodkod.ast.Formula;
 import kodkod.ast.Relation;
 import kodkod.engine.fol2sat.HigherOrderDeclException;
 import kodkod.engine.fol2sat.Translation;
 import kodkod.engine.fol2sat.Translator;
 import kodkod.engine.satlab.SATFactory;
 import kodkod.engine.satlab.SATSolver;
 import kodkod.instance.Tuple;
 import kodkod.instance.TupleSet;
 import de.psi.alloy4smt.ast.IntRefPreprocessor.IntrefSigRecord;
 import de.psi.alloy4smt.hysat.HysatSolver;
 import edu.mit.csail.sdg.alloy4.A4Reporter;
 import edu.mit.csail.sdg.alloy4.ConstList;
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4.ErrorFatal;
 import edu.mit.csail.sdg.alloy4.ErrorType;
 import edu.mit.csail.sdg.alloy4.Pair;
 import edu.mit.csail.sdg.alloy4.Pos;
 import edu.mit.csail.sdg.alloy4compiler.ast.Command;
 import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
 import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Options.SatSolver;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
 import edu.mit.csail.sdg.alloy4compiler.translator.BoundsComputer;
 import edu.mit.csail.sdg.alloy4compiler.translator.ScopeComputer;
 import edu.mit.csail.sdg.alloy4compiler.translator.Simplifier;
 import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
 import edu.mit.csail.sdg.alloy4compiler.translator.TranslateKodkodToJava;
 
 public final class HyTranslator extends TranslateAlloyToKodkod {

    public static class Dbg {
        public static Formula kFormula;
    }
 	
 	public static final String intrefmod = 
 		"module util/intref\n" +
 		"abstract sig IntRef { aqclass: IntRef }\n";
 	
 	public static void execute(String document) throws Err {
 		Map<String, String> fm = new HashMap<String, String>();
 		fm.put("/tmp/x", document);
 		fm.put("/tmp/util/intref.als", intrefmod);
 		
 		final CompModule module = CompUtil.parseEverything_fromFile(null, fm, "/tmp/x");
 		final IntRefPreprocessor pp = IntRefPreprocessor.processModule(module);
 		execute(null, pp.commands.get(0), new HysatSolver());
 	}
 	
 	public static void execute(String document, HysatSolver solver) throws Err {
 		Map<String, String> fm = new HashMap<String, String>();
 		fm.put("/tmp/x", document);
 		fm.put("/tmp/util/intref.als", intrefmod);
 		
 		final CompModule module = CompUtil.parseEverything_fromFile(null, fm, "/tmp/x");
 		final IntRefPreprocessor pp = IntRefPreprocessor.processModule(module);
 		execute(null, pp.commands.get(0), solver);		
 	}
 	
 	public static void execute(A4Reporter rep, IntRefPreprocessor.CmdBundle bundle,
 			                         final HysatSolver solver) throws Err {
 		rep = rep == null ? A4Reporter.NOP : rep;
 		final A4Options opt = makeA4Options();
 		final Iterable<Sig> sigs = bundle.sigs;
 		final Command cmd = bundle.command;
 		final ConstList<String> hyexprs = bundle.hysatExprs;
 		final ConstList<String> intrefatoms = hyexprs != null ? bundle.getIntrefAtoms() : null;
 		final Pair<A4Solution, ScopeComputer> pair = ScopeComputer.compute(rep, opt, sigs, cmd);
 		
 		BoundsComputer.compute(rep, pair.a, pair.b, sigs);
 		
 		Expr formula = cmd.formula;
 		Relation equalsrel = null;
 		TupleSet equalsupper = null;
 		if (hyexprs != null) {
 			Sig.Field equalsfield = bundle.intref.addField("equals", bundle.intref.setOf());
 			equalsupper = bundle.getIntRefEqualsTupleSet(pair.a.getFactory());
 			equalsrel = pair.a.addRel("IntRef/equals", null, equalsupper);
 			pair.a.addField(equalsfield, equalsrel);
 			
 			/*
 			 * fact {
 			 *   all disj a, b: intref/IntRef {
 			 *     (b in a.equals or a in b.equals) <=> a.aqclass = b.aqclass
 			 *     (b in a.equals) => (b.aqclass = a or b.aqclass in a.equals)
 			 *   }
 			 * }
 			 */
 			Decl da = bundle.intref.oneOf("a");
 			Decl db = bundle.intref.oneOf("b");
 			Field aqclass = Helpers.getFieldByName(bundle.intref.getFields(), "aqclass");
 			
 			// b in a.equals or a in b.equals
 			Expr e1 = db.get().in(da.get().join(equalsfield)).or(da.get().in(db.get().join(equalsfield)));
 			// a.aqclass = b.aqclass
 			Expr e2 = da.get().join(aqclass).equal(db.get().join(aqclass));
 			// b in a.equals
 			Expr e3 = db.get().in(da.get().join(equalsfield));
 			// b.aqclass = a or b.aqclass in a.equals
 			Expr e4 = db.get().join(aqclass).equal(da.get()).or(db.get().join(aqclass).in(da.get().join(equalsfield)));
 			// a != b
 			Expr e5 = da.get().equal(db.get()).not();
 			
 			Expr sub = e5.implies(e1.iff(e2).and(e3.implies(e4)));
 			formula = formula.and(ExprQt.Op.ALL.make(null, null, Arrays.asList(new Decl[] {da, db}), sub));
 			
 			for (IntrefSigRecord record : bundle.intrefRecords) {
 				if (record.mapfield != null) {
 					Relation rel = (Relation) pair.a.a2k(record.mapfield);
 					TupleSet bound = record.getMapBounds(bundle.command, pair.a.getFactory());
 					pair.a.shrink(rel, bound, bound);
 				}
 			}
 		}
 		
 		HyTranslator tr = null;
 		Translation tl = null;
 		try {	
 			tr = new HyTranslator(rep, cmd, opt, pair.a);
 			pair.a.solver.options().setLogTranslation(2);
 			pair.a.solver.options().setSolver(new SATFactory() {
 				@Override
 				public SATSolver instance() {
 					if (hyexprs != null) {
 						for (String atom : intrefatoms) {
 							solver.addHysatVariable(atom, -1000000, 1000000);
 						}
 						for (String expr : hyexprs) {
 							solver.addHysatExpr(expr);
 						}
 					}
 					return solver;
 				}
 
 				@Override  public boolean prover() { return false; }
 				@Override  public boolean minimizer() { return false; }
 				@Override  public boolean incremental() { return false; }
 				@Override  public String toString() { return "HySAT solver"; }
 			});
 			
 			tr.makeFacts(formula);
 			
 			List<String> atoms = new Vector<String>();
 			for (Iterator<Object> it = tr.frame.getFactory().universe().iterator(); it.hasNext();) { 
 				atoms.add((String) it.next()); 
 			}
 			Formula kformula = tr.frame.makeFormula(rep, new Simplifier());
            Dbg.kFormula = kformula;
 			String kodkodout = TranslateKodkodToJava.convert(kformula, tr.frame.getBitwidth(), atoms, tr.frame.getBounds(), null);
 			File tmpout = File.createTempFile("kodkodout", ".txt");
 			FileWriter writer = new FileWriter(tmpout);
 			writer.write(kodkodout);
 			writer.close();
 			
 			tl = Translator.translate(kformula, tr.frame.getBounds(), tr.frame.solver.options());
 			
 		} catch (HigherOrderDeclException ex) {
             Pos p = tr!=null ? tr.frame.kv2typepos(ex.decl().variable()).b : Pos.UNKNOWN;
             throw new ErrorType(p, "Analysis cannot be performed since it requires higher-order quantification that could not be skolemized.");			
 		} catch (Throwable ex) {			
             if (ex instanceof Err) throw (Err)ex; else throw new ErrorFatal("Unknown exception occurred: "+ex, ex);
 		}
 
 		if (equalsrel != null) {
 			int[] relvars = tl.primaryVariables(equalsrel).toArray();
 			int i = 0;
 			for (Tuple t : equalsupper) {
 				solver.addHysatExpr("cnf_" + relvars[i] + " <-> (" + t.atom(0).toString() + " = " + t.atom(1).toString() + ")");
 				++i;
 			}

 		}

        solver.solve();
 	}
 	
 	private static A4Options makeA4Options() {
 		final A4Options opt = new A4Options();
 		opt.recordKodkod = true;
 		opt.tempDirectory = "/tmp";
 		opt.solverDirectory = "/tmp";
 		opt.solver = SatSolver.SAT4J;
 		opt.skolemDepth = 4;
 		return opt;
 	}
 	
 	private HyTranslator(A4Reporter rep, Command cmd, A4Options opt, A4Solution frame) throws Err {
 		super(rep, opt, frame, cmd);
 	}
 }
