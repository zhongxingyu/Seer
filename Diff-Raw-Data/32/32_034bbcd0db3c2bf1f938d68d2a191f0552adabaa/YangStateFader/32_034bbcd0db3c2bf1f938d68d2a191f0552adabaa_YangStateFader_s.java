 package yang.util.statesystem.statefading;
 
 import yang.util.statesystem.StateSystemInterface;
 import yang.util.statesystem.YangProgramState;
 import yang.util.statesystem.YangProgramStateSystem;
 
 public abstract class YangStateFader<StateSystemType extends YangProgramStateSystem> extends YangProgramState<StateSystemType> {
 
 	protected YangProgramState<StateSystemType> mFromState = null, mToState = null;
 	public float mTransitionTime = 1;
 	protected float mFadeProgress = 0;
 
 	public YangStateFader(float transitionTime) {
 		mTransitionTime = transitionTime;
 	}
 
 	public YangStateFader() {
 		this(1);
 	}
 
 	protected abstract void refreshProgress(float deltaTime,float toWeight);
 	protected abstract boolean prepareStateDraw(YangProgramState<StateSystemType> state,float fade);
 
 	protected void preStateDraw() { }
 	protected void postStateDraw() { }
 
 	public YangStateFader<StateSystemType> setTargetState(YangProgramState<StateSystemType> toState) {
 		mFromState = null;
 		mToState = toState;
 		return this;
 	}
 
 	@Override
 	protected void step(float deltaTime) {
 		mFadeProgress += deltaTime/mTransitionTime;
 		if(mFadeProgress>1) {
 			mFadeProgress = 1;
 			if(mFromState!=null)
 				mFromState.mFadeProgress = 1;
 			if(mToState!=null) {
 				mToState.mFadeProgress = 1;
 				mToState.onFadeInFinished();
 				mToState.getParentStateSystem().setStateNoStart(getStateSystemLayer(), mToState);
 			} else {
 				mFromState.getParentStateSystem().setStateNoStart(getStateSystemLayer(), null);
 			}
 		}else{
 			refreshProgress(deltaTime,mFadeProgress);
 		}
 	}
 
 	@Override
 	protected void draw() {
 		preStateDraw();
		if(mFromState!=null) {
 			if(prepareStateDraw(mFromState,mFromState.mFadeProgress)) {
 				mFromState.preDrawFrame();
 				mFromState.drawFrame();
 			}
 		}
		if(mToState!=null) {
 			if(prepareStateDraw(mToState,mToState.mFadeProgress)) {
 				mToState.preDrawFrame();
 				mToState.drawFrame();
 			}
 		}
 		postStateDraw();
 	}
 
 	@Override
 	public void onSet(StateSystemInterface stateSystem,int layer) {
 		mFadeProgress = 0;
 		mFromState = stateSystem.getCurrentState(layer);
 		if(mFromState!=null) {
 			if(!mFromState.isInitialized())
 				mFromState.init(mStateSystem);
 			mFromState.onFadeOut();
 			mFromState.mFadeProgress = 1;
 		}
 		if(mToState!=null) {
 			if(!mToState.isInitialized())
 				mToState.init(mStateSystem);
 			mToState.mFadeProgress = 0;
 			mToState.onSet(stateSystem,layer);
 		}
 		super.onSet(stateSystem, layer);
 	}
 
 	@Override
 	public void stop() {
 		mFadeProgress = 0;
 		if(mFromState!=null) {
 			mFromState.mFadeProgress = 1;
 			mFromState.stop();
 		}
 		if(mToState!=null)
 			mToState.mFadeProgress = 1;
 	}
 
 	public void abort() {
 		mFadeProgress = 1;
 	}
 
 
 	@Override
 	public void onBlock() {
 		if(mFromState!=null) {
 			mFromState.mBlocked = true;
 			mFromState.onBlock();
 		}
 		if(mToState!=null) {
 			mToState.mBlocked = true;
 			mToState.onBlock();
 		}
 	}
 
 	@Override
 	public void onUnblock() {
 		if(mFromState!=null) {
 			mFromState.mBlocked = false;
 			mFromState.onUnblock();
 		}
 		if(mToState!=null) {
 			mToState.mBlocked = false;
 			mToState.onUnblock();
 		}
 	}
 }
