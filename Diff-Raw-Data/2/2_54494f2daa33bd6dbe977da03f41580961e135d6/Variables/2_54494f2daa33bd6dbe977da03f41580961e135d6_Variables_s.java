 /*
  * Copyright (c) 2010,2011 Starschema Kft. - www.starschema.net
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.starschema.stagekillaz.ODI;
 
 // common stuff
 import com.starschema.stagekillaz.DataStageReader;
 import com.starschema.stagekillaz.KillaException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.apache.log4j.Logger;
 // ODI
 import oracle.odi.core.config.OdiConfigurationException;
 import oracle.odi.core.security.AuthenticationException;
 import oracle.odi.core.OdiInstance;
 import oracle.odi.core.persistence.IOdiEntityManager;
 import oracle.odi.core.persistence.transaction.ITransactionStatus;
 import oracle.odi.core.persistence.transaction.support.ITransactionCallback;
 import oracle.odi.core.persistence.transaction.support.TransactionTemplate;
 import oracle.odi.domain.project.OdiFolder;
 import oracle.odi.domain.project.OdiProject;
 import oracle.odi.domain.project.OdiVariable;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Variables
 {
   /* log4j logger */
 
   static Logger logger = Logger.getLogger(Variables.class);
 
   static void storeVariables(final Document dsExport, final SimpleOdiInstanceHandle odiInstanceHandle,
           final OdiProject project, final OdiFolder folder) throws KillaException
   {
     try {
       XPathFactory factory = XPathFactory.newInstance();
       XPath xpath = factory.newXPath();
       XPathExpression expr = xpath.compile("//Record/Collection[@Type='Parameters']/SubRecord");
 
       NodeList nodes = DataStageReader.executeXPath("//Record/Collection[@Type='Parameters']/SubRecord");
 
       for (int i = 0; i < nodes.getLength(); i++) {
         Node subRecord = nodes.item(i);
 
        final String name = DataStageReader.propertyValue(subRecord, "Prompt");
         final String prompt = DataStageReader.propertyValue(subRecord, "Prompt");
         final String def = DataStageReader.propertyValue(subRecord, "Default");
 
 
         TransactionTemplate tx = new TransactionTemplate(odiInstanceHandle.getOdiInstance().getTransactionManager());
         tx.execute(new ITransactionCallback()
         {
 
           public Object doInTransaction(ITransactionStatus pStatus)
           {
             IOdiEntityManager entityManager = odiInstanceHandle.getOdiInstance().getTransactionalEntityManager();
 
             logger.info("Adding parameter " + name + " (prompt: " + prompt + ", default: "
                     + def + ")");
 
             OdiVariable var = new OdiVariable(project, name);
             var.setDefaultValue(def);
             var.setDescription(prompt);
 
             entityManager.persist(var);
 
             return null;
           }
         });
 
 
       }
 
     } catch (XPathExpressionException exc) {
       throw new KillaException("Cannot search for parameters: " + exc.getMessage(),
               exc);
     }
   }
 }
