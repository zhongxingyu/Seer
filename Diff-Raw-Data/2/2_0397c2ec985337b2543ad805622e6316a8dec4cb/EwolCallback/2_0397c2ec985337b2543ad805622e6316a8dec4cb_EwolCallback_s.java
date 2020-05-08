 /**
 *******************************************************************************
 * @file ewol EwolCallback.java
 * @brief CPP callback.
 * @author Edouard DUPIN
 * @date 20/04/2012
 * @par Project
 * ewol
 *
 * @par Copyright
 * Copyright 2011 Edouard DUPIN, all right reserved
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY.
 *
 * Licence summary : 
 *    You can modify and redistribute the sources code and binaries.
 *    You can send me the bug-fix
 *
 * Term of the licence in in the file licence.txt.
 *
 *******************************************************************************
 */
 
 package org.ewol;
 
public interface NativeCallback {
 
     public void keyboardUpdate(boolean show);
     public void eventNotifier(String[] args);
     public void orientationUpdate(int screenMode);
 
 }
