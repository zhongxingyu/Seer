 package ersteVersuche.Immobilie;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import ersteVersuche.Material.Haus;
 import ersteVersuche.Material.Immobilie;
 import ersteVersuche.Material.Makler;
 import ersteVersuche.Material.Wohnung;
 import ersteVersuche.Services.ImmoService;
 
 public class ImmobilienmenuWerkzeug {
 	private final ImmobilienmenuGUI _GUI;
 	private final ImmobilieNeuWerkzeug _immobilieNeu;
 	private Makler _aktuellerMakler;
 
 	private final ImmoService _service;
 
 	public ImmobilienmenuWerkzeug(ImmoService service) {
 		_service = service;
 
 		_GUI = new ImmobilienmenuGUI();
 		_immobilieNeu = new ImmobilieNeuWerkzeug();
 		_GUI.AddImmobilienAddListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AddImmobilie();
 			}
 
 		});
 		_GUI.AddImmobilienDelListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DelImmobilie(_GUI.GetAktiveImmobilie());
 			}
 
 		});
 
 		_GUI.AddImmobilienUpdListener(new TableModelListener() {
 
 			@Override
 			public void tableChanged(TableModelEvent e) {
 				int row = e.getFirstRow();
 				// int column = e.getColumn();
 				TableModelImmobilie model = (TableModelImmobilie) e.getSource();
 				// String columnName = model.getColumnName(column);
 				// Object data = model.getValueAt(row, column);
 				Immobilie i = model.GetElement(row);
 				UpdImmobilie(i);
 			}
 		});
 
 		_GUI.AddRefreshListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_GUI.GetTableModel().SetData(LadeImmobilien());
				_GUI.repaint();
 			}
 
 		});
 
 	}
 
 	private void AddImmobilie() {
 		Immobilie i = _immobilieNeu.ErstelleImmobilie();
 		i.setVerwalter(_aktuellerMakler);
 		if (i != null && AddImmobilieSQL(i)) {
 			_GUI.GetTableModel().AddImmobilie(i);
 		}
 		_GUI.repaint();
 	}
 
 	private void DelImmobilie(Immobilie i) {
 
 		if (i != null && DelImmobilieSQL(i)) {
 			_GUI.GetTableModel().DeleteImmobilie(i);
 		}
 		_GUI.repaint();
 	}
 
 	private void UpdImmobilie(Immobilie i) {
 
 		if (i != null && UpdImmobilieSQL(i)) {
 		}
 		_GUI.repaint();
 	}
 
 	public void ZeigeImmobilienMenu(Makler m) {
 		_aktuellerMakler = m;
 		_GUI.GetTableModel().SetData(LadeImmobilien());
 		_GUI.setVisible(true);
 
 	}
 
 	// private boolean AddImmobilieSQL(Wohnung i) {
 	// _service.addWohnung(i);
 	// return true;
 	// }
 	//
 	// private boolean AddImmobilieSQL(Haus i) {
 	// _service.addHaus(i);
 	// return true;
 	// }
 
 	private boolean AddImmobilieSQL(Immobilie i) {
 		_service.addImmobilie(i);
 		return true;
 	}
 
 	private boolean DelImmobilieSQL(Immobilie i) {
 		_service.deleteImmobilie(i);
 		return true;
 	}
 
 	private boolean UpdImmobilieSQL(Immobilie i) {
 		_service.AktualisiereImmobilie(i);
 		return false;
 	}
 
 	private List<Immobilie> LadeImmobilien() {
 
 		List<Immobilie> result = new ArrayList<Immobilie>();
 		result.addAll(LadeImmobilienHaeuser());
 		result.addAll(LadeImmobilienWohnungen());
 
 		return result;
 	}
 
 	private List<Haus> LadeImmobilienHaeuser() {
 
 		List<Haus> result = new ArrayList<Haus>(
 				_service.getAllHaeuserForMakler(_aktuellerMakler));
 
 		return result;
 	}
 
 	private List<Wohnung> LadeImmobilienWohnungen() {
 
 		List<Wohnung> result = new ArrayList<Wohnung>(
 				_service.getAllWohnungenForMakler(_aktuellerMakler));
 
 		return result;
 	}
 
 	public void AddWindowListener(WindowListener l) {
 		_GUI.addWindowListener(l);
 	}
 
 }
