 package no.kantega.android.afp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.*;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import no.kantega.android.afp.controllers.Transactions;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * This activity displays a fine pie chart for the given month and year
  */
 public class PieChartActivity extends Activity {
 
     private final List<PieItem> PieData = new ArrayList<PieItem>();
     private Transactions db;
     private Cursor cursor;
     private int maxCount = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.pie);
         String month = getIntent().getExtras().getString("month");
         String year = getIntent().getExtras().getString("year");
         this.db = new Transactions(getApplicationContext());
         this.cursor = db.getCursorTags(month, year);
         createPieDataFromCursor();
         Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         int displayWidth = display.getWidth();
         int size = displayWidth;
         int bgColor = 0xffa1a1a1;
        Bitmap backgroundImage = Bitmap.createBitmap(size, size + 250, Bitmap.Config.RGB_565);
         PieChart pieChart = new PieChart(this);
         pieChart.setLayoutParams(new ViewGroup.LayoutParams(size, size));
         pieChart.setGeometry(size, size, 5, 5, 5, 5);
         pieChart.setBgColor(bgColor);
         pieChart.setData(PieData, maxCount);
         pieChart.invalidate();
         pieChart.draw(new Canvas(backgroundImage));
         ImageView imageView = new ImageView(this);
         imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         imageView.setBackgroundColor(bgColor);
         imageView.setImageBitmap(backgroundImage);
         LinearLayout targetPieView = (LinearLayout) findViewById(R.id.pie_container);
         targetPieView.addView(imageView);
     }
 
     /**
      * Create a pie char from the cursor
      */
     private void createPieDataFromCursor() {
         PieItem item;
         Random numGen = new Random();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             item = new PieItem();
             item.setCount((int) cursor.getDouble(cursor.getColumnIndex("sum")));
             String tag = cursor.getString(cursor.getColumnIndex("tag"));
             item.setLabel(tag);
             item.setColor(0xff000000 + 256 * 256 * numGen.nextInt(256) + 256 * numGen.nextInt(256) + numGen.nextInt(256));
             PieData.add(item);
             maxCount += item.getCount();
             cursor.moveToNext();
         }
         cursor.close();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         db.closeCursor(cursor);
         db.close();
     }
 
     /**
      * A custom pie chart view
      */
     private class PieChart extends View {
 
         private static final int WAIT = 0;
         private static final int IS_READY_TO_DRAW = 1;
         private static final int IS_DRAW = 2;
         private static final float START_INC = 30;
         private final Paint bgPaints = new Paint();
         private final Paint linePaints = new Paint();
         private final Paint textPaints = new Paint();
         private String tag = null;
         private int width;
         private int height;
         private int gapLeft;
         private int gapRight;
         private int gapTop;
         private int gapBottom;
         private int bgColor;
         private int DESCRIPTION_MARGIN_LEFT = 75;
         private int DESCRIPTION_MARGIN_TOP = 50;
         private final int COLUMN_MARGIN_WIDTH = 175;
         private final int COLUMN_MARGIN_HEIGHT = 40;
         private final int DESCRIPTION_CIRCLE_RADIUS = 10;
         private final int CIRCLE_MARGIN_LEFT = 15;
         private int state = WAIT;
         private float start;
         private float sweep;
         private int maxConnection;
         private List<PieItem> dataArray;
 
         /**
          * Create a pie chart in the given context
          *
          * @param context Application context
          */
         public PieChart(Context context) {
             super(context);
         }
 
         @Override
         protected void onDraw(Canvas canvas) {
             super.onDraw(canvas);
             if (state != IS_READY_TO_DRAW) return;
             canvas.drawColor(bgColor);
             bgPaints.setAntiAlias(true);
             bgPaints.setStyle(Paint.Style.FILL);
             bgPaints.setColor(0x88FF0000);
             bgPaints.setStrokeWidth(0.5f);
             linePaints.setAntiAlias(true);
             linePaints.setStyle(Paint.Style.STROKE);
             linePaints.setColor(0xff000000);
             linePaints.setStrokeWidth(0.5f);
             linePaints.setTextSize(15);
             textPaints.setAntiAlias(true);
             textPaints.setStyle(Paint.Style.FILL_AND_STROKE);
             textPaints.setStrokeWidth(0.5f);
             textPaints.setColor(Color.BLACK);
             textPaints.setTextSize(20);
             RectF ovals = new RectF(gapLeft, gapTop, width - gapRight, height - gapBottom);
             start = START_INC;
             float lblX;
             float lblY;
             String LblPercent;
             float Percent;
             DecimalFormat FloatFormatter = new DecimalFormat("0.## %");
             float CenterOffset = (width / 2); // Pie Center from Top-Left origin
             float Conv = (float) (2 * Math.PI / 360);     // Constant for convert Degree to rad.
             float Radius = 2 * (width / 2) / 3;     // Radius of the circle will be drawn the legend.
             Rect bounds = new Rect();
             PieItem item;
             for (int i = 0; i < dataArray.size(); i++) {
                 item = dataArray.get(i);
                 bgPaints.setColor(item.getColor());
                 sweep = (float) 360 * ((float) item.getCount() / (float) maxConnection);
                 canvas.drawArc(ovals, start, sweep, true, bgPaints);
                 canvas.drawArc(ovals, start, sweep, true, linePaints);
                 Percent = (float) item.getCount() / (float) maxConnection;
                 sweep = (float) 360 * Percent;
                 // Format Label
                 LblPercent = FloatFormatter.format(Percent);
                 // Get Label width and height in pixels
                 linePaints.getTextBounds(LblPercent, 0, LblPercent.length(), bounds);
                 // Claculate final coords for Label
                 lblX = (float) ((float) CenterOffset + Radius * Math.cos(Conv * (start + sweep / 2))) - bounds.width() / 2;
                 lblY = (float) ((float) CenterOffset + Radius * Math.sin(Conv * (start + sweep / 2))) + bounds.height() / 2;
                 // Dwraw Label on Canvas
                 canvas.drawText(LblPercent, lblX, lblY, textPaints);
                 canvas.drawCircle(DESCRIPTION_MARGIN_LEFT, height + DESCRIPTION_MARGIN_TOP, DESCRIPTION_CIRCLE_RADIUS, bgPaints);
                 tag = item.getLabel();
                 if (tag == null) {
                     tag = getResources().getString(R.string.not_tagged);
                 }
                 if (i % 2 == 0) {
                     canvas.drawText(tag, DESCRIPTION_MARGIN_LEFT + CIRCLE_MARGIN_LEFT, height + DESCRIPTION_MARGIN_TOP, textPaints);
                     DESCRIPTION_MARGIN_LEFT += COLUMN_MARGIN_WIDTH;
                 } else {
                     canvas.drawText(tag, DESCRIPTION_MARGIN_LEFT + CIRCLE_MARGIN_LEFT, height + DESCRIPTION_MARGIN_TOP, textPaints);
                     DESCRIPTION_MARGIN_TOP += COLUMN_MARGIN_HEIGHT;
                     DESCRIPTION_MARGIN_LEFT -= COLUMN_MARGIN_WIDTH;
                 }
                 start += sweep;
             }
 
             state = IS_DRAW;
         }
 
         /**
          * Set geometry
          *
          * @param width     Width
          * @param height    Height
          * @param gapLeft   Gap left
          * @param gapRight  Gap right
          * @param gapTop    Gap top
          * @param gapBottom Gap bottom
          */
         public void setGeometry(int width, int height, int gapLeft, int gapRight, int gapTop, int gapBottom) {
             this.width = width;
             this.height = height;
             this.gapLeft = gapLeft;
             this.gapRight = gapRight;
             this.gapTop = gapTop;
             this.gapBottom = gapBottom;
         }
 
         /**
          * Set background color
          *
          * @param bgColor The background color
          */
         public void setBgColor(int bgColor) {
             this.bgColor = bgColor;
         }
 
         /**
          * Set data
          *
          * @param data          Pie items
          * @param maxConnection Sum of values
          */
         public void setData(List<PieItem> data, int maxConnection) {
             dataArray = data;
             this.maxConnection = maxConnection;
             state = IS_READY_TO_DRAW;
         }
     }
 
     /**
      * Represents a single pie ite
      */
     private class PieItem {
 
         private int count;
         private String label;
         private int color;
 
         /**
          * Get item label
          *
          * @return The label
          */
         public String getLabel() {
             return label;
         }
 
         /**
          * Set item label
          *
          * @param label The label to set
          */
         public void setLabel(String label) {
             this.label = label;
         }
 
         /**
          * Get color
          *
          * @return The color
          */
         public int getColor() {
             return color;
         }
 
         /**
          * Set color
          *
          * @param color The color to set
          */
         public void setColor(int color) {
             this.color = color;
         }
 
         /**
          * Get count
          *
          * @return The count
          */
         public int getCount() {
             return count;
         }
 
         /**
          * Set count
          *
          * @param count The count to set
          */
         public void setCount(int count) {
             this.count = count;
         }
     }
 }
