 package yang.util.statesystem.statefading;
 
 import yang.events.eventtypes.YangEvent;
import yang.util.statesystem.StateSystemInterface;
 import yang.util.statesystem.YangProgramState;
 import yang.util.statesystem.YangProgramStateSystem;
 
 public class YangStateFadeOutIn<StateSystemType extends YangProgramStateSystem> extends YangStateFader<StateSystemType> {
 
 	public YangStateFadeOutIn(float transitionTime) {
 		super(transitionTime);
 	}
 
 	protected void prepareFade(YangProgramState<StateSystemType> state,float fade) {
 		mGraphics2D.setColorFactor(fade);
 	}
 
 	@Override
	public void onSet(StateSystemInterface stateSystem,int layer) {
		super.onSet(stateSystem, layer);
		if (mFromState == null) mFadeProgress = 0.5f;

	}

	@Override
 	protected boolean prepareStateDraw(YangProgramState<StateSystemType> state,float fadeProgress) {
		prepareFade(state,fadeProgress);
		return true;
 	}
 
 	@Override
 	protected void refreshProgress(float deltaTime,float fadeProgress) {
 		if(fadeProgress<0.5f) {
 			if(mFromState!=null) {
 				mFromState.mFadeProgress = 1-fadeProgress*2;
 				mFromState.proceed(deltaTime);
 			}
 		}else{
			if (mFromState != null) mFromState.mFadeProgress = 0;
 			if(mToState!=null) {
 				mToState.mFadeProgress = fadeProgress*2-1;
 				mToState.proceed(deltaTime);
 			}
 		}
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
		if(mFromState != null && (mToState==null || mFromState.mFadeProgress>0.5f))
 			return event.handle(mFromState);
 		else if(mFromState==null)
 			return event.handle(mToState);
 		else
 			return false;
 	}
 
 }
