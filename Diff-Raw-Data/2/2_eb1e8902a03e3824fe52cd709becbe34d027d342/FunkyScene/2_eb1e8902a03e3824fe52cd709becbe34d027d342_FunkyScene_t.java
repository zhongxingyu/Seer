 public class FunkyScene{
 
 	private static final int OBJECTS_LIMIT = 500;
 	private static final float QUICKEN_FACTOR = 0.8f;
 
 	public int width, height, cycles = 1;
	public FunkyBaseObject[] objects = new FunkyBaseObject[OBJECTS_LIMIT];
 	public int objectsNumber = 0;
 	public FunkyCoordinate mouseOffsets = new FunkyCoordinate(0, 0);
 
 	private int newObjectThreshold = 100;
 
 	public void tick() {
 		cycles++;
 		if (cycles % newObjectThreshold == 0) {
 			addObject();
 			if ( newObjectThreshold > 500 ) newObjectThreshold = (int) ( (float) newObjectThreshold * QUICKEN_FACTOR );
 		}
 	}
 
 	private void _addObject(int x, int y) {
 		if (objectsNumber < OBJECTS_LIMIT - 1)	{
 			objects[objectsNumber] = new FunkyBaseObject(x, y);
 			objectsNumber++;
 		}
 	}
 
 	public void addObject(int x, int y) {
 		_addObject(x, y);
 	}
 
 	public void addObject() {
 		_addObject((int)(Math.random() * width), (int)(Math.random() * height));
 	}
 }
