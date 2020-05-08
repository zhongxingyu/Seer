 package cz.stoupa.showtimes.imports.cinestar;
 
 import com.google.inject.AbstractModule;
 
 public class CinestarModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		bind(CinestarPageScraper.class);
 		bind(CinestarDateTimeParser.class);
		bind(KnownDatesScanner.class);
 	}
 	
 }
