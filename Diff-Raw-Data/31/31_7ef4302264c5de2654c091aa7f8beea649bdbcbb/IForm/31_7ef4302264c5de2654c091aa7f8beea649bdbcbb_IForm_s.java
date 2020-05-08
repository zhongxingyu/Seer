 package com.redshape.servlet.form;
 
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.form.render.IFormRenderer;
 
 import java.util.List;
 
 public interface IForm extends com.redshape.servlet.form.IFormItem {
 
 	public <T> T getValue( String name );
 
 	public void setProcessHandler( com.redshape.servlet.form.IFormProcessHandler handler );
 	
 	public void process( IHttpRequest request ) throws com.redshape.servlet.form.InvalidDataException;
 	
 	public void setLegend( String legend );
 	
 	public String getLegend();
 	
 	public void setAction( String action );
 	
 	public String getAction();
 	
 	public void setMethod( String method );
 	
 	public String getMethod();
 	
 	public IForm findContext( String name );
 	
 	public <T> com.redshape.servlet.form.IFormField<T> findField( String name );
 	
 	public void addField( com.redshape.servlet.form.IFormField<?> field );
 	
 	public void removeField( com.redshape.servlet.form.IFormField<?> field );
 	
 	public List<com.redshape.servlet.form.IFormField<?>> getFields();
 	
 	public void addSubForm( IForm form, String name );
 	
 	public void removeSubForm( String name );
 	
 	public List<IForm> getSubForms();
 	
 	public List<com.redshape.servlet.form.IFormItem> getItems();
 
 	public IFormRenderer getRenderer();
 	
 	public void setRenderer( IFormRenderer renderer );
 	
 }
