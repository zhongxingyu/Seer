 package yang.events;
 
 import java.io.IOException;
 
 import yang.events.eventtypes.SurfacePointerEvent;
 import yang.events.eventtypes.YangEvent;
 import yang.events.eventtypes.YangInput3DEvent;
 import yang.events.eventtypes.YangKeyEvent;
 import yang.events.eventtypes.YangSensorEvent;
 import yang.events.eventtypes.YangZoomEvent;
 import yang.events.listeners.YangEventListener;
 import yang.events.macro.MacroWriter;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.math.objects.Point3f;
 import yang.math.objects.Quaternion;
 import yang.util.YangList;
 
 //TODO split meta and runtime, esp. pointerTrackers/keyStates
 
 public class YangEventQueue {
 
 	public static final int MAX_KEY_INDICES = 512;
 	public static int MAX_POINTERS = 10;
 
 	public static final int ID_POINTER_EVENT = 0;
 	public static final int ID_KEY_EVENT = 1;
 	public static final int ID_ZOOM_EVENT = 2;
 	public static final int ID_SENSOR_EVENT = 3;
 	public static final int ID_INPUT3D_EVENT = 4;
 
 	public InputState mInputState,mMetaInputState;
 	private final int mMaxEvents;
 	private final SurfacePointerEvent[] mPointerEventQueue;
 	private final YangKeyEvent[] mKeyEventQueue;
 	private final YangZoomEvent[] mZoomEventQueue;
 	private final YangSensorEvent[] mSensorEventQueue;
 	private final YangInput3DEvent[] mInput3DEventQueue;
 	public YangEvent[][] mQueuePools;
 	private final YangEvent[] mQueue;
 	private final YangEvent[] mMetaEventQueue;
 	private int mPointerEventId;
 	private int mKeyEventId;
 	private int mZoomEventId;
 	private int mSensorEventId;
 	private int mInput3DEventId;
 	private int mQueueId;
 	private int mMetaEventQueueId;
 	private int mQueueFirst;
 	private int mMetaEventQueueFirst;
 	private GraphicsTranslator mGraphics;
 	private final boolean[] mMetaKeys;
 	public boolean mMetaMode = false;
 	public YangList<MacroWriter> mMacroWriters;
 	public float mSurfacePointerZ = 0;
 
 	public YangEventQueue(int maxEvents,int eventTypes) {
 		mGraphics = null;
 		mMaxEvents = maxEvents;
 		mPointerEventId = 0;
 		mKeyEventId = 0;
 		mQueueId = 0;
 		mMetaEventQueueId = 0;
 		mSensorEventId = 0;
 		mQueueFirst = 0;
 		mMetaEventQueueFirst = 0;
 		mInput3DEventId = 0;
 		mQueue = new YangEvent[maxEvents];
 		mMetaEventQueue = new YangEvent[maxEvents];
 		mPointerEventQueue = new SurfacePointerEvent[maxEvents];
 		mKeyEventQueue = new YangKeyEvent[maxEvents];
 		mZoomEventQueue = new YangZoomEvent[maxEvents];
 		mSensorEventQueue = new YangSensorEvent[maxEvents];
 		mInput3DEventQueue = new YangInput3DEvent[maxEvents];
 		for(int i=0;i<maxEvents;i++) {
 			mPointerEventQueue[i] = new SurfacePointerEvent();
 			//mPointerEventQueue[i].mEventStates = mInputState;
 			mKeyEventQueue[i] = new YangKeyEvent();
 			//mKeyEventQueue[i].mEventQueue = this;
 			mZoomEventQueue[i] = new YangZoomEvent();
 			//mZoomEventQueue[i].mEventQueue = this;
 			mSensorEventQueue[i] = new YangSensorEvent();
 			//mSensorEventQueue[i].mEventQueue = this;
 			mInput3DEventQueue[i] = new YangInput3DEvent();
 			//mInput3DEventQueue[i].mEventQueue = this;
 		}
 		mMetaKeys = new boolean[MAX_KEY_INDICES];
 		for(int i=0;i<mMetaKeys.length;i++) {
 			mMetaKeys[i] = false;
 		}
 
 		mInputState = new InputState(this);
 		mMetaInputState = new InputState(this);
 
 		mQueuePools = new YangEvent[eventTypes][];
 		mQueuePools[ID_POINTER_EVENT] = mPointerEventQueue;
 		mQueuePools[ID_KEY_EVENT] = mKeyEventQueue;
 		mQueuePools[ID_ZOOM_EVENT] = mZoomEventQueue;
 		mQueuePools[ID_SENSOR_EVENT] = mSensorEventQueue;
 		mQueuePools[ID_INPUT3D_EVENT] = mInput3DEventQueue;
 	}
 
 	public YangEventQueue(int maxEvents) {
 		this(maxEvents,5);
 	}
 
 	public synchronized void putRuntimeEvent(YangEvent event) {
 		if(mMetaMode) {
 			putMetaEvent(event);
 			return;
 		}
 		event.mInputState = mInputState;
 		mQueue[mQueueId++] = event;
 //		event.onPoll();
 		if(mQueueId>=mMaxEvents)
 			mQueueId = 0;
 	}
 
 	public synchronized void putEventForceRuntime(YangEvent event) {
 		event.mInputState = mInputState;
 		mQueue[mQueueId++] = event;
 //		event.onPoll();
 		if(mQueueId>=mMaxEvents)
 			mQueueId = 0;
 	}
 
 	public synchronized void putMetaEvent(YangEvent event) {
 		event.mInputState = mMetaInputState;
 		mMetaEventQueue[mMetaEventQueueId++] = event;
 //		event.onPoll();
 		if(mMetaEventQueueId>=mMaxEvents)
 			mMetaEventQueueId = 0;
 	}
 
 	public void registerEventWriter(MacroWriter writer) {
 		if(mMacroWriters==null)
 			mMacroWriters = new YangList<MacroWriter>();
 		mMacroWriters.add(writer);
 		writer.start();
 	}
 
 	public void removeEventWriter(MacroWriter writer) {
 		mMacroWriters.remove(writer);
 	}
 
 	public void setMetaKey(int code) {
 		mMetaKeys[code] = true;
 	}
 
 	public synchronized SurfacePointerEvent newPointerEvent() {
 		final SurfacePointerEvent newEvent = mPointerEventQueue[mPointerEventId++];
 		if(mPointerEventId>=mMaxEvents)
 			mPointerEventId = 0;
 		return newEvent;
 	}
 
 	public synchronized YangKeyEvent newKeyEvent() {
 		final YangKeyEvent newEvent = mKeyEventQueue[mKeyEventId++];
 		if(mKeyEventId>=mMaxEvents)
 			mKeyEventId = 0;
 		return newEvent;
 	}
 
 	public synchronized YangZoomEvent newZoomEvent() {
 		final YangZoomEvent newEvent = mZoomEventQueue[mZoomEventId++];
 		if(mZoomEventId>=mMaxEvents)
 			mZoomEventId = 0;
 		return newEvent;
 	}
 
 	public synchronized YangSensorEvent newSensorEvent() {
 		final YangSensorEvent newEvent = mSensorEventQueue[mSensorEventId++];
 		if(mSensorEventId>=mMaxEvents)
 			mSensorEventId = 0;
 		return newEvent;
 	}
 
 	public synchronized YangInput3DEvent newInput3DEvent() {
 		final YangInput3DEvent newEvent = mInput3DEventQueue[mInput3DEventId++];
 		if(mInput3DEventId>=mMaxEvents)
 			mInput3DEventId = 0;
 		return newEvent;
 	}
 
 
 	public synchronized void putPointerEvent(int action, float x,float y,float z, int button, int id) {
 		final SurfacePointerEvent newEvent = mPointerEventQueue[mPointerEventId++];
 		if(mPointerEventId>=mMaxEvents)
 			mPointerEventId = 0;
 		newEvent.mButton = button;
 		newEvent.mX = x;
 		newEvent.mY = y;
 		newEvent.mZ = z;
 		newEvent.mAction = action;
 		newEvent.mId = id;
 		putRuntimeEvent(newEvent);
 	}
 
 	public synchronized void putSurfacePointerEvent(int action, int x,int y, int button, int id) {
 		if(mGraphics==null)
 			return;
 		putPointerEvent(action, mGraphics.toNormX(x),mGraphics.toNormY(y),mSurfacePointerZ, button,id);
 	}
 
 	public synchronized void putKeyEvent(int key, int action) {
 		final YangKeyEvent newEvent = mKeyEventQueue[mKeyEventId++];
 		if(mKeyEventId>=mMaxEvents)
 			mKeyEventId = 0;
 		newEvent.mKey = key;
 		newEvent.mAction = action;
 		if(key<MAX_KEY_INDICES && mMetaKeys[key])
 			putMetaEvent(newEvent);
 		else
 			putRuntimeEvent(newEvent);
 	}
 
 	public void putZoomEvent(float value) {
 		final YangZoomEvent newEvent = mZoomEventQueue[mZoomEventId++];
 		if(mZoomEventId>=mMaxEvents)
 			mZoomEventId = 0;
 		newEvent.mValue = value;
 		putRuntimeEvent(newEvent);
 	}
 
 	public void putSensorEvent(int eventType,float x,float y,float z) {
 		final YangSensorEvent newEvent = mSensorEventQueue[mSensorEventId++];
 		if(mSensorEventId>=mMaxEvents)
 			mSensorEventId = 0;
 		newEvent.mType = eventType;
 		newEvent.mX = x;
 		newEvent.mY = y;
 		newEvent.mZ = z;
 		putRuntimeEvent(newEvent);
 	}
 
 	public void putSensorEvent(int eventType,float x,float y,float z, float w) {
 		final YangSensorEvent newEvent = mSensorEventQueue[mSensorEventId++];
 		if(mSensorEventId>=mMaxEvents)
 			mSensorEventId = 0;
 		newEvent.mType = eventType;
 		newEvent.mX = x;
 		newEvent.mY = y;
 		newEvent.mZ = z;
 		newEvent.mW = w;
 		putRuntimeEvent(newEvent);
 	}
 
 	public void putSensorEvent(int eventType,float[] values) {
 		final YangSensorEvent newEvent = mSensorEventQueue[mSensorEventId++];
 		if(mSensorEventId>=mMaxEvents)
 			mSensorEventId = 0;
 		newEvent.mType = eventType;
 		newEvent.mX = values[0];
 		newEvent.mY = values[1];
 		newEvent.mZ = values[2];
 		if(values.length>3)
 			newEvent.mW = values[3];
 		putRuntimeEvent(newEvent);
 	}
 
 	public void putInput3DEvent(int id, float x,float y,float z, float qx,float qy,float qz,float qw) {
 		final YangInput3DEvent newEvent = mInput3DEventQueue[mInput3DEventId++];
 		if(mInput3DEventId>=mMaxEvents)
 			mInput3DEventId = 0;
 		newEvent.mId = id;
 		newEvent.mPosition.set(x,y,z);
 		newEvent.mOrientation.set(qx,qy,qz,qw);
 		putRuntimeEvent(newEvent);
 	}
 
 	public void putInput3DEvent(int id, Point3f mPosition,Quaternion mOrientation) {
 		final YangInput3DEvent newEvent = mInput3DEventQueue[mInput3DEventId++];
 		if(mInput3DEventId>=mMaxEvents)
 			mInput3DEventId = 0;
 		newEvent.mId = id;
 		newEvent.mPosition.set(mPosition);
 		newEvent.mOrientation.set(mOrientation);
 		putRuntimeEvent(newEvent);
 	}
 
 	public void putInput3DEvent(int id, float x,float y,float z) {
 		putInput3DEvent(id, x,y,z, 0,0,0,1);
 	}
 
 	public synchronized YangEvent peekEvent() {
 		if(mQueueFirst==mQueueId)
 			return null;
 		else
 			return mQueue[mQueueFirst];
 	}
 
 	public YangEvent peekMetaEvent() {
 		if(mMetaEventQueueFirst==mMetaEventQueueId)
 			return null;
 		else
 			return mMetaEventQueue[mMetaEventQueueFirst];
 	}
 
 	public synchronized YangEvent pollEvent() {
 		final YangEvent result = peekEvent();
 		if(result==null)
 			return null;
 		else{
 			result.onPoll();
 			mQueueFirst++;
 			if(mQueueFirst>=mMaxEvents)
 				mQueueFirst = 0;
 			return result;
 		}
 	}
 
 	public synchronized YangEvent pollMetaEvent() {
 		final YangEvent result = peekMetaEvent();
 		if(result==null)
 			return null;
 		else{
 			result.onPoll();
 			mMetaEventQueueFirst++;
 			if(mMetaEventQueueFirst>=mMaxEvents)
 				mMetaEventQueueFirst = 0;
 			return result;
 		}
 	}
 
 	public boolean hasEvent() {
 		return mQueueFirst!=mQueueId;
 	}
 
 	public boolean hasMetaEvent() {
 		return mMetaEventQueueFirst!=mMetaEventQueueId;
 	}
 
 	public synchronized void handleEvents(YangEventListener eventInterface) {
 		YangEvent event;
 		while((event = pollEvent())!=null) {
 			if(mMacroWriters!=null) {
 				for(final MacroWriter writer:mMacroWriters) {
					if (writer.isOpen()) {
						try {
							writer.writeEvent(event);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
 					}
 				}
 			}
 			event.handle(eventInterface);
 		}
 	}
 
 	public void handleMetaEvents(YangEventListener eventInterface) {
 		YangEvent event;
 		while((event = pollMetaEvent())!=null) {
 			event.handle(eventInterface);
 		}
 	}
 
 	public void clearEvents() {
 		mQueueFirst = mQueueId;
 	}
 
 	public void clearMetaEvents() {
 		mMetaEventQueueFirst = mMetaEventQueueId;
 	}
 
 	public void setGraphics(GraphicsTranslator graphics) {
 		mGraphics = graphics;
 	}
 
 	public void setMetaKeys(int startKey, int count) {
 		for(int i=0;i<count;i++) {
 			setMetaKey(startKey+i);
 		}
 	}
 
 	public void clearMetaKeys() {
 		for (int i = 0; i < mMetaKeys.length; i++) mMetaKeys[i] = false;
 	}
 
 	public void close() {
 		if(mMacroWriters!=null) {
 			for(final MacroWriter writer:mMacroWriters)
 				writer.close();
 		}
 	}
 
 	public double getTime() {
 		return System.currentTimeMillis()*0.001;
 	}
 
 	public boolean isKeyDown(int code) {
 		return mInputState.isKeyDown(code);
 	}
 
 	public boolean isMetaKeyDown(int code) {
 		return mMetaInputState.isKeyDown(code);
 	}
 
 }
