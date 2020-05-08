 package wicket.contrib.gmap3.api;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Request;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.repeater.RepeatingView;
 
 import wicket.contrib.gmap3.GMap;
 
 /**
  * Represents an Google Maps API's <a href=
  * "http://www.google.com/apis/maps/documentation/reference.html#GInfoWindow"
  * >GInfoWindow</a>.
  */
 public class GInfoWindow extends WebMarkupContainer {
     /**
      * Default serialVersionUID.
      */
     private static final long serialVersionUID = 1L;
 
     private GInfoWindowContent _infoWindowContent;
 
     private LatLng _latLng;
 
     private GMarker _marker;
 
     private final RepeatingView _content = new RepeatingView( "content" );
 
     public GInfoWindow() {
         super( "infoWindow" );
         setOutputMarkupId( true );
         add( _content );
 
     }
 
     /**
      * Update state from a request to an AJAX target.
      */
     public void update() {
         Request request = RequestCycle.get().getRequest();
 
         if ( Boolean.parseBoolean( request.getParameter( "infoWindow.hidden" ) ) ) {
             // Attention: don't use close() as this might result in an
             // endless AJAX request loop
             _marker = null;
             _latLng = null;
         }
     }
 
     public final String getJSinit() {
         return _infoWindowContent.getJSconstructor();
     }
 
     /**
      * Open an info window.
      * 
      * @param content
      *            content to open in info window
      * @return This
      */
     public GInfoWindow open( LatLng latLng, Component content ) {
         return open( latLng, new GInfoWindowContent( content ) );
     }
 
     /**
      * Open an info window.
      * 
      * @param map
      * 
      * @param content
      *            content to open in info window
      * @return This
      */
     public GInfoWindow open( GMap map, GMarker marker, Component content ) {
         return open( marker, new GInfoWindowContent( content ) );
     }
 
     public GInfoWindow open( LatLng latLng, GInfoWindowContent tab ) {
 
         this._latLng = latLng;
         this._marker = null;
         _content.add( tab.getContent() );
         if ( AjaxRequestTarget.get() != null ) {
             AjaxRequestTarget.get().appendJavascript( getJSopen( latLng, tab ) );
             AjaxRequestTarget.get().addComponent( this );
         }
 
         return this;
     }
 
     public GInfoWindow open( GMarker marker, GInfoWindowContent tab ) {
 
         this._latLng = null;
         this._marker = marker;
 
         if ( AjaxRequestTarget.get() != null ) {
             AjaxRequestTarget.get().appendJavascript( getJSopen( marker, tab ) );
             AjaxRequestTarget.get().addComponent( this );
         }
 
         return this;
     }
 
     public boolean isOpen() {
         return ( _latLng != null || _marker != null );
     }
 
     public void close() {
 
         _marker = null;
         _latLng = null;
 
         if ( AjaxRequestTarget.get() != null ) {
             AjaxRequestTarget.get().appendJavascript( getJSclose() );
             AjaxRequestTarget.get().addComponent( this );
         }
     }
 
     private String getJSopen( LatLng latLng, GInfoWindowContent tab ) {
         StringBuffer buffer = new StringBuffer();
         buffer.append( " var info =  " + tab.getJSconstructor( latLng ) + " ;\n" );

         buffer.append( "info.open(" );
         buffer.append( getGMap2().getJsReference() + ".map" );

         buffer.append( ");" );

         return buffer.toString();
     }
 
     private String getJSopen( GMarker marker, GInfoWindowContent tab ) {
         StringBuffer buffer = new StringBuffer();
         buffer.append( " var infoWin =  " + tab.getJSconstructor() + " ;\n" );
 
         buffer.append( "infoWin.open(" );
         buffer.append( getGMap2().getJsReference() + ".map," );
         buffer.append( marker.getId() );
         buffer.append( ");" );
 
         return buffer.toString();
     }
 
     private String getJSclose() {
         return getGMap2().getJSinvoke( "closeInfoWindow()" );
     }
 
     private GMap getGMap2() {
         return findParent( GMap.class );
     }
 
 }
