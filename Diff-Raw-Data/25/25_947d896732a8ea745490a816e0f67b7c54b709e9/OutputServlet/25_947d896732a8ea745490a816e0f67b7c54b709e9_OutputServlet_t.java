 package eu.stratosphere.meteor.client.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import eu.stratosphere.meteor.client.common.HDFSUtil;
 import eu.stratosphere.meteor.client.common.MeteorContextHandler;
import eu.stratosphere.nephele.util.StringUtils;
import eu.stratosphere.util.StringUtil;
 
 /**
  * 
  * The output servlet shows the JSON script of pact plan
  * 
  * @author André Greiner-Petter
  *
  */
 public class OutputServlet extends AbstractServletGUI {
 	
 	/**
 	 * Generated UID
 	 */
 	private static final long serialVersionUID = 3588555832578499570L;
 	
 	/**
 	 * Output files of the meteor script
 	 */
 	private List<String> outputs;
 	
 	/**
 	 * Index of selected file
 	 */
 	private int selectionidx = 0;
 	
 	/**
 	 * Is job currently running or not for more consistency
 	 */
 	private boolean jobIsRunning = false;
 	
 	/**
 	 * Construct a new 
 	 */
 	public OutputServlet(){
 		super( "output" );
 		addJavaScript( "jsonHighlighting.js" );
 		addStylesheet( "jsonHighlightBrushes.css" );
 		addStylesheet( "meteorFrontend.css" );
 	}
 	
 	/**
 	 * Updates content to set new scripts.
 	 * @param map contains all scripts
 	 */
 	public void update(List<String> outputs){
 		this.outputs = outputs;
 		this.selectionidx = 0;
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		@SuppressWarnings("rawtypes")
 		Enumeration params = request.getParameterNames();
 		while (params.hasMoreElements()) {
 			String param = params.nextElement().toString();
 			System.out.println(param + " --- " + request.getParameter(param));
 		}
 		
 		// get selected script index
 		String idxstring = request.getParameter("selectionidx");
 		if (idxstring != null) {
 			int idx = Integer.parseInt(idxstring);
 			if (0 <= idx && idx < outputs.size()) {
 				selectionidx = idx;
 				System.out.println("set selection: " + selectionidx);
 			}
 		}
 		
 		// get status of job before reload
 		jobIsRunning = MeteorContextHandler.isInProgress();
 		
 		// print html
 		super.doGet(request, response);
 	}
 
 	@Override
 	protected void writePage(PrintWriter writer) {
 		// write table
 		writer.println("<form action=\"/output\" method=\"get\" accept-charset=\"UTF-8\">");
 		writer.println("  <div class=\"main\">");
 		
 		// table header
 		writer.println("    <h1>Output</h1>");
 		
 		// selections
 		writer.println("    <div align=\"center\">");
 		writer.println("      <select id=\"selectionidx\" name=\"selectionidx\" class=\"scriptSelection\" size=\"1\" onChange=\"this.form.submit()\">");
 		organizeSelections( writer );
 		writer.println("      </select>");
 		writer.println("    </div>");
 		
 		// output area
 		if ( !MeteorContextHandler.finishedWithError() ){
 			writer.println("    <pre id=\"script\"><span class=\"placeHolder\">The output block for JSON script!</span></pre>");
 		} else { writer.println("    <pre id=\"script\"><span class=\"error\">Job failed. Please check /error sub page for more informations.</span></pre>"); }
 		
 		if ( jobIsRunning ){
 			writer.println("    <div class=\"footer\" align=\"center\"><progress style=\"width:50%\"></progress></div>");
 		} else { writer.println("    <div class=\"footer\" align=\"right\"><a href=\"/runtime?viewMode=view\" target=\"_blank\">Job States</a></div>"); }
 		
 		writer.println("  </div>");
 		writer.println("</form>");
 		
 		// open a new window shows the status of query
 		if ( jobIsRunning ){
 			writer.println("<script type=\"text/javascript\">window.open(\"/runtime?viewMode=process\", \"_blank\");</script>");
 		}
 		
 		// inner script
 		writeHighlightScript( writer );
 	}
 	
 	/**
 	 * Write selectable scripts
 	 * @param writer
 	 */
 	private void organizeSelections( PrintWriter writer ){
 		// selection values
 		if ( outputs != null && !jobIsRunning ) {
 			for ( int i = 0; i < outputs.size(); i++ ) {
 				writer.print("        ");
 				
 				// option header
 				writer.print("<option value=\"" + i + "\" ");
 				
 				// if this option is selected
 				if (i == selectionidx) writer.print("selected=\"selected\" ");
 				
 				// option footer
 				writer.print('>');
 				writer.print( outputs.get(i) );
 				writer.println( "</option>" );
 			}
 		}
 	}
 	
 	/**
 	 * Write a inner script that fill the script pre-block with JSON code or an error message
 	 * @param writer
 	 */
 	private void writeHighlightScript( PrintWriter writer ){
 		// script header
 		writer.println("<script type=\"text/javascript\">");
 		
 		// script or error message
 		try {
 			if ( outputs != null && !jobIsRunning ) {
 				writer.print("  var obj = ");
 				HDFSUtil.getHDFSContent(outputs.get(selectionidx), writer);
 				writer.println(";");
 				writer.println("  var str = JSON.stringify(obj, null, 4);");
 				writer.println("  output( syntaxHighlight(str) );");
 			}
 		} catch (Exception e) {
 			writer.println("\"Error accessing HDFS. See /error sub pages for more informations.\";");
 			writer.println("  output( obj );");
 			
 			// create error string
			ErrorServlet.setError( StringUtils.stringifyException(e) );
 		}
 		
 		// script footer
 		writer.println("</script>");
 	}
 }
