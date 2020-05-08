 package br.usp.ime.choreos.vv;
 
 import java.util.HashMap;
 import java.util.List;
 
 public interface ResponseInterface {
         
         public String getContent();
         public Integer  getContentAsInt();
         public Double  getContentAsDouble();
         public ResponseItem getChild(String name) throws NoSuchFieldException;
         public List<ResponseItem> getChildAsList(String name) throws NoSuchFieldException;
         public void addChild(ResponseItem item);
         public void setContent(String content);
         public HashMap<String, String> getTagAttributes();

 }
