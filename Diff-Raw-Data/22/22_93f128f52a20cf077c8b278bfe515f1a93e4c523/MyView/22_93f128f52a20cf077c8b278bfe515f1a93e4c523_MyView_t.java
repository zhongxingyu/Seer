 package dragimage.namespace;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class MyView extends View
 {
 	private int x=100;
 	private int y=100;
 	private Bitmap img=null;
 	private boolean dragOn=false;
 	// 10) offset tramite 2 variabili d'istanza
 	private int offsetx;
 	private int offsety;
 	
 	// 4) inizializzare la bitmap da una risorsa drawable
 	public MyView(Context context)
 	{
 		super(context);
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		opts.inJustDecodeBounds = true;
 		img = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
 		// 10) offset tramite 2 variabili d'istanza
		//offsetx = (img.getWidth())/2;
		//offsety = (img.getHeight())/2;
 	}
 	
 	// 5) Sovrascrivere il metodo onDraw della bitmap per ridisegnare l'immagine
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		canvas.drawBitmap(img, x, y, null);
 	}
 	
 	// 7) Riscrivere il metodo onTouchEvent
 	@Override
 	public boolean onTouchEvent(MotionEvent event) 
 	{
 		int eventaction = event.getAction();
 		int touchx = (int)event.getX();
 		int touchy = (int)event.getY();
 		// visto che il drop funziona anche se non clicco sull'immagine controllo i valori di touchx e touchy con dei log
 		String XX = Integer.toString(touchx);
 		String YY = Integer.toString(touchy);
 		Log.d("TOUCH X", (XX));
 		Log.d("TOUCH Y", (YY));
 		switch (eventaction)
 		{
 			case MotionEvent.ACTION_DOWN:
 			{
 				if ((touchx > x & touchx < x + img.getWidth()) & (touchy > y & touchy < y + img.getHeight()))
 				{
					offsetx=touchx-x;
					offsety=touchy-y;
 					dragOn=true;
 				}
 				break;	
 			}
 			case MotionEvent.ACTION_MOVE:
 			{
				if(dragOn)
				{
					x=touchx-offsetx;
					y=touchy-offsety;
					//x=touchx;
					//y=touchy;
					invalidate();
				}
				
 				break;
 			}
 			case MotionEvent.ACTION_UP:
 			{
 				dragOn=false;
 			}
 		
 		}
 		return true;
 	}
 		
 }
