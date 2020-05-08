 package net.dontdrinkandroot.wicket.bootstrap.component.panel;
 
 import net.dontdrinkandroot.wicket.component.basic.Heading;
 import net.dontdrinkandroot.wicket.component.basic.Heading.Level;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.model.IModel;
 
 
 public abstract class StandardPanel<T> extends Panel<T> {
 
 	private final IModel<String> headingModel;
 
 	private final Level headingLevel;
 
 
 	public StandardPanel(String id, IModel<String> headingModel, Heading.Level headingLevel) {
 
 		super(id);
 
 		this.headingModel = headingModel;
 		this.headingLevel = headingLevel;
 	}
 
 
 	public StandardPanel(String id, IModel<T> model, IModel<String> headingModel, Heading.Level headingLevel) {
 
 		super(id, model);
 
 		this.headingModel = headingModel;
 		this.headingLevel = headingLevel;
 	}
 
 
 	@Override
 	protected Component createFooterComponent(String id) {
 
 		WebMarkupContainer footerContainer = new WebMarkupContainer(id);
 		footerContainer.setVisible(false);
 
 		return footerContainer;
 	}
 
 
 	@Override
 	protected Component createAfterBodyComponent(String id) {
 
 		WebMarkupContainer afterBodyContainer = new WebMarkupContainer(id);
 		afterBodyContainer.setVisible(false);
 
 		return afterBodyContainer;
 	}
 
 
 	@Override
 	protected Component createHeadingComponent(String id) {
 
 		return new PanelHeading(id, this.headingModel, this.headingLevel);
 	}
 
 }
