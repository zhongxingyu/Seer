 package com.cahoots.eclipse.collab;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import com.cahoots.connection.CahootsConnection;
 import com.cahoots.connection.ConnectionDetails;
 import com.cahoots.connection.websocket.CahootsSocket;
 import com.cahoots.eclipse.Activator;
 import com.google.inject.Injector;
 
 public class DisconnectDialog extends Window {
 
 	private CahootsConnection cahootsConnection;
 	private CahootsSocket cahootsSocket;
 
 	/**
 	 * Create the dialog.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public DisconnectDialog(final Shell parent) {
 		super(parent);
 
 		setShellStyle((getShellStyle() | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
 				& ~(SWT.RESIZE | SWT.MAX));
 		final Injector injector = Activator.getInjector();
 		cahootsConnection = injector.getInstance(CahootsConnection.class);
 		cahootsSocket = injector.getInstance(CahootsSocket.class);
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 */
 	@Override
 	protected Composite createContents(final Composite parent) {
 		final Shell shell = getShell();
 		shell.setSize(262, 91);
 		shell.setText("Cahoots - Disconnect");
 		shell.setLayout(null);
 
 		final Composite content = parent;
 		// Disconnect label
 		final Label disconnectLabel = new Label(content, SWT.NONE);
 		disconnectLabel.setBounds(10, 10, 244, 15);
 		disconnectLabel.setText("Would you like to disconnect from Cahoots?");
 
 		// Disconnect button
 		final Button disconnect = new Button(content, SWT.NONE);
 		disconnect.setBounds(98, 31, 75, 25);
 		disconnect.setText("Disconnect");
 		disconnect.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				final HttpClient client = new HttpClient();
 				final PostMethod method = new PostMethod("http://"
 						+ cahootsConnection.getServer() + "/app/logout");
 				final List<NameValuePair> data = new LinkedList<NameValuePair>();
 				data.add(new NameValuePair("auth_token", cahootsConnection
 						.getAuthToken()));
 
 				method.setRequestBody(data.toArray(new NameValuePair[data
 						.size()]));
 				try {
					client.executeMethod(method);
 					performDisconnect();
 				} catch (final HttpException ex) {
 					MessageDialog.openInformation(
 							null,
 							"Disconnect Error",
 							"Error disconnecting from server. "
 									+ ex.getMessage());
 				} catch (final IOException ex) {
 					MessageDialog.openInformation(
 							null,
 							"Disconnect Error",
 							"Error disconnecting from server. "
 									+ ex.getMessage());
 				}
 			}
 
 			private void performDisconnect() {
 				final ConnectionDetails details = new ConnectionDetails("", "", "",
 						"");
 				cahootsConnection.updateConnectionDetails(details);
 				cahootsSocket.disconnect();
 				close();
 			}
 		});
 
 		// Cancel button
 		final Button cancel = new Button(content, SWT.NONE);
 		cancel.setBounds(179, 31, 75, 25);
 		cancel.setText("Cancel");
 		cancel.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				shell.close();
 			}
 		});
 		return content;
 	}
 }
