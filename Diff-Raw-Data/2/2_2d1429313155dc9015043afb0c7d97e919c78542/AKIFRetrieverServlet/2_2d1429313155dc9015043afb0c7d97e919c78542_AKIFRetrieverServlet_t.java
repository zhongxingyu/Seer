 package gr.agroknow.service.akifRetriever ;
 
 import java.io.File ;
 import java.io.IOException ;
 import java.io.PrintWriter ;
 import java.util.Map ;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletException ;
 import javax.servlet.http.HttpServlet ;
 import javax.servlet.http.HttpServletRequest ;
 import javax.servlet.http.HttpServletResponse ;
 
 import org.apache.commons.io.FileUtils ;
 import org.json.simple.JSONArray ;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 
 public class AKIFRetrieverServlet extends HttpServlet
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
      
     @SuppressWarnings("unchecked")
 	private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {	
     	String jsonResponseData = null ;
     	Map<String,String[]> params = request.getParameterMap() ;
     	if ( params.containsKey( "id" ) )
     	{
     		try
     		{
     			JSONObject jRecord = (JSONObject) JSONValue.parse( getRecord( request.getParameter("id") ) ) ;
     			jsonResponseData =  jRecord.toJSONString() ;
     		}
     		catch ( Exception e )
 			{
 				System.err.println( "Cannot retrieve record #" + request.getParameter("id") + ": "  + e.getMessage() ) ;
 			}
     	}
     	else if ( params.containsKey( "ids" ) )
     	{
     		String[] ids = request.getParameter( "ids" ).split( "," ) ;
     		JSONArray jarray = new JSONArray() ;
     		for ( String id : ids )
     		{
     			try
     			{
     				JSONObject jRecord = (JSONObject) JSONValue.parse( getRecord( id ) ) ;
     				jarray.add( jRecord ) ;
     			}
     			catch ( Exception e )
     			{
     				System.err.println( "Cannot retrieve record #" + id + ": "  + e.getMessage() ) ;
     			}
     		}
     		jsonResponseData = jarray.toJSONString() ;
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
    	response.setContentType( "text/javascript; charset=UTF-8" ) ;
     	PrintWriter out = response.getWriter();
     	out.println( jsonPoutput ) ;
     	//out.println( "test( 1'2 )".replaceAll( "'", "\\\\'" ) ) ;
     }
     
     private String getRecord( String numericId ) throws IOException
     {
 
     	int identifier = Integer.parseInt( numericId ) ;
 		String path = getPathToRepository() + File.separator + this.getSubDir( identifier ) + File.separator + identifier + ".json" ;
     	File record = new File( path ) ;
     	return FileUtils.readFileToString( record, "UTF-8" ) ;
     }
     
     private String getPathToRepository() throws IOException
     {
     	try
     	{
     		Context init = new InitialContext() ;
     		Context context  = (Context)init.lookup( "java:/comp/env" ) ;
     		return (String)context.lookup( "akif.dir" ) ;
     	}
     	catch( NamingException ne )
     	{
     		//return "/home/workflow/repository/AKIF" ;
         	//return "/Users/dmssrt/tmp/transformer/AKIF" ;
     		throw new IOException( "Cannot look up path to AKIF repository !" ) ;
     	}
     }
     
     private String getSubDir( int identifier )
     {
     	String result = "" + ( identifier % 1000 ) ;
     	if (result.length() == 1)
     	{
     		result = "00" + result ;
     	}
     	if (result.length() == 2)
     	{
     		result = "0" + result ;
     	}
     	return result ;
     }
    
 }
