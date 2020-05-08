 package com.isencia.passerelle.workbench.model.editor.ui.editpart;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 import ptolemy.kernel.util.Attribute;
 import ptolemy.kernel.util.NamedObj;
 import ptolemy.kernel.util.StringAttribute;
 import ptolemy.vergil.kernel.attributes.TextAttribute;
 
 import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
 import com.isencia.passerelle.workbench.model.editor.ui.figure.CommentFigure;
 import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;
 import com.isencia.passerelle.workbench.model.editor.ui.properties.CommentPropertySource;
 
 public class CommentEditPart extends AbstractNodeEditPart {
 
 	protected void onChangePropertyResource(Object source) {
 		
 		final String nameChanged = ((NamedObj)source).getContainer().getName();
 		final String thisName    = ((TextAttribute)getModel()).getName();
 		
 		if (!nameChanged.equals(thisName)) return;
 		
 		if (source instanceof TextAttribute || source instanceof StringAttribute) {
 			
 			String label = getText(source);
 
 			// Execute the dummy command force a dirty state
 			((CommentFigure) getFigure()).setText(label);
 			getFigure().repaint();
 		}
 	}
 
 	protected void createEditPolicies() {
 		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentNodeDeletePolicy());
 	}
 
 	/**
 	 * Returns a newly created Figure to represent this.
 	 * 
 	 * @return Figure of this.
 	 */
 	protected IFigure createFigure() {
 		
 		final Object model = getModel();
 		String label = getText(model);
 		
 		/**
 		 * BODGE WARNING: to avoid text appearing incorrectly in Workbench diagram we look for the
 		 * default attribute text coming from the ptolemy class which incorrectly says 'Double click'.
 		 */
		if ("Double click to edit text.".equals(label)) label = "Click to edit in 'Actor Attributes'";
 		ImageDescriptor imageDescriptor = PaletteItemFactory.getInstance().getIcon(TextAttribute.class);
 		
 		return new CommentFigure(label, createImage(imageDescriptor));
 	}
 
 	public CommentFigure getCommentFigure() {
 		return (CommentFigure) getFigure();
 	}
 
 	protected TextAttribute getTextAttribute() {
 		return (TextAttribute) getModel();
 	}
 
 	public void setSelected(int i) {
 		super.setSelected(i);
 		super.refreshVisuals();
 	}
 
 	protected IPropertySource getPropertySource() {
 		if (propertySource == null) {
 			propertySource = new CommentPropertySource(getEntity(), getFigure());
 		}
 		return propertySource;
 	}
 
 	protected String getText(Object source) {
 		if (source instanceof StringAttribute) {
 			return ((StringAttribute) source).getExpression();
 		}
 		if (source instanceof TextAttribute) {
 			Attribute attribute = ((TextAttribute) source).getAttribute("text");
 			if (attribute instanceof StringAttribute)
 				return ((StringAttribute) attribute).getExpression();
 		}
 		return "";
 	}
 	@Override
 	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart arg0) {
 		// Not source connection anchor for Comment
 		return null;
 	}
 
 	@Override
 	public ConnectionAnchor getSourceConnectionAnchor(Request arg0) {
 		// Not source connection anchor for Comment
 		return null;
 	}
 
 	@Override
 	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart arg0) {
 		// Not target connection anchor for Comment
 		return null;
 	}
 
 	@Override
 	public ConnectionAnchor getTargetConnectionAnchor(Request arg0) {
 		// Not target connection anchor for Comment
 		return null;
 	}
 
 }
