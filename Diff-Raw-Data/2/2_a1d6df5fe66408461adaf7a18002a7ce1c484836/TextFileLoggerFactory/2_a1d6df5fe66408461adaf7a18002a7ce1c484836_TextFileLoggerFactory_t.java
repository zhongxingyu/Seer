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
 package org.araqne.log.api;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 
 @Component(name = "text-file-logger-factory")
 @Provides
 public class TextFileLoggerFactory extends AbstractLoggerFactory {
 	private final List<LoggerConfigOption> options;
 
 	public TextFileLoggerFactory() {
 		options = new ArrayList<LoggerConfigOption>();
 		options.add(new StringConfigType("file.path", t("File Path", "파일 경로"), t("Log file path", "텍스트 로그 파일의 절대 경로"), true,
 				t("/var/log/")));
 		options.add(new StringConfigType("charset", t("Charset", "문자 집합"), t("Charset", "문자 집합. 기본값 UTF-8"), false, t("utf-8")));
 		options.add(new StringConfigType("date.extractor", t("Date Extractor", "날짜 정규식"), t("Regex for date extraction",
 				"날짜 문자열 추출에 사용되는 정규표현식"), false, t(null)));
 		options.add(new StringConfigType("date.pattern", t("Date Pattern", "날짜 패턴"), t("Date pattern of log file",
 				"날짜 파싱에 필요한 패턴 (예시: yyyy-MM-dd HH:mm:ss)"), false, t("MMM dd HH:mm:ss")));
 		options.add(new StringConfigType("date.locale", t("Date Locale", "날짜 로케일"), t("Date locale of log file",
 				"날짜 문자열의 로케일. 가령 날짜 패턴의 MMM 지시어은 영문 로케일에서 Jan으로 인식됩니다."), false, t("en")));
 	}
 
 	private Map<Locale, String> t(String text) {
 		return t(text, text);
 	}
 
 	private Map<Locale, String> t(String enText, String koText) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, enText);
 		m.put(Locale.KOREAN, koText);
 		return m;
 	}
 
 	@Override
 	public String getName() {
 		return "textfile";
 	}
 
 	@Override
 	protected Logger createLogger(LoggerSpecification spec) {
 		try {
 			return new TextFileLogger(spec, this);
 		} catch (Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN);
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN);
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
		if (locale == Locale.KOREAN)
 			return "텍스트 로그 파일";
 		return "Text file logger";
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale == Locale.KOREAN)
 			return "롤링되는 텍스트 로그 파일로부터 주기적으로 로그를 수집합니다.";
 		return "Text file logger";
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
 		return options;
 	}
 }
