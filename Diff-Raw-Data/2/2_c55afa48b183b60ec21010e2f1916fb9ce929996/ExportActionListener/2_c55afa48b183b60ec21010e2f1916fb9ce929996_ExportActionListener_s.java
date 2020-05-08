 package mapeper.minecraft.modloader.config.gui;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 
 import mapeper.minecraft.modloader.GenerateStartCommand;
 import mapeper.minecraft.modloader.config.export.Exporter;
 
 public class ExportActionListener implements ActionListener {
 
 	Exporter exporter;
 	JFileChooser fileChooser = new JFileChooser();
 	ConfigFrame configFrame;
 	public ExportActionListener(Exporter exporter, ConfigFrame parent) {
 
 		this.exporter=exporter;
 		this.configFrame=parent;
 		fileChooser.setFileFilter(new ExtensionFileFilter(exporter.getExtension()));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		int selection=fileChooser.showSaveDialog(configFrame);
 		if(selection==JFileChooser.APPROVE_OPTION)
 		{
 			FileOutputStream fos;
 			try {
 				fos = new FileOutputStream(fileChooser.getSelectedFile());
 				ArrayList<String> arr = GenerateStartCommand.fromConfiguration(configFrame.createConfiguration());
 				String[] command = new String[arr.size()];
 				arr.toArray(command);
 				
 				exporter.export(fos, command);
 				fos.close();
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 		}
 	}
 	private static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter
 	{
 		private final String extension;
 		public ExtensionFileFilter(String extension)
 		{
 			this.extension="."+extension;
 		}
 		@Override
 		public boolean accept(File pathname) {
			return pathname.toString().endsWith(extension);
 		}
 		@Override
 		public String getDescription() {
 			return extension;
 		}
 	}
 }
