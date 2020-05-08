 /*
  * Copyright 2012 Joseph Spencer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.xforj;
 
 import java.util.regex.Pattern;
 
 /**
  *
  * @author Joseph Spencer
  */
 public interface Characters {
    //SEQUENCES
    Pattern ABSOLUTE_PATH = Pattern.compile("^(/(?:[^/\\}]++/)*+).*+");
    Pattern CLOSE_BLOCK = Pattern.compile("^(\\}).*+");
    Pattern CONTEXT_STATIC_REFINEMENT_NAMESPACE = Pattern.compile("^(\\s*+(?:[a-zA-Z$_][a-zA-Z0-9$_]*+\\s*+\\.)*+\\s*+[a-zA-Z$_][a-zA-Z0-9$_]*+\\s*).*+");
   Pattern IMPORT_PATH = Pattern.compile("((?:\\\\\\}|[^\\}])++).*+");
    Pattern INPUT_TOKENS = Pattern.compile("^([^\\{]++).*+");
    Pattern NAME = Pattern.compile("^([a-zA-Z$_][a-zA-Z0-9$_]*+).*+");
    Pattern NS = Pattern.compile("^([a-zA-Z0-9$_][a-zA-Z0-9$_]*+(?:\\.[a-zA-Z$_][a-zA-Z0-9$_]*+)*+).*+");
    Pattern NS_FORCED = Pattern.compile("^([a-zA-Z0-9$_][a-zA-Z0-9$_]*+(?:\\.[a-zA-Z$_][a-zA-Z0-9$_]*+)++).*+");
    Pattern OPEN_BLOCK = Pattern.compile("^(\\{).*+");
    Pattern SORT_DIRECTION = Pattern.compile("^(asc|desc)(?![a-zA-Z0-9$_]).*+");
    Pattern SORT_MODIFIERS = Pattern.compile("^\\|([in]{1,2})(?![a-zA-Z0-9$_]).*+");
    Pattern SPACE = Pattern.compile("^(\\s++).*+");
    Pattern SPACE_PRECEDING_CURLY = Pattern.compile("^(\\s++)(?=\\{).*+");
 
    //RESERVED WORDS
    Pattern CALL = Pattern.compile("^(\\{call\\s++).*+");
    Pattern CALL_CLOSING = Pattern.compile("^(\\{/call\\}).*+");
    Pattern CHOOSE = Pattern.compile("^(\\{choose\\}).*+");
    Pattern CHOOSE_CLOSING = Pattern.compile("^(\\{/choose\\}).*+");
    Pattern COUNT_FN = Pattern.compile("^(count\\().*+");
    Pattern CURRENT_FN = Pattern.compile("^(current\\(\\)).*+");
    Pattern FOREACH = Pattern.compile("^(\\{foreach\\s++).*+");
    Pattern FOREACH_CLOSING = Pattern.compile("^(\\{/foreach\\}).*+");
    Pattern IF = Pattern.compile("^(\\{if\\s++).*+");
    Pattern IF_CLOSING = Pattern.compile("^(\\{/if\\}).*+");
    Pattern IMPORT = Pattern.compile("^(\\{import\\s++).*+");
    Pattern LAST_FN = Pattern.compile("^(last\\(\\)).*+");
    Pattern LOG = Pattern.compile("^(\\{log\\s++).*+");
    Pattern NAME_FN = Pattern.compile("^(name\\(\\)).*+");
    Pattern NAMESPACE = Pattern.compile("^(\\{namespace\\s++).*+");
    Pattern OTHERWISE = Pattern.compile("^(\\{otherwise\\}).*+");
    Pattern OTHERWISE_CLOSING = Pattern.compile("^(\\{/otherwise\\}).*+");
    Pattern PARAM = Pattern.compile("^(\\{param\\s++).*+");
    Pattern POSITION_FN = Pattern.compile("^(position\\(\\)).*+");
    Pattern SORT = Pattern.compile("^(\\{sort\\s++).*+");
    Pattern TEMPLATE = Pattern.compile("^(\\{template\\s++).*+");
    Pattern TEMPLATE_CLOSING = Pattern.compile("^(\\{/template\\})(?=\\s|\\}).*+");
    Pattern TEXT = Pattern.compile("^(\\{text\\}).*+");
    Pattern TEXT_CLOSING = Pattern.compile("^(\\{/text\\}).*+");
    Pattern TEXT_INPUT = Pattern.compile("^((?:(?!\\{/text\\})[\\s\\S])*+[\\s\\S]?)\\{/text\\}.*+");
    Pattern VAR = Pattern.compile("^(\\{var\\s++).*+");
    Pattern WHEN = Pattern.compile("^(\\{when\\s++).*+");
    Pattern WHEN_CLOSING = Pattern.compile("^(\\{/when\\}).*+");
 
 
 
    //PRIMITIVES
    Pattern BOOLEAN = Pattern.compile("^(false|true)(?![a-z0-9A-Z$_]).*+");
    Pattern DECIMAL = Pattern.compile("^(0x[0-9A-Fa-f]++(?:[eE][+-][0-9]++)?|(?:0(?=\\.)|[1-9][0-9]*+)(?:\\.?[0-9]*+[eE][+-][0-9]++|\\.[0-9]++(?:[eE][+-][0-9]++)?)).*+");
    Pattern INTEGER = Pattern.compile("^((?:[0-9]++)(?!\\.)).*+");
    Pattern NULL = Pattern.compile("^(null)(?![a-zA-Z0-9$_@'\"]).*+");
    Pattern STRING = Pattern.compile("^((['\"])((?:(?!\\2)(?!\\r?\\n)(?:\\\\\\\\|\\\\\\r?\\n|\\\\\\2|[^\\r\\n]))*+)\\2).*+");
 
 
    //JAVASCRIPT
    /*DEVELOPMENT
    final String js_bld="bld";
    final String js_context="context";
    final String js_count="count";
    final String js_CountElements="CountElements";
    final String js_currentNS="currentNS";
    final String js_data="data";
    final String js__data="_data";
    final String js_foreach="Foreach";
    final String js_GetSortArray="GetSortArray";
    final String js_last="last";
    final String js_name="name";
    final String js_params="params";
    final String js__params="_params";
    final String js_position="position";
    final String js_StringBuffer="StringBuffer";
    final String js_TemplateBasket="TemplateBasket";
    final String js_SafeValue="safeValue";*/
 
    /*MUNGED*/
    final String js_bld="b";
    final String js_context="X";
    final String js_count="T";
    final String js_CountElements="C";
    final String js_currentNS="N";
    final String js_data="D";
    final String js__data="A";
    final String js_foreach="F";
    final String js_GetSortArray="G";
    final String js_last="L";
    final String js_name="n";
    final String js_params="P";
    final String js__params="M";
    final String js_position="O";
    final String js_StringBuffer="S";
    final String js_TemplateBasket="B";
    final String js_SafeValue="f";
    /**/
 }
