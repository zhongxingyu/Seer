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
 
 import java.util.Stack;
 
 import com.zavakid.mockingbird.automaton.NFA;
 import com.zavakid.mockingbird.automaton.State;
 import com.zavakid.mockingbird.common.CharBuffer;
 
 /**
  * NOT Thread safe!
  * 
  * @author Zava 2013-1-28 下午11:01:46
  * @since 0.0.1
  */
 public class Matcher {
 
     private NFA nfa;
 
     public Matcher(String pattern){
         nfa = compile(pattern);
         // may be we can add cache for some hot pattern
     }
 
     public boolean match(String string) {
         nfa.reset();
         for (char ch : string.toCharArray()) {
             nfa.moveStates(ch);
         }
         return nfa.inAcceptState();
     }
 
     protected NFA compile(String pattern) {
         CharBuffer chars = new CharBuffer(pattern);
         NFA newNfa = new NFA();
 
         State tail = newNfa.getInitalState();
         Stack<State> stack = new Stack<State>();
 
         while (chars.remain()) {
             State newState;
             char c = chars.next();
             switch (c) {
                 default:
                     newState = buildDefaultState(tail, stack, chars, c);
                     break;
                 case '.':
                     newState = buildDotState(tail, stack, chars, c);
                     break;
                 case '*':
                     newState = buildStarState(tail, stack, chars, c);
                     break;
                 case '+':
                     newState = buildPlusState(tail, stack, chars, c);
             }
             tail = newState;
         }
 
         tail.markAccept();
         return newNfa;
     }
 
     private static State buildDefaultState(State tail, Stack<State> stack, CharBuffer chars, char c) {
         State newState = State.createState();
         tail.addTransfer(c, newState);
         stack.push(tail);
         return newState;
     }
 
     private static State buildDotState(State tail, Stack<State> stack, CharBuffer chars, char c) {
         State newState = State.createState();
         tail.addTransfer(State.ANY_CHARACTOR, newState);
         stack.push(tail);
         return newState;
     }
 
     private static State buildStarState(State tail, Stack<State> stack, CharBuffer chars, char c) {
         State prev = stack.pop();
         tail.addTransfer(State.EPSILON, prev);
         tail.addTransfer(convertMetaIfNeccessary(chars.lookbefore(1)), tail);
        stack.push(tail);
         return prev;
     }
 
     private static State buildPlusState(State tail, Stack<State> stack, CharBuffer chars, char c) {
         tail.addTransfer(convertMetaIfNeccessary(chars.lookbefore(1)), tail);
         stack.push(tail);
         return tail;
     }
 
     private static Object convertMetaIfNeccessary(char c) {
         switch (c) {
             case '.':
                 return State.ANY_CHARACTOR;
             default:
                 return c;
         }
     }
 }
