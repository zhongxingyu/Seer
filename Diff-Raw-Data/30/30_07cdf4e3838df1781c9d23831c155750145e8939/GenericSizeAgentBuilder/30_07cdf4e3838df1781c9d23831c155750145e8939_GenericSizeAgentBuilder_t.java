 /* GenericSizeAgentBuilder.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		May 4, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import org.zkoss.zats.ZatsException;
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.impl.ClientCtrl;
 import org.zkoss.zats.mimic.impl.au.EventDataManager;
 import org.zkoss.zats.mimic.operation.SizeAgent;
 import org.zkoss.zk.ui.HtmlBasedComponent;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.MaximizeEvent;
 import org.zkoss.zk.ui.event.MinimizeEvent;
 import org.zkoss.zk.ui.event.SizeEvent;
 
 /**
  * The builder for size agent.
  * @author pao
  */
 public class GenericSizeAgentBuilder implements OperationAgentBuilder<SizeAgent> {
 
 	public SizeAgent getOperation(ComponentAgent target) {
 		return new SizeAgentImpl(target);
 	}
 
 	private class SizeAgentImpl extends AgentDelegator implements SizeAgent {
 		public SizeAgentImpl(ComponentAgent target) {
 			super(target);
 		}
 
 		public void maximize(boolean maximized) {
 			check("Maximizable");
 			HtmlBasedComponent comp = (HtmlBasedComponent) getDelegatee();
 			String cmd = Events.ON_MAXIMIZE;
 			MaximizeEvent event = new MaximizeEvent(cmd, comp, "", "", comp.getWidth(), comp.getHeight(), maximized);
 			Map<String, Object> data = EventDataManager.build(event);
 			((ClientCtrl) getClient()).postUpdate(target.getDesktop().getId(), target.getUuid(), cmd, data);
 		}
 
 		public void minimize(boolean minimized) {
 			check("Minimizable");
 			HtmlBasedComponent comp = (HtmlBasedComponent) getDelegatee();
 			String cmd = Events.ON_MINIMIZE;
 			MinimizeEvent event = new MinimizeEvent(cmd, comp, "", "", comp.getWidth(), comp.getHeight(), minimized);
 			Map<String, Object> data = EventDataManager.build(event);
 			((ClientCtrl) getClient()).postUpdate(target.getDesktop().getId(), target.getUuid(), cmd, data);
 		}
 
 		public void resize(int width, int height) {
			if (width < 0 && height < 0)
				return; // do nothing

			// check flag
 			HtmlBasedComponent comp = (HtmlBasedComponent) getDelegatee();
			check("Sizable");
			// check arguments
			try {
				if (width < 0) {
					String widthStr = comp.getWidth();
					if (widthStr == null || widthStr.length() <= 0) {
						Method method = getDelegatee().getClass().getMethod("getMinwidth");
						widthStr = method.invoke(getDelegatee()).toString() + "px";
					}
					width = Integer.parseInt(widthStr.substring(0, widthStr.length() - 2));
				}
				if (height < 0) {
					String heightStr = comp.getHeight();
					if (heightStr == null || heightStr.length() <= 0) {
						Method method = getDelegatee().getClass().getMethod("getMinheight");
						heightStr = method.invoke(getDelegatee()).toString() + "px";
					}
					height = Integer.parseInt(heightStr.substring(0, heightStr.length() - 2));
				}
			} catch (Exception e) {
				throw new ZatsException("", e);
			}

			// post AU
 			String cmd = Events.ON_SIZE;
 			SizeEvent event = new SizeEvent(cmd, comp, width + "px", height + "px", 0);
 			Map<String, Object> data = EventDataManager.build(event);
 			((ClientCtrl) getClient()).postUpdate(target.getDesktop().getId(), target.getUuid(), cmd, data);
 		}
 
 		/**
 		 * check enabled flag.
 		 */
 		private void check(String property) {
 			try {
 				Method method = getDelegatee().getClass().getMethod("is" + property);
 				boolean maximizable = (Boolean) method.invoke(getDelegatee());
 				if (!maximizable)
 					throw new AgentException("component isn't " + property.toLowerCase());
 			} catch (AgentException e) {
 				throw e;
 			} catch (Exception e) {
 				throw new ZatsException(getDelegatee().getClass().getName() + " doesn't support "
 						+ property.toLowerCase() + " operation", e);
 			}
 		}
 	}
 }
