 package newsrack.filter;
 
 import java.util.List;
 
 public final class Score implements Comparable, java.io.Serializable
 {
 	private int _score;
 	private List<Integer>  _matchPosns;
 	private List<Category> _matchedCats;
 
 	public Score(int n, List<Category> l) 
 	{ 
 		_score = n; 
 		_matchedCats = l;
 	}
 
 	private int getWeightedScore(int posn)
 	{
 		int n = 1;
 
 			// Incorporating logic here that tracks the position of the concept.
 			// Basically, concepts that match at the beginning of the article
 			// get their value boosted!
 		if (posn < 10)
 			n += 5;
 		else if (posn < 50)
 			n += 2;
 		else if (posn < 200)
 			n += 1;

		return n;
 	}
 
 	public Score(int posn)
 	{
 		_score = getWeightedScore(posn);
 		_matchPosns = new java.util.ArrayList<Integer>();
 		_matchPosns.add(posn);
 		_matchedCats = null;
 	}
 
 	public void addMatch(int posn) { _score += getWeightedScore(posn); _matchPosns.add(posn); }
 
 	public int value() { return _score; }
 
 	public List<Integer> getMatchPosns() { return _matchPosns; }
 
 	public int compareTo(Object o) { return ((Score)o)._score - _score; /* desc order */ }
 
 	public List<Category> getMatchedCats() { return _matchedCats; }
 
 	public String toString() { return Integer.toString(_score); }
 }
 
