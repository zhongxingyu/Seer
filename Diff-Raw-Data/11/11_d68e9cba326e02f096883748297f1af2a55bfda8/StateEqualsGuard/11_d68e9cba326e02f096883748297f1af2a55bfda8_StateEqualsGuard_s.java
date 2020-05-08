 package net.ysuga.firosophy.guard;
 
 import java.util.Set;
 
 import net.ysuga.firosophy.FIROSOPHY;
 import net.ysuga.firosophy.state.RTState;
 import net.ysuga.rtsbuilder.RTCCondition;
 import net.ysuga.rtsbuilder.RTSystemBuilder;
import net.ysuga.rtsystem.profile.Component;
 import net.ysuga.rtsystem.profile.RTSObject;
 import net.ysuga.rtsystem.profile.RTSystemProfile;
 import net.ysuga.statemachine.guard.AbstractGuard;
 import net.ysuga.statemachine.state.State;
 import net.ysuga.statemachine.util.ParameterMap;
 
 public class StateEqualsGuard extends AbstractGuard {
  
 	static final String PATHURI = "pathUri";
 	static final String RTCCONDITION = "RTCCondition";
 	private String pathUri;
 	private RTCCondition rtcCondition;
 	 
 	/**
 	 * <div lang="ja">
 	 * @param state
 	 * @return
 	 * @throws Exception
 	 * </div>
 	 * <div lang="en">
 	 * @param state
 	 * @return
 	 * @throws Exception
 	 * </div>
 	 */
 	public boolean operate(State state) throws Exception {
 		if(!pathUri.equals("any")) {
			RTCCondition currentCondition = RTSystemBuilder.getComponentCondition(pathUri);
 			return currentCondition.equals(rtcCondition);
 		} else {
 			RTSystemProfile profile = ((RTState)state).getRTSystemProfile();
			Set<Component> componentSet = profile.componentSet;
 
 			for(RTSObject component : componentSet) {
				RTCCondition currentCondition = RTSystemBuilder.getComponentCondition(component);
 				if(currentCondition.equals(rtcCondition)) {
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 	
 	/**
 	 * <div lang="ja">
 	 * �R���X�g���N�^
 	 * @param name
 	 * @param componentPathUri
 	 * @param stateCondition
 	 * </div>
 	 * <div lang="en">
 	 * Constructor
 	 * @param name
 	 * @param componentPathUri
 	 * @param stateCondition
 	 * </div>
 	 */
 	public StateEqualsGuard(String name, String componentPathUri, RTCCondition stateCondition) {
 		super(name, FIROSOPHY.STATE_EQUALS);
 		this.pathUri = componentPathUri;
 		this.rtcCondition = stateCondition;
 	}
 
 	/**
 	 * <div lang="ja">
 	 * @return
 	 * </div>
 	 * <div lang="en">
 	 * @return
 	 * </div>
 	 */
 	@Override
 	public ParameterMap getParameterMap() {
 		ParameterMap map = new ParameterMap();
 		map.put(PATHURI, pathUri);
 		map.put(RTCCONDITION, rtcCondition.toString());
 		return map;
 	}
 
 	/**
 	 * <div lang="ja">
 	 * @param state
 	 * </div>
 	 * <div lang="en">
 	 * @param state
 	 * </div>
 	 */
 	public void onInitialize(State state) {
 		// TODO �����������ꂽ���\�b�h�E�X�^�u
 		
 	}
 
 	/**
 	 * <div lang="ja">
 	 * @param state
 	 * </div>
 	 * <div lang="en">
 	 * @param state
 	 * </div>
 	 */
 	public void onFinalize(State state) {
 		// TODO �����������ꂽ���\�b�h�E�X�^�u
 		
 	}
 	 
 }
  
