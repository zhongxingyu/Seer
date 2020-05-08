 package org.openmrs.module.restmodule.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.restmodule.JsonPatient;
 import org.openmrs.module.restmodule.RestUtil;
 import org.openmrs.module.restmodule.XmlPatient;
 
 /**
  * Provides a simple RESTful access to patients
  */
 public class PatientResource implements RestResource {
 
 	/**
 	 * Handle all requests to this resource
 	 */
 	public void handleRequest(Operation operation, OutputType outputType, String restRequest,
 			HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		PrintWriter out = response.getWriter();
 
 		switch (operation) {
 
 		case GET:
 			Collection<Patient> patientList = Context.getPatientService()
 					.getPatientsByIdentifier(restRequest, false);
 			
 			printPatientList(out, outputType, patientList);
 			
 			break;
 
 		case POST:
 		case PUT:
 		case DELETE:
 			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
 			return;
 		}
 
 	}
 	
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param out
 	 * @param outputType
 	 * @param patientList
 	 */
 	public static void printPatientList(PrintWriter out, OutputType outputType, Collection<Patient> patientList) {
 		if (outputType == OutputType.XML) {
 			out.print("<patientList>");
 			int i = 0;
 			int max = RestUtil.getMaxResults();
 			for (Patient patient : patientList) {
 				out.print(XmlPatient.encode(patient));
 				i++;
 				if (max > 0 && i >= max)
 					break; // if max set, abort before exceeding
 			}
 			out.print("</patientList>");
 		}
 		else if (outputType == OutputType.JSON) {
 			out.print("[");
 			int i = 0;
 			int max = RestUtil.getMaxResults();
 			for (Patient patient : patientList) {
 				out.print(JsonPatient.encode(patient));
 				i++;
 				if (max > 0 && i >= max)
 					break; // if max set, abort before exceeding
 			}
 			out.print("]");
 		}
 	}
 }
