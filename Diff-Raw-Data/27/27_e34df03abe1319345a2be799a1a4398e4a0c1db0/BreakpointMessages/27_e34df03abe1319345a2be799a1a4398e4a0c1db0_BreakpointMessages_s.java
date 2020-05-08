 package org.eclipse.dltk.debug.ui.breakpoints;
 
 import org.eclipse.osgi.util.NLS;
 
 public class BreakpointMessages {
 	private static final String BUNDLE_NAME = "org.eclipse.dltk.debug.ui.breakpoints.BreakpointMessages"; //$NON-NLS-1$
 	
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, BreakpointMessages.class);
 	}
 	
 	// Breakpoint titles
 	public static String LineBreakpointTitle;
 	public static String MethodBreakpointTitle;
 	public static String WatchpointTitle;
 	
 	
 	// Script breakpoint labels
 	public static String LanguageLabel;
 	
 	public static String EnabledLabel;
 	
 	public static String FileLabel;
 	
	public static String EngineIdLabel;
 	
 	public static String HitCountLabel;
 	
 	public static String UseConditionLabel;
 	
 	public static String BreakWhenHitCountLabel;
 	
 	public static String HitsLabel;
 	
 	// Line breakpoint labels
 	public static String LineNumberLabel;
 	
 	// Method breakpoint labels
 	public static String SuspendOnMethodEntryLabel;
 	
 	public static String SuspendOnMethodExitLabel;
 	
 	// Watchpoint labels
 	public static String WatchFieldLabel;
 	
 	public static String SuspendOnAccessLabel;
 	
 	public static String SuspendOnModificationLabel;
 	
 	// Errors
 	public static String InvalidNumberOfHits;
 	
 	//
	public static String EngineIdNotAvailableMessage;
 	
 	public static String HitCountNotAvailableMessage;
 	
 	public static String HitConditionGreaterOrEqual;
 	
 	public static String HitConditionEqual;
 	
 	public static String HitConditionMultiple;
 }
