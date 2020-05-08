 package keno.gui.panel;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 
 import keno.KenoApp;
 import keno.gui.MainWindow;
 import keno.model.Draw;
 import keno.service.LotteryService;
 import keno.util.download.FileDownloader;
 
 public class InformationPanel extends JPanel {
 	
 	private static final long serialVersionUID = 1L;
 
 	private static final String DATE_FORMAT_KEY = "app.dateformat";
 	private static final String REFRESH_KEY = "info.refresh";
 	private static final String STATUS_DOWNLOADING_KEY = "info.status.downloading";
 	private static final String STATUS_MISSING_KEY = "info.status.missing";
 	private static final String STATUS_LAST_DRAW_KEY = "info.status.lastdraw";
 	
 	private MainWindow mainWindow;
 	
 	private Box numbersBox;
 	private JLabel statusLabel;
 	private JButton refreshButton;
 	
 	public InformationPanel(MainWindow mainWindow) {
 		this.mainWindow = mainWindow;
 		init();
 	}
 
 	private void init() {
 		setLayout(new BorderLayout());
 		
 		final KenoApp app = KenoApp.getInstance();
 		final ResourceBundle bundle = app.getResourceBundle();
 		final LotteryService service = app.getLotteryService();
 		
 		numbersBox = Box.createVerticalBox();
 		Draw mostRecent = service.getMostRecentDraw();
 		fillNumbersBox(mostRecent);
 		statusLabel = new JLabel(getStatus(mostRecent, bundle));
 		statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		refreshButton = new JButton(bundle.getString(REFRESH_KEY));
 		refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		refreshButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				numbersBox.removeAll();
 				statusLabel.setText(bundle.getString(STATUS_DOWNLOADING_KEY));
 				refreshButton.setEnabled(false);
 				mainWindow.setEnabled(false);
 				
 				new SwingWorker<Boolean, Void>() {
 					@Override
 					protected Boolean doInBackground() throws Exception {
 						return new FileDownloader(app.getSourceUrl(),
 								app.getLotteryFile()).download();
 					}
 					@Override
 					protected void done() {
 						SwingUtilities.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								Draw mostRecent = service.getMostRecentDraw();
 								fillNumbersBox(mostRecent);
 								statusLabel.setText(getStatus(mostRecent, bundle));
 								refreshButton.setEnabled(true);
 								mainWindow.setEnabled(true);
 							}
 						});
 					}
 				}.execute();
 			}
 		});
 		
 		Box box = Box.createVerticalBox();
 		box.add(Box.createVerticalGlue());
 		box.add(numbersBox);
 		box.add(statusLabel);
 		box.add(refreshButton);
 		box.add(Box.createVerticalGlue());
 		
 		add(box, BorderLayout.CENTER);
 	}
 	
 	private void fillNumbersBox(Draw draw) {
 		numbersBox.removeAll();
 		for (String line : getNumbers(draw)) {
 			JLabel label = new JLabel(line);
 			label.setAlignmentX(Component.CENTER_ALIGNMENT);
 			numbersBox.add(label);
 		}
 	}
 
 	public static String getStatus(Draw mostRecent, ResourceBundle bundle) {
 		return mostRecent == null ? bundle.getString(STATUS_MISSING_KEY) : 
 			bundle.getString(STATUS_LAST_DRAW_KEY)
 			+ ": " + new SimpleDateFormat(bundle.getString(DATE_FORMAT_KEY))
 		.format(mostRecent.getDate());
 	}
 	
 	private static List<String> getNumbers(Draw draw) {
 		if (draw == null || draw.getNumbers().length == 0) {
			return Collections.emptyList();
 		}
 		
 		List<String> result = new ArrayList<String>();
 		
 		StringBuilder builder = new StringBuilder();
 		builder.append(draw.getNumbers()[0]);
 		for (int i = 1; i < draw.getNumbers().length; ++i) {
 			if (i % 5 == 0) {
 				result.add(builder.toString());
 				builder = new StringBuilder();
 				builder.append(draw.getNumbers()[i]);
 			} else {
 				builder.append(" ").append(draw.getNumbers()[i]);
 			}
 		}
 		if (builder.length() > 0) {
 			result.add(builder.toString());
 		}
 		
 		return result;
 	}
 	
 }
