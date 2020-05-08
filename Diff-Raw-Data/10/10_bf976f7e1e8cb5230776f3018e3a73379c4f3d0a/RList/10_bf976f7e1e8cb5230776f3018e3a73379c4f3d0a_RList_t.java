 package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.rlist;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;
 
 public class RList extends JPanel {
 
 	final public JScrollPane scrollPane;
 
 	final ButtonGroup group;
 	final Map<JRadioButton, Revision> buttonRevisionMap;
 
 	public RList() {
 		super();
 		this.group = new ButtonGroup();
 		this.buttonRevisionMap = new HashMap<JRadioButton, Revision>();
 		try {
 			final SortedSet<Revision> revisions = new TreeSet<Revision>(
 					new Comparator<Revision>() {
 						@Override
 						public int compare(final Revision r1, final Revision r2) {
 							if (r1.number < r2.number) {
 								return 1;
 							} else if (r1.number > r2.number) {
 								return -1;
 							} else {
 								return 0;
 							}
 						}
 					});
 
//			final int threshold = 100;
 			revisions.addAll(ReadOnlyDAO.getInstance().getRevisions());
//			this.setLayout(new GridLayout(
//					revisions.size() < threshold ? revisions.size() : threshold,
//					1));
			this.setLayout(new GridLayout(revisions.size(), 1));
 			int number = 0;
 			for (final Revision revision : revisions) {
 				final StringBuilder text = new StringBuilder();
 				text.append(revision.number);
 				text.append(" (");
 				text.append(revision.date);
 				text.append(")");
 				final JRadioButton button = new JRadioButton(text.toString(),
 						true);
 				this.group.add(button);
 				this.add(button);
 				this.buttonRevisionMap.put(button, revision);
 				if (100 < ++number) {
 					break;
 				}
 			}
 		} catch (final Exception e) {
 			this.add(new JLabel("Error happened in getting revisions."));
 		}
 
 		this.scrollPane = new JScrollPane();
 		this.scrollPane.setViewportView(this);
 		this.scrollPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		this.scrollPane
 				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
 				"Revisions"));
 	}
 
 	public final long getSelectedRevision() {
 
 		for (final Entry<JRadioButton, Revision> entry : this.buttonRevisionMap
 				.entrySet()) {
 			if (entry.getKey().isSelected()) {
 				return entry.getValue().number;
 			}
 		}
 		return 0;
 
 		// Object[] selectedObjects = this.group.getSelection()
 		// .getSelectedObjects();
 		// if (null != selectedObjects) {
 		// return Long.parseLong((String) selectedObjects[0]);
 		// } else {
 		// return 0;
 		// }
 
 	}
 }
