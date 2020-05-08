 package com.strategy.util.operation;
 
 import java.io.PrintStream;
 
 import net.sf.javabdd.BDD;
 
 import com.strategy.util.Preferences;
 
 /**
  * @author Ralph Dürig
  */
 public class Bdd {
 
 	private PrintStream out;
 	private String caption;
 	private int sumTimes;
 	private long sumDurations;
 	private long sumNodes;
 
 	/**
 	 * Creates a {@link Bdd} logging to stdout.
 	 */
 	public static Bdd create(String caption) {
 		return new Bdd(Preferences.getInstance().getOut(), caption);
 	}
 
 	// ************************************************************************
 
	public Bdd(PrintStream out, String caption) {
 		this.out = out;
 		this.caption = caption;
 		sumDurations = 0;
 		sumNodes = 0;
 		sumTimes = 0;
 	}
 
 	public BDD andLog(BDD x, BDD y) {
 		return applyOp(x, y, Op.AND);
 	}
 
 	public BDD orLog(BDD x, BDD y) {
 		return applyOp(x, y, Op.OR);
 	}
 
 	public void log() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(caption);
 		sb.append(": ");
 		sb.append(sumTimes);
 		sb.append(" times, ");
 		sb.append(sumDurations);
 		sb.append(" msec, ");
 		sb.append(sumNodes);
 		sb.append(" nodes");
 
 		out.println(sb.toString());
 	}
 
 	// ************************************************************************
 
 	private BDD applyOp(BDD x, BDD y, Op op) {
 		sumTimes++;
 		int nodesBefore = x.nodeCount();
 		long millisBefore = System.currentTimeMillis();
 		BDD result = op.apply(x, y);
 		long millisAfter = System.currentTimeMillis();
 		int nodesAfter = result.nodeCount();
 		sumDurations += millisAfter - millisBefore;
 		sumNodes += nodesAfter - nodesBefore;
 		return result;
 	}
 
 }
