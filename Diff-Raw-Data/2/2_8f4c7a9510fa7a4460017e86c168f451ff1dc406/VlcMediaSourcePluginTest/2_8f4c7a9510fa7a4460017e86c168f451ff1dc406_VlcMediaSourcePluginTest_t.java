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
 
 package com.dmdirc.addons.mediasource_vlc;
 
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.util.io.FileUtils;
 import com.dmdirc.util.io.TextFile;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class VlcMediaSourcePluginTest {
 
     @Test
     public void testProcessInformation1() throws IOException, URISyntaxException,
             UnsupportedEncodingException {
         final PluginInfo pluginInfo = mock(PluginInfo.class);
         final IdentityController identityController = mock(IdentityController.class);
        final VlcManager plugin = new VlcManager(pluginInfo, identityController);
 
         final TextFile index = new TextFile(FileUtils.getPathForResource(
                 getClass().getResource("index-1.html")), Charset.forName("UTF-8"));
         final TextFile info = new TextFile(FileUtils.getPathForResource(
                 getClass().getResource("info-1.html")), Charset.forName("UTF-8"));
 
         plugin.parseInformation(info.getLines(), index.getLines());
 
         // This doesn't work anymore because getState() calls fetchInformation()
         // which overwrites the stuff set by the parseInformation() call.
         // assertTrue(plugin.getState() == MediaSourceState.PLAYING);
 
         assertEquals("Manic Street Preachers", plugin.getArtist());
         assertEquals("Send Away The Tigers", plugin.getAlbum());
         assertEquals("The Second Great Depression", plugin.getTitle());
         assertEquals("128 kb/s", plugin.getBitrate());
         assertEquals("VLC", plugin.getAppName());
         assertEquals("mpga", plugin.getFormat());
         assertEquals("249", plugin.getLength());
         assertEquals("38", plugin.getTime());
     }
 
 }
