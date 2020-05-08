 package nd.regex;
 
 /**
  *
  */
 public class MatcherCharacterClassTest extends MatcherTestCase {
 
 
     public void test_user_defined_char_class() {
         checkWorkEqually("abc", "[abc][abc][abc]");
         checkWorkEqually("abc", "[ab][bc][ac]");
         checkWorkEqually("ad",  "[ab][ab]");
     }
 
 
     public void test_user_defined_char_class_with_interval() {
         checkWorkEqually("abc", "[a-c][-a-c][a-c]");
         checkWorkEqually("ab", "[ab][a-c]");
         checkWorkEqually("abc", "a[a-z][c]");
         checkWorkEqually("-ab", "[-]a[a-z]");
         checkWorkEqually("a-b", "a[a-b]b");
         checkWorkEqually("a-b", "a[a-]b");
         checkWorkEqually("a-b", "a[-a]b");
     }
 
 
     public void test_exclusive_character_class() {
         checkWorkEqually("d",   "[^abc]");
         checkWorkEqually("o",   "[^a-z]");
         checkWorkEqually("abc", "a[^0-9]c");
     }
 
 
     public void test_predefined_char_class() {
         checkWorkEqually("123", "\\d\\d\\d");
         checkWorkEqually("ab",  "\\D\\D");
         checkWorkEqually(" ",   "\\s");
         checkWorkEqually("\t",  "\\s");
         checkWorkEqually("\n",  "\\s");
         checkWorkEqually("\r",  "\\s");
         checkWorkEqually("\f",  "\\s");
         checkWorkEqually("a",   "\\S");
         checkWorkEqually("a_9", "\\w\\w\\w");
         checkWorkEqually(" !%", "\\W\\W\\W");
         checkWorkEqually(" a9", "...");
     }
 
 
     public void test_special_characters_in_char_class() {
         checkWorkEqually("]",  "[]]");
         checkWorkEqually("[",  "[[]");
         checkWorkEqually("|",  "[|]");
         checkWorkEqually("$",  "[$]");
         checkWorkEqually("(",  "[(]");
         checkWorkEqually(")",  "[)]");
         checkWorkEqually("{",  "[{]");
         checkWorkEqually("}",  "[}]");
         checkWorkEqually("*",  "[*]");
         checkWorkEqually("+",  "[+]");
         checkWorkEqually("?",  "[?]");
         checkWorkEqually(".",  "[.]");
         checkWorkEqually("\\", "[\\\\]");
         checkWorkEqually("$",  "[\\$]");
         checkWorkEqually("[",  "[\\[]");
         checkWorkEqually("]",  "[\\]]");
         checkWorkEqually("^",  "[\\^]");
         checkWorkEqually("-",  "[\\-]");
         checkWorkEqually("$",  "[\\$]");
         checkWorkEqually("-",  "[-]");
     }
 
 
     public void test_inner_char_class() {
         checkWorkEqually("c", "[[a-d][^mn]]");
     }
 }
