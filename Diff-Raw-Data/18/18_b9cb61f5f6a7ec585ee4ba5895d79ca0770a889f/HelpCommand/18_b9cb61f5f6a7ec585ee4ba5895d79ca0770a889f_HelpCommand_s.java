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
 
 package net.jimj.automaton.commands;
 
 import net.jimj.automaton.events.HelpEvent;
 import net.jimj.automaton.events.MessageEvent;
 import net.jimj.automaton.model.User;
 
 public class HelpCommand extends Command {
     @Override
     public String getCommandName() {
         return "help";
     }
 
     @Override
     public void execute(User user, String args) {
         addEvent(new HelpEvent(user, args));
     }
 
     @Override
     public void help(User user) {
        addEvent(new MessageEvent(user, "help <command> - returns help text for the given command."));
     }
 }
