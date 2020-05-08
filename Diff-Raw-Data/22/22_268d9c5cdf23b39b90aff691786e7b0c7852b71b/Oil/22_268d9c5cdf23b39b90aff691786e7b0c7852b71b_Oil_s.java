 package de.ncoder.elements.element;
 
 import de.ncoder.elements.element.type.Burnable;
 import de.ncoder.elements.engine.Element;
 
 public class Oil extends Element implements Burnable{
 	private boolean burning = false;
 	
 	public Oil() {
 		setColor(110, 99, 86);
 		setGravity(1);
 		setFluidity(5);
 	}
 
 	@Override
 	public void act() {
 		if(burning) {
 			switch (random.nextInt(4)) {
 			case 0:
 				interactWith(getX() + 1, getY());
 				break;
 			case 1:
 				interactWith(getX(), getY() + 1);
 				break;
 			case 2:
 				interactWith(getX() - 1, getY());
 				break;
 			case 3:
 				interactWith(getX(), getY() - 1);
 				break;
 			}
 		}
 	}
 	
 	@Override
 	public void interactWithSpace(int x, int y) {
 		getWorld().setElementAt(x, y, new Fire());
 	}
 	
 	@Override
 	public void interactWithElement(Element e) {
 		if(e instanceof Burnable) {
 			((Burnable) e).burn(this);
 		}
 	}
 	
 	@Override
 	public void burn(Element from) {
 		burning = true;
 		setColor(120, 109, 96);
 	}
 }
