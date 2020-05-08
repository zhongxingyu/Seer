 package cx.ath.jbzdak.jpaGui.ui.table;
 
 import cx.ath.jbzdak.jpaGui.Transaction;
 import static cx.ath.jbzdak.jpaGui.Utils.getId;
 import static cx.ath.jbzdak.jpaGui.Utils.isIdNull;
 import cx.ath.jbzdak.jpaGui.db.DBManager;
 import cx.ath.jbzdak.jpaGui.db.dao.DAO;
 import org.apache.commons.collections.functors.CloneTransformer;
 import org.jdesktop.observablecollections.ObservableCollections;
 import org.jdesktop.observablecollections.ObservableList;
 import org.jdesktop.swingbinding.JTableBinding;
 
 import javax.persistence.EntityManager;
 import javax.swing.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Nie jest to model w sensie modelu danych, ale model zarządzający persystencją wierszy
  * tabeli. Zawiera {@link EntityManager} na którym po każdej akcji wywołuje się {@link EntityManager#clear()},
  * w ten sposób można jednym {@link EntityManager} zarządzać wieloma encjami na raz i perzystować je od siebie niezależnie.
  * <br/>
  * Zasadniczo idea jest taka że {@link EditableTableModel} będzie współpracować z {@link JTableBinding},
  * i do synchronizacji {@link #entities} (które zwierają przechowywane encje) starczy sam fakt że {@link #entities}
  * jest instancją {@link ObservableList}.
  * <br/>
  * Sprawdzanie tego czy encja została edytowana odbywa się poprzez porównanie zawartości {@link #entities} i
  * {@link #entitiesCompare}.
  * Wywołanie {@link #setEntities(List)},
  * zapełnia klonami (patrz {@link #clone(Object)}), encji tabelę {@link #entitiesCompare}. Wartość jej wierszy
  * jest synchronizowana z {@link #entities} podczas wywoływania akcji: {@link #remove(Object)}, {@link #refresh(Object)},
  * {@link #save(Object)} (za pomocą metod {@link #setEntity(int, Object)}, {@link #addEntity(Object)},
  * {@link #removeEntity(int)}).
  * Samo porównywanie odbywa się za pomocą {@link #compare(Object, Object)}.
  * <br/>
  * {@link AkcjeRendererEditor} is class that is designed to accompany instances of this class, its an renderer/editor
  * containing buttons that fire apptopriate actions on this model. (see #createRendererEditor()}).
  *
  * TODO przerobić to kiedyś na 'prawdziwy' table model
  * TODO metoda dispose
  * @author jb
  *
  * @param <T>
  */
 @SuppressWarnings({"WeakerAccess"})
 public abstract class EditableTableModel<T> {
 
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
 
    protected final EntityManager manager;
 
    private final Class<T> clazz;
 
    protected final DBManager dbManager;
 
    protected final JTable table;
 
    protected final DAO<T> dao;
 
    protected boolean insertNewRow = true;
 
    protected ObservableList<T> entities = ObservableCollections.observableList(Collections.<T>emptyList());
 
    /**
     * Służy do sprawdzania czy jakaś encja zmieniła się.
     */
    private final ArrayList<T> entitiesCompare = new ArrayList<T>();
 
    private final boolean firingChanges = false;
 
    /**
     * Listener dodawany do instancji TableModel. Działa tak że jeśli wykrywa updejt jednej kolumny
     * odpala event o zmianie wszystkich
     */
    protected final TableModelListener modelListener = new TableModelListener(){
       @Override
       public void tableChanged(TableModelEvent e) {
          if(!firingChanges){
             if(e.getColumn()!=TableModelEvent.ALL_COLUMNS &&
                     e.getType() == TableModelEvent.UPDATE){
                table.tableChanged(new TableModelEvent((TableModel) e.getSource(),
                                                       e.getFirstRow(), e.getLastRow(),
                                                       TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
             }
          }
       }
    };
 
    public EditableTableModel(DBManager dbManager, Class<T> clazz, JTable table) {
       super();
       this.dbManager = dbManager;
       this.clazz = clazz;
       this.table = table;
       this.dao = dbManager.getDao(clazz);
       //dao.setRefreshType(RefreshType.FIND);
       dao.setAutoCreateEntity(false);
       manager = this.dbManager.createEntityManager();
       table.addPropertyChangeListener("model", new PropertyChangeListener(){
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
             if(evt.getOldValue()!=null){
                ((TableModel)evt.getOldValue()).removeTableModelListener(modelListener);
             }
             if(evt.getNewValue()!=null){
                ((TableModel)evt.getNewValue()).addTableModelListener(modelListener);
             }
          }
       });
       table.getModel().addTableModelListener(modelListener);
    }
 
    /**
     * Konkretnie sprawdza czy wszystkie pola zostały zapisane
     * @param idx
     * @return
     */
    public abstract boolean isEditingDone(int idx);
 
    /**
     * Sprawdza czy wiersz o danym indeksie powinien zostać podświetlony (jako zawierający zmiany
     * które trzeba zapisać do bazy danych)
     * @param idx
     * @return
     */
    public boolean isHighlighted(int idx){
       return (!isEditingDone(idx)) || maySave(idx);
    }
 
    /**
     * Sprwdza czy wiersz o danym indeksie był edytowany. Porównuje zawartość
     * @param idx
     * @return
     */
    protected boolean wasEdited(int idx){
       return compare(entities.get(idx), entitiesCompare.get(idx));
    }
 
    protected abstract boolean compare(T changed, T orig);
 
 
    /**
     * Sprawdza czy wiersz o danym indeksie może być zapisany. Różnica z {@link #isHighlighted(int)} jest taka
     * że {@link #isHighlighted(int)} zwraca <code>true</code> również gdy edycja nie jest zakończona.
     * @param idx
     * @return
     */
    public boolean maySave(int idx){
       if(isIdNull(entities.get(idx))){
          return isEditingDone(idx);
       }
       return wasEdited(idx) && isEditingDone(idx);
    }
 
    /**
     * Czy można odświerzyć dany wiersz.
     * @param idx
     * @return
     */
    public boolean mayRefresh(int idx){
       return !isIdNull(entities.get(idx)) && wasEdited(idx);
    }
 
    /**
     * Czy można usunąć dany wiersz
     * @param idx
     * @return
     */
    public boolean mayDelete(int idx){
       return !isIdNull(entities.get(idx));
    }
 
    /**
     * Usuwa encję. Encja musi pochodzić ż {@link #getEntities()}
     * @param t
     */
    public void remove(final T t){
       dao.setEntity(t);
       removeEntry(t, manager);
       dao.remove();
       removeEntity(entities.indexOf(t));
       manager.clear();
    }
 
    /**
     * Wywołane tuż przed usunięciem <code>t</code>.
     * @param t encja która jest już zarządzana przez <code>manager</code>.
     * @param manager
     */
    protected abstract void removeEntry(T t, EntityManager manager);
 
 
    /**
     * Używane do kolonowania instancji t. Uzywa {@link CloneTransformer}.
     * @param t
     * @return
     */
    protected T clone(T t ){
       return clazz.cast(CloneTransformer.INSTANCE.transform(t));
    }
 
    /**
     * Dodaje encję do {@link #entities} i {@link #entitiesCompare}
     * @param t
     */
    public void addEntity(T t){
       entities.add(t);
       entitiesCompare.add(clone(t));
    }
 
    /**
      * Dodaje encję do {@link #entities} i {@link #entitiesCompare}
      * @param col
      */
     public void addAll(Collection<T> col){
       for(T t : col){
          addEntity(t);
       }
    }
 
 
    /**
     * Ustawia zawartość wiersza ii w tabeli. (
     * @param ii
     * @param t
     */
    protected void setEntity(int ii, T t){
       entities.set(ii, t);
       entitiesCompare.set(ii, clone(t));
       TableModelEvent evt = new TableModelEvent(table.getModel(), ii, ii, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
       table.tableChanged(evt);
    }
 
    /**
     * Usuwa wartość wiersza ii w tabeli
     * @param ii
     */
    protected void removeEntity(int ii){
       entities.remove(ii);
       entitiesCompare.remove(ii);
    }
 
    /**
     * Zapisuje encję. Encja musi pochodzić z {@link #getEntities()}
     * @param t
     */
    public void save(final T t){
       if (insertNewRow && !isIdNull(entities.get(entities.size() - 1))) {
          addEntity(createNewEntity());
       }
       if(isIdNull(t)){
         persistingEntityEntry(t, manager);
       }
       Transaction.execute(manager, new Transaction() {
          @Override
          public void doTransaction(EntityManager entityManager) throws Exception {
             preMergeEntry(t, entityManager);
          }
       });
       int index = entities.indexOf(t);
       dao.setEntity(t);
       dao.persistOrUpdate();
       if(index!=-1){ //Żeby do entietesCompare trafiła wersja zustawionym id jeśli było odpalone persist
          setEntity(index, dao.getEntity());
       }
       manager.clear();
    }
 
    /**
     * Wywoływane przez {@link #save(Object)} zawsze przed zapisaniem od bazy danych t.
     * @param t encja która jest już zarządzana przez <code>manager</code>.
     * @param entityManager
     */
    protected abstract void preMergeEntry(T t, EntityManager entityManager);
 
    /**
     * Wywoływane przez {@link #save(Object)} jeśli ta metoda stwierdzi że <code>t</code> będzie
     * zapiywane po raz pie
     * @param t encja która jest już zarządzana przez <code>manager</code>.
     * @param manager
     */
    protected abstract void persistingEntityEntry(T t, EntityManager manager);
 
    /**T
     * Tworzy nową instancję T. Domyslna implementacja korzysta z {@link Class#newInstance()}.
     * @return nową instancję T
     */
    protected T createNewEntity(){
       try {
          return clazz.newInstance();
       } catch (RuntimeException e) {
          throw e;
       } catch (Exception e) {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Odświerza t z bazy danych. Dokładniej: powoduje że odświerzona wersja t wyświetli się w bazie danych.
     * Nie odświerza samego obiektu <code>t</code>.
     * @param t
     */
    public void refresh(final T t){
       manager.clear();
       dao.find(getId(t));
       setEntity(entities.indexOf(t), dao.getEntity());
       dao.clearEntity();
       manager.clear();
    }
 
    public AkcjeRendererEditor<T> createRendererEditor(){
       return new AkcjeRendererEditor<T>(clazz, this);
    }
 
    public HighlightCellRenderer createHighlightRenderer(){
       return new SimpleHighlightRenderer(){
          private static final long serialVersionUID = 1L;
 
          @Override
          protected boolean isHighlighted(JTable table, Object value,
                                          boolean isSelected, boolean hasFocus, int row, int column) {
             return EditableTableModel.this.isHighlighted(row);
          }
       };
    }
 
    public boolean isInsertNewRow() {
       return insertNewRow;
    }
 
    public void setInsertNewRow(boolean insertNewRow) {
       this.insertNewRow = insertNewRow;
    }
 
    public List<T> getEntities() {
       //return BeansbindingUtils.observableUnmodifiableList(entities);
       return  entities;
    }
 
    public void setEntities(List<T> entities) {
       List<T> old = this.entities;
       this.entities = ObservableCollections.observableList(new ArrayList<T>(entities));
       entitiesCompare.clear();
       manager.clear();
       for(T t : entities){
          entitiesCompare.add(clone(t));
       }
       if(insertNewRow){
          addEntity(createNewEntity());
       }
       boolean added = false;
       if(this.entities.size()==0){
          this.entities.add(createNewEntity());
          added = true;
       }
       support.firePropertyChange("entities", old, this.entities);
       if(added){
          this.entities.remove(0);
       }
 
    }
 
    public void addPropertyChangeListener(PropertyChangeListener listener) {
       support.addPropertyChangeListener(listener);
    }
 
 
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
       support.addPropertyChangeListener(propertyName, listener);
    }
 
 
    public PropertyChangeListener[] getPropertyChangeListeners() {
       return support.getPropertyChangeListeners();
    }
 
 
    public PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
       return support.getPropertyChangeListeners(propertyName);
    }
 
 
    public boolean hasListeners(String propertyName) {
       return support.hasListeners(propertyName);
    }
 
 
    public void removePropertyChangeListener(PropertyChangeListener listener) {
       support.removePropertyChangeListener(listener);
    }
 
 
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener) {
       support.removePropertyChangeListener(propertyName, listener);
    }
 
 
 
    protected PropertyChangeSupport getSupport() {
       return support;
    }
 
    protected DBManager getDbManager() {
       return dbManager;
    }
 
    protected EntityManager getManager() {
       return manager;
    }
 
 
    protected List<T> getEntitiesCompare() {
       return Collections.unmodifiableList(entitiesCompare);
    }
 
    protected Class<T> getClazz() {
       return clazz;
    }
 
 
 }
