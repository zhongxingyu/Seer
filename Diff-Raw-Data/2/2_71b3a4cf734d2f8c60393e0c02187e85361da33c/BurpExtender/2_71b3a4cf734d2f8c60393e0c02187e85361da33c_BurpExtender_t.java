 package burp;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.*;
 import javax.net.ssl.HttpsURLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 public class BurpExtender extends AbstractTableModel implements IBurpExtender, ITab, IContextMenuFactory {
 	
     // Burp specific shit
     private IExtensionHelpers helper;
     private JSplitPane splitPane;
     private IBurpExtenderCallbacks callback;
     private final List<LogEntry> log = new ArrayList<LogEntry>();
     private PrintWriter stdout;
 
     // ArrayList to hold the parameters
     private ArrayList<String> parameters = new ArrayList<String>();
         
     @Override
     public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
         // obtain an extension helpers object
         helper = callbacks.getHelpers();
         callback = callbacks;
         stdout = new PrintWriter(callback.getStdout(), true);
         // set our extension name
         callback.setExtensionName("Crush Extension");
         callback.registerContextMenuFactory(this);
         
         // create our UI
         SwingUtilities.invokeLater(new Runnable() 
         {
             @Override
             public void run()
             {
                 // main split pane
                 splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                         
                 // table of log entries
                 Table logTable = new Table(BurpExtender.this);
                 JScrollPane scrollPane = new JScrollPane(logTable);
                 splitPane.setLeftComponent(scrollPane);
 
                 // customize our UI components
                 callback.customizeUiComponent(splitPane);
                 callback.customizeUiComponent(logTable);
                 callback.customizeUiComponent(scrollPane);
  
                 // add the custom tab to Burp's UI
                 callback.addSuiteTab(BurpExtender.this);   
             }
         });
     }
     
     public void performCrush(final IHttpRequestResponse[] requestResponse) throws IOException {
         IHttpService service;
         IRequestInfo requestInfo;
         List<IParameter> parameters;
 
         ArrayList<IParameter> cookies = new ArrayList<IParameter>();
         ArrayList<IParameter> posts = new ArrayList<IParameter>();
         ArrayList<IParameter> gets = new ArrayList<IParameter>();
         ArrayList<IParameter> xmls = new ArrayList<IParameter>();
         ArrayList<IParameter> jsons = new ArrayList<IParameter>();
 
 
         String host;
         int port;
 
         byte[] request;
         byte[] tempRequest;
 
         ExecutorService executor = Executors.newFixedThreadPool(5);
 
     	for(IHttpRequestResponse item : requestResponse) {
 
             service =  item.getHttpService();
             host = service.getHost();
             port = service.getPort();
             stdout.println("Socket: " + host + ":" + port + "\n");
 
             request = item.getRequest();
             requestInfo = helper.analyzeRequest(service, request);
             stdout.println("--------------------Original Request--------------------\n" + new String(request) + "\n--------------------------------------------------------\n");
             parameters = requestInfo.getParameters();
 
             for(IParameter param : parameters) {
                 if(param.getType() == IParameter.PARAM_COOKIE) {
                     cookies.add(param);
                 }
                 else if(param.getType() == IParameter.PARAM_BODY) {
                     posts.add(param);
                 }
                 else if (param.getType() == IParameter.PARAM_URL) {
                     gets.add(param);
                 }
                 else if( param.getType() == IParameter.PARAM_XML) {
                     xmls.add(param);
                 }
                 else if (param.getType() == IParameter.PARAM_JSON) {
                     jsons.add(param);
                 }
             }
 
             for(IParameter cookie : cookies) {
                 tempRequest = helper.removeParameter(request, cookie);
                 Runnable worker = new SendRequest(host, port, cookie, tempRequest);
                 executor.execute(worker);
             }
 
             for(IParameter post : posts) {              
                 tempRequest = helper.removeParameter(request, post);
                 Runnable worker = new SendRequest(host, port, post, tempRequest);
                 executor.execute(worker);
             }
 
             for(IParameter get : gets) {             
                 tempRequest = helper.removeParameter(request, get);
                 Runnable worker = new SendRequest(host, port, get, tempRequest);
                 executor.execute(worker);
             } 
 
             for(IParameter xml : xmls) {             
                 tempRequest = helper.removeParameter(request, xml);
                 Runnable worker = new SendRequest(host, port, xml, tempRequest);
                 executor.execute(worker);
             } 
 
             for(IParameter json : jsons) {             
                 tempRequest = helper.removeParameter(request, json);
                 Runnable worker = new SendRequest(host, port, json, tempRequest);
                 executor.execute(worker);
             } 
 		}			
     }
 
     // GUI shit
     @Override
     public String getTabCaption() {
         return "Crush";
     }
 
     @Override
     public Component getUiComponent() {
         return splitPane;
     }
     
     //
     // extend AbstractTableModel
     //
     @Override
     public int getRowCount() {
         return log.size();
     }
 
     @Override
     public int getColumnCount() {
         return 2;
     }
 
     @Override
     public String getColumnName(int columnIndex) {
         switch (columnIndex) {
             case 0:
                 return "Removed Parameter";
             case 1:
                 return "Status Code";
             default:
                 return "";
         }
     }
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         return String.class;
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         LogEntry logEntry = log.get(rowIndex);
 
         switch (columnIndex) {
             case 0:
                 return logEntry.parameter;
             case 1:
                 return logEntry.code;
             default:
                 return "";
         }
     }
     
     //
     // extend JTable to handle cell selection
     //
     private class Table extends JTable {
         public Table(TableModel tableModel) {
             super(tableModel);
         }
         
         @Override
         public void changeSelection(int row, int col, boolean toggle, boolean extend) {
             // show the log entry for the selected row            
             super.changeSelection(row, col, toggle, extend);
         }        
     }
     
 	@Override
 	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
 		
 		if(invocation.getInvocationContext() == invocation.CONTEXT_MESSAGE_EDITOR_REQUEST || invocation.getInvocationContext() == invocation.CONTEXT_MESSAGE_VIEWER_REQUEST) {
 
             final IHttpRequestResponse[] requestResponse = invocation.getSelectedMessages();
 			List<JMenuItem> list = new ArrayList<JMenuItem>();
 			JMenuItem item = new JMenuItem("Run Crush");
 			item.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					new Thread(new MyRunnable(requestResponse)).start();
 				}
 			});
 			
 			list.add(item);
 			return list;
 		}
 		else {
 			return null;
 		}
 	}
 
     // Log details
     private static class LogEntry
     {
         final String parameter;
         final String code;
 
         LogEntry(String parameter, String code) {
             this.parameter = parameter;
             this.code = code;
         }
     }
 	
     // Class to send request
     private class SendRequest implements Runnable {
         String host = "";
         int port;
         int row;
 
         byte[] request;
         IParameter param;
 
         public SendRequest(String host, int port, IParameter param, byte[] request) {
             this.host = host;
             this.port = port;
             this.request = request;   
             this.param = param;
         }
 
         @Override
         public void run() {
             IResponseInfo requestInfo;
             String str = "";
             String response = "";
 
             try {
                 Socket s = new Socket(host, port);
                 DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
                 BufferedReader dIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 dOut.write(request);
 
                while(!(str = dIn.readLine()).equals("")) {    
                     response = response + str + "\r\n";
                 }
                 dIn.close(); dOut.close(); s.close();
             } catch (IOException e) {
                 stdout.println("Shit has hit the fan");
             }
             
             stdout.println("Done waiting");
             requestInfo = helper.analyzeResponse(response.getBytes());
             stdout.println(param.getName() + " " + String.valueOf(requestInfo.getStatusCode()));
             row = log.size();
             log.add(new LogEntry(param.getName(), String.valueOf(requestInfo.getStatusCode())));
             fireTableRowsInserted(row, row);
 
         }
     }
 
     // Seperate thread from UI
 	private class MyRunnable implements Runnable {
 
         private IHttpRequestResponse[] requestResponse;
 
         public MyRunnable(IHttpRequestResponse[] requestResponse) {
             this.requestResponse = requestResponse;
         }
 
 		@Override
 		public void run() {
 			try {
 				performCrush(requestResponse);
 			} catch (IOException e) {
 				stdout.println("Shit hit the fan");
 				e.printStackTrace();
 			}	
 		}	
 	}
     
 }
