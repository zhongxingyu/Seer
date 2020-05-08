 /*******************************************************************************
  * Copyright (c) 2013 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Lautaro Matas (lmatas@gmail.com) - Desarrollo e implementación
  *     Emiliano Marmonti(emarmonti@gmail.com) - Coordinación del componente III
  * 
  * Este software fue desarrollado en el marco de la consultoría "Desarrollo e implementación de las soluciones - Prueba piloto del Componente III -Desarrollador para las herramientas de back-end" del proyecto “Estrategia Regional y Marco de Interoperabilidad y Gestión para una Red Federada Latinoamericana de Repositorios Institucionales de Documentación Científica” financiado por Banco Interamericano de Desarrollo (BID) y ejecutado por la Cooperación Latino Americana de Redes Avanzadas, CLARA.
  ******************************************************************************/
 package org.lareferencia.backend.rest;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.lareferencia.backend.domain.NationalNetwork;
 import org.lareferencia.backend.domain.NetworkSnapshotStat;
 import org.lareferencia.backend.domain.NetworkSnapshot;
 import org.lareferencia.backend.domain.OAIProviderStat;
 import org.lareferencia.backend.domain.OAIRecord;
 import org.lareferencia.backend.domain.RecordStatus;
 import org.lareferencia.backend.domain.SnapshotStatus;
 import org.lareferencia.backend.harvester.OAIRecordMetadata;
 import org.lareferencia.backend.harvester.OAIRecordMetadata.OAIRecordMetadataParseException;
 import org.lareferencia.backend.indexer.IIndexer;
 import org.lareferencia.backend.indexer.IndexerWorker;
 import org.lareferencia.backend.repositories.NationalNetworkRepository;
 import org.lareferencia.backend.repositories.NetworkSnapshotRepository;
 import org.lareferencia.backend.repositories.NetworkSnapshotStatRepository;
 import org.lareferencia.backend.repositories.OAIProviderStatRepository;
 import org.lareferencia.backend.repositories.OAIRecordRepository;
 import org.lareferencia.backend.stats.MetadataOccurrenceCountSnapshotStatProcessor;
 import org.lareferencia.backend.stats.RejectedByFieldSnapshotStatProcessor;
 import org.lareferencia.backend.tasks.SnapshotManager;
 import org.lareferencia.backend.transformer.ITransformer;
 import org.lareferencia.backend.util.JsonDateSerializer;
 import org.lareferencia.backend.validator.IValidator;
 import org.lareferencia.backend.validator.ValidationResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.fasterxml.jackson.databind.annotation.JsonSerialize;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class BackEndController {
 	
 	@Autowired 
 	private ApplicationContext applicationContext;
 	
 	@Autowired
 	private NationalNetworkRepository nationalNetworkRepository;
 	
 	@Autowired
 	private NetworkSnapshotRepository networkSnapshotRepository;
 	
 	@Autowired
 	private NetworkSnapshotStatRepository statsRepository;
 	
 	@Autowired
 	private OAIRecordRepository recordRepository;
 	
 	@Autowired 
 	private OAIProviderStatRepository oaiProviderStatRepository;
 	
 	@Autowired
 	IIndexer indexer;
 	
 	@Autowired
 	TaskScheduler scheduler;
 	
 	@Autowired
 	IValidator validator;
 	
 	@Autowired
 	ITransformer transformer;
 
 	
 	//private static final Logger logger = LoggerFactory.getLogger(BackEndController.class);
 	
 	//private static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 				
 		return "home";
 	}
 	
 	
 	/************************** Backend ************************************/
 	
 	@RequestMapping(value="/private/harvester/{networkID}", method=RequestMethod.GET)
 	public ResponseEntity<String> harvesting(@PathVariable Long networkID) {
 		//TODO: debiera chequear la existencia de la red
 		
 		SnapshotManager manager = applicationContext.getBean("snapshotManager", SnapshotManager.class);
 		manager.lauchHarvesting(networkID);
 		
 		return new ResponseEntity<String>("Havesting:" + networkID, HttpStatus.OK);
 	}
 	
 	/**
 	 * Este servicio para cada origen explora los sets (no los almacenados sino los provistos por ListSets)
 	 * y para cada uno de ellos realiza una cosecha. Si los sets son disjuntos la coschecha final es completa y
 	 * sin repeticiones
 	 * @param networkID
 	 * @return
 	 */
 	@RequestMapping(value="/private/harvestSetBySet/{networkID}", method=RequestMethod.GET)
 	public ResponseEntity<String> harvestSetBySet(@PathVariable Long networkID) {
 		
 		SnapshotManager manager = applicationContext.getBean("snapshotManager", SnapshotManager.class);
 		manager.lauchSetBySetHarvesting(networkID);
 		
 		return new ResponseEntity<String>("Havesting:" + networkID, HttpStatus.OK);
 	}
 	
 	@RequestMapping(value="/private/resumeHarvestingBySnapshotID/{snapshotID}", method=RequestMethod.GET)
 	public ResponseEntity<String> resumeHarvestingBySnapshotID(@PathVariable Long snapshotID) {
 		
 		SnapshotManager manager = applicationContext.getBean("snapshotManager", SnapshotManager.class);
 		manager.relauchHarvesting(snapshotID);
 		
 		return new ResponseEntity<String>("Relauch Havesting:" + snapshotID, HttpStatus.OK);
 	}
 	
 	
 	@Transactional
 	@RequestMapping(value="/private/deleteAllButLGKSnapshot/{id}", method=RequestMethod.GET)
 	public ResponseEntity<String> deleteAllButLGKSnapshot(@PathVariable Long id) throws Exception {
 		
 		NationalNetwork network = nationalNetworkRepository.findOne(id);
 		if ( network == null )
 			throw new Exception("No se encontró RED");
 		
 		
 		NetworkSnapshot lgkSnapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(id);
 		
 		if ( lgkSnapshot != null) {
 		
 			for ( NetworkSnapshot snapshot:network.getSnapshots() ) {
 				if ( !snapshot.getId().equals(lgkSnapshot.getId()) && !snapshot.isDeleted() ) {
 					// borra los registros
 					recordRepository.deleteBySnapshotID(snapshot.getId());
 					// lo marca borrado
 					snapshot.setDeleted(true);
 					// almacena el estado del snap
 					networkSnapshotRepository.save(snapshot);
 				}
 			}
 		}
 		else
 			throw new Exception("No se encontró LGK Snapshot para esa red, no se realizaron cambios");
 
 		
 		return new ResponseEntity<String>("Borrados snapshots excedentes de:" + network.getName(), HttpStatus.OK);
 	}
 	
 	@Transactional
 	@RequestMapping(value="/private/deleteRecordsBySnapshotID/{id}", method=RequestMethod.GET)
 	public ResponseEntity<Map<String,String>> deleteRecordsBySnapshotID(@PathVariable Long id) {
 		
 		recordRepository.deleteBySnapshotID(id);
 		
 		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
 		snapshot.setDeleted(true);
 		networkSnapshotRepository.save(snapshot);
 		
 		
 		Map<String,String> result = new HashMap<String, String>();
 		result.put("result", "OK");
 		
 		ResponseEntity<Map<String,String>> response = new ResponseEntity<Map<String,String>>(result, HttpStatus.OK);
 		
 		return response;
 	}
 	
 	@RequestMapping(value="/private/indexValidRecordsBySnapshotID/{id}", method=RequestMethod.GET)
 	public ResponseEntity<Map<String,String>> indexRecordsBySnapshotID(@PathVariable Long id) {
 		
 		// Se crea un proceso separado para la indexación
 		IndexerWorker worker = applicationContext.getBean("indexerWorker", IndexerWorker.class);
 		worker.setSnapshotID(id);
 		scheduler.schedule(worker, new Date());
 	
 		Map<String,String> result = new HashMap<String, String>();
 		result.put("result", "INDEXING SNAPSHOT " + id);
 
 		return new ResponseEntity<Map<String,String>>(result, HttpStatus.OK);
 	}
 	
 
 	/**************************** FrontEnd ************************************/
 
 	@RequestMapping(value="/public/validateRecordByID/{id}", method=RequestMethod.GET)
 	public ResponseEntity<ValidationResult> validateRecordByID(@PathVariable Long id) throws OAIRecordMetadataParseException {
 		
 		
 		OAIRecord record = recordRepository.findOne( id );	
 		OAIRecordMetadata metadata = new OAIRecordMetadata(record.getIdentifier(), record.getOriginalXML());
 		
 		ValidationResult result = validator.validate(metadata);
 		
 		ResponseEntity<ValidationResult> response = new ResponseEntity<ValidationResult>(result, HttpStatus.OK);
 		
 		return response;
 	}
 	
 	@RequestMapping(value="/public/transformRecordByID/{id}", method=RequestMethod.GET)
 	public ResponseEntity<OAIRecordTransformationInfo> transformRecordByID(@PathVariable Long id) throws Exception {
 		
 		OAIRecordTransformationInfo result = new OAIRecordTransformationInfo();
 		
 		OAIRecord record = recordRepository.findOne( id );	
 		OAIRecordMetadata metadata = new OAIRecordMetadata(record.getIdentifier(), record.getOriginalXML());
 		
 		ValidationResult preValidationResult = validator.validate(metadata);
 		transformer.transform(metadata, preValidationResult);
 		ValidationResult posValidationResult = validator.validate(metadata);
 
 		result.id = id;
 		result.originalHeaderId = record.getIdentifier();
 		result.originalMetadata = record.getOriginalXML();
 		result.transformedMetadata = metadata.toString();
 		result.isOriginalValid = preValidationResult.isValid();
 		result.isTransformedValid = posValidationResult.isValid();
 		
 		ResponseEntity<OAIRecordTransformationInfo> response = new ResponseEntity<OAIRecordTransformationInfo>(result, HttpStatus.OK);
 		
 		return response;
 	}
 	
 	
 	@RequestMapping(value="/public/lastGoodKnowSnapshotByNetworkID/{id}", method=RequestMethod.GET)
 	public ResponseEntity<NetworkSnapshot> getLGKSnapshot(@PathVariable Long id) {
 			
 		NetworkSnapshot snapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(id);
 		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
 			snapshot,
 			snapshot == null ? HttpStatus.NOT_FOUND : HttpStatus.OK
 		);
 		return response;
 	}
 	
 	@RequestMapping(value="/public/lastGoodKnowSnapshotByCountryISO/{iso}", method=RequestMethod.GET)
 	public ResponseEntity<NetworkSnapshot> getLGKSnapshot(@PathVariable String iso) throws Exception {
 		
 		NationalNetwork network = nationalNetworkRepository.findByCountryISO(iso);
 		if ( network == null ) // TODO: Implementar Exc
 			throw new Exception("No se encontró RED perteneciente a: " + iso);
 		
 		NetworkSnapshot snapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(network.getId());
 		if (snapshot == null) // TODO: Implementar Exc
 			throw new Exception("No se encontró snapshot válido de la RED: " + iso);
 		
 		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
 			snapshot,
 			snapshot == null ? HttpStatus.NOT_FOUND : HttpStatus.OK
 		);
 		return response;
 	}
 	
 	@RequestMapping(value="/public/listSnapshotsByCountryISO/{iso}", method=RequestMethod.GET)
 	public ResponseEntity<List<NetworkSnapshot>> listSnapshotsByCountryISO(@PathVariable String iso) throws Exception {
 		
 		NationalNetwork network = nationalNetworkRepository.findByCountryISO(iso);
 		if ( network == null )
 			throw new Exception("No se encontró RED perteneciente a: " + iso);
 		
 		ResponseEntity<List<NetworkSnapshot>> response = new ResponseEntity<List<NetworkSnapshot>>(networkSnapshotRepository.findByNetworkOrderByEndTimeAsc(network), HttpStatus.OK);
 		
 		return response;
 	}
 	
 	
 	@RequestMapping(value="/public/listNetworks", method=RequestMethod.GET)
 	public ResponseEntity<List<NetworkInfo>> listNetworks() {
 		
 				
 		List<NationalNetwork> allNetworks = nationalNetworkRepository.findByPublishedOrderByNameAsc(true);//OrderByName();
 		List<NetworkInfo> NInfoList = new ArrayList<NetworkInfo>();
 
 		for (NationalNetwork network:allNetworks) {
 			
 			NetworkInfo ninfo = new NetworkInfo();
 			ninfo.networkID = network.getId();
 			ninfo.country = network.getCountryISO();
 			ninfo.name = network.getName();
 			
 			NetworkSnapshot snapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(network.getId());
 			
 			if ( snapshot != null) {
 				
 				ninfo.snapshotID = snapshot.getId();
 				ninfo.datestamp = snapshot.getEndTime();
 				ninfo.size = snapshot.getSize();
 				ninfo.validSize = snapshot.getValidSize();
 				
 			}		
 			NInfoList.add( ninfo );		
 		}
 	
 		ResponseEntity<List<NetworkInfo>> response = new ResponseEntity<List<NetworkInfo>>(NInfoList, HttpStatus.OK);
 		
 		return response;
 	}
 	
 	@RequestMapping(value="/public/listNetworksHistory", method=RequestMethod.GET)
 	public ResponseEntity<List<NetworkHistory>> listNetworksHistory() {
 		
 		List<NationalNetwork> allNetworks = nationalNetworkRepository.findByPublishedOrderByNameAsc(true);//OrderByName();
 		List<NetworkHistory> NHistoryList = new ArrayList<NetworkHistory>();
 
 		for (NationalNetwork network:allNetworks) {	
 			NetworkHistory nhistory = new NetworkHistory();
 			nhistory.networkID = network.getId();
 			nhistory.country = network.getCountryISO();
 			nhistory.validSnapshots =  networkSnapshotRepository.findByNetworkAndStatusOrderByEndTimeAsc(network, SnapshotStatus.VALID);
 			NHistoryList.add( nhistory );		
 		}
 	
 		ResponseEntity<List<NetworkHistory>> response = new ResponseEntity<List<NetworkHistory>>(NHistoryList, HttpStatus.OK);
 		
 		return response;
 	}
 	
 	@RequestMapping(value="/public/listProviderStats", method=RequestMethod.GET)
 	@ResponseBody
 	public PageResource<OAIProviderStat> listProviderStats(@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) {
 		
 		if (page == null)
 			page = 0;
 		if (size == null)
 			size = 100;
 		
 		Page<OAIProviderStat> pageResult = oaiProviderStatRepository.findAll( new PageRequest(page, size, new Sort(Sort.Direction.DESC,"requestCount")));	
 		
 		return new PageResource<OAIProviderStat>(pageResult,"page","size");
 	}
 	
 	@RequestMapping(value="/public/listInvalidRecordsInfoBySnapshotID/{id}", method=RequestMethod.GET)
 	@ResponseBody
 	public PageResource<OAIRecord> listInvalidRecordsInfoBySnapshotID(@PathVariable Long id, @RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) throws Exception {
 		
 		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
 		
 		if (snapshot == null) // TODO: Implementar Exc
 			throw new Exception("No se encontró snapshot con id: " + id);
 			
 		if (page == null)
 			page = 0;
 		if (size == null)
 			size = 100;
 		
 		Page<OAIRecord> pageResult = recordRepository.findBySnapshotAndStatus(snapshot, RecordStatus.INVALID, new PageRequest(page, size));	
 		
 		return new PageResource<OAIRecord>(pageResult,"page","size");
 	}
 	
 	@RequestMapping(value="/public/listTransformedRecordsInfoBySnapshotID/{id}", method=RequestMethod.GET)
 	@ResponseBody
 	public PageResource<OAIRecord> listTransformedRecordsInfoBySnapshotID(@PathVariable Long id, @RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) throws Exception {
 		
 		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
 		
 		if (snapshot == null) // TODO: Implementar Exc
 			throw new Exception("No se encontró snapshot con id: " + id);
 			
 		if (page == null)
 			page = 0;
 		if (size == null)
 			size = 100;
 		
 		Page<OAIRecord> pageResult = recordRepository.findBySnapshotAndWasTransformed(snapshot, true, new PageRequest(page, size));	
 		
 		return new PageResource<OAIRecord>(pageResult,"page","size");
 	}
 	
 	
 	@ResponseBody
 	@RequestMapping(value="/public/metadataOccurrenceCountBySnapshotId/{id}", method=RequestMethod.GET)
 	public List<NetworkSnapshotStat> metadataOccurrenceCountBySnapshotId(@PathVariable Long id) throws Exception {
 		
 		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
 		if (snapshot == null) 
 			throw new Exception("No se encontró snapshot: " + id);
 		
 		List<NetworkSnapshotStat> stats = statsRepository.findBySnapshotAndStatId(snapshot, MetadataOccurrenceCountSnapshotStatProcessor.ID);
 		
 		return stats;
 	}
 	
 	@ResponseBody
 	@RequestMapping(value="/public/rejectedFieldCountBySnapshotId/{id}", method=RequestMethod.GET)
 	public List<NetworkSnapshotStat> rejectedFieldCountBySnapshotId(@PathVariable Long id) throws Exception {
 		
 		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
 		if (snapshot == null) // TODO: Implementar Exc
 			throw new Exception("No se encontró snapshot: " + id);
 		
 		List<NetworkSnapshotStat> stats = statsRepository.findBySnapshotAndStatId(snapshot, RejectedByFieldSnapshotStatProcessor.ID);
 			
 		return stats;
 	}
 	
 	/**************  Clases de retorno de resultados *******************/
 	
 	@Getter
 	@Setter
 	class NetworkInfo {	
 		private Long   networkID;
 		private String country;
 		private String name;
 		
 		private Long snapshotID;
 		
 		@JsonSerialize(using=JsonDateSerializer.class)
 		private Date datestamp;
 		private int size;
 		private int validSize;
 	}
 	
 	@Getter
 	@Setter
 	class NetworkHistory {	
 		private Long   networkID;
 		private String country;
 		private List<NetworkSnapshot> validSnapshots;
 	}
 	
 	@Getter
 	@Setter
 	class OAIRecordValidationInfo {	
 		private Long   id;
 		private String originalHeaderId;
 		private boolean isValid;
 		private boolean isDriverType;
 		private String  dcTypeFieldContents;
 	}
 	
 	@Getter
 	@Setter
 	class OAIRecordTransformationInfo {	
 		private Long   id;
 		private String originalHeaderId;
 		private String originalMetadata;
 		private String transformedMetadata;
 		private boolean isOriginalValid;
 		private boolean isTransformedValid;
 	}
 	
 	
 }
