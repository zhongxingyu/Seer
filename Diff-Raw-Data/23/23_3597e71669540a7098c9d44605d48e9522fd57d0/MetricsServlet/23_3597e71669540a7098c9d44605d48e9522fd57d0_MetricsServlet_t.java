 package de.lbe.sandbox.metrics.servlet;
 
 import com.codahale.metrics.servlets.AdminServlet;
 
 /**
 * We cannot add the annotation because TomEE tries to deploy the servlet twice if it comes from a jar files with results in an exception (seems to be a bug in TomEE). Tomcat
 * doesn't have these problems.
 * 
  * @author lbeuster
  */
// @WebServlet(urlPatterns = "/admin/metrics/*")
 public class MetricsServlet extends AdminServlet {
	// no impl
 }
