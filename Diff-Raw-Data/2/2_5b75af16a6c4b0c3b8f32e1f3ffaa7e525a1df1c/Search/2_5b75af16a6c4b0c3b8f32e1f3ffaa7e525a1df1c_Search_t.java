 package ru.chuprikov.search.search;
 
 import ru.chuprikov.search.database.*;
 import ru.chuprikov.search.datatypes.*;
 import ru.chuprikov.search.search.joiners.Joiners;
 import ru.chuprikov.search.search.tokens.TokenKind;
 import ru.chuprikov.search.search.tokens.Tokenizer;
 import ru.kirillova.search.normspellcorr.Kgramm;
 import ru.kirillova.search.normspellcorr.Normalize;
 import ru.kirillova.search.normspellcorr.Suggestion;
 
 import java.util.*;
 
 public class Search implements AutoCloseable {
     private final IndexDB indexDB;
     private final TermDB termDB;
     private final DocumentDB documentDB;
     private final BigrammDB bigrammDB;
     private final Kgramm kgramm;
 
     public Search(SearchDatabase searchDB) throws Exception {
         this.documentDB = searchDB.openDocumentDB();
         this.indexDB = searchDB.openIndexDB(0);
         this.termDB = searchDB.openTermDB();
         this.bigrammDB = searchDB.openBigrammDB();
         this.kgramm = new Kgramm(termDB, bigrammDB);
     }
 
     private Iterator<PostingInfo> parseE(Tokenizer tokenizer, List<CloseableIterator<Datatypes.Posting>> openedIterators, Suggestions sugest) throws Exception {
         Iterator<PostingInfo> cur = parseN(tokenizer, openedIterators, sugest);
         while (tokenizer.currentToken().kind() == TokenKind.PROXIMITY) {
             int proximity = tokenizer.currentToken().getIntegerValue();
             sugest.appendToken(" /" + proximity + " ");
             tokenizer.readNextToken();
             Iterator<PostingInfo> next = parseN(tokenizer, openedIterators, sugest);
             cur = new Intersector(cur, next, Joiners.getProximityJoiner(proximity));
         }
         return cur;
     }
 
     private Iterator<PostingInfo> getWildcardIterator(String token, List<CloseableIterator<Datatypes.Posting>> openedIterators, Suggestions sugest) throws Exception {
         final String stemmedToken = Normalize.getBasisWord(token);
         final String suffix = token.substring(stemmedToken.length(), token.length());
 
         Set<String> kgramms = new HashSet<>();
         String[] bits = stemmedToken.split("\\*");
         if (bits[0].length() >= 2)
             kgramms.add("$" + bits[0].substring(0, 2));
        if (!stemmedToken.endsWith("*") && bits[bits.length - 1].length() >= 2)
             kgramms.add(bits[bits.length - 1].substring(bits[bits.length - 1].length() - 2, bits[bits.length - 1].length()) + "$");
         for (String bit : bits) {
             if (bit.length() == 2)
                 kgramms.add(bit);
             else for (int i = 0; i < bit.length() - 2; i++)
                 kgramms.add(bit.substring(i, i + 3));
         }
 
         Map<Long, Integer> termHits = new HashMap<>();
         for (String kg : kgramms) {
             for (BigrammUnit bu : bigrammDB.get(kg)) {
                 if (!termHits.containsKey(bu.getTermID()))
                     termHits.put(bu.getTermID(), 0);
                 termHits.put(bu.getTermID(), termHits.get(bu.getTermID()) + 1);
             }
         }
 
         List<Term> candidates = new ArrayList<>();
         for (Long termID : termHits.keySet()) {
             if (termHits.get(termID) == kgramms.size()) {
                 candidates.add(termDB.get(termID));
             }
         }
 
         List<Iterator<PostingInfo>> options = new ArrayList<>();
 
         sugest.appendToken("{");
         for (Term term : candidates) {
             if (term.getTerm().matches(stemmedToken.replaceAll("\\*", ".*"))) {
                 options.add(getTermIterator(term, openedIterators));
                 sugest.appendToken(term.getTerm() + suffix + " ");
             }
         }
         sugest.appendToken("}");
 
         return new Merger(options);
     }
 
     private Iterator<PostingInfo> getTermIterator(Term term, List<CloseableIterator<Datatypes.Posting>> openedIterators) throws Exception {
         final CloseableIterator<Datatypes.Posting> termPostingsIterator = indexDB.iterator(term.getTermID());
         openedIterators.add(termPostingsIterator);
         return new PostingsAdapter(termPostingsIterator);
     }
 
     private Iterator<PostingInfo> getPostingsIterator(String token, List<CloseableIterator<Datatypes.Posting>> openedIterators, Suggestions sugest) throws Exception {
         if (token.contains("*")) {
             return getWildcardIterator(token, openedIterators, sugest);
         } else {
             final String termStr = Normalize.getBasisWord(token);
             Term term = termDB.get(termStr);
 
             if (term == null) {
                 List<Suggestion> suggestions = kgramm.fixMistake(token);
                 String tokenReplacement = suggestions.get(0).getWord();
                 term = termDB.get(Normalize.getBasisWord(tokenReplacement));
 
                 sugest.appendSuggestions(suggestions);
             } else
                 sugest.appendToken(token);
 
             return getTermIterator(term, openedIterators);
         }
     }
 
     private Iterator<PostingInfo> parseN(Tokenizer tokenizer, List<CloseableIterator<Datatypes.Posting>> openedIterators, Suggestions sugest) throws Exception {
         Iterator<PostingInfo> result;
         if (tokenizer.currentToken().kind() == TokenKind.CITE) {
             sugest.appendToken("\"");
             tokenizer.readNextToken();
 
             result = getPostingsIterator(tokenizer.currentToken().getStringValue(), openedIterators, sugest);
             tokenizer.readNextToken();
 
             while (tokenizer.currentToken().kind() != TokenKind.CITE) {
                 sugest.appendToken(" ");
                 result = new Intersector(result, getPostingsIterator(tokenizer.currentToken().getStringValue(), openedIterators, sugest), Joiners.getPhraseJoiner());
                 tokenizer.readNextToken();
             }
 
             sugest.appendToken("\"");
             tokenizer.readNextToken();
         } else {
             result = getPostingsIterator(tokenizer.currentToken().getStringValue(), openedIterators, sugest);
             tokenizer.readNextToken();
         }
         return result;
     }
 
     private Iterator<PostingInfo> parseC(Tokenizer tokenizer, List<CloseableIterator<Datatypes.Posting>> openedIterators, Suggestions sugest) throws Exception {
         Iterator<PostingInfo> cur = parseE(tokenizer, openedIterators, sugest);
         while (tokenizer.currentToken().kind() != TokenKind.EOF) {
             sugest.appendToken(" ");
             cur = new Intersector(cur, parseE(tokenizer, openedIterators, sugest), Joiners.getOrJoiner());
         }
 
         return cur;
     }
 
     public SearchResponse searchAndGetDocIDs(String request, int limit) throws Exception {
         ArrayList<CloseableIterator<Datatypes.Posting>> openedIterators = new ArrayList<>();
         try {
             long startTime = System.currentTimeMillis();
 
             Suggestions sugest = new Suggestions();
             Iterator<PostingInfo> it = parseC(new Tokenizer(request), openedIterators, sugest);
             Set<Long> duplicateEliminator = new HashSet<>();
 
             while (it.hasNext() && duplicateEliminator.size() < limit)
                 duplicateEliminator.add(it.next().getDocumentID());
 
             Document[] found = new Document[duplicateEliminator.size()];
             int writeIdx = 0;
             for (long documentID : duplicateEliminator)
                 found[writeIdx++] = documentDB.get(documentID);
 
             return new SearchResponse(System.currentTimeMillis() - startTime, sugest.getSuggestions(5), found);
         } finally {
             for (CloseableIterator<Datatypes.Posting> cit : openedIterators)
                 cit.close();
         }
     }
 
     @Override
     public void close() throws Exception {
         documentDB.close();
         indexDB.close();
         bigrammDB.close();
         termDB.close();
     }
 }
