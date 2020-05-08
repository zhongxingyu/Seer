 /*
  * Copyright (C) 2012 uPhyca Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package com.uphyca.robots.core;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 /**
  * 
  * RPU-10技術資料:RS485通信機能について より
  * 
  * 通常RPU-10を使用してコマンド方式サーボを制御する場合は、RPU-10に付属のソフトウェア"Motion Editor RPU-10"を使用しますが、
  * RPU-10のRS232Cへ専用のヘッダーを付加したコマンド方式サーボ用のパケットを送信することで、PCなどからRPU-10を経由してサーボと通信することができます。
  * 
  * RPU-10を接続するRS232Cの通信設定は次のようになります。
  * 
  * <dl>
  * 
  * <dt>ビット/秒</dt>
  * <dd>115,200</dd>
  * 
  * <dt>データビット</dt>
  * <dd>8</dd>
  * 
  * <dt>パリティ</dt>
  * <dd>なし</dd>
  * 
  * <dt>ストップビット</dt>
  * <dd>1</dd>
  * 
  * <dt>フロー制御</dt>
  * <dd>なし</dd>
  * 
  * </dl>
  *
  * <dl>
  * 
  * <dt>送信可能なパケットのサイズは127Byte以下</dt>
  * <dd>多数のサーボを制御するロングパケットなどで、RPU-10_HeaderからPacket for Servoのチェックサムまでの合計がこの制限を越えてしまう場合は、
  * 一回のデータ長が127バイト以下に収まるよう分割して送信するなどの工夫が必要となります。
  * </dd>
  * 
  * <dt>USB-RS485変換器RSC-U485などを使用した場合と比べて、通信が遅くなる</dt>
  * <dd>純粋な変換器と比較して、サーボとの通信に要する時間が長くなることがあります。
  * </dd>
  * 
  * <dt>PC〜RPU間、およびRPU〜サーボ間の通信速度は115.2kbpsのみ</dt>
  * <dd></dd>
  * 
  * <dt>[注意]RPU-10を介してサーボの通信速度設定を変更されると、RPU-10経由ではそのサーボと通信できなくなります。</dt>
  * <dd>通信速度の再変更には、RSC-U485等通信速度を変更可能なRS485通信機器が必要となりますのでご注意ください。</dd>
  * 
  * </dl>
  * 
  * TODO 続き書く
  */
 public class RS485 implements Sendable {
 
     public static final class Builder {
 
         private final RS485 packet = new RS485();
 
         private Builder() {
         }
 
         public RS485.Builder data(Packet newData) {
             packet.setData(newData);
             return this;
         }
 
         public RS485.Builder footer(byte newFooter) {
             packet.setFooter(newFooter);
             packet.setFooterSet(true);
             return this;
         }
 
         public RS485.Builder footer(int newFooter) {
             return footer(Bytes.of(newFooter));
         }
 
         public RS485 build() {
             return packet;
         }
     }
 
     private static RS485.Builder builder() {
         return new RS485.Builder();
     }
 
    private static final byte[] HEADER_WITHOUT_RETURN = { Bytes.of(0x53) };
    private static final byte[] HEADER_WITH_RETURN = { Bytes.of(0x54) };

     private byte[] header;
     private Packet data;
     private byte footer;
     private boolean footerSet;
 
     RS485() {
        setHeader(HEADER_WITHOUT_RETURN);
     }
 
     public static RS485.Builder data(Packet newData) {
         return builder().data(newData);
     }
 
     public byte[] header() {
         return header;
     }
 
     public Packet data() {
         return data;
     }
 
     public byte footer() {
         return footer;
     }
 
     void setHeader(byte[] newHeader) {
         header = newHeader;
     }
 
     void setData(Packet newData) {
         data = newData;
     }
 
     void setFooter(byte newFooter) {
         footer = newFooter;
     }
 
     void setFooterSet(boolean newFooterSet) {
         footerSet = newFooterSet;
     }
 
     @Override
     public void send(OutputStream out) throws IOException {
         // Write RPU-10 Header
        out.write(footerSet ? HEADER_WITH_RETURN : header);
 
         // Write RPU-10 Length
         byte[] rawData = Bytes.of(data);
 
         int length = rawData.length;
         if (footerSet) {
             ++length;
         }
         out.write(new byte[] { Bytes.of(length) });
 
         // Write Data
         out.write(rawData);
 
         // Write footer if necessary
         if (footerSet) {
             out.write(new byte[] { footer });
         }
 
         out.flush();
     }
 }
