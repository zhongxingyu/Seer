 package pt.ua.tm.trigner.model;
 
 import cc.mallet.pipe.Pipe;
 import cc.mallet.pipe.PrintTokenSequenceFeatures;
 import cc.mallet.pipe.SerialPipes;
 import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
 import cc.mallet.pipe.tsf.*;
 import pt.ua.tm.gimli.features.mallet.*;
 import pt.ua.tm.trigner.model.configuration.ModelConfiguration;
 import pt.ua.tm.trigner.model.features.NGramsUtil;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: david
  * Date: 19/03/13
  * Time: 13:59
  * To change this template use File | Settings | File Templates.
  */
 public class ModelFeaturePipeline {
 
     private static final String CAPS = "[A-Z]";
     private static final String LOW = "[a-z]";
 
     public static Pipe get(final ModelConfiguration mc, final String dictionaryPath) {
         ArrayList<Pipe> pipe = new ArrayList<>();
 
         // Input parsing
         pipe.add(new pt.ua.tm.trigner.model.pipe.Input2TokenSequence(mc));
 
         // Capitalization
         if (mc.isProperty("capitalization")) {
             pipe.add(new RegexMatches("InitCap", Pattern.compile(CAPS + ".*")));
             pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + CAPS)));
             pipe.add(new RegexMatches("AllCaps", Pattern.compile(CAPS + "+")));
             pipe.add(new RegexMatches("Lowercase", Pattern.compile(LOW + "+")));
             pipe.add(new MixCase());
             pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));
         }
 
         // Counting
         if (mc.isProperty("counting")) {
             pipe.add(new NumberOfCap());
             pipe.add(new NumberOfDigit());
             pipe.add(new WordLength());
         }
 
         // Symbols
         if (mc.isProperty("symbols")) {
             pipe.add(new RegexMatches("Hyphen", Pattern.compile(".*[-].*")));
             pipe.add(new RegexMatches("BackSlash", Pattern.compile(".*[/].*")));
             pipe.add(new RegexMatches("OpenSquare", Pattern.compile(".*[\\[].*")));
             pipe.add(new RegexMatches("CloseSquare", Pattern.compile(".*[\\]].*")));
             pipe.add(new RegexMatches("Colon", Pattern.compile(".*[:].*")));
             pipe.add(new RegexMatches("SemiColon", Pattern.compile(".*[;].*")));
             pipe.add(new RegexMatches("Percent", Pattern.compile(".*[%].*")));
             pipe.add(new RegexMatches("OpenParen", Pattern.compile(".*[(].*")));
             pipe.add(new RegexMatches("CloseParen", Pattern.compile(".*[)].*")));
             pipe.add(new RegexMatches("Comma", Pattern.compile(".*[,].*")));
             pipe.add(new RegexMatches("Dot", Pattern.compile(".*[\\.].*")));
             pipe.add(new RegexMatches("Apostrophe", Pattern.compile(".*['].*")));
             pipe.add(new RegexMatches("QuotationMark", Pattern.compile(".*[\"].*")));
             pipe.add(new RegexMatches("Star", Pattern.compile(".*[*].*")));
             pipe.add(new RegexMatches("Equal", Pattern.compile(".*[=].*")));
             pipe.add(new RegexMatches("Plus", Pattern.compile(".*[+].*")));
         }
 
 
         // Char n-gram
         if (mc.isProperty("char_ngrams")) {
             int[] ngrams = NGramsUtil.fromString(mc.getProperty("char_ngrams_sizes"));
             pipe.add(new TokenTextCharNGrams("CHARNGRAM=", ngrams));
         }
 
         // Suffixes
         if (mc.isProperty("suffix")) {
             int[] ngrams = NGramsUtil.fromString(mc.getProperty("suffix_sizes"));
             for (int ngram : ngrams) {
                 pipe.add(new TokenTextCharSuffix(ngram + "SUFFIX=", ngram));
             }
         }
 
         // Prefixes
         if (mc.isProperty("prefix")) {
             int[] ngrams = NGramsUtil.fromString(mc.getProperty("prefix_sizes"));
             for (int ngram : ngrams) {
                pipe.add(new TokenTextCharPrefix(ngram + "PREFIX=", ngram));
             }
 
         }
 
         // Word shape
         if (mc.isProperty("word_shape")) {
             pipe.add(new WordShape());
         }
 
         if (mc.isProperty("triggers")) {
             File file = new File(dictionaryPath);
             try {
                 pipe.add(new TrieLexiconMembership("TRIGGER", file, true));
             } catch (FileNotFoundException ex) {
                 throw new RuntimeException("There was a problem reading the dictionary for triggers matching: " + file.getName(), ex);
             }
         }
 
         ModelConfiguration.ContextType context = ModelConfiguration.ContextType.valueOf(mc.getProperty("context"));
         switch (context) {
             case NONE:
                 break;
             case WINDOW:
 //                pipe.add(new FeaturesInWindow("WINDOW=", -3, 3));
                 pipe.add(new FeaturesInWindow("WINDOW=", -1, 0, Pattern.compile("[WORD|LEMMA|POS|CHUNK]=.*"), true));
                 pipe.add(new FeaturesInWindow("WINDOW=", -2, -1, Pattern.compile("[WORD|LEMMA|POS|CHUNK]=.*"), true));
                 pipe.add(new FeaturesInWindow("WINDOW=", 0, 1, Pattern.compile("[WORD|LEMMA|POS|CHUNK]=.*"), true));
                 pipe.add(new FeaturesInWindow("WINDOW=", -1, 1, Pattern.compile("[WORD|LEMMA|POS|CHUNK]=.*"), true));
                 pipe.add(new FeaturesInWindow("WINDOW=", -3, -1, Pattern.compile("[WORD|LEMMA|POS|CHUNK]=.*"), true));
 
                 break;
             case CONJUNCTIONS:
                 pipe.add(new OffsetConjunctions(true, Pattern.compile("WORD=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                 pipe.add(new OffsetConjunctions(true, Pattern.compile("LEMMA=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                 pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                 break;
         }
 
         // Print
 //        pipe.add(new PrintTokenSequenceFeatures());
 
         pipe.add(new TokenSequence2FeatureVectorSequence(true, true));
 
         return new SerialPipes(pipe);
     }
 }
