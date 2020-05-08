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
 package org.jboss.deployers.plugins.deployers.helpers;
 
 import java.io.InputStream;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 
 import org.jboss.deployers.spi.deployer.DeploymentUnit;
 import org.jboss.util.xml.DOMWriter;
 import org.jboss.util.xml.JBossErrorHandler;
 import org.jboss.virtual.VirtualFile;
 import org.w3c.dom.Document;
 
 /**
  * SchemaResolverDeployer.
  * 
  * @param <T> the expected type 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public abstract class XSLDeployer<T> extends JAXPDeployer<T>
 {
    /** The xsl resource path */
    protected String xslPath;
 
    /** The templates */
    private Templates templates;
 
    /**
     * Create a new XSLDeployer.
     * 
     * @param deploymentType the deployment type
     * @throws IllegalArgumentException for a null deployment type
     */
    public XSLDeployer(Class<T> deploymentType)
    {
       super(deploymentType);
    }
 
    /**
     * Get the xslPath.
     * 
     * @return the xslPath.
     */
    public String getXSLPath()
    {
       return xslPath;
    }
 
    /**
     * Set the xslPath.
     * 
     * @param xslPath the xslPath.
     */
    public void setXSLPath(String xslPath)
    {
       this.xslPath = xslPath;
    }
 
    /**
     * Get the templates.
     * 
     * @return the templates.
     * @throws IllegalStateException if the create method has not been invoked
     */
    public Templates getTemplates()
    {
       if (templates == null)
          throw new IllegalStateException("Templates have not been constructed");
       return templates;
    }
 
    /**
     * Create lifecycle
     * 
     * @throws Exception for any problem
     */
    public void create() throws Exception
    {
       super.create();
 
       TransformerFactory tf = TransformerFactory.newInstance();
       
       InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslPath);
       try
       {
          StreamSource ss = new StreamSource(is);
          ss.setSystemId(xslPath);
          templates = tf.newTemplates(ss);
          log.debug("Created templates: " + templates);
       }
       finally
       {
          try
          {
             is.close();
          }
          catch (Exception ignored)
          {
          }
       }
    }
 
    /**
     * Destroy lifecycle
     */
    public void destroy()
    {
       super.destroy();
       templates = null;
    }
   
   @Override
   protected T parse(DeploymentUnit unit, VirtualFile file, T root) throws Exception
    {
       if (file == null)
          throw new IllegalArgumentException("Null file");
 
       Document document = doParse(unit, file);
 
       Transformer trans = getTemplates().newTransformer();
       trans.setErrorListener(new JBossErrorHandler(file.getPathName(), null));
       Source s = new DOMSource(document);
       DOMResult r = new DOMResult();
       setParameters(trans);
       
       trans.transform(s, r);
       
       document = (Document) r.getNode();
       String docStr = DOMWriter.printNode(document, true);
       log.debug("Transformed " + file.getPathName() + " into " + docStr);
       
       return parse(unit, file, document);
    }
 
    /**
     * Set parameters for the transformation
     * 
     * @param trans the transformer
     * @throws Exception for any problem
     */
    protected void setParameters(Transformer trans) throws Exception
    {
       // nothing by default
    }
 
    /**
     * Parse a deployment
     * 
     * @param unit the deployment unit
     * @param file the metadata file
     * @param document the document
     * @return the metadata
     * @throws Exception for any error
     */
    protected abstract T parse(DeploymentUnit unit, VirtualFile file, Document document) throws Exception;
 }
