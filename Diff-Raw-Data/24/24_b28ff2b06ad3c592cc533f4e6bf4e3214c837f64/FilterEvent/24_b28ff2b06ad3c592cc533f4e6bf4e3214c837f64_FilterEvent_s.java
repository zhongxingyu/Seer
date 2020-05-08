 package net.sf.okapi.apptest.filters;
 
 import net.sf.okapi.apptest.common.IResource;
 import net.sf.okapi.apptest.common.ISkeleton;
 
 public class FilterEvent {
 	
 	public static enum FilterEventType {
 		START, START_DOCUMENT, END_DOCUMENT, START_SUBDOCUMENT, END_SUBDOCUMENT,
 		START_GROUP, END_GROUP, TEXT_UNIT, DOCUMENT_PART, FINISHED
 	};
 	
 	private FilterEventType filterEventType;
 	private IResource resource;
 	
	public FilterEvent(FilterEventType filterEventType, IResource resource) {
 		this.filterEventType = filterEventType;
 		this.resource = resource;
 	}	
 	
	public FilterEvent(FilterEventType filterEventType, IResource resource, ISkeleton skeleton) {
 		this.filterEventType = filterEventType;
 		this.resource = resource;
 		this.resource.setSkeleton(skeleton);
 	}	
 	
 	public FilterEventType getEventType() {
 		return filterEventType;
 	}
 	
 	public IResource getResource() {
 		return resource;
 	}
 	
 }
