 /*******************************************************************************
  * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.LinkedList;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IErrorReport;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IProcessesV1;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tm.internal.terminal.control.ITerminalListener;
 import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
 import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
 import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
 import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
 import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
 import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
 import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.AbstractConsole;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.part.IPageBookViewPage;
 import org.eclipse.ui.part.Page;
 
 @SuppressWarnings("restriction")
 class TCFConsole extends AbstractConsole {
     public static final int
         TYPE_PROCESS_CONSOLE = 1,
         TYPE_PROCESS_TERMINAL = 2,
         TYPE_UART_TERMINAL = 3,
         TYPE_CMD_LINE = 4,
         TYPE_DPRINTF = 5;
 
     private static int page_id_cnt = 0;
 
     private final TCFModel model;
     private final Display display;
     private final String ctx_id;
     private final int type;
 
     private final LinkedList<ViewPage> pages = new LinkedList<ViewPage>();
     private final LinkedList<Message> history = new LinkedList<Message>();
 
     private static class Message {
         int stream_id;
         byte[] data;
     }
 
     private class ViewPage extends Page implements ITerminalConnector, ITerminalListener {
 
         private final String page_id = "Page-" + page_id_cnt++;
         private final LinkedList<Message> inp_queue = new LinkedList<Message>();
 
         private final OutputStream out_stream = new OutputStream() {
             @Override
             public void write(final int b) throws IOException {
                 if (ctx_id == null) return;
                 Protocol.invokeAndWait(new Runnable() {
                     public void run() {
                         try {
                             String s = "" + (char)b;
                             byte[] buf = s.getBytes("UTF8");
                             model.getLaunch().writeProcessInputStream(ctx_id, buf, 0, buf.length);
                         }
                         catch (Exception x) {
                             model.onProcessStreamError(ctx_id, 0, x, 0);
                         }
                     }
                 });
             }
         };
 
         private ITerminalViewControl view_control;
         private OutputStream rtt;
         private int ws_col;
         private int ws_row;
 
         private final Thread inp_thread = new Thread() {
             public void run() {
                 try {
                     for (;;) {
                         Message m = null;
                         synchronized (inp_queue) {
                             while (inp_queue.size() == 0) inp_queue.wait();
                             m = inp_queue.removeFirst();
                         }
                         if (m.data == null) break;
                         if (type == TYPE_PROCESS_CONSOLE) {
                             String s = "\u001b[30m";
                             switch (m.stream_id) {
                             case 1: s = "\u001b[31m"; break;
                             case 2: s = "\u001b[34m"; break;
                             case 3: s = "\u001b[32m"; break;
                             }
                             rtt.write(s.getBytes("UTF8"));
                             for (int i = 0; i < m.data.length; i++) {
                                 int ch = m.data[i] & 0xff;
                                 if (ch == '\n') rtt.write('\r');
                                 rtt.write(ch);
                             }
                         }
                        else if (type == TYPE_DPRINTF) {
                            int i = 0;
                            for (int j = 0; j < m.data.length; j++) {
                                if (m.data[j] == '\n') {
                                    rtt.write(m.data, i, j - i);
                                    rtt.write('\r');
                                    i = j;
                                }
                            }
                            rtt.write(m.data, i, m.data.length - i);
                        }
                         else {
                             rtt.write(m.data);
                         }
                     }
                 }
                 catch (Throwable x) {
                     Activator.log("Cannot write console output", x);
                 }
             }
         };
 
         @Override
         public void createControl(Composite parent) {
             assert view_control == null;
             view_control = TerminalViewControlFactory.makeControl(this, parent, null);
             view_control.setConnector(this);
             view_control.connectTerminal();
         }
 
         @Override
         public void dispose() {
             if (view_control != null) {
 
                 // TODO: need a way to stop terminal update timer, see PollingTextCanvasModel
                 // It the timer is not stopped, it causes memory leak
 
                 view_control.disposeTerminal();
                 view_control.setConnector(null);
                 view_control = null;
             }
         }
 
         @Override
         public Control getControl() {
             return view_control.getRootControl();
         }
 
         @Override
         public void setActionBars(IActionBars actionBars) {
         }
 
         @Override
         public void setFocus() {
             view_control.setFocus();
         }
 
         @Override
         @SuppressWarnings("rawtypes")
         public Object getAdapter(Class adapter) {
             return null;
         }
 
         @Override
         public void setTerminalSize(final int w, final int h) {
             if (ws_col == w && ws_row == h) return;
             ws_col = w;
             ws_row = h;
             if (type != TYPE_PROCESS_TERMINAL) return;
             Protocol.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     final TCFLaunch launch = model.getLaunch();
                     if (launch.isProcessExited()) return;
                     final IChannel channel = launch.getChannel();
                     if (channel.getState() != IChannel.STATE_OPEN) return;
                     IProcessesV1 prs = channel.getRemoteService(IProcessesV1.class);
                     if (prs == null) return;
                     prs.setWinSize(ctx_id, w, h, new IProcessesV1.DoneCommand() {
                         @Override
                         public void doneCommand(IToken token, Exception error) {
                             if (error == null) return;
                             if (launch.isProcessExited()) return;
                             if (channel.getState() != IChannel.STATE_OPEN) return;
                             if (error instanceof IErrorReport && ((IErrorReport)error).getErrorCode() == IErrorReport.TCF_ERROR_INV_COMMAND) return;
                             Activator.log("Cannot set process TTY window size", error);
                         }
                     });
                 }
             });
         }
 
         @Override
         public void save(ISettingsStore store) {
         }
 
         @Override
         public ISettingsPage makeSettingsPage() {
             return null;
         }
 
         @Override
         public void load(ISettingsStore store) {
         }
 
         @Override
         public boolean isLocalEcho() {
             return false;
         }
 
         @Override
         public boolean isInitialized() {
             return true;
         }
 
         @Override
         public boolean isHidden() {
             return false;
         }
 
         @Override
         public OutputStream getTerminalToRemoteStream() {
             return out_stream;
         }
 
         @Override
         public String getSettingsSummary() {
             return null;
         }
 
         @Override
         public String getName() {
             return TCFConsole.this.getName();
         }
         @Override
         public String getInitializationErrorMessage() {
             return null;
         }
 
         @Override
         public String getId() {
             return page_id;
         }
 
         @Override
         public void disconnect() {
             Protocol.invokeAndWait(new Runnable() {
                 @Override
                 public void run() {
                     pages.remove(ViewPage.this);
                     synchronized (inp_queue) {
                         inp_queue.add(new Message());
                         inp_queue.notify();
                     }
                 }
             });
         }
 
         @Override
         public void connect(ITerminalControl term_control) {
             try {
                 term_control.setState(TerminalState.CONNECTING);
                 term_control.setEncoding("UTF8");
                 rtt = term_control.getRemoteToTerminalOutputStream();
                 Protocol.invokeAndWait(new Runnable() {
                     @Override
                     public void run() {
                         pages.add(ViewPage.this);
                         for (Message m : history) inp_queue.add(m);
                         inp_thread.setName("TCF Console Input");
                         inp_thread.start();
                     }
                 });
                 term_control.setState(TerminalState.CONNECTED);
             }
             catch (Exception x) {
                 Activator.log("Cannot connect a terminal", x);
                 term_control.setState(TerminalState.CLOSED);
             }
         }
 
         @Override
         public void setState(TerminalState state) {
         }
 
         @Override
         public void setTerminalTitle(String title) {
         }
     }
 
     TCFConsole(final TCFModel model, int type, String ctx_id) {
         super(getViewName(type, ctx_id), null, getImageDescriptor(ctx_id), false);
         this.model = model;
         this.type = type;
         this.ctx_id = ctx_id;
         display = model.getDisplay();
         model.asyncExec(new Runnable() {
             public void run() {
                 try {
                     if (PlatformUI.getWorkbench().isClosing()) {
                         return;
                     }
                     if (!PlatformUI.isWorkbenchRunning() || PlatformUI.getWorkbench().isStarting()) {
                         display.timerExec(200, this);
                         return;
                     }
                     IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                     manager.addConsoles(new IConsole[]{ TCFConsole.this });
                     manager.showConsoleView(TCFConsole.this);
                 }
                 catch (Throwable x) {
                     Activator.log("Cannot open Console view", x);
                 }
             }
         });
     }
 
     private static ImageDescriptor getImageDescriptor(String ctx_id) {
         String image = ctx_id != null ? ImageCache.IMG_PROCESS_RUNNING : ImageCache.IMG_TCF;
         return ImageCache.getImageDescriptor(image);
     }
 
     private static String getViewName(int type, String name) {
         String title = "TCF";
         switch (type) {
         case TYPE_PROCESS_CONSOLE:
             title += " Debug Process Console - " + name;
             break;
         case TYPE_PROCESS_TERMINAL:
             title += " Debug Process Terminal - " + name;
             break;
         case TYPE_UART_TERMINAL:
             title += " Debug Virtual Terminal - " + name;
             break;
         case TYPE_CMD_LINE:
             title += " Debugger Command Line";
             break;
         case TYPE_DPRINTF:
             title += " Debugger Dynamic Print";
             break;
         }
         return title;
     }
 
     void onModelConnected() {
         if (ctx_id != null) {
             // Change view title to include context name instead of context ID
             Protocol.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     if (!model.createNode(ctx_id, this)) return;
                     TCFNode node = (TCFNode)model.getNode(ctx_id);
                     if (node instanceof TCFNodeExecContext) {
                         TCFNodeExecContext exe = (TCFNodeExecContext)node;
                         TCFDataCache<IRunControl.RunControlContext> ctx_cache = exe.getRunContext();
                         if (!ctx_cache.validate(this)) return;
                         IRunControl.RunControlContext ctx_data = ctx_cache.getData();
                         if (ctx_data != null && ctx_data.getName() != null) {
                             final String name = ctx_data.getName();
                             model.asyncExec(new Runnable() {
                                 @Override
                                 public void run() {
                                     setName(getViewName(type, name));
                                 }
                             });
                         }
                     }
                 }
             });
         }
     }
 
     void write(final int stream_id, byte[] data) {
         assert Protocol.isDispatchThread();
         if (data == null || data.length == 0) return;
         Message m = new Message();
         m.stream_id = stream_id;
         m.data = data;
         history.add(m);
         if (history.size() > 1000) history.removeFirst();
         for (ViewPage p : pages) {
             synchronized (p.inp_queue) {
                 p.inp_queue.add(m);
                 p.inp_queue.notify();
             }
         }
     }
 
     void close() {
         assert Protocol.isDispatchThread();
         Message m = new Message();
         for (ViewPage p : pages) {
             synchronized (p.inp_queue) {
                 p.inp_queue.add(m);
                 p.inp_queue.notify();
             }
         }
         model.asyncExec(new Runnable() {
             public void run() {
                 IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                 manager.removeConsoles(new IConsole[]{ TCFConsole.this });
             }
         });
     }
 
     @Override
     public IPageBookViewPage createPage(IConsoleView view) {
         return new ViewPage();
     }
 }
