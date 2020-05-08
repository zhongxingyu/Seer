 package yang.util.statesystem.statefading;
 
 import yang.events.eventtypes.YangEvent;
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
 	protected boolean prepareStateDraw(YangProgramState<StateSystemType> state,float fadeProgress) {
		if(fadeProgress>0.5f) {
			prepareFade(state,fadeProgress*2-1);
			return true;
		}else
			return false;
 	}
 
 	@Override
 	protected void refreshProgress(float deltaTime,float fadeProgress) {
 		if(fadeProgress<0.5f) {
 			if(mFromState!=null) {
 				mFromState.mFadeProgress = 1-fadeProgress*2;
 				mFromState.proceed(deltaTime);
 			}
 		}else{
 			if(mToState!=null) {
 				mToState.mFadeProgress = fadeProgress*2-1;
 				mToState.proceed(deltaTime);
 			}
 		}
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
		if(mToState==null || mFromState.mFadeProgress>0.5f)
 			return event.handle(mFromState);
 		else if(mFromState==null)
 			return event.handle(mToState);
 		else
 			return false;
 	}
 
 }
