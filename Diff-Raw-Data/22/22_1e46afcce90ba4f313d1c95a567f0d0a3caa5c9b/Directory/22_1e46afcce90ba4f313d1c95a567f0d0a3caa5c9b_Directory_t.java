 package fmv;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.swing.DefaultListModel;
 
 public class Directory {
 
 	/**
 	 * Name of the directory.
 	 */
 	private String dir = null;
 
 	/**
 	 * The model for the list of zip files in the directory.
 	 */
 	@SuppressWarnings("rawtypes")
 	private DefaultListModel listModel = null;
 
 	private Properties properties = null;
 
 	/**
 	 * Construct a directory given the name.
 	 * 
 	 * @param dir
 	 *            name of the directory
 	 */
 	@SuppressWarnings("rawtypes")
 	public Directory(String dir) {
 		this.dir = dir;
 		properties = new Properties();
 		File f = new File(dir + File.separator + "fmv.xml");
 		if (f.exists()) {
 			try {
 				properties.loadFromXML(new FileInputStream(f));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		listModel = new DefaultListModel();
 	}
 
 	public String getXArchiveProperty(Archive archive, String key, String defualt) {
 		if (key == null) {
 			return properties.getProperty(archive.toString(), defualt);
 		} else {
 			return properties.getProperty(archive.toString() + "." + key, defualt);
 		}
 	}
 
 	public void setXArchiveProperty(Archive archive, String key, String value) {
 		if (key == null) {
 			properties.setProperty(archive.toString(), value);
 		} else {
 			properties.setProperty(archive.toString() + "." + key, value);
 		}
 	}
 
 	public String getXVersionProperty(Archive archive, Source source, Date date, String key, String defualt) {
 		String prefix = archive.toString() + "." + source.getPath() + "." + date.getTime() + "." + key;
 		return properties.getProperty(prefix, defualt);
 	}
 
 	public void setXVersionProperty(Archive archive, Source source, Date date, String key, String value) {
 		String prefix = archive.toString() + "." + source.getPath() + "." + date.getTime() + "." + key;
 		properties.setProperty(prefix, value);
 	}
 
 	public String getXDirectoryProperty(String key, String defualt) {
 		return properties.getProperty(key, defualt);
 	}
 
 	public void setXDirectoryProperty(String key, String value) {
 		properties.setProperty(key, value);
 	}
 
 	public void saveXProperties() {
 		File f = new File(dir + File.separator + "fmv.xml");
 		try {
			for(Object value : properties.values()){
				FMV.log(Directory.class.getName(), value.toString());
			}
 			properties.storeToXML(new FileOutputStream(f), "FMV properties (" + new Date() + ")");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Add a new archive to this directory. When the directory is opened, the
 	 * main application will enumerate its contents and call this routine for
 	 * each of the entries.
 	 * 
 	 * @param name
 	 *            name of the next entry in the directory
 	 */
 	@SuppressWarnings("unchecked")
 	public void addArchive(String name) {
 		listModel.addElement(new Archive(dir, name));
 	}
 
 	/**
 	 * Returns the model for the directory contents.
 	 * 
 	 * @return list of directory entries
 	 */
 	@SuppressWarnings("rawtypes")
 	public DefaultListModel getModel() {
 		return listModel;
 	}
 
 	/**
 	 * Returns the model for the i-th zip file entry in the directory.
 	 * 
 	 * @param index
 	 *            index of the entry in the directory
 	 * @return list of files in the index-th entry in directory
 	 */
 	@SuppressWarnings("rawtypes")
 	public DefaultListModel getModel(int index) {
 		return ((Archive) listModel.getElementAt(index)).getModel(this);
 	}
 
 	public void setDiff(int i, int j) {
 		Archive a = (Archive) listModel.getElementAt(i);
 		a.setDiff(j);
 	}
 
 	public Archive getArchive(int index) {
 		return (Archive) listModel.getElementAt(index);
 	}
 
 }
