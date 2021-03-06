 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.nfc;
 
 import com.android.nfc.nxp.NativeLlcpConnectionlessSocket;
 import com.android.nfc.nxp.NativeLlcpServiceSocket;
 import com.android.nfc.nxp.NativeLlcpSocket;
 
 import android.nfc.NdefMessage;
 import android.os.Bundle;
 
 public interface DeviceHost {
     public interface DeviceHostListener {
         public void onRemoteEndpointDiscovered(TagEndpoint tag);
 
         /**
          * Notifies transaction
          */
         public void onCardEmulationDeselected();
 
         /**
          * Notifies transaction
          */
         public void onCardEmulationAidSelected(byte[] aid);
 
         /**
          * Notifies P2P Device detected, to activate LLCP link
          */
         public void onLlcpLinkActivated(NfcDepEndpoint device);
 
         /**
          * Notifies P2P Device detected, to activate LLCP link
          */
         public void onLlcpLinkDeactivated(NfcDepEndpoint device);
 
         public void onRemoteFieldActivated();
 
         public void onRemoteFieldDeactivated();
 
         public void onSeApduReceived(byte[] apdu);
 
         public void onSeEmvCardRemoval();
 
         public void onSeMifareAccess(byte[] block);
     }
 
     public interface TagEndpoint {
         boolean connect(int technology);
         boolean reconnect();
         boolean disconnect();
 
         boolean presenceCheck();
         boolean isPresent();
         void startPresenceChecking();
 
         int[] getTechList();
         void removeTechnology(int tech); // TODO remove this one
         Bundle[] getTechExtras();
         byte[] getUid();
         int getHandle();
 
         byte[] transceive(byte[] data, boolean raw, int[] returnCode);
 
         boolean checkNdef(int[] out);
         byte[] readNdef();
         boolean writeNdef(byte[] data);
         NdefMessage[] findAndReadNdef();
         boolean formatNdef(byte[] key);
         boolean isNdefFormatable();
         boolean makeReadOnly();
     }
 
     public interface NfceeEndpoint {
         // TODO flesh out multi-EE and use this
     }
 
     public interface NfcDepEndpoint {
 
         /**
          * Peer-to-Peer Target
          */
         public static final short MODE_P2P_TARGET = 0x00;
         /**
          * Peer-to-Peer Initiator
          */
         public static final short MODE_P2P_INITIATOR = 0x01;
         /**
          * Invalid target mode
          */
         public static final short MODE_INVALID = 0xff;
 
         public byte[] receive();
 
         public boolean send(byte[] data);
 
         public boolean connect();
 
         public boolean disconnect();
 
         public byte[] transceive(byte[] data);
 
         public int getHandle();
 
         public int getMode();
 
         public byte[] getGeneralBytes();
     }
 
     public boolean initialize();
 
     public boolean deinitialize();
 
     public void enableDiscovery();
 
     public void disableDiscovery();
 
     public int[] doGetSecureElementList();
 
     public void doSelectSecureElement();
 
     public void doDeselectSecureElement();
 
     public int doGetLastError();
 
     public NativeLlcpConnectionlessSocket doCreateLlcpConnectionlessSocket(int nSap);
 
     public NativeLlcpServiceSocket doCreateLlcpServiceSocket(int nSap, String sn, int miu,
             int rw, int linearBufferLength);
 
     public NativeLlcpSocket doCreateLlcpSocket(int sap, int miu, int rw,
             int linearBufferLength);
 
     public boolean doCheckLlcp();
 
     public boolean doActivateLlcp();
 
     public void resetTimeouts();
 
     public boolean setTimeout(int technology, int timeout);
 }
