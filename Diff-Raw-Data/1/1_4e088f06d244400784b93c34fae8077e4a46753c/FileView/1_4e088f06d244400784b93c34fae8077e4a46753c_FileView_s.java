 package nl.rug.peerbox.ui;
 
 import nl.rug.peerbox.logic.FileRequestTask;
 import nl.rug.peerbox.logic.PeerboxFile;
 import nl.rug.peerbox.logic.PeerboxFileListener;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 
 public class FileView extends Composite implements DisposeListener, SelectionListener, PeerboxFileListener {
 
 	private PeerboxFile model;
 	private final Text filename;
 	private final Text owner;
 	private final Color background;
 	private final Color foreground;
 	private final Button action;
 
 	public FileView(Composite parent) {
 		super(parent, SWT.BORDER);
 		addDisposeListener(this);
 
 		background = new Color(getDisplay(), new RGB(215, 215, 215));
 		foreground = new Color(getDisplay(), new RGB(75, 75, 75));
 		setBackground(background);
 		setForeground(foreground);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 12;
 		layout.makeColumnsEqualWidth = true;
 		layout.marginHeight=0;
 		layout.marginWidth=0;
 		layout.marginTop=0;
 		layout.marginBottom=0;
 		setLayout(layout);
 
 		GridData filenameData = new GridData();
 		filenameData.grabExcessHorizontalSpace = true;
 		filenameData.horizontalAlignment = SWT.FILL;
 		filenameData.horizontalSpan = 6;
 		filename = new Text(this, SWT.NONE);
 		filename.setBackground(background);
 		filename.setForeground(foreground);
 		filename.setLayoutData(filenameData);
 
 		GridData ownerData = new GridData();
 		ownerData.grabExcessHorizontalSpace = true;
 		ownerData.horizontalAlignment = SWT.FILL;
 		ownerData.horizontalSpan = 3;
 		owner = new Text(this, SWT.NONE);
 		owner.setBackground(background);
 		owner.setForeground(foreground);
 		owner.setLayoutData(ownerData);
 		
 		action = new Button(this, SWT.PUSH);
 		action.setVisible(false);
 		GridData actionData = new GridData();
 		actionData.grabExcessHorizontalSpace = true;
 		actionData.horizontalAlignment = SWT.FILL;
 		actionData.horizontalSpan = 3;
 		action.setLayoutData(actionData);
 	}
 
 	public void setModel(PeerboxFile model) {
 		if (this.model != null) {
 			this.model.removeListener(this);
 		}
 		this.model = model;
 		this.model.addListener(this);
 		filename.setText(this.model.getFilename());
 		action.removeSelectionListener(this);
 		action.setVisible(false);
 		if (!model.isOwn()) {
 			owner.setText("@" + model.getOwner().getName());
 			if (!model.exists()) {
 				action.setVisible(true);
 				action.setText("Get");
 				action.addSelectionListener(this);
 			}
 		} else {
 			owner.setText("");
 		}
 		layout();
 		
 	}
 
 	public PeerboxFile getModel() {
 		return model;
 	}
 
 	@Override
 	public Point computeSize(int whint, int hhint, boolean changed) {
 		super.computeSize(whint, hhint, changed);
 		Point size = new Point(0, 0);
 		if (whint == SWT.DEFAULT) {
 			size.x = 150;
 		} else {
 			size.x = (whint < 50) ? 50 : whint;
 		}
 
 		size.y = 30;
 
 		return size;
 	}
 
 	@Override
 	public void widgetDisposed(DisposeEvent arg0) {
 		background.dispose();
 		foreground.dispose();
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent arg0) {		
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent arg0) {
 		this.getDisplay().asyncExec(new FileRequestTask(getModel()));	
 	}
 
 	@Override
 	public void modelUpdated() {
 		this.getDisplay().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				if (getModel().exists()) {
 					action.setVisible(false);
 				}
 				layout();
 			}
 		});
 	}
 
 }
