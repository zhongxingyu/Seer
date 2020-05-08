 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage.script;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.araqne.api.Script;
 import org.araqne.api.ScriptArgument;
 import org.araqne.api.ScriptContext;
 import org.araqne.api.ScriptUsage;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigService;
 import org.araqne.log.api.FieldDefinition;
 import org.araqne.log.api.WildcardMatcher;
 import org.araqne.logstorage.*;
 import org.araqne.logstorage.engine.ConfigUtil;
 import org.araqne.logstorage.engine.Constants;
 import org.araqne.logstorage.engine.LogTableSchema;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LogStorageScript implements Script {
 	private final Logger logger = LoggerFactory.getLogger(LogStorageScript.class);
 	private ScriptContext context;
 	private LogTableRegistry tableRegistry;
 	private LogStorage storage;
 	private LogStorageMonitor monitor;
 	private ConfigService conf;
 	private LogFileServiceRegistry lfsRegistry;
 	private LogCryptoProfileRegistry cryptoRegistry;
 
 	public LogStorageScript(LogTableRegistry tableRegistry, LogStorage archive, LogStorageMonitor monitor, ConfigService conf,
 			LogFileServiceRegistry lfsRegistry, LogCryptoProfileRegistry cryptoRegistry) {
 		this.tableRegistry = tableRegistry;
 		this.storage = archive;
 		this.monitor = monitor;
 		this.conf = conf;
 		this.lfsRegistry = lfsRegistry;
 		this.cryptoRegistry = cryptoRegistry;
 	}
 
 	@Override
 	public void setScriptContext(ScriptContext context) {
 		this.context = context;
 	}
 
 	public void cryptoProfiles(String[] args) {
 		context.println("Crypto Profiles");
 		context.println("-----------------");
 		for (LogCryptoProfile p : cryptoRegistry.getProfiles()) {
 			context.println(p);
 		}
 	}
 
 	@ScriptUsage(description = "create crypto profile", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "crypto profile name") })
 	public void createCryptoProfile(String[] args) throws InterruptedException {
 		LogCryptoProfile p = new LogCryptoProfile();
 		p.setName(args[0]);
 
 		context.print("pkcs12 path? ");
 		String line = context.readLine().trim();
 		p.setFilePath(line);
 
 		context.print("pkcs12 password? ");
 		line = context.readLine();
 		p.setPassword(line);
 
 		context.print("cipher algorithm? ");
 		line = context.readLine().trim();
 		p.setCipher(line.trim().isEmpty() ? null : line);
 
 		context.print("digest algorithm? ");
 		line = context.readLine().trim();
 		p.setDigest(line.trim().isEmpty() ? null : line);
 
 		cryptoRegistry.addProfile(p);
 		context.println("created");
 	}
 
 	@ScriptUsage(description = "create crypto profile", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "crypto profile name") })
 	public void removeCryptoProfile(String[] args) {
 		cryptoRegistry.removeProfile(args[0]);
 		context.println("removed");
 	}
 
 	public void forceRetentionCheck(String[] args) {
 		monitor.forceRetentionCheck();
 		context.println("triggered");
 	}
 
 	@ScriptUsage(description = "set retention policy", arguments = { @ScriptArgument(name = "table name", type = "string", description = "table name") })
 	public void retention(String[] args) {
 		String tableName = args[0];
 		LogRetentionPolicy p = storage.getRetentionPolicy(tableName);
 		context.println(p.getRetentionDays() + "days");
 	}
 
 	@ScriptUsage(description = "set retention policy", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "retention days", type = "int", description = "retention days (0 for infinite)") })
 	public void setRetention(String[] args) {
 		LogRetentionPolicy p = new LogRetentionPolicy();
 		p.setTableName(args[0]);
 		p.setRetentionDays(Integer.valueOf(args[1]));
 
 		storage.setRetentionPolicy(p);
 		context.println("set");
 	}
 
 	@ScriptUsage(description = "migrate old properties to new confdb metadata")
 	public void migrate(String[] args) {
 		context.println("migrate table metadata from properties to confdb");
 
 		FileInputStream is = null;
 		try {
 			ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 			is = new FileInputStream(new File(System.getProperty("araqne.data.dir"), "araqne-logstorage/tables"));
 
 			Properties p = new Properties();
 			p.load(is);
 
 			for (Object key : p.keySet()) {
 				String tableName = key.toString();
 				if (!tableName.contains(".")) {
 					int id = Integer.valueOf(p.getProperty(tableName));
 					LogTableSchema t = new LogTableSchema(id, tableName);
 					db.add(t, "araqne-logstorage", tableName + " metadata is migrated from old version");
 				}
 			}
 		} catch (IOException e) {
 			context.println(e.getMessage());
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	@ScriptUsage(description = "print table metadata", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "table metadata key", type = "string", description = "key", optional = true),
 			@ScriptArgument(name = "table metadata value", type = "string", description = "value", optional = true) })
 	public void table(String[] args) {
 		String tableName = args[0];
 
 		if (!tableRegistry.exists(tableName)) {
 			context.println("table not found");
 			return;
 		}
 
 		if (args.length == 1) {
 			context.println("Table " + args[0]);
 			context.println();
 			context.println("Table Metadata");
 			context.println("----------------");
 			for (String key : tableRegistry.getTableMetadataKeys(tableName)) {
 				String value = tableRegistry.getTableMetadata(tableName, key);
 				context.println(key + "=" + value);
 			}
 
 			long total = 0;
 			File dir = storage.getTableDirectory(tableName);
 			if (dir.exists()) {
 				for (File f : dir.listFiles())
 					total += f.length();
 			}
 
 			List<FieldDefinition> fields = tableRegistry.getTableFields(tableName);
 			if (fields != null) {
 				context.println("");
 				context.println("Table Schema");
 				context.println("---------------");
 				for (FieldDefinition field : fields) {
 					String line = null;
 					if (field.getLength() > 0)
 						line = field.getName() + "\t" + field.getType() + "(" + field.getLength() + ")";
 					line = field.getName() + "\t" + field.getType();
 
 					context.println(line);
 				}
 			}
 
 			context.println();
 			context.println("Storage information");
 			context.println("---------------------");
 			context.println("Data path: " + storage.getTableDirectory(tableName));
 			NumberFormat nf = NumberFormat.getNumberInstance();
 			context.println("Consumption: " + nf.format(total) + " bytes");
 		} else if (args.length == 2) {
 			String value = tableRegistry.getTableMetadata(tableName, args[1]);
 			context.println("unset " + value);
 			tableRegistry.unsetTableMetadata(tableName, args[1]);
 		} else if (args.length == 3) {
 			tableRegistry.setTableMetadata(tableName, args[1], args[2]);
 			context.printf("set %s to %s\n", args[1], args[2]);
 		}
 	}
 
 	@ScriptUsage(description = "list tables", arguments = { @ScriptArgument(name = "filter", type = "string", description = "table name filter", optional = true) })
 	public void tables(String[] args) {
 		String filter = null;
 		if (args.length > 0)
 			filter = args[0];
 
 		context.println("Tables");
 		context.println("--------");
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
 		ArrayList<TableInfo> tables = new ArrayList<TableInfo>();
 		for (String tableName : tableRegistry.getTableNames()) {
 			if (filter != null && !tableName.contains(filter))
 				continue;
 
 			int tableId = tableRegistry.getTableId(tableName);
 			Iterator<Date> it = storage.getLogDates(tableName).iterator();
 			Date lastDay = null;
 			if (it.hasNext())
 				lastDay = it.next();
 
 			String lastRecord = lastDay != null ? dateFormat.format(lastDay) : "none";
 			tables.add(new TableInfo(tableId, "[" + tableId + "] " + tableName + ": " + lastRecord));
 		}
 
 		// sort by id and print all
 		Collections.sort(tables, new Comparator<TableInfo>() {
 			@Override
 			public int compare(TableInfo o1, TableInfo o2) {
 				return o1.id - o2.id;
 			}
 		});
 
 		for (TableInfo t : tables)
 			context.println(t.info);
 	}
 
 	private class TableInfo {
 		public int id;
 		public String info;
 
 		public TableInfo(int id, String info) {
 			this.id = id;
 			this.info = info;
 		}
 	}
 
 	public void open(String[] args) {
 		storage.start();
 		context.println("opened");
 	}
 
 	public void close(String[] args) {
 		storage.stop();
 		context.println("closed");
 	}
 
 	public void reload(String[] args) {
 		storage.reload();
 	}
 
 	@ScriptUsage(description = "create new table", arguments = {
 			@ScriptArgument(name = "name", type = "string", description = "log table name"),
 			@ScriptArgument(name = "type", type = "string", description = "log file type (v1, v2, etc)") })
 	public void createTable(String[] args) {
 		Map<String, String> metadata = new HashMap<String, String>();
 		if (args.length > 2) {
 			for (int i = 2; i < args.length; i++) {
 				String[] pair = args[i].split("=");
 				if (pair.length != 2)
 					continue;
 
 				metadata.put(pair[0], pair[1]);
 			}
 		}
 
 		storage.createTable(args[0], args[1], metadata);
 		context.println("table created");
 	}
 
 	@ScriptUsage(description = "rename table", arguments = {
 			@ScriptArgument(name = "current table name", type = "string", description = "current log table name"),
 			@ScriptArgument(name = "new table name", type = "string", description = "new table name") })
 	public void renameTable(String[] args) {
 		tableRegistry.renameTable(args[0], args[1]);
 		context.println("ok");
 	}
 
 	@ScriptUsage(description = "drop log table", arguments = { @ScriptArgument(name = "name", type = "string", description = "log table name") })
 	public void dropTable(String[] args) {
 		try {
 			storage.dropTable(args[0]);
 			context.println("table dropped");
 		} catch (Exception e) {
 			context.println(e.getMessage());
 		}
 	}
 
 	@ScriptUsage(description = "set table fields", arguments = { @ScriptArgument(name = "table name", type = "string", description = "table name") })
 	public void setFields(String[] args) {
 		String tableName = args[0];
 
 		try {
 			List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
 			context.println("Use 'name type(length)' format. Length can be omitted.");
 			context.println("Allowed types: string, short, int, long, float, double, date, and bool");
 			context.println("Enter empty line to finish.");
 
 			while (true) {
 				context.print("field definition? ");
 				String line = context.readLine();
 				if (line.isEmpty())
 					break;
 
 				FieldDefinition field = FieldDefinition.parse(line);
 				fields.add(field);
 			}
 
 			tableRegistry.setTableFields(tableName, fields);
 			context.println("schema changed");
 		} catch (Throwable t) {
 			context.println("cannot update schema: " + t.getMessage());
 			logger.error("araqne logstorage: cannot update table fields", t);
 		}
 	}
 
 	@ScriptUsage(description = "unset table fields", arguments = { @ScriptArgument(name = "table name", type = "string", description = "table name") })
 	public void unsetFields(String[] args) {
 		tableRegistry.setTableFields(args[0], null);
 		context.println("schema changed");
 	}
 
 	@ScriptUsage(description = "get logs", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "from", type = "string", description = "yyyyMMddHH format"),
 			@ScriptArgument(name = "to", type = "string", description = "yyyyMMddHH format"),
 			@ScriptArgument(name = "offset", type = "int", description = "offset"),
 			@ScriptArgument(name = "limit", type = "int", description = "log limit") })
 	public void logs(String[] args) throws ParseException {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
 		String tableName = args[0];
 		Date from = dateFormat.parse(args[1]);
 		Date to = dateFormat.parse(args[2]);
 		int offset = Integer.valueOf(args[3]);
 		int limit = Integer.valueOf(args[4]);
 
 		try {
 			storage.search(tableName, from, to, offset, limit, new LogSearchCallback() {
 
 				@Override
 				public void onLogBatch(String tableName, List<Log> logBatch) {
 					for (Log log : logBatch)
 						context.println(log.toString());
 				}
 
 				@Override
 				public boolean isInterrupted() {
 					return false;
 				}
 
 				@Override
 				public void interrupt() {
 				}
 			});
 		} catch (InterruptedException e) {
 			context.println("interrupted");
 		}
 	}
 
 	@ScriptUsage(description = "search table", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "log table name"),
 			@ScriptArgument(name = "from", type = "string", description = "from"),
 			@ScriptArgument(name = "to", type = "string", description = "to"),
 			@ScriptArgument(name = "limit", type = "int", description = "count limit") })
 	public void searchTable(String[] args) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			String tableName = args[0];
 			Date from = dateFormat.parse(args[1]);
 			Date to = dateFormat.parse(args[2]);
 			int limit = Integer.parseInt(args[3]);
 
 			long begin = new Date().getTime();
 
 			LogSearchCallback callback = new PrintCallback();
 			storage.search(tableName, from, to, limit, callback);
 
 			long end = new Date().getTime();
 
 			context.println("elapsed: " + (end - begin) + "ms");
 		} catch (Exception e) {
 			context.println(e.getMessage());
 		}
 	}
 
 	private class PrintCallback implements LogSearchCallback {
 		@Override
 		public void interrupt() {
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			return false;
 		}
 
 		@Override
 		public void onLogBatch(String tableName, List<Log> logBatch) {
 			for (Log log : logBatch) {
 				Map<String, Object> m = log.getData();
 				context.print(log.getId() + ": ");
 				for (String key : m.keySet()) {
 					context.print(key + "=" + m.get(key) + ", ");
 				}
 				context.println("");
 			}
 		}
 	}
 
 	@ScriptUsage(description = "print all parameters")
 	public void parameters(String[] args) {
 		for (Constants c : Constants.values()) {
 			context.println(c.getName() + ": " + ConfigUtil.get(conf, c));
 		}
 	}
 
 	@ScriptUsage(description = "set parameters", arguments = {
 			@ScriptArgument(name = "key", type = "string", description = "parameter key"),
 			@ScriptArgument(name = "value", type = "string", description = "parameter value") })
 	public void setParameter(String[] args) {
 		Constants configKey = Constants.parse(args[0]);
 		if (configKey == null) {
 			context.println("invalid key name");
 			return;
 		}
 
 		String value = null;
 		if (configKey.getType().equals("string")) {
 			value = args[1];
 		} else if (configKey.getType().equals("int")) {
 			int interval = 0;
 			try {
 				interval = Integer.parseInt(args[1]);
 				value = Integer.toString(interval);
 			} catch (NumberFormatException e) {
 				context.println("invalid parameter format");
 				return;
 			}
 		}
 
 		ConfigUtil.set(conf, configKey, value);
 		context.println("set");
 	}
 
 	public static void main(String[] args) {
 		String s = "/hdd/hhsonbo/*/*.log";
 
 		List<File> files = getMatchingFiles(s, null);
 
 		for (File f : files) {
 			System.out.println(f.getAbsolutePath());
 		}
 	}
 
 	private static List<File> getMatchingFiles(String s, File workingDir) {
 		File root = getListRoot(s);
 
 		Stack<File> dirs = new Stack<File>();
 		dirs.push(root);
 
 		if (workingDir == null) {
			s = new File(s).getAbsolutePath();
 		} else {
			s = new File(workingDir, s).getAbsolutePath();
 		}
 
 		Pattern p = WildcardMatcher.buildPattern(s);
 
 		List<File> result = new ArrayList<File>();
 		while (!dirs.isEmpty()) {
 			File cur = dirs.pop();
 			File[] files = null;
 			if ((files = cur.listFiles()) == null)
 				continue;
 			for (File f : files) {
 				if (f.isDirectory()) {
 					dirs.push(f);
 					continue;
 				}
				if (p.matcher(f.getAbsolutePath()).matches())
 					result.add(f);
 			}
 		}
 
 		Collections.sort(result);
 
 		return result;
 	}
 
 	private static File getListRoot(String s) {
 		File parent = new File(s);
 		while (true) {
 			if (parent.getAbsolutePath().contains("*"))
 				parent = parent.getParentFile();
			else 
 				break;
 		}
 		return parent;
 	}
 
 	@ScriptUsage(description = "import text log file", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "file path", type = "string", description = "text log file path"),
 			@ScriptArgument(name = "offset", type = "int", description = "skip offset", optional = true),
 			@ScriptArgument(name = "limit", type = "int", description = "load limit count", optional = true) })
 	public void importTextFile(String[] args) throws IOException {
 		String tableName = args[0];
 		if (args[1].contains("*")) {
 			int offset = 0;
 			int limit = Integer.MAX_VALUE;
 
 			List<File> files = getMatchingFiles(args[1], null);
 
 			int cur = 1;
 			for (File f : files) {
 				String startedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
 				context.printf("[%s] loading (%d/%d) %s\n", startedAt, cur, files.size(), f.getAbsolutePath());
 				importFile(tableName, f, offset, limit);
 				cur += 1;
 			}
 		} else {
 			File file = new File(args[1]);
 			int offset = 0;
 			if (args.length > 2)
 				offset = Integer.valueOf(args[2]);
 
 			int limit = Integer.MAX_VALUE;
 			if (args.length > 3)
 				limit = Integer.valueOf(args[3]);
 
 			importFile(tableName, file, offset, limit);
 		}
 	}
 
 	private void importFile(String tableName, File file, int offset, int limit) throws IOException {
 		InputStream is = null;
 		try {
 			is = new FileInputStream(file);
 			if (file.getName().endsWith(".gz")) {
 				is = new GZIPInputStream(is);
 			}
 			importFromStream(tableName, is, offset, limit);
 		} catch (Exception e) {
 			context.println("import failed, " + e.getMessage());
 			logger.error("araqne logstorage: cannot import text file " + file.getAbsolutePath(), e);
 		} finally {
 			if (is != null)
 				is.close();
 		}
 	}
 
 	@ScriptUsage(description = "import zipped text log file", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "zip file path", type = "string", description = "zip file path"),
 			@ScriptArgument(name = "entry path", type = "string", description = "zip entry of text log file path"),
 			@ScriptArgument(name = "offset", type = "int", description = "skip offset", optional = true),
 			@ScriptArgument(name = "limit", type = "int", description = "load limit count", optional = true) })
 	public void importZipFile(String[] args) throws ZipException, IOException {
 		String tableName = args[0];
 		String filePath = args[1];
 		String entryPath = args[2];
 		File file = new File(args[1]);
 		int offset = 0;
 		if (args.length > 3)
 			offset = Integer.valueOf(args[3]);
 
 		int limit = Integer.MAX_VALUE;
 		if (args.length > 4)
 			limit = Integer.valueOf(args[4]);
 
 		ZipFile zipFile = new ZipFile(file);
 		ZipEntry entry = zipFile.getEntry(entryPath);
 		if (entry == null) {
 			context.println("entry [" + entryPath + "] not found in zip file [" + filePath + "]");
 			return;
 		}
 
 		InputStream is = null;
 		try {
 			is = zipFile.getInputStream(entry);
 			importFromStream(tableName, is, offset, limit);
 		} catch (Exception e) {
 			context.println("import failed, " + e.getMessage());
 			logger.error("araqne logstorage: cannot import zipped text file " + file.getAbsolutePath(), e);
 		} finally {
 			if (is != null)
 				is.close();
 		}
 	}
 
 	private void importFromStream(String tableName, InputStream fis, int offset, int limit) throws IOException {
 		Date begin = new Date();
 		long count = 0;
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis), 16384 * 1024); // 16MB
 		String line = null;
 
 		ArrayList<Log> buf = new ArrayList<Log>(1000);
 
 		int i = 0;
 		long lastCheck = System.currentTimeMillis();
 		long lastCount = 0;
 		double lps = 0.0;
 		while (true) {
 			line = br.readLine();
 			if (line == null)
 				break;
 
 			if (count >= limit)
 				break;
 
 			if (i++ < offset)
 				continue;
 
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("line", line);
 
 			Log log = new Log(tableName, new Date(), m);
 			buf.add(log);
 
 			count++;
 
 			if (count % 1000 == 0) {
 				try {
 					storage.write(buf);
 					buf = new ArrayList<Log>(1000);
 				} catch (IllegalArgumentException e) {
 					context.println("skip " + line + ", " + e.getMessage());
 				}
 			}
 
 			if (count % 10000 == 0) {
 				context.print("\rloaded " + count + (lps != 0.0 ? String.format(" (%.2f lps)", lps) : ""));
 				long curTime = System.currentTimeMillis();
 				if (curTime - lastCheck > 1000) {
 					lps = (count - lastCount) / ((double) (curTime - lastCheck) / 1000);
 					lastCount = count;
 					lastCheck = curTime;
 				}
 			}
 		}
 
 		if (buf.size() > 0)
 			storage.write(buf);
 
 		long milliseconds = new Date().getTime() - begin.getTime();
 		long speed = count * 1000 / milliseconds;
 		context.println("\rloaded " + count + " logs in " + milliseconds + " ms, " + speed + " logs/sec");
 	}
 
 	@ScriptUsage(description = "benchmark table fullscan", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "from", type = "string", description = "date from (yyyyMMdd format)"),
 			@ScriptArgument(name = "to", type = "string", description = "date to (yyyyMMdd format)") })
 	public void fullscan(String[] args) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 			String tableName = args[0];
 			Date from = dateFormat.parse(args[1]);
 			Date to = dateFormat.parse(args[2]);
 
 			LogCounter counter = new LogCounter();
 			Date timestamp = new Date();
 			storage.search(tableName, from, to, Integer.MAX_VALUE, counter);
 			long elapsed = new Date().getTime() - timestamp.getTime();
 
 			context.println("total count: " + counter.getCount() + ", elapsed: " + elapsed + "ms");
 		} catch (ParseException e) {
 			context.println("invalid date format");
 		} catch (InterruptedException e) {
 			context.println("interrupted");
 		}
 	}
 
 	public void flush(String[] args) {
 		storage.flush();
 	}
 
 	private static class LogCounter implements LogSearchCallback {
 		private int count = 0;
 
 		@Override
 		public void onLogBatch(String tableName, List<Log> logBatch) {
 			count += logBatch.size();
 		}
 
 		public int getCount() {
 			return count;
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			return false;
 		}
 
 		@Override
 		public void interrupt() {
 		}
 	}
 
 	@ScriptUsage(description = "print all online writer statuses")
 	public void writers(String[] args) {
 		context.println("Online Writers");
 		context.println("-----------------");
 		for (LogWriterStatus s : storage.getWriterStatuses()) {
 			context.println(s);
 		}
 	}
 
 	public void supportedLogFileTypes(String[] args) {
 		for (String type : lfsRegistry.getServiceTypes()) {
 			context.println(type);
 		}
 	}
 
 	@ScriptUsage(description = "print current engine configs", arguments = { @ScriptArgument(name = "engine type", type = "string", description = "v1, v2, or else") })
 	public void engineConfigs(String[] args) {
 		LogFileService s = lfsRegistry.getLogFileService(args[0]);
 		if (s == null) {
 			context.println("no engine found");
 			return;
 		}
 
 		context.println(s.getConfigs().toString());
 	}
 
 	@ScriptUsage(description = "set engine config", arguments = {
 			@ScriptArgument(name = "engine type", type = "string", description = "v1, v2, or else"),
 			@ScriptArgument(name = "key", type = "string", description = "config key"),
 			@ScriptArgument(name = "value", type = "string", description = "config value") })
 	public void setEngine(String[] args) {
 		LogFileService s = lfsRegistry.getLogFileService(args[0]);
 		if (s == null) {
 			context.println("no engine found");
 			return;
 		}
 
 		s.setConfig(args[1], args[2]);
 		context.println("set");
 	}
 
 	@ScriptUsage(description = "unset engine config", arguments = {
 			@ScriptArgument(name = "engine type", type = "string", description = "v1, v2, or else"),
 			@ScriptArgument(name = "key", type = "string", description = "config key") })
 	public void unsetEngine(String[] args) {
 		LogFileService s = lfsRegistry.getLogFileService(args[0]);
 		if (s == null) {
 			context.println("no engine found");
 			return;
 		}
 
 		s.unsetConfig(args[1]);
 		context.println("unset");
 	}
 
 	@ScriptUsage(description = "", arguments = {
 			@ScriptArgument(name = "count", type = "integer", description = "log count", optional = true),
 			@ScriptArgument(name = "repeat", type = "integer", description = "repeat count", optional = true) })
 	public void benchmark(String[] args) {
 		String tableName = "benchmark";
 		int count = 1000000;
 		int repeat = 1;
 		if (args.length >= 1)
 			count = Integer.parseInt(args[0]);
 		if (args.length >= 2)
 			repeat = Integer.parseInt(args[1]);
 
 		Map<String, Object> text = new HashMap<String, Object>();
 		text.put("_data", "2011-08-22 17:30:23 Google 111.222.33.44 GET /search q=cache:xgLxoOQBOoIJ:"
 				+ "araqneapps.org/+araqneapps&cd=1&hl=en&ct=clnk&source=www.google.com 80 - 123.234.34.45 "
 				+ "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 "
 				+ "Safari/535.1 404 0 3");
 
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("c-ip", "111.222.33.44");
 		map.put("cs(User-Agent)",
 				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
 		map.put("cs-method", "GET");
 		map.put("cs-uri-query", "q=cache:xgLxoOQBOoIJ:araqneapps.org/+araqneapps&cd=1&hl=en&ct=clnk&source=www.google.com");
 		map.put("cs-uri-stem", "/search");
 		map.put("cs-username", "-");
 		map.put("date", "2011-08-22");
 		map.put("s-ip", "123.234.34.45");
 		map.put("s-port", "80");
 		map.put("s-sitename", "Google");
 		map.put("sc-status", "200");
 		map.put("sc-substatus", "0");
 		map.put("sc-win32-status", "0");
 		map.put("time", "17:30:23");
 
 		for (int i = 1; i <= repeat; i++) {
 			context.println("=== Test #" + i + " ===");
 			benchmark("text", tableName, count, text);
 			benchmark("map", tableName, count, map);
 			context.println("");
 		}
 	}
 
 	private void benchmark(String name, String tableName, int count, Map<String, Object> data) {
 		try {
 			storage.createTable(tableName, "v3p");
 		} catch (UnsupportedLogFileTypeException e) {
 			storage.createTable(tableName, "v2");
 		}
 
 		Log log = new Log(tableName, new Date(), data);
 		long begin = System.currentTimeMillis();
 		for (long id = 1; id <= count; id++) {
 			log.setId(id);
 			storage.write(log);
 		}
 		long end = System.currentTimeMillis();
 		long time = end - begin;
 		String timeStr = null;
 		if (time == 0)
 			timeStr = "n/a";
 		else
 			timeStr = String.format("%d logs/s", count * 1000L / time);
 		context.println(String.format("%s(write): %d log/%d ms (%s)", name, count, time, timeStr));
 
 		begin = System.currentTimeMillis();
 		try {
 			storage.search(tableName, new Date(0), new Date(), count, new BenchmarkCallback());
 		} catch (InterruptedException e) {
 		}
 		end = System.currentTimeMillis();
 		time = end - begin;
 		if (time == 0)
 			timeStr = "n/a";
 		else
 			timeStr = String.format("%d logs/s", count * 1000L / time);
 		context.println(String.format("%s(read): %d log/%d ms (%s)", name, count, time, timeStr));
 
 		storage.dropTable(tableName);
 	}
 
 	private class BenchmarkCallback implements LogSearchCallback {
 		private boolean interrupt = false;
 
 		@Override
 		public void onLogBatch(String tableName, List<Log> logBatch) {
 		}
 
 		@Override
 		public void interrupt() {
 			interrupt = true;
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			return interrupt;
 		}
 	}
 
 	/**
 	 * @since 1.16.0
 	 */
 	public void installedEngines(String[] args) {
 		context.println("Installed File Engines");
 		context.println("------------------------");
 		for (String type : lfsRegistry.getInstalledTypes()) {
 			context.println(type);
 		}
 	}
 
 	/**
 	 * @since 1.16.0
 	 */
 	@ScriptUsage(description = "uninstall engine. request to unloaded engine will not blocked any more. "
 			+ "insteads, it will throw unsupported exception", arguments = { @ScriptArgument(name = "engine type", type = "string", description = "engine type name") })
 	public void uninstallEngine(String[] args) {
 		lfsRegistry.uninstall(args[0]);
 		context.println("removed from file engine list");
 	}
 
 	@ScriptUsage(description = "purge log files between specified days", arguments = {
 			@ScriptArgument(name = "table name", type = "string", description = "table name"),
 			@ScriptArgument(name = "from", type = "string", description = "yyyyMMdd"),
 			@ScriptArgument(name = "to", type = "string", description = "yyyyMMdd") })
 	public void purge(String[] args) throws ParseException {
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
 		String receivedTableName = args[0];
 		Date fromDay = df.parse(args[1]);
 		Date toDay = df.parse(args[2]);
 
 		PurgePrinter printer = new PurgePrinter();
 		try {
 			storage.addEventListener(printer);
 			Set<String> tableNames = TableWildcardMatcher.apply(new HashSet<String>(tableRegistry.getTableNames()),
 					receivedTableName);
 			for (String tableName : tableNames)
 				storage.purge(tableName, fromDay, toDay);
 		} finally {
 			storage.removeEventListener(printer);
 		}
 		context.println("completed");
 	}
 
 	private class PurgePrinter implements LogStorageEventListener {
 
 		@Override
 		public void onPurge(String tableName, Date day) {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			context.println("purging table " + tableName + " day " + df.format(day));
 		}
 	}
 }
