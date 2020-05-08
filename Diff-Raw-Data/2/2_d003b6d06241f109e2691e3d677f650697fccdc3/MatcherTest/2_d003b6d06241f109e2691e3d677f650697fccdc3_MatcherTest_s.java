 /*
    Copyright 2013 Zava (http://www.zavakid.com)
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package com.zavakid.mockingbird;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * @author Zava 2013-1-28 下午11:19:24
  * @since 0.0.1
  */
 @RunWith(Parameterized.class)
 public class MatcherTest {
 
     @Parameters
     public static List<Object[]> params() {
         return Arrays.asList(new Object[][] {
                 new Object[] { "abc", new String[] { "abc" } },
                 new Object[] { "a.*", new String[] { "abcde" } },
                 new Object[] { ".", new String[] { "f" } },
                 new Object[] { ".", new String[] { "f" } },
                 new Object[] { ".*", new String[] { "sdfajlakjf", "ab" } },
                 new Object[] { "a*", new String[] { "a", "", "aaaaa" } },
                 new Object[] { "a+", new String[] { "a" } },
                 new Object[] { "a?", new String[] { "", "a" } },
                 new Object[] { "b.a?", new String[] { "bx", "bza" } },
                 new Object[] { "b*a?", new String[] { "bbbbbbbbbbbb", "bbbbbbbbbbbba" } },
                 new Object[] {
                         "(xy)*(abc)+",
                         new String[] { "xyabc", "abc", "abcabc", "xyabc", "xyabcabc", "xyxyxyxyabc", "xyxyxyxyabcabc",
                                 "xyxyxyxyabcabcabc" } },
 
                 new Object[] { "(xy)*(abc)?",
                         new String[] { "", "xy", "xyxy", "xyxyxy", "", "xyabc", "xyxyabc", "xyxyxyabc" } },
 
                 new Object[] { "a|b", new String[] { "a", "b" } },
                 new Object[] { "ab|xy", new String[] { "ab", "xy" } },
                 new Object[] { "(ab|xy)zz", new String[] { "abzz", "xyzz" } },
                 new Object[] { "(ab?|xy)zz", new String[] { "azz", "abzz", "xyzz" } },
                 new Object[] { "(ab|xy)?zz", new String[] { "zz", "abzz", "xyzz" } },
                 new Object[] { "(ab|xy)*zz", new String[] { "zz", "abababzz", "xyxyxyzz", "abxyabxyxyabzz" } },
                 new Object[] { "ab|cd|de", new String[] { "ab", "cd", "de" } },
                 new Object[] { "(x|y)+", new String[] { "x", "yxy", "xxyxyxyxyyyyyxxxxx" } },
                 new Object[] { "((a|b)(x|y))?", new String[] { "", "ax", "bx", "ay", "by" } },
                 new Object[] { "((a|b)?(x|y))?", new String[] { "", "ax", "bx", "ay", "by", "x", "y" } },
                 new Object[] { "a\\|b", new String[] { "a|b" } }, new Object[] { "a\\*", new String[] { "a*" } },
 
         });
     }
 
     private String   pattern;
     private String[] testStrs;
 
     public MatcherTest(String pattern, String[] testStrs){
         this.pattern = pattern;
         this.testStrs = testStrs;
     }
 
     @Test
     public void test() {
        MatcherWithFrag matcher = new MatcherWithFrag(pattern);
         for (String testStr : testStrs) {
             Assert.assertTrue(String.format("pattern [ %s ] not matche string [ %s ]", pattern, testStr),
                               matcher.match(testStr));
         }
     }
 }
