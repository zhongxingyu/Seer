 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.deployers.vfs.structure.file.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.test.BaseTestCase;
 import org.jboss.test.deployers.vfs.structure.file.support.BshFileMatcher;
 import org.jboss.test.deployers.vfs.structure.file.support.TmpFileStructure;
 import org.jboss.virtual.VirtualFile;
 import org.jboss.virtual.spi.VFSContext;
 import org.jboss.virtual.spi.VirtualFileHandler;
 
 /**
  * FileMatcherTestCase.
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  */
 public class FileMatcherTestCase extends BaseTestCase
 {
    public static Test suite()
    {
       return new TestSuite(FileMatcherTestCase.class);
    }
 
    public FileMatcherTestCase(String name)
    {
       super(name);
    }
 
    protected VirtualFile getVirtualFile() throws Throwable
    {
       return new MyVirtualFile();
    }
 
    public void testMatcher() throws Throwable
    {
       BasicBootstrap bootstrap = new BasicBootstrap();
       bootstrap.run();
       Kernel kernel = bootstrap.getKernel();
       KernelController controller = kernel.getController();
       try
       {
          BeanMetaData fsMD = new AbstractBeanMetaData("fileStructure", TmpFileStructure.class.getName());
          KernelControllerContext fsCC = controller.install(fsMD);
          assertEquals(fsCC.getState(), ControllerState.INSTALLED);
          TmpFileStructure fs = (TmpFileStructure)fsCC.getTarget();
          assertNotNull(fs);
 
          VirtualFile file = getVirtualFile();
          assertFalse(fs.checkFileMatchers(file));
 
          BeanMetaData fmMD = new AbstractBeanMetaData("bshFileMatcher", BshFileMatcher.class.getName());
          controller.install(fmMD);
 
          assertTrue(fs.checkFileMatchers(file));
 
          controller.uninstall(fmMD.getName());
 
          assertFalse(fs.checkFileMatchers(file));
       }
       finally
       {
          controller.shutdown();
       }
    }
 
    private static class MyVirtualFile extends VirtualFile
    {
      /** The serialVersionUID */
      private static final long serialVersionUID = 1L;

       public MyVirtualFile()
       {
          super(getVirtualFileHandler());
       }
 
       private static VirtualFileHandler getVirtualFileHandler()
       {
          return new VirtualFileHandler()
          {
            /** The serialVersionUID */
            private static final long serialVersionUID = 1L;

             public String getName()
             {
                return null;
             }
 
             public String getPathName()
             {
                return null;
             }
 
             public URL toVfsUrl() throws MalformedURLException, URISyntaxException
             {
                return null;
             }
 
             public URI toURI() throws URISyntaxException
             {
                return null;
             }
 
             public URL toURL() throws MalformedURLException, URISyntaxException
             {
                return null;
             }
 
             public long getLastModified() throws IOException
             {
                return 0;
             }
 
             public boolean hasBeenModified() throws IOException
             {
                return false;
             }
 
             public long getSize() throws IOException
             {
                return 0;
             }
 
             public boolean exists() throws IOException
             {
                return false;
             }
 
             public boolean isLeaf() throws IOException
             {
                return false;
             }
 
             public boolean isHidden() throws IOException
             {
                return false;
             }
 
             public InputStream openStream() throws IOException
             {
                return null;
             }
 
             public VirtualFileHandler getParent() throws IOException
             {
                return null;
             }
 
             public List<VirtualFileHandler> getChildren(boolean ignoreErrors) throws IOException
             {
                return null;
             }
 
             public VirtualFileHandler findChild(String path) throws IOException
             {
                return null;
             }
 
             public VFSContext getVFSContext()
             {
                return null;
             }
 
             public VirtualFile getVirtualFile()
             {
                return null;
             }
 
             public void close()
             {
                
             }
          };
       }
 
       public String getName()
       {
          return "somefile.bsh";
       }
    }
 }
