 /****************************************************************
  *
  * Copyright (c) 2011, 2012
  *
  * School of Engineering, Cardiff University, UK
  *
  * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *
  * Project name: srs EU FP7 (www.srs-project.eu)
  * ROS stack name: NA
  * ROS package name: knowledge_server
  * Description: At the stage, only for testing to validate some functions. 
  *
  * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *
  * @author Ze Ji, email: jiz1@cf.ac.uk
  *
  * Date of creation: March 2012
  * ToDo: 
  *
  * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *	 * Redistributions of source code must retain the above copyright
  *	   notice, this list of conditions and the following disclaimer.
  *	 * Redistributions in binary form must reproduce the above copyright
  *	   notice, this list of conditions and the following disclaimer in the
  *	   documentation and/or other materials provided with the distribution.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License LGPL as 
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License LGPL for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public 
  * License LGPL along with this program. 
  * If not, see <http://www.gnu.org/licenses/>.
  *
  ****************************************************************/
 
 package org.srs.knowledge_server.ros;
 
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.vocabulary.*;
 import com.hp.hpl.jena.util.FileManager;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.shared.JenaException;
 import ros.*;
 import ros.communication.*;
 import ros.pkg.knowledge_server.srv.QuerySparQL;
 import ros.pkg.knowledge_server.srv.SparQLRule;
 import ros.pkg.knowledge_server.srv.*;
 
 import com.hp.hpl.jena.rdf.model.Statement;
 import org.srs.knowledge_server.ontology.*;
 
 import java.util.Properties;
 
 import java.io.*;
 import java.util.StringTokenizer;
 import java.util.ArrayList; 
 import java.util.Iterator;
 
 public class KnowledgeServer
 {
     public static Ros ros;
     public static OntologyDB ontoDB;
 
     public static NodeHandle nodeHandle;
 
     public KnowledgeServer()
     {
 	this.defaultContextPath();
     }
 
     public KnowledgeServer(String contextPath)
     {
 	this.setContextPath(contextPath);
     }
 
     public void setContextPath(String path)
     {
 	this.confPath = path;
     }
 
     public boolean init(String cfgFile)
     {
 	try {
 	    initProperties(cfgFile);
 	}
 	catch(Exception e) {
 	    System.out.println(e.getMessage());
 	    return false;
 	}
 	
 	ros = Ros.getInstance();
 	ros.init(nodeName);
 	ros.logInfo("INFO: Start RosJava_JNI service");
 	
 	nodeHandle = ros.createNodeHandle();
 
 	try{
 	    this.initQuerySparQL();
	    this.initSparQLRule();
 	}
 	catch(RosException e){
 	    System.out.println(e.getMessage());
 	    return false;
 	}
 
 	ros.spin();
 
 	return true;
     }
 
     private ArrayList<String> parseOntologyFileNames(String names)
     {
 	ArrayList<String> nameList = new ArrayList<String>();
 
 	StringTokenizer st = new StringTokenizer(names, " ");
 	
 	while(st.hasMoreTokens()) {
 	    nameList.add(this.confPath + "/" + st.nextToken());
 	}
 	for(String v: nameList) {
 	    System.out.println(v);
 	}
 	return nameList;
     }
     
     private void initProperties(String cfgFile) throws Exception
     {
 	InputStream is = new FileInputStream(cfgFile);
 	this.config = new Properties();
 	this.config.load(is);
 
 	String ontoDBFile = config.getProperty("ontologyFile", "house.owl");
 
 	ArrayList<String> nameList = parseOntologyFileNames(ontoDBFile);
 
 	ontoDB = new OntologyDB(nameList);
 
 	this.nodeName = config.getProperty("nodename", "knowledge_srs_node");
     }
     
     private QuerySparQL.Response handleQuerySparQL(QuerySparQL.Request req)
     {
 	QuerySparQL.Response re = new QuerySparQL.Response();
 	String queryString = req.query;
 	System.out.println(queryString);
 
 	re.result = ontoDB.executeQuery(queryString);
 	return re;
     }
 
     private void initQuerySparQL() throws RosException
     {
 	ServiceServer.Callback<QuerySparQL.Request, QuerySparQL.Response> scb = new ServiceServer.Callback<QuerySparQL.Request, QuerySparQL.Response>() {
             public QuerySparQL.Response call(QuerySparQL.Request request) {
 		return handleQuerySparQL(request);
             }
 	};
 
 	ServiceServer<QuerySparQL.Request, QuerySparQL.Response, QuerySparQL> srv = nodeHandle.advertiseService( querySparQLService , new QuerySparQL(), scb);
     }
 
     private ConstructRule.Response handleConstructSparQL(ConstructRule.Request req)
     {
 	ConstructRule.Response re = new ConstructRule.Response();
 	String queryString = req.constructRule;
 	System.out.println(queryString);
 
 	boolean res = ontoDB.executeConstructQuery(queryString);
 	re.result = res;
 	return re;
     }
 
     private void initConstructSparQL() throws RosException
     {
 	ServiceServer.Callback<ConstructRule.Request, ConstructRule.Response> scb = new ServiceServer.Callback<ConstructRule.Request, ConstructRule.Response>() {
             public ConstructRule.Response call(ConstructRule.Request request) {
 		return handleConstructSparQL(request);
             }
 	};
 
 	ServiceServer<ConstructRule.Request, ConstructRule.Response, ConstructRule> srv = nodeHandle.advertiseService( "construct_rule" , new ConstructRule(), scb);
     }
 
    private SparQLRule.Response handleSparQLRule(SparQLRule.Request req)
    {
	SparQLRule.Response re = new SparQLRule.Response();
	String queryString = req.sparQLRule;
	System.out.println(queryString);
	re.result = ontoDB.executeSparQLRule(queryString);
	return re;
    }

    private void initSparQLRule() throws RosException
    {
	ServiceServer.Callback<SparQLRule.Request, SparQLRule.Response> scb = new ServiceServer.Callback<SparQLRule.Request, SparQLRule.Response>() {
            public SparQLRule.Response call(SparQLRule.Request request) {
		return handleSparQLRule(request);
            }
	};

	ServiceServer<SparQLRule.Request, SparQLRule.Response, SparQLRule> srv = nodeHandle.advertiseService( sparQLRuleService , new SparQLRule(), scb);
    }

 
     private String defaultContextPath()
     {
 	/*
 	  this.confPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();	
 	  this.confPath = this.confPath + "../conf/";
 	  return this.confPath;
	*/	 
 	return this.confPath;
     }
     
     public String getContextPath()
     {
 	return this.confPath;
     }
 
     public static void main(String[] args)
     {
 	String configFile = new String();
 	String pkgName = "";
 	String pkgPath = "";
 
 	if(args.length == 2) {
 	    pkgName = args[0];
 	    configFile = args[1];
 	    String[] cmds = {"rospack", "find", pkgName};
 	    try{
 		Runtime rt = Runtime.getRuntime();
 		Process res = rt.exec(cmds);
 		InputStream is = res.getInputStream();
 		InputStreamReader isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 
 		pkgPath = br.readLine();
 		if(pkgPath == null) {
 		    System.out.println("Package Path is Null. Check package name  ");
 		}
 		else {
 		    configFile = pkgPath + "/conf/" + configFile;
 		    System.out.println("Load from: " + configFile);
 		}
 	    }
 	    catch(Throwable t) {
 		t.printStackTrace();
 	    }
 	}
 	else {
 	    System.out.println("rosrun knowledge_server pkg_name config_file");
 	    System.exit(-1);
 	}
 
 	Properties conf = new Properties();
 	KnowledgeServer knowEng = new KnowledgeServer(pkgPath);
 
 	if (knowEng.init(configFile)) {
 	    System.out.println("OK");
 	
 	}
 	else {
 	    System.out.println("Something wrong with initialisation");
 	}	
     }
 
     private String nodeName;
     private Properties config;
     private String querySparQLService = "query_sparql";
     private String sparQLRuleService = "sparql_rule";
     private String confPath;
 }
