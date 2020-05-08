 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.cuentos;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.User;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.DataStorageService;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.utils.GiftCount;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.utils.GiftPopup;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 import com.google.inject.Inject;
 
 @ContentView(R.layout.pintar)
 public class PintarActivity extends RoboActivity implements
 		OnCompletionListener {
 	private final static String FOTO_COUNT = ".pintar";
 
 	@Inject
 	private DataStorageService db;
 
 	@Inject
 	private User user;
 
 	@InjectView(R.id.dibujo)
 	private FrameLayout dibujo;
 
 	@InjectView(R.id.amarillo)
 	private ImageView amarillo;
 
 	@InjectView(R.id.rojo)
 	private ImageView rojo;
 
 	@InjectView(R.id.verde)
 	private ImageView verde;
 
 	@InjectView(R.id.azul)
 	private ImageView azul;
 
 	@InjectView(R.id.naranja)
 	private ImageView naranja;
 
 	@InjectView(R.id.violeta)
 	private ImageView violeta;
 
 	@InjectView(R.id.gros30)
 	private ImageView gros30;
 
 	@InjectView(R.id.gros20)
 	private ImageView gros20;
 
 	@InjectView(R.id.gros10)
 	private ImageView gros10;
 
 	@InjectView(R.id.rubber)
 	private ImageView rubber;
 
 	@InjectView(R.id.camera)
 	private ImageView camera;
 
 	private List<MyPaint> paths = new ArrayList<MyPaint>();
 	private MyView view;
 	private Path path;
 	private Paint paint;
 
 	private Bitmap bitmap;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		if (!db.contain(user.getUserName() + FOTO_COUNT))
 			db.put(user.getUserName() + FOTO_COUNT, 0);
 		Bundle arguments = getIntent().getExtras();
 		int cuento = arguments.getInt("cuento");
 
 		dibujo.setBackgroundResource(R.drawable.pinocho_pintar);
 
 		dibujo.setDrawingCacheEnabled(true);
 		view = new MyView(this);
 		view.setDrawingCacheEnabled(true);
 		view.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		// view.setBackgroundResource(R.drawable.paint);
 		view.setZOrderOnTop(true);
 		view.getHolder().setFormat(PixelFormat.TRANSPARENT);
 		dibujo.addView(view);
 
 		paint = createPaint(0xffffff00, 30);
 
 		view.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				synchronized (view.getThread().getHolder()) {
 					if (event.getAction() == MotionEvent.ACTION_DOWN) {
 						path = new Path();
 						path.moveTo(event.getX(), event.getY());
 						path.lineTo(event.getX(), event.getY());
 					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 						// canvas.drawPath(path, paint);
 						// path = new Path();
 						path.lineTo(event.getX(), event.getY());
 						// path.lineTo(event.getX(), event.getY());
 						paths.add(new MyPaint(paint, path));
 					} else if (event.getAction() == MotionEvent.ACTION_UP) {
 						path.lineTo(event.getX(), event.getY());
 						paths.add(new MyPaint(paint, path));
 						// canvas.drawPath(path, paint);
 					}
 					// view.getHolder().unlockCanvasAndPost(canvas);
 					// holder.unlockCanvasAndPost(canvas);
 					return true;
 
 				}
 			}
 		});
 
 		violeta.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xffff00ff);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(),
 						R.raw.violeta);
 				mp.start();
 				completion(mp);
 			}
 		});
 
 		verde.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xff00ff00);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(), R.raw.verde);
 				mp.start();
 				completion(mp);
 			}
 		});
 
 		gros30.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				paint = copyPaint(paint);
 				paint.setStrokeWidth(30);
 				gros30.setBackgroundResource(R.drawable.borde);
 				gros10.setBackgroundResource(R.drawable.grosor_lapiz);
 				gros20.setBackgroundResource(R.drawable.grosor_lapiz);
 			}
 		});
 
 		gros20.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				paint = copyPaint(paint);
 				paint.setStrokeWidth(20);
 				gros20.setBackgroundResource(R.drawable.borde);
 				gros10.setBackgroundResource(R.drawable.grosor_lapiz);
 				gros30.setBackgroundResource(R.drawable.grosor_lapiz);
 
 			}
 		});
 
 		gros10.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				paint = copyPaint(paint);
 				paint.setStrokeWidth(10);
 				gros10.setBackgroundResource(R.drawable.borde);
 				gros20.setBackgroundResource(R.drawable.grosor_lapiz);
 				gros30.setBackgroundResource(R.drawable.grosor_lapiz);
 
 			}
 		});
 
 		rojo.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xffff0000);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(), R.raw.rojo);
 				mp.start();
 				completion(mp);
 			}
 		});
 
 		azul.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xff0000ff);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(), R.raw.azul);
 				mp.start();
 				completion(mp);
 			}
 		});
 
 		naranja.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xffff5500);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(),
 						R.raw.naranja);
 				mp.start();
 				completion(mp);
 			}
 		});
 
 		amarillo.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xffffff00);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(),
 						R.raw.amarillo);
 				mp.start();
 				completion(mp);
 			}
 		});
 
 		rubber.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				paint = copyPaint(paint);
 				paint.setColor(0xffffffff);
 
 			}
 		});
 		final PintarActivity that = this;
 		camera.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				int fotos = db.get(user.getUserName() + FOTO_COUNT,
 						Integer.class);
 				db.put(user.getUserName() + FOTO_COUNT, ++fotos);
 				MediaPlayer mp = MediaPlayer.create(v.getContext(),
 						R.raw.camera_shutter);
 				mp.start();
 				completion(mp);
 				Bitmap bitmap = dibujo.getDrawingCache();
 				File file = new File("/sdcard/integrarT/" + user.getUserName());
 				file.mkdirs();
 				file = new File(file, "foto" + fotos + ".png");
 
 				Canvas canvas = new Canvas(bitmap);
 
 				Rect src = new Rect(0, 0, view.getWidth(), view.getHeight());
 				canvas.drawBitmap(that.bitmap, src, src, null);
 				try {
 					file.createNewFile();
 					FileOutputStream os = new FileOutputStream(file);
 					bitmap.compress(CompressFormat.PNG, 100, os);
 				} catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 				showMessage("/integrarT/" + user.getUserName());
 			}
 
 			
 		});
 		user.addGifts(3);
 		db.put("currentUser", user);
 		new GiftPopup(this, user.getGifts(), GiftCount.THREE).show();
 	}
 
 	private void showMessage(final String str) {
 		final Context that = this;
 		user.addGifts(1);
 		db.put("currentUser", user);
 		Dialog dialog = new GiftPopup(this, user.getGifts());
 		dialog.show();
 		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				final AlertDialog.Builder builder = new AlertDialog.Builder(that);
 				builder.setTitle(R.string.drawingTitle);
 				builder.setMessage(getString(R.string.drawingMessage)+ str);
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						builder.create().show();
 					}
 				});
 			}
 		});
 
 	}
 
 	public void completion(MediaPlayer mp) {
 		mp.setOnCompletionListener(new OnCompletionListener() {
 
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 
 				if (mp != null) {
 					mp.stop();
 					mp.release();
 					mp = null;
 
 				}
 
 			}
 		});
 
 	}
 
 	public Paint createPaint(int color, int grosor) {
 		paint = new Paint();
 		paint.setDither(true);
 		paint.setColor(color);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeJoin(Paint.Join.ROUND);
 		paint.setStrokeCap(Paint.Cap.ROUND);
 		paint.setStrokeWidth(grosor);
 		return paint;
 
 	}
 
 	public Paint copyPaint(Paint paint) {
 		Paint copyPaint = new Paint();
 		copyPaint.setDither(true);
 		copyPaint.setColor(paint.getColor());
 		copyPaint.setStyle(Paint.Style.STROKE);
 		copyPaint.setStrokeJoin(Paint.Join.ROUND);
 		copyPaint.setStrokeCap(Paint.Cap.ROUND);
 		copyPaint.setStrokeWidth(paint.getStrokeWidth());
 		return copyPaint;
 
 	}
 
 	private class MyView extends SurfaceView implements SurfaceHolder.Callback {
 		private MyThread thread;
 
 		public MyView(Context context) {
 			super(context);
 			this.getHolder().addCallback(this);
 			thread = new MyThread(this);
 			setFocusable(true);
 			thread.start();
 		}
 
 		public MyThread getThread() {
 			return thread;
 		}
 
 		@Override
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 				int height) {
 
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder) {
 			Log.d("Starting View", "Iniciando");
 			// thread.start();
 		}
 
 		@Override
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			thread.end();
 		}
 
 		@Override
 		public void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			if (bitmap == null && view.getWidth() > 0)
 				bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
 						Config.ARGB_8888);
 
 			Canvas buffer = null;
 			if (bitmap != null)
 				buffer = new Canvas(bitmap);

 			for (MyPaint path : paths) {
 				buffer.drawColor(0);
 				canvas.drawPath(path.getPath(), path.getPaint());
 				if (buffer != null)
 					buffer.drawPath(path.getPath(), path.getPaint());
 			}
 
 		}
 
 	}
 
 	private class MyThread extends Thread {
 		private boolean stop;
 		private final MyView view;
 
 		public MyThread(MyView view) {
 			this.view = view;
 		}
 
 		public SurfaceHolder getHolder() {
 			return view.getHolder();
 		}
 
 		public void end() {
 			stop = true;
 		}
 
 		@Override
 		public void run() {
 			while (!stop) {
 				Canvas canvas = null;
 				SurfaceHolder holder = view.getHolder();
 				try {
 					canvas = holder.lockCanvas();
 					synchronized (holder) {
 						view.onDraw(canvas);
 					}
 				} finally {
 					if (canvas != null) {
 						holder.unlockCanvasAndPost(canvas);
 					}
 				}
 			}
 		}
 	}
 
 	private class MyPaint {
 
 		final private Paint paint;
 
 		public Paint getPaint() {
 			return paint;
 		}
 
 		public Path getPath() {
 			return path;
 		}
 
 		final private Path path;
 
 		public MyPaint(Paint paint, Path path) {
 			this.paint = paint;
 			this.path = path;
 
 		}
 
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
