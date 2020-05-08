 package mave.minecraftjs;
 
 import org.bukkit.block.BlockFace;
 import org.mozilla.javascript.*;
 
 public class JS_BlockFace extends ScriptableObject
 {
 	private static final long serialVersionUID = -5381627693919188543L;
 	public BlockFace blockFace = null;
 
 	public JS_BlockFace()
 	{
 	}
 	
 	public void initializeFunctionProperties()
 	{
 		defineFunctionProperties(new String[] { "toString" },
 				JS_BlockFace.class, DONTENUM);
 	}
 	
	public static void jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
 	{
 		if (MinecraftJS.m_bInternalConstruction)
 		{
 			return;
 		}
 		JS_BlockFace caller = (JS_BlockFace)ctorObj;
 		
 		if (args.length < 1)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		caller.blockFace = ConvertUtility.stringToBlockFace(Context.toString(args[0]));
 	}
 	
 	@Override
 	public String toString()
 	{
 		return blockFace.toString();
 	}
 	
 	@Override
 	public String getClassName()
 	{
 		return "BlockFace";
 	}
 	
 	public int jsGet_modX()
 	{
 		return blockFace.getModX();
 	}
 	
 	public int jsGet_modY()
 	{
 		return blockFace.getModY();
 	}
 	
 	public int jsGet_modZ()
 	{
 		return blockFace.getModZ();
 	}
 	
 	public Scriptable jsGet_oppositeFace()
 	{
 		Context cx = Context.getCurrentContext();
 		Scriptable scope = ScriptableObject.getTopLevelScope(this);
 		
 		return ConvertUtility.toScriptable(blockFace.getOppositeFace(), cx, scope);
 	}
 }
