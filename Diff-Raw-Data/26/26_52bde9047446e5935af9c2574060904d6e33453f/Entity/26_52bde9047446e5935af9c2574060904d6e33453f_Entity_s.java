 package gmod.objects;
 
 import gmod.Lua;
 
 public class Entity extends Lua.Object {
 	
 	protected Entity(int index) {
 		super(index);
 	}
 	
 	public Entity() {
 		super();
 	}
 
 	public static Entity parse(int index) {
 		return new Entity(index);
 	}
 	
 	public void activate() {
 		Lua.lock();
 		{
 			Lua.getfield(index, "Activate");
 			Lua.pushvalue(index);
 			Lua.call(1, 0);
 		}
 		Lua.unlock();
 	}
 	
 	public Angle getAngle() {
 		Angle ret_val;
 		
 		Lua.lock();
 		{
 			Lua.getfield(index, "Angle");
			Lua.call(0, 1);
 			ret_val = new Angle(Lua.gettop());
 		}
 		Lua.unlock();
 		
 		return ret_val;
 	}
 	
 	public String getClassName() {
 		String ret_val;
 		
 		Lua.lock();
 		{
 			Lua.getfield(index, "GetClass");
 			Lua.pushvalue(index);
 			Lua.call(1, 1);
 			
 			ret_val = Lua.tostring(-1);
 		}
 		Lua.unlock();
 		
 		return ret_val;
 	}
 	
 	public Vector getForward() {
 		Vector ret_val;
 		
 		Lua.lock();
 		{
 			Lua.getfield(index, "GetForward");
			Lua.call(0, 1);
 			ret_val = new Vector(Lua.gettop());
 		}
 		Lua.unlock();
 		
 		return ret_val;
 	}
 	
 	public String getName() {
 		String ret_val;
 		
 		Lua.lock();
 		{
 			Lua.getfield(index, "GetName");
 			Lua.pushvalue(index);
 			Lua.call(1, 1);
 			
 			ret_val = Lua.tostring(-1);
 		}
 		Lua.unlock();
 		
 		return ret_val;
 	}
 	
 	public Vector getPos() {
 		Vector ret_val;
 		
 		Lua.lock();
 		{
 			Lua.getfield(index, "GetPos");
			Lua.call(0, 1);
 			ret_val = new Vector(Lua.gettop());
 		}
 		Lua.unlock();
 		
 		return ret_val;
 	}
 	
 	public void setPos(Vector vector) {
 		Lua.lock();
 		{
 			Lua.getfield(index, "SetPos");
 			Lua.pushvalue(vector.index());
			Lua.call(1, 0);
 		}
 		Lua.unlock();
 	}
 	
 }
