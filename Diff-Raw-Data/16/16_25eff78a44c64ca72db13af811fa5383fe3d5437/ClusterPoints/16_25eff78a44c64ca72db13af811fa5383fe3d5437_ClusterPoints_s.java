 package uk.co.plogic.gwt.lib.map.overlay;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Timer;
 import com.google.maps.gwt.client.GoogleMap;
 import com.google.maps.gwt.client.GoogleMap.BoundsChangedHandler;
 import com.google.maps.gwt.client.GoogleMap.CenterChangedHandler;
 import com.google.maps.gwt.client.LatLng;
 import com.google.maps.gwt.client.LatLngBounds;
 import com.google.maps.gwt.client.MarkerImage;
 import com.google.maps.gwt.client.Point;
 import com.google.maps.gwt.client.Size;
 import com.reveregroup.gwt.imagepreloader.ImageLoadEvent;
 import com.reveregroup.gwt.imagepreloader.ImageLoadHandler;
 import com.reveregroup.gwt.imagepreloader.ImagePreloader;
 
 import uk.co.plogic.gwt.lib.cluster.domain.Coord;
 import uk.co.plogic.gwt.lib.cluster.uncoil.Nest;
 import uk.co.plogic.gwt.lib.cluster.uncoil.Uncoil;
 import uk.co.plogic.gwt.lib.comms.DropBox;
 import uk.co.plogic.gwt.lib.comms.UxPostalService.LetterBox;
 import uk.co.plogic.gwt.lib.comms.envelope.ClusterPointsEnvelope;
 import uk.co.plogic.gwt.lib.events.ClusterSetPointCountEvent;
 import uk.co.plogic.gwt.lib.events.ClusterSetPointCountEventHandler;
 import uk.co.plogic.gwt.lib.map.BasicPoint;
 import uk.co.plogic.gwt.lib.map.IconMarker;
 import uk.co.plogic.gwt.lib.map.MarkerMoveAnimation;
 import uk.co.plogic.gwt.lib.map.AbstractMapMarker;
 
 public class ClusterPoints implements DropBox {
 
 	private LetterBox letterBox;
 	private GoogleMap gMap;
 	
 	// see note in refreshMapMarkers()
 	//private ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
 	//private int currentKeyFrame;
 	private KeyFrame oldKeyFrame;
 	private KeyFrame newKeyFrame;
 	
 	private HandlerManager eventBus;
 	private int requestedNoPoints = 45;
 	final static int markerAnimationDuration = 750;
 	final String mapMarkersUrl; // the integer weight is added to the end of this
 	
 	// weight -> markerIcon
 	private HashMap<Integer, MarkerImage> markerIcons = new HashMap<Integer, MarkerImage>();
 	private MarkerImage holdingMarker; // used when numbered icons haven't yet been loaded
 	private HashMap<Integer, ArrayList<IconMarker>> markersNeedingIcons = new HashMap<Integer, ArrayList<IconMarker>>();
 	private Timer fetchMissingMarkersTimer;
 	private ArrayList<IconMarker> holdingIcons = new ArrayList<IconMarker>(); // see clearHoldingIcons()
 	private Timer removeHoldingIconsTimer;
 
 	final static int delayDuration = 200; // wait a bit after map moves and eventBus requests
 	private Timer requestTimer;  		  // before making a request
 
 	
 	class KeyFrame {
 		Uncoil uncoil;
 		HashMap<Integer, AbstractMapMarker> markers = new HashMap<Integer, AbstractMapMarker>();
 		KeyFrame(Uncoil uncoil) {
 			this.uncoil = uncoil;
 		}
 	}
 
 	
 	public ClusterPoints(HandlerManager eventBus, final String mapMarkersUrl) {
 		this.eventBus = eventBus;
 		this.mapMarkersUrl = mapMarkersUrl;
 
 		requestTimer = new Timer() {  
 		    @Override
 		    public void run() {
 		    	//System.out.println("requesting now");
 		    	
 		    	if( gMap != null ) {
 			    	LatLngBounds mapBounds = gMap.getBounds();
 			    	LatLng ll0 = mapBounds.getSouthWest();
 			    	LatLng ll1 = mapBounds.getNorthEast();
 
 			    	ClusterPointsEnvelope envelope = new ClusterPointsEnvelope();
 			    	envelope.requestBounding(ll0.lng(), ll0.lat(), ll1.lng(), ll1.lat());
 			    	envelope.requestNoPoints(requestedNoPoints);
 			    	letterBox.send(envelope);
 		    	}
 		    }
 		};
 		
 		fetchMissingMarkersTimer = new Timer() {  
 		    @Override
 		    public void run() {
 		    	updateMarkersNeedingIcons();
 		    }
 		};
 		
 		removeHoldingIconsTimer = new Timer() {  
 		    @Override
 		    public void run() {
 		    	clearHoldingIcons();
 		    }
 		};
 		
 		eventBus.addHandler(ClusterSetPointCountEvent.TYPE, new ClusterSetPointCountEventHandler() {
 
 			@Override
 			public void onSetPointCount(ClusterSetPointCountEvent e) {
 				
 				if( requestedNoPoints != e.getPointCount() ) {
 					requestedNoPoints = e.getPointCount();
 					requestTimer.cancel();
 					requestTimer.schedule(delayDuration);
 				}
 			}
 			
 		});
 	}
 
 	@Override
 	public void onDelivery(String jsonEncodedPayload) {
 
 		ClusterPointsEnvelope envelope = new ClusterPointsEnvelope();
 		envelope.loadJson(jsonEncodedPayload);
 
 		ArrayList<BasicPoint> points = envelope.getPoints();
 		System.out.println("Got ["+points.size()+"] new points");
 		Uncoil u = new Uncoil();
 		for( BasicPoint point : points ) {
 			
 			String[] nodePosition = point.getId().split("\\.");
 			int left = Integer.parseInt(nodePosition[0]);
 			int right = Integer.parseInt(nodePosition[1]);
 			Coord c = new Coord(point.getLng(), point.getLat());
 			Nest nst = new Nest(left, right, c, point.getWeight());
 			u.addNest(nst);
 		}
 		oldKeyFrame = newKeyFrame;
 		newKeyFrame = new KeyFrame(u);
 
 		refreshMapMarkers();
 
 	}
 
 	/**
 	 * get mapMarkers into sync with points (which have probably just been
 	 * delivered).
 	 * 
 	 * for now: clear old markers and add new ones
 	 */
 	public void refreshMapMarkers() {
 
 		// see git commit 36780f26f77aae7482decf19efcc67bb2e825198
 		// keyFrames was an array which kept hold of a history which would
 		// reduce server requests when the back button etc. is used. For now,
 		// just use new and old keyframes.
 
 		while( newKeyFrame.uncoil.hasNext() ) {
 
 			Nest nst = newKeyFrame.uncoil.next();
 			Coord c = nst.getCoord(); 
 			LatLng endPosition = LatLng.create(c.getY(), c.getX());
 			
 			//MarkerOptions options = MarkerOptions.create();
 			//options.setMap(gMap);
 		
 			Nest relativeNst = null;
 			if( oldKeyFrame != null ) {
 				relativeNst = oldKeyFrame.uncoil.findRelative(nst.getLeftID(), nst.getRightID());
 			}
 
 			if( relativeNst == null ) {
 				// no known relatives ; use normal marker
 				//options.setPosition(endPosition);
 				//Marker mapMarker = Marker.create(options);
 				
 				IconMarker mapMarker = getIconMarker(nst.getWeight(), endPosition);
 				newKeyFrame.markers.put(nst.getLeftID(), mapMarker);
 
 			} else {
 				// animate from/to a relative
 				
 				if( relativeNst.getLeftID() > nst.getLeftID() ) {
 					// relative is a child
 					// so move child to parent position and then make
 					// parent appear
 					final AbstractMapMarker childMarker = 
 											oldKeyFrame.markers.get(relativeNst.getLeftID());
 					
 					// marker might already have been used by another parent
 					if( childMarker != null ) {
 						MarkerMoveAnimation ma = new MarkerMoveAnimation(childMarker,
 																		 childMarker.getPosition(),
 																		 endPosition);
 						ma.run(markerAnimationDuration);
 						// make child disappear at end of duration
 						final Timer childTimer = new Timer() {  
 						    @Override
 						    public void run() {
 						    	childMarker.hideMarker();
 						    }
 						};
 						childTimer.schedule(markerAnimationDuration);
 					}
 
 					// parent to appear at end of duration
 					final IconMarker mapMarker = getIconMarker(nst.getWeight(), endPosition);
 					mapMarker.hideMarker();
 					
 					
 					newKeyFrame.markers.put(nst.getLeftID(), mapMarker);
 					final Timer parentTimer = new Timer() {  
 					    @Override
 					    public void run() {
 					    	mapMarker.showMarker();
 					    }
 					};
 					parentTimer.schedule(markerAnimationDuration);
 
 
 				} else {
 					// relative is the parent or relative is self (equal)
 
 					Coord cRel = relativeNst.getCoord();
 					LatLng startPosition = LatLng.create(cRel.getY(), cRel.getX());
 					
 					//options.setPosition(startPosition);
 					//Marker mapMarker = Marker.create(options);
 					
 					final IconMarker mapMarker = getIconMarker(nst.getWeight(), startPosition);
 					newKeyFrame.markers.put(nst.getLeftID(), mapMarker);
 					MarkerMoveAnimation ma = new MarkerMoveAnimation(mapMarker, startPosition,
 																	 endPosition);
 					ma.run(markerAnimationDuration);
 					
 					// remove parent marker
 					AbstractMapMarker parentMarker = oldKeyFrame.markers.get(relativeNst.getLeftID());
 					if( parentMarker != null )
 						parentMarker.hideMarker();
 					
 				}
 				// remove relative that we used. i.e. those markers remaining in
 				// oldKeyFrame need to be removed from the map
 				if( oldKeyFrame.markers.containsKey(relativeNst.getLeftID()) )
 					oldKeyFrame.markers.remove(relativeNst.getLeftID());
 				
 			}
 
 		}
 		
 		// dispose of markers from last key frame
 		if( oldKeyFrame != null ) {
 			
 			oldKeyFrame.uncoil.resetIterator();
 			while( oldKeyFrame.uncoil.hasNext() ) {
 
 				Nest nst = oldKeyFrame.uncoil.next();
 				int nstKeyID = nst.getLeftID();
 				if( ! oldKeyFrame.markers.containsKey(nstKeyID) ) {
 					//System.out.println("cant find: "+nstKeyID);
 					continue;
 				}
 				AbstractMapMarker mapMarker = oldKeyFrame.markers.get(nstKeyID);
 				//System.out.println("animating: "+nstKeyID);
 
 				Nest relativeNst = newKeyFrame.uncoil.findRelative(nstKeyID, nst.getRightID());
 				if( relativeNst == null )
 					continue;
 
 				if( relativeNst.getLeftID() < nstKeyID ) {
 					// has parent in newFrame so move from current
 					// position to that of the parent
 					
 					Coord cRel = relativeNst.getCoord();
 					LatLng endPosition = LatLng.create(cRel.getY(), cRel.getX());
 
 					MarkerMoveAnimation ma = new MarkerMoveAnimation(mapMarker,
 																	 mapMarker.getPosition(),
 																	 endPosition);
 					ma.run(markerAnimationDuration);
 				} else {
 					// has child in newFrame
 					// I don't think this will ever happen as this node
 					// would have been dealt with above
 					// TODO - this happened when zooming in around Chew Stoke
 					System.out.println("Found a parent in newKeyFrame??");
 				}
 				
 			}
 
 			// at the end of the animation, every marker left in oldKeyFrame needs to be
 			// removed from the map
 			final Collection<AbstractMapMarker> oldMarkers = oldKeyFrame.markers.values();
 			final Timer clearTimer = new Timer() {  
 			    @Override
 			    public void run() {
 					for( AbstractMapMarker oldMarker : oldMarkers ) {
 						oldMarker.hideMarker();
 					}
 			    }
 			};
 			clearTimer.schedule(markerAnimationDuration);
 		}
 		
 		// wait for animation to finish then start fetching any missing marker icons
 		fetchMissingMarkersTimer.cancel();
 		fetchMissingMarkersTimer.schedule(markerAnimationDuration);
 
 	}
 	
 	private IconMarker getIconMarker(int weight, LatLng position) {
 		
 		IconMarker mapMarker;
 		
 		// TODO - this is presentation layer and doesn't belong here
 		if( weight == 1 || weight > 999 ) {
 			// don't number these points
 			mapMarker = new IconMarker(eventBus, holdingMarker, position, gMap);
 		} else if( markerIcons.containsKey(weight) ) {
 			// marker icon already loaded
 			mapMarker = new IconMarker(eventBus, markerIcons.get(weight), position, gMap);
 		} else {
 			// keep track, this marker will need to be re-icon'ed later
 			mapMarker = new IconMarker(eventBus, holdingMarker, position, gMap);
 
			// see clearHoldingIcons()
			holdingIcons.add(new IconMarker(eventBus, holdingMarker, position, gMap));

 			if( ! markersNeedingIcons.containsKey(weight) )
 				markersNeedingIcons.put(weight, new ArrayList<IconMarker>());
 			markersNeedingIcons.get(weight).add(mapMarker);			
 		}
 
 		return mapMarker;
 	}
 
 	/**
 	 * When map markers are built fetching a numbered icon before the animation
 	 * would cause too much of a delay so they are loaded later and replace
 	 * the holding icon.
 	 */
 	private void updateMarkersNeedingIcons() {
 
 		MarkerImage icon;
 
 		for( Integer weight : markersNeedingIcons.keySet() ) {
 
 			if( markerIcons.containsKey(weight) ) {
 
 				icon = markerIcons.get(weight);
 				for( IconMarker m : markersNeedingIcons.get(weight) ) {
 					m.setIcon(icon);
 				}
 				markersNeedingIcons.remove(weight);
 
 			} else {
 				
 				final int weight_s = weight;
 				ImagePreloader.load(mapMarkersUrl+weight+"/", new ImageLoadHandler() {
 				    public void imageLoaded(ImageLoadEvent event) {
 				        if (event.isLoadFailed()) {
 				        	// TODO write to logger
 				            System.out.println("Image " + event.getImageUrl() + " failed.");
 				        } else {
 				        	//System.out.println("Image " + event.getImageUrl() + " OK");
 					        int width = event.getDimensions().getWidth();
 							int height = event.getDimensions().getHeight();
 							MarkerImage icon = MarkerImage.create(mapMarkersUrl+weight_s+"/",
 														  Size.create(width, height),
 														  Point.create(0, 0),
 														  Point.create(width/2, height/2));
 							markerIcons.put(weight_s, icon);
 							for( IconMarker m : markersNeedingIcons.get(weight_s) ) {
 								m.setIcon(icon);
 							}
 							markersNeedingIcons.remove(weight_s);
 							if( markersNeedingIcons.size() < 1 ) {
 								// instead of < 1 could be < number of failed images
 								removeHoldingIconsTimer.cancel();
 								removeHoldingIconsTimer.schedule(markerAnimationDuration);
 							}
 				        }
 				    }
 				});
 			}
 		}
 	}
 	
 	/**
 	 * holding icons are an additional set of blank markers which are placed to reduce
 	 * the flicker when the holding markers are replaced with numbered markers.
 	 * 
 	 * If shadow icons hadn't been removed with 'visual refresh' this wouldn't be needed
 	 */
 	private void clearHoldingIcons() {
 		for( IconMarker m : holdingIcons ) {
 			m.hideMarker();
 		}
 	}
 	
 	public void setLetterBox(LetterBox registeredLetterBox) {
 		this.letterBox = registeredLetterBox;
 	}
 
 	public void setMap(GoogleMap googleMap) {
 		
 		this.gMap = googleMap;
 
 		// pre-load default marker
 		ImagePreloader.load(mapMarkersUrl, new ImageLoadHandler() {
 		    public void imageLoaded(ImageLoadEvent event) {
 		        if (event.isLoadFailed()) {
 		        	// TODO write to logger
 		            System.out.println("Holding Image " + event.getImageUrl() + " failed.");
 		        } else {
 		        	
 		        	int width = event.getDimensions().getWidth();
 					int height = event.getDimensions().getHeight();
 					holdingMarker = MarkerImage.create(mapMarkersUrl,
 												  Size.create(width, height),
 												  Point.create(0, 0),
 												  Point.create(width/2, height/2));
 		        }
 		    }
 		});
 
 //		gMap.addIdleListener(new IdleHandler(){
 //
 //			@Override
 //			public void handle() {
 //				System.out.println("Idle now");
 //				
 //			}
 //			
 //		}); 
 //		gMap.addZoomChangedListener(new ZoomChangedHandler() {
 //
 //			@Override
 //			public void handle() {
 //				System.out.println("zoom changed");
 //			}
 //			
 //		});
 		gMap.addBoundsChangedListener(new BoundsChangedHandler() {
 
 			@Override
 			public void handle() {
 				//System.out.println("bounds changed");
 				requestTimer.cancel();
 				requestTimer.schedule(delayDuration);
 			}
 			
 		});
 		
 		gMap.addCenterChangedListener(new CenterChangedHandler() {
 
 			@Override
 			public void handle() {
 				//System.out.println("center changed");
 				requestTimer.cancel();
 				requestTimer.schedule(delayDuration);
 			}
 			
 		});
 		
 //		gMap.addDragEndListener(new DragEndHandler() {
 //
 //			@Override
 //			public void handle() {
 //				System.out.println("end drag");
 //			}
 //			
 //		});
 //		
 //		gMap.addResizeListener(new ResizeHandler() {
 //
 //			@Override
 //			public void handle() {
 //				System.out.println("resized");
 //			}
 //			
 //		});
 		
 	}
 
 	
 }
