 /*
  * @(#) ProgramView.java
  * 
  * Tern Tangible Programming Language
  * Copyright (c) 2011 Michael S. Horn
  * 
  *           Michael S. Horn (michael.horn@tufts.edu)
  *           Northwestern University
  *           2120 Campus Drive
  *           Evanston, IL 60613
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License (version 2) as
  * published by the Free Software Foundation.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package tidal.tern;
 
 import tidal.tern.compiler.CompileException;
 import tidal.tern.compiler.Program;
 import tidal.tern.compiler.Statement;
 import tidal.tern.compiler.TangibleCompiler;
 import tidal.tern.rt.Debugger;
 import tidal.tern.rt.Interpreter;
 import tidal.tern.rt.Robot;
 import topcodes.TopCode;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 
 /**
  * This class is responsible for painting the screen and debug
  * visuals
  */
 public class ProgramView extends View implements Debugger, Runnable {
    
    public static final String TAG = "Tern";
    
    //private int POP_SOUND = 0;
    public static final int COMPILE_SUCCESS = 100;
    public static final int COMPILE_FAILURE = 101;
    
    
    
    /** Used to compile bitmap images into programs */
    protected TangibleCompiler compiler;
    
    /** Used to run tern programs */
    protected Interpreter interp;
    
    /** Hold the complete collection of topCodes found */   
    protected Program collection = null;
    
    /** Most recently compiled program */   
    protected Program program = null;
    
    /** Whether or not we're in the middle of a compile */
    protected boolean compiling = false;
    
    /** Current captured bitmap image */
    protected Bitmap bitmap = null;
 
    /** Robot that "executes" the interpreter commands */
    protected Robot robot;
 
    /** Sound effects */
    public static SoundPool sounds;
    
    /** Link back to the main activity */
    protected Tern tern = null;
    
    /** Name of the current statement (action) */
    protected String message = "";
    
    /** ID of the current statement */
    protected int trace_id = -1;
    
    /** Progress dialog for compiles */   
    protected ProgressDialog pd = null;
    
    /** Compile (camera) button */
    protected TButton camera;
    
    /** Compile (gallery) button */
    protected TButton gallery;
 
    /** Resume (play) button */   
    protected TButton play;
    
    /** Pause button */
    protected TButton pause;
    
    /** Restart button */
    protected TButton restart;
    
    /** Stop button */
    protected TButton stop;
    
    /** Connection config button */
    protected TButton config;
    
    protected int numStatements = 0;
    
    protected boolean running = false;
    
    protected boolean errorParse = false;
    
    protected boolean emptyProgram = false;
    
    protected boolean missedSticker = false;
    protected String stickerName = null;
    
    protected boolean interpFinished = false;
    
    protected boolean beginFound = false;
    protected boolean s_loopF = false;
    protected boolean e_loopF = false;
    protected boolean s_loopC = false;
    protected boolean e_loopC = false;
    
    public static int walk_sound;
    public static int jump_sound;
    public static int spin_sound;
    public static int wiggle_sound;
    public static int run_sound;
    public static int sit_sound;
    public static int stand_sound;
    public static int sleep_sound;
    public static int wait_sound;
    public static int yawn_sound;
    
 
    
    public ProgramView(Context context) {
       super(context);
    }
       
       
    public ProgramView(Context context, AttributeSet attribs) {
       super(context, attribs);
    }
    
 /**
  * Called from Activity.onCreate. Initialize everything...
  */
    public void init(Context context, Tern tern) {
       this.tern = tern;
       
       //------------------------------------------------------
       // Initialize the tangible compiler
       // Use nxt_statements and nxt_driver for LEGO NXT
       //------------------------------------------------------
       this.compiler = new TangibleCompiler(getResources(),
                                            //R.xml.roberto_statements2,
     		  							   R.xml.roberto_statements2,
                                            R.raw.roberto_driver);
       
       //------------------------------------------------------
       // Initialize the "robot" connection manager
       //------------------------------------------------------
       //this.robot = new NXTRobot(this);
       this.robot = new Roberto(this);
       this.robot.openConnection();
 
       //------------------------------------------------------
       // Initialize the runtime interpreter
       //------------------------------------------------------
       this.interp = new Interpreter();
       this.interp.setRobot(robot);
       this.interp.addDebugger(this);
       
       //------------------------------------------------------
       // Initialize sound effects
       //------------------------------------------------------
       sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
       //POP_SOUND = sounds.load(context, R.raw.pop, 1);
       walk_sound   = sounds.load(context, R.raw.walk, 1);
       jump_sound   = sounds.load(context, R.raw.jump, 1);
       spin_sound   = sounds.load(context, R.raw.spin, 1);
       wiggle_sound = sounds.load(context, R.raw.wiggle, 1);
       sit_sound    = sounds.load(context, R.raw.sit, 1);
       stand_sound  = sounds.load(context, R.raw.stand, 1);
       run_sound    = sounds.load(context, R.raw.run, 1);
       sleep_sound  = sounds.load(context, R.raw.sleep, 1);
       wait_sound   = sounds.load(context, R.raw.wait_for_tap, 1);
       yawn_sound   = sounds.load(context, R.raw.yawn, 1);
       
 
       //------------------------------------------------------
       // Create UI buttons
       //------------------------------------------------------
       this.camera =
       new TButton(getResources(),
                   R.drawable.go,
                   R.drawable.go_dn,
                   R.drawable.go_off,
                   cameraHandler);
       
       this.gallery =
       new TButton(getResources(),
                   R.drawable.gallery,
                   R.drawable.gallery_dn,
                   R.drawable.gallery_off,
                   galleryHandler);
       
       this.play =
       new TButton(getResources(),
                   R.drawable.play,
                   R.drawable.play_dn,
                   R.drawable.play_off,
                   playPauseHandler);
       
       this.pause =
       new TButton(getResources(),
                   R.drawable.pause,
                   R.drawable.pause_dn,
                   R.drawable.pause_off,
                   playPauseHandler);
       
       this.restart =
       new TButton(getResources(),
                   R.drawable.restart,
                   R.drawable.restart_dn,
                   R.drawable.restart_off,
                   restartHandler);
       
       this.stop =
     	      new TButton(getResources(),
     	                  R.drawable.stop_button,
     	                  R.drawable.stop_button,
     	                  R.drawable.stop_button,
     	                  stopHandler);
       
       this.config =
       new TButton(getResources(),
                   R.drawable.roberto,
                   R.drawable.roberto,
                   R.drawable.roberto,
                   emptyHandler);
       //new TButton(getResources(),
       //            R.drawable.config,
       //            R.drawable.config_dn,
       //            R.drawable.config_off,
       //            configHandler);
    }
    
    
 /**
  * Called from Activity.onDestroy()
  */
    public void destroy() {
       this.robot.closeConnection();
       if (this.bitmap != null) {
          this.bitmap.recycle();
          this.bitmap = null;
       }
    }
    
    
    public void setBluetoothDevice(String address) {
       this.robot.setAddress(address);
       this.robot.openConnection();
    }
    
    
 /**
  * COMPILE PHASE 1: Called from the GO/CAMERA button. Starts a tangible compile
  * by launching a camera intent
  */
    public void startCompile(boolean capture) {
       if (compiling) return;
       try {
          if (this.bitmap != null) {
             this.bitmap.recycle();
             this.bitmap = null;
          }
          if (capture) {
             tern.captureBitmap();
          } else {
             tern.selectBitmap();
          }
       } catch (Exception x) {
          Log.e(TAG, "Save file error.", x);
       }
    }
    
 
 /**
  * COMPILE PHASE 2: Called from Tern when the bitmap is ready to be processed
  * (after the user has taken a picture)
  */
    public void loadBitmap(Bitmap bitmap) {
       if (bitmap != null) {
          this.compiling = true;
          this.bitmap = bitmap;
          showProgressDialog("Compiling Program...");
          (new Thread(this)).start();
       }
    }
 
 
 /**
  * COMPILE PHASE 3: Processes the bitmap and then calls finishCompile via a
  * handler
  */
    public void run() {
 	   
 	   
       try { 
     	  this.collection = compiler.collect(this.bitmap); //full set of topcodes here
     	  this.program = compiler.compile(this.collection); //convert to Assembly
           compileHandler.sendEmptyMessage(COMPILE_SUCCESS);
       }
       catch (CompileException cx) {
          Log.e(TAG, cx.getMessage());
          compileHandler.sendEmptyMessage(COMPILE_FAILURE);
       }
    }
    
    
 /**
  * COMPILE PHASE 4: Called after the bitmap has been processed from a
  * separate thread (via a handler).
  */
    protected void finishCompile(boolean success) {
 	   Log.i(TAG, "Compile Finished...");
        hideProgressDialog();
        this.compiling = false;
        
        this.numStatements = 0;
        
        //reset all flags..
 	   this.e_loopC = this.e_loopF = this.s_loopC = this.s_loopF = 
 	   this.errorParse = this.emptyProgram = this.beginFound = false;
 	   
 	   //checkBeginSticker();
        this.beginFound = collection.hasStartStatement();
        
        if (!success) {
     	   errorParse = true;
     	   if (running)
    		   running = false;
     	   
     	   Roberto.isPlaying = false;
     	   program = null; // to prevent the ToolBox from being drawn...
     	   repaint();
     	   return;
        }
        
        if (program.isEmpty()) {
     	   this.emptyProgram = true;
     	   repaint();
        }
        
        try {
     	   interp.stop();
     	   interp.clear();
     	   interp.load(program.getAssemblyCode());
     	   //interp.start();
     	   repaint();
        } catch (Exception x) {
     	   Log.e(TAG, "Interpreter error", x);
        }
    }
    
 /**
  * Handle button presses
  */
    public boolean onTouchEvent(MotionEvent event) {
       
       if (
          this.camera.touchEvent(event) ||
          this.gallery.touchEvent(event) ||
          this.play.touchEvent(event) ||
          this.pause.touchEvent(event) ||
          this.restart.touchEvent(event) ||
          this.stop.touchEvent(event) ||
          this.config.touchEvent(event)) {
          repaint();
       }
       
       else {
     	  int action = event.getAction();   
     	  if (action == MotionEvent.ACTION_DOWN) {         
    		   Roberto.tsensor = true;  } 
       }
 	   
       return true;
    }
    
    
    protected void onDraw(Canvas canvas) {
 	   int w = this.getWidth();
 	   int h = this.getHeight();
 	   int dw = 0, dh = 0;
 	   float ds=0;
 	   
 	   Paint font = new Paint(Paint.ANTI_ALIAS_FLAG);
        font.setColor(Color.BLACK);
        font.setStyle(Style.FILL);
        //font.setTextSize(23); //tablet version
        font.setTextSize(18); //phone version
        font.setTextAlign(Paint.Align.CENTER);
 	   
        // clear background 
        canvas.drawRGB(210, 210, 210);
        
        if (this.emptyProgram) {
     	   drawRect(w, h, canvas);
     	   canvas.drawText("Empty program,", w/2, 27, font);
            canvas.drawText("Please try again..", w/2, 67, font);
            drawCamera(w, h, canvas);
            drawGallery(w, h, canvas);
            this.emptyProgram = false;
            program = null;
        }
       
       else { //not an empty program
     	  if (!running) {
     		  drawLogo(w, h, canvas);
     	      drawCamera(w, h, canvas);
     	      drawGallery(w, h, canvas);
     	      drawConfig(w, h, canvas);
     	      
     	      // Draw program bitmap with debug info
     	      if (bitmap != null) {
     	    	  dw = bitmap.getWidth();
    	           	  dh = bitmap.getHeight();
    	           	  ds = ((float)h / dh) * 0.85f;
    	           	  dw *= ds;
    	           	  dh *= ds;
     	    	  drawBitmap(w, h, dw, dh, ds, canvas);
     	      }
     	  } 
     	  else { //if running, Draw robot
     		  this.robot.draw(canvas);  
     	  }
     	  
     	  //Begin sticker is missing
  	      if (!this.beginFound && program != null) {
  		      drawRect(w, h, canvas);
  	    	  canvas.drawText("Start sticker wasn't detected,", w/2, 27, font);
 	          canvas.drawText("Make sure it is aligned and not faded and try again..", w/2, 67, font); 
  	          program = null;
  	      }
  	      
     	  //Parsing error occured
     	  if (errorParse) {  
     		  checkMisPlaced();
     		  //drawRect(w, h, canvas);
     		  
     		  if (!this.s_loopC && this.e_loopC ) { //if end repeat isn't compiled and start is compiled
     			  Log.i(TAG, "begin or end repeat sticker isn't aligned");
     	  		  //canvas.drawText("Make sure begin and end repeat stickers are aligned,", w/2, 27, font);
     	  		  //canvas.drawText("and try again..", w/2, 67, font);
     		  }
     	  	   
     	  	  else if (this.s_loopF && !this.s_loopC && !this.e_loopF) { // if end repeat isn't found
     	  		  drawRect(w, h, canvas);
     	  		  Log.i(TAG, "make sure you paste the end repeat sticker");
    	  		  canvas.drawText("'End Repeat' sticker wasn't detected,", w/2, 27, font);
    	  		  canvas.drawText("Make sure it is aligned and not faded and try again..", w/2, 67, font);
     	  	  }
     	  	  
     	  	  else if (!this.s_loopF && !this.e_loopC && this.e_loopF) { //if start repeat isn't found while end repeat compiled
     	  		  drawRect(w, h, canvas);
     	  		  Log.i(TAG, "make sure you paste the begin repeat sticker");
    	  		  canvas.drawText("'BeginRepeat' sticker wasn't detected,", w/2, 27, font);
    	  		  canvas.drawText("Make sure it is aligned and not faded and try again..", w/2, 67, font);
     	  	  }
     	      errorParse = false;
     	  }
     	  
     	  if (this.numStatements < 2 && program!= null) {
     		  //drawRect(w, h, canvas);
 	  		  //canvas.drawText("Make sure stickers after 'Begin' are aligned", w/2, 27, font);
 	  		  //canvas.drawText("and not faded and try again..", w/2, 67, font);
 	  		  program = null;
     	  }
     	  
     	  drawToolBox(w, h, canvas); 
       }
      
    }
    
    protected void drawBitmap(int w, int h, int dw, int dh, float ds, Canvas c) {
 	   int dx = w/2 - dw/2;
        int dy = h/2 - dh/2;
        
        RectF dest = new RectF(dx, dy, dx + dw, dy + dh);
        c.drawBitmap(bitmap, null, dest, null);
        
        if (collection != null) {
     	   c.save();
            c.translate(w/2 - dw/2, h/2 - dh/2);
            c.scale(ds, ds, 0, 0);
              
            // Draw connections first
            for (Statement s : collection.getStatements()) {
         	  drawStatementConnector(s, c);
            }
              
            // Now highlight topcodes
            for (Statement s : collection.getStatements()) {
          	  TopCode top = new TopCode(s.getTopCode());
                 
                if (!s.isCompiled() && !s.isParam()) { //show which aren't compiled, ignore parameters
             	   top.setDiameter( top.getDiameter() * 1.25f );
                    Log.i(TAG, s.getName() + " sticker is misplaced");
                    outlineTopCode(top, Color.RED, c);
                    top.draw(c);
                 }
                 else {
              	   top.setDiameter( top.getDiameter() * 1.25f );
              	   outlineTopCode(top, Color.GREEN, c);
              	   top.draw(c);
              	   this.numStatements++;
                 }
            }
            c.restore();
        }
    }
    
    protected void drawStatementConnector(Statement statement, Canvas g) {
 	   if (!statement.hasOutgoingConnection()) return;
 	   TopCode a = statement.getTopCode();
 	   TopCode b = statement.getFirstOutgoingConnection().getTopCode();
 
 	   if (a == null || b == null) return;
 	   
 	   Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	   paint.setColor(Color.YELLOW);
 	   paint.setStrokeWidth(a.getDiameter() * 0.3f);
 	   g.drawLine(a.getCenterX(), a.getCenterY(), 
 			      b.getCenterX(), b.getCenterY(), paint);	  
 	   
 	   //use 15px for phone version, 50px for tablet
 	   Path path = new Path(); 
        path.moveTo(15, 0); 
        path.lineTo(0, 15); 
        path.lineTo(0, -15); 
        path.close(); 
        path.offset(b.getCenterX()- b.getDiameter() - 3, b.getCenterY()); 
        g.drawPath(path, paint); 
    }
 
    protected void outlineTopCode(TopCode top, int color, Canvas g) {
 	   Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	   float r = top.getDiameter() * 0.6f;
 	   paint.setColor(color);
 	   g.drawCircle(top.getCenterX(), top.getCenterY(), r, paint);
    }
    
    protected void drawLogo(int w, int h, Canvas c) {
 	   Resources res   = getResources();
 	   Drawable logo   = res.getDrawable(R.drawable.logo);
 	   int dw = logo.getIntrinsicWidth();
 	   int dh = logo.getIntrinsicHeight();
 	   float ds = Math.min(0.8f, 0.8f * w / dw);
 	   dw *= ds;
 	   dh *= ds;
 	   int dx = w/2 - dw/2;
 	   int dy = h/2 - dh/2;
 	   logo.setBounds(dx, dy, dx + dw, dy + dh);
 	   logo.draw(c);
    }
    
    protected void drawCamera(int w, int h, Canvas c) {
        int dw = this.camera.getWidth();
        int dh = this.camera.getHeight();
        this.camera.setLocation(w - dw - 12, h - dh - 12);
        this.camera.setEnabled( true );
        this.camera.draw(c);
    }
    
    
    protected void drawGallery(int w, int h, Canvas c) {
 	   int dx = w - gallery.getWidth() - camera.getWidth() - 36;
 	   int dy = h - gallery.getHeight() - 5;
 	   this.gallery.setLocation(dx, dy);
 	   this.gallery.setEnabled( true );
 	   this.gallery.draw(c);
    }
    
    protected void drawConfig(int w, int h, Canvas c) {
 	   this.config.setLocation(3, h - config.getHeight() - 3);
 	   this.config.setEnabled( true );
 	   this.config.draw(c); 
    }
    
    protected void drawRect(int w, int h, Canvas c) {
 	   //RectF toolbox = new RectF(w/2 - 300, 0, w/2 + 300,  100); //tablet version
 	   RectF toolbox = new RectF(w/2 - 225, 0, w/2 + 225,  100); //phone version
 	   Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	   paint.setColor(Color.WHITE);
 	   paint.setStyle(Paint.Style.FILL);
 	   c.drawRoundRect(toolbox, 10, 10, paint);
 	   paint.setColor(Color.BLACK);
 	   paint.setStyle(Paint.Style.STROKE);
 	   c.drawRoundRect(toolbox, 10, 10, paint);
    }
    
    protected void drawToolBox(int w, int h, Canvas c) {
 	   int dw = this.play.getWidth();
 	   int dh = this.play.getHeight();
 	   this.play.setLocation(w/2 - 20 , h - dh - 15);
 	   this.pause.setLocation(w/2 - 20, h - dh - 15);
 	   this.restart.setLocation(w/2 - dw - 20, h - dh - 15);
 	   this.stop.setLocation(w/2 + dw - 20 , h - dh - 14);
 
 	   // Draw toolbox border
 	   if (program != null && bitmap != null) {
 		   int dx = w/2 - dw - 30;
 	       int dy = h - dh - 25;
 	       dw = this.play.getWidth() * 2 + 60;
 	       dh = this.play.getHeight() + 20;
 	       RectF toolbox = new RectF(dx, dy, dx + dw, dy + dh);
 	       Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	       paint.setColor(Color.WHITE);
 	       paint.setStyle(Paint.Style.FILL);
 	       c.drawRoundRect(toolbox, 10, 10, paint);
 	       paint.setColor(Color.BLACK);
 	       paint.setStyle(Paint.Style.STROKE);
 	       c.drawRoundRect(toolbox, 10, 10, paint);
 	   }
 	      
 	   this.play.setEnabled( false );
 	   this.pause.setEnabled( false );
 	   this.restart.setEnabled( false );
 	   this.stop.setEnabled( false );
 	    
 	   if (program != null && bitmap != null) {
 		   this.restart.enable();
 		   this.restart.draw(c);
 		   this.stop.enable();
 		   this.stop.draw(c);
 		   if (interp.isPaused() || interp.isStopped()) {
 			   this.play.enable();
 	           this.play.draw(c);
 		   } else {
 			   this.pause.enable();
 	           this.pause.draw(c);
 	         }
 	   }
 	   
    }
    
    public void showProgressDialog(String message) {
       this.pd = ProgressDialog.show(tern, TAG, message, true, false);
    }
    
    
    public void hideProgressDialog() {
       this.pd.dismiss();
    }
    
    public void checkBeginSticker() {
 	   for (Statement s : collection.getStatements()) {
 		   if (s.isStartStatement())
 			   this.beginFound = true;
 	   }
    }
    
    public void checkMisPlaced() {
 	   for (Statement s : collection.getStatements()) {
 		   if (s.isSLoopStatement()){
 			   this.s_loopF = true;
 		   }
 			   
 		   else if (s.isELoopStatement()) {
 			   this.e_loopF = true;
 		   }
 		   
 		   if (!s.isCompiled()) { //show which aren't compiled
         	   Log.i(TAG, s.getName() + " sticker is misplaced");
         	   this.missedSticker = true;
         	   this.stickerName = s.getName();
         	   
         	   if (this.stickerName.equals("End Repeat")) {
         		   this.e_loopC = true;
         	   }
         	   else if (this.stickerName.equals("Begin Repeat")) {
         		   this.s_loopC = true;
         	   }
            }
 	   }
    }
    
    
 /**
  * DEBUGGER IMPLEMENTATION
  */
    public void processStarted(tidal.tern.rt.Process p) { Roberto.tsensor = false; running = true; this.interpFinished= false; Log.i(TAG, "processStarted");}
    
    
    public void processStopped(tidal.tern.rt.Process p) {  this.interpFinished = true; repaint();  Log.i(TAG,"processStopped"); }
 
    
    public void trace(tidal.tern.rt.Process p, String message) {
       try {
          this.trace_id = Integer.parseInt(message);
          //sounds.play(POP_SOUND, 1, 1, 1, 0, 1);
          //repaint();
       } catch (Exception x) {
          this.trace_id = -1;
       }
    }
 
    
    public void print(tidal.tern.rt.Process p, String message) {
       Log.i(TAG, message);
       this.message = message;
       //repaint();
    }
    
    
    public void error(tidal.tern.rt.Process p, String message) {
       Log.i(TAG, message);
       this.message = message;
       repaint();
    }
    
    
 /**
  * Thread-safe invalidate function
  */
    public void repaint() {
       repaintHandler.sendEmptyMessage(0);
    }
    
    public void repaint(int delay_ms) {
       repaintHandler.sendEmptyMessageDelayed(0, delay_ms);
    }
    
    private Handler compileHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
          finishCompile(msg.what == COMPILE_SUCCESS);
       }
    };
    
    private Handler repaintHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
          invalidate();
       }
    };
    
    private Handler cameraHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
          startCompile(true);
       }
    };
    
    private Handler galleryHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
          startCompile(false);
       }
    };
    
    private Handler playPauseHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
          if (interp.isPaused()) {
             interp.resume();
          } else if (interp.isStopped()) {
             interp.restart();
          } else {
             interp.pause();
          }
       }
    };
    
    private Handler restartHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
          interp.restart();
       }
    };
    
    private Handler stopHandler = new Handler() {
 	      @Override public void handleMessage(Message msg) {
 	         tern.onBackPressed();
 	   }
    };
 	   
    private Handler configHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
          tern.selectBluetoothDevice();
       }
    };
    
    private Handler emptyHandler = new Handler() {
       @Override public void handleMessage(Message msg) {
 
       }
    };
 }
