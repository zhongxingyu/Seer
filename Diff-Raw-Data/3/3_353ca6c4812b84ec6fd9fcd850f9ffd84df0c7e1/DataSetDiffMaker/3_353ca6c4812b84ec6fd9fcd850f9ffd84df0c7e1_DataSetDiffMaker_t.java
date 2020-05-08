 /**
  * 
  */
 package com.gooddata.connector;
 
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_ATTRIBUTE;
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_CONNECTION_POINT;
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_DATE;
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_FACT;
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_LABEL;
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_REFERENCE;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 
 class DataSetDiffMaker {
     private static Logger l = Logger.getLogger(DataSetDiffMaker.class);
 
     private final ColumnsSet remote = new ColumnsSet();
 	private final ColumnsSet local  = new ColumnsSet();
 	private final List<SourceColumn> deletedColumns = new ArrayList<SourceColumn>();
 	
 	DataSetDiffMaker(GdcRESTApiWrapper gd, SLI sli, SourceSchema ss) {
 		SourceColumn sourceConnectionPoint = null;
 		Map<String, SourceColumn> sourceDateColumns = new HashMap<String, SourceColumn>();
 	    String remoteConnectionPointName = null;
 
 		for (SourceColumn sc : ss.getColumns()) {
 			sc.setName(sc.getName());
 			sc.setReference(sc.getReference());
 			sc.setSchemaReference(sc.getSchemaReference());
 			local.columns.add(sc);
 			if (LDM_TYPE_CONNECTION_POINT.equals(sc.getLdmType())) {
 				sourceConnectionPoint = sc;
 			} else if (LDM_TYPE_DATE.equals(sc.getLdmType())) {
 				sourceDateColumns.put(sc.getName(), sc);
 			}
 		}
 		Map<String, String> dateColumns = new HashMap<String, String>(); // maps date dim names to column names
 		String datasetId = sli.getId().replaceAll("^dataset\\.", "");
 		String factPrefix = N.FCT_PFX + datasetId + "." + N.FCT_PFX;
 		String cpPrefix   = N.FCT_PFX + datasetId + "." + N.NM_PFX;
 		String datePrefix = N.FCT_PFX + datasetId + "." + N.DT_PFX;
 		String timeFactPrefix = N.FCT_PFX + datasetId + "." + N.TM_PFX;
 		String timeAttrPrefix = "d_time_second_of_day_";
 		String lookupPrefix = N.LKP_PFX;
 		final List<Column> sliColumns = gd.getSLIColumns(sli.getUri());
 					
 		for (final Column c : sliColumns) {
 			final String ldmType,
 			             name;
 			String reference = null,
 			       schemaReference = null;
 			final int prefixLen;
 			boolean remoteColumn = true;
 			boolean dateFact = false,
 			        timeFact = false;
 			
 			// fields populating a fact table column
 			if (c.getName().startsWith(factPrefix)) {   // FACT
 				prefixLen = factPrefix.length();
 				ldmType = LDM_TYPE_FACT;
 				name = c.getName().substring(prefixLen).replaceAll("\\..*$", "");
 			} else if (c.getName().startsWith(datePrefix)) { // DATE
 				prefixLen = datePrefix.length();
 				if (c.getName().endsWith("_" + N.ID)) {
 				    ldmType = LDM_TYPE_DATE;
 				    String sourceName = c.getName().substring(prefixLen).replaceAll(".*\\." + N.DT, "").replaceAll("_id", "");
 				    name = sourceName; // + "_" + N.DT;
 	                for (String pop : c.getPopulates()) {
 	                    // HACK - where is this naming convention defined?
 	                    if (pop.contains(".date.")) { // date attribute
 	                        schemaReference = pop.replaceAll("\\.date\\..*$", "");
 	                        dateColumns.put(schemaReference, sourceName);
 	                    } else if (pop.matches(".*\\.[^\\.]*$")) { // old date dimension, identifiers like "${schema}.${randomString}"
 	                        schemaReference = pop.replaceAll("\\.[^\\.]*$", "");
 	                        dateColumns.put(schemaReference, sourceName);
 	                    } else {
 	                        l.warn(String.format("Cannot determine the ldm type for field '%s'", name));
 	                        continue;
 	                    }
 	                }
 				} else {
 				    ldmType = LDM_TYPE_FACT;
 				    name = c.getName().substring(prefixLen).replaceAll(".*\\." + N.DT, "") + "_" + N.DT;
 				    dateFact = true;
 				}
 			} else if (c.getName().startsWith(timeFactPrefix)) { // TIME
                 prefixLen = timeFactPrefix.length();
                 ldmType = LDM_TYPE_FACT;
                 name = c.getName().substring(prefixLen).replaceAll(".*\\." + N.TM, "") + "_" + N.TM;
                 timeFact = true;
             } else if (c.getName().startsWith(timeAttrPrefix)) {
                 prefixLen = timeAttrPrefix.length();
                 name = c.getName().substring(prefixLen).replaceAll("\\..*$", "");
                 schemaReference = name;
                 ldmType = LDM_TYPE_REFERENCE;
                 timeFact = true;
                 remote.timeDimensions.add(schemaReference);
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
 					ldmType = LDM_TYPE_CONNECTION_POINT;
 				} else {
 					ldmType = LDM_TYPE_LABEL;
 					reference = sourceConnectionPoint.getName();
 				}
 					
 			// fields populating a lookup table column
 			} else if (c.getName().startsWith(lookupPrefix)) {
			    String tmp = c.getName().replaceAll("^" + lookupPrefix, "");
			    String remoteSchemaReference = tmp.replaceAll("_[^_]*\\..*$", "");
 				prefixLen = (lookupPrefix + remoteSchemaReference + "_").length();
 				String nameAndRemoteField = c.getName().substring(prefixLen);
 				String referenceName = nameAndRemoteField.replaceAll("\\..*$", "");
 				name = nameAndRemoteField.replaceAll(".*\\." + N.NM_PFX, "");
 				if ((remoteSchemaReference != null) && !remoteSchemaReference.equals(datasetId)) {
 				    schemaReference = remoteSchemaReference;
 				}
 				if (name.equals(referenceName)) {
 					ldmType = LDM_TYPE_ATTRIBUTE;
 				} else {
 					ldmType = LDM_TYPE_LABEL;
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
 			final SourceColumn column = new SourceColumn(name, ldmType, name); // title (3rd) arg is ignored in this use case
 
 			column.setDateFact(dateFact);
 		    column.setTimeFact(timeFact);
 
 			if (reference != null) {
 				column.setReference(reference);
 			}
 			if (schemaReference != null) {
 				column.setSchemaReference(schemaReference);
 			}
 			if (remoteColumn) {
 			    remote.columns.add(column);
 			}
 			if (!contains(local.columns, column)) {
 			    final boolean cond = LDM_TYPE_REFERENCE.equals(column.getLdmType()) && !column.isTimeFact();
 			    if (cond) {
 			        l.warn(String.format(
 			                "Reference from %s to %s.%s has been removed locally. Removing remote references is not supported yet. Skipping.",
 			                 datasetId, column.getSchemaReference(), column.getReference()));
 	            } else {
 	                deletedColumns.add(column);
 	            }
 	        }
 		}
 		fixTimeDimensions(remote, dateColumns);
 		if (sourceConnectionPoint != null && remoteConnectionPointName == null) {
 			throw new UnsupportedOperationException("Adding a new connection point is not supported yet.");
 		}
 	}
 
 	private void fixTimeDimensions(ColumnsSet columns, Map<String,String> dim2column) {
 	    for (final SourceColumn sc : columns.columns) {
 	        if (LDM_TYPE_REFERENCE.equals(sc.getLdmType()) && sc.isTimeFact()) {
 	            String columnName = dim2column.get(sc.getName());
 	            if (columnName == null) {
 	                throw new IllegalStateException(String.format("Time dimension '%s' without a corresponding date field", sc.getName()));
 	            }
 	            sc.setName(columnName + "_id");
 	        }
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
         if (SourceColumn.LDM_TYPE_REFERENCE.equals(column.getLdmType())) {
             for (final SourceColumn sc : sourceColumns) {
                 if ("REFERENCE".equals(sc.getLdmType())) {
                     if (sc.getSchemaReference().equals(column.getSchemaReference())) {
                         if ((sc.isDateFact() && column.isDateFact()) || (sc.isTimeFact() && column.isTimeFact())) {
                             return true;
                         } else if (sc.getReference() != null && sc.getReference().equals(column.getReference())) {
                             return true;
                         }
                     }
                 }
             }
             return false;
         }
         return sourceColumns.contains(column);
     }
 
     List<SourceColumn> findNewColumns() {
 		return findDiff(local, remote);
 	}
 	
 	List<SourceColumn> findDeletedColumns() {
 		return deletedColumns;
 	}
 	
 	List<SourceColumn> findDiff(ColumnsSet src, ColumnsSet tgt) {
 		final List<SourceColumn> result = new ArrayList<SourceColumn>();
 		for (final SourceColumn sc : src.columns) {
 //		    if (LDM_TYPE_REFERENCE.equals(sc.getLdmType()) && sc.isTimeFact()) {
 //		        if (!tgt.timeDimensions.contains(sc.getSchemaReference())) {
 //		            result.add(sc);
 //		        }
 //		    }
 		    if (!contains(tgt.columns, sc)) {
 				result.add(sc);
 			}
 		}
 		return result;
 	}
 
 	Set<SourceColumn> getLocalColumns() {
 	    return local.columns;
 	}
 
 	private static class ColumnsSet {
 	    public final Set<SourceColumn> columns = new HashSet<SourceColumn>();
 	    public final Set<String> timeDimensions = new HashSet<String>();
 	}
 }
