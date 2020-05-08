 package edu.cs319.client.customcomponents;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.PlainDocument;
 
 import edu.cs319.client.WindowClient;
 import edu.cs319.dataobjects.DocumentInfo;
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.dataobjects.SectionizedDocument;
 import edu.cs319.dataobjects.impl.DocumentSubSectionImpl;
 import edu.cs319.dataobjects.impl.SectionizedDocumentImpl;
 import edu.cs319.server.CoLabPrivilegeLevel;
 import edu.cs319.util.Util;
 
 /**
  * 
  * @author Amelia Gee
  * @author Wayne Wrowweeclifffe
  * @author Justin Nelson
  * 
  */
 @SuppressWarnings("serial")
 public class JDocTabPanel extends JPanel {
 
 	// Number of milliseconds between automatic updates
 	private final static int UPDATE_NUM_MS = 500;
 
 	private JPanel sectionPanel;
 	private JSplitPane wholePane;
 	private JSplitPane workspace;
 	private DocumentDisplayPane topFullDocumentPane;
 	private JEditorPane currentWorkintPane;
 	private JButton sectionUpButton;
 	private JButton sectionDownButton;
 	private JButton aquireLock;
 	private JButton updateSection;
 	private JButton addSubSection;
 	private JButton unlockSubSection;
 
 	private SubSectionList listOfSubSections;
 	
 	final private DocumentInfo info;
 
 	private WindowClient client;
 
 	public JDocTabPanel(DocumentInfo info, WindowClient client) {
 		this.info = info;
 		this.client = client;
 		setName(info.getDocumentName());
 		listOfSubSections = new SubSectionList(new SectionizedDocumentImpl(info.getDocumentName()));
 		Font docFont = new Font("Courier New", Font.PLAIN, 11);
 		topFullDocumentPane = new DocumentDisplayPane();
 		topFullDocumentPane.setEditable(false);
 		topFullDocumentPane.setFont(docFont);
 		topFullDocumentPane.setLineWrap(false);
 		topFullDocumentPane.setTabSize(4);
 
 		currentWorkintPane = new WorkingPane();
 		currentWorkintPane.setFont(docFont);
 		currentWorkintPane.addMouseListener(new RightClickListener());
 		PlainDocument doc2 = (PlainDocument) currentWorkintPane.getDocument();
 		doc2.putProperty(PlainDocument.tabSizeAttribute, 4);
 		setUpAppearance();
 		setUpListeners();
 
 		Timer timer = new Timer(UPDATE_NUM_MS, new AutoUpdateTask());
 		// timer.start();
 	}
 
 	private void setUpAppearance() {
 		try {
 			sectionUpButton = new JButton(new ImageIcon(ImageIO.read(new File(
 					"images/green_up_arrow_small.png"))));
 			sectionDownButton = new JButton(new ImageIcon(ImageIO.read(new File(
 					"images/green_down_arrow_small.png"))));
 		} catch (IOException e) {
 			if (Util.DEBUG) {
 				e.printStackTrace();
 			}
 			sectionUpButton = new JButton("^");
 			sectionDownButton = new JButton("v");
 		}
 		sectionUpButton.setToolTipText("Move Section Up");
 		sectionDownButton.setToolTipText("Move Section Down");
 		aquireLock = new JButton("Aquire Lock");
 		updateSection = new JButton("Update");
 		addSubSection = new JButton("New SubSection");
 		unlockSubSection = new JButton("Unlock");
 		setLayout(new BorderLayout());
 		setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
 		buttonPanel.add(sectionUpButton, BorderLayout.NORTH);
 		buttonPanel.add(sectionDownButton, BorderLayout.SOUTH);
 		sectionPanel = new JPanel(new BorderLayout(10, 10));
 		sectionPanel.add(listOfSubSections, BorderLayout.CENTER);
 		sectionPanel.add(buttonPanel, BorderLayout.SOUTH);
 
 		JPanel bottomPane = new JPanel(new BorderLayout());
 		JPanel north = new JPanel();
 		north.add(aquireLock);
 		north.add(updateSection);
 		north.add(unlockSubSection);
 		north.add(addSubSection);
 		bottomPane.add(north, BorderLayout.NORTH);
 
 		topFullDocumentPane.setMinimumSize(new Dimension(0, 0));
 		currentWorkintPane.setMinimumSize(new Dimension(0, 0));
 		JScrollPane workScroll = new JScrollPane(currentWorkintPane);
 		JScrollPane docScroll = new JScrollPane(topFullDocumentPane);
 		bottomPane.add(workScroll, BorderLayout.CENTER);
 		workspace = new JSplitPane(JSplitPane.VERTICAL_SPLIT, docScroll, bottomPane);
 		wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionPanel, workspace);
 		wholePane.setContinuousLayout(true);
 		
 		workspace.setDividerLocation(250);
 		workspace.setOneTouchExpandable(true);
 		wholePane.setDividerLocation(150);
 		wholePane.setOneTouchExpandable(true);
 
 		add(wholePane, BorderLayout.CENTER);
 	}
 
 	private boolean hasPermission() {
 		if (client.getPrivLevel() == CoLabPrivilegeLevel.OBSERVER) {
 			JOptionPane
 					.showMessageDialog(
 							this,
 							"You do not have permission to do this action.  Ask your Admin for a promotion",
 							"Insufficient Permissions", JOptionPane.INFORMATION_MESSAGE, null);
 			return false;
 		}
 		return true;
 	}
 
 	public void newSubSection(String name) {
 		if (!hasPermission()) {
 			return;
 		}
 		info.getServer().newSubSection(info.getUserName(), info.getRoomName(), listOfSubSections.getName(), name,
 				listOfSubSections.getSubSectionCount());
 	}
 
 	private void setUpListeners() {
 		listOfSubSections.addMouseListener(new RightClickListener());
 		sectionUpButton.addActionListener(new UpButtonListener());
 		sectionDownButton.addActionListener(new DownButtonListener());
 		aquireLock.addActionListener(new AquireLockListener());
 		updateSection.addActionListener(new UpdateSubSectionListener());
 		listOfSubSections.addListSelectionListener(new SelectedSubSectionListener());
 		addSubSection.addActionListener(new NewSubSectionListener());
 		unlockSubSection.addActionListener(new ReleaseLockListener());
 	}
 
 	public JList getList() {
 		return listOfSubSections;
 	}
 
 	public void updateDocumentView() {
 		topFullDocumentPane.updateDocument(listOfSubSections);
 	}
 
 	public SectionizedDocument getSectionizedDocument() {
 		return listOfSubSections;
 	}
 
 	private void updateSubSection(DocumentSubSection ds, String newText) {
 		if (!hasPermission())
 			return;
 		if (ds == null) {
 			if (Util.DEBUG) {
 				System.out.println("JDocTabedPanel.updateSubSection  the section was null...wtf");
 			}
 			return;
 		}
 		DocumentSubSection temp = new DocumentSubSectionImpl(ds.getName());
 		temp.setLocked(ds.isLocked(), ds.lockedByUser());
 		temp.setText(info.getUserName(), newText);
 		info.getServer().subSectionUpdated(info.getUserName(), info.getRoomName(),
 				info.getDocumentName(), ds.getName(), temp);
 	}
 
 	private DocumentSubSection getCurrentSubSection() {
 		DocumentSubSection sel = (DocumentSubSection) listOfSubSections.getSelectedValue();
 		if (sel == null) {
 			if (listOfSubSections.getModel().getSize() == 0)
 				return null;
 			return (DocumentSubSection) listOfSubSections.getModel().getElementAt(0);
 		}
 		return sel;
 	}
 
 	private class AutoUpdateTask implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// if the current subsection is not locked by this user, don't send the updates
 			if (!info.getUserName().equals(getCurrentSubSection().lockedByUser())) {
 				return;
 			}
 			if (currentWorkintPane.getText().trim().equals("")) {
 				return;
 			}
 			if (getCurrentSubSection().getText().equals(currentWorkintPane.getText())) {
 				return;
 			}
 			updateSubSection(getCurrentSubSection(), currentWorkintPane.getText());
 		}
 	}
 
 	private class UpButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (!hasPermission())
 				return;
 			if (listOfSubSections.getSelectedIndex() > 0) {
 				DocumentSubSection moveUp = (DocumentSubSection) listOfSubSections.getSelectedValue();
 				DocumentSubSection moveDown = (DocumentSubSection) listOfSubSections.getModel().getElementAt(
 						listOfSubSections.getSelectedIndex() - 1);
 				info.getServer().subSectionFlopped(info.getUserName(), info.getRoomName(),
 						info.getDocumentName(), moveUp.getName(), moveDown.getName());
 				listOfSubSections.setSelectedValue(moveUp, true);
 			}
 		}
 	}
 
 	private class DownButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (!hasPermission())
 				return;
 			if (listOfSubSections.getSelectedIndex() != -1
 					&& listOfSubSections.getSelectedIndex() < listOfSubSections.getModel().getSize() - 1) {
 				DocumentSubSection moveDown = (DocumentSubSection) listOfSubSections.getSelectedValue();
 				DocumentSubSection moveUp = (DocumentSubSection) listOfSubSections.getModel().getElementAt(
 						listOfSubSections.getSelectedIndex() + 1);
 				info.getServer().subSectionFlopped(info.getUserName(), info.getRoomName(),
 						info.getDocumentName(), moveUp.getName(), moveDown.getName());
 				listOfSubSections.setSelectedValue(moveDown, true);
 			}
 		}
 	}
 
 	private class AquireLockListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (!hasPermission())
 				return;
 			info.getServer().subSectionLocked(info.getUserName(), info.getRoomName(),
 					info.getDocumentName(), getCurrentSubSection().getName());
			updateWorkPane(getCurrentSubSection());
 		}
 	}
 
 	private class UpdateSubSectionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (!hasPermission())
 				return;
 			updateSubSection(getCurrentSubSection(), currentWorkintPane.getText());
 		}
 	}
 
 	private class SelectedSubSectionListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent e) {
 			if (e.getValueIsAdjusting() == false) {
 				DocumentSubSection ds = getCurrentSubSection();
 				updateWorkPane(ds);
 			}
 		}
 	}
 
 	public void updateWorkPane(String secName) {
 		updateWorkPane(listOfSubSections.getSection(secName));
 	}
 
 	public void updateWorkPane(DocumentSubSection ds) {
 		synchronized (currentWorkintPane) {
 			int carrotPos = currentWorkintPane.getCaretPosition();
 			if (ds != null) {
 				currentWorkintPane.setEditable(info.getUserName().equals(ds.lockedByUser()));
 				currentWorkintPane.setText(ds.getText());
 				int length = currentWorkintPane.getText().length();
 				currentWorkintPane.setCaretPosition(carrotPos <= length ? carrotPos : length);
 			} else {
 				currentWorkintPane.setText("");
 				currentWorkintPane.setEditable(false);
 			}
 		}
 	}
 
 	private class NewSubSectionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (!hasPermission())
 				return;
 			String name = JOptionPane.showInputDialog(JDocTabPanel.this,
 					"Enter a name for the new SubSection");
 			if (name == null)
 				return;
 			newSubSection(name);
 		}
 	}
 
 	private class ReleaseLockListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			info.getServer().subSectionUnLocked(info.getUserName(), info.getRoomName(),
 					info.getDocumentName(), getCurrentSubSection().getName());
			updateWorkPane(getCurrentSubSection());
 		}
 	}
 
 	private class DeleteSubSectionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			deleteSubSection();
 		}
 
 	}
 
 	private class SplitSubSectionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			splitSubSection();
 		}
 	}
 
 	private class MergeSubSectionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			mergeSubSection();
 		}
 	}
 
 	@SuppressWarnings("serial")
 	private static class SplitChooser extends JDialog {
 		private JTextArea theText;
 		private JButton ok;
 		private JLabel instr;
 		private int retIndex = -1;
 
 		private SplitChooser(DocumentSubSection sec) {
 			super();
 			setModal(true);
 			instr = new JLabel("Place your cursor where you would like to split the subsection");
 			theText = new JTextArea();
 			theText.setText(sec.getText());
 			// theText.setEditable(false);
 			Font f = new Font("Courier New", Font.PLAIN, 11);
 			theText.setFont(f);
 			KeyListener[] list = theText.getKeyListeners();
 			for (KeyListener k : list) {
 				theText.removeKeyListener(k);
 			}
 			theText.setTabSize(4);
 			ok = new JButton("Ok");
 			JScrollPane scroll = new JScrollPane(theText);
 			add(scroll, BorderLayout.CENTER);
 			add(instr, BorderLayout.NORTH);
 			add(ok, BorderLayout.SOUTH);
 			ok.addActionListener(okAction);
 			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			pack();
 		}
 
 		public int getChosenIndex() {
 			return retIndex;
 		}
 
 		public static int showSplitChooserDialog(DocumentSubSection sec) {
 			SplitChooser ch = new SplitChooser(sec);
 			ch.setVisible(true);
 			if (Util.DEBUG) {
 				System.out.println("Index chosen: " + ch.getChosenIndex());
 			}
 			return ch.getChosenIndex();
 		}
 
 		private ActionListener okAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				retIndex = theText.getCaretPosition();
 				SplitChooser.this.dispose();
 			}
 		};
 	}
 
 	private class RightClickListener extends MouseAdapter {
 		@Override
 		public void mousePressed(MouseEvent e) {
 			doPop(e);
 		}
 
 		private void doPop(MouseEvent e) {
 			if (e.getButton() != MouseEvent.BUTTON3)
 				return;
 			if (Util.DEBUG) {
 				System.out.println("click event");
 			}
 			if (e.isPopupTrigger()) {
 				JPopupMenu menu;
 				if (e.getSource() == currentWorkintPane) {
 					menu = new WorkingViewRightClickMenu();
 				} else {
 					menu = new SectionRightClickMenu(getCurrentSubSection());
 				}
 				menu.show(e.getComponent(), e.getX(), e.getY());
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			doPop(e);
 		}
 	}
 
 	public class WorkingViewRightClickMenu extends JPopupMenu {
 
 		private JMenuItem splitSubSectionItem;
 		private long bornondate;
 
 		public WorkingViewRightClickMenu() {
 			bornondate = System.currentTimeMillis();
 			splitSubSectionItem = new JMenuItem("Split SubSection At Carrot");
 			splitSubSectionItem.addActionListener(new SplitAtCarrotListener());
 			add(splitSubSectionItem);
 		}
 
 	}
 
 	private class SplitAtCarrotListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int i = currentWorkintPane.getCaretPosition();
 			if (i == -1)
 				return;
 			String name1 = JOptionPane
 					.showInputDialog(JDocTabPanel.this, "Name of the first part:");
 			if (name1 == null)
 				return;
 			String name2 = JOptionPane.showInputDialog(JDocTabPanel.this,
 					"Name of the second part:");
 			if (name2 == null)
 				return;
 			DocumentSubSection sec = getCurrentSubSection();
 			info.getServer().subSectionSplit(info.getUserName(), info.getRoomName(), listOfSubSections.getName(),
 					sec.getName(), name1, name2, i);
 		}
 
 	}
 
 	public class SectionRightClickMenu extends JPopupMenu {
 
 		private JMenuItem aquireLockItem;
 		private JMenuItem releaseLockItem;
 		private JMenuItem splitSectionItem;
 		private JMenuItem mergeSectionItem;
 		private JMenuItem newSubSectionItem;
 
 		private DocumentSubSection sec;
 
 		public SectionRightClickMenu(DocumentSubSection section) {
 			super();
 			sec = section;
 			aquireLockItem = new JMenuItem("Aquire Lock");
 			aquireLockItem.addActionListener(new AquireLockListener());
 			add(aquireLockItem);
 			releaseLockItem = new JMenuItem("Release Lock");
 			releaseLockItem.addActionListener(new ReleaseLockListener());
 			add(releaseLockItem);
 			splitSectionItem = new JMenuItem("Split SubSection");
 			splitSectionItem.addActionListener(new SplitSubSectionListener());
 			add(splitSectionItem);
 			mergeSectionItem = new JMenuItem("Merge Subsection");
 			mergeSectionItem.addActionListener(new MergeSubSectionListener());
 			add(mergeSectionItem);
 			newSubSectionItem = new JMenuItem("Add New SubSection");
 			newSubSectionItem.addActionListener(new NewSubSectionListener());
 			add(newSubSectionItem);
 		}
 
 	}
 
 	public void deleteSubSection() {
 		// TODO make this work
 	}
 
 	public void splitSubSection() {
 		if (!hasPermission())
 			return;
 		String name1 = JOptionPane.showInputDialog(JDocTabPanel.this, "Name of the first part:");
 		if (name1 == null)
 			return;
 		String name2 = JOptionPane.showInputDialog(JDocTabPanel.this, "Name of the second part:");
 		if (name2 == null)
 			return;
 		DocumentSubSection sec = getCurrentSubSection();
 		int idx = SplitChooser.showSplitChooserDialog(sec);
 		if (idx == -1)
 			return;
 		info.getServer().subSectionSplit(info.getUserName(), info.getRoomName(), listOfSubSections.getName(),
 				sec.getName(), name1, name2, idx);
 	}
 
 	public void mergeSubSection() {
 		if (!hasPermission())
 			return;
 		int count = listOfSubSections.getSubSectionCount();
 		if (count < 2)
 			return;
 		DocumentSubSection top = null, bottom = null;
 		if (listOfSubSections.getSelectedIndex() == 0) {
 			top = listOfSubSections.getSectionAt(0);
 			bottom = listOfSubSections.getSectionAt(1);
 		} else if (listOfSubSections.getSelectedIndex() == count - 1) {
 			top = listOfSubSections.getSectionAt(count - 2);
 			bottom = listOfSubSections.getSectionAt(count - 1);
 		} else {
 			String[] values = { "Above", "Below" };
 			String aboveOrBelow = (String) JOptionPane
 					.showInputDialog(
 							JDocTabPanel.this,
 							"Would you like to merge the selected section \nwith the section above or below?",
 							"Merge SubSections", JOptionPane.QUESTION_MESSAGE, null, values,
 							values[0]);
 			if (aboveOrBelow == null)
 				return;
 			if (aboveOrBelow.equals("Above")) {
 				top = listOfSubSections.getSectionAt(listOfSubSections.getSelectedIndex() - 1);
 				bottom = listOfSubSections.getSectionAt(listOfSubSections.getSelectedIndex());
 			} else if (aboveOrBelow.equals("Below")) {
 				top = listOfSubSections.getSectionAt(listOfSubSections.getSelectedIndex());
 				bottom = listOfSubSections.getSectionAt(listOfSubSections.getSelectedIndex() + 1);
 			}
 		}
 		String name = JOptionPane.showInputDialog(JDocTabPanel.this, "Name of merged section:");
 		if (name == null) {
 			return;
 		}
 		info.getServer().subSectionCombined(info.getUserName(), info.getRoomName(), listOfSubSections.getName(),
 				top.getName(), bottom.getName(), name);
 	}
 }
