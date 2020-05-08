 package com.iteye.weimingtom.luaprofiler;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * 
  * @author weimingtom
  * 
  */
 public class GUIMain {
     private final static int SHELL_WIDTH = 400;
     private final static int SHELL_HEIGHT = 400;
     private final static String ICON_NAME = "icon.gif";
     private final static int TIMER_INTERVAL = 200;
     public transient Shell shell;
     public transient CTabFolder tabFolder;
     public transient CTabItem logItem;
     public transient CTabItem settingItem;
     public transient LogTab logTab;
     public transient CTabItem sourceViewItem;
     public transient SourceViewTab sourceViewTab;
     public transient Shell dlgImme;
     public transient Button button1;
     public transient Button button2;
     public transient Button button3;
     
     public final static String COMMAND = "lua";
     public final static String LIB_NAME = "debughook";
     public static String scriptFileName = null; //= "test/factorial.lua";
     private StringBuffer outputBuffer = new StringBuffer();
     private Process proc;
     
     public GUIMain() {
 
     }
 
     /**
      * 
      * @throws IOException
      */
     public void init() throws IOException {
 	final Display display = new Display();
 	shell = new Shell(display, SWT.MAX | SWT.MIN | SWT.CLOSE | SWT.TITLE
 		| SWT.RESIZE);
 	shell.setText("SimpleLuaProfiler");
 	shell.setLayout(new FormLayout());
 	shell.setImage(new Image(Display.getCurrent(), GUIMain.class
 		.getResourceAsStream(ICON_NAME)));
 	tabFolder = new CTabFolder(shell, SWT.NONE);
 	tabFolder.setLayoutData(new GridData(1808));
 
 	sourceViewItem = new CTabItem(tabFolder, 0);
 	sourceViewItem.setText("Sources");
 	sourceViewTab = new SourceViewTab(tabFolder);
 	sourceViewItem.setControl(sourceViewTab);
 
 	logItem = new CTabItem(tabFolder, 0);
 	logTab = new LogTab(tabFolder);
 	logItem.setControl(logTab);
 	logItem.setText("Logs");
 	
 	/*
 	settingItem = new CTabItem(tabFolder, 0);
 	settingItem.setText("Settings");
 	*/
 	button1 = new Button(shell, SWT.PUSH);
 	button1.setText("&Stop and clean log");
 	button1.addListener(SWT.Selection, new Listener() {
 	    @Override
 	    public void handleEvent(final Event event) {
 		killCommand();
 		sourceViewTab.reset();
 		logTab.logOutput.setText("");
 	    }
 	});
 	button2 = new Button(shell, SWT.PUSH);
 	button2.setText("&Run lua script");
 	button2.addListener(SWT.Selection, new Listener() {
 	    @Override
 	    public void handleEvent(final Event event) {
 		runCommand();
 	    }
 	});
 	button3 = new Button(shell, SWT.PUSH);
 	button3.setText("&Open lua script");
 	button3.addListener(SWT.Selection, new Listener() {
 	    @Override
 	    public void handleEvent(final Event event) {
 		FileDialog dialog = new FileDialog (shell, SWT.OPEN);
 		String [] filterNames = new String [] {"Lua Script Files(*.lua)", "All Files (*)"};
 		String [] filterExtensions = new String [] {"*.lua", "*"};
 		String filterPath = "/";
 		String platform = SWT.getPlatform();
 		if (platform.equals("win32") || platform.equals("wpf")) {
 			filterNames = new String [] {"Lua Script Files(*.lua)", "All Files (*.*)"};
 			filterExtensions = new String [] {"*.lua", "*.*"};
 			//filterPath = "c:\\";
 			filterPath = ".";
 		}
 		dialog.setFilterNames (filterNames);
 		dialog.setFilterExtensions (filterExtensions);
 		dialog.setFilterPath (filterPath);
 		//System.out.println ("Open " + dialog.open ());
 		String filename = dialog.open();
 		if (filename != null) {
 		    scriptFileName = filename;
 		    shell.setText("SimpleLuaProfiler - " + scriptFileName);
 		    killCommand();
 		    sourceViewTab.reset();
 		    logTab.logOutput.setText("");
 		}
 	    }
 	});
 	// Layout
 	FormData data;
 	data = new FormData();
 	data.bottom = new FormAttachment(100, -5);
 	data.right = new FormAttachment(100, -5);
 	button1.setLayoutData(data);
 	data = new FormData();
 	data.bottom = new FormAttachment(100, -5);
 	data.right = new FormAttachment(button1, -5);
 	button2.setLayoutData(data);
 	data = new FormData();
 	data.bottom = new FormAttachment(100, -5);
 	data.right = new FormAttachment(button2, -5);
 	button3.setLayoutData(data);
 	data = new FormData();
 	data.bottom = new FormAttachment(100, -10);
 	data.left = new FormAttachment(0, 5);
 	data = new FormData();
 	data.top = new FormAttachment(0, 5);
 	data.left = new FormAttachment(0, 5);
 	data.bottom = new FormAttachment(button1, -5);
 	data.right = new FormAttachment(100, -5);
 	tabFolder.setLayoutData(data);
 	display.timerExec(TIMER_INTERVAL, new Runnable() {
 	    @Override
 	    public void run() {
 		synchronized (outputBuffer) {
 		    if (outputBuffer.length() > 0) {
 			logTab.logOutput.setText("");
 			checkSource(outputBuffer.toString());
 			logTab.logOutput.append(outputBuffer.toString());
 			outputBuffer.setLength(0);
 		    }
 		}
 		display.timerExec(TIMER_INTERVAL, this);
 	    }
 	});
 	setShellCenter(SHELL_WIDTH, SHELL_HEIGHT);
 	shell.open();
 	while (!shell.isDisposed()) {
 	    if (!display.readAndDispatch()) {
 		display.sleep();
 	    }
 	}
     }
 
     /**
      * @param posX
      * @param posY
      */
     public final void setShellCenter(final int posX, final int posY) {
 	final Rectangle rect = shell.getDisplay().getBounds();
 	if (rect.width > posX) {
 	    if (rect.height > posY) {
 		shell.setSize(posX, posY);
 		shell.setLocation((rect.width - posX) / 2,
 			(rect.height - posY) / 2);
 	    } else {
 		shell.setSize(posX, posY);
 		shell.setLocation((rect.width - posX) / 2, rect.height);
 	    }
 	} else if (rect.height > posY) {
 	    shell.setSize(posX, posY);
 	    shell.setLocation(rect.width, (rect.height - posY) / 2);
 	} else {
 	    shell.setSize(posX, posY);
 	    shell.setLocation(rect.width, rect.height);
 	}
     }
 
     /**
      * @param args
      */
     public final static void main(final String[] args) {
 	final GUIMain gui = new GUIMain();
 	try {
 	    gui.init();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    final Display display = Display.getCurrent();
 	    if (display != null) {
 		final MessageBox messageBox = new MessageBox(
 			new Shell(display), 33);
 		messageBox.setText("Error");
 		messageBox.setMessage(e.toString());
 		messageBox.open();
 	    }
 	} finally {
 	    gui.killCommand();
 	}
     }
     
     public void runCommand() {
 	if (proc == null) {
 	    final String[] commands = { COMMAND,
 		    "-l",
 		    LIB_NAME,
 		    scriptFileName };
 	    final ProcessBuilder pb = new ProcessBuilder(commands);
 	    Thread thread = new Thread(new Runnable() {
 		@Override
 		public void run() {
 		    try {
 			proc = pb.start();
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			StreamCopyThread t1 = new StreamCopyThread(
 				"stderr for " + commands,
 				proc.getErrorStream(), out, outputBuffer);
 			StreamCopyThread t2 = new StreamCopyThread(
 				"stdout for " + commands,
 				proc.getInputStream(), out, outputBuffer);
 			t1.start();
 			t2.start();
 			proc.getOutputStream().close();
 			proc.waitFor();
 			t1.join();
 			t2.join();
 			proc = null;
 		    } catch (IOException e) {
 			e.printStackTrace();
 		    } catch (InterruptedException e) {
 			e.printStackTrace();
 		    }
 		}
 	    });
 	    thread.start();
 	}
     }
     
     /**
      * @see http://en.w3support.net/index.php?db=so&id=63758
      * @see http://man.ddvip.com/program/java_api_zh/java/lang/Process.html
      */
     public void killCommand() {
 	if (proc != null) {
 	    proc.destroy();
 	    proc = null;
 	}
     }
 
     public void checkSource(String source) {
 	int startIndex = 0;
 	int endIndex = 0;
 	String tag = "[DEBUG]";
 	int tagLength = tag.length();
 	List lines = new ArrayList();
 	while(true) {
 	    endIndex = source.indexOf(tag, startIndex);
 	    if(endIndex == -1) {
 		break;
 	    } else {
 		startIndex = endIndex + tagLength;
 		int endLineIndex = source.indexOf('\r', startIndex);
 		if (endLineIndex != -1) {
 		    //System.out.println(startIndex + "," + endLineIndex);
 		    String line = source.substring(startIndex, endLineIndex);
 		    lines.add(line);
 		} else {
 		    String line = source.substring(endIndex);
 		    lines.add(line);
 		}	
 	    }
 	}
 	sourceViewTab.gotoLine(getLastFileName(lines), getLastLine(lines));
 	sourceViewTab.updateHit(getAllHit(scriptFileName, lines));
     }
     
     private String getLastFileName(List lines) {
 	if(lines != null && lines.size() > 0) {
 	    String str = (String) lines.get(lines.size() - 1);
 	    String[] result = str.split(":");
 	    if(result != null && result.length == 2) {
 		String fileName = result[0];
 		return fileName;
 	    }
 	}
 	return null;
     }
     
     private int getLastLine(List lines) {
 	if(lines != null && lines.size() > 0) {
 	    String str = (String) lines.get(lines.size() - 1);
 	    String[] result = str.split(":");
	    if(result != null) {
		if (result.length == 2) {
		    String lineno = result[1];
		    return Integer.parseInt(lineno) - 1;
		}
		if (result.length == 3) {
		    //FIXME: Windows C:\\
		    String lineno = result[2];
		    return Integer.parseInt(lineno) - 1;
		}
 	    }
 	}
 	return 0;
     }
     
     private List getAllHit(String findFileName, List lines) {
 	List list = new ArrayList();
 	Iterator it = lines.iterator();
 	while (it.hasNext()) {
 	    String str = (String) it.next();
 	    String[] result = str.split(":");
 	    String fileName = null;
 	    Integer lineno = null;
 	    if (result != null &&  
 		result[0].startsWith("@")) {
 		if (result.length == 2) {
 		    fileName = result[0].substring(1);
 		    lineno = Integer.parseInt(result[1]) - 1;
 		} else if (result.length == 3) {
 		    //prefix @
 		    //windows C:\\
 		    fileName = (result[0] + ":" + result[1]).substring(1);
 		    lineno = Integer.parseInt(result[2]) - 1;
 		}
 	    }
 	    if (fileName != null && lineno != null) {
 		//FIXME:
 		if (fileName.equals(findFileName)) {
 		    //System.out.println("lineno:" + lineno);
 		    list.add(lineno);
 		}
 	    }
 	}
 	return list;
     }
 }
