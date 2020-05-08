 /*
  * Copyright 2012 Robert Stoll <rstoll@tutteli.ch>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package ch.tutteli.tsphp.translators.php54.test.testutils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author Robert Stoll <rstoll@tutteli.ch>
  */
 public class ExpressionHelper
 {
 
     public static String[][] getAssignmentExpression() {
         return new String[][]{
                     {"$a = $b","($a = $b)"},
                     {"$a += $b","($a += $b)"},
                     {"$a -= $b","($a -= $b)"},
                     {"$a *= $b","($a *= $b)"},
                     {"$a /= $b","($a /= $b)"},
                     {"$a &= $b","($a &= $b)"},
                     {"$a |= $b","($a |= $b)"},
                     {"$a ^= $b","($a ^= $b)"},
                     {"$a %= $b","($a %= $b)"},
                     {"$a .= $b","($a .= $b)"},
                     {"$a <<= $b","($a <<= $b)"},
                     {"$a >>= $b","($a >>= $b)"},
                     //cannot be covered here, because the type of $a is not known
                     //{"$a =() $b","(cAssign $a $b)"},
 
                     {
                         "$a = $b += $c -= $d *= $e /= $f &= $g |= $h ^= $i %= $j .= $k <<= $l >>= $m",
                         "($a = ($b += ($c -= ($d *= ($e /= ($f &= ($g |= ($h ^= ($i %= "
                             + "($j .= ($k <<= ($l >>= $m))))))))))))"
                     }
                 };
     }
 
     public static List<String[]> getExpressions() {
         List<String[]> collection = new ArrayList<>();
         collection.addAll(Arrays.asList(getAssignmentExpression()));
         collection.addAll(Arrays.asList(getExpressionsWihtoutAssignment()));
         return collection;
     }
 
     public static String[][] getExpressionsWihtoutAssignment() {
         return new String[][]{
                     {"$a or $b","($a or $b)"},
                     {"$a or $b or $c","(($a or $b) or $c)"},
                     {"$a xor $b","($a xor $b)"},
                     {"$a xor $b xor $c","(($a xor $b) xor $c)"},
                     {"$a and $b","($a and $b)"},
                     {"$a and $b and $c","(($a and $b) and $c)"},
                     {"$a and $b or $c xor $d","(($a and $b) or ($c xor $d))"},
                     {"$a or $b and $c xor $d","($a or (($b and $c) xor $d))"},
                     {"true ? $a : $b","(true ? $a : $b)"},
                     {"true ? $a ? $b : $c : $d","(true ? ($a ? $b : $c) : $d)"},
                     {"true ? $a : $b ? $c : $d","(true ? $a : ($b ? $c : $d))"},
                     {"$a = true ? $c += $d : $e -= $f","($a = (true ? ($c += $d) : ($e -= $f)))"},
                     {
                         "$a *= true ? $c /= $d ? $e &= $f : $g |= $h : $i ^= $j",
                         "($a *= (true ? ($c /= ($d ? ($e &= $f) : ($g |= $h))) : ($i ^= $j)))"
                     },
                     {
                         "$a %= true ? $c .= $d ? $e <<= $f : $g >>= $h : $i = $j",
                         "($a %= (true ? ($c .= ($d ? ($e <<= $f) : ($g >>= $h))) : ($i = $j)))"
                     },
                     {"$a || $b","($a || $b)"},
                     {"$a || $b || $c","(($a || $b) || $c)"},
                     {"$a && $b","($a && $b)"},
                     {"$a && $b && $c","(($a && $b) && $c)"},
                     {"$a && $b || $c","(($a && $b) || $c)"},
                     {"$a || $b && $c","($a || ($b && $c))"},
                     {"$a || $b && $c ? $d : $e","(($a || ($b && $c)) ? $d : $e)"},
                     {"$a | $b","($a | $b)"},
                     {"$a | $b | $c","(($a | $b) | $c)"},
                     {"$a ^ $b","($a ^ $b)"},
                     {"$a ^ $b ^ $c","(($a ^ $b) ^ $c)"},
                     {"$a & $b","($a & $b)"},
                     {"$a & $b & $c","(($a & $b) & $c)"},
                     {"$a & $b | $c ^ $d","(($a & $b) | ($c ^ $d))"},
                     {"$a | $b & $c ^ $d","($a | (($b & $c) ^ $d))"},
                     {"$a == $b","($a == $b)"},
                     {"$a === $b","($a === $b)"},
                     {"$a != $b","($a != $b)"},
                     {"$a !== $b","($a !== $b)"},
                     {"$a <> $b","($a <> $b)"},
                     {"$a < $b","($a < $b)"},
                     {"$a <= $b","($a <= $b)"},
                     {"$a > $b","($a > $b)"},
                     {"$a >= $b","($a >= $b)"},
                     {
                         "$a == $b | $c < $d & $e ? $f != $g : $h === $i",
                         "((($a == $b) | (($c < $d) & $e)) ? ($f != $g) : ($h === $i))"
                     },
                     {"1 << 2","(1 << 2)"},
                     {"1 >> 2","(1 >> 2)"},
                     {"1 >> 2 << 3 >> 5","(((1 >> 2) << 3) >> 5)"},
                     {"1 + 2","(1 + 2)"},
                     {"1 - 2","(1 - 2)"},
                     {"$a . $b","($a . $b)"},
                     {"$a << $b >> $c + $d * $e - $f","(($a << $b) >> (($c + ($d * $e)) - $f))"},
                     {"!$a","!$a"},
                     {"!!$a","!!$a"},
                     {"!!! $a","!!!$a"},
                     //                    {"$a instanceof MyClass","$a instanceof MyClass"},
                     //                    {"$a instanceof $b","$a instanceof $b"},
                     {"(Type) $a","($a instanceof Type ? $a : \\trigger_error('Cast failed, the evaluation type of $a must be Type', \\E_RECOVERABLE_ERROR))"},
                    {"(int) $a","(int) $a"},
                     {"~$a","~$a"},
                     {"@$a","@$a"},
                     {"(Type) (MyClass) $a","(($a instanceof MyClass ? $a : \\trigger_error('Cast failed, the evaluation type of $a must be MyClass', \\E_RECOVERABLE_ERROR)) instanceof Type ? ($a instanceof MyClass ? $a : \\trigger_error('Cast failed, the evaluation type of $a must be MyClass', \\E_RECOVERABLE_ERROR)) : \\trigger_error('Cast failed, the evaluation type of ($a instanceof MyClass ? $a : \\trigger_error('Cast failed, the evaluation type of $a must be MyClass', \\E_RECOVERABLE_ERROR)) must be Type', \\E_RECOVERABLE_ERROR))"},
                     {"~~$a","~~$a"},
                     {"@@$a","@@$a"},
                     {"@(Type) ~$a","@(~$a instanceof Type ? ~$a : \\trigger_error('Cast failed, the evaluation type of ~$a must be Type', \\E_RECOVERABLE_ERROR))"},
                     //                    {"clone $a","clone $a"},
                     {"+$a","$a"},
                     {"+1","1"},
                     {"-$a","-$a"},
                     {"-2","-2"},
                     {"+$a + $b","($a + $b)"},
                     {"-$a - $b","(-$a - $b)"},
                     //                    {"clone $a","clone $a"},
                     //                    {"clone $a->a","clone $a->a"},
                     //                    {"new Type","new Type"},
                     //                    {"foo()","foo()"},
                     //                    {"\\foo(1,1+2,3)","\\foo(1, 1 + 2, 3)"},
                     //                    {"$a->foo()","$a->foo()"},
                     //                    {"$a->foo(true || false,123*9)","$a->foo(true || false, 123 * 9)"},
                     //                    {"exit","exit"},
                     //                    {"exit(1)","exit(1)"},
                     //                    {"($a)","($a)"},
                     {"$a++","$a++"},
                     {"$a--","$a--"},
                     {"++$a","++$a"},
                     {"--$a","--$a"},
                     {"$a","$a"},
                     //                    {"$a->a","$a->a"},
                     //                    {"foo()","foo()"},
                     //                    {"\\a\\foo()","\\a\\foo()"},
                     //                    {"self::$a","self::$a"},
                     {"self::a","self::a"},
                     {"parent::a","parent::a"},
                     {"Foo::a","Foo::a"},
                     {"a\\Foo::a","a\\Foo::a"},
                     {"true","true"},
                     {"false","false"},
                     {"null","null"},
                     {"a\\b","a\\b"},
                     {"1","1"},
                     {"2.123","2.123"},
                     {"'a'","'a'"},
                     {"\"asdf\"", "\"asdf\""},
                     {"[1,2,'a'=>3]","[1, 2, 'a' => 3]"},
                     //                    {"(int) clone $a + $b","(int) clone $a + $b"},
                     {"(-$a + $b) * $c","((-$a + $b) * $c)"}, //                    {"!($a instanceof Type) || $a < $b+$c == ~(1 | 3 & 12)","!($a instanceof Type) || $a < $b+$c == ~(1 | 3 & 12)"}
                 };
     }
 }
