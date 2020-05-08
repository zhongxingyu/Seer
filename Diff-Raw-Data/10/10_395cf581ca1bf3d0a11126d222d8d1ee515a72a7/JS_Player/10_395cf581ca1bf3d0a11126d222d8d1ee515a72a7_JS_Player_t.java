 package mave.minecraftjs;
 
 import org.bukkit.Effect;
 import org.bukkit.entity.Player;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 public class JS_Player extends JS_LivingEntity<Player>
 {
 	private static final long serialVersionUID = -657071278805801306L;
 
 	public JS_Player()
 	{
 	}
 	
 	public void jsConstructor()
 	{
 		if (!MinecraftJS.m_bInternalConstruction)
 		{
 			throw Context.reportRuntimeError("This internal class cannot be instantiated");
 		}
 	}
 	
 	@Override
 	public String getClassName()
 	{
 		return "Player";
 	}
 	
 	public String jsGet_displayName()
 	{
 		return getDelegate().getDisplayName();
 	}
 	
 	public void jsSet_displayName(String sDisplayName)
 	{
 		getDelegate().setDisplayName(sDisplayName);
 	}
 	
 	public boolean jsGet_online()
 	{
 		return getDelegate().isOnline();
 	}
 	
 	public static void jsFunction_sendRawMessage(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		caller.getDelegate().sendRawMessage(Context.toString(args[0]));
 	}
 	
 	public static void jsFunction_kickPlayer(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		caller.getDelegate().kickPlayer(Context.toString(args[0]));
 	}
 	
 	public static void jsFunction_chat(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		caller.getDelegate().chat(Context.toString(args[0]));
 	}
 	
 	public Scriptable jsGet_compassTarget()
 	{
 		Context cx = Context.getCurrentContext();
 		Scriptable scope = ScriptableObject.getTopLevelScope(this);
 		
 		return ConvertUtility.toScriptable(getDelegate().getCompassTarget(), cx, scope);
 	}
 	
 	public void jsSet_compassTarget(Scriptable compassTarget)
 	{
 		if (!(compassTarget instanceof JS_Location))
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		JS_Location location = (JS_Location)compassTarget;
 		getDelegate().setCompassTarget(location.getDelegate());
 	}
 
 	public static boolean jsFunction_performCommand(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		return caller.getDelegate().performCommand(Context.toString(args[0]));
 	}
 	
 	public static void jsFunction_playNote(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 3)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		if (!(args[0] instanceof JS_Location))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Location location = (JS_Location)args[0];
 		
 		caller.getDelegate().playNote(location.getDelegate(), (byte)Context.toNumber(args[1]), (byte)Context.toNumber(args[2]));
 	}
 	
 	public static void jsFunction_playEffect(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 3)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		if (!(args[0] instanceof JS_Location))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Location location = (JS_Location)args[0];
 		
 		Effect effect = Effect.valueOf(Context.toString(args[0]).toUpperCase());
 		caller.getDelegate().playEffect(location.getDelegate(), effect, (int)Context.toNumber(args[2]));
 	}
 	
 	// TODO: sendChunkChange
 	
 	public boolean jsGet_sneaking()
 	{
 		return getDelegate().isSneaking();
 	}
 	
 	public void jsSet_sneaking(boolean bSneaking)
 	{
 		getDelegate().setSneaking(bSneaking);
 	}
 	
 	public void jsFunction_saveData()
 	{
 		getDelegate().saveData();
 	}
 	
 	public static void jsFunction_sendBlockChange(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 3)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		if (!(args[0] instanceof JS_Location))
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Location location = (JS_Location)args[0];
 		
 		if (args[1] instanceof JS_Material)
 		{
 			JS_Material material = (JS_Material)args[1];
 			caller.getDelegate().sendBlockChange(location.getDelegate(), material.getDelegate(), (byte)Context.toNumber(args[2]));
 			return;
 		}
 		
 		caller.getDelegate().sendBlockChange(location.getDelegate(), (int)Context.toNumber(args[1]), (byte)Context.toNumber(args[2]));
 	}
 	
 	public void jsFunction_loadData()
 	{
 		getDelegate().loadData();
 	}
 	
 	public void jsSet_sleepingIgnored(boolean bSleepingIgnored)
 	{
 		getDelegate().setSleepingIgnored(bSleepingIgnored);
 	}
 	
 	public boolean jsGet_sleepingIgnored()
 	{
 		return getDelegate().isSleepingIgnored();
 	}
 	
 	public void jsFunction_updateInventory()
 	{
 		getDelegate().updateInventory();
 	}
 	
 	public Scriptable jsGet_itemInHand()
 	{
 		Context cx = Context.getCurrentContext();
 		Scriptable scope = ScriptableObject.getTopLevelScope(this);
 		
 		return ConvertUtility.toScriptable(getDelegate().getItemInHand(), cx, scope);
 	}
 	
 	public int jsGet_sleepTicks()
 	{
 		return getDelegate().getSleepTicks();
 	}
 	
 	public boolean jsGet_sleeping()
 	{
 		return getDelegate().isSleeping();
 	}
 	
 	public static void jsFunction_sendMessage(Context cx, Scriptable thisObj, Object[] args, Function funObj)
 	{
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		JS_Player caller = (JS_Player)thisObj;
 		
 		caller.getDelegate().sendMessage(Context.toString(args[0]));
 	}
 	
 	public boolean jsGet_op()
 	{
 		return getDelegate().isOp();
 	}
 }
 
