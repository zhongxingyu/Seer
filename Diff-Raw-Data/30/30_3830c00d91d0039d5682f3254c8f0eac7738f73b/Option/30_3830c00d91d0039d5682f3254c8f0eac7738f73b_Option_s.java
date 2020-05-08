 /*
 * utils - Option.java - Copyright © 2008 David Roden
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
 
 package net.pterodactylus.util.cmdline;
 
 /**
  * Container for {@link CommandLine} options and their values.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Option {
 
 	/** The short name. */
 	private final char shortName;
 
 	/** The long name. */
 	private final String longName;
 
 	/** Whether the option needs a parameter. */
 	private final boolean needsParameter;
 
 	/** The value of the parameter. */
 	private String value;
 
 	/** The option counter. */
 	private int counter;
 
 	/**
 	 * Creates a new option that does not require a parameter.
 	 *
 	 * @param shortName
 	 *            The short name of the option (may be <code>\u0000</code>)
 	 * @param longName
 	 *            The long name of the option (may be <code>null</code>)
 	 */
 	public Option(char shortName, String longName) {
 		this(shortName, longName, false);
 	}
 
 	/**
 	 * Creates a new option.
 	 *
 	 * @param shortName
 	 *            The short name of the option (may be <code>\u0000</code>)
 	 * @param longName
 	 *            The long name of the option (may be <code>null</code>)
 	 * @param needsParameter
 	 *            <code>true</code> if the option requires a parameter,
 	 *            <code>false</code> otherwise
 	 */
 	public Option(char shortName, String longName, boolean needsParameter) {
 		this.shortName = shortName;
 		this.longName = longName;
 		this.needsParameter = needsParameter;
 	}
 
 	/**
 	 * Returns the short name of the option.
 	 *
 	 * @return The short name of the option
 	 */
 	public char getShortName() {
 		return shortName;
 	}
 
 	/**
 	 * Returns the long name of the option.
 	 *
 	 * @return The long name of the option
 	 */
 	public String getLongName() {
 		return longName;
 	}
 
 	/**
 	 * Returns whether the option needs a parameter.
 	 *
 	 * @return <code>true</code> if the option requires a parameter,
 	 *         <code>false</code> otherwise
 	 */
 	public boolean needsParameter() {
 		return needsParameter;
 	}
 
 	/**
 	 * Returns the value of the option’s parameter.
 	 *
 	 * @return The value of the parameter, or <code>null</code> if no parameter
 	 *         was set
 	 */
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * Sets the value of the option’s parameter.
 	 *
 	 * @param value
 	 *            The value of the parameter
 	 */
 	void setValue(String value) {
 		this.value = value;
 	}
 
 	/**
 	 * Returns the counter of the option.
 	 *
 	 * @return The number of times the option was given on the command line
 	 */
 	public int getCounter() {
 		return counter;
 	}
 
 	/**
 	 * Returns whether this option was present in the command line.
 	 *
 	 * @return <code>true</code> if the option was present, <code>false</code>
 	 *         otherwise
 	 */
 	public boolean isPresent() {
 		return counter > 0;
 	}
 
 	/**
 	 * Increments the option counter.
 	 */
 	void incrementCounter() {
 		counter++;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return ((shortName != 0) ? ("-" + shortName) : "") + ((longName != null) ? (((shortName != 0) ? ("|") : ("")) + "--" + longName) : ("")) + (needsParameter ? ("=") : (""));
 	}
 
 }
