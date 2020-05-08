 package org.benetech.mde.servlet;
 
 //import org.apache.commons.lang3.StringUtils;
 
 import gov.nasa.ial.mde.util.ResourceUtil;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.benetech.mde.bean.GraphDescriptionBean;
 import org.benetech.mde.compute.GraphDescriber;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 //@WebServlet("/MdeDescribeEquation")
 public class GetEquationDescription extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private String optionalMode = "standards";
 	private String optionalFormat = "text";
 	private String optionalResponseType = "text";
 
 	public GetEquationDescription() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setHeader("Cache-Control", "no-cache");
 		response.setHeader("Pragma", "no-cache");
 		//TODO: Set Access-Control-Allow-Origin
 
 		// Get input parameters
 		String equation = request.getParameter("equation");
 		String mdeFormatOut = request.getParameter("mdeFormatOut");
 		String descriptionMode = request.getParameter("descriptionMode");
 		String responseFormat = request.getParameter("responseFormat");
 
 		if ((equation != null) && (equation != "")) {
 			equation = equation.toLowerCase();
 
 			if (mdeFormatOut != null)
 				mdeFormatOut = mdeFormatOut.toLowerCase();
 			else
 				mdeFormatOut = "text";
 			if (descriptionMode != null)
 				descriptionMode = descriptionMode.toLowerCase();
 			else
 				descriptionMode = "standards";
 			if (responseFormat != null)
 				responseFormat = responseFormat.toLowerCase();
 			else
 				responseFormat = "text";
 
 			// System.out.println("responseFormat: " + responseFormat);
 			// System.out.println("responseFormat null?  " + (responseFormat ==
 			// null));
 			// System.out.println("responseFormat blank? " + (responseFormat ==
 			// ""));
 
 			// If mde options are specified, use them
 			if (mdeFormatOut.equals("text") || mdeFormatOut.equals("html")
 					|| mdeFormatOut.equals("xml"))
 				optionalFormat = mdeFormatOut;
 			if (descriptionMode.equals("visual")
 					|| descriptionMode.equals("math")
 					|| descriptionMode.equals("standards"))
 				optionalMode = descriptionMode;
 			if (responseFormat.equals("text")
 					|| responseFormat.equals("textbean")
 					|| responseFormat.equals("json")
 					|| responseFormat.equals("svg")
 					|| responseFormat.equals("svgfile")
 					|| responseFormat.equals("mp3file")
 					|| responseFormat.equals("jsonp"))
 				optionalResponseType = responseFormat;
 
 			// Instantiate GraphDescriber helper class
 			GraphDescriber describer = new GraphDescriber(equation,
 					optionalFormat, optionalMode);
 
 			// If response type qualifies as text
 			// Set up text response writer
 			// Compute the desired response
 			// Write the response
 			if (optionalResponseType.equals("text")
 					|| optionalResponseType.equals("textbean")
 					|| optionalResponseType.equals("json")
 					|| optionalResponseType.equals("jsonp")
 					|| optionalResponseType.equals("svg")) {
 
 				PrintWriter out = response.getWriter();
 				response.setStatus(200);
 
 				if (optionalResponseType.equals("text")) {
 					response.setContentType("text/html");
 					String text = describer.getTextDescription();
 					out.print(text);
 				} else if (optionalResponseType.equals("textbean")) {
 					response.setContentType("text/html");
 					GraphDescriptionBean bean = describer
 							.getTextDescriptionBean();
 					out.print(bean);
 				} else if (optionalResponseType.equals("json")) {
 					response.setContentType("application/json");
 					JSONObject respJson = describer.getJSONDescription();
 					out.print(respJson);
 				} else if (optionalResponseType.equals("jsonp")) {
 					response.setContentType("application/json");
 					final JSONObject respJson = new JSONObject();
 					try {
 						respJson.put("description", describer.getJSONDescription());
 						respJson.put("svg", describer.getGraphSVG());
 						out.print("updateEquationDescription(" + respJson + ")");
 					} catch (JSONException e) {
 						response.setStatus(400);
 						response.setContentType("text/html");
 						response.sendError(400, "Bad Request.  Error generating json response.");
 					}
 				} else if (optionalResponseType.equals("svg")) {
 					response.setContentType("text/html");
 					String SVGout = describer.getGraphSVG();
 					out.print(SVGout);
 				}
 
 				// HTML out for testing only. Comment out for production.
 				// ====================
 				// out.println("<html>");
 				// out.println("<head></head><body>");
 				// out.println("<form method='get' action='/Mde-Web-Service/GetEquationDescription'>");
 				// out.println("Enter an equation: ");
 				// out.println("<input type='text' name='equation'>");
 				// out.println("<p>Enter mde output format: text, html, xml:");
 				// out.println("<input type='text' name='mdeFormatOut'>");
 				// out.println("<p>Enter mde description mode: visual,math,standards");
 				// out.println("<input type='text' name='descriptionMode'>");
 				// out.println("<p>Enter response format : text,json,textbean,svg,svgfile");
 				// out.println("<input type='text' name='optionalResponseType'>");
 				// out.println("<input type='submit'>");
 				// out.println("</form>");
 				// out.println("</head></body>");
 				// out.println("</html>");
 				// end test convenience code
 				// ======================================================================
 
 				out.close();
 
 				// Else if response type is binary
 				// Set up binary writer
 				// Compute response
 				// Write the response
 
 			} else if (optionalResponseType.equals("svgfile")
 					|| optionalResponseType.equals("mp3file")) {
 
 				ServletOutputStream servOut = response.getOutputStream();
				response.setStatus(200);
 
 				if (optionalResponseType.equals("svgfile")) {
 					response.setHeader("Content-Disposition",
 							"attachment; filename=graph.svg");
 					response.setContentType("image/svg+xml");
					String SVGFileContents = describer.getGraphSVG();
 					String graphFilePath = getServletContext().getRealPath("/")
 							+ "graph.svg";

 					ResourceUtil.saveFile(graphFilePath,
 							SVGFileContents.getBytes());
 					servOut.write(ResourceUtil.readFile(graphFilePath));
 				}
 				// else if (optionalResponseType.equals("mp3file")) {
 				// // TODO: Implement
 				// ;
 				// }
 
 				servOut.close();
 			} 
 		} 
 			else { // equation is null. error out
			response.setStatus(400);
 			response.setContentType("text/html");
			response.sendError(400, "Bad Request.  Equation is required input.");
 		}
 
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 }
