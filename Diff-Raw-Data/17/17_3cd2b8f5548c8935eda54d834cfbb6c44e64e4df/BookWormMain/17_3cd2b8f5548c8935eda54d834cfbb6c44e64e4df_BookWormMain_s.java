 package com.tnicoll.apps.bookworm;
 
 import java.awt.BorderLayout;
 import java.awt.GraphicsConfiguration;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 import javax.swing.*;
 
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.parser.*;
 import org.apache.tika.sax.BodyContentHandler;
 
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Multiset.Entry;
 import com.tnicoll.apps.bookworm.gui.BookPanel;
 import com.tnicoll.apps.bookworm.model.Book;
 
 
 
 /**
  * Main Class
  *
  */
 public class BookWormMain extends javax.swing.JFrame
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2381944488789900157L;
 	private JMenuItem helpMenuItem;
 
 	private JMenuBar toolbar;
 
 
 	private JMenuItem exitMenuItem;
 	private JSeparator jSeparator2;
 	private JMenuItem closeFileMenuItem;
 	private JMenuItem openFileMenuItem;
 	private JMenu fileMenu;
 	private JMenu helpMenu;
 	private JFileChooser fc = new JFileChooser();
 
 	private JTabbedPane tabbedPane;
 
 	private int width;
 	private int height;
 	public BodyContentHandler handler;
 	public Metadata metadata;
 	public AutoDetectParser parser;
 	public ParseContext context;
 	public boolean done;
 
 
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException | UnsupportedLookAndFeelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 
 				BookWormMain inst = new BookWormMain();
 				inst.setLocationRelativeTo(null);
 				inst.setVisible(true);
 			}
 		});
 
 	}
 
 	public BookWormMain() {
 		super();
 		initGUI();
 	}
 
 	private void initGUI() {
 
 		GraphicsConfiguration gc = getGraphicsConfiguration( ); 
 		Rectangle screenRect = gc.getBounds( ); // screen dimensions  
 		Toolkit tk = Toolkit.getDefaultToolkit( ); 
 		Insets desktopInsets = tk.getScreenInsets(gc);  
 		Insets frameInsets = getInsets( ); // only works after pack( )  
 		width = screenRect.width - (desktopInsets.left + desktopInsets.right) - 
 				(frameInsets.left + frameInsets.right); 
 		height = screenRect.height - (desktopInsets.top + desktopInsets.bottom) - 
 				(frameInsets.top + frameInsets.bottom);
 		this.setSize(width, height);
 		this.setResizable(true);
 		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				System.exit(0);
 			}
 		});
 		tabbedPane = new JTabbedPane();
 
 		getContentPane().add(tabbedPane, BorderLayout.WEST);
 
 
 
 
 
 		//Add toolbar menu
 		toolbar = new JMenuBar();
 		setJMenuBar(toolbar);
 
 		fileMenu = new JMenu();
 		toolbar.add(fileMenu);
 		fileMenu.setText("File");
 		{
 			openFileMenuItem = new JMenuItem();
 			openFileMenuItem.addActionListener(new OpenListener());
 			fileMenu.add(openFileMenuItem);
 			openFileMenuItem.setText("Open");
 		}
 
 		{
 			closeFileMenuItem = new JMenuItem();
 			closeFileMenuItem.addActionListener(new CloseListener());
 			fileMenu.add(closeFileMenuItem);
 			closeFileMenuItem.setText("Close");
 		}
 		{
 			jSeparator2 = new JSeparator();
 			fileMenu.add(jSeparator2);
 		}
 		{
 			exitMenuItem = new JMenuItem();
 			fileMenu.add(exitMenuItem);
 			exitMenuItem.setText("Exit");
 		}
 
 		helpMenu = new JMenu();
 		toolbar.add(helpMenu);
 		helpMenu.setText("Help");
 		{
 			helpMenuItem = new JMenuItem();
 			helpMenu.add(helpMenuItem);
 			helpMenuItem.setText("Help");
 		}
 
 
 	}
 
 
 
 
 	class OpenListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == openFileMenuItem) {
 				int returnVal = fc.showOpenDialog(BookWormMain.this);
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 
 
 					openFile();
 				} else {
 					//Do something else
 				}
 			}
 		}
 	}
 
 	class CloseListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == closeFileMenuItem) {
 				int index = tabbedPane.getSelectedIndex();
 				if(index!=-1)
 					tabbedPane.remove(index);
 			}
 		}
 	}
 
 
 	public void openFile()
 	{
 		File file = fc.getSelectedFile();
 		int i=0;
 		try (InputStream stream = new FileInputStream(file);){
 			System.out.println("stream loaded");
 			handler = new BodyContentHandler(-1);
 			metadata = new Metadata();
 			parser = new AutoDetectParser();
 			System.out.println("before parser");
 			context = new ParseContext();
 
 			parser.parse(stream, handler, metadata, context);
 
 			System.out.println("after parser");
 			String content = handler.toString();
 			System.out.println("got content");
 			System.out.println("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
 			//        System.out.println("Title: " + metadata.get(Metadata.TITLE));
 			//        System.out.println("Word count: " + metadata.get(Metadata.WORD_COUNT));
 			//        System.out.println("Paragraph count: " +  metadata.get(Metadata.PARAGRAPH_COUNT));
 
 			BookPanel bp = new BookPanel(width-20, height-40);
 
 
 			bp.setConsoleText(content);
 
 			Book b = new Book();
 			Multiset <String> words = b.readBook(content);
 
 			// String []columnNames = {"Word", "Count"};
 			Object [][] data = new Object [words.size()][2];
 			Object []columnNames = {"Word", "Count"};
 
 			BookModel model = bp.getModel();
 
 			for(Entry<String> entry : words.entrySet())
 			{
 				data[i][0]=entry.getElement();
 				data[i][1]=entry.getCount();
 				model.setValueAt(entry.getElement(), i, 0);
 				model.setValueAt(entry.getCount(), i, 1);
 				i++;
 				if(i<words.size())
 					model.addRow(data);
 			}
 
 			bp.setModel(model);
 			bp.setParagraphCnt(b.getParagraph_count());
 			tabbedPane.addTab("Book1",bp);
 
 		}
 
 		catch (Exception ex)
 		{
			System.out.println("I: " + i);
 			ex.printStackTrace();
 		}
 	}
 
	public void resetList()
	{
		//		console.setText("");
		//        while (model.getRowCount()>0){
		//        	model.removeRow(0);
		//        	}
		//		Object [][] data = {{"",new Integer(0)}};;
		//		model.addRow(data);
		//		model.setValueAt("", 0, 0);
		//		model.setValueAt(0, 0, 1);
	}

 }
