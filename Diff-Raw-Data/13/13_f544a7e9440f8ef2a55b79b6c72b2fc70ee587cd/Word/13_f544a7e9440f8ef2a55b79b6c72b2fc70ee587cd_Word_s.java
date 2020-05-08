 package com.alexrnl.commons.utils;
 
 import java.io.Serializable;
 import java.util.Objects;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.object.AutoCompare;
 import com.alexrnl.commons.utils.object.AutoHashCode;
 import com.alexrnl.commons.utils.object.Field;
 
 /**
  * Represent a word in a {@link String}.<br />
  * @author Alex
  */
 public final class Word implements Serializable, Comparable<String>, CharSequence {
 	/** Logger */
	private static Logger	lg	= Logger.getLogger(Word.class.getName());

 	/** Serial version UID */
 	private static final long	serialVersionUID	= -2514704200569256612L;
 	
 	/** The word it self */
	private final String	word;
 	/** The index of the beginning of the word */
	private final int		begin;
 	/** The index of the end of the word */
	private final int		end;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param word
 	 *        the word represented (cannot be null).
 	 * @param begin
 	 *        the index of the beginning of the word.
 	 * @param end
 	 *        the index of the end of the word.
 	 */
 	public Word (final String word, final int begin, final int end) {
 		super();
 		Objects.requireNonNull(word);
 		if (word.length() != end - begin) {
 			throw new IllegalArgumentException("Word " + word + " does not fit between "
 					+ begin + " and " + end);
 		}
 		this.word = word;
 		this.begin = begin;
 		this.end = end;
 	}
 
 	/**
 	 * Return the attribute word.
 	 * @return the attribute word.
 	 */
 	@Field
 	public String getWord () {
 		return word;
 	}
 
 	/**
 	 * Return the attribute begin.
 	 * @return the attribute begin.
 	 */
 	@Field
 	public int getBegin () {
 		return begin;
 	}
 
 	/**
 	 * Return the attribute end.
 	 * @return the attribute end.
 	 */
 	@Field
 	public int getEnd () {
 		return end;
 	}
 	
 	@Override
 	public int length () {
 		return word.length();
 	}
 
 	@Override
 	public char charAt (final int index) {
 		return word.charAt(index);
 	}
 
 	@Override
 	public CharSequence subSequence (final int start, final int end) {
 		return word.subSequence(start, end);
 	}
 
 	@Override
 	public int compareTo (final String o) {
 		return word.compareTo(o);
 	}
 
 	@Override
 	public String toString () {
 		return word;
 	}
 	
 	@Override
 	public int hashCode () {
 		return AutoHashCode.getInstance().hashCode(this);
 	}
 
 	@Override
 	public boolean equals (final Object obj) {
 		if (!(obj instanceof Word)) {
 			return false;
 		}
 		return AutoCompare.getInstance().compare(this, (Word) obj);
 	}
 
 	/**
 	 * Return the next word in the {@link String}.<br />
 	 * @param string
 	 *        the string to parse.
 	 * @return the next word.
 	 */
 	public static Word getNextWord (final String string) {
 		Objects.requireNonNull(string);
 		
 		int begin = 0;
 		int end = string.length();
 		
 		boolean beginFound = false;
 		int currentIndex = begin;
 		while (currentIndex < end) {
 			if (!beginFound && allowedInWord(string.charAt(currentIndex))) {
 				begin = currentIndex;
 				beginFound = true;
 			} else if (beginFound && !allowedInWord(string.charAt(currentIndex))) {
 				end = currentIndex;
 			}
 			currentIndex++;
 		}
 		
 		// This is the end of the string and no word have been found
 		if (!beginFound) {
 			return new Word("", currentIndex, end);
 		}
 		
 		final String word = string.substring(begin, end);
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("The next word in '" + string + "' is '" + word + "'");
 		}
 		return new Word(word, begin, end);
 	}
 	
 	/**
 	 * Check if a {@link Character} is allowed in a word.<br />
 	 * <code>true</code> if this is a letter from the alphabet or a dash <code>-</code>.
 	 * @param c
 	 *        the character to check.
 	 * @return <code>true</code> if the character can be found in a word.
 	 */
 	public static boolean allowedInWord (final char c) {
 		return Character.isAlphabetic(c) || c == '-';
 	}
 }
