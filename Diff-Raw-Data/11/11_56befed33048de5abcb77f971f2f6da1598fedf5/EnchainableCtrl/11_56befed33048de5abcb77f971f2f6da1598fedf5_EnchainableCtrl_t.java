 /**
  * 
  */
 package be.lreenaers.lafay.web.control;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.model.ListDataModel;
 
 import org.primefaces.event.RowEditEvent;
 
 import be.lreenaers.lafay.DAOs.EnchainableDAO;
 import be.lreenaers.lafay.DAOs.ExerciceDAO;
 import be.lreenaers.lafay.DAOs.NiveauDAO;
 import be.lreenaers.lafay.beans.Enchainable;
 import be.lreenaers.lafay.beans.Exercice;
 import be.lreenaers.lafay.beans.Niveau;
 import be.lreenaers.lafay.factories.DAOFactory;
 import be.lreenaers.lafay.web.interfaces.Controlable;
 import be.lreenaers.lafay.web.interfaces.Filterable;
 import be.lreenaers.lafay.web.interfaces.RowEditable;
 
 /**
  * @author media
  * 
  */
 @ManagedBean(name = "enchainableCtrl")
 @SessionScoped
 public class EnchainableCtrl implements Controlable, RowEditable,
 		Filterable<Enchainable> {
 
 	private EnchainableDAO dao;
 	private NiveauDAO ndao;
 	private ExerciceDAO edao;
 	private Enchainable enchainable;
 	private List<Exercice> exercices;
 	private Enchainable enchainableEdit;
 	private ListDataModel<Enchainable> enchainables;
 	private List<Enchainable> filtered;
 
 	public EnchainableCtrl() {
 		this.dao = DAOFactory.getEnchainableDAO();
 		this.enchainable = new Enchainable();
 		this.enchainableEdit = new Enchainable();
 		this.enchainables = new ListDataModel<Enchainable>();
 		this.enchainables.setWrappedData(this.dao.all());
 	}
 
 	@Override
 	public String create() {
 		this.dao.save(this.enchainable);
 		this.enchainable = new Enchainable();
 		this.enchainables.setWrappedData(this.dao.all());
 		return "goEnchList";
 	}
 
 	@Override
 	public String delete() {
 		Enchainable e = this.enchainables.getRowData();
 		this.ndao = DAOFactory.getNiveauDAO();
 		List<Niveau> nivs = this.ndao.all();
 		Iterator<Niveau> it = nivs.iterator();
 		Niveau n;
 		List<Enchainable> enchs;
 		while (it.hasNext()) {
 			n = it.next();
 			enchs = n.getSeries();
 			enchs.remove(e);
 			n.setSeries(enchs);
 			this.ndao.save(n);
 		}
 		this.dao.delete(e);
 		this.enchainables.setWrappedData(this.dao.all());
 		return "goEnchList";
 	}
 
 	public Enchainable getEnchainable() {
 		return enchainable;
 	}
 
 	public Enchainable getEnchainableEdit() {
 		return enchainableEdit;
 	}
 
 	public ListDataModel<Enchainable> getEnchainables() {
 		return enchainables;
 	}
 
 	@Override
 	public List<Enchainable> getFiltered() {
 		return this.filtered;
 	}
 
 	public String goCreate() {
System.out.println("go");
 		return "goCreateEnch";
 	}
 
 	@Override
 	public void onCancel(RowEditEvent event) {
 
 	}
 
 	@Override
 	public void onEdit(RowEditEvent event) {
 		this.dao.save((Enchainable) event.getObject());
 
 	}
 
 	public void setEnchainable(Enchainable enchainable) {
		System.out.println("set");
 		this.enchainable = enchainable;
 	}
 
 	public void setEnchainableEdit(Enchainable enchainableEdit) {
 		this.enchainableEdit = enchainableEdit;
 	}
 
 	public void setEnchainables(ListDataModel<Enchainable> enchainables) {
 		this.enchainables = enchainables;
 	}
 
 	@Override
 	public void setFiltered(List<Enchainable> list) {
 		this.filtered = list;
 
 	}
 
 	@Override
 	public String update() {
 		this.dao.save(this.enchainableEdit);
 		this.enchainables.setWrappedData(this.dao.all());
 		return "goEnchList";
 	}
 
 	public List<Exercice> getExercices() {
		
 		if(this.exercices == null){
 			this.edao = DAOFactory.getExerciceDAO();
 			this.exercices = this.edao.all();
 		}
 		return this.exercices;
 	}
 
 	public void setExercices(List<Exercice> exercices) {
 		this.exercices = exercices;
 	}
 
 }
