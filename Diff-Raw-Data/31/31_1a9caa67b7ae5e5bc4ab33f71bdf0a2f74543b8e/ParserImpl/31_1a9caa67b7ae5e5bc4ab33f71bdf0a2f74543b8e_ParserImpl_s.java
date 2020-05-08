 package org.andrew.nlp.parser;
 
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.google.common.collect.Lists;
 
 public class ParserImpl implements Parser
 {
 
 	private SentenceParserAdapter sentenceParser = null;
 	private TokenizerParserAdapter tokenizerParser = null;
 
 	public ParserImpl(Parser.Builder builder)
 	{
 		this.sentenceParser = builder.sentenceParser;
 		this.tokenizerParser = builder.tokenizerParser;
 	}
 
 	public String[] parse(String text)
 	{
 
 		String[] parsedSentences = null;
 		parsedSentences = sentenceDetect(text);
 		parsedSentences = tokenize(parsedSentences);
 		return parsedSentences;
 	}
 
 	private String[] tokenize(String[] sentenceDetectedSentences)
 	{
 		List<String> sentences = Lists.newArrayList();
 		if (tokenizerParser != null)
 		{
 			for (String sentence : sentenceDetectedSentences)
 			{
 				String[] tokens = tokenizerParser.parse(sentence);
 				sentences.add(StringUtils.join(tokens, " "));
 			}
 		}
 		if (!sentences.isEmpty())
 		{
 			return sentences.toArray(new String[sentences.size()]);
 		}
 		else
 		{
 			return sentenceDetectedSentences;
 		}
 	}
 
 	private String[] sentenceDetect(String text)
 	{
 		if (sentenceParser != null)
 		{
 			return sentenceParser.parse(text);
 		}
 		return null;
 	}
 
 	
 
 }
