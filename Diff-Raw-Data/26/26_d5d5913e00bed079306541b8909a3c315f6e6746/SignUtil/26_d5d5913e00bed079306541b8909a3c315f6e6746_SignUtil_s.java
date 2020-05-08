 package mc.alk.util;
 
 import mc.alk.util.handlers.ISignHandler;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 public class SignUtil
 {
 	private static class NullSignHandler implements ISignHandler
 	{
 		@Override
 		public void sendLines(Player player, Sign sign, String[] lines)
 		{/* do nothing */
 		}
 	}
 
 	static ISignHandler handler = null;
 
 	static
 	{
 		try
 		{
 			final String pkg = Bukkit.getServer().getClass().getPackage()
 					.getName();
 			String version = pkg.substring(pkg.lastIndexOf('.') + 1);
 			final Class<?> clazz;
 			if (version.equalsIgnoreCase("craftbukkit"))
 			{
				clazz = Class
						.forName("mc.alk.joining.compat.v1_2_5.SignHandler");
 			}
 			else
 			{
				clazz = Class.forName("mc.alk.joining.compat." + version
 						+ ".SignHandler");
 			}
 			Class<?>[] args = {};
 			handler = (ISignHandler) clazz.getConstructor(args).newInstance(
 					(Object[]) args);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			handler = new NullSignHandler();
 		}
 	}
 
 	public static void sendLines(Player player, Sign sign, String lines[])
 	{
 		for (int i = 0; i < lines.length; i++)
 			if (lines[i].length() > 15)
 				lines[i] = lines[i].substring(0, 15);
 
 		handler.sendLines(player, sign, lines);
 	}
 }
