 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.example.richbean;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 
 import uk.ac.gda.client.experimentdefinition.ExperimentObject;
 import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
 
 public class ExampleExperimentObject extends ExperimentObject {
 
 	public static final String SCANBEANTYPE = "Scan";
 	
 	@Override
 	public void createFilesFromTemplates() {
 		final IFolder folder = getFolder();
 		XMLCommandHandler xmlCH = new XMLCommandHandler();
 		
 		// just one file in each experiment at the moment
 		IFile scanFile = xmlCH.doTemplateCopy(folder, "ExampleExpt_Parameters.xml");
 		typeToFileMap.put(SCANBEANTYPE, scanFile.getName());
 	}
 	
 	@Override
 	public String getOutputPath() {
 		return null;
 	}
 
 	@Override
 	public long estimateTime() throws Exception {
 		return 0;
 	}
 	
 	/**
 	 * This is used when marshalling/unmarshalling this object to a .scan file
 	 */
 	@Override
 	public String toPersistenceString() {
 		final StringBuilder buf = new StringBuilder(getRunName());
 		buf.append(" ");
 		buf.append(getScanFileName());
 		buf.append(" ");
 		buf.append(getNumberRepetitions());
 		return buf.toString();
 	}
 	
 	public void setScanFileName(String string) {
 		if (string.indexOf(' ') > -1)
 			throw new RuntimeException("Scan name cannot contain a space.");
 		getTypeToFileMap().put(SCANBEANTYPE, string);
 //		notifyListeners("ScanFileName");
 	}
 	
 	public String getScanFileName() {
 		return getTypeToFileMap().get(SCANBEANTYPE);
 	}
 
 	@Override
 	public String getCommandString() throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public String getCommandSummaryString() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void parseEditorFile(String fileName) throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
