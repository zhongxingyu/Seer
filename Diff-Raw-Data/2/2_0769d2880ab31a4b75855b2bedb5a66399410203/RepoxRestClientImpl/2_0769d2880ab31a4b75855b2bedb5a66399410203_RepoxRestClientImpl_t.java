 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.repoxclient.rest;
 
 import org.joda.time.DateTime;
 import org.springframework.web.client.RestTemplate;
 import eu.europeana.uim.repox.AggregatorOperationException;
 import eu.europeana.uim.repox.DataSourceOperationException;
 import eu.europeana.uim.repox.HarvestingOperationException;
 import eu.europeana.uim.repox.ProviderOperationException;
 import eu.europeana.uim.repox.RecordOperationException;
 import eu.europeana.uim.repox.model.IngestFrequency;
 import eu.europeana.uim.repoxclient.jibxbindings.Aggregator;
 import eu.europeana.uim.repoxclient.jibxbindings.Aggregators;
 import eu.europeana.uim.repoxclient.jibxbindings.DataSources;
 import eu.europeana.uim.repoxclient.jibxbindings.Log;
 import eu.europeana.uim.repoxclient.jibxbindings.Provider;
 import eu.europeana.uim.repoxclient.jibxbindings.DataProviders;
 import eu.europeana.uim.repoxclient.jibxbindings.RecordResult;
 import eu.europeana.uim.repoxclient.jibxbindings.Response;
 import eu.europeana.uim.repoxclient.jibxbindings.RunningTasks;
 import eu.europeana.uim.repoxclient.jibxbindings.ScheduleTasks;
 import eu.europeana.uim.repoxclient.jibxbindings.Source;
 import eu.europeana.uim.repoxclient.jibxbindings.Success;
 import eu.europeana.uim.repoxclient.jibxbindings.HarvestingStatus;
 import eu.europeana.uim.repoxclient.jibxbindings._Error;
 import eu.europeana.uim.repoxclient.plugin.RepoxRestClient;
 
 /**
  * Class implementing REST functionality for accessing REPOX
  * 
  * @author Georgios Markakis
  * @author Yorgos Mamakis
  */
 public class RepoxRestClientImpl implements RepoxRestClient {
 
 	private RestTemplate restTemplate;
 	private String defaultURI;
 	
 	private final static String unknown_error="Unidentified Repox Error";
 	private final static String idVar="id=";
 	private final static String nameVar="name=";
 	private final static String nameCodeVar="nameCode=";
 	private final static String homepageVar="homepage=";
 	private final static String descriptionVar="description=";
 	private final static String createVar="create";	
 	private final static String updateVar="update";		
 	private final static String dataProviderIdVar="dataProviderId=";
 	private final static String exportPathVar="exportPath=";
 	private final static String schemaVar="schema=";
 	private final static String namespaceVar="namespace=";
 	private final static String metadataFormatVar="metadataFormat=";
 	private final static String userVar="user=";
 	private final static String passwordVar="password=";
 	private final static String charsetVar="charset=";
 	private final static String recordIdPolicyVar="recordIdPolicy=";
 	private final static String idExportedVar="idExported"; 
 	private final static String idXpathVar="idXpath=";
 	private final static String namespacePrefixVar="namespacePrefix=";
 	private final static String namespaceUriVar="namespaceUri=";
 
 	
 	
 	/*
 	 * Aggregator related operations
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#createAggregator(
 	 * eu.europeana.uim.repoxclient.jibxbindings.Aggregator)
 	 */
 	@Override
 	public Aggregator createAggregator(Aggregator aggregator)
 			throws AggregatorOperationException {
 
 		StringBuffer name = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer homepage = new StringBuffer();
 
 		name.append(nameVar);
 		name.append(aggregator.getName().getName());
 		nameCode.append(nameCodeVar);
 		nameCode.append(aggregator.getNameCode().getNameCode());
 		homepage.append(homepageVar);
 		homepage.append(aggregator.getUrl().getUrl());
 
 		Response resp = invokRestTemplate("/aggregators/create",
 				Response.class, name.toString(), nameCode.toString(),
 				homepage.toString());
 
 		if (resp.getAggregator() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new AggregatorOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new AggregatorOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getAggregator();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#deleteAggregator(
 	 * java.lang.String)
 	 */
 	@Override
 	public Success deleteAggregator(String aggregatorId)
 			throws AggregatorOperationException {
 
 		StringBuffer id = new StringBuffer();
 		id.append(idVar);
 		id.append(aggregatorId);
 
 		Response resp = invokRestTemplate("/aggregators/delete",
 				Response.class, id.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new AggregatorOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new AggregatorOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#updateAggregator(
 	 * eu.europeana.uim.repoxclient.jibxbindings.Aggregator)
 	 */
 	@Override
 	public Aggregator updateAggregator(Aggregator aggregator)
 			throws AggregatorOperationException {
 		StringBuffer id = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer homepage = new StringBuffer();
 
 		id.append(idVar);
 		id.append(aggregator.getId());
 		name.append(nameVar);
 		name.append(aggregator.getName().getName());
 		nameCode.append(nameCodeVar);
 		nameCode.append(aggregator.getNameCode().getNameCode());
 		homepage.append(homepageVar);
 		homepage.append(aggregator.getUrl().getUrl());
 
 		Response resp = invokRestTemplate("/aggregators/update",
 				Response.class, id.toString(), name.toString(),
 				nameCode.toString(), homepage.toString());
 
 		if (resp.getAggregator() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new AggregatorOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new AggregatorOperationException(
 						unknown_error);
 			}
 		} else {
 			return resp.getAggregator();
 		}
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#retrieveAggregators()
 	 */
 	@Override
 	public Aggregators retrieveAggregators()
 			throws AggregatorOperationException {
 		Response resp = invokRestTemplate("/aggregators/list", Response.class);
 
 		if (resp.getAggregators() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new AggregatorOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new AggregatorOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getAggregators();
 		}
 	}
 
 	
 	
 	/*
 	 * Provider related operations
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#createProvider(eu
 	 * .europeana.uim.repoxclient.jibxbindings.Provider,
 	 * eu.europeana.uim.repoxclient.jibxbindings.Aggregator)
 	 */
 	@Override
 	public Provider createProvider(Provider prov, Aggregator agr)
 			throws ProviderOperationException {
 
 		StringBuffer aggregatorId = new StringBuffer();
 		StringBuffer dataproviderId = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer country = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer homepage = new StringBuffer();
 		StringBuffer datasetType = new StringBuffer();
 
 		aggregatorId.append("aggregatorId=");
 		aggregatorId.append(agr.getId());
 
 		dataproviderId.append("dataProviderId=");
	    //dataproviderId.append(prov.getId());
 	
 		name.append(nameVar);
 		name.append(prov.getName().getName());
 		description.append(descriptionVar);
 
 		if (prov.getDescription() != null) {
 			description.append(prov.getDescription().getDescription());
 		} else {
 			description.append("NONE");
 		}
 
 		country.append("country=");
 
 		if (prov.getCountry() != null) {
 			country.append(prov.getCountry().getCountry());
 		} else {
 			country.append("eu");
 		}
 
 		nameCode.append(nameCodeVar);
 		nameCode.append(prov.getNameCode().getNameCode());
 		homepage.append("url=");
 
 		if (!prov.getUrl().getUrl().equals("")) {
 			if (!prov.getUrl().getUrl().startsWith("http://")) {
 				homepage.append("http://");
 			}
 			homepage.append(prov.getUrl().getUrl().split("/")[0]);
 		} else {
 			homepage.append("http://europeana.eu");
 		}
 
 		datasetType.append("dataSetType=");
 		if (prov.getType() != null) {
 			datasetType.append(prov.getType().getType());
 		} else {
 			datasetType.append("UNKNOWN");
 		}
 
 		Response resp = invokRestTemplate("/dataProviders/create",
 				Response.class, aggregatorId.toString(),dataproviderId.toString(),
 				name.toString(),
 				description.toString(), country.toString(),
 				nameCode.toString(), homepage.toString(),
 				datasetType.toString());
 
 		if (resp.getProvider() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new ProviderOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new ProviderOperationException(unknown_error);
 			}
 		} else {
 
 			return resp.getProvider();
 		}
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#deleteProvider(java
 	 * .lang.String)
 	 */
 	@Override
 	public Success deleteProvider(String provID)
 			throws ProviderOperationException {
 		StringBuffer providerId = new StringBuffer();
 		providerId.append(idVar);
 		providerId.append(provID);
 
 		Response resp = invokRestTemplate("/dataProviders/delete",
 				Response.class, providerId.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new ProviderOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new ProviderOperationException(unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#updateProvider(eu
 	 * .europeana.uim.repoxclient.jibxbindings.Provider)
 	 */
 	@Override
 	public Provider updateProvider(Provider prov)
 			throws ProviderOperationException {
 
 		StringBuffer provId = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer country = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer homepage = new StringBuffer();
 		StringBuffer datasetType = new StringBuffer();
 
 		provId.append(idVar);
 		provId.append(prov.getId());
 
 		name.append(nameVar);
 		name.append(prov.getName().getName());
 
 		description.append(descriptionVar);
 
 		if (prov.getDescription() != null) {
 			description.append(prov.getDescription().getDescription());
 		} else {
 			description.append("NONE");
 		}
 
 		country.append("country=");
 		if (prov.getCountry() != null) {
 			country.append(prov.getCountry().getCountry());
 		} else {
 			country.append("eu");
 		}
 
 		nameCode.append(nameCodeVar);
 		nameCode.append(prov.getNameCode().getNameCode());
 		homepage.append("url=");
 
 		if (prov.getUrl().getUrl() != "") {
 			if (!prov.getUrl().getUrl().startsWith("http")) {
 				homepage.append("http://"
 						+ prov.getUrl().getUrl().split("/")[0]);
 			} else {
 				homepage.append(prov.getUrl().getUrl());
 			}
 
 		} else {
 			homepage.append("http://europeana.eu");
 		}
 
 		datasetType.append("dataSetType=");
 		if (prov.getType() != null) {
 			datasetType.append(prov.getType().getType());
 		} else {
 			datasetType.append("UNKNOWN");
 		}
 
 		Response resp = invokRestTemplate("/dataProviders/update",
 				Response.class, provId.toString(), name.toString(),
 				description.toString(), country.toString(),
 				nameCode.toString(), homepage.toString(),
 				datasetType.toString());
 
 		if (resp.getProvider() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new ProviderOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new ProviderOperationException(unknown_error);
 			}
 		} else {
 
 			return resp.getProvider();
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.repoxclient.plugin.RepoxRestClient#retrieveProviders()
 	 */
 	@Override
 	public DataProviders retrieveProviders() throws ProviderOperationException {
 		Response resp = invokRestTemplate("/dataProviders/list", Response.class);
 		return resp.getDataProviders();
 	}
 
 	/*
 	 * Datasources related operations
 	 */
 
 	/**
 	 * 
 	 * @return DataSources the available datasources
 	 * @throws RepoxException
 	 */
 	@Override
 	public DataSources retrieveDataSources()
 			throws DataSourceOperationException {
 
 		Response resp = invokRestTemplate("/dataSources/list", Response.class);
 
 		if (resp.getDataSources() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getDataSources();
 		}
 	}
 
 	/**
 	 * Creates an OAI DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/createOai?
 	 *    dataProviderId=DPRestr0&
 	 *    id=bdaSet&
 	 *    description=Biblioteca Digital Do Alentejo&
 	 *    nameCode=00123&
 	 *    name=Alentejo&
 	 *    exportPath=D:/Projectos/repoxdata_new&
 	 *    schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
 	 *    namespace=http://www.europeana.eu/schemas/ese/&
 	 *    metadataFormat=ese&
 	 *    oaiURL=http://bd1.inesc-id.pt:8080/repoxel/OAIHandler&
 	 *    oaiSet=bda
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 
 	@Override
 	public Source createDatasourceOAI(Source ds, Provider prov)
 			throws DataSourceOperationException {
 
 		Response resp = createUpdateDSOAI(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 
 	}
 
 	/**
 	 * Creates a Z3950Timestamp DataSource. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *  /rest/dataSources/createZ3950Timestamp?
 	 *   dataProviderId=DPRestr0&
 	 *   id=z3950TimeTest&
 	 *   description=test Z39.50 with time stamp&nameCode=00130&
 	 *   name=Z3950-TimeStamp&
 	 *   exportPath=D:/Projectos/repoxdata_new&
 	 *   schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *   namespace=info:lc/xmlns/marcxchange-v1&
 	 *   address=193.6.201.205&
 	 *   port=1616&
 	 *   database=B1&
 	 *   user=&
 	 *   password=&
 	 *   recordSyntax=usmarc&
 	 *   charset=UTF-8&
 	 *   earliestTimestamp=20110301&
 	 *   recordIdPolicy=IdGenerated&
 	 *   idXpath=&
 	 *   namespacePrefix=&
 	 *   namespaceUri=
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source createDatasourceZ3950Timestamp(Source ds, Provider prov)
 			throws DataSourceOperationException {
 
 		Response resp = createUpdateZ3950Timestamp(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Creates a Z3950IdFile DataSource. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/createZ3950IdList?
 	 *    dataProviderId=DPRestr0&
 	 *    id=z3950IdFile&
 	 *    description=test Z39.50 with id list&
 	 *    nameCode=00124&
 	 *    name=Z3950-IdFile&
 	 *    exportPath=D:/Projectos/repoxdata_new&
 	 *    schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *    namespace=info:lc/xmlns/marcxchange-v1&
 	 *    address=aleph.lbfl.li&
 	 *    port=9909&
 	 *    database=LLB_IDS&
 	 *    user=&
 	 *    password=&
 	 *    recordSyntax=usmarc&
 	 *    charset=UTF-8&
 	 *    filePath=C:\folderZ3950\1900028192z3960idList.txt&
 	 *    recordIdPolicy=IdGenerated&
 	 *    idXpath=&
 	 *    namespacePrefix=&
 	 *    namespaceUri=
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source createDatasourceZ3950IdFile(Source ds, Provider prov)
 			throws DataSourceOperationException {
 
 		Response resp = createUpdateDSZ3950IdFile(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Creates a Z3950IdSequence DataSource. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *   /rest/dataSources/createZ3950IdSequence?
 	 *   dataProviderId=DPRestr0&
 	 *   id=z3950IdSeqTest&
 	 *   description=test%20Z39.50%20with%20id%20sequence&
 	 *   nameCode=00129&
 	 *   name=Z3950-IdSeq&
 	 *   exportPath=D:/Projectos/repoxdata_new&
 	 *   schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *   namespace=info:lc/xmlns/marcxchange-v1&
 	 *   address=aleph.lbfl.li&
 	 *   port=9909&
 	 *   database=LLB_IDS&
 	 *   user=&
 	 *   password=&
 	 *   recordSyntax=usmarc&
 	 *   charset=UTF-8&
 	 *   maximumId=6000&
 	 *   recordIdPolicy=IdGenerated&
 	 *   idXpath=&
 	 *   namespacePrefix=&
 	 *   namespaceUri=
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source createDatasourceZ3950IdSequence(Source ds, Provider prov)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSZ3950IdSequence(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Creates an Ftp DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *   /rest/dataSources/createFtp?
 	 *   dataProviderId=DPRestr0&
 	 *   id=ftpTest&
 	 *   description=test FTP data source&
 	 *   nameCode=00124&
 	 *   name=FTP&
 	 *   exportPath=D:/Projectos/repoxdata_new&
 	 *   schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
 	 *   namespace=http://www.europeana.eu/schemas/ese/&
 	 *   metadataFormat=ese&
 	 *   isoFormat=&
 	 *   charset=&
 	 *   recordIdPolicy=IdGenerated&
 	 *   idXpath=&
 	 *   namespacePrefix=&
 	 *   namespaceUri=&
 	 *   recordXPath=record&
 	 *   server=bd1.inesc-id.pt&
 	 *   user=ftp&
 	 *   password=pmath2010.&
 	 *   ftpPath=/Lizbeth
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source createDatasourceFtp(Source ds, Provider prov)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSFtp(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Creates an Http DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/createHttp?
 	 *     dataProviderId=DPRestr0&
 	 *     id=httpTest&
 	 *     description=test HTTP data source&
 	 *     nameCode=00124&
 	 *     name=HTTP&
 	 *     exportPath=D:/Projectos/repoxdata_new&
 	 *     schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
 	 *     namespace=http://www.europeana.eu/schemas/ese/&
 	 *     metadataFormat=ese&
 	 *     isoFormat=&
 	 *     charset=&
 	 *     recordIdPolicy=IdGenerated&
 	 *     idXpath=&
 	 *     namespacePrefix=&
 	 *     namespaceUri=&
 	 *     recordXPath=record&
 	 *     url=http://digmap2.ist.utl.pt:8080/index_digital/contente/09428_Ag_DE_ELocal.zip
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source createDatasourceHttp(Source ds, Provider prov)
 			throws DataSourceOperationException {
 
 		Response resp = createUpdateDSHttp(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Creates an Folder DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/createFolder?
 	 *     id=folderTest&
 	 *     description=test%20Folder%20data%20source3333333&
 	 *     nameCode=4444444444400124&
 	 *     name=Folder&
 	 *     exportPath=D:/Projectos/repoxdata_new&
 	 *     schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *     namespace=info:lc/xmlns/marcxchange-v1&
 	 *     metadataFormat=ISO2709&
 	 *     isoFormat=pt.utl.ist.marc.iso2709.IteratorIso2709&
 	 *     charset=UTF-8&
 	 *     recordIdPolicy=IdExtracted&
 	 *     idXpath=/mx:record/mx:controlfield[@tag=%22001%22]&
 	 *     namespacePrefix=mx&
 	 *     namespaceUri=info:lc/xmlns/marcxchange-v1&
 	 *     recordXPath=&
 	 *     folder=C:\folderNew
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a DataSource object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source createDatasourceFolder(Source ds, Provider prov)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSFolder(createVar, ds, prov);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates an OAI DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/updateOai?
 	 *     id=bdaSet&
 	 *     description=222Biblioteca Digital Do Alentejo&
 	 *     nameCode=333300123&
 	 *     name=4444Alentejo&
 	 *     exportPath=D:/Projectos/repoxdata_new2&
 	 *     schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
 	 *     namespace=http://www.europeana.eu/schemas/ese/&
 	 *     metadataFormat=ese&
 	 *     oaiURL=http://bd1.inesc-id.pt:8080/repoxel/OAIHandler&
 	 *     oaiSet=bda
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceOAI(Source ds)
 			throws DataSourceOperationException {
 
 		Response resp = createUpdateDSOAI(updateVar, ds, null);
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates a Z3950Timestamp DataSource. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/updateZ3950Timestamp?
 	 *     id=z3950TimeTest&
 	 *     description=new test Z39.50 with time stamp&
 	 *     nameCode=99900130&
 	 *     name=Z3950-TimeStampWorking&
 	 *     exportPath=D:/Projectos/repoxdata_new&
 	 *     schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *     namespace=info:lc/xmlns/marcxchange-v1&
 	 *     address=193.6.201.205&
 	 *     port=1616&
 	 *     database=B1&
 	 *     user=&
 	 *     password=&
 	 *     recordSyntax=usmarc&
 	 *     charset=UTF-8&
 	 *     earliestTimestamp=20110301&
 	 *     recordIdPolicy=IdGenerated&
 	 *     idXpath=&
 	 *     namespacePrefix=&
 	 *     namespaceUri=
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceZ3950Timestamp(Source ds)
 			throws DataSourceOperationException {
 		Response resp = createUpdateZ3950Timestamp(updateVar, ds, null);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates a Z3950IdFile DataSource. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *      /rest/dataSources/updateZ3950IdList?
 	 *      id=z3950IdFile&
 	 *      description=new test Z39.50 with id list&
 	 *      nameCode=001245555&
 	 *      name=Z3950-IdFilenew&
 	 *      exportPath=D:/Projectos/repoxdata_new1&
 	 *      schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *      namespace=info:lc/xmlns/marcxchange-v1&
 	 *      address=aleph.lbfl.li&port=9909&
 	 *      database=LLB_IDS&
 	 *      user=&
 	 *      password=&
 	 *      recordSyntax=usmarc&
 	 *      charset=UTF-8&
 	 *      filePath=C:\folderZ3950\newFile.txt&
 	 *      recordIdPolicy=IdGenerated&
 	 *      idXpath=&
 	 *      namespacePrefix=&
 	 *      namespaceUri=
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceZ3950IdFile(Source ds)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSZ3950IdFile(updateVar, ds, null);
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates a Z3950IdSequence DataSource. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/updateZ3950IdSequence?
 	 *    id=z3950IdSeqTest&
 	 *    description=newtest Z39.50 with id sequence&
 	 *    nameCode=222200129&
 	 *    name=NEWZ3950-IdSeq&
 	 *    exportPath=D:/Projectos/repoxdata_new21&
 	 *    schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *    namespace=info:lc/xmlns/marcxchange-v1&
 	 *    address=aleph.lbfl.li&port=9909&
 	 *    database=LLB_IDS&
 	 *    user=&
 	 *    password=&
 	 *    recordSyntax=usmarc&
 	 *    charset=UTF-8&
 	 *    maximumId=300&
 	 *    recordIdPolicy=IdGenerated&
 	 *    idXpath=&
 	 *    namespacePrefix=&
 	 *    namespaceUri=
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceZ3950IdSequence(Source ds)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSZ3950IdSequence(updateVar, ds, null);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates a Ftp DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/updateFtp?
 	 *     id=ftpTest&
 	 *     description=newtest FTP data source&
 	 *     nameCode=555555500124&
 	 *     name=FTP&
 	 *     exportPath=D:/Projectos/repoxdata_new21212121&
 	 *     schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
 	 *     namespace=http://www.europeana.eu/schemas/ese/&
 	 *     metadataFormat=ese&
 	 *     isoFormat=&
 	 *     charset=&
 	 *     recordIdPolicy=IdGenerated&
 	 *     idXpath=&
 	 *     namespacePrefix=&
 	 *     namespaceUri=&
 	 *     recordXPath=record&
 	 *     server=bd1.inesc-id.pt&
 	 *     user=ftp&
 	 *     password=pmath2010.&
 	 *     ftpPath=/Lizbeth
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceFtp(Source ds)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSFtp(updateVar, ds, null);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates an Http DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/updateHttp?
 	 *     id=ftpTest&
 	 *     description=newtest FTP data source&
 	 *     nameCode=555555500124&
 	 *     name=FTP&
 	 *     exportPath=D:/Projectos/repoxdata_new21212121&
 	 *     schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
 	 *     namespace=http://www.europeana.eu/schemas/ese/&
 	 *     metadataFormat=ese&
 	 *     isoFormat=&
 	 *     charset=&
 	 *     recordIdPolicy=IdGenerated&
 	 *     idXpath=&
 	 *     namespacePrefix=&
 	 *     namespaceUri=&
 	 *     recordXPath=record&
 	 *     server=bd1.inesc-id.pt&
 	 *     user=ftp&
 	 *     password=pmath2010.&
 	 *     ftpPath=/Lizbeth
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceHttp(Source ds)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSHttp(updateVar, ds, null);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Updates a Folder DataSource. It accesses the following REST Interface:
 	 * 
 	 * <code>
 	 *     /rest/dataSources/updateFolder?
 	 *     id=folderTest&
 	 *     description=test%20Folder%20data%20source3333333&
 	 *     nameCode=4444444444400124&
 	 *     name=Folder&
 	 *     exportPath=D:/Projectos/repoxdata_new&
 	 *     schema=info:lc/xmlns/marcxchange-v1.xsd&
 	 *     namespace=info:lc/xmlns/marcxchange-v1&
 	 *     metadataFormat=ISO2709&
 	 *     isoFormat=pt.utl.ist.marc.iso2709.IteratorIso2709&
 	 *     charset=UTF-8&
 	 *     recordIdPolicy=IdExtracted&
 	 *     idXpath=/mx:record/mx:controlfield[@tag=%22001%22]&
 	 *     namespacePrefix=mx&
 	 *     namespaceUri=info:lc/xmlns/marcxchange-v1&
 	 *     recordXPath=&
 	 *     folder=C:\folderNew
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Source updateDatasourceFolder(Source ds)
 			throws DataSourceOperationException {
 		Response resp = createUpdateDSFolder(updateVar, ds, null);
 
 		if (resp.getSource() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSource();
 		}
 	}
 
 	/**
 	 * Deletes a DataSource of any Type. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/delete?id=ftpTest
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Success deleteDatasource(String dsID)
 			throws DataSourceOperationException {
 
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		Response resp = invokRestTemplate("/dataSources/delete",
 				Response.class, id.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new DataSourceOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#retrieveAggregator(java.lang.String)
 	 */
 	@Override
 	public Aggregator retrieveAggregator(String aggregatorID)
 			throws AggregatorOperationException {
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(aggregatorID);
 		
 		Response resp = invokRestTemplate("/aggregators/getAggregator",
 				Response.class, id.toString());
 		
 		if (resp.getError() != null) {
 
 			if(resp.getError().getType().equals("NOT_FOUND")){
 				return null;
 			}
 				throw new AggregatorOperationException(
 						createRepoxFailureMessage(resp.getError()));
 		}
 		else {
 			return resp.getAggregator();
 
 			}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#retrieveProvider(java.lang.String)
 	 */
 	@Override
 	public Provider retrieveProvider(String providerID)
 			throws ProviderOperationException {
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(providerID);
 		
 		Response resp = invokRestTemplate("/dataProviders/getDataProvider",
 				Response.class, id.toString());
 
 		if (resp.getError() != null) {
 
 			if(resp.getError().getType().equals("NOT_FOUND")){
 				return null;
 			}
 				throw new ProviderOperationException(
 						createRepoxFailureMessage(resp.getError()));
 		}
 		else {
 			return resp.getProvider();
 
 			}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#retrieveDataSource(java.lang.String)
 	 */
 	@Override
 	public Source retrieveDataSource(String dsID)
 			throws DataSourceOperationException {
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 		
 		Response resp = invokRestTemplate("/dataSources/getDataSource",
 				Response.class, id.toString());
 		
 		if (resp.getError() != null) {
 
 			if(resp.getError().getType().equals("NOT_FOUND")){
 				return null;
 			}
 				throw new DataSourceOperationException(
 						createRepoxFailureMessage(resp.getError()));
 		}
 		else {
 			return resp.getSource();
 
 			}
 	}
 	
 	
 	/*
 	 * Record related operations
 	 */
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#retrieveRecord(java.lang.String)
 	 */
 	public RecordResult retrieveRecord(String recordString)
 			throws RecordOperationException {
 
 		throw new UnsupportedOperationException("Not implemented yet...");
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#saveRecord(java.lang.String, eu.europeana.uim.repoxclient.jibxbindings.Source, java.lang.String)
 	 */
 	@Override
 	public Success saveRecord(String recordID, Source ds, String recordXML)
 			throws RecordOperationException {
 
 		throw new UnsupportedOperationException("Not implemented yet...");
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#markRecordAsDeleted(java.lang.String)
 	 */
 	@Override
 	public Success markRecordAsDeleted(String recordID)
 			throws RecordOperationException {
 
 		throw new UnsupportedOperationException("Not implemented yet...");
 
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.repoxclient.plugin.RepoxRestClient#eraseRecord(java.lang.String)
 	 */
 	@Override
 	public Success eraseRecord(String recordID) throws RecordOperationException {
 
 		throw new UnsupportedOperationException("Not implemented yet...");
 	}
 
 	/*
 	 * Harvest Control/Monitoring operations
 	 */
 
 	/**
 	 * Initializes a Harvesting Session. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 * 	 /rest/dataSources/startIngest?id=bmfinancas
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Success initiateHarvesting(String dsID, boolean isfull)
 			throws HarvestingOperationException {
 
 		StringBuffer id = new StringBuffer();
 		StringBuffer fullIngest = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 		fullIngest.append("fullIngest=");
 		fullIngest.append(isfull);
 
 		Response resp = invokRestTemplate("/dataSources/startIngest",
 				Response.class, id.toString(), fullIngest.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 
 	}
 
 	/**
 	 * Cancels an existing Harvesting Session. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 * 	 /rest/dataSources/stopIngest?id=bmfinancas
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Success cancelHarvesting(String dsID)
 			throws HarvestingOperationException {
 
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		Response resp = invokRestTemplate("/dataSources/stopIngest",
 				Response.class, id.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 
 	}
 
 	/**
 	 * Initializes a Harvesting Session. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 * 	    /rest/dataSources/scheduleIngest?id=bmfinancas&
 	 *      firstRunDate=06-07-2011&
 	 *      firstRunHour=17:43&
 	 *      frequency=Daily&
 	 *      xmonths=&
 	 *      fullIngest=true
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Success scheduleHarvesting(String dsID, DateTime ingestionDate,
 			IngestFrequency frequencyobj, boolean isfull)
 			throws HarvestingOperationException {
 
 		StringBuffer id = new StringBuffer();
 		StringBuffer firstRunDate = new StringBuffer();
 		StringBuffer firstRunHour = new StringBuffer();
 		StringBuffer frequency = new StringBuffer();
 		StringBuffer xmonths = new StringBuffer();
 		StringBuffer fullIngest = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		firstRunDate.append("firstRunDate=");
 		firstRunDate.append(ingestionDate.getDayOfMonth() + "-"
 				+ ingestionDate.getMonthOfYear() + "-"
 				+ ingestionDate.getYear());
 
 		firstRunHour.append("firstRunHour=");
 		firstRunHour.append(new Integer(ingestionDate.getHourOfDay()).toString() + ":"
 				+ new Integer(ingestionDate.getMinuteOfHour()).toString());
 
 		frequency.append("frequency=");
 		frequency.append(frequencyobj.toString());
 
 		xmonths.append("xmonths=");
 		// xmonths.append(frequencyobj.toString());
 
 		frequency.append("fullIngest=");
 		frequency.append(fullIngest.toString());
 
 		Response resp = invokRestTemplate("/dataSources/scheduleIngest",
 				Response.class, id.toString(), firstRunDate.toString(),
 				firstRunHour.toString(), frequency.toString(),
 				xmonths.toString(), fullIngest.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 
 	}
 
 	/**
 	 * Gets the status for a Harvesting Session. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/harvestStatus?id=httpTest
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public HarvestingStatus getHarvestingStatus(String dsID)
 			throws HarvestingOperationException {
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		Response resp = invokRestTemplate("/dataSources/harvestStatus",
 				Response.class, id.toString());
 
 		if (resp.getHarvestingStatus() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getHarvestingStatus();
 		}
 	}
 
 	/**
 	 * Gets the Active Harvesting Sessions. It accesses the following REST
 	 * Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/harvesting
 	 *  </code>
 	 * 
 	 * @param ds
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public RunningTasks getActiveHarvestingSessions()
 			throws HarvestingOperationException {
 
 		Response resp = invokRestTemplate("/dataSources/harvesting",
 				Response.class);
 
 		if (resp.getRunningTasks() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getRunningTasks();
 		}
 	}
 
 	/**
 	 * Gets the Scheduled Harvesting Sessions for a DataSource. It accesses the
 	 * following REST Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/scheduleList?id=bmfinancas
 	 *  </code>
 	 * 
 	 * @throws HarvestingOperationException
 	 */
 	@Override
 	public ScheduleTasks getScheduledHarvestingSessions(String dsID)
 			throws HarvestingOperationException {
 
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		Response resp = invokRestTemplate("/dataSources/scheduleList",
 				Response.class, id.toString());
 
 		if (resp.getScheduleTasks() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getScheduleTasks();
 		}
 
 	}
 
 	/**
 	 * Gets the latest Harvesting Log record for a DataSource. It accesses the
 	 * following REST Interface:
 	 * 
 	 * <code>
 	 *    /rest/dataSources/log?id=httpTest
 	 *  </code>
 	 * 
 	 * @param dsID
 	 *            a Source object
 	 * @throws DataSourceOperationException
 	 */
 	@Override
 	public Log getHarvestLog(String dsID) throws HarvestingOperationException {
 
 		StringBuffer id = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		Response resp = invokRestTemplate("/dataSources/log", Response.class,
 				id.toString());
 
 		if (resp.getLog() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getLog();
 		}
 
 	}
 
 	/**
 	 * Initializes the export of records
 	 * 
 	 * <code>
 	 *    /rest/dataSources/startExport?id=bmfinancas&recordsPerFile=1000
 	 *  </code>
 	 * 
 	 * @param dsID
 	 *            the DataSource reference
 	 * @param records
 	 *            no of records per file
 	 */
 	@Override
 	public Success initializeExport(String dsID, int records)
 			throws HarvestingOperationException {
 
 		StringBuffer id = new StringBuffer();
 		StringBuffer recordsPerFile = new StringBuffer();
 
 		id.append(idVar);
 		id.append(dsID);
 
 		recordsPerFile.append("recordsPerFile=");
 
 		if (records == 0) {
 			recordsPerFile.append("ALL");
 		} else {
 			recordsPerFile.append(records);
 		}
 
 		Response resp = invokRestTemplate("/dataSources/startExport",
 				Response.class, id.toString(), recordsPerFile.toString());
 
 		if (resp.getSuccess() == null) {
 			if (createRepoxFailureMessage(resp.getError()) != null) {
 				throw new HarvestingOperationException(
 						createRepoxFailureMessage(resp.getError()));
 			} else {
 				throw new HarvestingOperationException(
 						unknown_error);
 			}
 		} else {
 
 			return resp.getSuccess();
 		}
 
 	}
 
 	// Private Methods
 
 	/**
 	 * Auxiliary method for invoking a REST operation
 	 * 
 	 * @param <S>
 	 *            the return type
 	 * @param wsOperation
 	 * @return
 	 */
 	private <S> S invokRestTemplate(String restOperation, Class<S> responseClass) {
 
 		StringBuffer operation = new StringBuffer();
 		operation.append(defaultURI);
 		operation.append(restOperation);
 		S restResponse = restTemplate.getForObject(operation.toString(),
 				responseClass);
 
 		return restResponse;
 	}
 
 	/**
 	 * Auxiliary method for invoking a REST operation with parameters
 	 * 
 	 * @param <S>
 	 *            the return type
 	 * @param wsOperation
 	 * @return
 	 */
 	private <S> S invokRestTemplate(String restOperation,
 			Class<S> responseClass, String... params) {
 
 		StringBuffer operation = new StringBuffer();
 		operation.append(defaultURI);
 		operation.append(restOperation);
 		operation.append("?");
 
 		for (int i = 0; i < params.length; i++) {
 			if (i != 0) {
 				operation.append("&");
 			}
 			operation.append(params[i]);
 		}
 
 		S restResponse = restTemplate.getForObject(operation.toString(),
 				responseClass);
 
 		return restResponse;
 	}
 
 	// Getters & Setters
 
 	public void setRestTemplate(RestTemplate restTemplate) {
 		this.restTemplate = restTemplate;
 	}
 
 	public RestTemplate getRestTemplate() {
 		return restTemplate;
 	}
 
 	public void setDefaultURI(String defaultURI) {
 		this.defaultURI = defaultURI;
 	}
 
 	public String getDefaultURI() {
 		return defaultURI;
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateDSOAI(String action, Source ds, Provider prov) {
 
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer metadataFormat = new StringBuffer();
 		StringBuffer oaiURL = new StringBuffer();
 		StringBuffer oaiSet = new StringBuffer();
 
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		// description.append(ds.getDescription().getDescription());
 		description.append("NONE");
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		metadataFormat.append(metadataFormatVar);
 		metadataFormat.append(ds.getMetadataFormat());
 		oaiURL.append("oaiURL=");
 		oaiURL.append(ds.getSequence().getOaiSource().getOaiSource());
 		oaiSet.append("oaiSet=");
 		oaiSet.append(ds.getSequence().getOaiSet().getOaiSet());
 		if (action.equals(createVar)) {
 			return invokRestTemplate("/dataSources/createOai", Response.class,
 					dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					oaiURL.toString(), oaiSet.toString());
 		} else {
 			return invokRestTemplate("/dataSources/updateOai", Response.class,
 
 			id.toString(), description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					oaiURL.toString(), oaiSet.toString());
 		}
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateZ3950Timestamp(String action, Source ds,
 			Provider prov) {
 
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer address = new StringBuffer();
 		StringBuffer port = new StringBuffer();
 		StringBuffer database = new StringBuffer();
 		StringBuffer user = new StringBuffer();
 		StringBuffer password = new StringBuffer();
 		StringBuffer recordSyntax = new StringBuffer();
 		StringBuffer charset = new StringBuffer();
 		StringBuffer earliestTimestamp = new StringBuffer();
 		StringBuffer recordIdPolicy = new StringBuffer();
 
 		StringBuffer idXpath = new StringBuffer();
 		StringBuffer namespacePrefix = new StringBuffer();
 		StringBuffer namespaceUri = new StringBuffer();
 
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		description.append(ds.getDescription().getDescription());
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		address.append("address=");
 		address.append(ds.getSequence2().getTarget().getAddress().getAddress());
 		port.append("port=");
 		port.append(ds.getSequence2().getTarget().getPort().getPort());
 		database.append("database=");
 		database.append(ds.getSequence2().getTarget().getDatabase()
 				.getDatabase());
 
 		user.append(userVar);
 		user.append(ds.getSequence2().getTarget().getUser().getUser());
 		password.append(passwordVar);
 		password.append(ds.getSequence2().getTarget().getPassword()
 				.getPassword());
 		recordSyntax.append("recordSyntax=");
 		recordSyntax.append(ds.getSequence2().getTarget().getRecordSyntax()
 				.getRecordSyntax());
 
 		charset.append(charsetVar);
 		charset.append(ds.getSequence2().getTarget().getCharset().getCharset());
 		earliestTimestamp.append("earliestTimestamp=");
 		earliestTimestamp.append(ds.getChoice().getEarliestTimestamp()
 				.getEarliestTimestamp());
 		recordIdPolicy.append(recordIdPolicyVar);
 		recordIdPolicy.append(ds.getRecordIdPolicy().getType());
 		if (ds.getRecordIdPolicy().getType().equals(idExportedVar)) {
 			idXpath.append(idXpathVar);
 			idXpath.append(ds.getRecordIdPolicy().getSequence().getIdXpath()
 					.getIdXpath());
 
 			namespacePrefix.append(namespacePrefixVar);
 			namespacePrefix.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespacePrefix()
 					.getNamespacePrefix());
 			namespaceUri.append(namespaceUriVar);
 			namespaceUri.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespaceUri()
 					.getNamespaceUri());
 		}
 		if (action.equals(createVar)) {
 
 			Response resp = invokRestTemplate(
 					"/dataSources/createZ3950Timestamp", Response.class,
 					dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), address.toString(), port.toString(),
 					database.toString(), user.toString(), password.toString(),
 					recordSyntax.toString(), charset.toString(),
 					earliestTimestamp.toString(), recordIdPolicy.toString(),
 					idXpath.toString(), namespacePrefix.toString(),
 					namespaceUri.toString());
 
 			return resp;
 		} else {
 			Response resp = invokRestTemplate(
 					"/dataSources/updateZ3950Timestamp", Response.class,
 					id.toString(), description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), address.toString(), port.toString(),
 					database.toString(), user.toString(), password.toString(),
 					recordSyntax.toString(), charset.toString(),
 					earliestTimestamp.toString(), recordIdPolicy.toString(),
 					idXpath.toString(), namespacePrefix.toString(),
 					namespaceUri.toString());
 
 			return resp;
 		}
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateDSZ3950IdFile(String action, Source ds,
 			Provider prov) {
 
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer address = new StringBuffer();
 		StringBuffer port = new StringBuffer();
 		StringBuffer database = new StringBuffer();
 		StringBuffer user = new StringBuffer();
 		StringBuffer password = new StringBuffer();
 		StringBuffer recordSyntax = new StringBuffer();
 		StringBuffer charset = new StringBuffer();
 
 		StringBuffer filePath = new StringBuffer();
 		StringBuffer recordIdPolicy = new StringBuffer();
 		StringBuffer idXpath = new StringBuffer();
 		StringBuffer namespacePrefix = new StringBuffer();
 		StringBuffer namespaceUri = new StringBuffer();
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		description.append(ds.getDescription().getDescription());
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		address.append("address=");
 		address.append(ds.getSequence2().getTarget().getAddress().getAddress());
 		port.append("port=");
 		port.append(ds.getSequence2().getTarget().getPort().getPort());
 		database.append("database=");
 		database.append(ds.getSequence2().getTarget().getDatabase()
 				.getDatabase());
 
 		user.append(userVar);
 		user.append(ds.getSequence2().getTarget().getUser().getUser());
 		password.append(passwordVar);
 		password.append(ds.getSequence2().getTarget().getPassword()
 				.getPassword());
 		recordSyntax.append("recordSyntax=");
 		recordSyntax.append(ds.getSequence2().getTarget().getRecordSyntax()
 				.getRecordSyntax());
 
 		charset.append(charsetVar);
 		charset.append(ds.getSequence2().getTarget().getCharset().getCharset());
 		filePath.append("filePath=");
 		filePath.append(ds.getChoice().getFilePath().getFilePath());
 		recordIdPolicy.append(recordIdPolicyVar);
 		recordIdPolicy.append(ds.getRecordIdPolicy().getType());
 		if (ds.getRecordIdPolicy().getType().equals(idExportedVar)) {
 			idXpath.append(idXpathVar);
 			idXpath.append(ds.getRecordIdPolicy().getSequence().getIdXpath()
 					.getIdXpath());
 
 			namespacePrefix.append(namespacePrefixVar);
 			namespacePrefix.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespacePrefix()
 					.getNamespacePrefix());
 			namespaceUri.append(namespaceUriVar);
 			namespaceUri.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespaceUri()
 					.getNamespaceUri());
 		}
 		if (action.equals(createVar)) {
 
 			Response resp = invokRestTemplate("/dataSources/createZ3950IdList",
 					Response.class, dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), address.toString(), port.toString(),
 					database.toString(), user.toString(), password.toString(),
 					recordSyntax.toString(), charset.toString(),
 					filePath.toString(), recordIdPolicy.toString(),
 					idXpath.toString(), namespacePrefix.toString(),
 					namespaceUri.toString());
 
 			return resp;
 		} else {
 
 			Response resp = invokRestTemplate("/dataSources/updateZ3950IdList",
 					Response.class, id.toString(), description.toString(),
 					nameCode.toString(), name.toString(),
 					exportPath.toString(), schema.toString(),
 					namespace.toString(), address.toString(), port.toString(),
 					database.toString(), user.toString(), password.toString(),
 					recordSyntax.toString(), charset.toString(),
 					filePath.toString(), recordIdPolicy.toString(),
 					idXpath.toString(), namespacePrefix.toString(),
 					namespaceUri.toString());
 
 			return resp;
 		}
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateDSZ3950IdSequence(String action, Source ds,
 			Provider prov) {
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer address = new StringBuffer();
 		StringBuffer port = new StringBuffer();
 		StringBuffer database = new StringBuffer();
 		StringBuffer user = new StringBuffer();
 		StringBuffer password = new StringBuffer();
 		StringBuffer recordSyntax = new StringBuffer();
 		StringBuffer charset = new StringBuffer();
 
 		StringBuffer maximumId = new StringBuffer();
 
 		StringBuffer recordIdPolicy = new StringBuffer();
 		StringBuffer idXpath = new StringBuffer();
 		StringBuffer namespacePrefix = new StringBuffer();
 		StringBuffer namespaceUri = new StringBuffer();
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		description.append(ds.getDescription().getDescription());
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		address.append("address=");
 		address.append(ds.getSequence2().getTarget().getAddress().getAddress());
 		port.append("port=");
 		port.append(ds.getSequence2().getTarget().getPort().getPort());
 		database.append("database=");
 		database.append(ds.getSequence2().getTarget().getDatabase()
 				.getDatabase());
 
 		user.append(userVar);
 		user.append(ds.getSequence2().getTarget().getUser().getUser());
 		password.append(passwordVar);
 		password.append(ds.getSequence2().getTarget().getPassword()
 				.getPassword());
 		recordSyntax.append("recordSyntax=");
 		recordSyntax.append(ds.getSequence2().getTarget().getRecordSyntax()
 				.getRecordSyntax());
 
 		charset.append(charsetVar);
 		charset.append(ds.getSequence2().getTarget().getCharset().getCharset());
 
 		maximumId.append("maximumId=");
 		maximumId.append(ds.getChoice().getMaximumId());
 
 		recordIdPolicy.append(recordIdPolicyVar);
 		recordIdPolicy.append(ds.getRecordIdPolicy().getType());
 		if (ds.getRecordIdPolicy().getType().equals(idExportedVar)) {
 			idXpath.append(idXpathVar);
 			idXpath.append(ds.getRecordIdPolicy().getSequence().getIdXpath()
 					.getIdXpath());
 
 			namespacePrefix.append(namespacePrefixVar);
 			namespacePrefix.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespacePrefix()
 					.getNamespacePrefix());
 			namespaceUri.append(namespaceUriVar);
 			namespaceUri.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespaceUri()
 					.getNamespaceUri());
 		}
 		if (action.equals(createVar)) {
 			return invokRestTemplate("/dataSources/createZ3950IdSequence",
 					Response.class, dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), address.toString(), port.toString(),
 					database.toString(), user.toString(), password.toString(),
 					recordSyntax.toString(), charset.toString(),
 					maximumId.toString(), recordIdPolicy.toString(),
 					idXpath.toString(), namespacePrefix.toString(),
 					namespaceUri.toString());
 		} else {
 			return invokRestTemplate("/dataSources/updateZ3950IdSequence",
 					Response.class,
 
 					id.toString(), description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), address.toString(), port.toString(),
 					database.toString(), user.toString(), password.toString(),
 					recordSyntax.toString(), charset.toString(),
 					maximumId.toString(), recordIdPolicy.toString(),
 					idXpath.toString(), namespacePrefix.toString(),
 					namespaceUri.toString());
 		}
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateDSFtp(String action, Source ds, Provider prov) {
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer metadataFormat = new StringBuffer();
 		StringBuffer isoFormat = new StringBuffer();
 		StringBuffer charset = new StringBuffer();
 		StringBuffer recordIdPolicy = new StringBuffer();
 		StringBuffer idXpath = new StringBuffer();
 		StringBuffer namespacePrefix = new StringBuffer();
 		StringBuffer namespaceUri = new StringBuffer();
 		StringBuffer recordXPath = new StringBuffer();
 		StringBuffer server = new StringBuffer();
 		StringBuffer user = new StringBuffer();
 		StringBuffer password = new StringBuffer();
 		StringBuffer ftpPath = new StringBuffer();
 
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		description.append(ds.getDescription().getDescription());
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		metadataFormat.append(metadataFormatVar);
 		metadataFormat.append(ds.getMetadataFormat());
 		// TODO: isoFormat and charset and ftpPath are missing
 		isoFormat.append("isoFormat=");
 		isoFormat.append(ds.getChoice().getIsoFormat().getIsoFormat());
 		// isoFormat.append("test");
 
 		charset.append(charsetVar);
 		charset.append(ds.getSequence2().getTarget().getCharset().getCharset());
 		// charset.append("test");
 		recordIdPolicy.append(recordIdPolicyVar);
 		recordIdPolicy.append(ds.getRecordIdPolicy().getType());
 		if (ds.getRecordIdPolicy().getType().equals(idExportedVar)) {
 			idXpath.append(idXpathVar);
 			idXpath.append(ds.getRecordIdPolicy().getSequence().getIdXpath()
 					.getIdXpath());
 			namespacePrefix.append(namespacePrefixVar);
 			namespacePrefix.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespacePrefix()
 					.getNamespacePrefix());
 			namespaceUri.append(namespaceUriVar);
 			namespaceUri.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespaceUri()
 					.getNamespaceUri());
 		}
 		recordXPath.append("recordXPath=");
 		recordXPath.append(ds.getSplitRecords().getRecordXPath()
 				.getRecordXPath());
 
 		server.append("server=");
 		server.append(ds.getSequence1().getRetrieveStrategy().getChoice()
 				.getServer().getServer());
 		user.append(userVar);
 		user.append(ds.getSequence1().getRetrieveStrategy().getChoice()
 				.getUser().getUser());
 		password.append(passwordVar);
 		password.append(ds.getSequence1().getRetrieveStrategy().getChoice()
 				.getPassword().getPassword());
 
 		ftpPath.append("ftpPath=");
 		// ftpPath.append("/test");
 		ftpPath.append(ds.getChoice().getFtpPath().getFtpPath());
 		if (action.equals(createVar)) {
 			return invokRestTemplate("/dataSources/createFtp", Response.class,
 					dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					isoFormat.toString(), charset.toString(),
 					recordIdPolicy.toString(), idXpath.toString(),
 					namespacePrefix.toString(), namespaceUri.toString(),
 					recordXPath.toString(), server.toString(), user.toString(),
 					password.toString(), ftpPath.toString());
 		} else {
 			return invokRestTemplate("/dataSources/updateFtp", Response.class,
 
 			id.toString(), description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					isoFormat.toString(), charset.toString(),
 					recordIdPolicy.toString(), idXpath.toString(),
 					namespacePrefix.toString(), namespaceUri.toString(),
 					recordXPath.toString(), server.toString(), user.toString(),
 					password.toString(), ftpPath.toString());
 		}
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateDSHttp(String action, Source ds, Provider prov) {
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer metadataFormat = new StringBuffer();
 		StringBuffer isoFormat = new StringBuffer();
 		StringBuffer charset = new StringBuffer();
 		StringBuffer recordIdPolicy = new StringBuffer();
 		StringBuffer idXpath = new StringBuffer();
 		StringBuffer namespacePrefix = new StringBuffer();
 		StringBuffer namespaceUri = new StringBuffer();
 		StringBuffer recordXPath = new StringBuffer();
 
 		StringBuffer url = new StringBuffer();
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		description.append(ds.getDescription().getDescription());
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		metadataFormat.append(metadataFormatVar);
 		metadataFormat.append(ds.getMetadataFormat());
 		// TODO: not supported
 		isoFormat.append("isoFormat=");
 		isoFormat.append(ds.getChoice().getIsoFormat().getIsoFormat());
 		charset.append(charsetVar);
 		charset.append(ds.getSequence2().getTarget().getCharset().getCharset());
 		recordIdPolicy.append(recordIdPolicyVar);
 		recordIdPolicy.append(ds.getRecordIdPolicy().getType());
 		if (ds.getRecordIdPolicy().getType().equals(idExportedVar)) {
 			idXpath.append(idXpathVar);
 			idXpath.append(ds.getRecordIdPolicy().getSequence().getIdXpath()
 					.getIdXpath());
 			namespacePrefix.append(namespacePrefixVar);
 			namespacePrefix.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespacePrefix()
 					.getNamespacePrefix());
 			namespaceUri.append(namespaceUriVar);
 			namespaceUri.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespaceUri()
 					.getNamespaceUri());
 
 			recordXPath.append("recordXPath=");
 			recordXPath.append(ds.getSplitRecords().getRecordXPath()
 					.getRecordXPath());
 		}
 		url.append("url=");
 		url.append(ds.getSequence1().getRetrieveStrategy().getChoice().getUrl()
 				.getUrl());
 
 		if (action.equals(createVar)) {
 			return invokRestTemplate("/dataSources/createHttp", Response.class,
 					dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					isoFormat.toString(), charset.toString(),
 					recordIdPolicy.toString(), idXpath.toString(),
 					namespacePrefix.toString(), namespaceUri.toString(),
 					recordXPath.toString(), url.toString());
 		} else {
 			return invokRestTemplate("/dataSources/updateHttp", Response.class,
 					id.toString(), description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					isoFormat.toString(), charset.toString(),
 					recordIdPolicy.toString(), idXpath.toString(),
 					namespacePrefix.toString(), namespaceUri.toString(),
 					recordXPath.toString(), url.toString());
 		}
 
 	}
 
 	/**
 	 * @param action
 	 * @param ds
 	 * @param prov
 	 * @return
 	 */
 	private Response createUpdateDSFolder(String action, Source ds,
 			Provider prov) {
 		StringBuffer dataProviderId = new StringBuffer();
 		StringBuffer id = new StringBuffer();
 		StringBuffer description = new StringBuffer();
 		StringBuffer nameCode = new StringBuffer();
 		StringBuffer name = new StringBuffer();
 		StringBuffer exportPath = new StringBuffer();
 		StringBuffer schema = new StringBuffer();
 		StringBuffer namespace = new StringBuffer();
 
 		// Method specific
 		StringBuffer metadataFormat = new StringBuffer();
 		StringBuffer isoFormat = new StringBuffer();
 		StringBuffer charset = new StringBuffer();
 		StringBuffer recordIdPolicy = new StringBuffer();
 		StringBuffer idXpath = new StringBuffer();
 		StringBuffer namespacePrefix = new StringBuffer();
 		StringBuffer namespaceUri = new StringBuffer();
 		StringBuffer recordXPath = new StringBuffer();
 
 		StringBuffer folder = new StringBuffer();
 		if (action.equals(createVar)) {
 			dataProviderId.append(dataProviderIdVar);
 			dataProviderId.append(prov.getId());
 		}
 		id.append(idVar);
 		id.append(ds.getId());
 		description.append(descriptionVar);
 		description.append(ds.getDescription().getDescription());
 		nameCode.append(nameCodeVar);
 		nameCode.append(ds.getNameCode());
 		name.append(nameVar);
 		name.append(ds.getName());
 		exportPath.append(exportPathVar);
 		exportPath.append(ds.getExportPath());
 		schema.append(schemaVar);
 		schema.append(ds.getSchema());
 		namespace.append(namespaceVar);
 		namespace.append(ds.getNamespace());
 		metadataFormat.append(metadataFormatVar);
 		metadataFormat.append(ds.getMetadataFormat());
 		// todo: isoFormat charset folder are missing
 		isoFormat.append("isoFormat=");
 		isoFormat.append(ds.getChoice().getIsoFormat().getIsoFormat());
 		// isoFormat.append("test");
 		charset.append(charsetVar);
 		// charset.append("test");
 		charset.append(ds.getSequence2().getTarget().getCharset().getCharset());
 		recordIdPolicy.append(recordIdPolicyVar);
 		recordIdPolicy.append(ds.getRecordIdPolicy().getType());
 		if (ds.getRecordIdPolicy().getType().equals(idExportedVar)) {
 			idXpath.append(idXpathVar);
 
 			idXpath.append(ds.getRecordIdPolicy().getSequence().getIdXpath()
 					.getIdXpath());
 			namespacePrefix.append(namespacePrefixVar);
 			namespacePrefix.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespacePrefix()
 					.getNamespacePrefix());
 			namespaceUri.append(namespaceUriVar);
 			namespaceUri.append(ds.getRecordIdPolicy().getSequence()
 					.getNamespaces().getNamespace().getNamespaceUri()
 					.getNamespaceUri());
 
 			recordXPath.append("recordXPath=");
 			recordXPath.append(ds.getSplitRecords().getRecordXPath()
 					.getRecordXPath());
 		}
 		folder.append("folder=");
 		folder.append("/tmp");
 		// folder.append(ds.getChoice().getFolder());
 
 		if (action.equals(createVar)) {
 			return invokRestTemplate("/dataSources/createFolder",
 					Response.class, dataProviderId.toString(), id.toString(),
 					description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					isoFormat.toString(), charset.toString(),
 					recordIdPolicy.toString(), idXpath.toString(),
 					namespacePrefix.toString(), namespaceUri.toString(),
 					recordXPath.toString(), folder.toString());
 		} else {
 			return invokRestTemplate("/dataSources/updateFolder",
 					Response.class,
 
 					id.toString(), description.toString(), nameCode.toString(),
 					name.toString(), exportPath.toString(), schema.toString(),
 					namespace.toString(), metadataFormat.toString(),
 					isoFormat.toString(), charset.toString(),
 					recordIdPolicy.toString(), idXpath.toString(),
 					namespacePrefix.toString(), namespaceUri.toString(),
 					recordXPath.toString(), folder.toString());
 		}
 
 	}
 
 	/**
 	 * Auxiliary method for extracting error information from an _Error object
 	 * 
 	 * @param err
 	 *            the Error object
 	 * @return the error description
 	 */
 	private static String createRepoxFailureMessage(_Error err) {
 
 		StringBuffer sb = new StringBuffer();
 		sb.append(" Error Type: ");
 		sb.append(err.getType());
 		sb.append(" Error Cause: ");
 		sb.append(err.getCause());
 		sb.append(" Request URI: ");
 		sb.append(err.getRequestURI());
 
 		return sb.toString();
 	}
 
 
 
 }
