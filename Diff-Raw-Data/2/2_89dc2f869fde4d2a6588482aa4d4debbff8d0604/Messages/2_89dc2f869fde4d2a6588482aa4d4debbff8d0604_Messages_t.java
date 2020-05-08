 package org.eclipse.dltk.core.search.indexing.core;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.dltk.core.search.indexing.core.messages"; //$NON-NLS-1$
 	public static String MixinBuilder_buildingRuntimeModelFor;
 	public static String MixinBuilder_buildingRuntimeModelFor2;
 	public static String MixinBuilder_savingIndexFor;
 	public static String MixinIndexer_indexLibraryError;
 	public static String MixinIndexer_startIndexingError;
 	public static String MixinIndexer_unknownProjectFragment;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
