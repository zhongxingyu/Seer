 package org.cocoa4android.navigationsample;
 
 
 import org.cocoa4android.navigationsample.R;
 import org.cocoa4android.ui.UIButton;
 import org.cocoa4android.ui.UIColor;
 import org.cocoa4android.ui.UIControl.UIControlEvent;
 import org.cocoa4android.ui.UIControl.UIControlState;
 import org.cocoa4android.ui.UIImage;
 import org.cocoa4android.ui.UIImageView;
 import org.cocoa4android.ui.UIViewController;
 
 public class FirstViewController extends UIViewController {
 	@Override
 	protected void viewDidLoad() {
 		super.viewDidLoad();
 		this.setTitle("FirstView");
 		
 		UIImageView imageView = new UIImageView(UIImage.imageNamed(R.drawable.ic_launcher));
 		imageView.setCenter(CGPointMake(160, 200));
 		//this is very important
 		imageView.setKeepAspectRatio(YES);
 		this.view.addSubview(imageView);
 		
 		UIButton nextPageButton = new UIButton(CGRectMake(60, 240, 200, 50));
 		nextPageButton.setTitle("ClickMe");
 		nextPageButton.setTitleColor(UIColor.blackColor());
 		nextPageButton.setTitleColor(UIColor.grayColor(), UIControlState.UIControlStateHighlighted);
		nextPageButton.addTarget(this, "nextButtonClicked", UIControlEvent.UIControlEventTouchUpInside);
 		this.view.addSubview(nextPageButton);
 	}
 	
 	public void nextButtonClicked(){
 		SecondViewController viewController = new SecondViewController();
 		this.navigationController.pushViewController(viewController, YES);
 	}
 }
