 package gx.realtime;
 
 public class TestObject extends EventTarget implements Cloneable {
 
 	private int id;
 	private TestObject child;

 	public TestObject(int id){
         this.id = id;
 	}
 	
 	public TestObject(int id, TestObject child){
 		this(id);
 		this.child = child;
 	}

 	public int getId(){
 		return id;
 	}
 	
 	public TestObject getChild(){
 		return child;
 	}
 	
 	@Override
 	public boolean equals(Object object){
 		boolean result = false;
 		if(object instanceof TestObject){
 			TestObject that = (TestObject) object;
 			result = this.getId() == that.getId();
             if(this.getChild() != null){
                 result &= this.getChild().equals(that.getChild());
             } else {
                 result &= that.getChild() == null;
             }
 		}
 		return result;
 	}
 
     @Override
     public String toString(){
         return "(" + this.id + ", " + this.child + ")";
     }
 	
 	@Override
 	public TestObject clone(){
 		return new TestObject(this.getId(), this.getChild().clone());
 	}
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Override
     public void addEventListener(EventType type, EventHandler handler)
     {
         // TODO
     }
 
     @Override
     public void removeEventListener(EventType type, EventHandler handler)
     {
         // TODO
     }
 }
