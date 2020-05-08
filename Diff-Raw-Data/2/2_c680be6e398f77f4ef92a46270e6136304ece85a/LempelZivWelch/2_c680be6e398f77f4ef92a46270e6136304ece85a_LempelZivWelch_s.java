 package suite.algo;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import suite.util.FunUtil.Sink;
 import suite.util.FunUtil.Source;
 import suite.util.Util;
 
 public class LempelZivWelch<Unit> {
 
 	private List<Unit> units;
 
 	private class Trie {
 		private Integer index;
 		private Map<Unit, Trie> branches = new HashMap<>();
 
 		private Trie(Integer index) {
 			this.index = index;
 		}
 	}
 
 	public LempelZivWelch(List<Unit> units) {
 		this.units = units;
 	}
 
 	public void encode(Source<Unit> source, Sink<Integer> sink) {
 		Trie root = new Trie(null);
 		int index = 0;
 
 		for (Unit unit : units)
 			root.branches.put(unit, new Trie(index++));
 
 		Trie trie = root;
 		Unit unit;
 
 		while ((unit = source.source()) != null) {
 			if (!trie.branches.containsKey(unit)) {
 				sink.sink(trie.index);
 				trie.branches.put(unit, new Trie(index++));
 				trie = root;
 			}
 
 			trie = trie.branches.get(unit);
 		}
 
 		if (trie != root)
 			sink.sink(trie.index);
 	}
 
 	public void decode(Source<Integer> source, Sink<Unit> sink) {
 		List<List<Unit>> dict = new ArrayList<>();
 
 		for (Unit unit : units)
 			dict.add(Arrays.asList(unit));
 
 		Integer index;
 
 		if ((index = source.source()) != null) {
 			List<Unit> word;
 
 			for (Unit unit : word = dict.get(index))
 				sink.sink(unit);
 
 			while ((index = source.source()) != null) {
 				List<Unit> w0 = word;
 				List<Unit> newWord;
 
 				if (index < dict.size())
					newWord = Util.add(w0, word = dict.get(index));
 				else
 					newWord = word = Util.add(w0, Util.left(w0, 1));
 
 				if (!w0.isEmpty())
 					dict.add(newWord);
 
 				for (Unit unit : word)
 					sink.sink(unit);
 			}
 		}
 	}
 
 }
