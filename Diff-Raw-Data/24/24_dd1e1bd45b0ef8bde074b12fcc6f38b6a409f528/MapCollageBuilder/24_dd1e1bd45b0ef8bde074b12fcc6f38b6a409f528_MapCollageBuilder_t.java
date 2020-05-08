 package Generator;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import ActivationManager.DedicatedRequest;
 import Bing.BingServices;
 import Bing.Pushpin;
 import Bing.StaticMap;
 import Common.ActualEvent;
 import Common.ActualEventsBundle;
 import Common.Photo;
 import Generator.LocatePicturesWithMap.SlotPushPinTuple;
 import android.R.bool;
 import android.R.integer;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.Log;
 
 public class MapCollageBuilder extends AbstractBuilder{
 	
 	MapTemplate template = null;
 	static {@SuppressWarnings("unused")
 	byte[] dummy = new byte[8 * 1024 * 1024];
 	}
 	
 	private static final String TAG = MapCollageBuilder.class.getName();
 
 	public MapCollageBuilder(ActualEventsBundle bundle) {
 		super(bundle);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public Photo buildCollage() {
 
 		System.gc();
 		Canvas canvas = null;
 		Bitmap bmpBase = null;
 
 		// moved to comments since canvas size for map collage was changes
 		// bmpBase = Bitmap.createBitmap(3264, 2448, Bitmap.Config.RGB_565);
 		bmpBase = Bitmap.createBitmap(1469, 1102, Bitmap.Config.RGB_565);
 		canvas = new Canvas(bmpBase);
 
 		
 		populateTemplate();
 		Slot slotToAddToCanvas;
 		Bitmap bitmap = null;
 		// draw images saved in Template onto canvas
 		for (int slot = 0; slot < template.getNumberOfSlots(); slot ++) {
 			try {
 				slotToAddToCanvas = template.getSlot(slot);
 				addSlotImageToCanvas(bitmap, canvas,slotToAddToCanvas);
 			}
 			catch (NullPointerException exception) {
 				// TODO: deal with error
 				int x = 5;
 			}
 		}
 		
 		// draw Bing map into output
 		try {
 			addSlotImageToCanvas(bitmap, canvas, template.getMapSlot());
 		}
 		catch (NullPointerException exception) {
 			//TODO: deal with error
 		}
 		
 		//free bitmap
 		bitmap.recycle();
 		bitmap = null;
 
 		// add lines
 		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		paint.setColor(android.graphics.Color.MAGENTA);
 		paint.setStrokeWidth(3f);
 		canvas.drawLine(642, 2080, 2448, 642, paint);
 
 		Photo collage;
 		try {
 			collage = saveCollage(bmpBase);
 		}
 		catch (IOException exception) {
 			Log.e(TAG, "Error when saving collage file");
 			// TODO: notify user about error in saving collage
 			return null;
 		}
 
 		clearProcessPhotos(); // not to reuse same photos
 		
 		return collage;
 	}
 
 	/**
 	 * This method locate the different pictures in the relevant slots. Return true upon success. 
 	 */
 	public boolean populateTemplate() {
 		
 		template = MapTemplate.getTemplate(3);
 		Set<PixelPoint> connectionPixelPoints = template.getLinesConnectionPoints();
 		List<Photo> photosList = new LinkedList<Photo>();
 		for (ActualEvent event: bundle.getActualEvents() )
 		{
 			photosList.add(event.selectPhotoFromEvent());
 		}
 		StaticMap mapFromDataSource = BingServices.getStaticMap(photosList, template.getMapPixelWidth(), template.getMapPixelHeight());
 		//StaticMap mapFromDataSource = BingServices.getStaticMap(photosList, 899,833);
 		template.setMap(mapFromDataSource);
 		HashMap<PixelPoint, Pushpin> pixelPointsToPushPins = getAdjustedPixelPointPushPinDictionary(mapFromDataSource.getPushPins());
 		HashMap<PixelPoint, Slot> pixelPointsToSlot = getPixelPointSlotDictionaryHashMap(template.slots);
 		LocatePicturesWithMap locatePicturesWithMap = new LocatePicturesWithMap(pixelPointsToSlot, pixelPointsToPushPins);
 		List<SlotPushPinTuple> tuples = locatePicturesWithMap.matchPictureOnMapToPointOnFrame();
 		updatePicturesOfSlots (tuples,photosList);
 		return true;
 	}
 	
 	@Override
 	public DedicatedRequest setTemplate() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 	/**
 	 * @param slots - array of slots of the template
 	 * @return dictionary which its keys are pixelPoints of "connection" points of the slots, and values are the relvant slots
 	 */
 	private HashMap<PixelPoint, Slot> getPixelPointSlotDictionaryHashMap (Slot[] slots)
 	{
 		if (slots == null)
 			return null;
 		 HashMap<PixelPoint, Slot> pixelPointSlotDictionary = new  HashMap<PixelPoint, Slot>();
 		 for (Integer i=0; i< slots.length; i++)
 		 {
 			 if (slots[i].hasConnectingLinePoint())
 				 pixelPointSlotDictionary.put(slots[i].getConnectingLinePoint(), slots[i]);
 		 }
 		 return pixelPointSlotDictionary;
 	}
 	
 	/**
 	 * @param pushPins - the list of pushPins on map retrieved from bing
 	 * @return Dictionary which contains the actual pixel of the pushPin in the output collage as key, and the pushPin object as value 
 	 */
 	
 	private HashMap<PixelPoint, Pushpin> getAdjustedPixelPointPushPinDictionary(List<Pushpin> pushPins)
 	{
 		if (pushPins == null)
 			return null;
 		Integer xInterval = template.getMapSlot().getTopLeft().getX();
 		Integer yInterval = template.getMapSlot().getTopLeft().getY();
 		HashMap<PixelPoint, Pushpin> adjustedPushPinsPixelPoints = new HashMap<PixelPoint, Pushpin>();
 		
 		
 		PixelPoint tempPixelPoint;
 		// the adjusted pixel point for each pushPin its is originalX + the top left X coordinate of the map (the same for Y coordinate)
 		for (Pushpin pin: pushPins)
 		{
 			tempPixelPoint = new PixelPoint(xInterval + pin.getAnchor().getX(), yInterval + pin.getAnchor().getY());
 			adjustedPushPinsPixelPoints.put(tempPixelPoint, pin);
 		}
 		return adjustedPushPinsPixelPoints ;
 	}
 	
 	
 	/**
 	 * @param tuples - tuples of location PixelPoint and pushPin pixelPoint
 	 * @param photosList - list of photos in the collage
 	 * The method assign to each slot in the template its relevant picture
 	 */
 	private boolean updatePicturesOfSlots (List<SlotPushPinTuple> tuples, List<Photo> photosList)
 	{ 
 		if ((tuples == null) || (photosList == null))
 			return false;
 		for (SlotPushPinTuple tuple : tuples)
 		{
 			if ((tuple.getPushpin() == null) || (tuple.getSlot() == null))
 			{
 				return false;
 			}
 			for (Photo photo: photosList)
 			{
 				if (photo.getLocation() == null)
 					return false;
 				if (photo.getLocation().equals(tuple.getPushpin().getPoint()))
 				{
 					tuple.getSlot().assignToPhoto(photo);
 				}
 			
 			}
 		}
		for (SlotPushPinTuple tuple : tuples)
		{
			if (tuple.getSlot().getPhoto() == null)
			{
				Integer x = 3;
				return false;
			}
		}
			
 		return true;
 	}
 	
 	
 	
 }
