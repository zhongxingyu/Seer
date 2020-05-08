 package org.jnect.emfstore.replay;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Slider;
 import org.jnect.emfstore.EMFStorage;
 
 public class SliderDialog extends Dialog implements Observer {
 
 	private IReplayBodyProvider replayBodyProvider;
 	private Slider slider;
 	private Label label;
 	private int max;
 
 	public SliderDialog(Shell parent, int style, IReplayBodyProvider rbp) {
 		super(parent, style);
 		this.replayBodyProvider = rbp;
 		EMFStorage.getInstance().addObserver(this);
 	}
 
 	public void open() {
 
 		Shell parent = getParent();
 		GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
 		this.getParent().setLayout(layout);
		this.getParent().setText("Replay control");
 
 		GridData gData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		slider = new Slider(parent, SWT.HORIZONTAL);
 		Rectangle clientArea = parent.getClientArea();
 		slider.setBounds(clientArea.x + 10, clientArea.y + 10, 200, 32);
 		max = replayBodyProvider.getReplayStatesCount();
 		slider.setValues(0, 0, max, 1, 1, 1);
 		slider.setLayoutData(gData);
 
 		label = new Label(parent, SWT.BORDER);
 		gData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		label.setLayoutData(gData);
 		label.setText(slider.getSelection() + 1 + "/" + max + "steps");
 
 		Button button = new Button(parent, SWT.NONE);
 		gData = new GridData(SWT.LEAD, SWT.FILL, false, false);
 		button.setLayoutData(gData);
 		button.setText("Play");
 
 		slider.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				switch (event.detail) {
 				case SWT.HOME:
 				case SWT.DRAG:
 				case SWT.END:
 				case SWT.ARROW_DOWN:
 				case SWT.ARROW_UP:
 				case SWT.PAGE_DOWN:
 				case SWT.PAGE_UP:
 					replayBodyProvider.setReplayToState(slider.getSelection());
 					label.setText(slider.getSelection() + 1 + "/" + max + "steps");
 					break;
 				}
 			}
 		});
 
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				replayBodyProvider.replay(slider.getSelection());
 			}
 		});
 
 		parent.open();
 	}
 
 	@Override
 	public void update(Observable o, final Object arg) {
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				slider.setSelection((Integer) arg);
 				label.setText(slider.getSelection() + 1 + "/" + max + "steps");
 			}
 		});
 	}
 
 }
