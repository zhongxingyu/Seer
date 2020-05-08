 package de.spektrumprojekt.informationextraction.extractors;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.Bag;
 import org.apache.commons.collections.bag.HashBag;
 import org.apache.commons.lang3.StringUtils;
 
 import de.spektrumprojekt.commons.chain.Command;
 import de.spektrumprojekt.datamodel.message.MessageGroup;
 import de.spektrumprojekt.datamodel.message.ScoredTerm;
 import de.spektrumprojekt.datamodel.message.Term;
 import de.spektrumprojekt.informationextraction.InformationExtractionContext;
 
 public class NGramsExtractorCommand implements Command<InformationExtractionContext> {
 
     /** Allowed characters for a token, everything else will be filtered out. */
     private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9\\.]+");
 
     private static final String LANGUAGE_EN = "en";
     private static final String LANGUAGE_DE = "de";
 
     private final int nGrams;
     private final boolean useMessageGroupIdForToken;
     private final boolean assertMessageGroup;
     private final int minimumTermLength;
 
     public NGramsExtractorCommand(boolean useMessageGroupIdForToken, boolean assertMessageGroup,
             int nGrams, int minimumTermLength) {
         this.useMessageGroupIdForToken = useMessageGroupIdForToken;
         this.assertMessageGroup = assertMessageGroup;
         this.nGrams = nGrams;
         this.minimumTermLength = minimumTermLength;
     }
 
     private List<String> cleanTokens(List<String> tokens) {
         List<String> cleanTokens = new ArrayList<String>();
         for (String token : tokens) {
             if (token.length() < 2) {
                 continue;
             }
             cleanTokens.add(token);
         }
         return cleanTokens;
     }
 
     @Override
     public String getConfigurationDescription() {
         return this.getClass().getSimpleName()
                 + " nGrams: " + nGrams
                 + " useMessageGroupIdForToken: " + useMessageGroupIdForToken
                 + " minimumTermLength: " + minimumTermLength
                 + " minimumTermLength: " + minimumTermLength;
 
     }
 
     @Override
     public void process(InformationExtractionContext context) {
 
         String text = context.getCleanText();
         if (StringUtils.isEmpty(text)) {
             return;
         }
 
         String language = LanguageDetectorCommand.getAnnotatedLanguage(context.getMessage());
 
         List<String> tokens = ExtractionUtils.tokenize(text);
         tokens = removeStopwords(language, tokens);
         tokens = ExtractionUtils.createNGrams(tokens, nGrams);
         tokens = cleanTokens(tokens);
 
         Bag tokenBag = new HashBag(tokens);
         String tokenPrefix = StringUtils.EMPTY;
         if (this.useMessageGroupIdForToken) {
             MessageGroup group = context.getMessage().getMessageGroup();
             if (group == null) {
                 if (this.assertMessageGroup) {
                     throw new IllegalStateException("messagegroup not set for message="
                             + context.getMessage());
                 }
             } else {
                 tokenPrefix = group.getId() + "#";
             }
         }
         int highestCount = BagHelper.getHighestCount(tokenBag);
         for (Object tokenObj : tokenBag) {
             String token = (String) tokenObj;
             if (token.length() < minimumTermLength) {
                 continue;
             }
             float frequency = (float) tokenBag.getCount(token) / highestCount;
             token = tokenPrefix + token;
             context.getMessagePart().addScoredTerm(
                     new ScoredTerm(context.getPersistence().getOrCreateTerm(
                             Term.TermCategory.TERM,
                             token),
                             frequency));
         }
     }
 
     private List<String> removeStopwords(String language, List<String> tokens) {
         Set<String> stopwords;
         if (language.equals(LANGUAGE_DE)) {
             stopwords = ExtractionUtils.STOPWORDS_DE;
         } else if (language.equals(LANGUAGE_EN)) {
             stopwords = ExtractionUtils.STOPWORDS_EN;
         } else {
             return tokens;
         }
         List<String> ret = new ArrayList<String>();
         for (String token : tokens) {
             if (!stopwords.contains(token.toLowerCase())) {
                 ret.add(token);
             }
         }
         return ret;
     }
 
 }
