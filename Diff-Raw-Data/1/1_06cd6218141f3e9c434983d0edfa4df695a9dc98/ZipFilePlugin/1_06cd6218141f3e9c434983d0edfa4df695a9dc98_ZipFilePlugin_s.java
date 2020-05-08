 package cz.zcu.kiv.kc.plugin.zip;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.NotDirectoryException;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.swing.BorderFactory;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.SwingWorker.StateValue;
 
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 
 public class ZipFilePlugin extends AbstractPlugin implements PropertyChangeListener
 {
 
 	public ZipFilePlugin()
 	{
 		super();
 		UIManager.put("ClassLoader", getClass().getClassLoader());
 	}
 	
 	private class CompressionTask extends SwingWorker<Void, Void>
 	{	
 		private List<File> selectedFiles;
 		private File destinationFile;
 		protected int filesCount = 0, filesProcessed = 0;
 		
 		private CompressionTask(
 			List<File> selectedFiles,
 			File destinationFile
 		)
 		{
 			this.selectedFiles = selectedFiles;
 			this.destinationFile = destinationFile;	
 			this.filesCount = selectedFiles.size();
 		}
 		
 		@Override
 		protected Void doInBackground() throws Exception
 		{
 			this.exec(
 				this.selectedFiles,
 				this.destinationFile
 			);
 			return null;
 		}
 		
 		protected void exec(
 				List<File> selectedFiles,
 				File destinationFile)
 		{
 
 			
 			this.filesCount = this.countFiles(selectedFiles);
 
 			try (FileOutputStream fout = new FileOutputStream(destinationFile))
 			{
 				try (ZipOutputStream zout = new ZipOutputStream(fout))
 				{
 					for (File file : selectedFiles)
 					{
 						if (file.isDirectory())
 						{
 			        		String newFolder = file.getName() + "/"; // '/' is defined in zip format
 			        		zout.putNextEntry(new ZipEntry(newFolder));
 							this.addDirectory(zout, file, newFolder);
 						}
 						else if (file.isFile())
 						{
 							this.addFile(zout, file, "");
 						}
 					}
 				}
 			}
 			catch (FileNotFoundException e)
 			{
 				JOptionPane.showMessageDialog(
 					ZipFilePlugin.this.mainWindow,
 					"Nepodailo se vytvoit vstupn soubor.",
 					"Chyba vstupnho souboru.",
 					JOptionPane.ERROR_MESSAGE
 				);
 			} catch (IOException e)
 			{
 				JOptionPane.showMessageDialog(
 					ZipFilePlugin.this.mainWindow,
 					"Neznm vstupn/vstupn chyba.\n" + e.getMessage(),
 					"Chyba vstupu/vstupu.",
 					JOptionPane.ERROR_MESSAGE
 				);
 			}
 			this.firePropertyChange("done", false, true);
 		}
 		
 		private int countFiles(List<File> selectedFiles2)
 		{
 			int ret = 0;
 			
 			for (File file : selectedFiles2)
 			{
 				if (file.isFile())
 				{
 					ret++;
 				}
 				else if (file.isDirectory())
 				{
 					try
 					{
 						ret += 1 + this.countFiles(file);
 					}
 					catch (NotDirectoryException ignore) {};
 				}
 			}
 			
 			return ret;
 		}
 		
 		private int countFiles(File directory) throws NotDirectoryException
 		{
 			int ret = 0;
 			
 			if (directory.isDirectory())
 			{
 				File[] content = directory.listFiles();
 				
 				for (File item : content)
 				{
 					if (item.isFile())
 					{
 						ret++;
 					}
 					else if (item.isDirectory())
 					{
 						ret += 1 + this.countFiles(item);
 					}
 				}
 			}
 			else  throw new NotDirectoryException(directory.toString());
 			
 			return ret;
 		}
 
 		protected void addDirectory(ZipOutputStream zout, File directory, String parentFolder) throws IOException
 		{
 		    //get sub-folder/files list
 		    File[] files = directory.listFiles();
 		    //System.out.println((int) (((float) 1+this.filesProcessed) / this.filesCount * 100));
 			this.setProgress((int) (((float) ++this.filesProcessed) / this.filesCount * 100));
 		    for (File file : files)
 		    {
 		    	if (file.isDirectory())
 		    	{ // the file is directory, call the function recursively
 		    		String newFolder = parentFolder + file.getName() + "/"; // '/' is defined in zip format
 		    		zout.putNextEntry(new ZipEntry(newFolder));
 		    		this.addDirectory(zout, file, newFolder);
 		    	}
 		    	else if (file.isFile())
 		    	{ // file is file (:-)), just add it
 		    		this.addFile(zout, file, parentFolder);
 		    	}
 		    }
 		}
 		
 		protected void addFile(ZipOutputStream zout, File file, String folder) {
 			try (FileInputStream fin = new FileInputStream(file))
 			{
 			    //create byte buffer
 			    byte[] buffer = new byte[1024];
 			   
 			    zout.putNextEntry(new ZipEntry(folder + file.getName()));
 			
 			    /*
 			     * After creating entry in the zip file, actually
 			     * write the file.
 			     */
 			    int length;
 			    while((length = fin.read(buffer)) > 0)
 			    {
 			       zout.write(buffer, 0, length);
 			    }
 			
 			    /*
 			     * After writing the file to ZipOutputStream, use
 			     *
 			     * void closeEntry() method of ZipOutputStream class to
 			     * close the current entry and position the stream to
 			     * write the next entry.
 			     */
 			     zout.closeEntry();
 			     
 			     //System.out.print("file adding: " + file + " ... ");
 			     
 			     //this.filesProcessed++;
 			     //this.setProgress(this.filesProcessed);
 			     //System.out.print(" progres set ");
 			     
 			     //System.out.println("done");
 			}
 			catch(IOException ioe)
 			{
 			    JOptionPane.showMessageDialog(
 			    	ZipFilePlugin.this.mainWindow,
 			    	"Neznm vstupn/vstupn chyba.",
 			    	"Error",
 			    	JOptionPane.ERROR_MESSAGE
 			    );           
 			}
 			finally
 			{
 				this.setProgress((int) (((float) ++this.filesProcessed) / this.filesCount * 100));
 		    	this.firePropertyChange("file", null, file);
 			}
 		}
 	};
 	
 	private CompressionTask worker;
 	
 	JDialog progressDialog = new JDialog(this.mainWindow);
 	JProgressBar pb = new JProgressBar();
 	JLabel jl = new JLabel("Status: ");
 	
 	String destinationPath;
 	
 	@Override
 	public void executeAction(
 		final List<File> selectedFiles,
 		final String destinationPath,
 		final String sourcePath)
 	{
 		if (selectedFiles.size() == 0)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Nebyly vybrny dn soubory.",
 				"dn soubory",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		
 		File outputFile = this.getDestinationFile(selectedFiles, destinationPath, sourcePath);
 		if (outputFile == null) return;
 		
 		this.destinationPath = destinationPath;
 		
 		this.jl.setPreferredSize(new Dimension(480, -1));
 		this.pb.setStringPainted(true);
 
 		JPanel panel = new JPanel(new GridLayout(2, 1));
 		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		panel.add(this.jl);
 		panel.add(this.pb);
 
 		this.progressDialog.add(panel);
 		this.progressDialog.pack();
 		this.progressDialog.setVisible(true);
 		this.progressDialog.setResizable(false);
 		
 		this.worker = new CompressionTask(selectedFiles, outputFile);
 		this.worker.addPropertyChangeListener(this);
 		this.worker.execute();
 	}
 	
 	@Override
 	public String getName() {
 		return "Zip";
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt)
 	{
 		if (evt.getPropertyName() == "state" && evt.getNewValue() == StateValue.STARTED)
 		{
 			this.pb.setValue(0);
 		}
 		if (evt.getPropertyName() == "file")
 		{
 			this.jl.setText(evt.getNewValue().toString() + " added");
 		}
 		if (evt.getPropertyName() == "progress")
 		{
 			this.pb.setValue((int) evt.getNewValue());
 		}
 		if (evt.getPropertyName() == "done")
 		{
 			this.sendEvent(this.destinationPath);			
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Operace byla dokonena.",
 				"Dokoneno.",
 				JOptionPane.INFORMATION_MESSAGE
 			);
 			this.progressDialog.dispose();
 		}
 	}
 
 	private File getDestinationFile(List<File> selectedFiles, String destinationPath, String sourcePath)
 	{
 		// in case when source path is root, use name of the first selected file
 		// no need for checking size od the list, because it is checked in execution 
 		String fileName = new File(sourcePath).getName();
 		if (fileName.isEmpty())
 		{
 			fileName = selectedFiles.get(0).getName(); 
 		}
 		
 		// create hint for destination filename
 		String destinationFileNameHint = destinationPath
 			+ (!destinationPath.endsWith(File.separator) ? File.separator : "")
 			+ fileName
 			+ ".zip";
 		// ask for actual destination filename
 		String destinationFileName = JOptionPane.showInputDialog(
 			this.mainWindow,
 			"Zadej jmno vstupnho souboru",
 			destinationFileNameHint
 		);
 		
 		if (destinationFileName == null)
 		{ // operation canceled
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Operace byla zruena uivatelem.",
 				"Operace zruena.",
 				JOptionPane.INFORMATION_MESSAGE
 			);
 			return null;
 		}
 		if (destinationFileName.isEmpty())
 		{ // empty name given
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Nesprvn zadn. Operace byla zruena.",
 				"Operace zruena.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return null;
 		}
 		
 		File ret = new File(destinationFileName);
 		if (ret.exists())
 		{
 			int res = JOptionPane.showConfirmDialog(
 				this.mainWindow,
 				"Soubor ji existuje. Pepsat?",
 				"Soubor ji existuje.",
 				JOptionPane.YES_NO_OPTION
 			);
 			if (res == JOptionPane.NO_OPTION)
 			{
 				return null;
 			}
 		}
 		
 		return ret;
 	}
 	
 }
