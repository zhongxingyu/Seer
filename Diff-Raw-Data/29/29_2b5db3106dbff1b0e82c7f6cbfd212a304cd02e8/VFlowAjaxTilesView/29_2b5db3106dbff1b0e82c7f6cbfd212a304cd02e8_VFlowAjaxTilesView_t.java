 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2014]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.webmvc.tiles3;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.ObjectUtils;
 import org.springframework.util.StringUtils;
 import org.springframework.webflow.core.collection.AttributeMap;
 import org.springframework.webflow.execution.Event;
 import org.springframework.webflow.execution.RequestContext;
 import org.springframework.webflow.execution.RequestContextHolder;
 import org.springframework.webflow.execution.View;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * {@code VFlowAjaxTilesView} is a rewritten {@link org.springframework.webflow.mvc.view.FlowAjaxTilesView} (for Apache Tiles 2.x) to work
  * with <b>Apache Tiles 3.x.x</b>.
  *
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public class VFlowAjaxTilesView
         extends VAjaxTilesView {
     private final static Logger LOGGER                 = Logger.getLogger(VFlowAjaxTilesView.class);
     private static final String DEFAULT_ATTR_NAME      = "fragments";
     private              String fragmentsAttributeName = DEFAULT_ATTR_NAME;
 
     @Override
     public void afterPropertiesSet() throws Exception {
         super.afterPropertiesSet();
         final ApplicationContext applicationContext = this.getApplicationContext();
         if (applicationContext != null) {
             try {
                 final Properties flowProperties = (Properties) applicationContext.getBean("flowProperties");
                 this.fragmentsAttributeName = flowProperties.getProperty("sa.flows.event.fragmentsAttributeName");
             } catch (Exception ignore) {
                 LOGGER.trace("Could not resolve fragmentsAttributeName from flowProperties, default fragments will be used");
             }
         }
     }
 
     protected String[] getRenderFragments(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) {
         final RequestContext context = RequestContextHolder.getRequestContext();
         if (context == null) {
             return super.getRenderFragments(model, request, response);
         } else {
             String[] fragments = (String[]) context.getFlashScope().get(View.RENDER_FRAGMENTS_ATTRIBUTE);
             if (fragments == null) {
 
                 LOGGER.warn(String.format("Could not find %s in %s", View.RENDER_FRAGMENTS_ATTRIBUTE, "RequestContext#getFlashScope"));
                 final Event currentEvent = context.getCurrentEvent();
 
                 if (currentEvent != null) {
 
                     LOGGER.debug(String.format("Trying to retrieve fragments from Event:%s by attributeName=%s",
                             currentEvent.getId(), this.fragmentsAttributeName)
                     );
 
                     final AttributeMap<Object> attributes = currentEvent.getAttributes();
                     final Object _frags = attributes.get(this.fragmentsAttributeName);
 
                    if (_frags != null) {
                        if (ClassUtils.isAssignable(String.class, _frags.getClass())) {
                            fragments = StringUtils.addStringToArray(new String[]{}, (String) _frags);
                        } else if (ClassUtils.isAssignable(String[].class, _frags.getClass())) {
                            fragments = (String[]) _frags;
                        }
 
                        if (fragments != null) {
                            context.getFlashScope().put(View.RENDER_FRAGMENTS_ATTRIBUTE, fragments);
                        }

                        LOGGER.info(String
                                .format("Detected %s to be rendered only -> %s", this.fragmentsAttributeName, ObjectUtils.getDisplayString(fragments))
                        );
                    } else {
                        LOGGER.debug(String.format("No %s in next event attribute", this.fragmentsAttributeName));
                    }
                 }
             }
             if (fragments == null) {
                 return super.getRenderFragments(model, request, response);
             }
             return fragments;
         }
     }
 
 }
