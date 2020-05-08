 package com.redshape.servlet.form.impl.internal;
 
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.form.IForm;
 import com.redshape.servlet.form.IFormField;
 import com.redshape.servlet.form.IFormProcessHandler;
 import com.redshape.servlet.form.InvalidDataException;
 import com.redshape.servlet.form.decorators.IDecorator;
 import com.redshape.servlet.form.render.IFormRenderer;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 public class SubFormItem implements IForm {
 	private static final long serialVersionUID = -3380922220515950665L;
 	
 	private String name;
 	private IForm form;
 	
 	public SubFormItem( IForm form, String name ) {
 		this.name = name;
 		this.form = form;
 	}
 
	@Override
	public void setValue(String name, Object value) {
		this.getForm().setValue(name, value);
	}

 	public List<String> getMessages() {
 		return this.form.getMessages();
 	}
 
 	public void addMessage( String message ) {
 		this.form.addMessage(message);
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public void setName( String name ) {
 		this.name = name;
 	}
 
 	public IForm getForm() {
 		return form;
 	}
 
 	public void setForm(IForm form) {
 		this.form = form;
 	}
 
     @Override
     public void resetState() {
         this.form.resetState();
     }
 
     @Override
 	public void setContext(IForm form) {
 		this.form.setContext(form);
 	}
 
 	@Override
 	public <T> T getValue( String name ) {
 		return this.form.<T>getValue(name);
 	}
 
 	@Override
 	public IForm getContext() {
 		return this.form.getContext();
 	}
 
 	@Override
 	public String getCanonicalName() {
 		return this.form.getCanonicalName();
 	}
 
 	@Override
 	public void setId(String id) {
 		this.form.setId(id);
 	}
 
 	@Override
 	public String getId() {
 		return this.form.getId();
 	}
 
     @Override
     public void resetMessages() {
         this.form.resetMessages();
     }
 
     @Override
 	public Map<String, Object> getAttributes() {
 		return this.form.getAttributes();
 	}
 
 	@Override
 	public void setAttribute(String name, Object value) {
 		this.form.setAttribute(name, value);
 	}
 
 	@Override
 	public <T> T getAttribute(String name) {
 		return this.form.<T>getAttribute(name);
 	}
 
 	@Override
 	public void setDecorator(IDecorator decorator) {
 		this.form.setDecorator(decorator);
 	}
 
 	@Override
 	public void setDecorators(IDecorator[] decorators) {
 		this.form.setDecorators(decorators);
 	}
 
 	@Override
 	public boolean hasDecorator(Class<? extends IDecorator> decorator) {
 		return this.form.hasDecorator(decorator);
 	}
 
 	@Override
 	public void clearDecorators() {
 		this.form.clearDecorators();
 	}
 
 	@Override
 	public void setDecorators(List<IDecorator> decorators) {
 		this.form.setDecorators(decorators);
 	}
 
 	@Override
 	public Collection<IDecorator> getDecorators() {
 		return this.form.getDecorators();
 	}
 
 	@Override
 	public void setProcessHandler(IFormProcessHandler handler) {
 		this.getForm().setProcessHandler(handler);
 	}
 
 	@Override
 	public void process(IHttpRequest request) throws InvalidDataException {
 		this.getForm().process(request);
 	}
 
 	@Override
 	public <T> IFormField<T> findField(String path) {
 		return this.getForm().findField(path);
 	}
 
 	@Override
 	public void setRenderer(IFormRenderer renderer) {
 		this.getForm().setRenderer(renderer);
 	}
 	
 	@Override
 	public IFormRenderer getRenderer() {
 		return this.getForm().getRenderer();
 	}
 
 	@Override
 	public String render() {
 		return this.getForm().render();
 	}
 
 	@Override
 	public void setLegend(String legend) {
 		this.getForm().setLegend(legend);
 	}
 
 	@Override
 	public String getLegend() {
 		return this.getForm().getLegend();
 	}
 
 	@Override
 	public void setAction(String action) {
 		this.getForm().setAction(action);
 	}
 
 	@Override
 	public String getAction() {
 		return this.getForm().getAction();
 	}
 
 	@Override
 	public void setMethod(String method) {
 		this.getForm().setMethod(method);
 	}
 
 	@Override
 	public String getMethod() {
 		return this.getForm().getMethod();
 	}
 
 	@Override
 	public void addField(IFormField<?> field) {
 		this.getForm().addField(field);
 	}
 
 	@Override
 	public void removeField(IFormField<?> field) {
 		this.getForm().removeField(field);
 	}
  
 	@Override
 	public List<IFormField<?>> getFields() {
 		return this.getForm().getFields();
 	}
 
 	@Override
 	public void addSubForm(IForm form, String name) {
 		this.getForm().addSubForm(form, name);
 	}
 
 	@Override
 	public void removeSubForm(String name) {
 		this.getForm().removeSubForm(name);
 	}
 
 	@Override
 	public List<IForm> getSubForms() {
 		return this.getForm().getSubForms();
 	}
 
 	@Override
 	public List<com.redshape.servlet.form.IFormItem> getItems() {
 		return this.getForm().getItems();
 	}
 
 	@Override
 	public boolean isValid() {
 		return this.getForm().isValid();
 	}
 
 	@Override
 	public String toString() {
 		return this.getForm().toString();
 	}
 
 	@Override
 	public String render(com.redshape.servlet.form.RenderMode mode) {
 		return this.getForm().render(mode);
 	}
 
 	@Override
 	public boolean hasAttribute(String name) {
 		return this.getForm().hasAttribute(name);
 	}
 
 	@Override
 	public IForm findContext(String name) {
 		return this.getForm().findContext(name);
 	}
 }
