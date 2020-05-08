 package filmeUtils.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 import org.apache.commons.cli.Options;
 
 import filmeUtils.OutputListener;
 import filmeUtils.SearchListener;
 import filmeUtils.http.SimpleHttpClient;
 import filmeUtils.http.SimpleHttpClientImpl;
 import filmeUtils.subtitleSites.LegendasTv;
 
 public class SearchScreen extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JTextArea resultsArea;
 	private final ExecutorService executor;
 
 	public static void main(final String[] args) {
 		final SearchScreen screen = new SearchScreen();
 		screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
 	public SearchScreen(){
 		executor = Executors.newSingleThreadExecutor();
 		addComponents();
 	}
 
 	private void addComponents() {
 		final JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(createSearchField(), BorderLayout.NORTH);
 		mainPanel.add(getResultsArea(), BorderLayout.CENTER);
 		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		mainPanel.setPreferredSize(new Dimension(640, 480));
 		add(mainPanel);
 		pack();
 		setVisible(true);
 	}
 
 	private JTextArea getResultsArea() {
 		resultsArea = new JTextArea();
 		return resultsArea;
 	}
 
 	private JTextField createSearchField() {
 		final JTextField result = new JTextField(40);
 		result.setFont(result.getFont().deriveFont(30f));
 		result.addActionListener(new ActionListener() {  public void actionPerformed(final ActionEvent e) {
 			executor.execute(new Runnable() {  public void run() {
 				performQueryWith(result.getText());				
 			}});
 		}});
 		return result;
 	}
 
 	protected void performQueryWith(final String text) {
 		final SimpleHttpClient simpleHttpClient = new SimpleHttpClientImpl();
 		final LegendasTv legendasTv = new LegendasTv(null,simpleHttpClient, new OutputListener() {
 			public void out(final String string) {
 				resultsArea.append(string);
 			}
 
 			public void outVerbose(final String string) {
 				resultsArea.append(string);
 			}
 
 			public void printHelp(final String applicationName, final Options options) {
 				throw new RuntimeException("Method not implemented");
 			}
 		});
 		resultsArea.setText("Pesquisando " + text + " , aguarde...");
 		legendasTv.search(text, new SearchListener() {
 			public boolean foundReturnSuccess(final String name, final String link) {
				if(!resultsArea.getText().isEmpty()){
 					resultsArea.append("\n");
 				}
 				resultsArea.append(name);
 				return true;
 			}
 		});
 	}
 	
 }
