 package be.kuleuven.med.brainfuck.view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Ellipse2D;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 import be.kuleuven.med.brainfuck.controller.LedMatrixController;
 import be.kuleuven.med.brainfuck.domain.settings.LedPosition;
 import be.kuleuven.med.brainfuck.domain.settings.LedSettings;
 import be.kuleuven.med.brainfuck.model.LedMatrixGfxModel;
 
 import com.google.common.collect.Maps;
 
 public class LedMatrixGfxView extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private LedMatrixGfxModel ledMatrixGfxModel;
 	
 	private Map<LedPosition, Shape> shapeMap;
 
 	public LedMatrixGfxView(final LedMatrixController ledMatrixController, LedMatrixGfxModel ledMatrixGfxModel) {	
 		super(new MigLayout("nogrid", ":300:", ":450:"));
 		this.ledMatrixGfxModel = ledMatrixGfxModel;
 		addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				LedPosition ledPosition = findLedPosition(event.getX(), event.getY());
 				ledMatrixController.updateSelection(ledPosition, event.isShiftDown(), event.isControlDown());
 			}
 			
 		});
 	}
 
 	private LedPosition findLedPosition(int x, int y) {
 		for (Entry<LedPosition, Shape> shapeEntry : shapeMap.entrySet()) {
 			Shape shape = shapeEntry.getValue();
 			if (shape.contains(x, y)) {
 				return shapeEntry.getKey();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected void paintComponent(Graphics graphics) {
 		super.paintComponent(graphics);
 		Graphics g = graphics.create();
 		g.setColor(Color.WHITE);
 		g.fillRect(0,  0, getWidth(), getHeight());
 		boolean illuminated = ledMatrixGfxModel.isIlluminated();
 		for (Entry<LedPosition, Shape> shapeEntry : shapeMap.entrySet()) {
 			LedPosition ledPosition = shapeEntry.getKey();
 			Shape shape = shapeEntry.getValue();
 			LedSettings ledSettings = ledMatrixGfxModel.getLedSettings(ledPosition);
 			boolean selected = ledMatrixGfxModel.isSelected(ledSettings);
 			// set a nice color..
 			Rectangle rectangle = shape.getBounds();
 			if (selected) {
 				Color fillColor = new Color(150, 0, 0);
 				if (illuminated) {
 					fillColor = new Color(255-ledSettings.getIntensity(), 
 							255-ledSettings.getIntensity(), 255);
 				}
 				g.setColor(fillColor);
 				g.fillOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
 			} 
 			g.setColor(Color.LIGHT_GRAY);
 			g.drawOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
 			g.setColor(Color.GRAY);
 			int xPos = rectangle.x + rectangle.width / 2 - 10;
 			int yPos = rectangle.y + rectangle.height / 2;
 			g.drawString("R " + ledSettings.getRowPin(), xPos, yPos - 15);
 			g.drawString("C " + ledSettings.getColumnPin(), xPos, yPos);
 			g.drawString(Integer.toString(ledSettings.getIntensity()) + " mA", xPos - 10, yPos + 15);
 			g.drawString(Integer.toString(ledSettings.getFlickerFrequency()) + " Hz", xPos, yPos + 30);
 		}
 		g.dispose();
 	}
 	
 	public void resizeMatrix(Set<LedPosition> ledPositions, int width, int height) {
 		shapeMap = Maps.newHashMap();
 		int size = getLedDiameter(width, height);
 		for(LedPosition ledPosition : ledPositions) {
 			Ellipse2D.Double shape = new Ellipse2D.Double(getPosX(ledPosition.getX(), width), getPosY(ledPosition.getY(), height), size, size);
 			shapeMap.put(ledPosition, shape);
 		}
 	}
 	
 	private int getLedDiameter(int width, int height) {
 		return Math.min(getLedDiameterX(width), getLedDiameterY(height));
 	}
 	
 	private int getLedDiameterX(int nbRows) {
 		return (getWidth() - 20) / nbRows;
 	}
 	
 	private int getLedDiameterY(int nbColumns) {
 		return (getHeight() - 20) / nbColumns;
 	}
 	
 	private int getPosX(int x, int nbRows) {
 		return ((x + 1) * getWidth() - 10) / nbRows - getLedDiameterX(nbRows);
 	}
 	
 	private int getPosY(int y, int nbColumns) {
 		return ((y + 1) * getHeight() - 10) / nbColumns - getLedDiameterY(nbColumns);
 	}
 	
 }
