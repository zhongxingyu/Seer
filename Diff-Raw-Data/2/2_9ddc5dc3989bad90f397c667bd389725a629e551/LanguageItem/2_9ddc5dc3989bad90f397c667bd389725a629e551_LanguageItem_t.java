 /*
  * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
  *
  * http://izpack.org/
  * http://izpack.codehaus.org/
  *
  * Copyright 2008 Ari Voutilainen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package IzPack.TestLangPacks;
 
 /**
  * Contains language file items.
  * 
  * @author Ari Voutilainen
  */
 public class LanguageItem
 {
    private String key = null;
    private String value = null;
    private String[] unknownAttrs = null;
    
    /**
     * The default contructor of the class.
     * 
     * @param key     ID in the 'str' element.
     * @param value   Text in the 'str' element.
     */
    public LanguageItem(String key, String value)
    {
       this.key = key;
       this.value = value;
    }
    
    /**
     * Returns the ID in 'str' element.
     */
    public String getKey()
    {
       return key;
    }
    
    /**
     * Returns the text in 'str' element.
     */
    public String getValue()
    {
       return value;
    }
    
    /**
     * Returns the unknown attributes in 'str' element.
     */
    public String[] getUnknownAttributes()
    {
       return unknownAttrs;
    }
    
    /**
     * Sets the ID in 'str' element.
     * 
     * @param key  ID to set.
     */
    public void setKey(String key)
    {
       this.key = key;
    }
    
    /**
     * Sets the text in 'str' element.
     * 
     * @param value   Value to set.
     */
    public void setValue(String value)
    {
       this.value = value;
    }
    
    /**
     * Sets the unknown attributes in 'str' element.
     * 
     * @param unknownAttrs  String array containing unknown attributes. Each
     *                      index contain element and attributes (not 'id' and
     *                      'txt').
     */
    public void setUnknownAttributes(String[] unknownAttrs)
    {
      if (unknownAttrs.length >= 0)
       {
          this.unknownAttrs = unknownAttrs;
       }
    }
    
    /**
     * Checks whether given ID is the same as in this object.
     * 
     * @param id   ID to check.
     * @return     true if IDs are the same. false if not.
     */
    public boolean equalsId(String id)
    {
       return key.equals(id);
    }
 }
