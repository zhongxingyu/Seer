 package it.polito.atlas.alea2;
 
 import java.util.List;
 
 import it.polito.atlas.alea2.tule.Lemma;
 import it.polito.atlas.alea2.tule.TuleClient;
 
 public class TrackText extends Track {
 	public TrackText (Annotation parent, String name)
 	{
 		super(parent, name);
 		type = Track.Types.Text;
 	}
 	
 	public void addLemma(String str) {
 		Slice s=new Slice(null, 0, 0);
 		s.setName(str);
 		s.addProperty(new Property(s, "Lemma", str));
 		addSlice(s);
 		s.setParent(this);
 		setModified();
 	}
 	
 	public void addLemma(Lemma lemma) {
 		if (lemma == null)
 			return;
 		
 		// Create a floating slice
 		Slice s=new Slice(null, 0, 0);
 		s.setName(lemma.getText());
 
 		// Create attributes inside the slice
 		for (String attr : lemma.getAttributes().keySet()) {
 			s.addProperty(new Property(s, attr, lemma.getAttributes().get(attr)));
 		}
 		
 		// Add the slice to this track
 		addSlice(s);
 		s.setParent(this);
 		setModified();
 	}
 	
 	public void addWords(String text) {
 		addWords(text, " ");
 	}
 	
 	public void addWords(String text, String regex) {
 		if (text==null)
 			return;
 		String [] words = text.split(regex);
 		
 		// Create a slice for each word (lemma)
 		for (String str : words) {
 			if (str.compareTo("")==0)
 				continue;
 			addLemma(str);
 		}
 	}
 
 	public void addLemmas(List <Lemma> lemmas) {
 		if (lemmas==null)
 			return;
 
 		// Create a slice for each lemma
 		for (Lemma lemma : lemmas) {
 			addLemma(lemma);
 		}
 	}
 
 	public void addTuleLemmas(String text, TuleClient tule) {
 		if (tule==null)
 			return;
		addLemmas(tule.parseTuleText(text));
 	}
 }
