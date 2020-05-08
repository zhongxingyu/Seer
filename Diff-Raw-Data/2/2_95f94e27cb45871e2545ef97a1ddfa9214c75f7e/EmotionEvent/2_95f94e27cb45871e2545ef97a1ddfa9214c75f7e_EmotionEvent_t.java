 /*
  * Copyright (c) <2013> <Jim Johnson jimj@jimj.net>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.jimj.automaton.events;
 
 import net.jimj.automaton.model.User;
 
 public class EmotionEvent extends Event {
     public enum Feeling {
         SASSY, CONFUSED, SORRY
     }
     public static final String SASS[] = {
         "BEEP BOOP fuck you.",
         ".no",
         "Sure thing cheif!"
     };
 
     public static final String HUH[] = {
         "huh?",
         "what?",
         "sorry?",
         "I'm afraid I can't let you do that $nick.",
         "Looks like a case of PEBKAC.",
         "You're not very smart are you?",
         "I'm here to .help"
     };
 
     public static final String SORRY[] = {
         "I suck :(",
         "I'm sorry $nick.",
         "Something went wrong.",
         "Please try again, $nick."
     };
 
     private Feeling feeling;
 
     public EmotionEvent(User user, Feeling feeling) {
         super(user);
         this.feeling = feeling;
     }
 
     public String getSurlyMessage() {
         String message = "";
         if(feeling == Feeling.SASSY) {
             message = SASS[RANDOM.nextInt(SASS.length)];
         }else if(feeling == Feeling.CONFUSED){
             message = HUH[RANDOM.nextInt(HUH.length)];
         }else if(feeling == Feeling.SORRY) {
             message = SORRY[RANDOM.nextInt(SORRY.length)];
         }
 
         if(message.contains("$nick")) {
            message = message.replace("$nick", getUser().getNick());
         }
 
         return message;
     }
 }
