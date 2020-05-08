 package org.umece.android.umaine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class ScheduleDrawable extends View {
 	Context mContext;
 	private static final int start_time = 8;
 	private static final int end_time = 20;
 	private Color[] m;
 	private Color[] t;
 	private Color[] w;
 	private Color[] h;
 	private Color[] f;
 	private Semester semester;
 	private List<Rect> rect_queue;
 	private List<Color> color_queue;
 	public int x, y;
 	public double each_width;
 	private HashMap<Course, Color> colors;
 	
 	public ScheduleDrawable(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 		this.semester = null;
 		m = new Color[(end_time - start_time) * 2];
 		t = new Color[(end_time - start_time) * 2];
 		w = new Color[(end_time - start_time) * 2];
 		h = new Color[(end_time - start_time) * 2];
 		f = new Color[(end_time - start_time) * 2];
 		
 		rect_queue = new ArrayList<Rect>();
 		color_queue = new ArrayList<Color>();
 	}
 	
 	public ScheduleDrawable(Context context) {
 		this(context, (Semester)null);
 	}
 
 	public ScheduleDrawable(Context context, Semester sem) {
 		super(context);
 		mContext = context;
 		this.semester = sem;
 		m = new Color[(end_time - start_time) * 2];
 		t = new Color[(end_time - start_time) * 2];
 		w = new Color[(end_time - start_time) * 2];
 		h = new Color[(end_time - start_time) * 2];
 		f = new Color[(end_time - start_time) * 2];
 		
 		rect_queue = new ArrayList<Rect>();
 		color_queue = new ArrayList<Color>();
 	}
 	
 	public void onChange() {
 		clearArrays();
 		fillArray();
 		drawSchedule();
 		
 		invalidate();
 	}
 	
 	public int getQueueSize() {
 		if (rect_queue.size() != color_queue.size()) {
 			throw new RuntimeException();
 		}
 
 		return rect_queue.size();
 	}
 	
 	private void drawSchedule() {
 		rect_queue.clear();
 		color_queue.clear();
 		int start_rect = (int)(.98 * x);
 		int end_rect = (int)(.05 * x);
 		Rect m_rect = new Rect(start_rect, (int)(y * .15), end_rect, (int)(y * .32));
 		Rect t_rect = new Rect(start_rect, (int)(y * .33), end_rect, (int)(y * .50));
 		Rect w_rect = new Rect(start_rect, (int)(y * .51), end_rect, (int)(y * .68));
 		Rect h_rect = new Rect(start_rect, (int)(y * .69), end_rect, (int)(y * .84));
 		Rect f_rect = new Rect(start_rect, (int)(y * .85), end_rect, (int)(y * 1.00));
 		Color white = Color.getColor("GRAY");
 		
 		rect_queue.add(m_rect);
 		rect_queue.add(t_rect);
 		rect_queue.add(w_rect);
 		rect_queue.add(h_rect);
 		rect_queue.add(f_rect);
 		color_queue.add(white);
 		color_queue.add(white);
 		color_queue.add(white);
 		color_queue.add(white);
 		color_queue.add(white);
 		
 		addRects(m_rect, m);
 		addRects(t_rect, t);
 		addRects(w_rect, w);
 		addRects(h_rect, h);
 		addRects(f_rect, f);
 	}
 
 	private void addRects(Rect mRect, Color[] colors) {
 		int rect_width = mRect.left - mRect.right;
 		each_width = ((double)rect_width) / colors.length;
 		int i;
 		double left = mRect.right;
 		
 		for (i = 0; i < colors.length; i++) {
 			rect_queue.add(new Rect(
 					(int)(left + 1),
 					mRect.top + 1,
 					(int)(left + each_width - 1),
 					mRect.bottom - 1));
 			left += each_width;
 			color_queue.add(colors[i]);
 		}
 	}
 	
 	public Color getColor(Course course) {
 		return colors.get(course);
 	}
 
 	private void fillArray() {
 		colors = new HashMap<Course, Color>();
 		int i = Color.getColor("RED").getId();
 		
 		if (semester == null) return;
 		
 		for (Course course : semester.getCourses()) {
 			course.setColor(Color.getColor(i));
 			colors.put(course, Color.getColor(i++));
 			if (i == (Color.getMaxId() + 1)) i = Color.getColor("RED").getId();
 		}
 		
 		for (Course course : semester.getCourses()) {
 			int start = parseTime(course.getMeetingTime().split(" ")[1], false);
 			int end = parseTime(course.getMeetingTime().split(" ")[3], true);
 			
 			while ((start > 0) &&
 					(start < m.length) &&
 					(start <= end)) {
 				if (course.getMeetingTime().split(" ")[0].contains("Mo")) {
 					m[start] = colors.get(course);
 				}
 				if (course.getMeetingTime().split(" ")[0].contains("Tu")) {
 					t[start] = colors.get(course);
 				}
 				if (course.getMeetingTime().split(" ")[0].contains("We")) {
 					w[start] = colors.get(course);
 				}
 				if (course.getMeetingTime().split(" ")[0].contains("Th")) {
 					h[start] = colors.get(course);
 				}
 				if (course.getMeetingTime().split(" ")[0].contains("Fr")) {
 					f[start] = colors.get(course);
 				}
 				
 				start++;
 			}
 		}
 	}
 	
 	private int parseTime(String string, boolean last) {
 		boolean pm = false;
 		
 		if (string.contains("PM")) {
 			pm = true;
 		}
 		
 		String time = string;//.split(" ")[0];
 		int val = 2 * Integer.parseInt(time.split(":")[0]);
 		if (pm && (val != 24)) {
 			val += 24;
 		} else if (!pm && (val == 1200)) {
 			val = 0;
 		}
 		
 		val += (Integer.parseInt(time.split(":")[1].replaceAll("AM", "").replaceAll("PM", "")) > 30)?1:0;
 		if (last && (Integer.parseInt(time.split(":")[1].replaceAll("AM", "").replaceAll("PM", "")) == 0)) {
 			val--;
 		}
 		val -= 16;
 	
 		return val;
 	}
 
 	private void clearArrays() {
 		int i;
 		for (i = 0; i < m.length; i++) {
 			m[i] = Color.getColor("BLACK");
 			t[i] = Color.getColor("BLACK");
 			w[i] = Color.getColor("BLACK");
 			h[i] = Color.getColor("BLACK");
 			f[i] = Color.getColor("BLACK");
 		}
 	}
 
 	public void setSemester(Semester sem) {
 		semester = sem;
 		if (semester != null) {
 			onChange();
 		}
 	}
 	
 	public Semester getSemester() {
 		return semester;
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		x = MeasureSpec.getSize(widthMeasureSpec);
 		y = MeasureSpec.getSize(heightMeasureSpec);
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		onChange();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		drawQueue(canvas);
 //		Paint paint = new Paint();
 //		paint.setStrokeWidth(3);
 //		paint.setColor(Color.getColor("WHITE").getColor());
 //		paint.setTextSize(20);
 //		String text = "M";
 //		float[] pos = new float[] { (float) (.005*x), (float) (.25*y) };
 //		canvas.drawPosText(text, pos, paint);
 //		text = "T";
 //		pos = new float[] { (float) (.01*x), (float) (.43*y) };
 //		canvas.drawPosText(text, pos, paint);
 //		text = "W";
 //		pos = new float[] { (float) (.005*x), (float) (.61*y) };
 //		canvas.drawPosText(text, pos, paint);
 //		text = "H";
 //		pos = new float[] { (float) (.01*x), (float) (.78*y) };
 //		canvas.drawPosText(text, pos, paint);
 //		text = "F";
 //		pos = new float[] { (float) (.01*x), (float) (.95*y) };
 //		canvas.drawPosText(text, pos, paint);
 		
 	}
 
 	private void drawQueue(Canvas canvas) {
 		int i;
 		
 		if (rect_queue.size() != color_queue.size()) {
 			throw new RuntimeException();
 		}
 		
 		for (i = 0; i < rect_queue.size(); i++) {
 			drawRect(rect_queue.get(i), color_queue.get(i), canvas);
 		}
 	}
 
 	private void drawRect(Rect rect, Color color, Canvas canvas) {
 		Paint paint = new Paint();
 		paint.setStrokeWidth(3);
 		paint.setColor(color.getColor());
 		
 		canvas.drawRect(rect, paint);
 	}
 
 	public int getSize() {
 		return colors.size();
 	}
 
 }
