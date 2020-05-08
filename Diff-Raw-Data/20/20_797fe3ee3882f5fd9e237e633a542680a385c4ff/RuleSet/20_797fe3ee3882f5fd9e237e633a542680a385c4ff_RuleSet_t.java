 /*
  * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
  *
  * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
  * In most instances, the license terms are contained in a file named license.txt.
  */
 package org.fabrican.extension.variable.provider.rules;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.StringTokenizer;
 
 public class RuleSet implements Cloneable{
     private String name;
     private String desc;
     private boolean matchCase;
     private List<Rule> rules;
     private Integer version;
     
     public Integer getVersion(){
         return version;
     }
     public void setVersion(int ver){
         this.version = ver;
     }
     public String getName() {
         return name;
     }
     public void setName(String name) {
         this.name = name;
     }
     public String getDesc() {
         return desc;
     }
     public void setDesc(String desc) {
         this.desc = desc;
     }
     public boolean isMatchCase() {
         return matchCase;
     }
     public void setMatchCase(boolean matchCase) {
         this.matchCase = matchCase;
     }
     public List<Rule> getRules() {
         return rules;
     }
     public void setRules(List<Rule> rules) {
         this.rules = rules;
     }
     
     public HashMap<String, String> evaluate(String pKey, String sKey) {
        HashMap<String, String> result = new HashMap<>();
        if ( pKey == null || sKey == null ){
            return result;
        }
         HashMap<String, Rule > staging = new HashMap<>();
         for (Rule r : rules ){
             if ( r.evaluate(pKey, sKey, !isMatchCase()) ){
                 Rule rOld = staging.get(r.getVName());
                 if ( rOld == null || r.compareTo(rOld) > 0){
                     staging.put(r.getVName(), r);
                 }
             }
         }
         for (Entry<String, Rule> e : staging.entrySet()){
             result.put(e.getKey(), e.getValue().getVValue());
         }
         return result;
     }
     
     private static final char SEP='\u0001';
     private static final String SEP_STR="\u0001";
     public void setRules(byte[][] bAryAry) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         for ( byte[] b : bAryAry ){
             if ( b != null ){
                 baos.write(b);
             }
         }
         String rulesStr = new String(baos.toByteArray(), "utf-8");
         List<Rule> rst = new LinkedList<Rule>();
         StringTokenizer st = new StringTokenizer(rulesStr, SEP_STR );
         while (st.hasMoreTokens() ){
             Rule r = new Rule();
             r.setPKey(st.nextToken());
             r.setSKey(st.nextToken());
             r.setVName(st.nextToken());
             r.setVValue(st.nextToken());
             rst.add(r);
         }
         setRules(rst);
     }
     
     public byte[][] encodeRules(int chunks, int chunk_size) {
         StringBuffer sb = new StringBuffer();
         if ( getRules() != null ){
             boolean started = false;
             for ( Rule r : getRules() ){
                 String s1 = r.getPKey();
                 String s2 = r.getSKey();
                 String s3 = r.getVName();
                 String s4 = r.getVValue();
                 if ( started ){
                     sb.append(SEP);
                 } else {
                     started = true;
                 }
                 if ( s1 == null ){
                     s1 = "";
                 }
                 sb.append(s1);
                 sb.append(SEP);
                 if ( s2 == null ){
                     s2 = "";
                 }
                 sb.append(s2);
                 sb.append(SEP);
                 if ( s3 == null ){
                     s3 = "";
                 }
                 sb.append(s3);
                 sb.append(SEP);
                 if ( s4 == null ){
                     s4 = "";
                 }
                 sb.append(s4);                
             }
         }
         try {
             byte[] bytes = sb.toString().getBytes("utf-8");
             if( bytes.length > chunks * chunk_size){
                 throw new RuntimeException("Rule lines can't fit into database, the maximum size in bytes is " + chunks * chunk_size);
             }
             byte[][] rst = new byte[chunks][];
             for ( int i = 0; i < chunks; i++ ){
                 int start = i * chunk_size;
                 int len = start > bytes.length ? 0 : (bytes.length - start) % chunk_size;
                 if ( len == 0 ){
                     rst[i] = new byte[0];
                 } else {
                     rst[i] = new byte[len];
                     System.arraycopy(bytes, start, rst[i], 0, len);
                 }
             }
             return rst;
         } catch (UnsupportedEncodingException e) {
             //"utf-8" is always supported.
             return null;
         }
     }
     
     /** used by cache */
     public RuleSet deepCopy(){
         RuleSet rst = new RuleSet();
         rst.setName(getName());
         rst.setDesc(getDesc());
         rst.setMatchCase(isMatchCase());
         rst.setVersion(getVersion());
         if ( getRules() != null ){
             List<Rule> rl = new LinkedList<Rule>();
             for (Rule rule : getRules() ){
                 rl.add(rule.deepCopy());
             }
             rst.setRules(rl);
         } 
         return rst;
     }
     /** used by cache */
     public void shallowCopy(RuleSet rs){
         rs.setName(getName());
         rs.setDesc(getDesc());
         rs.setVersion(getVersion());
         rs.setRules((List<Rule>)null);
     }
     
 }
