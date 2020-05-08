 package org.omidbiz.component.html;
 
 import javax.el.ELException;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 import org.omidbiz.component.UICheckbox;
 
 public class HtmlCheckbox extends UICheckbox{
 
final static public  String COMPONENT_FAMILY = "org.omidbiz.Checkbox";
 
final static public  String COMPONENT_TYPE = "org.omidbiz.Checkbox";
 
 /*
 * 
 */
 private  boolean _checked = false;
 
 private  boolean _checkedSet = false;
 
 /*
 * null
 */
 private  String _onchange = null;
 
 /*
 * The clientside script method to be called when the element is clicked
 */
 private  String _onclick = null;
 
 /*
 * The client side script method to be called when the element is double-clicked
 */
 private  String _ondblclick = null;
 
 /*
 * The client side script method to be called when a key is pressed down over the element
 */
 private  String _onkeydown = null;
 
 /*
 * The client side script method to be called when a key is pressed over the element and released
 */
 private  String _onkeypress = null;
 
 /*
 * The client side script method to be called when a key is released
 */
 private  String _onkeyup = null;
 
 /*
 * The client side script method to be called when a mouse button is pressed down over the element
 */
 private  String _onmousedown = null;
 
 /*
 * The client side script method to be called when a pointer is moved within the element
 */
 private  String _onmousemove = null;
 
 /*
 * The client side script method to be called when a pointer is moved away from the element
 */
 private  String _onmouseout = null;
 
 /*
 * The client side script method to be called when a pointer is moved onto the element
 */
 private  String _onmouseover = null;
 
 /*
 * The client side script method to be called when a mouse button is released
 */
 private  String _onmouseup = null;
 
 
 public HtmlCheckbox(){
 setRendererType("org.omidbiz.CheckboxRenderer");
 }
 
 public boolean isChecked(){
 	if (this._checkedSet) {
 	    return (this._checked);
 	}
 	ValueExpression ve = getValueExpression("checked");
 	if (ve != null) {
 	    Boolean value = null;
 	    
 	    try {
 			value = (Boolean) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    if (null == value) {
 			return (this._checked);
 	    }
 	    
 	    return value;
 	} else {
 	    return (this._checked);
 	}
 
 }
 
 public void setChecked(boolean _checked){
 this._checked = _checked;
 this._checkedSet = true;
 }
 
 public String getOnchange(){
 	if (this._onchange != null) {
 		return this._onchange;
 	}
 	ValueExpression ve = getValueExpression("onchange");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnchange(String _onchange){
 this._onchange = _onchange;
 }
 
 public String getOnclick(){
 	if (this._onclick != null) {
 		return this._onclick;
 	}
 	ValueExpression ve = getValueExpression("onclick");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnclick(String _onclick){
 this._onclick = _onclick;
 }
 
 public String getOndblclick(){
 	if (this._ondblclick != null) {
 		return this._ondblclick;
 	}
 	ValueExpression ve = getValueExpression("ondblclick");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOndblclick(String _ondblclick){
 this._ondblclick = _ondblclick;
 }
 
 public String getOnkeydown(){
 	if (this._onkeydown != null) {
 		return this._onkeydown;
 	}
 	ValueExpression ve = getValueExpression("onkeydown");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnkeydown(String _onkeydown){
 this._onkeydown = _onkeydown;
 }
 
 public String getOnkeypress(){
 	if (this._onkeypress != null) {
 		return this._onkeypress;
 	}
 	ValueExpression ve = getValueExpression("onkeypress");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnkeypress(String _onkeypress){
 this._onkeypress = _onkeypress;
 }
 
 public String getOnkeyup(){
 	if (this._onkeyup != null) {
 		return this._onkeyup;
 	}
 	ValueExpression ve = getValueExpression("onkeyup");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnkeyup(String _onkeyup){
 this._onkeyup = _onkeyup;
 }
 
 public String getOnmousedown(){
 	if (this._onmousedown != null) {
 		return this._onmousedown;
 	}
 	ValueExpression ve = getValueExpression("onmousedown");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnmousedown(String _onmousedown){
 this._onmousedown = _onmousedown;
 }
 
 public String getOnmousemove(){
 	if (this._onmousemove != null) {
 		return this._onmousemove;
 	}
 	ValueExpression ve = getValueExpression("onmousemove");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnmousemove(String _onmousemove){
 this._onmousemove = _onmousemove;
 }
 
 public String getOnmouseout(){
 	if (this._onmouseout != null) {
 		return this._onmouseout;
 	}
 	ValueExpression ve = getValueExpression("onmouseout");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnmouseout(String _onmouseout){
 this._onmouseout = _onmouseout;
 }
 
 public String getOnmouseover(){
 	if (this._onmouseover != null) {
 		return this._onmouseover;
 	}
 	ValueExpression ve = getValueExpression("onmouseover");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnmouseover(String _onmouseover){
 this._onmouseover = _onmouseover;
 }
 
 public String getOnmouseup(){
 	if (this._onmouseup != null) {
 		return this._onmouseup;
 	}
 	ValueExpression ve = getValueExpression("onmouseup");
 	if (ve != null) {
 	    String value = null;
 	    
 	    try {
 			value = (String) ve.getValue(getFacesContext().getELContext());
 	    } catch (ELException e) {
 			throw new FacesException(e);
 	    }
 	    
 	    return value;
 	} 
 
     return null;
 	
 
 }
 
 public void setOnmouseup(String _onmouseup){
 this._onmouseup = _onmouseup;
 }
 
 public String getFamily(){
 return COMPONENT_FAMILY;
 }
 
 @Override
 public Object saveState(FacesContext context){
 Object [] state = new Object[14];
 state[0] = super.saveState(context);
 state[1] = Boolean.valueOf(_checked);
 state[2] = Boolean.valueOf(_checkedSet);
 state[3] = _onchange;
 state[4] = _onclick;
 state[5] = _ondblclick;
 state[6] = _onkeydown;
 state[7] = _onkeypress;
 state[8] = _onkeyup;
 state[9] = _onmousedown;
 state[10] = _onmousemove;
 state[11] = _onmouseout;
 state[12] = _onmouseover;
 state[13] = _onmouseup;
 return state;
 }
 
 @Override
 public void restoreState(FacesContext context, Object state){
 Object[] states = (Object[]) state;
 super.restoreState(context, states[0]);
 	_checked = ((Boolean)states[1]).booleanValue();
 		_checkedSet = ((Boolean)states[2]).booleanValue();
 		_onchange = (String)states[3];;
 		_onclick = (String)states[4];;
 		_ondblclick = (String)states[5];;
 		_onkeydown = (String)states[6];;
 		_onkeypress = (String)states[7];;
 		_onkeyup = (String)states[8];;
 		_onmousedown = (String)states[9];;
 		_onmousemove = (String)states[10];;
 		_onmouseout = (String)states[11];;
 		_onmouseover = (String)states[12];;
 		_onmouseup = (String)states[13];;
 	
 }
 
 }
