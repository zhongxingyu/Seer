 package com.inspedio.samples;
 
 import com.inspedio.actions.MoveBy;
 import com.inspedio.actions.Sequence;
 import com.inspedio.entity.InsAction;
 import com.inspedio.entity.InsShape;
 import com.inspedio.entity.InsState;
 import com.inspedio.entity.primitive.InsCallback;
 import com.inspedio.entity.ui.InsButton;
 import com.inspedio.enums.FontSize;
 import com.inspedio.enums.FontStyle;
 import com.inspedio.system.core.InsCanvas;
 import com.inspedio.system.core.InsGlobal;
 
 public class SampleActionState extends InsState{
 
 	InsShape obj;
 	InsButton sequence;
 	InsButton parallel;
 	
 	
 	public void create() {
 		int midX = InsGlobal.screenWidth / 2;
 		int midY = InsGlobal.screenHeight / 2;
 		
 		this.obj = new InsShape(midX, midY - 50, 50, 50);
 		this.obj.setColor(InsCanvas.COLOR_RED, true);
 		
 		this.sequence = new InsButton(midX - 60, midY + 50, 100, 60, "SEQUENCE", InsCanvas.COLOR_GREEN);
 		this.sequence.setCaption("SEQUENCE", InsCanvas.COLOR_BLACK, FontSize.SMALL, FontStyle.PLAIN);
 		this.sequence.setClickedCallback(new InsCallback() {
 			public void call() {
 				obj.setAction(MoveBy.create(10, 50, 0, null));
 				obj.appendAction(MoveBy.create(10, -50, 0, null));
 			}
 		});
 		
 		this.parallel = new InsButton(midX + 60, midY + 50, 100, 60, "PARALLEL", InsCanvas.COLOR_GREEN);
 		this.parallel.setCaption("PARALLEL", InsCanvas.COLOR_BLACK, FontSize.SMALL, FontStyle.PLAIN);
 		this.parallel.setClickedCallback(new InsCallback() {
 			public void call() {
 				obj.setAction(Sequence.create(new InsAction[]{MoveBy.create(10, 50, 0, null), MoveBy.create(10, -50, 0, null)}, null));
 				obj.combineAction(Sequence.create(new InsAction[]{MoveBy.create(10, 0, 50, null), MoveBy.create(10, 0, -50, null)}, null));
 			}
 		});
 		
 		this.add(this.obj);
 		this.add(this.sequence);
 		this.add(this.parallel);
 	}
 
 }
