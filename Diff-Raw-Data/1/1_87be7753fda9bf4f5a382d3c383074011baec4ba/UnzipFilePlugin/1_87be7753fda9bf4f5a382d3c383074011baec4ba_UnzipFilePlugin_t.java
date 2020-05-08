 package cz.zcu.kiv.kc.plugin.unzip;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
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
 
 public class UnzipFilePlugin extends AbstractPlugin implements PropertyChangeListener
 {
 	private class DecompressionTask extends SwingWorker<Void, Void>
 	{
 		private File fileToDecompress;
 		private File destinationDirectory;
 		
 		public DecompressionTask(
 			File fileToDecompress,
 			File destinationDirectory
 		)
 		{			
 			this.fileToDecompress = fileToDecompress;
 			this.destinationDirectory = destinationDirectory;
 		}
 		
 		@Override
 		protected Void doInBackground() throws Exception
 		{
 			this.exec(
 				this.fileToDecompress,
 				this.destinationDirectory.getAbsolutePath()
 			);
 			return null;
 		}
 
 		private void exec(
 			File fileToDecompress,
 			String destinationPath)
 		{
 			byte[] buffer = new byte[1024];
 			try (ZipFile zif = new ZipFile(fileToDecompress))
 			{
 				int totalEntries = zif.size();
 				int processedEntries = 0;
 				Enumeration<? extends ZipEntry> entries = zif.entries();
 				while (entries.hasMoreElements())
 				{
 					ZipEntry entry = entries.nextElement();
 					try (InputStream is = zif.getInputStream(entry))
 					{
 						File newFile = new File(destinationPath + File.separator + entry.getName());
 						if (entry.isDirectory())
 						{ // entry is directory, create and continue with other entry
 							newFile.mkdirs();
 						}
 						else
 						{ // entry is file, create parent directories and write content
 							new File(newFile.getParent()).mkdirs();
 							this.firePropertyChange("file", null, newFile);
 							try (FileOutputStream fos = new FileOutputStream(newFile))
 							{
 								int len;
 								while ((len = is.read(buffer)) > 0)
 								{
 									fos.write(buffer, 0, len);
 								}
 							}
 							catch (FileNotFoundException ex)
 							{ // should not occur
 								System.out.println("chyba zapisu: " + ex.getMessage());
 							}
 						}
 						this.setProgress((int) ( (float) ++processedEntries / totalEntries * 100) );
 					}
 				}
 			}
 			catch (ZipException e) { }
 			catch (IOException e) { }
 			finally
 			{
 				this.firePropertyChange("done", false, true);
 			}
 		}
 	}
 
 	JDialog progressDialog = new JDialog(this.mainWindow);
 	JProgressBar pb = new JProgressBar();
 	JLabel jl = new JLabel("Status: ");
 	
 	String destinationPath;
 	
 	public UnzipFilePlugin()
 	{
 		super();
 		UIManager.put("ClassLoader", getClass().getClassLoader());
 	}
 	
 	@Override
 	public void executeAction(
 		List<File> selectedFiles,
 		String destinationPath,
 		String sourcePath)
 	{
 		this.destinationPath = destinationPath;
 		
 		for (File file : selectedFiles)
 		{
 			destinationPath = JOptionPane.showInputDialog(
 				UnzipFilePlugin.this.mainWindow,
 				"Zadejte clov adres pro dekompresi.",
 				destinationPath
 				+ (destinationPath.endsWith(File.separator) ? "" : File.separator)
 				+ file.getName().substring(0, file.getName().lastIndexOf('.'))
 			);
 			if (destinationPath == null || destinationPath.trim().isEmpty())
 			{
 				JOptionPane.showMessageDialog(
 					UnzipFilePlugin.this.mainWindow,
 					"Operace byla zruena uivatelem."
 				);
 				return;
 			}
 			
 			this.jl.setPreferredSize(new Dimension(480, -1));
 			this.pb.setStringPainted(true);
 			
 			JPanel panel = new JPanel(new GridLayout(2, 1));
 			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 			panel.add(this.jl);
 			panel.add(this.pb);
 			
 			this.progressDialog.add(panel);
 			this.progressDialog.pack();
 			this.progressDialog.setResizable(false);
 			this.progressDialog.setVisible(true);
 			
 			DecompressionTask task = new DecompressionTask(
 				file,
 				new File(destinationPath)
 			);
 			task.addPropertyChangeListener(this);
 			task.execute();
 		}	
 	}
 
 	@Override
 	public String getName() {
 		return "Unzip";
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
 			this.jl.setText(evt.getNewValue().toString() + " extracted");
 		}
 		if (evt.getPropertyName() == "progress")
 		{
 			this.pb.setValue((int) evt.getNewValue());
 		}
 		if (evt.getPropertyName() == "done")
 		{
 			System.out.println("refreshing: " + this.destinationPath);
 			this.sendEvent(this.destinationPath);			
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Operace byla dokonena.",
 				"Dokoneno.",
 				JOptionPane.INFORMATION_MESSAGE
 			);
 			this.progressDialog.dispose();
			this.sendEvent(destinationPath);
 		}
 	}
 
 }
