 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package au.org.plantphenomics.podd;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.github.podd.client.api.PoddClientException;
 import com.github.podd.client.impl.restlet.RestletPoddClientImpl;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides operations specific to HRPPC in relation to putting projects into PODD.
  *
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class HrppcPoddClient extends RestletPoddClientImpl 
 {
 	private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     	public static final int MIN_HEADERS_SIZE = 7;
 	
 	public static final String TRAY_ID = "TrayID";
 	public static final String TRAY_NOTES = "TrayNotes";
 	public static final String TRAY_TYPE_NAME = "TrayTypeName";
 	public static final String POSITION = "Position";
 	public static final String PLANT_ID = "PlantID";
 	public static final String PLANT_NAME = "PlantName";
 	public static final String PLANT_NOTES = "PlantNotes";
 	
	public static final Pattern REGEX_PROJECT = Pattern.compile("^PROJECT#(\\d{4})-(\\d{4})");
 	
 	// PROJECT#YYYY-NNNN_EXPERIMENT#NNNN_GENUS.SPECIES_TRAY#NNNNN
	public static final Pattern REGEX_TRAY = Pattern.compile("PROJECT#(\\d{4})-(\\d{4})_EXPERIMENT#(\\d{4})_(\\w+)\.(\\w+)_TRAY#(\\d{4,5})");
 	
     	/**
     	 * Number of groups matching in the tray id regex.
     	 */
     	public static final int TRAY_ID_SIZE = 6;
 	
 	// PROJECT#YYYY-NNNN_EXPERIMENT#NNNN_GENUS.SPECIES_TRAY#NNNNN_POT#NNNNN
	public static final Pattern REGEX_PLANT = Pattern.compile("PROJECT#(\\d{4})-(\\d{4})_EXPERIMENT#(\\d{4})_(\\w+)\.(\\w+)_TRAY#(\\d{4,5})_POT#(\\d{4,5})");
 	
     	/**
     	 * Number of groups matching in the plant id regex.
     	 */
     	public static final int PLANT_ID_SIZE = 7;
 	
     	public static final Pattern REGEX_POSITION = Pattern.compile("([a-zA-Z]+)([0-9])");
     	
     	/**
     	 * Number of groups matching in the position regex.
     	 */
     	public static final int POSITION_SIZE = 7;
 	
 	public HrppcPoddClient()
 	{
 		super();
 	}
 	
 	public HrppcPoddClient(final String serverUrl)
 	{
 		super(serverUrl);
 	}
     
 	/**
 	 * Parses the given PlantScan project/experiment/tray/pot list and inserts the 
 	 * items into PODD where they do not exist.
 	 *
 	 * TODO: Should this process create new projects where they do not already exist? 
 	 * Ideally they should be created and roles assigned before this process, but could be fine to do that in here
 	 */
 	public void uploadPlantScanList(InputStream in) throws IOException, PoddClientException
 	{
 		List<InferredOWLOntologyID> currentUnpublishedArtifacts = this.listUnpublishedArtifacts();
 		
 		// Keep a queue so that we only need to update each project once for this operation to succeed
 		ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue = new ConcurrentHashMap<>();
 		
 		// Map starting at project name strings and ending with both the URI of the project and the artifact
 		ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap = new ConcurrentHashMap<>();
 		
 		// Map known project names to their URIs, as the URIs are needed to create statements internally
 		populateProjectUriMap(currentUnpublishedArtifacts, projectUriMap);
 		
 		// TODO: Implement getObjectsByType(InferredOWLOntology, URI) so that experiments etc can be found easily 
 		// and the identifier can be mapped as necessary to the identifier in the header
 		
 		List<String> headers = null;
                 CSVReader reader = new CSVReader(new InputStreamReader(in, Charset.forName("UTF-8")));
                 String[] nextLine;
                 while ((nextLine = reader.readNext()) != null) 
                 {
                         if (headers == null) 
                         {
                                 // header line is mandatory in PODD CSV
                                 headers = Arrays.asList(nextLine);
                                 try
                                 {
                                 	verifyProjectListHeaders(headers);
                                 }
                                 catch(IllegalArgumentException e)
                                 {
                                 	this.log.error("Could not verify headers for project list: {}", e.getMessage());
                                 	throw new PoddClientException("Could not verify headers for project list", e);
                                 }
                         }
                         else 
                         {
 				if(nextLine.length != headers.size())
 				{
 					this.log.error("Line and header sizes were different: {} {}", headers, nextLine);
 				}
         	
                         	// Process the next line and add it to the upload queue 
                        	processPlantScanLine(headers, Arrays.asList(nextLine), projectUriMap, uploadQueue);
                         }
                 }
                 
                 if(headers == null)
                 {
                 	this.log.error("Document did not contain headers");
                 }
                 
                 if(uploadQueue.isEmpty())
                 {
                 	this.log.error("Document did not contain any rows");
                 }
                 
                 for(InferredOWLOntologyID nextUpload : uploadQueue.keySet())
                 {
                 	StringWriter writer = new StringWriter();
                 	Rio.write(uploadQueue.get(nextUpload), writer, RDFFormat.RDFJSON);
                 	InferredOWLOntologyID newID = this.appendArtifact(nextUpload, new ByteArrayInputStream(writer.toString().getBytes(Charset.forName("UTF-8"))), RDFFormat.RDFJSON);
                 	
                 	if(newID == null)
                 	{
                 		this.log.error("Did not find a valid result from append artifact: {}", nextUpload);
                 	}
                 	
                 	if(nextUpload.equals(newID))
                 	{
                 		this.log.error("Result from append artifact was not changed, as expected. {} {}", nextUpload, newID);
                 	}
                 }
         }
         
         private void populateProjectUriMap(List<InferredOWLOntologyID> currentUnpublishedArtifacts, ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap)
         {
 		for(InferredOWLOntologyID nextArtifact : currentUnpublishedArtifacts) 
 		{
 			// TODO: Implement getTopObject(InferredOWLOntologyID) so that the top object for each can be 
 			// scanned easily to determine its name which is required, by convention, here
 			Model nextTopObject = getTopObject(nextArtifact);
 			
 			Model types = nextTopObject.filter(null, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_PROJECT);
 			if(types.isEmpty())
 			{
 				// We only map project based artifacts, others are ignored with log messages here
 				this.log.info("Found a non-project based artifact, ignoring as it isn't relevant here: {}", nextArtifact);
 			}
 			else if(types.subjects().size() > 1)
 			{
 				// We only map single-project based artifacts, others are ignored with log messages here
 				this.log.error("Found multiple projects for an artifact: {} {}", nextArtifact, types.subjects());
 			}
 			else
 			{
 				Resource project = types.subjects().iterator().next();
 				
 				if(project instanceof URI)
 				{
 					Model label = nextTopObject.filter(project, RDFS.LABEL, null);
 					
 					if(label.isEmpty())
 					{
 						this.log.error("Project did not have a label: {} {}", nextArtifact, project);
 					}
 					else
 					{
 						for(Value nextLabel : label.objects())
 						{
 							if(nextLabel instanceof Literal)
 							{
 								String nextLabelString = nextLabel.stringValue();
 								
 								if(!REGEX_PROJECT.matcher(nextLabelString).matches())
 								{
 									this.log.error("Found project label that did not start with expected format: {}", nextLabel);
 									continue;
 								}
 								
 								// take off any descriptions and leave the project number behind
 								nextLabelString = nextLabelString.split(" ")[0];
 								
 								ConcurrentMap<URI, InferredOWLOntologyID> labelMap = new ConcurrentHashMap<>();
 								ConcurrentMap<URI, InferredOWLOntologyID> putIfAbsent = projectUriMap.putIfAbsent(nextLabelString, labelMap);
 								if(putIfAbsent != null)
 								{
 									this.log.error("Found duplicate project name, inconsistent results may follow: {} {} {}", nextArtifact, project, nextLabel);
 									// Overwrite our reference with the one that already existed
 									labelMap = putIfAbsent;
 								}
 								InferredOWLOntologyID existingArtifact = labelMap.putIfAbsent((URI)project, nextArtifact);
 								// Check for the case where project name maps to different artifacts
 								if(existingArtifact != null && !existingArtifact.equals(nextArtifact)) 
 								{
 									this.log.error("Found duplicate project name across different projects, inconsistent results may follow: {} {} {} {}", nextArtifact, existingArtifact, project, nextLabel);
 								}
 							}
 							else
 							{
 								this.log.error("Project had a non-literal label: {} {} {}", nextArtifact, project, nextLabel);
 							}
 						}
 					}
 				}
 				else
 				{
 					// We only map URI references, as blank nodes which are allowable, cannot be reserialised to update the artifact, and should not exist
 					this.log.error("Found non-URI project reference for an artifact: {} {}", nextArtifact, types.subjects());
 				}
 			}
 		}
         }
         
         /**
          * Process a single line from the input file, using the given headers as the definitions for the line. 
          */
         private void processPlantScanLine(List<String> headers, List<String> nextLine, ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap, ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue)
         {
         	String trayId = null;
         	String trayNotes = null;
         	String trayTypeName = null;
         	String position = null;
         	String plantId = null;
         	String plantName = null;
         	String plantNotes = null;
         	
         	for(int i = 0; i < headers.size();i++)
         	{
         		String nextHeader = headers.get(i);
 			String nextField = nextLine.get(i);
         		
 			if(nextHeader.trim().equals(TRAY_ID))
 			{
 				trayId = nextField;
 			}
 			else if(nextHeader.trim().equals(TRAY_NOTES))
 			{
 				trayNotes = nextField;
 			}
 			else if(nextHeader.trim().equals(TRAY_TYPE_NAME))
 			{
 				trayTypeName = nextField;
 			}
 			else if(nextHeader.trim().equals(POSITION))
 			{
 				position = nextField;
 			}
 			else if(nextHeader.trim().equals(PLANT_ID))
 			{
 				plantId = nextField;
 			}
 			else if(nextHeader.trim().equals(PLANT_NAME))
 			{
 				plantName = nextField;
 			}
 			else if(nextHeader.trim().equals(PLANT_NOTES))
 			{
 				plantNotes = nextField;
 			}
 			else
 			{
 				this.log.error("Found unrecognised header: {} {}", nextHeader, nextField);
 			}
 		}
 		
 		String projectYear = null;
 		String projectNumber = null;
 		String experimentNumber = null;
 		String genus = null;
 		String species = null;
 		String trayNumber = null;
 		String potNumber = null;
 		
 		Matcher trayMatcher = REGEX_TRAY.matcher(trayId);
 		
 		if(!trayMatcher.matches())
 		{
 			this.log.error("Tray ID did not match expected format: {}", trayId);
 		}
 		else
 		{
 			if(trayMatcher.groupCount() != TRAY_ID_SIZE)
 			{
 				this.log.error("Did not find the expected number of regex matches for Tray ID: {} {}", trayMatcher.groupCount(), TRAY_ID_SIZE);
 			}
 			else
 			{
 				projectYear = trayMatcher.group(1);
 				projectNumber = trayMatcher.group(2);
 				experimentNumber = trayMatcher.group(3);
 				genus = trayMatcher.group(4);
 				species = trayMatcher.group(5);
 				trayNumber = trayMatcher.group(6);
 			}
 		}
 		
 		Matcher plantMatcher = REGEX_PLANT.matcher(plantId);
 		
 		if(!plantMatcher.matches())
 		{
 			this.log.error("Plant ID did not match expected format: {}", plantId);
 		}
 		else
 		{
 			if(plantMatcher.groupCount() != PLANT_ID_SIZE)
 			{
 				this.log.error("Did not find the expected number of regex matches for Plant ID: {} {}", plantMatcher.groupCount(), PLANT_ID_SIZE);
 			}
 			else
 			{
 				if(projectYear == null)
 				{
 					projectYear = plantMatcher.group(1);
 				}
 				if(projectNumber == null)
 				{
 					projectNumber = plantMatcher.group(2);
 				}
 				if(experimentNumber == null)
 				{
 					experimentNumber = plantMatcher.group(3);
 				}
 				if(genus == null)
 				{
 					genus = plantMatcher.group(4);
 				}
 				if(species == null)
 				{
 					species = plantMatcher.group(5);
 				}
 				if(trayNumber == null)
 				{
 					trayNumber = plantMatcher.group(6);
 				}
 				potNumber = plantMatcher.group(7);
 			}
 		}
 		
 		String columnLetter = null;
 		String rowNumber = null;
 		
 		Matcher positionMatcher = REGEX_POSITION.matcher(position);
 		
 		if(!positionMatcher.matches())
 		{
 			this.log.error("Position did not match expected format: {}", position);
 		}
 		else
 		{
 			if(positionMatcher.groupCount() != POSITION_SIZE)
 			{
 				this.log.error("Did not find the expected number of regex matches for Position: {} {}", positionMatcher.groupCount(), POSITION_SIZE);
 			}
 			else
 			{
 				columnLetter = positionMatcher.group(1);
 				rowNumber = positionMatcher.group(2);
 			}
 		}
         }
         
         /**
          * Verifies the list of projects, throwing an IllegalArgumentException if there are 
          * unrecognised headers or if any mandatory headers are missing.
          * 
          * @throws IllegalArgumentException If the headers are not verified correctly.
          */
         private void verifyProjectListHeaders(List<String> headers) throws IllegalArgumentException
         {
         	if(headers == null || headers.size() < MIN_HEADERS_SIZE)
         	{
         		this.log.error("Did not find valid headers: {}", headers);
         		throw new IllegalArgumentException("Did not find valid headers");
         	}
         	
         	if(!headers.contains(TRAY_ID))
         	{
         		throw new IllegalArgumentException("Did not find tray id header");
         	}
 
         	if(!headers.contains(TRAY_NOTES))
         	{
         		throw new IllegalArgumentException("Did not find tray notes header");
         	}
 
         	if(!headers.contains(TRAY_TYPE_NAME))
         	{
         		throw new IllegalArgumentException("Did not find tray type name header");
         	}
 
         	if(!headers.contains(POSITION))
         	{
         		throw new IllegalArgumentException("Did not find position header");
         	}
         	
         	if(!headers.contains(PLANT_ID))
         	{
         		throw new IllegalArgumentException("Did not find plant id header");
         	}
         	
         	if(!headers.contains(PLANT_NAME))
         	{
         		throw new IllegalArgumentException("Did not find plant name header");
         	}
         	
         	if(!headers.contains(PLANT_NOTES))
         	{
         		throw new IllegalArgumentException("Did not find plant notes header");
         	}
         }
 }
