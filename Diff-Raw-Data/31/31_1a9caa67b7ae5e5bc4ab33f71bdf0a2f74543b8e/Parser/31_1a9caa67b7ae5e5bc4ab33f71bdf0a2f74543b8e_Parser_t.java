 package org.andrew.nlp.parser;
 
 public interface Parser
 {
 	public String[] parse(String text);
	public String[] parseSentences(String text);
	public String[] parseTokens(String text);
 	
 	public static class Builder
 	{
 
 		protected SentenceParserAdapter sentenceParser = null;
 		protected TokenizerParserAdapter tokenizerParser = null;
 
 		public Builder addSentenceParser(String modelFileName)
 		{
 			this.sentenceParser = new SentenceParserAdapter(modelFileName);
 			return this;
 		}
 
 		public Builder addTokenizerParser(String modelFileName)
 		{
 			this.tokenizerParser = new TokenizerParserAdapter(modelFileName);
 			return this;
 		}
 
 		public ParserImpl build()
 		{
 			return new ParserImpl(this);
 		}
 
 	}
 }
