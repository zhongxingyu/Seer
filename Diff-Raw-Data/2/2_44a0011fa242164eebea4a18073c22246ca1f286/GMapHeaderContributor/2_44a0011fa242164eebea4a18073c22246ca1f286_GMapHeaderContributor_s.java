 package wicket.contrib.gmap3;
 
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.WicketAjaxReference;
 import org.apache.wicket.behavior.HeaderContributor;
 import org.apache.wicket.markup.html.IHeaderContributor;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WicketEventReference;
 import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
 
 public class GMapHeaderContributor extends HeaderContributor {
     private static final long serialVersionUID = 1L;
 
     // URL for Google Maps' API endpoint.
     private static final String GMAP_API_URL = "://maps.google.com/maps/api/js?v=3&sensor=false";
 
     private static final String HTTP = "http";
 
     // We have some custom Javascript.
     private static final ResourceReference WICKET_GMAP_JS = new JavascriptResourceReference( GMap.class, "wicket-gmap.js" );
 
     protected static final String EMPTY = "";
 
     String _clientId;
 
     public GMapHeaderContributor() {
         this( HTTP, null );
     }
 
     public GMapHeaderContributor( final String schema, final String clientId ) {
         super( new IHeaderContributor() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void renderHead( IHeaderResponse response ) {
                 final String clientParm;
                if ( !EMPTY.equals( clientId ) ) {
                     clientParm = "&client=" + clientId;
                 } else {
                     clientParm = EMPTY;
                 }
                 response.renderJavascriptReference( schema + GMAP_API_URL + clientParm );
                 response.renderJavascriptReference( WicketEventReference.INSTANCE );
                 response.renderJavascriptReference( WicketAjaxReference.INSTANCE );
                 response.renderJavascriptReference( WICKET_GMAP_JS );
 
             }
         } );
     }
 
     String getClientId() {
         return _clientId;
     }
 }
