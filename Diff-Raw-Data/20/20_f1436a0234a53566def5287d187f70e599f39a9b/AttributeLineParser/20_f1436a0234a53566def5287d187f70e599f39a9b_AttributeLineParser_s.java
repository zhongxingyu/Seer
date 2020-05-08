 package cytoscape.data.readers;
 
 import giny.model.Edge;
 import giny.model.Node;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.readers.TextTableReader.ObjectType;
 
 /**
  * Take a line of data, analyze it, and map to CyAttributes.
  * 
  * @since Cytoscape 2.4
  * @version 0.9
  * @author Keiichiro Ono
  * 
  */
 public class AttributeLineParser {
 
 	private AttributeMappingParameters mapping;
 
 	public AttributeLineParser(AttributeMappingParameters mapping) {
 		this.mapping = mapping;
 	}
 
 	/**
 	 * Takes an array of entries, which is one line of text file, and maps them
 	 * to CyAttributes.
 	 * 
 	 * @param parts
 	 */
 	public void parseEntry(String[] parts) {
 		
		System.out.println(mapping.getObjectType().toString() + " <--- ####### Net: ");
		
 		/*
 		 * Split the line and extract values
 		 */
 		final String primaryKey = parts[mapping.getKeyIndex()].trim();
 
 		/*
 		 * Set aliases In this case, "aliases" means alias entries in the TEXT
 		 * TABLE, not the ones returned by Cytoscape.getNodeAliases()
 		 * 
 		 * The variable aliasSet has non-redundant set of object names.
 		 */
 		final Set<String> aliasSet = new TreeSet<String>();
 		if (mapping.getAliasIndexList().size() != 0) {
 			/*
 			 * Alias column exists. Extract those keys.
 			 */
 			String aliasCell = null;
 			for (int aliasIndex : mapping.getAliasIndexList()) {
 
 				if (parts.length > aliasIndex) {
 					aliasCell = parts[aliasIndex];
 					if (aliasCell != null && aliasCell.trim().length() != 0) {
 						aliasSet.addAll(buildList(aliasCell));
 					}
 				}
 			}
 		}
 
 		aliasSet.add(primaryKey);
 
 		/*
 		 * Case 1: use node ID as the key
 		 */
 		if (mapping.getMappingAttribute().equals(mapping.ID)) {
 			transfer2cyattributes(primaryKey, aliasSet, parts);
 		} else {
 			/*
 			 * Case 2: use an attribute as the key.
 			 */
 
 			List<String> objectIDs = null;
 			for (String id : aliasSet) {
 				if (mapping.getAttributeToIDMap().containsKey(id)) {
 					objectIDs = mapping.toID(id);
 
 					for (String objectID : objectIDs) {
 						mapping.getAlias().add(objectID,
 								new ArrayList<String>(aliasSet));
 					}
 					break;
 				}
 			}
 			if (objectIDs != null) {
 				for (String key : objectIDs) {
 					transfer2cyattributes(key, aliasSet, parts);
 				}
 			}
 		}
 
 	}
 
 	private void transfer2cyattributes(String primaryKey, Set<String> aliasSet,
 			String[] parts) {
 
 		String altKey = null;
 		String targetNetworkID = null;
 		/*
 		 * Search the key
 		 */
 		switch (mapping.getObjectType()) {
 		case NODE:
 
 			Node node = Cytoscape.getCyNode(primaryKey);
 			if (node == null) {
 				for (String alias : aliasSet) {
 					node = Cytoscape.getCyNode(alias);
 					if (node != null) {
 						altKey = alias;
 						break;
 					}
 				}
 				if (node == null) {
 					return;
 				}
 			} else {
 				break;
 			}
 
 			break;
 		case EDGE:
 			Edge edge = Cytoscape.getRootGraph().getEdge(primaryKey);
 			if (edge == null) {
 				for (String alias : aliasSet) {
 					edge = Cytoscape.getRootGraph().getEdge(alias);
 					if (edge != null) {
 						altKey = alias;
 						break;
 					}
 				}
 				if (edge == null) {
 					return;
 				}
 			} else {
 				break;
 			}
 
 			break;
 		case NETWORK:
 			/*
 			 * This is a special case: Since network IDs are only integers and
 			 * not always the same, we need to use title instead of ID.
 			 */
 			
 			System.out.println("####### Net: " + primaryKey);
 			
 
 			
 			if (mapping.getnetworkTitleMap().containsKey(primaryKey)) {
 				targetNetworkID = mapping.getnetworkTitleMap().get(primaryKey);
 				System.out.println("Found! " + targetNetworkID);
 				break;
 			}
 
 			if (targetNetworkID == null) {
 				for (String alias : aliasSet) {
 					if (mapping.getnetworkTitleMap().containsKey(alias)) {
 						targetNetworkID = mapping.getnetworkTitleMap().get(alias);
 						break;
 					}
 				}
 			}
 			if (targetNetworkID == null) {
 				/*
 				 * Network not found: just ignore this line.
 				 */
 				return;
 			}
 			break;
 		default:
 		}
 
 		/*
 		 * Now, transfer entries into CyAttributes.
 		 */
 		for (int i = 0; i < parts.length; i++) {
 			if (i != mapping.getKeyIndex()
 					&& !mapping.getAliasIndexList().contains(i)
 					&& mapping.getImportFlag()[i]) {
 				if (parts[i] == null) {
 					// Do nothing
 				} else if(mapping.getObjectType() == ObjectType.NETWORK) {
 					mapAttribute(targetNetworkID, parts[i].trim(), i);
 				}
 				/*
 				 * Frist, check the node exists or not with the primary key
 				 */
 				else if (altKey == null) {
 					mapAttribute(primaryKey, parts[i].trim(), i);
 				} else {
 					mapAttribute(altKey, parts[i].trim(), i);
 				}
 			}
 		}
 
 		/*
 		 * Finally, add aliases
 		 */
 		if (altKey == null) {
 			mapping.getAlias().add(primaryKey, new ArrayList<String>(aliasSet));
 		} else {
 			mapping.getAlias().add(altKey, new ArrayList<String>(aliasSet));
 		}
 	}
 
 	/**
 	 * Based on the attribute types, map the entry to CyAttributes.<br>
 	 * 
 	 * @param key
 	 * @param entry
 	 * @param index
 	 */
 	private void mapAttribute(final String key, final String entry,
 			final int index) {
 		
 		Byte type = mapping.getAttributeTypes()[index];
 
 		switch (type) {
 		case CyAttributes.TYPE_BOOLEAN:
 			mapping.getAttributes().setAttribute(key,
 					mapping.getAttributeNames()[index], new Boolean(entry));
 			break;
 		case CyAttributes.TYPE_INTEGER:
 			mapping.getAttributes().setAttribute(key,
 					mapping.getAttributeNames()[index], new Integer(entry));
 			break;
 		case CyAttributes.TYPE_FLOATING:
 			mapping.getAttributes().setAttribute(key,
 					mapping.getAttributeNames()[index], new Double(entry));
 			break;
 		case CyAttributes.TYPE_STRING:
 			mapping.getAttributes().setAttribute(key,
 					mapping.getAttributeNames()[index], entry);
 			break;
 		case CyAttributes.TYPE_SIMPLE_LIST:
 			/*
 			 * In case of list, not overwrite the attribute. Get the existing
 			 * list, and add it to the list.
 			 */
 			List curList = mapping.getAttributes().getAttributeList(key,
 					mapping.getAttributeNames()[index]);
 			if (curList == null) {
 				curList = new ArrayList();
 			}
 			curList.addAll(buildList(entry));
 			mapping.getAttributes().setAttributeList(key,
 					mapping.getAttributeNames()[index], curList);
 			break;
 		default:
 			mapping.getAttributes().setAttribute(key,
 					mapping.getAttributeNames()[index], entry);
 		}
 	}
 
 	/**
 	 * If an entry is a list, split the string and create new List Attribute.
 	 * 
 	 * @return
 	 */
 	private List buildList(final String entry) {
 
 		if (entry == null) {
 			return null;
 		}
 
 		final List<String> listAttr = new ArrayList<String>();
 
 		final String[] parts = (entry.replace("\"", "")).split(mapping
 				.getListDelimiter());
 		for (String listItem : parts) {
 			listAttr.add(listItem.trim());
 		}
 		return listAttr;
 	}
 
 }
