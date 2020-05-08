 package jmt.manual;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Scrollbar;
 import java.awt.Toolkit;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreePath;
 
 import com.sun.pdfview.OutlineNode;
 import com.sun.pdfview.PDFFile;
 import com.sun.pdfview.PDFPage;
 import com.sun.pdfview.action.GoToAction;
 
 /**
  * @author Lucia Guglielmetti
  */
 public class PDFViewerBuffer extends JFrame implements AdjustmentListener,
 		TreeSelectionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final int PAGE_NUM_RESERVED = -1;
	private static final String MANUAL_RESOURCE = "manuals/manual.pdf";
	private static final String EMPTY_RESOURCE = "emptyFile.txt";
 	
 	//size of buffer
 	private static int BUFFER_SIZE = 3;// MUST BE ODD
 	//size of page display
 	private static int PAGE_DIMENSION_RATIO = 65;
 	
 	//buffer to memory use for load manual
 	private Image buffer[];
 	private int bufferIndex[];
 	
 	//manual
 	private PDFFile pdfFile;
 	//page start and page end to section of manual specified
 	private int pageStart;
 	private int pageEnd;
 	
 	// use to create bar in the panel
 	private Scrollbar bar;
 	private static int SCROLLBAR_ACCURANCY = 1;
 
 	// Panel where upload manual
 	private JPanel canvas;
 
 	//size of image displayed
 	private double imageWidth;
 	private double imageHeight;
 	
 	//grid with one row and one column for displayed images
 	private GridLayout canvasLayout;
 	
 	private OutlineNode outline;
 	
 	//Second Panel to display index
 	private JDialog olf;
 	
 	private OutlineNode currentOutline;
 	
 	//tree use to display index
 	private JTree jt;
 
 	/**
 	 * It render the section of the manual specified by @marker. 
 	 * 
 	 * @param title
 	 * @param marker
 	 * @throws IOException
 	 */
 	public PDFViewerBuffer(String title, ManualBookmarkers marker)
 			throws IOException {
 		super(title);
 
 	/**	To avoid out of
 	 * memory issue, it uses a buffer to load the pages.	
 	 */	
 		buffer = new Image[BUFFER_SIZE];
 		bufferIndex = new int[BUFFER_SIZE];
 		JPanel omni = new JPanel(new BorderLayout());
 		canvas = new JPanel();
 
 		loadManual(marker);
 		loadPage(0);
 
 		updateCanvas();
 	
 	/**	
 	 * creation and management scrollbars	
 	 */	
 		
 		bar = new Scrollbar(Scrollbar.VERTICAL, 0, 1, 0, (pageEnd - pageStart)
 				* SCROLLBAR_ACCURANCY);
 		bar.addAdjustmentListener(this);
 		JScrollPane scroller = new JScrollPane(canvas);
 		scroller.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
 		scroller.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
 		omni.add(scroller, BorderLayout.CENTER);
 
 		omni.add(bar, BorderLayout.WEST);
 		omni.add(bar, BorderLayout.EAST);
 		setContentPane(omni);
 
 		setPreferredSize(new Dimension((int) imageWidth,
 				(int) (imageHeight * 1.9)));
 		pack();
 		 setVisible(true);
 
 	}
 
 	
 	
 	/**
 	 * Creating jpanel with image upload and creation secondary window
 	 * with index
 	 */
 	private void updateCanvas() {
 		canvas.removeAll();
 		canvasLayout = new GridLayout(1, 1);
 		canvas.setLayout(canvasLayout);
 		JPanel panel = new JPanel(new BorderLayout());
 
 		JLabel labelImg = new JLabel(new ImageIcon(buffer[BUFFER_SIZE / 2]));
 		JPanel panelSep = new JPanel(new FlowLayout());
 		panelSep.setOpaque(true);
 		panelSep.setBackground(Color.BLACK);
 
 		panel.add(labelImg, BorderLayout.CENTER);
 		panel.add(panelSep, BorderLayout.SOUTH);
 
 		try {
 			outline = pdfFile.getOutline();
 		} catch (IOException ioe) {
 		}
 		
 		/**
 		 * Creation a secondary window. 
 		 * This window see with tree hierarchy of the index menu.
 		 */
 		if (outline != null && jt == null) {
 			if (outline.getChildCount() > 0) {
 				olf = new JDialog(this, "Men");
 				olf.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 				olf.setLocation(876, 0);
 
 				jt = new JTree(currentOutline);
 				jt.setRootVisible(false);
 				jt.addTreeSelectionListener(this);
 
 				JScrollPane jsp = new JScrollPane(jt);
 				olf.getContentPane().add(jsp);
 				olf.pack();
 				olf.setVisible(true);
 			} else {
 				if (olf != null) {
 					olf.setVisible(false);
 					olf = null;
 				}
 			}
 		}
 		canvas.add(panel);
 		validate();
 	}
 
 	/**
 	 * Loads the pdf manual file from the file system and set pageStart and
 	 * pageEnd accordingly with @marker  the manual you want to open
 	 * 
 	 * @param marker
 	 *           
 	 * @throws IOException
 	 */
 	private void loadManual(ManualBookmarkers marker) throws IOException {
		// Search for manual in default path
		File manualFile = new File(MANUAL_RESOURCE);
		// Try to search starting from JMT.jar location
		if (!manualFile.isFile()) {
			try {
				URI uri = PDFViewerBuffer.class.getResource(EMPTY_RESOURCE).toURI();
				manualFile = new File(new File(uri).getParentFile(), MANUAL_RESOURCE);
			} catch (Exception ex) {
				//Nothing to do here, we will throw exceptions later.
			}
		}
		if (!manualFile.isFile()) {
			throw new IOException("Could not find JMT manual.pdf file. Please place it in $JMT_BASE_DIR/manuals/manual.pdf location.");
 		}
 
		RandomAccessFile raf = new RandomAccessFile(manualFile, "r");
 
 		FileChannel fc = raf.getChannel();
 		ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 		pdfFile = new PDFFile(buf);
 
 		pageStart = 0;
 		pageEnd = pdfFile.getNumPages();
 		OutlineNode on = (OutlineNode) pdfFile.getOutline();
 		if (on != null) {
 			for (int i = 0; i < on.getChildCount(); i++) {
 				OutlineNode child = (OutlineNode) on.getChildAt(i);
 				GoToAction act = (GoToAction) child.getAction();
 	            
 				//Find the beginning of the chapter selected, 
 				//then take the page start and page end number
 				if (child.toString().equals(marker.toString())) {
 					currentOutline = child;
 					pageStart = pdfFile.getPageNumber(act.getDestination()
 							.getPage());
 					if (i + 1 < on.getChildCount()) {
 						OutlineNode childAfter = (OutlineNode) on
 								.getChildAt(i + 1);
 						GoToAction actAfter = (GoToAction) childAfter
 								.getAction();
 						pageEnd = pdfFile.getPageNumber(actAfter
 								.getDestination().getPage());
 					}// else if child is the last one, pageEnd is already set.
 					break;
 				}
 			}
 		}
 
 	}
     /**
      * Loads the number of pages (indicated to @num ) of the selected section 
      * of the manual.
      * To avoid problems of memory used is a buffer memory
      **/
 	private void loadPage(int num) {
 
 		for (int i = 0; i < BUFFER_SIZE; i++) {
 			int page = pageStart + num - (BUFFER_SIZE / 2) + i;
 			if (page < pageStart || page >= pageEnd) {
 				buffer[i] = null;
 				bufferIndex[i] = PAGE_NUM_RESERVED;
 				continue;
 			}
 			buffer[i] = getPageFromBuffer(page);
 			if (buffer[i] == null) {
 				buffer[i] = getPageFromDisk(page);
 			} else {
 			}
 			// TODO check this update
 			bufferIndex[i] = page;
 		}
 	}
     /**
      * 
      * @param page
      * @return
      */
 	private Image getPageFromBuffer(int page) {
 		Image res = null;
 		for (int i = 0; i < BUFFER_SIZE; i++) {
 			if (bufferIndex[i] == page)
 				return buffer[i];
 		}
 		return res;
 	}
 	
 	/**
 	 * create the image to load
 	 **/
 	private Image getPageFromDisk(int pageNum) {
 		PDFPage page = pdfFile.getPage(pageNum + 1);
 		Rectangle2D r2d = page.getBBox();
 		imageWidth = r2d.getWidth();
 		imageHeight = r2d.getHeight();
 		imageWidth /= PAGE_DIMENSION_RATIO;
 		imageHeight /= PAGE_DIMENSION_RATIO;
 		int res = Toolkit.getDefaultToolkit().getScreenResolution();
 		imageWidth *= res;
 		imageHeight *= res;
 		return page.getImage((int) imageWidth, (int) imageHeight, r2d, null,
 				true, true);
 	}
 
 	public static void main(final String[] args) throws IOException {
 		Runnable r = new Runnable() {
 			public void run() {
 				try {
 					new PDFViewerBuffer("PDF Viewer",
 							ManualBookmarkers.JSIMgraph);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		EventQueue.invokeLater(r);
 
 	}
 
 	public void adjustmentValueChanged(AdjustmentEvent ev) {
 		int value = bar.getValue();
 		int page = value;
 		validate();
 		loadPage(page);
 		updateCanvas();
 	}
 
 	/**
 	 * Once you select a subsection from the menu window,
 	 * it does refer to the correct page
 	 */
 	public void valueChanged(TreeSelectionEvent e) {
 
 		TreePath path = e.getNewLeadSelectionPath();
 		OutlineNode selectedNode = (OutlineNode) path.getLastPathComponent();
 		GoToAction act = (GoToAction) selectedNode.getAction();
 		
 		try {
 			int pageNode = pdfFile
 					.getPageNumber(act.getDestination().getPage());
 			bar.setValue(pageNode - pageStart);
 			loadPage(pageNode - pageStart);
 			updateCanvas();
 		} catch (IOException e1) {
 
 		}
 
 	}
 
 }
