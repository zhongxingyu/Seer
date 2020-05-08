 package ru.cut2drox.ui;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FontDialog;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Combo;
 
 import ru.cut2drox.brain.Config;
 
 public class TextLabel extends Dialog {
 
 	protected Object result;
 	protected Shell shell;
 	protected Shell parent;
 	private Text text;
 	Font font;
 	
 	FontData loadFont(Display display) throws FileNotFoundException
 	{
 		Config conf=new Config().takeConfig();
 		FontData defaultFont = new FontData(conf.getFont(), conf.getFontSize(),conf.getFontStyle());
 		font=new Font(display, defaultFont);
 		return defaultFont;
 	}
 	
 	void setFont(FontData newFont) throws IOException
 	{
 		Config conf=new Config().takeConfig();
 		conf.setFont(newFont.getName());
 		conf.setFontSize(newFont.getHeight());
 		conf.setFontStyle(newFont.getStyle());
 		conf.makeConfig();
 	}
 
 	public TextLabel(Shell parent, int style) {
 		super(parent, style);
 		this.parent=parent;
 		setText("SWT Dialog");
 	}
 
 	public Object open(final ResizableCanvas canvas,final Image image) throws FileNotFoundException {
 		Display display = getParent().getDisplay();
 		createContents(canvas,image,display);
 		shell.open();
 		shell.layout();
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	private void createContents(final ResizableCanvas canvas,final Image image,final Display display) throws FileNotFoundException {
 		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
 		shell.setSize(303, 182);
 		shell.setText(getText());
 		final FontDialog fd = new FontDialog(shell);
 		fd.setFontData(loadFont(display));
 		
 		Rectangle client=shell.getBounds();
 		Rectangle par=parent.getBounds();
 		client.x=(par.x+par.width/2)-client.width/2;
 		client.y=(par.y+par.height/2)-client.height/2;
 		shell.setBounds(client);
 		
 		Button button = new Button(shell, SWT.NONE);
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				shell.dispose();
 			}
 		});
 		button.setBounds(154, 119, 75, 25);
 		button.setText("Отмена");
 		
 		Button btnNewButton = new Button(shell, SWT.NONE);
 		btnNewButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent se) {
 				shell.dispose();
 				//-----------------------------------
 				Listener listener = new Listener() {
 		            public void handleEvent(Event e) {
 		      		switch (e.type) {
 		      			case SWT.MouseEnter:
 		      				canvas.drawRText("looool",e.x,e.y-17,font);
 		      				break;
 		      			case SWT.MouseDown:
 		      				GC gc = new GC(image);
 		      				gc.setFont(font); 
		      				gc.drawText("looool", e.x-canvas.canvasShiftX,e.y-17-canvas.canvasShiftY,true);
 		      				gc.dispose();	      				
 		      				break;
 		      			case SWT.MouseMove:
 		      				canvas.drawRText("looool",e.x,e.y-17,font);
 		      				break;
 		      			case SWT.MouseUp:
 		      				canvas.drawRText("",-1,-1,font); //убираем мышку с канваса
 		      				canvas.removeListener(SWT.MouseMove, this);
 		      				canvas.removeListener(SWT.MouseDown, this);
 		      				canvas.removeListener(SWT.MouseUp, this);
 		      				canvas.removeListener(SWT.MouseEnter, this);
 		      				canvas.redraw();
 		      	  	      	break;
 		      			}
 		            }
 		        };
 		        canvas.addListener(SWT.MouseDown, listener);
 		        canvas.addListener(SWT.MouseMove, listener);
 		        canvas.addListener(SWT.MouseUp, listener);
 		        canvas.addListener(SWT.MouseEnter, listener);
 		        //-------------------------------------
 			}
 		});
 		btnNewButton.setBounds(73, 119, 75, 25);
 		btnNewButton.setText("ОК");
 		
 		text = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI);
 		text.setBounds(10, 40, 277, 73);
 		
 		Label label = new Label(shell, SWT.NONE);
 		label.setBounds(10, 19, 33, 15);
 		label.setText("Текст:");
 		
 		Label label_1 = new Label(shell, SWT.NONE);
 		label_1.setBounds(57, 11, 91, 15);
 		label_1.setText("Размер шрифта:");
 		
 		Combo combo = new Combo(shell, SWT.NONE);
 		combo.setBounds(150, 8, 42, 23);
 		
 		Button btnNewButton_1 = new Button(shell, SWT.NONE);
 		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
 			@SuppressWarnings("deprecation")
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 		        FontData newFont = fd.open();
 		        if (newFont == null) return;
 		        font=new Font(display, newFont);
 		        try {
 					setFont(newFont);
 				} catch (IOException e) {e.printStackTrace();}
 			}
 		});
 		btnNewButton_1.setBounds(212, 8, 75, 23);
 		btnNewButton_1.setText("Шрифт");
 
 	}
 }
