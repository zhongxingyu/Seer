 package nl.minicom.evenexus.gui.utils.dialogs;
 
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.inject.Inject;
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import nl.minicom.evenexus.core.Application;
 import nl.minicom.evenexus.gui.utils.dialogs.titles.AboutTitle;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class represents a dialog which has some about info about the application.
  *
  * @author michael
  */
 public class AboutDialog extends CustomDialog {
 
 	private static final long serialVersionUID = 8860569373371189189L;
 	private static final Logger LOG = LoggerFactory.getLogger(AboutDialog.class);
 		
 	private final Application application;
 	private final BugReportDialog dialog;
 	
 	/**
 	 * This constructs a new {@link AboutDialog} object.
 	 * 
 	 * @param application
 	 * 		The {@link Application}.
 	 * 
 	 * @param dialog
 	 * 		The {@link BugReportDialog}.
 	 */
 	@Inject
 	public AboutDialog(Application application, BugReportDialog dialog) {
 		super(new AboutTitle(), 370, 384);
 		this.application = application;
 		this.dialog = dialog;
 	}
 	
 	/**
 	 * This method initializes this {@link AboutDialog} object.
 	 */
 	public void initialize() {
 		buildGui();
 		setVisible(true);
 		setResizable(true);
 	}
 
 	@Override
 	public void createGui(JPanel guiPanel) {	
 		JPanel devPanel = createDevPanel();
 		JPanel versionPanel = createVersionPanel();
 		JPanel contributionPanel = createContributionPanel();
 		
 		GroupLayout layout = new GroupLayout(guiPanel);
 		guiPanel.setLayout(layout);        
 		layout.setHorizontalGroup(
       	layout.createSequentialGroup()
         		.addGroup(layout.createParallelGroup(Alignment.LEADING)
         				.addComponent(versionPanel)
         				.addComponent(devPanel)
         				.addComponent(contributionPanel)
         		)
     	);
     	layout.setVerticalGroup(
     		layout.createSequentialGroup()
 	    		.addComponent(versionPanel)
 	    		.addGap(7)
 	    		.addComponent(devPanel)
 	    		.addGap(7)
 	    		.addComponent(contributionPanel)
     	);
 	}
 	
 	private JPanel createVersionPanel() {
 		JPanel versionPanel = new JPanel();
 		versionPanel.setBorder(createTitledBorder("Version"));
 		versionPanel.setMinimumSize(new Dimension(0, 80));
 		versionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
 		
 		JLabel versions = new JLabel(createVersionString());
 		versions.setAlignmentX(JComponent.LEFT_ALIGNMENT);
 		
 		GroupLayout layout = new GroupLayout(versionPanel);
 		versionPanel.setLayout(layout);
 		
 		layout.setHorizontalGroup(
 			layout.createSequentialGroup()
         		.addComponent(versions)
     	);
     	layout.setVerticalGroup(
     		layout.createSequentialGroup()
 	    		.addComponent(versions)
     	);
 		
 		return versionPanel;
 	}
 
 	private String createVersionString() {
 		String applicationVersion = getClass().getPackage().getSpecificationVersion();
 		String databaseVersion = application.getDatabaseVersion();
 		String contentVersion = application.getContentVersion();
		String buildNumber = getClass().getPackage().getImplementationVersion();
		
		if (buildNumber == null || buildNumber.trim().isEmpty()) {
			buildNumber = "unknown";
		}
 
 		Font font = UIManager.getFont("Label.font");
         font = font.deriveFont(12.0f);
 		
         StringBuilder builder = new StringBuilder();
 		builder.append("<html>");
 		builder.append("<head>");
 		builder.append("<style>");
 		builder.append("BODY { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }");
 		builder.append("</style>");
 		builder.append("</head>");
 		builder.append("<body>");
		builder.append("Application version: " + applicationVersion);
		builder.append(" (build: " + buildNumber + ")<br>");
 		builder.append("Database version: " + databaseVersion + "<br>");
 		builder.append("Content version: " + contentVersion);
 		builder.append("</body>");
 		builder.append("</html>");
 		
 		return builder.toString();
 	}
 	
 	private JPanel createContributionPanel() {
 		JPanel contributionPanel = new JPanel();
 		contributionPanel.setBorder(createTitledBorder("Special thanks to"));
 		contributionPanel.setMinimumSize(new Dimension(0, 112));
 		contributionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
 				
 		JEditorPane contribution = new JEditorPane("text/html", createContributionString());
 		contribution.setEditable(false);
 		contribution.setBackground(contributionPanel.getBackground());
 		contribution.setAlignmentX(JComponent.LEFT_ALIGNMENT);
 		contribution.addHyperlinkListener(new HyperlinkListener() {			
 			@Override
 			public void hyperlinkUpdate(HyperlinkEvent event) {
 				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					try {
 						Desktop.getDesktop().browse(event.getURL().toURI());
 					}
 					catch (Exception e) {
 						LOG.error(e.getLocalizedMessage(), e);
 						dialog.setVisible(true);
 					} 
 				}
 			}
 		});
 		
 		GroupLayout layout = new GroupLayout(contributionPanel);
 		contributionPanel.setLayout(layout);        
 		layout.setHorizontalGroup(
       	layout.createSequentialGroup()
         		.addComponent(contribution)
     	);
     	layout.setVerticalGroup(
     		layout.createSequentialGroup()
 	    		.addComponent(contribution)
     	);
 		
 		return contributionPanel;
 	}
 
 	private CompoundBorder createTitledBorder(String title) {
 		TitledBorder titleBorder = BorderFactory.createTitledBorder(title);
 		return new CompoundBorder(titleBorder, new EmptyBorder(0, 10, 0, 10));
 	}
 
 	private String createContributionString() {
 		Font font = UIManager.getFont("Label.font");
         font = font.deriveFont(12.0f);
 		
 		StringBuilder builder = new StringBuilder();
 		builder.append("<html>");
 		builder.append("<head>");
 		builder.append("<style>");
 		builder.append("BODY { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }");
 		builder.append("</style>");
 		builder.append("</head>");
 		builder.append("<body>");
 		builder.append("<a href='http://www.eve-online.com/'>CCP Games</a>, ");
 		builder.append("for the game itself and an awesome API<br>");
 		builder.append("<a href='http://wiki.eve-id.net/'>EVEDev</a>, ");
 		builder.append("for their extensive documentation of the API<br>");
 		builder.append("<a href='http://cemagraphics.deviantart.com/'>=cemagraphics</a>, for designing the logo<br>");
 		builder.append("<a href='http://www.wefunction.com/'>WeFunction.com</a>, for all the other icons<br>");
 		builder.append("<a href='https://github.com/michaeldejong/EveNexus'>GitHub.com</a>, ");
 		builder.append("for being a tremendous Git host!<br>");
 		builder.append("</body>");
 		builder.append("</html>");
 		
 		return builder.toString();
 	}
 
 	private JPanel createDevPanel() {
 		Font font = getFont().deriveFont(12.0f);
 		
 		JPanel devPanel = new JPanel();
 		devPanel.setBorder(createTitledBorder("Developers"));
 		devPanel.setMinimumSize(new Dimension(0, 64));
 		devPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
 		
 		JPanel developers = new JPanel();
 		developers.setAlignmentX(JComponent.LEFT_ALIGNMENT);
 
 		JLabel developer1 = new JLabel("Michael de Jong");
 		JLabel developer2 = new JLabel("Lars Heller");
 		
 		developer1.setFont(font);
 		developer2.setFont(font);
 		
 		GroupLayout innerLayout = new GroupLayout(developers);
 		developers.setLayout(innerLayout);        
 		innerLayout.setHorizontalGroup(
       	innerLayout.createParallelGroup()
         		.addComponent(developer1)
         		.addComponent(developer2)
     	);
     	innerLayout.setVerticalGroup(
     		innerLayout.createSequentialGroup()
 	    		.addComponent(developer1)
 	    		.addComponent(developer2)
     	);
 		
 		GroupLayout layout = new GroupLayout(devPanel);
 		devPanel.setLayout(layout);        
 		layout.setHorizontalGroup(
       	layout.createSequentialGroup()
         		.addComponent(developers)
     	);
     	layout.setVerticalGroup(
     		layout.createSequentialGroup()
 	    		.addComponent(developers)
     	);
 		
 		return devPanel;
 	}
 	
 
 }
