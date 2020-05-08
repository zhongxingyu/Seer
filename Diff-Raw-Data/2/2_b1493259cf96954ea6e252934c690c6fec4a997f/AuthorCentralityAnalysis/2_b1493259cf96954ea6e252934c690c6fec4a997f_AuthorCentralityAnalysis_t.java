 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.analytics.socialnetworking;
 
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.velocity.VelocityContext;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.html.VelocityTemplateService;
 import org.seasr.meandre.support.generic.text.analytics.AnalyticsUtils;
 
 import edu.uci.ics.jung.algorithms.importance.AbstractRanker;
 import edu.uci.ics.jung.algorithms.importance.NodeRanking;
 import edu.uci.ics.jung.graph.Graph;
 
 /**
  *  This class calculates author centrality based on a list of authors per entry.
  *  Originally developed to deal with author data from a Zotero RDF.
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  */
 
 @Component(
 		creator = "Xavier Llora",
 		description = "Given a collection of authors, grouped by publication, this component "+
 		              "generates a report based on the social network analysis. This analysis uses the JUNG "+
 		              "network importance algorithms to rank the authors. This component uses Betweenness Centrality, which " +
 		              "ranks each author in the author-citation graph derived from the number of "+
 		              "shortest paths that pass through them.",
 		name = "Author Centrality Analysis",
 		tags = "author, betweenness centrality, social network analysis",
 		rights = Licenses.UofINCSA,
 		mode = Mode.compute,
 		firingPolicy = FiringPolicy.all,
 		baseURL = "meandre://seasr.org/components/foundry/",
 		dependency = {"protobuf-java-2.2.0.jar"},
         resources = {"AuthorCentralityAnalysis.vm"}
 )
 public class AuthorCentralityAnalysis extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 	        name = Names.PORT_AUTHOR_LIST,
 			description = "A list of vectors containing the names of the authors. There is one vector for each entry." +
 			    "<br>TYPE: java.util.List<java.util.Vector<java.lang.String>>"
 	)
 	protected static final String IN_AUTHOR_LIST = Names.PORT_AUTHOR_LIST;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 	        name = Names.PORT_HTML,
 			description = "A report of the social network analysis." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_HTML_REPORT = Names.PORT_HTML;
 
 	@ComponentOutput(
 	        name = Names.PORT_GRAPH,
 			description = "The graph generated." +
 			    "<br>TYPE: edu.uci.ics.jung.graph.Graph"
 	)
 	protected static final String OUT_GRAPH = Names.PORT_GRAPH;
 
 	//------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
            defaultValue = "org/seasr/meandre/components/analytics/socialnetworking/AuthorCentralityAnalysis.vm",
             description = "The template to use for wrapping the HTML input",
             name = Names.PROP_TEMPLATE
     )
     protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;
 
     //--------------------------------------------------------------------------------------------
 
 
     final String AUTHOR = "Author";
 
     private VelocityContext _context;
     private String _template;
 
     public class AuthorRanking {
         private final String _authorName;
         private final double _rankScore;
 
         public AuthorRanking(String authorName, double rankScore) {
             _authorName = authorName;
             _rankScore = rankScore;
         }
 
         public String getAuthorName() {
             return _authorName;
         }
 
         public double getRankScore() {
             return _rankScore;
         }
     }
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    _template = ccp.getProperty(PROP_TEMPLATE);
 
         _context = VelocityTemplateService.getInstance().getNewContext();
         _context.put("ccp", ccp);
 	}
 
 	@Override
     @SuppressWarnings("unchecked")
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		List<Vector<String>> listAuthors =
 		    (List<Vector<String>>) cc.getDataComponentFromInput(IN_AUTHOR_LIST);
 
 		Graph g = AnalyticsUtils.buildGraph(listAuthors, AUTHOR);
 		AbstractRanker bc = AnalyticsUtils.computeBetweenness(g);
 
 		List<AuthorRanking> rankings = new Vector<AuthorRanking>();
         for ( Object or : bc.getRankings() ) {
             NodeRanking nr = (NodeRanking) or;
             rankings.add(new AuthorRanking(nr.vertex.getUserDatum(AUTHOR).toString(), nr.rankScore));
         }
 
 		cc.pushDataComponentToOutput(OUT_GRAPH, g);
 		cc.pushDataComponentToOutput(OUT_HTML_REPORT,
 		        BasicDataTypesTools.stringToStrings(generateReport(rankings)));
 	}
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 
     //--------------------------------------------------------------------------------------------
 
     private String generateReport(List<AuthorRanking> rankings) throws Exception {
         VelocityTemplateService velocity = VelocityTemplateService.getInstance();
         _context.put("centralityRankings", rankings);
         return velocity.generateOutput(_context, _template);
     }
 }
