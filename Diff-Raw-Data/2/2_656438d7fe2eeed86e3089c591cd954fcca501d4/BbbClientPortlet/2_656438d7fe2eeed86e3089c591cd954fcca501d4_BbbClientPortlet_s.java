 /**
  *  bbbClient : BigBlueButton Liferay portal integration portlet
  *  Copyright (C) 2011  Aritz Galdos Otermin, Sareweb
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  Contact: aritz@sareweb.net
  */
 
 package net.sareweb.bigBlueButton.portlet;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Enumeration;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 import javax.portlet.PortletPreferences;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import net.sareweb.bigBlueButton.BbbManager;
 
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 
 /**
  * Portlet implementation class BbbClientPortlet
  */
 public class BbbClientPortlet extends MVCPortlet {
  
 	private PortletPreferences prefs;
 	private ThemeDisplay themeDisplay;
 	@Override
 	public void doView(RenderRequest request,RenderResponse response) throws IOException ,PortletException {
 		
 		prefs = request.getPreferences();
 		themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
 		
 		if(themeDisplay.isSignedIn()){
 			String pref_server = prefs.getValue("pref_server","localhost");
 			String pref_port = prefs.getValue("pref_port","80");
 			String pref_api_base = prefs.getValue("pref_api_base","bigbluebutton/api");
 			String pref_salt = prefs.getValue("pref_salt","");
 			
 			String apiUrl ="http://" + pref_server + ":" + pref_port + "/" + pref_api_base + "/";
 			System.out.println(apiUrl);
 			BbbManager bbbManager = new BbbManager(apiUrl, pref_salt);
 			
 			try{
 			
 				boolean running = bbbManager.isMeetingRuning(String.valueOf(themeDisplay.getScopeGroupId()));
 				
 				if(!running){
 					bbbManager.createMeeting(String.valueOf(themeDisplay.getScopeGroupId()), String.valueOf(themeDisplay.getScopeGroupId()), String.valueOf(themeDisplay.getScopeGroupId()), String.valueOf(themeDisplay.getScopeGroupId()));
 				}
 			
 				request.setAttribute("roomURL", bbbManager.createConnectionURL(themeDisplay.getUser().getScreenName(), String.valueOf(themeDisplay.getScopeGroupId()), String.valueOf(themeDisplay.getScopeGroupId())));
			
 			}
 			catch(Exception e){
 				_log.error("Error accesing meeting.", e);
 				viewJSP="/html/bbb/error.jsp";
 			}
 		}
 		else{
 			viewJSP="/html/bbb/not_logged.jsp";
 		}
 			
 		super.doView(request, response);
 	}
 	
 	public void savePreferences(ActionRequest request, ActionResponse response) throws Exception {
 		_log.debug("Saving preferences");
 		prefs = request.getPreferences();
 		
 		Enumeration params = request.getParameterNames();
 		
 		while (params.hasMoreElements()){
 			String prefName = (String)params.nextElement();
 			if(prefName.startsWith("pref")){
 				_log.debug("updating " + prefName);
 				String prefValue= request.getParameter(prefName);
 				if(prefValue!=null)prefs.setValue(prefName, prefValue);
 			}
 		}
 		prefs.store();
 	}
 	
 	
 	private static Log _log = LogFactoryUtil.getLog(BbbClientPortlet.class);
 }
