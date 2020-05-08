 package org.metware.mzAnnotate;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import org.openscience.cdk.ChemFile;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.interfaces.IChemFile;
 import org.openscience.cdk.io.CMLReader;
 import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
 
 import de.ipbhalle.metfrag.TEMP.PeakMzAnno;
 import de.ipbhalle.metfrag.TEMP.Spectrum;
 
 
 public class MzAnnotateReader {
 	
 	private static final String SPECTRUM = "spectrum";
 	private static final String CONDITIONLIST = "conditionList";
 	private static final String SCALAR = "scalar";
 	private static final String UNITS = "units";
 	private static final String POLARITY = "cml:polarity";
 	private static final String COLLISIONENERGY = "cml:collisionenergy";
 	private static final String METADATALIST = "metadataList";
 	private static final String METADATA = "metadata";
 	private static final String METADATACONTENT = "content";
 	private static final String METADATANAME = "name";
 	private static final String PEAKLIST = "peakList";
 	private static final String PEAK = "peak";
 	private static final String PEAKMOLECULE = "molecule";
 	private static final String PEAKMOLECULEREF = "ref";
 	private static final String PEAKMZ = "xValue";
 	private static final String PEAKINT = "yValue";
 	private static final String ASSIGNMENTMETHOD = "ms:assignmentMethod";
 	private static final String EXACTMASS = "ms:exactMass";
 	private static final String REACTIONLIST = "reactionList";
 	private static final String REACTION = "reaction";
 	private static final String REACTANTLIST = "reactantList";
 	private static final String REACTANT = "reactant";
 	private static final String REACTIONMOLECULE = "molecule";
 	private static final String REACTIONMOLECULEREF = "ref";
 	private static final String PRODUCTLIST = "productList";
 	private static final String PRODUCT = "product";
 	private static final String PRODUCTMOLECULE = "molecule";
 	private static final String PRODUCTMOLECULEREF = "ref";
 	private static final String MOLECULELIST = "moleculeList";
 	private static final String MOLECULEMOLECULE = "molecule";
 	
 	
 	
 	
 	public MzAnnotateReader()
 	{
 		
 	}
 	
 	/**
 	 * Read mzAnnotate from string.
 	 * 
 	 * @param mzAnnotateString the mz annotate string
 	 * 
 	 * @return the mz annotate
 	 * 
 	 * @throws FileNotFoundException the file not found exception
 	 * @throws XMLStreamException the XML stream exception
 	 * @throws CDKException the CDK exception
 	 */
 	public MzAnnotate readMzAnnotateFromString(String mzAnnotateString) throws FileNotFoundException, XMLStreamException, CDKException
 	{
 		InputStream in = new ByteArrayInputStream(mzAnnotateString.getBytes());
 		XMLInputFactory factory = XMLInputFactory.newInstance(); 
 		XMLEventReader parser = factory.createXMLEventReader( in ); 
 		MzAnnotate ret = readMzAnnot(parser);
 		return ret;
 	}
 	
 	
 	/**
 	 * Read mzAnnotate.
 	 * 
 	 * @param file the file
 	 * 
 	 * @return the mz annotate
 	 * 
 	 * @throws FileNotFoundException the file not found exception
 	 * @throws XMLStreamException the XML stream exception
 	 * @throws CDKException the CDK exception
 	 */
 	public MzAnnotate readMzAnnotate(String file) throws FileNotFoundException, XMLStreamException, CDKException
 	{
 		InputStream in = new FileInputStream(file); 
 		XMLInputFactory factory = XMLInputFactory.newInstance(); 
 		XMLEventReader parser = factory.createXMLEventReader( in ); 
 		MzAnnotate ret = readMzAnnot(parser);
 		return ret;
 	}
 	
 	private MzAnnotate readMzAnnot(XMLEventReader parser) throws XMLStreamException, FileNotFoundException, CDKException
 	{
 		
 		StringBuilder spacer = new StringBuilder(); 
 		
 		
 		String spectrumCollisionEnergy = "";
 		Integer spectrumMode = -1;
 		String spectrumAssignmentMethod = "";
 		//TODO: allow several names
 		String spectrumName = "";
 		Double spectrumExactMass = 0.0;
 		Map<String, String> spectrumMetaDataMap = new HashMap<String, String>();
 		Map<Double, String> spectrumPeakToStructure = new HashMap<Double, String>();
 		Vector<PeakMzAnno> spectrumPeakList = new Vector<PeakMzAnno>();
 		List<Spectrum> spectra = new ArrayList<Spectrum>();
 		
 		Map<String, List<String>> reactionListReactantToProduct = new HashMap<String, List<String>>();
 		
 		List<String> moleculeListString = new ArrayList<String>();
 		
 		
 		boolean isSpectrumData = false;
 		boolean isConditionList = false;
 		boolean isScalarCondition = false;
 		boolean isCollisionEnergy = false;
 		boolean isMode = false;
 		boolean isMetaDataList = false;
 		boolean isMetaData = false;
 		boolean isPeakList = false;
 		boolean isPeak = false;
 		boolean isPeakMolRef = false;
 		
 		boolean isReactionList = false;
 		boolean isReaction = false;
 		boolean isReactantList = false;
 		boolean isReactant = false;
 		boolean isReactantMolecule = false;
 		boolean isProductList = false;
 		boolean isProduct = false;
 		boolean isProductMolecule = false;
 		
 		boolean isMoleculeList = false;
 		boolean isMolecule = false;
 		
 	    //peak
 	    String peakMZ = "";
 	    String peakInt = "";
 	    //reaction list
 	    String reactant = "";
 	    List<String> productList = new ArrayList<String>();
 	    
 	    String moleculeCMLString = "";
 	    
 		while ( parser.hasNext() ) 
 		{ 
 		  
 		  XMLEvent event = parser.nextEvent(); 
 		  switch ( event.getEventType() ) 
 		  { 
 		    case XMLStreamConstants.START_DOCUMENT: 
 		      System.out.println( "START_DOCUMENT:" ); 
 		      break;
 		      
 		      
 		    case XMLStreamConstants.END_DOCUMENT: 
 		      System.out.println( "END_DOCUMENT:" ); 
 		      parser.close(); 
 		      break; 
 		      
 		      
 		    case XMLStreamConstants.START_ELEMENT: 
 		      StartElement element = event.asStartElement(); 
 		      spacer.append( "  " ); 
 		      System.out.println( spacer.toString() + "START_ELEMENT: " + element.getName() ); 
 		      
 		      //start of spectrum
 		      if(element.getName().getLocalPart().equals(SPECTRUM))
 		    	  isSpectrumData = true;
 		      //start of condition list
 		      if(element.getName().getLocalPart().equals(CONDITIONLIST))
 		    	  isConditionList = true;
 		      //start of condition list
 		      if(element.getName().getLocalPart().equals(SCALAR))
 		    	  isScalarCondition = true;
 		      //start of metadataList
 		      if(element.getName().getLocalPart().equals(METADATALIST))
 		    	  isMetaDataList = true;
 		      //start of metadataList
 		      if(element.getName().getLocalPart().equals(METADATA))
 		    	  isMetaData = true;
 		      //start peakList
 		      if(element.getName().getLocalPart().equals(PEAKLIST))
 		    	  isPeakList = true;
 		      //start peak
 		      if(element.getName().getLocalPart().equals(PEAK))
 		    	  isPeak = true;
 		      //start peak molecule ref
 		      if(element.getName().getLocalPart().equals(PEAKMOLECULE) && isPeak)
 		    	  isPeakMolRef = true;
 		    	  
 		      
 		      //start reaction list
 		      if(element.getName().getLocalPart().equals(REACTIONLIST))
 		    	  isReactionList = true;
 		      //start reaction
 		      if(element.getName().getLocalPart().equals(REACTION))
 		    	  isReaction = true;
 		      //start reactant list
 		      if(element.getName().getLocalPart().equals(REACTANTLIST))
 		    	  isReactantList = true;
 		      //start reactant list
 		      if(element.getName().getLocalPart().equals(REACTANT))
 		    	  isReactant = true;
 		      //start reactant molecule ref
 		      if(element.getName().getLocalPart().equals(REACTIONMOLECULE) && isReactant)
 		    	  isReactantMolecule = true;
 		      //start product list
 		      if(element.getName().getLocalPart().equals(PRODUCTLIST))
 		    	  isProductList = true;
 		      //start product list
 		      if(element.getName().getLocalPart().equals(PRODUCT))
 		    	  isProduct = true;
 		      //start product molecule ref
 		      if(element.getName().getLocalPart().equals(PRODUCTMOLECULE) && isProduct)
 		    	  isProductMolecule = true;
 		      
 		      //start molecule list
 		      if(element.getName().getLocalPart().equals(MOLECULELIST))
 		    	  isMoleculeList = true;
 		      //start molecule molecule ref
 		      if(element.getName().getLocalPart().equals(MOLECULEMOLECULE) && isMoleculeList)
 		    	  isMolecule = true;
 		      
 		      
 		      if(isMolecule && element.getAttributes().hasNext())
 		    	  moleculeCMLString += "<" + element.getName().getLocalPart() +  " ";
 		      else if(isMolecule && !element.getAttributes().hasNext())
 		    	  moleculeCMLString += "<" + element.getName().getLocalPart() +  ">";
 		      
 		      //ATTRIBUTES
 		      
 		      //metadata
 		      String metaDataContent = "";
 		      String metaDataName = "";
 		      
 
 		      for ( Iterator<?> attributes = element.getAttributes(); 
 		            attributes.hasNext(); ) 
 		      { 
 		        Attribute attribute = (Attribute) attributes.next(); 
 		        System.out.println( spacer.toString() + "  Attribut: " 
 		                            + attribute.getName() + " Wert: " 
 		                            + attribute.getValue() ); 
 		        
 		        if(isMolecule && attributes.hasNext())
 		        {
 		        	moleculeCMLString += attribute.getName().getLocalPart() + "=\"" + attribute.getValue() + "\" ";
 		        }
 		        else if(isMolecule && !attributes.hasNext())
 		        {
 		        	moleculeCMLString += attribute.getName().getLocalPart() + "=\"" + attribute.getValue() + "\" >";
 		        }
 		        
 		        if(isSpectrumData && isConditionList && isScalarCondition && attribute.getValue().equals(COLLISIONENERGY))
 		        {
 		        	isCollisionEnergy = true;
 		        }
 		        
 		        if(isSpectrumData && isConditionList && isScalarCondition && attribute.getValue().equals(POLARITY))
 		        {
 		        	isMode = true;
 		        }
 		        
 		        //attribute from conditionlist scalar 
 		        //get the unit!
 		        //TODO: get all data from the attributes
 		        if(isSpectrumData && isConditionList && isScalarCondition && attribute.getName().getLocalPart().equals(UNITS))
 		        {
 		        	spectrumCollisionEnergy = attribute.getValue().split(":")[1];
 		        }
 		        
 		        //metadata
 		        if(isSpectrumData && isMetaDataList && isMetaData)
 		        {
 		        	if(attribute.getName().getLocalPart().equals(METADATACONTENT))
 		        		metaDataContent = attribute.getValue();
 		        	else if(attribute.getName().getLocalPart().equals(METADATANAME))
 		        		metaDataName = attribute.getValue();	
 		        }
 		        
 		        
 		        //peak data
 		        if(isSpectrumData && isPeakList && isPeak)
 		        {
 		        	if(attribute.getName().getLocalPart().equals(PEAKMZ))
 		        		peakMZ = attribute.getValue();
 		        	else if(attribute.getName().getLocalPart().equals(PEAKINT))
 		        		peakInt = attribute.getValue();
 		        }
 
 		        //peak mol ref
 		        if(isSpectrumData && isPeakList && isPeak && isPeakMolRef)
 		        {
 		        	if(attribute.getName().getLocalPart().equals(PEAKMOLECULEREF))
 		        		spectrumPeakToStructure.put(Double.parseDouble(peakMZ), attribute.getValue());
 		        }
 		        
 		        
 		        //reactant mol ref
 		        if(isReactionList && isReaction && isReactantList && isReactant && isReactantMolecule)
 		        {
 		        	if(attribute.getName().getLocalPart().equals(REACTIONMOLECULEREF))
 		        		reactant = attribute.getValue();
 		        }
 		        //product mol ref
 		        if(isReactionList && isReaction && isProductList && isProduct && isProductMolecule)
 		        {
 		        	if(attribute.getName().getLocalPart().equals(PRODUCTMOLECULEREF))
 		        		productList.add(attribute.getValue());
 		        }
 		        
 		        
 		      } 
 		      
 		      //now add the metadata to the hashmap
 		      if(isSpectrumData && isMetaDataList && isMetaData)
 		      {
 		    	  spectrumMetaDataMap.put(metaDataName, metaDataContent);
 		      }
 		      
 		      break; 
 		    case XMLStreamConstants.CHARACTERS: 
 		      Characters characters = event.asCharacters(); 
 		      if ( ! characters.isWhiteSpace() ) 
 		        System.out.println( spacer.toString() 
 		                            + "  CHARACTERS: " 
 		                            + characters.getData() ); 
 		      
 		      if(isMolecule)
 		    	  moleculeCMLString += characters.getData();
 		      
 		      
 		      //collision energy
 		      if(isSpectrumData && isConditionList && isScalarCondition && isCollisionEnergy)
 		      {
 		    	  //the unit is previously stored
 		    	  spectrumCollisionEnergy = characters.getData() + spectrumCollisionEnergy;
 		    	  isCollisionEnergy = false;
 		      }
 		    	  
 		      
 		      //positive or negative mode
 		      if(isSpectrumData && isConditionList && isScalarCondition && isMode)
 		      {
 		    	  //the unit is previously stored
 		    	  String temp = characters.getData();
 		    	  if(temp.equals("positive"))
 		    		  spectrumMode = 1;
 		    	  else
 		    		  spectrumMode = -1;
 		    	  
 		    	  isMode = false;
 		      }
 		      
 		      
 		      break; 
 		    case XMLStreamConstants.END_ELEMENT: 
 		      System.out.println( spacer.toString() 
 		                          + "END_ELEMENT: " 
 		                          + event.asEndElement().getName() ); 
 		      spacer.delete( (spacer.length() - 2), spacer.length() ); 
 		      
 		      if(isMolecule)
 		    	  moleculeCMLString += "</" + event.asEndElement().getName().getLocalPart() + ">";
 		      
 		      //no more spectrum data
 		      if(event.asEndElement().getName().getLocalPart().equals(SPECTRUM) && isSpectrumData)
 		      {
		    	  spectrumExactMass = Double.parseDouble(spectrumMetaDataMap.get(EXACTMASS));
		  		  //add assignment method to spectrum!
		    	  spectrumAssignmentMethod = spectrumMetaDataMap.get(ASSIGNMENTMETHOD);
 		    	  //it is possible to have several spectra in one file
		    	  spectra.add(new Spectrum(Integer.parseInt(spectrumCollisionEnergy.substring(0, 2)), spectrumPeakList, spectrumExactMass, spectrumMode));
 		    	  isSpectrumData = false;
 		      }
 		      
 		      //close condition list inside of spectrum
 		      if(event.asEndElement().getName().getLocalPart().equals(CONDITIONLIST) && isSpectrumData && isConditionList)
 		    	  isConditionList = false;
 
 		      
 		      //close scalar inside of conditionlist
 		      if(event.asEndElement().getName().getLocalPart().equals(SCALAR) && isSpectrumData && isConditionList && isScalarCondition)
 		    	  isScalarCondition = false;
 		      
 		      //close metadata list inside of spectrum
 		      if(event.asEndElement().getName().getLocalPart().equals(METADATALIST) && isSpectrumData && isMetaDataList)
 		    	  isMetaDataList = false;
 		      
 		      //close metadata inside of spectrum
 		      if(event.asEndElement().getName().getLocalPart().equals(METADATA) && isSpectrumData && isMetaDataList && isMetaData)
 		    	  isMetaData = false;
 		      
 		      //close peakList inside of spectrum
 		      if(event.asEndElement().getName().getLocalPart().equals(PEAKLIST) && isSpectrumData && isPeakList)
 		    	  isPeakList = false;
 
 		      //close peak inside of spectrum
 		      if(event.asEndElement().getName().getLocalPart().equals(PEAK) && isSpectrumData && isPeakList && isPeak)
 		      {
 		    	  //write peak data to vector
 		    	  PeakMzAnno peak = new PeakMzAnno(Double.parseDouble(peakMZ), Double.parseDouble(peakInt));
 		    	  spectrumPeakList.add(peak);
 		    	  isPeak = false;
 		      }
 		      //close peak mol ref inside of spectrum
 		      if(event.asEndElement().getName().getLocalPart().equals(PEAKMOLECULE) && isSpectrumData && isPeakList && isPeak && isPeakMolRef)
 		    	  isPeakMolRef = false;
 
 		      
 		      
 		      //close reaction list
 		      if(event.asEndElement().getName().getLocalPart().equals(REACTIONMOLECULE) && isReactant && isReactantList && isReaction && isReactionList)
 		    	  isReactantMolecule = false;
 		      if(event.asEndElement().getName().getLocalPart().equals(REACTANT) && isReactantList && isReaction && isReactionList)
 		    	  isReactant = false;
 		      if(event.asEndElement().getName().getLocalPart().equals(REACTANTLIST) && isReaction && isReactionList)
 		    	  isReactantList = false;
 		      if(event.asEndElement().getName().getLocalPart().equals(PRODUCTMOLECULE) && isProduct && isProductList && isReaction && isReactionList)
 		    	  isProductMolecule = false;
 		      if(event.asEndElement().getName().getLocalPart().equals(PRODUCT) && isProductList && isReaction && isReactionList)
 		    	  isProduct = false;
 		      if(event.asEndElement().getName().getLocalPart().equals(PRODUCTLIST) && isReaction && isReactionList)
 		    	  isProductList = false;
 		      //now close reaction
 		      if(event.asEndElement().getName().getLocalPart().equals(REACTION) && isReactionList)
 		      {
 		    	  reactionListReactantToProduct.put(reactant, productList);
 		    	  isReaction = false;
 		      }
 		      
 		      
 		      //molecule
 		      if(event.asEndElement().getName().getLocalPart().equals(MOLECULEMOLECULE) && isMoleculeList)
 		      {
 		    	  //TODO: read in the cml molecule
 		    	  moleculeListString.add(moleculeCMLString);
 		    	  moleculeCMLString = "";
 		    	  isMolecule = false;
 		      }
 		      if(event.asEndElement().getName().getLocalPart().equals(MOLECULELIST))
 		    	  isMoleculeList = false;
 		      
 		    	  
 		      
 		      break; 
 		    case XMLStreamConstants.ATTRIBUTE: 
 		      break; 
 		    default : 
 		      break; 
 		  } 
 		}
 		
 		
 		FragmentList fragList = new FragmentList();
 
 		//now read in the saved cml xml
 		for (String molString : moleculeListString) {
 			InputStream bais = new ByteArrayInputStream(molString.getBytes());
 			CMLReader reader = new CMLReader(bais);
 			IChemFile chemFile = new ChemFile();
 	        chemFile = (IChemFile) reader.read(chemFile);
 	        IAtomContainer container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
 	        
 	        //TODO: properly find reactant
 	        if(container.getID().startsWith("m"))
 	        	fragList.addStructure(container);
 	        else
 		        //those molecules already have a id
 		        fragList.addFragment(container);
 		}
 		
 		
 		//TODO: for now only one spectrum per file....completly rewrite the spectram data object and merge with mzML
 		SpectrumData specData = new SpectrumData(spectra.get(0));
 		MzAnnotate mzAnno = new MzAnnotate(specData, fragList);
 		
 		//now assign the links between the peaks and the fragments
 		for (Double peak : spectrumPeakToStructure.keySet()) {
 			try {
 				mzAnno.assignPeakToFragment(Tools.getPeak(specData, peak), spectrumPeakToStructure.get(peak));
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return mzAnno;
 	}
 
 }
