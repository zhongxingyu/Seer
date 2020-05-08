 package org.janusproject.demos.meetingscheduler.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.AbstractCellEditor;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import org.janusproject.demos.meetingscheduler.ontology.Meeting;
 import org.janusproject.demos.meetingscheduler.ontology.MeetingResponse;
 import org.janusproject.demos.meetingscheduler.util.DateRangeUtil;
 import org.janusproject.demos.meetingscheduler.util.KernelWatcher;
 
 import com.miginfocom.calendar.activity.ActivityDepository;
 import com.miginfocom.calendar.activity.ActivityList;
 import com.miginfocom.util.dates.ImmutableDateRange;
 
 /**
  * The UI shown when a meeting proposal is received.
  * 
  * @author bfeld
  * @author ngrenie
  * @version $FullVersion$
  * @mavengroupid $Groupid$
  * @mavenartifactid $ArtifactId$
  *
  */
 public class meetingProposalFrame extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = -8269547358309700827L;
 	private JTable propList;
 	private ActivityDepository depository;
 	private Vector<Vector<Object>> data;
 	private List<ImmutableDateRange> ranges;
 	private Meeting meeting;
 	private KernelWatcher kw;
 	private String who;
 
 	public meetingProposalFrame(String who, Meeting meeting, KernelWatcher kw) {
 		this.meeting = meeting;
 		this.kw = kw;
 		this.who = who;
 		setTitle(who + " new meeting proposal from " + meeting.getInitiator());
 
 		this.depository = ActivityDepository.getInstance(who);
 
 		Container contentPane = this.getContentPane();
 		contentPane.setLayout(new BoxLayout(contentPane,
 				getDefaultCloseOperation()));
 		this.setSize(500, 300);
 		this.setLocation(300, 400);
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 
 		Vector<String> columnNames = new Vector<String>();
 		columnNames.add("Time Slot");
 		columnNames.add("Rank");
 
 		data = new Vector<Vector<Object>>();
 		ranges = new ArrayList<ImmutableDateRange>();
 
 		ActivityList existingActivites = depository.getActivities();
 
 		for (ImmutableDateRange date : meeting.getDates()) {
 			if (!existingActivites
 					.hasOverlapping(date.getDateRangeForReading())) {
 				Vector<Object> row = new Vector<Object>();
 				row.add(DateRangeUtil.dateToHumanFriendly(date));
				row.add(1);
 				data.add(row);
 				ranges.add(date);
 			}
 		}
 		DefaultTableModel model = new DefaultTableModel(data, columnNames);
 		propList = new JTable(model);
 
 		TableColumn col = propList.getColumnModel().getColumn(1);
		col.setCellRenderer(new SpinnerEditor());
		col.setCellEditor(new SpinnerEditor());
 		col.setMinWidth(50);
 		col.setMaxWidth(50);
 
 		JButton submitButton = new JButton("Submit");
 		submitButton.setActionCommand("SUBMIT");
 		submitButton.addActionListener(this);
 
 		JLabel descLabel = new JLabel(
 				"Rate your prefered proposition (1 is the best)");
 
 		JLabel meetingDescription = new JLabel(meeting.getDescription());
 
 		JScrollPane scrollPane = new JScrollPane(propList);
 		JPanel northPanel = new JPanel();
 		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
 		northPanel.add(descLabel);
 		northPanel.add(meetingDescription);
 
 		panel.add(northPanel, BorderLayout.NORTH);
 		panel.add(submitButton, BorderLayout.SOUTH);
 		panel.add(scrollPane, BorderLayout.CENTER);
 		this.add(panel);
 		this.pack();
 		this.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		String cmd = evt.getActionCommand();
 		if (cmd == "SUBMIT") {
 			MeetingResponse meetingResponse = new MeetingResponse(meeting);
 			for (int i = 0; i < propList.getModel().getRowCount(); i++) {
				meetingResponse.addResponseDate(ranges.get(i),
						(Integer) propList.getModel().getValueAt(i, 1));
 			}
 			this.kw.getChannel(this.who).responseMeeting(meetingResponse);
 			this.dispose();
 		}
 	}
 }
 
 class SpinnerEditor extends AbstractCellEditor implements TableCellRenderer,
 		TableCellEditor {
 
 	private static final long serialVersionUID = 1L;
 	final JSpinner spinner = new JSpinner();
 
 	public SpinnerEditor() {
 		spinner.setModel(new SpinnerNumberModel(1, 0, 5, 1));
 	}
 
 	public Component getTableCellEditorComponent(JTable table, Object value,
 			boolean isSelected, int row, int column) {
 		System.out.println(value.getClass());
 		spinner.setValue(value);
 		return spinner;
 	}
 
 	public boolean isCellEditable(EventObject evt) {
 		if (evt instanceof MouseEvent) {
 			return ((MouseEvent) evt).getClickCount() >= 2;
 		}
 		return true;
 	}
 
 	public Object getCellEditorValue() {
 		return spinner.getValue();
 	}
 
 	@Override
 	public Component getTableCellRendererComponent(JTable table, Object value,
 			boolean isSelected, boolean hasFocus, int row, int column) {
 		return spinner;
 	}
 }
