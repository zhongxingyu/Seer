 package de.flapdoodle.bugs;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 public class PageA extends WebPage {
 
 	public PageA() {
		this(0);
	}
	
	public PageA(int start) {
 		
		IModel<Integer> model=Model.of(start);
 		add(new Label("count",model));
 		
 		add(new Link<Integer>("link",model) {
 			@Override
 			public void onClick() {
 				setModelObject(1+getModelObject());
				setResponsePage(new PageA(getModelObject()));
 			}
 		});
 	}
 }
