 package com.trsvax.datepicker.mixins;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.tapestry5.Asset;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.annotations.AfterRender;
 import org.apache.tapestry5.annotations.BeginRender;
 import org.apache.tapestry5.annotations.BindParameter;
 import org.apache.tapestry5.annotations.InjectContainer;
 import org.apache.tapestry5.annotations.MixinAfter;
 import org.apache.tapestry5.annotations.Path;
 import org.apache.tapestry5.corelib.components.TextField;
 import org.apache.tapestry5.dom.Element;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.TypeCoercer;
 import org.apache.tapestry5.json.JSONObject;
 import org.apache.tapestry5.services.AssetSource;
 import org.apache.tapestry5.services.FormSupport;
 import org.apache.tapestry5.services.javascript.JavaScriptSupport;
 
 import com.trsvax.datepicker.DatePickerConstants;
 
 @MixinAfter
 public class JQueryDatePicker {
 	
 	@BindParameter
 	private Object value;
 		
     @Inject
 	@Path("classpath:/META-INF/assets/datefield/calendar.png")
     private Asset icon;
     
     @Inject
     private AssetSource assetSource;
     
     @Inject
     @Symbol(DatePickerConstants.JQUERY_CSS)
     private String css;
     
     @Inject
     @Symbol(DatePickerConstants.JQUERY_LIBRARY)
     private String javascript;
       
 	@Inject
 	private JavaScriptSupport javaScriptSupport;
 
 	@InjectContainer
 	private TextField clientElement;
 	
 	@Inject
 	private FormSupport formSupport;
 	
	@Inject
    private TypeCoercer coercer;
	
 	private Element element;
 	
 	@BeginRender
 	void beginRender(MarkupWriter writer) {		
 		element = writer.getElement();
 		element.forceAttributes("type","date");	
 	}
 	
 	@AfterRender
 	void afterRender(MarkupWriter writer) {
 		String id = clientElement.getClientId();
 		String clientID = javaScriptSupport.allocateClientId(id);
 		String formID = formSupport.getClientId();
		Date date = coercer.coerce(value, Date.class);
 		String formatedDate = "";
 		if ( date != null ) {
 			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 			formatedDate = format.format(date);
 		}
 		Element dateField = element.elementBefore("input", 
 				"value",formatedDate,"type","hidden","class","form-control","id",clientID);
		if (clientElement.isDisabled()) {
 			 dateField.attribute("disabled", "disabled");
 		}
 		javaScriptSupport.require("datepicker/datepicker").with(new JSONObject("id", id, "clientID", clientID,"formID",formID));
 		if ( ! DatePickerConstants.NULL.equals(css)) {
 			javaScriptSupport.importStylesheet(assetSource.getExpandedAsset(css));
 		}
 		if ( ! DatePickerConstants.NULL.equals(javascript)) {
 			javaScriptSupport.importJavaScriptLibrary(assetSource.getExpandedAsset(javascript));
 		}
 	}
 	
 }
