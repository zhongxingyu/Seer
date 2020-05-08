 package com.kit.lightserver.adapters.adapterin;
 
 import java.util.Date;
 
 import kit.primitives.forms.FormContent;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.kit.lightserver.domain.types.ConhecimentoIdSTY;
 import com.kit.lightserver.statemachine.events.FormContentConhecimentoReadSME;
 import com.kit.lightserver.statemachine.states.KitEventSME;
 
 
 final class AdiFormContent {
 
 	static private final Logger LOGGER = LoggerFactory.getLogger(AdiFormContent.class);
 
 	static public ReceivedPrimitiveConverterResult<KitEventSME> adapt(final FormContent primitive) {
 
 	    final ReceivedPrimitiveConverterResult<KitEventSME> result;
 		if(primitive.formStatus == FormContent.FORM_READ) {
 
 		    String formId = primitive.formId; //formId=conhecimentos%1094966
 		    String[] splitedFormIdArray = formId.split("%");
 
 
 		    final int ktRowId = Integer.parseInt( splitedFormIdArray[1] );
 		    final String formType = splitedFormIdArray[0];
 		    final Date firstReadDate = primitive.firstReadDate;
 		    if( "conhecimentos".equals(formType) ) {
 		        ConhecimentoIdSTY conhecimentoId = new ConhecimentoIdSTY(ktRowId);
 		        FormContentConhecimentoReadSME event = new FormContentConhecimentoReadSME(conhecimentoId, firstReadDate);
 	            result = new ReceivedPrimitiveConverterResult<KitEventSME>(true, event);
 		    }
 		    else {
 		        LOGGER.error("Invalid form type.");
 	            LOGGER.error("splitedFormIdArray[0]="+splitedFormIdArray[0]); //splitedFormIdArray[0]=conhecimentos
 	            LOGGER.error("splitedFormIdArray[1]="+splitedFormIdArray[1]); //splitedFormIdArray[1]=1094966
 	            result = new ReceivedPrimitiveConverterResult<KitEventSME>();
 		    }
 
 
 
 
 		}
 		else {
 			LOGGER.error("Unknow type. primitive="+primitive);
 			result = new ReceivedPrimitiveConverterResult<KitEventSME>();
 		}
 
 		return result;
 
 	}
 
 }// class
