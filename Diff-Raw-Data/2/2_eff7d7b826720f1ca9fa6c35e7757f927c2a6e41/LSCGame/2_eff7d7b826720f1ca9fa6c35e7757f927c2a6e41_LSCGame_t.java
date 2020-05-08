 import edu.wis.jtlv.env.*;
 import edu.wis.jtlv.lib.*;
 import edu.wis.jtlv.env.module.*;
 import edu.wis.jtlv.env.spec.*;
 import net.sf.javabdd.*;
 import java.io.*;
 
 /** Class for encapsulating the LSC realizability checking */
 public class LSCGame{
 	/** Environment module (initialized in constructor */
 	Module env;
 
 	/** Environment module (initialized in constructor */
 	Module sys;
 
 	/** Winning states (null, if not computed yet) */
 	BDD win;
 
 	/** Target configurations */
 	BDD q;
 	
 	/** Progress reporter */
 	ProgressReporter reporter;
 
 	/** Construct LSC Game from SMV model
 	 * @remarks: This invalidates other instances of LSCGame, yes, poor pattern but it works.
 	 */
 	public LSCGame(ProgressReporter reporter) throws SpecException{
 		this.reporter = reporter;
 		
 		if(reporter != null)
 			reporter.report("Loading sub-modules...\\n");
 		
 		// Get Modules
 		env = Env.getModule("main.env");
 		sys = Env.getModule("main.sys");
 
 		if(reporter != null)
 			reporter.report("Loading target configurations...\\n");
 
 		// Get target configurations
 		q = Env.loadSpecString("SPEC env.gbuchi = 0")[0].toBDD();
 	}
 
 	/** Check if this game is realizable */
 	public boolean realizable(){
 		if(win == null) computeWinningStates();
 		if(reporter != null)
 			reporter.report("Testing for realizability...\\n");
 		return env.initial().and(sys.initial()).and(win.not()).isZero();
 	}
 
 	/** Find Winning States, results cached in win */
 	private void computeWinningStates(){
 		if(reporter != null)
 			reporter.report("Computing Winning Stategy");
 		BDD z = Env.TRUE();
 		FixPoint<BDD> Fz = new FixPoint<BDD>();
 		while(Fz.advance(z)){
 			BDD qcpredz = q.and(cpred(z));
 			BDD y = Env.FALSE();
 			FixPoint<BDD> Fy = new FixPoint<BDD>();
 			while(Fy.advance(y)){
 				y = cpred(y).or(qcpredz);
 			}
 			z = y;
 			if(reporter != null)
 				reporter.report(".");
 		}
 		win = z;
 		if(reporter != null)
			reporter.report("\\n");
 	}
 
 	/** Controllable Predecessors */
 	private BDD cpred(BDD q) {
 		return	env.trans().imp(
 					Env.prime(q).and(sys.trans()).exist(sys.modulePrimeVars())
 				).forAll(env.modulePrimeVars());
 	}
 
 	/** Synthesize a winning strategy */
 	public String synthesize(){
 		if(!realizable())
 			return "Resistence is furtile we've lost (not realizable!)";
 		if(win == null)
 			computeWinningStates();
 
 		if(reporter != null)
 			reporter.report("Computing transistion from winning target configrations...\\n");
 
 		// Compute commonly used expressions
 		BDD qwin = q.and(win);
 		BDD rXY = env.trans().and(sys.trans());
 
 		// Initial step on the transtion system (the target transition)
 		// Transition says: 		win & q -> win
 		BDD trans = qwin.and(rXY).and(Env.prime(win));
 
 		if(reporter != null)
 			reporter.report("Computing loading to target");
 
 		BDD y = Env.FALSE();
 		FixPoint<BDD> Fy = new FixPoint<BDD>();
 		while(Fy.advance(y)){
 			// Configurations that are qwin or controllable in i number of steps (i = iteration number)
 			BDD next_y = cpred(y).or(qwin);
 			// Transition says:		next_y & !y -> y			(Keep in mind that y in iteration 1 is qwin)
 			BDD next_rule = next_y.and(y.not()).and(rXY).and(Env.prime(y));
 			trans = trans.or(next_rule);
 			y = next_y;
 			if(reporter != null)
 				reporter.report(".");
 		}
 		if(reporter != null)
 			reporter.report("\\nStrategy computed, the BDD for the transistion system has " + trans.nodeCount() + " nodes");
 		return "" + trans.nodeCount();
 	}
 }
 
