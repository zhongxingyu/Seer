 package net.lecousin.dataorganizer.ui.datalist;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.lecousin.dataorganizer.Local;
 import net.lecousin.dataorganizer.core.DataOrganizer;
 import net.lecousin.dataorganizer.core.DataLabels.Label;
 import net.lecousin.dataorganizer.core.database.Data;
 import net.lecousin.dataorganizer.core.database.content.DataContentType;
 import net.lecousin.dataorganizer.core.database.source.DataSource;
 import net.lecousin.dataorganizer.ui.DataOrganizerDND;
 import net.lecousin.dataorganizer.ui.control.DataImageControl;
 import net.lecousin.dataorganizer.ui.control.LabelsControl;
 import net.lecousin.dataorganizer.ui.control.RateDataControl;
 import net.lecousin.framework.Pair;
 import net.lecousin.framework.event.Event;
 import net.lecousin.framework.event.Event.Listener;
 import net.lecousin.framework.event.Event.ListenerData;
 import net.lecousin.framework.time.DateTimeUtil;
 import net.lecousin.framework.ui.eclipse.UIUtil;
 import net.lecousin.framework.ui.eclipse.control.list.AdvancedList;
 import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
 import net.lecousin.framework.ui.eclipse.control.list.LCMosaic.MosaicConfig;
 import net.lecousin.framework.ui.eclipse.control.list.LCMosaic.MosaicProvider;
 import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
 import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderControl;
 import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
 import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
 import net.lecousin.framework.ui.eclipse.control.list.LCViewer.DragListener;
 import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.URLTransfer;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.part.ViewPart;
 
 public class DataListView extends ViewPart {
 
 	public static final String ID = "net.lecousin.dataorganizer.datalistView";
 	
 	public DataListView() {
 	}
 
 	private AdvancedList<Data> list;
 	private Event<Data> labellingChanged = new Event<Data>();
 	private Listener<Pair<Label,Data>> labellingListener = new Listener<Pair<Label,Data>>() {
 		public void fire(Pair<Label, Data> event) {
 			labellingChanged.fire(event.getValue2());
 		}
 	};
 	
 	private Font dateFont;
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setBackground(ColorUtil.getWhite());
 		dateFont = UIUtil.increaseFontSize(parent.getFont(), -2);
 		list = new AdvancedList<Data>(parent, SWT.MULTI, new TitleProvider(), new ContentProvider());
 		list.addContentChangedEvent(DataOrganizer.search().searchChanged());
 		list.addElementChangedEvent(DataOrganizer.database().dataChanged());
 		Event<Data> contentChanged = new Event<Data>();
 		DataOrganizer.database().dataContentChanged().addListener(new ListenerData<DataContentType,Event<Data>>(contentChanged) {
 			public void fire(DataContentType event) {
 				data().fire(event.getData());
 			}
 		});
 		list.addElementChangedEvent(contentChanged);
 		list.addElementChangedEvent(labellingChanged);
 		DataOrganizer.labels().labelAssigned().addListener(labellingListener);
 		DataOrganizer.labels().labelUnassigned().addListener(labellingListener);
 		list.addRemoveElementEvent(DataOrganizer.search().dataRemoved());
 		list.addAddElementEvent(DataOrganizer.search().dataAdded());
 		DataOrganizer.database().dataAdded().addFireListener(new Runnable() {
 			public void run() {
 				list.refreshTitle();
 			}
 		});
 		DataOrganizer.database().dataRemoved().addFireListener(new Runnable() {
 			public void run() {
 				list.refreshTitle();
 			}
 		});
 		list.addSelectionChangedListener(new Listener<List<Data>>() {
 			public void fire(List<Data> event) {
 				if (event == null || event.size() != 1) { DataOrganizer.setSelectedData(null); return; }
 				DataOrganizer.setSelectedData(event.get(0));
 			}
 		});
 		list.addDoubleClickListener(new Listener<Data>() {
 			public void fire(Data data) {
 				DataListMenu.openDefault(data);
 			}
 		});
 		list.addRightClickListener(new Listener<Data>() {
 			public void fire(Data data) {
 				List<Data> sel = list.getSelection();
 				if (sel == null || sel.isEmpty()) {
 					if (data == null) return;
 					DataListMenu.menu(data, true);
 				} else {
 					if (!sel.contains(data))
 						DataListMenu.menu(data, true);
 					else
 						DataListMenu.menu(sel, true);
 				}
 			}
 		});
 		list.addKeyListener(new KeyListener());
 		TableConfig tableConfig = new TableConfig();
 		tableConfig.multiSelection = true;
 		tableConfig.fixedRowHeight = 18;
 		list.addTableView(Local.Table.toString(), getTableColumns(), tableConfig);
 		MosaicConfig mosaicConfig = new MosaicConfig();
 		mosaicConfig.multiSelection = true;
 		list.addMosaicView(Local.Mosaic.toString(), new Mosaic(), mosaicConfig);
 		list.addDragSupport(DND.DROP_LINK, new Transfer[] { TextTransfer.getInstance(), URLTransfer.getInstance(), FileTransfer.getInstance() }, new DragListener<Data>() {
 			public void dragStart(DragSourceEvent event, List<Data> data) {
 				if (data == null || data.isEmpty()) 
 					event.doit = false;
 				else
 					event.doit = true;
 			}
 			public void dragSetData(DragSourceEvent event, List<Data> data) {
 				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
 					event.data = DataOrganizerDND.getDataDNDString(data);
 					return;
 				}
 				if (URLTransfer.getInstance().isSupportedType(event.dataType)) {
 					if (data.size() > 1) return;
 					List<DataSource> sources = data.get(0).getSources();
 					if (sources.size() != 1) return;
 					DataSource source = sources.get(0);
 					if (source == null) return;
 					try { 
 						URI uri = source.ensurePresenceAndGetURI();
 						event.data = uri.toURL(); 
 					}
 					catch (MalformedURLException e) {}
 					catch (FileNotFoundException e) {}
 					return;
 				}
 				if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
 					List<String> list = new LinkedList<String>();
 					for (Data d : data) {
 						for (DataSource s : d.getSources())
 							if (s != null)
 								try { list.add(new File(s.ensurePresenceAndGetURI()).getAbsolutePath()); }
 								catch (FileNotFoundException e){}
 					}
 					if (list.isEmpty()) return;
 					event.data = list.toArray(new String[list.size()]);
 					return;
 				}
 			}
 			public void dragFinished(DragSourceEvent event, List<Data> data) {
 			}
 		});
 	}
 	
 	private class TitleProvider implements AdvancedList.TitleProvider {
 		public String getTitle() {
 			int nb = DataOrganizer.search().getResult().size();
 			int total = DataOrganizer.database().getAllData().size();
 			return Local.Search_result + "\r\n(" + nb + " " + Local.on + " " + total + " " + (total > 1 ? Local.datas : Local.data) + ")";
 		}
 	}
 	private class ContentProvider implements LCContentProvider<Data> {
 		public Iterable<Data> getElements() {
 			return DataOrganizer.search().getResult();
 		}
 	}
 	@SuppressWarnings("unchecked")
 	private ColumnProvider<Data>[] getTableColumns() {
 		return new ColumnProvider[] {
 			new ColumnName(), new ColumnRate(), new ColumnViews(), new ColumnLastOpened(), new ColumnDateAdded(), new ColumnLabels()
 		};
 	}
 	private class ColumnName implements ColumnProviderText<Data> {
 		public String getTitle() { return Local.Name.toString(); }
 		public int getDefaultWidth() { return 350; }
 		public int getAlignment() { return SWT.LEFT; }
 		public Font getFont(Data element) { return null; }
 		public String getText(Data element) { return element.getName(); }
 		public Image getImage(Data element) { return element.getContentType().getIcon(); }
 		public int compare(Data element1, String text1, Data element2, String text2) { return text1.compareTo(text2); }
 	}
 	private class ColumnViews implements ColumnProviderText<Data> {
 		public String getTitle() { return Local.Views.toString(); }
 		public int getDefaultWidth() { return 50; }
 		public int getAlignment() { return SWT.CENTER; }
 		public Font getFont(Data element) { return null; }
 		public String getText(Data element) { return Integer.toString(element.getViews().size()); }
 		public Image getImage(Data element) { return null; }
 		public int compare(Data element1, String text1, Data element2, String text2) { return element1.getViews().size() - element2.getViews().size(); }
 	}
 	private class ColumnLastOpened implements ColumnProviderText<Data> {
 		public String getTitle() { return Local.Last_open.toString(); }
 		public int getDefaultWidth() { return 65; }
 		public int getAlignment() { return SWT.RIGHT; }
 		public Font getFont(Data element) { return dateFont; }
 		public String getText(Data element) { List<Long> views = element.getViews(); return views.isEmpty() ? Local.Never.toString() : DateTimeUtil.getDateString(views.get(views.size()-1)); }
 		public Image getImage(Data element) { return null; }
 		public int compare(Data element1, String text1, Data element2, String text2) {
 			List<Long> views1 = element1.getViews();
 			List<Long> views2 = element2.getViews();
 			long v1, v2;
 			if (views1.isEmpty()) return views2.isEmpty() ? 0 : 1;
 			if (views2.isEmpty()) return -1;
 			v1 = views1.get(views1.size()-1);
 			v2 = views2.get(views2.size()-1);
 			return (int)(v1 - v2); 
 		}
 	}
 	private class ColumnDateAdded implements ColumnProviderText<Data> {
 		public String getTitle() { return Local.Added.toString(); }
 		public int getDefaultWidth() { return 65; }
 		public int getAlignment() { return SWT.RIGHT; }
 		public Font getFont(Data element) { return dateFont; }
 		public String getText(Data element) { return DateTimeUtil.getDateString(element.getDateAdded()); }
 		public Image getImage(Data element) { return null; }
 		public int compare(Data element1, String text1, Data element2, String text2) { return (int)(element1.getDateAdded() - element2.getDateAdded()); }
 	}
 	private class ColumnRate implements ColumnProviderControl<Data> {
 		public String getTitle() { return Local.Rate.toString(); }
 		public int getDefaultWidth() { return 68; }
 		public int getAlignment() { return SWT.CENTER; }
 		public Control getControl(Composite parent, Data element) { return new RateDataControl(parent, element, true); }
 		public int compare(Data element1, Data element2) { return element1.getRate() - element2.getRate(); }
 	}
 	private class ColumnLabels implements ColumnProviderControl<Data> {
 		public String getTitle() { return Local.Labels.toString(); }
 		public int getDefaultWidth() { return 175; }
 		public int getAlignment() { return SWT.LEFT; }
 		public Control getControl(Composite parent, Data element) { return new LabelsControl(parent, element); }
 		public int compare(Data element1, Data element2) { return 0; /* TODO */ }
 	}
 	
 	private class Mosaic implements MosaicProvider<Data> {
 		public String getText(Data element) {
 			return element.getName();
 		}
 		public Control getImageControl(Composite parent, Data element) {
 			DataImageControl c = new DataImageControl(parent, element, 128, 128);
 			return c;
 		}
 		public Control refreshImageControl(Data element, Control current) {
 			return current;
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		list.setFocus();
 	}
 
 	private class KeyListener implements org.eclipse.swt.events.KeyListener {
 		public void keyPressed(KeyEvent e) {
 		}
 		public void keyReleased(KeyEvent e) {
 			if (e.character == SWT.DEL) {
 				deleteSelection();
 			}
 		}
 	}
 	
 	private void deleteSelection() {
 		List<Data> sel = list.getSelection();
 		if (sel.isEmpty()) return;
 		DataListActions.delete(sel);
 	}
 }
