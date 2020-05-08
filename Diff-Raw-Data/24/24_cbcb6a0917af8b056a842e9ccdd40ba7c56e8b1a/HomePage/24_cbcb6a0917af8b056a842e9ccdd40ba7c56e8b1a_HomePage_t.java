 package de.effectivetrainings;
 
 import org.apache.wicket.bootstrap.Bootstrap;
 import org.apache.wicket.markup.head.JavaScriptHeaderItem;
 import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.head.IHeaderResponse; 
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.PackageResourceReference;
 
 public class HomePage extends WebPage {
 	private static final long serialVersionUID = 1L;
 
 	public HomePage(final PageParameters parameters) {
 		super(parameters);
         add(new HeaderResponseContainer("footerJS",WicketApplication.FOOTER_BUCKET));
     }
 
     @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(Bootstrap.responsive()));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(HomePage.class,"file1.js")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(HomePage.class,"file2.js")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(HomePage.class,"file3.js")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(HomePage.class,"file4.js")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(HomePage.class,"file5.js")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(HomePage.class,"file6.js")));
     }
 }
