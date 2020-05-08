 package com.patelsoft.fastopen;
 
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.*;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.BadLocationException;
 import org.gjt.sp.jedit.*;
 import org.gjt.sp.jedit.gui.DefaultFocusComponent;
 import org.gjt.sp.jedit.gui.DockableWindowManager;
 import org.gjt.sp.jedit.gui.FloatingWindowContainer;
 import org.gjt.sp.jedit.io.VFSManager;
 import org.gjt.sp.jedit.textarea.JEditTextArea;
 import org.gjt.sp.util.*;
 import gnu.regexp.*;
 
 
 public class FastOpen extends JPanel implements ActionListener, IndexListener, DefaultFocusComponent
 {
 	private final String TITLE = "Fast Open " + jEdit.getProperty("plugin.com.patelsoft.fastopen.FastOpenPlugin.version");
 	View view;
 	JTextField txtfilename;
 	JList  jlist;
 	//List _foundfileslist;
 	JDialog mainWindow = null;
 	JComboBox projectCombo;
 	ProjectSwitchListener vListener;
 	Comparator comparator = new FastOpenComparator();
 	public static Color openFilesForeground = jEdit.getColorProperty("fastopen.openFiles.foregroundcolor", Color.black);
 	public static Color nonprjopenFilesForeground = jEdit.getColorProperty("fastopen.nonprjOpenFiles.foregroundcolor", Color.green.darker());
 	final RE reLineNo = new UncheckedRE("(.*):([0-9]+)");
 	private IndexManager indexManager;
 	//Set allfiles =  new HashSet(1); //Just  to avoid NPE
 	private final String noWordSep;
 	private Files files = new Files();
 	private int indexingStatus = 0; // 0 = INDEXING NEVER RAN BEFORE, 1/2 = INDEXING ON/OFF
 	private boolean initialIndexingInProgress;
 	private javax.swing.Timer timer;
 
 
 
 
 
 	/**
 	 *  Constructor for the FastOpen object
 	 *
 	 * @param  view  Description of the Parameter
 	 */
 	public FastOpen(View view)
 	{
 		this.view = view;
 		noWordSep = view.getBuffer().getProperty("noWordSep") + ".:-" + File.separator;
 		indexManager = getIndexManager();
 		setupFastOpen();
 	}//End of FastOpen constructor

	/**  Shows the Main FastOpen window  */
	public FastOpen showWindow()
 	{
 		if(jEdit.getBooleanProperty("fastopen.patternFromSelectedText"))
 			{
 				String txtSelection = getFileAtCaret();
 				int lno = -1;
 				List vecContent = parseFileLnoPattern(txtSelection);
 
 				if(vecContent != null)
 				{
 					//txtSelection = (String)vecContent.get(0);
 					lno =((Integer)vecContent.get(1)).intValue();
 				}
 
 
 				if(txtSelection != null)
 				{
 					if(txtSelection.indexOf("\n") == -1) //Only run the Algo if the input filename does not contain newline characters.
 					{
 						FastOpenFile matchingfiles[] = retrieveMatchingFiles(txtSelection);
 
 						if(matchingfiles != null && matchingfiles.length == 1)
 						{
 							//Only one matching file so why show the mainWindow
 							openFile((FastOpenFile)matchingfiles[0]);
 							
 							if(lno != -1)
 							{
 								gotoLine(lno);
 							}
 							closeMainWindow();									
							return this;
 						}
 
 						txtfilename.setText((txtSelection == null?null:txtSelection.trim()));
 					}
 					else
 					{
 						view.getStatus().setMessageAndClear("FastOpen ignored long text exceeding 50 characters as useful search criteria.");
 						txtfilename.setText("");
 					}
 				}
 			}
 			if(files.isPVThere())
 			{
 				loadProjectsInCombo();
 			}
 			//}
 
 			txtfilename.selectAll();
		return this;
 	}
 
 
 	/**  Closes the FastOpen window */
 	public void closeMainWindow()
 	{		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				DockableWindowManager dwm = jEdit.getActiveView().getDockableWindowManager();
 				dwm.hideDockableWindow("fastopen");					
 			}
 		});
 		
 	}
 
 
 	/**  Description of the Method */
 	private void setupFastOpen()
 	{
 		setLayout(new BorderLayout());
 		//txtfilename = new FastOpenTextField(20);
 		txtfilename = new FastOpenTextField("fastopen.patterns", false, true);
 //		this.setNextFocusableComponent(txtfilename);
 		txtfilename.addActionListener(this);
 		txtfilename.getDocument().addDocumentListener(
 			new DocumentListener()
 			{
 				public void insertUpdate(DocumentEvent e)
 				{
 					SwingUtilities.invokeLater(new Runnable()
 						{
 							public void run()
 							{
 								if(timer == null)
 								{
 									startDelayTimer();
 								}
 							}
 						}
 						);
 				}
 
 
 				public void changedUpdate(DocumentEvent e)
 				{
 					SwingUtilities.invokeLater(new Runnable()
 						{
 							public void run()
 							{
 								if(timer == null)
 								{
 									startDelayTimer();
 								}
 							}
 						}
 						);
 				}
 
 
 				public void removeUpdate(DocumentEvent e)
 				{
 					try
 					{
 						if(e.getDocument().getText(0, e.getDocument().getLength()).trim().length() == 0)
 						{
 							jlist.setListData(new String[0]);
 						}
 
 						else
 						{
 							SwingUtilities.invokeLater(new Runnable()
 								{
 									public void run()
 									{
 										if(timer == null)
 										{
 											startDelayTimer();
 										}
 									}
 								}
 								);
 						}
 					}
 
 					catch(BadLocationException badle)
 					{}
 				}
 
 
 				private void startDelayTimer()
 				{
 					timer = new javax.swing.Timer((int)(jEdit.getDoubleProperty("fastopen.search.delay",2)*1000), new DelaySearchAction());
 					timer.setRepeats(false);
 					timer.start();
 					//System.out.println("Timer started with delay " + timer.getDelay());
 				}
 
 			}
 
 		);
 		Action down_Action =
 			new AbstractAction("DownArrow")
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					moveListDown();
 				}
 			};
 
 		Action up_Action =
 			new AbstractAction("UpArrow")
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					moveListUp();
 				}
 			};
 
 		//Below steps are used to receive notifications of the KeyStrokes we are interested in unlike ActionListener/KeyListener
 		//which is fired irrespective of the kind of KeyStroke.
 		InputMap inputMap = txtfilename.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		KeyStroke up_arrow = KeyStroke.getKeyStroke("UP");
 		KeyStroke down_arrow = KeyStroke.getKeyStroke("DOWN");
 
 		inputMap.put(up_arrow, up_Action.getValue(Action.NAME));
 		inputMap.put(down_arrow, down_Action.getValue(Action.NAME));
 
 		ActionMap actionMap = txtfilename.getActionMap();
 		actionMap.put(up_Action.getValue(Action.NAME), up_Action);
 		actionMap.put(down_Action.getValue(Action.NAME), down_Action);
 
 		JPanel pnlNorth = new JPanel(new BorderLayout());
 
 		pnlNorth.add(txtfilename, BorderLayout.CENTER);
 
 		if(files.isPVThere())
 		{
 			vListener = new ProjectSwitchListener();
 			projectCombo = new JComboBox();
 			projectCombo.setRenderer(new DefaultListCellRenderer()
 									 {
 										 public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
 										 {
 											 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 											 projectviewer.vpt.VPTProject project = (projectviewer.vpt.VPTProject)value;
                                              if(project != null)
                                              {
                                                 setText(project.getName());
                                              }
 											 return this;
 										 }
 									 });
 			projectCombo.addItemListener(vListener);
 			loadProjectsInCombo();//For dockables which does not call showWindow.
 			pnlNorth.add(projectCombo, BorderLayout.EAST);
 		}
 
 		this.add(pnlNorth, BorderLayout.NORTH);
 		jlist = new JList();
 		jlist.setCellRenderer(new FastOpenRenderer());
 		jlist.addKeyListener(new ListHandler());
 		jlist.addMouseListener(new ListHandler());
 		JScrollPane scroller = new JScrollPane(jlist);
 		this.add(scroller, BorderLayout.CENTER);
 
 		//createMainWindow
 		/*
 		mainWindow = new JDialog(view, this.TITLE, false);
 		mainWindow.setSize(554, 182);//Default size for new FastOpen installation.
 		mainWindow.addNotify();
 		mainWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 		mainWindow.getContentPane().add(this, BorderLayout.CENTER);
 		mainWindow.addComponentListener(
 			new ComponentAdapter()
 			{
 				public void componentResized(ComponentEvent e)
 				{
 					GUIUtilities.saveGeometry(mainWindow, "fastopen.window");
 				}
 
 
 				public void componentMoved(ComponentEvent e)
 				{
 					GUIUtilities.saveGeometry(mainWindow, "fastopen.window");
 				}
 			}
 
 		);
 		*/
 		// Add escape-key event handling to widgets
 		KeyHandler keyHandler = new KeyHandler();
 		addKeyListener(keyHandler);
 		txtfilename.addKeyListener(keyHandler);
 //		mainWindow.addKeyListener(keyHandler);
 
 	}//End of setupFastOpen
 
 
 	/**  Description of the Method */
 	void moveListDown()
 	{
 		int selectedIndex = jlist.getSelectedIndex();
 		int listSize = jlist.getModel().getSize();
 
 		if(listSize > 1 && selectedIndex >= 0 && (selectedIndex + 1) < listSize)
 		{
 			selectedIndex++;
 			jlist.setSelectedIndex(selectedIndex);
 			jlist.ensureIndexIsVisible(selectedIndex);
 		}
 	}
 
 
 	/**  Description of the Method */
 	void moveListUp()
 	{
 		int selectedIndex = jlist.getSelectedIndex();
 		int listSize = jlist.getModel().getSize();
 
 		if(listSize > 1 && (selectedIndex - 1) >= 0)
 		{
 			selectedIndex--;
 			jlist.setSelectedIndex(selectedIndex);
 			jlist.ensureIndexIsVisible(selectedIndex);
 		}
 	}
 
 
 	/**  Called when FastOpen object or FastOpenPlugin is destroyed. */
 	void killWindow()
 	{
 		//System.out.println("Killing Main Window");
 		if(mainWindow != null)
 		{
 			mainWindow.dispose();
 			mainWindow = null;
 		}
 
 		if(indexManager != null)
 		{
 			indexManager.stop();
 			indexManager = null;
 		}
 		//pv = null;
 	}
 
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  evt  Description of the Parameter
 	 */
 	public void actionPerformed(ActionEvent evt)
 	{
 		//For enter keys pressed inside txtfilename
 
 		int selectedIndex = jlist.getSelectedIndex();
 		int listSize = jlist.getModel().getSize();
 
 		if(selectedIndex != -1 && listSize != 0 && selectedIndex < listSize)
 		{
 			List vec = parseFileLnoPattern(getFilePattern());
 
 			if(vec != null)
 			{
 				//openFile(jlist.getSelectedIndex(),((Integer)vec.get(1)).intValue());
 				openFile((FastOpenFile)jlist.getSelectedValue(),((Integer)vec.get(1)).intValue());
 			}
 			else
 			{
 				//openFile(new int[]{jlist.getSelectedIndex()});
 				openFile((FastOpenFile)jlist.getSelectedValue());
 			}
 
 			closeMainWindow();
 		}
 	}
 
 
 
 
 	/**  Description of the Method */
 	void findfile()
 	{
 		//System.out.println("Inside findfile initialIndexingInProgress " + initialIndexingInProgress);
 		if(!initialIndexingInProgress)
 		{
 			String txtToFind = getFilePattern();
 			if(txtToFind != null && txtToFind.trim().length() > 0)
 			{
 				//	Log.log(Log.DEBUG, this.getClass(), "Trying to retrieveFiles with text " + txtToFind);
 				FastOpenFile foundfileslist[] = retrieveMatchingFiles(txtToFind);
 				if(foundfileslist != null && foundfileslist.length > 0)
 				{
 					if(txtfilename.getForeground() == Color.red)
 					{
 						txtfilename.setForeground(jEdit.getColorProperty("view.fgColor", Color.black));
 					}
 
 					/* if(_foundfileslist != null)
 					{
 						_foundfileslist.clear();
 					}
 					_foundfileslist = foundfileslist;
 					foundfileslist = null; */
 
 					//jlist.setListData(extractPathFromPrjDir(_foundfileslist));
 					jlist.setListData(foundfileslist);
 
 
 					jlist.setSelectedIndex(0);
 				}
 				else
 				{
 					//Log.log(Log.NOTICE, FastOpen.class, "No matching file found.");
 					jlist.setListData(new String[0]);
 					txtfilename.setForeground(Color.red);
 				}
 				foundfileslist = null;
 			}
 		}
 	}
 
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  event  Description of the Parameter
 	 */
 	void onefileselection(AWTEvent event)
 	{
 		//openFile(((JList)event.getSource()).getSelectedIndices());
 
 		Object selectedFiles[] = ((JList)event.getSource()).getSelectedValues();
 		//Since getSelectedValues return type is Object[], it cannot be directly casted to FastOpenFile[] and thus the below loop.
 		for(int i = 0; i < selectedFiles.length; i++)
 		{
 			try
 			{
 				openFile((FastOpenFile)selectedFiles[i]);
 			}
 			catch(NullPointerException e)
 			{
 				//Sometimes (during testing) jEdit was throwing a NPE as below. due to which only some files could be opened in Multi select mode. and thus this catch to prevent an exception halting opening remaining files.
 				//[error] AWT-EventQueue-0: java.lang.NullPointerException
 				//[error] AWT-EventQueue-0:  at org.gjt.sp.jedit.Buffer.markTokens(Buffer.java:2109)
 				//[error] AWT-EventQueue-0:  at org.gjt.sp.jedit.textarea.ChunkCache.lineToChunkList(ChunkCache.java:772)
 			}
 		}//End of for
 
 	}
 
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  matchingfileindex  Description of the Parameter
 	 * @return                    Description of the Return Value
 	 */
 	/*private void openFile(int matchingfileindex[])
 	{
 		if(matchingfileindex != null && matchingfileindex.length >0)
 		{
 			for(int i=0;i<matchingfileindex.length;i++)
 			{
 				openFile((FastOpenFile)_foundfileslist.get(matchingfileindex[i]));
 			}
 		}
 	}*/
 
 	private void openFile(FastOpenFile matchingfiles[])
 	{
 		if(matchingfiles != null && matchingfiles.length >0)
 		{
 			for(int i=0;i<matchingfiles.length;i++)
 			{
 				openFile(matchingfiles[i]);
 			}
 		}
 	}
 
 /*	private Buffer openFile(int matchingfileindex, int lineNo)
 	{
 		Buffer buf =openFile((FastOpenFile)_foundfileslist.get(matchingfileindex));
 		gotoLine(lineNo);
 		return buf;
 	}*/
 
 	private Buffer openFile(FastOpenFile matchingfile, int lineNo)
 	{
 		Buffer buf =openFile(matchingfile);
 		gotoLine(lineNo);
 		return buf;
 	}
 
 	private String getFilePattern()
 	{
 		try
 		{
 			return txtfilename.getDocument().getText(0, txtfilename.getDocument().getLength());
 		}
 
 		catch(BadLocationException e)
 		{
 			Log.log(Log.DEBUG, this.getClass(), "Caught BadLocationException. Returning.");
 		}
 
 		return null;
 	}
 
 	private void gotoLine(final int lineNo)
 	{
 		VFSManager.runInAWTThread(
 			new Runnable()
 			{
 				public void run()
 				{
 					JEditTextArea txtArea = view.getTextArea();
 
 					if(lineNo <= txtArea.getLineCount())
 					{
 						txtArea.setCaretPosition(txtArea.getLineStartOffset(lineNo - 1));
 					}
 
 					else
 					{
 						Log.log(Log.DEBUG, this.getClass(), "Ignoring linecounbt " + lineNo);
 					}
 				}
 			}
 
 		);
 	}
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  pf  Description of the Parameter
 	 * @return     Description of the Return Value
 	 */
 	public Buffer openFile(FastOpenFile pf)
 	{
 		return pf.open(this.view);
 	}
 
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  foundfileslist  Description of the Parameter
 	 * @return                 Description of the Return Value
 	 */
 	private String[] extractPathFromPrjDir(List foundfileslist)
 	{
 		String paths[] = new String[foundfileslist.size()];
 
 		Iterator iter = foundfileslist.iterator();
 		for(int i=0;iter.hasNext();i++)
 		{
 			FastOpenFile pf = (FastOpenFile)iter.next();
 			paths[i] = pf.getDecoratedPath();
 		}
 
 		return paths;
 	}
 
 
 	/**
 	 * @deprecated Moved to FastOpenFile class. This was done to avoid multiple path (String object) creation for efficiency reasons.
 	 *  Gets the decoratedPath attribute of the FastOpen object
 	 *
 	 * @param  pf  Description of the Parameter
 	 * @return     The decoratedPath value
 	 */
 	/*public String getDecoratedPath(FastOpenFile pf)
 	{
 		if(pf == null)
 		{
 			return null;
 		}
 
 		return pf.getName() + " (" + pf.getPath() + ")";
 	}*/
 
 
 	/**
 	 *  Gets the fileAtCaret attribute of the FastOpen object
 	 *
 	 * @return    The fileAtCaret value
 	 */
 	private String getFileAtCaret()
 	{
 		JEditTextArea textArea = view.getTextArea();
 
 		if(textArea.getSelectionCount() > 0)
 		{
 			return textArea.getSelectedText(textArea.getSelection()[0]);
 		}
 		int line = textArea.getCaretLine();
 		int lineLength = textArea.getLineLength(line);
 
 		if(lineLength == 0)
 		{
 			return null;
 		}
 
 		String lineText = textArea.getLineText(line);
 		int lineStart = textArea.getLineStartOffset(line);
 		int offset = textArea.getCaretPosition() - lineStart;
 
 		if(offset == lineLength)
 		{
 			//--offset;
 			return null;
 		}
 
 		int wordStart = TextUtilities.findWordStart(lineText, offset, noWordSep);
 		int wordEnd = TextUtilities.findWordEnd(lineText, offset + 1, noWordSep);
 		String filename = textArea.getText(lineStart + wordStart, wordEnd - wordStart);
 
 		/* Get rid of any path info, since we index by filename and not by pathname */
 		int idx = filename.lastIndexOf("/");
 		if (idx > -1) {
 			filename = filename.substring(idx+1);
 		}
 		idx = filename.lastIndexOf("\\");
 		if (idx > -1) {
 			filename = filename.substring(idx+1);
 		}
 		return filename;
 	}
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  txtSelection  Description of the Parameter
 	 */
 	List parseFileLnoPattern(String txtSelection)
 	{
 		if((txtSelection != null && txtSelection.trim().length() != 0) && reLineNo.isMatch(txtSelection))
 		{
 			REMatch match = reLineNo.getMatch(txtSelection);
 
 			List vecReturn = new ArrayList(2);
 			vecReturn.add(0,match.toString(1));
 			vecReturn.add(1,new Integer(match.toString(2)));
 			return vecReturn;
 		}
 
 		return null;
 	}
 
 	/**  Description of the Method */
 	private void loadProjectsInCombo()
 	{
 		vListener.pause();
 
 		if(projectCombo.getItemCount() != 0)
 		{
 			projectCombo.removeAllItems();
 		}
 		Iterator iter = projectviewer.ProjectManager.getInstance().getProjects();
 		int currProjectIdx = 0;
 		projectviewer.vpt.VPTProject currPrj = files.getCurrentProject(view);
 
 		List projects = new ArrayList();
 
 		for(int i = 1; iter.hasNext(); i++)
 		{
 			projectviewer.vpt.VPTProject nextItem = (projectviewer.vpt.VPTProject)iter.next();
 			projects.add(nextItem);
 		}
 
 		Collections.sort(projects, new Sorter());
 		projectCombo.setModel(new DefaultComboBoxModel(projects.toArray()));
 		if(currPrj != null)
 		{
 			projectCombo.setSelectedItem(currPrj);
 		}
 		vListener.resume();
 	}
 
 	/**
 	 *  Description of the Method
 	 *
 	 * @param  fileToFind  Description of the Parameter
 	 * @return             Description of the Return Value
 	 */
 	/*	public List retrieveMatchingFiles(String fileToFind, View view)
 		{
 			//Log.log(Log.DEBUG,this.getClass(),"Got fileToFind "+ fileToFind);
 			if(fileToFind != null)
 			{
 				List foundfileslist = new ArrayList();
 				//System.out.println( "Trying to match inside retrieveMatchingFiles " + fileToFind);
 				Set allfiles = new TreeSet(comparator);
 
 	////				long start = System.currentTimeMillis();
 				Collection dataFiles = files.prjFile2FOFile(view);
 				if(dataFiles != null)
 				{
 					allfiles.addAll(dataFiles);
 				}
 
 	//				long end = System.currentTimeMillis();
 				System.out.println("Time taken to get projects and add " + (end-start) + " ms");
 
 	//				start = System.currentTimeMillis();
 				dataFiles =files.diffPrjFilesWithOpenBuffers(jEdit.getBuffers(),view);
 				if(dataFiles != null)
 				{
 					allfiles.addAll(dataFiles);
 				}
 	//				end = System.currentTimeMillis();
 				System.out.println("Time taken to diff projectfile with open buffers and add " + (end-start) + " ms");
 
 
 	//				start = System.currentTimeMillis();
 				if(jEdit.getBooleanProperty("fastopen.showrecentfiles"))
 				{
 					dataFiles =getRecentFiles();
 					if(dataFiles != null)
 					{
 						allfiles.addAll(dataFiles);
 					}
 				}
 	//				end = System.currentTimeMillis();
 				System.out.println("Time taken to get recentfiles and add " + (end-start) + " ms");
 
 
 				System.out.println("Total files collected " + allfiles.size());
 				//Iterator iterPrjFiles = project.getFiles().iterator();
 				Iterator iterPrjFiles = allfiles.iterator();
 
 				List vecPattern = parseFileLnoPattern(fileToFind);
 				if(vecPattern != null)
 				{
 					fileToFind = (String)vecPattern.get(0);
 				}
 				try
 				{
 					RE re = null;
 					if(jEdit.getBooleanProperty("fastopen.ignorecase"))
 					{
 						re = new RE(MiscUtilities.globToRE("^" + fileToFind), RE.REG_ICASE);
 					}
 					else
 					{
 						re = new RE(MiscUtilities.globToRE("^" + fileToFind));
 					}
 
 					while(iterPrjFiles.hasNext())
 					{
 						FastOpenFile file = (FastOpenFile)iterPrjFiles.next();
 						if(jEdit.getBooleanProperty("fastopen.hideOpenFiles"))
 						{
 							if(file.isOpened())
 							{
 								continue;
 							}
 						}
 
 						if(re.getMatch(file.getName()) != null)
 						{
 							foundfileslist.add(file);
 						}
 					}
 					//End of while
 					iterPrjFiles = null;
 
 					//if(jEdit.getBooleanProperty("fastopen.sortFiles"))
 					//{
 					//	Collections.sort(foundfileslist, comparator);
 					//}
 				}
 				catch(REException e)
 				{
 					txtfilename.setForeground(Color.red);
 					return foundfileslist;
 				}
 
 				return foundfileslist;
 			}
 			else
 			{
 				return new ArrayList(0);
 			}
 		}
 	 */
 
 
 	public FastOpenFile[] retrieveMatchingFiles(String fileToFind)
 	{
 		//We try to collect files just once and hold on to them until the user closes FO. This reduces Object creation and hopefully increases performance.
 
 		//Log.log(Log.DEBUG,this.getClass(),"Got fileToFind "+ fileToFind);
 		//long start,end;
 		if(fileToFind != null)
 		{
 				//if(allfiles == null || allfiles.size() == 0)
 				//{
 					/*long start = System.currentTimeMillis();
 					//start = System.currentTimeMillis();
 					files.prjFile2FOFile(view,allfiles);
 
 					//end = System.currentTimeMillis();
 					//System.out.println("Time taken to get projects and add " + (end-start) + " ms but before adding to addFiles");
 
 					//long end2 = System.currentTimeMillis();
 					//System.out.println("Time taken to get projects and add " + (end2-end) + " ms total time is " + (end2-start));
 
 					//start = System.currentTimeMillis();
 					files.diffPrjFilesWithOpenBuffers(jEdit.getBuffers(),view, allfiles);
 
 					//end = System.currentTimeMillis();
 					//System.out.println("Time taken to diff projectfile with open buffers and add " + (end-start) + " ms");
 
 					//start = System.currentTimeMillis();
 
 					if(jEdit.getBooleanProperty("fastopen.showrecentfiles"))
 					{
 						getRecentFiles(allfiles);
 					}
 
 					long end = System.currentTimeMillis();
 					System.out.println("Time taken to get allfiles and add " + (end-start) + " ms");
 					System.out.println("Total files collected " + allfiles.size());*/
 
 				/*}
 				else
 				{
 					System.out.println("Reusing allfiles " + allfiles);
 				}*/
 				//System.out.println("Completed allfiles for filetoFind " + fileToFind + " " + allfiles);
 
 				//long startTime = System.currentTimeMillis();
 				Set allfiles = indexManager.getCollectedFiles();
 				//long endTime = System.currentTimeMillis();
 				//System.out.println("IndexManager.getCollectedFiles took time " + (endTime - startTime)/1000 +" sec");
 				if(allfiles == null || allfiles.size() == 0) //IndexManager still loading.
 				{
 					return null;
 				}
 
 				//startTime = System.currentTimeMillis();
 				List vecPattern = parseFileLnoPattern(fileToFind);
 				//endTime = System.currentTimeMillis();
 
 				//System.out.println("parsing Line no pattern took time " + (endTime - startTime)/1000 +" sec");
 				if(vecPattern != null)
 				{
 					fileToFind = (String)vecPattern.get(0);
 				}
 
 				//long start = System.currentTimeMillis();
 				List foundfileslist = new ArrayList(allfiles.size()); //Initializating Collections is a Performance Optimization since the need for expansion is done away with. Setting foundfileslist's size to max allfiles.size() since thats the max it can go(in case of say regexp '*') anyways. unused elements are as it  is NULLs.
 				try
 				{
 					RE re = null;
 
 					if(jEdit.getBooleanProperty("fastopen.ignorecase"))
 					{
 						re = new RE(MiscUtilities.globToRE("^" + fileToFind), RE.REG_ICASE);
 					}
 					else
 					{
 						re = new RE(MiscUtilities.globToRE("^" + fileToFind));
 					}
 
 					//start = System.currentTimeMillis();
 
 					final boolean hideOpenFiles = jEdit.getBooleanProperty("fastopen.hideOpenFiles"); //Moved outside the while loop to improve performance instead of repeated seaching in jEdit's hash properties when it is not going to change between call.
 					Iterator iterPrjFiles = allfiles.iterator();
 					int i=0;
 					while(iterPrjFiles.hasNext()) //Since we check for hideOpenFiles option, there is a possibility of Array fragmentation because i wud be incremented but nothing would be stored at that position since the file under iteration is already open, so we increment i manually when applicable.
 					{
 						FastOpenFile file = (FastOpenFile)iterPrjFiles.next();
 						if(hideOpenFiles)
 						{
 							if(file.isOpened())
 							{
 								continue;
 							}
 						}
 
 						if(re.getMatch(file.getName()) != null)
 						{
 							//System.out.println("Duplicate file exists for file "+ file + " "+ (foundfileslist.contains(file)));
 							foundfileslist.add(file);
 						}
 					}//End of while
 
 					//System.out.println("Final foundfileslist "+ foundfileslist);
 					//end = System.currentTimeMillis();
 
 					//System.out.println("Time taken to collect matching files " + (end-start) + " ms");
 
 					iterPrjFiles = null;
 					allfiles = null;
 
 
 					if(jEdit.getBooleanProperty("fastopen.sortFiles"))
 					{
 						//start = System.currentTimeMillis();
 						Collections.sort(foundfileslist, comparator);
 						//long end = System.currentTimeMillis();
 						//System.out.println("Time taken to find and sort " + (end-start) + " ms found files " + foundfileslist.size());
 					}
 
 				}
 				catch(REException e)
 				{
 					txtfilename.setForeground(Color.red);
 					return new FastOpenFile[0];// To avoid caller to having check NPE
 				}
 				return (FastOpenFile[])foundfileslist.toArray(new FastOpenFile[foundfileslist.size()]);
 		}
 		return new FastOpenFile[0];
 	}
 
 	/**
 	*   Simplistic Factory.
 	*/
 	private IndexManager getIndexManager()
 	{
 		String indexManagerType = jEdit.getProperty("fastopen.indexing.strategy");
 		//System.out.println("See indexmanager strategy " + indexManagerType);
 		if(indexManagerType.equals("polling"))
 		{
 			IndexManager idxMgr = new PollingIndexManager(view, files);
 			idxMgr.addIndexListener(this);
 			return idxMgr;
 		}
 		else if(indexManagerType.equals("simple"))
 		{
 			IndexManager idxMgr = new SimpleIndexManager(view, files);
 			idxMgr.addIndexListener(this);
 			return idxMgr;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 
 	public void indexingStarted(IndexManager manager)
 	{
 		//System.out.println("Inside indexingStarted");
 		if(indexingStatus == 0) //This is the first time Indexing is underway.
 		{
 			initialIndexingInProgress = true;
 		}
 		indexingStatus = 1;
 		//System.out.println("Leaving indexingStarted");
 	}
 
 	public void indexingCompleted(IndexManager manager)
 	{
 		//System.out.println("Inside indexingCompleted");
 		if(initialIndexingInProgress)
 		{
 			initialIndexingInProgress = false;
 			findfile(); //Try to search for file which the user typed in the name for.
 		}
 		indexingStatus = 2;
 		//System.out.println("Leaving indexingCompleted");
 	}
 
 
 	//Inner Classes start
 		/**
 		 *  Description of the Class
 		 *
 		 * @author     jiger
 		 * @created    February 24, 2003
 		 */
 
 		class ListHandler extends KeyAdapter implements MouseListener
 		{
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  event  Description of the Parameter
 			 */
 			public void keyPressed(KeyEvent event)
 			{
 				if(event.getKeyCode() == KeyEvent.VK_ENTER)
 				{
 					handleListEvents(event);
 				}
 			}
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  e  Description of the Parameter
 			 */
 			public void mouseClicked(MouseEvent e)
 			{
 				if(e.getClickCount() == 2)
 				{
 					handleListEvents(e);
 				}
 			}
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  e  Description of the Parameter
 			 */
 			public void mouseEntered(MouseEvent e)
 			{}
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  e  Description of the Parameter
 			 */
 			public void mouseExited(MouseEvent e)
 			{}
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  e  Description of the Parameter
 			 */
 			public void mousePressed(MouseEvent e)
 			{}
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  e  Description of the Parameter
 			 */
 			public void mouseReleased(MouseEvent e)
 			{}
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  event  Description of the Parameter
 			 */
 			private void handleListEvents(AWTEvent event)
 			{
 				txtfilename.grabFocus();
 				txtfilename.selectAll();
 				onefileselection(event);
 				closeMainWindow();
 			}
 		}//End of class ListHandler
 
 
 		/**
 		 *  Description of the Class
 		 *
 		 * @author     jiger
 		 * @created    February 24, 2003
 		 */
 
 		class ProjectSwitchListener implements ItemListener
 		{
 			private boolean paused;
 
 
 			/**  Description of the Method */
 			public void pause()
 			{
 				paused = true;
 			}
 
 
 			/**  Description of the Method */
 			public void resume()
 			{
 				paused = false;
 			}
 
 
 
 			/**
 			 *  Description of the Method
 			 *
 			 * @param  evt  Description of the Parameter
 			 */
 			public void itemStateChanged(ItemEvent evt)
 			{
 				//Log.log(Log.DEBUG,this,"Inside itemStateChanged paused " + paused +" event is ItemEvent.SELECTED "+ (evt.getStateChange() == ItemEvent.SELECTED));
 				if(!paused && evt.getStateChange() == ItemEvent.SELECTED)
 				{
 					projectviewer.vpt.VPTProject newProject = (projectviewer.vpt.VPTProject)evt.getItem();
                     projectviewer.ProjectViewer.setActiveNode(view, newProject);
 					//projectviewer.config.ProjectViewerConfig.getInstance().setLastNode(newProject);
 					//Set PV(if there is one) to the new Project.
 					//projectviewer.ProjectViewer pv = projectviewer.ProjectViewer.getViewer(view);
 
 					//if(pv != null)
 					//{
 						//System.out.println("PV present when switching project. Setting to OV Object!! " + newProject);
 						//pv.setProject(projectviewer.ProjectManager.getInstance().getProject(newProject));
 						//pv.setProject(newProject);
                         //pv.setActiveNode(view,newProject);
 						mainWindow.toFront();//when PV is updating itself, FO loses focus. Hopefully this call should work in some jdk/platforms if implemented properly by the platform's jdk.
 					//}
 					//else
 					//{
 						//System.out.println("PV is NULL!!! when setting " + newProject);
 					//}
 
 					/* if(_foundfileslist != null)
 					{
 						_foundfileslist.clear();
 					} */
 
 					jlist.setListData(new String[0]);
 					//allfiles=null; //Very important. Not having this, means that retrieveMatchingFiles will not search the newly selected Project at all!!
 					//updateTitle();
 					indexManager.suggestReindex();
 
 					txtfilename.grabFocus();
 					txtfilename.selectAll();
 				}
 			}
 
 		}//End of ProjectSwitchListener
 
 
 		/**
 		 *  FastOpen comparator class to compare two Project files for various stuff
 		 *  like already open, etc
 		 *
 		 * @author     jiger
 		 * @created    February 24, 2003
 		 */
 
 		class FastOpenComparator implements Comparator
 		{
 			Comparator collator = java.text.Collator.getInstance();
 
 
 			/**
 			 *  Compares 2 files for sorting purpose.
 			 *
 			 * @param  obj1  FastOpenFile 1
 			 * @param  obj2  FastOpenFile 1
 			 * @return       a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
 			 */
 			public int compare(Object obj1, Object obj2)
 			{
 				if(obj1 instanceof FastOpenFile && obj2 instanceof FastOpenFile)
 				{
 					FastOpenFile pf1 = (FastOpenFile)obj1;
 					FastOpenFile pf2 = (FastOpenFile)obj2;
 					String fileOrder = jEdit.getProperty("fastopen.filesOrder");
 
 					if(fileOrder != null && fileOrder.equals(FastOpenPlugin.OPEN_FILES_FIRST))
 					{
 						if(pf1.isOpened() && !pf2.isOpened())
 						{
 							return -1;
 						}
 						else if(!pf1.isOpened() && pf2.isOpened())
 						{
 							return 1;
 						}
 						else if(!pf1.isOpened() && !pf2.isOpened()) //Incase of closed files, Project files should take precedence.
 						{
 							//Then check for which one is Project file.
 							if(pf1.isProjectFile() && !pf2.isProjectFile())
 							{
 								return -1;
 							}
 							else if(!pf1.isProjectFile() && pf2.isProjectFile())
 							{
 								return 1;
 							}
 							else //Let the default comparator take care.
 							{
 								return collator.compare(pf1.getDecoratedPath().toLowerCase(), pf2.getDecoratedPath().toLowerCase()); //Paths are converted to lowercase for comparision to simulate case-insensivitivity becoz not doing so would lead to showing the same file twice if the path contains mixed case letters. For e.g jEdit Recent files would store path as C:/dir/path_to_file.txt and Projectviewer would as c:\dir/path_to_file.txt (notice the c drive case).
 							}
 						}
 						else
 						{
 							return collator.compare(pf1.getDecoratedPath().toLowerCase(), pf2.getDecoratedPath().toLowerCase()); //Paths are converted to lowercase for comparision to simulate case-insensivitivity becoz not doing so would lead to showing the same file twice if the path contains mixed case letters. For e.g jEdit Recent files would store path as C:/dir/path_to_file.txt and Projectviewer would as c:\dir/path_to_file.txt (notice the c drive case).
 						}
 					}
 					else if(fileOrder != null && fileOrder.equals(FastOpenPlugin.OPEN_FILES_LAST))
 					{
 						if(pf1.isOpened() && !pf2.isOpened())
 						{
 							return 1;
 						}
 						else if(!pf1.isOpened() && pf2.isOpened())
 						{
 							return -1;
 						}
 						else if(!pf1.isOpened() && !pf2.isOpened()) //Incase of closed files, Project files should take precedence.
 						{
 							//Then check for which one is Project file.
 							if(pf1.isProjectFile() && !pf2.isProjectFile())
 							{
 								return 1;
 							}
 							else if(!pf1.isProjectFile() && pf2.isProjectFile())
 							{
 								return -1;
 							}
 							else //Let the default comparator take care.
 							{
 								return collator.compare(pf1.getDecoratedPath().toLowerCase(), pf2.getDecoratedPath().toLowerCase()); //Paths are converted to lowercase for comparision to simulate case-insensivitivity becoz not doing so would lead to showing the same file twice if the path contains mixed case letters. For e.g jEdit Recent files would store path as C:/dir/path_to_file.txt and Projectviewer would as c:\dir/path_to_file.txt (notice the c drive case).
 							}
 						}
 						else
 						{
 							return collator.compare(pf1.getDecoratedPath().toLowerCase(), pf2.getDecoratedPath().toLowerCase());
 						}
 					}
 					else
 					{
 						return collator.compare(pf1.getDecoratedPath().toLowerCase(), pf2.getDecoratedPath().toLowerCase());
 					}
 				}
 				return collator.compare(obj1, obj2);
 			}
 
 		}//End of class FastOpenComparator
 
 
 		/**
 		 *  Renderer for Matching File List.
 		 *
 		 * @author     jiger
 		 * @created    February 24, 2003
 		 */
 
 		class FastOpenRenderer extends DefaultListCellRenderer
 		{
 			/**
 			 *  Gets the listCellRendererComponent attribute of the FastOpenRenderer
 			 *  object
 			 *
 			 * @param  list          Description of the Parameter
 			 * @param  value         Description of the Parameter
 			 * @param  index         Description of the Parameter
 			 * @param  isSelected    Description of the Parameter
 			 * @param  cellHasFocus  Description of the Parameter
 			 * @return               The listCellRendererComponent value
 			 */
 			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
 			{
 				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 				FastOpenFile file = (FastOpenFile)value; //(FastOpenFile)_foundfileslist.get(index);
 				setText(file.getDecoratedPath());
 				if(file.isOpened())
 				{
 					if(file.isProjectFile())
 					{
 						setForeground(FastOpen.openFilesForeground);
 					}
 					else
 					{//Open, Non-Project file
 						setForeground(FastOpen.nonprjopenFilesForeground);
 					}
 				}
 				return this;
 			}
 
 		}//End of class FastOpenRenderer
 
 
 		/**
 		*	This class is used for sorting the project combo box.
 		*/
 		public static class Sorter implements Comparator
 		{
 			public int compare(Object element1, Object element2)
 			{
 				String lower1 = element1.toString().toLowerCase();
 				String lower2 = element2.toString().toLowerCase();
 				return lower1.compareTo(lower2);
 			}
 		}//End of Sorter
 
 
 		class DelaySearchAction implements ActionListener
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				findfile();
 				timer = null;
 			}
 		}
 	//Inner Classes Ends.
 
 	public void reindex()
 	{
 		indexManager.suggestReindex();
 	}
 
	public void focusOnDefaultComponent()
	{
		txtfilename.requestFocus();
	}
 	
 	class KeyHandler extends KeyAdapter
 	{
 		public void keyPressed(KeyEvent evt)
 		{
 			if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
 			{
 				closeMainWindow();
 				evt.consume();
 			}
 		}
 	}
 	
 }
 
