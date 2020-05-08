 
 package edu.common.dynamicextensions.util.parser;
 
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.DATE_RANGE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.DEFAULT_VALUE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.DISPLAY_LABLE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.FORM_DEFINITION;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.INSTANCE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.OPTIONS;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.OVERRIDE_PV;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.PERMISSIBLE_VALUES;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.PERMISSIBLE_VALUES_FILE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.RANGE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.RELATED_ATTIBUTE;
 import static edu.common.dynamicextensions.util.parser.CategoryCSVConstants.RULES;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import au.com.bytecode.opencsv.CSVReader;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.util.Constants;
 import edu.common.dynamicextensions.validation.category.CategoryValidator;
 
 /**
  * @author kunal_kamble
  */
 public class CategoryCSVFileParser extends CategoryFileParser
 {
 
 	protected CSVReader reader;
 
 	private String[] line;
 
 	protected long lineNumber = 0;
 
 	/**
 	 * @param filePath
 	 * @throws DynamicExtensionsSystemException
 	 * @throws FileNotFoundException 
 	 */
 	public CategoryCSVFileParser(String filePath) throws DynamicExtensionsSystemException, FileNotFoundException
 	{
 		super(filePath);
 		reader = new CSVReader(new FileReader(filePath));
 		categoryValidator = new CategoryValidator(this);
 	}
 
 	/**
 	 * @return current line number
 	 */
 	public long getLineNumber()
 	{
 		return lineNumber;
 	}
 
 	/**
 	 * This method reads the next line 
 	 * @param reader
 	 * @return
 	 * @throws IOException
 	 */
 	public boolean readNext() throws IOException
 	{
 		//To skip the blank lines
 		while ((line = reader.readNext()) != null)
 		{
 			lineNumber++;
			if (line[0].length() != 0 && !line[0].startsWith("##"))
 			{
 				break;
 			}
 		}
 
 		if (line == null)
 		{
 			return false;
 		}
 		return true;
 
 	}
 
 	/**
 	 * @return current line
 	 */
 	public String[] readLine()
 	{
 		return line;
 	}
 
 	/**
 	 * @return paths
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Map<String, List<String>> getPaths() throws DynamicExtensionsSystemException
 	{
 		Map<String, List<String>> entityNamePath = new LinkedHashMap<String, List<String>>();
 
 		for (String entityNameAndPath : readLine())
 		{
 			List<String> path = new ArrayList<String>();
 			String entityName = entityNameAndPath.split("~")[0];
 
 			StringTokenizer stringTokenizer = new StringTokenizer(entityNameAndPath.split("~")[1], ":");
 			while (stringTokenizer.hasMoreTokens())
 			{
 				path.add(stringTokenizer.nextToken());
 			}
 
 			entityNamePath.put(entityName, path);
 		}
 		return entityNamePath;
 	}
 
 	/**
 	 * @return display label for the container
 	 */
 	public String getDisplyLable()
 	{
 		return readLine()[0].split(":")[1].trim();
 	}
 
 	/**
 	 * @return showCaption
 	 */
 	public boolean isShowCaption()
 	{
 		return new Boolean(readLine()[1].split("=")[1].trim());
 	}
 
 	public String[] getCategoryPaths()
 	{
 		readLine()[0] = readLine()[0].replace("instance:", "");
 		return readLine();
 	}
 
 	/**
 	 * @return entity name
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public String getEntityName() throws DynamicExtensionsSystemException
 	{
 		this.categoryValidator.validateEntityName(readLine()[0].split(":")[0].trim());
 		return readLine()[0].split(":")[0].trim();
 	}
 
 	/**
 	 * @return attribute name
 	 */
 	public String getAttributeName()
 	{
 		return readLine()[0].split(":")[1].trim();
 	}
 
 	/**
 	 * @return control type
 	 */
 	public String getControlType()
 	{
 		return readLine()[1].trim();
 	}
 
 	/**
 	 * @return control label
 	 */
 	public String getControlCaption()
 	{
 		return readLine()[2].trim();
 	}
 
 	/**
 	 * @return permissible values collection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Map<String, Collection<SemanticPropertyInterface>> getPermissibleValues() throws DynamicExtensionsSystemException
 	{
 		//counter for to locate the start of the permissible values
 		String[] nextLine = readLine();
 		int i;
 		boolean permissibleValuesPresent = false;
 		for (i = 0; i < nextLine.length; i++)
 		{
 			if (nextLine[i].toLowerCase().startsWith(PERMISSIBLE_VALUES.toLowerCase())
 					|| nextLine[i].toLowerCase().startsWith(PERMISSIBLE_VALUES_FILE.toLowerCase()))
 			{
 				permissibleValuesPresent = true;
 				break;
 			}
 		}
 		if (!permissibleValuesPresent)
 		{
 			return null;
 		}
 
 		Map<String, Collection<SemanticPropertyInterface>> pvVsSemanticPropertyCollection = new HashMap<String, Collection<SemanticPropertyInterface>>();
 
 		int indexOfTilda = nextLine[i].indexOf("~");
 		String permissibleValueKey = nextLine[i].substring(0, indexOfTilda);
 
 		if (PERMISSIBLE_VALUES.equalsIgnoreCase(permissibleValueKey))
 		{
 			String pvString = nextLine[i].substring(indexOfTilda + 1);
 			String originalPVString = pvString;
 			int pvStringLength = 1;
 			while (pvStringLength <= originalPVString.trim().length())
 			{
 				int indexOFColon = pvString.indexOf(":");
 				int indexOfConceptCodeStart = pvString.indexOf("<");
 				if (indexOFColon < indexOfConceptCodeStart || (indexOFColon == -1 && indexOfConceptCodeStart == -1))
 				{
 					indexOfConceptCodeStart = -1;
 				}
 				String permiValue = "";
 				Collection<SemanticPropertyInterface> semanticPropertyCollection = new HashSet<SemanticPropertyInterface>();
 
 				if (indexOfConceptCodeStart != -1)
 				{//Concept code is present
 					permiValue = pvString.substring(0, indexOfConceptCodeStart);
 					pvStringLength = pvStringLength + permiValue.length();
 
 					int conceptCodeEnd = pvString.indexOf(">");
 					if (pvString.charAt(conceptCodeEnd + 1) == ':')
 					{
 						pvStringLength = pvStringLength + 1;
 					}
 					String tempCodesString = pvString.substring(indexOfConceptCodeStart + 1, conceptCodeEnd);
 					pvStringLength = pvStringLength + 2;
 
 					String[] conceptString = tempCodesString.split(":");
 					pvStringLength = pvStringLength + conceptString.length - 1;
 					for (String conceptAttrString : conceptString)
 					{//All concept codes for the pv
 						int seqNo = 1;
 						SemanticPropertyInterface semanticProperty = DomainObjectFactory.getInstance().createSemanticProperty();
 						String[] conceptAttributes = conceptAttrString.split("#");
 						pvStringLength = pvStringLength + conceptAttributes.length - 1;
 						for (String conceptAttr : conceptAttributes)
 						{
 							String[] conceptCodeKeyValue = conceptAttr.split("~");
 							populateSemanticProperty(semanticProperty, conceptCodeKeyValue[0], conceptCodeKeyValue[1]);
 							pvStringLength = pvStringLength + conceptCodeKeyValue[0].length() + conceptCodeKeyValue[1].length() + 1;
 						}
 						semanticProperty.setSequenceNumber(seqNo);
 						seqNo++;
 						semanticPropertyCollection.add(semanticProperty);
 					}
 				}
 				else
 				{//Concept Code not defined
 					int indexOfColon = pvString.indexOf(":");
 					if (indexOfColon != -1)
 					{
 						permiValue = pvString.substring(0, indexOfColon);
 						pvStringLength = pvStringLength + permiValue.length() + 1;
 					}
 					else
 					{
 						permiValue = pvString.substring(0);
 						pvStringLength = pvStringLength + permiValue.length();
 					}
 				}
 				pvVsSemanticPropertyCollection.put(permiValue, semanticPropertyCollection);
 				pvString = originalPVString.substring(pvStringLength - 1);
 			}
 		}
 
 		else if (PERMISSIBLE_VALUES_FILE.equalsIgnoreCase(permissibleValueKey))
 		{//PV from File
 			String filePath = getSystemIndependantFilePath(nextLine[i].substring(indexOfTilda + 1));
 			try
 			{
 				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
 				String line = null;
 				while ((line = reader.readLine()) != null)
 				{
 					if (line.trim().length() != 0)//skip the line if it is blank 
 					{
 						Collection<SemanticPropertyInterface> semanticPropertyCollection = new HashSet<SemanticPropertyInterface>();
 						String pvString = line.trim();
 						int indexOfConceptCodeStart = pvString.indexOf("<");
 						int conceptCodeEnd = pvString.indexOf(">");
 						String pv = "";
 						if (indexOfConceptCodeStart != -1 && conceptCodeEnd != -1)
 						{
 							pv = pvString.substring(0, indexOfConceptCodeStart);
 							String tempCodesString = pvString.substring(indexOfConceptCodeStart + 1, conceptCodeEnd);
 							String[] conceptString = tempCodesString.split(":");
 							for (String conceptAttrString : conceptString)
 							{//All concept codes for the pv
 								int seqNo = 1;
 								SemanticPropertyInterface semanticProperty = DomainObjectFactory.getInstance().createSemanticProperty();
 								String[] conceptAttributes = conceptAttrString.split("#");
 								for (String conceptAttr : conceptAttributes)
 								{
 									String[] conceptCodeKeyValue = conceptAttr.split("~");
 									populateSemanticProperty(semanticProperty, conceptCodeKeyValue[0], conceptCodeKeyValue[1]);
 								}
 								semanticProperty.setSequenceNumber(seqNo);
 								seqNo++;
 								semanticPropertyCollection.add(semanticProperty);
 							}
 						}
 						else
 						{
 							pv = pvString;
 						}
 						pvVsSemanticPropertyCollection.put(pv, semanticPropertyCollection);
 					}
 				}
 			}
 			catch (FileNotFoundException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while reading permissible values file " + filePath, e);
 			}
 			catch (IOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while reading permissible values file " + filePath, e);
 			}
 
 		}
 
 		return pvVsSemanticPropertyCollection;
 	}
 
 	/**
 	 * @param semanticProperty
 	 * @param key
 	 * @param value
 	 */
 	private void populateSemanticProperty(SemanticPropertyInterface semanticProperty, String key, String value)
 	{
 		if (key.equalsIgnoreCase(Constants.CONCEPT_CODE))
 		{
 			semanticProperty.setConceptCode(value);
 		}
 		else if (key.equalsIgnoreCase(Constants.CONCEPT_DEFINITION))
 		{
 			semanticProperty.setConceptDefinition(value);
 		}
 		else if (key.equalsIgnoreCase(Constants.PREFERRED_NAME))
 		{
 			semanticProperty.setThesaurasName(value);
 		}
 		else if (key.equalsIgnoreCase(Constants.DEFINITION_SOURCE))
 		{
 			semanticProperty.setTerm(value);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean hasDisplayLable()
 	{
 		if (readLine()[0].trim().toLowerCase().startsWith(DISPLAY_LABLE.toLowerCase()))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean hasFormDefination()
 	{
 		if (FORM_DEFINITION.equalsIgnoreCase(readLine()[0].trim()))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getCategoryName()
 	{
 		return readLine()[0].trim();
 	}
 
 	/**
 	 * @return
 	 */
 	public String getEntityGroupName()
 	{
 		return readLine()[0].trim();
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean hasSubcategory()
 	{
 		if (readLine()[0].toLowerCase().contains("subcategory:"))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getTargetContainerCaption()
 	{
 		return readLine()[0].split(":")[1].trim();
 	}
 
 	/**
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getMultiplicity() throws DynamicExtensionsSystemException
 	{
 		this.categoryValidator.validateMultiplicity();
 		return readLine()[0].split(":")[2].trim();
 	}
 
 	/**
 	 * @return
 	 */
 	public Map<String, String> getControlOptions()
 	{
 		Map<String, String> controlOptions = new HashMap<String, String>();
 		for (String string : readLine())
 		{
 			if (string.toLowerCase().startsWith(OPTIONS.toLowerCase() + "~"))
 			{
 				String[] controlOptionsValue = string.split("~")[1].split(":");
 
 				for (String optionValue : controlOptionsValue)
 				{
 					controlOptions.put(optionValue.split("=")[0], optionValue.split("=")[1]);
 				}
 
 			}
 		}
 
 		return controlOptions;
 	}
 
 	/**
 	 * @return
 	 */
 	public Map<String, Object> getRules()
 	{
 		Map<String, Object> rules = new HashMap<String, Object>();
 
 		for (String string : readLine())
 		{
 			if (string.toLowerCase().startsWith(RULES.toLowerCase() + "~"))
 			{
 				String[] rulesValues = string.split("~")[1].split(":");
 
 				for (String ruleValue : rulesValues)
 				{
 					if (ruleValue.toLowerCase().startsWith(RANGE.toLowerCase()))
 					{
 						String[] rangeValues = ruleValue.split("-");
 						for (String rangeValue : rangeValues)
 						{
 							if (!(rangeValue.toLowerCase().startsWith(RANGE.toLowerCase())))
 							{
 								String[] minMaxValues = rangeValue.split("&");
 								boolean isDateRange = false;
 								Map<String, Object> valuesMap = new HashMap<String, Object>();
 								for (String value : minMaxValues)
 								{
 									if (value.split("=")[1].contains("/"))
 									{
 										valuesMap.put(value.split("=")[0], value.split("=")[1].replace("/", "-"));
 										isDateRange = true;
 									}
 									else
 									{
 										valuesMap.put(value.split("=")[0], value.split("=")[1]);
 									}
 								}
 
 								if (isDateRange)
 								{
 									rules.put(DATE_RANGE, valuesMap);
 								}
 								else
 								{
 									rules.put(RANGE.toLowerCase(), valuesMap);
 								}
 							}
 						}
 					}
 					else
 					{
 						rules.put(ruleValue.split("=")[0], null);
 					}
 				}
 			}
 		}
 
 		return rules;
 	}
 
 	/**
 	 * @return showCaption
 	 * @throws IOException 
 	 */
 	public boolean isOverridePermissibleValues() throws IOException
 	{
 		if (OVERRIDE_PV.equals(readLine()[0].split("=")[0].trim()))
 		{
 			String string = readLine()[0].split("=")[1].trim();
 			this.readNext();
 			return new Boolean(string);
 
 		}
 		return false;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.util.parser.CategoryFileParser#hasRelatedAttributes()
 	 */
 	public boolean hasRelatedAttributes()
 	{
 		if (readLine()[0].trim().toLowerCase().startsWith(RELATED_ATTIBUTE.toLowerCase()))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.util.parser.CategoryFileParser#hasInsatanceInformation()
 	 */
 	public boolean hasInsatanceInformation()
 	{
 		if (readLine() != null && readLine()[0].trim().toLowerCase().startsWith(INSTANCE.toLowerCase()))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.util.parser.CategoryFileParser#getDefaultValueForRelatedAttribute()
 	 */
 	public String getDefaultValueForRelatedAttribute()
 	{
 		return readLine()[0].split("=")[1].trim();
 	}
 
 	public String getRelatedAttributeName()
 	{
 		return readLine()[0].split("=")[0].split(":")[1].trim();
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.util.parser.CategoryFileParser#getDefaultValue()
 	 */
 	public String getDefaultValue()
 	{
 		String defaultValue = null;
 		for (String string : readLine())
 		{
 			if (string.startsWith(DEFAULT_VALUE))
 			{
 				defaultValue = string.split("=")[1];
 			}
 		}
 		return defaultValue;
 	}
 }
