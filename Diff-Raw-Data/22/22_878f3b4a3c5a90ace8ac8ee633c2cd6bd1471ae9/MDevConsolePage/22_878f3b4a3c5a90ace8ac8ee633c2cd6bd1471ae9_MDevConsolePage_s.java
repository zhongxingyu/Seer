 package gov.va.mumps.debug.ui.console;
 
 import gov.va.mumps.launching.InputReadyListener;
 
 import java.util.Iterator;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.IPageBookViewPage;
 import org.eclipse.ui.part.IPageSite;
 
 //REFER to here for more info on how to implement an IPageBookViewPage:
 //http://grepcode.com/file/repository.grepcode.com/java/eclipse.org/4.2/org.eclipse.ui/console/3.5.100/org/eclipse/ui/console/TextConsolePage.java#TextConsolePage.%3Cinit%3E%28org.eclipse.ui.console.TextConsole%2Corg.eclipse.ui.console.IConsoleView%29
 public class MDevConsolePage implements IPageBookViewPage, KeyListener {
 	
 	private MDevConsole console;
 	private StyledText textWidget;
 	private IPageSite pageSite;
 	
 	
 	public MDevConsolePage(MDevConsole console) {
 		super();
 		
 		this.console = console;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		textWidget = new StyledText(parent, SWT.V_SCROLL);
 		textWidget.setText("");
 		Device device = Display.getCurrent();
 		textWidget.setBackground(new Color(device, 0, 0, 0));
 		textWidget.setEditable(false);
 		textWidget.setForeground(new Color(device, 255, 255, 255));
 		FontData fd = new FontData("Courier New", 10, 0);
 		textWidget.setFont(new Font(device, fd)); //TODO: add backup true type fonts/test for other supported OS'es
 		textWidget.addKeyListener(this);
 		
 		//NOTE: can create actions and add them to the toolbar here.	
 	}
 
 	@Override
 	public void dispose() {
 		textWidget.removeKeyListener(this);
 		textWidget.dispose();
 	}
 
 	@Override
 	public Control getControl() {
 		return textWidget;
 	}
 
 	@Override
 	public void setActionBars(IActionBars actionBars) {
 	}
 
 	@Override
 	public void setFocus() {
 		if (textWidget != null) {
 			textWidget.setFocus();
 		}
 	}
 
 	@Override
 	public IPageSite getSite() {
 		return pageSite;
 	}
 
 	@Override
 	public void init(IPageSite pageSite) throws PartInitException {
 		this.pageSite = pageSite;
 	}
 
 	private int keysTyped = 0;
 	private String keyInput = "";
 	
 	@Override
 	public void keyPressed(KeyEvent keyEvent) {
		System.out.println("char: " +keyEvent.character);
		System.out.println("code: " +keyEvent.keyCode);
 
		if (!console.isReadingUserInput()) //not possible for this to happen: || keysTyped >= console.getMaxCharInput())
 			return;
 		
		//determine if typing character or special key like esc, page up, etc
		if (Character.isLetterOrDigit(keyEvent.character)) {

 			appendText(keyEvent.character+""); //echo input
 			keyInput += keyEvent.character; //record input
 			keysTyped++;
 		} else { //handle special keys
 			if (keyEvent.keyCode == SWT.BS && keysTyped > 0) { //support for delete key
 				//remove the echo'ed input
 				keysTyped--;
 				textWidget.setText(textWidget.getText().substring(0, textWidget.getText().length() - 1));
 				keyInput = keyInput.substring(0, keyInput.length() - 1);
 			}
 		}
 		
 		//handle return key or max chars entered
 		if (keysTyped == console.getMaxCharInput() || keyEvent.character == SWT.CR) { //TODO: test this on eclipse linux
 			handleInputReadyListeners();
 			keysTyped = 0;
 			keyInput = "";
 			console.setReadingInput(false);
 		}
 	}
 	
 	private void handleInputReadyListeners() {
 		
 		Iterator<InputReadyListener> inputListeners = console.getInputReadyInputListeners();
 		while (inputListeners.hasNext()) {
 			getSite().getShell().getDisplay().asyncExec(new ReadInputHandler(inputListeners.next(), new String(this.keyInput)));
 		}
 	}
 	
 	private class ReadInputHandler implements Runnable {
 		
 		private InputReadyListener listener;
 		private String input;
 		
 		private ReadInputHandler(InputReadyListener listener, String input) {
 			this.listener = listener;
 			this.input = input;
 		}
 
 		@Override
 		public void run() {
 			listener.handleInput(input);
 		}
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent keyEvent) {
 		//do nothing, this only is fired when no more keys are pressed, not for each key release.
 	}
 
 	public void setKeysTyped(int keysTyped) {
 		this.keysTyped = keysTyped;
 	}
 
 	public void appendText(String text) {
 		textWidget.setText(textWidget.getText() + text);
 	}
 	
 }
