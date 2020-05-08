 package dart.blackcat.talker.ru;
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import dart.blackcat.talker.ru.util.StringLengthComparator;
 
 public class Root extends AbstractMorpheme {
 	
 	private static final String[] S = {
		"ид", "ход", "хож", "гул", "шел", "шл", "вод", "ворот", "лож"
 	};
 
 //	@Override
 //	public String[] getMorfemes() {
 //		return S;
 //	}
 	
 	@Override
 	public SortedSet<String> getSortedMorfemes(boolean asc) {
 		SortedSet<String> set = new TreeSet<String>(new StringLengthComparator(asc));
 		for (int i = 0; i < S.length; i++) {
 			set.add(S[i]);
 		}
 		return set;
 	}
 
 }
