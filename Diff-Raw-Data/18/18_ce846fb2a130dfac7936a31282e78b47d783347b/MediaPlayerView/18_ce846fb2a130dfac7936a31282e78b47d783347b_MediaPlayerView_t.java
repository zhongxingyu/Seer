 /*
  * Copyright (c) 2009 David McIntosh (david.mcintosh@yahoo.com)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package nrider.ui;
 
 import com.sun.media.jmc.MediaProvider;
 import com.sun.media.jmc.control.VideoDataBuffer;
 import com.sun.media.jmc.control.VideoRenderControl;
 import com.sun.media.jmc.event.VideoRendererEvent;
 import com.sun.media.jmc.event.VideoRendererListener;
 import nrider.core.IWorkoutListener;
 import nrider.core.RideLoad;
 import nrider.core.Rider;
 import nrider.core.WorkoutSession;
 import nrider.media.IMediaEventListener;
 import nrider.media.MediaEvent;
 import nrider.ride.IRide;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.IntBuffer;
 
 /**
  * Use jmc classes from javafx to render video
  */
 public class MediaPlayerView implements IMediaEventListener, IWorkoutListener
 {
 	private final static Logger LOG = Logger.getLogger( MediaPlayerView.class );
 
 	private JFrame _window;
 	private VideoRenderControl _vrc;
 	private MediaProvider _mp;
 	private JPanel _mediaPanel;
 
 	public void launch()
 	{
 		SwingUtilities.invokeLater( new Runnable() {
 			public void run() {
 				init();
 			}
 		});
 	}
 
 	private void init()
 	{
 		_window = new JFrame();
 		_mediaPanel = new MediaPanel();
 		_mediaPanel.setLayout(new BorderLayout());
 
 		_window.add(_mediaPanel, BorderLayout.CENTER);
 
 		_window.setSize( 640, 480 );
 
 		_window.pack();
 		_window.setLocationRelativeTo(null);
 		_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		_window.setVisible( true );
 	}
 
 	public void handleMediaEvent( final MediaEvent me )
 	{
 		SwingUtilities.invokeLater( new Runnable() {
 			public void run() {
 				doHandleMediaEvent( me );
 			}
 		});
 	}
 
 	private void doHandleMediaEvent( MediaEvent me )
 	{
 		try
 		{
 			switch( me.getType() )
 			{
 				case LOAD:
 					loadURI( new URI( me.getMedia() ) );
 					break;
 				case PLAY:
 					_mp.play();
 					break;
 				case SEEK:
 					_mp.setMediaTime( me.getPosition() );
 					break;
 				case PAUSE:
 					_mp.pause();
 					break;
 			}
 		}
 		catch( URISyntaxException e )
 		{
 			LOG.error( "Unable to load", e );
 		}
 	}
 
 	public void handleLoadAdjust( String riderId, RideLoad newLoad )
 	{
 	}
 
 	public void handleAddRider( Rider rider )
 	{
 	}
 
 	public void handleRiderThresholdAdjust( String riderId, double newThreshold )
 	{
 	}
 
 	public void handleRideLoaded( IRide ride )
 	{
 	}
 
 	public void handleRideTimeUpdate( long rideTime )
 	{
 	}
 
 	public void handleAddRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
 	{
 	}
 
 	public void handleRemoveRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
 	{
 	}
 
 	public void handleRideStatusUpdate( final IRide.Status status )
 	{
 		SwingUtilities.invokeLater(
 			new Runnable()
 			{
 				public void run()
 				{
 					doHandleRideStatusUpdate( status );
 				}
 			});
 	}
 
 	private void doHandleRideStatusUpdate( IRide.Status status )
 	{
 		switch( status )
 		{
 			case RUNNING:
 				_mp.play();
 				break;
 			case STOPPED:
 			case PAUSED:
 				_mp.pause();
 				break;
 		}
 	}
 
 	private void loadURI(URI uri) {
 		if( _mp != null )
 		{
 			_mp.pause();
 		}
 
 		_mp = new MediaProvider(uri);
 
 		_vrc = _mp.getControl(VideoRenderControl.class);
 		_vrc.addVideoRendererListener(
 				new VideoRendererListener() {
 					public void videoFrameUpdated( VideoRendererEvent videorendererevent) {
 						_window.repaint();
 					}
 				});
 
 		int width = _vrc.getFrameWidth();
 		int height = _vrc.getFrameHeight();
 		_mediaPanel.setPreferredSize(new Dimension( width,
 						height) );
 		_window.pack();
 		_window.setLocationRelativeTo(null);
 	}
 
 	class MediaPanel extends JPanel
 	{
 		@Override
 		protected void paintComponent(java.awt.Graphics g) {
 			super.paintComponent( g );
 			// checkered background
 			Graphics2D graphics = (Graphics2D) g.create();
 			int w = this.getWidth();
 			int h = this.getHeight();
 
 			if ((_mp != null) && (_vrc != null)) {
 //				graphics.setComposite(AlphaComposite.SrcOver
 //						.derive(0.85f));
 				VideoDataBuffer buffer = new VideoDataBuffer( null, 0, 0, 0, VideoDataBuffer.Format.BGR );
 				_vrc.getData( buffer );
 				IntBuffer intBuffer = (IntBuffer) buffer.getBuffer();
 
                if( intBuffer != null )
                {
                    BufferedImage image = (BufferedImage) createImage( buffer.getWidth(), buffer.getHeight() );
 
                    image.setRGB( 0, 0, buffer.getWidth(), buffer.getHeight(), intBuffer.array(), 0, buffer.getWidth() );
 
                    AffineTransform transform = new AffineTransform();
                    transform.scale( 1, -1 );
                    transform.translate( 0, -buffer.getHeight() );
 
                    graphics.drawImage( image, transform, this );
                }
 
 				_vrc.releaseData( buffer );
 
 				graphics.setComposite(AlphaComposite.SrcOver);
 				graphics.setColor(Color.red.darker());
 
 				graphics.setFont(new Font("Arial", Font.BOLD, 12));
 				graphics.drawString(
 						"Curr " + format(_mp.getMediaTime()), 10, 20);
 				graphics.drawString(
 						"Total " + format(_mp.getDuration()), 10, 40);
 				graphics.drawString("Rate " + _mp.getRate(), 10, 60);
 			}
 		}
 
 		private String format(int val, int places) {
 			String result = "" + val;
 			while (result.length() < places)
 				result = "0" + result;
 			return result;
 		}
 
 		private String format(double val) {
 			int minutes = (int) (val / 60);
 			int seconds = (int) val % 60;
 			int milli = (int) (val * 1000) % 1000;
 
 			return format(minutes, 2) + ":" + format(seconds, 2) + "."
 					+ format(milli, 3);
 		}
 	};
 }
