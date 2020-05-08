 /**
  * 
  */
 package org.tucana.web;
 
 import java.util.List;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.AbstractItem;
 import org.apache.wicket.markup.repeater.RepeatingView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.tucana.domain.Constellation;
 import org.tucana.service.ConstellationService;
 
 /**
  * @author kamann
  *
  */
 public class ConstellationList extends WebPage {
 	private static final long serialVersionUID = 894038876771030980L;
 	
 	@SpringBean
 	private ConstellationService constellationService;
 
 	/**
 	 * 
 	 */
 	public ConstellationList() {
 	}
 
 	/**
 	 * @param model
 	 */
 	public ConstellationList(IModel<?> model) {
 		super(model);
 	}
 
 	/**
 	 * @param parameters
 	 */
 	public ConstellationList(PageParameters parameters) {
 		super(parameters);
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see org.apache.wicket.Component#onInitialize()
 	 */
 	@Override
 	protected void onInitialize() {
 		super.onInitialize();
 		addRepeatingView();
 	}
 
 	private void addRepeatingView() {
 		RepeatingView repeatingView;
 
 		repeatingView = new RepeatingView("datatable");
 
 		int index = 0;
 		List<Constellation> constellations = constellationService.findAllConstellations();
 		//add(new Label("c_count", String.valueOf(constellations.size())));
 		
 		add(new Label("title", 
 				new StringResourceModel("content.title", null, 
 						new Object[]{String.valueOf(constellations.size())})));
 		
 		
 		
 		for (Constellation constellation : constellations) {
 			AbstractItem item = new AbstractItem(repeatingView.newChildId());
			item.add(new Label("c_code", constellation.getCode().toUpperCase()));
 			item.add(new Label("c_name", constellation.getName()));
 			item.add(new Label("c_gen_name", constellation.getGenitiveName()));
 
 			final int idx = index;
 			item.add(AttributeModifier.replace("class",
 					new AbstractReadOnlyModel<String>() {
 						private static final long serialVersionUID = 1L;
 
 						@Override
 						public String getObject() {
 							return (idx % 2 == 1) ? "even" : "odd";
 						}
 					}));
 
 			index++;
 			repeatingView.add(item);
 		}
 		add(repeatingView);
 	}
 
 }
