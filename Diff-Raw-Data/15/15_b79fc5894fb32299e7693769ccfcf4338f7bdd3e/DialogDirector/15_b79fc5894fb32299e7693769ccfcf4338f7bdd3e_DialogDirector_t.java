 package com.xaf.form;
 
 import org.w3c.dom.*;
 import com.xaf.value.*;
 
 public class DialogDirector extends DialogField
 {
 	private String submitCaption = "   OK   ";
 	private String cancelCaption = " Cancel ";
 	private SingleValueSource submitActionUrl;
 	private SingleValueSource cancelActionUrl;
 
 	public DialogDirector()
 	{
 		this("director");
 	}
 
 	public DialogDirector(String name)
 	{
 		super(name, null);
 	}
 
 	public String getSubmitCaption() { return submitCaption; }
 	public void setSubmitCaption(String value) { submitCaption = value; }
 
 	public String getCancelCaption() { return cancelCaption; }
 	public void setCancelCaption(String value) { cancelCaption = value; }
 
 	public SingleValueSource getSubmitActionUrl() { return submitActionUrl; }
 	public void setSubmitActionUrl(String value)
     {
         submitActionUrl = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
 	public SingleValueSource getCancelActionUrl() { return cancelActionUrl; }
 	public void setCancelActionUrl(String value)
     {
         cancelActionUrl = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
 	public void importFromXml(Element elem)
 	{
 		super.importFromXml(elem);
 
 		String value = elem.getAttribute("style");
 		if("data".equals(value))
 			submitCaption = "  Save  ";
 		else if("confirm".equals(value))
 		{
 			submitCaption = "  Yes  ";
 			cancelCaption = "  No   ";
 		}
 
 		value = elem.getAttribute("submit-caption");
 		if(value.length() != 0)
 			submitCaption = value;
 
 		value = elem.getAttribute("cancel-caption");
 		if(value.length() != 0)
 			cancelCaption = value;
 
 		value = elem.getAttribute("submit-url");
 		if(value.length() != 0)
 			this.setSubmitActionUrl(value);
 
 		value = elem.getAttribute("cancel-url");
 		if(value.length() != 0)
 			this.setCancelActionUrl(value);
 	}
 
 	public String getControlHtml(DialogContext dc)
 	{
 		String attrs = dc.getSkin().getDefaultControlAttrs();
 
 		String submitCaption = this.submitCaption;
 		String cancelCaption = this.cancelCaption;
 
 		switch(dc.getDataCommand())
 		{
 			case DialogContext.DATA_CMD_ADD:
 			case DialogContext.DATA_CMD_EDIT:
 				submitCaption = " Save ";
 				break;
 
 			case DialogContext.DATA_CMD_DELETE:
 				submitCaption = " Delete ";
 				break;
 
 			case DialogContext.DATA_CMD_CONFIRM:
 				submitCaption = "  Yes  ";
 				cancelCaption = "  No   ";
 				break;
 		}
 
 		StringBuffer html = new StringBuffer("<center>");
 		html.append("<input type='submit' value='");
 		html.append(submitCaption);
 		html.append("' ");
 		html.append(attrs);
 		html.append(">&nbsp;&nbsp;");
 		html.append("<input type='button' value='");
 		html.append(cancelCaption);
 		html.append("' ");
 		if(cancelActionUrl == null)
 		{
 			html.append("onclick=\"document.location = '");
 	    	html.append(dc.getOriginalReferer());
 		    html.append("'\" ");
 		}
 		else
 		{
            String cancelStr = cancelActionUrl != null ? cancelActionUrl.getValue(dc) : null;
			if("back".equals(cancelStr))
			{
 				html.append("onclick=\"history.back()\" ");
			}
			else if(cancelStr != null && cancelStr.startsWith("javascript:"))
			{
				html.append("onclick=\"");
				html.append(cancelStr);
				html.append("\" ");
			}
 			else
 			{
 				html.append("onclick=\"document.location = '");
 				html.append(cancelStr);
 				html.append("'\" ");
 			}
 		}
 		html.append(attrs);
 		html.append("></center>");
 
 		return html.toString();
 	}
 }
