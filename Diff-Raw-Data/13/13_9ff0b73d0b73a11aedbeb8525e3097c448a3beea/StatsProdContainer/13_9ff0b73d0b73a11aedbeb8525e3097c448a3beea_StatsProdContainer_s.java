 package com.argility.centralpages.data;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import com.vaadin.data.util.BeanItemContainer;
 
 @SuppressWarnings("serial")
 public class StatsProdContainer extends BeanItemContainer<StatsProd> implements
 		Serializable {
 
 	public static final Object[] NATURAL_COL_ORDER = new Object[] {
		"brCde","central","lastReplicated","xoutReceived", "replProcess", "triadProcess",
 		"replAuditUpTo", "replUpTo", "replDiff", "lastSwLoad", "swAudUpTo", "swDiff", "swCrashed", "swCrashAudId"
 	};
 	
 	public static final String[] COL_HEADERS_ENGLISH = new String[] {
		"Branch","Central","Last Replicated","Xout Received", "Repl Process", "Triad Process",
		"Repl Audit UpTo", "Repl UpTo", "Repl Diff", "Last Sw Load", "Sw Aud UpTo", "Sw Diff", "Sw Crashed", "Sw Crash AudId"
 	};
 	
 	public StatsProdContainer(Class<? super StatsProd> type,
 			Collection<? extends StatsProd> collection)
 			throws IllegalArgumentException {
 		super(type, collection);
 		
 	}
 
 	
 
 }
