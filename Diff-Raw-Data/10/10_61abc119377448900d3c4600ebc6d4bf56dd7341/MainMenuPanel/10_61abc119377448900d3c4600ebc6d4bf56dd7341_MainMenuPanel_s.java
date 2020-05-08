 package calculator;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import javax.swing.BoxLayout;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class MainMenuPanel extends GUIPanel {
 
 	private static final long serialVersionUID = 997821906993439522L;
 	protected JPanel mainMenuPanel;
 
 	public MainMenuPanel(SystemController controller) {
 		super(controller);
 
 		// Layout.
 		mainMenuPanel = new JPanel();
 		mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel,
 				BoxLayout.PAGE_AXIS));
 
 		// Select school button.
 		mainMenuPanel.add(createButton("selectSchool", "Select School..."));
 
 		// Load active users information (if any) and display on screen.
 		for (String schoolName : controller.activeUser.getTranscripts()
 				.keySet()) {
 			mainMenuPanel.add(createButton(schoolName));
 		}
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		add(mainMenuPanel);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String action = e.getActionCommand();
 		if (action.equals("selectSchool")) {
 			controller.addPanel(new SchoolPanel(controller), "schoolPanel");
 			controller.showPanel("schoolPanel", this);
 		} else {
 			controller.addPanel(new SemesterPanel(controller, action),
 					"semesterPanel");
 			controller.showPanel("semesterPanel", this);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Component component = e.getComponent();
 		// If right click and a school button.
 		if (SwingUtilities.isRightMouseButton(e)
				&& controller.schools.containsKey(component.getName())) {
 			int response = JOptionPane.showConfirmDialog(null,
 					"Are you sure you wish to remove this school?", "Confirm",
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 			if (response == JOptionPane.YES_OPTION) {
 				controller.activeUser.getTranscripts().remove(
 						component.getName());
 				controller.saveUserList();
 				mainMenuPanel.remove(component);
 				mainMenuPanel.revalidate();
 				mainMenuPanel.repaint();
 			} else if (response == JOptionPane.CLOSED_OPTION) {
 				return;
 			}
 		} else {
 			return;
 		}
 	}
 }
