 /**
  * 
  */
 package com.gooddata.connector;
 
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
 
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.util.StringUtil;
 
 class DataSetDiffMaker {
     private static Logger l = Logger.getLogger(DataSetDiffMaker.class);
 
     private final Set<SourceColumn> remoteColumns  = new HashSet<SourceColumn>();
 	final Set<SourceColumn> sourceColumns  = new HashSet<SourceColumn>();
 	private final List<SourceColumn> deletedColumns = new ArrayList<SourceColumn>();
 	
 	DataSetDiffMaker(GdcRESTApiWrapper gd, SLI sli, SourceSchema ss) {
 		SourceColumn sourceConnectionPoint = null;
 		Map<String, SourceColumn> sourceDateColumns = new HashMap<String, SourceColumn>();
 	    String remoteConnectionPointName = null;
 
 		for (SourceColumn sc : ss.getColumns()) {
 			sc.setName(StringUtil.toIdentifier(sc.getName()));
 			sc.setReference(StringUtil.toIdentifier(sc.getReference()));
 			sc.setSchemaReference(StringUtil.toIdentifier(sc.getSchemaReference()));
 			sourceColumns.add(sc);
 			if (SourceColumn.LDM_TYPE_CONNECTION_POINT.equals(sc.getLdmType())) {
 				sourceConnectionPoint = sc;
 			} else if (SourceColumn.LDM_TYPE_DATE.equals(sc.getLdmType())) {
 				sourceDateColumns.put(sc.getName(), sc);
 			}
 		}
 		String datasetId = sli.getId().replaceAll("^dataset\\.", "");
 		String factPrefix = N.FCT_PFX + datasetId + "." + N.FCT_PFX;
 		String cpPrefix   = N.FCT_PFX + datasetId + "." + N.NM_PFX;
 		String datePrefix   = N.FCT_PFX + datasetId + "." + N.DT_PFX;
 		String lookupPrefix = N.LKP_PFX + datasetId + "_";
 		final List<Column> sliColumns = gd.getSLIColumns(sli.getUri());
 					
 		for (final Column c : sliColumns) {
 			final String ldmType,
 			             name;
 			String reference = null,
 			       schemaReference = null;
 			final int prefixLen;
 			boolean remoteColumn = true;
 			
 			// fields populating a fact table column
 			if (c.getName().startsWith(factPrefix)) {   // FACT
 				prefixLen = factPrefix.length();
 				ldmType = SourceColumn.LDM_TYPE_FACT;
 				name = c.getName().substring(prefixLen).replaceAll("\\..*$", "");
 			} else if (c.getName().startsWith(datePrefix)) { // DATE
 				prefixLen = datePrefix.length();
 				ldmType = SourceColumn.LDM_TYPE_DATE;
 				name = c.getName().substring(prefixLen).replaceAll(".*\\." + N.DT, "").replaceAll("_id$", "");
 				for (String pop : c.getPopulates()) {
					// HACK - where is this naming convention defined?
					if (pop.startsWith("dt.") && pop.endsWith(name)) { // date fact
 					    remoteColumn = false; // date attribute provides more information than the date fact
					} else if (pop.contains(".date.")) { // date attribute
						schemaReference = pop.replaceAll("\\.date\\..*$", "");
					} else {
						l.warn(String.format("Cannot determine the ldm type for field '%s'", name));
						continue;
 					}
 				}
 				
 			} else if (c.getName().startsWith(cpPrefix)) {  // CONNECTION_POINT (or its LABEL)
 				prefixLen = cpPrefix.length();
 				name = c.getName().substring(prefixLen).replaceAll(".*\\." + N.NM_PFX, "");
 				// we don't support dropping connection points
 				// so this field may be either the same connection 
 				// point as in the local file or a label of it
 				if (sourceConnectionPoint == null) {
 					throw new UnsupportedOperationException("Dropping a connection point is not supported.");
 				}
 				if (name.equals(sourceConnectionPoint.getName())) {
 					remoteConnectionPointName = name;
 					ldmType = SourceColumn.LDM_TYPE_CONNECTION_POINT;
 				} else {
 					ldmType = SourceColumn.LDM_TYPE_LABEL;
 					reference = sourceConnectionPoint.getName();
 				}
 					
 			// fields populating a lookup table column
 			} else if (c.getName().startsWith(lookupPrefix)) {
 				prefixLen = lookupPrefix.length();
 				String nameAndRemoteField = c.getName().substring(prefixLen);
 				String referenceName = nameAndRemoteField.replaceAll("\\..*$", "");
 				name = nameAndRemoteField.replaceAll(".*\\." + N.NM_PFX, "");
 				if (name.equals(referenceName)) {
 					ldmType = SourceColumn.LDM_TYPE_ATTRIBUTE;
 				} else {
 					ldmType = SourceColumn.LDM_TYPE_LABEL;
 					reference = referenceName;
 				}
 				
 			// references to lookups from other data sets
 			} else if (c.getName().startsWith(N.LKP_PFX)) {
 				continue; // we cannot detect other changes than facts and attributes yet
 
 			// references to fact tables of other data sets
 			} else if (c.getName().startsWith(N.FCT_PFX)) {
 			    name = null; // the name of a reference field cannot be 
 			    schemaReference = c.getName().replaceAll("^" + N.FCT_PFX, "").replaceAll("\\..*$", "");
 			    if (c.getPopulates() == null || c.getPopulates().length == 0) {
 			        throw new IllegalStateException(String.format(
 			                "'%s' field in dataset '%s' does not populate anything", c.getName(), datasetId));
 			    }
 			    reference = c.getPopulates()[0].replaceAll("^label\\.[^\\.]*\\.", "").replaceAll("\\..*$", "");
 			    ldmType = "REFERENCE";
 			// unknown stuff
 			} else {
 				throw new IllegalStateException(String.format(
 						"Unsupported naming convention: '%s' field in dataset '%s",
 						c.getName(), datasetId));
 			}
 			if ("DATE".equals(ldmType) && schemaReference == null) {
 			    continue; // date fact, let's skip it - all information will be provided by the DATE attribute
 			}
 			final SourceColumn column = new SourceColumn(name, ldmType, name); // title (3rd) arg is ignored in this use case
 			if (reference != null) {
 				column.setReference(reference);
 			}
 			if (schemaReference != null) {
 				column.setSchemaReference(schemaReference);
 			}
 			if (remoteColumn) {
 			    remoteColumns.add(column);
 			}
 			if (!contains(sourceColumns, column)) {
 			    if ("REFERENCE".equals(column.getLdmType())) {
 			        l.warn(String.format(
 			                "Reference from %s to %s.%s has been removed locally. Removing remoted references is not supported yet. Skipping.",
 			                 datasetId, column.getSchemaReference(), column.getReference()));
 	            } else {
 	                deletedColumns.add(column);
 	            }
 	        }		}
 		if (sourceConnectionPoint != null && remoteConnectionPointName == null) {
 			throw new UnsupportedOperationException("Adding a new connection point is not supported yet.");
 		}
 	}
 
 	/**
 	 * Searches the given column, only reference and schemaReference fields are taken into
 	 * consideration (NOT name!) in the case of REFERENCE ldmType
 	 * 
 	 * @param sourceColumns set of source columns to be searched
 	 * @param column the reference column
 	 * @return
 	 */
 	private static boolean contains(Set<SourceColumn> sourceColumns, SourceColumn column) {
         if (!"REFERENCE".equals(column.getLdmType())) {
             return sourceColumns.contains(column);
         }
         for (final SourceColumn sc : sourceColumns) {
             if ("REFERENCE".equals(sc.getLdmType())) {
                 if (sc.getSchemaReference().equals(column.getSchemaReference())
                         && sc.getReference().equals(column.getReference()))
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 
     List<SourceColumn> findNewColumns() {
 		return findDiff(sourceColumns, remoteColumns);
 	}
 	
 	List<SourceColumn> findDeletedColumns() {
 		return deletedColumns;
 	}
 	
 	List<SourceColumn> findDiff(Set<SourceColumn> src, Set<SourceColumn> tgt) {
 		final List<SourceColumn> result = new ArrayList<SourceColumn>();
 		for (final SourceColumn sc : src) {
 			if (!contains(tgt, sc)) {
 				result.add(sc);
 			}
 		}
 		return result;
 	}
 	
 }
