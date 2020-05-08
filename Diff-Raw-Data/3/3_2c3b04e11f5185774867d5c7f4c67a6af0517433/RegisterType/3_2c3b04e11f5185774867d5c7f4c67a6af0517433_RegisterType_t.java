 /*
  * This file is part of Bytecast.
  *
  * Bytecast is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Bytecast is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Bytecast.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package edu.syr.bytecast.amd64.api.constants;
 
 /**
  *
  * @author Chen Qian
  */
 public enum RegisterType {
 
   /**
   * General Register
   */
   AH,
   BH,
   CH,
   DH,
   AL,
   BL,
   CL,
   DL,
   SIL,
   DIL,
   BPL,
   SPL,
   R8B,
   R9B,
   R10B,
   R11B,
   R12B,
   R13B,
   R14B,
   R15B,
   AX,
   BX,
   CX,
   DX,
   SI,
   DI,
   BP,
   SP,
   R8W,
   R9W,
   R10W,
   R11W,
   R12W,
   R13W,
   R14W,
   R15W,
   EAX,
   EBX,
   ECX,
   EDX,
   ESI,
   EDI,
   EBP,
   ESP,
   R8D,
   R9D,
   R10D,
   R11D,
   R12D,
   R13D,
   R14D,
   R15D,
   RAX,
   RBX,
   RCX,
   RDX,
   RSI,
   RDI,
   RBP,
   RSP,
   R8,
   R9,
   R10,
   R11,
   R12,
   R13,
   R14,
   R15,
   RFLAGS,
   EFLAGS,
   FLAGS,
   RIP,
   EIP,
   IP,
   /**
   * Segment Register
   */
   CS,
   DS,
   ES,
   FS,
   GS,
   SS,
   
   /*Error Flags*/
   CF,
   PF,
   AF,
   ZF,
   SF,
   TF,
   IF,
  DF,
   OF,
   IOPL,
   NT,
   RF,
   VM,
   AC,
   VIF,
   VIP,
   ID
 
 }
