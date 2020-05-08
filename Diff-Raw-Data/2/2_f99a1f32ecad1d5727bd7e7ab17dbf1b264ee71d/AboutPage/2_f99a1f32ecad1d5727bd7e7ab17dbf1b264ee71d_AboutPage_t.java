 package com.redhat.latam.brms.about;
 
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.Model;
 
 import com.redhat.claro.engine.config.Configuration;
 import com.redhat.latam.brms.BasePage;
 
 public class AboutPage extends BasePage {
 
 	private static final long serialVersionUID = 3933761211141354959L;
 
 	public AboutPage() {
 
 		add(new TextField<String>("refresh", new Model<String>(Configuration.instance().get("drools.resource.scanner.interval"))));
 		add(new TextField<String>("usuario", new Model<String>(this.getNombreCliente())));
		add(new TextField<String>("changeset", new Model<String>(Configuration.instance().get("changeset"))));
 
 	}
 
 }
