 package edu.umd.cs.guitar.model;
 
 /*	
  *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *	conditions:
  * 
  *	The above copyright notice and this permission notice shall be included in all copies or substantial 
  *	portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 /**
  * 
  * Putting all GUITAR constants in one place
  * 
  * <p>
  * 
  * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
  * 
  */
 public interface GUITARConstants {
     
 
     /**
      * The location of the default guitar logging configuration (.glc) file.
      * Currently unused 
      */
    public static final String DEFAULT_LOGGING_CONFIGURATION = "log/guitar-default.glc";
 
 
 	public static final String COMPONENT_ID_PREFIX = "w";
 
 	// ---------------------------
 	// Tag names
 	// ---------------------------
 
 	final static String ID_TAG_NAME = "ID";
 	final static String TITLE_TAG_NAME = "Title";
 
 	final static String ROOTWINDOW_TAG_NAME = "Rootwindow";
 	final static String MODAL_TAG_NAME = "Modal";
 
 	final static String CLASS_TAG_NAME = "Class";
 	final static String TYPE_TAG_NAME = "Type";
 	final static String X_TAG_NAME = "X";
 	final static String Y_TAG_NAME = "Y";
 
 	final static String HASHCODE_TAG_NAME = "Hashcode";
 
 	final static String INVOKELIST_TAG_NAME = "Invokelist";
 	final static String EVENT_TAG_NAME = "ReplayableAction";
 
 	// ------------------------------
 	// Define the suffix to a pattern of ignored objects
 	// ------------------------------
 	public static final String NAME_PATTERN_SUFFIX = "~";
 
 	public static final String NAME_SEPARATOR = "_";
 
 	public static final String IGNORE_COMMENT_PREFIX = "//";
 
 	public static final String CMD_ARGUMENT_SEPARATOR = ":";
 
 	// ------------------------------
 	// Definitions for the different GUITAR event types.
 	// ------------------------------
 
 	/**
 	 * Close the current window
 	 */
 	public static String TERMINAL = "TERMINAL";
 
 	// 
 	/**
 	 * Expand a new menu/tab/tree node for more components
 	 */
 	public static String EXPAND = "EXPAND";
 
 	/**
 	 * Open modal window(s)
 	 */
 	public static String RESTRICED_FOCUS = "RESTRICED FOCUS";
 
 	/**
 	 * Open modeless window(s)
 	 */
 	public static String UNRESTRICED_FOCUS = "UNRESTRICED FOCUS";
 
 	/**
 	 * Other events not affect the GUI structure
 	 */
 	public static String SYSTEM_INTERACTION = "SYSTEM INTERACTION";
 
 	/**
 	 * Activated by a call from parent's node
 	 */
 	// public static String ACTIVATED_BY_PARENT = "ACTIVATED BY PARENT";
 
 	// ------------------------------
 	// Definitions for the different EFG edge types
 	// ------------------------------
 
 	/**
 	 * No follow realationship between 2 events
 	 */
 	public static final int NO_EDGE = 0;
 
 	/**
 	 * Normal edges
 	 */
 	public static final int FOLLOW_EDGE = 1;
 
 	/**
 	 * The edges used to reach an event
 	 */
 	public static final int REACHING_EDGE = 2;
 
 }
