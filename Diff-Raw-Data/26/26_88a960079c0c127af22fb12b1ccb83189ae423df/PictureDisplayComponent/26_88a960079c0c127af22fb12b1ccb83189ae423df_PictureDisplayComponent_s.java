 package yapto.swing;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 
 import yapto.datasource.IPicture;
 import yapto.datasource.IPictureBrowser;
 import yapto.datasource.PictureChangedEvent;
 
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Component used to display a picture.
  * 
  * @author benobiwan
  * 
  */
 public final class PictureDisplayComponent extends JComponent
 {
 	/**
 	 * serialVersionUID for Serialization.
 	 */
 	private static final long serialVersionUID = 7055585500143733186L;
 
 	/**
 	 * The picture to display on this component.
 	 */
 	private BufferedImage _img;
 
 	/**
 	 * The {@link IPictureBrowser} used to display picture on this
 	 * {@link PictureDisplayComponent}.
 	 */
 	private final IPictureBrowser<? extends IPicture> _pictureIterator;
 
 	/**
 	 * Creates a new {@link PictureDisplayComponent}.
 	 * 
 	 * @param pictureIterator
 	 *            the {@link IPictureBrowser} to use.
 	 */
 	public PictureDisplayComponent(
 			final IPictureBrowser<? extends IPicture> pictureIterator)
 	{
 		_pictureIterator = pictureIterator;
 	}
 
 	/**
 	 * Load the picture to display.
 	 * 
 	 * @throws IOException
 	 *             if an error occurs during reading.
 	 */
 	public void loadPicture() throws IOException
 	{
		_img = _pictureIterator.getCurrentPicture().getImageData();
		setPreferredSize(new Dimension(_img.getWidth(), _img.getHeight()));
		repaint();
 	}
 
 	@Override
 	public void paint(final Graphics g)
 	{
 		g.drawImage(_img, 0, 0, null);
 	}
 
 	/**
 	 * Method called when the selected picture has changed.
 	 * 
 	 * @param e
 	 *            the event signaling the change of the picture.
 	 */
 	@Subscribe
 	public void handlePictureChanged(
 			@SuppressWarnings("unused") final PictureChangedEvent e)
 	{
 		try
 		{
 			loadPicture();
 		}
 		catch (final IOException e1)
 		{
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 }
