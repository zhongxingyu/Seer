 package riateche;
 
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import riateche.KeyboardLayoutItem.Command;
 import riateche.keyboard.R;
 //import riateche.keyboard.R;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.Paint.Align;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 
 public class KeyboardView extends View implements GestureDetector.OnGestureListener {
  private static final int singleGestureMaxTime = 200; //milliseconds
   private final Service service;
   private int preferredButtonSize, minButtonSize;
   private final int padding = 2;
   private final Point buttonsCount = new Point();
   private final Paint paint = new Paint();    
   private final Paint paintBackground = new Paint();    
   private final Paint paintText = new Paint();  
   private boolean hasCurrentButton = false;
   private int currentButtonX, currentButtonY;
   private float currentButtonPositionX, currentButtonPositionY;
  private long gestureStartTime;
 
   private int buttonSize = 36;
   private int paddingLeft = 0, paddingTop = 0;
   
   private boolean capsEnabled = false;
   private boolean shiftPressed = false;
   
   private boolean hasShiftCandidate = false;
   private int shiftCandidateX, shiftCandidateY;
 
   DisplayMetrics displayMetrics = new DisplayMetrics();
   
   private Timer timer = new Timer(false);
   private final int repeatDelay = 20;
   private int repeatCounter = -1;
   private KeyboardLayoutItem repeatCandidate = null;
   
   private boolean hasCandidateButton = false;
   private int candidateButtonX, candidateButtonY;
 
  
   
 
   private GestureDetector gd;
   
   private KeyboardLayoutItem getItemForCandidate(int buttonX, int buttonY) {
     if (!hasCurrentButton) return null;
     int posInButtonX = buttonX - currentButtonX + 1;
     int posInButtonY = buttonY - currentButtonY + 1;
     return service.getCurrentLayout().getItemForButton(currentButtonX, currentButtonY, posInButtonX, posInButtonY);
   }
 
 
   private Rect getButtonRect(int x, int y) {
     int left = paddingLeft + x * (buttonSize + padding);
     int top = paddingTop + y * (buttonSize + padding);
     return new Rect(left, top, left + buttonSize, top + buttonSize);   
   }
 
   private boolean isCandidateDiagonal(int buttonX, int buttonY) {
     return (currentButtonX - buttonX + currentButtonY - buttonY) % 2 == 0;
   }
 
   public KeyboardView(Service service) {
     super(service);
     this.service = service;
     onLayoutChanged();
 
     WindowManager wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
     
     if (wm != null) {
       wm.getDefaultDisplay().getMetrics(displayMetrics);
     } else {
       Log.e("KeyboardView", "Failed to get window manager");
     }
     
     preferredButtonSize = (int) (displayMetrics.densityDpi * 0.5);
     minButtonSize = (int) (displayMetrics.densityDpi * 0.35);
     buttonSize = preferredButtonSize;
     
 
     paintBackground.setARGB(255, 255, 255, 255);
     paintBackground.setStyle(Style.FILL); 
     paintText.setTextAlign(Align.CENTER);
     
     
     timer.schedule(new TimerTask() {
       
       @Override
       public void run() {
         if (repeatCounter > 0) {
           repeatCounter--;
         } else if (repeatCounter == 0) {
           post(new Runnable() {
             public void run() {
               executeCurrentCommand();
             }
           });          
         }
       }
     },  new Date(), 50);
     //Typeface typeface = Typeface.createFromAsset(service.getAssets(), "font2.ttf"); 
     //paintText.setTypeface(typeface);
     gd = new GestureDetector(getContext(), this);
   }
 
 
   @Override
   protected void onDraw(Canvas canvas) {
     canvas.drawPaint(paintBackground);
     for(int i = 0; i < buttonsCount.x; i++) {
       for(int j = 0; j < buttonsCount.y; j++) {
         boolean candidate = false, currentCandidate = false, currentShiftCandidate = false;
         if (hasCurrentButton) {
           if (i == currentButtonX && j == currentButtonY) {
             continue;
           } else if (Math.abs(i - currentButtonX) <= 1 &&
               Math.abs(j - currentButtonY) <= 1) {
             candidate = true;
           }
         }
         if (hasCandidateButton) {
           if (candidateButtonX == i && candidateButtonY == j) currentCandidate = true;
         }
         if (hasShiftCandidate && shiftPressed) {
           if (shiftCandidateX == i && shiftCandidateY == j) currentShiftCandidate = true;
         }
         if (currentCandidate) {
           paint.setARGB(255, 0, 255, 0);            
         } else if (currentShiftCandidate) {
           paint.setARGB(255, 255, 255, 0);
         } else {
           paint.setARGB(255, 0, 0, 0);                    
         }
         Rect rect = getButtonRect(i, j);
         canvas.drawRect(rect, paint);        
         
         if (currentCandidate || currentShiftCandidate) {
           paintText.setARGB(255, 0, 0, 0);
         } else {
           paintText.setARGB(255, 255, 255, 255);          
         }
         if (candidate) {
           paintText.setTextSize(buttonSize / 2);
           KeyboardLayoutItem item = getItemForCandidate(i, j);
           String s;
           if (item.getCommand() == Command.SWITCH_LAYOUT) {
             int index = item.getLayoutNumber();
             if (index >= 0 && index < service.getLayouts().size()) {
               s = service.getLayouts().get(item.getLayoutNumber()).getLabel();
             } else {
               s = "";
             }
           } else {
             s = item.keyLabel(capsEnabled, currentShiftCandidate);
           }
           canvas.drawText(s, 
               rect.left + buttonSize / 2,  
               rect.top + buttonSize * 3 / 4, 
               paintText);                       
           
         } else {
           int textSize = buttonSize / 4;
           paintText.setTextSize(textSize);
           int textPaddingY = (int) (( buttonSize - 3 * textSize ) / 4);
           /*Log.i("KeyboardView", "=======");
           Log.i("KeyboardView", "textSize=" + textSize  + " real=" + paint.getTextSize());
           Log.i("KeyboardView", "dpi=" + displayMetrics.densityDpi);
           Log.i("KeyboardView", "density=" + displayMetrics.density);
           Log.i("KeyboardView", "textPaddingY=" + textPaddingY);
           Log.i("KeyboardView", "buttonSize=" + buttonSize);
           Log.i("KeyboardView", "descent=" + paintText.descent()); */
           for(int ti = 0; ti < 3; ti++) {  
             for(int tj = 0; tj < 3; tj++) {
               //Log.i("", "" + service.getCurrentLayout());
               KeyboardLayoutItem item = service.getCurrentLayout().getItemForButton(i,  j, ti, tj);
               if (item != null) {
                 if (ti == 1 && tj == 1) {
                   paintText.setARGB(255, 0, 0, 200);
                 } else if (item.getCommand() != Command.LETTER) {
                   paintText.setARGB(255, 200, 0, 0);                  
                 } else {
                   paintText.setARGB(255, 200, 200, 200);                  
                 }
                 canvas.drawText(item.keyLabel(capsEnabled, false), 
                     rect.left + (ti + 1) * buttonSize / 4, 
                     rect.top + (tj + 1) * (textPaddingY + textSize) - paintText.descent(), 
                     paintText);                       
               }
             }
           }
         }
       }
     }
     if (hasCurrentButton) {
       paint.setARGB(50, 0, 0, 0);
       canvas.drawRect(currentButtonPositionX, currentButtonPositionY, 
           currentButtonPositionX + buttonSize, currentButtonPositionY + buttonSize, 
           paint);                          
     }
   }
   
   private int getMaxButtonSize(int maxTotalWidth, int maxTotalHeight) {
     int maxButtonSizeX = (maxTotalWidth - padding * (buttonsCount.x + 1)) / buttonsCount.x; 
     int maxButtonSizeY = (maxTotalHeight - padding * (buttonsCount.y + 1)) / buttonsCount.y;
     int maxButtonSize = maxButtonSizeX > maxButtonSizeY ? maxButtonSizeY : maxButtonSizeX;
     int r = preferredButtonSize > maxButtonSize ? maxButtonSize : preferredButtonSize;    
     r = minButtonSize > r? minButtonSize: r;
     return r;    
   }
 
   @Override
   protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
     Log.i("", "onMeasure");
     int x = MeasureSpec.getSize(widthMeasureSpec);
     int y = MeasureSpec.getSize(heightMeasureSpec);
     int newButtonSize = getMaxButtonSize(x, y / 2);        
     setMeasuredDimension(x, padding + buttonsCount.y * (padding + newButtonSize));
     //setMeasuredDimension(padding + buttonsCount.x * (padding + buttonSize), 
     //    padding + buttonsCount.y * (padding + buttonSize));
   }
   
   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
     buttonSize = getMaxButtonSize(w, h);
     paddingLeft = (w - buttonSize * buttonsCount.x - padding * (buttonsCount.x - 1)) / 2;
     paddingTop =  (h - buttonSize * buttonsCount.y - padding * (buttonsCount.y - 1)) / 2;
     
     invalidate();    
   }
   
   private void executeCurrentCommand() {
     /*KeyboardLayoutItem item = null;
     if (hasCandidateButton) {
       item = getItemForCandidate(candidateButtonX, candidateButtonY);
     } else if (shiftPressed) {
       item = getItemForCandidate(shiftCandidateX, shiftCandidateY);        
     } else {
       if (System.nanoTime() - gestureStartTime < singleGestureMaxTime * 1e6) {
         item = service.getCurrentLayout().getItemForButton(currentButtonX, currentButtonY, 1, 1);          
       }
     }      
     if (item != null) {
     } */
     if (repeatCandidate != null) {
       if (repeatCandidate.getCommand() == Command.CAPS_LOCK) {
         capsEnabled = !capsEnabled;
       } else {
         service.executeCommand(repeatCandidate, capsEnabled, shiftPressed);
       }      
     }
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
     boolean r = gd.onTouchEvent(event);
     if (!r && event.getAction() == MotionEvent.ACTION_UP) {
       executeCurrentCommand();      
       hasCurrentButton = false;
       hasCandidateButton = false;
       shiftPressed = false;
       hasShiftCandidate = false;
       repeatCounter = -1;
       invalidate();
       return true;
     }
     return r;
   }
   
   public void onLayoutChanged() {
     buttonsCount.set(service.getCurrentLayout().getWidth(), service.getCurrentLayout().getHeight());
     Log.i("", "buttonsCount=" + buttonsCount);
     invalidate();
     requestLayout();
   }
 
 
   @Override
   public boolean onDown(MotionEvent e) {
     Log.i("", "ondown!");
     if (!hasCurrentButton) {
       for(int i = 0; i < buttonsCount.x; i++) {
         for(int j = 0; j < buttonsCount.y; j++) {
           Rect rect = getButtonRect(i, j);
           if (rect.contains((int) e.getX(), (int) e.getY())) {
             hasCurrentButton = true;
             currentButtonX = i;
             currentButtonY = j;
             currentButtonPositionX = rect.left;
             currentButtonPositionY = rect.top;
            gestureStartTime = System.nanoTime();
             invalidate();
             return true;                            
           }                       
         }
       }
     }
     return false;
   }
 
 
   @Override
   public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
       float arg3) {
     return false;
   }
 
 
   @Override
   public void onLongPress(MotionEvent arg0) { }
 
 
   @Override
   public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float distanceX,
       float distanceY) {
     if (!hasCurrentButton) return false;
     currentButtonPositionX -= distanceX; 
     currentButtonPositionY -= distanceY; 
     double minDistanse = buttonSize * 100;
     Rect currentButtonDefault = getButtonRect(currentButtonX, currentButtonY);
     double selfDistanse = Math.pow(currentButtonDefault.left - currentButtonPositionX, 2) +
         Math.pow(currentButtonDefault.top - currentButtonPositionY, 2);
     KeyboardLayoutItem oldRepeatCandidate = repeatCandidate;
     if (Math.sqrt(selfDistanse) < buttonSize * 0.4) {
       hasCandidateButton = false;
       shiftPressed = hasShiftCandidate;
       repeatCandidate = hasShiftCandidate?
           getItemForCandidate(shiftCandidateX, shiftCandidateY): 
           service.getCurrentLayout().getItemForButton(currentButtonX, currentButtonY, 1, 1);
     } else if (Math.sqrt(selfDistanse) > buttonSize * 2) {
       shiftPressed = false;
       hasCandidateButton = false;
       hasShiftCandidate = false;
     } else {
       shiftPressed = false;
       hasCandidateButton = true;
       for(int i = currentButtonX - 1; i <= currentButtonX + 1; i++) {                     
         for(int j = currentButtonY - 1; j <= currentButtonY + 1; j++) {
           if (i < 0 || j < 0) continue;
           if (i >= buttonsCount.x || j >= buttonsCount.y) continue;
           if (i == currentButtonX && j == currentButtonY) continue;
           Rect rect = getButtonRect(i, j);
           double distanse = Math.pow(rect.left - currentButtonPositionX, 2) +
               Math.pow(rect.top - currentButtonPositionY, 2);
           //Log.i("onScroll", "distance = " + distanse);
           if (distanse < minDistanse) {
             minDistanse = distanse;
             candidateButtonX = i;
             candidateButtonY = j;
           }
 
         }
       }
       repeatCandidate = getItemForCandidate(candidateButtonX, candidateButtonY);
       
       boolean wasDiagonal = hasShiftCandidate && isCandidateDiagonal(shiftCandidateX, shiftCandidateY);
       boolean newDiagonal = isCandidateDiagonal(candidateButtonX, candidateButtonY);
       if (!wasDiagonal || newDiagonal) {
         shiftCandidateX = candidateButtonX;
         shiftCandidateY = candidateButtonY;
       }
       hasShiftCandidate = true;
 
     }
     invalidate();
     if (oldRepeatCandidate != repeatCandidate) {
       if (repeatCandidate == null) {
         repeatCounter = -1;
       } else {
         repeatCounter = repeatDelay;
       }
     }
     return true;
   }
 
 
   @Override
   public void onShowPress(MotionEvent arg0) {
   }
 
 
   @Override
   public boolean onSingleTapUp(MotionEvent arg0) {
     return false;
   }
     
 }
