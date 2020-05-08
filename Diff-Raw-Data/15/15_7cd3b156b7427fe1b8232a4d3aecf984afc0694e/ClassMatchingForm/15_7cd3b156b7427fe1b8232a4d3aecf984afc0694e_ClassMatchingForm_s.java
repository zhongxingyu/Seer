 package de.uni_leipzig.simba.saim.gui.widget;
 
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.VerticalLayout;
 
 import de.konrad.commons.sparql.PrefixHelper;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.saim.gui.widget.ClassChooser.ClassNode;
 
 /** The class matching consists of two sets of classes from both knowledge bases and a match between them.
  * In RAVEN, the mapping is injective and uniquely defined (there is at most one match for a class).
  * The user gets shown some classes and can add some manually or create the matching manually.*/
 public class ClassMatchingForm extends Form
 {	
 	protected final ComboBox field; 
 //	protected final TextField target; 
 	protected final ClassChooser chooser;
 	KBInfo info;
 	
 	public ClassMatchingForm(String caption, final KBInfo info) {
 		this.info = info;
 		Layout layout = new VerticalLayout();
 		layout.setWidth("100%"); //$NON-NLS-1$
 		setLayout(layout);
 		field = new ComboBox(caption);
 		field.setWidth("100%"); //$NON-NLS-1$
 		chooser = new ClassChooser(info.endpoint, info.id, info.graph);
 		
 		chooser.tree.addListener(new ItemClickListener() {	
 			@Override
 			public void itemClick(ItemClickEvent event) {
 				field.setValue(event.getItemId());
 				ClassNode node = (ClassNode) event.getItemId();
 				field.addItem(node.url);
 				field.setValue(node.url);
				String rest = info.var +" rdf:type "+PrefixHelper.abbreviate(node.url); //$NON-NLS-1$
 				info.restrictions.clear();
 				if(node.url != null) {
 					info.restrictions.add(rest);
					info.prefixes.put(PrefixHelper.getPrefixFromURI(PrefixHelper.abbreviate(node.url)), PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(PrefixHelper.abbreviate(node.url))));
					System.out.println("Setting class restiction to "+rest);
					System.out.println("...and adding prefix: "+PrefixHelper.abbreviate(node.url)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(PrefixHelper.abbreviate(node.url))));
 				}
 				else {
 					System.out.println("Class Matching Form:: Cannot set class restiction, due to  url=null aborting");
 				}
 			}
 		});
 		addField("textfield", field); //$NON-NLS-1$
 		this.getLayout().addComponent(chooser);
 	}
 	
 	/**
 	 * Method to add and select a value in the field.
 	 * @param uri
 	 */
 	public void addItem(Object uri, boolean select)
 	{
 		if(!field.containsId(uri)) {
 			field.addItem(uri);
 		}
 		if(select) {field.select(uri);}
 	}
 	
 }
