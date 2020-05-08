 package com.github.cruscotto;
 
import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
 
 public class Cruscotto {
 
 	private Color shellBgColor;
 	private final Composite topContainer;
 
 	public Cruscotto(Composite topContainer) {
 		this.topContainer = topContainer;
 		GridLayout layout = new GridLayout();
 		layout.marginLeft = 0;
 		layout.marginRight = 0;
 		layout.marginBottom = 0;
 		layout.marginTop = 0;
 		layout.horizontalSpacing = 0;
 		layout.marginWidth = 0;
 		topContainer.setLayout(layout);
 		shellBgColor = new Color(topContainer.getDisplay(), 255, 255, 255);
 		topContainer.setBackground(shellBgColor);
 	}
 
 	public void dispose() {
 		shellBgColor.dispose();
 	}
 
 	public void addTopic(TopicContainer topic) {
 		
 	}
 
 	public Composite getTopContainer() {
 		return topContainer;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		Cruscotto cruscotto = new Cruscotto(shell);
 
		// add admin topic
		new TopicContainer(cruscotto, "admin") {
			@Override
			public void createContent(Composite contentArea) {
				contentArea.setLayout(new GridLayout(2, false));
				Label fontLabel = new Label(contentArea, SWT.NONE);
				fontLabel.setText("Header Font size:");
				fontLabel.setBackground(contentArea.getBackground());
				Text fontSize = new Text(contentArea, SWT.SINGLE | SWT.FLAT);
				fontSize.setText("10");
				fontSize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			}
		};

 		// add two topics, just for test...
 		new TopicContainer(cruscotto, "one");
 		new TopicContainer(cruscotto, "two");
 
 		// show the window and run the event loop
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 		
 		// terminated, dispose
 		cruscotto.dispose();
 		display.dispose();
 	}
 }
