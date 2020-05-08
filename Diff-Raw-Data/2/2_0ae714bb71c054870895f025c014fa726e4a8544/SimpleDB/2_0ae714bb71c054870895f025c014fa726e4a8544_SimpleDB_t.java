 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.sdb;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.typica.sdb.jaxb.CreateDomainResponse;
 import com.xerox.amazonws.typica.sdb.jaxb.DeleteDomainResponse;
 import com.xerox.amazonws.typica.sdb.jaxb.ListDomainsResponse;
 
 /**
  * This class provides an interface with the Amazon SDB service. It provides high level
  * methods for listing and creating and deleting domains.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class SimpleDB extends AWSQueryConnection {
 
     private static Log logger = LogFactory.getLog(SimpleDB.class);
 
 	/**
 	 * Initializes the sdb service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
 	 */
     public SimpleDB(String awsAccessId, String awsSecretAccessKey) {
         this(awsAccessId, awsSecretAccessKey, true);
     }
 
 	/**
 	 * Initializes the sdb service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from SDB.
 	 */
     public SimpleDB(String awsAccessId, String awsSecretAccessKey, boolean isSecure) {
         this(awsAccessId, awsSecretAccessKey, isSecure, "sds.amazonaws.com");
     }
 
 	/**
 	 * Initializes the sdb service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from SDB.
      * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
 	 */
     public SimpleDB(String awsAccessId, String awsSecretAccessKey, boolean isSecure,
                              String server)
     {
         this(awsAccessId, awsSecretAccessKey, isSecure, server,
              isSecure ? 443 : 80);
     }
 
     /**
 	 * Initializes the sdb service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from SDB.
      * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
      * @param port Which port to use.
      */
     public SimpleDB(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure,
                              String server, int port)
     {
 		super(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port);
 		ArrayList vals = new ArrayList();
 		vals.add("2007-11-07");
 		super.headers.put("Version", vals);
     }
 
 	/**
 	 * Terminates a selection of running instances.
 	 * 
 	 * @return A list of {@link Domain} instances.
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public ListDomainsResult listDomains() throws SDBException {
 		return this.listDomains(null);
 	}
 
 	/**
 	 * Terminates a selection of running instances.
 	 * 
 	 * @param instanceIds A list of instances ({@link ReservationDescription.Instance#instanceId}.
 	 * @return A list of {@link Domain} instances.
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public ListDomainsResult listDomains(String nextToken) throws SDBException {
 		return this.listDomains(nextToken, 0);
 	}
 
 	/**
 	 * Creates a domain. If domain already exists, no error is thrown.
 	 *
 	 * @param name name of the new domain
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public Domain createDomain(String name) throws SDBException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("DomainName", name);
 		GetMethod method = new GetMethod();
 		try {
 			CreateDomainResponse response =
 						makeRequest(method, "CreateDomain", params, CreateDomainResponse.class);
 			return new Domain(name, getAwsAccessKeyId(), getSecretAccessKey(),
 									isSecure(), getServer());
 		} catch (JAXBException ex) {
 			throw new SDBException("Problem parsing returned message.", ex);
 		} catch (HttpException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Deletes a domain.
 	 *
 	 * @param domain the domain to be deleted
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public void deleteDomain(Domain domain) throws SDBException {
 		deleteDomain(domain.getName());
 	}
 
 	/**
 	 * Deletes a domain.
 	 *
 	 * @param name the name of the domain to be deleted
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public void deleteDomain(String name) throws SDBException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("DomainName", name);
 		GetMethod method = new GetMethod();
 		try {
 			DeleteDomainResponse response =
 						makeRequest(method, "DeleteDomain", params, DeleteDomainResponse.class);
 		} catch (JAXBException ex) {
 			throw new SDBException("Problem parsing returned message.", ex);
 		} catch (HttpException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Method for getting a Domain object without getting a list of them.
 	 *
 	 * @param domainName the name of the domain to be returned
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public Domain getDomain(String domainName) throws SDBException {
 		return new Domain(domainName, getAwsAccessKeyId(), getSecretAccessKey(),
 								isSecure(), getServer());
 	}
 
 	/**
 	 * Gets a list of domains
 	 * 
 	 * @param nextToken token to use when retrieving next results
 	 * @param maxResults the max number of results to return (0 means no max defined)
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public ListDomainsResult listDomains(String nextToken, int maxResults) throws SDBException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (nextToken != null) {
 			params.put("NextToken", nextToken);
 		}
 		if (maxResults > 0) {
			params.put("MaxNumberOfDomains", ""+maxResults);
 		}
 		GetMethod method = new GetMethod();
 		try {
 			ListDomainsResponse response =
 						makeRequest(method, "ListDomains", params, ListDomainsResponse.class);
 			return new ListDomainsResult(response.getListDomainsResult().getNextToken(),
 							Domain.createList(response.getListDomainsResult().getDomainNames().toArray(new String[] {}),
 								getAwsAccessKeyId(), getSecretAccessKey(),
 								isSecure(), getServer()));
 		} catch (JAXBException ex) {
 			throw new SDBException("Problem parsing returned message.", ex);
 		} catch (HttpException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 }
