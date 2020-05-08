 package edu.bu.cs673.AwesomeAlphabet.controller;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import edu.bu.cs673.AwesomeAlphabet.model.PageName;
 import edu.bu.cs673.AwesomeAlphabet.model.Theme;
 import edu.bu.cs673.AwesomeAlphabet.model.ThemeManager;
 import edu.bu.cs673.AwesomeAlphabet.view.IPageObserver;
 import edu.bu.cs673.AwesomeAlphabet.view.WordEditView;
 import edu.bu.cs673.AwesomeAlphabet.model.Alphabet;
 import edu.bu.cs673.AwesomeAlphabet.model.WordPictureSound;
 
 public class WordEditController extends PageController {
 
 	WordEditView m_view;
 	ThemeManager m_themeMgr;
 	Alphabet m_model;
 	
 	public WordEditController(IPageObserver pageObserver, WordEditView view, ThemeManager themeMgr, Alphabet model) {
 		super(pageObserver);
 		
 		m_view = view;
 		m_themeMgr = themeMgr;
 		m_model = model;
 	}
 
 	public Iterator<String> getThemeNamesIterator() {
 		Iterator<Theme> themes = m_themeMgr.getIterator();
 		LinkedList<String> themeNames = new LinkedList<String>();
 		Theme theme;
 		
 		while(themes.hasNext())
 		{
 			theme = themes.next();
 			themeNames.add(theme.getThemeName());
 		}
 		
 		return themeNames.iterator();
 	}
 
 	public void CancelEditWord() {
 		m_model.unsetCurrentWordEditing();
 		GoToPage(PageName.WPSPage);
 	}
 	
 	public void SaveNewWord(String wordText, char letter_c, String imageFile, String soundFile, String themeName) {
 		// TODO: m_model.commit();
 		m_model.addNewWord(wordText, letter_c, imageFile, soundFile, themeName);
 		GoToPage(PageName.WPSPage);
 	}
 	
	public void SaveEditWord(String wordText, char associated_letter, String imageFile, String soundFile, String themeName) {
 		// TODO: m_model.commit();
		m_model.editWord(wordText, associated_letter, imageFile, soundFile, themeName);
 		m_model.unsetCurrentWordEditing();
 		GoToPage(PageName.WPSPage);
 	}
 	
 	public WordPictureSound getCurrentWordEditing()
 	{
 		return m_model.getCurrentWordEditing();
 	}
 	
 	public String getAbsSoundFilePath(String wordText)
 	{
 		return m_model.getAbsSoundFilePath(wordText);
 	}
 	
 	public String getAbsImageFilePath(String wordText)
 	{
 		return m_model.getAbsImageFilePath(wordText);
 	}
 
 	public int getLetterIndex(char wordLetter) {
 		return m_model.GetLetterIndex(wordLetter);
 	}
 
 }
