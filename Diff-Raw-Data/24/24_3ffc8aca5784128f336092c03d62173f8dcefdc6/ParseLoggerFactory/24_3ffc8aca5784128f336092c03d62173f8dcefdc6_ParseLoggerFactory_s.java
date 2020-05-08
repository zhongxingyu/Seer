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
		return "parse";
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
		return "parse";
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption sourceLogger = new StringConfigType("source_logger", t("Source Logger", "Source Logger"), t("", ""), true);
		LoggerConfigOption parserName = new StringConfigType("parser_name", t("Parser Name", "Parser Name"), t("", ""), true);
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
