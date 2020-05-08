 package org.palaso.languageforge.client.lex.model;
 
 public class ProjectDto extends BaseDto<ProjectDto> {
 
 	protected ProjectDto() {
 	};
 
 	// JSNI overlay methods
 	public final native String getProjectName() /*-{
 		return this.name;
 	}-*/;
 
 	public final native void setProjectName(String name) /*-{
 		this.name = name;
 	}-*/;
 
 	public final native String getProjectId() /*-{
 		return this.id;
 	}-*/;
 
 	public final native String getProjectTitle() /*-{
 		return this.title;
 	}-*/;
 
 	public final native String getProjectType() /*-{
 		return this.type;
 	}-*/;
 
 }
