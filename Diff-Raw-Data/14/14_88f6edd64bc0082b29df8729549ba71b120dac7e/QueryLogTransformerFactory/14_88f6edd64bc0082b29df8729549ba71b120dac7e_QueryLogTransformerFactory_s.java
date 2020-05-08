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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.log.api.AbstractLogTransformerFactory;
 import org.araqne.log.api.LogTransformer;
 import org.araqne.log.api.LogTransformerNotReadyException;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.log.api.StringConfigType;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryService;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryParserService;
 
 /**
  * @since 1.7.8
  * @author xeraph
  * 
  */
 @Component(name = "query-log-transformer-factory")
 @Provides
 public class QueryLogTransformerFactory extends AbstractLogTransformerFactory {
 	// force to wait dynamic query parser instance loading
 	@Requires
 	private QueryService queryService;
 
 	@Requires
 	private QueryParserService queryParser;
 
 	@Override
 	public String getName() {
 		return "query";
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "쿼리 기반 원본 가공";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "基于查询变换数据";
 		return "Query";
 	}
 
 	@Override
 	public List<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "로그 쿼리를 이용하여 원본 데이터를 가공합니다.";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "基于查询语句变换原始数据。";
 		return "Transform data using logdb query";
 	}
 
 	@Override
 	public List<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE);
 	}
 
 	@Override
 	public List<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption querystring = new StringConfigType("querystring", t("Query string", "쿼리 문자열", "查询语句"), t(
 				"Configure query string to evaluating and transforming input log data",
				"입력 로그를 변환하여 출력하는데 사용할 쿼리 문자열을 설정합니다. 그룹 함수 사용은 허용되지 않습니다.", "基于查询语句变换输入数据并输出(不支持组函数)。"), true);
 
 		return Arrays.asList(querystring);
 	}
 
 	private Map<Locale, String> t(String en, String ko, String cn) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, en);
 		m.put(Locale.KOREAN, ko);
 		m.put(Locale.CHINESE, cn);
 		return m;
 	}
 
 	@Override
 	public LogTransformer newTransformer(Map<String, String> config) {
 		try {
 			String queryString = config.get("querystring");
 
 			List<QueryCommand> commands = queryParser.parseCommands(null, queryString);
 			return new QueryLogTransformer(this, commands);
 		} catch (QueryParseException e) {
 			throw new LogTransformerNotReadyException(e);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Transform data using query";
 	}
 }
