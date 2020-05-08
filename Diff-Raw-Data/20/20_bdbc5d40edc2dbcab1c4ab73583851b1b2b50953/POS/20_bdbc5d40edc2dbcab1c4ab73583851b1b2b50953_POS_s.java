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
 	 * @param position
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
 
		try {
			int rel = s.words.indexOf(w);
 			r.add(s.words.get(rel + position).pos);
		} catch (Exception e) {
			System.out.println("bugger!");
 		}
 		return r;
 	}
 
 }
