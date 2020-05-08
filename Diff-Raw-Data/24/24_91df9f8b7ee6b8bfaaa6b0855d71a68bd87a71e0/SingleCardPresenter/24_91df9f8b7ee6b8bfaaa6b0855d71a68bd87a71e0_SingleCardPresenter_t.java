 package be.betty.gwtp.client.presenters;
 
 import be.betty.gwtp.client.Filter_kind;
 import be.betty.gwtp.client.Storage_access;
 import be.betty.gwtp.shared.Constants;
 
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.google.inject.Inject;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class SingleCardPresenter extends
 PresenterWidget<SingleCardPresenter.MyView> {
 
 	public interface MyView extends View {
 
 		VerticalPanel getVerticalPanel();
 
 		HTML getHeader();
 	}
 
 	private int teacherId;
 	private int groupId;
 	private String group;
	private String course;
 	private String teacher;
 	private boolean isPlaced;
 
 	@Inject
 	public SingleCardPresenter(final EventBus eventBus, final MyView view) {
 		super(eventBus, view);
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
 	}
 
 	public void init(int myI) {
 		String c = Storage_access.getCard(myI);
 		
		group = Storage_access.getGroupCard(c);
		teacher = Storage_access.getTeacherCard(c);
		course = Storage_access.getCourseCard(c);
		

		getView().getHeader().setText(course+ " T="+teacher);
 
 	}
 
 	/**
 	 * Not used, but, in a proper version, should be.
 	 * @param filter_kind
 	 * @return
 	 */
 	public int getKindId(Filter_kind filter_kind) {
 		switch (filter_kind) {
 		case TEACHER: return teacherId;
 		case GROUP: return groupId;
 		}
 		return 0;
 	}
 
 	public String getKindString(Filter_kind filter_kind) {
 		switch (filter_kind) {
 		case TEACHER: return teacher;
 		case GROUP: return group;
 		}
 		return "";
 	}
 
 	public boolean isPlaced() {
 		//idealement, faudrait juste verifier dans le local storage
 		return isPlaced;
 	}
 
 	public void setPlaced(boolean isPlaced) {
 		//idealement, faudrait renvoyer la rponse du localStorage
 		this.isPlaced = isPlaced;
 	}
 }
