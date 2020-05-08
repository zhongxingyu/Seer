 package  com.view.threeLevel;
 
 import java.util.Stack;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
import com.view.demo.BaseActivity;
 import com.view.demo.BaseFragment;
 import com.view.demo.BaseFragmentActivity;
 import com.view.demo.R;
 
 public class TestActivity extends BaseFragmentActivity implements OnClickListener{
 	Button mPre;
 	Button mNext;
 	TestFragment mTestFragment;
 	TestFragment mMainFragment;
 	Stack<BaseFragment> mStack = new Stack<BaseFragment>();
 	int i = 0;
 	ViewManager mViewManager;
 	ViewGroup mCoreView;
 			
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.threelevel_main);
 		
 //		mCoreView = (ViewGroup) findViewById(R.id.container);
 		mCoreView = (ViewGroup) getWindow().getDecorView();
 		mViewManager = ViewManager.getInstance();
 		mViewManager.setCoreView(mCoreView);
 		mViewManager.setFragmentManager(getFragmentManager());
 		
 		mPre = (Button) findViewById(R.id.pre);
 		mPre.setOnClickListener(this);
 		mNext = (Button) findViewById(R.id.next);
 		mNext.setOnClickListener(this);
 		
 		mMainFragment = new TestFragment(32);
 		mViewManager.showPage(mMainFragment, true);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onClick(View v) {
 		if(v == mPre){
 			if(!mStack.empty()){
 				mTestFragment = (TestFragment) mStack.pop();
 			}
 			showFragment(mTestFragment, false);
 		}else if(v == mNext){
 			
 			mTestFragment = new TestFragment(i);
 			i++;
 			if(i>3){
 				i = 0;
 			}
 			mStack.push(mTestFragment);
 			showFragment(mTestFragment, true);
 		}
 		
 	}
 	
 	private void showFragment(BaseFragment fragment, boolean saveToStack){
 		if(fragment == null) {
 			return ;
 		}
 		BasePopFragment popFragment = new BasePopFragment();
 		popFragment.setFragment(fragment);
 		mViewManager.showPage(popFragment, saveToStack);
 	}
 	
 }
