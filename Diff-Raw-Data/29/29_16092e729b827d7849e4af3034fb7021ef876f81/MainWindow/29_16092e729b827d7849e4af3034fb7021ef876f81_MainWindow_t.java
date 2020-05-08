 package cz.vse.metric.gui;
 
 import com.jgoodies.forms.builder.PanelBuilder;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import cz.vse.metric.dci.DCIMetric;
 
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: djaara
  * Date: 2011-09-10
  * Time: 19:04
  */
 public class MainWindow extends JFrame implements MouseListener {
 
 	private JButton addButton;
 	private InputFieldWithFileChooser wsdlUriField;
 	private LoadedDefinitionsPanel loadedDefinitions;
 	private JButton resetButton;
 	private JButton calculateButton;
 	private JTabbedPane tabbedPane;
 	private BasicResults basicResultsTab;
 	private OperationInterfaces operationInterfacesTab;
 	private ComplexTypes complexTypesTab;
 	private OperationCombinations operationCombinationsTab;
 	private List<ResultsBrowser> resultBrowser;
 
	public MainWindow(final String appName) {
		super(appName);
 		createGUI();
 	}
 
 	private void createGUI() {
 		init();
 
 		FormLayout layout = new FormLayout("p, 3dlu, p:g, 3dlu, 20dlu, 60dlu",
 				"p, 3dlu, f:70dlu, 3dlu, p, 3dlu, f:200dlu:g");
 		PanelBuilder pb = new PanelBuilder(layout);
 		pb.setDefaultDialogBorder();
 		CellConstraints cc = new CellConstraints();
 		pb.addLabel("WSDL URL/File:", cc.xy(1, 1));
 		pb.add(wsdlUriField, cc.xyw(3, 1, 3));
 		pb.add(addButton, cc.xy(6, 1));
 		pb.add(loadedDefinitions, cc.xyw(1, 3, 6));
 		pb.add(resetButton, cc.xy(1, 5));
 		pb.add(calculateButton, cc.xyw(5, 5, 2));
 		pb.add(tabbedPane, cc.xyw(1, 7, 6));
 		add(pb.getPanel());
 		setMinimumSize(getPreferredSize());
 		pack();
 	}
 
 	private void init() {
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		resultBrowser = new ArrayList<ResultsBrowser>();
 
 		loadedDefinitions = new LoadedDefinitionsPanel();
 		addButton = new JButton("Add");
 		addButton.addMouseListener(this);
 
 		resetButton = new JButton("Reset");
 		resetButton.addMouseListener(this);
 
 		calculateButton = new JButton("Calculate metric!");
 		calculateButton.addMouseListener(this);
 
 		tabbedPane = new JTabbedPane();
 
 		basicResultsTab = new BasicResults();
 		resultBrowser.add(basicResultsTab);
 		tabbedPane.addTab("Basic Results", basicResultsTab);
 
 		operationInterfacesTab = new OperationInterfaces();
 		resultBrowser.add(operationInterfacesTab);
 		tabbedPane.addTab("Operation Interfaces", operationInterfacesTab);
 
 		complexTypesTab = new ComplexTypes();
 		resultBrowser.add(complexTypesTab);
 		tabbedPane.addTab("Complex Types", complexTypesTab);
 
 		operationCombinationsTab = new OperationCombinations();
 		resultBrowser.add(operationCombinationsTab);
 		tabbedPane.addTab("Operation Combinations", operationCombinationsTab);
 
 		wsdlUriField = new InputFieldWithFileChooser(this);
 	}
 
 
 	public void mouseClicked(MouseEvent mouseEvent) {
 		Object source = mouseEvent.getSource();
 		if (source == addButton) {
 			String URI = wsdlUriField.getText();
 			URI = URI.trim();
 			if (URI.length() > 0) {
 				loadedDefinitions.add(URI);
 			}
 			wsdlUriField.setText(null);
 		} else if (source == resetButton) {
 			loadedDefinitions.reset();
 			wsdlUriField.setText(null);
 			for (ResultsBrowser result : resultBrowser) {
 				result.reset();
 			}
 		} else if (source == calculateButton) {
 			Object[] definitions = loadedDefinitions.getLoadedDefinitions();
 			if (definitions.length > 0) {
 				calculateButton.setEnabled(false);
 				ResultCalculation calculator = new ResultCalculation(this, definitions);
 				calculator.execute();
 			}
 		}
 	}
 
 	public void setResult(DCIMetric metric) {
 		calculateButton.setEnabled(true);
		if (metric.getDataCouplingIndex() >= 0.0) {
			for (ResultsBrowser result : resultBrowser) {
 				result.setResult(metric);
 			}
		}
 	}
 
 	public void mousePressed(MouseEvent mouseEvent) {}
 
 	public void mouseReleased(MouseEvent mouseEvent) {}
 
 	public void mouseEntered(MouseEvent mouseEvent) {}
 
 	public void mouseExited(MouseEvent mouseEvent) {}
 }
