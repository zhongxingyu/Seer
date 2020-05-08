 package semanticMarkup.ling.learn.knowledge;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import semanticMarkup.ling.learn.dataholder.DataHolder;
 import semanticMarkup.ling.learn.dataholder.SentenceStructure;
 import semanticMarkup.ling.learn.utility.LearnerUtility;
 import semanticMarkup.ling.learn.utility.StringUtility;
 
 /**
  * Tag remaining sentences whose tag is null
  * 
  * @author Dongye
  * 
  */
 public class NullSentenceTagger implements IModule {
 	private LearnerUtility myLearnerUtility;
 	private String defaultGeneralTag;
 
	public NullSentenceTagger(String dGTag) {
 		this.defaultGeneralTag = dGTag;
 	}
 
 	@Override
 	public void run(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.remainNullTag");
 
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String tag = sentenceItem.getTag();
 			String source = sentenceItem.getSource();
 			boolean c1 = (tag == null);
 			boolean c2 = (StringUtils.equals(tag, ""));
 			boolean c3 = (StringUtils.equals(tag, "ditto"));
 			boolean c4 = (StringUtils.equals(tag, "unknown"));
 			boolean c5 = StringUtility.isMatchedNullSafe(source, "-0$");
 
 			if ((c1 || c2 || c3 || c4) && c5) {
 				sentenceItem.setModifier("");
 				sentenceItem.setTag(this.defaultGeneralTag);
 				myLogger.debug(String.format("mark [%d] <general>: %s",
 						sentenceItem.getID(), sentenceItem.getSentence()));
 			}
 		}
 
 		String nPhrasePattern = "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+";
 		String mPhrasePattern = "(?:<[A-Z]*M[A-Z]*>[^<]+?<\\/[A-Z]*M[A-Z]*>\\s*)+";
 
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			// String tag = sentenceItem.getTag();
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String sentenceCopy = "" + sentenceItem.getSentence();
 			sentenceCopy = sentenceCopy.replaceAll("></?", "");
 			if (!StringUtility.isMatchedNullSafe(sentenceCopy, "<[NO]>")) {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
 						"ditto", "remainnulltag-[R3]");
 			} else {
 				if (sentenceCopy != null) {
 					Matcher m2 = StringUtility.createMatcher(sentenceCopy,
 							"(.*?)(" + nPhrasePattern + ")");
 					if (m2.find()) {
 						String head = m2.group(1);
 						String tagPhrase = m2.group(2);
 						tagPhrase = StringUtility.trimString(tagPhrase);
 						if (StringUtility.isMatchedNullSafe(head, "\\b("
 								+ this.myLearnerUtility.getConstant().PREPOSITION + ")\\b")) {
 							dataholderHandler.tagSentenceWithMT(sentenceID,
 									sentence, "", "ditto",
 									"remainnulltag-[R3:ditto]");
 						} else {
 							String[] words = tagPhrase.split("\\s+");
 							String tagX = words[words.length - 1];
 							List<String> wordList = new ArrayList<String>();
 							wordList.addAll(Arrays.asList(words));
 							String modifier = StringUtils.join(
 									wordList.subList(0, wordList.size() - 1),
 									" ");
 							if (head != null) {
 								Matcher m22 = StringUtility.createMatcher(head,
 										"([^,]+)$");
 								if (m22.find()) {
 									modifier = m22.group(1) + " " + modifier;
 								}
 								tagX = tagX.replaceAll("<\\S+?>", "");
 								modifier = modifier.replaceAll("<\\S+?>", "");
 								tagX = StringUtility.trimString(tagX);
 								dataholderHandler.tagSentenceWithMT(sentenceID,
 										sentence, modifier, tagX,
 										"remainnulltag-[R3:m-t]");
 							}
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 }
