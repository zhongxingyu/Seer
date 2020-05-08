 package ui.isometric;
 
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import util.Area;
 import util.Direction;
 import util.Position;
 
 import client.model.GameModel;
 import client.model.GameThing;
 import client.model.LevelLocation;
 import client.model.Location;
 
 /**
  * 
  * A class that provides data from a GameModel to an IsoCanvas
  * 
  * @author melby
  *
  */
 public class IsoGameModelDataSource implements IsoDataSource {
 	private GameModel gameModel;
 	private IsoSquare[][] squares = null;
 	private ReentrantReadWriteLock cacheChange = new ReentrantReadWriteLock();
 	private IsoSquare emptySquare = new IsoSquare();
 	
 	private Direction viewDirection;
 	private Area querryArea;
 	private IsoTransformImp transform;
 	
 	private int arrayPaddingY = 100; // TODO: calculate
 	private int arrayPaddingX = 100; // TODO: calculate
 	
 	/**
 	 * Create a IsoGameModelDataSource with a given GameModel
 	 * @param model
 	 */
 	public IsoGameModelDataSource(GameModel model) {
 		gameModel = model;
 	}
 	
 	@Override
 	public IsoSquare squareAt(int x, int y) {
 		cacheChange.readLock().lock();
 		IsoSquare tmp;
 		try {
 			tmp = squares[x+arrayPaddingX][y+arrayPaddingY];
 		}
 		catch (IndexOutOfBoundsException e) {
 			tmp = null;
 		}
 		cacheChange.readLock().unlock();
 		if(tmp == null) {
 			tmp = emptySquare;
 		}
 		return tmp;
 	}
 
 	@Override
 	public void setViewableRect(int xOrigin, int yOrigin, int width, int height, Direction direction) {
 		cacheChange.writeLock().lock();
 		
 		transform = new IsoTransformImp(xOrigin, yOrigin, width, height, direction);
 		viewDirection = direction;
 		Area oldArea = querryArea;
 		querryArea = transform.querryArea();
 		
 		if(oldArea == null || !oldArea.equals(querryArea)) {
 			this.clearCache();
 		}
 		cacheChange.writeLock().unlock();
 	}
 	
 	/**
 	 * Clear and resize the internal cache to the size in querryArea at least
 	 */
 	private void clearCache() {
 		IsoSquare[][] tmp = new IsoSquare[querryArea.width()+arrayPaddingX*2][querryArea.height()+arrayPaddingY*2];
 		
 		cacheChange.writeLock().lock();
 		squares = tmp;
 		cacheChange.writeLock().unlock();
 	}
 	
 	@Override
 	public void update() {
 		cacheChange.writeLock().lock();
 		this.clearCache();
 		
		Iterable<GameThing> things = gameModel.level(0).portion(querryArea); // TODO: multi levels
 		for(GameThing thing : things) {
 			Location l = thing.location();
 			if(l instanceof LevelLocation) {
 				Position pos = transform.transformMapPosition(((LevelLocation)l).position());
 				IsoSquare square = squares[pos.x()+arrayPaddingX][pos.y()+arrayPaddingY];
 				if(square == null) {
 					square = new IsoSquare();
 				}
 				square.addImageForLevel(IsoRendererLibrary.newImageFromGameThing(square, thing, viewDirection), IsoRendererLibrary.levelFromArguments(thing.userArguments()));
 				squares[pos.x()+arrayPaddingX][pos.y()+arrayPaddingY] = square;
 			}
 		}
 		cacheChange.writeLock().unlock();
 	}
 	
 	@Override
 	public IsoTransform transform() {
 		return transform;
 	}
 }
