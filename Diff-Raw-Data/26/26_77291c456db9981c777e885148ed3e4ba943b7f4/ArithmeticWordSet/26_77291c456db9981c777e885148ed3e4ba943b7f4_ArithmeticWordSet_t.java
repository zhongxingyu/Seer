 package com.github.mjvesa.f4v.wordset;
 
 import com.github.mjvesa.f4v.BaseWord;
 import com.github.mjvesa.f4v.Interpreter;
 import com.github.mjvesa.f4v.Word;
 
 public class ArithmeticWordSet extends WordSet {
 
 	@Override
 	public Word[] getWords() {
 		return new Word[] {
 
 				new BaseWord("+", "( n1 n2 -- n1 + n2 ) Adds TOS to NOS",
 						Word.POSTPONED) {
 
 					@Override
 					public void execute(Interpreter interpreter) {
 						Integer a = (Integer) interpreter.popData();
 						Integer b = (Integer) interpreter.popData();
 						interpreter.pushData(a + b);
 					}
 				},
 
 				new BaseWord("-",
 						"( n1 n2 -- n1 - n2 ) Substracts TOS from NOS",
 						Word.POSTPONED) {
 
 					@Override
 					public void execute(Interpreter interpreter) {
 						Integer a = (Integer) interpreter.popData();
 						Integer b = (Integer) interpreter.popData();
 						interpreter.pushData(b - a);
 					}
 				},
 
 				new BaseWord("*",
 						"( n1 n2 -- n1 * n2 ) Multiplies TOS with NOS",
 						Word.POSTPONED) {
 
 					@Override
 					public void execute(Interpreter interpreter) {
 						Integer a = (Integer) interpreter.popData();
 						Integer b = (Integer) interpreter.popData();
 						interpreter.pushData(a * b);
 					}
 				},
 
 				new BaseWord("/", "( n1  n2 -- n1 / n2 ) Divides NOS by TOS",
 						Word.POSTPONED) {
 
 					@Override
 					public void execute(Interpreter interpreter) {
 						Integer a = (Integer) interpreter.popData();
 						Integer b = (Integer) interpreter.popData();
						interpreter.pushData(b / a);
 					}
 				}
 		};
 	}
 }
