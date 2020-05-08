 package ui.isometric;
 
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import util.Area;
 import util.Position;
 
 import clientinterface.GameModel;
 import clientinterface.GameThing;
 
 public class IsoGameModelDataSource implements IsoDataSource {
 	private GameModel gameModel;
 	private Area viewArea;
 	private IsoSquare[][] squares = new IsoSquare[0][0];
 	private ReentrantReadWriteLock cacheChange = new ReentrantReadWriteLock();
 	private IsoRendererLibrary rendererLibrary = new IsoRendererLibrary();
 	private IsoSquare emptySquare = new IsoSquare();
 	
 	public IsoGameModelDataSource(GameModel model) {
 		gameModel = model;
 	}
 	
 	@Override
 	public IsoSquare squareAt(int x, int y) {
 		cacheChange.readLock().lock();
 		IsoSquare tmp = squares[x][y];
 		cacheChange.readLock().unlock();
 		if(tmp == null) {
 			tmp = emptySquare;
 		}
 		return tmp;
 	}
 
 	@Override
 	public void setViewableRect(int xOrigin, int yOrigin, int width, int height) {
		cacheChange.writeLock().lock();
 		viewArea = new Area(xOrigin, yOrigin, width, height);
 		
 		this.resizeCache();
 		cacheChange.writeLock().unlock();
 	}
 	
 	private void resizeCache() {
 		IsoSquare[][] tmp = new IsoSquare[viewArea.width()][viewArea.height()];
 		
 		cacheChange.writeLock().lock();
 		for(int x = 0; x < tmp.length; x++) {
 			if(x < viewArea.width()) {
 				for(int y = 0; y < tmp[x].length; y++) {
 					if(y < viewArea.height()) {
 						tmp[x][y] = squares[x][y];
 					}
 				}
 			}
 		}
 		cacheChange.writeLock().unlock();
 	}
 	
 	@Override
 	public void update() {
 		cacheChange.writeLock().lock();
 		Iterable<GameThing> things = gameModel.thingsInRect(viewArea);
 		for(GameThing thing : things) {
 			Position pos = this.translate(thing);
 			IsoSquare square = squares[pos.x()][pos.y()];
 			if(square == null) {
 				square = new IsoSquare();
 			}
 			square.addImageForLevel(rendererLibrary.imageForRendererName(thing.renderer()), rendererLibrary.levelFromArguments(thing.userArguments()));
 		}
 		cacheChange.writeLock().unlock();
 	}
 
 	private Position translate(GameThing thing) {
 		return new Position(thing.area().x() - viewArea.x(), thing.area().y() - viewArea.y()); // TODO: rotation etc
 	}
 }
