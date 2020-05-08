 package info.kyorohiro.helloworld.display.widget.lineview.edit;
 
 
 import info.kyorohiro.helloworld.display.widget.lineview.LineViewBufferSpec;
 import info.kyorohiro.helloworld.io.BreakText;
 import info.kyorohiro.helloworld.text.KyoroString;
 
 //
 // next sCRLFt悤ɂB
 //
 public class EditableLineViewBuffer implements LineViewBufferSpec, IMEClient {
 
 	private Differ mDiffer = new Differ();
 	private LineViewBufferSpec mOwner = null;
 	private int mCursorRow = 0;
 	private int mCursorLine = 0;
 	private boolean mIsCrlfMode = false; // RRł悢͌Kv
 
 	public EditableLineViewBuffer(LineViewBufferSpec owner) {
 		super();
 		mOwner = owner;
 	}
 
 	public boolean isEdit() {
 		return (mDiffer.lengthOfLine()<=0?false:true);
 	}
 	public void IsCrlfMode(boolean isCrlfMode) {
 		mIsCrlfMode = isCrlfMode;
 	}
 	@Override
 	public void isSync(boolean isSync) {
 		mOwner.isSync(isSync);
 	}
 
 	@Override
 	public int getNumOfAdd() {
 		return mOwner.getNumOfAdd();
 	}
 
 	@Override
 	public void clearNumOfAdd() {
 		mOwner.clearNumOfAdd();
 	}
 
 	@Override
 	public int getNumberOfStockedElement() {
 		return mOwner.getNumberOfStockedElement() + mDiffer.length();
 	}
 
 	@Override
 	public KyoroString get(int i) {
 		return mDiffer.get(mOwner, i);
 	}
 
 	@Override
 	public BreakText getBreakText() {
 		return mOwner.getBreakText();
 	}
 
 	@Override
 	public KyoroString[] getElements(KyoroString[] ret, int start, int end) {
 		for (int i = start, j = 0; i < end; i++, j++) {
 			ret[j] = get(i);
 		}
 		return ret;
 	}
 
 	public int getRow() {
 		return mCursorRow;
 	}
 
 	public int getCol() {
 		return mCursorLine;
 	}
 
 	@Override
 	public void setCursor(int row, int col) {
 		// this method Should belong to LIneView
 		mCursorRow = row;
 		mCursorLine = col;
 		if (mCursorLine < 0) {
 			mCursorLine = 0;
 		} else if (mCursorLine >= getNumberOfStockedElement()) {
 			mCursorLine = getNumberOfStockedElement() - 1;
 		}
 
 		KyoroString c = get(mCursorLine);
 		if (mCursorRow < 0) {
 			mCursorRow = 0;
 		} else if (mCursorRow > c.lengthWithoutLF(mIsCrlfMode)) {
 			mCursorRow = c.lengthWithoutLF(mIsCrlfMode);
 		}
 	}
 
 	public void crlf() {
 		crlf(true);
 	}
 	public void crlf(boolean addLf) {
 		String lf = "";
 		if(addLf){
 			if(mIsCrlfMode) {
 				lf = "\r\n";
 			} else {
 				lf = "\n";				
 			}
 		}
 
 		int index = getNumberOfStockedElement() - 1;// +1;
 		if(index<0){
 			index=0;
 		}
 		if (index > mCursorLine) {
 			index = mCursorLine;
 		}
 		CharSequence c = get(mCursorLine);
 		int row = c.length();
 		if (row > mCursorRow) {
 			row = mCursorRow;
 		}
 
 		if (row == c.length()) {
 			mCursorLine += 1;
 			mDiffer.addLine(mCursorLine, lf);
 			mCursorRow = 0;
 		} else if (row == 0) {
 			mDiffer.setLine(mCursorLine, lf);
 			mCursorLine += 1;
 			mCursorRow = 0;
 			mDiffer.addLine(mCursorLine, c);
 		} else {
 			mDiffer.setLine(mCursorLine, c.subSequence(0, row) + lf);
 			mCursorLine += 1;
 			mCursorRow = 0;
 			mDiffer.addLine(mCursorLine, c.subSequence(row, c.length()));
 		}
 	}
 
 	private boolean lineIsEmpty(CharSequence line) {
 		if (line == null || line.length() == 0){
 			return true;
 		}
 		else if(line.length() == 1 && line.subSequence(0, 1).equals("\n")){
 			return true;
 		}
 		else if(mIsCrlfMode&&line.length() == 2 && line.subSequence(0, 2).equals("\r\n")){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	private boolean deleteTargetIsEmpty() {
 		if (mCursorLine == 0 && mCursorRow <= 0) {
 			return true;
 		} else if(0==getNumberOfStockedElement()){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	public void delete() {
 
 		if (deleteTargetIsEmpty()) {
 			return;
 		}
 //		if(true){
 //			deleteLinePerVisible();
 //			return;
 //		}
 
 		int index = getNumberOfStockedElement() - 1;
 		if (index > mCursorLine) {
 			index = mCursorLine;
 		}
 
 		KyoroString currentLineText = get(index);
 		if (lineIsEmpty(currentLineText)){
 			deleteLinePerVisible();
 			// J[\̈ʒu͌B
 			return;
 		}
 
 		int row = mCursorRow;
 		if (row >= currentLineText.lengthWithoutLF(mIsCrlfMode)) {
 			row = currentLineText.lengthWithoutLF(mIsCrlfMode);
 		}
 
 		if (mCursorRow <= 0&&index>0) {
 		//	android.util.Log.v("kiyo", "msg-------(1)");
 			KyoroString prevLine = get(index-1);
 			mCursorLine = index;
 			deleteLinePerVisible();
 			deleteLinePerVisible();
 			mCursorLine=index-1;
 			if(currentLineText.includeLF()){
 //				android.util.Log.v("kiyo", "msg-------(1-1-1)");
 				crlf(true);
 				mCursorLine=index-1;
 			}else {
 //				android.util.Log.v("kiyo", "msg-------(1-1-2)");
 				crlf(false);//false݈̎̌ʒuɂāA邱
 				mCursorLine=index-1;
 			}
 
 			if(prevLine.includeLF()) {
 				//				android.util.Log.v("kiyo", "msg-------(1-2-1)");
 				prevLine.pargeLF(mIsCrlfMode);
 				commit(""+prevLine+currentLineText, 0);//crlfKv
 				moveCursor(prevLine.length());
 				prevLine.releaseParge();
 			} else {
 				//				android.util.Log.v("kiyo", "msg-------(1-2-2)");
 				prevLine.pargeLF(mIsCrlfMode);
 				prevLine.pargeEnd();
 				commit(""+prevLine+currentLineText, 0);//crlfKv
 				moveCursor(prevLine.length());
 				prevLine.releaseParge();
 			}
 			return;
 		} else {
 			if (currentLineText.includeLF()){
 				//			android.util.Log.v("kiyo", "msg-------(2)");
 				CharSequence f = "";
 				if (row > 1) {
 					f = currentLineText.subSequence(0, row - 1);
 				}
 				CharSequence e = "";
 				if (row < currentLineText.length()) {
 					e = currentLineText.subSequence(row, currentLineText.length());
 				}
 				mDiffer.setLine(index, "" + f + e);
 				mCursorRow = row - 1;
 			} else {
 				//android.util.Log.v("kiyo", "msg-------(3)");
 				CharSequence f = currentLineText.subSequence(0, row - 1);
 				CharSequence e = "";
 				if (row < currentLineText.length()) {
 					e = currentLineText.subSequence(row, currentLineText.length());
 				}
 				int br = row - 1;
 				int bc = mCursorLine;
 				deleteLinePerVisible();
				//if (mCursorLine + 1 < getNumberOfStockedElement()) {
				//	mCursorLine += 1;
 					mCursorRow = 0;
				//}
 //				crlf(fal);
 				// mCursorC =row-1;
 				commit("" + f + e, index);
 				setCursor(br, bc);
 			}
 		}
 		if (mCursorRow < 0) {
 			mCursorRow = 0;
 		}
 	}
 
 	public void deleteLinePerVisible() {
 		int index = getNumberOfStockedElement();// +1;
 		if(index == 0){
 			return;
 		}
 		if (index >= mCursorLine) {
 			index = mCursorLine;
 		}
 		CharSequence deleted = get(index);
 		mDiffer.deleteLine(index);
 		mCursorLine -= 1;
 		//if (mCursorLine >= 0) {
 		//	mCursorRow = get(mCursorLine).length();
 		//} else {
 			mCursorRow = 0;
 		//}
 		/*CharSequence c = get(mCursorLine);
 		if (deleted.charAt(deleted.length() - 1) == '\n'
 				&& c.charAt(c.length() - 1) != '\n') {
 			mDiffer.setLine(mCursorLine, "" + c + "\n");
 		}*/
 		setCursor(mCursorRow, mCursorLine);
 
 	}
 
 	@Override
 	public void pushCommit(CharSequence text, int cursor) {
 		// android.util.Log.v("log","col="+mCursorCol + ",row="+mCursorRow);
 //		android.util.Log.v("kiyo", "[-b1-]col="+mCursorLine+",row="+mCursorRow);
 		int index = getNumberOfStockedElement();// +1;
 		if (index > mCursorLine) {
 			index = mCursorLine;
 		}
 
 		CharSequence l = "";
 //		android.util.Log.v("kiyo", "[-b2-]col="+index+",numof="+getNumberOfStockedElement());
 		if(index<getNumberOfStockedElement()){
 			l = get(index);
 		}
 //		android.util.Log.v("kiyo", "[-b3-]len="+l.length()+","+l);
 		if(mCursorRow > l.length()){
 			mCursorRow = l.length();
 		}
 //		android.util.Log.v("kiyo", "[-d1-]col="+mCursorLine+",row="+mCursorRow);
 		if (0<=mCursorRow-1&&l.charAt(mCursorRow-1) == '\n') {
 			mCursorRow -= 1;
 			if (mIsCrlfMode&&0<=mCursorRow-1&&l.charAt(mCursorRow-1) == '\r'){
 				mCursorRow -= 1;				
 			}
 		}
 //		android.util.Log.v("kiyo", "[-d1-]col="+mCursorLine+",row="+mCursorRow);
 //		android.util.Log.v("kiyo", "[-a-]col="+mCursorLine+",row="+mCursorRow);
 		commit(text, cursor);
 	}
 
 	@Override
 	public int getMaxOfStackedElement() {
 		return mOwner.getMaxOfStackedElement();
 	}
 
 	private void moveCursor(int move) {
 //		android.util.Log.v("kiyo", "[-1-]col="+mCursorLine+",row="+mCursorRow);
 		mCursorRow += move;
 		CharSequence c = "";
 		if(mCursorLine>=getNumberOfStockedElement()){
 			return;
 		}
 		while(true) {
 			c = get(mCursorLine);
 //			android.util.Log.v("kiyo", "[-2-]col="+mCursorLine+",row="+mCursorRow+",len="+c.length()+","+c);
 			if(c==null){
 				c="";
 			}
 			if(mCursorRow <= c.length()){
 				return;
 			}
 			if(mCursorLine >= getNumberOfStockedElement()){
 				//setCursor(mCursorRow, mCursorLine);
 				return;
 			}
 			mCursorLine += 1;
 			mCursorRow = mCursorRow-c.length();
 		}
 	}
 	//commit text\nӂ܂ꍇ͂ǂ͂Ă
 	private void commit(CharSequence text, int cursor) {
 //		android.util.Log.v("kiyo", "[-0-]col="+mCursorLine+",row="+mCursorRow);
 		//---------
 		if(mCursorLine<=0) {
 			mCursorLine = 0;
 		}
 		if(mCursorRow<=0){
 			mCursorRow = 0;
 		}
 		//---------
 		int cursorMove = text.length();
 		if(cursor<=0){
 			cursorMove = 0;
 		}
 
 		int cursorRow = mCursorRow;
 		int cursorLine = mCursorLine;
 		try {
 			if(text.length()>0&&text.charAt(text.length()-1)=='\n'){
 				if(mIsCrlfMode&&text.length()>1&&text.charAt(text.length()-2)=='\r'){
 					text = text.subSequence(0, text.length()-2);
 				}else {
 					text = text.subSequence(0, text.length()-1);					
 				}
 			}
 			if(0>=getNumberOfStockedElement()){
 				if(mIsCrlfMode) {
 					mDiffer.addLine(0, "\r\n");
 				} else {
 					mDiffer.addLine(0, "\n");
 				}
 			}
 
 			CharSequence ct = "";
 			if(0<getNumberOfStockedElement()){
 				ct=get(cursorLine);
 			}
 			if (cursorRow >= ct.length()) {
 				cursorRow = ct.length();
 			}
 			KyoroString f = composeString(ct, cursorRow, text);
 //			android.util.Log.v("kiyo", "_f0=" + f+","+cursorLine+"."+cursor);
 
 			int w = this.getBreakText().getWidth();
 			int breakLinePoint = 0;
 			int currentLineLength = 0;
 			boolean hasAlreadyExecuteFirstAction = false;
 			int i = 0;
 			int g = 0;
 			do {
 				i++;
 				//android.util.Log.v("kiyo", "_aaa1=" + f.length() + "," + w + ","+ f);
 				//breakLinePoint = getBreakText().breakText(f, 0, f.length(), w);
 
 				//todo
 				//
 				// Ƃ肠AҒʂɓmF邱!!
 				//
 				breakLinePoint = BreakText.breakText(getBreakText(), f, 0, f.length());
 				currentLineLength = f.length();
 				//android.util.Log.v("kiyo", "_aaa0=" + i+",row="+mCursorRow+",col="+mCursorLine);
 				//android.util.Log.v("kiyo", "_aaa1=" + i+",row="+(currentLineLength+g)+">"+breakLinePoint+","+g);
 				//android.util.Log.v("kiyo", "_aaa0=" + i+","+f);
 				if(f.length()>0&&f.charAt(f.length()-1)=='\n'){
 					if(mIsCrlfMode&&f.length()>1&&f.charAt(f.length()-2)=='\r'){
 						g=-2;
 					}else {
 						g=-1;				
 					}
 				} else {
 					g = 0;
 				}
 				//			if (!hasAlreadyExecuteFirstAction) {
 
 				if(g>=0){
 					g=0;
 					mDiffer.setLine(cursorLine, f.subSequence(0, breakLinePoint));
 					if ((currentLineLength+g) > breakLinePoint) {
 						cursorLine += 1;
 						cursorRow = 0;
 //						android.util.Log.v("kiyo", "_aaa2 break=" + breakLinePoint +",curre="+currentLineLength);
 						f = f.newKyoroString(breakLinePoint, currentLineLength);
 						if(f.length()>0&&f.charAt(f.length()-1)!='\n'&&cursorLine<getNumberOfStockedElement()) {
 							f = new KyoroString(""+f+get(cursorLine));
 						}
 					} else {
 						cursorRow += text.length();
 						break;
 					}
 				} else {
 					g=0;
 					if (currentLineLength > breakLinePoint) {
 						mDiffer.addLine(cursorLine, f.subSequence(0, breakLinePoint));
 						cursorLine += 1;
 						cursorRow += text.length();
 //						android.util.Log.v("kiyo", "_aaa2 break=" + breakLinePoint +",curre="+currentLineLength);
 						f = f.newKyoroString(breakLinePoint, currentLineLength);
 					} else {
 						if(!hasAlreadyExecuteFirstAction) {
 							mDiffer.setLine(cursorLine, f.subSequence(0, breakLinePoint));					
 						} else {
 							mDiffer.addLine(cursorLine, f.subSequence(0, breakLinePoint));
 							hasAlreadyExecuteFirstAction = true;
 						}
 						cursorRow = breakLinePoint;
 						break;
 					}
 				}
 			} while ((currentLineLength+g) > breakLinePoint&&f.length()>0);
 		} finally {
 			moveCursor(cursorMove);
 		}
 	}
 
 	public static KyoroString composeString(CharSequence b, int i,
 			CharSequence s) {
 		if (i >= b.length()) {
 			i = b.length();
 		}
 		//----------
 		//if(i<0){
 		//	i=0;
 		//}
 		//------
 		if (i == 0) {
 			return new KyoroString("" + s + b);
 		} else if (i == b.length()) {
 			return new KyoroString("" + b + s);
 		} else {
 			CharSequence f = b.subSequence(0, i);
 			CharSequence e = b.subSequence(i, b.length());
 			return new KyoroString("" + f + s + e);
 		}
 	}
 
 	@Override
 	public void dispose() {
 		mOwner.dispose();
 	}
 
 }
