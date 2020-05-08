 package pruebas.Renders.helpers.ui;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Array;
 
 public class TabContainer extends Panel {
 	private Array<Panel> tabPanels;
 	private Array<ToggleButton> tabHeaders;
 	private int currentIndex;
 	private float headerW = 120;
 	private float headerH = 201;
 	private float headerSep = 6.5f;
 
 	public TabContainer(TextureRegion bg) {
 		super(bg);
 		currentIndex = 0;
 		tabHeaders = new Array<ToggleButton>();
 		tabPanels = new Array<Panel>();
 	}
 
 	public void addTab(ToggleButton header, Panel panel) {
 		fixTabSize(header, panel);
 		tabPanels.add(panel);
 		tabHeaders.add(header);
 	}
 
 	private void fixTabSize(ToggleButton header, Panel panel) {
 		panel.setW(getW());
 		panel.setH(getH());
 		panel.setX(getX());
 		panel.setY(getY());
 
 		header.setW(headerW);
 		header.setH(headerH);
 		header.setY(getY() + getH() - headerH);
 		if (tabHeaders.size > 0) {
			header.setX(getX() + tabHeaders.get(tabHeaders.size - 1).x
					+ headerW + headerSep);
 		} else {
 			header.setX(getX() + headerSep);
 			header.toggle();
 		}
 	}
 
 	public Panel getCurrentPanel() {
 		return tabPanels.get(currentIndex);
 	}
 
 	public void next() {
 		currentIndex = ++currentIndex % tabPanels.size;
 	}
 
 	public void prev() {
 		currentIndex = --currentIndex % tabPanels.size;
 	}
 
 	@Override
 	public void draw(float dt, SpriteBatch batch) {
 		super.draw(dt, batch);
 		tabPanels.get(currentIndex).draw(dt, batch);
 		for (int i = 0; i < tabHeaders.size; i++) {
 			tabHeaders.get(i).draw(dt, batch);
 		}
 	}
 
 	@Override
 	public boolean hit(float x, float y) {
 		if (super.hit(x, y)) {
 			for (int i = 0; i < tabHeaders.size; i++) {
 				if (i != currentIndex && tabHeaders.get(i).hit(x, y)) {
 					tabHeaders.get(i).toggle();
 					tabHeaders.get(currentIndex).toggle();
 					currentIndex = i;
 					return true;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 }
