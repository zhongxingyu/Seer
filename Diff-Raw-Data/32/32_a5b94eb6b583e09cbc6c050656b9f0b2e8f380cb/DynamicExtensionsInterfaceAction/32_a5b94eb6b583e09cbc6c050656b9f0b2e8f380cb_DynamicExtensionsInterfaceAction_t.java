 
 package edu.common.dynamicextensions.interfaceactions;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 
 public class DynamicExtensionsInterfaceAction extends HttpServlet
 {
 	/**
	 * Do get method calls do post method
	 * @param req HttpServletRequest
 	 * @param res HttpServletResponse 
	 * @throws ServletException servlet exception
	 * @throws IOException io exception
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		doPost(req, res);
 	}
 
 	/**
	 * Do post method of the servlet
	 * @param req HttpServletRequest
 	 * @param res HttpServletResponse 
	 * @throws ServletException servlet exception
	 * @throws IOException io exception
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getInputStream()));
 		Object json = null;
 		try
 		{
 			json = bufferedReader.readLine();
 			bufferedReader.close();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		JSONObject requestObject = new JSONObject(json.toString());
 
 		JSONArray entityInterfaceJSONArray = new JSONArray();
 
 		//check which operation needs to be performed
 		String operation = requestObject.getString("operation");
 
 		//depending upon the operation execute the appropriate steps
 		if (operation.equalsIgnoreCase("getAllEntities"))
 		{
 
 			EntityManager entityManager = EntityManager.getInstance();
 			Collection entityCollection = null;
 			try
 			{
 				entityCollection = entityManager.getAllEntities();
 			}
 			catch (DynamicExtensionsSystemException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			if (entityCollection != null)
 			{
 				Iterator entityIterator = entityCollection.iterator();
 				EntityInterface entityInterface;

 				while (entityIterator.hasNext())
 				{
					JSONObject entityInterfaceJSONObject = new JSONObject();
 					entityInterface = (EntityInterface) entityIterator.next();
 					entityInterfaceJSONObject.put("entityName", entityInterface.getName());
 					entityInterfaceJSONObject.put("entityIdentifier", entityInterface.getId());
 
 					entityInterfaceJSONArray.put(entityInterfaceJSONObject);
 				}
 			}
 
 			res.setContentType("text/javascript");
 			PrintWriter out = res.getWriter();
 			out.write(entityInterfaceJSONArray.toString());
 		}
 
 	}
 }
