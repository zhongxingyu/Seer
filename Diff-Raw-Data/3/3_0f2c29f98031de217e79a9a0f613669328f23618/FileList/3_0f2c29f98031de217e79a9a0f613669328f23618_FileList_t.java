 package filelist;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.Collection;
 
 import javax.swing.Icon;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.filechooser.FileSystemView;
 
 import service.Common;
 import service.IOOperations;
 import tabber.Tab;
 
 
 @SuppressWarnings("rawtypes")
 public class FileList extends JList {
 	//Customization
 //	Property String	Object Type
 //	List.actionMap	ActionMap
 //	List.background	Color
 //	List.border	Border
 //	List.cellHeight	Integer
 //	List.cellRenderer	ListCellRenderer
 //	List.focusCellHighlightBorder	Border
 //	List.focusInputMap	InputMap
 //	List.focusInputMap.RightToLeft	InputMap
 //	List.font	Font
 //	List.foreground	Color
 //	List.lockToPositionOnScroll	Boolean
 //	List.rendererUseListColors Boolean	List.rendererUseUIBorder Boolean
 //	List.selectionBackground	Color
 //	List.selectionForeground	Color
 //	List.timeFactor	Long
 //	ListUI	String		
 	
 	private static final long serialVersionUID = 2216859386306446869L;
 	Tab parent;
 	
     //Alter = Vector, ArrayList
 	public MyListModel<ListItem> model = new MyListModel<ListItem>(this);
 	private IconListRenderer listrender = new IconListRenderer();
 	
 	public FileList(Tab parent_tab) {
 		parent = parent_tab;
 		CommonInitPart();
 		setBackground(Color.black);
 	}
     public FileList(Tab parent_tab, ListItem [] items) {
     	parent = parent_tab;
     	CommonInitPart();
     	AddElems(items);
     }	
 	
 	@SuppressWarnings("unchecked")
 	private void CommonInitPart() {
 		super.setModel(model);
     	super.setCellRenderer(listrender);
     	super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
     	new FileListEvents(this);
 	}
 
 //    private void ProceedElem(String elem) {	ProceedElem(new ListItem(elem)); }
 	private void ProceedElem(File elem) { ProceedElem(new ListItem(elem)); }
     private void ProceedElem(ListItem elem) {
         AddAssocIcon(elem.ext, FileSystemView.getFileSystemView().getSystemIcon(elem.file));
         model.addElement(elem);
         Common._initer.AddItem(elem);
     }
 
     public void AddElems(ListItem [] items) {
     	model.ensureCapacity(model.size() + items.length + 1);
     	for(ListItem elem:items) ProceedElem(elem);
     }
 	public void AddElemsLI(Collection<ListItem> items) {
     	model.ensureCapacity(model.size() + items.size() + 1);
     	for(ListItem elem:items) ProceedElem(elem);
     }
 	
     public void AddElemsF(Collection<File> items) {
     	model.ensureCapacity(model.size() + items.size() + 1);
     	for(File elem:items) ProceedElem(elem);
     }    
 	
     public void AddAssocIcon(String ext, Icon icon) {
     	if (listrender.icons.containsKey(ext) || icon == null) return; 
     	listrender.icons.put(ext, icon); 
     }
     
     public void ChangeViewToVertical() { setLayoutOrientation(JList.VERTICAL); }
     public void ChangeViewToVerticalWrap() { setLayoutOrientation(JList.VERTICAL_WRAP); }
     public void ChangeViewToHorizontalWrap() { setLayoutOrientation(JList.HORIZONTAL_WRAP); }
    
 	// This method is called as the cursor moves within the list.
     public String getToolTipText(MouseEvent evt) {
       int index = locationToIndex(evt.getPoint());
       if (index > -1) {
 	      ListItem item = (ListItem)getModel().getElementAt(index);
 	      return item.media_info.toString();
       }
       return null;
     }	
 
 
 
 //interactive search
 //import javax.swing.DefaultListModel;
 //import javax.swing.JList;
 //import javax.swing.event.DocumentEvent;
 //import javax.swing.event.DocumentListener;
 //import javax.swing.text.BadLocationException;
 // 
 //public class SolarSystemSearchField extends JList implements DocumentListener {
 // 
 //    private SolarSystemCollection m_collection;
 //    private static DefaultListModel m_listModel = new DefaultListModel();
 //     
 //    /**
 //     * 
 //     */
 //    private static final long serialVersionUID = 1L;
 //     
 //    SolarSystemSearchField(SolarSystemCollection collection) {
 //        super(m_listModel);
 //         
 //        m_collection = collection;
 //         
 //        for (int i=0; i<m_collection.GetSize(); ++i)
 //            m_listModel.addElement(m_collection.systems.get(i));
 //    }
 //     
 //    public SolarSystem getTopmost() {
 //        if (m_listModel.size() > 0)
 //            return (SolarSystem) m_listModel.get(0);
 //        else
 //            return null;
 //    }
 // 
 //    @Override
 //    public void changedUpdate(DocumentEvent arg0) {
 //        searchForHit(getSearchString(arg0));
 //    }
 // 
 //    @Override
 //    public void insertUpdate(DocumentEvent arg0) {
 //        searchForHit(getSearchString(arg0));
 //    }
 // 
 //    @Override
 //    public void removeUpdate(DocumentEvent arg0) {
 //        searchForHit(getSearchString(arg0));
 //    }
 //     
 //    private String getSearchString(DocumentEvent arg0) {
 //        try {
 //            return arg0.getDocument().getText(0, arg0.getDocument().getLength());
 //        } catch (BadLocationException e) {
 //    		  Errorist.printLog(e);
 //            return "";
 //        }
 //    }
 //     
 //    private void searchForHit(String searchStr) {
 //        m_listModel.clear();
 //        m_listModel.ensureCapacity(m_collection.GetSize());
 // 
 //        for (int i=0; i<m_collection.GetSize(); ++i) {
 //            SolarSystem s = m_collection.systems.get(i);
 //            if (s.name.toLowerCase().contains(searchStr.toLowerCase()))
 //                m_listModel.addElement(s);
 //        }
 //        //if (m_listModel.getSize() > 0)
 //        //  setSelectedIndex(0);
 //    }
 // 
 //}
 
 
 
 	/// Helper methods
 	
 	private int CheckRange(int index) {
 		if (index >= model.getSize()) index = 0;
 		return (index < 0) ? (model.getSize() - 1) : index;
 	}
 
 	public int CalcSelect(int curr, boolean next) {
 		int index = CheckRange(curr + (next ? 1 : -1));
 		setSelectedIndex(index);
 		return index;
 	}	
 	
 	public int MoveSelect(boolean next) { return CalcSelect(getSelectedIndex(), next); }
 	
 	public void MoveSelectAndInit(boolean next) { model.elementAt(MoveSelect(next)).Exec();	}
 	
 	public void DeleteSelectAndInit() {
 		int selected = getSelectedIndex();
 		if (selected == -1) {
 			MoveSelectAndInit(true);
 			return;
 		}
 		if (parent.options.delete_files)
			Common._trash.AddPath(((ListItem)getSelectedValue()).file);
//			IOOperations.deleteFile(((ListItem)getSelectedValue()).file);
 		model.remove(selected);
 		if ((selected = CheckRange(selected)) == -1) return;
 		setSelectedIndex(selected);
 		model.elementAt(selected).Exec();
 	}
 	
 	public void SetStatus(String status) {parent.SetStatus(status);}
 }
