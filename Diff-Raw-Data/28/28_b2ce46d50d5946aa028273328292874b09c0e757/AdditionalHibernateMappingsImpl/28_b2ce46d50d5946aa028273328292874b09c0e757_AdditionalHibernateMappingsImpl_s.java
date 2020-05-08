 /**********************************************************************************
  *
  * $Header: /cvs/sakai2/kernel/active-tool-component/src/java/org/sakaiproject/component/kernel/tool/ActiveToolComponent.java,v 1.17 2005/05/26 20:19:27 ggolden.umich.edu Exp $
  *
  ***********************************************************************************
  *
  * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
  *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
  *
  * Licensed under the Educational Community License Version 1.0 (the "License");
  * By obtaining, using and/or copying this Original Work, you agree that you have read,
  * understand, and will comply with the terms and conditions of the Educational Community License.
  * You may obtain a copy of the License at:
  *
  *      http://cvs.sakaiproject.org/licenses/license_1_0.html
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  **********************************************************************************/
 
 package org.sakaiproject.springframework.orm.hibernate.impl;
 
 import java.io.IOException;
 
import net.sf.hibernate.MappingException;
import net.sf.hibernate.cfg.Configuration;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 
 public class AdditionalHibernateMappingsImpl implements
     AdditionalHibernateMappings, Comparable
 {
   protected final transient Log logger = LogFactory.getLog(getClass());
 
   private Resource[] mappingLocations;
    private Integer sortOrder = new Integer(Integer.MAX_VALUE);
 
   public void setMappingResources(String[] mappingResources)
   {
     this.mappingLocations = new Resource[mappingResources.length];
     for (int i = 0; i < mappingResources.length; i++)
     {
       this.mappingLocations[i] = new ClassPathResource(mappingResources[i]
           .trim());
     }
   }
 
   public Resource[] getMappingLocations()
   {
     return mappingLocations;
   }
 
   public void processConfig(Configuration config) throws IOException,
       MappingException
   {
     for (int i = 0; i < this.mappingLocations.length; i++)
     {
       config.addInputStream(this.mappingLocations[i].getInputStream());
     }
   }
 
    public int compareTo(Object o) {
       return getSortOrder().compareTo(((AdditionalHibernateMappingsImpl)o).getSortOrder());
    }
 
    public Integer getSortOrder() {
       return sortOrder;
    }
 
    public void setSortOrder(Integer sortOrder) {
       this.sortOrder = sortOrder;
    }
 }
