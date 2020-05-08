 /**
  * ExternalAttributedTokensAnalyzer
  * Copyright 2012 Ippei Ukai
  */
 package com.github.ippeiukai.externaltoken.solr;
 
 import java.io.Reader;
 
 import org.apache.lucene.analysis.Tokenizer;
 import org.apache.solr.analysis.BaseTokenizerFactory;
 import org.apache.solr.analysis.TokenizerFactory;
 
 import com.github.ippeiukai.externaltoken.factorycore.ParameterProvider;
 
 class TokenizerFactoryParameterAdapter extends BaseTokenizerFactory implements ParameterProvider  {
   
   TokenizerFactoryParameterAdapter(TokenizerFactory tf){
     init(tf.getArgs());
   }
   
   @Override
   public String get(String parameterKey) {
    return getArgs().get(parameterKey);
   }
   
   @Override
   public int getInt(String parameterKey, int defaultValue) {
    return super.getInt(parameterKey, defaultValue);
   }
   
   @Override
   public boolean getBoolean(String parameterKey, boolean defaultValue) {
    return super.getBoolean(parameterKey, defaultValue);
   }
 
   @Override
   public Tokenizer create(Reader arg0) {
     throw new RuntimeException("not implemented");
   }
   
 }
