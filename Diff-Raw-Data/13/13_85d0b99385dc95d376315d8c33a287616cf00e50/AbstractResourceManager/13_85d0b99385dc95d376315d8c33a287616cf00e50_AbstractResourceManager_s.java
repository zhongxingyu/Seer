 package yang.systemdependent;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.Properties;
 
 public abstract class AbstractResourceManager {
 
 	public abstract String[] getFileList(String directory) throws IOException;
 	public abstract InputStream getAssetInputStream(String filename);
 	public abstract File getSystemFile(String filename);
 	public abstract File getExternalFile(String filename);
 
 	public boolean assetExists(String filename) {
 		return getAssetInputStream(filename)!=null;
 	}
 
 	public BufferedReader loadAssetTextFile(String filename) {
 		final InputStream stream = getAssetInputStream(filename);
 		return new BufferedReader(new InputStreamReader(stream));
 	}
 
 	public StringBuilder textFileToStringBuilder(String filename) {
 		final StringBuilder result = new StringBuilder();
 		String line;
 		try {
 			final BufferedReader reader = loadAssetTextFile(filename);
 			while((line = reader.readLine())!=null)
 				result.append(line).append('\n');
 		} catch (final IOException e) {
 			throw new RuntimeException("Error reading resource: '"+filename+"'");
 		}
 		return result;
 	}
 
 	public String textFileToString(String filename) {
 		return textFileToStringBuilder(filename).toString();
 	}
 
 	public Properties loadPropertiesFile(String filename) {
 		final Properties props = new Properties();
 		try {
 			props.load(getAssetInputStream(filename));
 		} catch (final Exception e) {
 			System.err.println("prop: '"+filename+"' not found");
 			e.printStackTrace();
 		}
 		return props;
 	}
 
 	public boolean fileExistsInFileSystem(String filename) {
 		return getSystemFile(filename).exists();
 	}
 
 	public boolean fileExistsInExternalFileSystem(String filename) {
 		return getExternalFile(filename).exists();
 	}
 
 	public InputStream getSystemInputStream(String filename) {
 		final File file = getSystemFile(filename);
 		if(!file.exists())
 			return null;
 		else
 			try {
 				return new FileInputStream(file);
 			} catch (final FileNotFoundException e) {
 				return null;
 			}
 	}
 
 	public OutputStream getSystemOutputStream(String filename) {
		final File newFile = getSystemFile(filename);
		newFile.mkdirs();
 		try {
			return new FileOutputStream(newFile);
 		} catch (final FileNotFoundException e) {
 			return null;
 		}
 	}
 
 	public OutputStream getExternalOutputStream(String filename) {
 		final File file = getExternalFile(filename);
 		final File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
 		try {
 			return new FileOutputStream(file);
 		}  catch (final FileNotFoundException e) {
 			return null;
 		}
 	}
 
 	public InputStream getExternalInputStream(String filename) {
 		final File file = getExternalFile(filename);
 		if(file==null)
 			return null;
 		else
 			try {
 				return new FileInputStream(file);
 			} catch (final FileNotFoundException e) {
 				return null;
 			}
 	}
 
 	public boolean deleteExternalFile(String filename) {
 		final File f = getExternalFile(filename);
 		return f.delete();
 	}
 
 	public boolean deleteSystemFile(String filename) {
 		final File f = getSystemFile(filename);
 		return f.delete();
 	}
 
 	public OutputStream getAssetOutputStream(String filename) {
 		throw new RuntimeException("Asset output stream not supported.");
 	}
 
 }
