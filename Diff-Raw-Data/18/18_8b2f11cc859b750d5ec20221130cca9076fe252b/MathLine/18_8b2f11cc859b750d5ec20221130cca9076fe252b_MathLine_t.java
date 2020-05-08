 package jm.tex.lib.equationeditor.editorboard.objects;
 
 import java.util.Collections;
 
 import jm.tex.lib.equationeditor.LaTeXMenu.DataObject;
 import jm.tex.lib.equationeditor.editorboard.EditorBoard;
 import android.content.Context;
 import android.view.ViewGroup;
 
 public class MathLine extends MathObject implements ParentInterface{
 	private MathObject begin,end;
 	private EditorBoard board;
 	private int depth;
 	private int lastOverTop,lastOverBottom,lastOutterTop,lastOutterBottom;
 	public MathLine(Context context,EditorBoard board,MathLine line,int depth) {
 		super(context,line);
 		this.depth = depth;
 		this.board = board;
 	}
 	public void addBegin(){
 		addBegin(false);
 	}
 	public void addBegin(boolean dontInvalidate){
 		if(begin==null){
 			end = begin = new EditTextMathObject(getContext(),this);
 			addView(begin,dontInvalidate);
 		}
 		//begin.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		//minLineHeight = begin.getMeasuredHeight();		
 	}
 	@Override
	public void addView(MathObject object,boolean dontInvalidate){
 
 		children.add(object);
 		Collections.sort(children);
		object.setParent(this,dontInvalidate);
 	}
 	@Override
 	public boolean isLine() {
 		return true;
 	}
 	public int getDepth(){
 		return depth;
 	}
 	public MathObject getEnd(){
 		return end;
 	}
 	public MathObject getBegin(){
 		return begin;
 	}
 	public EditorBoard getBoard(){
 		return board;
 	}
 	//Řádky sjednocují over a outter parametry do over parametru
 	@Override
 	public int getOverTop() {
 		return overTop+outterTop;
 	}
 	//Řádky sjednocují over a outter parametry do over parametru
 	@Override
 	public int getOverBottom() {
 		return overBottom+outterBottom;
 	}
 	@Override
 	public int getOutterBottom() {
 		return 0;
 	}
 	@Override
 	public int getOutterTop() {
 		return 0;
 	}
 	@Override
 	public int getOverTopLine() {
 		return overTop;
 	}
 	@Override
 	public int getOverBottomLine() {
 		return overBottom;
 	}
 	public void setBegin(MathObject newBegin) {
 		if(newBegin!=null){
 			if(begin!=null){
 				begin.setPrevious(newBegin);
 				newBegin.setNext(begin);
 			}
 			begin = newBegin;
 		}else if(newBegin==null){
 			begin = null;
 		}
 	}
 	public void putBehind(MathObject insert,MathObject behind){
 		if (behind == null) {
 			setBegin(insert);
 		} else {
 			MathObject next = behind.getNext();
 			if (next == null) {
 				end = insert;
 			} else {
 				next.setPrevious(insert);
 				insert.setNext(next);
 			}
 			behind.setNext(insert);
 			insert.setPrevious(behind);
 		}
 	}
 	@Override
 	protected void onMeasure(boolean recursive) {
 		lastOverTop = this.overTop;	
 		lastOverBottom = this.overBottom;
 		lastOutterTop = this.outterTop;
 		lastOutterBottom = this.outterBottom;
 		int width = 0;
 		overTop = 0;
 		overBottom = 0;
 		inside = 0;
 		outterTop = 0;
 		outterBottom = 0;
 		for(MathObject child:this.children){
 			if(recursive)
 				child.measure(true);
 			else if(child.size==Size.FILL_PARENT)
 				child.measure(true);
 			width += child.getMeasuredWidth();
 			inside = Math.max(inside, child.inside);
 
 			overTop = Math.max(overTop, child.getOverTop());
 			overBottom = Math.max(overBottom, child.getOverBottom());
 			outterTop = Math.max(outterTop, child.getOutterTop());
 			outterBottom = Math.max(outterBottom, child.getOutterBottom());
 		}
 		setMeasuredDimension(width, inside+overTop+overBottom+outterTop+outterBottom);
 	}
 	@Override
 	protected void layoutChild(MathObject mathObject) {
 
 		int lineHeight = getMeasuredHeight();
 		if(lastOverTop!=overTop || lastOverBottom!=overBottom || lastOutterTop!=outterTop || lastOutterBottom!=outterBottom/* || lastInside!=inside*/){
 			int objectWidth = 0;
 			MathObject object = getBegin();
 			while(object!=null){
 				
 				int nWidth = objectWidth+object.getMeasuredWidth();
 				if(object!=mathObject)
 					object.layout(left+objectWidth, top+overTop-object.getOverTop()+outterTop-object.getOutterTop(), left+nWidth, top+lineHeight);
 				else
 					object.setBounds(left+objectWidth, top+overTop-object.getOverTop()+outterTop-object.getOutterTop(), left+nWidth, top+lineHeight);
 				objectWidth = nWidth;
 				
 				object = object.getNext();				
 			}
 		}else{
 			MathObject prev = mathObject.getPrevious();
 			MathObject next = mathObject.getNext();
 			int startX,endX;
 			if(prev!=null)
 				startX = prev.getRight();
 			else{
 				startX = getLeft();
 			}
 			int width = mathObject.getMeasuredWidth();
 			int lastWidth = mathObject.getLastWidth();
 			
 			endX = startX+width;
 
 			
 			if(width!=lastWidth){
 				int s = endX;
 				while(next!=null){
 					next.left = s;
 					s += next.getMeasuredWidth();
 					next.right = s;
 					next.layout(true);
 					next = next.getNext();
 				}
 			}
 			mathObject.setBounds(startX, top+overTop-mathObject.getOverTop()+outterTop-mathObject.getOutterTop(), endX, top+getMeasuredHeight());
 		}
 		/*
 		if(!heightUpdate){
 			int e = endX;
 			for(int i=children.size()-1;i>=0;--i){
 				MathObject o = children.get(i);
 				if(o.getSize()!=Size.FILL_PARENT)
 					break;
 				children.get(i).layout(,e,top+lineHeight);
 			}
 		}*/
 	}
 	@Override
 	protected boolean measureChild(MathObject mathObject) {
 		int oldHeight = getMeasuredHeight();
 		int oldObjectWidth = mathObject.getLastWidth();
 	//	mathObject.measure();
 		lastOverTop = this.overTop;	
 		lastOverBottom = this.overBottom;
 		lastOutterTop = this.outterTop;
 		lastOutterBottom = this.outterBottom;
 		int ot = mathObject.getOverTop();
 		int ob = mathObject.getOverBottom();
 		int in = mathObject.getInside();
 		int outt = mathObject.getOutterTop();
 		int outb = mathObject.getOutterBottom();
 		
 		overTop = overTop<ot?ot:overTop;
 		overBottom = overBottom<ob?ob:overBottom;
 		inside = inside<in?in:inside;
 		outterBottom = outterBottom<outb?outb:outterBottom;
 		outterTop = outterTop<outt?outt:outterTop;
 		
 		int newHeight = inside+overTop+overBottom+outterTop+outterBottom;
 		
 		int delta = mathObject.getMeasuredWidth()-oldObjectWidth;
 		if(delta!=0 || newHeight!=oldHeight){
 			if(newHeight!=oldHeight){
 				for(int i=children.size()-1;i>=0;--i){
 					MathObject o = children.get(i);
 					if(o.getSize()!=Size.FILL_PARENT)//Objekty s nastaveným FILL_PARENT můžou změnit šířku, když se změní výška řádku=> pole children je seřazené podle size, takže nejdříve prochází objekty s FILL_PARENT a jakmile narazí na první jiný, tak vím, že už tam žádný FILL_PARENT není
 						break;
 					if(o!=mathObject){
 						int w = o.getMeasuredWidth();
 						o.measure(true);
 						delta += o.getMeasuredWidth()-w;
 					}
 				}
 			}
 			setMeasuredDimension(getMeasuredWidth()+delta, newHeight);
 			return true;
 		}else{
 			//FIXME Tohle by tady asi úplně nemělo být, ale funguje to s tím. Když se změní overTop objektu, ale zároveň se nezmění overTop řádku a zároveň se nezmění jinak výška objektu ani šířka tak neprojde vyšším layoutem, tak je potom objekt posunutý níže až do dalšího updatu.
 			mathObject.top = top+overTop-mathObject.getOverTop()+outterTop-mathObject.getOutterTop();
 		}
 		return false;
 	}
 	@Override
 	protected void onLayout(int left, int top, int right, int bottom,boolean children) {
 		//if(children){
 	//	if(changed){
 			int width = 0;
 			int height = getMeasuredHeight();
 			MathObject object = getBegin();
 			if(children){
 				while(object!=null){
 					int nWidth = width+object.getMeasuredWidth();
 					object.layout(left+width, top+overTop-object.getOverTop()+outterTop-object.getOutterTop(), left+nWidth, top+height);
 					width = nWidth;
 					
 					object = object.getNext();
 				}
 			}else{
 				while(object!=null){
 					int nWidth = width+object.getMeasuredWidth();
 					object.setBounds(left+width, top+overTop-object.getOverTop()+outterTop-object.getOutterTop(), left+nWidth, top+height);
 					width = nWidth;
 					
 					object = object.getNext();
 				}
 				
 			}
 			
 			
 		//}
 		//}
 		
 	}
 }
