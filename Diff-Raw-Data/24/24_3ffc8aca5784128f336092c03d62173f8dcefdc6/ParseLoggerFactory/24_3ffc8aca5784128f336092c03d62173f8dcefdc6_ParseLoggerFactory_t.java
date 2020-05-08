 package org.araqne.log.api;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 
 @Component(name = "parse-logger-factory")
 @Provides
 public class ParseLoggerFactory extends AbstractLoggerFactory {
 	@Requires
 	private LoggerRegistry loggerRegistry;
 
 	@Requires
 	private LogParserRegistry parserRegistry;
 
 	@Override
 	public String getName() {
 		return "parse";
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
		if (locale != null && locale.equals(Locale.KOREAN))
			return "파서";
		return "Parser";
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
		if (locale != null && locale.equals(Locale.KOREAN))
			return "지정된 파서를 이용하여 원본 로그를 파싱합니다.";
		return "Parse log using parser";
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption sourceLogger = new StringConfigType("source_logger", t("Source logger name", "원본 로거 이름"), t(
				"Full name of data source logger", "네임스페이스를 포함한 원본 로거 이름"), true);
		LoggerConfigOption parserName = new StringConfigType("parser_name", t("Parser Name", "파서 이름"), t("Parser name", "파서 이름"),
				true);
 		return Arrays.asList(sourceLogger, parserName);
 	}
 
 	private Map<Locale, String> t(String en, String ko) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, en);
 		m.put(Locale.KOREAN, ko);
 		return m;
 	}
 
 	@Override
 	protected Logger createLogger(LoggerSpecification spec) {
 		return new ParseLogger(spec, this, loggerRegistry, parserRegistry);
 	}
 
 }
