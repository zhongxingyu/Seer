 /**
  * 
  */
 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.upload.FormFile;
 
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.ui.webui.actionform.ControlsForm;
 
 /**
  * @author chetan_patil
  *
  */
 public class UploadFileAction extends BaseDynamicExtensionsAction
 {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.apache.struts.actions.DispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws DynamicExtensionsApplicationException
 	{
 		ControlsForm controlsForm = (ControlsForm) form;
 		FormFile file = controlsForm.getCsvFile();
 
 		String totalRows = request.getParameter("totalRows");
 		int rowNos = 0;
 		if (totalRows != null || totalRows != "")
 		{
 			rowNos = Integer.parseInt(totalRows);
 		}
 
 		StringBuffer returnXML = new StringBuffer();
 		try
 		{
 			if (file != null && file.getFileSize() > 0)
 			{
 				byte[] fileContents = file.getFileData();
 				String fileContentString = new String(fileContents);
 				String[] rowsStrings = fileContentString.split("\n");
 
 				for (int i = 0; i < rowsStrings.length; i++)
 				{
 					rowsStrings[i] = rowsStrings[i].trim();
 
 					StringBuffer tempRowString = new StringBuffer();
 					int firstfoundAt = 0;
 					int lastFoundAt = 0;
 					while (rowsStrings[i].indexOf("\"") != -1)
 					{
 						firstfoundAt = rowsStrings[i].indexOf("\"");
 						lastFoundAt = rowsStrings[i].indexOf("\"", firstfoundAt + 1);
 
 						if (firstfoundAt > 0)
 						{
 							tempRowString.append(rowsStrings[i].substring(0, firstfoundAt));
 						}
 
 						if (firstfoundAt != -1 && lastFoundAt != -1)
 						{
 							String columnValue = rowsStrings[i]
 									.substring(firstfoundAt, lastFoundAt);
 
 							columnValue = columnValue.replaceFirst("\"", "");
 							columnValue = columnValue.replace(",", " ");
 
 							tempRowString.append(columnValue);
 						}
 						rowsStrings[i] = rowsStrings[i].substring(lastFoundAt + 1);
 					}
 
 					if (rowsStrings[i].length() > 0)
 					{
 						tempRowString.append(rowsStrings[i]);
 					}
 
 					if (tempRowString.length() > 0)
 					{
 						rowsStrings[i] = tempRowString.toString();
 					}
 					rowsStrings[i] = rowNos++ + ",," + rowsStrings[i];
 					returnXML.append(rowsStrings[i] + "|");
 				}
 			}
 
			request.setAttribute("xmlString", returnXML.toString().trim());
 		}
 		catch (Exception e)
 		{
 			String actionForwardString = catchException(e, request);
 			if ((actionForwardString == null) || (actionForwardString.equals("")))
 			{
 				return mapping.getInputForward();
 			}
 			return (mapping.findForward(actionForwardString));
 		}
 		return mapping.findForward("success");
 	}
 
 }
