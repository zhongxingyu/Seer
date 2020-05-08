 package org.kalisen.classpathdoctor.gui;
 
 import java.awt.BorderLayout;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 
 import org.kalisen.classpathdoctor.ClassPath;
 import org.kalisen.classpathdoctor.PathEntry;
 import org.kalisen.classpathdoctor.adapter.ClassPathAdapter;
 import org.kalisen.classpathdoctor.adapter.DefaultClassPathAdapter;
 import org.kalisen.classpathdoctor.gui.actions.AddAnEntryAction;
 import org.kalisen.classpathdoctor.gui.actions.RemoveAnEntryAction;
 import org.kalisen.common.ErrorHandler;
 
 @SuppressWarnings("serial")
 public class ClassPathPanel extends JPanel {
 
 	private ErrorHandler errorHandler = null;
 
 	private JList classpathList = null;
 	private JTextArea classpathTextArea = null;
 	private JPanel buttonPanel = null;
 
 	private DefaultListModel classpathListModel = null;
 	private ClassPathAdapter adapter = null;
 
 	private DocumentListener textAreaListener = new DocumentListener() {
 		public void changedUpdate(DocumentEvent e) {
 			notifyAdapter(e);
 		}
 
 		public void insertUpdate(DocumentEvent e) {
 			notifyAdapter(e);
 		}
 
 		public void removeUpdate(DocumentEvent e) {
 			notifyAdapter(e);
 		}
 
 		private void notifyAdapter(DocumentEvent e) {
 			try {
 				getAdapter()
 						.setClassPath(
 								e.getDocument().getText(0,
 										e.getDocument().getLength()));
 			} catch (BadLocationException ble) {
 				handleError(ble);
 			}
 		}
 	};
 
 	private Observer adapterListener = new Observer() {
 		public void update(Observable o, Object arg) {
 			if (arg instanceof ClassPath) {
 				ClassPath cp = (ClassPath)arg;
 				//update the list
 				DefaultListModel model = ClassPathPanel.this.classpathListModel;
 				model.clear();
 				for (PathEntry entry : cp.getEntries()) {
 					model.addElement(entry);
 				}
 				
 				//update the text area
 				String classPathAsText = cp.toString();
				if (!classPathAsText.equals(ClassPathPanel.this.classpathTextArea)) {
 					ClassPathPanel.this.classpathTextArea.setText(classPathAsText);
 				}
 			}
 		}
 	};
 
 	public ClassPathPanel() {
 		init();
 	}
 
 	private void init() {
 		setLayout(new BorderLayout());
 		this.classpathList = buildListComponent();
 		this.classpathTextArea = buildTextComponent();
 		this.buttonPanel = buildButtonPanel();
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		splitPane.setDividerLocation(.8);
 		splitPane.setResizeWeight(.8);
 		JScrollPane listScrollPane = new JScrollPane(this.classpathList);
 		splitPane.add(listScrollPane, JSplitPane.TOP);
 		splitPane.add(this.classpathTextArea, JSplitPane.BOTTOM);
 		add(splitPane, BorderLayout.CENTER);
 		add(this.buttonPanel, BorderLayout.EAST);
 		initListeners();
 	}
 
 	private void initListeners() {
 		this.classpathTextArea.getDocument().addDocumentListener(
 				this.textAreaListener);
 		getAdapter().addListener(this.adapterListener);
 	}
 
 	private JPanel buildButtonPanel() {
 		JPanel result = new JPanel();
 		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
 		result.add(Box.createVerticalGlue());
 		 JButton addButton = new JButton(new AddAnEntryAction(this, getAdapter()));
 		 result.add(addButton);
 		 JButton removeButton = new JButton(new RemoveAnEntryAction(getAdapter()));
 		 result.add(removeButton);
 		result.add(Box.createVerticalGlue());
 		return result;
 	}
 
 	private JTextArea buildTextComponent() {
 		JTextArea result = new JTextArea();
 		return result;
 	}
 
 	private JList buildListComponent() {
 		this.classpathListModel = new DefaultListModel();
 		JList result = new JList(this.classpathListModel);
 		return result;
 	}
 
 	public ClassPathAdapter getAdapter() {
 		if (this.adapter == null) {
 			this.adapter = new DefaultClassPathAdapter();
 		}
 		return adapter;
 	}
 
 	public void setAdapter(ClassPathAdapter adapter) {
 		if (adapter == null) {
 			throw new IllegalArgumentException("null is not a valid argument");
 		}
 		this.adapter = adapter;
 	}
 
 	public void handleError(Throwable t) {
 		this.errorHandler.handleError(t);
 	}
 }
