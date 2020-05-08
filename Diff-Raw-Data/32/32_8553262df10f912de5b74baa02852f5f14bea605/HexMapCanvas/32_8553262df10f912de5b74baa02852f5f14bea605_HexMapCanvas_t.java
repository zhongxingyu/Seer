 package com.lunasoft.dynasty.tools.mapeditor;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Path;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 import com.lunasoft.dynasty.tools.mapeditor.TileData.ReliefType;
 import com.lunasoft.dynasty.tools.mapeditor.TileData.VegetationType;
 import com.lunasoft.dynasty.tools.mapeditor.icons.BaseIcon;
 import com.lunasoft.dynasty.tools.mapeditor.icons.Grassland;
 
 public class HexMapCanvas extends Canvas {
 	private GameMap gameMap;
 	private HexGridDimensions hexGridDimensions;
 	private Color backgroundColor;
 	private Color cursorColor;
 	private Color grasslandColor;
 	private Color hillsColor;
 	private Color mountainsColor;
 	private Color waterColor;
 	private Image image;
 	private Point cursor;
 	private ReliefType selectedReliefType = ReliefType.WATER;
 	private VegetationType selectedVegetationType = VegetationType.NONE;
 
 	public HexMapCanvas(Composite parent, int style) {
 		super(parent, style);
 		backgroundColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
 		cursorColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
 		grasslandColor = getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		hillsColor = getDisplay().getSystemColor(SWT.COLOR_GRAY);
 		mountainsColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
 		waterColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);
 		addPaintListener(new PaintListener() {
 			@Override
 			public void paintControl(PaintEvent e) {
 				if (image == null) {
 					return;
 				}
 				e.gc.drawImage(image, 0, 0);
 				if (cursor != null) {
 					double[] center = hexGridDimensions.getCenter(cursor.x, cursor.y);
 					Path path = getHexPath(e.gc, hexGridDimensions.getHexDimensions(),
 							(float) center[0], (float) center[1]);
 					e.gc.setForeground(cursorColor);
 					e.gc.drawPath(path);
 				}
 			}
 		});
 		addMouseMoveListener(new MouseMoveListener() {
 			@Override
 			public void mouseMove(MouseEvent e) {
 				// which hex are we over?
 				if (image == null || !image.getBounds().contains(e.x, e.y)) {
 					return;
 				}
 				cursor = hexGridDimensions.getHexContaining(e.x, e.y);
 				redraw();
 			}
 		});
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(MouseEvent e) {
 				if (e.button != 1) {
 					return;
 				}
 				Point hex = hexGridDimensions.getHexContaining(e.x, e.y);
				gameMap.setTile(hex.x, hex.y, new TileData(selectedReliefType,
						selectedVegetationType));
 				image = createImage(); // a bit heavy; maybe recreate just the bit that's changing
 				redraw();
 			}
 		});
 	}
 
 	public void setGameMap(GameMap gameMap) {
 		this.hexGridDimensions = new HexGridDimensions(gameMap.getWidth(),
 				gameMap.getHeight(), HexDimensions.withInRadius(10));
 		this.gameMap = gameMap;
 		this.image = createImage();
 		redraw();
 		update();
 	}
 
 	public GameMap getGameMap() {
 		return gameMap;
 	}
 
 	public void setSelectedReliefType(ReliefType terrainType) {
 		selectedReliefType = terrainType;
 	}
 
 	public void setSelectedVegetationType(VegetationType vegetationType) {
 		selectedVegetationType = vegetationType;
 	}
 
 	private Image createImage() {
 		if (gameMap == null) {
 			return null;
 		}
 		System.out.println("createImage()");
 		int width = (int) hexGridDimensions.getTotalWidth();
 		int height = (int) hexGridDimensions.getTotalHeight();
 		Image image = new Image(getDisplay(), width, height);
 		GC gc = new GC(image);
 		gc.setBackground(backgroundColor);
 		gc.fillRectangle(image.getBounds());
 		for (int i = 0; i < gameMap.getWidth(); i++) {
 			for (int j = 0; j < gameMap.getHeight(); j++) {
 				double[] center = hexGridDimensions.getCenter(i, j);
 				TileData tileData = gameMap.getTile(i, j);
 				drawHex(gc, tileData, hexGridDimensions.getHexDimensions(),
 						(float) center[0], (float) center[1]);
 			}
 		}
 		return image;
 	}
 
 	private Color getTerrainColor(ReliefType reliefType) {
 		switch (reliefType) {
 		case WATER:
 			return waterColor;
 		case HILLS:
 			return hillsColor;
 		case MOUNTAINS:
 			return mountainsColor;
 		case PLAINS:
 			return grasslandColor;
 		}
 		throw new IllegalStateException("Unknown relief type: " + reliefType);
 	}
 
 	private BaseIcon getIcon(VegetationType vegetationType) {
 		switch (vegetationType) {
 		case NONE:
 			return null;
 		case GRASSLAND:
 			return new Grassland();
 		}
 		throw new IllegalStateException("Unknown vegetation type: " + vegetationType);
 	}
 
 	private void drawHex(GC gc, TileData tileData, HexDimensions hexDimensions,
 			float cx, float cy) {
 		Path path = getHexPath(gc, hexDimensions, cx, cy);
 		gc.setBackground(getTerrainColor(tileData.getReliefType()));
 		gc.fillPath(path);
 		BaseIcon icon = getIcon(tileData.getVegetationType());
 		if (icon != null) {
 			icon.draw(gc, cx - 0.5 * hexDimensions.getInRadius(),
 					cy - 0.5 * hexDimensions.getInRadius(), hexDimensions.getInRadius());
 		}
 	}
 
 	private Path getHexPath(GC gc, HexDimensions hexDimensions,
 			float cx, float cy) {
 		Path path = new Path(gc.getDevice());
 		float circumRadius = (float) hexDimensions.getCircumRadius();
 		float inRadius = (float) hexDimensions.getInRadius();
 		path.moveTo(cx, cy - circumRadius);
 		path.lineTo(cx + inRadius, cy - 0.5f * circumRadius);
 		path.lineTo(cx + inRadius, cy + 0.5f * circumRadius);
 		path.lineTo(cx, cy + circumRadius);
 		path.lineTo(cx - inRadius, cy + 0.5f * circumRadius);
 		path.lineTo(cx - inRadius, cy - 0.5f * circumRadius);
 		path.lineTo(cx, cy - circumRadius);
 		return path;
 	}
 
 	public void previewHex(GC gc, Rectangle bounds) {
 		TileData tileData = new TileData(selectedReliefType, selectedVegetationType,
 				null, false);
 		drawHex(gc, tileData,
 				HexDimensions.withCircumRadius(bounds.width / 2),
 				bounds.width / 2, bounds.height / 2);
 	}
 }
