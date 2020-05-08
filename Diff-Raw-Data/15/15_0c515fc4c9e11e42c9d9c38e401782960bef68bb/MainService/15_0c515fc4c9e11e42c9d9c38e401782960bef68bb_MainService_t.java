 package com.jack.findUpdate.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 
 import com.jack.findUpdate.dataCollect.DataCollect;
 import com.jack.findUpdate.dataCollect.DataCollectFactory;
 import com.jack.findUpdate.dto.ModifyPath;
 import com.jack.findUpdate.dto.UserData;
 import com.jack.findUpdate.util.FileUtil;
 
 public class MainService {
 	public static List<ModifyPath> getModifyPath(UserData userData)
 			throws Exception {
 		DataCollect collect = DataCollectFactory.getCollect(userData
 				.getToolType());
 		List<ModifyPath> paths = collect.findModifyPathsFromVersion(userData);
 		return paths;
 	}
 
 	public static boolean saveUpdate(UserData userData) throws Exception {
 		List<ModifyPath> paths = getModifyPath(userData);
 		if (paths == null || paths.size() == 0) {
 			return true;
 		}
 		// generdate dir
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
 		String date = sdf.format(new Date());
 		String destDir = userData.getBuildPath() + "/" + date + "/data";
 		new File(destDir).mkdirs();
 		String updateDir = destDir + "/..";
 		new File(updateDir).mkdirs();
 		// copy update file
 		String srcClassDir = null;
 		if (userData.getAppType().toLowerCase().equals("web")) {
 			srcClassDir = "WebRoot/WEB-INF/classes";
 		}
 		List<ModifyPath> deletePaths = new ArrayList<ModifyPath>();
 		for (ModifyPath path : paths) {
 			if (path.getModifyType() == ModifyPath.ModifyType.ADD
 					|| path.getModifyType() == ModifyPath.ModifyType.MODIFY) {
 				if (path.getPathType() == ModifyPath.PathType.DIR) {
 					new File(path.getPath()).mkdirs();
 				} else if (path.getPathType() == ModifyPath.PathType.FILE) {
 					if (path.getPath().endsWith(".java")) {
 						String temp = path.getPath().replace(
 								"src", srcClassDir).replaceAll(
 								".java", ".class");
 						FileUtil.copyFile(new File(temp), new File(destDir + temp.replace(userData.getProjectPath(), "")));
 					} else {
 						FileUtil
 								.copyFile(
 										new File(path.getPath()),
 										new File(destDir + path.getPath().replace(userData.getProjectPath(), "")));
 					}
 				}
 			} else if (path.getModifyType() == ModifyPath.ModifyType.REMOVE) {
 				deletePaths.add(path);
 			}
 		}
		String srcDir = destDir + "/src";
		if(new File(srcDir).exists()){			
			FileUtil.copyDirectiory(destDir + "/src", destDir + "/WebRoot/WEB-INF/classes");
			//TODO
		}
 		// save update info
 		saveUpdateInfo(paths, date, userData, updateDir);
 		return true;
 	}
 
 	private static void saveUpdateInfo(List<ModifyPath> paths, String dateStr,
 			UserData userData, String updateDir) throws IOException {
 		Properties p = new Properties();
 		p.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, MainService.class.getResource(".").getPath());
 		Velocity.init(p);
 		VelocityContext context = new VelocityContext();
 		context.put("paths", paths);
 		context.put("dateStr", dateStr);
 		context.put("userData", userData);
 		Template template = null;
 
 		try {
 			template = Velocity.getTemplate("updateinfo.vm");
 		} catch (ResourceNotFoundException rnfe) {
 			System.out.println("Example : error : cannot find template "
 					+ "update_info.vm");
 		} catch (ParseErrorException pee) {
 			System.out.println("Example : Syntax error in template "
 					+ "update_info.vm" + ":" + pee);
 		}
 
 		/*
 		 * Now have the template engine process your template using the data
 		 * placed into the context. Think of it as a 'merge' of the template and
 		 * the data to produce the output stream.
 		 */
 
 		BufferedWriter writer;
 		writer = new BufferedWriter(new FileWriter(new File(updateDir + "/update.html")));
 
 		if (template != null)
 			template.merge(context, writer);
 
 		/*
 		 * flush and cleanup
 		 */
 
 		writer.flush();
 		writer.close();
 	}
 }
