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
 package org.inbio.modeling.web.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.inbio.modeling.core.dto.CategoryDTO;
 import org.inbio.modeling.core.layer.LayerType;
 import org.inbio.modeling.core.manager.GrassManager;
 import org.inbio.modeling.web.form.ChooseColumnForm;
 import org.inbio.modeling.web.form.converter.FormDTOConverter;
 import org.inbio.modeling.web.form.util.Category;
 import org.inbio.modeling.web.form.util.Layer;
 import org.inbio.modeling.web.session.CurrentInstanceData;
 import org.inbio.modeling.web.session.SessionUtils;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractFormController;
 
 
 /**
  *
  * @author asanabria
  */
 public class ColumnController extends AbstractFormController {
 
 	/** manager to comunicate with grass gis software */
 	private GrassManager grassManagerImpl;
     private IntervalsController intervalsController;
 
 	@Override
 	protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		return super.handleInvalidSubmit(request, response);
 	}
 
 	/**
 	 * If hit the /columns.html address directly you will be redirected to index
 	 * @param request
 	 * @param response
 	 * @param errors
 	 * @return
 	 * @throws Exception
 	 */
 	@Override
 	protected ModelAndView showForm(HttpServletRequest request
 		, HttpServletResponse response
 		, BindException errors) {
 		CurrentInstanceData currentInstanceData = null;
 		ChooseColumnForm cForm = null;
 		Long currentSessionId = null;
 		HttpSession session = null;
 		List<Layer> layers = null;
 		ModelAndView model = null;
 
 		// retrieve the session Information.
 		session = request.getSession();
 
 
 		currentInstanceData = SessionUtils.isSessionAlive(session);
 
 		if(currentInstanceData != null && errors != null && !errors.hasErrors()){
 
 			currentSessionId = currentInstanceData.getUserSessionId();
 			layers = currentInstanceData.getLayerList();
 
 			try {
 
 				// Asign the type to the layer.
 				layers = this.asingType2Layer(layers, currentSessionId);
 
 				// retrieve dataColumns
 				layers = this.retrieveColumns(layers, currentSessionId);
 
 			} catch (Exception ex) {
 				Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
 				errors.reject(ex.getMessage());
 			}
 
 		}
 		// asing the columns to the jsp.
 		cForm = new ChooseColumnForm();
 		cForm.setLayers(layers);
 		// Send the layer list to the JSP
 		model = new ModelAndView();
 		model.setViewName("columns");
 		model.addObject("columnsForm", cForm);
 		if (errors != null && errors.hasErrors()) {
 			model.addAllObjects(errors.getModel());
 		}
 		return model;
 	}
 
 
 
 
 	@Override
 	protected ModelAndView processFormSubmission(HttpServletRequest request
 		, HttpServletResponse response
 		, Object command
 		, BindException errors) {
 
 		CurrentInstanceData currentInstanceData = null;
 		HashMap<String, String> column = null;
 		String[] columnElements = null;
 		ChooseColumnForm cForm = null;
 		Long currentSessionId = null;
 		HttpSession session = null;
 		ModelAndView model = null;
 
 
 		if(errors.hasErrors())
 			return showForm(request, response, errors);
 
 		// get the information of the form.
 		cForm = (ChooseColumnForm)command;
 
 		// retrieve the session Information.
 		session = request.getSession();
 
 		currentInstanceData = SessionUtils.isSessionAlive(session);
 
 		if(currentInstanceData != null){
 
 			currentSessionId = currentInstanceData.getUserSessionId();
 
 			try {
 
 				// extract and format the information of the dataColumn to use.
 				for(Layer layer : cForm.getLayers()){
 
 					// split the information that comes from the Form
 					columnElements = layer.getColumns().get("selected").split(":");
 
 					// convert the array to a HashMap
 					column = new HashMap<String,String>();
 					column.put("selected", columnElements[0]);
 					layer.setColumns(column);
 
 					if(columnElements.length > 1 && columnElements[1].equals("CHARACTER")){
 						// vectorial reclasification
 						this.vectorialReclassification(layer, currentSessionId);
 					}else{
 						this.renameFile(layer, currentSessionId);
 					}
 
 					//convert the layer to a raster format
 					this.layer2Raster(layer, currentSessionId);
 
					if( columnElements.length > 1 && layer.getType().equals(LayerType.AREA )){
 
 						//asign the categories
 						this.asignCategories2Layer(layer
 							, columnElements[1]
 							, currentSessionId);
 					}else{
 						//asign the categories
 						this.asignCategories2Layer(layer
 							, "cat"
 							, currentSessionId);
 					}
 				}
 
 			} catch (Exception ex) {
 				Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
 				errors.reject(ex.getMessage());
 				return showForm(request, response, errors);
 			}
 		}else{
 
 			Exception ex = new Exception("errors.noSession");
 			Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
 			errors.reject(ex.getMessage());
 			return showForm(request, response, errors);
 		}
 
 		// Set the new information to the session info // selected layers and its weights
 		currentInstanceData.setLayerList(cForm.getLayers());
 		session.setAttribute("CurrentSessionInfo", currentInstanceData);
 
         return intervalsController.showForm(request, response, errors);
 	}
 
 
 	/**
 	 * change the name of a vector map.
 	 * @param layer
 	 * @param currentSessionId
 	 * @throws Exception
 	 */
 	private void renameFile(Layer layer, Long currentSessionId) throws Exception{
 		try {
 			this.grassManagerImpl.renameFile(FormDTOConverter.convert(layer), currentSessionId);
 		} catch (Exception ex) {
 			throw new Exception("errors.cantRenameFile", ex);
 		}
 
 	}
 
 	/**
 	 * Execute normalization of the values in a vectorial format map.
 	 * @param layerName
 	 * @param column
 	 * @param currentSessionId
 	 */
 	private void vectorialReclassification(Layer layer, Long currentSessionId) throws Exception{
 		try {
 			this.grassManagerImpl.executeVectorReclasification(FormDTOConverter.convert(layer), currentSessionId);
 		} catch (Exception ex) {
 			throw new Exception("errors.cantExecuteVectorReclass", ex);
 		}
 	}
 
 	/**
 	 * convert a vectorial layer into a raster one
 	 * @param layerName
 	 * @param currentSessionId
 	 * @param column
 	 */
 	private void layer2Raster(Layer layer, Long currentSessionId) throws Exception{
 		try {
 			// convert the vectorial layer to another in raster format
 			this.grassManagerImpl.convertLayer2Raster(FormDTOConverter.convert(layer), currentSessionId);
 		} catch (Exception ex) {
 			throw new Exception("errors.cantConvert2Raster", ex);
 		}
 
 	}
 
 
 	/**
 	 *
 	 * populate category information of the layer
 	 * @param layer
 	 * @param currentSessionId
 	 */
 	private void asignCategories2Layer(Layer layer, String dataType, Long currentSessionId) throws Exception{
 
 		List<CategoryDTO> categories = null;
 		try {
 			// retrieve an asing the layer categories
 			categories = this.grassManagerImpl.getLayerCategories(FormDTOConverter.convert(layer), dataType, currentSessionId);
 		} catch (Exception ex) {
 			throw new Exception("errors.cantRetrieveCategories", ex);
 		}
 
 		layer.setCategories(
 			FormDTOConverter.convert(categories, Category.class));
 	}
 
 
 
 	/**
 	 * Asign the type (line, area, points) to a layer.
 	 * @param layers
 	 * @param currentSessionId
 	 * @return list of layers with the field layer.type populated.
 	 */
 	private List<Layer> asingType2Layer(List<Layer> layers
 		, Long currentSessionId) throws Exception{
 
 		LayerType layerType = null;
 
 		List<Layer> list = new ArrayList<Layer>();
 
 		for(Layer layer : layers){
 			try {
 				layerType = this.grassManagerImpl.retrieveLayerType(FormDTOConverter.convert(layer), currentSessionId);
 			} catch (Exception ex) {
 				throw new Exception("errors.cantRetrieveLayerType",ex);
 			}
 
 			layer.setType(layerType);
 			list.add(layer);
 		}
 
 		return list;
 	}
 
 
 	/**
 	 * Return the name and the type of the columns of the dbf asociated to the
 	 * layer
 	 * @param layerList
 	 * @param currentSessionId
 	 * @return a list of layers with the columns information populated.
 	 */
 	private List<Layer> retrieveColumns(List<Layer> layerList
 		, Long currentSessionId) throws Exception{
 
 		Map<String,String> columns = null;
 
 		for (Layer layerForm: layerList){
 			if(layerForm.getType() == LayerType.AREA){
 				try {
 					// Retrive data columns
 					columns = grassManagerImpl.retrieveAvailableColumns(FormDTOConverter.convert(layerForm), currentSessionId);
 				} catch (Exception ex) {
 					throw new Exception("errors.cantRetrieveColumns",ex);
 				}
 				columns.remove("cat");
 				layerForm.setColumns(columns);
 			}
 		}
 
 		return layerList;
 	}
 
 
 
 	/* Getters y Setters */
 	public GrassManager getGrassManagerImpl() {
 		return grassManagerImpl;
 	}
 
 	public void setGrassManagerImpl(GrassManager grassManagerImpl) {
 		this.grassManagerImpl = grassManagerImpl;
 	}
 
     public IntervalsController getIntervalsController() {
         return intervalsController;
     }
 
     public void setIntervalsController(IntervalsController intervalsController) {
         this.intervalsController = intervalsController;
     }
 }
