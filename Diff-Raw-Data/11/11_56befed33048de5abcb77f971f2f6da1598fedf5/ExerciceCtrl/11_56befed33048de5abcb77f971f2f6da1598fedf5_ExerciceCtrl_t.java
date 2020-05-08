 package be.lreenaers.lafay.web.control;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.model.ListDataModel;
 
 import org.primefaces.event.RowEditEvent;
 
 import be.lreenaers.lafay.DAOs.EnchainableDAO;
 import be.lreenaers.lafay.DAOs.ExerciceDAO;
 import be.lreenaers.lafay.beans.Enchainable;
 import be.lreenaers.lafay.beans.Exercice;
 import be.lreenaers.lafay.factories.DAOFactory;
 import be.lreenaers.lafay.web.interfaces.Controlable;
 import be.lreenaers.lafay.web.interfaces.Filterable;
 import be.lreenaers.lafay.web.interfaces.Modifiable;
 import be.lreenaers.lafay.web.interfaces.RowEditable;
 @ManagedBean(name = "exerciceCtrl")
 @SessionScoped
 public class ExerciceCtrl implements Controlable, RowEditable,
 		Filterable<Exercice>, Modifiable {
 	private List<Exercice> filtered;
 	private ExerciceDAO dao;
 	private Exercice exercice;
 	private Exercice exerciceEdit;
 	private ListDataModel<Exercice> exercices;
 
 	public ExerciceCtrl() {
 		this.dao = DAOFactory.getExerciceDAO();
 		this.exercice = new Exercice();
 		this.exerciceEdit = new Exercice();
 		this.exercices = new ListDataModel<Exercice>();
 		this.exercices.setWrappedData(this.dao.all());
 	}
 
 	@Override
 	public String create() {
 		this.dao.save(this.exercice);
		this.exercice = new Exercice();
 		this.exercices.setWrappedData(this.dao.all());
 		return "goExLst";
 	}
 
 	@Override
 	public String delete() {
 		Exercice e = (Exercice) this.exercices.getRowData();
 		EnchainableDAO edao = DAOFactory.getEnchainableDAO();
 		List<Enchainable> enchs = edao.all();
 		Iterator<Enchainable> it = enchs.iterator();
 		Enchainable ench;
 		while (it.hasNext()) {
 			ench = it.next();
 			if (ench.getExercice().equals(e)) {
 				ench.setExercice(null);
 				edao.save(ench);
 			}
 		}
 		this.dao.delete(e);
 		this.exercices.setWrappedData(this.dao.all());
 		return "goExLst";
 	}
 
 	public Exercice getExercice() {
 		return exercice;
 	}
 
 	public Exercice getExerciceEdit() {
 		return exerciceEdit;
 	}
 
 	public ListDataModel<Exercice> getExercices() {
 		return exercices;
 	}
 
 	@Override
 	public List<Exercice> getFiltered() {
 		return this.filtered;
 	}
 
 	public String goCreate() {
 		return "goCreateEx";
 	}
 
 	@Override
 	public void onCancel(RowEditEvent event) {
 
 	}
 
 	@Override
 	public void onEdit(RowEditEvent event) {
 		this.dao.save((Exercice) event.getObject());
 
 	}
 
 	public void setExercice(Exercice exercice) {
 		this.exercice = exercice;
 	}
 
 	public void setExerciceEdit(Exercice exerciceEdit) {
 		this.exerciceEdit = exerciceEdit;
 	}
 
 	public void setExercices(ListDataModel<Exercice> exercices) {
 		this.exercices = exercices;
 	}
 
 	@Override
 	public void setFiltered(List<Exercice> list) {
 		this.filtered = list;
 
 	}
 
 	@Override
 	public String update() {
 		this.dao.save(this.exerciceEdit);
 		this.exercices.setWrappedData(this.dao.all());
 		return "goExLst";
 	}
 
 	@Override
 	public String edit() {
 		this.exerciceEdit = (Exercice) this.exercices.getRowData();
 		return "goExMod";
 	}
 
 }
