 import edu.jhu.agiga.AgigaSentence;
 import edu.jhu.agiga.AgigaToken;
 
 
 public class Event {
 	public static CountEvents countEvents = null;
 	
 	// Maybe this should be verb, argument-type (subject/object)
 
 	public int verb;
 	public boolean argType;
 	public int argTokId;
 	public int sentId;
 	
 	public AgigaSentence sentence;
 	public Event(int v, boolean a) {
 		verb = v;
 		argType = a;
 	}
 	
 	public boolean isComplement(Event other) {
 		return (other.argType != argType) &&
 			(countEvents.eventToVerbMap.get(verb).equals(countEvents.eventToVerbMap.get(other.verb)));
 	}
 	
 		
 	public boolean equals(Object e){
 		if (!(e instanceof Event))
 			  return false;
 		Event o = (Event)e;
 		if(o.verb==this.verb && o.argType == this.argType) return true;
 		return false;
 		
 	}
 	 
 	public int hashCode() {
 		return  argType?verb+1:-(verb+1);
 	}
 	
 	public Event getComplementEvent() {
 		// This used to be correct, but now it isn't because verb refers to eventID...
 		//return new Event(verb, !argType);
 		
 		String v = countEvents.eventToVerbMap.get(verb);
 		String otherArg = "nsubj";
 		if (argType)
 			otherArg = "dobj";
 		int otherEventID = countEvents.verbArgTypeMap.get(new Pair<String, String>(v, otherArg));
 		return new Event(otherEventID, !argType);
 	}
 	
 	/*public int getLargeIndex() {
 		return 2 * verb + (argType ? 0 : 1);
 	}*/
 	
 	public double getPMI(Event e) {
 		Integer cooccur = countEvents.eventPairCounts.get(new Pair<Event, Event>(this, e));
 		double cooccurP = (cooccur + 0.0) / countEvents.eventPairOverallCount;
 		
 		int thisCount = countEvents.eventsCountMap.get(this);
 		int eCount = countEvents.eventsCountMap.get(e);
 		
 		double thisP = (thisCount + 0.0) / countEvents.eventOverallCount;
 		double eP = (eCount + 0.0) / countEvents.eventOverallCount;
 		
 		return Math.log(cooccurP / (thisP * eP));
 	}
 	
 	public double getSimilarity(Event e, Protagonist p) {
 		Integer f = countEvents.eventPairProCounts.get(
 				new Triple<Event, Event, Protagonist>(this, e, p));
 		double freq = 0;
 		if (f != null)
 			freq = f;
 		if (freq == 0) {
			return 0; // that's the minimum you'd get anyway
 		}
 		freq = Math.log(freq);
 		freq *= CountEvents.LAMBDA;
 		return getPMI(e) + freq;
 	}
 	
 	public String toString() {
 		if (argType)
 			return "" + verb + " " + argType + ":" + countEvents.eventToVerbMap.get(this) + "S";
 		else
 			return "" + verb + " " + argType + ":" + countEvents.eventToVerbMap.get(this) + "O";
 	}
 	
 	public Event copy() {
 		return new Event(verb, argType);
 	}
 	public String getSentenceString() {
 		if (sentence != null) {
 			String s = "";
 			for (AgigaToken t : sentence.getTokens()) {
 				s += t.getWord() + " ";
 			}
 			return s;
 		}
 		return "";
 	}
 }
