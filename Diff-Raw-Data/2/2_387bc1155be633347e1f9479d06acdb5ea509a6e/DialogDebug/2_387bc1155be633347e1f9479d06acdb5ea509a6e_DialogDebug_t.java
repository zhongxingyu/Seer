 package app;
 
 import java.awt.*;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 final class DialogDebug
 		extends OutputStream
 		implements WindowListener
 {
 	private static final DialogDebug INSTANCE = new DialogDebug();
 	private static String _title;
 
 	private PrintStream _printStream = new PrintStream(this, true);
 	private StringBuffer _buffer = new StringBuffer(512);
 	private Frame _frame;
 	private TextArea _textArea = new TextArea();
 
 	private DialogDebug()
 	{
 	}
 
 	public static PrintStream getPrintStream(String dialogTitle)
 	{
		_title = dialogTitle;
 		return INSTANCE._printStream;
 	}
 
 	@Override
 	public final void write(int b)
 	{
 		synchronized (this) {
 			if (b != 10) {
 				_buffer.append((char) b);
 			} else {
 				if (_frame == null) {
 					_frame = new Frame(_title);
 					_frame.add(_textArea, BorderLayout.CENTER);
 					_frame.setLocation(320, 240);
 					_frame.setSize(720, 260);
 					_frame.addWindowListener(this);
 					_frame.setVisible(true);
 				}
 
 				_buffer.append("\n");
 				_textArea.append(_buffer.toString());
 				_buffer = new StringBuffer(512);
 			}
 		}
 	}
 
 	@Override
 	public final void windowClosing(WindowEvent e)
 	{
 		_frame.dispose();
 		_frame = null;
 	}
 
 	@Override
 	public final void windowDeactivated(WindowEvent e)
 	{
 	}
 
 	@Override
 	public final void windowOpened(WindowEvent e)
 	{
 	}
 
 	@Override
 	public final void windowIconified(WindowEvent e)
 	{
 	}
 
 	@Override
 	public final void windowDeiconified(WindowEvent e)
 	{
 	}
 
 	@Override
 	public final void windowClosed(WindowEvent e)
 	{
 	}
 
 	@Override
 	public final void windowActivated(WindowEvent e)
 	{
 	}
 }
 
 /*
  * Location: \\.psf\Home\Documents\java\jagexappletviewer\ Qualified Name:
  * app.Class_q JD-Core Version: 0.5.4
  */
