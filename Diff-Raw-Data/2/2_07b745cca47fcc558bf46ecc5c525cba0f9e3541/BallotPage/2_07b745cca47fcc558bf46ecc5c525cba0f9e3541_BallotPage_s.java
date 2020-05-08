 package org.techhouse.shirts.display.web.pages;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.techhouse.shirts.data.entities.Design;
 import org.techhouse.shirts.data.entities.Member;
 import org.techhouse.shirts.data.query.SortParam;
 import org.techhouse.shirts.display.web.WicketApplication;
 import org.techhouse.shirts.display.web.behaviors.SetCssClassToWicketIdBehavior;
 import org.techhouse.shirts.display.web.components.VoteButton;
 import org.techhouse.shirts.display.web.security.AuthenticatedWebPage;
 import org.techhouse.shirts.display.web.security.WicketSession;
 import org.techhouse.shirts.service.VoteService;
 
 public class BallotPage extends BasePage implements AuthenticatedWebPage {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(BallotPage.class);
 	
 	@SpringBean
 	private VoteService voteService;
 	
 	private final IModel<Member> memberModel;
 	
 	private final IModel<List<Design>> designsModel;
 
 	private final Form<Member> ballotForm;
 	
 	public BallotPage() {
 		super();
 		
 		if(WicketApplication.get().isDevelopment()){
 			memberModel = Model.of(Member.findMember("ben"));
 		} else {
 			memberModel = Model.of(WicketSession.get().getMember()); 
 		}
 			
 		
 		designsModel = new LoadableDetachableModel<List<Design>>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected List<Design> load() {
				return Design.findAllDesigns(new SortParam("year", false), new SortParam("name", true)).subList(0, 5);
 			}
 		};
 		
 		ballotForm = new Form<Member>("ballotForm", memberModel) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit() {
 //				LOGGER.info("{} is now voting for {}.", getModelObject().getName(), StringUtils.join(getModelObject().getDesigns(), ", "));
 				setModelObject(voteService.submitBallot(getModelObject()));
 				
 //				LOGGER.info("Redirecting to BallotPage");
 //				setResponsePage(BallotPage.class);
 			}
 			
 		};
 		add(ballotForm);
 		
 //		LOGGER.info("{} is now voting for {}.", ballotForm.getModelObject().getName(), StringUtils.join(ballotForm.getModelObject().getDesigns(), ", "));
 		
 		ballotForm.add(new ListView<Design>("designListView", designsModel) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(final ListItem<Design> item) {
 				item.setModel(new CompoundPropertyModel<Design>(item.getModelObject()));
 				
 				item.add(new Image("thumbnail").add(new AttributeModifier("src", new PropertyModel<URL>(item.getModel(), "thumbnail"))));
 				item.add(new Label("name").add(new SetCssClassToWicketIdBehavior()));
 				item.add(new Label("artistAndYear", new AbstractReadOnlyModel<String>() {
 
 					private static final long serialVersionUID = 1L;
 					private final Set<Object> nullInACollection = Collections.singleton(null);
 
 					@Override
 					public String getObject() {
 						final List<Object> split = new ArrayList<Object>(2);
 						split.add(item.getModelObject().getArtist());
 						split.add(item.getModelObject().getYear());
 						split.removeAll(nullInACollection);
 						return StringUtils.join(split, ", ");
 					}
 				}).add(new SetCssClassToWicketIdBehavior()));
 				
 				item.add(new VoteButton("voteButton", new SelectedDesignModel(item.getModel())).add(new SetCssClassToWicketIdBehavior()));
 			}
 		});
 	}
 	
 	private final class SelectedDesignModel extends Model<Boolean> {
 
 		private static final long serialVersionUID = 1L;
 		private final IModel<Design> designModel;
 
 		public SelectedDesignModel(IModel<Design> designModel) {
 			this.designModel = designModel;
 		}
 
 		@Override
 		public Boolean getObject() {
 			Design object = designModel.getObject();
 			return memberModel.getObject().getDesigns().contains(object);
 		}
 
 		@Override
 		public void setObject(Boolean value) {
 			Design object = designModel.getObject();
 			Member member = ballotForm.getModelObject();
 			if(value){
 				LOGGER.info("Setting member to vote for design "+object.hashCode());
 				member.getDesigns().add(object);
 			} else {
 				LOGGER.info("Setting member to not vote for design "+object.hashCode());
 				member.getDesigns().remove(object);
 			}
 		}
 		
 	}
 }
