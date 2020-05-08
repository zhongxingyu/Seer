 package com.simpoir.elixy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 
 import org.zkoss.lang.Threads;
 import org.zkoss.zk.ui.Desktop;
 import org.zkoss.zk.ui.Executions;

import com.simpoir.zk.vt.VirtTerm;
 
 public class ThreadedConsole extends Thread {
 
 	private final Desktop _desktop;
 	private final Process _proc;
 	private final BufferedReader _is;
 	private final int i = 0;
	private final VirtTerm _console;
 	private final OutputStream _os;
 
	public ThreadedConsole(Desktop desktop, VirtTerm console, Process proc) {
 		_desktop = desktop;
 		_proc = proc;
 		_is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 		_os = proc.getOutputStream();
 		_console = console;
 		desktop.setAttribute("console", this);
 	}
 
 
 
 	@Override
 	public void run() {
 		try {
 			while (true) {
 				if (_desktop == null || !_desktop.isServerPushEnabled()) {
 					return;
 				}
 				Executions.activate(_desktop);
 				try {
 					readConsole();
 				} finally {
 					Executions.deactivate(_desktop);
 				}
 				Threads.sleep(2000);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			_desktop.enableServerPush(false);
 			if (_proc != null) {
 				_proc.destroy();
 			}
 		}
 	}
 
 	protected void readConsole() throws IOException {
 		if (_is.ready()) {
 			char[] buf = new char[1024];
 			int len = _is.read(buf);
 			if (len != 0) {
				_console.setText(new String(buf));
 			}
 		}
 	}
 
 	public void write(byte[] ch) {
 		try {
 			_os.write(ch);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
