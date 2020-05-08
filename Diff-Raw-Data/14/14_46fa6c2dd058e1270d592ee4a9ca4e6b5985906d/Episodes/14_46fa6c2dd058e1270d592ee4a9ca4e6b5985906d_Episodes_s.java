 package EvilJohn.Api.MetaPlay;
 
 import java.util.*;
 
 import com.fourspaces.couchdb.*;
 
 import EvilJohn.Common.*;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 
 @Path("/episodes")
 public class Episodes {
 	@Context HttpServletRequest httpServletRequest;
 	@Context static ServletContext servletContext;
 	
 	private final static String couchUrl = "localhost";
 	private DBManager dbManager;
 	
 	public Episodes()
 	{
//		couchUrl = servletContext.getInitParameter("couchUrl");
 		dbManager = DBManagerFactory.Create(couchUrl, "episodes");
		System.out.println("Episodes constructor");
 	}
 	
 	static
 	{
		System.out.println("Episodes static field");
 	}
 	
 	
 	//Demo code
 //	{	
 //		Document document = dbManager.getDocumentById("someId");
 //		document.put("prop","value");
 //		dbManager.saveDocument(document);
 //
 //		Document newDocument = new Document();
 //		newDocument.put("newProp","newValue");
 //		dbManager.saveDocument(newDocument); // auto-generated id given by the database
 //	}
 //	
 //	{
 //		// Running a view
 //		ViewResults result = dbManager.getAllDocuments(); // same as db.view("_all_dbs");
 //		for (Document document: result.getResults()) {
 //			/*
 //				ViewResults don't actually contain the full document, only what the view
 //				returned.  So, in order to get the full document, you need to request a
 //				new copy from the database.
 //			*/
 //			Document fullDocument = dbManager.getDocumentById(document.getId());
 //			System.out.println(fullDocument.getId());
 //		}
 //		
 //		// Ad-Hoc view
 //		ViewResults resultAdHoc = dbManager.adHocView("function (doc) { if (doc.prop=='value') { emit(null, doc); }}");
 //		for (Document document: resultAdHoc.getResults()) {
 //			Document fullDocument = dbManager.getDocumentById(document.getId());
 //			System.out.println(fullDocument.getId());
 //		}
 //	}
 	
 	@GET
 	@Path("/latest")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response latest(@QueryParam("pageIndex") long pageIndex, @QueryParam("pageSize") long pageSize)
 	{
		System.out.println("Episodes latest");
 		if(0 >= pageSize)
 		{
 			pageSize = 20;
 		}
 		
 		List<Document> documents = new ArrayList<Document>();
 		
 		try
 		{
 			ViewResults result = dbManager.AdHocView("function (doc) { emit(null, doc); }");
 			documents = result.getResults();
 		}
 		catch (Exception ex)
 		{
 			//ToDo: Log.
			System.out.println(ex.getMessage());
			System.out.println(ex.getStackTrace());

//			return Response.status(404).build();
 		}
 		
 		return Response.ok().entity(documents).build();
 	}
 	
 	@GET
 	@Path("/{show}/latest")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response latestPerShow(@PathParam("show") String id)
 	{
 		return Response.ok().entity(new Object()).build();
 	}
 }
