 package suite.asm;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 
 import suite.Suite;
 import suite.lp.doer.Generalizer;
 import suite.lp.doer.Prover;
 import suite.node.Atom;
 import suite.node.Data;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Reference;
 import suite.node.Tree;
 import suite.node.io.CommentProcessor;
 import suite.node.io.TermOp;
 import suite.primitive.Bytes;
 import suite.primitive.Bytes.BytesBuilder;
 import suite.util.FunUtil.Source;
 import suite.util.Pair;
 import suite.util.Util;
 
 public class Assembler {
 
 	private Prover prover = Suite.createProver(Arrays.asList("asm.sl", "auto.sl"));
 
 	private int org;
 	private int bits;
 
 	public Assembler(int bits) {
 		this.bits = bits;
 	}
 
 	public Bytes assemble(String input) {
 		CommentProcessor commentProcessor = new CommentProcessor(Collections.singleton('\n'));
 		List<String> lines = Arrays.asList(commentProcessor.apply(input).split("\n"));
 		int start = 0;
 		String line0;
 
 		if ((line0 = lines.get(start).trim()).startsWith("ORG")) {
 			org = Integer.parseInt(line0.substring(3).trim(), 16);
 			start++;
 		}
 
 		Generalizer generalizer = new Generalizer();
 		List<Pair<Reference, Node>> lnis = new ArrayList<>();
 
 		for (String line : Util.right(lines, start)) {
 			String label = null;
 
 			if (line.startsWith(Generalizer.variablePrefix)) {
 				Pair<String, String> pair = Util.split2(line, " ");
 				label = pair.t0.trim();
 				line = pair.t1.trim();
 			} else
 				line = line.trim();
 
 			Pair<String, String> pair = Util.split2(line, " ");
 			Reference reference = label != null ? generalizer.getVariable(Atom.create(label)) : null;
 			Node instruction = generalizer.generalize(Tree.create(TermOp.TUPLE_, Atom.create(pair.t0), Suite.parse(pair.t1)));
 			lnis.add(Pair.create(reference, instruction));
 		}
 
 		return assemble(generalizer, lnis);
 	}
 
 	private Bytes assemble(Generalizer generalizer, List<Pair<Reference, Node>> lnis) {
 		Map<Reference, Node> addressesByLabel = new IdentityHashMap<>();
 		BytesBuilder out = new BytesBuilder();
 
 		for (boolean isPass2 : new boolean[] { false, true }) {
 			out.clear();
 
 			for (Pair<Reference, Node> lni : lnis)
 				if (lni.t0 != null && isPass2)
 					lni.t0.bound(addressesByLabel.get(lni.t0));
 
 			for (Pair<Reference, Node> lni : lnis) {
 				try {
 					out.append(assemble(org + out.size(), lni.t1));
 				} catch (Exception ex) {
 					throw new RuntimeException("In " + lni.t1, ex);
 				}
 
 				if (!isPass2 && lni.t0 != null)
 					addressesByLabel.put(lni.t0, Int.create(org + out.size()));
 			}
 
 			for (Pair<Reference, Node> lni : lnis)
 				if (lni.t0 != null && isPass2)
 					lni.t0.unbound();
 		}
 
 		return out.toBytes();
 	}
 
 	private Bytes assemble(int address, Node instruction) {
 		final Reference e = new Reference();
 		final List<Bytes> list = new ArrayList<>();
 
		Node goal = Suite.substitute("asi:.0 (.1 .2) .3, .4", Int.create(bits), Int.create(address), instruction, e, new Data<>(
 				new Source<Boolean>() {
 					public Boolean source() {
 						list.add(convertByteStream(e));
 						return true;
 					}
 				}));
 		// LogUtil.info(Formatter.dump(goal));
 
 		prover.elaborate(goal);
 
 		if (!list.isEmpty())
 			return Collections.min(list, new Comparator<Bytes>() {
 				public int compare(Bytes bytes0, Bytes bytes1) {
 					return bytes0.size() - bytes1.size();
 				}
 			});
 		else
 			throw new RuntimeException("Failure");
 	}
 
 	private Bytes convertByteStream(Node node) {
 		BytesBuilder bb = new BytesBuilder();
 		Tree tree;
 		while ((tree = Tree.decompose(node, TermOp.AND___)) != null) {
 			bb.append((byte) ((Int) tree.getLeft().finalNode()).getNumber());
 			node = tree.getRight();
 		}
 		return bb.toBytes();
 	}
 
 }
