 package ecologylab.semantics.metametadata.services;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.tidy.Tidy;
 
 import ecologylab.net.ParsedURL;
 import ecologylab.semantics.collecting.SemanticsSessionScope;
 import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
 import ecologylab.semantics.metametadata.MetaMetadata;
 import ecologylab.semantics.metametadata.MetaMetadataRepository;
 import ecologylab.semantics.metametadata.services.responses.MetaMetadataNameListResponse;
 import ecologylab.serialization.ElementState.FORMAT;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 
 
 @SuppressWarnings("serial")
 public class MMDJsonRepoServlet extends HttpServlet
 { 
 
 	private static TranslationScope mmdTScope;
 	private static MetaMetadataRepository repo;
 	static
 	{
 		TranslationScope.setGraphSwitch(); 
 		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), Tidy.class);
 		repo = infoCollector.getMetaMetadataRepository();
 	}
 
 	/**
 	 * 
 	 */
 	public MMDJsonRepoServlet()
 	{
 
 	}
 	
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
   		
   		response.addHeader("Access-Control-Allow-Origin", "*");
   		//As per gaurav, this header is required for the authoring tool.
   		
       response.setContentType("text/html");
       response.setStatus(HttpServletResponse.SC_OK);
       
       String purlParam = request.getParameter("purl");
       String mmdNameParam = request.getParameter("mmdName");
       String getMMDListParam = request.getParameter("getMMDList");
   		ParsedURL purl = ParsedURL.getAbsolute(purlParam);
   		PrintWriter printWriter;
   		if(purl != null )
   		{
   			sendMMDJsonForPurl(response, purl);
   		}
   		else if(getMMDListParam != null)
   		{
     		sendMMNameList(response);
   		}
   		else if(mmdNameParam != null && mmdNameParam.length() > 0 )
   		{
   			sendMMDByName(response, mmdNameParam);
   		}
   		else
   		{
   			printWriter = response.getWriter();
 				printWriter.append("Invalid request parameter");
 				printWriter.close();
   			//append("Invalid Purl");
   		}
   		
   }
 
   /**
    * 
    * @param response
    * @param mmdNameParam
    */
   private void sendMMDByName(HttpServletResponse response, String mmdNameParam)
 	{
   	MetaMetadata mmByName = repo.getMMByName(mmdNameParam);
   	
 		sendMM(response, mmByName);
 	}
 	
   /**
    * 
    * @param response
    */
   private void sendMMNameList(HttpServletResponse response)
 	{
 		ArrayList<String> mmdNameList = repo.getMMNameList();
 		if(mmdNameList != null && ! mmdNameList.isEmpty() )
 		{
 			
 
 			try
 			{
 				MetaMetadataNameListResponse mmdList = new MetaMetadataNameListResponse( mmdNameList);
 				OutputStream writer = response.getOutputStream();
 				mmdList.serialize(writer, FORMAT.JSON);
 			}
 			catch (SIMPLTranslationException e)
 			{
 				e.printStackTrace();
 			}
 			catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
   
   /**
    * 
    * @param response
    * @param purl
    * @throws IOException
    */
 	private void sendMMDJsonForPurl(HttpServletResponse response, ParsedURL purl) throws IOException
 	{
 		MetaMetadata requestedMM = repo.getDocumentMM(purl);
 
 		sendMM(response, requestedMM);
 
 		return;
 	}
 	
 	/**
 	 * 
 	 * @param response
 	 * @param requestedMM
 	 */
 	private void sendMM(HttpServletResponse response, MetaMetadata requestedMM)
 	{
 		PrintWriter printWriter;
 
 		try
 		{
 			
 			if(requestedMM == null)
 			{
 				printWriter = response.getWriter();
 				printWriter.append("No MM found");
 				printWriter.close();
 				return;
 			}
 		
 			OutputStream writer = response.getOutputStream();
 			System.out.println("Serializing MMD " + requestedMM);
 			//requestedMM.serialize(System.out, FORMAT.JSON);
 			requestedMM.serialize(writer, FORMAT.JSON);
 		}
 		catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
   
 }
