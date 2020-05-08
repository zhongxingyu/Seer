 package net.mms_projects.copyit.ui.swt.forms.login_dialogs;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.UUID;
 
 import net.mms_projects.copyit.LoginResponse;
 import net.mms_projects.copyit.Settings;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.LocationEvent;
 import org.eclipse.swt.browser.LocationListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Shell;
 
 public class AutoLoginDialog extends AbstractLoginDialog {
 
 	private Settings settings;
 
 	public AutoLoginDialog(Shell parent, Settings settings) {
 		super(parent);
 
 		this.settings = settings;
 
 		this.setText("Automatic setup");
 	}
 
 	@Override
 	protected void createContents() {
 		/*
 		 * Definitions
 		 */
 
 		// Shell
 		this.shell = new Shell(this.getParent());
 		// Browser
 		Browser browser = new Browser(this.shell, SWT.NONE);
 
 		/*
 		 * Layout and settings
 		 */
 
 		// Window
 		this.shell.setSize(800, 600);
 		this.shell.setText(getText());
 		this.shell.setLayout(new FillLayout(SWT.HORIZONTAL));
 		// Browser
 		browser.setUrl(this.settings.get("server.baseurl")
 				+ "/app-setup/setup?device_password=" + this.getPassword());
 
 		/*
 		 * Listeners
 		 */
 
 		// Browser
 		browser.addLocationListener(new LocationListener() {
 			@Override
 			public void changing(LocationEvent event) {
 				URL location = null;
 				try {
 					location = new URL(event.location);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
					return;
 				}
 
				System.out.println("Changing: " + location.getPath());
 
 				if (location.getPath().startsWith("/app-setup/done/")) {
 					LoginResponse response = new LoginResponse();
 					response.deviceId = UUID.fromString(location.getPath()
 							.substring(16));
 					response.devicePassword = AutoLoginDialog.this
 							.getPassword();
 					AutoLoginDialog.this.setResponse(response);
 				}
 			}
 
 			@Override
 			public void changed(LocationEvent event) {
				URL location = null;
				try {
					location = new URL(event.location);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return;
				}

				System.out.println("Changed: " + location.getPath());

				if (location.getPath().startsWith("/app-setup/done/")) {
					LoginResponse response = new LoginResponse();
					response.deviceId = UUID.fromString(location.getPath()
							.substring(16));
					response.devicePassword = AutoLoginDialog.this
							.getPassword();
					AutoLoginDialog.this.setResponse(response);
				}
 			}
 		});
 	}
 
 	@Override
 	protected void updateForm() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
