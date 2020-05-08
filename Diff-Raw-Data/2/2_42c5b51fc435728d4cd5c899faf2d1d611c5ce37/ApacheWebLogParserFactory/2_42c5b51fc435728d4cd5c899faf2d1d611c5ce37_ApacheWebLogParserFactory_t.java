 /*
  * Copyright 2011 NCHOVY
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
 package org.araqne.logfile;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserFactory;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.log.api.StringConfigType;
 
 @Component(name = "httpd-log-parser-factory")
 @Provides
 public class ApacheWebLogParserFactory implements LogParserFactory {
 	@Override
 	public String getName() {
 		return "httpd";
 	}
 
 	@Override
 	public LogParser createParser(Map<String, String> config) {
 		String t = config.get("log_format");
 		if (t != null)
 			return new ApacheWebLogParser(t);
 		return new ApacheWebLogParser();
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption s = new StringConfigType("log_format", text("Log Format", "LogFormat 옵션 설정", "Apache日志格式"), text("Apache Log Format",
 				"아파치 웹 서버의 설정 파일에서 LogFormat 지시어로 설정된 로그 형식을 입력합니다.", "Apache日志格式"), true, text("%h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\""));
 		return Arrays.asList(s);
 	}
 
 	private Map<Locale, String> text(String en) {
 		return text(en, en, en);
 	}
 
 	private Map<Locale, String> text(String en, String ko, String cn) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, en);
 		m.put(Locale.KOREAN, ko);
 		m.put(Locale.CHINESE, cn);
 		return m;
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "아파치 웹 로그";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "Apache Web日志";
 		return "Apache Web Log";
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "아파치 웹 로그 포맷 지시자의 조합을 이용한 아파치 웹 로그 파싱을 지원합니다.";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "解析Apache Web日志。";
 		return "Create apache httpd log parser with log format option";
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE);
 	}
 
 }
