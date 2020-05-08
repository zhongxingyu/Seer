 package jm.tex.lib.equationeditor.editorboard.objects;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import jm.tex.lib.equationeditor.LaTeXMenu.DataObject;
 import jm.tex.lib.equationeditor.editorboard.Calculator.DefaultFloatCalculator.EvaluateNumber;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class DrawMathObject extends MathObject {
 	public static class DrawMathSquareObject extends DrawMathObject{
 		
 		private int depth;
 		public DrawMathSquareObject(Context context, ViewGroup group, ParentInterface lineHeight,int depth) {
 			super(context, group, lineHeight);
			float size = 100;
 			if(depth==2)
 				size *= 0.5f;
			else if(depth>=3)
				size *= 0.2f;
 			
 			this.depth =depth;
 			inside = (int)size;
 			setMeasuredDimension(inside, inside);
 			variables.put("w", size);
 			variables.put("h", size);
 			AbstractMap<String, String> attributes = new TreeMap<String, String>();
 			if(depth==1)
 				attributes.put("d", "M (w-"+inside+")/2 h L (w-"+inside+")/2 h-"+inside+" L (w+"+inside+")/2 h-"+inside+" L (w+"+inside+")/2 h L (w-"+inside+")/2 h");
 			else
 				attributes.put("d", "L w 0 L w h L 0 h L 0 0");
 			DataObject square = new DataObject("path", attributes);
 			setChild(square);
 		}
 		@Override
 		protected void onMeasure(boolean recursive) {
 			MathObject parent = getParent();
 			if(depth==1 && parent!=null){
 				if(!parent.isLine() && parent.size==Size.WRAP_CONTENT){
 					ParentInterface line = getLineHeight();
 					if(line!=null){
 						int inside = line.getInside();
 						variables.put("w", (float)inside);
 						variables.put("h", (float)inside);
 						setMeasuredDimension(inside, inside);
 					}
 				}
 			}
 		}
 	}
 	
 	public interface CanvasDrawer{
 		public void drawGraphic(Canvas canvas,Paint paint,Map<String,Float> variables);
 	}
 	interface ReturnDrawer<T extends CanvasDrawer>{
 		void drawerCreated(T bmp);
 	}
 	public static class PathDrawer implements CanvasDrawer{
 		public abstract static class Command{
 			
 			public abstract void draw(Path path,Map<String, Float> variables);
 		}
 		public static class MoveToCommand extends Command{
 			EvaluateNumber x,y;
 			public MoveToCommand(String x,String y) {
 				this.x = new EvaluateNumber(x);
 				this.y = new EvaluateNumber(y);
 				
 			}
 			@Override
 			public void draw(Path path, Map<String, Float> variables) {
 				path.moveTo(x.evaluate(variables), y.evaluate(variables));
 			}
 		}
 		public static class LineToCommand extends Command{
 			EvaluateNumber x,y;
 			public LineToCommand(String x,String y) {
 				this.x = new EvaluateNumber(x);
 				this.y = new EvaluateNumber(y);
 			}
 			@Override
 			public void draw(Path path, Map<String, Float> variables) {
 				path.lineTo(x.evaluate(variables), y.evaluate(variables));
 			}
 			
 		}
 		public static class QuadToCommand extends Command{
 			EvaluateNumber x,y,x1,y1;
 			public QuadToCommand(String x1,String y1,String x,String y) {
 				this.x1 = new EvaluateNumber(x1);
 				this.y1 = new EvaluateNumber(y1);
 				this.x = new EvaluateNumber(x);
 				this.y = new EvaluateNumber(y);
 			}
 			@Override
 			public void draw(Path path, Map<String, Float> variables) {
 				path.quadTo(x1.evaluate(variables), y1.evaluate(variables), x.evaluate(variables), y.evaluate(variables));
 			}
 		}
 		public static CanvasDrawer createPath(String source){
 			PathDrawer path = new PathDrawer();
 			String tokens[] = source.split(" ");
 			
 			for(int i=0;i<tokens.length;++i){
 				Command command = null;
 				String token = tokens[i];
 				if(token.equals("M")){
 					command = new MoveToCommand(tokens[++i],tokens[++i]);
 				}else if(token.equals("L")){
 					command = new LineToCommand(tokens[++i],tokens[++i]);
 				}else if(token.equals("Q")){
 					command = new QuadToCommand(tokens[++i],tokens[++i],tokens[++i],tokens[++i]);					
 				}
 				
 				
 				if(command!=null)
 					path.addCommand(command);
 			}
 			
 			return path;
 		}
 		
 		private List<Command> commands = new ArrayList<Command>();
 		
 		public void addCommand(Command command){
 			commands.add(command);
 		}
 		public void drawGraphic(Canvas canvas,Paint paint,Map<String,Float> variables){
 			Path path = new Path();
 			for(Command command:commands){
 				command.draw(path, variables);
 			}
 			canvas.drawPath(path, paint);
 			
 		}
 	}
 	
 	public static class CoordsObject{
 		private MathObject object;
 		private EvaluateNumber x,y;
 		private int rX,rY;
 		private String id;
 		private boolean base;
 		public CoordsObject(MathObject object,String id,EvaluateNumber x,EvaluateNumber y) {
 			this.object = object;
 			this.x  = x;
 			this.y = y;
 			this.id = id;
 			if(id!=null)
 				this.base = id.equals("base");
 		}
 		public boolean isBase(){
 			return base;
 		}
 		public String getId(){
 			return id;
 		}
 		public MathObject getObject() {
 			return object;
 		}
 		public int getY() {
 			return rY;
 		}
 		public int getX() {
 			return rX;
 		}
 		public void evaluateCoords(TreeMap<String, Float> variables) {
 			rX = (int)x.evaluate(variables);
 			rY = (int)y.evaluate(variables);
 		}
 		public CoordsObject cloneShell(ViewGroup group,ParentInterface lineHeight){
 			return new CoordsObject(object.cloneShell(group,lineHeight), id, x, y);
 		}
 	}
 	private List<CanvasDrawer> paths = new ArrayList<CanvasDrawer>();
 	protected TreeMap<String, Float> variables = new TreeMap<String, Float>();
 	private List<CoordsObject> children = new ArrayList<DrawMathObject.CoordsObject>();
 	private Paint paint = new Paint();
 	private EvaluateNumber plusWidth,plusHeight,baseY;
 	private View view;
 	
 	public DrawMathObject(Context context,ViewGroup group,ParentInterface lineHeight) {
 		super(context,group,lineHeight);
 		init(context);
 	}
 	public DrawMathObject(Context context, MathLine line) {
 		super(context, line);
 		init(context);
 	}
 	private void init(Context context){
 		view = new View(context){
 			@Override
 			protected void onDraw(Canvas canvas) {
 
 				canvas.translate(1, 1);
 				for(CanvasDrawer pathDrawer:paths){
 					pathDrawer.drawGraphic(canvas,paint,variables);
 				}
 			
 				canvas.translate(-1, -1);
 				
 				super.onDraw(canvas);
 			}
 			@Override
 			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 				setMeasuredDimension(DrawMathObject.this.getMeasuredWidth()+2, DrawMathObject.this.getMeasuredHeight()+2);
 			}
 		};
 		paint.setAntiAlias(true);
 		paint.setStyle(Style.STROKE);
 		paint.setColor(Color.BLACK);
 		
 		getParentView().addView(view);
 		
 	}
 /*
 	@Override
 	public void onDraw(Canvas canvas) {
 		
 		canvas.translate(left, top);
 		for(CanvasDrawer pathDrawer:paths){
 			pathDrawer.drawGraphic(canvas,paint,variables);
 		}
 	
 		canvas.translate(-left, -top);
 		
 		super.onDraw(canvas);
 	}*/
 	@Override
 	protected void onMeasure(boolean recursive) {
 		ParentInterface line = getLineHeight();
 		int height,width;
 		int minLeft = Integer.MAX_VALUE;
 		int maxRight = Integer.MIN_VALUE;
 		switch(size){
 		case FILL_PARENT:
 			//Vyplňuje celé (je to FILL_PARENT) tedy overTop a overBottom jsou stejné jako na getLine(), ale může být nastaven outterTop nebo outterBottom (což ovlivňuje getLine())
 			overTop = line.getOverTopLine();
 			overBottom = line.getOverBottomLine();
 			inside = line.getInside();
 			
 			height = overTop+overBottom+inside;
 			variables.put("h", (float) height);
 
 			if(weightw!=0){
 				width = (int)(height*weightw);
 			}else{
 				minLeft = Integer.MAX_VALUE;
 				maxRight = Integer.MIN_VALUE;
 				for(CoordsObject o:children){
 					MathObject object = o.object;
 					if(recursive)
 						object.measure(true);
 					int w = object.getMeasuredWidth();
 					int x = o.getX();
 	
 					if(x<minLeft)
 						minLeft = x;
 					int right = x+w;
 					if(right>maxRight)
 						maxRight = right;
 				}
 				width = maxRight-minLeft;
 			}
 			variables.put("w", (float) width);
 
 			onMeasureExtra(recursive, width, height);
 			break;
 		case WRAP_CONTENT:
 			minLeft = Integer.MAX_VALUE;
 			int minTop = Integer.MAX_VALUE;
 			maxRight = Integer.MIN_VALUE;
 			int maxBottom = Integer.MIN_VALUE;
 			inside = line.getInside();
 			variables.put("lH", (float)inside);
 			
 			
 			//Nejdříve si poukládám rozměry všech vnitřních objektů abych potom pomocí rozměrů mohl dopočítat správně souřadnice všech
 			for(CoordsObject o:children){
 				MathObject object = o.object;
 				if(recursive)
 					object.measure(true);
 				int w = object.getMeasuredWidth();
 				int h = object.getMeasuredHeight();
 				
 				String id = o.getId();
 				if(id!=null){
 					variables.put(id+".h", (float) h);
 					variables.put(id+".w", (float) w);
 				}
 			}
 			//Počítám souřadnice pomoci již známých rozměrů. A zjistím celkovou šířku a výšku, pomocí horního, spodního, levého a pravého okraje.
 			for(CoordsObject o:children){
 				MathObject object = o.object;
 				int w = object.getMeasuredWidth();
 				int h = object.getMeasuredHeight();
 
 				o.evaluateCoords(variables);
 				int x = o.getX();
 				int y = o.getY();
 				
 				if(x<minLeft)
 					minLeft = x;
 				if(y<minTop)
 					minTop = y;
 				int right = x+w;
 				if(right>maxRight)
 					maxRight = right;
 				int bottom = y+h;
 				if(bottom>maxBottom)
 					maxBottom = bottom;
 				
 				if(o.isBase()){
 					overBottom = object.getOverBottom();
 				}
 			}
 			int w = maxRight-minLeft;
 			int h = maxBottom-minTop;
 
 			variables.put("w", (float) w);
 			variables.put("h", (float) h);
 			float widthF = w;
 			float heightF = h;
 			if(plusWidth!=null)
 				widthF += plusWidth.evaluate(variables);
 			if(plusHeight!=null)
 				heightF += plusHeight.evaluate(variables);
 			variables.put("pw", widthF);
 			variables.put("ph", heightF);
 
 			if(baseY!=null)
 				overBottom = (int) (heightF-baseY.evaluate(variables));
 			
 			overTop = (int)heightF-inside-overBottom;
 
 			onMeasureExtra(recursive, (int)widthF,(int)heightF);
 			break;
 		}
 	}
 	@Override
 	public View getView() {
 		return view;
 	}
 	@Override
 	public void remove() {
 		getParentView().removeView(view);
 		super.remove();
 	}
 	
 	@Override
 	protected void onLayout( int left, int top, int right, int bottom,boolean recursive) {
 		for (CoordsObject o : this.children) {
 			int x = o.getX();
 			int y = o.getY();
 			int topS = top+y+outterTop;
 			
 			
 			if (recursive) {
 				o.getObject().layout(left + x+overLeft, topS, overLeft+left + x + o.getObject().getMeasuredWidth(), topS + o.getObject().getMeasuredHeight());
 			} else {
 				o.getObject().setBounds(left + x+overLeft, topS, overLeft+left + x + o.getObject().getMeasuredWidth(), topS + o.getObject().getMeasuredHeight());
 			}
 		}
 		view.layout(left-1+overLeft, top+outterTop-1, right+1-overRight, bottom+1);
 		int t = isAddonsFullHeight()?0:overTop;//Když má nastaveno, že addons se mají přidávat k velikosti včetně overTop, tak musím nastavit ty p
 		onLayoutExtra(left+overLeft, top+outterTop+t, overLeft+left+innerWidth, top+outterTop+overTop+inside, recursive, null);
 		view.invalidate();
 	//	getLine().getBoard().invalidate(left-1, top-1, right+1, bottom+1);
 		
 	}
 	@Override
 	public void putBehind(MathObject insert,DataObject data) {
 		addView(insert);
 		String xs = data.getAttribute("x");
 		String ys = data.getAttribute("y");
 		EvaluateNumber x = null,y =null;
 		if(xs!=null)
 			x = new EvaluateNumber(xs);
 		if(ys!=null)
 			y = new EvaluateNumber(ys);
 		
 		String id = data.getAttribute("id");
 		children.add(new CoordsObject(insert,id, x==null?EvaluateNumber.Number0:x, y==null?EvaluateNumber.Number0:y));
 	}
 
 	@Override
 	public int setAttribute(DataObject child) {
 		String pW = child.getAttribute("plusWidth");
 		String pH = child.getAttribute("plusHeight");
 		if(pW!=null)
 			plusWidth = new EvaluateNumber(pW);
 		if(pH!=null)
 			plusHeight = new EvaluateNumber(pH);
 		String bY = child.getAttribute("base");
 		if(bY!=null)
 			baseY = new EvaluateNumber(bY);
 		
 		
 		return super.setAttribute(child);
 		
 	}
 	@Override
 	public MathObject setChild(DataObject child) {
 		if(child.getName().equals("picture")){
 			String source = child.getAttribute("source");
 			String name = child.getAttribute("name");
 			if(source!=null && name!=null){
 				PictureMathObject pic = new PictureMathObject(getContext(), getLine(),name,source);
 				putBehind(pic, child);
 				return pic;
 			}
 		}else if(child.getName().equals("path")){
 			String d = child.getAttribute("d");
 			if(d!=null)
 				paths.add(PathDrawer.createPath(d));
 			
 			return null;
 		}
 		return super.setChild(child);
 	}
 
 	@Override
 	public MathObject cloneShell(ViewGroup group,ParentInterface lineHeight) {
 		DrawMathObject object = new DrawMathObject(group.getContext(), group,lineHeight);
 		//Počítám s tím, že se dynamicky nebudou měnit
 		object.weightw = weightw;
 		object.paths = paths;
 		object.plusHeight = plusHeight;
 		object.plusWidth = plusWidth;
 		
 		for(CoordsObject child:children){
 			CoordsObject c = child.cloneShell(group,lineHeight);
 			//c.getObject().setParent(object,true);
 			object.addView(c.getObject());
 			object.children.add(c);
 		}
 		cloneShell(object);
 		return object;
 	}
 
 
 
 }
