 /*
  *    This program is free software; you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation; either version 2 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program; if not, write to the Free Software
  *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 /*
  *  
 *    Copyright (C) 2009 - 2010 
  *    							University of West Bohemia, 
  *                  Department of Computer Science and Engineering, 
  *                  Pilsen, Czech Republic
  */
 package ch.ethz.origo.juigle.application.observers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.ethz.origo.juigle.application.ILanguage;
 import ch.ethz.origo.juigle.application.exception.JUIGLELangException;
 
 /**
  * Observar pattern who points his listeners to switch language.
  * 
  * @author Vaclav Souhrada
 * @version 0.1.3 (3/20/2011)
  * @since 0.1.0 (10/15/2010)
  * @see AbstractJUIGLEObservable
  * 
  */
 public class LanguageObservable extends AbstractJUIGLEObservable {
 
 	private static final int DEFAULT_STATE = -1;
 
 	public static final int MSG_LANGUAGE_CHANGED = 1;
 
 	private static LanguageObservable instance;
 
 	private int state;
 
 	private List<ILanguage> listOfObservers = new ArrayList<ILanguage>();
 
 	public LanguageObservable() {
 		this.state = LanguageObservable.DEFAULT_STATE;
 	}
 
 	/**
 	 * Return instance of this class. (Singleton)
 	 * 
 	 * @return
 	 * @version 0.1.0
 	 * @since 0.1.0
 	 */
 	public static synchronized LanguageObservable getInstance() {
 		if (instance == null) {
 			instance = new LanguageObservable();
 		}
 		return instance;
 	}
 
 	@Override
 	protected void clearChanged() {
 		this.state = LanguageObservable.DEFAULT_STATE;
 
 	}
 
 	@Override
 	public Object getState() {
 		return state;
 	}
 
 	@Override
 	protected void notifyObserver(IObserver observer) {
 
 	}
 
 	@Override
 	protected void notifyObservers() {
 		for (ILanguage observer : listOfObservers) {
 			try {
 				observer.setLocalizedResourceBundle(observer.getResourceBundlePath());
 				observer.updateText();
 			} catch (JUIGLELangException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		clearChanged();
 	}
 
 	@Override
 	public void setState(int state) {
 		this.state = state;
 		notifyObservers();
 	}
 
 	@Override
 	public void attach(IObserver observer) {

 	}
 
 	public void attach(ILanguage observer) {
 		listOfObservers.add(observer);
 	}
 
 	@Override
 	public int countObservers() {
 		return listOfObservers.size();
 	}
 
 	@Override
 	public void detach(IObserver observer) {
 		listOfObservers.remove(observer);
 	}
 
 	/**
 	 * NOT IMPLEMENTED FOR THIS CLASS
 	 */
 	@Override
 	protected void notifyObserver(Object obj) {
 		/* NOT IMPLEMENTED */
 	}
 
 }
