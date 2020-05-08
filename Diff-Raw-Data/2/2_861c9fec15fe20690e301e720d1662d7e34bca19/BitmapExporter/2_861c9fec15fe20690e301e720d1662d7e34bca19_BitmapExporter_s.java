 package cytoscape.util.export;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.InternalFrameComponent;
 
 /**
  * Bitmap exporter by the ImageIO class.
  * @author Samad Lotia
  */
 public class BitmapExporter implements Exporter
 {
 	private String extension;
 	private double scale;
 
 	public BitmapExporter(String extension, double scale)
 	{
 		this.extension = extension;
 		this.scale = scale;
 
 		boolean match = false;
 		String[] formats = ImageIO.getWriterFormatNames();
 		for (int i = 0; i < formats.length; i++)
 		{
 			if (formats[i].equals(extension))
 			{
 				match = true;
 				break;
 			}
 		}
 		if (!match)
 			throw new IllegalArgumentException("Format " + extension + " is not supported by the ImageIO class");
 	}
 
 	public void export(CyNetworkView view, FileOutputStream stream) throws IOException
 	{
 		InternalFrameComponent ifc = Cytoscape.getDesktop().getNetworkViewManager().getInternalFrameComponent(view);
 		int width  = (int) (ifc.getWidth() * scale);
 		int height = (int) (ifc.getHeight() * scale);
 
 		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		g.scale(scale, scale);
		ifc.print(g);
 		g.dispose();
 		
 		ImageIO.write(image, extension, stream);
 	}
 }
