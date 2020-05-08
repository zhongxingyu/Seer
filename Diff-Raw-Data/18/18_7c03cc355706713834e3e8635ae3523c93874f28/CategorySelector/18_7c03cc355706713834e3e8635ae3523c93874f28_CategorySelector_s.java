 package org.infoglue.cms.workflow.taglib;
 
 import java.text.MessageFormat;
 import java.util.Iterator;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 
 import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
 import org.infoglue.cms.entities.management.CategoryVO;
 
 /**
  * 
  */
 public class CategorySelector extends ContentInputTag {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3256721792767703093L;
 	
 	private static final String SELECT_FIELD    = "<select id=\"{0}\" name=\"{0}\">{1}</select>";
 	private static final String OPTION_DEFAULT  = "<option value=\"\">{0}</option>";
 	private static final String OPTION          = "<option value=\"{0}\">{1}</option>";
	private static final String OPTION_SELECTED = "<option value=\"{0}\" selected=\"true\">{1}</option>";
 
 	private String categoryPath;
 	private String defaultLabel;
 
 	public CategorySelector() {
 		super();
 	}
 
 	/**
 	 * 
 	 */
 	public int doEndTag() throws JspException {
 		write(createSelectorHTML());
 		return EVAL_PAGE;
 	}
 
 	/**
 	 * 
 	 */
 	private String createSelectorHTML() throws JspTagException {
 		return MessageFormat.format(SELECT_FIELD, new Object[] { getName(), getOptionsHTML() });
 	}
 	
 	/**
 	 * 
 	 */
 	private String getOptionsHTML() throws JspTagException {
 		StringBuffer sb = new StringBuffer(); 
 		sb.append(MessageFormat.format(OPTION_DEFAULT, new Object[] { defaultLabel }));
 		for(Iterator i = getRootCategory().getChildren().iterator(); i.hasNext();) {
 			final CategoryVO categoryVO = (CategoryVO) i.next();
 			final String name           = categoryVO.getName();
 			final String value          = categoryVO.getId().toString();
 			if(value.equals(getSelected()))
 				sb.append(MessageFormat.format(OPTION_SELECTED, new Object[] { value, name }));
 			else
 				sb.append(MessageFormat.format(OPTION, new Object[] { value, name }));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * 
 	 */
 	private CategoryVO getRootCategory() throws JspTagException {
 		try {
 			CategoryVO categoryVO = CategoryController.getController().findByPath(categoryPath);
 			return CategoryController.getController().findWithChildren(categoryVO.getId());
 		} catch(Exception e) {
 			e.printStackTrace();
 			throw new JspTagException("CategorySelector.getRootCategory() : " + e);
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public String getCategoryPath() {
 		return categoryPath;
 	}
 
 	/**
 	 * 
 	 */
 	public void setCategoryPath(String categoryPath) {
 		this.categoryPath = categoryPath;
 	}
 	
 	/**
 	 * 
 	 */
 	public String getDefaultLabel() {
 		return defaultLabel;
 	}
 
 	/**
 	 * 
 	 */
 	public void setDefaultLabel(String defaultLabel) {
 		this.defaultLabel = defaultLabel;
 	}
 
 	/**
 	 * 
 	 */
     public String getSelected() {
 		return WorkflowHelper.getPropertyData(getName(), pageContext.getSession(), pageContext.getRequest());
     }
 }
