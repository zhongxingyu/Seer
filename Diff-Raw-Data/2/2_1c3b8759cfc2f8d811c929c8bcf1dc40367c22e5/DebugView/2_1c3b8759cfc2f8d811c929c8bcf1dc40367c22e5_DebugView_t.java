 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.rcp.views;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockClientAdapter;
 import cc.warlock.core.client.WarlockClientRegistry;
 import cc.warlock.core.network.IConnection;
 import cc.warlock.core.network.IConnectionListener;
 import cc.warlock.core.network.IConnection.ErrorType;
 import cc.warlock.rcp.ui.WarlockText;
 import cc.warlock.rcp.ui.network.SWTConnectionListenerAdapter;
 
 public class DebugView extends ViewPart implements IConnectionListener, IGameViewFocusListener {
 
 	protected PageBook book;
 	protected Text entry;
 	protected Button copyAll;
 	private HashMap<IWarlockClient, WarlockText> clientStreams = new HashMap<IWarlockClient, WarlockText>();
 	private IWarlockClient client;
 	
 	public static final String VIEW_ID = "cc.warlock.rcp.views.DebugView";
 	
 	public DebugView() {
 		// Add listeners to existing clients
 		for(IWarlockClient client : WarlockClientRegistry.getActiveClients()) {
 			IConnection conn = client.getConnection();
 			if(conn != null)
 				conn.addConnectionListener(new SWTConnectionListenerAdapter(DebugView.this));
 		}
 		
 		// Add listeners to future clients
 		WarlockClientRegistry.addWarlockClientListener(new WarlockClientAdapter() {
 			public void clientConnected(final IWarlockClient client) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run () {
 						setClient(client);
 						client.getConnection().addConnectionListener(new SWTConnectionListenerAdapter(DebugView.this));
 					}
 				});
 			}
 		});
		
		GameView.addGameViewFocusListener(this);
 	}
 	
 	public void setClient(IWarlockClient client) {
 		this.client = client;
 		book.showPage(getTextForClient(client).getTextWidget());
 	}
 	
 	private void debug (IWarlockClient client, String message)
 	{
 		WarlockText console = getTextForClient(client);
 		console.append(message);
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		Composite main = new Composite(parent, SWT.NONE);
 		main.setLayout(new GridLayout(1, false));
 
 		copyAll = new Button(main, SWT.PUSH);
 		copyAll.setText("Copy All");
 		
 		book = new PageBook(main, SWT.NONE);
 		book.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		
 		copyAll.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				getTextForClient(client).selectAll();
 				getTextForClient(client).copy();
 			}
 		});
 		
 		entry = new Text(main, SWT.BORDER);
 		entry.addKeyListener(new KeyAdapter () {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (e.keyCode == SWT.CR)
 				{
 					sendRawText();
 				}
 			}
 		});
 		entry.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		GameView view = GameView.getGameViewInFocus();
 		if(view != null) {
 			setClient(view.getWarlockClient());
 		}
 	}
 	
 	protected void sendRawText ()
 	{
 		String toSend = entry.getText() + "\n";
 		try {
 			client.getConnection().send(toSend.getBytes());
 			entry.setText("");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		getTextForClient(client).setFocus();
 	}
 	
 	public void connected(IConnection connection) {
 		debug (connection.getClient(), "connected");
 	}
 	
 	public void dataReady(IConnection connection, String line) {
 		debug (connection.getClient(), line);
 	}
 	
 	public void disconnected(IConnection connection) {
 		debug (connection.getClient(), "disconnected");
 	}
 	
 	public void connectionError(IConnection connection, ErrorType errorType) {
 		debug (connection.getClient(), "error: " + errorType.toString());
 	}
 	
 	public void gameViewFocused(GameView gameView) {
 		setClient(gameView.getWarlockClient());
 	}
 	
 	public WarlockText getTextForClient(IWarlockClient client) {
 		if (!clientStreams.containsKey(client))
 		{
 			// TODO move this section into WarlockText
 			WarlockText text = new WarlockText(book, SWT.V_SCROLL, client);
 			GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
 			text.setLayoutData(data);
 			text.setEditable(false);
 			text.setWordWrap(true);
 			text.getTextWidget().setIndent(1);
 			
 			text.setScrollDirection(SWT.DOWN);
 			
 			clientStreams.put(client, text);
 			return text;
 		}
 		else return clientStreams.get(client);
 	}
 }
