 package cz.stoupa.showtimes.imports.mat;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 
 import cz.stoupa.showtimes.imports.internal.fetcher.GetPageFetcher;
import cz.stoupa.showtimes.imports.mat.moviedetail.MatMovieDetailPageCreator;
 import cz.stoupa.showtimes.imports.mat.schedule.MatSchedulePageScraper;
 import cz.stoupa.showtimes.imports.mat.schedule.MatSchedulePageUrlGenerator;
import cz.stoupa.showtimes.imports.mat.schedule.MatShowingPageCreator;
 
 public class MatModule extends AbstractModule {
 
 	static final String SHOWING_PAGE_URL = "http://www.mat.cz/matclub/cz/kino/mesicni-program";
	static final String MOVIE_DETAIL_PAGE_URL = "http://www.mat.cz/matclub/cz/kino/mesicni-program";
 	
 	@Override
 	protected void configure() {
 		bind( MatSchedulePageScraper.class );
 		bind( MatDateTimeParser.class );
 		bind( MatShowingPageCreator.class );
		bind( MatMovieDetailPageCreator.class );
 		bind( String.class )
 			.annotatedWith( Names.named( "showingPageUrl" ) )
 			.toInstance( SHOWING_PAGE_URL );
		bind( String.class )
			.annotatedWith( Names.named( "movieDetailUrl" ) )
			.toInstance( MOVIE_DETAIL_PAGE_URL );		
 		bind( GetPageFetcher.class ); // TODO: multiple bindings in modules
 	}
 	
 	@Provides 
 	MatKnownDatesScanner provideMatKnownDatesScanner( @Named("showingPageUrl") String showingPageUrl, MatSchedulePageScraper pageScraper, GetPageFetcher pageFetcher ) {
 		return new MatKnownDatesScanner( showingPageUrl, pageScraper, pageFetcher );
 	}
 	
 	@Provides 
 	MatSchedulePageUrlGenerator provideMatSchedulePageUrlGenerator( @Named("showingPageUrl") String showingPageUrl ) {
 		return new MatSchedulePageUrlGenerator( showingPageUrl );
 	}
 	
 }
