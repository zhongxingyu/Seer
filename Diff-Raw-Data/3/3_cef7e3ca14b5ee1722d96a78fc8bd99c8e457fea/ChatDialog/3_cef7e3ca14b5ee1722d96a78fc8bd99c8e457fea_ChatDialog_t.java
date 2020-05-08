 package com.cahoots.eclipse.collab;
 
import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import com.cahoots.connection.CahootsConnection;
 import com.cahoots.connection.websocket.CahootsSocket;
 import com.cahoots.eclipse.Activator;
 import com.cahoots.json.receive.ChatReceiveMessage;
 import com.cahoots.json.send.ChatSendMessage;
 
 public class ChatDialog  extends Dialog{
 
 	private Object result;
 	private Shell dialog;
 	private CahootsSocket socket;
 	private CahootsConnection connection;
 	private Text text;
 	private StyledText styledText;
 	private String to;
 	
 	public ChatDialog(Shell parent, String to) {
 		super(parent, SWT.DIALOG_TRIM);
 		socket = Activator.getInjector().getInstance(CahootsSocket.class);
 		connection = Activator.getInjector().getInstance(CahootsConnection.class);
		this.to = to;
 	}
 	
 	public Object open() {
 		createContents();
 		dialog.open();
 		dialog.layout();
 		Display display = getParent().getDisplay();
 		while (!dialog.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	private void createContents() {
 		dialog = new Shell(getParent(), getStyle());
 		
 		styledText = new StyledText(dialog, SWT.BORDER | SWT.READ_ONLY);
 		styledText.setBounds(0, 0, 444, 217);
 		
 		text = new Text(dialog, SWT.BORDER);
 		text.setBounds(0, 223, 363, 48);
 		
 		text.addKeyListener(new KeyListener() {
 			
 			@Override
 			public void keyReleased(KeyEvent e) {
 				
 			}
 			
 			@Override
 			public void keyPressed(KeyEvent e) {
 				//TODO see if this is the right keycode in both windows and linux
 				if(e.keyCode == '\r')
 				{
 					sendMessage();
 				}
 			}
 		});
 		
 		Button btnSend = new Button(dialog, SWT.NONE);
 		btnSend.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				sendMessage();
 			}
 		});
 		btnSend.setBounds(369, 223, 75, 48);
 		btnSend.setText("Send");
 		
 	}
 	
 	private void sendMessage()
 	{
 		String msg = text.getText().trim();
 		if(msg.length() > 0)
 		{
 			socket.send(new ChatSendMessage(connection.getUsername(), to, new Date(), msg));
 		}
 		text.setText("");
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
 		String time = df.format(new Date());
 		styledText.append(time + " " + connection.getUsername() + " " + msg);
 		
 	}
 	
 	public void receiveMessage(ChatReceiveMessage msg)
 	{
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
 		String time = df.format(msg.getDate());
 		styledText.append(time + " " + msg.getFrom() + " " + msg.getMessage());
 	}
 }
