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
 package com.uphyca.robots.core.test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import com.uphyca.robots.core.Bytes;
 import com.uphyca.robots.core.RS485;
 import com.uphyca.robots.core.ShortPacket;
 
 public class RS485Test {
 
     /**
      * パケットが正しく送信されること.
      * 
      * RPU-10技術資料:RS485通信機能について P.3  例1) ID-1のサーボをトルクONします より.
      */
     @Test
     public void testThatPacketSentForTorqueOn() throws IOException {
 
         // Given
         RS485 underTest = RS485.data(ShortPacket.id(0x01)
                                                 .flag(0x00)
                                                 .address(0x24)
                                                 .count(0x01)
                                                 .data(0x01)
                                                 .build())
                                .build();
 
         ByteArrayOutputStream out = Bytes.stream();
 
         // When
         underTest.send(out);
 
         // Then
         assertThat(out.toByteArray(), is(Bytes.of(0x53, 0x09, 0xFA, 0xAF, 0x01, 0x00, 0x24, 0x01, 0x01, 0x01, 0x24)));
     }
 
     /**
      * パケットが正しく送信されること.
      * 
      * RPU-10技術資料:RS485通信機能について P.3  例2) ID-1のサーボを90.0度(384H)に、5秒(01F4H)で動かします より.
      * (資料のSUMの値が誤っている 正:68H  誤:24)
      */
     @Test
     public void testThatPacketSentForMove90Degree() throws IOException {
 
         // Given
         RS485 underTest = RS485.data(ShortPacket.id(0x01)
                                                 .flag(0x00)
                                                 .address(0x1E)
                                                 .count(0x01)
                                                 .data(0x84, 0x03, 0xF4, 0x01)
                                                 .build())
                                .build();
 
         ByteArrayOutputStream out = Bytes.stream();
 
         // When
         underTest.send(out);
 
         // Then
         assertThat(out.toByteArray(), is(Bytes.of(0x53, 0x0C, 0xFA, 0xAF, 0x01, 0x00, 0x1E, 0x04, 0x01, 0x84, 0x03, 0xF4, 0x01, 0x68)));
     }
 
     /**
      * パケットが正しく送信されること.
      * 
      * RPU-10技術資料:RS485通信機能について P.4  例3) ID-1のメモリーマップNo.42(24H)からNo.43(2BH)の値をリターンさせます より.
      * (資料のSUMの値が誤っている 正:68H  誤:24)
      */
     @Test
     public void testThatPacketSentForReturnPacket() throws IOException {
 
         // Given
         RS485 underTest = RS485.data(ShortPacket.id(0x01)
                                                 .flag(0x0F)
                                                 .address(0x2A)
                                                 .length(0x02)
                                                 .count(0x00)
                                                 .build())
                                .footer(0x0A)
                                .build();
 
         ByteArrayOutputStream out = Bytes.stream();
 
         // When
         underTest.send(out);
 
         // Then
        assertThat(out.toByteArray(), is(Bytes.of(0x54, 0x09, 0xFA, 0xAF, 0x01, 0x0F, 0x2A, 0x02, 0x00, 0x26, 0x0A)));
     }
 
 }
