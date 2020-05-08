 package lambda.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import lambda.Environment;
 import lambda.LambdaInterpreter;
 import lambda.ast.IRedex;
 import lambda.ast.Lambda;
 import lambda.ast.MacroExpander;
 import lambda.ast.RedexFinder;
 import lambda.ast.parser.ParserException;
 import lambda.conversion.Converter;
 import lambda.gui.macroview.MacroDefinitionView;
 import lambda.system.CommandDelegate;
 import lambda.system.CommandProcessor;
 import util.nullable.NullableBool;
 import util.nullable.NullableInt;
 import extgui.LineEditor;
 import extgui.flatsplitpane.FlatSplitPane;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame
 {
 	private JTabbedPane tabbedPane;
 
 	private LineEditor inputField;
 	private JButton buttonStep;
 	private JButton buttonClear;
 
 	private JCheckBox checkPrintStep;
 	private JCheckBox checkShort;
 	private JCheckBox checkEtaEnabled;
 	private JCheckBox checkDataConv;
 	private JCheckBox checkAuto;
 	private JCheckBox checkTraceInAuto;
 	private JButton buttonStop;
 
 	private RedexView redexView;
 	private JTextArea output;
 	private MacroDefinitionView macroView;
 
 	private Environment env = Environment.getEnvironment();
 	private final CommandProcessor commands = new CommandProcessor();
 	private LambdaInterpreter interpreter;
 
 	private boolean autoRunning;
 	private Thread thread;
 
 	public MainFrame()
 	{
 		setTitle("Lambda * Magica " + Environment.APPLICATION_VERSION);
 		setJMenuBar(new MainMenu(this));
 
 		JPanel leftPanel = new JPanel(new BorderLayout());
 
 		JPanel inputPanel = new JPanel(new BorderLayout());
 		inputField = new LineEditor();
 		inputField.setFont(env.getGUIFont());
 		inputField.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = inputField.getText().trim();
 				if (!s.isEmpty())
 				{
 					output.setText("");
 					inputField.saveHistory();
 					inputField.setText("");
 					start(s);
 				}
 			}
 		});
 		inputPanel.add(inputField, BorderLayout.CENTER);
 
 		buttonStep = new JButton("step");
 		buttonStep.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = inputField.getText().trim();
 				if (!s.isEmpty())
 				{
 					output.setText("");
 					inputField.saveHistory();
 					inputField.setText("");
 					start(s);
 				}
 				else
 				{
 					step();
 				}
 			}
 		});
 		inputPanel.add(buttonStep, BorderLayout.EAST);
 
 		leftPanel.add(inputPanel, BorderLayout.NORTH);
 
 		JPanel buttonPanel = new JPanel();
 
 		output = new JTextArea();
 		output.setEditable(false);
 		output.setFont(env.getGUIFont());
 		leftPanel.add(new JScrollPane(output), BorderLayout.CENTER);
 
 		JPanel pAuto = new JPanel();
 		pAuto.setLayout(new BoxLayout(pAuto, BoxLayout.Y_AXIS));
 		pAuto.setBorder(BorderFactory.createTitledBorder("Auto Mode"));
 		checkAuto = createOptionCheckBox(Environment.KEY_AUTO, "auto reduction");
 		pAuto.add(checkAuto);
 		checkTraceInAuto = createOptionCheckBox(Environment.KEY_TRACE, "show trace in auto mode");
 		pAuto.add(checkTraceInAuto);
 		buttonPanel.add(pAuto);
 
 		JPanel pReduction = new JPanel();
 		pReduction.setLayout(new BoxLayout(pReduction, BoxLayout.Y_AXIS));
 		pReduction.setBorder(BorderFactory.createTitledBorder("Reduction"));
 		checkEtaEnabled = createOptionCheckBox(Environment.KEY_ETA_REDUCTION, "enable eta-reduction");
 		pReduction.add(checkEtaEnabled);
 		buttonPanel.add(pReduction);
 
 		JPanel pPrinting = new JPanel();
 		pPrinting.setLayout(new BoxLayout(pPrinting, BoxLayout.Y_AXIS));
 		pPrinting.setBorder(BorderFactory.createTitledBorder("Printing"));
 		checkPrintStep = createOptionCheckBox(Environment.KEY_PRINT_STEP, "print step");
 		checkShort = createOptionCheckBox(Environment.KEY_SHORT, "short printing");
 		checkDataConv = createOptionCheckBox(Environment.KEY_DATA_CONV, "show nat/bool data");
 		pPrinting.add(checkPrintStep);
 		pPrinting.add(checkShort);
 		pPrinting.add(checkDataConv);
 		buttonPanel.add(pPrinting);
 
 		buttonStop = new JButton("stop");
 		buttonStop.setEnabled(false);
 		buttonStop.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				if (thread != null)
 				{
 					autoRunning = false;
 					try
 					{
 						thread.join();
 					}
 					catch (InterruptedException ex)
 					{
 						ex.printStackTrace();
 					}
 					println("- STOPPED.");
 					buttonStop.setEnabled(false);
 					thread = null;
 				}
 			}
 		});
 		buttonPanel.add(buttonStop);
 
 		buttonClear = new JButton("clear output");
 		buttonClear.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				output.setText("");
 			}
 		});
 		buttonPanel.add(buttonClear);
 
 		JButton buttonClearMacros = new JButton("clear macros");
 		buttonClearMacros.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				clearMacros();
 			}
 		});
 		buttonPanel.add(buttonClearMacros);
 
 		final int DEF_SIZE = GroupLayout.DEFAULT_SIZE;
 		final int INF_SIZE = Short.MAX_VALUE;
 		GroupLayout gl = new GroupLayout(buttonPanel);
 		gl.setAutoCreateContainerGaps(true);
 		gl.setAutoCreateGaps(true);
 		gl.setHorizontalGroup(gl.createParallelGroup()
 			.addComponent(pAuto, 0, DEF_SIZE, INF_SIZE)
 			.addComponent(pReduction, 0, DEF_SIZE, INF_SIZE)
 			.addComponent(pPrinting, 0, DEF_SIZE, INF_SIZE)
 			.addComponent(buttonStop, 0, DEF_SIZE, INF_SIZE)
 			.addComponent(buttonClear, 0, DEF_SIZE, INF_SIZE)
 			.addComponent(buttonClearMacros, 0, DEF_SIZE, INF_SIZE)
 		);
 		gl.setVerticalGroup(gl.createSequentialGroup()
 			.addComponent(pAuto)
 			.addComponent(pReduction)
 			.addComponent(pPrinting)
 			.addComponent(buttonStop)
 			.addComponent(buttonClear)
 			.addComponent(buttonClearMacros)
 		);
 		buttonPanel.setLayout(gl);
 
 		tabbedPane = new JTabbedPane();
 		tabbedPane.addTab("General", buttonPanel);
 
 		redexView = new RedexView();
 		redexView.setBackground(Color.WHITE);
 		redexView.setFont(env.getGUIFont());
 		redexView.setMargin(5, 5, 5, 5);
 		redexView.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				step();
 			}
 		});
 		tabbedPane.add("Redex", new JScrollPane(redexView));
 
 		macroView = new MacroDefinitionView();
 		macroView.setFont(env.getGUIFont());
 		tabbedPane.addTab("Macros", macroView);
 
 		final JSplitPane sp = new FlatSplitPane();
 		sp.setContinuousLayout(true);
 		sp.setLeftComponent(leftPanel);
 		sp.setRightComponent(tabbedPane);
 		addWindowListener(new WindowAdapter()
 		{
 			public void windowOpened(WindowEvent e)
 			{
 				sp.setDividerLocation(0.7);
 				sp.setResizeWeight(0.5);
 			}
 		});
 		sp.setDividerLocation(Short.MAX_VALUE);
 		add(sp);
 		setSize(700, 500);
 
 		setupAcceleration();
 
 		initializeCommands();
 	}
 
 	private void setupAcceleration()
 	{
 		int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
 		String keyInc = "font.size.increase";
 		String keyDec = "font.size.decrease";
 		String keyDef = "font.size.default";
 
 		InputMap imap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, mod), keyInc);
 		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, mod), keyInc);
 		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, mod), keyInc);
 		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mod), keyDec);
 		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, mod), keyDec);
 		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, mod), keyDef);
 
 		ActionMap amap = getRootPane().getActionMap();
 		amap.put(keyInc, new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				Environment env = Environment.getEnvironment();
 				int size = env.getInt(Environment.KEY_GUI_FONT_SIZE, 12);
 				if (size < 120)
 				{
 					env.set(Environment.KEY_GUI_FONT_SIZE, size + 1);
 					setCodeFont(env.getGUIFont());
 				}
 			}
 		});
 		amap.put(keyDec, new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				Environment env = Environment.getEnvironment();
 				int size = env.getInt(Environment.KEY_GUI_FONT_SIZE, 12);
 				if (5 < size)
 				{
 					env.set(Environment.KEY_GUI_FONT_SIZE, size - 1);
 					setCodeFont(env.getGUIFont());
 				}
 			}
 		});
 		amap.put(keyDef, new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				Environment env = Environment.getEnvironment();
 				env.set(Environment.KEY_GUI_FONT_SIZE, 12);
 				setCodeFont(env.getGUIFont());
 			}
 		});
 	}
 
 	private JCheckBox createOptionCheckBox(final String key, String text)
 	{
 		final JCheckBox checkBox = new JCheckBox(text);
 		checkBox.setSelected(env.getBoolean(key));
 		checkBox.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				env.set(key, checkBox.isSelected());
 			}
 		});
 		return checkBox;
 	}
 
 	public void setCodeFont(Font font)
 	{
 		inputField.setFont(font);
 		output.setFont(font);
 		redexView.setFont(font);
		macroView.setFont(font);
 	}
 
 	private void start(String text)
 	{
 		if (autoRunning) return;
 
 		text = text.trim();
 		if (text.startsWith(":"))
 		{
 			if (text.startsWith(":q"))
 			{
 				dispose();
 			}
 			else
 			{
 				commands.invokeCommand(text);
 			}
 		}
 		else if (!text.isEmpty())
 		{
 			try
 			{
 				evalLine(text);
 			}
 			catch (ParserException e)
 			{
 				String s = "";
 				for (int i = 0; i < e.column; i++)
 				{
 					s = s + ' ';
 				}
 				println(text);
 				println(s + '^');
 				println("- " + e.getMessage());
 			}
 		}
 	}
 
 	private boolean stepReduction()
 	{
 		boolean eta = env.getBoolean(Environment.KEY_ETA_REDUCTION);
 		IRedex redex = redexView.getSelectedRedex();
 		if (redex == null)
 		{
 			redex = RedexFinder.getLeftMostOuterMostRedex(interpreter.getLambda(), eta);
 		}
 		return interpreter.step(env, redex);
 	}
 
 	private boolean step()
 	{
 		if (interpreter == null)
 		{
 			return false;
 		}
 
 		boolean success = true;
 		boolean changed = stepReduction();
 		Lambda lambda = interpreter.getLambda();
 		if (changed)
 		{
 			if (!autoRunning || env.getBoolean(Environment.KEY_TRACE))
 			{
 				StringBuilder sb = new StringBuilder();
 				sb.append("--> ");
 				if (env.getBoolean(Environment.KEY_PRINT_STEP))
 				{
 					sb.append(String.format("%3d: ", interpreter.getStep()));
 				}
 				String s = lambda.toString();
 				if (env.getBoolean(Environment.KEY_SHORT) && s.length() > 75)
 				{
 					sb.append(s.substring(0, 35));
 					sb.append(" ... ");
 					sb.append(s.substring(s.length() - 35, s.length()));
 				}
 				else
 				{
 					sb.append(s);
 				}
 				println(sb.toString());
 			}
 			if (checkIfCycled() || checkIfNormalForm())
 			{
 				reductionTerminated();
 				success = false;
 			}
 		}
 		else
 		{
 			reductionTerminated();
 			success = false;
 		}
 		if (!autoRunning)
 		{
 			updateRedexView(lambda);
 		}
 		return success;
 	}
 
 	private void showConvertedData(Lambda lambda)
 	{
 		NullableInt natValue = Converter.toNat(lambda);
 		if (natValue.hasValue())
 		{
 			println("  = " + natValue + " (as nat)");
 		}
 		NullableBool boolValue = Converter.toBool(lambda);
 		if (boolValue.hasValue())
 		{
 			println("  = " + boolValue + " (as bool)");
 		}
 	}
 
 	private boolean checkIfNormalForm()
 	{
 		Lambda lambda = interpreter.getLambda();
 		boolean etaEnabled = env.getBoolean(Environment.KEY_ETA_REDUCTION);
 		if (RedexFinder.isNormalForm(lambda, etaEnabled))
 		{
 			printEndState(lambda, "(normal form)");
 			return true;
 		}
 		return false;
 	}
 
 	private boolean checkIfCycled()
 	{
 		if (interpreter.isCyclic())
 		{
 			printEndState(interpreter.getLambda(), "(cyclic reduction)");
 			return true;
 		}
 		return false;
 	}
 
 	private void printEndState(Lambda lambda, String label)
 	{
 		MacroExpander expander = new MacroExpander(env);
 		lambda = expander.expand(lambda, true);
 		println("==> " + lambda + "    " + label);
 		if (env.getBoolean(Environment.KEY_DATA_CONV))
 		{
 			showConvertedData(lambda);
 		}
 	}
 
 	private void reductionTerminated()
 	{
 		buttonStop.setEnabled(false);
 		interpreter = null;
 	}
 
 	private void updateRedexView(Lambda lambda)
 	{
 		redexView.setRedexes(lambda);
 		redexView.revalidate();
 		redexView.repaint();
 	}
 
 	private void defineMacro(String name, String expr)
 	{
 		try
 		{
 			Lambda lambda = Lambda.parse(expr);
 			env.defineMacro(name, lambda);
 			macroView.addMacro(name, lambda);
 			String s = String.format("- <%s> is defined as %s", name, lambda);
 			println(s);
 		}
 		catch (ParserException e)
 		{
 			println("- " + e.getMessage());
 		}
 	}
 
 	private void readMacro(String line)
 	{
 		if (line.indexOf('=') != -1)
 		{
 			String[] v = line.split("\\s*=\\s*");
 			if (v.length == 2)
 			{
 				defineMacro(v[0], v[1]);
 			}
 			else
 			{
 				println("- Invalid expression: " + line);
 			}
 		}
 	}
 
 	private void evalLine(String line) throws ParserException
 	{
 		if (line.indexOf('=') != -1)
 		{
 			readMacro(line);
 		}
 		else
 		{
 			Lambda lambda = Lambda.parse(line);
 
 			println(lambda.toString());
 
 			interpreter = new LambdaInterpreter(lambda);
 
 			if (checkIfNormalForm())
 			{
 				reductionTerminated();
 			}
 			else
 			{
 				if (!env.getBoolean(Environment.KEY_AUTO))
 				{
 					tabbedPane.setSelectedIndex(1);
 					buttonStep.requestFocus();
 					updateRedexView(lambda);
 				}
 				else
 				{
 					startAuto();
 				}
 			}
 		}
 	}
 
 	private void startAuto()
 	{
 		autoRunning = true;
 		buttonStop.setEnabled(true);
 		thread = new AutoRunningThread();
 		thread.start();
 	}
 
 	private void loadFile(String path)
 	{
 		if (!path.endsWith(".lm.txt"))
 		{
 			path = path + ".lm.txt";
 		}
 
 		try
 		{
 			BufferedReader reader = new BufferedReader(
 				new InputStreamReader(new FileInputStream(path)));
 
 			println("- load '" + path + "'");
 			String line;
 			while ((line = reader.readLine()) != null)
 			{
 				int c = line.indexOf('#');
 				if (c != -1) line = line.substring(0, c);
 				line = line.trim();
 				if (line.isEmpty()) continue;
 				readMacro(line);
 			}
 			reader.close();
 		}
 		catch (FileNotFoundException e)
 		{
 			println("- cannot open file \"" + path + "\"");
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void clearMacros()
 	{
 		env.clearMacros();
 		macroView.clearList();
 		println("- macros were cleared.");
 	}
 
 	private void initializeCommands()
 	{
 		commands.add(":l", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				if (params.length == 0)
 				{
 					File cd = new File(".");
 					for (File file : cd.listFiles())
 					{
 						if (file.isFile() && file.getName().endsWith(".lm.txt"))
 						{
 							loadFile(file.getName());
 						}
 					}
 				}
 				else
 				{
 					for (String fileName : params)
 					{
 						loadFile(fileName);
 					}
 				}
 			}
 		});
 		commands.add(":f", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				if (params.length >= 1)
 				{
 					String expr = "";
 					for (String s : params)
 					{
 						expr += s + " ";
 					}
 					try
 					{
 						Lambda lambda = Lambda.parse(expr);
 						MacroExpander expander = new MacroExpander(env);
 						println(expander.expand(lambda, true).toString());
 					}
 					catch (ParserException e)
 					{
 						println(e.getMessage());
 					}
 				}
 			}
 		});
 		commands.add(":c", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				clearMacros();
 			}
 		});
 		commands.add(":pwd", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				println(new File("").getAbsolutePath());
 			}
 		});
 		commands.add(":?", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				println("- :?         - show this help.");
 				println("- :f <expr>  - expand macros and show expression.");
 				println("- :l <path>  - load lines from a text file.");
 				println("- :s <n>     - set the number of continuation steps.");
 				println("- :t [on]    - set trace mode. ");
 				println("- :c         - clear all macros.");
 				println("- :pwd       - print working directory.");
 				println("- :q         - quit interpreter.");
 			}
 		});
 	}
 
 	private synchronized void println(String line)
 	{
 		output.append(line);
 		output.append(System.getProperty("line.separator"));
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				output.setCaretPosition(output.getText().length());
 			}
 		});
 	}
 
 	private class AutoRunningThread extends Thread
 	{
 		public AutoRunningThread()
 		{
 			setName("AutoRunningThread");
 			setDaemon(true);
 		}
 
 		public void run()
 		{
 			try
 			{
 				while (autoRunning)
 				{
 					if (!step())
 					{
 						break;
 					}
 				}
 			}
 			catch (StackOverflowError e)
 			{
 				println("Fatal Error: generated too large structure");
 			}
 			finally
 			{
 				buttonStop.setEnabled(false);
 				autoRunning = false;
 				thread = null;
 			}
 		}
 	}
 }
