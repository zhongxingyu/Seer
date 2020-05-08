 /* Modeling - Application to model threats.
  *
  * Copyright (C) 2010  INBio ( Instituto Nacional de Biodiversidad )
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.inbio.modeling.core.manager.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.inbio.modeling.core.dto.CategoryDTO;
 import org.inbio.modeling.core.dto.GrassLayerDTO;
 import org.inbio.modeling.core.manager.FileManager;
 import org.springframework.dao.EmptyResultDataAccessException;
 
 /**
  *
  * @author asanabria
  */
 public class FileManagerImpl implements FileManager {
 
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private String tempHome;
 	private String fileName;
 	private String fileExtension;
 	private String layerHome;
 
 
 	@Override
 	/**
 	 * @see org.inbio.modeling.core.dao.LayerDAO#listLayerHomeFolder()
 	 */
 	public List<String> listLayerHomeFolder() throws EmptyResultDataAccessException{
 
 
 		String childrenName = null;
 		List<String> names = null;
 
 			File dir = new File(this.layerHome);
 			String[] children = dir.list(filter);
 
 			if (children == null) {
 				throw new EmptyResultDataAccessException(tempHome, 1);
 			} else {
 				names = new ArrayList<String>();
 				for (int i = 0; i < children.length; i++) {
 
 					//deletes the extension.
 					childrenName = children[i];
 					childrenName = childrenName.replace(".shp", "");
 
 					// Add the file name to ArrayList
 					names.add(childrenName);
 				}
 
 			}
 
 		return names;
 	}
 
 	static FilenameFilter filter = new FilenameFilter() {
 		@Override
 		public boolean accept(File dir, String name) {
 			return name.endsWith(".shp");
 		}
 	};
 
 	@Override
 	/**
 	 * @see org.inbio.modeling.core.manager.FileManager#writeReclasFile(GrassLayerDTO layer, Long suffix)
 	 */
 	public void writeReclasFile(GrassLayerDTO layer, Long suffix) throws IOException{
 
 		FileWriter fstream = null;
 		BufferedWriter out = null;
 		String[] value = null;
 		String line = "";
 		int index = 1;
 
 		try {
 
 			fstream = new FileWriter(tempHome+fileName+suffix+fileExtension);
 			out = new BufferedWriter(fstream);
 
 			for(CategoryDTO category : layer.getCategories()){
 
 				if(this.validateFormat(category)){
 
 					if(category.isInterval()){
 						value = category.getValue().split("-");
 						category.setValue(value[0] + " thru " + value[1]);
 					}
 
 					line = category.getValue() +
 						" = " + index + " " + category.getDescription() +"\n";
 					index++;
 					out.write(line);
 				}
 			}
			out.write("* = 0 NULL\n");
 
 			out.close();
 			fstream.close();
 		} catch (IOException e) {
 			throw new IOException(tempHome);
 		}
 	}
 
 	private boolean validateFormat(CategoryDTO category){
 
 		if(category == null)
 			return false;
 
 		boolean result = false;
 		String temp[] = null;
 		String value = category.getValue();
 		value = value.replace(" ", "");
 
 		if(value.matches("\\d+")){
 			result = true;
 		} if(value.matches("\\d+(,\\d+)+")){
 			result = true;
 			value = value.replace(",", " ");
 			category.setValue(value);
 		} else if(value.matches("\\d+\\.?\\d*-\\d+\\.?\\d*")){
 			result = true;
 			category.setInterval(true);
 		}
 
 		return result;
 	}
 
 	/* getters y setters */
 	public String getTempHome() {
 		return tempHome;
 	}
 
 	public void setTempHome(String tempHome) {
 		this.tempHome = tempHome;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public String getFileExtension() {
 		return fileExtension;
 	}
 
 	public void setFileExtension(String fileExtension) {
 		this.fileExtension = fileExtension;
 	}
 
 	public String getLayerHome() {
 		return layerHome;
 	}
 
 	public void setLayerHome(String layerHome) {
 		this.layerHome = layerHome;
 	}
 }
