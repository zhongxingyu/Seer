 package controllers;
 
 /**
  * Copyright (C) <2011>
  * 
  * @author Jonas Ruef & Felix Langenegger <cis.unibe.ch@gmail.com>
  * @license GPLv3, for more informations see Readme.mdown
  */
 
 import helpers.Timer;
 
 import java.util.ArrayList;
 
 import models.Renderer;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import play.libs.Mail;
 import play.mvc.Controller;
 import algorithms.cna.CNAException;
 import algorithms.cna.CNAlgorithm;
 import datastructures.cna.CNAList;
 import datastructures.cna.CNATable;
 import datastructures.graph.Graph;
 import datastructures.mt.MinimalTheory;
 import datastructures.mt.MinimalTheorySet;
 import datastructures.parser.MTSetToTable;
 import datastructures.random.RandomMTGeneratorHelper;
 import datastructures.random.RandomMTSetGenerator;
 
 public class CNAController extends Controller {
 
     private static Timer timer;
     private static boolean showBundleNumRenderer;
     private static MinimalTheorySet theories;
     private static boolean makeEpi;
 
     static final int NUMFACTORS = 9;
     static final String MAILTo = "cis.unibe@arcadeweb.ch";
     static final String MAILFrom = MAILTo;
 
     public static void setup() {
 	render();
     }
 
     public static void prepare(String layers) {
 	int i = Integer.parseInt(layers);
 	render(i);
     }
 
     public static void generateGraph(ArrayList<Integer> bundles1,
 	    ArrayList<Integer> bundles2, ArrayList<Integer> bundles3,
 	    ArrayList<Integer> alterFactors, String epi, String showBundleNum)
 	    throws CNAException {
 	try {
 	    showBundleNumRenderer = (showBundleNum != null);
 	    makeEpi = (epi != null);
 	    RandomMTSetGenerator generator;
 	    ArrayList<ArrayList<Integer>> list;
 	    RandomMTGeneratorHelper input;
 
 	    list = new ArrayList<ArrayList<Integer>>();
 	    list.add(bundles1);
 	    list.add(bundles2);
 	    list.add(bundles3);
 
 	    input = new RandomMTGeneratorHelper(list, alterFactors, makeEpi);
 	    generator = new RandomMTSetGenerator(input.getCompleteList(),
 		    makeEpi);
 	    theories = generator.getMTSet();
 
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    String generatedGraphSource = renderer.getImageSource();
 	    String generatedGraphString = theories.toString();
 	    boolean calc = (theories.getAllNames().size() <= NUMFACTORS);
 	    if (!calc) {
		int allowed = NUMFACTORS -1;
		flash.error("Only up to " + allowed + " factors allowed.");
 		params.flash();
 	    }
 	    MTSetToTable parser = new MTSetToTable(theories);
 	    CNATable table = parser.getCoincTable();
 	    String coincTable = table.toString();
 	    render(calc, generatedGraphSource, generatedGraphString, coincTable);
 	} catch (CNAException e) {
 	    flash.error(e.toString());
 	    params.flash();
 	    setup();
 	} catch (IllegalArgumentException e) {
 	    flash.error("All minimal theories have zero factors. Please specifiy the number of factors and bundles.");
 	    params.flash();
 	    setup();
 	} catch (IndexOutOfBoundsException e) {
 	    try {
 		SimpleEmail email = new SimpleEmail();
 		email.setFrom(MAILFrom);
 		email.addTo(MAILTo);
 		email.setSubject("Error: IndexOutOfBoundsException");
 		String msg = e.getStackTrace().toString();
 		email.setMsg("CNA Random Gen\n" + msg);
 		Mail.send(email);
 	    } catch (EmailException e1) {
 		e1.printStackTrace();
 	    }
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	} catch (OutOfMemoryError e) {
 	    flash.error("Server is out of memory, please wait a minute.");
 	    params.flash();
 	    setup();
 	}
     }
 
     public static void calcCNAGraph(String generatedGraphSource,
 	    String generatedGraphString, String coincTable) {
 	try {
 	    timer = new Timer();
 	    MTSetToTable parser = new MTSetToTable(theories);
 	    CNATable table = parser.getCoincTable();
 	    CNAlgorithm cnaAlgorithm = new CNAlgorithm(table);
 	    ArrayList<String> graphsView = new ArrayList<String>();
 
 	    for (MinimalTheorySet set : cnaAlgorithm.getSets()) {
 		Graph graph = new Graph(set);
 		Renderer renderer = new Renderer();
 		renderer.setShowEdgeLabels(showBundleNumRenderer);
 		renderer.config(graph);
 		graphsView.add(renderer.getImageSource());
 		graphsView.add(set.toString());
 	    }
 
 	    String elapsedTime = timer.timeElapsed() + " ms";
 	    boolean specialcase = false;
 	    render(elapsedTime, graphsView, generatedGraphSource,
 		    generatedGraphString, coincTable, specialcase);
 	} catch (OutOfMemoryError e) {
 	    try {
 		ArrayList<String> graphsView = new ArrayList<String>();
 		timer = new Timer();
 		MTSetToTable parser = new MTSetToTable(theories);
 		CNATable table = parser.getCoincTable();
 		CNAlgorithm cnaAlgorithm = new CNAlgorithm(table);
 
 		ArrayList<MinimalTheory> theories = cnaAlgorithm
 			.getAllTheories();
 		for (MinimalTheory theory : theories) {
 		    graphsView.add(theory.toString());
 		}
 		if (graphsView.size() < 1) {
 		    flash.error("It was not possible to calculate a graph.");
 		    params.flash();
 		    setup();
 		}
 
 		String elapsedTime = timer.timeElapsed() + " ms";
 		boolean specialcase = true;
 
 		render(elapsedTime, graphsView, generatedGraphSource,
 			generatedGraphString, coincTable, specialcase);
 	    } catch (CNAException e1) {
 		flash.error(e1.toString());
 		params.flash();
 		setup();
 	    }
 	} catch (CNAException e) {
 	    flash.error(e.toString());
 	    params.flash();
 	    setup();
 	}
     }
 
     // public static void baumgartnerSample() {
     // timer = new Timer();
     // CNAlgorithm cnaAlgorithm;
     // try {
     // cnaAlgorithm = new CNAlgorithm(
     // new BaumgartnerSampleTable().getSampleTable());
     // MinimalTheorySet theories = cnaAlgorithm.getSets().get(0);
     //
     // Graph graph = new Graph(theories);
     // Renderer renderer = new Renderer();
     // renderer.setShowEdgeLabels(showBundleNumRenderer);
     // renderer.config(graph);
     // String graphPath = renderer.getImageSource();
     // String graphString = theories.toString();
     //
     // String elapsedTime = timer.timeElapsed() + " ms";
     //
     // render(elapsedTime, graphPath, graphString);
     // } catch (CNAException e) {
     // flash.error(e.toString());
     // params.flash();
     // setup();
     // } catch (OutOfMemoryError e) {
     // flash.error("Server is out of memory, please wait a minute.");
     // params.flash();
     // setup();
     // }
     // }
 
     public static void inputTable(String table) {
 	CNATable cnatable = new CNATable("\r\n", ",", table);
 
 	if (cnatable.get(0).size() >= NUMFACTORS) {
 	    flash.error("Only up to " + NUMFACTORS + " factors allowed.");
 	    params.flash();
 	    setup();
 	} else if (cnatable.get(0).size() < 3) {
 	    flash.error("Violation of Minimal Diversity pre-condition: Every MT must have at least two bundles, alternate factors, or a bundle and a alternate factor.");
 	    params.flash();
 	    setup();
 	}
 	try {
 	    ArrayList<String> graphsView = new ArrayList<String>();
 	    timer = new Timer();
 	    CNAlgorithm cnaAlgorithm = new CNAlgorithm(cnatable);
 	    for (MinimalTheorySet set : cnaAlgorithm.getSets()) {
 		Graph graph = new Graph(set);
 		Renderer renderer = new Renderer();
 		renderer.setShowEdgeLabels(showBundleNumRenderer);
 		renderer.config(graph);
 		graphsView.add(renderer.getImageSource());
 		graphsView.add(set.toString());
 	    }
 
 	    String elapsedTime = timer.timeElapsed() + " ms";
 	    boolean specialcase = false;
 	    render(elapsedTime, graphsView, specialcase);
 	} catch (OutOfMemoryError e) {
 	    try {
 		ArrayList<String> graphsView = new ArrayList<String>();
 		timer = new Timer();
 		CNAlgorithm cnaAlgorithm = new CNAlgorithm(cnatable);
 
 		ArrayList<MinimalTheory> theories = cnaAlgorithm
 			.getAllTheories();
 		for (MinimalTheory theory : theories) {
 		    graphsView.add(theory.toString());
 		}
 		if (graphsView.size() < 1) {
 		    flash.error("It was not possible to calculate a graph.");
 		    params.flash();
 		    setup();
 		}
 
 		String elapsedTime = timer.timeElapsed() + " ms";
 		boolean specialcase = true;
 		render(elapsedTime, graphsView, specialcase);
 	    } catch (CNAException e1) {
 		flash.error(e1.toString());
 		params.flash();
 		setup();
 	    }
 	} catch (CNAException e) {
 	    flash.error(e.toString());
 	    params.flash();
 	    setup();
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    try {
 		SimpleEmail email = new SimpleEmail();
 		email.setFrom(MAILFrom);
 		email.addTo(MAILTo);
 		email.setSubject("Error: IndexOutOfBoundsException");
 		String msg = e.getStackTrace().toString();
 		email.setMsg("CNA Input Table\n" + msg);
 		Mail.send(email);
 	    } catch (EmailException e1) {
 		e1.printStackTrace();
 	    }
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	} catch (IndexOutOfBoundsException e) {
 	    try {
 		SimpleEmail email = new SimpleEmail();
 		email.setFrom(MAILFrom);
 		email.addTo(MAILTo);
 		email.setSubject("Error: IndexOutOfBoundsException");
 		String msg = e.getStackTrace().toString();
 		email.setMsg("CNA Input Table\n" + msg);
 		Mail.send(email);
 	    } catch (EmailException e1) {
 		e1.printStackTrace();
 	    }
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	} catch (IllegalArgumentException e) {
 	    try {
 		SimpleEmail email = new SimpleEmail();
 		email.setFrom(MAILFrom);
 		email.addTo(MAILTo);
 		email.setSubject("Error: IllegalArgumentException");
 		String msg = e.getStackTrace().toString();
 		email.setMsg("CNA Input Table\n" + msg);
 		Mail.send(email);
 	    } catch (EmailException e1) {
 		e1.printStackTrace();
 	    }
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	}
     }
 
     public static void inputMT(String mtset) {
 	try {
 	    CNAList list = new CNAList("\r\n", mtset);
 	    CNAList factors;
 	    theories = new MinimalTheorySet();
 	    MinimalTheory theorie;
 	    for (String str : list) {
 		factors = new CNAList();
 		String[] array = str.split("=>");
 		String[] fac = array[0].split("v");
 		for (int i = 0; i < fac.length; i++) {
 		    factors.add(fac[i]);
 		}
 		if (array[1].length() > 1) {
 		    flash.error("Please insert as effect only a positive and only one factor.");
 		    params.flash();
 		    setup();
 		}
 		theorie = new MinimalTheory(factors, array[1]);
 		theories.add(theorie);
 	    }
 	    for (MinimalTheory theory : theories) {
 		if (theory.getBundleFactors().size() < 2) {
 		    flash.error("Violation of Minimal Diversity pre-condition: Every MT must have at least two bundles, alternate factors, or a bundle and a alternate factor.");
 		    params.flash();
 		    setup();
 		}
 	    }
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    String generatedGraphSource = renderer.getImageSource();
 	    String generatedGraphString = theories.toString();
 	    boolean calc = (theories.getAllNames().size() <= NUMFACTORS);
 	    if (!calc) {
 		flash.error("Only up to " + NUMFACTORS + " factors allowed.");
 		params.flash();
 	    }
 	    render(generatedGraphSource, generatedGraphString, calc);
 	} catch (OutOfMemoryError e) {
 	    flash.error("Server is out of memory, please wait a minute.");
 	    params.flash();
 	    setup();
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    flash.error("You're input is not according to our syntax. Please correct it.");
 	    params.flash();
 	    setup();
 	} catch (IndexOutOfBoundsException e) {
 	    try {
 		SimpleEmail email = new SimpleEmail();
 		email.setFrom(MAILFrom);
 		email.addTo(MAILTo);
 		email.setSubject("Error: IllegalArgumentException");
 		String msg = e.getStackTrace().toString();
 		email.setMsg("CNA Input MT\n" + msg);
 		Mail.send(email);
 	    } catch (EmailException e1) {
 		e1.printStackTrace();
 	    }
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	} catch (IllegalArgumentException e) {
 	    try {
 		SimpleEmail email = new SimpleEmail();
 		email.setFrom(MAILFrom);
 		email.addTo(MAILFrom);
 		email.setSubject("Error: IllegalArgumentException");
 		String msg = e.getStackTrace().toString();
 		email.setMsg("CNA Input MT\n" + msg);
 		Mail.send(email);
 	    } catch (EmailException e1) {
 		e1.printStackTrace();
 	    }
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	}
     }
 }
