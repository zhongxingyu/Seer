 /*
  * Copyright (C) 2003-2007 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 
 package org.exoplatform.applications.ooplugin;
 
 import com.sun.star.awt.Rectangle;
 import com.sun.star.awt.VclWindowPeerAttribute;
 import com.sun.star.awt.WindowAttribute;
 import com.sun.star.awt.WindowClass;
 import com.sun.star.awt.WindowDescriptor;
 import com.sun.star.awt.XControlContainer;
 import com.sun.star.awt.XDialog;
 import com.sun.star.awt.XMessageBox;
 import com.sun.star.awt.XToolkit;
 import com.sun.star.awt.XWindowPeer;
 import com.sun.star.frame.XFrame;
 import com.sun.star.lang.XComponent;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 
 import org.exoplatform.applications.ooplugin.dialog.DialogBuilder;
 import org.exoplatform.applications.ooplugin.dialog.EventHandler;
 import org.exoplatform.applications.ooplugin.utils.TextUtils;
 import org.exoplatform.applications.ooplugin.utils.WebDavUtils;
 import org.exoplatform.common.http.HTTPStatus;
 import org.exoplatform.common.http.client.HTTPConnection;
 import org.exoplatform.common.http.client.HTTPResponse;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 /**
  * Created by The eXo Platform SAS Author.
  * 
  * @author <a href="mailto:gavrikvetal@gmail.com">Vitaly Guly</a>
  * @version $Id: $
  */
 
 public class PlugInDialog
 {
 
    private static final Log LOG = ExoLogger.getLogger(PlugInDialog.class);
 
    protected String dialogName = "";
 
    protected boolean enabled = false;
 
    protected XComponentContext xComponentContext;
 
    protected XFrame xFrame;
 
    protected XToolkit xToolkit;
 
    protected XDialog xDialog;
 
    protected XControlContainer xControlContainer;
 
    protected ArrayList<EventHandler> eventHandlers = new ArrayList<EventHandler>();
 
    protected WebDavConfig config;
 
    public PlugInDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit)
    {
       this.config = config;
       this.xComponentContext = xComponentContext;
       this.xFrame = xFrame;
       this.xToolkit = xToolkit;
    }
 
    public void setEnabled(boolean enabled)
    {
       this.enabled = enabled;
    }
 
    public boolean isEnableg()
    {
       return enabled;
    }
 
    public String getDialogName()
    {
       return dialogName;
    }
 
    public XControlContainer getControlContainer()
    {
       return xControlContainer;
    }
 
    public void setControlContainer(XControlContainer xControlContainer)
    {
       this.xControlContainer = xControlContainer;
    }
 
    public XComponentContext getConponentContext()
    {
       return xComponentContext;
    }
 
    public void addHandler(String componentName, int componentType, Object listener)
    {
       EventHandler handler = new EventHandler(componentName, componentType, listener);
       eventHandlers.add(handler);
    }
 
    public boolean launchBeforeOpen()
    {
       return true;
    }
 
    public void createDialog() throws com.sun.star.uno.Exception
    {
       if ("".equals(dialogName))
       {
          return;
       }
 
       try
       {
          DialogBuilder builder = new DialogBuilder(this, xFrame, xToolkit);
          builder.init();
          Object dialog = builder.createDialog(dialogName, eventHandlers);
 
          xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);
 
          if (launchBeforeOpen())
          {
             enabled = true;
             xDialog.execute();
          }
 
          XComponent xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, dialog);
          xComponent.dispose();
 
       }
       catch (java.lang.Exception exc)
       {
          LOG.info("Unhandled exception: " + exc.getMessage(), exc);
       }
 
    }
 
    protected void prepareTmpPath(String tempPath)
    {
       if (tempPath.lastIndexOf("/") != 0)
       {
          tempPath = tempPath.substring(0, tempPath.lastIndexOf("/"));
       }
       else
       {
          tempPath = "/";
       }
 
       String documentPath =
                LocalFileSystem.getDocumentsPath() + File.separatorChar + LocalFileSystem.STORAGEDIR
                         + File.separatorChar + config.getWorkSpace() + tempPath;
       documentPath = documentPath.replace("\\", "/");
 
       File outDirectory = new File(documentPath);
       if (!outDirectory.exists())
       {
          outDirectory.mkdirs();
       }
 
    }
 
    protected void doOpenRemoteFile(String href) throws Exception
    {
       String serverPrefix = config.getServerPrefix();
 
       if (!href.startsWith(serverPrefix))
       {
          return;
       }
 
       String resourcePath = href.substring(serverPrefix.length());
 
       String[] pathSegments = resourcePath.split("/");
       StringBuffer sb = new StringBuffer();
 
       for (String segment : pathSegments)
       {
          sb.append(TextUtils.DecodePath(segment));
       }
       resourcePath = sb.toString();
 
       if (!resourcePath.startsWith("/"))
       {
          resourcePath = "/" + resourcePath;
       }
 
       HTTPConnection connection = WebDavUtils.getAuthConnection(config);
      HTTPResponse response = connection.Get(href);
 
       int status = response.getStatusCode();
 
       if (status != HTTPStatus.OK)
       {
          showMessageBox("Can't open remote file. ErrorCode: " + status);
          return;
       }
 
       prepareTmpPath(resourcePath);
 
       String filePath =
                LocalFileSystem.getDocumentsPath() + File.separatorChar + LocalFileSystem.STORAGEDIR
                         + File.separatorChar + config.getWorkSpace() + resourcePath;
       filePath = filePath.replace("\\", "/");
 
       filePath = filePath.replace("?", ".");
 
       File outFile = new File(filePath);
       if (outFile.exists())
       {
          outFile.delete();
       }
 
       outFile.createNewFile();
       FileOutputStream fileOutStream = new FileOutputStream(outFile);
 
       byte[] fileContent = response.getData();
 
       fileOutStream.write(fileContent);
       fileOutStream.close();
 
       OOUtils.loadFromFile(xComponentContext, filePath, resourcePath);
    }
 
    public void showMessageBox(String sMessage)
    {
       try
       {
          if (null != xFrame && null != xToolkit)
          {
             WindowDescriptor aDescriptor = new WindowDescriptor();
             aDescriptor.Type = WindowClass.MODALTOP;
             aDescriptor.WindowServiceName = new String("infobox");
             aDescriptor.ParentIndex = -1;
             aDescriptor.Parent =
                      (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xFrame.getContainerWindow());
             aDescriptor.Bounds = new Rectangle(0, 0, 300, 200);
             aDescriptor.WindowAttributes =
                      WindowAttribute.BORDER | WindowAttribute.MOVEABLE | WindowAttribute.CLOSEABLE;
 
             XWindowPeer xPeer = xToolkit.createWindow(aDescriptor);
             if (null != xPeer)
             {
                XMessageBox xMsgBox = (XMessageBox) UnoRuntime.queryInterface(XMessageBox.class, xPeer);
                if (null != xMsgBox)
                {
                   xMsgBox.setCaptionText("eXo-Platform OOPlug-In.");
                   xMsgBox.setMessageText(sMessage);
                   xMsgBox.execute();
                }
             }
          }
       }
       catch (com.sun.star.uno.Exception e)
       {
          LOG.info("Unhandled exception: " + e.getMessage(), e);
       }
    }
 
    public short confirmMessageBox(String sMessage)
    {
       try
       {
          if (null != xFrame && null != xToolkit)
          {
             WindowDescriptor aDescriptor = new WindowDescriptor();
             aDescriptor.Type = WindowClass.MODALTOP;
             aDescriptor.WindowServiceName = new String("querybox");
             aDescriptor.ParentIndex = -1;
             aDescriptor.Parent =
                      (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xFrame.getContainerWindow());
             aDescriptor.Bounds = new Rectangle(0, 0, 350, 200);
             aDescriptor.WindowAttributes =
                      WindowAttribute.BORDER | WindowAttribute.MOVEABLE | WindowAttribute.CLOSEABLE
                               | VclWindowPeerAttribute.YES_NO_CANCEL;
 
             XWindowPeer xPeer = xToolkit.createWindow(aDescriptor);
             if (null != xPeer)
             {
                XMessageBox xMsgBox = (XMessageBox) UnoRuntime.queryInterface(XMessageBox.class, xPeer);
                if (null != xMsgBox)
                {
                   xMsgBox.setCaptionText("eXo-Platform OOPlug-In.");
                   xMsgBox.setMessageText(sMessage);
                   return xMsgBox.execute();
                }
             }
          }
       }
       catch (com.sun.star.uno.Exception e)
       {
          LOG.info("Unhandled exception" + e.getMessage(), e);
       }
       return 3;
    }
 
 }
