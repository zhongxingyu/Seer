 package hello.jaxrs;
 
 import hello.service.GreetingService;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collection;
 
 import javax.inject.Inject;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 
 /**
 * Hello World JAX-RS Resource
  */
 @Path("/greetings")
 public class GreetingsResource {
 
 	// inject OSGi service using JAX-RS @Context
 	@Context
 	private GreetingService greetingService;
 
 	// inject OSGi service using JSR 330 @Inject
 	@Inject
 	private GreetingService greetingService2;
 
 	@Context
 	private UriInfo uriInfo;
 
 	@POST
 	public Response addGreeting(@FormParam("greeting") final String greeting) {
 		if (StringUtils.isBlank(greeting)) {
 			return Response.seeOther(getUriInfo().getRequestUriBuilder().replaceQuery("errorMissingParameter").build()).build();
 		}
 
 		// post to service
 		try {
 			getGreetingService().sayHello(greeting);
 		} catch (final IllegalStateException e) {
 			// no service is available; lets report that properly
 			return Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
 		} catch (final Exception e) {
 			// this looks like an issue deeper in some underlying code; we should log this properly
 			return Response.serverError().entity(ExceptionUtils.getRootCauseMessage(e)).build();
 		}
 
 		// redirect and show success message
 		return Response.seeOther(getUriInfo().getRequestUriBuilder().replaceQuery("added").build()).build();
 	}
 
 	public GreetingService getGreetingService() {
 		return greetingService;
 	}
 
 	public GreetingService getGreetingService2() {
 		return greetingService2;
 	}
 
 	public UriInfo getUriInfo() {
 		return uriInfo;
 	}
 
 	private void printAddedMessage(final PrintWriter writer) {
 		writer.println("<p><em>");
 		writer.println("Your greeting was added successfully.");
 		writer.println("</em></p>");
 	}
 
 	private void printAddGreetingForm(final PrintWriter writer) {
 		writer.println("<form method=\"post\">");
 		writer.println("<p>");
 		writer.println("Greeting:&nbsp;<input name=\"greeting\" type=\"text\" size=\"30\" maxlength=\"30\">");
 		writer.println("<input type=\"submit\" value=\" Submit \">");
 		writer.println("</p>");
 		writer.println("</form>");
 	}
 
 	private void printErrorMessage(final PrintWriter writer, final String message) {
 		writer.println("<p><strong>");
 		writer.println(StringEscapeUtils.escapeHtml(message));
 		writer.println("</strong></p>");
 	}
 
 	private void printFooter(final PrintWriter writer) {
 		writer.println("</body>");
 		writer.println("</html>");
 	}
 
 	private void printGreetings(final PrintWriter writer, final Collection<String> greetings) {
 		writer.println("<h2>Greetings!</h2>");
 		if (greetings.isEmpty()) {
 			writer.println("No greetings available. Be the first and say hello!");
 		} else {
 			writer.println("<ul>");
 			for (String greeting : greetings) {
 				// escape HTML
 				greeting = StringEscapeUtils.escapeHtml(StringUtils.trimToEmpty(greeting));
 				// format
 				greeting = StringUtils.replaceEach(greeting, new String[] { "(", ")" }, new String[] { "<br><small>(", ")</small>" });
 
 				// print
 				writer.println("<li>");
 				writer.println(greeting);
 				writer.println("</li>");
 			}
 			writer.println("</ul>");
 		}
 	}
 
 	private void printHeader(final PrintWriter writer) {
 		writer.println("<html>");
 		writer.println("<head>");
 		writer.println("<title>Hello Cloud</title>");
 		writer.println("</head>");
 		writer.println("<body>");
 		writer.println("<h1>Welcome!</h1>");
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public Response showGreetings(@QueryParam("added") final String added, @QueryParam("errorMissingParameter") final String errorMissingParameter) {
 		// read greetings
 		Collection<String> greetings;
 		try {
 			greetings = getGreetingService().getGreetings();
 		} catch (final IllegalStateException e) {
 			// no service is available; lets report that properly
 			return Response.status(Status.SERVICE_UNAVAILABLE).type(MediaType.TEXT_PLAIN_TYPE).entity(e.getMessage()).build();
 		} catch (final Exception e) {
 			// this looks like an issue deeper in some underlying code; we should log this properly
 			return Response.serverError().type(MediaType.TEXT_PLAIN_TYPE).entity(ExceptionUtils.getFullStackTrace(e)).build();
 		}
 
 		// render HTML
 		final StringWriter stringWriter = new StringWriter();
 		final PrintWriter writer = new PrintWriter(stringWriter);
 
 		printHeader(writer);
 		if (null != added) {
 			printAddedMessage(writer);
 		}
 		if (null != errorMissingParameter) {
 			printErrorMessage(writer, "Please enter a non-empty greeting prior to submitting the form!");
 		}
 
 		printAddGreetingForm(writer);
 		printGreetings(writer, greetings);
 
 		printFooter(writer);
 		return Response.ok(stringWriter.toString(), MediaType.TEXT_HTML_TYPE).build();
 	}
 }
