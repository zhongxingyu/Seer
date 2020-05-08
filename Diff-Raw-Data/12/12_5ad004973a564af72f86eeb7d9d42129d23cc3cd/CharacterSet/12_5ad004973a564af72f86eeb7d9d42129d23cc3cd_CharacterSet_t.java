 package com.wesabe.servlet.normalizers.util;
 
 import java.util.Set;
 
 import com.google.common.collect.ForwardingSet;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSet.Builder;
 
 /**
  * A set of characters.
  * 
  * @author coda
  */
 public class CharacterSet extends ForwardingSet<Character> {
 
 	/**
 	 * Creates a character set from an array of {@code char}.
 	 * 
 	 * @param chars
 	 *            the elements of the character set
 	 * @return a {@link CharacterSet} containing {@code chars}
 	 */
 	public static CharacterSet of(char... chars) {
 		Builder<Character> builder = ImmutableSet.builder();
 		for (char c : chars) {
 			builder.add(Character.valueOf(c));
 		}
 		return new CharacterSet(builder.build());
 	}
 
 	/**
 	 * Creates a character set from a string.
 	 * 
 	 * @param chars
 	 *            a string containing the elements of the character set
 	 * @return a {@link CharacterSet} containing the characters in {@code chars}
 	 */
 	public static CharacterSet of(String chars) {
 		return of(chars.toCharArray());
 	}
 
 	private final ImmutableSet<Character> characters;
 
 	private CharacterSet(ImmutableSet<Character> characters) {
 		this.characters = characters;
 	}
 
 	@Override
 	protected Set<Character> delegate() {
 		return characters;
 	}
 
 	/**
 	 * A primitive equivalent of {@link #contains(Object)}.
 	 */
 	public boolean contains(char character) {
 		return characters.contains(Character.valueOf(character));
 	}
 	
 	/**
 	 * Returns {@code true} if {@code s} is composed exclusively of the
 	 * characters in {@code this}.
 	 */
 	public boolean composes(String s) {
		if (s == null) {
			return false;
		}
		
 		for (int i = 0; i < s.length(); i++) {
 			if (!contains(s.charAt(i))) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 }
