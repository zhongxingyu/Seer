 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas taken from Eclipse code
  *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
  *******************************************************************************/
 package net.sourceforge.eclipseccase;
 
 /**
  * Shared preference constants for ClearCase plugin preferences.
  */
 public interface IClearcasePreferenceConstants
 {
     /** ClearCase preference */
     String ADD_AUTO = ClearcasePlugin.PLUGIN_ID
             + ".add.auto"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String ADD_WITH_CHECKIN = ClearcasePlugin.PLUGIN_ID
             + ".add.checkin"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String CHECKOUT_AUTO = ClearcasePlugin.PLUGIN_ID
             + ".checkout.auto"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String CHECKOUT_LATEST = ClearcasePlugin.PLUGIN_ID
             + ".checkout.latest"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String CHECKOUT_RESERVED = ClearcasePlugin.PLUGIN_ID
             + ".checkout.reserved"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String COMMENT_ADD = ClearcasePlugin.PLUGIN_ID
             + ".comment.add"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String COMMENT_ADD_NEVER_ON_AUTO = ClearcasePlugin.PLUGIN_ID
            + ".comment.add"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String COMMENT_CHECKIN = ClearcasePlugin.PLUGIN_ID
             + ".comment.checkin"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String COMMENT_CHECKOUT = ClearcasePlugin.PLUGIN_ID
             + ".comment.checkout"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String COMMENT_CHECKOUT_NEVER_ON_AUTO = ClearcasePlugin.PLUGIN_ID
             + ".comment.checkout.neverOnAuto"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String COMMENT_ESCAPE = ClearcasePlugin.PLUGIN_ID
             + ".comment.escape"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String IGNORE_NEW = ClearcasePlugin.PLUGIN_ID
             + ".ignore.new"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String PRESERVE_TIMES = ClearcasePlugin.PLUGIN_ID
             + ".preserveTimes"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String RECURSIVE = ClearcasePlugin.PLUGIN_ID
             + ".recursive"; //$NON-NLS-1$
 
 
     /** common preference */
     String SAVE_DIRTY_EDITORS = ClearcasePlugin.PLUGIN_ID
             + ".saveDirtyEditors"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String USE_CLEARTOOL = ClearcasePlugin.PLUGIN_ID
             + ".useCleartool"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String CACHE_TIMEOUT = ClearcasePlugin.PLUGIN_ID
             + ".cacheTimeOut"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String USE_CLEARDLG = ClearcasePlugin.PLUGIN_ID
             + ".useClearDlg"; //$NON-NLS-1$
 
     /** ClearCase preference */
     String CLEARCASE_API = ClearcasePlugin.PLUGIN_ID
             + ".clearcaseAPI"; //$NON-NLS-1$
 
     /** preference value for <code>CLEARCASE_API</code> */
     String CLEARCASE_NATIVE = "native_cal"; //$NON-NLS-1$
 
     /** preference value for <code>CLEARCASE_API</code> */
     String CLEARCASE_CLEARTOOL = "native_cleartool"; //$NON-NLS-1$
 
     /** preference value for <code>CLEARCASE_API</code> */
     String CLEARCASE_CLEARDLG = "compatible_cleardlg"; //$NON-NLS-1$
 
     /** preference value */
     String IF_POSSIBLE = "ifPossible"; //$NON-NLS-1$
 
     /** preference value */
     String ALWAYS = "always"; //$NON-NLS-1$
 
     /** preference value */
     String NEVER = "never"; //$NON-NLS-1$
 
     /** preference value */
     String PROMPT = "prompt"; //$NON-NLS-1$
     
     /** ClearCase preference */
     String HIDE_REFRESH_STATE_ACTIVITY = ClearcasePlugin.PLUGIN_ID
             + ".refreshState.hide"; //$NON-NLS-1$
 
 }
