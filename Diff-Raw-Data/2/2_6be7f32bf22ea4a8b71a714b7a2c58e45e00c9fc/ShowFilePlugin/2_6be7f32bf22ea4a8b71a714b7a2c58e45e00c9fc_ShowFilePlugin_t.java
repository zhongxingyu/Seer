 package cz.zcu.kiv.kc.plugin.show;
 
 import java.awt.Dialog.ModalityType;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 //import java.nio.file.InvalidPathException;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextPane;
 import javax.swing.UIManager;
 
 import cz.zcu.kiv.kc.interfaces.IViewPlugin;
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 
 public class ShowFilePlugin extends AbstractPlugin implements IViewPlugin {
 
 	public ShowFilePlugin()
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
 		if (selectedFiles.isEmpty())
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"No file selected.",
 				"Selection error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		File fileToShow = selectedFiles.get(0);
 		if (!fileToShow.canRead())
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Unable to read file.",
 				"Read error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		if (!fileToShow.isFile())
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Selected item is not file.",
 				"Selection error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		
 		String fileType = null;
 		
 		try
 		{
 			fileType = FileTypeDetector.probeFile(fileToShow.toPath());
 		}
 		catch (IOException ignore)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Unable to read file.",
 				"Read error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		
 		/*try { fileType = Files.probeContentType(fileToShow.toPath()); }
 		catch (InvalidPathException | IOException e)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Unable to read file.",
 				"Read error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}*/
 		
 		System.out.println(fileType);
 		if (fileType != null && fileType.startsWith("image/"))
 		{
 			this.showImage(fileToShow);
 		}
 		else if (fileType != null && fileType.startsWith("text/"))
 		{
 			this.showText(fileToShow);
 		}
 		else
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Unsupported file format.",
 				"Read error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 	}
 
 	/**
 	 * Open viewer's dialog window with content of file.
 	 * @param fileToShow
 	 * @throws IOException 
 	 */
 	private void showText(File fileToShow)
 	{
 		try
 		{
 			StringBuilder sb = new StringBuilder();
 			for (String line : Files.readAllLines(fileToShow.toPath(), StandardCharsets.UTF_8))
 			{
 				sb.append(line);
 				sb.append("\n");
 			}
			if (sb.length() > 0) sb.deleteCharAt(sb.length()-1);
 			
 			int winWidth = this.mainWindow.getWidth() - (this.mainWindow.getInsets().left + this.mainWindow.getInsets().right);
 			int winHeight = this.mainWindow.getHeight() - (this.mainWindow.getInsets().top + this.mainWindow.getInsets().bottom);
 			
 			JTextPane tp = new JTextPane();
 			tp.setEditable(false);
 			tp.setPreferredSize(new Dimension(winWidth, winHeight));
 			tp.setText(sb.toString());
 			
 			new ViewerDialog(this.mainWindow, ModalityType.MODELESS, tp);
 		}
 		catch (IOException e)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Unable to read file.",
 				"Error",
 				JOptionPane.ERROR_MESSAGE
 			);
 		}
 	}
 
 	/**
 	 * Open viewer's dialog window with image
 	 * @param fileToShow
 	 */
 	private void showImage(File fileToShow)
 	{		
 		try 
 		{
 			Image image = ImageIO.read(fileToShow);
 			if (image == null)
 			{
 				JOptionPane.showMessageDialog(
 					this.mainWindow,
 					"Unable to read file format.",
 					"Format error.",
 					JOptionPane.ERROR_MESSAGE
 				);
 				return;
 			}
 			image = this.rescaleToWindow(image);
 			new ViewerDialog(this.mainWindow, ModalityType.MODELESS, new JLabel(new ImageIcon(image)));		
 		}
 		catch (IOException e)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Unable to read file.",
 				"Read error.",
 				JOptionPane.ERROR_MESSAGE
 			);
 		}
 
 	}
 
 	/**
 	 * Creates new image instance rescaled to main window dimensions, keeps aspect ratio
 	 * @param image to scale
 	 * @return scaled image
 	 */
 	private Image rescaleToWindow(Image image)
 	{
 		int imgWidth = image.getWidth(null);
 		int imgHeight = image.getHeight(null);
 		
 		int winWidth = this.mainWindow.getWidth() - (this.mainWindow.getInsets().left + this.mainWindow.getInsets().right);
 		int winHeight = this.mainWindow.getHeight() - (this.mainWindow.getInsets().top + this.mainWindow.getInsets().bottom);
 		
 		// limit img dimensions by dimensions of main windows
 		if (imgWidth > winWidth)
 		{
 			imgWidth = winWidth;
 		}
 		if (imgHeight > winHeight)
 		{
 			imgHeight = winHeight;
 		}
 		
 		// release one dimension by img's orientation
 		if (imgWidth > imgHeight)
 		{
 			imgWidth = -1;
 		}
 		else
 		{
 			imgHeight = -1;
 		}
 		
 		image = image.getScaledInstance(imgWidth, imgHeight, java.awt.Image.SCALE_SMOOTH);
 		return image;
 	}
 
 	@Override
 	public String getName() {
 		return "Show";
 	}
 
 }
