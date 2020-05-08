 package de.uni_leipzig.simba.saim.gui.widget.window;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.terminal.FileResource;
 import com.vaadin.ui.Link;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import de.konrad.commons.sparql.PrefixHelper;
 import de.uni_leipzig.simba.data.Mapping;
 import de.uni_leipzig.simba.io.Serializer;
 import de.uni_leipzig.simba.io.SerializerFactory;
 import de.uni_leipzig.simba.saim.Messages;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 
 public class SerializationWindow extends Window {
 	
 	VerticalLayout mainLayout;
 	HashMap<String, Serializer> serializerNames;
 	Select serializerSelect;
 	Mapping mapping;
 	
 	public SerializationWindow(Mapping m) {
 		super();
 		mapping = m;
 		
 		mainLayout = new VerticalLayout();
 		this.setContent(mainLayout);
		setWidth("700px"); 
		setCaption(Messages.getString("downloadresults")); 
 		setModal(true);
 		
 		serializerSelect = getSerializerSelect();
 		//ttl, tab, nt
 		mainLayout.addComponent(serializerSelect);
 	}
 	
 	private Link getLinkToFile(Mapping m, Serializer serial, String fileEnding) {
 		Configuration config = Configuration.getInstance();
		String fileName   = ""; 
 		fileName += config.getSource().id+"_"+config.getTarget().id+fileEnding; 
 		serial.open(fileName);
 		String predicate = config.getLimesConfiReader().acceptanceRelation;
 		// print prefixes
 		System.out.println(config.getLimesConfiReader().prefixes);
 		serial.setPrefixes(config.getLimesConfiReader().prefixes);
 		for(String uri1 : m.map.keySet()) {
 			for(Entry<String, Double> e : m.map.get(uri1).entrySet()) {
 				serial.printStatement(PrefixHelper.expand(uri1), PrefixHelper.expand(predicate), PrefixHelper.expand(e.getKey()), e.getValue());
 			}
 		}
 		serial.close();
		return new Link(Messages.getString("downloadlinkspec"),new FileResource(new File(fileName), SAIMApplication.getInstance()));
 	}
 	
 	private Select getSerializerSelect() {
		Select select = new Select("Please select serialization format");
 		serializerNames = new HashMap<String, Serializer>();
		serializerNames.put("Turtle", SerializerFactory.getSerializer("ttl"));
		serializerNames.put("N3", SerializerFactory.getSerializer("N3"));
		serializerNames.put("Tab separated file", SerializerFactory.getSerializer("tab"));
 		for(String s : serializerNames.keySet())
 			select.addItem(s);
 		select.addListener(new SerializerSelectListener());
 		return select;
 	}
 	
 	private class SerializerSelectListener implements ValueChangeListener {
 		@Override
 		public void valueChange(ValueChangeEvent event) {
 			String key = event.getProperty().toString();
 			Serializer serial = serializerNames.get(key);
 			Link l = getLinkToFile(mapping, serial, serial.getName());
 			mainLayout.addComponent(l);
 		}
 		
 	}
 }
