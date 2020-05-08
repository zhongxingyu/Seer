 package pathfind;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class GeneratorGraph {
 	
 	private Graph graph;
 	private ArrayList<Node> nodes;
 	
 	/**
 	 * Constructor for GeneratorGraph
 	 * @param n the users nodes such as special location in rooms
 	 */
 	public GeneratorGraph(ArrayList<Node> n) {
 		graph = new Graph();
 		nodes = n;
 	}
 	
 	//TODO : doubler les points de gnration afin d'avoir deux "couloirs"
 	//TODO : Right = gauche et Left = droite
 	/**
 	 * generate the graph so we can calculate the orders to send
 	 * @return the generated graph
 	 */
 	public Graph generatateGraph() {
 		
 		//generation initiale
 		// TODO : pas generer les points qui sont en type exterieur
 		// TODO : creer un truc pour tourner par rapport au bout du mur
 		double epaisseurE;
 		double epaisseurI;
 		
 		Path chemin;
 		Wall autreWall;
 		double[] normale;
 		double[] director;
 		double[] coord = new double[2];
 		
 		Node nodeCourant1D;
 		Node nodeCourant1G;
 		Node nodeCourant2D;
 		Node nodeCourant2G;
 		
 		Node nodeNew1D;
 		Node nodeNew1G;
 		Node nodeNew2D;
 		Node nodeNew2G;
 		
 		//ecartementMoyen - ecartementVoie > 0;
		double ecartementMoyen = 30;
		double ecartementVoie = 5;
		double largeurPorte = 5;
 		
 		HashMap<Wall, murEtBout> mapAdjacenceWall = this.mapWallAdjacents();
 		
 		for (Wall m : affichage.Bipbip.walls) {
 			normale = m.getNormal();
 			epaisseurE = m.getWidth() + ecartementMoyen + ecartementVoie;
 			epaisseurI = m.getWidth() + ecartementMoyen - ecartementVoie;
 			
 			if (mapAdjacenceWall.get(m).getWallVecteur()[0]==null) {
 				//Cas bout de mur seul.
 				coord[0] = m.getCornerStart().getAbscissa() + normale[0]*epaisseurE;
 				coord[1] = m.getCornerStart().getOrdinate() + normale[1]*epaisseurE;
 				nodeCourant1D = new Node(coord[0], coord[1], m.getName() + "D1", m.getRoomDirect(Side.Left));
 				graph.addNode(nodeCourant1D);
 				
 				coord[0] = m.getCornerStart().getAbscissa() + normale[0]*epaisseurI;
 				coord[1] = m.getCornerStart().getOrdinate() + normale[1]*epaisseurI;
 				nodeCourant1G = new Node(coord[0], coord[1], m.getName() + "D1", m.getRoomDirect(Side.Right));
 				graph.addNode(nodeCourant1G);
 				
 				coord[0] = m.getCornerStart().getAbscissa() - normale[0]*epaisseurI;
 				coord[1] = m.getCornerStart().getOrdinate() - normale[1]*epaisseurI;
 				nodeCourant2D = new Node(coord[0], coord[1], m.getName() + "D2", m.getRoomIndirect(Side.Right));
 				graph.addNode(nodeCourant2D);
 				
 				coord[0] = m.getCornerStart().getAbscissa() - normale[0]*epaisseurE;
 				coord[1] = m.getCornerStart().getOrdinate() - normale[1]*epaisseurE;
 				nodeCourant2G = new Node(coord[0], coord[1], m.getName() + "D2", m.getRoomIndirect(Side.Left));
 				graph.addNode(nodeCourant2G);
 				
 				//A tester : croisement !
 				chemin = new Path();
 				chemin.addStep(nodeCourant2G);
 				chemin.addStep(nodeCourant1D);
 				graph.addArc(new Arc(nodeCourant2G, nodeCourant1D, chemin));
 				
 				chemin = new Path();
 				chemin.addStep(nodeCourant1G);
 				chemin.addStep(nodeCourant2D);
 				graph.addArc(new Arc(nodeCourant1G, nodeCourant2D, chemin));
 				
 			} else {
 				//cas plusieurs murs
 				
 				autreWall = mapAdjacenceWall.get(m).getWallVecteur()[0];
 
 				coord = m.getIntersection(autreWall, true, !mapAdjacenceWall.get(m).getWallBout()[0], ecartementMoyen + ecartementVoie);
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerStart().getAbscissa() + normale[0]*epaisseurE;
 					coord[1] = m.getCornerStart().getOrdinate() + normale[1]*epaisseurE;
 				}
 				nodeCourant1D = graph.searchNode(coord[0], coord[1]);
 				if (nodeCourant1D==null) {
 					nodeCourant1D = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomDirect(Side.Left));
 					graph.addNode(nodeCourant1D);
 				}
 				
 				coord = m.getIntersection(autreWall, true, !mapAdjacenceWall.get(m).getWallBout()[0], ecartementMoyen - ecartementVoie);
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerStart().getAbscissa() + normale[0]*epaisseurI;
 					coord[1] = m.getCornerStart().getOrdinate() + normale[1]*epaisseurI;
 				}
 				nodeCourant1G = graph.searchNode(coord[0], coord[1]);
 				if (nodeCourant1G==null) {
 					nodeCourant1G = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomDirect(Side.Right));
 					graph.addNode(nodeCourant1G);
 				}
 
 
 				autreWall = mapAdjacenceWall.get(m).getWallVecteur()[1];
 				
 				coord = m.getIntersection(autreWall, false, mapAdjacenceWall.get(m).getWallBout()[1], ecartementMoyen - ecartementVoie);			
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerStart().getAbscissa() - normale[0]*epaisseurI;
 					coord[1] = m.getCornerStart().getOrdinate() - normale[1]*epaisseurI;
 				}
 				nodeCourant2D = graph.searchNode(coord[0], coord[1]);
 				if (nodeCourant2D==null) {
 					nodeCourant2D = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomIndirect(Side.Right));
 					graph.addNode(nodeCourant2D);
 				}
 				
 				coord = m.getIntersection(autreWall, false, mapAdjacenceWall.get(m).getWallBout()[1], ecartementMoyen + ecartementVoie);			
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerStart().getAbscissa() - normale[0]*epaisseurE;
 					coord[1] = m.getCornerStart().getOrdinate() - normale[1]*epaisseurE;
 				}
 				nodeCourant2G = graph.searchNode(coord[0], coord[1]);
 				if (nodeCourant2G==null) {
 					nodeCourant2G = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomIndirect(Side.Left));
 					graph.addNode(nodeCourant2G);
 				}
 			}
 
 
 			//TODO : GERER DROITE/GAUCHE
 			if (m.getDoors()!=null) {
 				director = m.getDirector();
 				for (Node porte : m.getDoors()) {
 					coord[0] = porte.getAbscissa() + normale[0]*epaisseurE + director[0]*largeurPorte;
 					coord[1] = porte.getOrdinate() + normale[1]*epaisseurE + director[1]*largeurPorte;
 					nodeNew1D = new Node(coord[0], coord[1], porte.getName() + "1", m.getRoomDirect(Side.Left));
 					graph.addNode(nodeNew1D);
 					
 					coord[0] = porte.getAbscissa() + normale[0]*epaisseurI - director[0]*largeurPorte;
 					coord[1] = porte.getOrdinate() + normale[1]*epaisseurI - director[1]*largeurPorte;
 					nodeNew1G = new Node(coord[0], coord[1], porte.getName() + "1", m.getRoomDirect(Side.Right));
 					graph.addNode(nodeNew1G);
 
 					coord[0] = porte.getAbscissa() - normale[0]*epaisseurI - director[0]*largeurPorte;
 					coord[1] = porte.getOrdinate() - normale[1]*epaisseurI - director[1]*largeurPorte;
 					nodeNew2D = new Node(coord[0], coord[1], porte.getName() + "2", m.getRoomIndirect(Side.Right));
 					graph.addNode(nodeNew2D);
 					
 					coord[0] = porte.getAbscissa() - normale[0]*epaisseurE + director[0]*largeurPorte;
 					coord[1] = porte.getOrdinate() - normale[1]*epaisseurE + director[1]*largeurPorte;
 					nodeNew2G = new Node(coord[0], coord[1], porte.getName() + "2", m.getRoomIndirect(Side.Left));
 					graph.addNode(nodeNew2G);
 					
 					//demi-tour possible
 					chemin = new Path();
 					chemin.addStep(nodeNew1G);
 					chemin.addStep(nodeNew1D);
 					graph.addArc(new Arc(nodeNew1G, nodeNew1D, chemin));
 					
 					chemin = new Path();
 					chemin.addStep(nodeNew2G);
 					chemin.addStep(nodeNew2D);
 					graph.addArc(new Arc(nodeNew2G, nodeNew2D, chemin));
 					
 					//passer la porter
 					chemin = new Path();
 					chemin.addStep(nodeNew1D);
 					chemin.addStep(nodeNew2G);
 					graph.addArc(new Arc(nodeNew1D, nodeNew2G, chemin));
 					
 					chemin = new Path();
 					chemin.addStep(nodeNew2D);
 					chemin.addStep(nodeNew1G);
 					graph.addArc(new Arc(nodeNew2D, nodeNew1G, chemin));
 
 					//relier au reste du mur
 					chemin = new Path();
 					chemin.addStep(nodeCourant1D);
 					chemin.addStep(nodeNew1D);
 					graph.addArc(new Arc(nodeCourant1D, nodeNew1D, chemin));
 					
 					chemin = new Path();
 					chemin.addStep(nodeNew1G);
 					chemin.addStep(nodeCourant1G);
 					graph.addArc(new Arc(nodeNew1G, nodeCourant1G, chemin));
 
 					chemin = new Path();
 					chemin.addStep(nodeCourant2D);
 					chemin.addStep(nodeNew2D);
 					graph.addArc(new Arc(nodeCourant2D, nodeNew2D, chemin));
 					
 					chemin = new Path();
 					chemin.addStep(nodeNew2G);
 					chemin.addStep(nodeCourant2G);
 					graph.addArc(new Arc(nodeNew2G, nodeCourant2G, chemin));
 
 					nodeCourant1D = nodeNew1D;
 					nodeCourant1G = nodeNew1G;
 					nodeCourant2D = nodeNew2D;
 					nodeCourant2G = nodeNew2G;
 				}
 			}
 			
 			
 			if (mapAdjacenceWall.get(m).getWallVecteur()[2]==null) {
 				//Cas bout de mur seul.
 				coord[0] = m.getCornerEnd().getAbscissa() + normale[0]*epaisseurE;
 				coord[1] = m.getCornerEnd().getOrdinate() + normale[1]*epaisseurE;
 				nodeNew1D = new Node(coord[0], coord[1], m.getName() + "F1", m.getRoomDirect(Side.Left));
 				graph.addNode(nodeNew1D);
 				
 				coord[0] = m.getCornerEnd().getAbscissa() + normale[0]*epaisseurI;
 				coord[1] = m.getCornerEnd().getOrdinate() + normale[1]*epaisseurI;
 				nodeNew1G = new Node(coord[0], coord[1], m.getName() + "F1", m.getRoomDirect(Side.Right));
 				graph.addNode(nodeNew1G);
 				
 				coord[0] = m.getCornerEnd().getAbscissa() - normale[0]*epaisseurI;
 				coord[1] = m.getCornerEnd().getOrdinate() - normale[1]*epaisseurI;
 				nodeNew2D = new Node(coord[0], coord[1], m.getName() + "F2", m.getRoomIndirect(Side.Right));
 				graph.addNode(nodeNew2D);
 				
 				coord[0] = m.getCornerEnd().getAbscissa() - normale[0]*epaisseurE;
 				coord[1] = m.getCornerEnd().getOrdinate() - normale[1]*epaisseurE;
 				nodeNew2G = new Node(coord[0], coord[1], m.getName() + "F2", m.getRoomIndirect(Side.Left));
 				graph.addNode(nodeNew2G);
 				
 				chemin = new Path();
 				chemin.addStep(nodeNew1D);
 				chemin.addStep(nodeNew2G);
 				graph.addArc(new Arc(nodeNew1D, nodeNew2G, chemin));
 				
 				chemin = new Path();
 				chemin.addStep(nodeNew1G);
 				chemin.addStep(nodeNew2D);
 				graph.addArc(new Arc(nodeNew1G, nodeNew2D, chemin));
 					
 			} else {
 				
 				autreWall = mapAdjacenceWall.get(m).getWallVecteur()[2];
 
 				coord = m.getIntersection(autreWall, true, mapAdjacenceWall.get(m).getWallBout()[2], ecartementMoyen + ecartementVoie);
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerEnd().getAbscissa() + normale[0]*epaisseurE;
 					coord[1] = m.getCornerEnd().getOrdinate() + normale[1]*epaisseurE;
 				}
 				nodeNew1D = graph.searchNode(coord[0], coord[1]);
 				if (nodeNew1D==null) {
 					nodeNew1D = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomDirect(Side.Left));
 					graph.addNode(nodeNew1D);
 				}
 				
 				coord = m.getIntersection(autreWall, true, mapAdjacenceWall.get(m).getWallBout()[2], ecartementMoyen - ecartementVoie);
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerEnd().getAbscissa() + normale[0]*epaisseurI;
 					coord[1] = m.getCornerEnd().getOrdinate() + normale[1]*epaisseurI;
 				}
 				nodeNew1G = graph.searchNode(coord[0], coord[1]);
 				if (nodeNew1G==null) {
 					nodeNew1G = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomDirect(Side.Right));
 					graph.addNode(nodeNew1G);
 				}
 
 				
 				autreWall = mapAdjacenceWall.get(m).getWallVecteur()[3];
 
 				coord = m.getIntersection(autreWall, false, !mapAdjacenceWall.get(m).getWallBout()[3], ecartementMoyen - ecartementVoie);
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerEnd().getAbscissa() - normale[0]*epaisseurI;
 					coord[1] = m.getCornerEnd().getOrdinate() - normale[1]*epaisseurI;
 				}
 				nodeNew2D = graph.searchNode(coord[0], coord[1]);
 				if (nodeNew2D==null) {
 					nodeNew2D = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomIndirect(Side.Right));
 					graph.addNode(nodeNew2D);
 				}
 
 				coord = m.getIntersection(autreWall, false, !mapAdjacenceWall.get(m).getWallBout()[3], ecartementMoyen + ecartementVoie);
 				if (coord==null) {
 					coord = new double[2];
 					coord[0] = m.getCornerEnd().getAbscissa() - normale[0]*epaisseurE;
 					coord[1] = m.getCornerEnd().getOrdinate() - normale[1]*epaisseurE;
 				}
 				nodeNew2G = graph.searchNode(coord[0], coord[1]);
 				if (nodeNew2G==null) {
 					nodeNew2G = new Node(coord[0], coord[1], m.getName() + "|" + autreWall.getName(), m.getRoomIndirect(Side.Left));
 					graph.addNode(nodeNew2G);
 				}
 			}
 			
 			chemin = new Path();
 			chemin.addStep(nodeCourant1D);
 			chemin.addStep(nodeNew1D);
 			graph.addArc(new Arc(nodeCourant1D, nodeNew1D, chemin));
 			
 			chemin = new Path();
 			chemin.addStep(nodeNew1G);
 			chemin.addStep(nodeCourant1G);
 			graph.addArc(new Arc(nodeNew1G, nodeCourant1G, chemin));
 			
 			chemin = new Path();
 			chemin.addStep(nodeCourant2D);
 			chemin.addStep(nodeNew2D);
 			graph.addArc(new Arc(nodeCourant2D, nodeNew2D, chemin));
 			
 			chemin = new Path();
 			chemin.addStep(nodeNew2G);
 			chemin.addStep(nodeCourant2G);
 			graph.addArc(new Arc(nodeNew2G, nodeCourant2G, chemin));
 			
 		}
 
 		//generation finale
 		while ((nodes!=null)&&(!nodes.isEmpty())) {
 			Node n = nodes.get(0);
 			graph.addNode(n);
 			//premire idee : tous les nodes d'une mme pice sont fortement connexes.
 			if (n.getType() instanceof TypePiece) {
 				for (Node autreNode : graph.getNodes()) {
 					//TODO : HT1 : pices vides
 					if (n.getType().getId()==autreNode.getType().getId()
 							&&!n.equals(autreNode)) {
 						
 						chemin = new Path();
 						chemin.addStep(n);
 						chemin.addStep(autreNode);
 						Arc a = new Arc(n, autreNode, chemin);
 						graph.addArc(a);
 						
 						chemin = new Path();
 						chemin.addStep(autreNode);
 						chemin.addStep(n);
 						a = new Arc(autreNode, n, chemin);
 						graph.addArc(a);
 					}
 				}
 			} else if (n.getType() instanceof TypeCouloir) {
 				//passages pietons
 				/*
 				 * Premire idee basique : placer les passage
 				 * a la main, puis les relier a tous les trucs a proximite
 				 */
 				for (Node autreNode : graph.getNodes()) {
 					if (n.getType().getId()==autreNode.getType().getId()
 							&&!n.equals(autreNode)
 							&&n.calculateDistance(autreNode)<70) {
 						chemin = new Path();
 						chemin.addStep(n);
 						chemin.addStep(autreNode);
 						Arc a = new Arc(n, autreNode, chemin);
 						graph.addArc(a);
 					}
 				}
 			}
 			nodes.remove(0);
 		}
 		return graph;
 	}
 
 	public Graph getGraph() {
 		return graph;
 	}
 
 
 	private HashMap<Wall, murEtBout> mapWallAdjacents() {
 		HashMap<Wall, murEtBout> hm = new HashMap<Wall, murEtBout>();
 		Wall[] murVecteur;
 		boolean[] murBout;
 		murEtBout meb;
 		ArrayList<Wall> murListDebut = new ArrayList<Wall>();
 		ArrayList<Wall> murListFin = new ArrayList<Wall>();
 		Wall murMin;
 		Wall murMax;
 		double ecartMin = 0;
 		double ecartMax = 0;
 
 		for (Wall m : affichage.Bipbip.walls) {
 			murVecteur = new Wall[4];
 			murBout = new boolean[4];
 			murListDebut.clear();
 			murListFin.clear();
 			//rechercher les murs touchant
 			for (Wall autreM : affichage.Bipbip.walls) {
 				if((m.getCornerStart().equals(autreM.getCornerStart())
 						||m.getCornerStart().equals(autreM.getCornerEnd()))
 						&&!m.equals(autreM)) {
 					murListDebut.add(autreM);
 				}
 				if((m.getCornerEnd().equals(autreM.getCornerStart())
 						||m.getCornerEnd().equals(autreM.getCornerEnd()))
 						&&!m.equals(autreM)) {
 					murListFin.add(autreM);
 				}
 			}
 			
 			//rechercher parmis les murs touchant ceux qui sont a cote
 			murMin = null;
 			murMax = null;
 			for (Wall autreM : murListDebut) {
 				if (murMin==null) {
 					if (autreM.getCornerStart().equals(m.getCornerStart())) {
 						ecartMin = m.calculateAngle(autreM);
 					} else if (m.calculateAngle(autreM) - Math.PI >= 0) {
 						ecartMin = m.calculateAngle(autreM) - Math.PI;
 					} else {
 						ecartMin = m.calculateAngle(autreM) + Math.PI;
 					}
 					murMin = autreM;
 					
 				} else if (autreM.getCornerStart().equals(m.getCornerStart())
 						&&m.calculateAngle(autreM) < ecartMin) {
 					ecartMin = m.calculateAngle(autreM);
 					murMin = autreM;
 					
 				} else if (autreM.getCornerEnd().equals(m.getCornerStart())
 						&&m.calculateAngle(autreM) - Math.PI < ecartMin
 						&&m.calculateAngle(autreM) - Math.PI >= 0) {
 					ecartMin = m.calculateAngle(autreM) - Math.PI;
 					murMin = autreM;
 				}
 
 				if (murMax==null) {
 					if (autreM.getCornerStart().equals(m.getCornerStart())) {
 						ecartMax = m.calculateAngle(autreM);
 					} else if (m.calculateAngle(autreM) + Math.PI < 2*Math.PI) {
 						ecartMax = m.calculateAngle(autreM) + Math.PI;
 					} else {
 						ecartMax = m.calculateAngle(autreM) - Math.PI;
 					}
 					murMax = autreM;
 					
 				} else if (autreM.getCornerStart().equals(m.getCornerStart())
 						&&m.calculateAngle(autreM) > ecartMax) {
 					ecartMax = m.calculateAngle(autreM);
 					murMax = autreM;
 					
 				} else if (autreM.getCornerEnd().equals(m.getCornerStart())
 						&&m.calculateAngle(autreM) + Math.PI > ecartMax
 						&&m.calculateAngle(autreM) + Math.PI < 2*Math.PI) {
 					ecartMax = m.calculateAngle(autreM) + Math.PI;
 					murMax = autreM;
 				}
 
 			}
 
 			murVecteur[1] = murMin;
 			if (murMin!=null) {
 				murBout[1] = murMin.getCornerStart().equals(m.getCornerStart());
 			}
 
 			murVecteur[0] = murMax;
 			if (murMax!=null) {
 				murBout[0] = murMax.getCornerStart().equals(m.getCornerStart());
 			}
 
 			murMin = null;
 			murMax = null;
 			for (Wall autreM : murListFin) {
 				if (murMin==null) {
 					if (!autreM.getCornerStart().equals(m.getCornerEnd())) {
 						ecartMin = m.calculateAngle(autreM);
 					} else if (m.calculateAngle(autreM) + Math.PI < 2*Math.PI) {
 						ecartMin = m.calculateAngle(autreM) + Math.PI;
 					} else {
 						ecartMin = m.calculateAngle(autreM) - Math.PI;
 					}
 					murMin = autreM;
 					
 				} else if (autreM.getCornerStart().equals(m.getCornerEnd())
 						&&m.calculateAngle(autreM) + Math.PI > ecartMin
 						&&m.calculateAngle(autreM) + Math.PI < 2*Math.PI) {
 					ecartMin = m.calculateAngle(autreM) + Math.PI;
 					murMin = autreM;
 					
 				} else if (autreM.getCornerEnd().equals(m.getCornerEnd())
 						&&m.calculateAngle(autreM) > ecartMin) {
 					ecartMin = m.calculateAngle(autreM);
 					murMin = autreM;
 				}
 
 				if (murMax==null) {
 					if (!autreM.getCornerStart().equals(m.getCornerEnd())) {
 						ecartMax = m.calculateAngle(autreM);
 					} else if (m.calculateAngle(autreM) - Math.PI >= 0) {
 						ecartMax = m.calculateAngle(autreM) - Math.PI;
 					} else {
 						ecartMax = m.calculateAngle(autreM) + Math.PI;
 					}
 					murMax = autreM;
 					
 				} else if (autreM.getCornerStart().equals(m.getCornerEnd())
 						&&m.calculateAngle(autreM) - Math.PI < ecartMax
 						&&m.calculateAngle(autreM) - Math.PI >= 0) {
 					ecartMax = m.calculateAngle(autreM) - Math.PI;
 					murMax = autreM;
 					
 				} else if (autreM.getCornerEnd().equals(m.getCornerEnd())
 						&&(m.calculateAngle(autreM) < ecartMax || murMax==null)) {
 					ecartMax = m.calculateAngle(autreM);
 					murMax = autreM;
 				}
 
 			}
 
 			murVecteur[3] = murMin;
 			if (murMin!=null) {
 				murBout[3] = murMin.getCornerStart().equals(m.getCornerEnd());
 			}
 
 			murVecteur[2] = murMax;
 			if (murMax!=null) {
 				murBout[2] = murMax.getCornerStart().equals(m.getCornerEnd());
 			}
 
 			meb = new murEtBout(murVecteur, murBout);
 			hm.put(m, meb);
 		}
 		
 		return hm;
 	}
 	
 	//Class permettant juste de faire rentrer le bordel dans la hashmap
 	private class murEtBout {
 		
 		private Wall[] murVecteur;
 		private boolean[] murBout;
 		
 		public murEtBout(Wall[] mv, boolean[] mb) {
 			murVecteur = mv;
 			murBout = mb;
 		}
 
 		public Wall[] getWallVecteur() {
 			return murVecteur;
 		}
 
 		public boolean[] getWallBout() {
 			return murBout;
 		}	
 		
 	}
 
 }
