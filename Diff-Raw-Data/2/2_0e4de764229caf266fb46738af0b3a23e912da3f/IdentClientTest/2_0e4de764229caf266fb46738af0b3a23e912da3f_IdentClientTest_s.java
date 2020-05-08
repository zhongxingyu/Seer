 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.identd;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
 import com.dmdirc.interfaces.User;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.parser.irc.IRCClientInfo;
 import com.dmdirc.parser.irc.IRCParser;
 import com.dmdirc.util.SystemInfo;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Optional;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class IdentClientTest {
 
     @Mock private AggregateConfigProvider acp;
     @Mock private ConnectionManager sm;
     @Mock private Connection connection;
     @Mock private IRCParser parser;
     @Mock private IRCClientInfo client;
     @Mock private User user;
     @Mock private AggregateConfigProvider config;
     @Mock private DMDircMBassador eventBus;
     @Mock private SystemInfo systemInfo;
 
     protected IdentClient getClient() {
         final List<Connection> servers = new ArrayList<>();
         servers.add(connection);
 
         when(sm.getConnections()).thenReturn(servers);
         when(connection.getParser()).thenReturn(Optional.of(parser));
         when(connection.getLocalUser()).thenReturn(Optional.of(user));
         when(parser.getLocalPort()).thenReturn(60);
         when(parser.getLocalClient()).thenReturn(client);
         when(client.getNickname()).thenReturn("nickname");
         when(client.getUsername()).thenReturn("username");
         when(user.getNickname()).thenReturn("nickname");
         when(user.getUsername()).thenReturn(Optional.of("username"));
 
        return new IdentClient(eventBus, null, null, sm, config, "plugin-Identd", systemInfo);
     }
 
     @Test
     public void testInvalidIdent() {
         final String response = getClient().getIdentResponse("invalid request!", acp);
 
         assertContains("Illegal requests must result in an ERROR response",
                 response, "ERROR");
     }
 
     @Test
     public void testQuoting() {
         final String response = getClient().getIdentResponse("in\\valid:invalid", acp);
 
         assertStartsWith("Special chars in illegal requests must be quoted",
                 response, "in\\\\valid\\:invalid");
     }
 
     @Test
     public void testQuoting2() {
         final String response = getClient().getIdentResponse("in\\\\valid\\ inv\\:alid", acp);
 
         assertStartsWith("Escaped characters in illegal requests shouldn't be doubly-escaped",
                 response, "in\\\\valid\\ inv\\:alid");
     }
 
     @Test
     public void testNonNumericPort() {
         final String response = getClient().getIdentResponse("abc, def", acp);
 
         assertContains("Non-numeric ports must result in an ERROR response",
                 response, "ERROR");
         assertStartsWith("Specified ports must be returned in the response",
                 response.replaceAll("\\s+", ""), "abc,def:");
     }
 
     private void doPortTest(final String ports) {
         final String response = getClient().getIdentResponse(ports, acp);
 
         assertContains("Illegal ports must result in an ERROR response",
                 response, "ERROR");
         assertContains("Illegal ports must result in an INVALID-PORT response",
                 response, "INVALID-PORT");
         assertStartsWith("Port numbers must be returned as part of the response",
                 response.replaceAll("\\s+", ""), ports.replaceAll("\\s+", ""));
     }
 
     @Test
     public void testOutOfRangePorts() {
         doPortTest("0, 50");
         doPortTest("65536, 50");
         doPortTest("50, 0");
         doPortTest("50, 65536");
     }
 
     @Test
     public void testAlwaysOn() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(false);
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertContains("Unknown port requests must return an ERROR response",
                 response, "ERROR");
         assertContains("Unknown port requests must return a NO-USER response",
                 response, "NO-USER");
     }
 
     @Test
     public void testHidden() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(true);
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertContains("Hidden requests must return an ERROR response",
                 response, "ERROR");
         assertContains("Hidden requests must return a HIDDEN-USER response",
                 response, "HIDDEN-USER");
     }
 
     @Test
     public void testSystemNameQuoting() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(false);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(true);
         when(acp.getOption("plugin-Identd", "advanced.customSystem")).thenReturn("a:b\\c,d");
         when(acp.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(false);
         when(acp.getOption("plugin-Identd", "general.customName")).thenReturn("");
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertContains("Special characters must be quoted in system names",
                 response, "a\\:b\\\\c\\,d");
     }
 
     @Test
     public void testCustomNameQuoting() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(false);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(acp.getOption("plugin-Identd", "advanced.customSystem")).thenReturn("");
         when(acp.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(true);
         when(acp.getOption("plugin-Identd", "general.customName")).thenReturn("a:b\\c,d");
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertContains("Special characters must be quoted in custom names",
                 response, "a\\:b\\\\c\\,d");
     }
 
     @Test
     public void testCustomNames() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(false);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(true);
         when(acp.getOption("plugin-Identd", "advanced.customSystem")).thenReturn("system");
         when(acp.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(true);
         when(acp.getOption("plugin-Identd", "general.customName")).thenReturn("name");
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("50, 60", acp);
         final String[] bits = response.split(":");
 
         assertTrue("Responses must include port pair",
                 bits[0].matches("\\s*50\\s*,\\s*60\\s*"));
         assertEquals("Positive response must include USERID",
                 "USERID", bits[1].trim());
         assertEquals("Must use custom system name", "system", bits[2].trim());
         assertEquals("Must use custom name", "name", bits[3].trim());
     }
 
     @Test
     public void testOSWindows() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("windows");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : WIN32 : test", response);
     }
 
     @Test
     public void testOSMac() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("mac");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : MACOS : test", response);
     }
 
     @Test
     public void testOSLinux() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("linux");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : UNIX : test", response);
     }
 
     @Test
     public void testOSBSD() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("bsd");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : UNIX-BSD : test", response);
     }
 
     @Test
     public void testOSOS2() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("os/2");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : OS/2 : test", response);
     }
 
     @Test
     public void testOSUnix() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("unix");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : UNIX : test", response);
     }
 
     @Test
     public void testOSIrix() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("irix");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : IRIX : test", response);
     }
 
     @Test
     public void testOSUnknown() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : UNKNOWN : test", response);
     }
 
     @Test
     public void testNameCustom() {
         when(acp.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
         when(acp.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(true);
         when(acp.getOption("plugin-Identd", "general.customName")).thenReturn("name");
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("50, 50", acp);
         assertEquals("50 , 50 : USERID : UNKNOWN : name", response);
     }
 
     @Test
     public void testNameNickname() {
         when(acp.getOptionBool("plugin-Identd", "general.useNickname")).thenReturn(true);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("60, 50", acp);
         assertEquals("60 , 50 : USERID : UNKNOWN : nickname", response);
     }
 
     @Test
     public void testNameUsername() {
         when(acp.getOptionBool("plugin-Identd", "general.useUsername")).thenReturn(true);
         when(systemInfo.getProperty("user.name")).thenReturn("test");
         when(systemInfo.getProperty("os.name")).thenReturn("test");
 
         final String response = getClient().getIdentResponse("60, 50", acp);
         assertEquals("60 , 50 : USERID : UNKNOWN : username", response);
     }
 
     private static void assertContains(final String msg, final String haystack,
             final CharSequence needle) {
         assertTrue(msg, haystack.contains(needle));
     }
 
     private static void assertStartsWith(final String msg, final String haystack,
             final String needle) {
         assertTrue(msg, haystack.startsWith(needle));
     }
 
 }
