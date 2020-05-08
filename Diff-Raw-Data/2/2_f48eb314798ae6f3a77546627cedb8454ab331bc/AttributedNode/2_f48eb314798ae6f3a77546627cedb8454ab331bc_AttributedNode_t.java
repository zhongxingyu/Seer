 package a2_p01_JS_MJ;
 
 
 public class AttributedNode<E extends  Comparable<E>> {
     private E value;
     double attribute;
     public  AttributedNode(E value, double attribute)
     {
         this.value = value;
         this.attribute = attribute;
     }
 
     public double getAttribute()
     {
         return attribute;
     }
 
     public E getValue()
     {
         return  value;
     }
 
     @Override
     public boolean equals(Object o)
     {
         if (o==null) return false;
         if (!(o instanceof AttributedNode)) return false;
         AttributedNode other = (AttributedNode)o;
        if (other.getValue().equals(this.getValue()))
             return true;
         return false;
     }
 
 	@Override
 	public String toString() {
 		return value +"("+attribute+")";
 	}
 
     @Override
 	public int hashCode() {
     	return value.hashCode();
 	}
 	
 	
 }
