 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
  *   The vaadin-framework Infrastructure is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework;
 
 import static pt.ist.vaadinframework.annotation.EmbeddedComponentUtils.getAnnotation;
 import static pt.ist.vaadinframework.annotation.EmbeddedComponentUtils.getAnnotationPath;
 
 import java.util.EmptyStackException;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import pt.ist.vaadinframework.annotation.EmbeddedComponent;
 import pt.ist.vaadinframework.fragment.FragmentQuery;
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UriFragmentUtility;
 import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt), Sérgio Silva
  *         (sergio.silva@ist.utl.pt)
  */
 public class EmbeddedWindow extends Window {
     private final UriFragmentUtility fragmentUtility = new UriFragmentUtility();
 
     private Component current;
 
     private Class<? extends EmbeddedComponentContainer> currentType;
 
     private final Stack<String> history = new Stack<String>();
 
     private FragmentQuery currentQuery;
 
     private final Set<Class<? extends EmbeddedComponentContainer>> pages;
 
     public void setTypeForQuery() {
 	for (Class<? extends EmbeddedComponentContainer> page : pages) {
 	    final EmbeddedComponent annotation = getAnnotation(page);
 	    final String annotationPath = getAnnotationPath(annotation);
 	    if (currentQuery.getPath().equals(annotationPath)) {
 		currentType = page;
 		return;
 	    }
 	}
 	currentType = null;
     }
 
     public void open(String fragment) {
 	fragmentUtility.setFragment(fragment);
     }
 
     public void back() {
 	try {
 	    history.pop(); // consume current
 	    fragmentUtility.setFragment(history.pop());
 	} catch (EmptyStackException e) {
 	    // back fails quietly if no history is available
 	}
     }
 
     private void setFragment(String fragment) {
 	try {
 	    currentQuery = new FragmentQuery(fragment);
 	    setTypeForQuery();
 	} catch (Exception ife) {
 	    VaadinFrameworkLogger.getLogger().error("Fragment: " + fragment + " did not match any known page.");
 	    currentQuery = null;
 	}
     }
 
     public EmbeddedWindow(Set<Class<? extends EmbeddedComponentContainer>> pages) {
 	setImmediate(true);
 	this.pages = pages;
 	final VerticalLayout layout = new VerticalLayout();
 	layout.addComponent(fragmentUtility);
 	fragmentUtility.addListener(new UriFragmentUtility.FragmentChangedListener() {
 	    @Override
 	    public void fragmentChanged(FragmentChangedEvent source) {
 		String fragment = source.getUriFragmentUtility().getFragment();
 		history.push(fragment);
 		setFragment("#" + fragment);
 		refreshContent();
 	    }
 	});
 	setContent(layout);
 	addListener(new CloseListener() {
 	    @Override
 	    public void windowClose(CloseEvent e) {
		getContent().removeAllComponents();
		getContent().addComponent(fragmentUtility);
 		System.out.println("Closing embedded window!:" + e.getWindow().getName());
 	    }
 	});
     }
 
     public void refreshContent() {
 	try {
 	    getContent().removeAllComponents();
 	    getContent().addComponent(fragmentUtility);
 	    if (currentType != null && currentQuery != null) {
 		EmbeddedComponentContainer container = currentType.newInstance();
 		final Map<String, String> params = currentQuery.getParams();
 		container.setArguments(params);
 		getContent().addComponent(container);
 	    } else {
 		getContent().addComponent(new NoMatchingPatternFoundComponent());
 	    }
 	} catch (InstantiationException e) {
 	    VaadinFrameworkLogger.getLogger().error("Failed to load page: " + currentType.getName());
 	} catch (IllegalAccessException e) {
 	    VaadinFrameworkLogger.getLogger().error("Failed to load page: " + currentType.getName());
 	}
     }
 
     @Override
     public void attach() {
 	setLocale(Language.getLocale());
 	super.attach();
     }
 
     private class NoMatchingPatternFoundComponent extends VerticalLayout implements EmbeddedComponentContainer {
 	@Override
 	public void attach() {
 	    super.attach();
 	    addComponent(new Label("No matching component found"));
 	}
 
 	@Override
 	public void setArguments(Map<String, String> arguments) {
 	    // Not expecting any arguments
 	}
     }
 
     /**
      * @return the current
      */
     public Component getCurrent() {
 	return current;
     }
 
 }
