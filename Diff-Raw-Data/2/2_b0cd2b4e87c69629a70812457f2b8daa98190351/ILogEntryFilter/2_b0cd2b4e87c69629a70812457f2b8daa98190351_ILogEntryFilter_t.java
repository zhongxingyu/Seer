 package org.logparser;
 
 /**
  * Specifies the protocol required of log entry filters.
  * 
  * Typically, an {@link ILogEntryFilter} implementation will parse a log entry string
  * and return a corresponding log entry E if successful.
  * 
 * {@link ILogEntryFilter} implementations can be chained.
 * 
  * @author jorge.decastro
  * 
  * @param <E> the type of log entry.
  */
 public interface ILogEntryFilter<E> {
 	/**
 	 * Parse the given {@code text} and return a populated entry E.
 	 * 
 	 * @param text the {@code text} to parse.
 	 * @return a parsed entry of type E, or null if the filter is unable to parse
 	 *         {@code text}.
 	 */
 	public E parse(String text);
 }
