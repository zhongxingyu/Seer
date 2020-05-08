 package org.araqne.logdb.metadata;
 
 import java.lang.management.LockInfo;
 import java.lang.management.ManagementFactory;
 import java.lang.management.MonitorInfo;
 import java.lang.management.ThreadInfo;
 import java.lang.management.ThreadMXBean;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.logdb.FieldOrdering;
 import org.araqne.logdb.FunctionRegistry;
 import org.araqne.logdb.MetadataCallback;
 import org.araqne.logdb.MetadataProvider;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.query.parser.CommandOptions;
 import org.araqne.logdb.query.parser.ParseResult;
 import org.araqne.logdb.query.parser.QueryTokenizer;
 
 @Component(name = "logdb-thread-metadata")
 public class ThreadMetadataProvider implements MetadataProvider, FieldOrdering {
 
 	@Requires
 	private MetadataService metadataService;
 
 	@Requires
 	private FunctionRegistry functionRegistry;
 
 	@Validate
 	public void start() {
 		metadataService.addProvider(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (metadataService != null)
 			metadataService.removeProvider(this);
 	}
 
 	@Override
 	public String getType() {
 		return "threads";
 	}
 
 	@Override
 	public List<String> getFieldOrder() {
 		return Arrays.asList("tid", "name", "state", "stacktrace");
 	}
 
 	@Override
 	public void verify(QueryContext context, String queryString) {
 		if (!context.getSession().isAdmin()) {
 			throw new QueryParseException("95040", -1, -1, null);
 			// throw new QueryParseException("no-read-permission", -1);
 		}
 
 		QueryTokenizer.parseOptions(context, queryString, 0, Arrays.asList("prettystack"), functionRegistry);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void query(QueryContext context, String queryString, MetadataCallback callback) {
 		ParseResult r = QueryTokenizer.parseOptions(context, queryString, 0, Arrays.asList("prettystack"), functionRegistry);
 		Map<String, Object> options = (Map<String, Object>) r.value;
 
 		// enable by default
 		boolean prettyStack = true;
 		if (options.get("prettystack") != null) {
 			prettyStack = CommandOptions.parseBoolean(options.get("prettystack").toString());
 		}
 
 		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
 		long[] tids = bean.getAllThreadIds();
 
 		for (ThreadInfo t : bean.getThreadInfo(tids, true, true)) {
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("tid", t.getThreadId());
 			m.put("name", t.getThreadName());
 			m.put("state", t.getThreadState().toString());
 			m.put("stacktrace", prettyStack ? mergeStackTrace(t) : convertStackTrace(t.getStackTrace()));
 
 			callback.onPush(new Row(m));
 		}
 	}
 
 	private String mergeStackTrace(ThreadInfo t) {
 		StackTraceElement[] stacktrace = t.getStackTrace();
 		MonitorInfo[] monitors = t.getLockedMonitors();
 
 		int idx = 0;
 		StringBuilder sb = new StringBuilder();
 		for (StackTraceElement el : stacktrace) {
 			LockInfo lock = t.getLockInfo();
 			String lockOwner = t.getLockOwnerName();
 
 			sb.append(String.format("%s.%s%s\n", el.getClassName(), el.getMethodName(), getFileAndLineNumber(el)));
 
 			if (idx == 0) {
 				if (el.getClassName().equals("java.lang.Object") && el.getMethodName().equals("wait")) {
 					if (lock != null) {
 						sb.append(String.format("- waiting on <0x%016x> (%s)\n", lock.getIdentityHashCode(), lock.getClassName()));
 					}
 				} else if (lock != null) {
 					if (lockOwner == null) {
 						sb.append(String.format("- parking to wait for <0x%016x> (%s)\n", lock.getIdentityHashCode(),
 								lock.getClassName()));
 					} else {
 						sb.append(String.format("- waiting to lock <0x%016x> (%s) owned by \"%s\" @%d\n",
 								lock.getIdentityHashCode(), lock.getClassName(), lockOwner, t.getLockOwnerId()));
 					}
 				}
 			}
 
 			for (MonitorInfo monitor : monitors) {
 				if (monitor.getLockedStackDepth() == idx) {
 					sb.append(String.format("- locked <0x%016x> (%s)\n", monitor.getIdentityHashCode(), monitor.getClassName()));
 				}
 			}
 
 			idx++;
 		}
 		return sb.toString();
 	}
 
 	private String getFileAndLineNumber(StackTraceElement el) {
 		if (el.getFileName() != null && el.getLineNumber() > 0)
 			return String.format(" (%s:%d)", el.getFileName(), el.getLineNumber());
 		else if (el.getFileName() != null && el.getLineNumber() <= 0)
 			return String.format(" (%s)", el.getFileName());
 		else
 			return "";
 	}
 
 	private List<Object> convertStackTrace(StackTraceElement[] stacktrace) {
 		List<Object> l = new ArrayList<Object>();
 		if (stacktrace == null)
 			return null;
 
 		for (StackTraceElement e : stacktrace) {
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("class", e.getClassName());
 			m.put("file", e.getFileName());
 			m.put("line", e.getLineNumber());
 			m.put("method", e.getMethodName());
 			l.add(m);
 		}
 
 		return l;
 	}
 
 	private static class ThreadOrder implements Comparator<Thread> {
 		@Override
 		public int compare(Thread o1, Thread o2) {
 			return (int) (o1.getId() - o2.getId());
 		}
 	}
 }
