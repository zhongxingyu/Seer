 package gui.tools;
 
 import robot.*;
 import field.*;
 import field.Position;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import java.util.List;
 
 
 /*
  * Custom canvas klasse om de map van het doolhof
  * weer te geven.
  */
 public class DrawCanvas extends Canvas{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private RobotPool robotPool;
 	private Field field;
 	private int tileSize;
 	private int borderWidth;
 	private int halfTileSize;
 	private int halfArrow;
 	private int boardSize;
 	private int halfBoardSize;
 	private int halfBorderWidth;
 	private double scale;
 	private int bar;
 	private int startX;
 	private int startY;
 	private int barStart;
 	private int barEnd;
 	
 	public DrawCanvas(RobotPool robotPool){
 		setRobotPool(robotPool);
 		this.setVisible(true);
 	}
 	
 	public void setRobotPool(RobotPool robotPool) {
 		if (robotPool != null) {
 			this.robotPool = robotPool;
 			setField(robotPool.getMainRobot().getField());
 		}
 	}
 
 	protected void setField(Field field) {
 		this.field = field;
 	}
 	
 	// Tekent de map van het doolhof zoals ze op dit moment bekend is.
 	public void paint(Graphics g){ 
 		if (robotPool != null) {
 			rescale();
 			paintTiles(g);
 			paintBorders(g);
 			paintPos(g);
 			paintObjects(g);
 			shortestPath(g);
 		}
 	}
 	
 	// zoekt de uiterste afmetingen van het doolhof en herschaald de map hieraan.
 	private void rescale(){
 		int maxX = 0;
 		int maxY = 0;
 		int minX = 0;
 		int minY = 0;
 		for (Tile currentTile : field.getTileMap()){
 			int x = currentTile.getPosition().getX();
 			int y = currentTile.getPosition().getY();
 			if (x > maxX){
 				maxX = x;
 			}
 			if (y > maxY){
 				maxY = y;
 			}
 			if (x < minX){
 				minX = x;
 			}
 			if (y < minY){
 				minY = y;
 			}
 		}
 		int xDiff = maxX + Math.abs(minX);
 		int yDiff = maxY + Math.abs(minY);
 		int size = Math.max(xDiff, yDiff) + 1;
 		boardSize = Math.min(getWidth(),getHeight()) - 10;
 		halfBoardSize = (int) (boardSize /2);
 		tileSize = (int) (boardSize / (size + 1));
 		borderWidth = (int) (tileSize * (.1));
 		halfTileSize = (int) (tileSize / 2);
 		halfArrow = (int) (halfTileSize / 2);
 		halfBorderWidth = (int) (borderWidth / 2);
 		scale = (double)tileSize / 40.0;
 		startX = 5 + (tileSize * (Math.abs(minX) +1));
 		startY = (boardSize - 5) - (tileSize * (Math.abs(minY) + 1));
 		bar = (int) (scale * 3.0);
 		barStart = (int) (scale * 8.0);
 		barEnd = barStart + (7 * bar);
 	}
 	
 	// Tekent de huidige posities op de map. De robots als rechthoek.
 	private void paintPos(Graphics g){
 		for (RobotModel currentRobot : robotPool){
 		//Graphics2D g2 = (Graphics2D)g;  
 		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                // RenderingHints.VALUE_ANTIALIAS_ON); 
 		int x = (int) currentRobot.getPosition().getPosX() + currentRobot.getCurrTile().getPosition().getX() * 40;
 		int y = (int) currentRobot.getPosition().getPosY() + currentRobot.getCurrTile().getPosition().getY() * 40;
 		double[] xs = currentRobot.getCornersX();
 		double[] ys = currentRobot.getCornersY();
 		int[] drawXs = new int[4];
 		int[] drawYs = new int[4];
 		for (int i = 0; i < 4; i++){
 			drawXs[i] = (int)((startX + (xs[i] * scale) + (x * scale)));
 			drawYs[i] = (int)((startY - (ys[i] * scale) - (y * scale)));
 		}
 		double r = currentRobot.getPosition().getRotationRadian() + (Math.PI/2);
 		/*System.out.println("paintpos " + x + ", " + y);
 		System.out.println("startpos " + startX + ", " + startY);
 		System.out.println("scale " + scale );*/
 		Polygon robotSurface = new Polygon(drawXs, drawYs, 4);
 		if (currentRobot == robotPool.getMainRobot()){
 			g.setColor(Color.GREEN);
 		} else {
 			g.setColor(Color.BLUE);
 		}
 		g.fillPolygon(robotSurface);
 		// robot heeft object bij.
 		if (currentRobot.hasBall()){
 			g.setColor(Color.YELLOW);
			g.fillOval((int)(startX + x * scale), (int)(startY - y * scale), borderWidth, borderWidth);
 		}
 		g.setColor(Color.BLACK);
 		g.drawLine((int) ((x * scale) + startX), (int) (startY - (y * scale)), (int) ((scale * x) + startX - (borderWidth * Math.cos(r))), (int) (startY - (scale * y) - (borderWidth * Math.sin(r))));
 		}
 		
 		//current tile
 		int x = robotPool.getMainRobot().getCurrTile().getPosition().getX();
 		int y = robotPool.getMainRobot().getCurrTile().getPosition().getY();
 		g.setColor(Color.RED);
 		g.drawRect((startX - halfTileSize)  + (x * (tileSize)),(startY - halfTileSize) - (y * (tileSize)), tileSize, tileSize);
 		
 
 		//ghost
		/*if (robotPool.getMainRobot().isSim()){
 			x = (int) robotPool.getMainRobot().getSimX();
 			y = (int) robotPool.getMainRobot().getSimY();
 			double r = robotPool.getMainRobot().getSimAngle() + (Math.PI/2);
 			g.setColor(Color.CYAN);
 			g.drawLine((int) ((x * scale) + startX), (int) (startY - (y * scale)), (int) ((scale * x) + startX - (borderWidth * Math.cos(r))), (int) (startY - (scale * y) - (borderWidth * Math.sin(r))));
 			g.fillOval((int) ((x * scale) + (startX - halfBorderWidth)), (int) ((startY - halfBorderWidth) - (y * scale)), borderWidth, borderWidth);
		}*/
 	}
 	
 	// Tekent alle bekende tegels op de map.
 	private void paintTiles(Graphics g){
 		//Graphics2D g2 = (Graphics2D)g;  
 		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
               //  RenderingHints.VALUE_ANTIALIAS_ON); 
 		synchronized(field.getTileMap()){
 		for (Tile currentTile : field.getTileMap()){
 			int x = currentTile.getPosition().getX();
 			int y = currentTile.getPosition().getY();
 			int pixelX = (startX - halfTileSize)  + (x * (tileSize));
 			int pixelY = (startY - halfTileSize) - (y * (tileSize));
 			g.setColor(Color.BLACK);
 			g.drawRect((startX - halfTileSize)  + (x * (tileSize)),(startY - halfTileSize) - (y * (tileSize)), tileSize, tileSize);
 			if (!(currentTile.getBarcode() == null)){
 				int[] code = currentTile.getBarcode().getCode();
 				g.drawString("B", startX + (x * (tileSize)), startY - (y * (tileSize)));
 				Direction dir2 = null;
 				for (Direction dir : Direction.values()){
 					try {
 						Border bord = field.getBorderInDirection(currentTile, dir);
 						if (bord instanceof WhiteBorder) {
 							dir2 = dir;
 							break;
 						}
 					} catch (IllegalArgumentException top) {
 						
 					}
 				}
 				// check ori�ntatie barcode
 				if (dir2 == Direction.TOP || dir2 == Direction.BOTTOM){
 					// platte barcode ---
 					g.setColor(Color.BLACK);
 					g.fillRect(pixelX, (int) (pixelY + (barStart)), tileSize, bar);
 					g.fillRect(pixelX, (int) (pixelY + (barEnd)), tileSize, bar);
 					for (int i = 0; i < 6; i++){
 						if (code[i] == 0){
 							g.setColor(Color.BLACK);
 						} else {
 							g.setColor(Color.WHITE);
 						}
 						g.fillRect(pixelX, pixelY + barStart + ((i + 1) * bar), tileSize, bar);
 						g.setColor(Color.BLACK);
 					}
 					
 				} else {
 					if (dir2 == Direction.LEFT || dir2 == Direction.RIGHT){
 					// rechte barcode |||
 						g.setColor(Color.BLACK);
 						g.fillRect((int) (pixelX + (barStart)), pixelY , bar, tileSize);
 						g.fillRect((int) (pixelX + (barEnd)), pixelY , bar, tileSize);
 						for (int i = 0; i < 6; i++){
 							if (code[i] == 0){
 								g.setColor(Color.BLACK);
 							} else {
 								g.setColor(Color.WHITE);
 							}
 							g.fillRect(pixelX + barStart + ((i + 1) * bar), pixelY, bar, tileSize);
 							g.setColor(Color.BLACK);
 						}
 					}
 				}
 				// vakje bevat object
 				/**if (currentTile.hasObject()){ // code = object code
 					int xBall = startX + (x * (tileSize));
 					int yBall = startY - (y * (tileSize));
 					if (true){// code = eigen object code
 						g.setColor(Color.YELLOW);
 					} else {
 						g.setColor(Color.RED);
 					}
 					switch(dir2){
 						case LEFT: xBall = xBall + halfTileSize - borderWidth ;
 						case RIGHT: xBall = xBall - halfTileSize + borderWidth;
 						case TOP: yBall = yBall + halfTileSize - borderWidth;
 						case BOTTOM: yBall = yBall - halfTileSize + borderWidth;
 					}
 					g.fillOval(xBall, yBall, borderWidth, borderWidth);
 					g.setColor(Color.BLACK);
 				}**/
 				// vakje onderdeel van wip
 				/**if (currentTile.isWip()){
 				   	int[] Xpnts = new int[6];
 				   	int[] Ypnts = new int[6];
 				   	// Alle 6 punten van de pijl in het midden van het vakje.
 				   	for (int i = 0; i < 6; i++){
 				   	 	Xpnts[i] = startX + (x * (tileSize));
 				   		Ypnts[i] = startY - (y * (tileSize));
 				   	}
 				   	// Pas de nodige punten aan afhankelijk van de richting.
 					field.Direction dir = currentTile.getDirection();
 					switch (dir) {
 						// onder boven links boven rechts boven
 						case TOP:		Ypnts[0] = Ypnts[0] - halfArrow;
 										Ypnts[1] = Ypnts[1] + halfArrow;
 										Xpnts[2] = Xpnts[2] - halfArrow;
 										Ypnts[3] = Ypnts[3] + halfArrow;
 										Xpnts[4] = Xpnts[4] + halfArrow;
 										Ypnts[5] = Ypnts[5] + halfArrow;
 										break;
 						// boven onder links onder rechts onder
 						case BOTTOM:	Ypnts[0] = Ypnts[0] + halfArrow;
 										Ypnts[1] = Ypnts[1] - halfArrow;
 										Xpnts[2] = Xpnts[2] - halfArrow;
 										Ypnts[3] = Ypnts[3] - halfArrow;
 										Xpnts[4] = Xpnts[4] + halfArrow;
 										Ypnts[5] = Ypnts[5] - halfArrow;
 										break;
 						// rechts links onder links boven links
 						case LEFT:		Xpnts[0] = Xpnts[0] + halfArrow;
 										Xpnts[1] = Xpnts[1] - halfArrow;
 										Ypnts[2] = Ypnts[2] - halfArrow;
 										Xpnts[3] = Xpnts[3] - halfArrow;
 										Ypnts[4] = Ypnts[4] + halfArrow;
 										Xpnts[5] = Xpnts[5] - halfArrow;
 										break;
 						// links rechts onder rechts boven rechts
 						case RIGHT: 	Xpnts[0] = Xpnts[0] - halfArrow;
 										Xpnts[1] = Xpnts[1] + halfArrow;
 										Ypnts[2] = Ypnts[2] - halfArrow;
 										Xpnts[3] = Xpnts[3] + halfArrow;
 										Ypnts[4] = Ypnts[4] + halfArrow;
 										Xpnts[5] = Xpnts[5] + halfArrow;
 										break;
 						}
 					// draw arrow
 					 * g.setColor(Color.BLACK);
 					g.drawPolygon(Xpnts,Ypnts,6);
 				}**/
 			}
 		}
 		}
 	}
 	
 	// Tekent alle bekende muren op de map.
 	private void paintBorders(Graphics g){
 		//Graphics2D g2 = (Graphics2D)g;  
 		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                // RenderingHints.VALUE_ANTIALIAS_ON); 
 		g.setColor(Color.WHITE);
 		for (Border currentBorder : field.getBorderMap()){
 			int x1 = currentBorder.getBorderPos().getPosition1().getX();
 			int y1 = currentBorder.getBorderPos().getPosition1().getY();
 			int x2 = currentBorder.getBorderPos().getPosition2().getX();
 			int y2 = currentBorder.getBorderPos().getPosition2().getY();
 			if (currentBorder.isPassable() && !(currentBorder instanceof UnsureBorder)){	
 				if (x1 == x2){
 					//platte border
 					if (y1 < y2){
 						g.fillRect((startX - halfTileSize) + (x1 * (tileSize)), (startY - halfTileSize - halfBorderWidth) - (y1 * (tileSize)), tileSize, borderWidth);
 					} else {
 						g.fillRect((startX - halfTileSize) + (x1 * (tileSize)), (startY - halfTileSize - halfBorderWidth) - (y2 * (tileSize)), tileSize, borderWidth);
 					}	
 				} else {
 					//rechte border
 					if (x1 < x2){
 						g.fillRect((startX + halfTileSize - halfBorderWidth) + (x1 * (tileSize)), (startY - halfTileSize) - (y1 * (tileSize)), borderWidth, tileSize);
 					} else {
 						g.fillRect((startX + halfTileSize - halfBorderWidth) + (x2 * (tileSize)), (startY - halfTileSize) - (y1 * (tileSize)), borderWidth, tileSize);
 					}
 				}
 			}
 			
 		}
 		g.setColor(Color.BLACK);
 		for (Border currentBorder : field.getBorderMap()){
 			int x1 = currentBorder.getBorderPos().getPosition1().getX();
 			int y1 = currentBorder.getBorderPos().getPosition1().getY();
 			int x2 = currentBorder.getBorderPos().getPosition2().getX();
 			int y2 = currentBorder.getBorderPos().getPosition2().getY();
 			if (!currentBorder.isPassable() && !(currentBorder instanceof UnsureBorder)){
 				if (x1 == x2){
 					//platte border
 					if (y1 < y2){
 						g.fillRect((startX - halfTileSize) + (x1 * (tileSize)), (startY - halfTileSize - halfBorderWidth) - (y1 * (tileSize)), tileSize, borderWidth);
 					} else {
 						g.fillRect((startX - halfTileSize) + (x1 * (tileSize)), (startY - halfTileSize - halfBorderWidth) - (y2 * (tileSize)), tileSize, borderWidth);
 					}	
 				} else {
 					//rechte border
 					if (x1 < x2){
 						g.fillRect((startX + halfTileSize - halfBorderWidth) + (x1 * (tileSize)), (startY - halfTileSize) - (y1 * (tileSize)), borderWidth, tileSize);
 					} else {
 						g.fillRect((startX + halfTileSize - halfBorderWidth) + (x2 * (tileSize)), (startY - halfTileSize) - (y1 * (tileSize)), borderWidth, tileSize);
 					}
 				}
 			}
 		}
 		g.setColor(Color.GRAY);
 		for (Border currentBorder : field.getBorderMap()){
 			int x1 = currentBorder.getBorderPos().getPosition1().getX();
 			int y1 = currentBorder.getBorderPos().getPosition1().getY();
 			int x2 = currentBorder.getBorderPos().getPosition2().getX();
 			int y2 = currentBorder.getBorderPos().getPosition2().getY();
 			if (currentBorder instanceof UnsureBorder){
 				if (x1 == x2){
 					//platte border
 					if (y1 < y2){
 						g.fillRect((startX - halfTileSize) + (x1 * (tileSize)), (startY - halfTileSize - halfBorderWidth) - (y1 * (tileSize)), tileSize, borderWidth);
 					} else {
 						g.fillRect((startX - halfTileSize) + (x1 * (tileSize)), (startY - halfTileSize - halfBorderWidth) - (y2 * (tileSize)), tileSize, borderWidth);
 					}	
 				} else {
 					//rechte border
 					if (x1 < x2){
 						g.fillRect((startX + halfTileSize - halfBorderWidth) + (x1 * (tileSize)), (startY - halfTileSize) - (y1 * (tileSize)), borderWidth, tileSize);
 					} else {
 						g.fillRect((startX + halfTileSize - halfBorderWidth) + (x2 * (tileSize)), (startY - halfTileSize) - (y1 * (tileSize)), borderWidth, tileSize);
 					}
 				}
 			}
 		}
 	}
 	
 	// tekent de balletjes in het doolhof
 	private void paintObjects(Graphics g){
 		ObjectMap<Position, Ball> ballMap = robotPool.getMainRobot().getField().getBallMap();
 		ObjectMap<Position, Tile> TileMap = robotPool.getMainRobot().getField().getTileMap();
 		for (Position pos : ballMap.getKeys()){
 			Tile currentTile = TileMap.getObjectAtId(pos);
 			int xBall = startX + (pos.getX() * (tileSize));
 			int yBall = startY - (pos.getY() * (tileSize));
 			Direction dir2 = null;
 			for (Direction dir : Direction.values()){
 				try {
 					Border bord = field.getBorderInDirection(currentTile, dir);
 					if (bord instanceof WhiteBorder) {
 						dir2 = dir;
 						break;
 					}
 				} catch (IllegalArgumentException top) {
 					
 				}
 			}
 			switch(dir2){
 			case LEFT: xBall = xBall + halfTileSize - borderWidth ;
 			case RIGHT: xBall = xBall - halfTileSize + borderWidth;
 			case TOP: yBall = yBall + halfTileSize - borderWidth;
 			case BOTTOM: yBall = yBall - halfTileSize + borderWidth;
 		}
 		g.setColor(Color.YELLOW);
 		g.fillOval(xBall, yBall, borderWidth, borderWidth);
 		g.setColor(Color.BLACK);
 		}
 	}
 	
 	private void shortestPath(Graphics g){
 		if (robotPool.getMainRobot().getAStarTileList() != null){
 			g.setColor(Color.CYAN);
 			List<Tile> list = robotPool.getMainRobot().getAStarTileList();
 			for (int i = 0; i < list.size()-1; i++){
 				Position pos1 = list.get(i).getPosition();
 				Position pos2 = list.get(i+1).getPosition();
 				int x1 = startX + (pos1.getX() * (tileSize));
 				int y1 = startY - (pos1.getY() * (tileSize));
 				int x2 = startX + (pos2.getX() * (tileSize));
 				int y2 = startY - (pos2.getY() * (tileSize));
 				g.drawLine(x1, y1, x2, y2);
 			}
 		}
 		g.setColor(Color.BLACK);
 	}
 	
 	@Override
 	public void update(Graphics g) {
 		super.update(g);
 	}
 }
