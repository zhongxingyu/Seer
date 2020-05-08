 /*
  * Copyright (c) Novedia Group 2012.
  *
  *    This file is part of Hubiquitus
  *
  *    Permission is hereby granted, free of charge, to any person obtaining a copy
  *    of this software and associated documentation files (the "Software"), to deal
  *    in the Software without restriction, including without limitation the rights
  *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  *    of the Software, and to permit persons to whom the Software is furnished to do so,
  *    subject to the following conditions:
  *
  *    The above copyright notice and this permission notice shall be included in all copies
  *    or substantial portions of the Software.
  *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  *    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  *    PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  *    FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  *    ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  *    You should have received a copy of the MIT License along with Hubiquitus.
  *    If not, see <http://opensource.org/licenses/mit-license.php>.
  */
 
 package org.hubiquitus.hapi.hStructures;
 
 import org.hubiquitus.hapi.exceptions.MissingAttrException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 /**
  * @version 0.5 
  * hAPI Command. For more info, see Hubiquitus reference
  */
 
 public class HCommand extends JSONObject {
 	final Logger logger = LoggerFactory.getLogger(HCommand.class);
 
 	public HCommand() {
 		super();
 	}
 
 	public HCommand(String cmd, JSONObject params, HCondition filter){
 		this();
 		try {
 			setCmd(cmd);
 		} catch (MissingAttrException e) {
 			logger.error("message: ", e);
 		}
 		setParams(params);
 		setFilter(filter);
 	}
 	
 	
 	public HCommand(JSONObject jsonObj) throws JSONException {
 		super(jsonObj.toString());
 	}
 
 	/* Getters & Setters */
 
 	/**
 	 * @return command. NULL if undefined
 	 */
 	public String getCmd() {
 		String cmd;
 		try {
 			cmd = this.getString("cmd");
 		} catch (JSONException e) {
 			cmd = null;
 		}
 		return cmd;
 	}
 
 	public void setCmd(String cmd) throws MissingAttrException {
 		try {
 			if (cmd == null || cmd.length()<=0) {
 				throw new MissingAttrException("cmd");
 			} else {
 				this.put("cmd", cmd);
 			}
 		} catch (JSONException e) {
 			logger.warn("message: ", e);
 		}
 	}
 
 	/**
 	 * @return params throws to the hserver. NULL if undefined
 	 */
 	public JSONObject getParams() {
 		JSONObject params;
 		try {
 			params = this.getJSONObject("params");
 		} catch (JSONException e) {
 			params = null;
 		}
 		return params;
 	}
 
 	public void setParams(JSONObject params) {
 		try {
 			if (params == null) {
 				this.remove("params");
 			} else {
 				this.put("params", params);
 			}
 		} catch (JSONException e) {
 			logger.warn("message: ", e);
 		}
 	}
 	
 	public HCondition getFilter(){
 		HCondition filter;
 		try {
 			filter = new HCondition(this.getJSONObject("filter"));
 		} catch (JSONException e) {
 			filter = null;
 		}
 		return filter;
 	}
 	
 	public void setFilter(HCondition filter){
 		try {
 			if(filter == null){
 				this.remove("filter");
 			}else{
 				this.put("filter", filter);
 			}
 		} catch (JSONException e) {
 			logger.warn("message: ", e);
 		}
 	}
 
 }
