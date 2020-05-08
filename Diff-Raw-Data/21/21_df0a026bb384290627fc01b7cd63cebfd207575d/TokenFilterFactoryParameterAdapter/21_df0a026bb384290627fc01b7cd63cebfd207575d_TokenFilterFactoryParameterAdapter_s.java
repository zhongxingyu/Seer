 /**
  * ExternalAttributedTokensAnalyzer
  * Copyright 2012 Ippei Ukai
  */
 package com.github.ippeiukai.externaltoken.solr;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.solr.analysis.BaseTokenFilterFactory;
 import org.apache.solr.analysis.TokenFilterFactory;
 
 import com.github.ippeiukai.externaltoken.factorycore.ParameterProvider;
 
 class TokenFilterFactoryParameterAdapter extends BaseTokenFilterFactory
     implements ParameterProvider {
   
   TokenFilterFactoryParameterAdapter(TokenFilterFactory tf) {
     init(tf.getArgs());
   }
   
   @Override
   public String get(String parameterKey) {
    return get(parameterKey);
   }
   
   @Override
   public int getInt(String parameterKey, int defaultValue) {
    return getInt(parameterKey, defaultValue);
   }
   
   @Override
   public boolean getBoolean(String parameterKey, boolean defaultValue) {
    return getBoolean(parameterKey, defaultValue);
   }
   
   @Override
   public TokenStream create(TokenStream input) {
     throw new RuntimeException("not implemented");
   }
   
 }
