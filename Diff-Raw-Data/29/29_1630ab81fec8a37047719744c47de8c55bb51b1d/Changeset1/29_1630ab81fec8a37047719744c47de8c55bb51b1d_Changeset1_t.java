 import java.io.*;
 import java.net.*;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 // See also https://github.com/SomeoneElseOSM/Changeset1 for more details
 
 public class Changeset1 
 {
 	static final String live_api_path = "http://openstreetmap.org/api/0.6/";
 	static final String dev_api_path = "http://api06.dev.openstreetmap.org/api/0.6/";
   
 	final static int Log_Debug_Off = 0;			// Used to turn debug off
 	final static int Log_Serious = 1;			// A serious error has occurred
 	final static int Log_Error = 2;				// An error that we can work around has occurred
 	final static int Log_Warning = 3;			// Not currently used
 	final static int Log_Return = 4; 			// Return values from top-level subroutines
 	final static int Log_Informational_1 = 5;	// Important informational stuff
 	final static int Log_Top_Routine_Start = 6;	// top-level routine start code
 	final static int Log_Low_Routine_Start = 7; // low-level routing start code
 	final static int Log_Informational_2 = 8;	// Any other informational stuff
 
 /* ------------------------------------------------------------------------------
  * Parameters that require a value end in "="; parameters that don't, don't.
  * ------------------------------------------------------------------------------ */
 	final static String param_help = "-help";
 	final static String param_input = "-input=";
 	final static String param_output = "-output=";
 	final static String param_display_name = "-display_name=";
 	final static String param_uid = "-user=";
 	final static String param_id = "-id=";
 	final static String param_time = "-time=";
 	final static String param_dev = "-dev";
 	final static String param_debug = "-debug=";
 	final static String param_bbox = "-bbox=";		// Not passed to the API; we compare changesets in that bounding box
 	final static String param_download_changeset = "-download_changeset=";
 	final static String param_download_nodes = "-download_nodes=";
 	final static String param_building = "-building=";
 	final static String param_overlapnodes = "-report_overlap_nodes=";
 	
 	final static int Overlap_All   = 1;
 	final static int Overlap_Yes   = 2;
 	final static int Overlap_No    = 3;
 	final static int Overlap_Error = 4;
 	
 	static String api_path = live_api_path;		// Set to dev_api_path for testing via -dev
 	static boolean arg_help = false;			// Set to show that -help has been passed on the command line
 	static String arg_in_file = "";				// -input=
 	static String arg_out_file = "";			// -output=
 	static int arg_debug = 0;					// -debug=
 	static String arg_bbox = "";				// -bbox=
 	static String arg_download_changeset = "0";			// -download_changeset=
 	static String arg_download_nodes = "0";			// -download_nodes=
 /* ------------------------------------------------------------------------------
  * This value is used to compare the number of ways in buildings (and shops) 
  * against.  The default is 2001 (more than the maximum number of nodes in a way)
  * so by default we won't flag any potentially "created by mistake" buildings.
  * 
  * The source of these erroneous buildings is iD issue 542.  New mappers click in
  * a landuse area and set the details at the left, and then save, not realising
  * that they have changed the landuse to a building.
  * ------------------------------------------------------------------------------ */
 	static String arg_building = "2001";		// -building
 	
 	static boolean arg_overlapnodes = true;		// -report_overlap_nodes
 	
 	static String arg_min_lat_string = "";
 	static String arg_min_lon_string = "";
 	static String arg_max_lat_string = "";
 	static String arg_max_lon_string = "";
 
 	static FileReader myFileReader;
 	static BufferedReader myBufferedReader;
 	static OutputStream myOutputStream;
 	static PrintStream myPrintStream;
 	
 	static boolean check_lon_overlap( Double arg_min_lon, Double arg_max_lon,
 			                          Double min_lon,     Double max_lon )
 	{
 		boolean return_value = false;
 		
 		if ( arg_min_lon < min_lon )
 		{
 			if ( arg_max_lon > min_lon )
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "In 1: " + arg_min_lon + " " + arg_max_lon + " " + min_lon + " " + max_lon);
 				}
 
 				return_value = true;
 			}
 			else
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Out because arg lon is too small: " + arg_min_lon + " " + arg_max_lon + " " + min_lon + " " + max_lon );
 				}
 
 				return_value = false;
 			}
 		}
 		else
 		{
 			if ( arg_min_lon < max_lon )
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "In 2: " + arg_min_lon + " " + arg_max_lon + " " + min_lon + " " + max_lon);
 				}
 
 				return_value = true;
 			}
 			else
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Out because arg lon is too big: " + arg_min_lon + " " + arg_max_lon + " " + min_lon + " " + max_lon  );
 				}
 
 				return_value = false;
 			}
 		}
 
 		return return_value;
 	}
 	
 	
 	/**
 	 * check_bboxes_overlap
 	 * 
 	 * Checks whether two bounding boxes overlap.  Typically the first bounding box is what we're checking 
 	 * against (what the user specified that they were interested in) and the second is the bounding box
 	 * for the changeset that we are currently processing.
 	 * 
 	 * @param arg_min_lon  The first bounding box
 	 * @param arg_min_lat
 	 * @param arg_max_lat
 	 * @param arg_max_lon
 	 * 
 	 * @param min_lon The second bounding box
 	 * @param min_lat
 	 * @param max_lat
 	 * @param max_lon
 	 * 
 	 * @return true if the bounding boxes overlap.
 	 */
 	static boolean check_bboxes_overlap( Double arg_min_lon, Double arg_min_lat, Double arg_max_lat, Double arg_max_lon,
 			                             Double min_lon,     Double min_lat,     Double max_lat,     Double max_lon )
 	{
 		boolean return_value = false;
 		
 		if ( arg_min_lat < min_lat )
 		{
 			if ( arg_max_lat > min_lat )
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Probably in 1; look at longitude: " + arg_min_lat + " " + arg_max_lat + " " + min_lat + " " + max_lat);
 				}
 
 				return_value = check_lon_overlap( arg_min_lon, arg_max_lon, min_lon, max_lon );
 			}
 			else
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Out because arg lat is too small: " + arg_min_lat + " " + arg_max_lat + " " + min_lat + " " + max_lat );
 				}
 
 				return_value = false;
 			}
 		}
 		else
 		{
 			if ( arg_min_lat < max_lat )
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Probably in 2; look at longitude: " + arg_min_lat + " " + arg_max_lat + " " + min_lat + " " + max_lat );
 				}
 
 				return_value = check_lon_overlap( arg_min_lon, arg_max_lon, min_lon, max_lon );
 			}
 			else
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Out because arg lat is too big: " + arg_min_lat + " " + arg_max_lat + " " + min_lat + " " + max_lat  );
 				}
 
 				return_value = false;
 			}
 		}
 		
 		return return_value;
 	}
 	
 
 
 	private static int check_bbox_interest( Node root_node, NamedNodeMap item_attributes, Node id_node, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string )
 	{
 		boolean it_overlaps = true;
 		int return_value = 0;
 		Double p_min_lat_d;
 		Double p_min_lon_d;
 		Double p_max_lat_d;
 		Double p_max_lon_d;
 		
 		if ( passed_min_lat_string.length() == 0 )
 		{
 			if ( arg_debug >= Log_Informational_1 )
 			{
 				System.out.println( "We're interested in all changesets" );
 			}
 
 			return_value = Overlap_All;
 		}
 		else
 		{
 			Double min_lat_d;
 			Double min_lon_d;
 			Double max_lat_d;
 			Double max_lon_d;
 			
 			try
 			{
 				min_lat_d = Double.valueOf( item_attributes.getNamedItem( "min_lat" ).getNodeValue() );
 				min_lon_d = Double.valueOf( item_attributes.getNamedItem( "min_lon" ).getNodeValue() );
 				max_lat_d = Double.valueOf( item_attributes.getNamedItem( "max_lat" ).getNodeValue() );
 				max_lon_d = Double.valueOf( item_attributes.getNamedItem( "max_lon" ).getNodeValue() );
 				
 				p_min_lat_d = Double.valueOf( passed_min_lat_string ); 
 				p_min_lon_d = Double.valueOf( passed_min_lon_string ); 
 				p_max_lat_d = Double.valueOf( passed_max_lat_string ); 
 				p_max_lon_d = Double.valueOf( passed_max_lon_string ); 
 
 				it_overlaps = check_bboxes_overlap( p_min_lon_d, p_min_lat_d, p_max_lat_d, p_max_lon_d,
 			                                 		min_lon_d, min_lat_d, max_lat_d, max_lon_d );
 
 				if ( it_overlaps == true )
 				{
 					return_value = Overlap_Yes;
 
 					if ( arg_debug >= Log_Informational_1 )
 					{
 						System.out.println( "We're interested in this changeset" );
 					}
 				}
 				else
 				{
 					return_value = Overlap_No;
 
 					if ( arg_debug >= Log_Informational_1 )
 					{
 						System.out.println( "We're not interested in this changeset" );
 					}
 				}
 			}
 			catch( Exception ex )
 			{
 				return_value = Overlap_Error;
 				
 				if ( arg_debug >= Log_Informational_1 )
 				{
 					System.out.println( "Error parsing lat or lon from this changeset, so we'll assume that we are interested in it.  Error: " + ex.getMessage() );
 					
 /* ------------------------------------------------------------------------------
  * This can happen if a changeset has no nodes inside it (since we're checking
  * node positions, not the changeset bounding box, if we choose to download the
  * changeset).  
  * ------------------------------------------------------------------------------ */
 				}
 			}
 		}
 		
 		return return_value;
 	}
 
 
 	/**
 	 * process_download_xml
 	 * 
 	 * @param root_node  The XML node passed in here is the root node of the XML tree of the download of this changeset
 	 * @param passed_changeset_number
 	 * 
 	 * @param passed_min_lat_string  The bounding box that we're interested in - node positions will be checked against this box.
 	 * @param passed_min_lon_string
 	 * @param passed_max_lat_string
 	 * @param passed_max_lon_string  The "number of nodes in a shop/building" value to worry about
 	 * 
 	 * @param passed_building  The "number of nodes in a shop/building" value to worry about
 	 * 
 	 * @return  returns "true" if there are nodes in the changeset that overlaps the bounding box that we passed in.
 	 */
 	private static boolean process_downloaded_changeset_xml( Node root_node, String passed_changeset_number, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_building, boolean passed_overlapnodes, String passed_download_nodes )
 	{
 		boolean changeset_nodes_overlap = false;
 		
 /* ------------------------------------------------------------------------------
  * For an example of the sort of XML that may be being processed here look at 
  * any changeset osmChange XML, such as
  * http://api06.dev.openstreetmap.org/api/0.6/changeset/37228/download
  * ------------------------------------------------------------------------------ */
 		if ( root_node.getNodeType() == Node.ELEMENT_NODE ) 
 		{
 			NodeList level_1_xmlnodes = root_node.getChildNodes();
 			int num_l1_xmlnodes = level_1_xmlnodes.getLength();
 	
 			if ( arg_debug >= Log_Informational_2 )
 			{
 				System.out.println( "Changeset L1 nodes found: " + num_l1_xmlnodes );
 			}
 	
 /* ------------------------------------------------------------------------------------------------------------
  * Create an object in which to store details of our current node, way or relation.
  * ------------------------------------------------------------------------------------------------------------ */
 			OsmObjectList osmObjectList = new OsmObjectList();
 							
 /* ------------------------------------------------------------------------------------------------------------
  * Iterate through the level 1 nodes
  * ------------------------------------------------------------------------------------------------------------ */
 			for ( int cntr_1 = 0; cntr_1 < num_l1_xmlnodes; ++cntr_1 ) 
 			{
 				Node this_l1_item = level_1_xmlnodes.item( cntr_1 );
 				String l1_item_type = this_l1_item.getNodeName();
 
 /* ------------------------------------------------------------------------------------------------------------
  * We're expecting "create", "modify" or "delete" here
  * ------------------------------------------------------------------------------------------------------------ */
 				if ( !l1_item_type.equals( "#text" ))
 				{
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "Found 1: " + l1_item_type );
 					}
 
 					NodeList level_2_xmlnodes = this_l1_item.getChildNodes();
 					int num_l2_xmlnodes = level_2_xmlnodes.getLength();
 	
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "Changeset L2 nodes found: " + num_l2_xmlnodes );
 					}
 	                    
 					for ( int cntr_2 = 0; cntr_2 < num_l2_xmlnodes; ++cntr_2 ) 
 					{
 						Node this_l2_item = level_2_xmlnodes.item( cntr_2 );
 						String l2_item_type = this_l2_item.getNodeName();
 /* ------------------------------------------------------------------------------------------------------------
  * l2_item_type == "the current level 2 thing that we're dealing with" - expected to be a node, way or relation.
  * ------------------------------------------------------------------------------------------------------------ */
 
 /* ------------------------------------------------------------------------------------------------------------
  * We're expecting "node", "way" or "relation" here
  * ------------------------------------------------------------------------------------------------------------ */
 						if ( !l2_item_type.equals( "#text" ))
 						{
 							if ( arg_debug >= Log_Informational_2 )
 							{
 								System.out.println( "Found 2: " + l2_item_type );
 							}
 
 /* ------------------------------------------------------------------------------------------------------------
  * Create an object in which to store details of our current node, way or relation.
  * ------------------------------------------------------------------------------------------------------------ */
 							OsmObjectInfo osmObjectInfo = new OsmObjectInfo();
 							
 							if ( l1_item_type.equals( "create" ))
 							{
 								osmObjectInfo.set_last_action( OsmObjectDetails.Action_Create );
 							} 
 							else if ( l1_item_type.equals( "modify" ))
 							{
 								osmObjectInfo.set_last_action( OsmObjectDetails.Action_Modify );
 							} 
 							else if ( l1_item_type.equals( "delete" ))
 							{
 								osmObjectInfo.set_last_action( OsmObjectDetails.Action_Delete );
 							}
 							
 /* ------------------------------------------------------------------------------------------------------------
  * Look at attributes first.  These vary depending on whether we've got a node or a way or relation.
  * ------------------------------------------------------------------------------------------------------------ */
 							if ( l2_item_type.equals( "node" ))
 							{
 								osmObjectInfo.set_item_type( OsmObjectKey.Item_Node );
 								
 								osmObjectInfo.process_downloaded_changeset_node_attributes( passed_min_lat_string, passed_min_lon_string, 
 										passed_max_lat_string, passed_max_lon_string, 
 										this_l2_item, arg_debug, passed_overlapnodes, changeset_nodes_overlap, passed_download_nodes, api_path );
 								
 							} //node
 							else
 							{
 								if ( l2_item_type.equals( "way" ))
 								{
 									osmObjectInfo.set_item_type( OsmObjectKey.Item_Way );
 									
 									osmObjectInfo.process_downloaded_changeset_wayrelation_attributes( this_l2_item,  
 											l1_item_type, passed_changeset_number, 
 											arg_debug, arg_out_file, myPrintStream );
 								}
 								else
 								{
 									if ( l2_item_type.equals( "relation" ))
 									{
 										osmObjectInfo.set_item_type( OsmObjectKey.Item_Relation );
 										
 										osmObjectInfo.process_downloaded_changeset_wayrelation_attributes( this_l2_item,  
 												l1_item_type, passed_changeset_number, 
 												arg_debug, arg_out_file, myPrintStream );
 									} // relation
 									else
 									{
 										System.out.println( "Unexpected l2_item_type: " + l2_item_type );
 									}
 								} // !way
 							} //!node
 
 /* ------------------------------------------------------------------------------------------------------------
  * We've looked at the attributes.  Now let's look at the other tags
  * ------------------------------------------------------------------------------------------------------------ */
 							NodeList level_3_xmlnodes = this_l2_item.getChildNodes();
 							int num_l3_xmlnodes = level_3_xmlnodes.getLength();
 			
 							if ( arg_debug >= Log_Informational_2 )
 							{
 								System.out.println( "Changeset L3 nodes found: " + num_l3_xmlnodes );
 							}
 			                    
 							for ( int cntr_3 = 0; cntr_3 < num_l3_xmlnodes; ++cntr_3 ) 
 							{
 								Node this_l3_item = level_3_xmlnodes.item( cntr_3 );
 								String l3_item_type = this_l3_item.getNodeName();
 
 /* ------------------------------------------------------------------------------------------------------------
  * Depending on whether we're dealing with a "node", "way" or "relation", 
  * we're expecting "nd", "member" or "tag" here.
  * ------------------------------------------------------------------------------------------------------------ */
 								if ( !l3_item_type.equals( "#text" ))
 								{
 									if ( arg_debug >= Log_Informational_2 )
 									{
 										System.out.println( "Found 3: " + l3_item_type );
 									}
 									
 /* ------------------------------------------------------------------------------------------------------------
  * "nd" implies we're processing a way, so increment the counter for the number of nodes in the way.
  * ------------------------------------------------------------------------------------------------------------ */
 									if ( l3_item_type.equals( "nd" ))
 									{
 										boolean way_node_overlaps = false;
 										osmObjectInfo.inc_number_of_children();
 										
 /* ------------------------------------------------------------------------------------------------------------
  * We'd expect an "nd" to at least have the attribute "ref".
  * ------------------------------------------------------------------------------------------------------------ */
 										if ( this_l3_item.hasAttributes() )
 										{
 											NamedNodeMap item_attributes = this_l3_item.getAttributes();
 											Node ref_node = item_attributes.getNamedItem( "ref" );
 	
 											if ( ref_node == null )
 											{
 												System.out.println( "Download way member  processing: No nd ref found" );
 											}
 											else
 											{
 												if ( arg_debug >= Log_Informational_2 )
 												{
 													System.out.println( "nd ref: " + ref_node.getNodeValue() );
 												}
 
 /* ------------------------------------------------------------------------------
  * We're processing a node within a way.  If necessary we can download it and 
  * set "way_node_overlaps" and "overlaps_bbox" for the parent object based on  
  * whether it overlaps our bbox of interest.
  *
  * We try and avoid making the extra API call to download the node if we can.
  * ------------------------------------------------------------------------------ */
 												if ((  passed_download_nodes.equals( "1"   )) &&
 													( !passed_min_lat_string.equals( ""    )) &&
 													( !passed_min_lon_string.equals( ""    )) &&
 													( !passed_max_lat_string.equals( ""    )) &&
 													( !passed_max_lon_string.equals( ""    )) &&
 													( !osmObjectInfo.get_overlaps_bbox() || passed_overlapnodes )) 
 													{
 														OsmObjectInfo osm_wayrelation = new OsmObjectInfo();
 		
 														try
 														{
 															way_node_overlaps = osm_wayrelation.download_node( ref_node.getNodeValue(), 
 																	passed_min_lat_string, passed_min_lon_string, 
 																	passed_max_lat_string, passed_max_lon_string, 
 																	api_path, arg_debug );
 															
 															if ( way_node_overlaps )
 															{
 																osmObjectInfo.set_overlaps_bbox( true );
 															}
 														}
 														catch( Exception ex )
 														{
 															System.out.println( "Exception downloading node: " + ref_node.getNodeValue() + ", " + ex.getMessage() );
 														}
 													}
 
 // here would go some actual processing of the nd from the OSC file
 												
 											} // nd node not null
 										} // nd has attributes
 									} // nd
 									else
 									{ // !nd
 										if ( l3_item_type.equals( "tag" ))
 										{
 											osmObjectInfo.inc_number_of_tags();
 											
 											if ( this_l3_item.hasAttributes() )
 											{
 												NamedNodeMap item_attributes = this_l3_item.getAttributes();
 												Node key_node = item_attributes.getNamedItem( "k" );
 												Node value_node = item_attributes.getNamedItem( "v" );
 		
 												if ( key_node == null )
 												{
 													System.out.println( "Download tag/value processing: No key found" );
 /* ------------------------------------------------------------------------------------------------------------
  * If we haven't found a tag name don't bother looking for a value. 
  * ------------------------------------------------------------------------------------------------------------ */
 												}
 												else
 												{ // we have at least a key
 													if ( arg_debug >= Log_Informational_2 )
 													{
 														System.out.println( "tag: " + key_node.getNodeValue() );
 													}
 		
 /* ------------------------------------------------------------------------------------------------------------
  * We have found a tag name - check the value. 
  * ------------------------------------------------------------------------------------------------------------ */
 													if ( value_node == null )
 													{
 														System.out.println( "Download tag/value processing: No value found" );
 													}
 													else
 													{ // we have both a key and a value
 														if ( arg_debug >= Log_Informational_2 )
 														{
 															System.out.println( "value: " + value_node.getNodeValue() );
 														}
 		
 /* ------------------------------------------------------------------------------------------------------------
  * Store the value of the key "name" if it exists as we'll used it for reporting later. 
  * ------------------------------------------------------------------------------------------------------------ */
 														if ( key_node.getNodeValue().equals( "name" ))
 														{
 															osmObjectInfo.set_node_name( value_node.getNodeValue() );
 														}
 														else
 														{
 /* ------------------------------------------------------------------------------------------------------------
  * If we've got a key that a novice iD user might have turned a residential area into, store that fact - we'll
  * look at the number of nodes in at later.  Currently we're just looking for "building" or "shop", but that
  * might expand later.
  * ------------------------------------------------------------------------------------------------------------ */
 															if ( key_node.getNodeValue().equals( "building" ) || key_node.getNodeValue().equals( "shop" ))
 															{
 																osmObjectInfo.set_building_or_shop_found( true );
 															}
 															
 // here would go any other processing of the tag / value from the OSC file
 																													
 														}
 													} // both a key and a value
 												} // key node not null
 											} // tag has attributes
 										} // tag
 										else
 										{ // !nd and !tag
 /* ------------------------------------------------------------------------------------------------------------
  * Only relations have "member"s 
  * ------------------------------------------------------------------------------------------------------------ */
 											if ( l3_item_type.equals( "member" ))
 											{
 												osmObjectInfo.inc_number_of_children();
 												
 												if ( this_l3_item.hasAttributes() )
 												{
 													NamedNodeMap item_attributes = this_l3_item.getAttributes();
 													Node type_node = item_attributes.getNamedItem( "type" );
 													Node ref_node = item_attributes.getNamedItem( "ref" );
 													Node role_node = item_attributes.getNamedItem( "role" );
 			
 													if ( type_node == null )
 													{
 														System.out.println( "Download member processing: No type found" );
 /* ------------------------------------------------------------------------------------------------------------
  * If we haven't found a member type; don't bother looking for a ref 
  * ------------------------------------------------------------------------------------------------------------ */
 													}
 													else
 													{
 														if ( arg_debug >= Log_Informational_2 )
 														{
 															System.out.println( "type: " + type_node.getNodeValue() );
 														}
 			
 /* ------------------------------------------------------------------------------------------------------------
  * We have found a member type - check the ref 
  * ------------------------------------------------------------------------------------------------------------ */
 		
 														if ( ref_node == null )
 														{
 															System.out.println( "Download member processing: No ref found" );
 /* ------------------------------------------------------------------------------------------------------------
  * If we haven't found a member ref; don't bother looking for a role 
  * ------------------------------------------------------------------------------------------------------------ */
 														}
 														else
 														{
 															if ( arg_debug >= Log_Informational_2 )
 															{
 																System.out.println( "ref: " + ref_node.getNodeValue() );
 															}
 /* ------------------------------------------------------------------------------------------------------------
  * We have found a member ref - check the role 
  * ------------------------------------------------------------------------------------------------------------ */
 															if ( role_node == null )
 															{
 																System.out.println( "Download member processing: No role found" );
 															}
 															else
 															{
 																if ( arg_debug >= Log_Informational_2 )
 																{
 																	System.out.println( "role: " + role_node.getNodeValue() );
 																}
 																
 // here would go some actual processing of the type / ref / role  from the OSC file.  There's nothing here yet.
 															}
 														}
 													} // type node not null
 												} // member attributes
 											} // member
 											else
 											{
 												System.out.println( "Changeset other L3 item found: " + l3_item_type );
 											}
 										} // !tag
 									} // !nd
 								} // l3 item not #text
 							} // for level 3 nodes - something like "nd", "member" or "tag".
 
 /* ------------------------------------------------------------------------------------------------------------
  * At this point we've processed all the child XML nodes of the "node", "way" or "relation" that we're 
  * currently processing, including things like "nodes for a way" and "tags for an object".
  * 
  * If we've found a "create" for a new item, it won't exist in our List and we can just add it.  If we've
  * found a "modify" or "delete"; it might already exist.
  * ------------------------------------------------------------------------------------------------------------ */
 							if ( l1_item_type.equals( "create" ))
 							{
 								osmObjectList.add( osmObjectInfo.get_osmObjectKey(), osmObjectInfo.get_osmObjectDetails() );
 							}
 							else
 							{
 								osmObjectList.addOrUpdate( osmObjectInfo.get_osmObjectKey(), osmObjectInfo.get_osmObjectDetails() );
 							}
 							
 							if ( osmObjectInfo.get_overlaps_bbox() )
 							{
 								changeset_nodes_overlap = true;
 								
 								if (( arg_out_file != "" ) &&  passed_overlapnodes )
 								{
 									myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Node " + osmObjectInfo.get_item_id() + " (" + osmObjectInfo.get_node_name() + ") overlaps" );
 								}
 							}
 
 /* ------------------------------------------------------------------------------------------------------------
  * Report on relations without members etc.
  * Deleted relations are reported elsewhere - don't also report that they have no members. 
  * ------------------------------------------------------------------------------------------------------------ */
 
 							if ( osmObjectInfo.get_item_type() == OsmObjectKey.Item_Relation )
 							{
 								if ( arg_debug >= Log_Informational_2 )
 								{
 									System.out.println( "Members: " + osmObjectInfo.get_number_of_children() );
 								}
 
 								if ( osmObjectInfo.get_number_of_children() == 0 )
 								{
 									if (( arg_out_file != ""                ) &&
 										( !l1_item_type.equals( "delete"   )))
 									{
 										myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Relation " + osmObjectInfo.get_item_id()  + " has no members" );
 									}
 								}
 							}
 
 /* ------------------------------------------------------------------------------------------------------------
  * Deleted ways are reported elsewhere - don't also report that they have no members.
  * We do report on created or modified ways with no nodes, though.  We also report on single-node ways. 
  * ------------------------------------------------------------------------------------------------------------ */
 							if ( osmObjectInfo.get_item_type() == OsmObjectKey.Item_Way )
 							{
 								if ( arg_debug >= Log_Informational_2 )
 								{
 									System.out.println( "Nodes: " + osmObjectInfo.get_number_of_children() );
 								}
 								
 								if ( osmObjectInfo.get_number_of_children() == 0 )
 								{
 									if (( arg_out_file != ""                ) &&
 										( !l1_item_type.equals( "delete"   )))
 									{
 										myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Way " + osmObjectInfo.get_item_id()  + " has no nodes" );
 									}
 								}
 								
 								if ( osmObjectInfo.get_number_of_children() == 1 )
 								{
 									if ( arg_out_file != ""  )
 									{
 										myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Way " + osmObjectInfo.get_item_id()  + " has only 1 node" );
 									}
 								}
 							} // way
 
 /* ------------------------------------------------------------------------------------------------------------
  * Deleted items are expected to have no tags; likewise nodes probably won't.
  * Ways and Relations probably will, though (although this ignores ways that might be part of relations).
  * 
  * We report on ways and relations without tags so that they can be manually checked to see if they are part
  * of a relation.
  * ------------------------------------------------------------------------------------------------------------ */
 							if ( arg_debug >= Log_Informational_2 )
 							{
 								System.out.println( "Tags: " + osmObjectInfo.get_number_of_tags() );
 							}
 						} // l2 not #text
 					} // for l2 XML nodes (nodes, ways and relations)
 				} // l1 not #text
 			} // for l2 XML nodes (create, modify and delete)
 			
 /* ------------------------------------------------------------------------------------------------------------
  * After processing all XML nodes in the changeset, look through the objects in our list.
  * ------------------------------------------------------------------------------------------------------------ */
 			if ( arg_out_file != "" )
 			{
 				for ( int cntr_1 = 0; cntr_1 < osmObjectList.size(); ++cntr_1 ) 
 				{
 					OsmObjectInfo osmObjectInfo = osmObjectList.get( cntr_1 );
 					
 					if (( osmObjectInfo.get_item_type() != OsmObjectKey.Item_Node ) &&
 						( osmObjectInfo.get_number_of_tags() == 0          ) &&
 						( osmObjectInfo.get_last_action() != OsmObjectDetails.Action_Delete ))
 						{
 							if ( osmObjectInfo.get_item_type() == OsmObjectKey.Item_Way )
 							{
 								myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Way " + osmObjectInfo.get_item_id()  + " finally has no tags"  );
 							}
 							else
 							{
 								myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Relation " + osmObjectInfo.get_item_id()  + " finally has no tags" );
 							}
 						}
 					
 					try
 					{
 						if ( osmObjectInfo.get_building_or_shop_found() && ( osmObjectInfo.get_number_of_children() > Integer.valueOf( passed_building )))
 						{
 							myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Way " + osmObjectInfo.get_item_id()  + " is a huge building or shop" );
 						}
 					}
 					catch( Exception ex )
 					{
 						myPrintStream.println( osmObjectInfo.get_item_user() + ";" + osmObjectInfo.get_item_uid() + ";" + passed_changeset_number + ";;;;Way " + osmObjectInfo.get_item_id()  + " - error evaluating way nodes: " + ex.getMessage() );
 					}
 				} // for each object in the list
 			} // arg_out_file != ""
 		} // root node in the XML is an element
 		
 		return changeset_nodes_overlap;
 	}
 
 	/**
 	 * download_changeset
 	 * 
 	 * @param passed_changeset_number  The changeset number to download
 	 * 
 	 * @param passed_min_lat_string  The bounding box that we're interested in - 
 	 * node positions will be checked against this box.
 	 * @param passed_min_lon_string
 	 * @param passed_max_lat_string
 	 * @param passed_max_lon_string
 	 * 
 	 * @param passed_building  The "number of nodes in a shop/building" value to worry about
 	 * 
 	 * @return returns "true" if there are individual nodes (not nodes in a way or relation) 
 	 * in the changeset that overlaps the bounding box that we passed in.
 	 * 
 	 * @throws Exception
 	 */
 	private static boolean download_changeset( String passed_changeset_number, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception
 	{
 		boolean nodes_overlap = false;
 		
 		if ( arg_debug >= Log_Informational_2 )
 		{
 			System.out.println( "We will try and download changeset: " + passed_changeset_number );
 		}
 		
 		URL url = new URL( api_path + "changeset/" + passed_changeset_number + "/download" );
 		InputStreamReader input;
 		
 		URLConnection urlConn = url.openConnection();
 		urlConn.setDoInput( true );
 		urlConn.setDoOutput( false );
 		urlConn.setUseCaches( false );
 	
 		input = new InputStreamReader( urlConn.getInputStream() );
 	
 	    char[] data = new char[ 256 ];
 	    int len = 0;
 		StringBuffer sb = new StringBuffer();		
 	
 	    while ( -1 != ( len = input.read( data, 0, 255 )) )
 	    {
 	        sb.append( new String( data, 0, len ));
 	    }   
 	
 	    DocumentBuilderFactory AJTfactory = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder AJTbuilder = AJTfactory.newDocumentBuilder();
 	    ByteArrayInputStream inputStream = new ByteArrayInputStream( sb.toString().getBytes( "UTF-8" ));
 	
 	    Document AJTdocument = AJTbuilder.parse( inputStream );
 	    Element AJTrootElement = AJTdocument.getDocumentElement();
 	    
 	    nodes_overlap = process_downloaded_changeset_xml( AJTrootElement, passed_changeset_number, 
 	    		passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 	    		passed_building, passed_overlapnodes, passed_download_nodes );
 	
 	    input.close();
 	    return nodes_overlap;
 	}
 	
 	
 	/**
 	 * process_changesets_xml
 	 * 
 	 * We've made an API call for changesets (for parameters that might include "since" time and user info).  
 	 *  
 	 * @param root_node  The root node of the XML tree of the changesets returned in response to our query
 	 * 
 	 * @param passed_display_name  The current display_name that we're interested in, if any.
 	 * @param passed_uid  The current user ID that we're interested in, if any.
 	 * 
 	 * @param passed_min_lat_string  The bounding box that we want to check that a particular changeset occurs within.
 	 * @param passed_min_lon_string
 	 * @param passed_max_lat_string
 	 * @param passed_max_lon_string
 	 * 
 	 * @param passed_download_changeset  If set to "1", download the changeset to check for potential problems, such as deleted ways and relations.
 	 * 
 	 * @param passed_building  If set, the value to compare the number of nodes in a shop or building against to warn that e.g. some landuse has
 	 * been turned into a shop or building by mistake.  This parameter is only valid if passed_download_changeset is set to "1".
 	 *  
 	 * @param passed_overlapnodes  If set, we're being asked to report on each overlapping node within our bounding box.  
 	 * This parameter is only valid if passed_download_changeset is set to "1".
 	 * 
 	 * @param passed_download_nodes  If set to "1", we're being asked to download the previous version of deleted nodes to see if they have been
 	 * deleted from our bounding box.  This parameter is only valid if passed_download_changeset is set to "1".
 	 */
 	private static void process_changesets_xml( Node root_node, String passed_display_name, String passed_uid, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes )
 	{
 		int osm_changesets_found = 0;
 		int osm_changesets_of_interest = 0;
 	
 		if ( root_node.getNodeType() == Node.ELEMENT_NODE ) 
 		{
 			NodeList level_1_xmlnodes = root_node.getChildNodes();
 			int num_l1_xmlnodes = level_1_xmlnodes.getLength();
 	
 			if ( arg_debug >= Log_Informational_2 )
 			{
 				System.out.println( "Changesets L1 nodes found: " + num_l1_xmlnodes );
 			}
 	
 /* ------------------------------------------------------------------------------------------------------------
  * Iterate through the changesets for a user. 
  * ------------------------------------------------------------------------------------------------------------ */
 			for ( int cntr_1 = 0; cntr_1 < num_l1_xmlnodes; ++cntr_1 ) 
 			{
 				Node this_l1_item = level_1_xmlnodes.item( cntr_1 );
 				String l1_item_type = this_l1_item.getNodeName();
 	
 				if ( l1_item_type.equals( "changeset" ))
 				{
 					int changeset_bbox_interest_flag = 0;
 					Node id_node = null; 
 					Node user_node = null;
 					Node uid_node = null;
 					String editor_name = "";
 					String editor_version = "";
 					String comment = "";
 					
 					osm_changesets_found++;
 	
 					NodeList level_2_xmlnodes = this_l1_item.getChildNodes();
 					int num_l2_xmlnodes = level_2_xmlnodes.getLength();
 	
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "L2 nodes found: " + num_l2_xmlnodes );
 					}
 	                    
 /* ------------------------------------------------------------------------------------------------------------
  * Items can have both attributes (e.g. "id", "user") and tags (XML child nodes) - process the attributes first. 
  * ------------------------------------------------------------------------------------------------------------ */
 					if ( this_l1_item.hasAttributes() )
 					{
 						NamedNodeMap item_attributes = this_l1_item.getAttributes();
 						id_node = item_attributes.getNamedItem( "id" );
 						user_node = item_attributes.getNamedItem( "user" );
 						uid_node = item_attributes.getNamedItem( "uid" );
 						
 						if ( id_node == null )
 						{
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "No changeset ID found" );
 							}
 			            }
 						else
 						{
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "Changeset: " + id_node.getNodeValue() );
 							}
 						}
 
 						if ( user_node == null )
 						{
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "No user name found" );
 							}
 			            }
 						else
 						{
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "User: " + user_node.getNodeValue() );
 							}
 						}
 
 						if ( uid_node == null )
 						{
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "No uid found" );
 							}
 			            }
 						else
 						{
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "Uid: " + uid_node.getNodeValue() );
 							}
 						}
 
 /* ------------------------------------------------------------------------------------------------------------
  * Based on the changset XML only, decide whether we're interested in processing this changset. 
  * ------------------------------------------------------------------------------------------------------------ */
 						changeset_bbox_interest_flag = check_bbox_interest( root_node, item_attributes, id_node, 
 								passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string );
 					} // attributes
 					
 					for ( int cntr_2 = 0; cntr_2 < num_l2_xmlnodes; ++cntr_2 ) 
 					{
 						Node this_l2_item = level_2_xmlnodes.item( cntr_2 );
 						String l2_item_type = this_l2_item.getNodeName();
 
 						if ( l2_item_type.equals( "tag" ))
 						{
 							if ( this_l2_item.hasAttributes() )
 							{
 								NamedNodeMap item_attributes = this_l2_item.getAttributes();
 								Node key_node = item_attributes.getNamedItem( "k" );
 								Node value_node = item_attributes.getNamedItem( "v" );
 
 								if ( key_node == null )
 								{
 									System.out.println( "Changeset tag/value processing: No key found" );
 /* ------------------------------------------------------------------------------------------------------------
  * If we haven't found a tag name don't bother looking for a value. 
  * ------------------------------------------------------------------------------------------------------------ */
 								}
 								else
 								{
 									if ( arg_debug >= Log_Informational_2 )
 									{
 										System.out.println( "tag: " + key_node.getNodeValue() );
 									}
 
 /* ------------------------------------------------------------------------------------------------------------
  * We have found a tag name - check the value. 
  * ------------------------------------------------------------------------------------------------------------ */
 
 									if ( value_node == null )
 									{
 										System.out.println( "Changeset tag/value processing: No value found" );
 									}
 									else
 									{
 										if ( arg_debug >= Log_Informational_2 )
 										{
 											System.out.println( "value: " + value_node.getNodeValue() );
 										}
 
 										if ( key_node.getNodeValue().equals( "created_by" ))
 										{
 											editor_name = value_node.getNodeValue();
 										}
 
 										if ( key_node.getNodeValue().equals( "version" ))
 										{
 											editor_version = value_node.getNodeValue();
 										}
 
 										if ( key_node.getNodeValue().equals( "comment" ))
 										{
 											comment = value_node.getNodeValue();
 										}
 									}
 								}
 
 							} // tag item has attributes
 						}
 					}
 
 /* ------------------------------------------------------------------------------------------------------------
  * We've processed all attributes and child nodes; write out what we know about this changeset if we are 
  * interested in it.
  * ------------------------------------------------------------------------------------------------------------ */
 					if ( changeset_bbox_interest_flag == Overlap_All )
 					{
 /* ------------------------------------------------------------------------------------------------------------
  * We're interested in all changesets.  Although download_changeset will return true if a node within the 
  * changeset has a lat/lon within the lat/lon ranges that we are interested in, we don't care.  
  * ------------------------------------------------------------------------------------------------------------ */
 						osm_changesets_of_interest++;
 
 						if ( arg_out_file != "" )
 						{
 							myPrintStream.println( user_node.getNodeValue() + ";" + uid_node.getNodeValue() + ";" + id_node.getNodeValue() + ";" + editor_name + ";" + editor_version + ";" + comment + ";Changeset: all" );
 						}
 
 /* ------------------------------------------------------------------------------------------------------------
  * By default we don't download the changesets themselves - we only do that if explicitly requested to by the
  * user.  The "-building=X" value only makes sense if we are downloading changesets. 
  * ------------------------------------------------------------------------------------------------------------ */
 						if ( passed_download_changeset.equals( "1") )
 						{
 							try
 							{
 /* ------------------------------------------------------------------------------------------------------------
  * Download the changeset and check it, but ignore the return value since we don't need to know if it 
  * overlaps as we're interested in all changesets. 
  * ------------------------------------------------------------------------------------------------------------ */
 								download_changeset( id_node.getNodeValue(), 
 										passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 										passed_building, passed_overlapnodes, passed_download_nodes );
 							}
 							catch( Exception ex )
 							{
 								System.out.println( "Exception downloading changeset" );
 							}
 						}
 					} // Overlap_All
 					else
 					{
 /* ------------------------------------------------------------------------------------------------------------
  * If we have "changeset_bbox_interest_flag == Overlap_Error" it's probably because a changeset has no bounding
  * box, which can happen if it has nothing at all or only relations in it.
  * 
  * Because we don't know, we'll assume that we are interested.
  * ------------------------------------------------------------------------------------------------------------ */
 						if (( changeset_bbox_interest_flag == Overlap_Yes   ) ||
 							( changeset_bbox_interest_flag == Overlap_Error ))
 						{
 /* ------------------------------------------------------------------------------------------------------------
  * We're interested in overlapping changesets.  If we're downloading it well set the "interested" counter based
  * on whether nodes in the changeset are in our area of interest.  If not, we'll use the changeset bbox (which
  * we already know because our_interest is set to Overlap_Yes).
  * 
  * Note that the "bbox" parameter wasn't passed to the API so we're reading through all changesets for a user
  * within our date range, not just those within the bbox.  The reason for this is so that we can tell whether
  * a user has (a) edited near us, (b) edited elsewhere or (c) not edited at all.
  * ------------------------------------------------------------------------------------------------------------ */
 
 							if ( arg_out_file != "" )
 							{
 								myPrintStream.println( user_node.getNodeValue() + ";" + uid_node.getNodeValue() + ";" + id_node.getNodeValue() + ";" + editor_name + ";" + editor_version + ";" + comment + ";Changeset: bbox overlaps" );
 							}
 
 							if ( passed_download_changeset.equals( "1") )
 							{
 								try
 								{
 									boolean changeset_node_interest_flag = false;
 									
 /* ------------------------------------------------------------------------------------------------------------
  * changeset_node_interest_flag is set based on seeing if any nodes in the changeset overlap our bounding box.
  * If there weren't any nodes in the changeset it won't get set and we may undercount  
  * "osm_changesets_of_interest" below, even though we've actually processed the changeset looking for unnamed
  * ways etc.
  * ------------------------------------------------------------------------------------------------------------ */
 									changeset_node_interest_flag = download_changeset( id_node.getNodeValue(), 
 											passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 											passed_building, passed_overlapnodes, passed_download_nodes );
 									
 									if ( changeset_node_interest_flag )
 									{
 										osm_changesets_of_interest++;
 									}
 								}
 								catch( Exception ex )
 								{
 									System.out.println( "Exception downloading changeset" );
 								}
 							} // passed_download_changeset set
 							else
 							{
 /* ------------------------------------------------------------------------------
  * We're not downloading each changeset's XML so assume we are interested based
  * on the fact that the changeset bbox overlaps.
  * ------------------------------------------------------------------------------ */
 								osm_changesets_of_interest++;
 							}
 						} // Overlap_Yes or Overlap_Error
 						else
 						{
 							if ( changeset_bbox_interest_flag != Overlap_No )
 							{
 /* ------------------------------------------------------------------------------
  * We're not expecting anything other than Overlap_No by this stage, but write
  * out an error if we get anything unexpected.
  * ------------------------------------------------------------------------------ */
 								if ( arg_out_file != "" )
 								{
 									myPrintStream.println( user_node.getNodeValue() + ";" + uid_node.getNodeValue() + ";" + id_node.getNodeValue() + ";" + editor_name + ";" + editor_version + ";" + comment + ";changeset_bbox_interest_flag: " + changeset_bbox_interest_flag );
 								}
 							}
 						}
 					}
 					
 				} // changeset
 				else
 				{ // !changeset
 					if ( l1_item_type != "#text" )
 					{
 						if ( arg_debug >= Log_Informational_1 )
 						{
 							System.out.println( "Node " + cntr_1 + ": " + l1_item_type );
 						}
 					}
 				} // !changeset
 			} // for L1 nodes
 	            
 			if ( arg_debug >= Log_Informational_1 )
 			{
 				System.out.println( "Changesets found: " + osm_changesets_found + ", of interest: " + osm_changesets_of_interest );
 			}
 
 			if (( arg_out_file != ""              ) &&
 				( osm_changesets_of_interest == 0 ))
 			{
 				myPrintStream.println( passed_display_name + ";" + passed_uid + ";;;;;" + osm_changesets_found + " changesets, none of interest" );
 			}
 		}	
 		else
 		{
 			if ( arg_debug >= Log_Error )
 			{
 				System.out.println( "XML Parsing Error - element node expected" );
 			}
 		}
 	}
 	
	static void process_changesets_url_common ( URL passed_url, String passed_display_name, String passed_uid, String passed_id,  
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception
 	{
 		if ( arg_debug >= Log_Informational_2 )
 		{
 			System.out.println( "Url: " + passed_url );
 		}
 		
 		InputStreamReader input;
 	
 		URLConnection urlConn = passed_url.openConnection();
 		urlConn.setDoInput( true );
 		urlConn.setDoOutput( false );
 		urlConn.setUseCaches( false );
 	
 		try
 		{
 			input = new InputStreamReader( urlConn.getInputStream() );
 			
 		    char[] data = new char[ 256 ];
 		    int len = 0;
 			StringBuffer sb = new StringBuffer();		
 		
 		    while ( -1 != ( len = input.read( data, 0, 255 )) )
 		    {
 		        sb.append( new String( data, 0, len ));
 		    }   
 		
 		    DocumentBuilderFactory AJTfactory = DocumentBuilderFactory.newInstance();
 		    DocumentBuilder AJTbuilder = AJTfactory.newDocumentBuilder();
 		    ByteArrayInputStream inputStream = new ByteArrayInputStream( sb.toString().getBytes( "UTF-8" ));
 		
 		    Document AJTdocument = AJTbuilder.parse( inputStream );
 		    Element AJTrootElement = AJTdocument.getDocumentElement();
 		    
 		    process_changesets_xml( AJTrootElement, passed_display_name, passed_uid, 
 		    		passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 		    		passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 		
 		    input.close();
 		}
 		catch( Exception ex )
 		{
 			if ( arg_debug >= Log_Error )
 			{
				//qqq
				System.out.println( "Error obtaining: " + passed_url );
			}
			
			if ( arg_out_file != "" )
			{
				myPrintStream.println( passed_display_name + ";" + passed_uid + ";" + passed_id + ";;;;0 changesets, error obtaining" );
 			}
 		}
 	}
 	
 	
 	static void process_display_name_and_time( String passed_display_name, String passed_time, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception 
 	{
 		if ( arg_debug >= Log_Low_Routine_Start )
 		{
 			System.out.println( "process_display_name_and_time" );
 		}
 
 		URL url;
 		url = new URL( api_path + "changesets?display_name=" + ( URLEncoder.encode( passed_display_name , "UTF-8" )) + "&time=" + passed_time );
 		
		process_changesets_url_common( url, passed_display_name, "", "", 
 				passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 				passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 	}
 	
 	
 	static void process_uid_and_time( String passed_uid, String passed_time, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception 
 	{
 		if ( arg_debug >= Log_Low_Routine_Start )
 		{
 			System.out.println( "process_uid_and_time" );
 		}
 
 		URL url;
 		url = new URL( api_path + "changesets?user=" + ( URLEncoder.encode( passed_uid , "UTF-8" )) + "&time=" + passed_time );
 		
		process_changesets_url_common( url, "", passed_uid, "", passed_min_lat_string, 
 				passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 				passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 	}
 	
 	
 	static void process_display_name( String passed_display_name, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception 
 	{
 		if ( arg_debug >= Log_Low_Routine_Start )
 		{
 			System.out.println( "process_display_name" );
 		}
 
 		URL url;
 		url = new URL( api_path + "changesets?display_name=" + ( URLEncoder.encode( passed_display_name , "UTF-8" )));
 		
		process_changesets_url_common( url, passed_display_name, "", "", 
 				passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 				passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 	}
 	
 	
 	static void process_uid( String passed_uid, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception 
 	{
 		if ( arg_debug >= Log_Low_Routine_Start )
 		{
 			System.out.println( "process_uid" );
 		}
 
 		URL url;
 		url = new URL( api_path + "changesets?user=" + ( URLEncoder.encode( passed_uid , "UTF-8" )));
 		
		process_changesets_url_common( url, "", passed_uid, "", 
 				passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 				passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 	}
 	
 	
 	static void process_time( String passed_time, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception 
 	{
 		if ( arg_debug >= Log_Low_Routine_Start )
 		{
 			System.out.println( "process_time" );
 		}
 
 		URL url;
 		url = new URL( api_path + "changesets?time=" + passed_time );
 		
		process_changesets_url_common( url, "All Users", "", "", 
 				passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 				passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 	}
 	
 	
 	static void process_id( String passed_id, 
 			String passed_min_lat_string, String passed_min_lon_string, String passed_max_lat_string, String passed_max_lon_string, 
 			String passed_download_changeset, String passed_building, boolean passed_overlapnodes, String passed_download_nodes ) throws Exception 
 	{
 		if ( arg_debug >= Log_Low_Routine_Start )
 		{
 			System.out.println( "process_id" );
 		}
 
 		URL url;
 		url = new URL( api_path + "changeset/" + passed_id );
 		
		process_changesets_url_common( url, "Any User", "", passed_id, 
 				passed_min_lat_string, passed_min_lon_string, passed_max_lat_string, passed_max_lon_string, 
 				passed_download_changeset, passed_building, passed_overlapnodes, passed_download_nodes );
 	}
 
 	
 	/**
 	 * get_line_param
 	 * 
 	 * @param passed_param  The parameter that we are looking for, such as "-display_name=".
 	 * @param passed_in_line  The line that we've read from the input file in which we're looking for the value of that parameter.
 	 * 
 	 * @return  The value of the parameter from that line.
 	 */
 	static String get_line_string_param( String passed_param, String passed_in_line )
 	{
 		int param_start = 0;
 		int param_end = 0;
 		String line_param = "";
 		
 		try
 		{
 			param_start = passed_in_line.indexOf( passed_param );
 			
 			if ( param_start != -1 )
 			{
 				if ( passed_in_line.substring(( param_start + passed_param.length() ), ( param_start + passed_param.length() + 1 )).equals( "\"" ))
 				{
 					param_start++;
 					param_end = passed_in_line.indexOf( "\"", ( param_start + passed_param.length() ));
 	
 					if ( param_end == -1 )
 					{
 						param_end = passed_in_line.length() + 1;
 					}
 	
 					line_param = passed_in_line.substring( ( param_start + passed_param.length() ), param_end );
 				}
 				else
 				{
 					param_end = passed_in_line.indexOf( " ", ( param_start + passed_param.length() ));
 					
 					if ( param_end == -1 )
 					{
 						param_end = passed_in_line.length();
 					}
 					
 					line_param = passed_in_line.substring( ( param_start + passed_param.length() ), param_end );
 				}
 			}
 		}
 		catch( Exception ex )
 		{
 			System.out.println( "Error parsing param: " + passed_in_line );
 		}
 		
 		return line_param;
 	}
 	
 /* ------------------------------------------------------------------------------
  * Data passed on the command line:
  * 
  * param_help = "-help=";
  * param_input = "-input=";
  * param_output = "-output=";
  * param_display_name = "-display_name=";
  * param_uid = "-user=";
  * param_id = "-id=";
  * param_time = "-time=";
  * param_dev = "-dev";
  * param_debug = "-debug=";
  * param_bbox = "-bbox=";
  * param_download_changeset = "-download_changeset=";
  * param_download_nodes = "-download_nodes=";
  * param_building = "-building=";
  * param_overlapnodes = "-report_overlap_nodes=";
  * 
  * ------------------------------------------------------------------------------ */
 /**
  * @param args
  */
 	public static void main(String[] args) throws Exception 
 	{
 		String arg_display_name = "";
 		String arg_uid = "";
 		String arg_id = "";
 		String arg_time = "";
 		
 /* ------------------------------------------------------------------------------
  * What command-line arguments have we been passed? 
  * 
  * These are processed in the order that we encounter them, so if specifying 
  * debug, it will take effect from where it's found in the command line.
  * ------------------------------------------------------------------------------ */
 		for ( int i=0; i<args.length; i++ )
 		{
 /* ------------------------------------------------------------------------------
  * Command line arguments are all "minus something", so are all at least two 
  * characters long. 
  * ------------------------------------------------------------------------------ */
 			if ( args[i].length() >= 2)
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "arg: " + i );
 					System.out.println( "arg length: " + args[i].length() );
 				}
 				
 /* ------------------------------------------------------------------------------
  * Help
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_help ))
 				{	
 					arg_help = true;
 					show_help();
 				}
 /* ------------------------------------------------------------------------------
  * Input file
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_input ))
 				{	
 					arg_in_file = args[i].substring( param_input.length() );
 
 					try
 					{
 						myFileReader = new FileReader( arg_in_file );
 						myBufferedReader = new BufferedReader( myFileReader );
 					}
 					catch( Exception ex )
 					{
 /* ------------------------------------------------------------------------------
  * If there's an error opening the input file, don't pretend that it wasn't 
  * specified on the command line.
  * ------------------------------------------------------------------------------ */
 						arg_in_file = "!file";
 						
 						if ( arg_debug >= Log_Informational_1 )
 						{
 							System.out.println( "Error opening input file: " + ex.getMessage() );
 						}
 					}
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_in_file: " + arg_in_file );
 						System.out.println( "arg_in_file length: " + arg_in_file.length() );
 					}
 				} // -input
 				
 /* ------------------------------------------------------------------------------
  * Output file
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_output ))
 				{	
 					arg_out_file = args[i].substring( param_output.length() );
 
 					try
 					{
 						myOutputStream = new FileOutputStream( arg_out_file );
 						myPrintStream = new PrintStream( myOutputStream );
 					}
 					catch( Exception ex )
 					{
 						arg_out_file = "";
 						
 						if ( arg_debug >= Log_Informational_1 )
 						{
 							System.out.println( "Error opening output file: " + ex.getMessage() );
 						}
 					}
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_out_file: " + arg_out_file );
 						System.out.println( "arg_out_file length: " + arg_out_file.length() );
 					}
 				} // -output
 				
 /* ------------------------------------------------------------------------------
  * The user that we're interested in changesets for - display name
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_display_name ))
 				{	
 					arg_display_name = args[i].substring( param_display_name.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_display_name: " + arg_display_name );
 						System.out.println( "arg_display_name length: " + arg_display_name.length() );
 					}
 				} // -display_name
 				
 /* ------------------------------------------------------------------------------
  * The user that we're interested in changesets for - uid
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_uid ))
 				{	
 					arg_uid = args[i].substring( param_uid.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_uid: " + arg_uid );
 						System.out.println( "arg_uid length: " + arg_uid.length() );
 					}
 				} // -uid
 				
 /* ------------------------------------------------------------------------------
  * The changeset number that we're interested in changesets for - id
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_id ))
 				{	
 					arg_id = args[i].substring( param_id.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_id: " + arg_id );
 						System.out.println( "arg_id length: " + arg_id.length() );
 					}
 				} // -id
 				
 /* ------------------------------------------------------------------------------
  * The time to start looking for changesets from 
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_time ))
 				{	
 					arg_time = args[i].substring( param_time.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_time: " + arg_time );
 						System.out.println( "arg_time length: " + arg_time.length() );
 					}
 				} // -time
 				
 /* ------------------------------------------------------------------------------
  * Should we user the dev API?
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_dev ))
 				{	
 					api_path = dev_api_path;
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "Dev server will be used" );
 					}
 				} // -dev
 				
 /* ------------------------------------------------------------------------------
  * Should we download changesets that we are interested in?
  * 
  * We do this if we want to look for e.g. deleted relations.
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_download_changeset ))
 				{	
 					arg_download_changeset = args[i].substring( param_download_changeset.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_download_changeset: " + arg_download_changeset );
 						System.out.println( "arg_download_changeset length: " + arg_download_changeset.length() );
 					}
 				} // -download_changeset
 				
 /* ------------------------------------------------------------------------------
  * Should we download individual nodes that we are interested in?
  * 
  * We do this if we want to specifically look for deleted nodes within a 
  * bounding box.
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_download_nodes ))
 				{	
 					arg_download_nodes = args[i].substring( param_download_nodes.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_download_nodes: " + arg_download_nodes );
 						System.out.println( "arg_download_nodes length: " + arg_download_nodes.length() );
 					}
 				} // -download_nodes
 				
 /* ------------------------------------------------------------------------------
  * After how many nodes should we warn about buildings?
  * 
  * A building with a very large number of nodes is likely to have been converted 
  * from landuse because of iD bug 542.
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_building ))
 				{	
 					arg_building = args[i].substring( param_building.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_building: " + arg_building );
 						System.out.println( "arg_building length: " + arg_building.length() );
 					}
 				} // -building
 				
 /* ------------------------------------------------------------------------------
  * Should we explicitly report on nodes that overlap our bounding box?
  * 
  * It's useful for large changesets that have only few overlapping nodes (e.g.
  * "tagfiddling" ones).
  * 
  * It's less useful for changesets that have a very large number of overlapping
  * nodes.
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_overlapnodes ))
 				{	
 					try
 					{
 						arg_overlapnodes = ( Integer.valueOf( args[i].substring( param_overlapnodes.length())) > 0 );
 					}
 					catch( Exception ex )
 					{
 /* ------------------------------------------------------------------------------
  * Any failure above just means that we leave arg_overlapnodes at the default 
  * value
  * ------------------------------------------------------------------------------ */
 					}
 
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_overlapnodes: " + arg_overlapnodes );
 					}
 				} // -overlapnodes
 				
 /* ------------------------------------------------------------------------------
  * Debug level
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_debug ))
 				{	
 					try
 					{
 						arg_debug = Integer.valueOf( args[i].substring( param_debug.length() ));
 					}
 					catch( Exception ex )
 					{
 /* ------------------------------------------------------------------------------
  * Any failure above just means that we leave arg_debug at 0
  * ------------------------------------------------------------------------------ */
 					}
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_debug: " + arg_debug );
 					}
 				} // -debug
 				
 /* ------------------------------------------------------------------------------
  * A bbox that we're interesting in comparing changesets with.
  * This isn't passed to the API; we compare changesets with it later.
  * ------------------------------------------------------------------------------ */
 				if ( args[i].startsWith( param_bbox ))
 				{	
 					arg_bbox = args[i].substring( param_bbox.length() );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "arg_bbox: " + arg_bbox );
 						System.out.println( "arg_bbox length: " + arg_bbox.length() );
 					}
 					
 					int comma_pos = arg_bbox.indexOf( "," );
 					int old_comma_pos = 0;
 					
 					if ( comma_pos > 0 )
 					{ // found min lon
 						arg_min_lon_string = arg_bbox.substring( 0, comma_pos );
 						
 						if ( arg_debug >= Log_Informational_1 )
 						{
 							System.out.println( "arg_min_lon: " + arg_min_lon_string );
 						}
 
 						
 						old_comma_pos = comma_pos;
 						comma_pos = arg_bbox.indexOf( ",", comma_pos+1 );
 
 						if ( comma_pos > 0 )
 						{ // found min lat
 							arg_min_lat_string = arg_bbox.substring( old_comma_pos+1, comma_pos );
 							
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "arg_min_lat: " + arg_min_lat_string );
 							}
 
 							
 							old_comma_pos = comma_pos;
 							comma_pos = arg_bbox.indexOf( ",", comma_pos+1 );
 
 							if ( comma_pos > 0 )
 							{ // found max lon; what's left must be max lat
 								arg_max_lon_string = arg_bbox.substring( old_comma_pos+1, comma_pos );
 								
 								if ( arg_debug >= Log_Informational_1 )
 								{
 									System.out.println( "arg_max_lon: " + arg_max_lon_string );
 								}
 
 
 								old_comma_pos = comma_pos;
 								arg_max_lat_string = arg_bbox.substring( old_comma_pos+1 );
 								
 								if ( arg_debug >= Log_Informational_1 )
 								{
 									System.out.println( "arg_max_lat: " + arg_max_lat_string );
 								}
 							} // max lon found
 							else
 							{
 								if ( arg_debug >= Log_Error )
 								{
 									System.out.println( "3rd comma_pos: " + comma_pos );
 									arg_bbox = "";
 								}
 							} // no max lon
 						} // min lat found
 						else
 						{
 							if ( arg_debug >= Log_Error )
 							{
 								System.out.println( "2nd comma_pos: " + comma_pos );
 								arg_bbox = "";
 							}
 						} // no min lat
 					} // min lon found
 					else
 					{
 						if ( arg_debug >= Log_Error )
 						{
 							System.out.println( "1st comma_pos: " + comma_pos );
 							arg_bbox = "";
 						}
 					} // no min lon
 				} // -bbox
 			} // potentially valid argument
 		} // for each thing on the command line
 
 /* ------------------------------------------------------------------------------
  * Actually do what we've been asked to do.
  * 
  * If we've not been passed an input file will just process the parameters from
  * the command line.
  * ------------------------------------------------------------------------------ */
 		if ( arg_in_file.equals( "" ))
 		{
 			if ( arg_debug >= Log_Informational_2 )
 			{
 				System.out.println( "No input file" );
 			}
 			
  /* ------------------------------------------------------------------------------
   * If we're processing users and/or time we need one or both of those arguments.
   * ------------------------------------------------------------------------------ */
 			if ( arg_display_name.length() == 0 )
 			{
 				if ( arg_uid.length() == 0 )
 				{
 					if ( arg_time.length() == 0 )
 					{
 						if ( arg_id.length() == 0 )
 						{
  /* ------------------------------------------------------------------------------
   * None of the parameters that we would have expect to have been passed were, 
   * so show the help screen, if we haven't already been asked to do so.
   * ------------------------------------------------------------------------------ */
 							if ( arg_debug >= Log_Informational_2 )
 							{
 								System.out.println( "None of display_name, user, id or time passed" );
 								System.out.println( "" );
 							}
 							
 							if ( arg_help == false )
 							{
 								show_help();
 							}
 						}
 						else
 						{ // We've been passed a specific changeset id
 							process_id( arg_id, arg_min_lat_string, arg_min_lon_string, 
 									arg_max_lat_string, arg_max_lon_string, 
 									arg_download_changeset, arg_building, 
 									arg_overlapnodes, arg_download_nodes );
 						}
 					}
 					else
 					{
 						process_time( arg_time, arg_min_lat_string, arg_min_lon_string, 
 								arg_max_lat_string, arg_max_lon_string, 
 								arg_download_changeset, arg_building, 
 								arg_overlapnodes, arg_download_nodes );
 					}
 				}
 				else
 				{ // no display_name, but we do have a uid
 					if ( arg_time.length() == 0 )
 					{
 						process_uid( arg_uid, arg_min_lat_string, arg_min_lon_string, 
 								arg_max_lat_string, arg_max_lon_string, 
 								arg_download_changeset, arg_building, 
 								arg_overlapnodes, arg_download_nodes );
 					} // no time argument passed
 					else
 					{
 						process_uid_and_time( arg_uid, arg_time, 
 								arg_min_lat_string, arg_min_lon_string, 
 								arg_max_lat_string, arg_max_lon_string, 
 								arg_download_changeset, arg_building, 
 								arg_overlapnodes, arg_download_nodes );
 					}
 				}
 			} // no display_name argument passed
 			else
 			{ // display_name passed.  We'll not bother checking for a uid.
 				if ( arg_time.length() == 0 )
 				{
 					process_display_name( arg_display_name, 
 							arg_min_lat_string, arg_min_lon_string, 
 							arg_max_lat_string, arg_max_lon_string, 
 							arg_download_changeset, arg_building, 
 							arg_overlapnodes, arg_download_nodes );
 				} // no time argument passed
 				else
 				{
 					process_display_name_and_time( arg_display_name, arg_time, 
 							arg_min_lat_string, arg_min_lon_string, 
 							arg_max_lat_string, arg_max_lon_string, 
 							arg_download_changeset, arg_building, 
 							arg_overlapnodes, arg_download_nodes );
 				}
 			} // user argument passed
 		} // no "in" file
 		else
 		{ // input file passed
 			if ( arg_in_file.equals( "!file" ))
 			{
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Input file could not be opened" );
 				}
 			}
 			else
 			{
 /* ------------------------------------------------------------------------------
  * We do have an input file defined and we have been able to open it.
  * 
  * Each line in the input file can contain additional parameters.  For example,
  * one could contain:
  * 
  * -display_name="SomeoneElse" -bbox=-2.123,52.809,-0.331,53.521 -download_changeset=1 -report_overlap_nodes=1
  * 
  * Other than "input", "output", "dev" and "debug", any parameter can be used in
  * an input file line in this way.  If a value is set on the command line, and
  * to a different value in an input file line, the value on the input file 
  * line takes precedence.
  * ------------------------------------------------------------------------------ */
 				if ( arg_debug >= Log_Informational_2 )
 				{
 					System.out.println( "Input file: " + arg_in_file );
 				}
 
 				String in_line = "";
 				String line_display_name = "";
 				String line_uid = "";
 				String line_id = "";
 				String line_time = "";
 				String line_bbox = "";
 				String line_min_lat_string = "";
 				String line_min_lon_string = "";
 				String line_max_lat_string = "";
 				String line_max_lon_string = "";
 				String line_download_changeset = "";
 				String line_download_nodes = "";
 				String line_building = "";
 				String line_overlapnodes_string = "";
 				boolean line_overlapnodes=arg_overlapnodes;
 				
 				while(( in_line = myBufferedReader.readLine() ) != null )
 				{
 /* ------------------------------------------------------------------------------
  * The "line_" values default to "" (see above).  If any of these values aren't 
  * set from the line, set to the comment line "arg_" values. 
  * ------------------------------------------------------------------------------ */
 					line_display_name = get_line_string_param( param_display_name, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_display_name: " + line_display_name );
 					}
 					
 					if ( line_display_name.equals( "" ))
 					{
 						line_display_name = arg_display_name;
 					}
 					
 					line_uid          = get_line_string_param( param_uid, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_uid: " + line_uid );
 					}
 					
 					if ( line_uid.equals( "" ))
 					{
 						line_uid = arg_uid;
 					}
 					
 					line_id          = get_line_string_param( param_id, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_id: " + line_id );
 					}
 					
 					if ( line_id.equals( "" ))
 					{
 						line_id = arg_id;
 					}
 					
 					line_time         = get_line_string_param( param_time, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_time: " + line_time );
 					}
 					
 					if ( line_time.equals( "" ))
 					{
 						line_time = arg_time;
 					}
 					
 					line_bbox         = get_line_string_param( param_bbox, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_bbox: " + line_bbox );
 					}
 					
 					if ( line_bbox.equals( "" ))
 					{
 						line_bbox = arg_bbox;
 					}
 					
 					line_download_changeset     = get_line_string_param( param_download_changeset, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_download_changeset: " + line_download_changeset );
 					}
 					
 					if ( line_download_changeset.equals( "" ))
 					{
 						line_download_changeset = arg_download_changeset;
 					}
 					
 					line_download_nodes     = get_line_string_param( param_download_nodes, in_line );
 					
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_download_nodes: " + line_download_nodes );
 					}
 					
 					if ( line_download_nodes.equals( "" ))
 					{
 						line_download_nodes = arg_download_nodes;
 					}
 					
 					line_building     = get_line_string_param( param_building, in_line );
 
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_building: " + line_building );
 					}
 					
 					if ( line_building.equals( "" ))
 					{
 						line_building = arg_building;
 					}
 					
 					line_overlapnodes_string     = get_line_string_param( param_overlapnodes, in_line );
 
 					if ( arg_debug >= Log_Informational_2 )
 					{
 						System.out.println( "line_overlapnodes_string: " + line_overlapnodes_string );
 					}
 					
 					if ( line_overlapnodes_string.equals( "" ))
 					{
 /* ------------------------------------------------------------------------------
  * Do nothing.  We've already set "line_overlapnodes = arg_overlapnodes" above 
  * ------------------------------------------------------------------------------ */
 					}
 					else
 					{
 						try
 						{
 							line_overlapnodes = ( Integer.valueOf( line_overlapnodes_string ) > 0 );
 						}
 						catch( Exception ex )
 						{
 /* ------------------------------------------------------------------------------
  * Do nothing.  We've already set "line_overlapnodes = arg_overlapnodes" above 
  * ------------------------------------------------------------------------------ */
 						}
 					}
 					
 					int comma_pos = line_bbox.indexOf( "," );
 					int old_comma_pos = 0;
 					
 					if ( comma_pos > 0 )
 					{ // found min lon
 						line_min_lon_string = line_bbox.substring( 0, comma_pos );
 						
 						if ( arg_debug >= Log_Informational_1 )
 						{
 							System.out.println( "line_min_lon: " + line_min_lon_string );
 						}
 
 						old_comma_pos = comma_pos;
 						comma_pos = line_bbox.indexOf( ",", comma_pos+1 );
 
 						if ( comma_pos > 0 )
 						{ // found min lat
 							line_min_lat_string = line_bbox.substring( old_comma_pos+1, comma_pos );
 							
 							if ( arg_debug >= Log_Informational_1 )
 							{
 								System.out.println( "line_min_lat: " + line_min_lat_string );
 							}
 
 							
 							old_comma_pos = comma_pos;
 							comma_pos = line_bbox.indexOf( ",", comma_pos+1 );
 
 							if ( comma_pos > 0 )
 							{ // found max lon; what's left must be max lat
 								line_max_lon_string = line_bbox.substring( old_comma_pos+1, comma_pos );
 								
 								if ( arg_debug >= Log_Informational_1 )
 								{
 									System.out.println( "line_max_lon: " + line_max_lon_string );
 								}
 
 
 								old_comma_pos = comma_pos;
 								line_max_lat_string = line_bbox.substring( old_comma_pos+1 );
 								
 								if ( arg_debug >= Log_Informational_1 )
 								{
 									System.out.println( "line_max_lat: " + line_max_lat_string );
 								}
 							} // max lon found
 							else
 							{
 								if ( arg_debug >= Log_Error )
 								{
 									System.out.println( "3rd comma_pos: " + comma_pos );
 									line_bbox = "";
 								}
 							} // no max lon
 						} // min lat found
 						else
 						{
 							if ( arg_debug >= Log_Error )
 							{
 								System.out.println( "2nd comma_pos: " + comma_pos );
 								line_bbox = "";
 							}
 						} // no min lat
 					} // min lon found
 					else
 					{
 						if ( arg_debug >= Log_Error )
 						{
 							System.out.println( "1st comma_pos: " + comma_pos );
 							line_bbox = "";
 						}
 					} // no min lon
 
 /* ------------------------------------------------------------------------------
  * Check that the user has entered something on a line - otherwise a blank line
  * will just try and download "all changesets"
  * ------------------------------------------------------------------------------ */
 					if (( line_display_name.length() != 0 ) ||
 					    ( line_uid.length()          != 0 ) ||
 					    ( line_id.length()           != 0 ) ||
 					    ( line_time.length()         != 0 ) ||
 					    ( line_bbox.length()         != 0 ))
 					{
 						if ( line_display_name.length() == 0 )
 						{
 							line_display_name = arg_display_name;
 						}
 						
 						if ( line_uid.length() == 0 )
 						{
 							line_uid = arg_uid;
 						}
 						
 						if ( line_id.length() == 0 )
 						{
 							line_id = arg_id;
 						}
 						
 						if ( line_time.length() == 0 )
 						{
 							line_time = arg_time;
 						}
 	
 						if ( line_bbox.length() == 0 )
 						{
 							line_bbox = arg_bbox;
 							line_min_lat_string = arg_min_lat_string; 
 							line_min_lon_string = arg_min_lon_string;
 							line_max_lat_string = arg_max_lat_string;
 							line_max_lon_string = arg_max_lon_string;
 						}
 	
 						if ( line_download_changeset.length() == 0 )
 						{
 							line_download_changeset = arg_download_changeset;
 						}
 	
 /* ------------------------------------------------------------------------------------------------------------
  * Now call the API with whatever parameters we have from this line in the input file or the command line 
  * ------------------------------------------------------------------------------------------------------------ */
 						if ( line_time.length() == 0 )
 						{
 							if ( line_display_name.length() == 0 )
 							{
 								if ( line_uid.length() == 0 )
 								{
 									if ( line_id.length() == 0 )
 									{
 										if ( arg_debug >= Log_Informational_2 )
 										{
 											System.out.println( "None of display_name, uid, id or time passed on this line" );
 										}
 									}
 									else
 									{
 										process_id( line_id, 
 												line_min_lat_string, line_min_lon_string, line_max_lat_string, line_max_lon_string, 
 												line_download_changeset, line_building, line_overlapnodes, line_download_nodes );
 									}
 								}
 								else
 								{
 									process_uid( line_uid, 
 											line_min_lat_string, line_min_lon_string, line_max_lat_string, line_max_lon_string, 
 											line_download_changeset, line_building, line_overlapnodes, line_download_nodes );
 								}
 							}
 							else
 							{
 								process_display_name( line_display_name, 
 										line_min_lat_string, line_min_lon_string, line_max_lat_string, line_max_lon_string, 
 										line_download_changeset, line_building, line_overlapnodes, line_download_nodes );
 							}
 						} // no time argument passed
 						else
 						{
 							if ( line_display_name.length() == 0 )
 							{
 								if ( line_uid.length() == 0 )
 								{
 									process_time( line_time, 
 											line_min_lat_string, line_min_lon_string, line_max_lat_string, line_max_lon_string, 
 											line_download_changeset, line_building, line_overlapnodes, line_download_nodes );
 								}
 								else
 								{
 									process_uid_and_time( line_uid, line_time, 
 											line_min_lat_string, line_min_lon_string, line_max_lat_string, line_max_lon_string, 
 											line_download_changeset, line_building, line_overlapnodes, line_download_nodes );
 								}
 							}
 							else
 							{
 								process_display_name_and_time( line_display_name, line_time, 
 										line_min_lat_string, line_min_lon_string, line_max_lat_string, line_max_lon_string, 
 										line_download_changeset, line_building, line_overlapnodes, line_download_nodes );
 							}
 						}
 					} // Some senesible parameters passed in an input file line
 					else
 					{
 						if ( arg_debug >= Log_Informational_1 )
 						{
 							System.out.println( "No data to process from line in input file" );
 						}
 					}
 				}
 			}
 		}
 		
 
 		
 /* ------------------------------------------------------------------------------
  * If we've been writing to an output file, close it.
  * ------------------------------------------------------------------------------ */
 		if ( !arg_out_file.equals( "" ))
 		{
 			myOutputStream.close();
 		}
 	} // main
 
 
 /* ------------------------------------------------------------------------------
  * The "help" text is sent straight to "standard out".
  * ------------------------------------------------------------------------------ */
 private static void show_help() 
 	{
 		System.out.println( "" );
 		System.out.println( "Changeset1" );
 		System.out.println( "==========" );
 		System.out.println( "Process OSM's changeset feed, and check node overlaps within a bounding box." );
 		System.out.println( "" );
 		System.out.println( "Usage example:" );
 		System.out.println( "java Changeset1 -time=\"2013-11-04T20:53\" -debug=5 -display_name=\"SomeoneElse\" -bbox=-2.123,52.809,-0.331,53.521 -output=example_out.txt" );
 		System.out.println( "" );
 		System.out.println( "This looks for changesets by the user named \"SomeoneElse\" in the specified bounding box since the specified time." );
 		System.out.println( "" );
 		System.out.println( "See https://github.com/SomeoneElseOSM/Changeset1/blob/master/README.md for more details." );
 		System.out.println( "" );
 	}
 }
