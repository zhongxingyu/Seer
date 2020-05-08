 /*
  * Copyright (c) 2013, Blackboard, Inc. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided with the distribution. 3. Neither the
  * name of the Blackboard Inc. nor the names of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * BLACKBOARD MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-
  * INFRINGEMENT. BLACKBOARD SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
  * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  */
 
 package blackboard.plugin.hayabusa.view;
 
 import blackboard.persist.PersistenceException;
 import blackboard.platform.context.ContextManagerFactory;
 import blackboard.platform.context.UnsetContextException;
 import blackboard.platform.plugin.*;
 import blackboard.platform.servlet.JspResourceIncludeUtil;
 import blackboard.platform.vxi.data.VirtualInstallation;
 import blackboard.platform.vxi.service.VirtualSystemException;
 import blackboard.servlet.renderinghook.RenderingHook;
 import blackboard.servlet.renderinghook.RenderingHookKey;
 
 import org.springframework.stereotype.Component;
 
 /**
  * Rendering hook to display Hayabusa command bar in top frame.
  * 
  * @author Noriaki Tatsumi
  * @since 1.0
  */
 @Component
 public class CommandBarRenderingHook implements RenderingHook
 {
   public static final String HANDLE = "hayabusa";
   public static final String VENDOR = "bb";
 
   private static final String DIV_START = "<div id=\"light\" class=\"lightbox_content\">";
   private static final String DIV_END = "</div>";
   private static final String FORM_START = "<form name=\"search_form\" id=\"searchForm\" method=\"GET\">";
   private static final String FORM_END = "</form>";
   private static final String INPUT_FIELD = "<input id=\"lightbox_input\" type=\"text\" x-webkit-speech autofocus /><button name=\"go\" id=\"go\">GO!</button>";
 
   @Override
   public String getContent()
   {
     String uriPrefix = "";
     try
     {
       uriPrefix = getUriPrefix();
     }
     catch ( VirtualSystemException | PersistenceException | UnsetContextException e )
     {
       e.printStackTrace();
     }
     JspResourceIncludeUtil resourceIncludeUtil = JspResourceIncludeUtil.getThreadInstance();
     resourceIncludeUtil.addCssFile( uriPrefix + "css/hayabusa-main.css" );
     resourceIncludeUtil.addCssFile( uriPrefix + "css/jquery-ui.css" );
     resourceIncludeUtil.addJsFile( uriPrefix + "js/mousetrap.min.js" );
    resourceIncludeUtil.addJsFile( uriPrefix + "js/mousetrap-global-bind.min.js" );
     resourceIncludeUtil.addJsFile( uriPrefix + "js/hayabusa-shortcutkeys.js" );
     resourceIncludeUtil.addJsFile( uriPrefix + "js/jquery-1.9.1.js" );
     resourceIncludeUtil.addJsFile( uriPrefix + "js/jquery-ui.js" );
     resourceIncludeUtil.addJsFile( uriPrefix + "js/main.js" );
     return constructForm();
   }
 
   private String constructForm()
   {
     return DIV_START + FORM_START + INPUT_FIELD + FORM_END + DIV_END;
   }
 
   private static String getUriPrefix() throws VirtualSystemException, PersistenceException, UnsetContextException
   {
     PlugIn plugIn = PlugInManagerFactory.getInstance().getPlugIn( VENDOR, HANDLE );
     VirtualInstallation vi = ContextManagerFactory.getInstance().getContext().getVirtualInstallation();
     return PlugInUtil.getUriStem( plugIn, vi );
   }
 
   @Override
   public String getKey()
   {
     return RenderingHookKey.Frameset.getKey();
   }
 }
