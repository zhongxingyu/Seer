 package logbook.client.a_nonroo.app.client.ui;
 
 import logbook.client.managed.proxy.SkillProxy;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SkillLevelCheckboxViewImpl extends Composite implements SkillLevelCheckboxView{
 
 	private static final SkillLevelCheckboxViewImpllUiBinder uiBinder = GWT.create(SkillLevelCheckboxViewImpllUiBinder.class);
 	
 	interface SkillLevelCheckboxViewImpllUiBinder extends UiBinder<Widget, SkillLevelCheckboxViewImpl> {
 	}
 	
 	private Delegate delegate;
 
 	private presenter presenter;
 	
 	private SkillLevelCheckboxViewImpl skillLevelCheckboxViewImpl;
 	
 	private int classificationTopicRow=0;
 	
 	public int getClassificationTopicRow() {
 		return classificationTopicRow;
 	}
 
 	public void setClassificationTopicRow(int classificationTopicRow) {
 		this.classificationTopicRow = classificationTopicRow;
 	}
 
 	public int getTopicRow() {
 		return topicRow;
 	}
 
 	public void setTopicRow(int topicRow) {
 		this.topicRow = topicRow;
 	}
 
 	public int getMainClassificationRow() {
 		return mainClassificationRow;
 	}
 
 	public void setMainClassificationRow(int mainClassificationRow) {
 		this.mainClassificationRow = mainClassificationRow;
 	}
 
 	private int topicRow=0;
 	
 	private int mainClassificationRow=0;
 	
 	@UiField
 	CheckBox checkbox;
 	
 	public CheckBox getCheckbox() {
 		return checkbox;
 	}
 
 	public void setCheckbox(CheckBox checkbox) {
 		this.checkbox = checkbox;
 	}
 
 	@UiHandler("checkbox")
 	public void checkboxSelected(ValueChangeEvent<Boolean> event){
 		//Window.alert("" +event.getValue() + "gwt skill :" + skillProxy.getId());
 		delegate.chekBoxSelected(skillProxy,isLevel1,skillLevelCheckboxViewImpl);
 		
 	}
 	
 	private SkillProxy skillProxy;
 	
 	public SkillProxy getSkillProxy() {
 		return skillProxy;
 	}
 	
 	public boolean isLevel1=false;
 	public boolean isLevel1() {
 		return isLevel1;
 	}
 
 	public void setLevel1(boolean isLevel1) {
 		this.isLevel1 = isLevel1;
 	}
 
 	public void setSkillProxy(SkillProxy skillProxy) {
 		this.skillProxy = skillProxy;
 	}
 
 	public int row; 
 	
 	public int getRow() {
 		return row;
 	}
 
 	public void setRow(int row) {
 		this.row = row;
 	}
 
 	public int column;
 	
 	public int getColumn() {
 		return column;
 	}
 
 	public void setColumn(int column) {
 		this.column = column;
 	}
 
 	public SkillLevelCheckboxViewImpl()
 	{
 		skillLevelCheckboxViewImpl=this;
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		
 	}
 
 	@Override
 	public void setPresenter(presenter presenter) {
 		this.presenter = presenter;
 		
 	}
 
 	@Override
 	public void setDelegate(Delegate delegate) {
 		this.delegate = delegate;
 		
 	}
 	
 	
 }
