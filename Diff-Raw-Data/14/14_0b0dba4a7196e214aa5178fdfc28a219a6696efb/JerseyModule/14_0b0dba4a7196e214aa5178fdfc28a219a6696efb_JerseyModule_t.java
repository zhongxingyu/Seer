 import com.google.inject.AbstractModule;
 
import java.net.MalformedURLException;
 import java.net.URL;
 
 import static com.google.inject.name.Names.named;
 
 public class JerseyModule extends AbstractModule {
 	private final static String PLANNING_JSON = "http://planning.code-story.net/planning.json";
 	private final static String VOTES_JSON = "http://planning.code-story.net/starsPerTalk";
 
 	@Override
 	protected void configure() {
 		try {
 			bind(URL.class).annotatedWith(named("planningUrl")).toInstance(new URL(PLANNING_JSON));
		} catch (MalformedURLException e) {
			addError(e);
		}

		try {
 			bind(URL.class).annotatedWith(named("votesUrl")).toInstance(new URL(VOTES_JSON));
		} catch (MalformedURLException e) {
			addError(e);
 		}
 	}
 }
