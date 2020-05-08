 package model;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Set;
 
 import model.physics.Vect;
 import exception.CannotRotateException;
 
 
 public class Overlord extends Observable implements iOverlord {
 
 	private HashMap<String, iGizmo> gizmos;
 	private HashMap<Integer, ArrayList<iGizmo>> keyTriggersDown;
 	private HashMap<Integer, ArrayList<iGizmo>> keyTriggersUp;
 	private ArrayList<String> connects;
 	private HashMap<String, iBall> balls;
 	private FileParser fileParse;
 	private String[][] board;
 	private double cellWidth, cellHeight;
 	private double gravity;
 	private float mu, mu2;
 	private boolean loadingFile;
 
 	public Overlord(Dimension gridDimentions, Dimension canvasDimentions) {
 
 		cellWidth 			= canvasDimentions.getWidth() / gridDimentions.getWidth();
 		cellHeight			= canvasDimentions.getHeight() / gridDimentions.getHeight();
 		gizmos 				= new HashMap<String, iGizmo>();
 		balls 				= new HashMap<String, iBall>();
 		keyTriggersDown 	= new HashMap<Integer, ArrayList<iGizmo>>();
 		keyTriggersUp 		= new HashMap<Integer, ArrayList<iGizmo>>();
 		connects 			= new ArrayList<String>();
 		board 				= new String[gridDimentions.height][gridDimentions.width]; 
 		gravity 			= ((double)1) / 25;
 
 		for (int x = 0; x < gridDimentions.width; x++) {
 			for (int y = 0; y < gridDimentions.height; y++) {
 				board[y][x] = "";
 			}
 		}
 		gizmos.put("Wall", new Wall(cellWidth, cellHeight));
 	}
 
 	@Override
 	public boolean removeGizmo(String gizmoName) {
 		iGizmo gizRem = getGizmo(gizmoName);
 		if(gizRem != null){
 			for (iGizmo giz : getGizmos()) {
 				giz.removeTrigger(gizRem);
 			}
 			for (Integer keyVal : keyTriggersDown.keySet()) {
 				keyTriggersDown.get(keyVal).remove(gizRem);
 	
 			}
 			for (Integer keyVal : keyTriggersUp.keySet()) {
 				keyTriggersUp.get(keyVal).remove(gizRem);
 			}
 			gizmos.remove(gizmoName);
 			connects.remove(gizmoName);
 			removeFromBoard(gizmoName);
 			
 			setChanged();
 			notifyObservers(gizmoName);
 			
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public List<iGizmo> getGizmos() {
 		return new ArrayList<iGizmo>(gizmos.values());
 	}
 
 	@Override
 	public iGizmo getGizmo(String gizmoName) {
 		return gizmos.get(gizmoName);
 	}
 
 	@Override
 	public List<iBall> getBalls() {
 		ArrayList<iBall> tmp 		= new ArrayList<iBall>(balls.values());
 		ArrayList<iBall> returned 	= new ArrayList<iBall>();
 		for (iBall b : tmp) {
 			returned.add(b);
 		}
 		return returned;
 	}
 
 	@Override
 	public iBall getBall(String identifier) {
 		for (iBall b : getBalls())
 			if (b.getIdentifier().equals(identifier))
 				return b;
 		return null;
 	}
 
 	@Override
 	public void setGravity(double newGrav) {
 		gravity = newGrav;
 		
 		for(iBall b : getBalls()) { 
 			b.setgravity(gravity);
 		}
 	}
 
 	@Override
 	public void saveGame(String fileName) {
 		fileParse = new FileParser(this);
 		fileParse.saveFile(fileName);
 		for (iGizmo ig : getGizmos()) {
 			if (!(ig instanceof Wall)) {
 				fileParse.saveGizmo(ig.toString());
 				if (ig instanceof TriangleBumper) {
 					for (int i = (int) ig.getRotation(); i > 0; i = (i - 90)) {
 						fileParse.saveGizmo("Rotate " + ig.getIdentifier());
 					}
 				}
 			}
 		}
 		for (String s : connects) {
 			for (iGizmo g : getGizmo(s).getTriggers()) {
 				fileParse.saveGizmo("Connect " + s + " " + g.getIdentifier());
 			}
 		}
 		// map iterator code from
 		// http://www.devmanuals.com/tutorials/java/corejava/Collection/Map/HashMap/GetKeyAndValue.html
 		Set mapSet = (Set) keyTriggersDown.entrySet();
 		Iterator mapIterator = mapSet.iterator();
 		while (mapIterator.hasNext()) {
 			Map.Entry mapEntry = (Map.Entry) mapIterator.next();
 			for (iGizmo g : (ArrayList<iGizmo>) mapEntry.getValue()) {
 				fileParse.saveGizmo("KeyConnect Key " + mapEntry.getKey()
 						+ " down " + g.getIdentifier());
 			}
 		}
 		mapSet = (Set) keyTriggersUp.entrySet();
 		mapIterator = mapSet.iterator();
 		while (mapIterator.hasNext()) {
 			Map.Entry mapEntry = (Map.Entry) mapIterator.next();
 			for (iGizmo g : (ArrayList<iGizmo>) mapEntry.getValue()) {
 				fileParse.saveGizmo("KeyConnect Key " + mapEntry.getKey()
 						+ " up " + g.getIdentifier());
 			}
 		}
 		for (iBall b : getBalls()) {
 			fileParse.saveGizmo(b.toString());
 		}
 		fileParse.closeSaveFile();
 		fileParse = null;
 	}
 
 	public void fileError() {
 		javax.swing.JOptionPane.showMessageDialog(
 			null, 
 			"The file you tried to load from was found to have an error.\n" +
 			"You will be returned to the build screen with the Corruptted gizmo missing.\n" +
 			"You can now try and rebuild it.",
 			"Corrupt File",
 			javax.swing.JOptionPane.WARNING_MESSAGE);
 	}
 
 	@Override
 	public void setFriction(float mu, float mu2) {
 		this.mu 	= mu;
 		this.mu2 	= mu2;
 	}
 
 	private boolean canPlace(String ex, int startX, int startY, int endX, int endY) {
 		if(startX < 0 || startX > 19 || startY < 0 || startY > 19 || endX < 0 || endX > 19 || endY < 0 || endY > 19) {
 					 return false;
 		}
 		if (startX == endX && startY == endY) {
 			if (!board[startY][startX].equals("") && !board[startY][startX].equals(ex)){
 				return false;
 			}
 			return true;
 		} else if(ex.contains("F")){
 			if(startX < 0 || startX > 18 || startY < 0 || startY > 18 || endX < 0 || endX > 19 || endY < 0 || endY > 19){
 					 return false;
 			}
 			if ((!board[startY][startX].equals("") && !board[startY][startX].equals(ex)) ||
 				(!board[startY][startX+1].equals("") && !board[startY][startX+1].equals(ex)) ||
 				(!board[startY+1][startX].equals("") && !board[startY+1][startX].equals(ex)) ||
 				(!board[startY+1][startX+1].equals("") && !board[startY+1][startX+1].equals(ex)))
 				return false;	
 		} else {
 			for (int y = startY; y < endY; y++) {
 				for (int x = startX; x < endX; x++) {
 					if (ex.length() > 0) {
 						if (!board[y][x].equals("") && !board[y][x].equals(ex))
 							return false;
 					} else {
 						if (!board[y][x].equals(""))
 							return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean canPlaceBall(String ex, float startX, float startY, float endX, float endY) {
 
 		int x = (int) Math.floor(startX);
 		int y = (int) Math.floor(startY);
 		
 		if (ex.length() > 0) {
 			if (!board[y][x].equals("") && !board[y][x].equals(ex)) {
 				return false;
 			}
 		} else {
 			if (!board[y][x].equals(""))
 				return false;
 		}
 		return true;
 	}
 
 	private void setPlace(String place, int startX, int startY, int endX, int endY) {
 		if(place.contains("S") || place.contains("T") || place.contains("C")){
 			board[startY][startX] = place;
 		}else if(place.contains("F")){
 			board[startY][startX] = place;
 			board[startY][startX+1] = place;
 			board[startY+1][startX] = place;
 			board[startY+1][startX+1] = place;
 		}else if(place.contains("A")){
 			if(startY == endY){
 				for(int i = startX; i < endX; i++){
 					board[startY][i] = place;
 				}
 			}else{
 				for(int y = startY; y < endY; y++){
 					for(int i = startX; i < endX; i++){
 						board[y][i] = place;
 					}
 				}
 			}
 		}
 	}
 
 	private void removeFromBoard(String toRemove) {
 		for (int x = 0; x < board[0].length; x++) {
 			for (int y = 0; y < board.length; y++) {
 				if (board[y][x].equals(toRemove)) {
 					board[y][x] = "";
 				}
 			}
 		}
 	}
 	
 	private void clearBoard(){
 		for (int x = 0; x < board[0].length; x++) {
 			for (int y = 0; y < board.length; y++) {
 					board[y][x] = "";
 			}
 		}
 	}
 
 	@Override
 	public boolean addAbsorber(String id, int x, int y, int x2, int y2) {
 		int height = Math.abs(y - y2);
 		int width = Math.abs(x - x2);
 		if(height < 1 || width < 1){
 			return false;
 		}
 		
 		if (canPlace(id, x, y, (x + width-1), (y + height-1))) {
 			gizmos.put(id, new Absorber(id, new GizPoint(x, y), width, height, cellWidth, cellHeight));
 			setPlace(id, x, y, (x + width-1), (y + height-1));
 
 			if (!loadingFile) {
 				setChanged();
 				notifyObservers(id);
 			}
 			return true;
 		}
 		return false;
 
 	}
 
 	@Override
 	public boolean addBall(String ballName, String absorberName, float x, float y, double vx, double vy) {
 		iGizmo absorb = null;
 		if (absorberName.length() > 0) {
 			absorb = getGizmo(absorberName);
 		}
 		if (absorb != null) {
 			if (canPlaceBall(absorberName, x, y, x, y)) {
 				iBall newBall = new Ball(ballName, new BallPoint(x, y), 1, 1, cellWidth, cellHeight, vx, vy, gravity, true);
 				newBall.setCaptured(true);
 				balls.put(ballName, newBall);
 				((Absorber) absorb).captureBall(newBall, true);
 				return true;
 			}
 		} else {
 			if (canPlaceBall("", x, y, x, y)) {
 				iBall newBall = new Ball(ballName, new BallPoint(x, y), 1, 1, cellWidth, cellHeight, vx, vy, gravity, false);
 				newBall.setCaptured(false);
 				balls.put(ballName, newBall);
 				setPlace(ballName, (int) x, (int) y, (int) x, (int) y);
 				if (!loadingFile) {
 					setChanged();
 					notifyObservers(ballName);
 				}
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean moveGizmo(String gizmoName, int x, int y) {
 		iGizmo temp = getGizmo(gizmoName);
 		if(temp == null) return false;
 		if (canPlace(gizmoName, x, y, x + (temp.getWidth()-1), y + (temp.getHeight()-1))) {
 			temp.setLocation(new GizPoint(x, y));
 			removeFromBoard(gizmoName);
 			setPlace(gizmoName, x, y, x + (temp.getWidth()),
 					y + (temp.getHeight()));
 			setChanged();
 			notifyObservers(gizmoName);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean addCircle(String id, int x, int y) {
 		if (canPlace(id, x, y, x, y)) {
 			gizmos.put(id, new CircleBumper(id, new GizPoint(x, y), 1, 1,
 					cellWidth, cellHeight));
 			setPlace(id, x, y, x, y);
 			if (!loadingFile) {
 				setChanged();
 				notifyObservers(id);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean addFlipper(String id, int x, int y, boolean orient) {
 		if (canPlace(id, x, y, x + 1, y + 1)) {
 			if (!orient) {
 				gizmos.put(id, new LeftFlipper(id, new GizPoint(x, y), 1, 2,
 						cellWidth, cellHeight));
 				setPlace(id, x, y, x + 2, y + 2);
 			} else {
 				gizmos.put(id, new RightFlipper(id, new GizPoint(x, y), 1, 2,
 						cellWidth, cellHeight));
 				setPlace(id, x, y, x + 2, y + 2);
 			}
 			if (!loadingFile) {
 				setChanged();
 				notifyObservers(id);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean addSquare(String id, int x, int y) {
 		if (canPlace(id, x, y, x, y)) {
 			gizmos.put(id, new SquareBumper(id, new GizPoint(x, y), 1, 1,
 					cellWidth, cellHeight));
 			setPlace(id, x, y, x, y);
 			if (!loadingFile) {
 				setChanged();
 				notifyObservers(id);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean addTriangle(String id, int x, int y) {
 		if (canPlace(id, x, y, x, y)) {
 			gizmos.put(id, new TriangleBumper(id, new GizPoint(x, y), 1, 1,
 					cellWidth, cellHeight));
 			setPlace(id, x, y, x, y);
 			if (!loadingFile) {
 				setChanged();
 				notifyObservers(id);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public boolean rotateGizmo(String gizmoName) throws CannotRotateException {
 		iGizmo tmp = getGizmo(gizmoName);
 		if (tmp != null) {
			if ((tmp.getWidth() == 1 && tmp.getHeight() == 1)
					|| (tmp.getWidth() == 2 && tmp.getHeight() == 2)) {

 				tmp.rotate(); // 90
 				setChanged();
 				notifyObservers(gizmoName);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyConnect(int keyNum, boolean direction, String consumer) {
 		ArrayList<iGizmo> tmp;
 		iGizmo con = getGizmo(consumer);
 		if (con == null)
 			return false;
 		if (direction) {
 			tmp = (ArrayList<iGizmo>) keyTriggersUp.get(keyNum);
 			if (tmp == null) {
 				tmp = new ArrayList<iGizmo>();
 			}
 			tmp.add(con);
 			keyTriggersUp.put(keyNum, tmp);
 			return true;
 		} else {
 			tmp = (ArrayList<iGizmo>) keyTriggersDown.get(keyNum);
 			if (tmp == null) {
 				tmp = new ArrayList<iGizmo>();
 			}
 			tmp.add(con);
 			keyTriggersDown.put(keyNum, tmp);
 			return true;
 		}
 
 	}
 	
 	@Override
 	public boolean removeKeyConnect(int keyNum, boolean direction, String gizName) {
 		ArrayList<iGizmo> tmp;
 		iGizmo con = getGizmo(gizName);
 		if (con == null)
 			return false;
 		if (direction) {
 			tmp = (ArrayList<iGizmo>) keyTriggersUp.get(keyNum);
 			if (tmp == null) {
 				return false;
 			}
 			tmp.remove(con);
 			keyTriggersUp.put(keyNum, tmp);
 			return true;
 		} else {
 			tmp = (ArrayList<iGizmo>) keyTriggersDown.get(keyNum);
 			if (tmp == null) {
 				return false;
 			}
 			tmp.remove(con);
 			keyTriggersDown.put(keyNum, tmp);
 			return true;
 		}
 	}
 
 	@Override
 	public boolean connect(String producerGizmo, String consumerGizmo) {
 		String[] tmp = new String[2];
 		iGizmo producer = getGizmo(producerGizmo);
 		iGizmo consumer = getGizmo(consumerGizmo);
 		tmp[0] = producerGizmo;
 		tmp[1] = consumerGizmo;
 		if (producer == null || consumer == null) return false;
 		producer.addTrigger(consumer);
 		connects.add(producerGizmo);
 		setChanged();
 		notifyObservers();
 		return true;
 	}
 	
 	public boolean disconnect(String producerGizmo, String consumerGizmo){
 		String[] tmp = new String[2];
 		iGizmo producer = getGizmo(producerGizmo);
 		iGizmo consumer = getGizmo(consumerGizmo);
 		tmp[0] = producerGizmo;
 		tmp[1] = consumerGizmo;
 		if (producer == null || consumer == null)
 			return false;
 		producer.removeTrigger(consumer);
 		if(producer.getTriggerCount() == 0){
 		connects.remove(producerGizmo);
 		}
 		setChanged();
 		notifyObservers();
 		return true;
 		
 	}
 
 	@Override
 	public void loadGame(String mapName) {
 		gizmos.clear();
 		keyTriggersDown.clear();
 		keyTriggersUp.clear();
 		connects.clear();
 		balls.clear();
 		clearBoard();
 		loadingFile = true;
 		fileParse = new FileParser(this);
 		fileParse.loadFile(mapName);
 		fileParse = null;
 		loadingFile = false;
 		gizmos.put("Wall", new Wall(cellWidth, cellHeight));
 		
 		setChanged();
 		notifyObservers();
 	}
 
 	@Override
 	public void setBallLocation(String ballName, float x, float y) {
 		balls.get(ballName).setLocation(new BallPoint(x, y));
 		setChanged();
 		notifyObservers(ballName);
 
 	}
 
 	@Override
 	public boolean removeBall(String ballName) {
 		iBall ball = balls.remove(ballName);
 		if (ball != null) {
 			for (iGizmo a : getGizmos()) {
 				if (a instanceof Absorber) {
 					((Absorber) a).removeStoredBall(ballName);
 				}
 			}
 			setChanged();
 			notifyObservers();
 			return true;
 		}
 		return false;
 
 	}
 
 	@Override
	public boolean moveBall(String ballName, String absorberName, float x,
			float y) {
 		iBall temp = getBall(ballName);
 		if(temp == null) return false;
 		iGizmo absorb = null;
 		if (absorberName.length() > 0) {
 			absorb = getGizmo(absorberName);
 		}
 		Vect v = temp.getVelocity();
 		if (absorb != null) {
 			if (canPlaceBall(absorberName, x, y, x, y)) {
 				temp.setLocation(new BallPoint(19, 19));
 				removeFromBoard(ballName);
 				temp.setCaptured(true);
 				((Absorber) absorb).captureBall(temp, true);
 				// setPlace(ballName, (int)x, (int)y, (int)x, (int)y); //if the
 				// ball is inside the absorber, dont place on map
 				setChanged();
 				notifyObservers(ballName);
 				return true;
 			}
 		}
 		if (canPlaceBall("", x, y, x, y)) {
 			temp.setLocation(new BallPoint(x, y));
 			removeFromBoard(ballName);
 			setPlace(ballName, (int) x, (int) y, (int) x, (int) y); // we might
 																	// not want
 																	// the
 																	// "ball" in
 																	// the board
 			setChanged();
 			notifyObservers(ballName);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public double getGravity() {
 		return gravity;
 	}
 
 	@Override
 	public float getFrictionMu() {
 		return mu;
 	}
 
 	@Override
 	public float getFrictionMu2() {
 		return mu2;
 	}
 
 	@Override
 	public void moveAllGizmos(double Delta_T) {
 		for (iGizmo giz : getGizmos()) {
 			giz.move(Delta_T);
 		}
 		setChanged();
 		notifyObservers();
 
 	}
 
 	@Override
 	public ArrayList<iGizmo> getGizmoDownKeytriggers(int keyCode) {
 		ArrayList<iGizmo> tmp = new ArrayList<iGizmo>();
 		if (keyTriggersDown.get(keyCode) != null) {
 			return keyTriggersDown.get(keyCode);
 		}
 		return tmp;
 	}
 
 	@Override
 	public ArrayList<iGizmo> getGizmoUpKeytriggers(int keyCode) {
 		ArrayList<iGizmo> tmp = new ArrayList<iGizmo>();
 		if (keyTriggersUp.get(keyCode) != null) {
 			return keyTriggersUp.get(keyCode);
 		}
 		return tmp;
 	}
 
 	public void resetGame() {
 		for (iBall ball : balls.values()) {
 			ball.setLocation(ball.getOrigLocation());
 
 			ball.setVelocity(ball.getOrigVelocity());
 			ball.setCaptured(ball.getOrigCapture());
 		}
 		for (iGizmo giz : getGizmos()) {
 			if (giz instanceof Flipper) {
 				giz.setRotation(0);
 				giz.performAction(false);
 			}else if(giz instanceof Absorber){
 				((Absorber) giz).reset();
 			}
 		}
 		setChanged();
 		notifyObservers();
 	}
 
 	public String getGizName(int x, int y) {
 		if(x > 19 || y > 19) return "";
 		if (board[y][x].equals("") || board[y][x].contains("B"))
 			return "";
 
 		return board[y][x];
 	}
 
 	public String getBallName(int x, int y) {
 		for (iBall ball : balls.values()) {
 			if (ball.getLocation().getX() == (float) x
 					&& ball.getLocation().getY() == (float) y) {
 				return ball.getIdentifier();
 			}
 		}
 		return "";
 	}
 
 	public String getNextName(String name){
 		int maxNo = 0;
 		int no;
 		if(name.length() == 2){
 			for(iGizmo giz: getGizmos()){
 				if(giz.getIdentifier().contains(name)){
 					if(giz.getIdentifier().length() != 2){
 					no = Integer.parseInt(giz.getIdentifier().substring(2));
 					if(no > maxNo) maxNo = no;
 					}
 				}
 			}
 			return name + (maxNo+1);
 		}
 		if(name.contains("B")){
 			for(iBall ball: balls.values()){
 				no = Integer.parseInt(ball.getIdentifier().substring(1));
 				if(no > maxNo) maxNo = no;
 			}
 			return "B" + (maxNo+1);
 		}else{
 			
 			for(iGizmo giz: getGizmos()){
 				if(giz.getIdentifier().contains(name)){
 					if(giz.getIdentifier().length() != 1 && !giz.getIdentifier().contains("L")){
 					no = Integer.parseInt(giz.getIdentifier().substring(1));
 					if(no > maxNo) maxNo = no;
 					}
 				}
 			}
 			return name + (maxNo+1);
 		}
 	}
 
 	@Override
 	public ArrayList<String> getConnects() {
 		return connects;
 	}
 
 	@Override
 	public void setGizSelected(String giz, boolean sel) {
 		if(getGizmo(giz) != null){
 		getGizmo(giz).setSelected(sel);
 		setChanged();
 		notifyObservers();
 		}
 		
 	}
 
 	@Override
 	public boolean addPortal(String id, int x, int y, int x2, int y2) {
 		if (canPlace(id, x, y, x, y)) {
 			gizmos.put(id, new Portal(id, new GizPoint(x, y), new GizPoint(x2, y2), 1, 1, cellWidth, cellHeight));
 			setPlace(id, x, y, x, y);
 			setPlace(id, x2, y2, x2, y2);
 			if (!loadingFile) {
 				setChanged();
 				notifyObservers(id);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	
 }
