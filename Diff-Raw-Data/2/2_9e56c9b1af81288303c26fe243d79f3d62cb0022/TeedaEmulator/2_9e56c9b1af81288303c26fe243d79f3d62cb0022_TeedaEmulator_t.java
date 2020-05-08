 /*
  * Copyright 2004-2010 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.core.teeda;
 
 import java.util.regex.Pattern;
 
 import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
 import jp.aonir.fuzzyxml.FuzzyXMLElement;
 
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author taichi
  * 
  */
 public class TeedaEmulator {
 
     public static final Pattern EXIST_TO_FILE_PREFIX = Pattern.compile(
             "(go|jump)[A-Z\\d_].*", Pattern.CASE_INSENSITIVE);
 
     public static final Pattern MAPPING_MULTI_ITEM = Pattern
             .compile("[\\w]*(Items|Grid[xX]?[yY]?)$");
 
     public static final Pattern MAPPING_MULTI_ITEM_LOOP_TAG = Pattern.compile(
             "(div|table|tbody)", Pattern.CASE_INSENSITIVE);
 
     public static final Pattern MAPPING_SKIP_ID = Pattern
             .compile(".*[^\\w-].*|(allM|m)essages|[\\w]+Message|(go|jump|is|mock)[A-Z\\d_][\\w-]*");
 
     public static final Pattern MAPPING_SKIP_TAGS = Pattern.compile(
             "form|label", Pattern.CASE_INSENSITIVE);
 
     public static final Pattern MAPPING_CONDITION_TAG = Pattern.compile("div",
             Pattern.CASE_INSENSITIVE);
 
     public static final Pattern MAPPING_CONDITION_ID = Pattern
             .compile("is(Not)?[A-Z\\d_][\\w-]*");
 
     public static final Pattern MAPPING_COMMAND_METHOD_TAG = Pattern.compile(
             "input", Pattern.CASE_INSENSITIVE);
 
     public static final Pattern MAPPING_COMMAND_METHOD_TAG_TYPE = Pattern
            .compile("submit|button|image", Pattern.CASE_INSENSITIVE);
 
     public static final Pattern MAPPING_COMMAND_METHOD_ID = Pattern
             .compile("do[\\w-]*");
 
     public static boolean isCommandId(FuzzyXMLElement e, String id) {
         if (MAPPING_COMMAND_METHOD_TAG.matcher(e.getName()).matches()) {
             return isLegalAttribute(e.getAttributeNode("type"),
                     MAPPING_COMMAND_METHOD_TAG_TYPE)
                     && isLegalAttribute(e.getAttributeNode("id"),
                             MAPPING_COMMAND_METHOD_ID);
         }
         return false;
     }
 
     private static boolean isLegalAttribute(FuzzyXMLAttribute a, Pattern p) {
         return a != null && p.matcher(a.getValue()).matches();
     }
 
     public static boolean isConditionId(FuzzyXMLElement e, String id) {
         return MAPPING_CONDITION_TAG.matcher(e.getName()).matches()
                 && MAPPING_CONDITION_ID.matcher(id).matches();
     }
 
     public static String calcConditionMethodName(String id) {
         if (StringUtil.isEmpty(id) == false
                 && MAPPING_CONDITION_ID.matcher(id).matches()) {
             return id.replaceAll("^is(Not)?", "is");
         }
         return null;
     }
 
     public static boolean isNotSkipId(FuzzyXMLElement e, String id) {
         if (MAPPING_SKIP_TAGS.matcher(e.getName()).matches()) {
             return false;
         }
         if (MAPPING_SKIP_ID.matcher(id).matches()) {
             return false;
         }
         return true;
     }
 
     public static String toOutComeFileName(String s) {
         int index = 0;
         if (StringUtil.isEmpty(s) == false) {
             s = s.replaceAll("^(go|jump)", "");
         }
         return StringUtil.decapitalize(s.substring(index));
     }
 
     public static final String toMultiItemName(String id) {
         return id.replaceAll("(Items|Grid[xX]?[yY]?)$", "Items");
     }
 
     public static String calcMappingId(FuzzyXMLElement e, String id) {
         String result = id;
         int index = id.indexOf('-');
         if (-1 < index) {
             result = id.substring(0, index);
         }
 
         if (isConditionId(e, id)) {
             result = calcConditionMethodName(id);
         }
         if (MAPPING_MULTI_ITEM.matcher(id).matches()) {
             result = toMultiItemName(id);
         }
         return result;
     }
 
     public static String calcMultiItemIndexId(String multiItemId) {
         return multiItemId.replaceAll("Items", "Index");
     }
 
     public static boolean needIndex(FuzzyXMLElement e, String id) {
         return MAPPING_MULTI_ITEM_LOOP_TAG.matcher(e.getName()).matches();
     }
 
     public static boolean isSelect(FuzzyXMLElement e) {
         String s = e.getName();
         return StringUtil.isEmpty(s) == false
                 && "select".equals(s.toLowerCase());
     }
 }
