 package de.hswt.hrm.scheme.model;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.util.Map;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.component.model.Component;
 
 /**
  * This class represents the position of a component in a scheme grid.
  * 
  * @author Michael Sieger
  *
  */
 public final class SchemeComponent {
 	
     private final int id;
     private int x;
     private int y;
     private Direction direction;
     private Component component;
     private Scheme scheme;
    Map<Attribute, String> attributes;
     
     public SchemeComponent(int id, Scheme scheme, int x, int y, Direction direction, 
     		Component component, Map<Attribute, String> attributes) {
     	
         this.id = id;
         setX(x);
         setY(y);
         setDirection(direction);
         setComponent(component);
         setScheme(scheme);
         setAttributes(attributes);
     }
     
     public SchemeComponent(Scheme scheme, int x, int y, Direction direction, Component component) {
         this(-1, scheme, x, y, direction, component, null);
     }
     
     public SchemeComponent(Scheme scheme, int x, int y, Direction direction, Component component,
             Map<Attribute, String> attributes) {
         
         this(-1, scheme, x, y, direction, component, attributes);
     }
 
     public Direction getDirection() {
 		return direction;
 	}
 
 	public void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 
 	public int getId() {
         return id;
     }
 
     public int getX() {
         checkArgument(x >= 0);
         return x;
     }
 
     public void setX(int x) {
         checkArgument(x >= 0);
         this.x = x;
     }
 
     public int getY() {
         return y;
     }
 
     public void setY(int y) {
     	checkArgument(y >= 0);
         this.y = y;
     }
     
     public Optional<Map<Attribute, String>> getAttributes() {
         return Optional.fromNullable(attributes);
     }
     
     public void setAttributes(Map<Attribute, String> attributes) {
         this.attributes = attributes;
     }
 
 	public Component getComponent() {
 		return component;
 	}
 
 	public void setComponent(Component component) {
 		checkNotNull(component);
 		this.component = component;
 	}
 	
 	public Scheme getScheme() {
 	    return scheme;
 	}
 	
 	public void setScheme(final Scheme scheme) {
 	    this.scheme = scheme;
 	}
     
 	private boolean isHorizontal(){
 		switch(getDirection()){
 		case leftRight:
 		case rightLeft:
 			return true;
 		case upDown:
 		case downUp:
 			return false;
 		default:
 			throw new RuntimeException("More than 4 directions?");
 		}
 	}
 	
 	public int getWidth(){
 		checkCategoryPresent();
 		Category c = getComponent().getCategory().get();
 		if(isHorizontal()){
 			return c.getHeight();
 		}else{
 			return c.getWidth();
 		}
 	}
 	
 	public int getHeight(){
 		checkCategoryPresent();
 		Category c = getComponent().getCategory().get();
 		if(isHorizontal()){
 			return c.getWidth();
 		}else{
 			return c.getHeight();
 		}
 	}
 	
 	private void checkCategoryPresent(){
 		if(!getComponent().getCategory().isPresent()){
 			throw new IllegalArgumentException("Component category must be present for this");
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((component == null) ? 0 : component.hashCode());
 		result = prime * result
 				+ ((direction == null) ? 0 : direction.hashCode());
 		result = prime * result + id;
 		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
 		result = prime * result + x;
 		result = prime * result + y;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SchemeComponent other = (SchemeComponent) obj;
 		if (component == null) {
 			if (other.component != null)
 				return false;
 		} else if (!component.equals(other.component))
 			return false;
 		if (direction != other.direction)
 			return false;
 		if (id != other.id)
 			return false;
 		if (scheme == null) {
 			if (other.scheme != null)
 				return false;
 		} else if (!scheme.equals(other.scheme))
 			return false;
 		if (x != other.x)
 			return false;
 		if (y != other.y)
 			return false;
 		return true;
 	}
 	
 }
