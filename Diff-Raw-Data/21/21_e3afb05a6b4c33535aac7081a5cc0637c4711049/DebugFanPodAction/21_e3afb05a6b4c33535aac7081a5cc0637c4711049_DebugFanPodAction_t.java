 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.actions;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Future;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.platform.FanPlatformSettings;
 import net.colar.netbeans.fan.project.FanProject;
 import org.netbeans.api.debugger.jpda.JPDADebugger;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.spi.java.classpath.support.ClassPathSupport;
 import org.netbeans.spi.project.ActionProvider;
 import org.openide.util.Lookup;
 
 /**
  *
  * @author tcolar
  */
 public class DebugFanPodAction extends FanAction
 {
 
 	public static final String COMMAND_DEBUG_FAN_POD = ActionProvider.COMMAND_DEBUG;
 
 	public DebugFanPodAction(FanProject project)
 	{
 		super(project);
 	}
 
 	@Override
 	public String getCommandId()
 	{
 		return COMMAND_DEBUG_FAN_POD;
 	}
 
 	@Override
 	public void invokeAction(Lookup context) throws IllegalArgumentException
 	{
 		//TODO: only build if files changed
 		Future future = buildPodAction(context);
 		Object result = null;
 		try
 		{
 			if (future != null)
 			{
 				result = future.get();
 			}
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		// if build didn't fail, then run the pod.
 		if (result != null)
 		{
 			if (((Integer) result) == 0)
 			// true = debug mode
 			{
 				runPodAction(context, true);
 			}
 
 			// start JPDA
 			FanUtilities.GENERIC_LOGGER.info("Starting JPDA");
 			String portStr = FanPlatformSettings.getInstance().get(FanPlatformSettings.PREF_DEBUG_PORT, "8000");
 			int port = new Integer(portStr).intValue();
 			try
 			{
 				// TODO: this is kinda ugly - Use JPDASupport instead ??
 				Thread.sleep(1500);
 				JPDADebugger.attach("localhost", port, new Object[0]);
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public boolean isActionEnabled(Lookup context) throws IllegalArgumentException
 	{
 		return true;
 	}
 
 	private static Object[] createServices()
 	{
 		try
 		{
 			Map<String,Object> map = new HashMap<String,Object>();
 			String sourceRoot = System.getProperty("test.dir.src");
 			URL sourceUrl = new File(sourceRoot).toURI().toURL();
			String sourceUrlStr = sourceUrl.toString() + File.separator;
 			sourceUrl = new URL(sourceUrlStr);
 			ClassPath cp = ClassPathSupport.createClassPath(new URL[]
 				{
 					sourceUrl
 				});
 			map.put("sourcepath", cp);
 			map.put("baseDir", new File(sourceRoot).getParentFile());
 			return new Object[]
 				{
 					map
 				};
 		} catch (MalformedURLException ex)
 		{
 			//System.err.println("MalformedURLException: sourceRoot = '"+sourceRoot+"'.");
 			ex.printStackTrace();
 			return new Object[]
 				{
 				};
 		}
 	}
 }
