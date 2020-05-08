 package cz.havlena.dictionary;
 
 public interface IDictionaryService {
 	public void onSearchStart();
 	public void onFoundElement(Object element);
	public void onSearchStop(boolean interupted);
 	public void onError(Exception ex);
 }
