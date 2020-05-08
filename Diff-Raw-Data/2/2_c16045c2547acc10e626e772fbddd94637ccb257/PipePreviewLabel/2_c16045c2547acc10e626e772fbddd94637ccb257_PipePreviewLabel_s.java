 package pleocmd.itfc.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 import pleocmd.Log;
 import pleocmd.cfg.Configuration;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.pipe.Pipe;
 
 public class PipePreviewLabel extends JLabel {
 
 	private static final long serialVersionUID = -2427386445040802543L;
 
 	private Pipe lastPipe;
 
 	public PipePreviewLabel() {
 		setBorder(BorderFactory.createBevelBorder(1));
 		setPreferredSize(new Dimension(300, 100));
 		update((Pipe) null);
 		addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(final ComponentEvent e) {
 				update(getLastPipe());
 			}
 		});
 	}
 
 	public final void update(final File pipeConfig) {
		if (pipeConfig == null) {
 			update((Pipe) null);
 			return;
 		}
 		final Configuration config = new Configuration();
 		final Pipe pipe = new Pipe(config);
 		try {
 			config.readFromFile(pipeConfig, pipe);
 			pipe.setLastSaveFile(pipeConfig);
 			update(pipe);
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	public final void update(final Pipe pipe) {
 		lastPipe = pipe;
 		final int width = getWidth();
 		final int height = getHeight();
 		if (pipe == null || width == 0 || height == 0)
 			setIcon(null);
 		else {
 			final BoardPainter painter = new BoardPainter();
 			final BufferedImage img = new BufferedImage(width, height,
 					BufferedImage.TYPE_INT_RGB);
 			painter.setPipe(pipe, img.getGraphics(), false);
 			final Dimension pref = painter.getPreferredSize();
 			painter.setScale(Math.min((double) width / (double) pref.width,
 					(double) height / (double) pref.height));
 			painter.setBounds(pref.width, pref.height, false);
 			painter.paint(img.getGraphics(), null, null, null, null, false,
 					null, true);
 			setIcon(new ImageIcon(img));
 		}
 	}
 
 	public final Pipe getLastPipe() {
 		return lastPipe;
 	}
 
 }
