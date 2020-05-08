 package com.blogspot.ostas.dbpojo.utils;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.annotation.PostConstruct;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import static com.google.common.base.CaseFormat.*;
 
 public class NameConversionHelperImpl implements NameConversionHelper {
    static Logger logger = Logger.getLogger(NameConversionHelperImpl.class);
     private Set<String> keywords;
     private String keywordsFile;
 
     public String getKeywordsFile() {
         return keywordsFile;
     }
 
     public void setKeywordsFile(String keywordsFile) {
         this.keywordsFile = keywordsFile;
     }
 
     @Override
     public String tableNameToClassName(String tableName){
         String preResult = tableName.toLowerCase();
         if(preResult.indexOf('$')!=-1){
             preResult = StringUtils.replace(preResult, "$", "_");
         }
         return LOWER_UNDERSCORE.to(UPPER_CAMEL,preResult);
     }
     @Override
     public String columnNameToIdentifier(String columnName)
     {
         String preResult = columnName.toLowerCase();
         if(preResult.indexOf('$')!=-1){
             preResult = StringUtils.replace(preResult, "$", "_");
         }
         preResult = StringUtils.remove(preResult, "#");
         preResult = LOWER_UNDERSCORE.to(LOWER_CAMEL,preResult);
         if(isKeyword(preResult)){
            preResult = nornalizeField(preResult);
         }
         return preResult;
     }
     @Override
    public String nornalizeField(String nameCandidate){
         return "changeMe___"+nameCandidate;
     }
 
     @PostConstruct
     private Set<String> loadKeyWords()
     {
         keywords = new HashSet<String>();
        BufferedReader in = null;
         try {
             in = new BufferedReader(new FileReader(keywordsFile));
             String keyword;
             while ((keyword = in.readLine()) != null){
                 keywords.add(keyword);
             }
             in.close();
         } catch (IOException e) {
             logger.error(e);
         }
         return keywords;
     }
     private boolean isKeyword(String fieldName){
        if(keywords.contains(fieldName)) return true;
        return false;
    };
 }
