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
 package org.araqne.logdb.logapi;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserFactory;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.log.api.StringConfigType;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryParserService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "query-log-parser-factory")
 @Provides
 public class QueryLogParserFactory implements LogParserFactory {
 	private final Logger logger = LoggerFactory.getLogger(QueryLogParserFactory.class);
 
 	@Requires
 	private QueryParserService queryParser;
 
 	@Override
 	public String getName() {
 		return "query";
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "쿼리 기반 파서";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "查询解析器";
 
 		return "Query based parser";
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "쿼리를 이용하여 파싱을 수행합니다.";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "基于查询进行解析。";
 
 		return "Parse logs using query.";
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption query = new StringConfigType("query", t("Query string", "쿼리", "查询字符串"), t("Query string for log parsing",
				"로그 파싱에 사용할 쿼리", "用于日志解析的查询字符串"), true);
 		return Arrays.asList(query);
 	}
 
 	private Map<Locale, String> t(String en, String ko, String cn) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, en);
 		m.put(Locale.KOREAN, ko);
 		m.put(Locale.CHINESE, cn);
 		return m;
 	}
 
 	@Override
 	public LogParser createParser(Map<String, String> configs) {
 		String query = configs.get("query");
 
 		try {
 			List<QueryCommand> commands = queryParser.parseCommands(null, query);
 			return new QueryLogParser(query, commands);
 		} catch (Throwable t) {
 			logger.debug("araqne logdb: cannot parse query string for parser - query [" + query + "]", t);
 		}
 
 		return null;
 	}
 }
