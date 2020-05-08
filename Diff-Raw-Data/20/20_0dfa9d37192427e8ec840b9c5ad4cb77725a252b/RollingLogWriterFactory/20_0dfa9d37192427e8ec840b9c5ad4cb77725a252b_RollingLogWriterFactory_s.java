 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.log.api;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.cron.MinutelyJob;
 
 @MinutelyJob
 @Component(name = "rolling-log-writer-factory")
 @Provides
 public class RollingLogWriterFactory extends AbstractLoggerFactory implements Runnable {
 	@Requires
 	private LoggerRegistry loggerRegistry;
 
 	private ConcurrentMap<String, RollingLogWriter> writers = new ConcurrentHashMap<String, RollingLogWriter>();
 
 	@Override
 	public String getName() {
 		return "tofile";
 	}
 
 	@Override
 	public List<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale.equals(Locale.KOREAN))
 			return "롤링 로그 파일";
 		if (locale.equals(Locale.JAPANESE))
 			return "ローリングログファイル";
 		if(locale != null && locale.equals(Locale.CHINESE))
 			return "写入滚动文件(大小)";
 		return "Rolling Log File";
 	}
 
 	@Override
 	public List<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale.equals(Locale.KOREAN))
 			return "실시간으로 롤링 로그 파일을 생성합니다.";
 		if (locale.equals(Locale.JAPANESE))
 			return "実時間でローリングログファイルを作ります。";
 		if(locale != null && locale.equals(Locale.CHINESE))
 			return "写入基于指定大小滚动的文件。";
 		return "write rolling log file";
 	}
 
 	@Override
 	public List<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption loggerName = new StringConfigType("source_logger", t("Source logger name", "원본 로거 이름", "元ロガー名", "原始数据采集器"), t(
				"Full name of data source logger", "네임스페이스를 포함한 원본 로거 이름", "ネームスペースを含む元ロガー名", "包含名字空间的原始Logger名称"), true);
 		LoggerConfigOption filePath = new StringConfigType("file_path", t("file path", "파일 경로", "ファイル経路","文件路径"), t("rolling file path",
 				"롤링되는 파일 경로", "ローリングされるファイル経路","轮询的文件路径"), true);
 		LoggerConfigOption maxFileSize = new StringConfigType("max_file_size", t("max file size", "최대 파일 크기", "最大ファイルサイズ", "最大文件大小"), t(
 				"max file size in bytes", "바이트 단위 최대 파일 크기", "byte単位最大ファイルサイズ", "最大文件大小"), true);
 		LoggerConfigOption maxBackupIndex = new IntegerConfigType("max_backup_index", t("max backup index", "최대 백업 인덱스",
 				"最大バックアップインデックス", "最大备份索引"), t("number of retained files", "롤링 백업되는 파일 갯수, 기본값 1", "ローリングバックアップされるファイルの数。基本値1", 
 						"轮询备份的文件个数, 默认值1"), false);
 		LoggerConfigOption charsetName = new StringConfigType("charset", t("charset", "문자집합", "文字セット", "字符集"), t("utf-8 by default",
 				"기본값은 utf-8", "基本値はutf-8", "默认值为utf-8"), false);
 
 		return Arrays.asList(loggerName, filePath, maxFileSize, maxBackupIndex, charsetName);
 	}
 
 	private Map<Locale, String> t(String enText, String koText, String jpText, String cnText) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, enText);
 		m.put(Locale.KOREAN, koText);
 		m.put(Locale.JAPANESE, jpText);
 		m.put(Locale.CHINESE, cnText);
 		return m;
 	}
 
 	@Override
 	public void run() {
 		// force flush at least per 1min
 		for (RollingLogWriter writer : new ArrayList<RollingLogWriter>(writers.values())) {
 			writer.flush();
 		}
 	}
 
 	@Override
 	protected Logger createLogger(LoggerSpecification spec) {
 		String fullName = spec.getNamespace() + "\\" + spec.getName();
 		RollingLogWriter writer = new RollingLogWriter(spec, this, loggerRegistry);
 		RollingLogWriter old = writers.putIfAbsent(fullName, writer);
 		if (old != null)
 			throw new IllegalStateException("duplicated rolling log writer: " + fullName);
 		return writer;
 	}
 
 	@Override
 	public void deleteLogger(String namespace, String name) {
 		String fullName = namespace + "\\" + name;
 		writers.remove(fullName);
 		super.deleteLogger(namespace, name);
 	}
 
 }
