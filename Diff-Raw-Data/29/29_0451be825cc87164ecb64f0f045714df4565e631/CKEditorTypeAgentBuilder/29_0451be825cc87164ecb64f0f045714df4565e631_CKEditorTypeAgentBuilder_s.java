 /* CKEditorTypeAgentBuilder.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Apr 10, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.util.Map;
 
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
import org.zkoss.zats.mimic.impl.ConversationCtrl;
 import org.zkoss.zats.mimic.impl.au.EventDataManager;
 import org.zkoss.zats.mimic.operation.TypeAgent;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.InputEvent;
 
 /**
  * The builder of typing operation for CKEditor.
  * Different from other input components, the CKEditor doesn't support onFocus event.
  * @author pao
  */
 public class CKEditorTypeAgentBuilder implements OperationAgentBuilder<TypeAgent> {
 
 	public TypeAgent getOperation(ComponentAgent target) {
 		return new TypeAgentImpl(target);
 	}
 
 	class TypeAgentImpl extends AgentDelegator implements TypeAgent {
 		public TypeAgentImpl(ComponentAgent target) {
 			super(target);
 		}
 
 		public void type(String value) {
 			try {
 				String desktopId = target.getDesktop().getId();
				ConversationCtrl ctrl = (ConversationCtrl) target.getConversation();
 				// changing
 				String cmd = Events.ON_CHANGING;
 				InputEvent event = new InputEvent(cmd, target.getComponent(), value, null);
 				Map<String, Object> data = EventDataManager.build(event);
 				ctrl.postUpdate(desktopId, target.getUuid(), cmd, data);
 				// change (reuse changing event data collection)
 				cmd = Events.ON_CHANGE;
 				data.put("value", value);
 				ctrl.postUpdate(desktopId, target.getUuid(), cmd, data);
 
 			} catch (Exception e) {
 				throw new AgentException("value \"" + value + "\" is invalid for the component: " + target, e);
 			}
 		}
 	}
 }
