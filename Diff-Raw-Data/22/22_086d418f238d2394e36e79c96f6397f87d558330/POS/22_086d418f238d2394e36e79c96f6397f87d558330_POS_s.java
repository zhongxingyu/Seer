 /**
  * 
  */
 package features;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import model.Sentence;
 import model.Word;
 
 /**
  * @author muelleml
  * 
  */
 public class POS implements Feature {
 
 	int position;
 
 	public POS() {
 		position = 0;
 	}
 
 	/**
 	 * Part Of Speech Tag
 	 * 
 	 * @param position
 	 *            The position relative to the given Word. 0 is the Word itself
 	 */
 	public POS(int position) {
 		this.position = position;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see features.Feature#extract(model.Word, model.Sentence)
 	 */
 	@Override
 	public List<String> extract(Word w, Sentence s) {
 		List<String> r = new LinkedList<String>();
 
		int rel = s.words.indexOf(w);
 
		// System.out.println(Integer.toString(position)+Integer.toString(rel)+Integer.toString(s.words.size()));
 
 		if (rel + position >= 0 && rel + position <= s.words.size() - 1) {
 
 			r.add(s.words.get(rel + position).pos);
 		} else {
 			r.add("");
 		}
 
 		return r;
 	}
 
 }
