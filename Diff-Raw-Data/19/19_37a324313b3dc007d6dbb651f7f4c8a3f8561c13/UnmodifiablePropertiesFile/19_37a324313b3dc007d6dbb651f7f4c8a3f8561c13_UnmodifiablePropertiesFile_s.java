 package net.visualillusionsent.utils;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 
 /**
  * An Unmodifiable Properties File implementation
  * 
  * @author Jason (darkdiplomat)
  */
 public final class UnmodifiablePropertiesFile extends AbstractPropertiesFile{
 
     public UnmodifiablePropertiesFile(String filepath) throws UtilityException{
         super(filepath);
         if(propsFile.exists()){
             try{
                 load(new FileInputStream(propsFile));
             }
             catch(FileNotFoundException e){
                 throw new UtilityException("file.err.ioe", filepath);
             }
         }
     }
 
     public UnmodifiablePropertiesFile(String jarpath, String entry) throws UtilityException{
         super(jarpath, entry);
         JarEntry ent = jar.getJarEntry(entry);
         try{
             load(jar.getInputStream(ent));
         }
         catch(IOException e){
             throw new UtilityException("file.err.ioe", filepath);
         }
     }
 
     /**
      * Loads the Properties File
      * 
      * @throws UtilityException
      * <br>
      *             if there was an error with reading the properties file
      */
     @Override
     protected final void load(InputStream instream) throws UtilityException{
         HashMap<String, String> tempProps = new HashMap<String, String>();
         HashMap<String, String> tempInLine = new HashMap<String, String>();
         HashMap<String, List<String>> tempCom = new HashMap<String, List<String>>();
         List<String> tempHead = new LinkedList<String>();
         List<String> tempFoot = new LinkedList<String>();
         UtilityException uex = null;
         BufferedReader in = null;
         try{
             in = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
             String inLine;
             LinkedList<String> inComments = new LinkedList<String>();
             while((inLine = in.readLine()) != null){
                 if(inLine.startsWith(";#")){
                     tempHead.add(inLine);
                 }
                 else if(inLine.startsWith("#;")){
                     tempFoot.add(inLine);
                 }
                 else if(inLine.startsWith(";") || inLine.startsWith("#")){
                     inComments.add(inLine);
                 }
                 else{
                     String[] propsLine = null;
                     try{
                         propsLine = inLine.split("=");
                         String key = propsLine[0].trim();
                         String value = propsLine[1];
                         if(value.contains("#!")){
                             String inlinec = value.split("#!")[1]; //Don't trim the comment
                             tempInLine.put(key, inlinec);
                             value = value.split("#!")[0];
                         }
                         tempProps.put(key.trim(), value.replace("\\#\\!", "#!").trim()); //remove escape sequence
                     }
                     catch(ArrayIndexOutOfBoundsException aioobe){
                         //Empty value?
                         if(inLine.contains("=")){
                             tempProps.put(propsLine[0], "");
                         }
                         //Incomplete property, drop reference
                         else{
                             inComments.clear();
                             continue;
                         }
                     }
                     if(!inComments.isEmpty()){
                         LinkedList<String> temp = new LinkedList<String>(inComments);
                         Collections.copy(temp, inComments);
                         tempCom.put(propsLine[0], Collections.unmodifiableList(new LinkedList<String>(temp)));
                         inComments.clear();
                     }
                 }
             }
         }
         catch(IOException ioe){
             UtilsLogger.severe(String.format("An IOException occurred in File: '%s'", filepath), ioe);
             uex = new UtilityException("file.err.ioe", filepath);
         }
         finally{
             if(in != null){
                 try{
                     in.close();
                 }
                 catch(IOException e){
                     //do nothing
                 }
             }
             if(uex != null){
                 throw uex;
             }
         }
         this.props = Collections.unmodifiableMap(tempProps);
         this.inlineCom = Collections.unmodifiableMap(tempInLine);
         this.comments = Collections.unmodifiableMap(tempCom);
         this.header = Collections.unmodifiableList(tempHead);
         this.footer = Collections.unmodifiableList(tempFoot);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final void reload() throws UtilityException{
        props.clear();
        comments.clear();
         if(jar != null){
             JarEntry ent = jar.getJarEntry(filepath);
             if(ent == null){
                 throw new UtilityException("entry.missing", filepath);
             }
             try{
                 load(jar.getInputStream(ent));
             }
             catch(IOException e){
                 throw new UtilityException("file.err.ioe", filepath);
             }
         }
         else{
             try{
                 load(new FileInputStream(propsFile));
             }
             catch(FileNotFoundException e){
                 throw new UtilityException("file.err.ioe", filepath);
             }
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void save() throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void forceSave() throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void save(boolean force) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean containsKey(String key) throws UtilityException{
         if(key == null){
             throw new UtilityException("arg.null", "String key");
         }
         else if(key.trim().isEmpty()){
             throw new UtilityException("arg.empty", "String key");
         }
         return props.containsKey(key);
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void removeKey(String key) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String getString(String key) throws UtilityException{
         if(key == null){
             throw new UtilityException("arg.null", "String key");
         }
         else if(key.isEmpty()){
             throw new UtilityException("arg.empty", "String key");
         }
         else if(containsKey(key)){
             return props.get(key);
         }
         throw new UtilityException("key.missing", key);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String getString(String key, String def) throws UtilityException{
         if(key == null){
             throw new UtilityException("arg.null", "String key");
         }
         else if(key.isEmpty()){
             throw new UtilityException("arg.empty", "String key");
         }
         else if(containsKey(key)){
             return props.get(key);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setString(String key, String value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setString(String key, String value, String... comments) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String[] getStringArray(String key) throws UtilityException{
         return getStringArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String[] getStringArray(String key, String[] def) throws UtilityException{
         if(containsKey(key)){
             return getStringArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setStringArray(String key, String[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setStringArray(String key, String[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String[] getStringArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.trimElements(getString(key).split(splitBy));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String[] getStringArray(String key, String splitBy, String[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.trimElements(getString(key).split(splitBy));
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setStringArray(String key, String spacer, String[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setStringArray(String key, String spacer, String[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final byte getByte(String key) throws UtilityException{
         try{
             return Byte.parseByte(getString(key));
         }
         catch(NumberFormatException nfe){
             throw new UtilityException("prop.nan", key);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final byte getByte(String key, byte def) throws UtilityException{
         if(containsKey(key)){
             try{
                 return Byte.parseByte(getString(key));
             }
             catch(NumberFormatException nfe){
                 throw new UtilityException("prop.nan", key);
             }
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setByte(String key, byte value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setByte(String key, byte value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final byte[] getByteArray(String key) throws UtilityException{
         return getByteArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final byte[] getByteArray(String key, byte[] def) throws UtilityException{
         if(containsKey(key)){
             return getByteArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setByteArray(String key, byte[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setByteArray(String key, byte[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final byte[] getByteArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToByteArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final byte[] getByteArray(String key, String splitBy, byte[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToByteArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setByteArray(String key, String spacer, byte[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setByteArray(String key, String spacer, byte[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final short getShort(String key) throws UtilityException{
         try{
             return Short.parseShort(getString(key));
         }
         catch(NumberFormatException nfe){
             throw new UtilityException("prop.nan", key);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final short getShort(String key, short def) throws UtilityException{
         if(containsKey(key)){
             try{
                 return Short.parseShort(getString(key));
             }
             catch(NumberFormatException nfe){
                 throw new UtilityException("prop.nan", key);
             }
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setShort(String key, short value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setShort(String key, short value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final short[] getShortArray(String key) throws UtilityException{
         return getShortArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final short[] getShortArray(String key, short[] def) throws UtilityException{
         if(containsKey(key)){
             return getShortArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setShortArray(String key, short[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setShortArray(String key, short[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final short[] getShortArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToShortArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final short[] getShortArray(String key, String splitBy, short[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToShortArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setShortArray(String key, String spacer, short[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setShortArray(String key, String spacer, short[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final int getInt(String key) throws UtilityException{
         try{
             return Integer.parseInt(getString(key));
         }
         catch(NumberFormatException nfe){
             throw new UtilityException("prop.nan", key);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final int getInt(String key, int def) throws UtilityException{
         if(containsKey(key)){
             try{
                 return Integer.parseInt(getString(key));
             }
             catch(NumberFormatException nfe){
                 throw new UtilityException("prop.nan", key);
             }
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setInt(String key, int value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setInt(String key, int value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final int[] getIntArray(String key) throws UtilityException{
         return getIntArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final int[] getIntArray(String key, int[] def) throws UtilityException{
         if(containsKey(key)){
             return getIntArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setIntArray(String key, int[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setIntArray(String key, int[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final int[] getIntArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToIntArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final int[] getIntArray(String key, String splitBy, int[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToIntArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setIntArray(String key, String spacer, int[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setIntArray(String key, String spacer, int[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long getLong(String key) throws UtilityException{
         try{
             return Long.parseLong(getString(key));
         }
         catch(NumberFormatException nfe){
             throw new UtilityException("prop.nan", key);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long getLong(String key, long def) throws UtilityException{
         if(containsKey(key)){
             try{
                 return Long.parseLong(getString(key));
             }
             catch(NumberFormatException nfe){
                 throw new UtilityException("prop.nan", key);
             }
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setLong(String key, long value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setLong(String key, long value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long[] getLongArray(String key) throws UtilityException{
         return getLongArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long[] getLongArray(String key, long[] def) throws UtilityException{
         if(containsKey(key)){
             return getLongArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setLongArray(String key, long[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setLongArray(String key, long[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long[] getLongArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToLongArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final long[] getLongArray(String key, String splitBy, long[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToLongArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setLongArray(String key, String spacer, long[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setLongArray(String key, String spacer, long[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final float getFloat(String key) throws UtilityException{
         try{
             return Float.parseFloat(getString(key));
         }
         catch(NumberFormatException nfe){
             throw new UtilityException("prop.nan", key);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final float getFloat(String key, float def) throws UtilityException{
         if(containsKey(key)){
             try{
                 return Float.parseFloat(getString(key));
             }
             catch(NumberFormatException nfe){
                 throw new UtilityException("prop.nan", key);
             }
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setFloat(String key, float value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setFloat(String key, float value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final float[] getFloatArray(String key) throws UtilityException{
         return getFloatArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final float[] getFloatArray(String key, float[] def) throws UtilityException{
         if(containsKey(key)){
             return getFloatArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setFloatArray(String key, float[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setFloatArray(String key, float[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final float[] getFloatArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToFloatArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final float[] getFloatArray(String key, String splitBy, float[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToFloatArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setFloatArray(String key, String spacer, float[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setFloatArray(String key, String spacer, float[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final double getDouble(String key) throws UtilityException{
         try{
             return Double.parseDouble(getString(key));
         }
         catch(NumberFormatException nfe){
             throw new UtilityException("prop.nan");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final double getDouble(String key, double def) throws UtilityException{
         if(containsKey(key)){
             try{
                 return Double.parseDouble(getString(key));
             }
             catch(NumberFormatException nfe){
                 throw new UtilityException("prop.nan", key);
             }
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setDouble(String key, double value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setDouble(String key, double value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final double[] getDoubleArray(String key) throws UtilityException{
         return getDoubleArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final double[] getDoubleArray(String key, double[] def) throws UtilityException{
         if(containsKey(key)){
             return getDoubleArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setDoubleArray(String key, double[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setDoubleArray(String key, double[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final double[] getDoubleArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToDoubleArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final double[] getDoubleArray(String key, String splitBy, double[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToDoubleArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setDoubleArray(String key, String spacer, double[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setDoubleArray(String key, String spacer, double[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean getBoolean(String key) throws UtilityException{
         return BooleanUtils.parseBoolean(getString(key));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean getBoolean(String key, boolean def) throws UtilityException{
         if(containsKey(key)){
             return BooleanUtils.parseBoolean(getString(key));
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setBoolean(String key, boolean value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setBoolean(String key, boolean value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean[] getBooleanArray(String key) throws UtilityException{
         return getBooleanArray(key, ",");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean[] getBooleanArray(String key, boolean[] def) throws UtilityException{
         if(containsKey(key)){
             return getBooleanArray(key, ",");
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setBooleanArray(String key, boolean[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setBooleanArray(String key, boolean[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean[] getBooleanArray(String key, String splitBy) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         return StringUtils.stringToBooleanArray(getString(key), splitBy);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final boolean[] getBooleanArray(String key, String splitBy, boolean[] def) throws UtilityException{
         if(splitBy == null){
             throw new UtilityException("arg.null", "String splitBy");
         }
         else if(splitBy.isEmpty()){
             throw new UtilityException("arg.empty", "String splitBy");
         }
         if(containsKey(key)){
             return StringUtils.stringToBooleanArray(getString(key), splitBy);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setBooleanArray(String key, String spacer, boolean[] value) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setBooleanArray(String key, String spacer, boolean[] value, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final char getCharacter(String key) throws UtilityException{
         return getString(key).trim().charAt(0);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final char getCharacter(String key, char def) throws UtilityException{
         if(containsKey(key)){
             return getString(key).trim().charAt(0);
         }
         else{
             return def;
         }
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setCharacter(String key, char ch) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setCharacter(String key, char ch, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Gets an unmodifiableMap of all keys and properties as Strings
      * 
      * @return unmodifiable properties map
      */
     public final Map<String, String> getPropertiesMap(){
         return Collections.unmodifiableMap(props);
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void addComment(String key, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void setComments(String key, String... comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Gets all the comments attached to the property key
      * 
      * @param key
      *            the property key
      * @return comments if found, {@code null} if no comments found
      */
     public final String[] getComments(String key){
         if(comments.containsKey(key)){
             return comments.get(key).toArray(new String[0]);
         }
         return null;
     }
 
     /**
      * Gets all the comments attached to the property key
      * 
      * @param key
      *            the property key
      * @return comments if found; {@code null} if no comments found
      */
     public final List<String> getCommentsAsList(String key){
         if(comments.containsKey(key)){
             return comments.get(key);
         }
         return null;
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void removeComment(String key, String comment) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void removeAllCommentsFromKey(String key) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void removeAllCommentsFromFile() throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void addHeaderLines(String... lines) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Gets the lines of the Header
      * 
      * @return the header lines
      */
     public final LinkedList<String> getHeaderLines(){
         LinkedList<String> toRet = new LinkedList<String>(header);
         Collections.copy(header, toRet);
         return toRet;
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void clearHeader() throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void addFooterLines(String... lines) throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Gets the lines of the Header
      * 
      * @return the header lines
      */
     public final LinkedList<String> getFooterLines(){
         LinkedList<String> toRet = new LinkedList<String>(footer);
         Collections.copy(footer, toRet);
         return toRet;
     }
 
     /**
      * Unsupported Operation with UnmodifiablePropertiesFiles
      * 
      * @throws UnsupportedOperationException
      *             Not supported with Unmodifiable Properties Files
      */
     @Override
     protected final void clearFooter() throws UnsupportedOperationException{
         throw new UnsupportedOperationException("Unable to modify an UnmodifiablePropertiesFile");
     }
 
     /**
      * Gets the InLine comment for a property
      * 
      * @param key
      *            the property key to get inline comment for
      * @return the inline comment or {@code null} if no comment
      */
     public final String getInlineComment(String key){
         return inlineCom.get(key);
     }
 }
