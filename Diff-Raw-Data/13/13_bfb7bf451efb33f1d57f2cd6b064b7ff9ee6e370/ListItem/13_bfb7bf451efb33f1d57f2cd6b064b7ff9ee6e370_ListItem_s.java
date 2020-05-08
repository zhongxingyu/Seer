 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipFile;
 
 import com.github.junrar.Archive;
 import com.github.junrar.exception.RarException;
 import com.github.junrar.rarfile.FileHeader;
 
 
 public class ListItem implements IFile {
 	private File file;
 
 	private ZipFile zipFile;
 	private String entryName;
 	
 	private Archive rarFile;
 	private FileHeader entryHeader;
 	
 	private File svgFile = null;
 	private boolean isSelected = false;
 	private boolean enableSelect = true;
 	private Rectangle clipRect = null;
 	
 	public ListItem(File file) {
 		this.zipFile = null;
 		this.entryName = null;
 		this.rarFile = null;
 		this.entryHeader = null;
 		this.file = file;
 		if (PathUtil.isRasterFile(file)) {
 			this.isSelected = true;
 		} else if (PathUtil.isSvgFile(file)) {
 			this.enableSelect = false;
 		}
 	}
 	
 	public ListItem(File file, ZipFile zipFile, String entryName) {
 		this.zipFile = zipFile;
 		this.entryName = entryName;
 		this.rarFile = null;
 		this.entryHeader = null;
 		this.file = file;
 		if (PathUtil.isRasterFile(entryName)) {
 			this.isSelected = true;
 		} else if (PathUtil.isSvgFile(entryName)) {
 			this.isSelected = false;
 		}
 	}
 
 	public ListItem(File file, Archive rarFile, FileHeader fh) {
 		this.zipFile = null;
 		this.entryName = null;
 		this.rarFile = rarFile;
 		this.entryHeader = fh;
 		this.file = file;
 		if (PathUtil.isRasterFile(fh.getFileNameString())) {
 			this.isSelected = true;
 		} else if (PathUtil.isSvgFile(fh.getFileNameString())) {
 			this.isSelected = false;
 		}
 	}
 
 	public boolean enableSelect() {
 		return this.enableSelect;
 	}
 	
 	public boolean isSelected() {
 		return isSelected;
 	}
 	
 	public void setSelected(boolean isSelected) {
 		this.isSelected = isSelected;
 	}
 	
 	public String toString() {
 		if (zipFile != null) {
 			return entryName;
 		} else if (rarFile != null) {
 			return entryHeader.getFileNameString();
 		} else {
 			return file.getName();
 		}
 	}
 
 	public File getFile() {
 		return file;
 	}
 	
 	public InputStream getInputStream() {
 		if (zipFile != null) {
 			try {
 				ZipArchiveEntry e = zipFile.getEntry(entryName);
 				InputStream stream = zipFile.getInputStream(e);
 				return stream;
 			} catch (Exception e) {
 				return null;
 			}
 		} else if (rarFile != null) {
 			try {
				return rarFile.getInputStream(entryHeader);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 			try {
 				return new FileInputStream(file);
 			} catch (Exception e) {
 				return null;
 			}
 		}
 	}
 	
 	public File getSvgFile() {
 		return svgFile;
 	}
 	
 	public void setSvgFile(File file) {
 		this.svgFile = file;
 	}
 
 	public Rectangle getClipRect() {
 		return clipRect;
 	}
 
 	public void setClipRect(Rectangle clipRect) {
 		this.clipRect = clipRect;
 	}
 
 	public String getDirName() {
 		if (zipFile != null) {
 			String name = file.getName();
 			int index = name.lastIndexOf(".");
 			if (name.substring(index).equalsIgnoreCase(".zip")) {
 				name = name.substring(0, index);
 			}
 			return name;
 		} else if (rarFile != null) {
 			String name = file.getName();
 			int index = name.lastIndexOf(".");
 			if (name.substring(index).equalsIgnoreCase(".rar")) {
 				name = name.substring(0, index);
 			}
 			return name;
 		} else {
 			return file.getParentFile().getName();
 		}
 	}
 
 	public String getFilename() {
 		if (zipFile != null) {
 			return entryName;
 		} else if (rarFile != null) {
 			return entryHeader.getFileNameString();
 		} else {
 			return file.getName();
 		}
 	}
 	
 	public String getURI() {
 		if (zipFile != null || rarFile != null) {
 			String path = PathUtil.getTmpDirectory();
 			path += getFilename();
 			File tmpFile = new File(path);
 			if (!tmpFile.exists()) {
 				if (!tmpFile.getParentFile().exists()) {
 					tmpFile.getParentFile().mkdirs();
 					tmpFile.getParentFile().deleteOnExit();
 				}
 					
 				tmpFile.deleteOnExit();
 				InputStream in = getInputStream();
 				try {
 					ImageUtil.copyFile(in, tmpFile);
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			return tmpFile.toURI().toString();
 		} else {
 			return file.toURI().toString();
 		}
 	}
 
 }
