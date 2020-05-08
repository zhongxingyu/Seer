 package org.logparser.io;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import net.jcip.annotations.Immutable;
 
 import org.apache.log4j.Logger;
 import org.logparser.Config;
 import org.logparser.ILogEntryFilter;
 import org.logparser.ILogFilter;
 import org.logparser.ITimestampedEntry;
 import org.logparser.LogSnapshot;
 
 import com.google.common.base.Preconditions;
 import com.google.common.io.Closeables;
 
 /**
  * Implementation of {@link ILogFilter} that processes a log file one line at a
  * time. It is expected to have slightly worse performance than
 * an "in memory" implementation but with better memory utilization.
  * 
  * @author jorge.decastro
  * 
  * @param <E> the type of elements held by this {@link ILogFilter}.
  */
 @Immutable
 public class LineByLineLogFilter<E extends ITimestampedEntry> implements ILogFilter<E> {
 	private static final Logger LOGGER = Logger.getLogger(LineByLineLogFilter.class.getName());
 	private final List<ILogEntryFilter<E>> logEntryFilters;
 	private final LogSnapshot<E> logSnapshot;
 
 	public LineByLineLogFilter(final Config config, final ILogEntryFilter<E>... messageFilter) {
 		this(config, Arrays.asList(messageFilter));
 	}
 
 	public LineByLineLogFilter(final Config config, final List<ILogEntryFilter<E>> messageFilters) {
 		Preconditions.checkNotNull(messageFilters);
 		for (ILogEntryFilter<E> filter : messageFilters) {
 			Preconditions.checkNotNull(filter);
 		}
 		this.logEntryFilters = Collections.unmodifiableList(messageFilters);
 		this.logSnapshot = new LogSnapshot<E>(config);
 	}
 
 	public LogSnapshot<E> filter(final String filepath) {
 		Preconditions.checkNotNull(filepath);
 		BufferedReader in = null;
 		int count = 0;
 		try {
 			in = new BufferedReader(new FileReader(filepath));
 			String str;
 			E entry;
 			while ((str = in.readLine()) != null) {
 				count++;
 				entry = applyFilters(str, logEntryFilters);
 				logSnapshot.consume(entry);
 			}
 			in.close();
 		} catch (IOException ioe) {
 			LOGGER.warn(String.format("IO error reading file '%s'", filepath), ioe);
 		} finally {
 			Closeables.closeQuietly(in);
 		}
 		return logSnapshot;
 	}
 
 	private E applyFilters(final String toParse, final List<ILogEntryFilter<E>> filters) {
 		E entry = null;
 		for (ILogEntryFilter<E> filter : filters) {
 			entry = filter.parse(toParse);
 			if (entry != null) {
 				break;
 			}
 		}
 		return entry;
 	}
 }
