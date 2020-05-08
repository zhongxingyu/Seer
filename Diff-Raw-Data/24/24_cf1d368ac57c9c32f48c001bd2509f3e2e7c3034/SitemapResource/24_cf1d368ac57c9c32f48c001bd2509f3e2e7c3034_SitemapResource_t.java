 package au.com.miskinhill.web.feeds;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 import au.com.miskinhill.schema.sitemaps.Url;
 import au.com.miskinhill.schema.sitemaps.Urlset;
 
 @Component
 @Path("/feeds/sitemap")
 public class SitemapResource {
 	
 	private static final String[] OTHER_URLS = {
		// homepage and /about/ are already in RDF, don't need to list them here
 		"http://miskinhill.com.au/contact/",
 		"http://miskinhill.com.au/journals/",
 		"http://miskinhill.com.au/feeds/issues",
 		"http://miskinhill.com.au/feeds/world"
 	};
 	private static final Pattern OUR_URLS =
 		Pattern.compile("http://miskinhill\\.com\\.au/[^#]*");
 	
 	private final Model model;
 	
 	@Autowired
 	public SitemapResource(Model model) {
 		this.model = model;
 	}
 	
 	@GET
 	@Produces("text/xml")
 	public Urlset getSitemap() {
 		Urlset urlset = new Urlset();
 		for (String loc: OTHER_URLS) {
 			urlset.add(new Url(loc));
 		}
 		List<String> rdfLocs = new ArrayList<String>();
 		for (ResIterator i = model.listSubjects(); i.hasNext();) {
 			Resource res = (Resource) i.next();
 			if (res.getURI() != null && OUR_URLS.matcher(res.getURI()).matches()) {
 				rdfLocs.add(res.getURI());
 			}
 		}
 		Collections.sort(rdfLocs);
 		for (String loc: rdfLocs) {
 			urlset.add(new Url(loc));
 		}
 		return urlset;
 	}
 
 }
