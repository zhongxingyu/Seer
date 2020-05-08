 /**
  * 
  */
 package classifier;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import model.Corpus;
 import model.Cue;
 import model.Sentence;
 import model.Word;
 
 /**
  * @author muelleml
  * 
  */
 public class Baseline implements Classifier {
 
 	private Set<String> lu = new TreeSet<String>();
 
 	private Corpus training, classif;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see classifier.Classifier#train(model.Corpus)
 	 */
 	@Override
 	public void train(Corpus c) {
 
 		training = c;
 
 		for (Sentence s : c.sentences) {
 			for (Word w : s.words) {
 				for (Cue cue : w.cues) {
 
 					if (!cue.cue.equals("_")) {
 						w.c = cue.cue;
 						if (cue.cue.equals("at")) {
 							System.out.println(w.toString());
 						}
 						// System.out.println(cue.cue + w.word);
 						lu.add(w.word);
 						for (Cue cu : w.cues) {
 							// System.out.println(cu.toString());
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see classifier.Classifier#classify(model.Corpus)
 	 */
 	@Override
 	public Corpus classify(Corpus c) {
 		classif = new Corpus();
 
 		for (Sentence s : c.sentences) {
 			classif.sentences.add(classify(s));
 
 		}
 
 		for (String s : lu) {
 			// System.out.println(s);
 		}
 
 		return classif;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see classifier.Classifier#classify(model.Sentence)
 	 */
 	@Override
 	public Sentence classify(Sentence s) {
 		Sentence r = new Sentence();
 
 		for (Word w : s.words) {
 			r.words.add(classify(w));
 		}
 
 		int max = 0;
 
		for (Word w : s.words) {
 			if (w.cues.size() > max) {
 				max = w.cues.size();
 			}
 		}

 		for (Word w : r.words) {
 			while (w.cues.size() < max) {
 				w.cues.add(new Cue("_", "_", "_"));
 			}
 		}
 		// System.out.println(max);
 
 		return r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see classifier.Classifier#classify(model.Word)
 	 */
 	@Override
 	public Word classify(Word w) {
 		Word r = new Word(w.origin, w.sentenceID, w.tokenID, w.word, w.lemma,
 				w.pos, w.parseTree);
 
 		if (lu.contains(w.word)) {
 			Cue cu = new Cue(w.word, "_", "_");
 			r.cues.add(cu);
 			r.c = w.c;
 		}
 
 		return r;
 	}
 
 	public Metric metrics(Corpus check) {
 
 		float tp = 0f;
 		float tn = 0f;
 		float fp = 0f;
 		float fn = 0f;
 
 		String c1;
 		String c2;
 
 		int nrSent = check.sentences.size();
 
 		if (check.sentences.size() > classif.sentences.size()) {
 			nrSent = classif.sentences.size();
 		}
 
 		for (int i = 0; i < nrSent; i++) {
 
 			for (int j = 0; j < check.sentences.get(i).words.size(); j++) {
 				//System.out.println(check.sentences.get(i).toString());
 				//System.out.println(check.sentences.get(i).toString());
 				
 
 				c1 = classif.sentences.get(i).words.get(j).c;
 				c2 = check.sentences.get(i).words.get(j).c;
 
 				if (c1.equals("_") & c2.equals("_")) {
 					tn++;
 				} else if (!c1.equals("_") & !c2.equals("_")) {
 					tp++;
 				} else if (c1.equals("_") & !c2.equals("_")) {
 					fn++;
 				} else if (!c1.equals("_") & c2.equals("_")) {
 					fp++;
 				}
 			}
 
 		}
 
 		return new Metric(tp, tn, fp, fn);
 	}
 }
