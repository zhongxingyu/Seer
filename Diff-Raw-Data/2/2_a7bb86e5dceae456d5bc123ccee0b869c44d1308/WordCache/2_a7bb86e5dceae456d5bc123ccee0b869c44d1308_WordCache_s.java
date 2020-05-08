 /**
  * 
  * The MIT License : http://www.opensource.org/licenses/mit-license.php
 
  * Copyright (c) 2010 Kowshik Prakasam
 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  */
 
 package com.android.wordzap;
 
 import java.util.List;
 
 /* 
 * Interface to be implemented by any WordCache
 * 
  * You can use this interface to design a cache to hold words from different languages in the future
  * Just write a class that implements this interface and pass it as a parameter to constructor of class com.android.wordzap.datamodel.LetterGrid
  * 
  */
 
 public interface WordCache {
 
 	// Returns true if word is valid, false otherwise
 	boolean isWordValid(String word);
 	
 	//Returns list of all cached valid words
 	List< String > getValidWords();
 
 	//Returns a random cached word
 	String getRandomWord();
 	
 	//Returns a random cached word not in the list : wordList
 	String getRandomWord(final List<String> wordList);
 	
 }
