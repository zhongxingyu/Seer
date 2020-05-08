 package com.phybots.picode.action;
 
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.swing.AbstractAction;
 
 import com.phybots.picode.PicodeMain;
 import com.phybots.picode.api.Pose;
 import com.phybots.picode.api.PoseLibrary;
 import com.phybots.picode.ui.editor.Decoration;
 import com.phybots.picode.ui.editor.PicodeEditor;
 import com.phybots.picode.ui.editor.Decoration.Type;
 
 public class PublishAsHTMLAction extends AbstractAction {
 	private static final long serialVersionUID = 970920955888702503L;
 	private PicodeMain picodeMain;
 	public PublishAsHTMLAction(PicodeMain picodeMain) {
 		putValue(NAME, "Publish as a HTML page");
 		putValue(SHORT_DESCRIPTION, "Publish current sketch as a HTML page.");
 		this.picodeMain = picodeMain;
 	}
 	public void actionPerformed(ActionEvent e) {
 		File sketchDir = picodeMain.getSketch().getFolder();
 		File outputDir = new File(sketchDir, "output");
 		try {
 
 			// Create directories.
 			outputDir.mkdirs();
 			File poseDir = new File(
 					outputDir,
 					"poses");
 			poseDir.mkdirs();
 
 			// Loop for each editor.
 			Set<String> poseFileNames = new HashSet<String>();
 			for (PicodeEditor editor : picodeMain.getFrame().getEditors()) {
 
 				// Output the HTML file.
 				File file = new File(
 						outputDir,
 						editor.getCode().getFileName() + ".html");
				Writer writer = new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(file), "UTF-8"));
 				writer.append(
 						editor.getDocumentManager().getHTML("./poses/"));
 				writer.close();
 
 				// List up poses.
 				SortedSet<Decoration> decorations =
 						editor.getDocumentManager().getDecorations();
 				for (Decoration decoration : decorations) {
 					if (decoration.getType() == Type.POSE) {
 						poseFileNames.add(
 								decoration.getOption().toString());
 					}
 				}
 			}
 
 			// Copy used poses.
 			for (String poseName : poseFileNames) {
 				Pose pose = PoseLibrary.getInstance().get(poseName);
 				pose.saveInDirectory(poseDir.getAbsolutePath());
 			}
 		
 			// Save to a zip archive.
 			// http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
 			List<File> fileList = new ArrayList<File>();
 			File zipFile = new File(
 					sketchDir,
 					picodeMain.getSketch().getName() + ".zip");
 			getAllFiles(outputDir, fileList);
 			writeZipFile(outputDir, fileList, zipFile);
 	
 			// Open the directory
 			Desktop.getDesktop().open(
 					sketchDir);
 
 		} catch (IOException e1) {
 			// Do nothing.
 			e1.printStackTrace();
 		}
 	}
 
 	public static void getAllFiles(File dir, List<File> fileList) {
 		File[] files = dir.listFiles();
 		for (File file : files) {
 			fileList.add(file);
 			if (file.isDirectory()) {
 				getAllFiles(file, fileList);
 			}
 		}
 	}
 
 	public static void writeZipFile(File directoryToZip, List<File> fileList, File zipFile) {
 
 		try {
 			FileOutputStream fos = new FileOutputStream(zipFile);
 			ZipOutputStream zos = new ZipOutputStream(fos);
 
 			for (File file : fileList) {
 				if (!file.isDirectory()) {
 					addToZip(directoryToZip, file, zos);
 				}
 			}
 
 			zos.close();
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
 			IOException {
 
 		FileInputStream fis = new FileInputStream(file);
 
 		// we want the zipEntry's path to be a relative path that is relative
 		// to the directory being zipped, so chop off the rest of the path
 		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
 				file.getCanonicalPath().length());
 		ZipEntry zipEntry = new ZipEntry(zipFilePath);
 		zos.putNextEntry(zipEntry);
 
 		byte[] bytes = new byte[1024];
 		int length;
 		while ((length = fis.read(bytes)) >= 0) {
 			zos.write(bytes, 0, length);
 		}
 
 		zos.closeEntry();
 		fis.close();
 	}
 
 }
