 package com.jshoes;
 
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 import com.jshoes.elements.ShoesButton;
 import com.jshoes.elements.TextBlock;
 import com.jshoes.internal.AskDialog;
 import com.jshoes.slots.Flow;
 import com.jshoes.slots.Slot;
 import com.jshoes.slots.Stack;
 
 public class JShoes {
 	
 	private Shell shell;
 	private Slot currentSlot;
 //	private String currentUrl = "/";
 	private FontRegistry fontRegistry;
 	
 	public JShoes(String title, CodeBlock block) {
 		Display display = new Display();
 		 shell = new Shell(display);
 		 shell.setLayout( new RowLayout());
 		 shell.setText(title);
 		 fontRegistry = new FontRegistry();
 		 currentSlot = new Flow(this);
 		 block.run(this);
 		 shell.pack();
 		 shell.open();
		 while( !shell.isDisposed()) {
			 if(!display.readAndDispatch()) 
				 display.sleep();
		 }
 		 display.dispose();
 	}
 	
 	public JShoes(CodeBlock block) {
 		this("JShoes", block);
 	}
 
 	public static JShoes app(CodeBlock block) {
 		return new JShoes(block);
 	}
 	
 	public static JShoes app(String title, CodeBlock block) {
 		return new JShoes(title, block);
 	}
 
 	public ShoesButton button(String message, final CodeBlock block) {
 		return new ShoesButton(this, currentSlot.getComposite(), message, block);
 	}
 	
 	public ShoesButton button(String message) {
 		return new ShoesButton(this, currentSlot.getComposite(), message);
 	}
 
 	public void alert(String message) {
 		MessageBox messageBox = new MessageBox(currentSlot.getComposite().getShell(), SWT.ICON_INFORMATION);
 		messageBox.setText("JShoes says : ");
 	    messageBox.setMessage(message);
 	    messageBox.open();
 	}
 	
 	public TextBlock para(Object... messages) {
 		return new Para(this, currentSlot.getComposite(), messages);
 	}
 	
 	public String ask(String question) {
 		return new AskDialog(shell, question).getResult();
 	}
 
 	public void stack(CodeBlock codeBlock) {
 		Slot oldSlot = currentSlot;
 		currentSlot = new Stack(this);
 		codeBlock.run(this);
 		currentSlot = oldSlot;
 	}
 	
 	public void flow(CodeBlock codeBlock) {
 		Slot oldSlot = currentSlot;
 		currentSlot = new Flow(this);
 		codeBlock.run(this);
 		currentSlot = oldSlot;
 	}
 	
 	public Slot getCurrentSlot() {
 		return currentSlot;
 	}
 	
 	public Shell getShell() {
 		return shell;
 	}
 
 	public void width(String width) {
 		currentSlot.getLayoutData().width(width);
 	}
 
 	public void height(String height) {
 		currentSlot.getLayoutData().height(height);
 	}
 
 	public void margin(String margin) {
 		currentSlot.getLayoutData().margin(margin);
 	}
 	
 	public void marginLeft(String margin) {
 		currentSlot.getLayoutData().marginLeft(margin);
 	}
 	
 	public void marginTop(String margin) {
 		currentSlot.getLayoutData().marginTop(margin);
 	}
 	
 	public void marginRight(String margin) {
 		currentSlot.getLayoutData().marginRight(margin);
 	}
 	
 	public void marginBottom(String margin) {
 		currentSlot.getLayoutData().marginBottom(margin);
 	}
 
 	public TextBlock banner(String message) {
 		return new TextBlock(this, currentSlot.getComposite(), message).size(48);
 	}
 	
 	public FontRegistry getFontRegistry(){
 		return fontRegistry;
 	}
 
 	public TextBlock title(String message) {
 		return new TextBlock(this, currentSlot.getComposite(), message).size(34);
 	}
 	
 	public TextBlock subtitle(String message) {
 		return new TextBlock(this, currentSlot.getComposite(), message).size(26);
 	}
 	
 	public TextBlock tagline(String message) {
 		return new TextBlock(this, currentSlot.getComposite(), message).size(18);
 	}
 	
 	public TextBlock caption(String message) {
 		return new TextBlock(this, currentSlot.getComposite(), message).size(14);
 	}
 	
 	public TextBlock inscription(String message) {
 		return new TextBlock(this, currentSlot.getComposite(), message).size(10);
 	}
 	
 	public void animate(final int fps, final CodeBlock block) {
 		shell.getDisplay().timerExec(1000/fps, new Runnable() {
 			@Override
 			public void run() {
				if(!shell.isDisposed()) {
					block.run(JShoes.this);
					shell.getDisplay().timerExec(1000/fps, this);
				}
 			}
 		});
 	}
 	
 	public void animate(CodeBlock block) {
 		animate(10, block);
 	}
 }
