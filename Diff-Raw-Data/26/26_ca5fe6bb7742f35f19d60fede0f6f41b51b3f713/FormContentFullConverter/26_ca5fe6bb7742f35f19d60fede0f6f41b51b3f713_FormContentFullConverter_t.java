 package com.kit.lightserver.adapters.adapterout;
 
 import java.util.Date;
 
 import kit.primitives.forms.FieldAndContentBean;
 import kit.primitives.forms.FormContent;
 import kit.primitives.forms.FormContentFull;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.kit.lightserver.domain.types.ConhecimentoSTY;
 import com.kit.lightserver.domain.types.NotafiscalSTY;
 import com.kit.lightserver.domain.types.TemplateEnumSTY;
 import com.kit.lightserver.types.response.FormContentFullConhecimentoRSTY;
 import com.kit.lightserver.types.response.FormContentFullNotafiscalRSTY;
 import com.kit.lightserver.types.response.FormContentFullRSTY;
 
 public final class FormContentFullConverter {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FormContentFullConverter.class);
 
     static public ConverterResult convertForm(final FormContentFullRSTY formContentFullRSTY) {
 
         final FormContentFull formContentFull;
         if (formContentFullRSTY instanceof FormContentFullNotafiscalRSTY) {
             final FormContentFullNotafiscalRSTY forContentFullNotafiscalRSTY = (FormContentFullNotafiscalRSTY) formContentFullRSTY;
             formContentFull = FormContentFullConverter.convertNotaFiscal(forContentFullNotafiscalRSTY);
         } else if (formContentFullRSTY instanceof FormContentFullConhecimentoRSTY) {
             final FormContentFullConhecimentoRSTY forContentFullNotafiscalRSTY = (FormContentFullConhecimentoRSTY) formContentFullRSTY;
             formContentFull = FormContentFullConverter.convertConhecimento(forContentFullNotafiscalRSTY);
         } else {
             formContentFull = null;
         }
 
         final ConverterResult result;
         if (formContentFull != null) {
             result = new ConverterResult(true, formContentFull);
         } else {
             result = new ConverterResult();
         }
 
         return result;
 
     }
 
     static private FormContentFull convertConhecimento(final FormContentFullConhecimentoRSTY formContentFullConhecimentoRSTY) {
 
         final ConhecimentoSTY form = formContentFullConhecimentoRSTY.getConhecimentoSTY();
 
         final int ktRowId = form.getKtRowId();
         final String anchorCategoryId = ktRowId + "P";
 
         final TemplateEnumSTY template = form.getTemplate();
         final String templateStr = TemplateEnumSTYConverter.convert(template);
 
         final FormContentFull response = new FormContentFull();
         response.formId = templateStr + "%" + ktRowId;
         response.formStatus = FormContentFullConverter.calculateFormStatus(form);
         response.templateId = templateStr;
         response.category = anchorCategoryId;
         response.title = form.getTitle();
        response.showFlags = 32; // because it is "Conhecimento" template
         response.firstReadDate = new Date(); // new Date(0L) didn't work, need to discuss
         response.lastEditDate = new Date(); // new Date(0L);
 
         final String childrenCategoryAncora = ktRowId + "";
         final FieldAndContentBean numeroConhecimentoBean = new FieldAndContentBean("ancora", childrenCategoryAncora);
         response.add(numeroConhecimentoBean);
 
         final String statusEntregaStr = StatusEntregaConverter.toKeyString(form.getStatusEntrega());
         final FieldAndContentBean statusEntregaBean = new FieldAndContentBean("statusEntrega", statusEntregaStr);
         response.add(statusEntregaBean);
 
         if( form.getDataEntrega() != null ) { // Sanity
             LOGGER.error("Investigate. form.getDataEntrega() != null, form="+form);
         }
         final FieldAndContentBean dataEntregaBean = new FieldAndContentBean("dataEntrega", "null");  // Mandatory, the mobile crashes without it
         response.add(dataEntregaBean);
 
         final String remetenteCNPJ = form.getRemetenteCNPJ();
         final FieldAndContentBean remetenteCNPJBean = new FieldAndContentBean("remetenteCNPJ", remetenteCNPJ);
         response.add(remetenteCNPJBean);
 
         final String destinatarioNome = form.getDestinatarioNome();
         final FieldAndContentBean destinatarioNomeBean = new FieldAndContentBean("destinatarioNome", destinatarioNome);
         response.add(destinatarioNomeBean);
 
         return response;
 
     }
 
     static public FormContentFull convertNotaFiscal(final FormContentFullNotafiscalRSTY formContentFullNotafiscalRSTY) {
 
         final NotafiscalSTY form = formContentFullNotafiscalRSTY.getNotafiscalSTY();
 
         final TemplateEnumSTY template = form.getTemplate();
 
         final FormContentFull response = new FormContentFull();
 
         final String templateStr = TemplateEnumSTYConverter.convert(template);
         final int ktRowId = form.getKtRowId();
         response.formId = templateStr + "%" + ktRowId;
         response.formStatus = FormContentFullConverter.calculateFormStatus(form);
         response.templateId = templateStr;
 
         final String category = Integer.toString(form.getParentKnowledgeRowId());
         response.category = category;
 
         response.title = form.getTitle();
         response.showFlags = 0; // because it is "NotaFiscal" template
         response.firstReadDate = new Date();
         response.lastEditDate = new Date();
 
         final String parentCategoryAncora = form.getParentKnowledgeRowId() + "P";
         final FieldAndContentBean numeroConhecimentoBean = new FieldAndContentBean("ancora", parentCategoryAncora);
         response.add(numeroConhecimentoBean);
 
         final FieldAndContentBean statusEntregaBean = new FieldAndContentBean("statusEntrega", "AN"); // AN = Ainda não entregue
         response.add(statusEntregaBean);
 
         final String dataEntregaStr = DataEntregaConverter.convert(form.getDataEntrega());
         final FieldAndContentBean dataEntregaBean = new FieldAndContentBean("dataEntrega", dataEntregaStr);
         response.add(dataEntregaBean);
 
         return response;
 
     }
 
     static private byte calculateFormStatus(final ConhecimentoSTY form) {
 
         if (form.isEdited()) {
             return FormContent.FORM_EDITED;
         }
         else if (form.isRead()) {
             return FormContent.FORM_READ;
         }
         else if (form.isReceived()) {
             return FormContent.FORM_RECEIVED;
         }
         else {
             return FormContent.FORM_NEW;
         }
 
     }
 
     static private byte calculateFormStatus(final NotafiscalSTY form) {
 
         if (form.isEdited()) {
             return FormContent.FORM_EDITED;
         }
         else if (form.isRead()) {
             return FormContent.FORM_READ;
         }
         else if (form.isReceived()) {
             return FormContent.FORM_RECEIVED;
         }
         else {
             return FormContent.FORM_NEW;
         }
 
     }
 
 }// class
