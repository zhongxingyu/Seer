 /*
 * Copyright 2010-2011 Systemic Pty Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
 package sifdemo.publisher;
 
 import openadk.library.ADKException;
 import openadk.library.Query;
 import openadk.library.SIFException;
 import openadk.library.Zone;
 import openadk.library.school.SchoolDTD;
 
 import sifdemo.publisher.iterator.SchoolInfoEventIterator;
 import sifdemo.publisher.iterator.SchoolInfoResponseIterator;
import sifdemo.publisher.iterator.XMLFileEventIterator;
import sifdemo.publisher.iterator.XMLFileResponseIterator;
 
 import systemic.sif.sifcommon.publisher.BasePublisher;
 import systemic.sif.sifcommon.publisher.SIFEventIterator;
 import systemic.sif.sifcommon.publisher.SIFResponseIterator;
 import au.com.systemic.framework.dao.connection.ConnectionManager;
 
 public class SchoolInfoPublisher extends BasePublisher
 {
 	public SchoolInfoPublisher(String publisherID)
 	{
 		super(publisherID);
 		setDtd(SchoolDTD.SCHOOLINFO);
 	}
 
 	@Override
     public void finalise()
     {
 	    logger.debug("Finalise for "+getId()+": Release Connection Pool." );
//        ConnectionManager.closeConnectionPools();
//	    logger.debug("Connecction Pool for "+getId()+" released." );
     }

 	@Override
     public SIFResponseIterator getRequestedSIFObjects(Query query, Zone zone) throws ADKException, SIFException
     {
 		logger.debug("getRequestedSIFObjects called for publisher: "+getId());
		return new XMLFileResponseIterator(this);
     }
 
 	@Override
     public SIFEventIterator getSIFEvents() throws ADKException
     {
 		logger.debug("getSIFEvents called for publisher: "+getId());
		return new XMLFileEventIterator(this);
     }

//	@Override
//    public SIFResponseIterator getRequestedSIFObjects(Query query, Zone zone) throws ADKException, SIFException
//    {
//		logger.debug("getRequestedSIFObjects called for publisher: "+getId());
//		return new SchoolInfoResponseIterator();
//    }
//
//	@Override
//    public SIFEventIterator getSIFEvents() throws ADKException
//    {
//		logger.debug("getSIFEvents called for publisher: "+getId());
//		return new SchoolInfoEventIterator();
//    }
 }
