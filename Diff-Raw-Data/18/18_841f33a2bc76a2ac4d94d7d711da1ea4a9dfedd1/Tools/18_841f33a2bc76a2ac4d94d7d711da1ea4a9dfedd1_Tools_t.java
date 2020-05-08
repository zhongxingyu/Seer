 /*******************************************************************************
  * Copyright (c) 2013 MinimaJack
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     MinimaJack - initial API and implementation
  ******************************************************************************/
 package org.jmangos.sniffer.jna;
 
 import com.sun.jna.Pointer;
 
 public interface Tools extends com.sun.jna.platform.win32.Kernel32 {
 
     boolean EnableDebugPriv();
 
     boolean Inject(int dwPid, String lpDll);
 
     int GetTargetProcessId(String lpProcName);
    
    int GetBaseAddress(int dwPid, String lpProcName);
 
     int ReadMemory(HANDLE hProcess, Pointer lpBaseAddress, Pointer lpBuffer, int nSize);
 
     int WriteMemory(HANDLE hProcess, Pointer lpBaseAddress, Pointer lpBuffer, int nSize);
 }
