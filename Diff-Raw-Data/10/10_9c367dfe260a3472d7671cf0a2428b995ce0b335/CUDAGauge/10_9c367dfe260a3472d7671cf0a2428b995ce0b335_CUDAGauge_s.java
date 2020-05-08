 import java.io.File;
 import java.util.Timer;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 
 
 
 public class CUDAGauge extends Canvas {
 	private Image gauge_img;
 	private Color red, gray;
 	private Display display;
 	private GC graphics;
 	private int cur_needle_pos;
 	
 	private int clear_x, clear_y;
 	private int clear_width, clear_height;
 	
 	private double angle;
 	
 	public boolean tweening;
 	private Timer tween;
 	
 	private static int origin = 80;
 	private static int needle_length = 60;
 	private static double max_angle = Math.PI / 3.2;
 	private static int height = 160;
 	private static int width = 160;
 	
 	public CUDAGauge( Composite parent, int flags ) {
 		super( parent, SWT.NONE );
 		this.display = this.getDisplay();
 		this.red = new Color( this.display, 220, 0, 0 );
 		this.gray = new Color( this.display, 160, 160, 160 );
 		
 		this.graphics = new GC( this );
 		this.tweening = false;
 		
 		this.clear_x = 0;
 		this.clear_y = 0;
 		this.clear_width = CUDAGauge.width;
 		this.clear_height = CUDAGauge.height;
 		
 		this.setNeedle( 50 );
 		
 		this.addPaintListener( new PaintListener() {
			CUDAGauge parent;
 			public void paintControl(PaintEvent e) {
				if ( this.parent == null )
					this.parent = (CUDAGauge)e.getSource();
				this.parent.draw();
 			}
 		
 		});

		
		
 	}
 
 	
 	private void draw () {
 		
 		if ( this.gauge_img != null ) {
 			Point needle[] = new Point[] { new Point( 0, -CUDAGauge.needle_length ), new Point( -5, 0 ), new Point( 0, 10 ), new Point( 5, 0 ) };
 			double transform[][] = new double[][] { { Math.cos(angle), -Math.sin(angle) }, { Math.sin(angle), Math.cos(angle) } };
 			Point new_needle[] = new Point[4];
 			Point pivot = adjustCoords( new Point( -3, -3 ) ); 
 			
 			for ( int i = 0; i < 4; i++ )
 				new_needle[i] = adjustCoords( rotate( needle[i], transform ) );
 			
 			this.graphics.drawImage( this.gauge_img, this.clear_x, this.clear_y, this.clear_width, this.clear_height, this.clear_x, this.clear_y, this.clear_width, this.clear_height );
 			this.graphics.setAntialias( SWT.ON );
 			this.graphics.setBackground( this.red );
 			this.graphics.fillPolygon( new int[] { new_needle[0].x, new_needle[0].y, new_needle[1].x, new_needle[1].y, new_needle[2].x, new_needle[2].y, new_needle[3].x, new_needle[3].y } );
 			this.graphics.setBackground( this.gray );
 			this.graphics.fillOval( pivot.x, pivot.y, 6, 6 );
 			this.graphics.setAntialias( SWT.OFF );
 			
 			int min_x = 0, min_y = 0, max_x = 0, max_y = 0;
 			for (Point p : new_needle ) {
 				if ( p.x > max_x ) max_x = p.x;
 				if ( p.x < min_x ) min_x = p.x;
 				if ( p.y > max_y ) max_y = p.y;
 				if ( p.y < min_y ) min_y = p.y;
 			}
 			this.clear_x = Math.max( min_x - 1, 0 );
 			this.clear_y = Math.max( min_y - 1, 0 );
 			this.clear_width = Math.min( max_x - min_x + 2, CUDAGauge.width );
 			this.clear_height = Math.min( max_y - min_y + 2, CUDAGauge.height );
 		}
 		
 	}
 	 
 	
 	public void setGaugeType( String gauge_type ) {
 		
 		this.gauge_img = new Image( this.display, "res" + File.separator + "gauge-" + gauge_type + ".png" );
 		
 	}
 	
 	
 	public void setNeedle( int value /* 0~100 */ ) {
 
 		this.angle = CUDAGauge.max_angle - value / 50.0 * CUDAGauge.max_angle;
 		this.cur_needle_pos = value;
 		
 		this.draw();
 		
 	}
 	
 	
 	public void moveNeedleTo( int value /* 0~100 */ ) {
 		
 		if ( !this.tweening ) {
 		
 			this.tween = new Timer();
 			
 			if ( this.cur_needle_pos == value )
 				return;
 			
 			int ticks = Math.abs( this.cur_needle_pos - value );
 			int period = 2000 / ticks;
 			
 			this.tween.scheduleAtFixedRate( new CUDAGaugeTween( this, this.cur_needle_pos, ticks, value > this.cur_needle_pos ? 1 : -1 ), 0, period );
 			
 		}
 			
 	}
 	
 	
 	private Point adjustCoords( Point p ) {
 		
 		p.x += CUDAGauge.origin;
 		p.y += CUDAGauge.origin;
 		return p;
 		
 	}
 	
 	
 	private Point rotate( Point p, double[][] m2 ) {
 		
 		return new Point( (int)(m2[0][0] * p.x + m2[1][0] * p.y), (int)(m2[0][1] * p.x + m2[1][1] * p.y) );
 		
 	}
 	
 	
 	public void finalize() throws Throwable {
 		try {
 			this.gauge_img.dispose();
 			this.red.dispose();
 			this.gray.dispose();
 			this.graphics.dispose();
 		} catch ( Throwable e ) {
 		} finally {
 			super.dispose();
 		}
 	}
 
 }
