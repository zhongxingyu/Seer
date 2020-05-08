 //----------------------------------------------------------------------------
 // $Id$
 //----------------------------------------------------------------------------
 
 package hexgui.gui;
 
 import hexgui.hex.*;
 import hexgui.util.Pair;
 import hexgui.util.StringUtils;
 import hexgui.game.Node;
 import hexgui.game.GameInfo;
 import hexgui.sgf.SgfWriter;
 import hexgui.sgf.SgfReader;
 import hexgui.htp.HtpController;
 import hexgui.htp.HtpError;
 import hexgui.htp.StreamCopy;
 import hexgui.version.Version;
 
 import java.io.*;
 import java.util.*;
 import javax.swing.*;          
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.event.*;
 import java.net.*;
 
 //----------------------------------------------------------------------------
 
 /** HexGui. */
 public final class HexGui 
     extends JFrame 
     implements ActionListener, GuiBoard.Listener, HtpShell.Callback,
                AnalyzeDialog.Callback, AnalyzeDialog.SelectionCallback
 {
     public HexGui()
     {
         super("HexGui");
 
 	System.out.println("HexGui v" + Version.id + "; " + Version.date 
 			   + "; build " + Version.build + "\n");
 	
 	// Catch the close action and shutdown nicely
 	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 	addWindowListener(new java.awt.event.WindowAdapter() 
 	    {
 		public void windowClosing(WindowEvent winEvt) {
 		    cmdShutdown();
 		}
 	    });
 
         m_selected_cells = new Vector<HexPoint>();
 
         m_about = new AboutDialog(this);
 
 	m_preferences = new GuiPreferences(getClass());
 	
 	m_menubar = new GuiMenuBar(this, m_preferences);
 	setJMenuBar(m_menubar.getJMenuBar());
 
 	m_toolbar = new GuiToolBar(this, m_preferences);
         getContentPane().add(m_toolbar.getJToolBar(), BorderLayout.NORTH);
 
         m_statusbar = new StatusBar();
         getContentPane().add(m_statusbar, BorderLayout.SOUTH);
 
 	m_guiboard = new GuiBoard(this, m_preferences);
         getContentPane().add(m_guiboard, BorderLayout.CENTER);
 
 	cmdNewGame();
 
         pack();
         setVisible(true);
 
     }
 
     //------------------------------------------------------------
     public void actionPerformed(ActionEvent e) 
     {
 // 	System.out.println("-----------------");
 // 	System.out.println("Received Action Event: ");
 // 	System.out.println(e.getActionCommand());
 // 	System.out.println(e.paramString());
 
 	String cmd = e.getActionCommand();
 
 	//
 	// system commands
 	//
 	if (cmd.equals("shutdown"))
 	    cmdShutdown();
 	else if (cmd.equals("connect-program"))
 	    cmdConnectRemoteProgram();
 	else if (cmd.equals("connect-local-program"))
 	    cmdConnectLocalProgram();
 	else if (cmd.equals("disconnect-program"))
 	    cmdDisconnectProgram();
 	//
 	// file/help commands
 	//
 	else if (cmd.equals("newgame")) 
 	    cmdNewGame();
 	else if (cmd.equals("savegame")) {
 	    if (gameChanged()) 
                 cmdSaveGame();
         }
 	else if (cmd.equals("savegameas"))
 	    cmdSaveGameAs();
 	else if (cmd.equals("loadgame"))
 	    cmdLoadGame();
         else if (cmd.equals("save-position-as"))
             cmdSavePositionAs();
 	else if (cmd.equals("about"))
 	    cmdAbout();
 	//
 	// gui commands
 	//
 	else if (cmd.equals("gui_toolbar_visible"))
 	    cmdGuiToolbarVisible();
 	else if (cmd.equals("gui_shell_visible"))
 	    cmdGuiShellVisible();
 	else if (cmd.equals("gui_analyze_visible"))
             cmdGuiAnalyzeVisible();
         else if (cmd.equals("gui_evalgraph_visible"))
             cmdGuiEvalGraphVisible();
 	else if (cmd.equals("gui_board_draw_type"))
 	    cmdGuiBoardDrawType();
 	else if (cmd.equals("gui_board_orientation"))
 	    cmdGuiBoardOrientation();
         else if (cmd.equals("show-preferences"))
             cmdShowPreferences();
 	//
         // game navigation commands  
 	//
         else if (cmd.equals("game_beginning"))
 	    backward(1000);
 	else if (cmd.equals("game_backward10"))
 	    backward(10);
 	else if (cmd.equals("game_back"))
 	    backward(1);
 	else if (cmd.equals("game_forward"))
 	    forward(1);
 	else if (cmd.equals("game_forward10"))
 	    forward(10);
 	else if (cmd.equals("game_end"))
 	    forward(1000);
 	else if (cmd.equals("game_up"))
 	    up();
 	else if (cmd.equals("game_down")) 
 	    down();
         else if (cmd.equals("game_swap"))
             humanMove(new Move(HexPoint.get("swap-pieces"), m_tomove));
 	else if (cmd.equals("genmove")) 
 	    htpGenMove(m_tomove);
 	else if (cmd.equals("stop")) {
 
         } else if (cmd.equals("toggle_tomove")) {
             cmdToggleToMove();
         } else if (cmd.equals("set_to_move")) {
             cmdSetToMove();
         } else if (cmd.equals("toggle_click_context")) {
             cmdToggleClickContext();
         }
 
 	//
 	// unknown command
 	//
 	else {
 	    System.out.println("Unknown command: '" + cmd + "'.");
 	}
     }
 
     //------------------------------------------------------------
     private void cmdShutdown()
     {
 	if (gameChanged() && !askSaveGame())
 	    return;
 
 	System.out.println("Shutting down...");
 
 	if (m_white_process != null) {
 	    System.out.println("Stopping [" + m_white_name + " " + 
 			       m_white_version + "] process...");
 	    m_white_process.destroy();
 	}
 
 	System.exit(0);
     }
 
     private void cmdConnectRemoteProgram()
     {
 	int port = 20000;
 	String hostname = "localhost";
 
         String remote = m_preferences.get("remote-host-name");
         String name = RemoteProgramDialog.show(this, remote);
         if (name == null) // user aborted
             return;
 
         hostname = name;
 	System.out.print("Connecting to HTP program at [" + hostname + 
 			 "] on port " + port + "...");
 	System.out.flush();
 
 	try {
 	    m_white_socket = new Socket(hostname, port);
 	}
 	catch (UnknownHostException e) {
 	    showError("Unknown host: '" + e.getMessage() + "'");
             System.out.println("\nconnection attempt aborted.");
 	    return;
 	}
 	catch (IOException e) {
 	    showError("Error creating socket: '" + e.getMessage() + "'");
             System.out.println("\nconnection attempt aborted.");
 	    return;
 	}
 	System.out.println("connected.");
 
 
 	InputStream in;
 	OutputStream out;
 	try {
 	    in = m_white_socket.getInputStream();
 	    out = m_white_socket.getOutputStream();
 	}
 	catch (IOException e) {
 	    showError("Error obtaing socket stream: " + e.getMessage());
 	    m_white = null;
 	    return;
 	}
 
         m_preferences.put("remote-host-name", hostname);
 
 	connectProgram(in, out);
     }
 
     private void cmdConnectLocalProgram()
     {
 	String defaultCommand = m_preferences.get("path-local-program");
         String prog = LocalProgramDialog.show(this, defaultCommand);
 	if (prog == null) // user aborted
 	    return;
 
 	Runtime runtime = Runtime.getRuntime();
 	String cmd = prog;
 	System.out.println("Executing '" + cmd + "'...");
 	try {
 	    m_white_process = runtime.exec(cmd);
 	}
 	catch (Throwable e) {
 	    showError("Error starting program: '" + e.getMessage() + "'");
 	    return;
 	}
 
 	m_preferences.put("path-local-program", prog);
 
  	Process proc = m_white_process;
 
 	///////////////////////////////
 	/// FIXME: DEBUGING!!! REMOVE!
 	Thread blah = new Thread(new StreamCopy(proc.getErrorStream(),
 						System.out));
 	blah.start();
 	///////////////////////////////
 
 	connectProgram(proc.getInputStream(), proc.getOutputStream());
     }
 
     private void connectProgram(InputStream in, OutputStream out)
     {
         m_analyze = new AnalyzeDialog(this, this, this, m_statusbar);
 	m_analyze.addWindowListener(new WindowAdapter() 
 	    {
 		public void windowClosing(WindowEvent winEvt) {
 		    m_menubar.setAnalyzeVisible(false);
 		}
 	    });
 
 	m_shell = new HtpShell(this, this);
 	m_shell.addWindowListener(new WindowAdapter() 
 	    {
 		public void windowClosing(WindowEvent winEvt) {
 		    m_menubar.setShellVisible(false);
 		}
 	    });
 	m_white = new HtpController(in, out, m_shell);
 
 	htpName();
 	htpVersion();
 
 	m_shell.setTitle("HexGui: [" + m_white_name + " " 
                             + m_white_version + "] Shell");
 
         htpListCommands();   // FIXME: make sure we block until its
                              // callback is finished (when we do stuff
                              // in separate threads. Not an issue now. 
 		 
 	m_toolbar.setProgramConnected(true);
 	m_menubar.setProgramConnected(true);
 
 	htpBoardsize(m_guiboard.getBoardSize());
 
         // play up to current move
 	Node cur = m_root;
         if (cur.hasSetup())
             playSetup(cur);
 	while (cur != m_current) {
 	    cur = cur.getChildContainingNode(m_current);
             if (cur.hasMove())
                 htpPlay(cur.getMove());
             else if (cur.hasSetup()) 
                 playSetup(cur);
 	}
         htpShowboard();
     }
 
     private void cmdDisconnectProgram()
     {
 	if (m_white == null) 
 	    return;
 
 	htpQuit();
 	try {
 	    if (m_white_process != null) {
 		m_white_process.waitFor();
 		m_white_process = null;
 	    } 
 	    if (m_white_socket != null) {
 		m_white_socket.close();
 		m_white_socket = null;
 	    }
 	    m_white = null;
 	    m_shell.dispose();
 	    m_shell = null;
             m_analyze.dispose();
             m_analyze = null;
 	    m_menubar.setProgramConnected(false);
 	    m_toolbar.setProgramConnected(false);
 	}
 	catch (Throwable e) {
 	    showError("Error: " + e.getMessage());
 	}
     }
 
     //------------------------------------------------------------
 
     private void cmdNewGame()
     {
 	if (gameChanged() && !askSaveGame())
 	    return;
 
 	String size = m_menubar.getSelectedBoardSize();
 	Dimension dim = new Dimension(-1,-1);
 	if (size.equals("Other...")) {
 	    size = BoardSizeDialog.show(this, m_guiboard.getBoardSize());
 	    if (size == null) return;
 	}
 
 	try {
 	    StringTokenizer st = new StringTokenizer(size);
 	    int w = Integer.parseInt(st.nextToken());
 	    st.nextToken();
 	    int h = Integer.parseInt(st.nextToken());
 	    dim.setSize(w,h);
 	}
 	catch (Throwable t) {
 	    showError("Size should be in format 'w x h'.");
 	    return;
 	}
 
 	if (dim.width < 1 || dim.height < 1) {
 	    showError("Invalid board size.");
 	} else {
 	    m_tomove = HexColor.BLACK;
             m_toolbar.setToMove(m_tomove.toString());
 
 	    m_root = new Node();
 	    m_current = m_root;
 	    m_gameinfo = new GameInfo();
 	    m_gameinfo.setBoardSize(dim);
 	    m_file = null;
 	    setGameChanged(false);
 	    setFrameTitle();
 	    
 	    m_guiboard.initSize(dim.width, dim.height);
 	    m_guiboard.repaint();
 
 	    m_preferences.put("gui-board-width", dim.width);
 	    m_preferences.put("gui-board-height", dim.height);
 
 	    m_toolbar.updateButtonStates(m_current);
             m_menubar.updateMenuStates(m_current);
             
             htpBoardsize(m_guiboard.getBoardSize());
             htpShowboard();
 	}
     }
 
     private boolean cmdSaveGame()
     {
 	if (m_file == null) 
 	    m_file = showSaveAsDialog();
 	
 	if (m_file != null) {
 	    System.out.println("Saving to file: " + m_file.getName());
 	    if (save(m_file)) {
 		setGameChanged(false);
 		setFrameTitle();
 		m_preferences.put("path-save-game", m_file.getPath());
 		return true;
 	    }
 	}
 	return false;
     }
 
     private boolean cmdSaveGameAs()
     {
 	File file = showSaveAsDialog();
 	if (file == null) 
 	    return false;
 
 	m_file = file;
 	return cmdSaveGame();
     }
 
     private void cmdSavePositionAs()
     {
         File file = showSaveAsDialog();
         if (file != null)
             savePosition(file);
     }
 
     private void cmdLoadGame()
     {
 	if (gameChanged() && !askSaveGame())
 	    return;
 
 	File file = showOpenDialog();
 	if (file == null) return;
 
 	System.out.println("Loading sgf from file: " + file.getName());
 	SgfReader sgf = load(file);
 	if (sgf != null) {
 	    m_root = sgf.getGameTree();
 	    m_gameinfo = sgf.getGameInfo();
 	    m_current = m_root;
 
 	    m_guiboard.initSize(m_gameinfo.getBoardSize());
             htpBoardsize(m_guiboard.getBoardSize());
 
             if (m_root.hasSetup())
                 playSetup(m_root);
 	    forward(1000);
 
 	    m_file = file;
 	    setGameChanged(false);
 	    setFrameTitle();
 
 	    m_preferences.put("path-load-game", file.getPath());
 	}
     }
 
     private void cmdAbout()
     {
         m_about.setVisible(true);
     }
 
     //------------------------------------------------------------
 
     private void cmdGuiToolbarVisible()
     {
 	boolean visible = m_menubar.getToolbarVisible();
 	m_toolbar.setVisible(visible);
     }
 
     private void cmdGuiShellVisible()
     {
 	if (m_shell == null) return;
 	boolean visible = m_menubar.getShellVisible();
 	m_shell.setVisible(visible);
     }
 
     private void cmdGuiAnalyzeVisible()
     {
 	if (m_analyze == null) return;
 	boolean visible = m_menubar.getAnalyzeVisible();
 	m_analyze.setVisible(visible);
     }
 
     private void cmdGuiEvalGraphVisible()
     {
 	boolean visible = m_menubar.getEvalGraphVisible();
         if (visible) {
             
             Vector<Integer> movenum = new Vector<Integer>();
             Vector<Double> score = new Vector<Double>();
 
             int move = 1;
             Node node = m_root;
             while (node != null) {
                 String value = node.getSgfProperty("V");
                 if (value != null) {
                     movenum.add(new Integer(move++));
                     score.add(new Double(value));
                 }
 
                 Node child = node.getChildContainingNode(m_current);
                 if (child == null)
                     child = node.getChild();
                 
                 node = child;
             }
 
             m_evalgraph = new EvalGraphDialog(this, movenum, score);
             m_evalgraph.addWindowListener(new WindowAdapter() 
 	    {
 		public void windowClosing(WindowEvent winEvt) {
 		    m_menubar.setEvalGraphVisible(false);
 		}
 	    });
         } 
         else {
             m_evalgraph.setVisible(false);
         }
     }
 
     private void cmdGuiBoardDrawType()
     {
 	String type = m_menubar.getCurrentBoardDrawType();
 	System.out.println(type);
 	m_guiboard.setDrawType(type);
 	m_guiboard.repaint();
     }
 
     private void cmdGuiBoardOrientation()
     {
 	String type = m_menubar.getCurrentBoardOrientation();
 	System.out.println(type);
 	m_guiboard.setOrientation(type);
 	m_guiboard.repaint();
     }
 
     private void cmdShowPreferences()
     {
         new PreferencesDialog(this, m_preferences);
     }
 
     private void cmdToggleToMove()
     {
         m_tomove = m_tomove.otherColor();
         m_toolbar.setToMove(m_tomove.toString());
         m_menubar.setToMove(m_tomove.toString());
     }
 
     private void cmdSetToMove()
     {
         m_tomove = HexColor.get(m_menubar.getToMove());
         m_toolbar.setToMove(m_tomove.toString());
     }
 
     private void cmdToggleClickContext()
     {
         String context = m_toolbar.getClickContext();
         if (context.equals("play"))
             m_toolbar.setClickContext("setup");
         else if (context.equals("setup"))
             m_toolbar.setClickContext("play");
         else 
             System.out.println("Unknown context!! '" + context + "'");
     }
 
 
     //------------------------------------------------------------
 
     /** Analyze dialog callback; calls the commandEntered method. */
     public void analyzeCommand(String cmd)
     {
         commandEntered(cmd);
     }
 
     /** HtpShell Callback. 
         By the name of the command it choose the propery callback function.
         Arguments are passed as given. 
     */
     
     public void commandEntered(String cmd)
     {
         String cleaned = StringUtils.cleanWhiteSpace(cmd.trim());
         String args[] = cleaned.split(" ");
 	String c = args[0];
 
         Runnable cb = new Runnable() { public void run() { cbEmptyResponse(); } };
 
 	if (c.equals("name"))
             cb = new Runnable() { public void run() { cbName(); } };
         else if (c.equals("version")) 
             cb = new Runnable() { public void run() { cbVersion(); } };
         else if (c.equals("genmove")) 
             cb = new Runnable() { public void run() { cbGenMove(); } };
         else if (c.equals("all_legal_moves")) 
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
         else if (c.equals("get_absorb_group"))
             cb = new Runnable() { public void run() { cbGetAbsorbGroup(); } };
 
 	else if (c.equals("shortest_paths")) 
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
 	else if (c.equals("shortest_vc_paths")) 
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
 
         else if (c.equals("compute_dead_cells"))
             cb = new Runnable() { public void run() { cbComputeDeadCells(); } };
         else if (c.equals("vc-build"))
             cb = new Runnable() { public void run() { cbComputeDeadCells(); } };
         else if (c.equals("vc-build-incremental"))
             cb = new Runnable() { public void run() { cbComputeDeadCells(); } };
 
 
         else if (c.equals("solver-find-winning"))
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };            
 
         else if (c.equals("find_sealed")) 
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
         else if (c.equals("strengthen_vcs")) 
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
 
 
         else if (c.equals("book-depths")) 
             cb = new Runnable() { public void run() { cbDisplayPointText(); } };
         else if (c.equals("book-sizes")) 
             cb = new Runnable() { public void run() { cbDisplayPointText(); } };
         else if (c.equals("book-scores"))
             cb = new Runnable() { public void run() { cbDisplayPointText(); } };
 	else if (c.equals("book-priorities"))
             cb = new Runnable() { public void run() { cbDisplayPointText(); } };
 
 
         else if (c.equals("vc-connected-to")) 
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
         else if (c.equals("vc-between-cells"))
             cb = new Runnable() { public void run() { cbBetweenCells(); } };
         else if (c.equals("vc-get-mustplay"))
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
         else if (c.equals("vc-intersection"))
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
         else if (c.equals("vc-union"))
             cb = new Runnable() { public void run() { cbDisplayPointList(); } };
 
         else if (c.equals("eval_twod")) 
             cb = new Runnable() { public void run() { cbDisplayPointText(); } };
         else if (c.equals("eval_resist")) 
             cb = new Runnable() { public void run() { cbEvalResist(); } };
         else if (c.equals("eval_resist_delta")) 
             cb = new Runnable() { public void run() { cbEvalResistDelta(); } };
 	else if (c.equals("eval_influence"))
             cb = new Runnable() { public void run() { cbDisplayPointText(); } };
 
         else if (c.equals("mohex-show-rollout")) 
             cb = new Runnable() { public void run() { cbMohexShowRollout(); } };
         else if (c.equals("quit")) 
             cb = new Runnable() { public void run() { cbEmptyResponse(); } };
         
         sendCommand(cmd, cb);
     }
 
     private void sendCommand(String cmd, Runnable callback)
     {
 	if (m_white == null) 
 	    return;
 
 	try {
 	    m_white.sendCommand(cmd, callback);
 	}
 	catch (HtpError e) {
             showError(e.getMessage());
 	}
     }
 
     // FIXME: add callback?
     private void htpQuit()
     {
 	sendCommand("quit\n", null);
     }
 
     // FIXME: handle errors!
     public void cbName()
     {
 	String str = m_white.getResponse();
 	m_white_name = str.trim();
     }
 
     private void htpName()
     {
 	Runnable callback = new Runnable() 
 	    { 
 		public void run() { cbName(); } 
 	    };
 	sendCommand("name\n", callback);
     }
 
     // FIXME: handle errors!
     public void cbVersion()
     {
 	String str = m_white.getResponse();
 	m_white_version = str.trim();
     }
 
     private void htpVersion()
     {
 	Runnable callback = new Runnable()
 	    {
 		public void run() { cbVersion(); }
 	    };
 	sendCommand("version\n", callback);
     }
 
     private void cbListCommands()
     {
         if (m_analyze == null) {
             System.out.println("No analyze dialog!!");
             return;
         }
         
         String str = m_white.getResponse();
 	Vector<String> cmds = StringUtils.parseStringList(str);
         Collections.sort(cmds);
         m_analyze.setCommands(cmds);
 
     }
 
     private void htpListCommands()
     {
 	Runnable callback = new Runnable()
 	    {
 		public void run() { cbListCommands(); }
 	    };
 	sendCommand("list_commands\n", callback);
     }
 
 
     // FIXME: check for errors
     public void cbEmptyResponse()
     {
     }
 
     private void htpShowboard()
     {
         sendCommand("showboard\n", null);
     }
 
     private void htpPlay(Move move)
     {
 	Runnable callback = new Runnable()
 	    {
 		public void run() { cbEmptyResponse(); }
 	    };
 
 	sendCommand("play " + move.getColor().toString() + 
 		    " " + move.getPoint().toString() + "\n", 
 		    callback);
         m_statusbar.setMessage(move.getColor().toString() + " " +  
                                move.getPoint().toString());
     }
  
     private void htpUndo()
     {
 	Runnable callback = new Runnable()
 	    {
 		public void run() { cbEmptyResponse(); }
 	    };
 
 	sendCommand("undo\n", callback);
         m_statusbar.setMessage("Undo!");
     }
 
     private void htpBoardsize(Dimension size)
     {
 	Runnable callback = new Runnable()
 	    {
 		public void run() { cbEmptyResponse(); }
 	    };
 
         sendCommand("boardsize " + size.width + " " + size.height + "\n",
                     callback);
         m_statusbar.setMessage("New game");
     }
 
     public void cbGenMove()
     {
         if (!m_white.wasSuccess())
             return;
 
 	String str = m_white.getResponse();
 	HexPoint point = HexPoint.get(str.trim());
 	if (point == null) {
 	    System.out.println("Invalid move!!");
 	} else {
 	    play(new Move(point, m_tomove));
 	}
     }
 
     private void htpGenMove(HexColor color)
     {
 	Runnable callback = new Runnable() 
 	    { 
 		public void run() { cbGenMove(); } 
 	    };
 	sendCommand("genmove " + color.toString() + "\n", callback);
 	sendCommand("showboard\n", null);
     }
 
     public void cbDisplayPointList()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
 
 	String str = m_white.getResponse();
 	Vector<HexPoint> points = StringUtils.parsePointList(str);
         m_guiboard.clearMarks();
         for (int i=0; i<points.size(); i++) {
 	    m_guiboard.setAlphaColor(points.get(i), Color.green);
 	}
 	m_guiboard.repaint();
     }
 
     private void htpAllLegalMoves()
     {
 	Runnable callback = new Runnable() 
 	    { 
 		public void run() { cbDisplayPointList(); } 
 	    };
 	sendCommand("all_legal_moves\n", callback);
     }
 
     public void cbGetAbsorbGroup()
     {
         if (!m_white.wasSuccess())
             return;
 
 	String str = m_white.getResponse();
 	Vector<HexPoint> points = StringUtils.parsePointList(str);
         m_guiboard.clearMarks();
 
         if (points.size() > 0) {
             m_guiboard.setAlphaColor(points.get(0), Color.blue);
             for (int i=1; i<points.size(); i++) {
                 m_guiboard.setAlphaColor(points.get(i), Color.green);
             }
         }
 	m_guiboard.repaint();
     }
 
     public void cbComputeDeadCells()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
 
 	String str = m_white.getResponse();
         Vector<Pair<String, String> > pairs = 
             StringUtils.parseStringPairList(str);
 
         m_guiboard.clearMarks();
         m_guiboard.aboutToDirtyStones();
         for (int i=0; i<pairs.size(); i++) {
 	    HexPoint point = HexPoint.get(pairs.get(i).first);
             String value = pairs.get(i).second;
             if (value.charAt(0) == 'd') {        // dead
                 m_guiboard.setAlphaColor(point, Color.cyan);
                 if (value.length() >= 2) {
                     if (value.charAt(1) == 'b')
                         m_guiboard.setColor(point, HexColor.BLACK);
                     else 
                         m_guiboard.setColor(point, HexColor.WHITE);
                 }
             }
             else if (value.charAt(0) == '#') {  // vulnerable
                 m_guiboard.setAlphaColor(point, Color.green);
                 if (value.charAt(1) == '[' && 
                     value.charAt(value.length()-1) == ']') {
                     String pts = value.substring(2, value.length()-1);
                     Vector<HexPoint> pp = StringUtils.parsePointList(pts,"-");
                     for (int j=0; j<pp.size(); ++j) {
                         m_guiboard.addArrow(point, pp.get(j));
                     }
                 }
             }
             else if (value.charAt(0) == '!') {  // dominated
                 HexPoint to = HexPoint.get(value.substring(1));
                 m_guiboard.addArrow(point, to);
                 m_guiboard.setAlphaColor(point, Color.yellow);
             }
             else if (value.charAt(0) == 'b') {  // black captured
                 m_guiboard.setColor(point, HexColor.BLACK);
                 m_guiboard.setAlphaColor(point, Color.red);
             }  
             else if (value.charAt(0) == 'w') {  // white captured
                 m_guiboard.setColor(point, HexColor.WHITE);
                 m_guiboard.setAlphaColor(point, Color.red);
             }                
 	}
 	m_guiboard.repaint();
     }
 
 
     //==================================================
     // vc commands
     //==================================================
 
     public void cbBetweenCells()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
         
         String str = m_white.getResponse();
         Vector<VC> vcs = StringUtils.parseVCList(str);
         new VCDisplayDialog(this, m_guiboard, vcs);
         
     }
 
     //==================================================
     // evaluation commands
     //==================================================
     
     public void cbDisplayPointText()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
 
 	String str = m_white.getResponse();
         Vector<Pair<String, String> > pairs = 
             StringUtils.parseStringPairList(str);
 
         m_guiboard.clearMarks();
 
         for (int i=0; i<pairs.size(); i++) {
 	    HexPoint point = HexPoint.get(pairs.get(i).first);
             String value = pairs.get(i).second;
             m_guiboard.setText(point, value);
 	}
 	m_guiboard.repaint();
     }
 
     public void cbEvalResist()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
 
 	String str = m_white.getResponse();
         Vector<Pair<String, String> > pairs = 
             StringUtils.parseStringPairList(str);
 
         String res = "";
         String rew = "";
         String reb = "";
         m_guiboard.clearMarks();
         for (int i=0; i<pairs.size(); i++) {
             if (pairs.get(i).first.equals("res")) {
                 res = pairs.get(i).second;
             } else if (pairs.get(i).first.equals("rew")) {
                 rew = pairs.get(i).second;
             } else if (pairs.get(i).first.equals("reb")) {
                 reb = pairs.get(i).second;
             } else {
                 HexPoint point = HexPoint.get(pairs.get(i).first);
                 String value = pairs.get(i).second;
                 m_guiboard.setText(point, value);
             }
 	}
 	m_guiboard.repaint();
         m_statusbar.setMessage("Resistance: " + res + 
                                " (" + rew + " - " + reb + ")");
     }
 
     public void cbEvalResistDelta()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
 
 	String str = m_white.getResponse();
         Vector<Pair<String, String> > pairs = 
             StringUtils.parseStringPairList(str);
 
         String res = "";
         m_guiboard.clearMarks();
         for (int i=0; i<pairs.size(); i++) {
             if (pairs.get(i).first.equals("res")) {
                 res = pairs.get(i).second;
             } else {
                 HexPoint point = HexPoint.get(pairs.get(i).first);
                 String value = pairs.get(i).second;
                 m_guiboard.setText(point, value);
             }
 	}
 	m_guiboard.repaint();
         m_statusbar.setMessage("Resistance: " + res);
     }
 
     //==================================================
     // commands specific to mohex
     //==================================================
 
     public void cbMohexShowRollout()
     {
 	if (!m_white.wasSuccess()) 
 	    return;
 
 	String str = m_white.getResponse();
         Vector<Pair<String, String> > pairs = 
             StringUtils.parseStringPairList(str);
 
         m_guiboard.clearMarks();
         m_guiboard.aboutToDirtyStones();
 
         HexPoint p = HexPoint.get(pairs.get(0).first);
         HexColor color = HexColor.get(pairs.get(0).second);
         m_guiboard.setColor(p, color);
         m_guiboard.setAlphaColor(p, Color.blue);
 
         for (int i=1; i<pairs.size(); i++) {
 	    HexPoint point = HexPoint.get(pairs.get(i).first);
             String value = pairs.get(i).second;
             if (value.equals("#"))
                 m_guiboard.setAlphaColor(point, Color.green);
             else
                 m_guiboard.setAlphaColor(point, Color.red);
 
             m_guiboard.setText(point, Integer.toString(i));
 
             color = color.otherColor();
             m_guiboard.setColor(point, color);
 	}
 	m_guiboard.repaint();
 
     }
 
     private void htpMohexShowRollout(HexPoint point, HexColor color)
     {
 	Runnable callback = new Runnable() 
 	    { 
 		public void run() { cbMohexShowRollout(); } 
 	    };
 
 	sendCommand("mohex-show-rollout " + point.toString() + " " 
                     + color.toString() + "\n",
                     callback);
     }
     
     //------------------------------------------------------------
 
 
     /** Callback from GuiBoard. 
 	Handle a mouse click.
     */
     public void fieldClicked(HexPoint point, boolean ctrl, boolean shift)
     {
         if (m_guiboard.areStonesDirty()) {
             m_guiboard.clearMarks();
         }
         
         if (ctrl) {
 
             if (!shift) {
                 for (int i=0; i<m_selected_cells.size(); i++) {
                     HexPoint p = m_selected_cells.get(i);
                     m_guiboard.setSelected(p, false);
                 }
                 m_selected_cells.clear();
 
                 m_guiboard.setSelected(point, true);
                 m_selected_cells.add(point);
             } else {
                 
                 int found_at = -1;
                 for (int i=0; i<m_selected_cells.size() && found_at == -1; i++) {
                     if (m_selected_cells.get(i) == point) 
                         found_at = i;
                 }
            
                 if (found_at != -1) {
                     m_guiboard.setSelected(point, false);
                     m_selected_cells.remove(found_at);
                 } else {
                     m_guiboard.setSelected(point, true);
                     m_selected_cells.add(point);
                 }
             }
 
             m_guiboard.repaint();
             
         } else {
 
             String context = m_toolbar.getClickContext();
             if (context.equals("play")) {
                 if (m_guiboard.getColor(point) == HexColor.EMPTY) {
                     humanMove(new Move(point, m_tomove));
                 }
             } else if (context.equals("setup")) {
 
                 m_statusbar.setMessage("Setup mode not supported yet!");
                 
             }
         }
     }
 
     public Vector<HexPoint> getSelectedCells()
     {
         return m_selected_cells;
     }
 
     public void humanMove(Move move)
     {
 	play(move);
 	htpPlay(move);
         htpShowboard();
         if (!m_guiboard.isBoardFull() && m_preferences.getBoolean("auto-respond"))
             htpGenMove(m_tomove);
     }
 
     private void play(Move move)
     {
         if (move.getPoint() == HexPoint.RESIGN)
             return;
 
         int variation = -1;
         for (int i=0; i<m_current.numChildren(); i++) {
            if (move.equals(m_current.getChild(i).getMove())) {
                 variation = i;
                 break;
             }
 	}
 
 	if (variation != -1) {
 
 	    m_current = m_current.getChild(variation);
 
 	} else {
 
             if (move.getPoint() == HexPoint.SWAP_PIECES) {
                 if (!m_current.isSwapAllowed()) {
                     showError("Swap move not allowed!");
                     return;
                 }
             } else {
                 if (m_guiboard.getColor(move.getPoint()) !=  HexColor.EMPTY) {
                     showError("Cell '" + move.getPoint().toString() + 
                               "' already occupied.");
                     return;
                 }
             }
 
 	    Node node = new Node(move);
 	    m_current.addChild(node);
 	    m_current = node;
 	}
 
         if (m_current.getMove().getPoint() != HexPoint.SWAP_PIECES) 
             cmdToggleToMove();
 
 	m_guiboard.setColor(m_current.getMove().getPoint(), 
                             m_current.getMove().getColor());
 
         m_guiboard.clearMarks();
 	markLastPlayedStone();
 
 	m_guiboard.paintImmediately();
 	m_toolbar.updateButtonStates(m_current);	
         m_menubar.updateMenuStates(m_current);
 
 	setGameChanged(true);
 	setFrameTitle();
     }
 
     private void playSetup(Node node)
     {
         Vector<HexPoint> black = node.getSetup(HexColor.BLACK);
         Vector<HexPoint> white = node.getSetup(HexColor.WHITE);
         for (int j=0; j<black.size(); j++) {
             HexPoint point = black.get(j);
             m_guiboard.setColor(point, HexColor.BLACK);
             htpPlay(new Move(point, HexColor.BLACK));
         }
         for (int j=0; j<white.size(); j++) {
             HexPoint point = white.get(j);
             m_guiboard.setColor(point, HexColor.WHITE);
             htpPlay(new Move(point, HexColor.WHITE));
         }
     }
 
     private void forward(int n)
     {
         m_guiboard.clearMarks();
 
 	for (int i=0; i<n; ) {
 	    Node child = m_current.getChild();
 	    if (child == null) break;
 
             if (child.hasMove()) {
                 Move move = child.getMove();
                 m_guiboard.setColor(move.getPoint(), move.getColor());
                 htpPlay(move);
                 if (move.getPoint() == HexPoint.RESIGN) {
                     m_statusbar.setMessage(move.getColor().toString() + 
                                            " resigned.");
                 }
 
                 i++;
             } else if (child.hasSetup()) {
                 playSetup(child);
             }
 
 	    m_current = child;
 	}
 	markLastPlayedStone();
 
 	m_guiboard.repaint();
 	m_toolbar.updateButtonStates(m_current);
         m_menubar.updateMenuStates(m_current);
         
         if (m_current.hasMove()) {
             m_tomove = m_current.getMove().getColor();
             if (m_current.getMove().getPoint() != HexPoint.SWAP_PIECES)
                 m_tomove = m_tomove.otherColor();
             m_toolbar.setToMove(m_tomove.toString());
         }
 
         htpShowboard();
     }
 
     private void backward(int n)
     {
         m_guiboard.clearMarks();
 
 	for (int i=0; i<n; ) {
 	    if (m_current == m_root) break;
             
             if (m_current.hasMove()) {
                 Move move = m_current.getMove();
                 m_guiboard.setColor(move.getPoint(), HexColor.EMPTY);
                 htpUndo();
                 htpShowboard();
                 i++;
             }
 
 	    m_current = m_current.getParent();
 	}
 	markLastPlayedStone();
 
 	m_guiboard.repaint();
 	m_toolbar.updateButtonStates(m_current);
         m_menubar.updateMenuStates(m_current);
 	    
 	if (m_current == m_root) 
 	    m_tomove = HexColor.BLACK;
 	else if (m_current.hasMove() && 
                  m_current.getMove().getPoint() != HexPoint.SWAP_PIECES) 
 	    m_tomove = m_current.getMove().getColor().otherColor();
         
         m_toolbar.setToMove(m_tomove.toString());
     }
 
     private void up()
     {
 	if (m_current.getNext() != null) {
             m_guiboard.clearMarks();
 
 	    HexPoint point = m_current.getMove().getPoint();
 	    m_guiboard.setColor(point, HexColor.EMPTY);
             htpUndo();
             htpShowboard();
 	    
 	    m_current = m_current.getNext();
 	    
 	    HexColor color = m_current.getMove().getColor();
 	    point = m_current.getMove().getPoint();
 	    m_guiboard.setColor(point, color);
 	    htpPlay(m_current.getMove());
             htpShowboard();
             if (point != HexPoint.SWAP_PIECES) {
                 m_tomove = color.otherColor();
                 m_toolbar.setToMove(m_tomove.toString());
             }
 
             m_guiboard.clearMarks();	    
 	    markLastPlayedStone();
 
 	    m_guiboard.repaint();
 	    m_toolbar.updateButtonStates(m_current);
             m_menubar.updateMenuStates(m_current);
 	}
     }
 
     private void down()
     {
 	if (m_current.getPrev() != null) {
             m_guiboard.clearMarks();
 
 	    HexPoint point = m_current.getMove().getPoint();
 	    m_guiboard.setColor(point, HexColor.EMPTY);
             htpUndo();
             htpShowboard();
 	    
 	    m_current = m_current.getPrev();
 	    
 	    HexColor color = m_current.getMove().getColor();
 	    point = m_current.getMove().getPoint();
 	    m_guiboard.setColor(point, color);
 	    htpPlay(m_current.getMove());
             htpShowboard();
             if (point != HexPoint.SWAP_PIECES) {
                 m_tomove = color.otherColor();
                 m_toolbar.setToMove(m_tomove.toString());
             }
 	    
             m_guiboard.clearMarks();
 	    markLastPlayedStone();
 
 	    m_guiboard.repaint();
 	    m_toolbar.updateButtonStates(m_current);
             m_menubar.updateMenuStates(m_current);
 	}
     }
 
 
     //------------------------------------------------------------
 
     private void markLastPlayedStone()
     {
         if (m_current == m_root || m_current.hasSetup()) {
             m_guiboard.markSwapPlayed(null);
 	    m_guiboard.markLastPlayed(null);   
             return;
         }
         
         Move move = m_current.getMove();
 
         if (move.getPoint() == HexPoint.RESIGN) {
             m_guiboard.markSwapPlayed(null);
 	    m_guiboard.markLastPlayed(null);   
             return;
         }
 
         if (move.getPoint() == HexPoint.SWAP_PIECES) {
             Node parent = m_current.getParent();
             assert(parent != null);
 
             m_guiboard.markLastPlayed(null);
             m_guiboard.markSwapPlayed(parent.getMove().getPoint());
 
         } else {
             m_guiboard.markLastPlayed(move.getPoint());
             m_guiboard.markSwapPlayed(null);
         }
     }	
 
     private void setGameChanged(boolean changed) 
     {
 	m_gameChanged = changed;
     }
 
     private boolean gameChanged()
     {
 	return m_gameChanged;
     }
 
     private void setFrameTitle()
     {
 	String filename = "untitled";
 	if (m_file != null) filename = m_file.getName();
 	if (gameChanged()) filename = filename + "*";
 	String name = "HexGui " + Version.id;
 	if (m_white != null) 
 	    name += " - [" + m_white_name + " " + m_white_version + "]";
 	setTitle(name + " - " + filename);
     }
 
     /** Returns false if action was aborted. */
     private boolean askSaveGame()
     {
 	Object options[] = {"Save", "Discard", "Cancel"};
 	int n = JOptionPane.showOptionDialog(this,
 					     "Game has changed.  Save changes?",
 					     "Save Game?",
 					     JOptionPane.YES_NO_CANCEL_OPTION,
 					     JOptionPane.QUESTION_MESSAGE,
 					     null,
 					     options,
 					     options[0]);
 	if (n == 0) {
 	    if (cmdSaveGame()) 
 		return true;
 	    return false;
 	} else if (n == 1) {
 	    return true;
 	}
 	return false;
     }
 
     /** Saves the current game state as a position in the specified
      * sgf file. */
     private boolean savePosition(File file)
     {
         Node root = new Node();
         Node child = new Node();
         root.addChild(child);
 
         GameInfo info = new GameInfo();
         info.setBoardSize(m_guiboard.getBoardSize());
         m_guiboard.storePosition(child);
         return save_tree(file, root, info);
     }
 
     /** Save game to file.
 	@return true If successful.
     */
     private boolean save(File file)
     {
         return save_tree(file, m_root, m_gameinfo);
     }
 
     
     private boolean save_tree(File file, Node root, GameInfo gameinfo)
     {
 	FileOutputStream out;
 	try {
 	    out = new FileOutputStream(file);
 	}
 	catch (FileNotFoundException e) {
 	    showError("File not found!");
 	    return false;
 	}
 	
 	new SgfWriter(out, root, gameinfo);
 	return true;
     }
 
 
     /* Load game from file. */
     private SgfReader load(File file)
     {
 	FileInputStream in;
 	try {
 	    in = new FileInputStream(file);
 	}
 	catch(FileNotFoundException e) {
 	    showError("File not found!");
 	    return null;
 	}
 
 	SgfReader sgf;
 	try {
 	    sgf = new SgfReader(in);
 	}
 	catch (SgfReader.SgfError e) {
 	    showError("Error reading SGF file:\n \"" + e.getMessage() + "\"");
 	    return null;
 	}
 	
 	return sgf;
     }
 
     //------------------------------------------------------------
 
     /** Show a simple error message dialog. */
     private void showError(String msg)
     {
 	JOptionPane.showMessageDialog(this, msg, "Error",
 				      JOptionPane.ERROR_MESSAGE);
     }
     
     /** Show save dialog, return File of selected filename.  
 	@return null If aborted.
     */
     private File showSaveAsDialog()
     {
 	JFileChooser fc = new JFileChooser(m_preferences.get("path-save-game"));
 	if (m_file != null) fc.setSelectedFile(m_file);
 	int ret = fc.showSaveDialog(this);
 	if (ret == JFileChooser.APPROVE_OPTION)
 	    return fc.getSelectedFile();
 	return null;
     }
 
     /** Show open dialog, return File of selected filename.  
 	@return null If aborted.
     */
     private File showOpenDialog()
     {
 	JFileChooser fc = new JFileChooser(m_preferences.get("path-load-game"));
 	int ret = fc.showOpenDialog(this);
 	if (ret == JFileChooser.APPROVE_OPTION)
 	    return fc.getSelectedFile();
 	return null;
     }
 
     private AboutDialog m_about;
     private GuiPreferences m_preferences;
     private GuiBoard m_guiboard;
     private GuiToolBar m_toolbar;
     private StatusBar m_statusbar;
     private GuiMenuBar m_menubar;
     private HtpShell m_shell;
     private AnalyzeDialog m_analyze;
     private EvalGraphDialog m_evalgraph;
 
     private Node m_root;
     private Node m_current;
     private GameInfo m_gameinfo;
     private HexColor m_tomove;
     private boolean m_gameChanged;
 
     private Vector<HexPoint> m_selected_cells;
 
     private HtpController m_white;
     private String m_white_name;
     private String m_white_version;
     private Process m_white_process;
     private Socket m_white_socket;
 
     private File m_file;
 }
 
 //----------------------------------------------------------------------------
