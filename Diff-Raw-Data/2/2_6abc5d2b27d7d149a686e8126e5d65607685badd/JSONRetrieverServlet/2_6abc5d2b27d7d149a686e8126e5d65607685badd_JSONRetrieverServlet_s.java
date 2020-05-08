 package gr.agroknow.service.jsonRetriever ;
 
 import java.io.File ;
 import java.io.IOException ;
 import java.io.InputStream;
 import java.io.PrintWriter ;
 import java.net.URL;
 import java.util.Map ;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletException ;
 import javax.servlet.http.HttpServlet ;
 import javax.servlet.http.HttpServletRequest ;
 import javax.servlet.http.HttpServletResponse ;
 
 import org.apache.commons.io.FileUtils ;
 import org.apache.commons.io.IOUtils;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 
 public class JSONRetrieverServlet extends HttpServlet
 {
    // NOTE: JSONP works only with GET request
 	
 	private static final long serialVersionUID = 1L ;
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {
     	execute( request, response ) ;
     }
     
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {
     	execute( request, response ) ;
     }
      
     private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {	
     	String jsonResponseData = null ;
     	Map<String,String[]> params = request.getParameterMap() ;
     	if ( params.containsKey( "url" ) )
     	{
     		String url = request.getParameter( "url" ) ;
     		try
     		{
     			String text = null ;
     			InputStream in = new URL( url ).openStream() ;
     			try 
     			{
     				text = IOUtils.toString( in, "UTF-8" ) ;
     			}
     			finally
     			{
     				IOUtils.closeQuietly( in ) ;
     			}
     			Object obj = JSONValue.parse( text ) ;
     			try
     			{
     				JSONObject jRecord = (JSONObject)obj ; 
     				jsonResponseData =  jRecord.toJSONString() ;
     			}
     			catch( ClassCastException cce )
     			{
     				JSONArray jArray = (JSONArray)obj ; 
     				jsonResponseData =  jArray.toJSONString() ;
     			}
     		}
     		catch ( Exception e )
 			{
 				System.err.println( "Cannot retrieve URL" + request.getParameter("url") + ": "  + e.getMessage() ) ;
 			}
     	}
 
     	String jsonPoutput ;
     	if ( params.containsKey( "callback" ) )
     	{
     		// escape "'", "(" and ")"
     		jsonResponseData = jsonResponseData.replaceAll( "'", "\\\\'" ) ;
     		jsonResponseData = jsonResponseData.replaceAll( "\\)", "\\\\)" ) ;
     		jsonResponseData = jsonResponseData.replaceAll( "\\(", "\\\\(" ) ;
     		jsonResponseData = jsonResponseData.replaceAll( "\\\\r", " " ) ;
         	String callBackJavaScripMethodName = request.getParameter( "callback" ) ;
         	jsonPoutput = callBackJavaScripMethodName + "('"+ jsonResponseData + "');";
     	}
     	else
     	{
     		jsonPoutput = jsonResponseData ;
     	}
 
     	//Write it to Response  
    	response.setContentType( "text/javascript" ) ;
     	PrintWriter out = response.getWriter();
     	out.println( jsonPoutput ) ;
     	//out.println( "test( 1'2 )".replaceAll( "'", "\\\\'" ) ) ;
     }
     
 }
