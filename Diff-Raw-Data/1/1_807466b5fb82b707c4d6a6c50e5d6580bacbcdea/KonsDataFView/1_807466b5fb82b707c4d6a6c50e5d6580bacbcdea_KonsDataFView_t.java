 package com.hilotec.elexis.kgview;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 
 import com.hilotec.elexis.kgview.data.KonsData;
 
 import ch.elexis.actions.ElexisEvent;
 import ch.elexis.actions.ElexisEventDispatcher;
 import ch.elexis.actions.ElexisEventListener;
 import ch.elexis.data.Konsultation;
 import ch.elexis.data.Patient;
 import ch.elexis.data.PersistentObject;
 import ch.elexis.icpc.IcpcCode;
 import ch.elexis.util.PersistentObjectDropTarget;
 import ch.rgw.tools.StringTool;
 
 public abstract class KonsDataFView extends SimpleTextFView
 	implements ElexisEventListener
 {
 	protected final String dbfield;
 	protected final String icpcfield;
 	private List icpc_list;
 	private ArrayList<IcpcCode> code_list;
 
 	protected KonsDataFView(String field) {
 		dbfield = field;
 		icpcfield = null;
 	}
 	
 	protected KonsDataFView(String field, String icpc) {
 		dbfield = field;
 		icpcfield = icpc;
 	}
 
 	protected void clearIcpc() {
 		if (icpcfield == null) return;
 		icpc_list.removeAll();
 		code_list.clear();
 	}
 	
 	private KonsData getCurKonsData() {
 		Konsultation k = (Konsultation)
 		ElexisEventDispatcher.getSelected(Konsultation.class);
 		return KonsData.load(k);
 	}
 
 	
 	protected void storeIcpc() {
 		if (icpcfield == null) return;
 		StringBuffer sb = new StringBuffer();
 		for (IcpcCode c: code_list) {
 			sb.append(c.getCode());
 			sb.append(",");
 		}
 		if (sb.length() > 0) sb.setLength(sb.length() - 1);
 		getCurKonsData().set(icpcfield, sb.toString());
 		setEmpty();
 	}
 	
 	protected void loadIcpc(Konsultation k) {
 		if (icpcfield == null) return;
 		KonsData kd = new KonsData(k);
 		clearIcpc();
 		
 		String entries[] = StringTool.unNull(kd.get(icpcfield)).split(",");
 		for (String c: entries) {
 			if (c.length() == 0) continue;
 			IcpcCode code = IcpcCode.load(c);
 			code_list.add(code);
 			icpc_list.add(code.getLabel());
 		}
 	}
 	
 	private void removeIcpcCode() {
 		if (icpcfield == null) return;
 		int i = icpc_list.getSelectionIndex();
 		if (i >= 0) {
 			code_list.remove(i);
 			icpc_list.remove(i);
 			storeIcpc();
 		}
 		setEmpty();
 	}
 	
 	@Override
 	protected void initialize() {
 		if (icpcfield != null) {
 			GridData gd = new GridData();
 			gd.horizontalAlignment = gd.verticalAlignment = GridData.FILL;
 			gd.grabExcessHorizontalSpace = true;
 			gd.heightHint = 40;
 			
 			code_list = new ArrayList<IcpcCode>();
 			icpc_list = new List(area, SWT.V_SCROLL);
 			icpc_list.setLayoutData(gd);
 			icpc_list.addKeyListener(new KeyListener() {
 				public void keyReleased(KeyEvent e) {}
 				public void keyPressed(KeyEvent e) {
 					if (e.keyCode != SWT.DEL) return;
 					removeIcpcCode();
 				}
 			});
 
 			Menu m  = new Menu(icpc_list);
 			MenuItem mi = new MenuItem(m, 0);
 			mi.setText("Entfernen");
 			mi.addSelectionListener(new SelectionListener() {
 				public void widgetSelected(SelectionEvent e) {
 					removeIcpcCode();
 				}
 				public void widgetDefaultSelected(SelectionEvent e) {}
 			});
 			icpc_list.setMenu(m);
 			
 			new PersistentObjectDropTarget(icpc_list,
 					new PersistentObjectDropTarget.IReceiver() {
 						public void dropped(PersistentObject o, DropTargetEvent e) {
 							IcpcCode code = (IcpcCode) o;
 							icpc_list.add(code.getLabel());
 							code_list.add(code);
 							storeIcpc();
 						}
 						public boolean accept(PersistentObject o) {
 							if (!(o instanceof IcpcCode) || code_list.contains(o))
 								return false;
 							return isEnabled();
 						}
 					});
 		}
 		
 		Konsultation k = (Konsultation)
 			ElexisEventDispatcher.getSelected(Konsultation.class);
 		if (k != null) {
 			konsChanged(k);
 		}
 		
 		ElexisEventDispatcher.getInstance().addListeners(this);
 	}
 
 	@Override
 	protected void fieldChanged() {
 		super.fieldChanged();
 		if (!isEnabled()) {
 			return;
 		}
 		KonsData kd = getCurKonsData();
 		kd.set(dbfield, getText());
 	}
 
 	private void konsChanged(Konsultation k) {
 		KonsData kd = new KonsData(k);
 		setEnabled(true);
 
 		loadIcpc(k);
 		String text = StringTool.unNull(kd.get(dbfield));
 		setText(text);
 	}
 	
 	@Override
 	protected boolean isEmpty() {
 		return super.isEmpty() &&
 			(code_list == null || code_list.isEmpty()); 
 	}
 	
 	@Override
 	protected void setEnabled(boolean en) {
 		super.setEnabled(en);
 		
 		clearIcpc();
 		if (icpcfield != null)
 			icpc_list.setEnabled(en);
 	}
 	
 	public void catchElexisEvent(ElexisEvent ev) {
		if (ev.getObject() == null) return;
 		if (ev.getObjectClass().equals(Konsultation.class)) {
 			Konsultation k = (Konsultation) ev.getObject();
 			KonsData kd = new KonsData(k);
 			if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
 				kd.set(dbfield, getText());
 				setEnabled(false);
 			} else if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
 				konsChanged(k);
 			}
 		} else if (ev.getObjectClass().equals(Patient.class)) {
 			Konsultation k = (Konsultation)
 				ElexisEventDispatcher.getSelected(Konsultation.class);
 			if (k == null || (ev.getType() == ElexisEvent.EVENT_SELECTED &&
 				(k.getFall().getPatient() != ev.getObject())))
 			{
 				setEnabled(false);
 			}  else {
 				konsChanged(k);
 			}
 		}
 	}
 
 	private final ElexisEvent eetmpl =
 		new ElexisEvent(null, null, ElexisEvent.EVENT_SELECTED
 			| ElexisEvent.EVENT_DESELECTED);
 
 	public ElexisEvent getElexisEventFilter() {
 		return eetmpl;
 	}
 	
 	@Override
 	public void dispose() {
 		ElexisEventDispatcher.getInstance().removeListeners(this);
 		super.dispose();
 	}
 }
