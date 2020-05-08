 package fr.frozen.iron.client.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.Color;
 
 import fr.frozen.iron.util.IronGL;
 
 public class DropList extends Component {
 
 	private static int ITEM_HEIGHT = 25;
 	
 	protected String label;
 	protected List<DropListItem> items;
 	
 	protected int itemHovered = -1;
 	protected int itemSelected = -1;
 	protected boolean editable = true;
 	protected boolean hover;
 	protected Vector2f boxSize;
 	
 	public DropList(String label, int x, int y, int w, int h) {
 		super(x,y, w, h);
 		this.label = label;
 		selected = false;
 		boxSize = new Vector2f(w, h);
 		items = new ArrayList<DropListItem>();
 	}
 	
 	public void reInit() {
 		listeners.clear();
 		items.clear();
 
 		itemHovered = -1;
 		itemSelected = -1;
 		selected = false;
 		hover = false;
 	}
 	
 	
 	public String getLabel() {
 		return label;
 	}
 	
 	public void setLabel(String val) {
 		label = val;
 	}
 	
 	public void addItem(DropListItem item) {
 		items.add(item);
 	}
 	
 	public boolean isEditable() {
 		return editable;
 	}
 	
 	public void setEditable(boolean val) {
 		editable = val;
 	}
 	
 	public void setSelectedItem(DropListItem item) {
 		for (int i = 0; i < items.size(); i++) {
 			if (items.get(i).equals(item)) {
 				itemSelected = i;
 				label = item.getLabel();
 				return;
 			}
 		}
 		itemSelected = -1;
 		label = "none";
 	}
 	
 	@Override
 	public void render(float deltaTime) {
 		if (!visible) return;
 		
 		
 		float r = 0.1607843f;
 		float g = 0.06274509f;
 		float b = 0.0078431f;
 		
 		if (!selected && hover)  {
 			r = 0.2407843f;
 			g = 0.14274509f;
 			b = 0.0878431f;
 		}
 		
 		IronGL.drawRect((int)pos.getX(),(int) pos.getY(), getWidth(), getHeight(),
 				r, g, b, 0.7f);
 		
 		float y = (float) (pos.getY() + boxSize.getY() - font.getHeight(label) - 5);//16 = font height and 5 for padding
 		
 		font.drawString(pos.getX() + 5, y, label, Color.white);
 
 		if (selected) {
 			float x = pos.getX();
 			y = (float)(pos.getY() + boxSize.getY());
 			
 			for (DropListItem item : items) {
 				renderItem(item, x , y);
 				y += ITEM_HEIGHT;
 			}
 		}
 	}
 
 	private void renderItem(DropListItem item, float x, float y) {
 		if (!visible) return;
 		
 		float r = 0.1607843f;
 		float g = 0.06274509f;
 		float b = 0.0078431f;
 		
 		if (hover && itemHovered > -1 && item.equals(items.get(itemHovered)))  {
 			r = 0.2407843f;
 			g = 0.14274509f;
 			b = 0.0878431f;
 		}
 		
 		IronGL.drawRect((int)x, (int)y, getWidth(), ITEM_HEIGHT,
 				r, g, b, 0.7f);
 
		font.drawString(pos.getX() + 5, y + ITEM_HEIGHT - font.getHeight(item.getLabel()) - 5, item.getLabel(), Color.white);
 	}
 	
 	
 	public DropListItem getSelectedItem() {
 		if (itemSelected == -1) {
 			return null;
 		}
 		return items.get(itemSelected);
 	}
 	
 	public boolean update(float deltaTime) {
 		return false;
 	}
 
 	@Override
 	public void setSelected(boolean val) {
 		super.setSelected(val);
 
 		if (val) {
 			setDim(getWidth(), getHeight() + items.size() * ITEM_HEIGHT);
 		} else {
 			setDim(boxSize);
 		}
 	}
 	
 	@Override
 	public void onExit() {
 		hover = false;
 		itemHovered = -1;
 	}
 
 	@Override
 	public void onHover(int x, int y) {
 		if (!editable) return;
 		
 		if (y - pos.getY() >= boxSize.getY()) {
 			itemHovered =  (int)(y - pos.getY() - boxSize.getY()) / ITEM_HEIGHT;
 		} else {
 			itemHovered = -1;
 		}
 		hover = true;
 	}
 
 	@Override
 	public void onLeftClick(int x, int y) {
 		if (!editable) return;
 		
 		if (!selected) {
 			setSelected(true);
 		} else {
 			if (y - pos.getY() >= boxSize.getY()) {
 				itemSelected = (int)(y - pos.getY() - boxSize.getY()) / ITEM_HEIGHT;
 				label = getSelectedItem().getLabel();
 				notifyActionListeners();
 			}
 			setSelected(false);
 			hover = false;
 		}
 	}
 
 	@Override
 	public void onRightClick(int x, int y) {
 	}
 }
