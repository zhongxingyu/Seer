 /**
  * 
  */
 package utility;
 
 import java.io.Serializable;
 
 import erp.Erp;
 
 /**
  * @author sirde
  * 
  */
 public class LinkedList<T_LinkedList> implements Serializable
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 607643732520894909L;
 
 	// ----------------------------------------------------------------------------
 	/**
 	 * Class Cellule interne de la class List
 	 */
 
	class Cell<T_Cell> implements Serializable, Cloneable
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 7412202956505489139L;
 		private T_Cell content;
 		private Cell<T_Cell> next;
 
 		protected Cell(T_Cell content, Cell<T_Cell> next)
 		{
 			this.content = content;
 			this.next = next;
 		}
		
		public Cell(Cell<T_Cell> objectToCopy){
			this.content = objectToCopy.content;
			this.next = objectToCopy.next;
		}
		
		public Cell<T_Cell> clone(){
			return new Cell<T_Cell>(this);
		}
 	}
 
 	// ----------------------------------------------------------------------------
 
 	// Donnes prives
 	// -----------------------------------------------------------
 
 	private Cell<T_LinkedList> firstCell;
 	private int size;
 
 	// Mthodes publiques
 	// --------------------------------------------------------
 	/**
 	 * Constructeur.
 	 */
 	public LinkedList()
 	{
 		firstCell = null;
 		size = 0;
 	}
 
 	/**
 	 * Predicat : Retourne true si la liste est vide, false sinon.
 	 * 
 	 * @return true if the list is empty
 	 */
 	public boolean isEmpty()
 	{
 		return (firstCell == null);
 	}
 
 	/**
 	 * Ajoute l'lement en fin de liste
 	 * 
 	 * @param elem
 	 */
 	public void add(T_LinkedList elem)
 	{
 		Cell<T_LinkedList> newCellule = new Cell<>(elem, null);
 
 		if (firstCell == null)
 		{
 			firstCell = newCellule;
 			size++;
 		}
 		else
 		{
 			Cell<T_LinkedList> a = firstCell;
 
 			while (a.next != null)
 			{
 				a = a.next;
 			}
 
 			a.next = newCellule;
 			size++;
 		}
 
 	}
 
 	/**
 	 * @param index
 	 * @return the employee a the chosen index
 	 */
 	public T_LinkedList get(int index)
 	{
 		Cell<T_LinkedList> a = firstCell;
 		int i = 1;
 		while (i < index && a.next != null)
 		{
 			a = a.next;
 			i++;
 		}
		if (index == i) return a.clone().content;
 		else return null;
 	}
 
 	/**
 	 * Delete the element at the chosen index
 	 * 
 	 * @param index
 	 */
 	public void delete(int index)
 	{
 
 		if (index == 1 && firstCell != null)
 		{
 			firstCell = firstCell.next;
 			size--;
 			if (Erp.DEBUG) System.out.println("First cell has been deleted");
 		}
 		else
 		{
 			Cell<T_LinkedList> previousCell = firstCell;
 			Cell<T_LinkedList> deletedCell = firstCell.next;
 
 			int i = 2;
 
 			while (i < index && deletedCell.next != null)
 			{
 				previousCell = deletedCell;
 				deletedCell = deletedCell.next;
 
 				i++;
 			}
 			if (index == i)
 			{
 				previousCell.next = deletedCell.next;
 				size--;
 				if (Erp.DEBUG) System.out.println("Cell " + i
 						+ " has been deleted");
 			}
 			else
 			{
 				if (Erp.DEBUG) System.out.println("No cell has been deleted");
 
 			}
 		}
 
 	}
 
 	/**
 	 * Replace the element at the chosen index
 	 * 
 	 * @param index
 	 * @param elem
 	 */
 	public void replace(int index, T_LinkedList elem)
 	{
 
 		Cell<T_LinkedList> a = firstCell;
 
 		int i = 1;
 
 		while (index != i && a.next != null)
 		{
 			a = a.next;
 			i++;
 		}
 		if (index == i) a.content = elem;
 	}
 
 	/**
 	 * @return the number of elements
 	 */
 	public int getSize()
 	{
 		return size;
 	}
 }
