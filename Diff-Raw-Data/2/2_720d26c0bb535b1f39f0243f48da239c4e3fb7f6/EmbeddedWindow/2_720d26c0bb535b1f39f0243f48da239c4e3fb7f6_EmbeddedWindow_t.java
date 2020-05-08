 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework-ant.
  *
  *   The vaadin-framework-ant Infrastructure is free software: you can 
  *   redistribute it and/or modify it under the terms of the GNU Lesser General 
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework-ant is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework-ant. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework;
 
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UriFragmentUtility;
 import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  * 
  */
 public class EmbeddedWindow extends Window {
     private final UriFragmentUtility fragmentUtility = new UriFragmentUtility();
 
     private Component current;
 
     public EmbeddedWindow(final Map<Pattern, Class<? extends EmbeddedComponentContainer>> resolver) {
 	final VerticalLayout layout = new VerticalLayout();
 	layout.addComponent(fragmentUtility);
 	fragmentUtility.addListener(new UriFragmentUtility.FragmentChangedListener() {
 	    @Override
 	    public void fragmentChanged(FragmentChangedEvent source) {
 		String fragment = source.getUriFragmentUtility().getFragment();
 		for (Entry<Pattern, Class<? extends EmbeddedComponentContainer>> entry : resolver.entrySet()) {
 		    Matcher matcher = entry.getKey().matcher(fragment);
		    if (matcher.matches()) {
 			try {
 			    EmbeddedComponentContainer container = entry.getValue().newInstance();
 			    Vector<String> arguments = new Vector<String>(matcher.groupCount() + 1);
 			    for (int i = 0; i <= matcher.groupCount(); i++) {
 				arguments.add(matcher.group(i));
 			    }
 			    container.setArguments(arguments.toArray(new String[0]));
 			    layout.replaceComponent(current, container);
 			    current = container;
 			    return;
 			} catch (InstantiationException e) {
 			    VaadinFrameworkLogger.getLogger().error(
 				    "Embedded component resolver could not instantiate matched pattern: <"
 					    + entry.getKey().pattern() + ", " + entry.getValue().getName() + ">", e);
 			} catch (IllegalAccessException e) {
 			    VaadinFrameworkLogger.getLogger().error(
 				    "Embedded component resolver could not instantiate matched pattern: <"
 					    + entry.getKey().pattern() + ", " + entry.getValue().getName() + ">", e);
 			}
 		    }
 		}
 		Component container = new NoMatchingPatternFoundComponent();
 		layout.replaceComponent(current, container);
 		current = container;
 	    }
 	});
 	current = new VerticalLayout();
 	layout.addComponent(current);
 	setContent(layout);
     }
 
     private class NoMatchingPatternFoundComponent extends VerticalLayout implements EmbeddedComponentContainer {
 	@Override
 	public void attach() {
 	    super.attach();
 	    addComponent(new Label("No matching component found"));
 	}
 
 	@Override
 	public void setArguments(String... arguments) {
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
