 package jfmi.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.MatteBorder;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import jfmi.app.EditedTaggedFile;
 import jfmi.app.FileTag;
 import jfmi.app.FileTagging;
 import jfmi.app.TaggedFile;
 import jfmi.control.TaggedFileHandler;
 
 /** A TaggedFileViewPanel displays all information contained in a TaggedFile
   to the user, and allows the user to communicate changes to a
   TaggedFileHandler.
   */
 public class TaggedFileViewDialog extends JDialog implements 
 	ActionListener, 
 	ListSelectionListener {
 
 	// PRIVATE INSTANCE Fields
 	private JLabel fileNameLabel;
 	private JLabel filePathLabel;
 
 	private JButton changePathButton;
 	private JButton addTagButton;
 	private JButton removeTagButton;
 	private JButton saveFileButton;
 
 	private JList<FileTag> tagJList;
 	private JList<FileTagging> taggingJList;
 	private FileTagging lastSelected;
 	private JTextArea commentArea;
 
 	private Box fileInfoBox;
 	private Box fileTaggingBox;
 	private Box saveFileBox;
 
 	private TaggedFileHandler fileHandler;
 	private EditedTaggedFile displayedFile;
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Constructs a TaggedFileViewPanel.
 	  @param parent a JFrame to server as this dialog's owner
 	  @param fileHandler_ file handler to associate with this instance
 	  @throws IllegalArgumentException if fileHandler_ is null
 	  */
 	public TaggedFileViewDialog(JFrame parent, TaggedFileHandler fileHandler_)
 	{
 		super(parent, "File Viewer", true);
 
 		// Initialize fields and child components
 		init(fileHandler_);
 		initFileInfoBox();
 		initFileTaggingBox();
 		initSaveFileBox();
 
 		// Add child components
 		add(fileInfoBox, BorderLayout.NORTH);
 		add(fileTaggingBox, BorderLayout.CENTER);
 		add(saveFileBox, BorderLayout.SOUTH);
 
 		// Not visible initially	
 		setVisible(false);
 	}
 
 	/** Accesses the displayed list of tags.
 	  */
 	public JList<FileTag> getTagJList()
 	{
 		return tagJList;
 	}
 
 	/** Sets the EditedTaggedFile whose information this instance is displaying.
 	  @param file the EditedTaggedFile to display
 	  @throws IllegalArgumentException if file is null
 	  */
 	public void setDisplayedFile(EditedTaggedFile file)
 	{
 		if (file == null) {
 			throw new IllegalArgumentException("file cannot be null");
 		}
 
 		displayedFile = file;
 	}
 
 	/** Sets this object's associated TaggedFileHandler.
 	  @param fileHandler_ the file handler to associate with this instance
 	  @throws IllegalArgumentException if fileHandler_ is null
 	  */
 	public void setFileHandler(TaggedFileHandler fileHandler_)
 	{
 		if (fileHandler_ == null) {
 			throw new IllegalArgumentException("fileHandler_ cannot be null");
 		}
 
 		fileHandler = fileHandler_;
 	}
 
 	/** Updates the information displayed by all of this instance's components.
 	  */
 	public void updateDisplay()
 	{
 		updateFileInfo();
 		updateTaggingJList();
 		updateCommentArea();
 	}
 
 	/** Tells this instance to display the specified file, and updates the
 	  information contained in all of the instance's components accordingly.
 	  @param file the file to display
 	  @throws IllegalArgumentException if file is null
 	  */
 	public void updateDisplayedFile(EditedTaggedFile file)
 	{
 		setDisplayedFile(file);
 		updateDisplay();
 	}
 
 	/** Updates the displayed file name and path with information from the 
 	  instance's FileTag.
 	  */
 	public void updateFileInfo()
 	{
 		fileNameLabel.setText(displayedFile.getEditedFile().getFileName());
 		filePathLabel.setText(displayedFile.getEditedFile().getFilePath());
 	}
 
 	/** Updates the displayed file's tags with information from the instance's 
 	  FileTag.
 	  */
 	public void updateTaggingJList()
 	{
 		Vector<FileTagging> taggings;
 		taggings = new Vector<FileTagging>(displayedFile.getWorkingTaggings());	
 
 		taggingJList.setListData(taggings);
 		taggingJList.setSelectedIndex(0);
 	}
 
 	/** Updates the displayed file's selected tag's comment with information 
 	  from the instance's FileTag.
 	  */
 	public void updateCommentArea()
 	{
 		FileTagging selectedTagging = taggingJList.getSelectedValue();
 
 		if (selectedTagging == null) {
 			return;
 		}
 
 		String comment = selectedTagging.getComment();
 		if (comment == null || comment.equals("")) {
 			commentArea.setText("<No comment>");
 		} else {
 			commentArea.setText(comment);
 		}
 	}
 
 
 	//************************************************************
 	// PRIVATE INSTANCE Methods
 	//************************************************************
 
 	/** Checks if the last selected tagging's comment matches the current
 	  contents of the comment text area. If the two are different, the
 	  file handler is invoked to perform an update on the displayed file's
 	  set of taggings.
 	  */
 	private void handleCommentChange()
 	{
 		if (lastSelected != null) {
 			String oldComment = lastSelected.getComment();
 			String newComment = commentArea.getText();
 
 			if (!oldComment.equals(newComment)) {
 				lastSelected.setComment(newComment);
 				fileHandler.beginUpdateTaggingInteraction(displayedFile,
 														  lastSelected);	
 			}
 		}
 	}
 
 	/** Initializes a TaggedFileViewPanel instance.
 	  @param fileHandler_ file handler to associate with this instance
 	  @throws IllegalArgumentException if fileHandler_ is null
 	  */
 	private final void init(TaggedFileHandler fileHandler_)
 	{
 		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		setLayout(new BorderLayout(5, 5));
 		setFileHandler(fileHandler_);	
 		Styles.setAllSizes(this, new Dimension(600, 400));
 
 		lastSelected = null;
 	}
 
 	/** Initializes the file information box, which displays the file path
 	  information.
 	  */
 	private final void initFileInfoBox()
 	{
 		// Set up left part of fileInfoBox
 		fileNameLabel = new JLabel("No file loaded");
 
 		filePathLabel = new JLabel("");
 
 		Box leftBox = Box.createVerticalBox();
 		leftBox.setBorder(new EmptyBorder(2, 2, 2, 2));
 		leftBox.add(fileNameLabel);
 		leftBox.add(Box.createVerticalStrut(5));
 		leftBox.add(filePathLabel);
 
 		// Set up right part of fileInfoBox
 		changePathButton = new JButton("Change Path");
 		changePathButton.addActionListener(this);
 
 		Box rightBox = Box.createVerticalBox();
 		rightBox.setBorder(new EmptyBorder(2, 2, 2, 2));
 		rightBox.add(changePathButton);
 
 		// Set up fileInfoBox
 		fileInfoBox = Box.createHorizontalBox();
 		fileInfoBox.setBorder(new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
 		fileInfoBox.add(leftBox);
 		fileInfoBox.add(rightBox);
 	}
 
 	/** Initializes the file tagging box, which displays information on the
 	  file's tags and comments.
 	  */
 	private final void initFileTaggingBox()
 	{
 		// Set up left part of box
 		// Set up the tag box
 		tagJList = new JList<FileTag>();
 		tagJList.setLayoutOrientation(JList.VERTICAL);
 		JScrollPane allTagScroller = new JScrollPane(tagJList);
 		allTagScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		Box tagBox = Box.createVerticalBox();
 		tagBox.add(new JLabel("All Tags"));
 		tagBox.add(allTagScroller);
 
 		// Set up the tagging box
 		JLabel fileTagLabel = new JLabel("File Tags");
 		fileTagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		taggingJList = new JList<FileTagging>();		
 		taggingJList.addListSelectionListener(this);
 		taggingJList.setLayoutOrientation(JList.VERTICAL);
 		JScrollPane fileTagScroller = new JScrollPane(taggingJList);
 		fileTagScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		addTagButton = new JButton("Add Tag");
 		addTagButton.addActionListener(this);
 
 		removeTagButton = new JButton("Remove Tag");
 		removeTagButton.addActionListener(this);
 		removeTagButton.setForeground(Styles.DANGER_COLOR);
 		
 		Box buttonBox = Box.createHorizontalBox();
 		buttonBox.setAlignmentX(Component.LEFT_ALIGNMENT);
 		buttonBox.setMaximumSize(new Dimension(300, addTagButton.getHeight()));
 		buttonBox.add(addTagButton);
 		buttonBox.add(Box.createHorizontalStrut(10));
 		buttonBox.add(removeTagButton);
 
 		Box taggingBox = Box.createVerticalBox();
 		taggingBox.add(fileTagLabel);
 		taggingBox.add(fileTagScroller);
 		taggingBox.add(buttonBox);
 		
 		Box leftBox = Box.createHorizontalBox();
 		leftBox.setBorder(new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
 		leftBox.setMaximumSize(new Dimension(600, 800));
 		leftBox.add(tagBox);
 		leftBox.add(Box.createHorizontalStrut(20));
 		leftBox.add(taggingBox);
 
 		// Set up right part of box
 		commentArea = new JTextArea();
 		JScrollPane tagCommentScroller = new JScrollPane(commentArea);
 
 		Box rightBox = Box.createVerticalBox();
 		rightBox.setBorder(new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
 		rightBox.add(new JLabel("Tag Comments"));
 		rightBox.add(tagCommentScroller);
 
 		// Set up fileTaggingBox
 		fileTaggingBox = Box.createHorizontalBox();
 		fileTaggingBox.add(leftBox);
 		fileTaggingBox.add(Box.createHorizontalStrut(30));
 		fileTaggingBox.add(rightBox);
 	}
 
 	/** Initializes the box containing the save button.
 	  */
 	private final void initSaveFileBox()
 	{
 		saveFileButton = new JButton("Save File");
 		saveFileButton.addActionListener(this);
 
 		saveFileBox = Box.createHorizontalBox();
 		saveFileBox.setBorder(new EmptyBorder(10, 10, 10, 10));
 		saveFileBox.add(saveFileButton);
 	}
 
 
 	//************************************************************
 	// IMPLEMENTATION ActionListener 
 	//************************************************************
 
 	/** Determines what action is taken when the user generates an ActionEvent
 	  on one of the components in the TaggedFileViewPanel.
 	  @param e a user-generated ActionEvent
 	  */
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source = e.getSource();
 
 		if (source == changePathButton) {
 
 			fileHandler.beginUpdateFilePathInteraction(displayedFile);
 
 		} else if (source == addTagButton) {
 
 			// Get the selected tag
 			FileTag selectedTag = tagJList.getSelectedValue();
 			if (selectedTag == null) {
 				return;
 			}
 
 			// Create a new FileTagging from the tag
 			FileTagging newTagging = new FileTagging();
 			newTagging.setFileId(displayedFile.getEditedFile().getFileId());
 			newTagging.setTag(selectedTag.getTag());
 
 			// Pass control off to the file handler
 			fileHandler.beginAddTaggingInteraction(displayedFile,
 												   newTagging);
 
 		} else if (source == removeTagButton) {
 
 			// Get the selected tagging
 			FileTagging tagging = taggingJList.getSelectedValue();
 			if (tagging == null) {
 				return;
 			}
 
 			// Pass control off to the file handler
 			fileHandler.beginRemoveTaggingInteraction(displayedFile, tagging);
 
 		} else if (source == saveFileButton) {
 
			handleCommentChange();
 			fileHandler.beginSaveFileInteraction(displayedFile);
 
 		}
 
 	}
 
 
 	//************************************************************
 	// IMPLEMENTATION ListSelectionListener 
 	//************************************************************
 
 	/** Determines what happens when the user generates a ListSelectionEvent
 	  on the dialog's list of file tags.
 	  @param e a user-generated ListSelectionEvent
 	  */
 	public void valueChanged(ListSelectionEvent e)
 	{
 		Object source = e.getSource();
 
 		/* If the source is the JList of taggings for the file being edited,
 		  we want to always update the commentArea with the comment of the newly
 		  selected tagging; however, we also want to check if the comment of
 		  the last selected tagging changed, and if so, assign it to the
 		  set of updated taggings in the displayed/edited file.
 		  */
 		if (source == taggingJList) {
 			handleCommentChange();
 			lastSelected = taggingJList.getSelectedValue();
 			updateCommentArea();
 		}	
 		
 	}
 
 }
