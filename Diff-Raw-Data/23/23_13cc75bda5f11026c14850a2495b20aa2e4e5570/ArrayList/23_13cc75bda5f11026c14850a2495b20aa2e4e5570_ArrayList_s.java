 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package isad.w2;
 
 /**
  *
  * @author jappie
  */
 public class ArrayList<T> {
 	private T[] _items;
 	private int _index;
 	/**
 	 * the size the array expands to
 	 */
 	private int _nextSize;
 	public ArrayList(int size){
 		this._nextSize = size;
 		this.constructorMethods();
 	}
 	private void constructorMethods(){
 		this.clear();
 	}
 	
 	// toevoegen aan het einde van de lijst, mits niet vol
 	public void add(T n) throws Exception{
 		_index++;
 		if(_index == _items.length){
 			throw new Exception("array is to small");
 		}
 		_items[_index] = n;
 	}
 
 	// haal de waarde op van een bepaalde index
 	public T get(int index)throws Exception{
 
 		if(!indexExists(index)){
 			throw new Exception("trying to get non existing index");
 		}
 
 		return _items[index];
 	};
 
 	// wijzig een item op een bepaalde index
 	void set(int index, T n)throws Exception{
 		if(!indexExists(index)){
 			throw new Exception("trying to set non existing index");
 		} 
 		for(int i = _items.length -1; i != index; i--){
 			_items[i] = _items[i-1];
 			
 		}
 		_items[index] = n;
 		_index++;
 	};
 
 	// print de inhoud van de list
 	void print(){
 		for(int i = 0; i < _items.length; i++){
 			System.out.println(i + ": " + _items[i]);;
 		}
 	};
 
 	// maak de list leeg
 	public void clear(){
 		_index = -1;
 		_items = (T[]) new Object[_nextSize];		
 	} 
 
 	// tel hoe vaak het gegeven getal voorkomt
 	int countOccurences(T n){
 		int occurence = 0;
		for(int i = 0; i < _items.length; i++){
 
 			if(_items[i].equals(n)){
 				occurence++;
 			}
 		}
 		return occurence;
 	} 
 	
 	private boolean indexExists(int index){
 		return (0 <= index) && (index < _items.length);
 	}
 }
