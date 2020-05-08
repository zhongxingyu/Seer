 /* GenericSelectByIndexBuilder.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Apr 10, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
import org.zkoss.zats.mimic.impl.ConversationCtrl;
 import org.zkoss.zats.mimic.operation.SelectByIndexAgent;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.Selectbox;
 
 /**
  * A builder for selection by index agent at Selectbox component. 
  * @author pao
  */
 public class SelectboxSelectByIndexAgentBuilder implements OperationAgentBuilder<SelectByIndexAgent> {
 
 	public SelectByIndexAgent getOperation(final ComponentAgent target) {
 		return new SelectByIndexAgentImpl(target);
 	}
 
 	class SelectByIndexAgentImpl extends AgentDelegator implements SelectByIndexAgent {
 
 		public SelectByIndexAgentImpl(ComponentAgent target) {
 			super(target);
 		}
 
 		public void select(int index) {
 			// check target
 			if (!target.is(Selectbox.class))
 				throw new AgentException("target is not a Selectbox");
 
 			// check bounds
 			Selectbox sb = target.as(Selectbox.class);
 			if (index < 0 || index >= sb.getModel().getSize())
 				throw new AgentException("index out of bounds: " + index);
 
 			// post AU
 			String cmd = Events.ON_SELECT;
 			Map<String, Object> data = new HashMap<String, Object>();
 			data.put("", index);
			ConversationCtrl ctrl = (ConversationCtrl) target.getConversation();
 			ctrl.postUpdate(target.getDesktop().getId(), target.getUuid(), cmd, data);
 		}
 	}
 }
