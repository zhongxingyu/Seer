 package mave.minecraftjs;
 
 import org.bukkit.util.Vector;
 import org.mozilla.javascript.*;
 
 public class JS_Vector extends ScriptableObject
 {
 	private static final long serialVersionUID = -2515348271765295694L;
 	public Vector vector = null;
 
 	public JS_Vector()
 	{
 	}
 	
 	public void initializeFunctionProperties()
 	{
 		defineFunctionProperties(new String[] { "toString" },
 				JS_Vector.class, DONTENUM);
 	}
 	
	public void jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExp)
 	{
 		if (MinecraftJS.m_bInternalConstruction)
 		{
 			return;
 		}
 		
 		JS_Vector caller = (JS_Vector)ctorObj;
 		
 		if (args.length < 3)
 		{
 			caller.vector = new Vector();
 		}
 		else
 		{
 			caller.vector = new Vector(Context.toNumber(args[0]), Context.toNumber(args[1]), Context.toNumber(args[2]));
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		return vector.toString();
 	}
 	
 	@Override
 	public String getClassName()
 	{
 		return "Vector";
 	}
 	
 	public double jsGet_x()
 	{
 		return vector.getX();
 	}
 	
 	public double jsGet_y()
 	{
 		return vector.getY();
 	}
 	
 	public double jsGet_z()
 	{
 		return vector.getZ();
 	}
 
 	public double jsGet_length()
 	{
 		return vector.length();
 	}
 	
 	public double jsGet_lengthSquared()
 	{
 		return vector.lengthSquared();
 	}
 	
 	public int jsGet_blockX()
 	{
 		return vector.getBlockX();
 	}
 	
 	public int jsGet_blockY()
 	{
 		return vector.getBlockY();
 	}
 	
 	public int jsGet_blockZ()
 	{
 		return vector.getBlockZ();
 	}
 	
 	public void jsSet_x(double x)
 	{
 		vector.setX(x);
 	}
 	
 	public void jsSet_y(double y)
 	{
 		vector.setY(y);
 	}
 	
 	public void jsSet_z(double z)
 	{
 		vector.setZ(z);
 	}
 	
 	public static Scriptable jsFunction_add(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.add(otherVector.vector), cx, thisObj);
 	}
 	
 	public static Scriptable jsFunction_subtract(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.subtract(otherVector.vector), cx, thisObj);
 	}
 	
 	public static Scriptable jsFunction_multiply(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			return ConvertUtility.toScriptable(caller.vector.multiply(Context.toNumber(args[0])), cx, thisObj);
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.multiply(otherVector.vector), cx, thisObj);
 	}
 	
 	public static Scriptable jsFunction_divide(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.divide(otherVector.vector), cx, thisObj);
 	}
 	
 	public static float jsFunction_angle(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return caller.vector.angle(otherVector.vector);
 	}
 	
 	public Scriptable jsFunction_clone()
 	{
 		Context cx = Context.getCurrentContext();
 		Scriptable scope = ScriptableObject.getTopLevelScope(this);
 		
 		return ConvertUtility.toScriptable(vector.clone(), cx, scope);
 	}
 	
 	public static Scriptable jsFunction_copy(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.copy(otherVector.vector), cx, thisObj);
 	}
 	
 	public static Scriptable jsFunction_crossProduct(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.crossProduct(otherVector.vector), cx, thisObj);
 	}
 	
 	public static double jsFunction_distance(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return caller.vector.distance(otherVector.vector);
 	}
 	
 	public static double jsFunction_distanceSquared(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return caller.vector.distanceSquared(otherVector.vector);
 	}
 	
 	public static double jsFunction_dot(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return caller.vector.dot(otherVector.vector);
 	}
 	
 	public static Scriptable jsFunction_midpoint(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return ConvertUtility.toScriptable(caller.vector.getMidpoint(otherVector.vector), cx, thisObj);
 	}
 	
 	public static boolean jsFunction_isInAABB(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 2)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector) || !(args[1] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		JS_Vector otherVector2 = (JS_Vector)args[1];
 		
 		return caller.vector.isInAABB(otherVector.vector, otherVector2.vector);
 	}
 	
 	public static boolean jsFunction_isInSphere(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 2)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_Vector))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector otherVector = (JS_Vector)args[0];
 		
 		return caller.vector.isInSphere(otherVector.vector, Context.toNumber(args[1]));
 	}
 	
 	public Scriptable jsFunction_normalize()
 	{
 		Context cx = Context.getCurrentContext();
 		Scriptable scope = ScriptableObject.getTopLevelScope(this);
 		
 		return ConvertUtility.toScriptable(vector.normalize(), cx, scope);
 	}
 	
 	public static Scriptable jsFunction_toLocation(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Vector caller = (JS_Vector)thisObj;
 		
 		if (!(args[0] instanceof JS_World))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_World world = (JS_World)args[0];
 		
 		if (args.length < 3)
 		{
 			return ConvertUtility.toScriptable(caller.vector.toLocation(world.world), cx, thisObj);
 		}
 		
 		return ConvertUtility.toScriptable(caller.vector.toLocation(world.world, (float)Context.toNumber(args[1]), (float)Context.toNumber(args[2])), cx, thisObj);
 	}
 	
 	public Scriptable jsFunction_zero()
 	{
 		Context cx = Context.getCurrentContext();
 		Scriptable scope = ScriptableObject.getTopLevelScope(this);
 		
 		return ConvertUtility.toScriptable(vector.zero(), cx, scope);
 	}
 }
