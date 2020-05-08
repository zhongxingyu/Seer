 package com.pixeldoctrine.hussh.shell;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Arrays;
 
 import android.annotation.SuppressLint;
 import android.graphics.Typeface;
 import android.text.Spannable;
 import android.text.style.BackgroundColorSpan;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.text.style.UnderlineSpan;
 import android.widget.TextView;
 
 import com.pixeldoctrine.hussh.util.ColorTermPrinter;
 import com.pixeldoctrine.hussh.util.ColorTerminal;
 import com.pixeldoctrine.hussh.util.ElementStyle;
 
 public class ConsoleOutput extends OutputStream implements ColorTerminal {
 
 	private TextView console;
 	private ColorTermPrinter colorPrinter;
 	private byte[] data = new byte[4096];
 	private int offset = 0;
 	private boolean doPost = true;
 	private boolean pipeToNull = false;
 	private boolean lastWasCR;
 
 	public ConsoleOutput(TextView console) {
 		this.console = console;
 		colorPrinter = new ColorTermPrinter();
 	}
 
 	public void print(String s) {
 		byte[] data = s.getBytes();
 		try {
 			write(data, 0, data.length);
 		} catch (IOException e) {
 		}
 	}
 
 	public void setPipeToNull(boolean pipeToNull) {
 		this.pipeToNull = pipeToNull;
 	}
 
 	@Override
 	public void rawPrint(String text) {
 		boolean eraseLine = false;
 		if (lastWasCR) {
 			if (!text.startsWith("\n")) {
 				eraseLine = true;
 			}
 		} else if (text.startsWith("\r") && !text.startsWith("\r\n")) {
 			if (text.length() == 1) {
 				lastWasCR = true;
 				return;
 			}
 			eraseLine = true;
 		}
 		lastWasCR = false;
 		if (eraseLine) {
 			String currentText = console.getText().toString();
 			int lastLineStart = currentText.lastIndexOf('\n') + 1;
 			if (lastLineStart < currentText.length()) {
 				currentText = currentText.substring(0, lastLineStart);
 				console.setText(currentText+text);
 				return;
 			}
 		}
 		if (text.equals("\b")) {
 			CharSequence currentText = console.getText();
 		    currentText = currentText.subSequence(0, currentText.length() - 1);
 		    console.setText(currentText);
 			return;
 		}
 		console.append(text);
 	}
 
 	@Override
 	public void rawPrint(String text, ElementStyle style) {
 		int start = console.getText().length();
 		rawPrint(text);
 	    int end = console.getText().length();
	    if (end-start < text.length()) {
	    	start = Math.max(0, end-text.length());
	    }
 
 	    Spannable spannableText = (Spannable) console.getText();
 	    spannableText.setSpan(new ForegroundColorSpan(style.fgColor.getIntColor()), start, end, 0);
 		if (style.bold) {
 		    spannableText.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
 		}
 		if (style.italic) {
 		    spannableText.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 0);
 		}
 		if (style.blink) {
 		    // ???
 		}
 		if (style.underline) {
 		    spannableText.setSpan(new UnderlineSpan(), start, end, 0);
 		}
 		if (style.conceal) {
 			// ???
 		}
 		if (style.bgColorSet) {
 		    spannableText.setSpan(new BackgroundColorSpan(style.bgColor.getIntColor()), start, end, 0);
 		}
 	}
 
 	@Override
 	public synchronized void write(byte[] buffer, int offset, int count) throws IOException {
 		doPost = false;
 		super.write(buffer, offset, count);
 		doPost = true;
 		post();
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public synchronized void write(int oneByte) throws IOException {
 		data[offset++] = (byte) oneByte;
 		if (doPost || offset > (data.length>>1)) {
 			data = Arrays.copyOf(data, data.length*2);
 			post();
 		}
 	}
 
 	public int getCharWidth() {
 		float w10 = console.getPaint().measureText("WWWWWWWWWW");
 		return (int)(console.getWidth() * 10 / w10);
 	}
 
 	public int getCharHeight() {
 		/*String text = "W\nW\nW\nW\nW";
 		Rect bounds = new Rect(0, 0, console.getWidth(), console.getHeight());
 		console.getPaint().getTextBounds(text, 0, text.length(), bounds);
 		int h5 = bounds.bottom;
 		return console.getHeight() * 5 / h5;*/
 		return console.getHeight() / 15;	// TODO: font height a bit hard-coded...
 	}
 
 	private void post() {
 		if (pipeToNull) {
 			return;
 		}
 		console.post(new Runnable() {
 			public void run(){
 				String text;
 				synchronized (ConsoleOutput.this) {
 					text = new String(data, 0, offset);
 					offset = 0;
 				}
 				colorPrinter.print(text, ConsoleOutput.this);
 			}
 		});
 	}
 }
