 package eu.monnetproject.nlp.stl.impl;
 
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.nlp.stl.Decomposer;
 import eu.monnetproject.nlp.stl.Termbase;
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * A brute force search algorithm which finds a optimal decomposition of a
  * minimum number of subterms from a termbase for a given term; a subterm is a
  * term in the termbase and a substring of the analyzed term.
  *
  * @author Tobias Wunner
  */
 public class MinimumSubtermDecomposer implements Decomposer, eu.monnetproject.translation.Decomposer {
 
     private static final Logger log = Logger.getLogger(MinimumSubtermDecomposer.class.getName());
     public static final int MIN_TERM_LENGTH = 2;
     public static final HashSet<List<String>> EMPTY_DECOMPOSITION = new HashSet<List<String>>();
     public static final ArrayList<String> EMPTY_DECOMP = new ArrayList<String>(0);
     public static final String ONE_OR_TWO_LETTER_TO_WORD_BOUNDARY = "..?\\b.*";
     public static final Pattern COMPOUND_INSERTION = Pattern.compile("(s|e|en|er|n)(.*)");
     //private final Integer currentmindecomp;
     public final Termbase global;
     //private final List<String> local = new LinkedList<String>();
 
     public MinimumSubtermDecomposer(Termbase termbase) {
         if (termbase == null) {
             throw new IllegalArgumentException("Termbase is null");
         } else {
             this.global = termbase;
             // add local tokens
             //      String[] localTermbaseArray = {" "};
             //    local.addAll(Arrays.asList(localTermbaseArray));
             // add local tokens de
             //  if (termbase.getLanguage().equals("de")) {
             //     String[] localTermbaseArray_de = {" ", "e", "n", "en"};
             //     local.addAll(Arrays.asList(localTermbaseArray_de));
             //}
         }
     }
     //public Map<String,String> termbase = new HashMap<String,String>();
     //public Map<String,List<String>> termbaseIds = new HashMap<String,List<String>>();
     //public Set<String> termbaseFiltered = new HashSet<String>();
 
     /*
      public List<String> getIds4Decomposition(List<String> decomposition) {
      List<String> decompositionWithIds = new LinkedList<String>();
      for(String term:decomposition) {
      if (term.equals(" "))
      term = "SPC";
      String termbaseId = "UNKNOWN";
      if (termbaseIds.lookup(term)) {
      Iterator<String> it = termbaseIds.get(term).iterator();
      if (it.hasNext() )
      termbaseId = it.next(); 
      }
      String termbaseColonTerm = termbaseId+":"+term;
      decompositionWithIds.add(termbaseColonTerm);
      }
      return decompositionWithIds;
      }
 
      public void setTermbase(Map<String,String> termbase) {
      this.termbase = termbase;
      }
 
      public void addTerm(String term,String termbaseid) {
      termbase.put(term.toLowerCase(),term);
      List<String> termbaseids = new LinkedList<String>();
      if (termbaseIds.containsKey(term))
      termbaseids = termbaseIds.get(term);
      termbaseids.add(termbaseid);
      termbaseIds.put(term,termbaseids);
      }
      */
     @Override
     public SortedSet<List<String>> decomposeRanked(String term) {
         // preprocess term
         //term = term.replaceAll("-", " ");
         //term = term.toLowerCase();
         // decompose
         Set<List<String>> decompositions = decompose(term);
         final Iterator<List<String>> decompIter = decompositions.iterator();
         while(decompIter.hasNext()) {
             if(decompIter.next().size() <= 1) {
                 decompIter.remove();
             }
         }
         // add merge plural decompositions
         boolean analyzePlurals = true;
         if (global.getLanguage().equals("de")) {
             if (analyzePlurals) {
                 Set<List<String>> decompositionsWithPlurals = new HashSet<List<String>>();
                 for (List<String> decomposition : decompositions) {
                     String s = decomposition.toString();
                     if (s.contains(", n,")
                             || s.contains(", e,") || s.contains(", e]")
                             || s.contains(", s,") || s.contains(", s]")
                             || s.contains(", en,") || s.contains(", en]")) {
                         s = s.replaceAll(", e,", "e,");
                         s = s.replaceAll(", e]", "e]");
                         s = s.replaceAll(", n,", "n,");
                         s = s.replaceAll(", s,", "s,");
                         s = s.replaceAll(", s]", "s]");
                         s = s.replaceAll(", en,", "en,");
                         s = s.replaceAll(", en]", "en]");
                         s = s.substring(1, s.length() - 1);
                         decompositionsWithPlurals.add(new LinkedList(Arrays.asList(s.split(", "))));
                     }
                 }
                 decompositions.addAll(decompositionsWithPlurals);
             }
         }
         // score decompositions
         final Map<List<String>, Double> scoremap = new HashMap<List<String>, Double>();
         for (List<String> decomposition : decompositions) {
             double cnt = 0;
             for (String subterm : decomposition) {
                 if (subterm.equals(".") || subterm.equals(" ")) {
                     cnt = cnt + 0.1;
                 } else {
                     cnt = cnt + 1;
                 }
             }
             double score = 1 / cnt;
             scoremap.put(recase(decomposition, term), score);
 
         }
         final TreeSet<List<String>> sorted = new TreeSet<List<String>>(new Comparator<List<String>>() {
             @Override
             public int compare(List<String> o1, List<String> o2) {
                 double s1 = scoremap.get(o1);
                 double s2 = scoremap.get(o2);
                 if (s2 > s1) {
                     return +1;
                 } else if (s2 < s1) {
                     return -1;
                 } else {
                     if (o1.size() < o2.size()) {
                         return +1;
                     } else if (o1.size() > o2.size()) {
                         return -1;
                     } else {
                         for (int i = 0; i < o1.size(); i++) {
                             final int c = o1.get(i).compareTo(o2.get(i));
                             if (c != 0) {
                                 return c;
                             }
                         }
                         return 0;
                     }
                 }
             }
         });
         sorted.addAll(scoremap.keySet());
         //if(!sorted.isEmpty()) {
         //	for(String s : sorted.first()) {
         //		System.err.println(s);
         //	}
         //} else {
         //	System.err.println("No decomposition");
         //}
         return sorted;
     }
 
     public List<String> decomposeBest(String term) {
         // decompose ranked
         SortedSet<List<String>> rankedDecompositions = decomposeRanked(term);
 
         if (rankedDecompositions.isEmpty()) {
             return EMPTY_DECOMP;
         }
 
         // sort scores
 //        List<Double> scores = new LinkedList<Double>(rankedDecompositions.keySet());
         //      Collections.sort(scores);
         //    Collections.reverse(scores);
 
         // get best
         //  List<String> bestDecomposition = new LinkedList<String>();
         // if (rankedDecompositions.size() > 0) {
         //    bestDecomposition = rankedDecompositions.get(scores.get(0)).iterator().next();
         //}
 
         return rankedDecompositions.first();
     }
 
     public Set<String> filterTerms(String term) {
 //        String lang = termbase.getLanguage();
 //        // filter local terms
 //        TermbaseImpl termbaseFiltered = new TermbaseImpl(lang);
 //        for (String localTerm : local) {
 //            termbaseFiltered.add(localTerm);
 //        }
 //        // filter termbase terms
 ////int cnt = 0;
 //        for (Object o : termbase) {
 //            String t = (String) o;
 //            if (t == null) {
 //                throw new NullPointerException(termbase.toString() + " returned null");
 //            }
 //            if ((term.contains(t)) && (!term.equals(t))) {
 //                termbaseFiltered.add(t);
 //            }
 ////cnt++;
 //        }
 //        /*
 //         System.out.println("cnt "+cnt);
 //         for(String t:termbaseFiltered) {
 //         System.out.println("  filter: "+t);
 //         }
 //         System.out.println(termbaseFiltered.size());
 //         */
 //        return termbaseFiltered;
         final HashSet<String> terms = new HashSet<String>();
         final StringBuffer termBuf = new StringBuffer(term.toLowerCase());
 
         for (int i = 0; i < termBuf.length() - MIN_TERM_LENGTH; i++) {
             for (int j = i + MIN_TERM_LENGTH; j <= termBuf.length(); j++) {
                 final String subterm = termBuf.substring(i, j);
                 if (global.lookup(subterm)) {
                     terms.add(subterm);
                 }
             }
         }
         return terms;
     }
 
     public Set<List<String>> decompose(String term) {
         //int currentmindecomp = 100;
         //term = term.toLowerCase();
         //Termbase termbaseFiltered = this.global;//filterTerms(this.global, term);
 //System.out.println(term+" -> "+termbaseFiltered);
         //return analyze(term, 0, 0, 0, new LinkedListString>(), new HashSet<List<String>>(), 100, termbaseFiltered, currentmindecomp);
         final Set<String> subterms = filterTerms(term);
         return decompose(term, subterms);
     }
 
     /**
      * Recursively find all subterms
      *
      * @param term
      * @param subterms
      * @return
      */
     private Set<List<String>> decompose(String term, Set<String> subterms) {
         if (term.length() == 0) {
             return Collections.singleton((List<String>) new LinkedList<String>());
         }
         final Set<List<String>> decompositions = new HashSet<List<String>>();
 
         final String lcTerm = term.toLowerCase();
 
         for (String subterm : subterms) {
             if (lcTerm.startsWith(subterm)) {
                 final String subterm2;
                 final String restterm = lcTerm.substring(subterm.length()).replaceAll("^\\s+", "");
                 Set<List<String>> decompsOfRestOfTerm = decompose(restterm, subterms);
                 Matcher ciMatcher = null;
                 // Hack that if we are only two characters away from the end of a word we assume
                 // it is just an inflectional ending and can be ignored
                 if (decompsOfRestOfTerm.isEmpty() && restterm.matches(ONE_OR_TWO_LETTER_TO_WORD_BOUNDARY)) {
                     if (restterm.length() >= 2 && Character.isLetter(restterm.charAt(1))) {
                         decompsOfRestOfTerm = decompose(restterm.substring(2).replaceAll("^\\s+", ""), subterms);
                         subterm2 = subterm + restterm.substring(0, 2);
                     } else {
                         decompsOfRestOfTerm = decompose(restterm.substring(1).replaceAll("^\\s+", ""), subterms);
                         subterm2 = subterm + restterm.substring(0, 1);
                     }
                 } else if(decompsOfRestOfTerm.isEmpty() && (ciMatcher = COMPOUND_INSERTION.matcher(restterm)).matches()) {
                     final String restterm2 = ciMatcher.group(2);
                     decompsOfRestOfTerm = decompose(restterm2, subterms);
                     subterm2 = subterm + ciMatcher.group(1);
                 } else {
                     subterm2 = subterm;
                 }
                 for (List<String> decompOfRestOfTerm : decompsOfRestOfTerm) {
                     decompOfRestOfTerm.add(0, subterm2);
                 }
                 decompositions.addAll(decompsOfRestOfTerm);
             }
         }
         return decompositions;
     }
 
     // This method is awfully slow
 //    private Set<List<String>> analyze(String term, int pos, int poslast, int subtermslength,
 //            List<String> decomp_last, Set<List<String>> decompositions, int minn,
 //            Termbase termbaseLocal, int currentmindecomp) {
 //        pos = pos + 1;
 //        // subterm found
 //        String subterm = term.substring(poslast, pos);
 ////System.out.println(poslast+":"+subterm+" -> "+decomp_last);
 //        if (termbaseLocal.lookup(subterm)) {
 //            List<String> decomp = new LinkedList<String>();
 //            for (String t : decomp_last) {
 //                decomp.add(t);
 //            }
 //            decomp.add(subterm);
 //            int newsubtermslength = subtermslength + subterm.length();
 //            // fork with subterm found
 //            if (pos < term.length()) {
 //                analyze(term, pos, pos, newsubtermslength, decomp, decompositions, minn, termbaseLocal, currentmindecomp);
 //            } else { // last character
 //                if (newsubtermslength == pos) {
 ////System.out.println("MATCH -> "+decomp+" "+decomp.size()+" "+currentmindecomp);
 //                    if (decomp.size() <= currentmindecomp) {
 //                        minn = decomp.size();
 //                        currentmindecomp = decomp.size();
 //                        decompositions.add(decomp);
 //                    }
 //                }
 //            }
 //        }
 //        // fork with next character
 //        if (pos < term.length()) {
 //            analyze(term, pos, poslast, subtermslength, decomp_last, decompositions, minn, termbaseLocal, currentmindecomp);
 //        }
 //        return decompositions;
 //    }
 //    public static void main(String[] args) throws IOException {
 //        String term = "intangible fixed assets";
 //        TermbaseImpl termbase = new TermbaseImpl("en");
 //        termbase.add("fixed");
 //        termbase.add("fixed assets");
 //        termbase.add("intangible");
 //        termbase.add("assets");
 //        termbase.add("asset");
 //        termbase.add(" ");
 //        MinimumSubtermDecomposer decomposer = new MinimumSubtermDecomposer(termbase);
 //        System.out.println(decomposer.decomposeBest(term));
 //    }
     public static void main(String[] args) throws Exception {
         if(args.length < 3) {
             throw new IllegalArgumentException();
         }
         final File termBaseFile = new File(args[0]);
         if(!termBaseFile.exists()) {
             throw new IllegalArgumentException();
         }
         final Language lang = Language.get(args[1]);
         final TermbaseImpl termBase = TermbaseImpl.fromFile(termBaseFile, lang.toString());
         final MinimumSubtermDecomposer decomposer = new MinimumSubtermDecomposer(termBase);
         final StringBuilder sb = new StringBuilder();
         sb.append(args[2]);
         for(int i = 3; i < args.length; i++) {
             sb.append(" ").append(args[i]);
         }        
         final SortedSet<List<String>> results = decomposer.decomposeRanked(sb.toString());
         for(List<String> result : results) {
             for(String word : result) {
                 System.out.print(word + " ");
             }
             System.out.println();
         }
     }
 
     public static List<String> recase(List<String> decomposition, String term) {
         if (term.matches("[\\p{Lu}[^\\p{L}]]+")) {
             final ArrayList<String> recased = new ArrayList<String>();
             for (String s : decomposition) {
                 recased.add(s.toUpperCase());
             }
             return recased;
         } else if (term.matches("[\\p{Ll}[^\\p{L}]]+")) {
             final ArrayList<String> recased = new ArrayList<String>();
             for (String s : decomposition) {
                 recased.add(s.toLowerCase());
             }
             return recased;
         } else if (term.matches("[^\\p{L}]*[\\p{Lu}].*")) {
             final ArrayList<String> recased = new ArrayList<String>();
             for (String s : decomposition) {
                 for (int i = 0; i < s.length(); i++) {
                     if (Character.isLetter(s.charAt(i))) {
                         s = s.replaceFirst("" + s.charAt(i), "" + Character.toUpperCase(s.charAt(i)));
                         break;
                     }
                 }
                 recased.add(s);
             }
             return recased;
         } else {
             return decomposition;
         }
     }
 }
