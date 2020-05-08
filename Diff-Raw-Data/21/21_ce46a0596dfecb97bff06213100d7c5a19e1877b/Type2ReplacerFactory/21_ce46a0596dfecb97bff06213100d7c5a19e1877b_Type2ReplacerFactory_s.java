 package ch.unibe.scg.cc;
 
 import jregex.Pattern;
 import ch.unibe.scg.cc.regex.Replace;
 
 public class Type2ReplacerFactory extends ReplacerProvider {
 
 	public Replace make00WordsIntoTs() {
 		Pattern word = new Pattern("[a-zA-Z]+");
 		return new Replace(word, "t");
 	}
 
 	public Replace make01NumbersIntoOnes() {
 		Pattern number = new Pattern("\\d+");
 		return new Replace(number, "1");
 	}
 
 	public Replace make02Tokenize() {
 		Pattern invoke = new Pattern("(\\w)\\.(\\w)");
 		return new Replace(invoke, "$1. $2");
 	}
 
 }
