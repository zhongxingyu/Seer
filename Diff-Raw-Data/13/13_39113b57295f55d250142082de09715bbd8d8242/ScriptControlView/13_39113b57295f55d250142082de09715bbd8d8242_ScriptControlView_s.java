 package cc.warlock.scribe.ui.views;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 import cc.warlock.client.IWarlockClient;
 import cc.warlock.client.IWarlockClientListener;
 import cc.warlock.client.WarlockClientRegistry;
 import cc.warlock.client.stormfront.IStormFrontClient;
 import cc.warlock.rcp.ui.client.SWTScriptListener;
 import cc.warlock.rcp.ui.client.SWTWarlockClientListener;
 import cc.warlock.scribe.ui.ScribeSharedImages;
 import cc.warlock.script.IScript;
 import cc.warlock.script.IScriptListener;
 
 public class ScriptControlView extends ViewPart implements IScriptListener {
 
 	protected IStormFrontClient client;
 	protected SWTScriptListener wrapper = new SWTScriptListener(this);
 	protected Composite main, scriptComposite;
 	protected ToolBar buttonsToolbar;
 	protected ToolItem pause, stop;
 	protected Label scriptNameLabel, durationLabel;
 	protected int duration = 0;
 	protected IScript currentScript;
 	
 	protected void updateCurrentClient ()
 	{
 		if (WarlockClientRegistry.getActiveClients().size() > 0)
 		{
 			IWarlockClient client = WarlockClientRegistry.getActiveClients().get(0);
 			if (client instanceof IStormFrontClient)
 			{
				this.client = (IStormFrontClient) client;
 			}
 		} else {
 			WarlockClientRegistry.addWarlockClientListener(new SWTWarlockClientListener(
 			new IWarlockClientListener() {
 				public void clientActivated(IWarlockClient client) {
 					if (client instanceof IStormFrontClient)
 					{
						ScriptControlView.this.client = (IStormFrontClient) client;
						ScriptControlView.this.client.addScriptListener(ScriptControlView.this.wrapper);
 					}
 				}
 				public void clientConnected(IWarlockClient client) {}
 				public void clientDisconnected(IWarlockClient client) {}
 				public void clientRemoved(IWarlockClient client) {}
 			}));
 		}
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		updateCurrentClient();
 		
 		main = new Composite(parent, SWT.NONE);
 		GridLayout mainLayout = new GridLayout(2, false);
 		mainLayout.marginHeight = mainLayout.marginWidth = 0;
 		main.setLayout(mainLayout);
 		main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		
 		Label label = new Label(main, SWT.NONE);
 		label.setText("Running Scripts");
 		label.setFont(JFaceResources.getBannerFont());
 	}
 
 	public void setFocus() {}
 	
 	public void scriptPaused(IScript script) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void scriptResumed(IScript script) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void scriptStarted(IScript script) {
 		duration = 0;
 		currentScript = script;
 		
 		updateScriptComposite(scriptComposite == null);
 		
 	}
 	
 	private void updateDuration ()
 	{
 		duration++;
 		
 		int hours = (int) Math.floor(((double)duration) / 3600.0);
 		int minutes = (int) Math.floor(((double) duration) / 60.0) % 60;
 		int seconds = duration - (minutes * 60) - (hours*3600);
 		String label = "" + seconds + "s";
 		
 		if (minutes > 0)
 			label = "" + minutes + "m " + label;
 		if (hours > 0)
 			label = "" + hours + "h " + label;
 		
 		final String finalLabel = label;
 		Display.getDefault().syncExec(new Runnable () {
 			public void run () {
 				durationLabel.setText("running: " + finalLabel);
 			}
 		});
 		
 	}
 	
 	private void resetControls ()
 	{
 		scriptNameLabel.setText("[" + currentScript.getName() + "]");
 		
 		pause.setImage(ScribeSharedImages.getImage(ScribeSharedImages.IMG_SUSPEND));
 		pause.setText("Pause");
 		stop.setImage(ScribeSharedImages.getImage(ScribeSharedImages.IMG_TERMINATE));
 		stop.setText("Stop");
 		pause.setEnabled(true);
 		stop.setEnabled(true);
 		durationLabel.setEnabled(true);
 		
 		durationLabel.setText("running: 0s      ");
 		
 		final Timer timer = new Timer();
 		timer.schedule(new TimerTask () {
 			public void run () {
 				if (currentScript.isRunning())
 				{
 					updateDuration();
 				} else timer.cancel(); 
 			}
 		}, 1000, 1000);
 	}
 	
 	private void updateScriptComposite (boolean create)
 	{
 		if (create) {
 			scriptComposite = new Composite(main, SWT.NONE);
 			GridLayout scriptLayout = new GridLayout(3, false);
 			scriptLayout.marginHeight = scriptLayout.marginWidth = 0;
 			scriptComposite.setLayout(scriptLayout);
 			scriptComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 			
 			scriptNameLabel = new Label(scriptComposite, SWT.NONE);
 			scriptNameLabel.setFont(JFaceResources.getDialogFont());
 			
 			buttonsToolbar = new ToolBar(scriptComposite, SWT.RIGHT | SWT.FLAT);
 			pause = new ToolItem(buttonsToolbar, SWT.PUSH);
 			stop = new ToolItem(buttonsToolbar, SWT.PUSH);
 			durationLabel = new Label(scriptComposite, SWT.NONE);
 		
 			pause.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					if (pause.getImage().equals(ScribeSharedImages.getImage(ScribeSharedImages.IMG_RESUME)))
 					{
 						pause.setImage(ScribeSharedImages.getImage(ScribeSharedImages.IMG_SUSPEND));
 						pause.setText("Pause");
 						currentScript.resume();
 					} else {
 						pause.setImage(ScribeSharedImages.getImage(ScribeSharedImages.IMG_RESUME));
 						pause.setText("Resume");
 						currentScript.suspend();
 					}
 				}
 			});
 			
 			stop.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					currentScript.stop();
 					
 					stop.setEnabled(false);
 					pause.setEnabled(false);
 					durationLabel.setText("finished");
 					durationLabel.setEnabled(false);
 				}
 			});
 		}
 		
 		resetControls();
 		
 		if (create) main.layout();
 	}
 	
 	public void scriptStopped(IScript script, boolean userStopped) {
 		// TODO Auto-generated method stub
 		
 	}
 }
