 package it.polito.atlas.alea2.components;
 
 import static it.polito.atlas.alea2.components.DisplaySingleton.display;
 
 import java.util.concurrent.TimeUnit;
 
 import it.polito.atlas.alea2.Annotation;
 import it.polito.atlas.alea2.TrackVideo;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Shell;
 
 public class AnnotationShell {
 	private Shell shell;
 	private SWTPlayer player;
 	private Scale scale;
 	Annotation annotation;
 
 	public Shell shell() {
 		return shell;
 	}
 
 	public AnnotationShell(Annotation a) {
 		annotation = a;
 		player = new SWTPlayer();
 		
		for (TrackVideo t : a.getTracksVideo()) {
 			System.out.println(t.getName());			
 			player.addVideo(t.getName());
 		}
 		
 		shell = new Shell(display());
 		shell.setText("Annotation Editor");
 		shell.setSize(640, 240);
 		shell.setLocation(SWT.DEFAULT, SWT.DEFAULT);
 		shell.addShellListener(new ShellListener() {
 			
 			@Override
 			public void shellIconified(ShellEvent arg0) {
 				if (player == null)
 					return;
 				player.pause();
 				player.iconify(true);
 			}
 			
 			@Override
 			public void shellDeiconified(ShellEvent arg0) {
 				if (player == null)
 					return;
 				player.iconify(false);
 			}
 			
 			@Override
 			public void shellDeactivated(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				player.dispose();
 			}
 			
 			@Override
 			public void shellActivated(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		shell.setLayout(new FormLayout());
 		
 		Point size;
 		FormData formData;
 		
 		// Label
 		Label label = new Label(shell(), SWT.BORDER);
 		label.setText("Annotation: " + a.getName());
 		label.pack();
 		size = label.getSize();
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(0);
 		label.setLayoutData(formData);
 		
 		// Scale
 		scale = new Scale(shell(), SWT.BORDER);
 		scale.pack();
 		long max=player.getEndTime();
 		scale.setMaximum ((int) max);
 		System.out.println(max);
 		
 		size = scale.getSize();
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(label);
 		formData.bottom = new FormAttachment(60);
 		scale.setLayoutData(formData);
 		scale.addSelectionListener( new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				scale.setMaximum((int) player.getEndTime() / 1000);
				int perspectiveValue = scale.getSelection();
				System.out.println(perspectiveValue);
 		        player.seek(perspectiveValue*1000);
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 
 		// Button Play
 		Button ok = new Button(shell(), SWT.PUSH);
         ok.setText("Play / Resync");
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(50);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(scale);
 		formData.bottom = new FormAttachment(100);
 		ok.setLayoutData(formData);
 		ok.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				player.play();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		// Button Pause
 		Button cancel = new Button(shell(), SWT.PUSH);
 		cancel.setText("Pause");
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(50);
 		formData.top = new FormAttachment(scale);
 		formData.bottom = new FormAttachment(100);
 		cancel.setLayoutData(formData);
 		cancel.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				player.pause();
 				System.out.println(player.videos.get(0).queryDuration(TimeUnit.MILLISECONDS));
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});		
 		shell.open();
 	}
 	public String getProjectName() {
 		return null;
 	}
 }
