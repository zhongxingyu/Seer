 package pl.edu.agh.two.mud.client.ui;
 
 import java.awt.Color;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 
 import net.miginfocom.swing.MigLayout;
 
 public class Console extends JPanel {
 
 	private static final long serialVersionUID = -9186318938049024415L;
 
 	/**
 	 * Listener for command invocation.
 	 *
 	 * @author kret
 	 */
 	public interface ICommandLineListener {
 
 		public abstract void commandInvoked(String command);
 
 	}
 
 	// BEGIN widgets
 
 	private JLabel prompt;
 
 	private JTextField commandLine;
 
 	private JTextPane output;
 
 	// END widgets
 
 	// BEGIN listeners
 
 	private Set<ICommandLineListener> listeners = new LinkedHashSet<ICommandLineListener>();
 
 	private KeyListener commandLineKeyListener = new KeyAdapter() {
 
 		public void keyPressed(KeyEvent e) {
 			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
 				String command = commandLine.getText();
 				for (ICommandLineListener listener : listeners) {
 					listener.commandInvoked(command);
 				}
 			}
 		};
 
 	};
 
 	private ICommandLineListener defaultCommandLineListener = new ICommandLineListener() {
 
 		@Override
 		public void commandInvoked(String command) {
 			appendTextToConsole(String.format("> %s", command));
 			commandLine.setText("");
 		}
 
 	};
 
 	private FocusListener panelFocusListener = new FocusAdapter() {
 
 		@Override
 		public void focusGained(FocusEvent e) {
 			commandLine.requestFocus();
 		}
 
 	};
 
 	// END listeners
 
 	/**
 	 * Create the panel.
 	 */
 	public Console() {
 		setBackground(Color.BLACK);
 		setLayout(new MigLayout("", "[grow]", "[grow][20px]"));
 
 		output = new JTextPane();
 		output.setEditable(false);
 		output.setText("");
 		output.setBackground(Color.BLACK);
 		output.setForeground(Color.WHITE);
 		output.setBorder(null);
 		output.setAutoscrolls(true);
 		output.addFocusListener(panelFocusListener);
 
 		JScrollPane slider = new JScrollPane(output);
 		add(slider, "cell 0 0,grow");
 		slider.setBorder(null);
 
 		prompt = new JLabel(">");
 		prompt.setForeground(Color.YELLOW);
 		prompt.setBackground(Color.BLACK);
 		add(prompt, "flowx,cell 0 1");
 
 		commandLine = new JTextField();
 		commandLine.setForeground(Color.YELLOW);
 		commandLine.setBackground(Color.BLACK);
 		commandLine.setColumns(10);
 		commandLine.setBorder(null);
 		commandLine.setRequestFocusEnabled(true);
 		add(commandLine, "cell 0 1,growx,aligny bottom");
 
 		commandLine.addKeyListener(commandLineKeyListener);
 		addCommandLineListener(defaultCommandLineListener);
 		addFocusListener(panelFocusListener);
 	}
 
 	public void addCommandLineListener(ICommandLineListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * This method allows to append text to console output.
 	 *
 	 * @param text
 	 *            text to append
 	 */
 	public void appendTextToConsole(String text) {
 		output.setText(output.getText() + "\n" + text);
 	}
 
 }
