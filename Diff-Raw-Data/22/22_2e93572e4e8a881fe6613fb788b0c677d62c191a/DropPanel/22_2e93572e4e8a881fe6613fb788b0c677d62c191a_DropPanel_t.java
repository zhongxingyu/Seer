 package drop_panel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import service.Errorist;
 import service.IOOperations;
 
 public class DropPanel extends JScrollPane {
 	private static final long serialVersionUID = -9051505855046492589L;
 	public JPanel content_pane = new JPanel();
 	DropPanelMenus panel_menus;
 	boolean vertical = false;
 	int button_angle = 0;
 	
 	private void commonInit(int axis_orient) {
 		panel_menus = new DropPanelMenus(new DropPanelDialogs(this));
 		setViewportView(content_pane);
 		content_pane.setBackground(Color.black);
 		BoxLayout box = new BoxLayout(content_pane, axis_orient);
 		content_pane.setLayout(box);
 		panel_menus.SetContainerMenu(this);
 		
 		new DropTarget(content_pane, new DropTargetListener() {
 			public void dropActionChanged(DropTargetDragEvent dtde) {}
 			public void drop(DropTargetDropEvent evt) {
 			      int action = evt.getDropAction();
 			      evt.acceptDrop(action);
 			      try {
 			          Transferable data = evt.getTransferable();
 			          if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 			              List<File> files = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
 			              for(File file:files) {
 			            	  if (!evt.isLocalTransfer())
 			            		  if (file.isDirectory())
 			            			  DropPanel.this.AddItem(file.getName(), file.getAbsolutePath());
 			              }
 			          }
 			          evt.dropComplete(true);
 			      }
 			      catch (Exception e) { evt.dropComplete(false); Errorist.printLog(e); }
 			}
 			public void dragOver(DropTargetDragEvent dtde) {}
 			public void dragExit(DropTargetEvent dte) {}
 			public void dragEnter(DropTargetDragEvent dtde) {}
 		});		
 	}
 	
 	private void initYGUI(int angle) {
 		button_angle = angle;
 		vertical = true;
 		commonInit(BoxLayout.Y_AXIS);
 		
		if (angle == 270) {
//			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
//			setPreferredSize(new Dimension(30, 0));
//			content_pane.setPreferredSize(new Dimension(50, 0));
		}
 		
 		this.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
 		this.getHorizontalScrollBar().setVisible(false);
 	}
 	
 	private void initXGUI() {
 		commonInit(BoxLayout.X_AXIS);
 		this.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
 		this.getVerticalScrollBar().setVisible(false);
 	}
 	
     public DropPanel() 					{ initXGUI(); }
     public DropPanel(int angle) 		{ initYGUI(angle); }
 	
 	public void AddItem(String text, String path) {
 		//if (text.length() == 0 || path.length() == 0) return;
 		DropPanelItem b = new DropPanelItem(text, path);
 		content_pane.add(b);
 		panel_menus.SetItemMenu(b);
 		if (vertical)
 			b.setUI(new VerticalButtonUI(button_angle));
 		revalidate();
 	}
 	
 	public void DropItem(DropPanelItem b) {
 		content_pane.remove(b);
 		revalidate();
 	}
 	
 	public void Load(String path) {
 		BufferedReader reader = null;
 		try {
 			reader = IOOperations.GetReader(path);
 	  		String strLine;
 	  			  		
 	  		while ((strLine = reader.readLine()) != null) {
 	  			if (strLine.length() == 0) continue;
 	  			AddItem(strLine, reader.readLine());
 	  		}
 	  		reader.close();			
 		} 
 		catch (Exception e) { Errorist.printLog(e); }
 		//finally { if (reader != null) reader.close(); }
 	}
 	
 	public void Save(String path) {
 		PrintWriter pw;
 		try {
 			DropPanelItem item;
 			pw = IOOperations.GetWriter(path, true, false);
 			for(int loop1 = 0; loop1 < content_pane.getComponentCount(); loop1++)
 			{
 				item = (DropPanelItem)content_pane.getComponent(loop1);
 				pw.println(item.getText());
 				pw.println(item.folder.getPath());
 			}
 		}
 		catch (Exception e) { Errorist.printLog(e); }
 	} 	
 }
