 package wicket.contrib.examples.gmap;
 
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 
 import wicket.contrib.examples.WicketExamplePage;
 import wicket.contrib.gmap.GMap2;
 import wicket.contrib.gmap.api.GControl;
 import wicket.contrib.gmap.api.GInfoWindowTab;
 import wicket.contrib.gmap.api.GLatLng;
 import wicket.contrib.gmap.api.GMapType;
 import wicket.contrib.gmap.api.GMarker;
 import wicket.contrib.gmap.api.GPolygon;
 import wicket.contrib.gmap.api.GPolyline;
 import wicket.contrib.gmap.event.ClickListener;
 import wicket.contrib.gmap.event.InfoWindowListener;
 import wicket.contrib.gmap.event.MoveListener;
 
 /**
  * Example HomePage for the wicket-contrib-gmap2 project
  */
 public class HomePage extends WicketExamplePage
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private final FeedbackPanel feedback;
 	
 	private final Label markerLabel;
 	private final Label zoomLabel;
 	private final Label center;
 
 	private MoveListener moveEndBehavior;
 	
 	public HomePage()
 	{
 		feedback = new FeedbackPanel("feedback");
 		feedback.setOutputMarkupId(true);
 		add(feedback);
 
 		final GMap2 topPanel = new GMap2("topPanel",
 				LOCALHOST_8080_WICKET_CONTRIB_GMAP2_EXAMPLES_KEY);
 		topPanel.setDoubleClickZoomEnabled(true);
 		topPanel.add(new MoveListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onMoveEnd(AjaxRequestTarget target)
 			{
 				target.addComponent(zoomLabel);
 			}
 		});
 		topPanel.add(new ClickListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onMarkerClick(AjaxRequestTarget target, GMarker marker)
 			{
 				topPanel.getInfoWindow().open(marker, new HelloPanel());
 				
 				markerSelected(target, marker);
 			}
 
 			@Override
 			protected void onMapClick(AjaxRequestTarget target, GLatLng gLatLng)
 			{
 				GMarker marker = new GMarker(gLatLng);
 				topPanel.addOverlay(marker);
 				
 				markerSelected(target, marker);
 			}
 
 		});
 		topPanel.setZoom(10);
 		topPanel.addOverlay(new GMarker(new GLatLng(37.4, -122.1), "Home"));
 		topPanel.addOverlay(new GPolygon("#000000", 4, 0.7f, "#E9601A", 0.7f, new GLatLng(37.3,
 				-122.4), new GLatLng(37.2, -122.2), new GLatLng(37.3, -122.0), new GLatLng(37.4,
 				-122.2), new GLatLng(37.3, -122.4)));
 		topPanel.addOverlay(new GPolyline("#FFFFFF", 8, 1.0f, new GLatLng(37.35, -122.3),
 				new GLatLng(37.25, -122.25), new GLatLng(37.3, -122.2),
 				new GLatLng(37.25, -122.15), new GLatLng(37.35, -122.1)));
 		topPanel.addControl(GControl.GLargeMapControl);
 		topPanel.addControl(GControl.GMapTypeControl);
 		add(topPanel);
 
 		zoomLabel = new Label("zoomLabel", new PropertyModel(topPanel, "zoom"));
 		zoomLabel.add(topPanel.new SetZoom("onclick", 10));
 		zoomLabel.setOutputMarkupId(true);
 		add(zoomLabel);
 
 		markerLabel = new Label("markerLabel", new Model(null));
 		markerLabel.add(new AjaxEventBehavior("onclick")
 		{
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
 			 */
 			@Override
 			protected void onEvent(AjaxRequestTarget target)
 			{
 				GLatLng point = ((GMarker)markerLabel.getModelObject()).getLagLng();
 
 				GMarker marker = new GMarker(new GLatLng(point.getLat()
 						* (0.9995 + Math.random() / 1000), point.getLng()
 						* (0.9995 + Math.random() / 1000)));
 
 				topPanel.addOverlay(marker);
 			}
 		});
 		add(markerLabel);
 
 		final Label zoomIn = new Label("zoomInLabel", "ZoomIn");
 		zoomIn.add(topPanel.new ZoomIn("onclick"));
 		add(zoomIn);
 
 		final Label zoomOut = new Label("zoomOutLabel", "ZoomOut");
 		zoomOut.add(topPanel.new ZoomOut("onclick"));
 		add(zoomOut);
 
 		final GMap2 bottomPanel = new GMap2("bottomPanel",
 				LOCALHOST_8080_WICKET_CONTRIB_GMAP2_EXAMPLES_KEY);
		bottomPanel.setOutputMarkupId(true);
 		bottomPanel.setMapType(GMapType.G_SATELLITE_MAP);
 		bottomPanel.setScrollWheelZoomEnabled(true);
 		moveEndBehavior = new MoveListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onMoveEnd(AjaxRequestTarget target)
 			{
 				target.addComponent(center);
 			}
 		};
 		bottomPanel.add(moveEndBehavior);
 		bottomPanel.add(new ClickListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onMarkerClick(AjaxRequestTarget target, GMarker marker)
 			{
 				// empty on purpose
 			}
 
 			@Override
 			protected void onMapClick(AjaxRequestTarget target, GLatLng gLatLng)
 			{
 				bottomPanel.getInfoWindow().open(gLatLng, new HelloPanel());
 			}
 
 		});
 		bottomPanel.add(new InfoWindowListener() {
 			@Override
 			protected void onInfoWindowClose(AjaxRequestTarget target) {
 				info("InfoWindow was closed");
 				target.addComponent(feedback);
 			}
 		});
 		bottomPanel.addControl(GControl.GSmallMapControl);
 		bottomPanel.getInfoWindow().open(new GLatLng(37.5, -122.1), new GInfoWindowTab("One", new HelloPanel()), new GInfoWindowTab("Two", new HelloPanel()));
 		add(bottomPanel);
 
 		center = new Label("center", new PropertyModel(bottomPanel, "center"));
 		center.add(bottomPanel.new SetCenter("onclick", new GLatLng(37.5, -122.1, false)));
 		center.setOutputMarkupId(true);
 		add(center);
 
 		final Label n = new Label("n", "N");
 		n.add(bottomPanel.new PanDirection("onclick", 0, 1));
 		add(n);
 
 		final Label ne = new Label("ne", "NE");
 		ne.add(bottomPanel.new PanDirection("onclick", -1, 1));
 		add(ne);
 
 		final Label e = new Label("e", "E");
 		e.add(bottomPanel.new PanDirection("onclick", -1, 0));
 		add(e);
 
 		final Label se = new Label("se", "SE");
 		se.add(bottomPanel.new PanDirection("onclick", -1, -1));
 		add(se);
 
 		final Label s = new Label("s", "S");
 		s.add(bottomPanel.new PanDirection("onclick", 0, -1));
 		add(s);
 
 		final Label sw = new Label("sw", "SW");
 		sw.add(bottomPanel.new PanDirection("onclick", 1, -1));
 		add(sw);
 
 		final Label w = new Label("w", "W");
 		w.add(bottomPanel.new PanDirection("onclick", 1, 0));
 		add(w);
 
 		final Label nw = new Label("nw", "NW");
 		nw.add(bottomPanel.new PanDirection("onclick", 1, 1));
 		add(nw);
 
 		final Label infoWindow = new Label("infoWindow", "openInfoWindow");
 		infoWindow.add(new AjaxEventBehavior("onclick")
 		{
 			/**
 			 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
 			 */
 			@Override
 			protected void onEvent(AjaxRequestTarget target)
 			{
 				bottomPanel.getInfoWindow().open(new GLatLng(37.5, -122.1), new HelloPanel());
 			}
 		});
 		add(infoWindow);
 		add(new Link("reload")
 		{
 			@Override
 			public void onClick()
 			{
 			}
 		});
 		final Label enabledMoveEnd = new Label("enabledMoveEnd",
 				"the move end behavior is enabled:");
 		add(enabledMoveEnd);
 		final Label enabledLabel = new Label("enabled", new Model()
 		{
 			@Override
 			public Object getObject()
 			{
 				return bottomPanel.getBehaviors().contains(moveEndBehavior);
 			}
 		});
 		enabledLabel.add(new AjaxEventBehavior("onclick")
 		{
 			@Override
 			protected void onEvent(AjaxRequestTarget target)
 			{
 				if (bottomPanel.getBehaviors().contains(moveEndBehavior))
 				{
 					bottomPanel.remove(moveEndBehavior);
 				}
 				else
 				{
 					//TODO AbstractAjaxBehaviors are not reusable, so we have to recreate:
 					//https://issues.apache.org/jira/browse/WICKET-713
 					moveEndBehavior = new MoveListener()
 					{
 						private static final long serialVersionUID = 1L;
 
 						@Override
 						protected void onMoveEnd(AjaxRequestTarget target)
 						{
 							target.addComponent(center);
 						}
 					};
 					bottomPanel.add(moveEndBehavior);
 				}
 				target.addComponent(bottomPanel);
 				target.addComponent(enabledLabel);
 			}
 		});
 		add(enabledLabel);
 	}
 
 	private void markerSelected(AjaxRequestTarget target, GMarker marker) {
 		markerLabel.getModel().setObject(marker);
 		target.addComponent(markerLabel);
 	}
 	
 	// pay attention at webapp deploy context, we need a different key for each
 	// deploy context
 	// check <a href="http://www.google.com/apis/maps/signup.html">Google Maps
 	// API - Sign Up</a> for more info
 
 	// key for http://localhost:8080/wicket-contrib-gmap-examples/
 	@SuppressWarnings("unused")
 	private static final String LOCALHOST_8080_WICKET_CONTRIB_GMAP_EXAMPLES_KEY = "ABQIAAAAf5yszl-6vzOSQ0g_Sk9hsxQwbIpmX_ZriduCDVKZPANEQcosVRSYYl2q0zAQNI9wY7N10hRcPVFbLw";
 
 	// http://localhost:8080/wicket-contrib-gmap2-examples/gmap/
 	private static final String LOCALHOST_8080_WICKET_CONTRIB_GMAP2_EXAMPLES_KEY = "ABQIAAAAf5yszl-6vzOSQ0g_Sk9hsxSRJOeFm910afBJASoNgKJoF-fSURRPODotP7LZxsDKHpLi_jvawkMyrQ";
 
 	// key for http://localhost:8080/wicket-contrib-gmap, deploy context is
 	// wicket-contrib-gmap
 	@SuppressWarnings("unused")
 	private static final String LOCALHOST_8080_WICKET_CONTRIB_GMAP_KEY = "ABQIAAAALjfJpigGWq5XvKwy7McLIxTDxbH1TVfo7w-iwzG2OxhXSIjJdhQTwgha-mCK8wiVEq4rgi9qvz8HYw";
 
 	// key for http://localhost:8080/gmap, deploy context is gmap
 	@SuppressWarnings("unused")
 	private static final String GMAP_8080_KEY = "ABQIAAAALjfJpigGWq5XvKwy7McLIxTh_sjBSLCHIDZfjzu1cFb3Pz7MrRQLOeA7BMLtPnXOjHn46gG11m_VFg";
 
 	// key for http://localhost/gmap
 	@SuppressWarnings("unused")
 	private static final String GMAP_DEFAULT_KEY = "ABQIAAAALjfJpigGWq5XvKwy7McLIxTIqKwA3nrz2BTziwZcGRDeDRNmMxS-FtSv7KGpE1A21EJiYSIibc-oEA";
 
 	// key for http://www.wicket-library.com/wicket-examples/
 	@SuppressWarnings("unused")
 	private static final String WICKET_LIBRARY_KEY = "ABQIAAAALjfJpigGWq5XvKwy7McLIxQTV35WN9IbLCS5__wznwqtm2prcBQxH8xw59T_NZJ3NCsDSwdTwHTrhg";
 }
