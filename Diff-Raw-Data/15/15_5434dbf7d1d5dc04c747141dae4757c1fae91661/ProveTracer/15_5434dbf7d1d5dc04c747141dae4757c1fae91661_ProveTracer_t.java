 package org.suite.doer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.util.Util.Fun;
 
 public class ProveTracer {
 
 	private List<Record> records = new ArrayList<>();
 	private Record currentRecord = null;
 	private int currentDepth;
 
 	private class Record {
 		private Record parent;
 		private Node query;
 		private int depth;
 		private int nOkays;
 		private int end = -1;
 
 		private Record(Record parent, Node query, int depth) {
 			this.parent = parent;
 			this.query = query;
 			this.depth = depth;
 		}
 
 		private void appendTo(StringBuilder sb, int pos) {
			if (Boolean.TRUE)
 				sb.append(String.format("[%4s:%-2d]  " //
 						, nOkays > 0 ? "OK__" : "FAIL", depth));
 			else
 				sb.append(String.format("[pos=%-5d, OKs=%-2d, end=%-5s]  " //
 						, pos //
 						, nOkays //
 						, end >= 0 ? String.valueOf(end) : ""));
 
 			for (int i = 1; i < depth; i++)
 				sb.append("| ");
 			sb.append(Formatter.dump(query));
 			sb.append("\n");
 		}
 	}
 
 	public Node expandWithTrace(Node query, Prover prover,
 			Fun<Node, Node> expand) {
 		Node query1 = new Cloner().clone(query);
 
 		final Record record0 = currentRecord;
 		final int depth0 = currentDepth;
 		final Record record = new Record(record0, query1, currentDepth + 1);
 
 		if (record.depth >= 64)
 			throw new RuntimeException("Maximum depth reached during trace");
 
 		final Station enter = new Station() {
 			public boolean run() {
 				currentRecord = record;
 				currentDepth = record.depth;
 				records.add(record);
 				return true;
 			}
 		};
 
 		final Station leaveOk = new Station() {
 			public boolean run() {
 				currentRecord = record0;
 				currentDepth = depth0;
 				record.nOkays++;
 				return true;
 			}
 		};
 
 		final Station leaveFail = new Station() {
 			public boolean run() {
 				currentRecord = record0;
 				currentDepth = depth0;
 				record.end = records.size();
 				return false;
 			}
 		};
 
 		Node alt = prover.getAlternative();
 		Node rem = prover.getRemaining();
 
 		alt = Tree.create(TermOp.OR____, leaveFail, alt);
 		rem = Tree.create(TermOp.AND___, leaveOk, rem);
 		query = expand.apply(query);
 		query = Tree.create(TermOp.AND___, enter, query);
 
 		prover.setAlternative(alt);
 		prover.setRemaining(rem);
 		return query;
 	}
 
 	public String getDump() {
 		StringBuilder sb = new StringBuilder();
 		int n = 0;
 
 		for (Record record : records)
 			record.appendTo(sb, n++);
 
 		return sb.toString();
 	}
 
 	public String getStackTrace() {
 		List<Node> traces = new ArrayList<>();
 		Record record = currentRecord;
 
 		while (record != null) {
 			traces.add(record.query);
 			record = record.parent;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (int i = traces.size(); i > 0; i--)
 			sb.append(traces.get(i - 1) + "\n");
 
 		return sb.toString();
 	}
 
 }
