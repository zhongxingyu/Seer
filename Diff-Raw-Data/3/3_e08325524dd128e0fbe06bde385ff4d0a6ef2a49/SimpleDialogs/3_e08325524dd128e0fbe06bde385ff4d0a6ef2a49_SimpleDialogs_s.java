 package com.dorado.ui;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import com.dorado.image.ImageModel;
 import com.dorado.image.ImageModelIO;
 
 public final class SimpleDialogs {
 	private static final String MODEL_EXTENSION = "ped";
 	private static final String EXPORT_EXTENSION = "png";
 	
 	public static void showSaveImageDialog(JFrame owner, ImageModel model) throws IOException {
 		JFileChooser chooser = model.getSource() != null ? new JFileChooser(model.getSource().getParentFile()) : new JFileChooser();
 		chooser.setFileFilter(new FileNameExtensionFilter("Dorado Image File", MODEL_EXTENSION));
 		if (chooser.showSaveDialog(owner) == JFileChooser.APPROVE_OPTION) {
 			File file = chooser.getSelectedFile();
 			ImageModelIO.writeImage(file, model);
 			model.setSource(file);
 			model.setNotDirty();
 		}
 	}
 	
 	public static void showOpenImageDialog(JFrame owner) throws IOException {
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileFilter(new FileNameExtensionFilter("All accepted images", MODEL_EXTENSION, EXPORT_EXTENSION));
 		if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
 			File file = chooser.getSelectedFile();
 			ImageModel model;
 			if (file.getName().toLowerCase().endsWith(MODEL_EXTENSION)) {
 				model = ImageModelIO.readImage(file);
 			} else {
 				model = ImageModelIO.importImage(file);
 			}
 			new AppWindow(model);
 		}
 	}
 	
 	public static void showExportImageDialog(JFrame owner, ImageModel model) throws IOException {
 		JFileChooser chooser = model.getSource() != null ? new JFileChooser(model.getSource().getParentFile()) : new JFileChooser();
 		chooser.setFileFilter(new FileNameExtensionFilter("PNG Image", EXPORT_EXTENSION));
 		chooser.setDialogTitle("Export image");
 		if (chooser.showDialog(owner, "Export") == JFileChooser.APPROVE_OPTION) {
 			File file = chooser.getSelectedFile();
 			if (!file.getName().endsWith("." + EXPORT_EXTENSION)) {
 				file = new File(file.getAbsolutePath() + "." + EXPORT_EXTENSION);
				ImageModelIO.exportImage(file, model);
 			}
 		}
 	}
 }
