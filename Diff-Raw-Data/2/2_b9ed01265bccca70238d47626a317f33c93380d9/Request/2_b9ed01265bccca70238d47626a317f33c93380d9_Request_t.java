 /* 
   Copyright 2010 Duncan Mac-Vicar P. <dmacvicar@suse.de>
  
   This file is part of the openSUSE Build Service Android Client.
 
   openSUSE Build Service Android Client is free software: you
   can  redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software Foundation,
   either version 3 of the License, or (at your option) any later
   version.
 
   openSUSE Build Service Android Client is distributed in the hope
   that it will be useful, but WITHOUT ANY WARRANTY; without even the
   implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with QuiteSleep.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.opensuse.android.obs.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 import org.simpleframework.xml.Root;
 
 @Root(strict=false)
 public class Request {
 	@Attribute
 	private String id;
 	@Element(required=false)
 	private String description;
 	@Element(name="state")
 	private State state;
 	
 	@Element(name="submit", required=false)	
 	private Submit submit;
 	
 	@ElementList(entry="action", inline=true, required=false)
 	private List<Action> actions;
 	
 	public String getId() {
 		return id;
 	}
 	
 	public String getDescription() {
 		return description;	
 	}
 	
 	public State getState() {
 		return state;
 	}
 	
 	public List<Action> getActions() {		
 		/* old requests have a submit element which is equivalent
 		 * to one action of type submit, if this is the case
 		 * convert it to a list of one actions
 		 */
		if (actions.isEmpty() && submit != null) {
 			List<Action> tmp_actions = new ArrayList<Action>();
 			Action tmp_action = new Action();
 			tmp_action.setType("submit");
 			tmp_action.setTarget(submit.getTarget());
 			tmp_action.setSource(submit.getSource());
 			tmp_actions.add(tmp_action);
 			return tmp_actions;
 		}
 		return actions;
 	}
 	
 	@Root(strict=false)
 	public static class State {		
 		@Attribute
 		private String name;
 		@Attribute
 		private String who;
 		@Attribute
 		private String when;
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String getWho() {
 			return who;
 		}
 		
 		public String getWhen() {
 			return when;
 		}
 	}
 	
 	@Root(name="submit", strict=false)
 	public static class Submit {
 		
 		@Element(name="source")
 		private Action.Source source;
 		@Element(name="target")
 		private Action.Target target;
 
 		public Action.Source getSource() {
 			return source;
 		}
 		public Action.Target getTarget() {
 			return target;
 		}	
 	}
 	
 	@Root(strict=false)
 	public static class Action {
 				
 		@Attribute
 		private String type;
 		@Element
 		private Source source;
 		@Element
 		private Target target;
 
 		public Source getSource() {
 			return source;
 		}
 		public Target getTarget() {
 			return target;
 		}
 		public String getType() {
 			return type;
 		}
 
 		public void setType(String type) {
 			this.type = type;
 		}
 		public void setSource(Source source) {
 			this.source = source;
 		}
 		public void setTarget(Target target) {
 			this.target = target;
 		}
 
 		@Root(strict=false, name="source")
 		public static class Source {
 			
 			@Attribute
 			private String project;
 			@Attribute(name="package")
 			private String _package;
 			@Attribute(required=false)
 			private String rev;
 			
 			public String getProject() {
 				return project;
 			}
 			public String getPackage() {
 				return _package;
 			}
 			public String getRev() {
 				return rev;
 			}	
 		}
 		
 		@Root(strict=false, name="target")
 		public static class Target {	
 			@Attribute
 			private String project;
 			@Attribute(name="package")
 			private String _package;
 			
 			public String getProject() {
 				return project;
 			}
 			public String getPackage() {
 				return _package;
 			}	
 		}
 
 	}
 	
 }
