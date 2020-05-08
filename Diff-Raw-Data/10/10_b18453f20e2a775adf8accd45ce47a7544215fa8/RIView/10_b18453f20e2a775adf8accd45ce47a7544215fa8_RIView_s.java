 package org.rubypeople.rdt.internal.ui.infoviews;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.rubypeople.rdt.internal.launching.RubyInterpreter;
 import org.rubypeople.rdt.internal.launching.RubyRuntime;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.RubyPluginImages;
 import org.rubypeople.rdt.ui.PreferenceConstants;
 
 public class RIView extends ViewPart {
 
 	private boolean riFound = false;
 	private PageBook pageBook;
     private SashForm form;    
     private Label interpreterNeededLabel, riNotFoundLabel;
 	private Text searchStr;
     private TableViewer searchListViewer;
     private Browser searchResult;
     private java.util.List possibleMatches = new ArrayList();
     private SearchValue itemToSearch = new SearchValue();
     private DescriptionUpdater descriptionUpdater = new DescriptionUpdater();
     private RubyRuntime.Listener runtimeListener;
     private ListContentProvider contentProvider = new ListContentProvider();
 
 	/**
 	 * The constructor.
 	 */
 	public RIView() {}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		
 		contributeToActionBars() ;
 		
 		pageBook = new PageBook(parent, SWT.NONE);
         
         interpreterNeededLabel = new Label(pageBook, SWT.NONE);
         interpreterNeededLabel.setText(InfoViewMessages.getString("RubyInformation.interpreter_not_selected"));
         
         riNotFoundLabel = new Label( pageBook, SWT.LEFT | SWT.TOP | SWT.WRAP );
         riNotFoundLabel.setText( InfoViewMessages.getString( "RubyInformation.ri_not_found")  );
                        
         
         form = new SashForm(pageBook, SWT.HORIZONTAL);        
                        
         Composite panel = new Composite(form, SWT.NONE);
         panel.setLayout(new GridLayout(1, false));              
        
         // Search String
         searchStr = new Text(panel, SWT.BORDER);
         GridData data = new GridData();        
         data.horizontalAlignment = SWT.FILL;
         searchStr.setLayoutData(data);
         searchStr.addModifyListener(new ModifyListener() {        
             public void modifyText(ModifyEvent e) {
                 filterSearchList();                
             }        
         });
         searchStr.addKeyListener(new KeyAdapter() {
             public void keyPressed(KeyEvent e) {
                 super.keyPressed(e);                                
                 if (e.keyCode == 16777218 || e.keyCode == 13) { // sorry didn't find the SWT constant for down arrow
                     searchListViewer.getTable().setFocus();
                 } else if (e.keyCode == SWT.ESC) {
                     searchStr.setText("");
                 }
             }
         });
         
         searchListViewer = new TableViewer(panel, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
         searchListViewer.setContentProvider(contentProvider);
         data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
         searchListViewer.getTable().setLayoutData(data);
         searchListViewer.getTable().addSelectionListener(new SelectionListener() {        
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         
             public void widgetSelected(SelectionEvent e) {     
                 showSelectedItem();
             }
         });        
         searchStr.addFocusListener(new FocusAdapter() {        
             public void focusGained(FocusEvent e) {
                 searchStr.selectAll();
             }        
         });
         
         // search result
         searchResult = new Browser(form, SWT.BORDER);
         
         form.setWeights(new int[]{1, 3});
 
         runtimeListener = new RubyRuntime.Listener() {
             public void selectedInterpreterChanged() {
                 updatePage();
             }
         };
         RubyRuntime.getDefault().addListener(runtimeListener);
         
         RubyPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
         	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
         		if (event.getProperty().equals(PreferenceConstants.RI_PATH)) {
        			updatePage() ;
         		}
         	}
         });
         
                 
         updatePage();
         descriptionUpdater.start();
 	}
 	    
 	private void contributeToActionBars() {
 		IAction refreshAction = new Action() {
 			public void run() {
 				updatePage();
 			}
 		};
 		refreshAction.setText("Refresh");
 		refreshAction.setToolTipText("Refresh list of names");
 		refreshAction.setImageDescriptor(RubyPluginImages.TOOLBAR_REFRESH);
 		
 		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
 		manager.add(refreshAction);		
 	}
 	
     private void updatePage() {
         RubyInterpreter interpreter = RubyRuntime.getDefault().getSelectedInterpreter();
         if (interpreter != null) {            
             initSearchList();
             if( riFound ){
             	pageBook.showPage(form);
             }
         }
         else {
             pageBook.showPage(interpreterNeededLabel);
         }
     }
     
     private void showSelectedItem() {
         String searchText = (String)((IStructuredSelection)searchListViewer.getSelection()).getFirstElement();        
         synchronized(itemToSearch) { itemToSearch.set(searchText); }
     }        
     
     public void dispose() {
         descriptionUpdater.requestStop();
         RubyRuntime.getDefault().removeListener(runtimeListener);
         super.dispose();
     }
         
     private class SearchValue {
         String value = null;
         
         void set(String value) {
             this.value = value;
             notifyAll();
         }
         
         boolean isSet() {
             return value != null;
         }
         
         String get() {
             String result = value;
             value = null;
             return result;            
         }
     }
     
 	private void initSearchList() {        
         RubyInvoker invoker = new RubyInvoker() {        
             protected void handleOutput(Process process) {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 String line = null;
                 int numberOfMatches = 0;
                 possibleMatches = new ArrayList() ;
                 try {
                     while ((line = reader.readLine()) != null) {
                         possibleMatches.add(line.trim());
                         numberOfMatches++;
                     }
                     // if not matches were found display an error message
                     if( numberOfMatches == 0 ){
                     	pageBook.showPage( riNotFoundLabel );
                     } else {
                     	riFound = true;
                     }
                 }
                 catch (IOException e) {
                     // TODO
                 }
             }        
             protected String getArgString() {
                 return "--no-pager -l";
             }
         };
         invoker.invoke();
         filterSearchList();
     }
 
     private void filterSearchList() {               
         java.util.List filteredList = new ArrayList();
         String text = searchStr.getText();
         for (Iterator iter = possibleMatches.iterator(); iter.hasNext();) {
             String possibleMatch = (String) iter.next();      
             if (possibleMatch.indexOf(text) > -1 ) {
                 filteredList.add(possibleMatch);                
             }
         }
         searchListViewer.setInput(filteredList);       
         if (filteredList.size() > 0) searchListViewer.getTable().setSelection(0);             
         showSelectedItem();
     }
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		form.setFocus();
 	}       
     
     private final class ListContentProvider implements IStructuredContentProvider {
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {                
         }
 
         public void dispose() {
         }
 
         public Object[] getElements(Object inputElement) {
             return ((Collection)inputElement).toArray();
         }
     }
 
     private final class DescriptionUpdater extends Thread {
         private String searchValue;
         private volatile boolean stopped = false;
         
         public DescriptionUpdater() {
             super("RI Description Updater");            
         }               
     
         private RubyInvoker invoker = new RubyInvoker() {
             protected String getArgString() {
                 return "--no-pager -f html " + searchValue;
             }
     
             protected void beforeInvoke() {
                 searchResult.setText(InfoViewMessages.getString("RubyInformation.please_wait"));
             }
     
             protected void handleOutput(final Process process) {
                 // any output?
                 final StreamRedirector outputRedirect = new StreamRedirector(process.getInputStream(), "");
                 // kick them off
                 outputRedirect.start();
                 final int rc;
                 try {
                     rc = process.waitFor();
                     if (rc != 0) {
                         outputRedirect.inQueue("<p>No matching results.</p>");
                     }
                     outputRedirect.done();                    
                 }
                 catch (InterruptedException IGNORE) {
                 }
             }
         };
     
         public void requestStop() {
             stopped = true;
             interrupt();
         }
         
         public void run() {
             while (!stopped) {
                 synchronized (itemToSearch) {
                     try {
                         while (!itemToSearch.isSet() && !stopped) { itemToSearch.wait(); }
                         searchValue = itemToSearch.get();
                     }
                     catch (InterruptedException IGNORE) {                            
                     }                    
                 }
                 if (stopped) return;
                 invoker.invoke();
             }
         }
     }
     
     private abstract class RubyInvoker {
         protected abstract String getArgString();
         protected abstract void handleOutput(Process process);
         protected void beforeInvoke(){}
         
         
        
         
         public final void invoke() {
			riFound = false;
 			
         	IPath rubyPath = RubyRuntime.getDefault().getSelectedInterpreter().getInstallLocation();
         	IPath riPath = new Path( RubyPlugin.getDefault().getPreferenceStore().getString( PreferenceConstants.RI_PATH ) );
         	
         	// check the ri path for existence. It might have been unconfigured
 			// and set to the default value or the file could have been removed
 			File file = new File(riPath.toOSString());
 
 			// If we can't find it ourselves then display an error to the user
 			if (!file.exists() || !file.isFile()) {
 				pageBook.showPage(riNotFoundLabel);
 				return;
 			}
 
     		String	riCmd =  "\"" + rubyPath + "\" \"" +  riPath.toString() + "\" ";     		
     		String call = riCmd + getArgString();
     		try {        			
     			final Process p = Runtime.getRuntime().exec(call);
                 handleOutput(p); 
     		} catch (IOException e) {
     			e.printStackTrace();
     		}        
         }
     }
 
     class StreamRedirector extends Thread {
 	    // TODO Add ability to style by CSS stylesheet!
 		// FIXME Use the HTMLPrinter from rdt.internal.ui.text!
 		InputStream is;
 		String linePrefix;
 		String line = null;
 		boolean done = false;
 		final java.util.List queue = new ArrayList();
 		private StringBuffer buffer = new StringBuffer();
 		private final String HEADER = "<html><head></head><body>";
 		private final String TAIL = "</body></html>";
 
 		StreamRedirector(final InputStream is, final String type) {
 			this.is = is;
 			this.linePrefix = type;
 		}
 
 		void done() {
 			done = true;
 		}
 
 		void inQueue(final String msg) {
 			queue.add(msg);
 		}
 
 		void addToBuffer(int position, final String line) {			
 		    if (position < 0) position = 0;
 		    StringBuffer modifiedLine = new StringBuffer(line);
 		    if (!line.endsWith(">")) modifiedLine.append("<br/>");
 			modifiedLine.append("\r\n");				
 			buffer.insert(position, modifiedLine.toString());
 		}
 
 		public void run() {
 			try {
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				// Insert all the text
 				while ((line = br.readLine()) != null) {
 					addToBuffer(buffer.length() - 1, linePrefix + line);					
 				}
 				buffer.insert(0, HEADER); // Put the header before all the contents
 				buffer.append(TAIL); // Put the body and html close tags at end
 				
 				setText();
 				// Insert later text at end, but before close body and html tags
 				while ((!done) || (queue.size() > 0)) {
 					if (queue.size() > 0) {
 						String queued = (String) queue.get(0);
 						if(queued == null) {
 							queue.remove(0);
 							continue;
 						}
 						addToBuffer(buffer.length() - TAIL.length(), linePrefix + queued);
 						queue.remove(0);
 						setText();
 					} else {
 						try {
 							Thread.sleep(100);
 						} catch (InterruptedException IGNORE) {}
 					}
 				}
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
         /**
          * Puts the contents of the buffer into the widget
          */
         private void setText() {
             Display.getDefault().syncExec(new Runnable() {
             	public void run() {
             		searchResult.setText(buffer.toString());
             	}
             });
         }
 	}
 }
