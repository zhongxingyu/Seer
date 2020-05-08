 package com.eyeq.pivot4j.ui.primefaces;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 
 import com.eyeq.pivot4j.PivotModel;
 import com.eyeq.pivot4j.ui.primefaces.tree.CubeNode;
 
@ManagedBean(name = "navigatorHandler")
 @RequestScoped
 public class NavigatorHandler {
 
 	@ManagedProperty(value = "#{pivotModelManager.model}")
 	private PivotModel model;
 
 	private CubeNode rootNode;
 
 	/**
 	 * @return the model
 	 */
 	public PivotModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @param model
 	 *            the model to set
 	 */
 	public void setModel(PivotModel model) {
 		this.model = model;
 	}
 
 	/**
 	 * @return the rootNode
 	 */
 	public CubeNode getRootNode() {
 		if (rootNode == null && model.isInitialized()) {
			this.rootNode = new CubeNode(model.getCube());
 		}
 
 		return rootNode;
 	}
 }
