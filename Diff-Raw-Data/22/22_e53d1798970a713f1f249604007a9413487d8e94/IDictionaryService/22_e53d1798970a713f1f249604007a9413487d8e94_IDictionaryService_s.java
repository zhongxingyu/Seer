 package cz.havlena.dictionary;
 
 public interface IDictionaryService {
 	public void onSearchStart();
 	public void onFoundElement(Object element);
	public void onSearchStop();
 	public void onError(Exception ex);
 }
