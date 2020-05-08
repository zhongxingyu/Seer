 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.ui;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Widget;
 
 import de.snertlab.xdccBee.irc.IrcServer;
 import de.snertlab.xdccBee.irc.listener.INotifyDccBotLogging;
 import de.snertlab.xdccBee.irc.listener.LogMessage;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerDccBotLogging;
 import de.snertlab.xdccBee.messages.XdccBeeMessages;
 
 /**
  * @author holgi
  *
  */
 public class InfoTabFolder extends CTabFolder implements INotifyDccBotLogging {
 
 	private Map<IrcServer, Widget> mapDebuggingTabs = new HashMap<IrcServer, Widget>();
 
 	public InfoTabFolder(Composite parent, int style) {
 		super(parent, style);
 		List<IrcServer> listIrcServers = Application.getServerSettings().getListServer();
 		NotifyManagerDccBotLogging.getNotifyManager().register(this);
 		for (IrcServer ircServer : listIrcServers) {
 			if(ircServer.isDebug()){
 				CTabItem tabItemDebugWindow = new CTabItem(this, SWT.NONE);
 				tabItemDebugWindow.setText( MessageFormat.format(XdccBeeMessages.getString("InfoTabFolder_TAB_DEBUG"), new Object[]{ ircServer.getHostname() } ) ); //$NON-NLS-1$
 				Composite compDebug = new Composite(this, SWT.NONE);
 				compDebug.setLayout(new GridLayout());
 				compDebug.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
 				StyledText txtDebug = new StyledText(compDebug, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
 				txtDebug.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
 				tabItemDebugWindow.setControl(compDebug);
 				makeTxtDebugContextMenu(txtDebug);
 				mapDebuggingTabs.put(ircServer, txtDebug);
 			}
 		}
 	}
 
 	public void writeLog(IrcServer ircServer, final LogMessage log) {
 		final StyledText txtDebug2 = (StyledText)mapDebuggingTabs.get(ircServer);
		if(txtDebug2==null) return;
 		if(txtDebug2.isDisposed()) return;
 		txtDebug2.getDisplay().asyncExec( new Runnable() {
 			@Override
 			public void run() {			
 				Color logColor = txtDebug2.getShell().getDisplay().getSystemColor(log.getLogColor());
 				int startIndex = txtDebug2.getText().length();
 				int length = log.getLogText().length();
 				txtDebug2.append(log.getLogText()+"\n");  //$NON-NLS-1$
 				txtDebug2.setTopIndex(txtDebug2.getLineCount());
 				txtDebug2.setStyleRange(new StyleRange(startIndex,length, logColor, null));
 			}
 		});
 	}
 	@Override
 	public void notifyDccBotLogging(final IrcServer ircServer, final LogMessage log) {
 		writeLog(ircServer, log);
 	}
 	
 	public void makeTxtDebugContextMenu(final StyledText txtDebug){
 		Menu mnuDebugTxt = new Menu(txtDebug);
 		txtDebug.setMenu(mnuDebugTxt);
 
 		final MenuItem mntmClearText = new MenuItem(mnuDebugTxt, SWT.CASCADE);
 		mntmClearText.setText(XdccBeeMessages.getString("DEBUG_CLEAR_TEXT")); //$NON-NLS-1$
 		mntmClearText.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				txtDebug.setText("");
 			}
 		});
 	}
 	
 }
