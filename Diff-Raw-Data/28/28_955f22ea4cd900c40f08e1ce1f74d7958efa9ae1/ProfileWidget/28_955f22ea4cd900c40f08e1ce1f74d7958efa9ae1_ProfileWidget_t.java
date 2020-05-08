 package project.client.profile;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 import project.client.login.LoginInfo;
 import project.client.screen.ScreenWidget;
 
 public class ProfileWidget extends ScreenWidget {
 	private SliderWidget main = new SliderWidget("User Story");
 	private SliderWidget ePoint = new SliderWidget("Entry Point");
 	private SliderWidget sketch = new SliderWidget("Sketch/Impl");
 	private SliderWidget testCase = new SliderWidget("Test Case");
 	private SliderWidget unit = new SliderWidget("Unit Test");
 
 
 
 	private TextBox nameBox;
 	private Button cancelButton;
 
 	private ScreenWidget other;
 
 	private final int height=SliderWidget.getSliderHeight()+10;
 
 	public ProfileWidget(LoginInfo loginInfo, ScreenWidget other) {
 		super(loginInfo);
 		this.other=other;
 		setSize("1150px", "768px");
 		override();
 		UI();
 	}
 
 	@Override
 	public void UI() {
 		mainPanel.add(main);
 		mainPanel.add(ePoint);
 		mainPanel.add(sketch);
 		mainPanel.add(testCase);
 		mainPanel.add(unit);
		
		HorizontalPanel h=new HorizontalPanel();
		mainPanel.add(h);
 		Label l=new Label("Nickname is: ");
		h.add(l);
 		nameBox=new TextBox();
		//nameBox.setText("1");
		h.add(nameBox);
 		cancelButton=new Button("Cancel");
 		horizontalPanel.add(cancelButton);
 		cancelButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent c){
 				transfer();
 			}
 		});
 	}
 
 
 
 
 	@Override
 	public void submit() {
 		submitService.setNickname(nameBox.getText(), new AsyncCallback<String>(){
 			public void onFailure(Throwable s){
 				System.out.println("no change");
 			}
 			public void onSuccess(String s){
 				updatePoints();
 				System.out.println(s);
 
 			}
 		});
 	}
 
 	private void override(){
 		button.setText("Save");
 		remover.removeHandler();
 		remover=button.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent c){
 				submit();
 				transfer();
 			}
 		});
		prefs.removeFromParent();
 	}
 
 	private void transfer(){
 		RootPanel.get("ace").clear();
 
 		RootPanel.get("ace").add(other);
 	}
 
 
 }
