 /**
  * Copyright (C) 2015  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.command;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.lh.dmlj.schema.AreaProcedureCallSpecification;
 import org.lh.dmlj.schema.Procedure;
 import org.lh.dmlj.schema.ProcedureCallSpecification;
 import org.lh.dmlj.schema.RecordProcedureCallSpecification;
 import org.lh.dmlj.schema.Schema;
 import org.lh.dmlj.schema.SchemaArea;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Set;
 import org.lh.dmlj.schema.SystemOwner;
 import org.lh.dmlj.schema.editor.command.helper.RemovableMemberRole;
 
 public class DeleteIndexCommand extends ModelChangeBasicCommand {	
 	
 	private Schema schema;
 	private Set set;
 	
 	private SystemOwner systemOwner;
 	
 	private SchemaArea area;
 	private RemovableMemberRole memberRoleToRemove;		
 	private List<Procedure> obsoleteProcedures = new ArrayList<>();	
 	private List<String> procedureNames = new ArrayList<>();
 	private SchemaRecord record;	
 	
 	private int areaInSchemaIndex;
 	private int areaSpecificationIndex;
 	private Map<String, Integer> procedureInSchemaIndexes = new HashMap<>();
 	private int setInSchemaIndex;
 	
 	
 	public DeleteIndexCommand(SystemOwner systemOwner) {
 		super("Delete index");
 		this.systemOwner = systemOwner;		
 	}		
 
 	@Override
 	public void execute() {
 		rememberState();
 		deleteIndex();
 	}
 	
 	private void rememberState() {		
 		rememberSchema();
 		rememberAreaData();
 		rememberRecordData();
 		rememberSetData();
 		rememberMembershipData();
 	}
 
 	private void rememberSchema() {
 		schema = systemOwner.getSet().getSchema();		
 	}
 	
 	private void rememberAreaData() {	
 		area = systemOwner.getAreaSpecification().getArea();
 		rememberAreaProcedures();
 	}	
 	
 	private void rememberAreaProcedures() {
 		for (AreaProcedureCallSpecification callSpec : area.getProcedures()) {
 			Procedure procedure = callSpec.getProcedure();
 			procedureNames.add(procedure.getName());			
 			boolean procedureIsObsolete = true;
 			for (ProcedureCallSpecification aCallSpec : procedure.getCallSpecifications()) {
 				if (aCallSpec instanceof RecordProcedureCallSpecification ||
 					((AreaProcedureCallSpecification) aCallSpec).getArea() != area) {
 					
 					procedureIsObsolete = false;
 					break;
 				}
 			}
 			if (procedureIsObsolete) {
 				int i = schema.getProcedures().indexOf(procedure);
 				procedureInSchemaIndexes.put(procedure.getName(), Integer.valueOf(i));
 				obsoleteProcedures.add(procedure);
 			}
 		}
 		Collections.sort(obsoleteProcedures, new Comparator<Procedure>() {
 			@Override
 			public int compare(Procedure procedure1, Procedure procedure2) {
 				int i = procedureInSchemaIndexes.get(procedure1.getName()).intValue();
 				int j = procedureInSchemaIndexes.get(procedure2.getName()).intValue();				
 				return i - j;
 			}			
 		});		
 		areaInSchemaIndex = schema.getAreas().indexOf(area);
 		areaSpecificationIndex = 
 			area.getAreaSpecifications().indexOf(systemOwner.getAreaSpecification());
 	}
 	
 	private void rememberRecordData() {
 		record = systemOwner.getSet().getMembers().get(0).getRecord();
 	}
 	
 	private void rememberSetData() {
 		set = systemOwner.getSet();
 		setInSchemaIndex = schema.getSets().indexOf(set);		
 	}
 	
 	private void rememberMembershipData() {		
 		memberRoleToRemove = 
 			new RemovableMemberRole(set.getMembers().get(0), 
 									Arrays.asList(systemOwner.getDiagramLocation()));				
 	}
 			
 	private void deleteIndex() {				
 		Assert.isTrue(record.getViaSpecification() == null ||
 					  record.getViaSpecification().getSet() != systemOwner.getSet(), 
 					  "record " + record.getName() + " is stored VIA index " + 
 					  systemOwner.getSet().getName());				
 		removeAreaSpecification();
 		if (area.getAreaSpecifications().isEmpty()) {
			// TODO don't remove the area here; we've got a dedicated command to remove an area and
			// whenever an area becomes obsolete after removing an index, a compound command should
			// be the answer
 			removeArea();
 		}		
 		removeMembershipData();
 		removeSet();					
 	}
 	
 	private void removeAreaSpecification() {
 		area.getAreaSpecifications().remove(systemOwner.getAreaSpecification());
 	}
 	
 	private void removeArea() {
 		schema.getAreas().remove(area);
 		removeProcedureCallSpecifications();
		// TODO don't remove any obsolete procedures here; we've got a dedicated command to remove a 
		// procedure and whenever a procedure becomes obsolete after removing an index, a compound 
		// command should be the answer
 		removeObsoleteProcedures();		
 	}
 	
 	private void removeProcedureCallSpecifications() {
 		for (AreaProcedureCallSpecification callSpec : area.getProcedures()) {
 			callSpec.setProcedure(null);
 		}		
 	}
 
 	private void removeObsoleteProcedures() {
 		for (String procedureName : procedureInSchemaIndexes.keySet()) {
 			Procedure procedure = schema.getProcedure(procedureName);
 			procedure.setSchema(null);
 		}
 	}
 
 	private void removeMembershipData() {
 		memberRoleToRemove.remove();
 	}	
 	
 	private void removeSet() {
 		schema.getSets().remove(set);
 	}	
 	
 	@Override
 	public void redo() {
 		deleteIndex();
 	}
 
 	@Override
 	public void undo() {
 		restoreIndex();
 	}
 	
 	private void restoreIndex() {		
 		if (area.getSchema() == null) {
 			restoreArea();
 		}				
 		restoreRemovedProcedures();				
 		restoreAreaSpecification();		
 		restoreMembershipData();
 		restoreSet();						
 	}
 
 	private void restoreArea() {
 		schema.getAreas().add(areaInSchemaIndex, area);
 	}
 
 	private void restoreRemovedProcedures() {
 		for (Procedure procedure : obsoleteProcedures) {
 			int i = procedureInSchemaIndexes.get(procedure.getName()).intValue();
 			schema.getProcedures().add(i, procedure);
 		}		
 		restoreRemovedProcedureCallSpecifications();		
 	}
 
 	private void restoreRemovedProcedureCallSpecifications() {
 		for (int i = 0; i < area.getProcedures().size(); i++) {
 			AreaProcedureCallSpecification callSpec = area.getProcedures().get(i);
 			Procedure procedure = schema.getProcedure(procedureNames.get(i));
 			callSpec.setProcedure(procedure);			
 		}		
 	}
 
 	private void restoreAreaSpecification() {
 		area.getAreaSpecifications().add(areaSpecificationIndex, 
 										 systemOwner.getAreaSpecification());
 	}
 
 	private void restoreMembershipData() {
 		memberRoleToRemove.restore();
 	}
 
 	private void restoreSet() {
 		schema.getSets().add(setInSchemaIndex, set);
 	}
 	
 }
