 /*
  * Thibaut Colar Aug 24, 2009
  */
 package net.colar.netbeans.fan.debugger;
 
 import java.net.URI;
 import java.net.URL;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.project.FanProject;
 import org.netbeans.api.debugger.jpda.LineBreakpoint;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.filesystems.URLMapper;
 
 /**
  * Helper utilities for debugger / breapoints manager.
  * @author thibautc
  */
 public class FanDebugHelper
 {
 
 	/**
 	 * Create a fan Breakpoint
 	 * Fan directory structure does not matches the java packages created by Fan
 	 * So I had to be a little creative with pathing.
 	 * @param url
 	 * @param lineNb
 	 * @return
 	 */
 	public static LineBreakpoint createFanBp(String url, int lineNb)
 	{
 		FanUtilities.GENERIC_LOGGER.debug("bp url: " + url);
 		URI uri=null;
 		try
 		{
 			uri = new URI(url);
 		}catch(Exception e){throw new RuntimeException("Create breakpoint null param ",e);}
 		Project prj = FileOwnerQuery.getOwner(uri);
 
 		LineBreakpoint bp = LineBreakpoint.create(url, lineNb);
 		if (prj != null && prj instanceof FanProject)
 		{
 			String name = getName(url);
 			String path = getPath(prj.getProjectDirectory(), url);
 			String pod = getPod(prj);
 			String filter = getClassFilter(pod, name);
 
 			bp.setStratum("Fan");
 			bp.setHidden(false);
 			bp.setSourceName(name);
			bp.setPrintText("[" + pod + "] " + "/" + path);
 			/*
 			 * SourcePath is required to match path in jar (LineBreakPointImpl check this)
 			 * so we have no choice but to give that path rather than the 'real' path
 			 * Our custom FanSourcePathProvider takes care of finding the right source file
 			 * given this 'jar' path.
 			 * The binary path is fan.{podname}.{typename}
 			 */
			bp.setSourcePath("fan/"+pod + "/" + name);
 			bp.setPreferredClassName(filter);
 			bp.setSuspend(LineBreakpoint.SUSPEND_ALL);
 			FanUtilities.GENERIC_LOGGER.debug("bp class:" + bp.getPreferredClassName());
 			FanUtilities.GENERIC_LOGGER.debug("bp sourceName:" + bp.getSourceName());
 			FanUtilities.GENERIC_LOGGER.debug("bp lineNb:" + bp.getLineNumber());
 			FanUtilities.GENERIC_LOGGER.debug("bp cond:" + bp.getCondition());
 			FanUtilities.GENERIC_LOGGER.debug("bp printText:" + bp.getPrintText());
 			FanUtilities.GENERIC_LOGGER.debug("bp groupName:" + bp.getGroupName());
 			FanUtilities.GENERIC_LOGGER.debug("bp vMessage:" + bp.getValidityMessage());
 			FanUtilities.GENERIC_LOGGER.debug("bp sourcePath:" + bp.getSourcePath());
 			FanUtilities.GENERIC_LOGGER.debug("bp url:" + bp.getURL());
 		}
 		return bp;
 	}
 
 	/**
 	 * The file name alone: example: Main.fan
 	 * @param url
 	 * @return
 	 */
 	private static String getName(String url)
 	{
 		FileObject fo = getUrlFo(url);
 		if (fo != null)
 		{
 			return fo.getNameExt();
 		}
 		return (url == null) ? null : url.toString();
 	}
 
 	/**
 	 * The path without the 'fan' folder
 	 * ex: Main.fan  or mydir/Main.fan
 	 * @param url
 	 * @return
 	 */
 	private static String getPath(FileObject prjDir, String url)
 	{
 		FileObject fo = getUrlFo(url);
 		String relativePath = url;
 		if (fo != null)
 		{
 			FileObject srcFolder = prjDir;
 			if (srcFolder != null)
 			{
 				prjDir = srcFolder;
 			}
 			relativePath = FileUtil.getRelativePath(prjDir, fo);
 		}
 		return relativePath;
 	}
 
 	/**
 	 * The pod name for the url.
 	 * @param url
 	 * @return
 	 */
 	private static String getPod(Project prj)
 	{
 		String pod = prj.getProjectDirectory().getName();
 		FanUtilities.GENERIC_LOGGER.debug("~~~ Pod: " + pod);
 		return pod;
 	}
 
 	/**
 	 * Breakpoint class filter
 	 * ex: fan.Debug.Main*
 	 * @param pod
 	 * @param path
 	 * @return
 	 */
 	private static String getClassFilter(String pod, String name)
 	{
 		if (name.endsWith(".fan") || name.endsWith(".fwt"))
 		{
 			name = name.substring(0, name.lastIndexOf("."));
 		}
 		return "fan." + pod + "." + name + "*";
 	}
 
 	private static FileObject getUrlFo(String url)
 	{
 		FileObject fo = null;
 		try
 		{
 			fo = URLMapper.findFileObject(new URL(url));
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return fo;
 	}
 }
