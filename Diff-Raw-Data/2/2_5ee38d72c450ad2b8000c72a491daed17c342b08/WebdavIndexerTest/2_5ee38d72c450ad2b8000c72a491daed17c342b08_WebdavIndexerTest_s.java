 /*
  * Copyright (C) 2010 eXo Platform SAS.
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
 package org.exoplatform.jcr.benchmark.jcrapi.webdav;
 
 import com.sun.japex.TestCase;
 
 import org.exoplatform.common.http.HTTPStatus;
 import org.exoplatform.common.http.client.HTTPResponse;
 import org.exoplatform.common.http.client.HttpOutputStream;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * @author <a href="mailto:dmitry.kataev@exoplatform.com">Dmytro Katayev</a>
  * @version $Id$
  *
  */
 public class WebdavIndexerTest extends AbstractWebdavTest
 {
 
    private ArrayList<TestResource> testResources = new ArrayList<TestResource>();
 
    private class TestResource
    {
       private String contentType;
 
       private String resourcePath;
 
       public TestResource(String resourcePath, String contentType) throws IOException
       {
          this.contentType = contentType;
          this.resourcePath = resourcePath;
       }
    }
 
    /**
     * @see org.exoplatform.jcr.benchmark.jcrapi.webdav.AbstractWebdavTest#doPrepare(com.sun.japex.TestCase, org.exoplatform.jcr.benchmark.jcrapi.webdav.WebdavTestContext)
     */
    @Override
    public void doPrepare(TestCase tc, WebdavTestContext context) throws Exception
    {
       JCRWebdavConnectionEx item = new JCRWebdavConnectionEx(context);
       rootNodeName = context.generateUniqueName("rootNode");
       item.addDir(rootNodeName);
 
       testResources.add(new TestResource("../resources/index/test_index.doc", "application/msword"));
       testResources.add(new TestResource("../resources/index/test_index.htm", "text/html"));
       testResources.add(new TestResource("../resources/index/test_index.xml", "text/xml"));
       testResources.add(new TestResource("../resources/index/test_index.ppt", "application/vnd.ms-powerpoint"));
       testResources.add(new TestResource("../resources/index/test_index.txt", "text/plain"));
       testResources.add(new TestResource("../resources/index/test_index.xls", "application/vnd.ms-excel"));
       // testTesources.add(new TestResource("../resources/index/test_index.pdf", "application/pdf"));
 
    }
 
    /**
     * @see org.exoplatform.jcr.benchmark.jcrapi.webdav.AbstractWebdavTest#createContent(java.lang.String, com.sun.japex.TestCase, org.exoplatform.jcr.benchmark.jcrapi.webdav.WebdavTestContext)
     */
    @Override
    protected void createContent(String parentNodeName, TestCase tc, WebdavTestContext context) throws Exception
    {
       // TODO Auto-generated method stub
 
    }
 
    /**
     * @see org.exoplatform.jcr.benchmark.jcrapi.webdav.AbstractWebdavTest#doRun(com.sun.japex.TestCase, org.exoplatform.jcr.benchmark.jcrapi.webdav.WebdavTestContext)
     */
    @Override
    public void doRun(TestCase tc, WebdavTestContext context) throws Exception
    {
       item = new JCRWebdavConnectionEx(context);
 
       try
       {
          HttpOutputStream outStream = new HttpOutputStream();
 
          int i = new Random().nextInt(6);
          FileInputStream inStream = new FileInputStream(testResources.get(i).resourcePath);
          String contentType = testResources.get(i).contentType;
 
          String nodeName = rootNodeName + "/" + context.generateUniqueName("node");
          HTTPResponse response = item.addNode(nodeName, outStream, contentType);
 
          writeToOutputStream(inStream, outStream);
 
          outStream.close();
 
          if (response.getStatusCode() != HTTPStatus.CREATED)
          {
            System.out.println("Server returned Status " + response.getStatusCode() + " : " + response.getData());
          }
       }
       finally
       {
          item.stop();
       }
 
    }
 
    private void writeToOutputStream(InputStream inStream, HttpOutputStream outStream) throws IOException
    {
       int b;
       while ((b = inStream.read()) != -1)
       {
          outStream.write(b);
       }
    }
 
    public void doFinish(TestCase tc, WebdavTestContext context) throws Exception
    {
       //super.doFinish(tc, context);
    }
 }
