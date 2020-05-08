 package swidget.dialogues.fileio;
 
 
 import java.awt.Window;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.jnlp.FileContents;
 import javax.jnlp.FileOpenService;
 import javax.jnlp.FileSaveService;
 import javax.jnlp.ServiceManager;
 import javax.jnlp.UnavailableServiceException;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import commonenvironment.AbstractFile;
 import commonenvironment.Env;
 
 import fava.functionable.FList;
 import fava.signatures.FnMap;
 
 import swidget.icons.IconSize;
 import swidget.icons.StockIcon;
 
 
 
 public class SwidgetIO
 {
 
 	public static List<AbstractFile> openFiles(Window parent, String title, String[] exts, String extDesc,
 			String startDir)
 	{
 
 		if (Env.isWebStart())
 		{
 
 			try
 			{
 				return wsOpenFiles("~/", exts);
 			}
 			catch (UnavailableServiceException e)
 			{
 				JOptionPane.showMessageDialog(
 						parent,
 						"The Web Start File-Read Service is not Available.",
 						"Read Failed.",
 						JOptionPane.ERROR_MESSAGE,
 						StockIcon.BADGE_WARNING.toImageIcon(IconSize.ICON));
 				return null;
 			}
 
 		}
 		else
 		{
 
 			JFileChooser chooser = new JFileChooser(startDir);
 			chooser.setMultiSelectionEnabled(true);
 			chooser.setDialogTitle(title);
 
 			SimpleFileFilter filter = new SimpleFileFilter();
 			for (String ext : exts)
 			{
 				filter.addExtension(ext);
 			}
 			filter.setDescription(extDesc);
 			chooser.setFileFilter(filter);
 
 			int returnVal = chooser.showOpenDialog(parent);
 			if (returnVal == JFileChooser.APPROVE_OPTION)
 			{
 				return new FList<File>(chooser.getSelectedFiles()).map(new FnMap<File, AbstractFile>() {
 
 					public AbstractFile f(File f)
 					{
 						return new AbstractFile(f.toString());
 					}
 				});
 
 			}
 			else
 			{
 				return null;
 			}
 
 		}
 
 	}
 
 
 
 	public static AbstractFile openFile(Window parent, String title, String[] exts, String extDesc, String startDir)
 	{
 
 		if (Env.isWebStart())
 		{
 
 			try
 			{
 				return wsOpenFile("~/", exts);
 			}
 			catch (UnavailableServiceException e)
 			{
 				JOptionPane.showMessageDialog(
 						parent,
 						"The Web Start File-Read Service is not Available.",
 						"Read Failed.",
 						JOptionPane.ERROR_MESSAGE,
 						StockIcon.BADGE_WARNING.toImageIcon(IconSize.ICON));
 				return null;
 			}
 
 		}
 		else
 		{
 
 			JFileChooser chooser = new JFileChooser(startDir);
 			chooser.setMultiSelectionEnabled(true);
 			chooser.setDialogTitle(title);
			
			if (exts.length > 0) 
 			{
				SimpleFileFilter filter = new SimpleFileFilter();
				for (String ext : exts)
				{
					filter.addExtension(ext);
				}
				filter.setDescription(extDesc);
				chooser.setFileFilter(filter);
 			}
 
 			int returnVal = chooser.showOpenDialog(parent);
 			if (returnVal == JFileChooser.APPROVE_OPTION)
 			{
 				return new AbstractFile(chooser.getSelectedFile().toString());
 			}
 			else
 			{
 				return null;
 			}
 
 		}
 
 
 	}
 
 
 	public static ByteArrayOutputStream getSaveFileBuffer()
 	{
 		return new ByteArrayOutputStream();
 
 
 	}
 
 
 	public static String saveFile(Window parent, String title, String ext, String extDesc, String startDir,
 			ByteArrayOutputStream outStream) throws IOException
 	{
 
 		outStream.close();
 
 		ByteArrayInputStream bais = new ByteArrayInputStream(outStream.toByteArray());
 
 
 
 		if (Env.isWebStart())
 		{
 
 			try
 			{
 				wsSaveFile("~/", "", new String[] { ext }, bais);
 			}
 			catch (UnavailableServiceException e)
 			{
 				JOptionPane.showMessageDialog(
 						parent,
 						"The Web Start File-Write Service is not Available.",
 						"Write Failed.",
 						JOptionPane.ERROR_MESSAGE,
 						StockIcon.BADGE_WARNING.toImageIcon(IconSize.ICON));
 				return null;
 			}
 			return "";
 
 		}
 		else
 		{
 
 			String saveFilename = SimpleIODialogues.chooseFileSave(parent, title, startDir, ext, extDesc);
 
 			if (saveFilename != null)
 			{
 				File saveFile = new File(saveFilename);
 				String savePictureFolder = saveFile.getParent();
 				FileOutputStream fos = new FileOutputStream(saveFile);
 				fos.write(outStream.toByteArray());
 				fos.flush();
 				fos.close();
 
 				return savePictureFolder;
 			}
 
 			return "";
 
 		}
 
 
 	}
 	
 	
 	
 	
 	private static List<AbstractFile> wsOpenFiles(String path, String[] extensions) throws UnavailableServiceException
 	{
 		FileOpenService fos;
 		try
 		{
 			fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
 			
 			return new FList<FileContents>(fos.openMultiFileDialog(path, extensions)).map(new FnMap<FileContents, AbstractFile>(){
 
 				
 				public AbstractFile f(FileContents element)
 				{
 					return new AbstractFile(element);
 				}});
 			
 		}
 		catch (IOException e)
 		{
 			return null;
 		}
 	
 	}
 	
 	private static AbstractFile wsOpenFile(String path, String[] extensions) throws UnavailableServiceException
 	{
 		FileOpenService fos;
 		try
 		{
 			fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
 			
 			return new AbstractFile(  fos.openFileDialog(path, extensions)  );			
 		}
 		catch (IOException e)
 		{
 			return null;
 		}
 	
 	}
 	
 	private static FileContents wsSaveFile(String path, String name, String[] extensions, InputStream inputStream) throws UnavailableServiceException
 	{
 		FileSaveService fos;
 
 		try
 		{
 			
 			fos = (FileSaveService) ServiceManager.lookup("javax.jnlp.FileSaveService");
 			
 			return fos.saveFileDialog(  path, extensions, inputStream, name  );
 			
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 
 	}
 
 }
