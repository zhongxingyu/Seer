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
 package jsl;
 
 import java.util.regex.Pattern;
 
 /**
  *
  * @author Joseph Spencer
  */
 public interface Characters {
    Pattern SPACE = Pattern.compile("^(\\s++).*+");
    Pattern OPEN_BLOCK = Pattern.compile("^(\\{).*+");
    Pattern CLOSE_BLOCK = Pattern.compile("^(\\}).*+");
 
    Pattern NAMESPACE = Pattern.compile("^(namespace)(?=\\s).*+");
    Pattern NULL = Pattern.compile("^(null)(?![a-zA-Z0-9$_@'\"]).*+");
    Pattern CHOOSE = Pattern.compile("^(choose)(?=\\}).*+");
    Pattern IF = Pattern.compile("^(if)(?=\\s|\\}).*+");
    Pattern IMPORT = Pattern.compile("^(import)(?=\\s).*+");
    Pattern TEMPLATE = Pattern.compile("^(template)(?=\\s|\\}).*+");
    Pattern OTHERWISE = Pattern.compile("^(otherwise)(?=\\}).*+");
    Pattern WHEN = Pattern.compile("^(when)(?=\\s|\\}).*+");
   Pattern NAME = Pattern.compile("^([a-zA-Z][a-zA-Z0-9_]*+).*+");
    Pattern IMPORT_PATH = Pattern.compile("^((?:/?(?:\\.\\./)?(?:[^/\\}]++/)*+)?[^/\\}]++).*+");
    Pattern ABSOLUTE_PATH = Pattern.compile("^(/(?:[^/\\}]++/)*+).*+");
 
 
    Pattern INTEGER = Pattern.compile("^((?:[0-9]++)(?!\\.)).*+");
    Pattern DECIMAL = Pattern.compile("^(0x[0-9A-Fa-f]++(?:[eE][+-][0-9]++)?|(?:0(?=\\.)|[1-9][0-9]*+)(?:\\.?[0-9]*+[eE][+-][0-9]++|\\.[0-9]++(?:[eE][+-][0-9]++)?)).*+");
    Pattern STRING = Pattern.compile("^((['\"])((?:(?!\\2)(?!\\r?\\n)(?:\\\\\\\\|\\\\\\r?\\n|\\\\\\2|[^\\r\\n]))*+)\\2).*+");
 
 
    Pattern INPUT_TOKENS = Pattern.compile("^([^\\{]++).*+");
 
    Pattern VARIABLE = Pattern.compile("^(variable)(?=\\s).*+");
 
    //SPACE
    char LF = 10;
    char CR = 13;
    char space = 32;
    char tab = 9;
 
    //SYMBOLS
    char amp = 38;
    char asterisk = 42;
    char close = 125;
    char colon = 58;
    char equal = 61;
    char exclamation = 33;
    char forward = 47;
    char gt = 62;
    char lt = 60;
    char minus = 45;
    char mod = 37;
    char open = 123;
    char pipe = 124;
    char plus = 43;
    char question = 63;
    char tilde = 126;
    char at = 64;
 
    //ALPHA
    char a = 97;
    char b = 98;
    char c = 99;
    char d = 100;
    char e = 101;
    char f = 102;
    char g = 103;
    char h = 104;
    char i = 105;
    char j = 106;
    char k = 107;
    char l = 108;
    char m = 109;
    char n = 110;
    char o = 111;
    char p = 112;
    char q = 113;
    char r = 114;
    char s = 115;
    char t = 116;
    char u = 117;
    char v = 118;
    char w = 119;
    char x = 120;
    char y = 121;
    char z = 122;
 
    //NUMBERS
    char zero = 48;
    char one = 49;
    char two = 50;
    char three = 51;
    char four = 52;
    char five = 53;
    char six = 54;
    char seven = 55;
    char eight = 56;
    char nine = 57;
 
 
    char squote = 39;
    char quote = 34;
 
    char cparen = 41;
    char cbracket = 93;
    char comma = 44;
    char dot = 46;
    char obracket = 91;
    char oparen = 40;
    char semic = 59;
 
 
 
 
 }
