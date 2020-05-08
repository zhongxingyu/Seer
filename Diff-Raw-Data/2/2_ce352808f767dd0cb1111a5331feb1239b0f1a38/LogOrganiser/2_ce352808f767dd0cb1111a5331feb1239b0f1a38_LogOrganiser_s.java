 package org.logparser;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.jcip.annotations.Immutable;
 
 /**
  * Responsible for grouping messages of the same type together, according to given criteria.
  * 
  * @author jorge.decastro
  */
 @Immutable
 public class LogOrganiser<E> {
 	private final ILogParser<E> logParser;
 	private final IStatsViewFactory<E> statsViewFactory;
 	private final Map<String, IStatsView<E>> organisedByKey = new HashMap<String, IStatsView<E>>();
 
 	public LogOrganiser(final ILogParser<E> logParser, final IStatsViewFactory<E> statsViewFactory) {
 		Preconditions.checkNotNull(logParser);
 		Preconditions.checkNotNull(statsViewFactory);
 		this.logParser = logParser;
 		this.statsViewFactory = statsViewFactory;
 	}
 
 	private Map<String, IStatsView<E>> groupBy(final List<E> entries, final String groupByKey) {
 		organisedByKey.clear();
 
 		String key = null;
 		for (E entry : entries) {
 			try {
 				/**
 				 * hmm, not pretty I know. But it's a choice of either
 				 * reflective access to a private field or passing the method
 				 * *name* as the key e.g.:
 				 * 
 				 * <pre>
 				 * Class aClass = entry.getClass();
 				 * Method m = aClass.getMethod("getUrl");
 				 * key = (String) m.invoke(entry);
 				 * </pre>
 				 */
 				Field field = entry.getClass().getDeclaredField(groupByKey);
 				field.setAccessible(true);
 				key = (String) field.get(entry);
 			} catch (Throwable t) {
				// TODO handle properly
 			}
 
 			// new request? create a new stats wrapper for it
 			if (!organisedByKey.containsKey(key)) {
 				IStatsView<E> stats = statsViewFactory.newInstance();
 				stats.add(entry);
 				organisedByKey.put(key, stats);
 			} else {
 				IStatsView<E> existingEntriesList = organisedByKey.get(key);
 				existingEntriesList.add(entry);
 			}
 		}
 
 		return organisedByKey;
 	}
 
 	public Map<String, IStatsView<E>> groupBy(final String groupByKey) {
 		Preconditions.checkNotNull(groupByKey);
 		final List<E> accessEntries = logParser.getParsedEntries();
 		return groupBy(accessEntries, groupByKey);
 	}
 }
