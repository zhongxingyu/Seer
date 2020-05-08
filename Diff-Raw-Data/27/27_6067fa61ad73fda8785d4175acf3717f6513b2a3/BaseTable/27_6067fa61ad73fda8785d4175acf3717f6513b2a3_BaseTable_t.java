 /**
  * package model.entity in MABIS
  * by kornicameister
  */
 package model;
 
 import java.io.Serializable;
 
 import model.entity.AudioAlbum;
 import model.entity.Book;
 import model.entity.Movie;
 import model.enums.TableType;
 import model.interfaces.Table;
 import model.utilities.ForeignKey;
 
 /**
  * Klasa {@link BaseTable} będąc klasą abstrakcyjną dostarcza podstawową
  * funkcjonalność jaką powinna reprezentować tabela relacyjnej bazy danych.
  * Implementując interfejs {@link Serializable} daje możliwość zapisu obiektów
  * do bazy danych bez konieczności rozbijania operacji na operacje elementarne
  * na poszczególnych atrybutach danej tabeli.
  * 
  * @author kornicameister
  * @version 0.3
  */
 public abstract class BaseTable implements Table, Comparable<BaseTable>,
 		Serializable {
 	private static final long serialVersionUID = 8934017748567797527L;
	private Integer primaryKey;
	protected String[] titles;
 	protected TableType tableType;
 
 	/**
 	 * Empty constructor, only initializes variables, used when new entity
 	 * object was created and which is at the moment not in the database
 	 */
 	public BaseTable() {
 		this.initFields();
 		this.initInternalFields();
 	}
 
 	/**
 	 * Constructs BaseTable with title in it's original language
 	 * 
 	 * @param originalTitle
 	 */
 	public BaseTable(String originalTitle) {
 		this.initFields();
 		this.initInternalFields();
 		this.titles[0] = originalTitle;
 	}
 
 	/**
 	 * Constructs an Table with and primaryKey
 	 * 
 	 * @param pk
 	 *            if Table is new object that is about to be inserted into DB,
 	 *            pk (<b>primary key</b>) should be set to < 0 value otherwise
 	 *            primaryKey represents actual identifier of row
 	 */
 	public BaseTable(int pk) {
 		this.initFields();
 		this.initInternalFields();
 		this.primaryKey = pk;
 	}
 
 	/**
 	 * 
 	 * @param pk
 	 *            if Table is new object that is about to be inserted into DB,
 	 *            pk (<b>primary key</b>) should be set to < 0 value otherwise
 	 *            primaryKey represents actual identifier of row
 	 * @param key
 	 *            represents author's primary key in context of foreign key for
 	 *            Table.</br> This key context can be different depends on which
 	 *            extending class object was created. In case of:
 	 *            <ul>
 	 *            <li> {@link AudioAlbum} - this will refer to band table primary
 	 *            key</li>
 	 *            <li> {@link Book} - this will refer to writer table primary key
 	 *            </li>
 	 *            <li> {@link Movie} - this will refer to director table primary
 	 *            key</li>
 	 *            </ul>
 	 * @see ForeignKey
 	 */
 	public BaseTable(int pk, ForeignKey... keys) {
 		this.initFields();
 		this.initInternalFields();
 	}
 
 	/**
 	 * intializes all fields of this class
 	 */
 	private void initFields() {
 		this.primaryKey = new Integer(-1);
 		this.titles = new String[4];
		this.tableType = TableType.NULL;
 	}
 
 	/**
 	 * overridden by extending class, called always as triggering call is
 	 * located in {@link BaseTable} constructor takes care of initializing all
 	 * internal fields that belongs to extending classes
 	 */
 	protected abstract void initInternalFields();
 
 	/**
 	 * @return title of a table
 	 */
 	public TableType getTableType() {
 		return tableType;
 	}
 
 	/**
 	 * @return an array of titles, first title is originalTitle, the second one
 	 *         referes to translated title
 	 */
 	public String[] getTitles() {
 		String t[] = new String[2];
 		t[0] = this.titles[0];
 		t[1] = this.titles[1];
 		return t;
 	}
 
 	/**
 	 * set original title
 	 * 
 	 * @param t
 	 */
 	public void setTitle(String t) {
 		this.titles[0] = t;
 	}
 
 	/**
 	 * set localized title
 	 * 
 	 * @param t
 	 */
 	public void setSubTitle(String t) {
 		this.titles[1] = t;
 	}
 
 	/**
 	 * @return title in it's original language
 	 */
 	public String getTitle() {
 		return this.titles[0];
 	}
 
 	/**
	 * @return subtitle of this collection's item
 	 */
 	public String getSubtitle() {
 		return this.titles[1];
 	}
 
 	@Override
 	public Integer getPrimaryKey() {
 		return this.primaryKey;
 	}
 
 	@Override
 	public void setPrimaryKey(Integer id) {
 		this.primaryKey = id;
 	}
 
 	@Override
 	public String toString() {
 		String str = "Table: " + tableType + "\n";
 		str += "[PK: " + this.primaryKey + "]\n";
 		return str;
 	}
 
 	/**
 	 * Metoda zwraca wektor opisujący kolumny w tabeli danych
 	 * 
 	 * @return wektor z opisem kolumn
 	 */
	public Object[] toColumnIdentifiers() {
		Object data[] = { "ID" };
 		return data;
 	}
 
 	/**
 	 * zwraca tablicę obiektów klasy Object, który stanowi opis elementu
 	 * kolekcji do wykorzystania w tabeli danych JTable
 	 * 
 	 * @return tablicę danych Object
 	 */
	public Object[] toRowData() {
		Object data[] = { this.primaryKey };
 		return data;
 	}
 
 	/**
 	 * Domyślny komparator, który porównuje po nazwie tabeli. Jeśli tabele są
 	 * identyczne, sprawdza klucze główne.
 	 */
 	@Override
 	public int compareTo(BaseTable o) {
 		int compareValue = 0;
 		compareValue = this.getTableType().compareTo(o.getTableType());
 		if (compareValue == 0) {
 			// the same table, lets check primary keys
 			compareValue = this.getPrimaryKey().compareTo(o.getPrimaryKey());
 		}
 		return compareValue;
 	}
 }
