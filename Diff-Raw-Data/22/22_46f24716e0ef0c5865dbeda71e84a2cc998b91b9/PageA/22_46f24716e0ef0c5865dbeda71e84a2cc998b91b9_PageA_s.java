 package de.flapdoodle.bugs;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 public class PageA extends WebPage {
 
 	public PageA() {
 		
		IModel<Integer> model=Model.of(0);
 		add(new Label("count",model));
 		
 		add(new Link<Integer>("link",model) {
 			@Override
 			public void onClick() {
 				setModelObject(1+getModelObject());
 			}
 		});
 	}
 }
