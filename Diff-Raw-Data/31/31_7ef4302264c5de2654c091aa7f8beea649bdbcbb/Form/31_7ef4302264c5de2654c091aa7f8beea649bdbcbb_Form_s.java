 package com.redshape.servlet.form.impl;
 
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.form.*;
 import com.redshape.servlet.form.decorators.LegendDecorator;
 import com.redshape.servlet.form.impl.internal.SubFormItem;
 import com.redshape.servlet.form.render.IFormRenderer;
 import com.redshape.servlet.form.render.impl.StandardFormRenderer;
 import com.redshape.utils.Commons;
 import com.redshape.utils.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 public class Form extends AbstractFormItem implements IForm {
 	private static final Logger log = Logger.getLogger( Form.class );
 	private static final long serialVersionUID = 5015229816321663639L;
 	
 	public enum FormState {
         NONE,
 		RENDERED,
 		VALIDATED,
 		PROCESSED
 	}
 	
 	public Form(String id) {
 		this(id, id);
 	}
 	
 	public Form( String id, String name ) {
 		super(id, name );
 
         this.renderer = new StandardFormRenderer();
 	}
 
 	private FormState state;
 	private String action;
 	private String method;
 	private String legend;
 	private IFormRenderer renderer;
 	private List<IFormItem> items = new ArrayList<IFormItem>();
 	private Map<String, Integer> itemsDict = new HashMap<String, Integer>();
 	private IFormProcessHandler handler;
 	
 	@Override
 	public void setProcessHandler( IFormProcessHandler handler ) {
 		this.handler = handler;
 	}
 	
 	protected IFormProcessHandler getProcessHandler() {
 		return this.handler;
 	}
 	
 	@Override
 	public void process( IHttpRequest request ) throws InvalidDataException {
 		if ( !request.isPost() ) {
 			throw new IllegalArgumentException("Processing only possible in POST context");
 		}
 
         this.resetState();
 
 		Map<String, Object> parameters = request.getParameters();
 		for ( String attribute : parameters.keySet() ) {
             this.setValue(attribute, parameters.get(attribute) );
 		}
 		
 		if ( !this.isValid() ) {
 			throw new InvalidDataException();
 		}
 		
 		if ( this.getProcessHandler() != null ) {
 			this.getProcessHandler().process( this );
 		}
 	}
 
 	@Override
 	public <T> T getValue( String path ) {
 		return this.<T>findField(path).getValue();
 	}
 
    protected void setValue( String path, Object value ) {
         this.findField(path).setValue(value);
     }
 
 	@Override
 	public <T> IFormField<T> findField( String path ) {
 		String[] parts = path.split("\\.");
 		if ( parts.length != 1 ) {
 			return this.findField( this, parts );
 		}
 		
 		return this.findField( this, path );
 	}
 	
 	@Override
 	public IForm findContext( String path ) {
 		return this.findContext( path.split("\\.") );
 	}
 	
 	protected IForm findContext( String[] path ) {
 		Integer index = this.itemsDict.get( path[0] );
 
         IFormItem item = null;
 		if ( index != null ) {
             item = this.items.get( index );
         } else {
             if ( this.getCanonicalName().equals( path[0] ) ) {
                 item = this;
             }
         }
 
         if ( item == null ) {
             throw new IllegalStateException();
         }
 		
 		if (  !( item instanceof IForm ) ) {
 			log.error("Current context:" + this.getCanonicalName() + ", full search path: " + Arrays.asList( path )  );
 			throw new IllegalArgumentException("Illegal path");
 		}
 		
 		IForm form = (IForm) item;
 		if ( path.length == 1 ) {
 			return form;
 		}
 		
 		return form.findContext( 
 				StringUtils.join( Arrays.copyOfRange( path, 1, path.length ), "." ) );
 	}
 	
 	protected <T> IFormField<T> findField( IForm context, String[] name ) {
 		IForm targetContext = this.findContext( Arrays.copyOfRange( name, 0, name.length - 1  ) );
 		if ( targetContext == null ) {
             if ( name.length == 2
                     && name[0].equals( Commons.select( this.getName(), this.getId() ) ) ) {
                 targetContext = this;
             } else {
 			    throw new IllegalArgumentException("Path " + Arrays.asList(name) + " not founded!");
             }
 		}
 		
 		return this.findField( targetContext, name[name.length-1] );
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected <T> IFormField<T> findField( IForm context, String subContextName ) {
 		IFormItem item = null;
 		
 		if ( context != this ) {
 			item = context.findField( subContextName );
 		} else {
 			Integer index = Commons.select( this.itemsDict.get(subContextName),
                                             this.itemsDict.get( subContextName + "[]" ) );
 			if ( index != null ) {
 				item = this.items.get(index);
 			}
 		}
 		
 		if ( item == null ) {
 			throw new IllegalArgumentException("Field " + context.getName() + "." + subContextName + " not founded");
 		}
 		
 		if ( !( item instanceof IFormField ) ) {
 			throw new IllegalArgumentException("Requested path not related to field");
 		}
 		
 		final IFormField<T> newItem = (IFormField<T>) item;
         return newItem;
 	}
 	
 	protected String getFieldId( IFormField<?> field ) {
 		String id = Commons.select( field.getName(), field.getId() );
         if ( id == null ) {
             return null;
         }
 
         return id.replace("[]", "");
 	}
 	
 	@Override
 	public void setRenderer(IFormRenderer renderer) {
 		this.renderer = renderer;
 	}
 
 	@Override
 	public String render() {
 		return this.render( RenderMode.FULL );
 	}
 
     @Override
     public void resetMessages() {
         for ( IFormItem item : this.getItems() ) {
             item.resetMessages();
         }
     }
 
     @Override
     public void resetState() {
         this.state = FormState.NONE;
 
         for ( IFormItem item : this.getItems() ) {
             item.resetState();
         }
     }
 
 	@Override
 	public String render( RenderMode mode ) {
 		if ( this.state != null && this.state.equals( FormState.RENDERED )
                 && this.getContext() != null ) {
 			if ( !this.hasAttribute("forceRender") ) { 
 				return "";
 			}
 		}
 		
 		if ( this.renderer == null ) {
 			throw new IllegalStateException("<null>");
 		}
 		
 		if ( this.getLegend() != null && !this.hasDecorator( LegendDecorator.class) ) {
 			this.setDecorator( new LegendDecorator() );
 		}
 		
 		String data = this.renderer.render(this, mode);
 		this.state = FormState.RENDERED;
 		return data;
 	}
 
 	@Override
 	public void setLegend(String legend) {
 		this.legend = legend;
 	}
 
 	@Override
 	public String getLegend() {
 		return this.legend;
 	}
 
 	@Override
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	@Override
 	public String getAction() {
 		return this.action;
 	}
 
 	@Override
 	public void setMethod(String method) {
 		this.method = method;
 	}
 
 	@Override
 	public String getMethod() {
 		return this.method;
 	}
 
 	@Override
 	public void addField(IFormField<?> field) {
 		field.setContext(this);
 		this.items.add( field );
 		this.itemsDict.put( this.getFieldId(field), this.items.size() - 1 );
 	}
 
 	@Override
 	public void removeField(IFormField<?> field) {
 		this.items.remove( field );
 		this.itemsDict.remove( this.getFieldId(field) );
 	}
 
 	@Override
 	public List<IFormField<?>> getFields() {
 		return null;
 	}
 
 	@Override
 	public IFormRenderer getRenderer() {
 		return this.renderer;
 	}
 	
 	@Override
 	public void addSubForm(IForm form, String name) {
 		form.setContext(this);
         form.setName( name );
 		if ( form.getRenderer() == null ) {
 			form.setRenderer( this.renderer );
 		}
 		
 		this.items.add( new SubFormItem( form, name  ) );
 		this.itemsDict.put( name, this.items.size() - 1 );
 	}
 
 	@Override
 	public void removeSubForm(String name) {
 		
 	}
 
 	@Override
 	public List<IForm> getSubForms() {
 		List<IForm> result = new ArrayList<IForm>();
 		for ( IFormItem item : this.items ) {
 			if ( item instanceof SubFormItem ) {
 				result.add( (IForm) ( (SubFormItem) item ).getForm() );
 			}
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public List<IFormItem> getItems() {
 		return this.items;
 	}
 	
 	@Override
 	public boolean isValid() {
 		boolean result = true;
 		for ( IFormItem item : this.getItems() ) {
 			boolean itemResult = item.isValid();
             result = itemResult && result;
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public String toString() {
 		return this.render();
 	}
 	
 }
