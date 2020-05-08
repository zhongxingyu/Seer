 package sgit.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sourceforge.stripes.controller.StripesFilter;
 
 import com.google.inject.Provides;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.name.Named;
 import com.google.inject.servlet.ServletModule;
 
 public class SGitModule extends ServletModule {	
 	@Override
 	protected void configureServlets() {		
 		bindInterceptor(
 				Matchers.subclassesOf(StripesFilter.class), 
 				new InitMatcher(), 
 				new MarkerUpdater());				
 		install(new sgit.SGitModule());		
 	}
 	
 	@Provides @Named("sgit.repositories")
 	List<File> provideRepositories() {
 		List<File> repos = new ArrayList<File>();		
		repos.add(new File("../demo/src/main/resources/xsbt-web-plugin.git"));
 		return repos;
 	}
 }
