 
 package pegasus.esp;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import pegasus.eventbus.client.Envelope;
 
 import com.espertech.esper.client.EventBean;
 import com.google.common.collect.Sets;
 
 class ConsensusSearchDetector extends EventMonitor {
 
     public static final String INFERRED_TYPE = "ConsensusSearchEvent";
     private static final String TERM_KEY = "term";
     private static final String USER_KEY = "user";
     private static final String FREQ_KEY = "frequency";
 
     /**
      * Catch a search event and split it into multiple search term
      * events.  This normalizes case to lower-case and removes stop
      * words.
      *
      * e.g.  when there's a event like
      *     Search("JackRyan", "Europe Satellite Imagery For Sunday")
      * this class will generate the events
      *     SearchTerm("JackRyan", 'europe')
      *     SearchTerm("JackRyan", 'satellite')
      *     SearchTerm("JackRyan", 'imagery')
      *     SearchTerm("JackRyan", 'sunday')
      *
      * @author israel
      *
      */
     class TermSplitter extends EventMonitor {
 
         boolean isEPL = true;
         String pattern = "select search from Envelope as search where eventType = 'Search'";
         private String fieldToSplit;
         private String inferredType;
 
 
         public TermSplitter(boolean isEPL, String pattern, String fieldToSplit, String inferredType) {
             super();
             this.inferredType = inferredType;
             this.isEPL = isEPL;
             this.pattern = pattern;
             this.fieldToSplit = fieldToSplit;
         }
 
         public InferredEventList receive(EventBean eventBean) {
             Envelope env = (Envelope) eventBean.get(fieldToSplit);
             InferredEventList events = new InferredEventList();
             String userid = env.getReplyTo();
             String[] searchTerms = getSearchTerms(env);
             for (String term : searchTerms) {
                 if (! STOPWORDS.contains(term)) {
                     InferredEvent event = makeInferredEvent();
                     event.putData(USER_KEY, userid).putData(TERM_KEY, term);
                     events.addInferredEvent(event);
                 }
             }
             return events;
         }
 
         @Override
         public void registerPatterns(EventStreamProcessor esp) {
             esp.monitor(isEPL, pattern, this);
         }
 
         @Override
         public String getInferredType() {
             return inferredType;
         }
     }
 
     private int minFreq;
     private String timeLimit;
 
     public ConsensusSearchDetector(int minFreq, String timeLimit) {
         super();
         this.minFreq = minFreq;
         this.timeLimit = timeLimit;
     }
 
    private String[] getSearchTerms(Envelope env) {
        String topic = env.getTopic();
        String[] terms = topic.toLowerCase().split(" ");
        return terms;
    }

     public InferredEvent receive(EventBean eventBean) {
         InferredEvent event = makeInferredEvent();
         String term = (String) eventBean.get("term");
         Long freq = (Long) eventBean.get("freq");
         event.putData(FREQ_KEY, "" + freq).putData(TERM_KEY, term);
         return event;
     }
 
     @Override
     public void registerPatterns(EventStreamProcessor esp) {
 
         // To calculate search term frequency, we need to do the following:
         //
         // 1. split the search string into individual terms and add them individually
         //    into the event stream (the TermSplitter class does this).
         // 2. extract the term and user strings from the search so that they can be aggregated
         //    and filtered.
         // 3. calculate a frequency count aggregating by the term.
         // 4. filter the terms with q frequency count at least the minimumn specified
         //    frequency count.
 
         String findSearches = "select search from Envelope as search where eventType = 'Search'";
         EventMonitor termSplitter = new TermSplitter(true, findSearches, "search", "Search Term");
 
         String createUT = "insert into UserTerms " +
                 "select event.getData('" + TERM_KEY + "') as term, event.getData('" +
                 USER_KEY + "') as user from InferredEvent.win:time(" +
                 timeLimit + ") as event where type='Search Term'";
 
         // TODO: have frequency counts based on unique user requests
 
         String createSTF = "insert into SearchTermFreq select term, count(*) as freq " +
                 "from UserTerms.win:time(" + timeLimit +
                 ") group by term";
 
         String getSTF = "select * from SearchTermFreq.win:time(" + timeLimit +
                 ") where freq >= " + minFreq;
 
         // extract search terms and add multiple inferred events (one for each)
         esp.monitor(true, findSearches, termSplitter);
 
         // extract out the search term and and user from the search term stream within the
         // time period
         esp.monitor(true, createUT, null);
 
         // extract frequency count by search term
         esp.monitor(true, createSTF, null);
 
         // filter out search terms with at least the desired minimum frequency
         esp.monitor(true, getSTF, this);
 
     }
 
     private final static Set<String> STOPWORDS = makeStopWordSet();
 
     private static HashSet<String> makeStopWordSet() {
         HashSet<String> words = Sets.newHashSet();
         String[] STOPWORDLIST = {"a", "about", "above",
                 "above", "across", "after", "afterwards", "again", "against", "all",
                 "almost", "alone", "along", "already",
                 "also","although","always","am","among", "amongst", "amoungst",
                 "amount", "an", "and", "another",
                 "any","anyhow","anyone","anything","anyway", "anywhere", "are",
                 "around", "as", "at", "back","be","became",
                 "because","become","becomes", "becoming", "been", "before",
                 "beforehand", "behind", "being", "below", "beside", "besides",
                 "between", "beyond", "bill", "both", "bottom","but", "by", "call",
                 "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry",
                 "de", "describe", "detail", "do", "done", "down", "due", "during",
                 "each", "eg", "eight", "either", "eleven","else", "elsewhere",
                 "empty", "enough", "etc", "even", "ever", "every", "everyone",
                 "everything", "everywhere", "except", "few", "fifteen", "fify",
                 "fill", "find", "fire", "first", "five", "for", "former",
                 "formerly", "forty", "found", "four", "from", "front", "full",
                 "further", "get", "give", "go", "had", "has", "hasnt", "have", "he",
                 "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon",
                 "hers", "herself", "him", "himself", "his", "how", "however",
                 "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into",
                 "is", "it", "its", "itself", "keep", "last", "latter", "latterly",
                 "least", "less", "ltd", "made", "many", "may", "me", "meanwhile",
                 "might", "mill", "mine", "more", "moreover", "most", "mostly",
                 "move", "much", "must", "my", "myself", "name", "namely", "neither",
                 "never", "nevertheless", "next", "nine", "no", "nobody", "none",
                 "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off",
                 "often", "on", "once", "one", "only", "onto", "or", "other",
                 "others", "otherwise", "our", "ours", "ourselves", "out", "over",
                 "own","part", "per", "perhaps", "please", "put", "rather", "re",
                 "same", "see", "seem", "seemed", "seeming", "seems", "serious",
                 "several", "she", "should", "show", "side", "since", "sincere",
                 "six", "sixty", "so", "some", "somehow", "someone", "something",
                 "sometime", "sometimes", "somewhere", "still", "such", "system",
                 "take", "ten", "than", "that", "the", "their", "them", "themselves",
                 "then", "thence", "there", "thereafter", "thereby", "therefore",
                 "therein", "thereupon", "these", "they", "thickv", "thin", "third",
                 "this", "those", "though", "three", "through", "throughout", "thru",
                 "thus", "to", "together", "too", "top", "toward", "towards",
                 "twelve", "twenty", "two", "un", "under", "until", "up", "upon",
                 "us", "very", "via", "was", "we", "well", "were", "what",
                 "whatever", "when", "whence", "whenever", "where", "whereafter",
                 "whereas", "whereby", "wherein", "whereupon", "wherever", "whether",
                 "which", "while", "whither", "who", "whoever", "whole", "whom",
                 "whose", "why", "will", "with", "within", "without", "would", "yet",
                 "you", "your", "yours", "yourself", "yourselves", "the" };
         for (String word : STOPWORDLIST) {
             words.add(word);
         }
         return words;
     }
 
     @Override
     public String getInferredType() {
         return INFERRED_TYPE;
     }
 
 }
