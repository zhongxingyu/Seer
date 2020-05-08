 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.config;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 /**
  * コメントを壊さずに、Yamlに情報を設定するためのユーティリティクラス
  * @author ucchy
  */
 public class YamlSetter {
     
     private ArrayList<String> contents;
     private String filename;
     
     /**
      * コンストラクタ。
      * @param filename Yamlファイル名
      * @throws UnsupportedEncodingException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public YamlSetter(String filename) 
             throws UnsupportedEncodingException, 
             FileNotFoundException, IOException {
         
         this.filename = filename;
         
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(filename));
             contents = new ArrayList<String>();
             while (reader.ready()) {
                 contents.add(reader.readLine());
             }
         } catch (UnsupportedEncodingException e) {
             throw e;
         } catch (FileNotFoundException e) {
             throw e;
         } catch (IOException e) {
             throw e;
         } finally {
             if ( reader != null ) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
     
    /**
     * 文字列値を設定する。
     * @param key キー
     * @param value 値
     */
    public void setString(String key, String value) {
         
         for ( String line : contents ) {
             if ( line.startsWith(key + ":") ) {
                 int index = contents.indexOf(line);
                 String newline = key + ": " + value;
                 contents.remove(index);
                 contents.add(index, newline);
                 return;
             }
         }
         
         String line = key + ": " + value;
         contents.add(line);
     }
     
     /**
      * 真偽値を設定する。
      * @param key キー
      * @param value 値
      */
     public void setBoolean(String key, boolean value) {
        setString(key, String.valueOf(value));
     }
     
     /**
      * 整数値を設定する。
      * @param key キー
      * @param value 値
      */
     public void setInt(String key, int value) {
        setString(key, String.valueOf(value));
     }
     
     /**
      * 値を設定する。
      * @param key キー
      * @param value 値
      */
     public void set(String key, Object value) {
         
         if ( value instanceof Integer ) {
             setInt(key, (Integer)value);
         } else if ( value instanceof Boolean ) {
             setBoolean(key, (Boolean)value );
         } else {
             setString(key, value.toString());
         }
     }
     
     /**
      * Yamlを上書き設定する。
      * @throws UnsupportedEncodingException 
      * @throws FileNotFoundException 
      * @throws IOException 
      */
     public void save() 
             throws UnsupportedEncodingException, 
             FileNotFoundException, IOException {
         
         BufferedWriter writer = null;
         try {
             writer = new BufferedWriter(new FileWriter(filename));
             
             for ( String line : contents ) {
                 writer.write(line);
                 writer.newLine();
             }
         } catch (UnsupportedEncodingException e) {
             throw e;
         } catch (FileNotFoundException e) {
             throw e;
         } catch (IOException e) {
             throw e;
         } finally {
             if ( writer != null ) {
                 try {
                     writer.flush();
                     writer.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
