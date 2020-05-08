 /*
  * utils - Version.java - Copyright © 2009 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.version;
 
 import java.util.Arrays;
 
 /**
  * Version number container.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Version implements Comparable<Version> {
 
 	/** The version numbers. */
 	private final int[] numbers;
 
 	/** An optional appendix. */
 	private final String appendix;
 
 	/**
 	 * Creates a new version with the given numbers.
 	 *
 	 * @param numbers
 	 *            The numbers of the version
 	 */
 	public Version(int... numbers) {
 		this(null, numbers);
 	}
 
 	/**
 	 * Creates a new version with the given numbers.
 	 *
 	 * @param appendix
 	 *            The optional appendix
 	 * @param numbers
 	 *            The numbers of the version
 	 */
 	public Version(String appendix, int... numbers) {
 		this.numbers = new int[numbers.length];
 		System.arraycopy(numbers, 0, this.numbers, 0, numbers.length);
 		this.appendix = appendix;
 	}
 
 	/**
 	 * Returns the number at the given index.
 	 *
 	 * @param index
 	 *            The index of the number
 	 * @return The number, or <code>0</code> if there is no number at the given
 	 *         index
 	 */
 	public int getNumber(int index) {
 		if (index >= numbers.length) {
 			return 0;
 		}
 		return numbers[index];
 	}
 
 	/**
 	 * Returns the first number of the version.
 	 *
 	 * @return The first number of the version
 	 */
 	public int getMajor() {
 		return getNumber(0);
 	}
 
 	/**
 	 * Returns the second number of the version.
 	 *
 	 * @return The second number of the version
 	 */
 	public int getMinor() {
 		return getNumber(1);
 	}
 
 	/**
 	 * Returns the third number of the version.
 	 *
 	 * @return The third number of the version
 	 */
 	public int getRelease() {
 		return getNumber(2);
 	}
 
 	/**
 	 * Returns the fourth number of the version.
 	 *
 	 * @return The fourth number of the version
 	 */
 	public int getPatch() {
 		return getNumber(3);
 	}
 
 	/**
 	 * Returns the optional appendix.
 	 *
 	 * @return The appendix, or <code>null</code> if there is no appendix
 	 */
 	public String getAppendix() {
 		return appendix;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder versionString = new StringBuilder();
 		for (int number : numbers) {
 			if (versionString.length() > 0) {
 				versionString.append('.');
 			}
 			versionString.append(number);
 		}
 		if (appendix != null) {
 			versionString.append('-').append(appendix);
 		}
 		return versionString.toString();
 	}
 
 	/**
 	 * Parses a version from the given string.
 	 *
 	 * @param versionString
 	 *            The version string to parse
 	 * @return The parsed version, or <code>null</code> if the string could not
 	 *         be parsed
 	 */
 	public static Version parse(String versionString) {
 		String[] componentStrings = versionString.split("\\.");
 		int[] components = new int[componentStrings.length];
 		String appendix = null;
 		int index = -1;
 		for (String componentString : componentStrings) {
 			try {
 				if (index == (componentStrings.length - 2)) {
 					int dash = componentString.indexOf('-');
 					if (dash > -1) {
 						appendix = componentString.substring(dash + 1);
 						componentString = componentString.substring(0, dash);
 					}
 				}
 				components[++index] = Integer.parseInt(componentString);
 			} catch (NumberFormatException nfe1) {
 				return null;
 			}
 		}
 		return new Version(appendix, components);
 	}
 
 	//
 	// INTERFACE Comparable<Version>
 	//
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(Version version) {
 		int lengthDiff = numbers.length - version.numbers.length;
		for (int index = 0; index < Math.min(numbers.length, version.numbers.length); index++) {
 			int diff = numbers[index] - version.numbers[index];
 			if (diff != 0) {
 				return diff;
 			}
 		}
 		return lengthDiff;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if ((obj == null) || !(obj instanceof Version)) {
 			return false;
 		}
 		Version version = (Version) obj;
 		return Arrays.equals(numbers, version.numbers);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return Arrays.hashCode(numbers);
 	}
 
 }
