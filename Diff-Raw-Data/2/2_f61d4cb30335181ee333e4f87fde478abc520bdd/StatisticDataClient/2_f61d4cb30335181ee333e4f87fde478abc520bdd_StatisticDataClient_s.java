 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.common.client.servlet.sm;
 
import de.escidoc.core.sm.StatisticDataHandler;
import de.escidoc.core.sm.StatisticDataHandlerServiceLocator;
 import de.escidoc.core.test.common.client.servlet.ClientBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 
 import javax.xml.rpc.ServiceException;
 import java.rmi.Remote;
 
 /**
  * Offers access methods to the escidoc REST and SOAP interface of the Statistic
  * Data resource.
  * 
  * @author MIH
  * 
  */
 public class StatisticDataClient extends ClientBase {
 
     //private StatisticDataHandler soapClient = null;
 
     /**
      * 
      * @param transport
      *            The transport identifier.
      */
     public StatisticDataClient(final int transport) {
         super(transport);
 
     }
 
     /**
      * Create an item in the escidoc framework.
      * 
      * @param aggregationXml
      *            The xml representation of the aggregation.
      * @return The HttpMethod after the service call (REST) or the result object
      *         (SOAP).
      * @throws Exception
      *             If the service call fails.
      */
     public Object create(final Object statisticDataXml) throws Exception {
 
         return callEsciDoc("StatisticData.create", METHOD_CREATE,
             Constants.HTTP_METHOD_PUT, Constants.STATISTIC_DATA_BASE_URI,
             new String[] {}, changeToString(statisticDataXml));
     }
 
     /**
      * 
      * @return Returns the soapClient.
      * @throws ServiceException
      *             If service instantiation fails.
      */
     public Remote getSoapClient() throws ServiceException {
 
         /*if (soapClient == null) {
             StatisticDataHandlerServiceLocator serviceLocator =
                 new StatisticDataHandlerServiceLocator(getEngineConfig());
             serviceLocator
                 .setStatisticDataHandlerServiceEndpointAddress(checkSoapAddress(serviceLocator
                     .getStatisticDataHandlerServiceAddress()));
             soapClient = serviceLocator.getStatisticDataHandlerService();
         }
         return soapClient;*/
         return null;
     }
 
 }
